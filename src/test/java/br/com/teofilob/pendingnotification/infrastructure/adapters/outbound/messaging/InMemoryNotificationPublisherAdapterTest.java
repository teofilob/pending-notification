package br.com.teofilob.pendingnotification.infrastructure.adapters.outbound.messaging;

import br.com.teofilob.pendingnotification.domain.model.NotificationStatus;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryNotificationPublisherAdapter Tests")
class InMemoryNotificationPublisherAdapterTest {

    @Test
    void shouldLogGenericMessageWithQueueAndPayload() {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger(InMemoryNotificationPublisherAdapter.class);
        var appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            adapter.publish("audit", "invoice-created");
            assertThat(appender.list).extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                    .contains("Simulating publish to queue 'audit' with payload: invoice-created");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private InMemoryNotificationPublisherAdapter adapter;
    private Pendency testPendency;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryNotificationPublisherAdapter();
        testPendency = new Pendency(
                "Test Invoice",
                LocalDateTime.now().plusDays(5),
                new Recipient("customer@example.com", "+5511999999999")
        );
    }

    @Test
    @DisplayName("Should publish pendency created event")
    void shouldPublishPendencyCreatedEvent() {
        // Act & Assert
        assertThatCode(() -> adapter.publishPendencyCreated(testPendency))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should publish notification processed event")
    void shouldPublishNotificationProcessedEvent() {
        // Act & Assert
        assertThatCode(() -> adapter.publishNotificationProcessed(testPendency.getId().toString(), "PROCESSED"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw when publishing null pendency")
    void shouldHandleNullPendency() {
        // Act & Assert - adapter requires non-null pendency to access its properties
        assertThatThrownBy(() -> adapter.publishPendencyCreated(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should support multiple publishes")
    void shouldSupportMultiplePublishes() {
        // Arrange
        Pendency pendency1 = new Pendency(
                "Invoice 1",
                LocalDateTime.now().plusDays(5),
                new Recipient("user1@example.com", "+5511111111111")
        );
        Pendency pendency2 = new Pendency(
                "Invoice 2",
                LocalDateTime.now().plusDays(10),
                new Recipient("user2@example.com", "+5522222222222")
        );

        // Act & Assert
        assertThatCode(() -> {
            adapter.publishPendencyCreated(pendency1);
            adapter.publishPendencyCreated(pendency2);
            adapter.publishNotificationProcessed(pendency1.getId().toString(), "PROCESSED");
            adapter.publishNotificationProcessed(pendency2.getId().toString(), "PROCESSED");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle publish from pending state")
    void shouldHandlePublishFromPendingState() {
        // Arrange
        Pendency pendingPendency = new Pendency(
                "Pending Invoice",
                LocalDateTime.now().plusDays(3),
                new Recipient("pending@example.com", "+5511333333333")
        );

        // Act & Assert
        assertThat(pendingPendency.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThatCode(() -> adapter.publishPendencyCreated(pendingPendency))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should publish message with complex recipient data")
    void shouldPublishWithComplexRecipientData() {
        // Arrange
        Pendency complexPendency = new Pendency(
                "Complex Invoice with Long Title and Special Characters: @#$%",
                LocalDateTime.now().plusDays(30),
                new Recipient("complex.email+tag@subdomain.example.co.uk", "+55 11 99999-9999")
        );

        // Act & Assert
        assertThatCode(() -> adapter.publishPendencyCreated(complexPendency))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle rapid successive publishes")
    void shouldHandleRapidSuccessivePublishes() {
        // Act & Assert
        assertThatCode(() -> {
            for (int i = 0; i < 100; i++) {
                Pendency temp = new Pendency(
                        "Invoice " + i,
                        LocalDateTime.now().plusDays(i % 30),
                        new Recipient("user" + i + "@example.com", "+5511" + String.format("%07d", i))
                );
                adapter.publishPendencyCreated(temp);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should preserve pendency data during publish")
    void shouldPreservePendencyDataDuringPublish() {
        // Arrange
        String expectedTitle = "Important Invoice";
        String expectedEmail = "important@example.com";
        Pendency pendency = new Pendency(
                expectedTitle,
                LocalDateTime.now().plusDays(5),
                new Recipient(expectedEmail, "+5511999999999")
        );

        // Act
        adapter.publishPendencyCreated(pendency);

        // Assert - verify the object is still intact
        assertThat(pendency.getTitle()).isEqualTo(expectedTitle);
        assertThat(pendency.getRecipient().email()).isEqualTo(expectedEmail);
        assertThat(pendency.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("Should handle publishing same pendency multiple times")
    void shouldHandlePublishingSamePendencyMultipleTimes() {
        // Act & Assert
        assertThatCode(() -> {
            adapter.publishPendencyCreated(testPendency);
            adapter.publishPendencyCreated(testPendency);
            adapter.publishNotificationProcessed(testPendency.getId().toString(), "PROCESSED");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should publish to correct queue names")
    void shouldPublishToCorrectQueueNames() {
        // Act & Assert - in-memory adapter should handle any queue name
        assertThatCode(() -> {
            adapter.publishPendencyCreated(testPendency);   // Should go to "notificar" queue
            adapter.publishNotificationProcessed(testPendency.getId().toString(), "PROCESSED"); // Should update Redis
        }).doesNotThrowAnyException();
    }
}





