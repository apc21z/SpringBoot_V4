# ProyectV4

Aplicación web de ejemplo construida con Java 21 y Spring Boot 4. Incluye páginas Thymeleaf, un API REST y persistencia de personas y skills en MySQL.

## Tecnologías

- Spring MVC y Thymeleaf para las páginas web.
- Spring Data JPA para el acceso a datos.
- Spring Security y JWT para autenticar usuarios de la API.
- MySQL 8.4 para guardar personas y sus skills.
- Maven Wrapper para compilar y ejecutar el proyecto.

## Estructura

- `controller`: controladores web separados por responsabilidad (`HomeController`, `PeopleController` y `ThymeTestController`).
- `rest`: controladores del API REST.
- `service`: lógica de aplicación y coordinación con los repositorios.
- `repository`: interfaces Spring Data JPA.
- `model`: entidades `Person` y `Skill`.
- `src/main/resources/templates`: vistas Thymeleaf.
- `src/main/resources/static`: recursos estáticos, como CSS.

## Requisitos

- JDK 21.
- Docker con Docker Compose.

## Iniciar la base de datos

Desde la raíz del proyecto, inicia MySQL:

```bash
docker compose up -d db
```

V4 publica MySQL en el puerto `3307` para poder convivir con la base del V3. Comprueba que el contenedor esté listo:

```bash
docker compose ps
```

Espera a que el servicio `db` aparezca como `healthy`. Compose crea la base `proyectv4` y conserva los datos en el volumen `mysql_data`. Al arrancar, Hibernate crea o actualiza las tablas según las entidades JPA.

## Ejecutar la aplicación

Con MySQL en estado `healthy`, ejecuta:

```bash
./mvnw spring-boot:run
```

En Windows usa `mvnw.cmd spring-boot:run`. La aplicación queda disponible en <http://localhost:8081>.

La configuración local de MySQL está en `src/main/resources/application.properties` y coincide con `compose.yaml`: host `localhost`, puerto `3307`, base `proyectv4`, usuario `root` y contraseña `password`. Estas credenciales son únicamente para desarrollo local; no las uses en producción.

Antes de arrancar la aplicación, define una clave aleatoria de al menos 32 bytes:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
./mvnw spring-boot:run
```

`JWT_EXPIRATION_MS` es opcional y por defecto vale `900000` (15 minutos). No guardes `JWT_SECRET` en el repositorio ni uses una clave de desarrollo en producción.

## Páginas

- `/`: inicio y estado de conexión a la base de datos.
- `/people`: formulario para crear personas y directorio con búsqueda por skill. Escribe varias skills separadas por comas.
- `/thymeTest`: ejemplos de Thymeleaf y manejo de skills en la sesión actual.

## API REST

La ruta principal es `/api/people`; `/api/person` se mantiene como alias.

La API de personas requiere `Authorization: Bearer <accessToken>`. El registro y el login son públicos; ambos devuelven un token.

Registro de usuario (contraseña mínima de 8 caracteres):

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"una-clave-segura"}'
```

Login:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"una-clave-segura"}'
```

Envía el campo `accessToken` de la respuesta para acceder a `/api/people`. Las contraseñas se almacenan con BCrypt; el secreto de firma se configura fuera del código.

```bash
curl http://localhost:8081/api/people \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

| Método | Ruta | Acción |
| --- | --- | --- |
| `GET` | `/api/people` | Listar personas |
| `GET` | `/api/people/{id}` | Obtener una persona |
| `POST` | `/api/people` | Crear una persona |
| `PUT` | `/api/people/{id}` | Actualizar una persona |
| `DELETE` | `/api/people/{id}` | Eliminar una persona |

Ejemplo de cuerpo JSON para crear una persona:

```json
{
  "firstName": "Ana",
  "lastName": "Ruiz",
  "profession": "Desarrolladora",
  "skills": [
    { "name": "Java" },
    { "name": "Spring Boot" }
  ]
}
```

## Detener MySQL

Para detener el contenedor sin borrar los datos:

```bash
docker compose down
```

Para eliminar también el volumen y todos los datos guardados (acción irreversible):

```bash
docker compose down -v
```