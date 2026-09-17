package br.com.teofilob.pendingnotification.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuração do Redis para a aplicação.
 * 
 * Ativa-se mediante a propriedade:
 * app.cache.type=redis
 * 
 * Fornece:
 * - StringRedisTemplate: template para operações com Redis
 * - Serialização padrão para strings
 */
@Configuration
@ConditionalOnProperty(
    name = "app.cache.type",
    havingValue = "redis",
    matchIfMissing = false
)
public class RedisConfig {

    /**
     * Configura o StringRedisTemplate com serialização otimizada.
     * 
     * @param connectionFactory factory de conexão do Redis
     * @return StringRedisTemplate configurado
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Serialização de strings
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Serializa key e value como String
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        
        // Serializa hashKey e hashValue como String (para operações com Hash)
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }
}

