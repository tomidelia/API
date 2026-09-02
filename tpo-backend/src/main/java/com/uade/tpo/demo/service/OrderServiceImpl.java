package com.uade.tpo.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.CartItem;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Checkout sin procesamiento de pago:
     * 1) valida que el carrito tenga productos,
     * 2) valida el stock de cada linea,
     * 3) calcula el total aplicando los descuentos vigentes,
     * 4) descuenta el stock, y
     * 5) vacia el carrito.
     *
     * Va todo en una unica transaccion: si falla una linea, no se descuenta
     * stock de ninguna y no queda una orden a medias.
     */
    @Override
    @Transactional
    public OrderResponse checkout(Long userId)
            throws UserNotFoundException, EmptyCartException, InsufficientStockException {

        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("No existe el usuario con id " + userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EmptyCartException("El carrito del usuario " + userId + " esta vacio"));

        if (cart.getItems().isEmpty())
            throw new EmptyCartException("El carrito del usuario " + userId + " esta vacio");

        Order order = new Order(cart.getUser());
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity())
                throw new InsufficientStockException("Stock insuficiente para " + product.getName()
                        + ". Disponible: " + product.getStock() + ", solicitado: " + cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
            order.getItems().add(orderItem);
            total = total.add(orderItem.getSubtotal());

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setTotal(total);
        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUser(Long userId, PageRequest pageRequest) throws UserNotFoundException {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("No existe el usuario con id " + userId);

        return orderRepository.findByUserIdOrderByDateDesc(userId, pageRequest).map(OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) throws OrderNotFoundException {
        // Se filtra por usuario antes de responder: si la orden es de otro,
        // devuelve 404 igual que si no existiera.
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() -> new OrderNotFoundException("No existe la orden con id " + orderId));

        return OrderResponse.from(order);
    }
}
