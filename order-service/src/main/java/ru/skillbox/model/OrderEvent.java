package ru.skillbox.model;

import lombok.Data;

@Data
public class OrderEvent {
    private String product;
    private Integer quantity;
}

