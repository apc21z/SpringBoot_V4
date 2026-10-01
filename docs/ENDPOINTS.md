# Añadir un endpoint REST

Esta guía explica cómo añadir un nuevo objeto al API REST de este proyecto. El ejemplo usa un recurso `Book` y sigue el patrón de `Person`: entidad JPA, DTO, repositorio, servicio, mapper y controlador.

Los ejemplos corresponden a `src/main/java/com/apc21z/proyectv4/`.

## Flujo de una petición

```text
HTTP/JSON -> Controller -> Service -> Repository -> JPA/MySQL
```

El controlador recibe la petición y devuelve el estado HTTP adecuado. El servicio aplica la lógica de negocio y las transacciones. El repositorio persiste las entidades. El mapper convierte entre DTOs (contrato público) y entidades (modelo persistido).

| Componente | Ruta | Responsabilidad |
| --- | --- | --- |
| Entidad | `model/Book.java` | Representar la tabla y sus datos. |
| DTO | `rest/dto/BookDTO.java` | Definir los datos de entrada y salida de la API. |
| Repositorio | `repository/BookRepository.java` | Acceder a los datos mediante Spring Data JPA. |
| Servicio | `service/BookService.java` | Coordinar operaciones, reglas y transacciones. |
| Mapper | `rest/mapper/BookMapper.java` | Convertir entre entidad y DTO. |
| Controlador | `rest/ApiBookController.java` | Exponer las rutas y gestionar HTTP. |

## 1. Crear la entidad

Crea `model/Book.java`. `@Entity` registra la clase en JPA y `@GeneratedValue` deja que la base de datos genere el identificador.

```java
package com.apc21z.proyectv4.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
}
```

En desarrollo, Hibernate actualiza el esquema según la configuración del proyecto. Para producción, revisa la estrategia de migración de base de datos antes de añadir o cambiar tablas.

## 2. Crear el DTO

Crea `rest/dto/BookDTO.java`. El DTO separa el contrato JSON del esquema de la entidad. Las restricciones de Jakarta Validation se aplican al validar las peticiones.

```java
package com.apc21z.proyectv4.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record BookDTO(
        Long id,
        @NotBlank String title,
        @NotBlank String author) {
}
```

El cliente omite `id` al crear un libro; la respuesta incluye el identificador generado.

## 3. Crear el repositorio

Crea `repository/BookRepository.java`. Spring Data implementa las operaciones CRUD heredadas de `JpaRepository`.

```java
package com.apc21z.proyectv4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apc21z.proyectv4.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
```

Ya están disponibles métodos como `findAll`, `findById`, `save`, `existsById` y `deleteById`. Para búsquedas adicionales se pueden declarar métodos derivados, por ejemplo `findByAuthorIgnoreCase(String author)`.

## 4. Crear el servicio

Crea `service/BookService.java`. Como en `PersonService`, el servicio mantiene la lógica fuera del controlador y define las transacciones.

```java
package com.apc21z.proyectv4.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.apc21z.proyectv4.model.Book;
import com.apc21z.proyectv4.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Book save(Book book) {
        return repository.save(book);
    }

    @Transactional
    public Optional<Book> update(Long id, Book changes) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(changes.getTitle());
            existing.setAuthor(changes.getAuthor());
            return repository.save(existing);
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
```

## 5. Crear el mapper MapStruct

Crea `rest/mapper/BookMapper.java`. `@Mapper(componentModel = "spring")` hace que MapStruct genere una implementación registrada como bean de Spring. El procesador de MapStruct ya está configurado en el `pom.xml` del proyecto.

```java
package com.apc21z.proyectv4.rest.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.apc21z.proyectv4.model.Book;
import com.apc21z.proyectv4.rest.dto.BookDTO;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDTO toDto(Book book);
    List<BookDTO> toDto(List<Book> books);

    @Mapping(target = "id", ignore = true)
    Book toEntity(BookDTO dto);
}
```

Se ignora el `id` al crear la entidad para que el cliente no elija la clave primaria que se persiste.

## 6. Crear el controlador REST

Crea `rest/ApiBookController.java`. `@Valid` activa las validaciones del DTO. La API usa `201 Created` al crear, `404 Not Found` cuando no existe el recurso y `204 No Content` al borrar correctamente.

```java
package com.apc21z.proyectv4.rest;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.apc21z.proyectv4.model.Book;
import com.apc21z.proyectv4.rest.dto.BookDTO;
import com.apc21z.proyectv4.rest.mapper.BookMapper;
import com.apc21z.proyectv4.service.BookService;

@RestController
@RequestMapping("/api/books")
public class ApiBookController {
    private final BookService service;
    private final BookMapper mapper;

    public ApiBookController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<BookDTO> getBooks() {
        return mapper.toDto(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        return service.findById(id).map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO dto) {
        Book saved = service.save(mapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Long id, @Valid @RequestBody BookDTO dto) {
        return service.update(id, mapper.toEntity(dto)).map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
```

### Rutas resultantes

| Método | Ruta | Resultado |
| --- | --- | --- |
| `GET` | `/api/books` | Lista de libros (`200`). |
| `GET` | `/api/books/{id}` | Libro (`200`) o no encontrado (`404`). |
| `POST` | `/api/books` | Libro creado (`201`). |
| `PUT` | `/api/books/{id}` | Libro actualizado (`200`) o no encontrado (`404`). |
| `DELETE` | `/api/books/{id}` | Sin contenido (`204`) o no encontrado (`404`). |

## 7. Configurar la seguridad

Las rutas `/api/**` usan JWT en `jwt/config/SecurityConfig.java`. En la configuración actual, las operaciones de escritura de personas requieren rol `ADMIN`; las demás rutas API requieren autenticación. Para aplicar la misma política a libros, añade estas reglas a `apiSecurity`, antes de `.anyRequest().authenticated()`:

```java
.requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
```

Así, cualquier usuario autenticado puede consultar libros y solo `ADMIN` puede crear, actualizar o borrar. Ajusta las reglas si el requisito de acceso del nuevo recurso es diferente. No olvides declarar explícitamente con `permitAll()` las rutas que deban ser públicas.

## 8. Compilar y probar

1. Inicia MySQL y configura/ejecuta la aplicación según el [README principal](../README.md). Para arrancar, define `JWT_SECRET` tal como se describe allí.
2. Ejecuta `./mvnw test`.
3. Registra o autentica un usuario con `POST /api/auth/register` o `POST /api/auth/login` y utiliza el token devuelto como `Authorization: Bearer <token>`.
4. Comprueba cada operación: listado, identificador existente e inexistente, creación, actualización y borrado.
5. Comprueba validaciones (por ejemplo, título vacío) y permisos: un usuario sin rol ADMIN no debe poder escribir si se aplicaron las reglas anteriores.

Ejemplo de creación:

```bash
curl -X POST http://localhost:8081/api/books \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin"}'
```

## Lista de comprobación

- [ ] Entidad con clave primaria y tabla JPA.
- [ ] DTO con los campos públicos y validaciones necesarias.
- [ ] Repositorio que extiende `JpaRepository<Entidad, Long>`.
- [ ] Servicio con reglas de negocio y transacciones.
- [ ] Mapper MapStruct para entidad y DTO.
- [ ] Controlador con rutas y códigos HTTP adecuados.
- [ ] Configuración JWT actualizada según los roles requeridos.
- [ ] Pruebas para éxito, errores, validación y autorización.

Como referencia del código existente, consulta `Person`, `PersonDTO`, `PersonRepository`, `PersonService`, `PersonMapper` y `ApiPersonController`.