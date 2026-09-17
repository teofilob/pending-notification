package br.com.teofilob.pendingnotification.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para a aplicação.
 * 
 * Ativa-se mediante a propriedade:
 * app.messaging.type=rabbitmq
 * 
 * Define:
 * - Fila "notificar": para processamento de pendências
 * - Fila "notificar.dlq": Dead Letter Queue para mensagens com falha
 * - Exchanges e bindings associados
 */
@Configuration
@ConditionalOnProperty(
    name = "app.messaging.type",
    havingValue = "rabbitmq",
    matchIfMissing = false
)
public class RabbitMQConfig {

    // ==================== FILAS ====================
    
    /**
     * Fila principal de processamento de notificações.
     * As mensagens possuem TTL de 1 hora.
     */
    @Bean
    public Queue notificarQueue() {
        return new Queue(
            "notificar",
            true,  // durable
            false, // exclusive
            false, // auto-delete
            null
        );
    }

    /**
     * Fila de letra morta (Dead Letter Queue).
     * Recebe mensagens que falharam após tentativas de retry.
     */
    @Bean
    public Queue notificarDlqQueue() {
        return new Queue(
            "notificar.dlq",
            true,  // durable
            false, // exclusive
            false, // auto-delete
            null
        );
    }

    // ==================== EXCHANGES ====================
    
    /**
     * Exchange direto para a fila de notificações.
     * Usa routing key para direcionar mensagens.
     */
    @Bean
    public DirectExchange notificarExchange() {
        return new DirectExchange(
            "notificar.exchange",
            true,  // durable
            false  // auto-delete
        );
    }

    /**
     * Exchange direto para a fila de letra morta.
     */
    @Bean
    public DirectExchange notificarDlqExchange() {
        return new DirectExchange(
            "notificar.dlq.exchange",
            true,  // durable
            false  // auto-delete
        );
    }

    // ==================== BINDINGS ====================
    
    /**
     * Binding entre queue "notificar" e exchange "notificar.exchange".
     * Routing key: "notificar.key"
     */
    @Bean
    public Binding notificarBinding(Queue notificarQueue, DirectExchange notificarExchange) {
        return BindingBuilder
            .bind(notificarQueue)
            .to(notificarExchange)
            .with("notificar.key");
    }

    /**
     * Binding entre queue "notificar.dlq" e exchange "notificar.dlq.exchange".
     * Routing key: "notificar.dlq.key"
     */
    @Bean
    public Binding notificarDlqBinding(Queue notificarDlqQueue, DirectExchange notificarDlqExchange) {
        return BindingBuilder
            .bind(notificarDlqQueue)
            .to(notificarDlqExchange)
            .with("notificar.dlq.key");
    }
}

