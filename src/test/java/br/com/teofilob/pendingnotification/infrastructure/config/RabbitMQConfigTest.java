package br.com.teofilob.pendingnotification.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import static org.assertj.core.api.Assertions.*;

class RabbitMQConfigTest {
    @Test
    void shouldConfigureDurableNotificationRoute() {
        var config = new RabbitMQConfig();
        assertRoute(config.notificarQueue(), config.notificarExchange(),
                config.notificarBinding(config.notificarQueue(), config.notificarExchange()), "notificar");
    }

    @Test
    void shouldConfigureDurableDeadLetterRoute() {
        var config = new RabbitMQConfig();
        assertRoute(config.notificarDlqQueue(), config.notificarDlqExchange(),
                config.notificarDlqBinding(config.notificarDlqQueue(), config.notificarDlqExchange()), "notificar.dlq");
    }

    private void assertRoute(Queue queue, DirectExchange exchange, Binding binding, String name) {
        assertThat(queue.getName()).isEqualTo(name);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isExclusive()).isFalse();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(exchange.getName()).isEqualTo(name + ".exchange");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo(name + ".key");
    }
}
