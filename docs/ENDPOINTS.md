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
| Entidad | `rest/model/Book.java` | Representar la tabla y sus datos. |
| DTO | `rest/model/dto/BookCreateDTO.java`, `BookResponseDTO.java` | Definir los datos de entrada y salida de la API. |
| Repositorio | `rest/repository/BookRepository.java` | Acceder a los datos mediante Spring Data JPA. |
| Servicio | `rest/service/BookService.java` | Coordinar operaciones, reglas y transacciones. |
| Mapper | `rest/model/mapper/BookMapper.java` | Convertir entre entidad y DTO. |
| Controlador | `rest/ApiBookController.java` | Exponer las rutas y gestionar HTTP. |

## 1. Crear la entidad

Crea `rest/model/Book.java`. `@Entity` registra la clase en JPA y `@GeneratedValue` deja que la base de datos genere el identificador.

```java
package com.apc21z.proyectv4.rest.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true)
    private String isbn;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "book_id")
    @OrderColumn(name = "page_order")
    private List<BookPage> pages = new ArrayList<>();
}
```

Cada página se persiste como `BookPage` en `book_pages`, con ID generado, título y texto. `@OrderColumn` conserva el orden de la lista. Si una base de datos conserva el antiguo campo numérico `pages`, `ddl-auto=update` no lo elimina ni convierte ese número en contenido: haz una copia de seguridad, migra lo que corresponda y elimina la columna antigua antes de crear libros nuevos.

```java
package com.apc21z.proyectv4.rest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String text;
}
```

## 2. Crear el DTO

El proyecto separa el DTO de entrada del de respuesta. `@Valid` en el controlador activa las restricciones de Jakarta Validation.

```java
package com.apc21z.proyectv4.rest.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookCreateDTO(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String author,
    @NotBlank @Size(max = 255) String isbn,
    @NotNull @Valid List<BookPageDTO> pages) {
}
```

```java
```java
package com.apc21z.proyectv4.rest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookPageDTO(
        Long id,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String text) {
}
```

`BookResponseDTO` devuelve el `id` generado del libro y `pages` como una lista de `BookPageDTO`; cada página incluye su ID, título y texto. El ID se omite al crear páginas y lo asigna la base de datos. La lista es obligatoria y `@Valid` aplica las restricciones a cada página. Los títulos/textos no pueden quedar vacíos; los títulos admiten hasta 255 caracteres.

Los DTO de personas aplican reglas similares: `PersonDTO` exige `firstName` y `lastName` (máximo 255 caracteres), deja `profession` opcional (máximo 255) y valida de forma anidada cada `SkillDTO`, cuyo `name` es obligatorio y admite hasta 255 caracteres.

## 3. Crear el repositorio

Crea `rest/repository/BookRepository.java`. Spring Data implementa las operaciones CRUD heredadas de `JpaRepository`.

```java
package com.apc21z.proyectv4.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apc21z.proyectv4.rest.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
```

Ya están disponibles métodos como `findAll`, `findById`, `save`, `existsById` y `deleteById`. Para búsquedas adicionales se pueden declarar métodos derivados, por ejemplo `findByAuthorIgnoreCase(String author)`.

## 4. Usar el servicio

`rest/service/BookService.java` mantiene la lógica fuera del controlador y define las transacciones. Al actualizar, reemplaza la lista de páginas recibida; `orphanRemoval` elimina las páginas anteriores.

```java
package com.apc21z.proyectv4.rest.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.repository.BookRepository;

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
            existing.setIsbn(changes.getIsbn());
            existing.getPages().clear();
            existing.getPages().addAll(changes.getPages());
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

Crea `rest/model/mapper/BookMapper.java`. `@Mapper(componentModel = "spring")` hace que MapStruct genere una implementación registrada como bean de Spring. El procesador de MapStruct ya está configurado en el `pom.xml` del proyecto.

```java
package com.apc21z.proyectv4.rest.model.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.model.BookPage;
import com.apc21z.proyectv4.rest.model.dto.BookCreateDTO;
import com.apc21z.proyectv4.rest.model.dto.BookPageDTO;
import com.apc21z.proyectv4.rest.model.dto.BookResponseDTO;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookResponseDTO toDto(Book book);
    List<BookResponseDTO> toDto(List<Book> books);
    BookPageDTO toDto(BookPage page);

    @Mapping(target = "id", ignore = true)
    Book toEntity(BookCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    BookPage toEntity(BookPageDTO dto);
}
```

Se ignoran los IDs al crear el libro y sus páginas para que el cliente no elija las claves primarias. MapStruct usa los métodos de `BookPageDTO` para convertir automáticamente la lista anidada.

## 6. Crear el controlador REST

Crea `rest/ApiBookController.java`. `@Valid` activa las validaciones del DTO de entrada. La API usa `201 Created` al crear, `404 Not Found` cuando no existe el recurso y `204 No Content` al borrar correctamente.

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
import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.model.dto.BookCreateDTO;
import com.apc21z.proyectv4.rest.model.dto.BookResponseDTO;
import com.apc21z.proyectv4.rest.model.mapper.BookMapper;
import com.apc21z.proyectv4.rest.service.BookService;

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
    public List<BookResponseDTO> getBooks() {
        return mapper.toDto(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable Long id) {
        return service.findById(id).map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookCreateDTO dto) {
        Book saved = service.save(mapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Long id, @Valid @RequestBody BookCreateDTO dto) {
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

Las rutas `/api/**` usan cookie JWT y CSRF, configurados en `security/jwt/config/SecurityConfig.java`. En la configuración actual, el login y el registro son públicos; las demás rutas requieren autenticación. Las operaciones de escritura de libros y personas requieren rol `ADMIN`.

Si añades otro recurso, declara sus reglas de autorización en `apiSecurity` antes de `.anyRequest().authenticated()`. Los clientes HTTP deben conservar la cookie JWT y enviar el token CSRF en las peticiones que modifican datos; la aplicación no acepta actualmente `Authorization: Bearer`.

## 8. Compilar y probar

1. Inicia MySQL y configura/ejecuta la aplicación según el [README principal](../README.md). El puerto configurado por defecto es `8080`.
2. Ejecuta `./mvnw test`.
3. En Postman, importa [`ProyectV4.postman_collection.json`](../postman/ProyectV4.postman_collection.json) y ejecuta primero `01 - Obtener CSRF`; la colección conserva las cookies y añade el encabezado CSRF.
4. Comprueba cada operación: listado, identificador existente e inexistente, creación, actualización y borrado.
5. Comprueba validaciones (por ejemplo, título vacío o de más de 255 caracteres) y permisos: un usuario sin rol `ADMIN` no puede escribir libros o personas.

El cuerpo JSON para crear un libro incluye sus datos y una lista de páginas. Cada página enviada necesita título y texto; el ID se genera en el servidor:

```json
{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "pages": [
        {
            "title": "Introducción",
            "text": "Texto de ejemplo."
        }
    ]
}
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