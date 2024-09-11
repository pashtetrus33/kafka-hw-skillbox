package ru.skillbox.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.skillbox.model.OrderEvent;
import ru.skillbox.model.OrderStatusEvent;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTopicListener {

    private final KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;

    @Value("${app.kafka.orderStatusTopic}")
    private String orderStatusTopic;

    @KafkaListener(topics = "${app.kafka.orderTopic}")
    public void listen(ConsumerRecord<String, OrderEvent> record) {

        log.info("Received message: {}", record.value());
        log.info("Key: {}; Partition: {}; Topic: {}, Timestamp: {}",
                record.key(), record.partition(), record.topic(), record.timestamp());

        OrderStatusEvent orderStatusEvent = new OrderStatusEvent();
        orderStatusEvent.setStatus("CREATED"); // or any other status
        orderStatusEvent.setDate(Instant.now());

        kafkaTemplate.send(orderStatusTopic, orderStatusEvent);
    }
}
