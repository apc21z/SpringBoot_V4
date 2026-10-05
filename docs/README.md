# Guías del proyecto

Documentación práctica de las páginas web, la API REST y la autenticación del proyecto.

## Aplicación

- [README principal](../README.md): arquitectura, rutas web, endpoints API, validaciones y ejecución local.
- [Colección Postman](../postman/ProyectV4.postman_collection.json): autenticación JWT en cookie y peticiones protegidas.

## Endpoints

- [Guía y contrato de endpoints REST](ENDPOINTS.md): rutas, estructura actual de libros, DTOs, validaciones, seguridad y pruebas.

## Autenticación JWT

- [Funcionamiento entre backend y frontend](JWT-FUNCIONAMIENTO.md): flujo de login, cookies, CSRF, roles y caducidad.
- [Implementación en este proyecto](JWT-IMPLEMENTACION.md): clases, filtros, configuración y observaciones sobre el comportamiento actual.