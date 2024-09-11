package ru.skillbox.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.skillbox.model.OrderStatusEvent;

@Component
@Slf4j
public class OrderStatusListener {

    @KafkaListener(topics = "${app.kafka.orderStatusTopic}")
    public void listen(ConsumerRecord<String, OrderStatusEvent> record) {
        log.info("Received message: {}", record.value());
        log.info("Key: {}; Partition: {}; Topic: {}, Timestamp: {}",
                record.key(), record.partition(), record.topic(), record.timestamp());
    }
}