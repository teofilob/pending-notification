package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.persistence;

import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class InMemoryPendencyRepositoryTest {
    @Test
    void shouldReturnSavedPendencyPreservingIdentityAndData() {
        var repository = new InMemoryPendencyRepository();
        var pendency = new Pendency("Invoice", LocalDateTime.of(2026, 9, 20, 10, 0),
                new Recipient("user@example.com", "11999999999"));
        assertThat(repository.save(pendency)).isSameAs(pendency);
        assertThat(repository.save(pendency)).isSameAs(pendency);
    }
}
