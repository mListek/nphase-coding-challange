package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public BigDecimal calculatePriceWithCategoryDiscount2(ShoppingCart shoppingCart) {
        Set <String> categoriesWithOver3Products = mapCategories(shoppingCart);

        List<List<Product>> filteredProductList = shoppingCart.getProducts()
            .stream()
            .collect(
                Collectors.teeing(
                    Collectors.filtering(product -> categoriesWithOver3Products.contains(product.getCategory()), Collectors.toList()),
                    Collectors.filtering(product -> !categoriesWithOver3Products.contains(product.getCategory()), Collectors.toList()),
                    List::of
                )
            );

        BigDecimal sumWithDiscount = filteredProductList.get(0).stream()
            .map(product -> product.getPricePerUnit()
                .multiply(BigDecimal.valueOf(product.getQuantity()))
                .multiply(BigDecimal.valueOf(0.9)))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        BigDecimal sumWithoutDiscount = filteredProductList.get(1).stream()
            .map(product -> product.getPricePerUnit()
                .multiply(BigDecimal.valueOf(product.getQuantity())))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        return sumWithDiscount.add(sumWithoutDiscount);
    }

    public Set<String> mapCategories(ShoppingCart shoppingCart) {
        Map<String, Integer> categories = shoppingCart.getProducts().stream()
            .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingInt(Product::getQuantity)));

        Set<String> categoriesWithOver3Products = new HashSet<>();

        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (category.getValue() > 3) {
                categoriesWithOver3Products.add(category.getKey());
            }
        }

        return categoriesWithOver3Products;
    }
}
