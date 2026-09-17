package br.com.teofilob.pendingnotification.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {
    @Test
    void shouldUseSuppliedConnectionFactoryAndStringSerializersWithoutConnecting() {
        var factory = mock(RedisConnectionFactory.class);
        var template = new RedisConfig().stringRedisTemplate(factory);
        assertThat(template.getConnectionFactory()).isSameAs(factory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getHashValueSerializer()).isInstanceOf(StringRedisSerializer.class);
        verifyNoInteractions(factory);
    }
}
