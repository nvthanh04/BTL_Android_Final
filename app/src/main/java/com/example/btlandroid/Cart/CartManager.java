package com.example.btlandroid.Cart;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static List<Cart> cartList = new ArrayList<>();

    public static void addToCart(Cart item) {
        // Nếu sản phẩm đã có trong giỏ thì cộng thêm số lượng
        for (Cart cart : cartList) {
            if (cart.getId() == item.getId()) {
                cart.setSoLuong(cart.getSoLuong() + item.getSoLuong());
                return;
            }
        }
        // Nếu chưa có thì thêm mới
        cartList.add(item);
    }

    public static List<Cart> getCartList() {
        return cartList;
    }

    public static void clearCart() {
        cartList.clear();
    }
}
