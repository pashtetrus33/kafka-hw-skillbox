package ru.skillbox.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import ru.skillbox.model.Order;
import ru.skillbox.model.OrderEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderControllerTest {

    // Устанавливаем значение orderTopic непосредственно для теста
    private final String orderTopic = "test-topic";

    @InjectMocks
    private OrderController orderController;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Устанавливаем значение orderTopic в контроллере
        orderController.setOrderTopic(orderTopic);
    }

    @Test
    public void testCreateOrder() {
        Order order = new Order();
        order.setProduct("Test Product");
        order.setQuantity(10);

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setProduct(order.getProduct());
        orderEvent.setQuantity(order.getQuantity());

        // Симулируем поведение KafkaTemplate
        when(kafkaTemplate.send(orderTopic, orderEvent)).thenReturn(null);

        ResponseEntity<String> response = orderController.createOrder(order);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Message sent to Kafka", response.getBody());

        verify(kafkaTemplate).send(orderTopic, orderEvent);
    }
}