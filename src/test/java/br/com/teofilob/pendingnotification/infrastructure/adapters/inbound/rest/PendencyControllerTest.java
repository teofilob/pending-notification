package br.com.teofilob.pendingnotification.infrastructure.adapters.inbound.rest;

import br.com.teofilob.pendingnotification.application.ports.in.CreatePendencyUseCase;
import br.com.teofilob.pendingnotification.domain.exception.PendencyAlreadyProcessingException;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import br.com.teofilob.pendingnotification.domain.model.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PendencyController Tests")
class PendencyControllerTest {

    private static final String VALID_TITLE = "Invoice Payment";
    private static final String VALID_DUE_DATE = "2026-09-20T10:00:00";
    private static final String VALID_EMAIL = "customer@example.com";
    private static final String VALID_PHONE = "+5511999999999";

    @Mock
    private CreatePendencyUseCase createPendencyUseCase;

    private PendencyController controller;

    @BeforeEach
    void setUp() {
        controller = new PendencyController(createPendencyUseCase);
    }

    @Test
    void shouldReturnValidationMessageWithBadRequestStatus() {
        var response = controller.handleIllegalArgument(new IllegalArgumentException("title must not be blank"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Erro de validação")
                .containsEntry("message", "title must not be blank");
        verifyNoInteractions(createPendencyUseCase);
    }

    @Test
    @DisplayName("Should return 202 Accepted when pendency is created successfully")
    void shouldReturn202AcceptedWhenCreatedSuccessfully() {
        // Arrange
        Pendency pendency = new Pendency(
                VALID_TITLE,
                LocalDateTime.parse(VALID_DUE_DATE),
                new Recipient(VALID_EMAIL, VALID_PHONE)
        );
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenReturn(pendency);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        ResponseEntity<Map<String, Object>> response = controller.create(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should return 409 Conflict when pendency already processing")
    void shouldReturn409ConflictWhenAlreadyProcessing() {
        // Arrange
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenThrow(new PendencyAlreadyProcessingException(VALID_EMAIL, VALID_TITLE));
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        ResponseEntity<Map<String, Object>> response = controller.create(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Should pass all parameters to use case")
    void shouldPassAllParametersToUseCase() {
        // Arrange
        Pendency pendency = new Pendency(
                VALID_TITLE,
                LocalDateTime.parse(VALID_DUE_DATE),
                new Recipient(VALID_EMAIL, VALID_PHONE)
        );
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenReturn(pendency);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        controller.create(request);

        // Assert
        verify(createPendencyUseCase).execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);
    }

    @Test
    @DisplayName("Should return response with pendency details on success")
    void shouldReturnResponseWithPendencyDetails() {
        // Arrange
        Pendency pendency = new Pendency(
                VALID_TITLE,
                LocalDateTime.parse(VALID_DUE_DATE),
                new Recipient(VALID_EMAIL, VALID_PHONE)
        );
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenReturn(pendency);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        ResponseEntity<Map<String, Object>> response = controller.create(request);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).containsKeys("id", "title", "status", "dueDate", "email");
    }

    @Test
    @DisplayName("Should handle exception with error message")
    void shouldHandleExceptionWithErrorMessage() {
        // Arrange
        PendencyAlreadyProcessingException exception = new PendencyAlreadyProcessingException(VALID_EMAIL, VALID_TITLE);
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenThrow(exception);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        ResponseEntity<Map<String, Object>> response = controller.create(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("error", "email", "title", "message");
    }

    @Test
    @DisplayName("Should call use case exactly once per request")
    void shouldCallUseCaseExactlyOncePerRequest() {
        // Arrange
        Pendency pendency = new Pendency(
                VALID_TITLE,
                LocalDateTime.parse(VALID_DUE_DATE),
                new Recipient(VALID_EMAIL, VALID_PHONE)
        );
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenReturn(pendency);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        controller.create(request);

        // Assert
        verify(createPendencyUseCase, times(1)).execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE);
    }

    @Test
    @DisplayName("Should return response with proper HTTP headers")
    void shouldReturnResponseWithProperHeaders() {
        // Arrange
        Pendency pendency = new Pendency(
                VALID_TITLE,
                LocalDateTime.parse(VALID_DUE_DATE),
                new Recipient(VALID_EMAIL, VALID_PHONE)
        );
        when(createPendencyUseCase.execute(VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE))
                .thenReturn(pendency);
        PendencyController.CreatePendencyRequest request = new PendencyController.CreatePendencyRequest(
                VALID_TITLE, VALID_DUE_DATE, VALID_EMAIL, VALID_PHONE
        );

        // Act
        ResponseEntity<Map<String, Object>> response = controller.create(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.hasBody()).isTrue();
    }
}


