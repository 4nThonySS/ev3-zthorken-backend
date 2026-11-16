# Zthorken Online: documento de integración

DSY1104 Desarrollo Fullstack II, Evaluación Parcial 3.

| Proyecto | Repositorio | Tecnología |
|---|---|---|
| Frontend | https://github.com/4nThonySS/ev3-zthorken-online | React 19, Vite, Bootstrap 5, React Router 7 |
| Backend | https://github.com/4nThonySS/ev3-zthorken-backend | Java 21, Spring Boot 4.1, Spring Data JPA, Spring Security, JWT (jjwt), springdoc OpenAPI |
| Base de datos | — | MySQL 8 (perfil `mysql`) o H2 en memoria (perfil `dev`) |

## 1. Arquitectura

```
Navegador (React)                      Backend (Spring Boot)                     Base de datos
-----------------                      ---------------------                     -------------
Vistas (pages/)                        JwtAuthFilter  -> valida el token
  -> useApi / api()  --- HTTP JSON -->  SecurityConfig -> permisos por rol
     (src/api.jsx)     Authorization:   Controller     -> /api/v1/...
                       Bearer <token>   Service        -> reglas de negocio    -> JPA/Hibernate -> MySQL
SesionProvider (token en localStorage)  Repository     -> Spring Data JPA
```

El frontend no guarda productos, categorías, usuarios ni órdenes: siempre los pide al backend. El backend es la única fuente de verdad y vuelve a revisar cada permiso, aunque el frontend ya oculte lo que un rol no puede usar.

## 2. Backend: base de datos, modelo y lógica de negocio (IE3.1.1)

### Conexión a la base de datos

| Perfil | Base de datos | Configuración |
|---|---|---|
| `dev` (por defecto) | H2 en memoria, se crea y llena al arrancar | `application-dev.properties` |
| `mysql` | MySQL 8; la base `zthorken` se crea si no existe | `application-mysql.properties`, variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` |

```bash
JWT_SECRET=una_clave_de_al_menos_32_caracteres DB_USER=root DB_PASSWORD=tu_clave ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Hibernate crea las tablas a partir de las entidades. `DataInitializer` carga los datos iniciales (6 categorías, 12 productos, 3 usuarios y 1 boleta) solo si la base está vacía.

### Modelo de datos

```
usuario 1 ──── * boleta 1 ──── * detalle_boleta * ──── 1 producto * ──── 1 categoria
```

| Entidad | Campos principales | Relaciones y reglas |
|---|---|---|
| `Usuario` | nombre, apellidos, correo (único), password (BCrypt), rol, dirección | Rol `ADMIN`, `VENDEDOR` o `CLIENTE` |
| `Categoria` | nombre (único), imagen | Tiene muchos productos |
| `Producto` | nombre, descripción, precio, precioOferta, stock, stockCritico, imagen | Pertenece a una categoría (obligatoria) |
| `Boleta` | fecha, estado (`PAGADA`/`ANULADA`), total, datos del cliente, `DireccionEnvio` embebida | Usuario opcional (permite comprar sin cuenta); tiene muchos detalles |
| `DetalleBoleta` | nombre y precio del producto al comprar, cantidad, subtotal | Guarda una copia del nombre y precio, así la boleta no cambia si después se edita o borra el producto |

### Lógica de negocio (capa `service`)

| Servicio | Reglas |
|---|---|
| `BoletaService` | Usa el precio vigente (la oferta, si existe), calcula subtotales y total, rechaza la compra si no hay stock suficiente y descuenta el stock en la misma transacción. Al anular una boleta devuelve el stock y no permite anularla dos veces |
| `ProductoService` | El precio de oferta debe ser menor que el precio normal; la categoría debe existir; filtra por categoría, texto y ofertas; lista los productos en stock crítico |
| `CategoriaService` | El nombre no se puede repetir; una categoría con productos no se puede eliminar |
| `UsuarioService` | Correo único y normalizado; contraseña cifrada con BCrypt; la contraseña vacía al editar mantiene la actual; al borrar un usuario sus boletas se conservan |

Los errores se devuelven siempre con el mismo formato desde `GlobalExceptionHandler`: `{ "status", "mensaje", "campos" }`, con 400 para validaciones y reglas de negocio, 401 sin sesión, 403 sin permiso y 404 si el recurso no existe.

## 3. API REST y Swagger (IE3.2.1)

Todas las rutas están versionadas bajo `/api/v1`. La documentación interactiva está en `http://localhost:8080/swagger-ui.html`. Cada endpoint tiene `@Operation` y sus `@ApiResponse`, y el botón **Authorize** permite pegar el token para probar las rutas protegidas.

| Recurso | Leer | Crear | Actualizar | Eliminar |
|---|---|---|---|---|
| Categorías | `GET /categorias`, `GET /categorias/{id}` | `POST /categorias` | `PUT /categorias/{id}` | `DELETE /categorias/{id}` |
| Productos | `GET /productos?categoriaId=&q=&oferta=`, `GET /productos/{id}`, `GET /productos/stock-critico` | `POST /productos` | `PUT /productos/{id}` | `DELETE /productos/{id}` |
| Usuarios | `GET /usuarios?rol=`, `GET /usuarios/{id}` | `POST /usuarios` | `PUT /usuarios/{id}` | `DELETE /usuarios/{id}` |
| Boletas | `GET /boletas?usuarioId=`, `GET /boletas/{id}` | `POST /boletas` | `PATCH /boletas/{id}/anular` | `DELETE /boletas/{id}` |
| Autenticación | — | `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh` | `PUT /auth/perfil` | — |

Las entradas se validan con Bean Validation (`@NotBlank`, `@Email`, `@Positive`, etc.) en los DTO de request, y las respuestas usan DTO propios, así que la contraseña nunca sale de la API.

## 4. Integración entre frontend y backend (IE3.2.2)

### Cliente HTTP único

Todas las vistas usan `src/api.jsx`:

- `api(ruta, { method, body })` arma la URL desde `VITE_API_URL`, agrega `Content-Type: application/json` y el header `Authorization: Bearer <token>` si hay sesión, y convierte la respuesta a JSON.
- Si el backend responde con error, lanza un `Error` con el mensaje del backend y los errores por campo (`error.campos`), que los formularios muestran junto a cada input.
- Si el servidor no responde, avisa «No se pudo conectar con el servidor».
- `useApi(ruta)` carga datos al montar la vista, muestra «Cargando...» o el error y entrega `recargar()` para refrescar después de crear, editar o borrar.

### Flujo de una compra (ejemplo completo)

1. El cliente arma el carrito en el navegador (`CartContext`). En Carrito se pide `GET /productos` para mostrar el stock y la imagen actuales.
2. En Checkout el formulario se valida en el navegador y se envía `POST /api/v1/boletas` solo con los ids y las cantidades, nunca con precios.
3. El backend busca cada producto, usa su precio vigente, revisa y descuenta el stock y guarda la boleta y sus detalles en una sola transacción.
4. El backend responde 201 con la boleta completa; el frontend vacía el carrito y muestra Pago exitoso con esa boleta. Si falta stock responde 400 y el frontend lleva a Pago con error con el motivo.
5. La boleta queda a nombre del usuario del token; el admin y el vendedor la ven en Órdenes y en los gráficos de Dashboard y Reportes.

### Por qué el flujo es eficiente

- **Una sola fuente de verdad:** precios, stock y totales se calculan en el backend, así que el frontend no puede mandar un precio alterado.
- **Pocas peticiones:** cada vista pide una vez lo que necesita al cargar; la búsqueda de productos filtra en el navegador para no mandar una petición por cada tecla.
- **Datos listos para mostrar:** los DTO entregan lo que la vista necesita (por ejemplo, la boleta ya trae sus ítems y el producto trae el nombre de su categoría), lo que evita peticiones extra.
- **CORS acotado:** el backend solo acepta peticiones del origen del frontend (`CORS_ORIGINS`) y de los métodos y headers que usa.
- **Sin estado en el servidor:** el backend no guarda sesiones (`STATELESS`); cada petición se autentica con su token, lo que facilita escalar.

## 5. Autenticación con JWT y roles en el backend (IE3.3.1)

| Paso | Cómo se hace |
|---|---|
| Registro y login | `POST /auth/register` crea siempre un `CLIENTE` aunque se envíe otro rol. `POST /auth/login` compara la contraseña con BCrypt y responde 401 con un mensaje genérico si no coincide |
| Generación | `JwtService` firma el token con HMAC-SHA256 y una clave de al menos 32 caracteres (`JWT_SECRET`). Lleva el id del usuario, su rol, el emisor `zthorken-api`, un id único y la fecha de expiración (24 h). La respuesta incluye `token`, `expiraEn` y el usuario sin contraseña |
| Verificación | `JwtAuthFilter` lee `Authorization: Bearer`, valida firma, emisor y expiración, y busca al usuario en la base de datos. El rol se toma de la base y no del token, así que un cambio de rol se aplica al instante y el token de un usuario eliminado deja de servir |
| Renovación | `POST /auth/refresh` entrega un token nuevo con 24 horas más a quien tenga un token vigente. Un token vencido no se puede renovar |
| Autorización | `SecurityConfig` define el acceso por ruta y método; `BoletaController` además limita al cliente a sus propias boletas |
| Clave | Solo el perfil `dev` trae una clave por defecto; en `mysql` la variable `JWT_SECRET` es obligatoria |

| Acceso | Rutas |
|---|---|
| Público | login, registro, `GET` de productos y categorías, `POST /boletas` (compra sin cuenta), `/health`, Swagger |
| Con token (cualquier rol) | `POST /auth/refresh`, `PUT /auth/perfil`, `GET /boletas` y `GET /boletas/{id}` (el cliente solo ve las suyas) |
| Solo `ADMIN` | crear, editar y borrar productos, categorías y usuarios; anular y borrar boletas |

Sin token, o con un token inválido o vencido, la respuesta es 401; con un rol sin permiso, 403.

## 6. Gestión de la sesión en el frontend (IE3.3.2)

`SesionProvider` (`src/context/SesionProvider.jsx`) mantiene el estado de autenticación para toda la aplicación:

- **Persistencia:** la sesión `{ token, expiraEn, usuario }` se guarda en localStorage y se lee al iniciar, así que sobrevive a una recarga de página.
- **Sesiones vencidas:** al leerla, si `expiraEn` ya pasó, se borra y la persona entra como visitante.
- **Renovación automática:** 5 minutos antes del vencimiento llama a `POST /auth/refresh` y guarda el token nuevo.
- **Cierre por seguridad:** si el backend responde 401 a una petición con token (token vencido, usuario eliminado), `api.jsx` borra la sesión y avisa al proveedor; el panel lleva a Iniciar sesión con el mensaje «Tu sesión expiró. Vuelve a iniciar sesión».
- **Varias pestañas:** escucha el evento `storage`, así que cerrar o iniciar sesión en una pestaña se refleja en las demás.
- **Datos mínimos:** se guarda el token y los datos públicos del usuario; la contraseña nunca llega al navegador.

## 7. Restricciones de acceso en el frontend (IE3.3.3)

| Rol | Qué ve | Cómo se restringe |
|---|---|---|
| Visitante | La tienda; puede comprar sin cuenta | Si entra a `/admin`, `AdminLayout` lo lleva a Iniciar sesión |
| Cliente | La tienda, su perfil y sus compras | Si entra a `/admin`, `AdminLayout` lo lleva al Home; el menú no muestra «Panel» |
| Vendedor | Panel con Órdenes (y boleta), Productos (solo ver) y Perfil | El menú del panel oculta las opciones de admin; `SoloAdmin` lo lleva a Órdenes si escribe la URL de una vista restringida; no ve botones de crear, editar ni eliminar |
| Administrador | Todo el panel: Dashboard con gráficos, Productos, Categorías, Usuarios, Órdenes, Reportes con gráficos y Perfil | Acceso completo |

Ejemplos de interfaces restringidas:

- `/admin/usuarios`, `/admin/categorias`, `/admin/reportes` y `/admin` (Dashboard): solo admin.
- `/admin/productos/nuevo` y `/admin/productos/:id/editar`: solo admin; el vendedor ve el listado sin acciones.
- El administrador no puede eliminarse a sí mismo ni quitarse su rol.

Las restricciones del frontend mejoran la experiencia porque cada rol solo ve lo que puede usar. La seguridad real está en el backend: aunque alguien modifique el navegador, la API responde 401 o 403.

## 8. Pruebas y resultados

| Proyecto | Herramientas | Qué se prueba | Resultado |
|---|---|---|---|
| Backend | JUnit 5, Mockito, Spring Boot Test, MockMvc | Servicios (reglas de negocio), repositorios, endpoints CRUD, seguridad: login, 401/403 por rol, cliente limitado a sus boletas, renovación del token, cambio de rol inmediato, token de usuario eliminado | 55 de 55 pruebas correctas con `./mvnw test` |
| Frontend | Jasmine, Karma, Testing Library | Vistas de la tienda y del panel, formularios, carrito y checkout, sesión (persistencia, expiración, renovación, varias pestañas), restricciones por rol y gráficos | 219 de 219 pruebas correctas; cobertura 95% de sentencias y 96% de líneas |

Prueba de integración manual con ambos proyectos corriendo (agregar capturas de cada paso):

1. `./mvnw spring-boot:run` en el backend y `npm run dev` en el frontend.
2. Login con cada cuenta de prueba y revisión de lo que ve cada rol.
3. Compra como visitante y como cliente; revisión de la boleta en Órdenes y del descuento de stock.
4. Prueba de cada endpoint en Swagger con y sin token para comprobar las respuestas 200, 401 y 403.

## 9. Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@zthorken.cl | admin123 |
| Vendedor | vendedor@zthorken.cl | vendedor123 |
| Cliente | pedro@correo.cl | pedro123 |
