# ProyectV4

Aplicación Spring Boot con tres capas bien diferenciadas:

- MVC web para páginas Thymeleaf
- REST API para endpoints JSON
- Security/JWT para autenticación

La idea del proyecto es mantener la capa de presentación separada de la capa de negocio y de la seguridad, para que cada parte pueda evolucionar sin mezclar responsabilidades.

## Tecnologías

- Java 21
- Spring Boot 3.x
- Spring MVC + Thymeleaf
- Spring Data JPA
- Spring Security
- JWT con firma HMAC
- MySQL
- Maven Wrapper

## Arquitectura general

```text
src/main/java/com/apc21z/proyectv4/
├── ProyectV4Application.java
├── web/
│   ├── IndexController.java
│   ├── LoginController.java
│   ├── PeopleController.java
│   ├── BookWebController.java
│   ├── ThymeTestController.java
│   ├── WebAuthenticationAdvice.java
│   └── dto/
│       └── CreatePersonForm.java
├── rest/
│   ├── ApiBookController.java
│   ├── ApiPersonController.java
│   ├── ApiPeopleController.java
│   ├── model/
│   ├── model/dto/
│   ├── model/mapper/
│   ├── repository/
│   ├── service/
│   ├── exception/
│   └── ...
├── security/
│   └── jwt/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── model/
│       └── service/
└── resources/
    ├── templates/
    ├── static/
    └── application.properties
```

## 1) Capa web (MVC)

La capa web está en `com.apc21z.proyectv4.web`.

Su responsabilidad es renderizar vistas HTML con Thymeleaf y gestionar la interacción del navegador con el usuario. Aquí no vive la lógica de negocio ni la autenticación del API; solo se conectan con servicios y devuelven nombres de vista.

Ejemplos clave:

- `IndexController`: página principal y registro de usuarios web
- `LoginController`: autenticación por formulario web
- `PeopleController`: formulario de personas y búsqueda por skills
- `BookWebController`: páginas de libros para la interfaz web
- `ThymeTestController`: pruebas y ejemplos con Thymeleaf

### Rutas web típicas

- `/`
- `/login`
- `/register`
- `/dashboard`
- `/admin`
- `/people`
- `/books`
- `/thymeTest`

Estas rutas devuelven templates sobre `src/main/resources/templates`, por ejemplo:

- `index.html`
- `login.html`
- `register.html`
- `dashboard.html`
- `people.html`
- `books.html`

### Regla de diseño

La web debe responder con vistas HTML y no exponerse como API JSON. Si una operación requiere un cliente HTTP (frontend o app móvil), normalmente es mejor usar la capa REST.

## 2) Capa REST

La capa REST está en `com.apc21z.proyectv4.rest`.

Su responsabilidad es exponer endpoints JSON para consumir desde frontend, Postman, Angular, React, móvil o scripts automatizados. Aquí se encuentran:

- controladores REST: `ApiBookController`, `ApiPersonController`, `ApiPeopleController`
- DTOs para contratos HTTP
- mappers con MapStruct para convertir entidad -> DTO
- servicios de negocio
- repositorios JPA
- excepciones REST

### Endpoints principales

```text
GET    /api/books
GET    /api/books/{id}
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}

GET    /api/person
GET    /api/person/{id}
POST   /api/person
PUT    /api/person/{id}
DELETE /api/person/{id}

GET    /api/people
GET    /api/people/{id}
POST   /api/people
PUT    /api/people/{id}
DELETE /api/people/{id}
```

### Regla de diseño

La REST es la capa de integración. Un cliente externo debe relacionarse con la API, no con los controladores web. La REST no debería depender de la capa MVC, y la MVC no debería inventar lógica de negocio que la REST ya gestiona.

## 3) Capa security

La capa de seguridad está en `com.apc21z.proyectv4.security.jwt`.

Aquí se define todo lo relacionado con autenticación, autorización y JWT:

- `SecurityConfig`: cadenas de seguridad y reglas de acceso
- `JwtAuthenticationFilter`: filtro que valida token por petición
- `JwtService`: creación y validación del JWT
- `JwtProperties`: propiedades de configuración del JWT
- `AuthenticatedUserDetails`: adaptación de usuario a `UserDetails`
- `UserAccount`: entidad de usuario persistida
- `UserAccountService`: registro, login y carga de credenciales
- `AuthController`: endpoints de login y registro para API
- `LoginRequest`, `RegisterRequest`, `AuthResponse`: DTOs de autenticación

### Objetivo

Esta capa no reemplaza la lógica de negocio; solo decide:

- quién es el usuario
- qué roles tiene
- qué rutas puede visitar
- si la petición lleva credenciales válidas

## JWT en este proyecto

La seguridad actual usa JWT con cookie HttpOnly y no con Authorization Bearer como mecanismo principal.

Esto quiere decir que el navegador recibe la cookie `JWT` en la respuesta y luego la envía automáticamente en peticiones posteriores. Esa cookie es la credencial del usuario para las rutas protegidas.

### Cómo está implementado

#### 1. Configuración

`JwtProperties` mapea:

```properties
security.jwt.secret=...
security.jwt.expiration-ms=900000
```

El secreto debe tener al menos 32 bytes. Esta clave se usa para firmar el token con HMAC.

#### 2. Creación del token

`JwtService` genera un JWT usando la clave HMAC. El flujo es:

1. Se toma el correo del usuario (`username`).
2. Se crea la fecha actual como `iat`.
3. Se añade una fecha de expiración (`exp`).
4. Se firma el token con `Jwts.builder()` + `signWith(signingKey)`. 
5. Se devuelve el JWT generado.

La aplicación también crea la cookie con:

- nombre: `JWT`
- `HttpOnly=true`
- `SameSite=Lax`
- `Path=/`
- `Max-Age` según la expiración
- `Secure` según si la petición es HTTPS

#### 3. Validación en cada petición

`JwtAuthenticationFilter` se registra antes del filtro de login de Spring Security y, en cada request:

1. Busca la cookie `JWT`
2. Si no existe, continúa normalmente
3. Si existe, extrae el username del token
4. Carga el usuario con `UserDetailsService`
5. Verifica firma, usuario y expiración
6. Si todo es correcto, crea un `Authentication` y lo guarda en `SecurityContextHolder`
7. La ruta puede entonces pasar la autorización de Spring Security

#### 4. Usuario y roles

`UserAccount` guarda:

- email
- password
- role

`UserAccountService` normaliza el correo a minúsculas, verifica duplicados, codifica la contraseña con BCrypt y carga el usuario para Spring Security.

`AuthenticatedUserDetails` convierte la cuenta al contrato requerido por Spring Security y arregla el rol a formato `ROLE_...`.

#### 5. Rutas protegidas

`SecurityConfig` define dos cadenas de seguridad:

- `apiSecurity`: matcher `/api/**`
- `webSecurity`: resto de rutas web

Reglas actuales:

- `/api/auth/register` y `/api/auth/login` son públicas
- `/api/**` requiere autenticación por defecto
- rutas de escritura en personas requieren rol `ADMIN`
- `/dashboard` permite `USER` y `ADMIN`
- `/admin` solo `ADMIN`

## Cómo recrear este JWT en otro proyecto

Si en el futuro quieres volver a implementar esto desde cero, la secuencia recomendada es:

1. Crear una entidad `UserAccount` con email, password y role.
2. Crear `UserAccountRepository` con `findByEmail` y `existsByEmail`.
3. Crear `UserDetailsService` para cargar al usuario desde BD.
4. Crear `JwtProperties` con `@ConfigurationProperties` para `security.jwt.*`.
5. Crear `JwtService` con:
   - clave HMAC
   - método `generateToken(username)`
   - método `isTokenValid(token, userDetails)`
   - método `extractUsername(token)`
6. Crear `JwtAuthenticationFilter` basado en `OncePerRequestFilter`.
7. Registrar ese filtro antes de `UsernamePasswordAuthenticationFilter`.
8. Definir `SecurityFilterChain` para web y API.
9. Usar `PasswordEncoder` con BCrypt.
10. En login, crear la cookie `JWT` y devolverla en la respuesta.
11. En las rutas protegidas, validar la cookie por cada petición.

## Recomendaciones futuras

- Mantener JWT, JWT filter y reglas de seguridad en paquetes separados de la REST y la web.
- Añadir pruebas de integración para login, registro, expiración y rutas protegidas.
- Considerar un mecanismo de revocación si necesitas invalidar tokens antes de su expiración.
- En producción, usar variables de entorno para `JWT_SECRET` y HTTPS para cookies `Secure`.
- Separar mejor dominio, servicios y repositorios si la aplicación crece.

## Requisitos y ejecución

### Requisitos

- JDK 21
- Docker + Docker Compose
- Maven Wrapper

### Base de datos

```bash
docker compose up -d db
```

### Ejecutar app

```bash
./mvnw spring-boot:run
```

### Variables útiles

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
```

La app usa la configuración de `application.properties` y los valores de `compose.yaml` para conectar a MySQL local.

## Estado actual

El proyecto compila correctamente con Maven y mantiene una separación clara entre:

- parte web
- parte REST
- parte de seguridad/JWT

Eso facilita mantener y recrear la autenticación en futuras versiones del proyecto.
