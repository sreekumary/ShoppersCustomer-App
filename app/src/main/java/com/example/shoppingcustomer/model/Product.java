package com.example.shoppingcustomer.model;

import androidx.annotation.NonNull;

public class Product {

    private String product;
    private Double price;
    private int quantity;

    public Product(String product, Double price, int quantity) {
      this.product = product;
      this.price = price;
      this.quantity = quantity;
     }


    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    @NonNull
    @Override
    public String toString() {
       return  "Product{" +
               ", product='" + product + '\'' +
               ", price=" + price +
               ", quantity=" + quantity +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.getPrice(), getPrice()) == 0 &&

                getProduct().equals(product.getProduct()) &&
                getQuantity()==(product.getQuantity()) &&
                getPrice().equals(product.getPrice());
    }
}
