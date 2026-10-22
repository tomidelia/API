# Fotos del catálogo inicial

Estas son las fotos que `DemoDataLoader` carga en la base la primera vez que la
aplicación arranca contra un esquema vacío. Están acá, dentro de `resources`,
porque el loader las lee como recursos del *classpath*: así siguen funcionando
cuando el proyecto se empaqueta en un `.jar`.

Si agregás un juego al catálogo inicial, poné su foto en esta carpeta y pasá el
nombre del archivo en la llamada a `crearProducto(...)`.

## De dónde salieron

Son las imágenes reales de cada juego, no ilustraciones nuestras. Todas se
procesaron igual antes de entrar acá: recortadas a 4:3, escaladas a 900×675 y
guardadas como JPEG de 60–100 KB. Ese paso importa porque el backend devuelve
las fotos en Base64 **adentro de cada producto**: una foto de varios MB haría
que el listado del catálogo pesara una barbaridad.

| Archivo | Origen |
|---|---|
| `catan.jpg`, `carcassonne.jpg`, `ticket.jpg`, `azul.jpg`, `dixit.jpg` | Tapas de caja publicadas en Wikipedia |
| `uno.jpg` | Foto de producto de la tienda oficial de Mattel |
| `codenames.jpg` | Foto de una partida, de Wikimedia Commons |
| `jenga.jpg` | Composición propia con el logo oficial de la marca |

Son material de las editoriales, usado acá únicamente como contenido de ejemplo
de un trabajo práctico de facultad. Si el proyecto alguna vez saliera de ese
ámbito habría que reemplazarlas por fotos propias o licenciadas.

Para cambiar la foto de un producto que ya está en la base no hace falta tocar
esta carpeta: se sube desde el panel de la tienda, en
`/admin/productos/:id/editar`. Las fotos nuevas reemplazan a las anteriores.

`foto-producto.png` no se usa en el catálogo: es un archivo chico y suelto para
tener algo que elegir cuando se prueba el alta de productos desde Insomnia.
