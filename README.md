# Mesa Llena — TPO Aplicaciones Interactivas

E-commerce de venta de **juegos de mesa**. Tienda única: nosotros somos el
vendedor y los usuarios que se registran son compradores.
UADE — Ingeniería Informática — 2º cuatrimestre 2026.

## Estructura

```
tpo-backend/     API REST con Spring Boot + Spring Data JPA + MySQL
client/          Interfaz de usuario con React + Vite
```

## Estado

| Parte | Estado |
|---|---|
| API REST | ✅ |
| Capa de persistencia | ✅ |
| Catálogo, búsqueda y filtros | ✅ |
| Carrito y checkout | ✅ |
| Gestión de productos, stock y descuentos | ✅ |
| Seguridad: registro, login y roles con JWT | ✅ |
| Interfaz de usuario conectada al backend | ✅ |

## Cómo arrancar

Hacen falta tres cosas corriendo a la vez: MySQL, el backend y el frontend.

**1. La base.** Crear el esquema una sola vez:

```sql
CREATE DATABASE ecommerce;
```

**2. El backend.** Copiar `tpo-backend/src/main/resources/application.properties.example`
a `application.properties` y poner ahí la contraseña del MySQL propio. Después,
desde `tpo-backend/`:

```bash
./mvnw spring-boot:run
```

Queda en `http://localhost:4002`.

**3. El frontend.** Desde `client/`:

```bash
npm install
```

```bash
npm run dev
```

Abre en `http://localhost:5173`, que es el puerto que el backend tiene habilitado
en el CORS.

> `application.properties` **no se sube al repo**: cada uno configura su propia
> contraseña de base de datos localmente.

## Documentación

- **[`tpo-backend/README.md`](tpo-backend/README.md)** — requisitos, configuración
  de la base, lista de endpoints y la colección de Insomnia para probar la API.
- **[`client/README.md`](client/README.md)** — estructura del frontend, las vistas,
  los roles y las decisiones de diseño.
