package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.CartItem;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.entity.dto.CartItemUpdateRequest;
import com.uade.tpo.demo.entity.dto.CartResponse;
import com.uade.tpo.demo.exceptions.CartItemNotFoundException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.ProductNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.repository.CartItemRepository;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) throws UserNotFoundException {
        return CartResponse.from(findOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest cartItemRequest)
            throws UserNotFoundException, ProductNotFoundException, InsufficientStockException {

        Cart cart = findOrCreateCart(userId);

        Product product = productRepository.findById(cartItemRequest.getProductId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(
                        "No existe el producto con id " + cartItemRequest.getProductId()));

        // El enunciado pide que un producto sin stock no se pueda agregar al carrito.
        if (!product.isAvailable())
            throw new InsufficientStockException(
                    "El producto " + product.getName() + " no tiene stock disponible");

        Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        int quantity = cartItemRequest.getQuantity() + existing.map(CartItem::getQuantity).orElse(0);

        validateStock(product, quantity);

        CartItem item = existing.orElseGet(() -> {
            CartItem nuevo = new CartItem(cart, product, 0);
            cart.getItems().add(nuevo);
            return nuevo;
        });
        item.setQuantity(quantity);
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, CartItemUpdateRequest cartItemRequest)
            throws UserNotFoundException, CartItemNotFoundException, InsufficientStockException {

        Cart cart = findOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        validateStock(item.getProduct(), cartItemRequest.getQuantity());

        item.setQuantity(cartItemRequest.getQuantity());
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId)
            throws UserNotFoundException, CartItemNotFoundException {

        Cart cart = findOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        // orphanRemoval = true en Cart.items borra la linea al sacarla de la lista.
        cart.getItems().remove(item);
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) throws UserNotFoundException {
        Cart cart = findOrCreateCart(userId);

        cart.getItems().clear();
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }

    /** El carrito se crea solo la primera vez que el usuario lo usa. */
    private Cart findOrCreateCart(Long userId) throws UserNotFoundException {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isPresent())
            return cart.get();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario con id " + userId));

        return cartRepository.save(new Cart(user));
    }

    private CartItem findItem(Cart cart, Long itemId) throws CartItemNotFoundException {
        return cart.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(
                        "El carrito no tiene una linea con id " + itemId));
    }

    private void validateStock(Product product, int quantity) throws InsufficientStockException {
        if (product.getStock() < quantity)
            throw new InsufficientStockException("Stock insuficiente para " + product.getName()
                    + ". Disponible: " + product.getStock() + ", solicitado: " + quantity);
    }
}
