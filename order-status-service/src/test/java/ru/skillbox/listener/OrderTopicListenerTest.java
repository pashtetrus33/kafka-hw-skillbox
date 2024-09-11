package ru.skillbox.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.skillbox.model.OrderEvent;
import ru.skillbox.model.OrderStatusEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class OrderTopicListenerTest {

    @Mock
    private KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;

    @InjectMocks
    private OrderTopicListener orderTopicListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Устанавливаем значение для orderStatusTopic вручную
        ReflectionTestUtils.setField(orderTopicListener, "orderStatusTopic", "test-order-status-topic");
    }

    @Test
    void testListen() {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setProduct("Test product");
        orderEvent.setQuantity(5);

        // Создаем тестовый объект ConsumerRecord
        ConsumerRecord<String, OrderEvent> record = new ConsumerRecord<>(
                "test-topic",
                0,
                0L,
                "key",
                orderEvent
        );

        // Вызов метода слушателя
        orderTopicListener.listen(record);

        // Проверяем, что kafkaTemplate.send был вызван с правильными параметрами
        verify(kafkaTemplate, times(1)).send(
                eq("test-order-status-topic"),
                any(OrderStatusEvent.class)
        );
    }
}