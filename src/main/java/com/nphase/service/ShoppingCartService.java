package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ShoppingCartService {

    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {
        return shoppingCart.getProducts()
                .stream()
                .map(product -> calculateProductPrice(product).setScale(1, RoundingMode.HALF_UP))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateProductPrice(Product product) {
        BigDecimal result = product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity()));

        return product.getQuantity() > 3 ? result.multiply(BigDecimal.valueOf(0.9)) : result;
    }
}
