package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.messaging;

import br.com.teofilob.pendingnotification.application.ports.out.NotificationPublisherPort;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Adapter de Mensageria em Memória (padrão) que implementa a interface NotificationPublisherPort.
 * Usado para desenvolvimento e testes sem infraestrutura real do RabbitMQ.
 * 
 * É o bean primário quando nenhuma configuração específica é feita.
 */
@Component
@Primary
public class InMemoryNotificationPublisherAdapter implements NotificationPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(InMemoryNotificationPublisherAdapter.class);

    @Override
    public void publishPendencyCreated(Pendency pendency) {
        log.info("Simulating publish of PendencyCreated to queue 'notificar' with payload: {} {}",
                pendency.getId(), pendency.getTitle());
    }

    @Override
    public void publishNotificationProcessed(String pendencyId, String status) {
        log.info("Simulating publish of NotificationProcessed to queue 'notificar.dlq' with pendencyId: {} status: {}",
                pendencyId, status);
    }

    @Override
    public void publish(String queueName, Object message) {
        log.info("Simulating publish to queue '{}' with payload: {}", queueName, message);
    }
}

