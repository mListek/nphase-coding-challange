package com.nphase.properties;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DiscountProperties {

  private final double discount;
  private final int minItemsForDiscount;
  public DiscountProperties() {
    Properties properties = new Properties();
    try {
      properties.load(new FileReader("src/main/resources/discount.properties"));
      this.discount = (100.0 - Double.parseDouble(properties.getProperty("discountPercentage"))) / 100;
      this.minItemsForDiscount = Integer.parseInt(properties.getProperty("minItemsForDiscount"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public double getDiscount() {
    return discount;
  }

  public int getMinItemsForDiscount() {
    return minItemsForDiscount;
  }
}
