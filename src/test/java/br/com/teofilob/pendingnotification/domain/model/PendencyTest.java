package br.com.teofilob.pendingnotification.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Pendency - Domain Model Tests")
class PendencyTest {

    private static final String VALID_TITLE = "Invoice Payment";
    private static final LocalDateTime VALID_DUE_DATE = LocalDateTime.now().plusDays(5);
    private static final String VALID_EMAIL = "customer@example.com";
    private static final String VALID_PHONE = "+5511999999999";

    @Test
    @DisplayName("Should create a Pendency with valid data")
    void shouldCreatePendencyWithValidData() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act
        Pendency pendency = new Pendency(VALID_TITLE, VALID_DUE_DATE, recipient);

        // Assert
        assertThat(pendency).isNotNull();
        assertThat(pendency.getId()).isNotNull();
        assertThat(pendency.getTitle()).isEqualTo(VALID_TITLE);
        assertThat(pendency.getDueDate()).isEqualTo(VALID_DUE_DATE);
        assertThat(pendency.getRecipient()).isEqualTo(recipient);
        assertThat(pendency.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("Should create Pendency with custom ID and status")
    void shouldCreatePendencyWithCustomIdAndStatus() {
        // Arrange
        UUID customId = UUID.randomUUID();
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);
        NotificationStatus status = NotificationStatus.SENT;

        // Act
        Pendency pendency = new Pendency(customId, VALID_TITLE, VALID_DUE_DATE, recipient, status);

        // Assert
        assertThat(pendency.getId()).isEqualTo(customId);
        assertThat(pendency.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThatThrownBy(() -> new Pendency(null, VALID_DUE_DATE, recipient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThatThrownBy(() -> new Pendency("   ", VALID_DUE_DATE, recipient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    @DisplayName("Should throw NullPointerException when dueDate is null")
    void shouldThrowExceptionWhenDueDateIsNull() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThatThrownBy(() -> new Pendency(VALID_TITLE, null, recipient))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dueDate must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when recipient is null")
    void shouldThrowExceptionWhenRecipientIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new Pendency(VALID_TITLE, VALID_DUE_DATE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("recipient must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when id is null in full constructor")
    void shouldThrowExceptionWhenIdIsNull() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThatThrownBy(() -> new Pendency(null, VALID_TITLE, VALID_DUE_DATE, recipient, NotificationStatus.PENDING))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        // Arrange
        UUID id = UUID.randomUUID();
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThatThrownBy(() -> new Pendency(id, VALID_TITLE, VALID_DUE_DATE, recipient, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status must not be null");
    }

    @Test
    @DisplayName("Should generate unique IDs for different Pendencies")
    void shouldGenerateUniqueIds() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act
        Pendency pendency1 = new Pendency(VALID_TITLE, VALID_DUE_DATE, recipient);
        Pendency pendency2 = new Pendency(VALID_TITLE, VALID_DUE_DATE, recipient);

        // Assert
        assertThat(pendency1.getId())
                .isNotEqualTo(pendency2.getId());
    }

    @Test
    @DisplayName("Should have different status values")
    void shouldSupportMultipleStatusValues() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);
        UUID id = UUID.randomUUID();

        // Act & Assert
        Pendency pending = new Pendency(id, VALID_TITLE, VALID_DUE_DATE, recipient, NotificationStatus.PENDING);
        Pendency sent = new Pendency(UUID.randomUUID(), VALID_TITLE, VALID_DUE_DATE, recipient, NotificationStatus.SENT);
        Pendency failed = new Pendency(UUID.randomUUID(), VALID_TITLE, VALID_DUE_DATE, recipient, NotificationStatus.FAILED);

        assertThat(pending.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(sent.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(failed.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    @DisplayName("Should allow future and past due dates")
    void shouldAllowAnyDueDate() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        LocalDateTime pastDate = LocalDateTime.now().minusDays(5);

        // Act
        Pendency futurePendency = new Pendency("Future Invoice", futureDate, recipient);
        Pendency pastPendency = new Pendency("Past Invoice", pastDate, recipient);

        // Assert
        assertThat(futurePendency.getDueDate()).isEqualTo(futureDate);
        assertThat(pastPendency.getDueDate()).isEqualTo(pastDate);
    }
}

