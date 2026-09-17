package br.com.teofilob.pendingnotification.application.ports.out;

import br.com.teofilob.pendingnotification.domain.model.Pendency;

/**
 * Output Port para publicar mensagens na fila de notificações (RabbitMQ).
 * Responsável por publicar eventos de pendência criada na fila "notificar".
 */
public interface NotificationPublisherPort {
    
    /**
     * Publica uma mensagem de pendência na fila "notificar" do RabbitMQ.
     * A mensagem será consumida pelo NotificationConsumer para processamento assíncrono.
     * 
     * @param pendency pendência a publicar
     * @throws RuntimeException se houver erro ao publicar a mensagem
     */
    void publishPendencyCreated(Pendency pendency);

    /**
     * Publica um evento de notificação processada.
     * 
     * @param pendencyId ID da pendência processada
     * @param status status final do processamento (ex: NOTIFIED, FAILED)
     */
    void publishNotificationProcessed(String pendencyId, String status);

    /**
     * Publica uma mensagem genérica em uma fila.
     * Método genérico para uso em casos especiais.
     * 
     * @param queueName nome da fila
     * @param message mensagem a publicar
     */
    void publish(String queueName, Object message);
}
