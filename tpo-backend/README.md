# TPO Aplicaciones Interactivas — E-commerce de juegos de mesa

API REST con Spring Boot + Spring Data JPA + MySQL + Spring Security (JWT).

**Modelo:** e-commerce con un vendedor único. La tienda es nuestra; los usuarios que
se registran son compradores.

**Estado:** backend completo, incluida la capa de seguridad. Falta el frontend.

---

## 1. Qué está hecho

| Requisito del enunciado | Estado |
|---|---|
| API REST sobre toda la información (completa o filtrada) | ✅ |
| Capa de persistencia sobre el negocio entregado | ✅ |
| Registro de usuarios (usuario, mail, contraseña, nombre y apellido) | ✅ |
| Autenticación mediante login | ✅ |
| Administración de cuentas con asignación de permisos | ✅ |
| Catálogo con foto y precio | ✅ |
| Búsqueda y filtrado por categoría, precio y texto | ✅ |
| Detalle del producto con imagen y descripción | ✅ |
| Agregar productos al carrito | ✅ |
| Producto sin stock: se indica y no se puede agregar al carrito | ✅ |
| Carrito: agregar, eliminar y modificar productos | ✅ |
| Checkout con cálculo automático del total | ✅ |
| Checkout descuenta stock y valida disponibilidad | ✅ |
| Alta de publicación con una o más fotos | ✅ |
| Publicación con descripción, categoría y precio | ✅ |
| Manejo de stock y descuentos individuales | ✅ |
| Baja de producto | ✅ |
| Interfaz de usuario dinámica (frontend) | ⏳ pendiente |

### Una aclaración sobre el modelo

El enunciado plantea que los usuarios se registren como compradores **y vendedores**, o
sea una plataforma con muchos vendedores publicando, estilo Mercado Libre.

**Nosotros hacemos un e-commerce con un vendedor único**: la página es nuestra y vendemos
nuestros propios juegos. Es una desviación consciente; la profesora mencionó en clase que
hay grupos trabajando con un solo vendedor, y en la clase de JWT dijo explícitamente que
en ese caso *"tocará hacer que por defecto cualquier usuario que se registra caiga en rol
de comprador, no de vendedor"*. Eso es exactamente lo que hace `AuthenticationService`.

Categorías es el código que dio la profesora: sólo se cambió la relación con Product de
`@OneToOne` a `@OneToMany`, porque una categoría agrupa varios productos.

---

## 2. Qué necesitás instalado

| Herramienta | Detalle |
|---|---|
| JDK 17 o superior | Probado con JDK 17 y 23 |
| MySQL Server 8 + Workbench | El servicio tiene que estar corriendo |
| Maven | No hace falta: el proyecto trae `mvnw` |
| Insomnia | Para probar los endpoints (ver sección 7) |

**Extensiones de VS Code:** **Extension Pack for Java** (Microsoft) y **Spring Boot
Extension Pack** (VMware).

---

## 3. Configuración (una sola vez por máquina)

### 3.1 Crear el esquema en MySQL

```sql
CREATE DATABASE ecommerce;
USE ecommerce;
```

No crees ninguna tabla a mano: las genera Hibernate a partir de las entidades.

### 3.2 Poner la contraseña

Copiá `application.properties.example` a `application.properties` dentro de
`src/main/resources/` y reemplazá `CAMBIAR_POR_TU_PASSWORD` por la contraseña de tu MySQL.
Ese archivo está en el `.gitignore`, tal como pidió la profesora.

Ahí también están la clave y la expiración del token, que `JwtService` inyecta con
`@Value` para no dejar datos sensibles escritos en el código.

---

## 4. Levantar el proyecto

**VS Code:** abrí `tpo-backend`, abrí `DemoApplication.java` y tocá **Run**.

**Terminal (PowerShell):**

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"; .\mvnw.cmd spring-boot:run
```

La API queda en **http://localhost:4002**.

### Correr los tests

Contra H2 en memoria, sin necesidad de MySQL:

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"; .\mvnw.cmd test
```

Son **24 tests**: catálogo, filtros, publicación, stock, carrito, checkout y toda la
capa de seguridad (registro, login, tokens manipulados y separación de roles).

---

## 5. Datos de prueba

Se cargan la primera vez que arranca (`config/DataInitializer.java`). Se apaga con
`app.seed-demo-data=false`.

**Cuentas** — la contraseña de todas es `123456`, guardada hasheada con BCrypt:

| email | rol | quién es |
|---|---|---|
| admin@juegosdemesa.com | ADMIN | la tienda |
| sofia@mail.com | USER | compradora |
| martin@mail.com | USER | comprador |

> La cuenta ADMIN se crea acá a propósito: el registro público **siempre** da rol USER,
> así nadie puede auto-asignarse permisos de tienda.

**Categorías:** Estrategia, Familiar, Cartas, Party games.

**Productos:** Catan, Carcassonne (15% off), Dixit, Uno (**stock 0 a propósito**, para
mostrar la validación) y Virus! (20% off).

### Sobre las fotos

La API guarda la **URL** de cada foto. Los productos de prueba usan `placehold.co`, que
son placeholders que sí cargan en el navegador.

**Todo producto tiene que tener al menos una imagen**, validado tanto al crear como al
modificar. Para poner fotos reales se manda la URL real en `images`; también acepta base64
(`data:image/png;base64,...`).

---

## 6. Seguridad

Implementada siguiendo el modelo que dio la cátedra: JWT, sin sesiones, política
`STATELESS`. El token viaja en el header `Authorization: Bearer <token>` de cada request.

### Cómo obtener el token

```
POST /api/v1/auth/register     → alta de comprador, devuelve el token
POST /api/v1/auth/authenticate → login por email y contraseña, devuelve el token
```

Ambos devuelven:

```json
{ "access_token": "eyJhbGciOi...", "role": "USER", "username": "sofia" }
```

### Quién puede hacer qué

Está todo en **`controllers/config/SecurityConfig.java`**:

| Zona | Endpoints | Quién entra |
|---|---|---|
| Pública | `POST /api/v1/auth/**` | cualquiera |
| Pública | `GET /products`, `GET /products/{id}`, filtros | cualquiera, sin token |
| Pública | `GET /categories`, `GET /categories/{id}` | cualquiera, sin token |
| Tienda | `POST/PUT/PATCH/DELETE /products/**` | sólo **ADMIN** |
| Tienda | `POST /categories` | sólo **ADMIN** |
| Compradores | `/carts/**` | sólo **USER** |
| Compradores | `/orders/**` | sólo **USER** |
| Todo lo demás | — | autenticado (red de seguridad) |

El catálogo es público a propósito: se navega sin registrarse, igual que en Mercado
Libre. Recién al usar el carrito hace falta identificarse.

### Piezas de la implementación

| Clase | Qué hace |
|---|---|
| `config/SecurityConfig` | Define el `SecurityFilterChain`: qué endpoint pide qué rol |
| `config/JwtAuthenticationFilter` | Corre una vez por request, **antes** del controller |
| `config/JwtService` | Arma, firma y valida los tokens |
| `config/ApplicationConfig` | Declara los beans: `UserDetailsService`, `AuthenticationManager`, `BCryptPasswordEncoder` |
| `auth/AuthenticationController` | Los dos endpoints públicos |
| `service/AuthenticationService` | Lógica de registro y login |

`User` implementa `UserDetails` y `Role` es un enum (`USER`, `ADMIN`), igual que en el
modelo de la cátedra. Para Spring Security el "username" es el **email**: es con lo que se
loguea y lo que viaja en el `subject` del token.

### Decisiones propias de nuestro negocio

- **El registro fuerza rol `USER`.** El request ni siquiera acepta un campo `role`.
- **El carrito se crea junto con el usuario**, en la misma transacción del registro.
- **El `userId` salió de las URLs.** Antes era `/carts/3`; ahora es `/carts` y el usuario
  se toma del token. Si no, cualquier persona logueada podría leer el carrito de otra.
- **`GET /orders/{id}` valida que la orden sea tuya** y, si no lo es, responde **404** en
  lugar de 403: así no le confirma a nadie que esa orden existe.
- **El login fallido devuelve 401 con un mensaje genérico** (*"Email o contraseña
  incorrectos"*), sin aclarar cuál de los dos falló.
- **La tienda no tiene carrito.** El ADMIN vende, no compra: `/carts/**` es sólo de USER.

---

## 7. Endpoints

Base: `http://localhost:4002`

### Autenticación (público)

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/v1/auth/register` | Alta de comprador |
| POST | `/api/v1/auth/authenticate` | Login |

```json
{ "username": "juanp", "email": "juan@mail.com", "password": "123456",
  "name": "Juan", "surname": "Perez" }
```

### Catálogo (público)

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/products` | Catálogo con filtros y paginado |
| GET | `/products/{id}` | Detalle |
| GET | `/categories` | Lista de categorías |
| GET | `/categories/{id}` | Una categoría |

```
/products?page=0&size=10&categoryId=1&minPrice=10000&maxPrice=60000&onlyAvailable=true&search=catan
```

### Gestión de productos (ADMIN)

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/products` | Alta de publicación |
| PUT | `/products/{id}` | Modificar |
| PATCH | `/products/{id}/stock` | Manejo de stock |
| PATCH | `/products/{id}/discount` | Descuento individual |
| DELETE | `/products/{id}` | Baja |
| POST | `/categories` | Crear categoría |

```json
{ "name": "Aventureros al Tren", "description": "Juego de rutas ferroviarias.",
  "price": 70000.00, "stock": 5, "discount": 10, "categoryId": 1,
  "images": ["https://placehold.co/600x600?text=Aventureros"] }
```

### Carrito (USER)

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/carts` | Mi carrito |
| POST | `/carts/items` | Agregar producto |
| PUT | `/carts/items/{itemId}` | Cambiar cantidad |
| DELETE | `/carts/items/{itemId}` | Sacar una línea |
| DELETE | `/carts` | Vaciar |

### Órdenes (USER)

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/orders/checkout` | Checkout |
| GET | `/orders/my` | Mi historial |
| GET | `/orders/{id}` | Detalle de una orden mía |

---

## 8. Probar todo con Insomnia

En el proyecto está **`insomnia-tpo.json`**: una colección con **67 requests** que cubre
todos los endpoints, todos los caminos de error y todas las reglas de seguridad.

**Importarla:** Insomnia → menú `File` (o botón `Create`) → **Import** → **From File**.

### Cómo arrancar

1. Abrí la carpeta **`00 - Autenticacion`** y corré **`Login TIENDA`**.
2. Copiá el `access_token` de la respuesta (el string, sin las comillas).
3. Abrí el **Base Environment** y pegalo en la variable `token_admin`.
4. Corré **`Login COMPRADOR`** y pegá su token en `token_user`.
5. Ya podés correr las carpetas 01 a 05 en orden: cada request usa el token que le toca.

| Carpeta | Qué muestra |
|---|---|
| 00 - Autenticación | Registro, login, y sus errores (401, 409, 400) |
| 01 - Catálogo público | Todo lo que se ve **sin token** |
| 02 - Sin token no se pasa | Los 403 de lo protegido |
| 03 - Gestión (ADMIN) | Publicar, stock, descuentos, baja, y el 403 del comprador |
| 04 - Carrito (USER) | Agregar, modificar, eliminar, y el 403 del admin |
| 05 - Checkout | La compra, el descuento de stock y el historial |

Las requests marcadas con **[400]**, **[401]**, **[403]**, **[404]**, **[409]** o
**[204]** fallan **a propósito**: son las validaciones de negocio y las reglas de
seguridad. De las 67, hay **34 casos de error** cubiertos.

### Antes de la demo

Los IDs de la colección asumen la base recién cargada. Antes de la clase: apagá la app,
corré el reset y volvé a levantarla.

```bash
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pTU_PASSWORD -e "DROP DATABASE ecommerce; CREATE DATABASE ecommerce;"
```

Ojo: al resetear la base, los tokens viejos dejan de servir (los usuarios se recrean).
Hay que volver a loguearse y pegar los tokens nuevos en el environment.

---

## 9. Guion para la demo en clase

1. **Catálogo sin token** → `GET /products`. Se ve `finalPrice` con el descuento y
   `available: false` en Uno. Nadie se registró todavía.
2. **Filtros** → `search=cartas&maxPrice=20000` y `onlyAvailable=true`.
3. **Chocar con la seguridad** → `GET /carts` sin token: **403**.
4. **Registro** → `POST /api/v1/auth/register`. Devuelve el token y `role: USER`.
5. **Login de la tienda** → token de ADMIN.
6. **Publicar** → `POST /products` con el token de ADMIN: **201**.
7. **Autorización** → la misma request con el token del comprador: **403**. Está
   autenticado, pero no autorizado.
8. **Stock y descuento** → `PATCH /products/6/stock` y `/discount`.
9. **Sin stock** → agregar Uno al carrito: **400**.
10. **Carrito** → agregar Catan x2, ver el total, cambiar cantidad.
11. **Checkout** → **201** con el total. El stock de Catan baja de 12 a 9.
12. **Historial** → `GET /orders/my`.
13. **Token manipulado** → cambiarle una letra al token: **403**.

---

## 10. Estructura del proyecto

Arquitectura en 3 capas con inyección de dependencias: por cada entidad, controller,
interfaz de servicio, implementación y repositorio.

```
com.uade.tpo.demo
├── controllers/          Capa de tráfico HTTP (@RestController)
│   ├── auth/             Registro y login + sus DTO
│   ├── config/           Spring Security: SecurityConfig, JwtService,
│   │                     JwtAuthenticationFilter, ApplicationConfig
│   ├── CategoriesController        (de la cátedra)
│   ├── ProductsController
│   ├── CartsController
│   ├── OrdersController
│   └── GlobalExceptionHandler      Traduce excepciones a status code + JSON
├── service/              Lógica de negocio (interfaz + @Service)
├── repository/           Acceso a datos (@Repository extends JpaRepository)
├── entity/               Entidades JPA
│   └── dto/              Request y Response
├── exceptions/           Excepciones de negocio
└── config/
    ├── DataInitializer   Datos de prueba
    └── WebConfig         CORS para el futuro frontend (puerto 5173)
```

### Relaciones entre entidades

| Relación | Dónde |
|---|---|
| OneToOne | `User` ↔ `Cart` |
| OneToMany / ManyToOne | `Category` → `Product`, `User` → `Order`, `Cart` → `CartItem`, `Order` → `OrderItem`, `Product` → `ProductImage` |

`Role` dejó de ser una entidad y pasó a ser un **enum**, igual que en el modelo de la
cátedra: así `SecurityConfig` puede escribir `hasAuthority(Role.ADMIN.name())` y no hace
falta ni tabla ni repositorio de roles. Con eso desapareció el ManyToMany
`User` ↔ `Role` que había antes.

### Decisiones que conviene poder explicar

- **DTOs para las respuestas.** Las entidades no se serializan directo: las relaciones
  bidireccionales harían que Jackson entre en recursión infinita. Además el DTO expone
  `finalPrice` y `available`, que el frontend necesita.
- **`@EqualsAndHashCode(of = "id")` en las entidades.** El `@Data` de Lombok genera un
  `equals`/`toString` que recorre las dos puntas de cada relación y desborda la pila.
- **Baja lógica del producto.** Borrarlo de verdad rompería las órdenes que ya lo
  referencian: se marca `active = false` y desaparece del catálogo.
- **`OrderItem` guarda precio y descuento del momento de la compra**, para que la orden no
  cambie si después se toca el precio.
- **`@Transactional` en checkout y en el registro.** Si falla el stock de una línea no se
  descuenta el de ninguna, y si falla el alta no queda un carrito huérfano. Es el principio
  de atomicidad de A.C.I.D.
- **El token lleva sólo el email.** Cuanto menos información viaje en el payload, mejor:
  con el email se recupera de la base todo lo demás, incluido el rol.
- **Lombok declarado como annotation processor en el `pom.xml`.** Desde Java 23 javac ya
  no ejecuta los processors que encuentra solos en el classpath.

---

## 11. Pendiente

- **Frontend** (React + Vite). El backend ya tiene CORS habilitado para `localhost:5173`.
- **Subida de imágenes por multipart.** Hoy la API guarda URLs. La cátedra subió un
  ejemplo de cómo recibir un `file` y guardarlo como Blob, devolviéndolo en base64; se
  puede sumar más adelante si el frontend lo necesita.
