# JWT: implementación en este proyecto

Este documento recorre las clases que implementan autenticación JWT en `src/main/java/com/apc21z/proyectv4/jwt/` y cómo se conectan con Spring Security, las cuentas y el frontend Thymeleaf.

## Estructura

| Archivo | Función |
| --- | --- |
| `jwt/model/UserAccount.java` | Entidad de usuarios persistida en `users`. |
| `repository/UserAccountRepository.java` | Consulta por correo y comprueba duplicados. |
| `service/UserAccountService.java` | Registro, normalización de correo y carga de usuarios para Spring Security. |
| `jwt/config/AuthenticatedUserDetails.java` | Adapta `UserAccount` a `UserDetails` y convierte el rol a autoridad Spring. |
| `jwt/config/JwtProperties.java` | Vincula `security.jwt.*` a propiedades Java. |
| `jwt/service/JwtService.java` | Firma, verifica, lee y empaqueta tokens en cookies. |
| `jwt/config/JwtAuthenticationFilter.java` | Extrae y valida la cookie en cada petición. |
| `jwt/config/SecurityConfig.java` | Define cadenas de seguridad, CSRF, login web y permisos. |
| `jwt/controller/AuthController.java` | Endpoints JSON de login y registro para la API. |
| `jwt/dto/LoginRequest.java`, `RegisterRequest.java` | Contratos JSON y validaciones de autenticación. |
| `jwt/dto/AuthResponse.java` | Record declarado, pero actualmente no utilizado por los endpoints. |

Las páginas web y el registro Thymeleaf están en `controller/IndexController.java`, `controller/LoginController.java` y `templates/login.html` / `templates/register.html`.

## Cuenta y credenciales

`UserAccount` persiste correo único, contraseña codificada y rol. El rol inicial es `USER`. `UserAccountService.register` recorta y pasa el correo a minúsculas, rechaza correos duplicados y codifica la contraseña mediante el `PasswordEncoder` proporcionado por Spring Security (`BCryptPasswordEncoder`). La base de datos nunca recibe la contraseña en texto claro.

El mismo servicio implementa `UserDetailsService`. Para autenticar o validar un JWT, busca el correo normalizado en el repositorio y convierte la cuenta en `AuthenticatedUserDetails`. Esa clase expone la contraseña codificada y la autoridad `ROLE_USER` o `ROLE_ADMIN` para Spring Security.

## Creación del token

`JwtProperties` recibe el prefijo `security.jwt`. Al construir `JwtService`:

1. El secreto se convierte a bytes UTF-8.
2. La aplicación exige una clave de al menos 32 bytes y una expiración positiva.
3. Se construye una clave HMAC con `Keys.hmacShaKeyFor`.
4. `generateToken` firma un token con correo en `sub`, fecha de emisión (`iat`) y expiración (`exp`).

Los roles no se añaden a los claims. El método `createTokenCookie` crea una cookie `JWT` con `HttpOnly`, `SameSite=Lax`, `Path=/`, `Max-Age` igual a la vida del token y `Secure` según si la petición llegó por HTTPS. `clearTokenCookie` escribe el mismo nombre y ruta con duración cero.

## Validación de la petición

`JwtAuthenticationFilter` extiende `OncePerRequestFilter` y se instala antes de `UsernamePasswordAuthenticationFilter` en ambas cadenas de seguridad.

1. Busca una cookie cuyo nombre sea `JWT`.
2. Si no hay cookie, continúa la cadena sin autenticar.
3. Si existe, `JwtService.extractUsername` analiza los claims y verifica la firma con la clave local.
4. Carga el usuario por correo mediante `UserDetailsService`.
5. `isTokenValid` comprueba que el sujeto coincide con el username y que `exp` es posterior a la hora actual.
6. Si es válido y aún no hay autenticación, crea `UsernamePasswordAuthenticationToken` con las autoridades cargadas de la cuenta y lo guarda en `SecurityContextHolder`.
7. Si el token está mal formado, ha fallado la verificación, el usuario no existe o el parseo falla, limpia el contexto y deja que la cadena de autorización resuelva la petición.

La firma garantiza que el contenido no se haya cambiado y que lo haya emitido alguien con el secreto. No cifra el token. Los claims deben tratarse como datos legibles por quien obtenga el token.

## Dos entradas de autenticación

### API REST

`AuthController` publica rutas bajo `/api/auth`:

- `POST /api/auth/register`: valida `RegisterRequest`, crea cuenta y devuelve `201` con `Set-Cookie`.
- `POST /api/auth/login`: valida `LoginRequest`, invoca `AuthenticationManager` y devuelve `200` con `Set-Cookie`.

Ambos retornan `ResponseEntity<Void>`: sus cuerpos están vacíos. El navegador almacena la cookie y la envía en futuras peticiones. `AuthResponse` existe en el código, pero no se usa aquí.

Ejemplo del cuerpo JSON de registro:

```json
{
  "email": "ana@example.com",
  "password": "una-clave-segura"
}
```

El endpoint de API puede usarse desde una aplicación web o herramienta HTTP; sin embargo, para conservar la cookie se debe mantener un cookie jar y cumplir la protección CSRF configurada. Un cliente que quiera autenticación por `Authorization: Bearer` necesitaría soporte explícito en el filtro, que hoy no existe.

### Páginas web

`GET /login` devuelve `login.html`. El formulario Thymeleaf envía `username`, `password` y CSRF a `POST /login`, que gestiona Spring Security mediante `formLogin`. El success handler genera la cookie JWT y redirige a `/dashboard`. El controlador MVC `POST /register` registra la cuenta y redirige a `/login`; a diferencia del endpoint JSON, no crea la cookie en ese paso.

El formulario de logout de `navbar.html` envía `POST /logout` con el token CSRF. El success handler elimina la cookie y redirige al inicio.

## Cadenas y reglas de seguridad

`SecurityConfig` define dos `SecurityFilterChain`:

1. `apiSecurity`, con `@Order(1)` y matcher `/api/**`, mantiene la API sin sesión HTTP (`STATELESS`), instala autenticación JWT y aplica autorización por ruta. Login y registro API son públicos. GET y demás rutas requieren autenticación; POST, PUT y DELETE para personas requieren rol `ADMIN`. Los errores no autenticados de API responden `401`.
2. `webSecurity`, con `@Order(2)`, atiende el resto, usa el formulario de login y protege páginas según roles: dashboard acepta `USER` o `ADMIN`, y `/admin` exige `ADMIN`.

Ambas cadenas usan `CookieCsrfTokenRepository`. Los templates insertan `${_csrf.parameterName}` y `${_csrf.token}` en los formularios. Esta protección es relevante porque el navegador adjunta cookies de forma automática también a peticiones que cambian datos.

## Configuración actual y verificaciones necesarias

### Secreto JWT

`application.properties` contiene actualmente un valor literal para `security.jwt.secret`. No está enlazado a `${JWT_SECRET:...}`. Por tanto, exportar `JWT_SECRET` por sí solo no cambia el secreto utilizado por esta configuración. Antes de usarlo fuera de desarrollo:

- elimina el secreto de ejemplo del archivo;
- configura el valor desde un secreto externo, por ejemplo con `security.jwt.secret=${JWT_SECRET}`;
- usa una clave aleatoria de al menos 32 bytes y no la subas al repositorio;
- activa HTTPS para que las cookies lleven `Secure`.

El valor `security.jwt.expiration-ms` sí admite `JWT_EXPIRATION_MS` y por defecto configura 900000 ms (15 minutos). `JwtProperties` también declara ese valor por defecto.

### Documentación Bearer frente al código

El README principal describe el uso de `Authorization: Bearer <accessToken>` y una respuesta con `accessToken`. La implementación actual no coincide con esa descripción:

- `AuthController` solo escribe la cookie y devuelve el cuerpo vacío.
- `JwtAuthenticationFilter.extractToken` solo busca la cookie `JWT`; no lee `Authorization`.
- `AuthResponse` y los métodos auxiliares de expiración no están conectados a la respuesta.

En consecuencia, el flujo implementado es cookie JWT, no bearer token para clientes sin cookies. Para cambiar a Bearer habría que actualizar emisión/respuesta y filtro, considerar CSRF si se mantiene soporte de cookies, y añadir pruebas de integración para ambos mecanismos.

### Pruebas existentes

`src/test/java/com/apc21z/proyectv4/jwt/JwtServiceTest.java` comprueba que el token generado identifica y valida al usuario correcto y que se rechazan secretos demasiado cortos. Conviene ampliar las pruebas para expiración, cookie y flags, tokens mal firmados, filtro, permisos por rol, CSRF y endpoints de login/registro.

## Recorrido rápido para depurar

1. Confirmar que las propiedades JWT se enlazan y que la clave cumple el mínimo de longitud.
2. En login válido, revisar que la respuesta incluye `Set-Cookie: JWT=...`.
3. En la siguiente petición, confirmar que el navegador envía la cookie.
4. Revisar que firma, `exp` y correo corresponden al usuario existente.
5. Comprobar la autoridad cargada (`ROLE_USER` o `ROLE_ADMIN`) y la regla de ruta aplicable.
6. Para POST/PUT/DELETE desde formularios o clientes web, comprobar también el token CSRF.

Para el flujo explicado sin entrar tanto en clases, consulta [JWT: funcionamiento entre backend y frontend](JWT-FUNCIONAMIENTO.md).