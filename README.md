# TPO — Aplicaciones Interactivas

E-commerce de venta de **juegos de mesa**.
UADE — Ingeniería Informática — 2º cuatrimestre 2026.

## Estructura

```
tpo-backend/     API REST con Spring Boot + Spring Data JPA + MySQL
```

El frontend se va a sumar como `tpo-frontend/` cuando lo veamos en clase.

## Estado

| Parte | Estado |
|---|---|
| API REST | ✅ completa |
| Capa de persistencia | ✅ completa |
| Catálogo, búsqueda y filtros | ✅ |
| Carrito y checkout | ✅ |
| Gestión de productos, stock y descuentos | ✅ |
| Seguridad (login, register, permisos) | ⏳ pendiente, se integra con el código de la cátedra |
| Frontend | ⏳ pendiente |

## Cómo arrancar

Toda la documentación está en **[`tpo-backend/README.md`](tpo-backend/README.md)**:
requisitos, configuración de la base, cómo levantar el proyecto, la lista de
endpoints y la colección de Insomnia para probarlo.

Resumen rápido:

1. Crear el esquema en MySQL: `CREATE DATABASE marketplace;`
2. Copiar `tpo-backend/src/main/resources/application.properties.example` a
   `application.properties` y poner ahí la contraseña de tu MySQL.
3. Abrir `tpo-backend` en VS Code y ejecutar `DemoApplication.java`.

La API queda en `http://localhost:4002`.

> `application.properties` **no se sube al repo**: cada uno configura su propia
> contraseña de base de datos localmente.
