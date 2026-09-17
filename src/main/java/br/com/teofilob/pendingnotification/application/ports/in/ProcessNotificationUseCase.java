package br.com.teofilob.pendingnotification.application.ports.in;

/**
 * Input Port para processar notificações.
 * Define o contrato para o caso de uso de processamento de notificações.
 */
public interface ProcessNotificationUseCase {

    /**
     * Processa uma notificação consumida da fila RabbitMQ.
     * Salva a pendência no banco de dados e atualiza o cache.
     * 
     * @param pendencyData dados da pendência em formato JSON
     */
    void processNotification(String pendencyData);
}

