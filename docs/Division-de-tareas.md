# Zthorken EV3 – División de tareas

Actualizado: 14 nov 2026 · Versión en línea: https://claude.ai/artifact/Kk2UqPJPuSwAfC5FyYQ6f7

**Avance: 6 de 8 tareas terminadas** (✅ = terminada)

## Resumen

La EV3 de DSY1104 (40%) pide un backend Spring Boot con base de datos, API REST versionada, Swagger y seguridad JWT por roles, conectado al frontend de Zthorken. El trabajo se divide en 8 tareas; las tareas 1 a 6 ya están terminadas.

| Repo | Contenido | Carpeta local |
| --- | --- | --- |
| [ev3-zthorken-online](https://github.com/4nThonySS/ev3-zthorken-online) | Frontend React + Vite | `full_ev3/ev3-zthorken-online` (pasará a `frontend/`) |
| [ev3-zthorken-backend](https://github.com/4nThonySS/ev3-zthorken-backend) | Backend Spring Boot 4.1 + Java 21 | `full_ev3/ev3-zthorken-backend` (pasará a `backend/`) |
| — | Instrucciones de la pauta y documentos de entrega | `full_ev3/docs` |

Roles del sistema: **Administrador** (acceso total), **Vendedor** (solo ver productos y órdenes con su detalle) y **Cliente** (solo la tienda).

## Cumplimiento de la pauta

De los 26 puntos del Anexo 1, el proyecto cumple 9, cumple 5 de forma parcial y le faltan 12. El backend y el frontend ya están integrados; lo que falta es la base de datos real, los colaboradores y los documentos de entrega.

Según los 4 criterios de evaluación:

- **Calidad del backend:** estructura, lógica y tests completos; falta conectarlo a MySQL u Oracle Cloud en vez de H2.
- **Integración REST:** la API está documentada en Swagger y el frontend la usa con `fetch` y el token JWT.
- **Seguridad:** login con JWT y permisos por rol en el backend, y vistas por rol en el frontend.
- **Colaboración y documentación:** el repo existe, pero todos los commits son de una sola cuenta y faltan los 4 documentos.

| # | Requisito de la pauta | Cumple | Evidencia o qué falta | Tarea |
| --- | --- | --- | --- | --- |
| 1 | Proyecto Spring Boot con dependencias de BD y seguridad | Sí | JPA, Security, JWT, Swagger, H2 y MySQL en `pom.xml` | 1 |
| 2 | BD relacional (MySQL, Oracle Cloud o Firebase) conectada al backend | Parcial | El perfil `mysql` está listo, pero todo corre en H2 en memoria. Falta instalar MySQL u Oracle Cloud y probarlo | 1 |
| 3 | Entidades y relaciones según el modelo (Figura 1) | Sí | `Usuario`, `Categoria`, `Producto`, `Boleta` y `DetalleBoleta` con sus relaciones | 2 |
| 4 | Repositorios y servicios para la BD | Sí | 5 repositorios JPA y 4 servicios | 2, 3 |
| 5 | Lógica de negocio para los casos de uso | Sí | Compra con total y descuento de stock, anulación, validaciones de precio, correo y categoría | 3 |
| 6 | Lógica encapsulada y testeada | Sí | Servicios + DTOs; 41 tests (21 unitarios con Mockito y 11 de endpoints) | 3 |
| 7 | Controladores REST con CRUD | Sí | Categorías, productos, boletas y usuarios | 4 |
| 8 | Rutas de la API versionadas | Sí | Todo bajo `/api/v1` | 4 |
| 9 | Swagger documenta los endpoints | Sí | `/swagger-ui.html`, con `@Operation` y `@ApiResponse` en cada endpoint | 4 |
| 10 | El frontend llama al backend con fetch o axios | Sí | `src/api.jsx` (fetch + token) y el hook `useApi` en todas las vistas | 6 |
| 11 | Comunicación REST efectiva y datos bien manejados | Sí | Estados de carga y error, mensajes del backend en los formularios; 198 tests del frontend con un backend falso | 6 |
| 12 | Spring Security gestiona la autenticación por roles | Sí | `SecurityConfig` con reglas por rol para cada endpoint | 5 |
| 13 | JWT para autenticación con tokens | Sí | `POST /auth/login` y `/auth/register` entregan el token; `JwtAuthFilter` lo valida en cada petición | 5 |
| 14 | Restringir funciones según los permisos del usuario | Sí | El backend responde 401/403 y el frontend oculta lo que cada rol no puede usar | 5, 6 |
| 15 | Repositorio remoto en GitHub para el backend | Sí | [ev3-zthorken-backend](https://github.com/4nThonySS/ev3-zthorken-backend), público | 1 |
| 16 | Commits con comentarios claros y coherentes | Parcial | Solo 2 commits en el backend, cada uno agrupa 2 tareas. Desde ahora conviene un commit por cambio, con mensaje descriptivo | Todas |
| 17 | Colaboración y tareas repartidas de forma equitativa | No | Los 3 commits (2 backend, 1 frontend) son de 4nThonySS. Faltan los colaboradores y que cada integrante haga commits de sus tareas | 5 a 8 |
| 18 | Administrador con acceso total | Sí | Login real y panel completo contra la API | 5, 6 |
| 19 | Vendedor solo ve productos y órdenes con su detalle; lo demás no aparece | Sí | Su panel solo muestra Órdenes, Productos (sin editar) y Perfil; las demás rutas lo redirigen | 5, 6 |
| 20 | Cliente solo accede a la tienda | Sí | Si entra a `/admin` vuelve al inicio; el backend solo le entrega sus boletas | 5, 6 |
| 21 | Documento ERS (requerimientos, herramientas y propuesta) | No | No iniciado | 7 |
| 22 | Manual de usuario con pantallazos detallados | No | No iniciado; necesita el frontend integrado | 7 |
| 23 | Documentación de APIs | Parcial | Swagger y tabla de endpoints en el README; falta el documento de entrega | 7 |
| 24 | Documento de APIs e integración: modelo de BD, endpoints, pruebas y resultados | No | No iniciado | 7 |
| 25 | Frontend y backend comprimidos | No | Se hace al final | 8 |
| 26 | Presentación de 15 min con demo integrada y ronda de preguntas | No | Cada integrante debe poder explicar sus commits | 8 |

La Figura 1 de la pauta es una imagen; el punto 3 se revisó contra ella en la tarea 2.

## Tareas

Cada integrante debería hacer commits de al menos una tarea desde su cuenta: en la presentación se pregunta por los cambios de cada uno en GitHub.

| # | Tarea | Repo | Depende de | Responsable | Estado |
| --- | --- | --- | --- | --- | --- |
| 1 | Entorno y proyecto base del backend | backend | — | 4nThonySS | ✅ Terminada |
| 2 | Modelo de datos: entidades, repositorios y datos iniciales | backend | 1 | 4nThonySS | ✅ Terminada |
| 3 | Servicios con lógica de negocio y tests | backend | 2 | 4nThonySS | ✅ Terminada |
| 4 | Controladores REST `/api/v1` y Swagger | backend | 3 | 4nThonySS | ✅ Terminada |
| 5 | Seguridad: Spring Security, JWT y roles | backend | 4 | Por asignar | ✅ Terminada |
| 6 | Integración del frontend con fetch, login y vistas por rol | frontend | 4, 5 | Por asignar | ✅ Terminada |
| 7 | Documentos: ERS, manual de usuario, APIs e integración | docs | 6 | Por asignar | Pendiente |
| 8 | Colaboradores, `.zip` y presentación | entrega | 7 | Todo el equipo | Pendiente |

## Detalle por tarea

### ✅ 1. Entorno y proyecto base (terminada)

- [x] Java 21 instalado; Spring Boot 4.1.2 con JPA, Security, Validation, JWT (jjwt 0.13) y Swagger (springdoc 3.2).
- [x] Perfiles `dev` (H2 en memoria) y `mysql` (credenciales por variables de entorno).
- [x] `SecurityConfig` provisional con CORS para `localhost:5173`, endpoint `/api/v1/health` y README.
- **Terminado:** compila, los tests pasan y Swagger carga en `localhost:8080/swagger-ui.html`.

### ✅ 2. Modelo de datos (terminada)

- [x] Entidades `Usuario` (con rol), `Categoria`, `Producto`, `Boleta` y `DetalleBoleta`, según la Figura 1 de la pauta.
- [x] Relaciones: Categoría 1–N Producto, Usuario 1–N Boleta, Boleta 1–N DetalleBoleta, Producto 1–N DetalleBoleta.
- [x] Repositorios JPA y carga de datos iniciales desde `src/data` del frontend, más un usuario por rol.
- **Terminado:** las tablas aparecen en la consola H2 con los datos cargados; 9 tests pasan.

### ✅ 3. Servicios y lógica de negocio (terminada)

- [x] Servicios `CategoriaService`, `ProductoService`, `UsuarioService` y `BoletaService` con las reglas de negocio:
    - La compra usa el precio de oferta si existe, calcula el total, descuenta stock y rechaza si no alcanza.
    - Anular una boleta devuelve el stock; no se puede anular dos veces.
    - Nombre de categoría y correo de usuario únicos; oferta menor que el precio; no se borra una categoría con productos.
    - Contraseñas cifradas con BCrypt; al editar un usuario sin contraseña se mantiene la actual.
- [x] DTOs (`record`) para no exponer entidades ni contraseñas y `GlobalExceptionHandler` con respuestas 400 y 404 que incluyen el error de cada campo.
- [x] 21 tests unitarios de los servicios con JUnit y Mockito.
- **Terminado:** `mvnw test` pasa: 41 tests en total.

### ✅ 4. API REST y Swagger (terminada)

- [x] CRUD en `/api/v1/categorias`, `/api/v1/productos`, `/api/v1/boletas` y `/api/v1/usuarios`.
- [x] Extras: filtros de productos (`categoriaId`, `q`, `oferta`), `GET /productos/stock-critico`, `GET /boletas?usuarioId=` y `PATCH /boletas/{id}/anular`.
- [x] Códigos 200, 201, 204, 400 y 404, con `@Operation` y `@ApiResponse` de Swagger en cada endpoint.
- [x] 11 tests de integración con MockMvc que recorren los endpoints.
- Provisional: `/api/v1/**` queda abierto sin token hasta la tarea 5.
- **Terminado:** los endpoints aparecen y responden en Swagger UI.

### ✅ 5. Seguridad (terminada)

- [x] `POST /api/v1/auth/login` y `/register` que devuelven un JWT; filtro que valida el token en cada petición.
- [x] Reglas: Admin todo; Vendedor solo `GET` de productos y boletas; Cliente solo la tienda y sus compras.
- [x] 8 tests de seguridad (`SeguridadTest`): 49 tests en total.
- **Terminado:** sin token responde 401/403 y cada rol accede solo a lo suyo, comprobado con tests.

### ✅ 6. Integración con el frontend (terminada)

- [x] Cliente `fetch` (`src/api.jsx`) con la URL del backend (`VITE_API_URL`) y el token en el header `Authorization`. Se usó fetch en vez de axios para no sumar una dependencia; la pauta acepta cualquiera de los dos.
- [x] Productos, categorías, usuarios y órdenes vienen de la API; login y registro reales con el token guardado. El blog y los mensajes de contacto siguen en localStorage porque el backend no tiene esas tablas.
- [x] Panel de administración completo y vista de vendedor (Órdenes, Productos sin editar y Perfil).
- [x] Nuevo endpoint `PUT /api/v1/auth/perfil` para que cada usuario edite sus datos (checkout y perfil) sin poder cambiarse el rol.
- [x] Tests del frontend reescritos con un backend falso (`src/test/backendFalso.js`): 198 tests y 95% de cobertura. Backend: 50 tests.
- **Terminado:** la tienda, el carrito, la compra y los paneles funcionan contra el backend.

### 7. Documentación

- [ ] ERS: requerimientos, herramientas y propuesta.
- [ ] Manual de usuario con pantallazos de cada rol.
- [ ] Documentación de APIs (exportada de Swagger) y documento de integración: modelo de BD, endpoints, pruebas y resultados.
- **Terminado:** los cuatro documentos están en `full_ev3/docs`.

### 8. Entrega

- [ ] Invitar a los compañeros como colaboradores en los dos repos.
- [ ] Comprimir frontend (sin `node_modules`) y backend (sin `target`).
- [ ] Preparar la presentación de 15 minutos y repasar quién explica cada parte.
- **Terminado:** todo lo de la sección Entregables está marcado.

## Entregables

La pauta pide 8 entregables más una presentación de 15 minutos y 5 de preguntas.

- [x] Enlace GitHub público del frontend
- [x] Enlace GitHub público del backend
- [ ] Frontend comprimido
- [ ] Backend comprimido
- [ ] Documento ERS terminado
- [ ] Manual de usuario con pantallazos
- [ ] Documentación de APIs
- [ ] Documento de APIs e integración
- [ ] Presentación ensayada: demo integrada y cada integrante listo para explicar sus commits

## Pendientes

- **Base de datos:** MySQL no está instalado; todo corre en H2. Hay que instalar MySQL o usar Oracle Cloud antes de la demo.
- **Colaboradores:** faltan los usuarios de GitHub del equipo para invitarlos a los dos repos.
- **Commits:** la pauta evalúa el reparto equitativo y en la ronda de preguntas se revisan los cambios de cada uno. Las tareas 5 a 7 deberían subirlas otros integrantes desde sus cuentas, con commits pequeños y mensajes claros.
- **Responsables:** asignar la tarea 7 en la tabla de Tareas.
- **Carpetas:** renombrar a `frontend/` y `backend/` cuando VS Code esté cerrado.
