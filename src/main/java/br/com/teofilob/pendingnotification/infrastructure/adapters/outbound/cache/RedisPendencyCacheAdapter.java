package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.cache;

import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Adapter de Cache Redis que implementa a interface PendencyCachePort.
 * Responsável por gerenciar a idempotência das pendências no Redis.
 * 
 * A chave de cache segue o formato: pendency:{email}:{title}
 * 
 * Este adapter é ativado mediante a propriedade de configuração:
 * app.cache.type=redis
 */
@Component
@ConditionalOnProperty(
    name = "app.cache.type",
    havingValue = "redis",
    matchIfMissing = false
)
public class RedisPendencyCacheAdapter implements PendencyCachePort {
    
    private static final Logger log = LoggerFactory.getLogger(RedisPendencyCacheAdapter.class);
    private final StringRedisTemplate redisTemplate;

    public RedisPendencyCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verifica se uma chave de pendência já existe no Redis.
     * 
     * @param key chave única da pendência
     * @return true se existe, false caso contrário
     */
    @Override
    public boolean exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(      key);
        log.debug("Cache check for key '{}': {}", key, hasKey);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * Salva uma pendência no Redis com TTL configurado.
     * 
     * @param key chave única da pendência
     * @param value dados da pendência
     * @param ttl tempo de vida
     */
    @Override
    public void save(String key, String value, Duration ttl) {
        log.info("Saving to cache - key: '{}', ttl: {} seconds", key, ttl.getSeconds());
        redisTemplate.opsForValue().set(key, value, ttl.getSeconds(), TimeUnit.SECONDS);
        log.debug("Successfully saved to cache: {}", key);
    }

    /**
     * Marca uma pendência como processada no cache.
     * Atualiza o valor com prefixo "PROCESSED:" para rastreabilidade.
     * 
     * @param key chave única da pendência
     */
    @Override
    public void markProcessed(String key) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (currentValue != null) {
            String processedValue = "PROCESSED:" + currentValue;
            // Mantém o TTL existente (aprox. 5 minutos para rastreamento)
            redisTemplate.opsForValue().set(key, processedValue, 5, TimeUnit.MINUTES);
            log.info("Marked as processed: {}", key);
        } else {
            log.warn("Attempted to mark as processed but key not found: {}", key);
        }
    }

    /**
     * Remove uma pendência do cache.
     * 
     * @param key chave única da pendência
     */
    @Override
    public void remove(String key) {
        Boolean deleted = redisTemplate.delete(key);
        log.info("Removed from cache - key: '{}', deleted: {}", key, deleted);
    }

    /**
     * Recupera o valor armazenado de uma pendência no cache.
     * 
     * @param key chave única da pendência
     * @return valor armazenado ou null se não existir
     */
    @Override
    public String get(String key) {
        String value = redisTemplate.opsForValue().get(key);
        log.debug("Retrieved from cache - key: '{}', value: {}", key, value);
        return value;
    }
}


