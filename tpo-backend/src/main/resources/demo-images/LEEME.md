# Imágenes del catálogo inicial

Estas son las tapas que `DemoDataLoader` carga en la base la primera vez que la
aplicación arranca contra un esquema vacío. Están acá, dentro de `resources`,
porque el loader las lee como recursos del *classpath*: así siguen funcionando
cuando el proyecto se empaqueta en un `.jar`.

Si agregás un juego al catálogo inicial, poné su tapa en esta carpeta y pasá el
nombre del archivo en la llamada a `crearProducto(...)`.

**No son las tapas reales de los juegos.** Son placeholders generados con la
paleta de Mesa Llena, con el nombre del juego escrito encima. Cuando tengamos las
fotos de verdad hay dos caminos:

- Reemplazar estos archivos (afecta a quien arranque con la base vacía).
- Subirlas desde el panel de la tienda, en `/admin/productos/:id/editar` (afecta
  a la base que ya está cargada; las fotos nuevas reemplazan a las anteriores).

`foto-producto.png` no se usa en el catálogo: es un archivo chico y suelto para
tener algo que elegir cuando se prueba el alta de productos desde Insomnia.
