package ru.skillbox.model;

import lombok.Data;

@Data
public class Order {
    private String product;
    private Integer quantity;
}