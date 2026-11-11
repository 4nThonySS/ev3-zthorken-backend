# Zthorken Backend

API REST de la tienda **Zthorken**, hecha con Spring Boot para la Evaluación 3 de DSY1104.

- Frontend: https://github.com/4nThonySS/ev3-zthorken-online

## Tecnologías
- Java 21 y Spring Boot 4.1
- Spring Data JPA con H2 (desarrollo) y MySQL (producción)
- Spring Security y JWT (jjwt)
- Swagger / OpenAPI (springdoc)

## Requisitos
- JDK 21. No hace falta instalar Maven: el proyecto trae su propio wrapper (`mvnw`).
- MySQL 8, solo si usas el perfil `mysql`.

## Cómo ejecutar

**Perfil de desarrollo (H2 en memoria, no requiere base de datos):**
```bash
./mvnw spring-boot:run          # Linux / macOS / Git Bash
mvnw.cmd spring-boot:run        # Windows CMD / PowerShell
```

**Perfil MySQL:**
```bash
DB_USER=root DB_PASSWORD=tu_clave ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```
La base de datos `zthorken` se crea automáticamente si no existe.

## URLs útiles
| URL | Descripción |
|---|---|
| http://localhost:8080/api/v1/health | Estado de la API |
| http://localhost:8080/swagger-ui.html | Documentación interactiva (Swagger) |
| http://localhost:8080/h2-console | Consola H2 (perfil dev; JDBC URL `jdbc:h2:mem:zthorken`, usuario `sa`) |

## Variables de entorno
| Variable | Por defecto | Uso |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` o `mysql` |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | localhost / root / vacío | Conexión MySQL |
| `JWT_SECRET` | clave de desarrollo | Clave para firmar los tokens (mínimo 32 caracteres) |
| `CORS_ORIGINS` | `http://localhost:5173` | Origen del frontend |

## Tests
```bash
./mvnw test
```

## Estructura del proyecto
```
src/main/java/cl/zthorken/backend/
├── config/        Seguridad, Swagger y carga de datos iniciales
├── controller/    Controladores REST (/api/v1/...)
├── dto/           Objetos de entrada y salida de la API
├── exception/     Errores de negocio y manejo global (400, 404)
├── model/         Entidades JPA y enums (Rol, EstadoBoleta)
├── repository/    Repositorios Spring Data JPA
└── service/       Lógica de negocio
src/main/resources/
├── application.properties         Configuración común
├── application-dev.properties     Perfil dev (H2)
└── application-mysql.properties   Perfil mysql
```

## Endpoints
Todos bajo `/api/v1`. Se pueden probar desde Swagger UI.

| Recurso | Endpoints |
|---|---|
| Categorías | `GET /categorias`, `GET /categorias/{id}`, `POST /categorias`, `PUT /categorias/{id}`, `DELETE /categorias/{id}` |
| Productos | `GET /productos?categoriaId=&q=&oferta=`, `GET /productos/stock-critico`, `GET /productos/{id}`, `POST /productos`, `PUT /productos/{id}`, `DELETE /productos/{id}` |
| Boletas | `GET /boletas?usuarioId=`, `GET /boletas/{id}`, `POST /boletas`, `PATCH /boletas/{id}/anular`, `DELETE /boletas/{id}` |
| Usuarios | `GET /usuarios?rol=`, `GET /usuarios/{id}`, `POST /usuarios`, `PUT /usuarios/{id}`, `DELETE /usuarios/{id}` |

Reglas principales:
- `POST /boletas` usa el precio vigente (oferta si existe), calcula el total y descuenta stock; si no alcanza responde 400.
- Anular una boleta devuelve el stock de sus productos.
- No se puede borrar una categoría con productos. Al borrar un producto o un usuario, sus boletas se conservan.
- Los errores responden con `status`, `mensaje` y, si son de validación, `campos` con el error de cada campo.

Por ahora los endpoints no piden token; la seguridad por roles se agrega en la tarea 5.

## Modelo de datos
`usuario` → `boleta` → `detalle_boleta` ← `producto` ← `categoria`

| Tabla | Descripción |
|---|---|
| `usuario` | Cuentas con rol `ADMIN`, `VENDEDOR` o `CLIENTE`; contraseña cifrada con BCrypt |
| `categoria` | Categorías de productos (nombre único) |
| `producto` | Precio en CLP, precio de oferta opcional, stock y stock crítico |
| `boleta` | Orden de compra: fecha, estado, total, datos del cliente y dirección de envío. El usuario es opcional (compra sin cuenta) |
| `detalle_boleta` | Líneas de la boleta con nombre y precio del producto al momento de la compra |

Al arrancar con la base vacía se cargan los mismos datos del frontend: 6 categorías, 12 productos y 1 boleta de ejemplo.

## Usuarios de prueba
| Rol | Correo | Contraseña |
|---|---|---|
| Admin | admin@zthorken.cl | admin123 |
| Vendedor | vendedor@zthorken.cl | vendedor123 |
| Cliente | pedro@correo.cl | pedro123 |
