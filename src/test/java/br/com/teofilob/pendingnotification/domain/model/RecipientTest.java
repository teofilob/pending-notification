package br.com.teofilob.pendingnotification.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Recipient - Value Object Tests")
class RecipientTest {

    private static final String VALID_EMAIL = "customer@example.com";
    private static final String VALID_PHONE = "+5511999999999";

    @Test
    @DisplayName("Should create a Recipient record with valid data")
    void shouldCreateRecipientWithValidData() {
        // Act
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Assert
        assertThat(recipient).isNotNull();
        assertThat(recipient.email()).isEqualTo(VALID_EMAIL);
        assertThat(recipient.phone()).isEqualTo(VALID_PHONE);
    }

    @Test
    @DisplayName("Should throw NullPointerException when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new Recipient(null, VALID_PHONE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("email must not be null");
    }

    @Test
    @DisplayName("Should throw NullPointerException when phone is null")
    void shouldThrowExceptionWhenPhoneIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new Recipient(VALID_EMAIL, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("phone must not be null");
    }

    @Test
    @DisplayName("Should allow empty strings for email and phone")
    void shouldAllowEmptyStrings() {
        // Act
        Recipient recipient = new Recipient("", "");

        // Assert
        assertThat(recipient.email()).isEmpty();
        assertThat(recipient.phone()).isEmpty();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        // Arrange
        Recipient recipient1 = new Recipient(VALID_EMAIL, VALID_PHONE);
        Recipient recipient2 = new Recipient(VALID_EMAIL, VALID_PHONE);
        Recipient recipient3 = new Recipient("other@example.com", "+5511888888888");

        // Act & Assert
        assertThat(recipient1).isEqualTo(recipient2);
        assertThat(recipient1).isNotEqualTo(recipient3);
    }

    @Test
    @DisplayName("Should support hashing")
    void shouldSupportHashing() {
        // Arrange
        Recipient recipient1 = new Recipient(VALID_EMAIL, VALID_PHONE);
        Recipient recipient2 = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act & Assert
        assertThat(recipient1.hashCode()).isEqualTo(recipient2.hashCode());
    }

    @Test
    @DisplayName("Should format as string")
    void shouldFormatAsString() {
        // Arrange
        Recipient recipient = new Recipient(VALID_EMAIL, VALID_PHONE);

        // Act
        String toString = recipient.toString();

        // Assert
        assertThat(toString).contains(VALID_EMAIL);
        assertThat(toString).contains(VALID_PHONE);
    }

    @Test
    @DisplayName("Should support special characters in email")
    void shouldAllowSpecialCharactersInEmail() {
        // Arrange
        String emailWithSpecialChars = "customer+tag@example.co.uk";

        // Act
        Recipient recipient = new Recipient(emailWithSpecialChars, VALID_PHONE);

        // Assert
        assertThat(recipient.email()).isEqualTo(emailWithSpecialChars);
    }

    @Test
    @DisplayName("Should support international phone numbers")
    void shouldAllowInternationalPhoneNumbers() {
        // Arrange
        String usPhonenumber = "+1-555-123-4567";
        String ukPhoneNumber = "+44-20-7946-0958";
        String brazilianPhoneNumber = "+55 11 99999-9999";

        // Act
        Recipient recipient1 = new Recipient(VALID_EMAIL, usPhonenumber);
        Recipient recipient2 = new Recipient(VALID_EMAIL, ukPhoneNumber);
        Recipient recipient3 = new Recipient(VALID_EMAIL, brazilianPhoneNumber);

        // Assert
        assertThat(recipient1.phone()).isEqualTo(usPhonenumber);
        assertThat(recipient2.phone()).isEqualTo(ukPhoneNumber);
        assertThat(recipient3.phone()).isEqualTo(brazilianPhoneNumber);
    }
}

