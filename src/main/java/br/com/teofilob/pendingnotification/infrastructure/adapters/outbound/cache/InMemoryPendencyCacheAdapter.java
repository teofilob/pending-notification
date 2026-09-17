package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.cache;

import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter de Cache em Memória (padrão) que implementa a interface PendencyCachePort.
 * Usado para desenvolvimento e testes sem infraestrutura real.
 * 
 * É o bean primário quando nenhuma configuração específica é feita.
 */
@Component
@Primary
public class InMemoryPendencyCacheAdapter implements PendencyCachePort {
    private final Map<String, String> storage = new ConcurrentHashMap<>();

    @Override
    public boolean exists(String key) {
        return storage.containsKey(key);
    }

    @Override
    public void save(String key, String value, Duration ttl) {
        storage.put(key, value);
    }

    @Override
    public void markProcessed(String key) {
        storage.remove(key);
    }

    @Override
    public void remove(String key) {
        storage.remove(key);
    }

    @Override
    public String get(String key) {
        return storage.get(key);
    }
}
