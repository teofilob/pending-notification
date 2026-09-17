package br.com.teofilob.pendingnotification.application.usecase;

import br.com.teofilob.pendingnotification.application.ports.in.CreatePendencyUseCase;
import br.com.teofilob.pendingnotification.application.ports.out.NotificationPublisherPort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyRepositoryPort;
import br.com.teofilob.pendingnotification.domain.exception.PendencyAlreadyProcessingException;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Serviço que implementa o caso de uso de criação de pendência.
 * 
 * Fluxo:
 * 1. Verifica duplicidade no Redis (cache de idempotência)
 * 2. Se não existe, cria a pendência no domínio
 * 3. Salva no cache com TTL
 * 4. Publica na fila "notificar" para processamento assíncrono
 * 
 * Segue os princípios de Arquitetura Hexagonal.
 */
@Service
public class CreatePendencyService implements CreatePendencyUseCase {
    
    private final PendencyRepositoryPort pendencyRepository;
    private final PendencyCachePort pendencyCachePort;
    private final NotificationPublisherPort notificationPublisherPort;
    
    // TTL padrão para pendência em processamento (15 minutos)
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    public CreatePendencyService(
            PendencyRepositoryPort pendencyRepository,
            PendencyCachePort pendencyCachePort,
            NotificationPublisherPort notificationPublisherPort
    ) {
        this.pendencyRepository = Objects.requireNonNull(pendencyRepository, "pendencyRepository must not be null");
        this.pendencyCachePort = Objects.requireNonNull(pendencyCachePort, "pendencyCachePort must not be null");
        this.notificationPublisherPort = Objects.requireNonNull(notificationPublisherPort, "notificationPublisherPort must not be null");
    }

    @Override
    public Pendency execute(String title, String dueDate, String email, String phone) {
        // Validação e normalização de entrada
        String normalizedTitle = Objects.requireNonNull(title, "title must not be null").trim();
        String normalizedEmail = Objects.requireNonNull(email, "email must not be null").trim().toLowerCase();
        String normalizedDueDate = Objects.requireNonNull(dueDate, "dueDate must not be null").trim();

        // Gera chave de cache no formato: pendency:{email}:{title}
        String cacheKey = "pendency:" + normalizedEmail + ":" + normalizedTitle;
        
        // Valida duplicidade: se a chave já existe, lança exceção
        if (pendencyCachePort.exists(cacheKey)) {
            throw new PendencyAlreadyProcessingException(normalizedEmail, normalizedTitle);
        }

        // Cria a entidade de domínio
        LocalDateTime parsedDueDate = LocalDateTime.parse(normalizedDueDate);
        Recipient recipient = new Recipient(normalizedEmail, phone);
        Pendency pendency = new Pendency(normalizedTitle, parsedDueDate, recipient);

        // Salva no cache com status PENDING e TTL
        pendencyCachePort.save(cacheKey, pendency.getId().toString(), CACHE_TTL);
        
        // Persiste no repositório (posteriormente em PostgreSQL)
        pendencyRepository.save(pendency);
        
        // Publica para processamento assíncrono na fila "notificar"
        notificationPublisherPort.publishPendencyCreated(pendency);

        return pendency;
    }
}

