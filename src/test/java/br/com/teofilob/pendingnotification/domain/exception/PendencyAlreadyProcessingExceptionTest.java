package br.com.teofilob.pendingnotification.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PendencyAlreadyProcessingException Tests")
class PendencyAlreadyProcessingExceptionTest {

    private static final String TEST_EMAIL = "customer@example.com";
    private static final String TEST_TITLE = "Invoice Payment";

    @Test
    @DisplayName("Should create exception with email and title")
    void shouldCreateExceptionWithEmailAndTitle() {
        // Act
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(TEST_EMAIL, TEST_TITLE);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getTitle()).isEqualTo(TEST_TITLE);
    }

    @Test
    @DisplayName("Should include email and title in exception message")
    void shouldIncludeDetailsInMessage() {
        // Act
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(TEST_EMAIL, TEST_TITLE);

        // Assert
        assertThat(exception.getMessage())
                .contains("Pendência já em processamento")
                .contains(TEST_EMAIL)
                .contains(TEST_TITLE);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // Act
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(TEST_EMAIL, TEST_TITLE);

        // Assert
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should allow null email")
    void shouldAllowNullEmail() {
        // Act
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(null, TEST_TITLE);

        // Assert
        assertThat(exception.getEmail()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("Should allow null title")
    void shouldAllowNullTitle() {
        // Act
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(TEST_EMAIL, null);

        // Assert
        assertThat(exception.getTitle()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void shouldBeThrowableAndCatchable() {
        // Act & Assert
        assertThatThrownBy(() -> {
            throw new PendencyAlreadyProcessingException(TEST_EMAIL, TEST_TITLE);
        })
                .isInstanceOf(PendencyAlreadyProcessingException.class)
                .hasMessageContaining(TEST_EMAIL)
                .hasMessageContaining(TEST_TITLE);
    }
}

