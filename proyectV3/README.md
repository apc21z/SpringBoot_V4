# ProyectV3

Aplicación web de ejemplo construida con Java 21 y Spring Boot 4. Incluye páginas Thymeleaf, un API REST y persistencia de personas y skills en MySQL.

## Tecnologías

- Spring MVC y Thymeleaf para las páginas web.
- Spring Data JPA para el acceso a datos.
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

Comprueba que el contenedor esté listo:

```bash
docker compose ps
```

Espera a que el servicio `db` aparezca como `healthy`. Compose crea la base `proyectv3` y conserva los datos en el volumen `mysql_data`. Al arrancar, Hibernate crea o actualiza las tablas según las entidades JPA.

## Ejecutar la aplicación

Con MySQL en estado `healthy`, ejecuta:

```bash
./mvnw spring-boot:run
```

En Windows usa `mvnw.cmd spring-boot:run`. La aplicación queda disponible en <http://localhost:8080>.

La configuración local de MySQL está en `src/main/resources/application.properties` y coincide con `compose.yaml`: host `localhost`, puerto `3306`, base `proyectv3`, usuario `root` y contraseña `password`. Estas credenciales son únicamente para desarrollo local; no las uses en producción.

## Páginas

- `/`: inicio y estado de conexión a la base de datos.
- `/people`: formulario para crear personas y directorio con búsqueda por skill. Escribe varias skills separadas por comas.
- `/thymeTest`: ejemplos de Thymeleaf y manejo de skills en la sesión actual.

## API REST

La ruta principal es `/api/people`; `/api/person` se mantiene como alias.

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