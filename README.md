# Pa'la Vaca — Backend

API REST del sistema integral de gestión ganadera **Pa'la Vaca**. Expone autenticación por sesión,
catálogos de finca/potrero/lote/animal, registros de hato, salud, alimentación, finanzas y la
sincronización offline que consume el cliente web.

> **Repositorio:** `back/` · **Rama de trabajo actual:** `feature/gestion-lotes-potreros`
> (base: `audit/full-project-review`). El frontend vive en el repositorio hermano
> `pa-la-vaca_front/`.

---

## Índice

- [Stack tecnológico](#stack-tecnológico)
- [Requisitos previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Levantamiento](#levantamiento)
- [Primer uso](#primer-uso)
- [Uso de la API](#uso-de-la-api)
- [Modelo de datos y reglas de negocio](#modelo-de-datos-y-reglas-de-negocio)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Desarrollo y pruebas](#desarrollo-y-pruebas)
- [Solución de problemas](#solución-de-problemas)

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java **21** |
| Framework | Spring Boot **4.1.2** (Web MVC, Data JPA, Security, Validation, AOP) |
| Base de datos | PostgreSQL (probado con **17**) |
| ORM / esquema | Hibernate/JPA con `ddl-auto=update`; Flyway presente **sin migraciones versionadas** |
| Seguridad | Sesión HTTP + CSRF (`CookieCsrfTokenRepository`), BCrypt, CORS configurable |
| Documentación | SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`) |
| Utilidades | Lombok, DevTools (reinicio automático en desarrollo) |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) — **no** requiere Maven instalado |

---

## Requisitos previos

1. **JDK 21** (o superior compatible) con `JAVA_HOME` configurado.
2. **PostgreSQL 14+** en ejecución (por defecto se espera `localhost:5432`).
3. Opcional: Docker, si prefieres levantar PostgreSQL en contenedor.

Verificación rápida:

```bash
java -version        # debe mostrar versión 21
psql --version       # opcional, para administrar la BD
```

---

## Instalación

### 1. Obtener el código

```bash
cd back
```

### 2. Crear la base de datos

```bash
psql -U postgres -c "CREATE DATABASE pala_vaca;"
```

O desde `psql`:

```sql
CREATE DATABASE pala_vaca;
```

El usuario y contraseña por defecto son `postgres` / `root` (ver `application.properties`).
Ajusta estos valores según tu entorno local.

### 3. Compilar

```powershell
# Windows
.\mvnw.cmd -q -DskipTests compile
```

```bash
# Linux / macOS
./mvnw -q -DskipTests compile
```

El wrapper descarga la versión de Maven necesaria la primera vez. El esquema se crea
automáticamente al arrancar la aplicación (Hibernate `ddl-auto=update`), por lo que **no** hay
que ejecutar scripts SQL manuales ni migraciones.

---

## Configuración

Toda la configuración está en `src/main/resources/application.properties`. Los valores marcados
como `${VARIABLE:valor}` pueden sobreescribirse con variables de entorno.

| Propiedad | Valor por defecto | Variable de entorno | Descripción |
|---|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/pala_vaca` | `SPRING_DATASOURCE_URL` | Cadena de conexión a PostgreSQL |
| `spring.datasource.username` | `postgres` | `SPRING_DATASOURCE_USERNAME` | Usuario de la BD |
| `spring.datasource.password` | `root` | `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD |
| `spring.jpa.hibernate.ddl-auto` | `update` | — | Crea/actualiza tablas desde las entidades JPA |
| `spring.jackson.property-naming-strategy` | `SNAKE_CASE` | — | Formato JSON de la API (`farm_id`, `current_paddock_id`, …) |
| `server.servlet.session.timeout` | `7d` | — | Vigencia de la sesión |
| `server.servlet.session.cookie.name` | `PALAVACA_SESSION` | — | Nombre de la cookie de sesión |
| `server.servlet.session.cookie.secure` | `true` | — | Cookie solo HTTPS (self-hosting) |
| `server.servlet.session.cookie.same-site` | `none` | — | Requerido para frontend en otro dominio |
| `app.cors.allowed-origins` | Vercel + `localhost:5173` + `localhost:4173` | `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) |
| `app.frontend.base-url` | `http://localhost:5173` | `FRONTEND_BASE_URL` | Origen del front, usado en enlaces de correo |
| `app.password-reset.token-ttl-minutes` | `60` | `PASSWORD_RESET_TTL_MINUTES` | Vigencia del token de recuperación |

Ejemplo de sobreescritura:

```powershell
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,https://tu-front.vercel.app"
$env:SPRING_DATASOURCE_PASSWORD = "tu-password"
.\mvnw.cmd spring-boot:run
```

> **Nota sobre el esquema:** `ddl-auto=update` crea y añade columnas, pero **no elimina** columnas
> obsoletas. Si vienes de un esquema anterior (por ejemplo con `animals.paddock_id` o
> `paddocks.capacity_animal_units`), recrea la base de datos desde cero para evitar columnas
> residuales. No se conservan ni migran datos.

---

## Levantamiento

### Modo desarrollo (con reinicio automático)

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

### Modo ejecutable

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\back-0.0.1-SNAPSHOT.jar
```

La API queda disponible en:

- **API base:** `http://localhost:8080/api/v1`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Primer uso

La base de datos arranca vacía. Puedes crear la organización, la primera finca y el usuario
administrador de dos formas:

### Opción A — desde el frontend

Abre `http://localhost:5173/register` y completa el formulario de alta inicial.

### Opción B — desde la API

```bash
curl -X POST http://localhost:8080/api/v1/registrations \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "Administrador",
    "userEmail": "admin@turancho.mx",
    "userPass": "TuClave_123",
    "orgName": "Tu Rancho",
    "farm": { "name": "Finca Norte", "hectares": 100 }
  }'
```

La contraseña debe tener entre **9 y 72 caracteres**, incluir mayúscula, minúscula, número y
carácter especial, y no contener espacios. `POST /registrations` crea de forma atómica la
organización, la finca (si se envía) y el usuario con rol `Administrador`, y devuelve la sesión.

### Iniciar sesión y obtener CSRF

```bash
# 1) Cookie de sesión + usuario
curl -c cookies.txt -X POST http://localhost:8080/api/v1/auth/session \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@turancho.mx","password":"TuClave_123"}'

# 2) Token CSRF (necesario para POST/PUT/PATCH/DELETE)
curl -b cookies.txt http://localhost:8080/api/v1/auth/csrf

# 3) Enviar el token en el header X-XSRF-TOKEN en cada escritura
curl -b cookies.txt -X POST http://localhost:8080/api/v1/paddocks \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: <token>" \
  -d '{"farm_id":"<uuid>","name":"Potrero Norte","hectares":30,"capacity":50}'
```

El cliente web hace esto automáticamente en `src/services/apiClient.ts` y
`src/services/authService.ts`.

---

## Uso de la API

Todas las rutas comparten el prefijo `/api/v1`, usan JSON en `snake_case`, UUID como identificadores
y fechas ISO 8601. Las escrituras requieren sesión autenticada y header `X-XSRF-TOKEN`.

| Grupo | Endpoints principales |
|---|---|
| **Autenticación** | `GET /auth/csrf`, `POST /auth/session`, `GET /auth/session`, `POST /auth/session/renew`, `DELETE /auth/session`, `GET /me`, `POST /auth/password-resets`, `POST /auth/password-resets/confirm` |
| **Alta inicial** | `POST /registrations` |
| **Hato** | `/animals`, `/lots`, `POST /lots`, `PUT /lots/{id}`, `GET /lot-movements`, `/weight-records` (alias `/animal-weights`), `/reproductive-records`, `/movements`, `/animal-identifiers` |
| **Salud** | `/health-records` (alias `/health-events`), `/treatments`, `/vaccinations`, `/medications` |
| **Alimentación** | `/feeding-records`, `/production-records`, `/milk-productions`, `/products`, `/inventories`, `/inventory-movements`, `/purchase-records` (alias `/purchases`) |
| **Finanzas** | `/expenses`, `/cost-allocations`, `/financial-records`, `GET /investment-summary`, `/sales-records` (alias `/sales`), `/customers`, `/suppliers` |
| **Administración** | `/farms`, `/paddocks`, `/users`, `/organizations`, `/alerts`, `/audit-logs` |
| **Sincronización offline** | `GET /sync/bootstrap`, `GET /sync/changes`, `/sync-queue` (alias `/offline-sync`), `POST /sync-queue/batch`, `PATCH /sync-queue/{id}/processed` |

Los recursos estándar exponen `GET` (listado y `/{id}`), `POST`, `PUT /{id}` y `DELETE /{id}`
(según el recurso). Los listados aceptan filtros por `farm_id` / `organization_id` y otros campos
según el controlador. La documentación interactiva y los esquemas completos están en Swagger UI.

### Ejemplos de recursos derivados

- `GET /paddocks` → `current_animals`, `available_capacity`, `occupancy_percentage`.
- `GET /lots` → `current_paddock_name`, `animal_count`.
- `GET /animals` → `paddock_id` y `paddock_name` **derivados del lote**.
- `GET /lot-movements` → historial de traslados (origen, destino, fecha, cantidad, usuario,
  observaciones).

---

## Modelo de datos y reglas de negocio

La jerarquía es **finca → potreros → lotes → animales**:

- Una finca tiene muchos potreros; un potrero pertenece a una finca.
- Un lote es una agrupación de animales y **siempre tiene un potrero actual**
  (`lots.current_paddock_id` es `NOT NULL`). Un potrero puede tener muchos lotes.
- El animal pertenece a un lote (`animals.lot_id`). **No existe** `animals.paddock_id`: su
  ubicación se deriva del potrero del lote.
- La capacidad de un potrero (`paddocks.capacity`) se mide en **número de animales**.
  Ocupación = animales activos (`status = active`) de los lotes del potrero;
  disponible = capacidad − ocupación; porcentaje = ocupación / capacidad × 100.
  Capacidad nula o ≤ 0 significa "sin límite".
- `PUT /lots/{id}` con otro `current_paddock_id` realiza el traslado del lote completo y registra
  un `LotMovement`. El campo opcional `movement_notes` guarda las observaciones del traslado sin
  sobrescribir las notas del lote.
- La capacidad se valida en backend con bloqueo pesimista del potrero para respetar concurrencia:
  se rechaza cualquier operación que exceda la capacidad, reducir la capacidad por debajo de la
  ocupación y eliminar un potrero con lotes asignados.

---

## Estructura del proyecto

```
back/
├── mvnw / mvnw.cmd               Maven Wrapper
├── pom.xml                       Dependencias y plugin de Spring Boot
└── src/
    ├── main/
    │   ├── java/com/palavaca/back/
    │   │   ├── config/           Seguridad, CORS, CSRF, idempotencia, aspecto de sync
    │   │   ├── controller/       Endpoints REST (uno por dominio)
    │   │   ├── dto/              Request/Response records
    │   │   ├── model/            Entidades JPA
    │   │   ├── repository/       Repositorios Spring Data JPA
    │   │   └── service/          Reglas de negocio, capacidad, sincronización, finanzas
    │   └── resources/
    │       └── application.properties
    └── test/java/.../BackApplicationTests.java
```

---

## Desarrollo y pruebas

```powershell
# Compilar sin ejecutar tests
.\mvnw.cmd -q -DskipTests compile

# Ejecutar tests
.\mvnw.cmd test

# Paquete ejecutable
.\mvnw.cmd -DskipTests package
```

- **DevTools** reinicia la aplicación automáticamente al recompilar.
- El único test presente (`BackApplicationTests`) es de contexto de Spring y **necesita una base de
  datos disponible** con la configuración de `application.properties`. Si solo quieres compilar,
  usa `-DskipTests`.
- No hay plugin de cobertura (JaCoCo) configurado.

---

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Connection refused` al arrancar | PostgreSQL no está corriendo o cambiaron las credenciales | Verificar servicio y `SPRING_DATASOURCE_*` |
| `401 Unauthorized` en escrituras | Falta sesión o token CSRF | Hacer `POST /auth/session`, luego `GET /auth/csrf` y enviar `X-XSRF-TOKEN` |
| El frontend en otro dominio no conecta | Origen no permitido en CORS | Añadirlo a `CORS_ALLOWED_ORIGINS` (y mantener `Secure; SameSite=None`) |
| Columnas antiguas después de cambiar el modelo | `ddl-auto=update` no elimina columnas | Recrear la base de datos desde cero |
| No llega el correo de recuperación | El notificador por defecto solo escribe en logs | Buscar el token en la consola (`LoggingPasswordResetNotifier`); integrar un proveedor SMTP para producción |
| Swagger no abre | Ruta incorrecta | Usar `http://localhost:8080/swagger-ui/index.html` |

---

## Relación con el frontend

El cliente React corre en `http://localhost:5173` y en desarrollo redirige `/api` a
`http://localhost:8080` mediante el proxy de Vite. En producción se configura
`VITE_BACKEND_PROXY_TARGET` (o `VITE_API_BASE_URL`) en el frontend. Consulta el README de
`pa-la-vaca_front/` y `API_BACKEND_MAP.md` para el contrato completo de pantallas y recursos.
