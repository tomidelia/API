package com.uade.tpo.demo.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.RoleRepository;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Carga datos de prueba la primera vez que se levanta la aplicacion, para poder
 * demostrar el CRUD completo desde Insomnia sin tener que cargar todo a mano.
 *
 * Las contrasenias van en texto plano a proposito: el register con hasheo lo
 * entrega la catedra en la clase de seguridad. Cuando eso se integre, este
 * componente se elimina o se apaga con app.seed-demo-data=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Ya hay datos cargados, no se vuelven a generar los datos de prueba.");
            return;
        }

        Role buyer = roleRepository.findByDescription("BUYER").orElseGet(() -> roleRepository.save(new Role("BUYER")));
        Role seller = roleRepository.findByDescription("SELLER")
                .orElseGet(() -> roleRepository.save(new Role("SELLER")));

        User vendedor = createUser("vendedor", "vendedor@juegos.com", "Lucia", "Fernandez", List.of(buyer, seller));
        User otroVendedor = createUser("ludoteca", "ventas@ludoteca.com", "Martin", "Alvarez", List.of(buyer, seller));
        User comprador = createUser("comprador", "comprador@mail.com", "Sofia", "Gomez", List.of(buyer));

        Category estrategia = createCategory("Estrategia");
        Category familiar = createCategory("Familiar");
        Category cartas = createCategory("Cartas");
        createCategory("Party games");

        createProduct("Catan", "Juego de estrategia y negociacion para 3 a 4 jugadores. Edicion en espaniol.",
                new BigDecimal("54999.00"), 12, 0, estrategia, vendedor,
                List.of("https://images.example.com/catan-1.jpg", "https://images.example.com/catan-2.jpg"));

        createProduct("Carcassonne", "Juego de colocacion de losetas. Incluye la expansion Rio.",
                new BigDecimal("42500.00"), 7, 15, estrategia, vendedor,
                List.of("https://images.example.com/carcassonne.jpg"));

        createProduct("Dixit", "Juego de cartas ilustradas y asociacion de ideas para toda la familia.",
                new BigDecimal("61000.00"), 4, 0, familiar, otroVendedor,
                List.of("https://images.example.com/dixit.jpg"));

        createProduct("Uno", "El clasico juego de cartas. Mazo de 108 cartas.",
                new BigDecimal("8900.00"), 0, 0, cartas, otroVendedor,
                List.of("https://images.example.com/uno.jpg"));

        createProduct("Virus!", "Juego de cartas rapido para 2 a 6 jugadores.",
                new BigDecimal("15400.00"), 25, 20, cartas, vendedor,
                List.of("https://images.example.com/virus.jpg"));

        log.info("Datos de prueba cargados.");
        log.info("Usuarios -> vendedor id={} | ludoteca id={} | comprador id={}",
                vendedor.getId(), otroVendedor.getId(), comprador.getId());
        log.info("Nota: 'Uno' se carga con stock 0 a proposito, para probar la validacion del carrito.");
    }

    private User createUser(String username, String email, String name, String surname, List<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("123456");
        user.setName(name);
        user.setSurname(surname);
        user.setRoles(new ArrayList<>(roles));
        return userRepository.save(user);
    }

    private Category createCategory(String description) {
        return categoryRepository.findByDescription(description).stream()
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(description)));
    }

    private void createProduct(String name, String description, BigDecimal price, int stock, int discount,
            Category category, User seller, List<String> images) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setDiscount(discount);
        product.setCategory(category);
        product.setSeller(seller);
        product.setActive(true);

        for (String url : images)
            product.getImages().add(new ProductImage(url, product));

        productRepository.save(product);
    }
}
