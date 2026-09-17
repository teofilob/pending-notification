package br.com.teofilob.pendingnotification.application.usecase;

import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyRepositoryPort;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessNotificationServiceTest {
    private final PendencyRepositoryPort repository = mock(PendencyRepositoryPort.class);
    private final PendencyCachePort cache = mock(PendencyCachePort.class);
    private final ObjectMapper mapper = mock(ObjectMapper.class);
    private final ProcessNotificationService service = new ProcessNotificationService(repository, cache, mapper);
    private final Pendency pendency = new Pendency("Invoice", LocalDateTime.of(2026, 9, 20, 10, 0),
            new Recipient("user@example.com", "11999999999"));
    private static final String JSON = "{\"title\":\"Invoice\"}";
    private static final String KEY = "pendency:user@example.com:Invoice";

    @Test
    void shouldPersistDeserializedPendencyBeforeMarkingCache() throws Exception {
        when(mapper.readValue(JSON, Pendency.class)).thenReturn(pendency);
        service.processNotification(JSON);
        var order = inOrder(repository, cache);
        order.verify(repository).save(same(pendency));
        order.verify(cache).markProcessed(KEY);
        verifyNoMoreInteractions(repository, cache);
    }

    @Test
    void shouldPreserveDeserializationFailureWithoutSideEffects() throws Exception {
        var failure = new JsonProcessingException("invalid JSON") { };
        when(mapper.readValue(JSON, Pendency.class)).thenThrow(failure);
        assertThatThrownBy(() -> service.processNotification(JSON))
                .isInstanceOf(RuntimeException.class).hasCause(failure).hasMessageContaining("invalid JSON");
        verifyNoInteractions(repository, cache);
    }

    @Test
    void shouldNotMarkCacheWhenPersistenceFails() throws Exception {
        when(mapper.readValue(JSON, Pendency.class)).thenReturn(pendency);
        var failure = new IllegalStateException("database unavailable");
        when(repository.save(pendency)).thenThrow(failure);
        assertThatThrownBy(() -> service.processNotification(JSON)).hasCause(failure);
        verifyNoInteractions(cache);
    }

    @Test
    void shouldPreserveCacheFailureAfterPersistence() throws Exception {
        when(mapper.readValue(JSON, Pendency.class)).thenReturn(pendency);
        var failure = new IllegalStateException("cache unavailable");
        doThrow(failure).when(cache).markProcessed(KEY);
        assertThatThrownBy(() -> service.processNotification(JSON)).hasCause(failure);
        verify(repository).save(pendency);
    }

    @Test
    void shouldRejectMissingDependencies() {
        assertThatNullPointerException().isThrownBy(() -> new ProcessNotificationService(null, cache, mapper))
                .withMessage("pendencyRepository must not be null");
        assertThatNullPointerException().isThrownBy(() -> new ProcessNotificationService(repository, null, mapper))
                .withMessage("pendencyCachePort must not be null");
        assertThatNullPointerException().isThrownBy(() -> new ProcessNotificationService(repository, cache, null))
                .withMessage("objectMapper must not be null");
    }
}
