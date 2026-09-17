package br.com.teofilob.pendingnotification.application.usecase;

import br.com.teofilob.pendingnotification.application.ports.in.ProcessNotificationUseCase;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyRepositoryPort;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * Serviço que implementa o caso de uso de processamento de notificações.
 * 
 * Fluxo:
 * 1. Recebe mensagem da fila "notificar" (consumida pelo NotificationConsumer)
 * 2. Desserializa o JSON para Pendency
 * 3. Persiste no banco de dados PostgreSQL
 * 4. Atualiza o status no cache do Redis para PROCESSED
 * 
 * Segue os princípios de Arquitetura Hexagonal.
 */
@Service
public class ProcessNotificationService implements ProcessNotificationUseCase {

    private final PendencyRepositoryPort pendencyRepository;
    private final PendencyCachePort pendencyCachePort;
    private final ObjectMapper objectMapper;

    public ProcessNotificationService(
            PendencyRepositoryPort pendencyRepository,
            PendencyCachePort pendencyCachePort,
            ObjectMapper objectMapper
    ) {
        this.pendencyRepository = Objects.requireNonNull(pendencyRepository, "pendencyRepository must not be null");
        this.pendencyCachePort = Objects.requireNonNull(pendencyCachePort, "pendencyCachePort must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public void processNotification(String pendencyData) {
        try {
            // Desserializa os dados JSON para objeto Pendency
            Pendency pendency = objectMapper.readValue(pendencyData, Pendency.class);
            
            // Persiste no banco de dados
            pendencyRepository.save(pendency);
            
            // Gera chave de cache: pendency:{email}:{title}
            String cacheKey = "pendency:" + pendency.getRecipient().email() + ":" + pendency.getTitle();
            
            // Atualiza status no cache para PROCESSED
            pendencyCachePort.markProcessed(cacheKey);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar notificação: " + e.getMessage(), e);
        }
    }
}


