package br.com.teofilob.pendingnotification.application.usecase;

import br.com.teofilob.pendingnotification.application.ports.out.NotificationPublisherPort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyCachePort;
import br.com.teofilob.pendingnotification.application.ports.out.PendencyRepositoryPort;
import br.com.teofilob.pendingnotification.domain.exception.PendencyAlreadyProcessingException;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePendencyService - Use Case Tests")
class CreatePendencyServiceTest {

    private static final String VALID_TITLE = "Invoice Payment";
    private static final String VALID_DUE_DATE = "2026-09-20T10:00:00";
    private static final String VALID_EMAIL = "customer@example.com";
    private static final String VALID_PHONE = "+5511999999999";

    @Mock
    private PendencyRepositoryPort pendencyRepository;

    @Mock
    private PendencyCachePort pendencyCachePort;

    @Mock
    private NotificationPublisherPort notificationPublisherPort;

    private CreatePendencyService service;

    @BeforeEach
    void setUp() {
        service = new CreatePendencyService(pendencyRepository, pendencyCachePort, notificationPublisherPort);
    }

    @Test
    @DisplayName("Should successfully create a pendency when not in cache")
    void shouldSuccessfullyCreatePendencyWhenNotInCache() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);

        // Act
        Pendency result = service.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo(VALID_TITLE);
        assertThat(result.getRecipient().email()).isEqualTo(VALID_EMAIL.toLowerCase());
        assertThat(result.getRecipient().phone()).isEqualTo(VALID_PHONE);

        // Verify interactions
        verify(pendencyCachePort).exists(contains(VALID_EMAIL.toLowerCase()));
        verify(pendencyCachePort).save(anyString(), anyString(), any());
        verify(pendencyRepository).save(any(Pendency.class));
        verify(notificationPublisherPort).publishPendencyCreated(any(Pendency.class));
    }

    @Test
    @DisplayName("Should throw exception when pendency already in cache")
    void shouldThrowExceptionWhenPendencyAlreadyInCache() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> service.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .isInstanceOf(PendencyAlreadyProcessingException.class)
                .hasMessageContaining(VALID_EMAIL.toLowerCase())
                .hasMessageContaining(VALID_TITLE);

        // Verify cache was checked but nothing else was done
        verify(pendencyCachePort).exists(contains(VALID_EMAIL.toLowerCase()));
        verify(pendencyCachePort, never()).save(anyString(), anyString(), any());
        verify(pendencyRepository, never()).save(any());
        verify(notificationPublisherPort, never()).publishPendencyCreated(any());
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmailToLowercase() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);
        String upperCaseEmail = "CUSTOMER@EXAMPLE.COM";

        // Act
        Pendency result = service.execute(VALID_TITLE, VALID_DUE_DATE, upperCaseEmail, VALID_PHONE);

        // Assert
        assertThat(result.getRecipient().email()).isEqualTo(upperCaseEmail.toLowerCase());
    }

    @Test
    @DisplayName("Should trim whitespace from inputs")
    void shouldTrimWhitespaceFromInputs() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);
        String titleWithSpaces = "  Invoice Payment  ";
        String emailWithSpaces = "  customer@example.com  ";

        // Act
        Pendency result = service.execute(titleWithSpaces, VALID_DUE_DATE, emailWithSpaces, VALID_PHONE);

        // Assert
        assertThat(result.getTitle()).isEqualTo(titleWithSpaces.trim());
        assertThat(result.getRecipient().email()).isEqualTo(emailWithSpaces.trim().toLowerCase());
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> service.execute(null, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("title must not be null");
    }

    @Test
    @DisplayName("Should throw exception when dueDate is null")
    void shouldThrowExceptionWhenDueDateIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> service.execute(VALID_TITLE, null, VALID_EMAIL, VALID_PHONE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dueDate must not be null");
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> service.execute(VALID_TITLE, VALID_DUE_DATE, null, VALID_PHONE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("email must not be null");
    }

    @Test
    @DisplayName("Should throw exception when dueDate format is invalid")
    void shouldThrowExceptionWhenDueDateFormatIsInvalid() {
        // Act & Assert
        assertThatThrownBy(() -> service.execute(VALID_TITLE, "invalid-date", VALID_EMAIL, VALID_PHONE))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should generate correct cache key format")
    void shouldGenerateCorrectCacheKeyFormat() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);

        // Act
        service.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);

        // Assert
        String expectedCacheKey = "pendency:" + VALID_EMAIL.toLowerCase() + ":" + VALID_TITLE;
        verify(pendencyCachePort).exists(expectedCacheKey);
        verify(pendencyCachePort).save(eq(expectedCacheKey), anyString(), any());
    }

    @Test
    @DisplayName("Should save to repository with correct data")
    void shouldSaveToRepositoryWithCorrectData() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);

        // Act
        service.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);

        // Assert
        verify(pendencyRepository).save(argThat(pendency ->
                pendency.getTitle().equals(VALID_TITLE) &&
                        pendency.getRecipient().email().equals(VALID_EMAIL.toLowerCase()) &&
                        pendency.getRecipient().phone().equals(VALID_PHONE)
        ));
    }

    @Test
    @DisplayName("Should publish to notification queue")
    void shouldPublishToNotificationQueue() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);

        // Act
        Pendency result = service.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);

        // Assert
        verify(notificationPublisherPort).publishPendencyCreated(result);
    }

    @Test
    @DisplayName("Should throw exception when pendencyRepository is null")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new CreatePendencyService(null, pendencyCachePort, notificationPublisherPort))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("pendencyRepository must not be null");
    }

    @Test
    @DisplayName("Should throw exception when cachePort is null")
    void shouldThrowExceptionWhenCachePortIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new CreatePendencyService(pendencyRepository, null, notificationPublisherPort))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("pendencyCachePort must not be null");
    }

    @Test
    @DisplayName("Should throw exception when publisherPort is null")
    void shouldThrowExceptionWhenPublisherPortIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new CreatePendencyService(pendencyRepository, pendencyCachePort, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("notificationPublisherPort must not be null");
    }

    @Test
    @DisplayName("Should handle multiple successive creations with different data")
    void shouldHandleMultipleSuccessiveCreations() {
        // Arrange
        when(pendencyCachePort.exists(anyString())).thenReturn(false);

        // Act
        Pendency result1 = service.execute("Invoice 1", VALID_DUE_DATE, "user1@example.com", "+5511111111111");
        Pendency result2 = service.execute("Invoice 2", VALID_DUE_DATE, "user2@example.com", "+5522222222222");

        // Assert
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getTitle()).isEqualTo("Invoice 1");
        assertThat(result2.getTitle()).isEqualTo("Invoice 2");
        verify(pendencyRepository, times(2)).save(any(Pendency.class));
        verify(notificationPublisherPort, times(2)).publishPendencyCreated(any(Pendency.class));
    }
}

