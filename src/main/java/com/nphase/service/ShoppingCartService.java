package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;
import com.nphase.properties.DiscountProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ShoppingCartService {
    private final DiscountProperties properties = new DiscountProperties();

    // task1, task2 - calculate total price of all items
    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {
        return shoppingCart.getProducts()
                .stream()
                .map(product -> calculateProductPrice(product).setScale(1, RoundingMode.HALF_UP))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    // helper function to apply discount if necessary
    public BigDecimal calculateProductPrice(Product product) {
        BigDecimal result = product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity()));

        return product.getQuantity() >= properties.getMinItemsForDiscount()
            ? result.multiply(BigDecimal.valueOf(properties.getDiscount()))
            : result;
    }

    // task3
    public BigDecimal calculatePriceWithCategoryDiscount(ShoppingCart shoppingCart) {
        // identify categories that can be discounted
        Set <String> categoriesWithDiscount = mapCategories(shoppingCart);

        // split products into two lists - discounted and regular
        List<List<Product>> filteredProductList = shoppingCart.getProducts()
            .stream()
            .collect(
                Collectors.teeing(
                    Collectors.filtering(product -> categoriesWithDiscount.contains(product.getCategory()), Collectors.toList()),
                    Collectors.filtering(product -> !categoriesWithDiscount.contains(product.getCategory()), Collectors.toList()),
                    List::of
                )
            );

        // sum first list and apply discount
        BigDecimal sumWithDiscount = filteredProductList.get(0).stream()
            .map(product -> product.getPricePerUnit()
                .multiply(BigDecimal.valueOf(product.getQuantity()))
                .multiply(BigDecimal.valueOf(properties.getDiscount())))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        // sum second list without discount
        BigDecimal sumWithoutDiscount = filteredProductList.get(1).stream()
            .map(product -> product.getPricePerUnit()
                .multiply(BigDecimal.valueOf(product.getQuantity())))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        return sumWithDiscount.add(sumWithoutDiscount);
    }

    // helper function to identify categories with enough products to apply discount
    public Set<String> mapCategories(ShoppingCart shoppingCart) {
        Map<String, Integer> categories = shoppingCart.getProducts().stream()
            .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingInt(Product::getQuantity)));

        Set<String> categoriesWithDiscount = new HashSet<>();

        // filter categories and add to the set
        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (category.getValue() >= properties.getMinItemsForDiscount()) {
                categoriesWithDiscount.add(category.getKey());
            }
        }

        return categoriesWithDiscount;
    }
}
