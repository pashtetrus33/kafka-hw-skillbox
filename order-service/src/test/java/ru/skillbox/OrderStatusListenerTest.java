package ru.skillbox;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.skillbox.model.OrderEvent;
import ru.skillbox.model.OrderStatusEvent;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
public class OrderStatusListenerTest {

    @MockBean
    private KafkaTemplate<String, OrderStatusEvent> orderStatusTemplate;

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:latest")
            .asCompatibleSubstituteFor("apache/kafka");

    static KafkaContainer kafkaContainer = new KafkaContainer(KAFKA_IMAGE);

    static {
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${app.kafka.orderTopic}")
    private String orderTopic;

    @Value("${app.kafka.orderStatusTopic}")
    private String orderStatusTopic;

    @Test
    void whenSendOrderEvent_thenReceiveOrderStatusEvent() {
        // Отправляем событие заказа
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setProduct("Test product");
        orderEvent.setQuantity(10);
        String key = UUID.randomUUID().toString();

        kafkaTemplate.send(orderTopic, key, orderEvent);

        // Потребляем сообщения из топика "order-status-topic"
        Consumer<String, OrderStatusEvent> consumer = createConsumer();
        consumer.subscribe(Collections.singletonList(orderStatusTopic));

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OrderStatusEvent orderStatusEvent = pollForMessage(consumer, 10000); // Передаем consumer и таймаут
                    assertThat(orderStatusEvent).isNotNull();
                    assertThat(orderStatusEvent.getStatus()).isEqualTo("CREATED");
                });

        consumer.close(); // Не забудьте закрыть consumer после использования
    }

    private Consumer<String, OrderStatusEvent> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderStatusEvent.class.getName());

        return new KafkaConsumer<>(props);
    }


    private OrderStatusEvent pollForMessage(Consumer<String, OrderStatusEvent> consumer, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            ConsumerRecords<String, OrderStatusEvent> records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                return records.iterator().next().value();
            }
        }
        throw new RuntimeException("No message received within the timeout period");
    }
}