package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.persistence;

import br.com.teofilob.pendingnotification.application.ports.out.PendencyRepositoryPort;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPendencyRepository implements PendencyRepositoryPort {
    private final Map<UUID, Pendency> storage = new ConcurrentHashMap<>();

    @Override
    public Pendency  save(Pendency pendency) {
        storage.put(pendency.getId(), pendency);
        return pendency;
    }
}

