# TPO Aplicaciones Interactivas — E-commerce de juegos de mesa

API REST con Spring Boot + Spring Data JPA + MySQL, construida sobre el código base
de la clase de Hibernate.

**Modelo:** tienda única. Nosotros somos el vendedor; los usuarios que se registran son
compradores. La administración del sitio publica y gestiona los productos.

**Estado:** backend completo salvo la capa de seguridad, que se integra después de
la clase correspondiente.

---

## 1. Qué está hecho y qué falta

| Requisito del enunciado | Estado |
|---|---|
| API REST sobre toda la información (completa o filtrada) | ✅ |
| Capa de persistencia sobre el negocio entregado | ✅ |
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
| Manejo de stock del producto | ✅ |
| Baja de producto | ✅ |
| Gestión de descuentos sobre productos individuales | ✅ |
| Registro y login de usuarios | ⏳ lo entrega la cátedra con seguridad |
| Administración de permisos | ⏳ lo entrega la cátedra con seguridad |
| Interfaz de usuario dinámica (frontend) | ⏳ pendiente |

### Una aclaración sobre el modelo

El enunciado dice *"registro de usuarios como compradores y vendedores"* y que *"los
usuarios registrados como vendedores podrán realizar el alta de una publicación"*, o sea
una plataforma con muchos vendedores publicando, estilo MercadoLibre.

**Nosotros hacemos un e-commerce con un vendedor único**: la página es nuestra y vendemos
nuestros propios juegos. Los usuarios que se registran son compradores. Es una desviación
consciente del enunciado; la profesora mencionó en clase que hay grupos trabajando con un
solo vendedor.

En la práctica esto significa que los productos no tienen dueño: los endpoints de alta,
modificación y baja son de administración del sitio y, cuando integremos seguridad, van a
quedar restringidos al rol `ADMIN`.

Categorías está tal cual la dio la profesora: sólo se cambió la relación con
Product de `@OneToOne` a `@OneToMany`, porque una categoría agrupa varios productos.

---

## 2. Qué necesitás instalado

| Herramienta | Detalle |
|---|---|
| JDK 17 o superior | Probado con JDK 17 y 23 |
| MySQL Server 8 + Workbench | El servicio tiene que estar corriendo |
| Maven | No hace falta instalarlo: el proyecto trae `mvnw` |
| Insomnia | Para probar los endpoints (ver sección 7) |

**Extensiones de VS Code:** instalá el **Extension Pack for Java** (de Microsoft) y
**Spring Boot Extension Pack** (de VMware). Con eso ya tenés compilación, el botón
de Run y soporte de Lombok. No hace falta nada más.

---

## 3. Configuración (una sola vez por máquina)

Estos dos pasos los hace cada uno en su computadora después de clonar el repo.

### 3.1 Crear el esquema en MySQL

Abrí MySQL Workbench, conectate a la instancia local y ejecutá:

```sql
CREATE DATABASE ecommerce;
USE ecommerce;
```

No crees ninguna tabla a mano: las genera Hibernate a partir de las entidades.

### 3.2 Poner la contraseña

Copiá `application.properties.example` a `application.properties` dentro de
`src/main/resources/` y reemplazá:

```properties
spring.datasource.password=CAMBIAR_POR_TU_PASSWORD
```

por la contraseña de tu MySQL. Ese archivo está en el `.gitignore` porque la contraseña
es de cada uno, tal como dijo la profesora en clase.

---

## 4. Levantar el proyecto

**Desde VS Code (lo más simple):** abrí la carpeta `tpo-backend`, abrí
`DemoApplication.java` y tocá **Run**.

**Desde la terminal:** hace falta `JAVA_HOME`. En PowerShell:

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"; .\mvnw.cmd spring-boot:run
```

Para dejarlo configurado para siempre (así no lo escribís cada vez):

```bash
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-23", "User")
```

La API queda en **http://localhost:4002**.

### Correr los tests

Los tests usan una base H2 en memoria, así que corren sin MySQL levantado:

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"; .\mvnw.cmd test
```

Son 18 tests que recorren catálogo, filtros, publicación, validación de stock y el
flujo completo de compra.

---

## 5. Datos de prueba

La primera vez que arranca, la aplicación carga datos para poder demostrar todo sin
tener que cargar nada a mano (`config/DataInitializer.java`). Se apaga con
`app.seed-demo-data=false`.

**Usuarios** (los IDs se imprimen en la consola al arrancar):

| id | username | rol | quién es |
|---|---|---|---|
| 1 | admin | ADMIN + USER | la tienda |
| 2 | sofia | USER | compradora |
| 3 | martin | USER | comprador |

**Categorías:** Estrategia, Familiar, Cartas, Party games.

**Productos:** Catan, Carcassonne (15% off), Dixit, Uno (**stock 0 a propósito**,
para mostrar la validación) y Virus! (20% off).

### Sobre las fotos

La API guarda la **URL** de cada foto, no el archivo. Los productos de prueba usan
imágenes de `placehold.co`, que son placeholders genéricos que sí cargan en el navegador.

**Todo producto tiene que tener al menos una imagen**: está validado tanto al crear como
al modificar, así el catálogo nunca queda con un producto sin foto.

Para poner fotos reales sólo hay que mandar la URL real en el campo `images` al crear o
modificar el producto. También acepta una imagen embebida en base64
(`data:image/png;base64,...`), por si más adelante el frontend sube archivos.

---

## 6. Endpoints

Base: `http://localhost:4002`

### Categorías (código de la cátedra)

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/categories?page=0&size=10` | Lista paginada |
| GET | `/categories/{id}` | Una categoría |
| POST | `/categories` | Crea una categoría |

```json
{ "description": "Eurogames" }
```

### Productos

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/products` | Catálogo con filtros y paginado |
| GET | `/products/{id}` | Detalle del producto |
| POST | `/products` | Alta de publicación |
| PUT | `/products/{id}` | Modificar publicación |
| PATCH | `/products/{id}/stock` | Manejo de stock |
| PATCH | `/products/{id}/discount` | Descuento individual |
| DELETE | `/products/{id}` | Eliminar publicación |

Filtros del catálogo (todos opcionales y combinables):

```
/products?page=0&size=10
         &categoryId=1
         &minPrice=10000
         &maxPrice=60000
         &onlyAvailable=true
         &search=catan
```

POST `/products`:

```json
{
  "name": "Aventureros al Tren",
  "description": "Juego de rutas ferroviarias para 2 a 5 jugadores.",
  "price": 70000.00,
  "stock": 5,
  "discount": 10,
  "categoryId": 1,
  "images": [
    "https://placehold.co/600x600?text=Aventureros",
    "https://placehold.co/600x600?text=Aventureros+caja"
  ]
}
```

PATCH `/products/{id}/stock` → `{ "stock": 20 }`
PATCH `/products/{id}/discount` → `{ "discount": 25 }`

### Carrito

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/carts/{userId}` | Ver el carrito (lo crea vacío si no existe) |
| POST | `/carts/{userId}/items` | Agregar producto |
| PUT | `/carts/{userId}/items/{itemId}` | Cambiar cantidad |
| DELETE | `/carts/{userId}/items/{itemId}` | Sacar una línea |
| DELETE | `/carts/{userId}` | Vaciar el carrito |

POST `/carts/2/items` → `{ "productId": 1, "quantity": 2 }`
PUT `/carts/2/items/1` → `{ "quantity": 3 }`

### Órdenes

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/orders/checkout/{userId}` | Checkout: calcula total y descuenta stock |
| GET | `/orders/user/{userId}?page=0&size=10` | Historial de compras |
| GET | `/orders/{orderId}` | Detalle de una orden |

---

## 7. Probar todo con Insomnia

En el proyecto está el archivo **`insomnia-tpo.json`**: una colección con **60 requests**
ya armadas, con sus bodies y una nota en cada una explicando qué tiene que devolver.

Cubre **todos los endpoints y todos los caminos de error**: cada validación de negocio,
cada 404, cada campo obligatorio. La idea es llegar a la clase de seguridad sabiendo que
la base está sana, porque una vez que se mete el token es mucho más difícil rastrear
dónde rompe.

**Importarla:** abrir Insomnia → menú `File` (o el botón `Create`) → **Import** →
**From File** → elegir `insomnia-tpo.json`.

Quedan 5 carpetas, pensadas para correrse **en orden**:

| Carpeta | Qué muestra |
|---|---|
| 01 - Categorías | El código de la cátedra: paginado, alta y el error de duplicada |
| 02 - Catálogo | Paginado, los filtros, el detalle y el 404 |
| 03 - Gestión de productos | Publicar con fotos, modificar, stock, descuentos y la baja |
| 04 - Carrito | Agregar, modificar, eliminar, y los 400 por falta de stock |
| 05 - Checkout y órdenes | La compra, el descuento de stock y el historial |

Las requests marcadas con **[400]**, **[404]** o **[204]** fallan **a propósito**: son
las validaciones del negocio. Cuando las corras vas a ver ese código de error y el mensaje
explicando el motivo. No están rotas.

De las 60, hay **26 casos de error** cubiertos: producto o categoría inexistente, campos
obligatorios, precio negativo, descuento mayor a 100, stock negativo, producto sin
imágenes, cantidad cero, más cantidad que stock, línea de carrito inexistente, checkout
con carrito vacío, usuario inexistente y categoría duplicada.

### Importante antes de la demo

Los IDs de la colección están fijos y asumen la base recién cargada. Si veniste probando
y creaste o borraste cosas, **antes de la clase**: apagá la app, corré el reset de abajo y
volvé a levantarla. Ahí los IDs vuelven a coincidir y la colección corre de punta a punta
sin un solo error inesperado.

```bash
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pTU_PASSWORD -e "DROP DATABASE ecommerce; CREATE DATABASE ecommerce;"
```

Reemplazá `TU_PASSWORD` por la contraseña de tu MySQL.

Un detalle a tener a mano: en **"Modificar cantidad de una línea"** y **"Eliminar una
línea"**, el número del final de la URL es el ID de la **línea del carrito**, no el del
producto. Sale del campo `id` de cada item que devuelve *Ver el carrito*. Con la base
recién reseteada son el 1 y el 2, que es lo que ya viene cargado.

---

## 8. Guion para la demo en clase

1. **Catálogo** → `GET /products` — se ve `finalPrice` ya con el descuento aplicado
   y `available: false` en Uno.
2. **Filtrar** → `GET /products?search=cartas&maxPrice=20000` y
   `GET /products?onlyAvailable=true` (Uno desaparece).
3. **Detalle** → `GET /products/1`. Los links de `images` se abren en el navegador.
4. **Publicar** → `POST /products` con dos fotos. Devuelve **201**.
5. **Validación** → `POST /products` con el body vacío. Devuelve **400** con el
   detalle de qué campo falta.
6. **Stock y descuento** → `PATCH /products/6/stock` y `PATCH /products/6/discount`.
7. **Sin stock** → `POST /carts/2/items` con el id de Uno. Devuelve **400**.
8. **Carrito** → agregar Catan x2, ver el total, cambiar la cantidad.
9. **Checkout** → `POST /orders/checkout/2`. Devuelve **201** con el total.
10. **Se descontó el stock** → `GET /products/1` y `GET /carts/2` (vacío).
11. **Historial** → `GET /orders/user/2`.

---

## 9. Estructura del proyecto

Arquitectura en 3 capas con inyección de dependencias, igual que en clase: por cada
entidad hay controller, interfaz de servicio, implementación y repositorio.

```
com.uade.tpo.demo
├── controllers/          Capa de tráfico HTTP (@RestController)
│   ├── CategoriesController        (de la cátedra)
│   ├── ProductsController
│   ├── CartsController
│   ├── OrdersController
│   └── GlobalExceptionHandler      Traduce excepciones a status code + JSON
├── service/              Lógica de negocio (interfaz + @Service que la implementa)
│   ├── CategoryService / CategoryServiceImpl      (de la cátedra)
│   ├── ProductService  / ProductServiceImpl
│   ├── CartService     / CartServiceImpl
│   └── OrderService    / OrderServiceImpl
├── repository/           Acceso a datos (@Repository extends JpaRepository)
├── entity/               Entidades JPA que Hibernate mapea a tablas
│   └── dto/              Request y Response, para no exponer las entidades
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
| ManyToMany | `User` ↔ `Role` (tabla intermedia `user_role`) |

### Decisiones que conviene poder explicar

- **DTOs para las respuestas.** Las entidades no se serializan directo: las
  relaciones bidireccionales harían que Jackson entre en recursión infinita. Además
  el DTO expone `finalPrice` y `available`, que el frontend necesita.
- **`@EqualsAndHashCode(of = "id")` en las entidades.** El `@Data` de Lombok genera
  un `equals`/`toString` que recorre las dos puntas de cada relación y desborda la
  pila. Se acota al id.
- **Baja lógica del producto.** Borrarlo de verdad rompería las órdenes que ya lo
  referencian, así que se marca `active = false`, se saca de los carritos y
  desaparece del catálogo.
- **`OrderItem` guarda precio y descuento del momento de la compra**, para que la
  orden no cambie si después se toca el precio.
- **El checkout es una única transacción**: si falla el stock de una línea, no se
  descuenta el stock de ninguna.
- **Lombok declarado como annotation processor en el `pom.xml`.** Desde Java 23
  javac ya no ejecuta los processors que encuentra solos en el classpath; sin esa
  configuración el proyecto no compila.

---

## 10. Cuando llegue la clase de seguridad

Lo que hay que tocar cuando se integre el código de login/register de la cátedra:

1. Reemplazar la entidad `User` por la que entregue la profesora (esta tiene
   `username`, `email`, `password`, `name` y `surname`, que es lo que pide el
   enunciado) y sacar la contraseña en texto plano.
2. Sacar los `userId` que hoy viajan por la URL en carrito y órdenes: pasan a salir
   del usuario del token JWT.
3. Poner `app.seed-demo-data=false` o borrar `DataInitializer`.
4. Restringir por rol: los endpoints de alta, modificación, stock, descuento y baja de
   productos sólo para `ADMIN`; carrito y checkout sólo para el dueño del carrito.
