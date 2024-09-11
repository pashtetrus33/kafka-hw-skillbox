package ru.skillbox.controller;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.model.Order;
import ru.skillbox.model.OrderEvent;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kafka")
public class OrderController {

    // Добавление сеттера для тестирования
    @Setter
    @Value("${app.kafka.orderTopic}")
    private String orderTopic;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @PostMapping("/order")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setProduct(order.getProduct());
        orderEvent.setQuantity(order.getQuantity());

        kafkaTemplate.send(orderTopic, orderEvent);

        return ResponseEntity.ok("Message sent to Kafka");
    }
}