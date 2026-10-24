package com.uade.tpo.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Carga el catálogo inicial de Mesa Llena la primera vez que arranca la
 * aplicación contra una base vacía.
 *
 * Por qué existe: los productos viven en la base de datos, no en el repositorio.
 * Si cada integrante del grupo clona el proyecto y levanta su propio MySQL, se
 * encuentra con un catálogo vacío y tiene que cargar todo a mano antes de poder
 * ver nada. Con esto, clonar y levantar alcanza para tener la tienda andando.
 *
 * Solo corre si la tabla de productos está vacía: nunca pisa datos existentes.
 * Se puede apagar poniendo app.seed-demo-data=false en application.properties.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    /** Carpeta dentro de resources donde están las tapas de los juegos. */
    private static final String CARPETA_IMAGENES = "/demo-images/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Si ya hay productos, no tocamos nada. Esto hace que arrancar la
        // aplicación dos veces no duplique el catálogo, y que los productos que
        // se carguen después desde el panel no corran riesgo.
        if (productRepository.count() > 0) {
            log.info("La base ya tiene productos: no se carga el catálogo inicial.");
            return;
        }

        log.info("Base vacía: cargando el catálogo inicial de Mesa Llena...");

        // Dos compradores de ejemplo, para poder probar el carrito sin tener
        // que registrarse primero. La contraseña de los dos es 123456.
        crearComprador("sofia", "sofia@mail.com", "Sofía", "Gómez");
        crearComprador("martin", "martin@mail.com", "Martín", "Álvarez");

        Category estrategia = crearCategoria("Estrategia");
        Category familiar = crearCategoria("Familiar");
        Category cartas = crearCategoria("Cartas");
        Category party = crearCategoria("Party games");

        crearProducto("Catan",
                "El clásico de la negociación. Colonizás una isla repartiendo recursos "
                        + "con tiradas de dados y cerrando trueques con el resto de la mesa.",
                new BigDecimal("54999.00"), 12, 15, estrategia, "catan.jpg",
                new Ficha(3, 4, 90, 10,
                        "Cada turno tirás dos dados y el número que sale reparte recursos a "
                        + "quien tenga poblados en esos hexágonos. Con madera, ladrillo, oveja, "
                        + "trigo y mineral construís caminos, poblados y ciudades, y cada "
                        + "construcción vale puntos. Lo que más define la partida es el "
                        + "intercambio: casi siempre te falta un recurso que otro tiene de "
                        + "sobra. Gana el primero que llega a 10 puntos."));

        crearProducto("Carcassonne",
                "Un paisaje medieval que se arma entre todos, loseta por loseta, "
                        + "mientras peleás por quedarte con las ciudades y los caminos.",
                new BigDecimal("42500.00"), 7, 10, estrategia, "carcassonne.jpg",
                new Ficha(2, 5, 35, 7,
                        "En tu turno sacás una loseta al azar y la colocás de manera que "
                        + "encaje con el terreno ya puesto. Después podés apoyar uno de tus "
                        + "meeples encima para reclamar un camino, una ciudad, un monasterio "
                        + "o un campo. Cuando esa construcción se completa sumás puntos y "
                        + "recuperás el meeple para volver a usarlo. Como tenés pocos, la "
                        + "decisión real es cuándo conviene invertirlos."));

        crearProducto("Ticket to Ride",
                "Cruzás el mapa de punta a punta reclamando rutas de tren antes de que "
                        + "te las saquen de abajo de la nariz.",
                new BigDecimal("48900.00"), 5, 0, estrategia, "ticket.jpg",
                new Ficha(2, 5, 45, 8,
                        "Juntás cartas de tren de colores y las gastás para reclamar las "
                        + "rutas que unen las ciudades del mapa. Cada ruta da puntos, y "
                        + "además tenés boletos secretos que te piden conectar dos ciudades "
                        + "concretas: si lo lográs sumás, y si no, restás. La partida se "
                        + "termina cuando a alguien le quedan pocos vagones, así que hay que "
                        + "elegir bien el momento de jugar las rutas largas."));

        // Este queda sin stock a propósito: sirve para mostrar que el catálogo
        // lo marca como agotado y que no se puede agregar al carrito.
        crearProducto("Azul",
                "Azulejos portugueses, un tablero propio y la tensión de elegir sin "
                        + "dejarle servido al de al lado lo que necesita.",
                new BigDecimal("39500.00"), 0, 0, estrategia, "azul.jpg",
                new Ficha(2, 4, 35, 8,
                        "Vas tomando azulejos de un mismo color de unas bandejas circulares "
                        + "y los acomodás en las filas de tu tablero. Cuando completás una "
                        + "fila, el azulejo pasa a tu mosaico y suma según los que ya tenga "
                        + "al lado. Lo que no te entra se te cae al piso y te resta, así que "
                        + "no alcanza con agarrar: hay que agarrar justo."));

        crearProducto("Dixit",
                "Cartas ilustradas que no quieren decir nada hasta que alguien las "
                        + "describe. El juego pasa por cómo te explicás.",
                new BigDecimal("44900.00"), 9, 20, cartas, "dixit.jpg",
                new Ficha(3, 6, 30, 8,
                        "Un jugador hace de narrador: elige una de sus cartas y dice una "
                        + "frase, una palabra o hasta un sonido que la describa. Los demás "
                        + "aportan una carta propia que pegue con esa pista, se mezclan "
                        + "todas boca abajo y hay que adivinar cuál era la del narrador. "
                        + "Lo difícil es el punto justo: si la adivinan todos o no la "
                        + "adivina nadie, el narrador no suma."));

        crearProducto("Uno",
                "El juego de cartas que todos saben jugar y con el que todos se pelean "
                        + "alguna vez. Rápido y para cualquier cantidad de gente.",
                new BigDecimal("9900.00"), 40, 0, cartas, "uno.jpg",
                new Ficha(2, 10, 15, 7,
                        "Tirás una carta que coincida en color o en número con la que está "
                        + "arriba del mazo. Las especiales cambian todo: el siguiente roba, "
                        + "se saltea un turno o se invierte el sentido de la ronda. Cuando "
                        + "te queda una sola carta tenés que cantar UNO, y si no lo decís y "
                        + "alguien te lo marca, robás dos. Gana el primero que se queda sin "
                        + "cartas."));

        crearProducto("Codenames",
                "Dos equipos, una grilla de palabras y un jefe de espías que solo puede "
                        + "dar pistas de una palabra. Se juega en grupo y se grita mucho.",
                new BigDecimal("29900.00"), 14, 0, party, "codenames.jpg",
                new Ficha(4, 8, 15, 10,
                        "Se arma una grilla de 25 cartas con palabras sueltas. Cada equipo "
                        + "tiene un jefe de espías, y solo ellos saben cuáles palabras son "
                        + "de su color. El jefe da una única pista de una palabra más un "
                        + "número, y su equipo tiene que deducir a cuáles se refería. Si "
                        + "tocan una del equipo contrario le regalan el turno, y si tocan "
                        + "la del asesino pierden en el acto."));

        crearProducto("Jenga",
                "La torre de maderitas que se tambalea. No hay nada que explicar: o te "
                        + "tiembla el pulso o no.",
                new BigDecimal("18500.00"), 22, 5, familiar, "jenga.jpg",
                new Ficha(1, 8, 20, 6,
                        "Se arma una torre con 54 bloques de madera, de a tres por piso y "
                        + "cruzados entre sí. Por turno sacás un bloque de cualquier piso "
                        + "que no sea el de más arriba, usando una sola mano, y lo apoyás "
                        + "arriba de todo. La torre se va volviendo más alta y más "
                        + "inestable con cada jugada. Pierde el que la termina tirando."));

        log.info("Catálogo inicial cargado: {} productos en {} categorías.",
                productRepository.count(), categoryRepository.count());
    }

    private void crearComprador(String nickname, String email, String name, String surname) {
        if (userRepository.existsByEmail(email))
            return;

        User user = new User();
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setName(name);
        user.setSurname(surname);
        user.setRole(Role.USER);

        cartRepository.save(new Cart(userRepository.save(user)));
    }

    private Category crearCategoria(String description) {
        return categoryRepository.findByDescription(description).stream()
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(description)));
    }

    /**
     * La ficha técnica de un juego. Va en un record y no en cinco parámetros
     * sueltos para que al leer las altas de arriba se note cuál es cuál.
     */
    private record Ficha(int minPlayers, int maxPlayers, int durationMinutes, int minAge,
            String howToPlay) {
    }

    private void crearProducto(String name, String description, BigDecimal price, int stock,
            int discount, Category category, String archivoImagen, Ficha ficha) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setDiscount(discount);
        product.setCategory(category);
        product.setActive(true);

        product.setMinPlayers(ficha.minPlayers());
        product.setMaxPlayers(ficha.maxPlayers());
        product.setDurationMinutes(ficha.durationMinutes());
        product.setMinAge(ficha.minAge());
        product.setHowToPlay(ficha.howToPlay());

        product.getImages().add(new ProductImage(leerImagen(archivoImagen), product));

        productRepository.save(product);
    }

    /**
     * Lee una tapa de src/main/resources/demo-images y la convierte en el Blob
     * que guarda la base.
     *
     * Se lee como recurso del classpath y no como archivo del disco: así sigue
     * funcionando cuando la aplicación se empaqueta en un .jar, donde las
     * imágenes quedan adentro del jar y ya no existen como archivos sueltos.
     */
    private Blob leerImagen(String archivo) {
        String ruta = CARPETA_IMAGENES + archivo;

        try (InputStream entrada = getClass().getResourceAsStream(ruta)) {
            if (entrada == null)
                throw new IllegalStateException("No se encontró la imagen " + ruta);

            return new SerialBlob(entrada.readAllBytes());

        } catch (IOException | SQLException e) {
            throw new IllegalStateException("No se pudo leer la imagen " + ruta, e);
        }
    }
}
