package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryPendencyCacheAdapter Tests")
class InMemoryPendencyCacheAdapterTest {

    private InMemoryPendencyCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryPendencyCacheAdapter();
    }

    @Test
    @DisplayName("Should save and retrieve value from cache")
    void shouldSaveAndRetrieveValueFromCache() {
        // Arrange
        String key = "pendency:customer@example.com:Invoice";
        String value = UUID.randomUUID().toString();
        Duration ttl = Duration.ofMinutes(15);

        // Act
        adapter.save(key, value, ttl);
        boolean exists = adapter.exists(key);
        String retrieved = adapter.get(key);

        // Assert
        assertThat(exists).isTrue();
        assertThat(retrieved).isEqualTo(value);
    }

    @Test
    @DisplayName("Should return false when key does not exist")
    void shouldReturnFalseWhenKeyDoesNotExist() {
        // Act
        boolean exists = adapter.exists("non-existent-key");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return null when retrieving non-existent key")
    void shouldReturnNullWhenKeyDoesNotExist() {
        // Act
        String result = adapter.get("non-existent-key");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should remove key from cache")
    void shouldRemoveKeyFromCache() {
        // Arrange
        String key = "pendency:customer@example.com:Invoice";
        String value = UUID.randomUUID().toString();
        adapter.save(key, value, Duration.ofMinutes(15));

        // Act
        adapter.remove(key);
        boolean exists = adapter.exists(key);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should mark key as processed")
    void shouldMarkKeyAsProcessed() {
        // Arrange
        String key = "pendency:customer@example.com:Invoice";
        String value = UUID.randomUUID().toString();
        adapter.save(key, value, Duration.ofMinutes(15));

        // Act
        adapter.markProcessed(key);

        // Assert
        assertThat(adapter.exists(key)).isFalse();
    }

    @Test
    @DisplayName("Should support multiple keys simultaneously")
    void shouldSupportMultipleKeys() {
        // Arrange
        String key1 = "pendency:user1@example.com:Invoice1";
        String key2 = "pendency:user2@example.com:Invoice2";
        String value1 = UUID.randomUUID().toString();
        String value2 = UUID.randomUUID().toString();

        // Act
        adapter.save(key1, value1, Duration.ofMinutes(15));
        adapter.save(key2, value2, Duration.ofMinutes(15));

        // Assert
        assertThat(adapter.exists(key1)).isTrue();
        assertThat(adapter.exists(key2)).isTrue();
        assertThat(adapter.get(key1)).isEqualTo(value1);
        assertThat(adapter.get(key2)).isEqualTo(value2);
    }

    @Test
    @DisplayName("Should handle null key gracefully")
    void shouldHandleNullKey() {
        // Act & Assert
        assertThatThrownBy(() -> adapter.save(null, "value", Duration.ofMinutes(15)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle empty key")
    void shouldHandleEmptyKey() {
        // Arrange
        String emptyKey = "";
        String value = UUID.randomUUID().toString();

        // Act
        adapter.save(emptyKey, value, Duration.ofMinutes(15));

        // Assert
        assertThat(adapter.exists(emptyKey)).isTrue();
    }

    @Test
    @DisplayName("Should handle key case sensitivity")
    void shouldHandleKeyCaseSensitivity() {
        // Arrange
        String key1 = "pendency:customer@example.com:Invoice";
        String key2 = "pendency:CUSTOMER@EXAMPLE.COM:INVOICE";
        String value = UUID.randomUUID().toString();

        // Act
        adapter.save(key1, value, Duration.ofMinutes(15));

        // Assert
        assertThat(adapter.exists(key1)).isTrue();
        assertThat(adapter.exists(key2)).isFalse();
    }

    @Test
    @DisplayName("Should allow removing non-existent key")
    void shouldAllowRemovingNonExistentKey() {
        // Act & Assert - should not throw
        assertThatCode(() -> adapter.remove("non-existent-key"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw when storing null value in cache")
    void shouldHandleNullValues() {
        // Arrange
        String key = "test-key";
        String nullValue = null;

        // Act & Assert - ConcurrentHashMap doesn't allow null values
        assertThatThrownBy(() -> adapter.save(key, nullValue, Duration.ofMinutes(15)))
                .isInstanceOf(NullPointerException.class);
    }
}

