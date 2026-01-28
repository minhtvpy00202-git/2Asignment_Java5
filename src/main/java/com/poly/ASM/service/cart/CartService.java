package com.poly.ASM.service.cart;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    /**
     * Add item into cart.
     * - Anonymous: store in HttpSession (SESSION_CART)
     * - Logged-in: store in DB (cart_items)
     */
    boolean addToCart(Integer productId, Integer sizeId, Integer quantity);

    /**
     * Get current cart items (session cart if anonymous, DB cart if logged-in).
     */
    List<CartItem> getCartItems();

    /**
     * Merge anonymous session cart into user DB cart (only called after login success).
     * After merge, session cart MUST be cleared.
     */
    void mergeSessionCartToUserCart(String username);

    /**
     * Clear anonymous session cart only.
     */
    void clearSessionCart();

    /**
     * Clear current cart (anonymous/session or logged-in/DB).
     */
    void clearCart();

    long getDistinctProductCount();

    java.util.Set<Integer> getProductIdsInCart();

    boolean add(Integer productId, Integer sizeId);

    boolean add(Integer productId, Integer sizeId, Integer quantity);

    void remove(Integer productId, Integer sizeId);

    boolean update(Integer productId, Integer sizeId, Integer quantity);

    void clear();

    List<CartItem> getItems();

    BigDecimal getTotalPrice();
}
