package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.messaging;

import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQPublisherAdapterTest {
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final RabbitMQPublisherAdapter adapter = new RabbitMQPublisherAdapter(rabbit, mapper);
    private final Pendency pendency = new Pendency("Invoice", LocalDateTime.of(2026, 9, 20, 10, 0),
            new Recipient("user@example.com", "11999999999"));

    @Test
    void shouldPublishPendencyJsonWithPersistentDeliveryAndExpiration() throws Exception {
        adapter.publishPendencyCreated(pendency);
        var message = captureMessage("notificar.exchange", "notificar.key");
        var json = mapper.readTree(message.getBody());
        assertThat(json.get("id").asText()).isEqualTo(pendency.getId().toString());
        assertThat(json.get("title").asText()).isEqualTo("Invoice");
        assertThat(json.get("recipient").get("email").asText()).isEqualTo("user@example.com");
        assertThat(json.get("status").asText()).isEqualTo("PENDING");
        assertProperties(message);
    }

    @Test
    void shouldPublishProcessedEventWithIdAndStatus() throws Exception {
        adapter.publishNotificationProcessed("id-123", "FAILED");
        var message = captureMessage("notificar.dlq.exchange", "notificar.dlq.key");
        assertThat(mapper.readTree(message.getBody()))
                .isEqualTo(mapper.readTree("{\"pendencyId\":\"id-123\",\"status\":\"FAILED\"}"));
        assertProperties(message);
    }

    @Test
    void shouldPublishGenericPayloadToRequestedQueue() throws Exception {
        adapter.publish("audit", Map.of("event", "created"));
        var payload = ArgumentCaptor.forClass(String.class);
        verify(rabbit).convertAndSend(eq("audit"), payload.capture());
        assertThat(mapper.readTree(payload.getValue()).get("event").asText()).isEqualTo("created");
    }

    @ParameterizedTest
    @ValueSource(strings = {"created", "processed", "generic"})
    void shouldWrapSerializationFailuresWithoutSending(String operation) throws Exception {
        var brokenMapper = mock(ObjectMapper.class);
        var failure = new JsonProcessingException("cannot serialize") { };
        when(brokenMapper.writeValueAsString(any())).thenThrow(failure);
        var publisher = new RabbitMQPublisherAdapter(rabbit, brokenMapper);
        assertThatThrownBy(() -> publish(publisher, operation)).isInstanceOf(RuntimeException.class)
                .hasCause(failure).hasMessageContaining("Erro ao publicar");
        verifyNoInteractions(rabbit);
    }

    @ParameterizedTest
    @ValueSource(strings = {"created", "processed", "generic"})
    void shouldPreserveBrokerFailure(String operation) {
        var failure = new IllegalStateException("broker unavailable");
        if (operation.equals("generic")) {
            doThrow(failure).when(rabbit).convertAndSend(eq("audit"), any(Object.class));
        } else {
            doThrow(failure).when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class));
        }
        assertThatThrownBy(() -> publish(adapter, operation)).hasCause(failure)
                .hasMessageContaining("Erro ao publicar");
    }

    @Test
    void shouldRejectMissingDependencies() {
        assertThatNullPointerException().isThrownBy(() -> new RabbitMQPublisherAdapter(null, mapper))
                .withMessage("rabbitTemplate must not be null");
        assertThatNullPointerException().isThrownBy(() -> new RabbitMQPublisherAdapter(rabbit, null))
                .withMessage("objectMapper must not be null");
    }

    private void publish(RabbitMQPublisherAdapter publisher, String operation) {
        switch (operation) {
            case "created" -> publisher.publishPendencyCreated(pendency);
            case "processed" -> publisher.publishNotificationProcessed("id-123", "FAILED");
            case "generic" -> publisher.publish("audit", Map.of("event", "created"));
            default -> throw new AssertionError(operation);
        }
    }

    private Message captureMessage(String exchange, String routingKey) {
        var captor = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).convertAndSend(eq(exchange), eq(routingKey), captor.capture());
        return captor.getValue();
    }

    private void assertProperties(Message message) {
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(message.getMessageProperties().getExpiration()).isEqualTo("3600000");
    }
}
