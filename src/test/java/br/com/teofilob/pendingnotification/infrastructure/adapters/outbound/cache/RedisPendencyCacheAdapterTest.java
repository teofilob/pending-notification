package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisPendencyCacheAdapterTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final RedisPendencyCacheAdapter adapter = new RedisPendencyCacheAdapter(redis);
    private static final String KEY = "pendency:user@example.com:Invoice";

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void shouldOnlyReportExistingKeyForTrue(Boolean result) {
        when(redis.hasKey(KEY)).thenReturn(result);
        assertThat(adapter.exists(KEY)).isEqualTo(Boolean.TRUE.equals(result));
        verify(redis).hasKey(KEY);
    }

    @Test
    void shouldSaveValueWithTtlInSeconds() {
        when(redis.opsForValue()).thenReturn(values);
        adapter.save(KEY, "id-123", Duration.ofMinutes(15));
        verify(values).set(KEY, "id-123", 900, TimeUnit.SECONDS);
    }

    @Test
    void shouldMarkExistingValueWithFiveMinuteTtl() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(KEY)).thenReturn("id-123");
        adapter.markProcessed(KEY);
        verify(values).set(KEY, "PROCESSED:id-123", 5, TimeUnit.MINUTES);
    }

    @Test
    void shouldNotCreateMissingKeyWhenMarkingProcessed() {
        when(redis.opsForValue()).thenReturn(values);
        adapter.markProcessed(KEY);
        verify(values).get(KEY);
        verifyNoMoreInteractions(values);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"id-123", "PROCESSED:id-123"})
    void shouldReturnStoredValueOrNull(String value) {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(KEY)).thenReturn(value);
        assertThat(adapter.get(KEY)).isEqualTo(value);
    }

    @Test
    void shouldDeleteRequestedKey() {
        adapter.remove(KEY);
        verify(redis).delete(KEY);
    }

    @Test
    void shouldPropagateRedisFailure() {
        var failure = new IllegalStateException("Redis unavailable");
        when(redis.hasKey(KEY)).thenThrow(failure);
        assertThatThrownBy(() -> adapter.exists(KEY)).isSameAs(failure);
    }
}
