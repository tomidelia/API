# Mesa Llena — TPO Aplicaciones Interactivas (backend)

E-commerce de venta de **juegos de mesa**. Tienda única: nosotros somos el
vendedor y los usuarios que se registran son compradores.
UADE — Ingeniería Informática — 2º cuatrimestre 2026.

## Los dos repositorios

El proyecto son dos aplicaciones separadas, cada una en su propio repositorio.
Se levantan a la par, pero se desarrollan y se publican por separado.

| | Repositorio |
|---|---|
| **Backend** (este) — API REST con Spring Boot + Spring Data JPA + MySQL | https://github.com/tomidelia/API |
| **Frontend** — interfaz de usuario con React + Vite | https://github.com/tomasszk/mesa-llena-frontend |

```
tpo-backend/     El proyecto de Spring Boot
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
| Interfaz de usuario conectada al backend | ✅ (en el otro repo) |

## Cómo arrancar

Hacen falta tres cosas corriendo a la vez: MySQL, el backend y el frontend.

**1. La base.** Crear el esquema una sola vez:

```sql
CREATE DATABASE ecommerce;
```

**2. El backend.** Copiar `tpo-backend/src/main/resources/application.properties.example`
a `application.properties` y completar la configuración local de MySQL. Después,
desde `tpo-backend/`:

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS/Linux:**

```bash
./mvnw spring-boot:run
```

Queda en `http://localhost:4002`.

La primera vez que arranca contra la base vacía se carga solo el catálogo inicial:
8 juegos con sus fotos, las 4 categorías, la cuenta de la tienda y dos compradores
de ejemplo (`sofia@mail.com` y `martin@mail.com`, contraseña `123456`). No hay que
cargar nada a mano.

> El catálogo inicial solo se carga si la tabla de productos está **vacía**, para
> no pisar datos que ya existan. Si tu base quedó a medio llenar y querés empezar
> de cero: `DROP DATABASE ecommerce; CREATE DATABASE ecommerce;` y volvé a levantar.

**3. El frontend.** Clonar el otro repositorio y seguir su README:

```bash
git clone https://github.com/tomasszk/mesa-llena-frontend.git
```

Abre en `http://localhost:5173`, que es el puerto que el backend tiene habilitado
en el CORS.

> `application.properties` **no se sube al repo**: cada uno configura su propia
> contraseña de base de datos localmente.

## Documentación

- **[`tpo-backend/README.md`](tpo-backend/README.md)** — requisitos, configuración
  de la base, el catálogo inicial, la lista de endpoints y la colección de Insomnia
  para probar la API.
- **[README del frontend](https://github.com/tomasszk/mesa-llena-frontend)** —
  estructura, vistas, roles y decisiones de diseño.
