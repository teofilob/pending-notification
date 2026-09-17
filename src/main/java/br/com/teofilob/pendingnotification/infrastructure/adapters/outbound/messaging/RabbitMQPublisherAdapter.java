package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.messaging;

import br.com.teofilob.pendingnotification.application.ports.out.NotificationPublisherPort;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adapter de Mensageria RabbitMQ que implementa a interface NotificationPublisherPort.
 * Responsável por publicar eventos de pendência na fila "notificar".
 * 
 * As mensagens são serializadas em JSON e publicadas no exchange "notificar.exchange"
 * com routing key "notificar.key" para a fila "notificar".
 * 
 * Este adapter é ativado mediante a propriedade de configuração:
 * app.messaging.type=rabbitmq
 */
@Component
@ConditionalOnProperty(
    name = "app.messaging.type",
    havingValue = "rabbitmq",
    matchIfMissing = false
)
public class RabbitMQPublisherAdapter implements NotificationPublisherPort {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisherAdapter.class);
    
    private static final String NOTIFICAR_EXCHANGE = "notificar.exchange";
    private static final String NOTIFICAR_ROUTING_KEY = "notificar.key";
    private static final String NOTIFICAR_QUEUE = "notificar";
    
    private static final String DLQ_EXCHANGE = "notificar.dlq.exchange";
    private static final String DLQ_ROUTING_KEY = "notificar.dlq.key";
    private static final String DLQ_QUEUE = "notificar.dlq";
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQPublisherAdapter(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "rabbitTemplate must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    /**
     * Publica um evento de pendência criada na fila "notificar".
     * A mensagem será processada pelo NotificationConsumer de forma assíncrona.
     * 
     * @param pendency pendência criada
     * @throws RuntimeException se houver erro na serialização ou envio
     */
    @Override
    public void publishPendencyCreated(Pendency pendency) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(pendency);
            
            Message message = new Message(
                jsonPayload.getBytes(),
                createMessageProperties()
            );
            
            rabbitTemplate.convertAndSend(NOTIFICAR_EXCHANGE, NOTIFICAR_ROUTING_KEY, message);
            log.info("Published PendencyCreated event - pendencyId: {}, title: {} to queue '{}'",
                    pendency.getId(), pendency.getTitle(), NOTIFICAR_QUEUE);
            
        } catch (Exception e) {
            log.error("Error publishing PendencyCreated event - pendencyId: {}", pendency.getId(), e);
            throw new RuntimeException("Erro ao publicar evento de pendência criada", e);
        }
    }

    /**
     * Publica um evento de notificação processada.
     * Usado para comunicar o resultado do processamento.
     * 
     * @param pendencyId ID da pendência processada
     * @param status status final (NOTIFIED, FAILED, etc.)
     */
    @Override
    public void publishNotificationProcessed(String pendencyId, String status) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(
                java.util.Map.of("pendencyId", pendencyId, "status", status)
            );
            
            Message message = new Message(
                jsonPayload.getBytes(),
                createMessageProperties()
            );
            
            rabbitTemplate.convertAndSend(DLQ_EXCHANGE, DLQ_ROUTING_KEY, message);
            log.info("Published NotificationProcessed event - pendencyId: {}, status: {} to queue '{}'",
                    pendencyId, status, DLQ_QUEUE);
            
        } catch (Exception e) {
            log.error("Error publishing NotificationProcessed event - pendencyId: {}", pendencyId, e);
            throw new RuntimeException("Erro ao publicar evento de notificação processada", e);
        }
    }

    /**
     * Publica uma mensagem genérica em uma fila.
     * Método de uso geral para outros casos especiais.
     * 
     * @param queueName nome da fila/exchange
     * @param message mensagem a publicar
     */
    @Override
    public void publish(String queueName, Object message) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(queueName, jsonPayload);
            log.info("Published generic message to queue '{}'", queueName);
        } catch (Exception e) {
            log.error("Error publishing generic message to queue '{}'", queueName, e);
            throw new RuntimeException("Erro ao publicar mensagem", e);
        }
    }

    /**
     * Cria as propriedades de mensagem com configurações padrão.
     * Define content-type como application/json e outros headers relevantes.
     */
    private MessageProperties createMessageProperties() {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE); // Persistent
        props.setExpiration("3600000"); // 1 hora de TTL na fila
        return props;
    }
}



