# JWT: funcionamiento entre backend y frontend

Esta guía describe el flujo de autenticación del proyecto. La aplicación usa páginas Thymeleaf renderizadas por el servidor y también expone una API REST. En ambos casos, el navegador recibe un JWT dentro de una cookie `HttpOnly`; no hay código frontend que lea el token o lo guarde en `localStorage`.

## Qué es un JWT

Un JSON Web Token (JWT) es una credencial compacta que el servidor firma. Esta aplicación incluye en el token:

- `sub`: el correo del usuario, que identifica al sujeto.
- `iat`: cuándo se emitió.
- `exp`: cuándo caduca.

El token está firmado, no cifrado. Su contenido puede leerse si alguien obtiene el valor; la firma permite detectar modificaciones. No se deben incluir contraseñas ni datos secretos en sus claims.

## Flujo del navegador y las páginas Thymeleaf

```text
Formulario del navegador
        | correo + contraseña + CSRF
        v
Spring Security autentica las credenciales
        | credenciales válidas
        v
Backend firma JWT y responde Set-Cookie: JWT=...
        | el navegador almacena la cookie HttpOnly
        v
Navegación a /dashboard; el navegador envía JWT automáticamente
        | filtro valida firma, expiración y usuario
        v
Spring Security aplica autenticación y roles
```

1. La persona abre `/login`. `login.html` muestra un formulario que hace `POST /login` con usuario (correo), contraseña y token CSRF oculto.
2. El filtro de login de Spring Security delega la comprobación al proveedor de autenticación, que carga la cuenta y compara la contraseña con BCrypt.
3. Si el login funciona, `SecurityConfig` genera un JWT, lo añade como cookie `JWT` y redirige a `/dashboard`.
4. En las siguientes peticiones, el navegador envía la cookie con la solicitud. JavaScript no necesita leerla; su atributo `HttpOnly` impide ese acceso desde scripts.
5. `JwtAuthenticationFilter` valida la cookie y crea el `Authentication` de Spring Security. Las reglas de autorización deciden si el usuario puede acceder a la ruta.
6. El formulario de cierre de sesión hace `POST /logout` con CSRF. Spring responde borrando la cookie y redirigiendo a `/`.

El registro web es diferente: el formulario de `/register` crea la cuenta y redirige a `/login`; no inicia sesión automáticamente. Es la persona quien debe enviar después el formulario de login.

## Flujo de la API REST

La API ofrece `POST /api/auth/register` y `POST /api/auth/login`. Ambos reciben JSON. El registro valida correo y contraseña (entre 8 y 72 caracteres); el login valida credenciales. Al completarse, el servidor devuelve `Set-Cookie: JWT=...`; el navegador guarda la cookie y la envía automáticamente a las siguientes rutas `/api/**`.

El registro responde `201 Created` con un `UserAccountDTO` que contiene `id`, `email` y `roles`. El login responde `200 OK` sin cuerpo. Ninguna respuesta incluye la contraseña ni un token en JSON: la autenticación se mantiene en la cookie, no en una cabecera `Authorization: Bearer ...`. Los cambios de estado están protegidos además con CSRF.

Ejemplo de petición de login:

```http
POST /api/auth/login
Content-Type: application/json

{"email":"ana@example.com","password":"una-clave-segura"}
```

La respuesta satisfactoria incluye una cabecera `Set-Cookie` con `JWT`, `HttpOnly`, `SameSite=Lax`, `Path=/` y duración según la expiración configurada. El atributo `Secure` se activa cuando la solicitud llega al servidor como HTTPS.

El cuerpo de registro tiene esta forma:

```json
{
        "id": 42,
        "email": "ana@example.com",
        "roles": ["USER"]
}
```

## Validación en cada petición

El filtro de JWT se ejecuta antes de la autenticación estándar de Spring Security y, en este proyecto, busca el token en la cookie llamada `JWT`. Cuando encuentra un token:

1. Verifica su firma con la clave HMAC configurada y analiza sus claims.
2. Obtiene el correo de `sub`.
3. Carga de la base de datos el usuario correspondiente.
4. Comprueba que el token pertenece a ese usuario y no ha caducado.
5. Si todo es válido, coloca la autenticación y las autoridades del usuario en el contexto de seguridad de la petición.

La aplicación no guarda una sesión HTTP tradicional; cada petición protegida vuelve a presentar y validar la cookie. Un token inválido no autentica al usuario. Las rutas protegidas de API devuelven `401` si no hay autenticación; un usuario autenticado sin el rol requerido no tiene autorización.

## Roles y autorización

Los roles no se incluyen como claims en el JWT de esta implementación: se cargan desde la cuenta al validar el token. Una cuenta puede tener varios roles; el registro asigna `USER` por defecto. Las reglas de `SecurityConfig` permiten `/dashboard` a `USER` y `ADMIN`, y `/admin` solo a `ADMIN`. Para la API, `/api/auth/register` y `/api/auth/login` son públicas; las rutas restantes requieren autenticación, y las operaciones de escritura de libros y personas requieren `ADMIN`.

Autenticación responde «quién eres»; autorización responde «qué puedes hacer». Tener un JWT válido no da permiso automáticamente para todas las rutas.

## CSRF y cookies

Una cookie se envía automáticamente por el navegador. Por eso, una autenticación basada en cookie necesita protección CSRF para las operaciones que modifican estado. Las dos cadenas de seguridad usan `CookieCsrfTokenRepository`; las vistas Thymeleaf insertan el token CSRF en un campo oculto en los formularios. Los clientes HTTP que hagan POST, PUT o DELETE deben enviar también el token CSRF esperado por Spring Security.

La cookie JWT es `HttpOnly`, `SameSite=Lax` y tiene `Path=/`. `HttpOnly` reduce la exposición a JavaScript, pero no reemplaza CSRF ni HTTPS. En producción debe servirse la aplicación por HTTPS para que la cookie lleve `Secure`.

## Caducidad y cierre de sesión

El token caduca según `security.jwt.expiration-ms`; el valor por defecto de `JwtProperties` es 900000 ms (15 minutos). El cierre de sesión web elimina la cookie del navegador. Como el servidor no mantiene una lista de revocación, borrar la cookie no invalida una copia robada del token: esa copia sigue siendo válida hasta su expiración, salvo que se añada un mecanismo de revocación.

## Esquema de rutas

| Flujo | Ruta | Resultado de autenticación |
| --- | --- | --- |
| Ver formulario web | `GET /login` | Público. |
| Login web | `POST /login` | Cookie JWT y redirección a `/dashboard`. |
| Registro web | `POST /register` | Crea la cuenta y redirige a `/login`. |
| Login de API | `POST /api/auth/login` | Cookie JWT; `200` con cuerpo vacío. |
| Registro de API | `POST /api/auth/register` | Crea cuenta, establece cookie JWT y devuelve `UserAccountDTO`; `201`. |
| Página privada | `GET /dashboard` | `USER` o `ADMIN`. |
| Administración | `GET /admin` | Solo `ADMIN`. |
| Salir de web | `POST /logout` | Elimina cookie y redirige. |

Para los detalles de clases y configuración, consulta [JWT: implementación en este proyecto](JWT-IMPLEMENTACION.md).