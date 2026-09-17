package br.com.teofilob.pendingnotification.infrastructure.adapters.inbound.rest;

import br.com.teofilob.pendingnotification.application.ports.in.CreatePendencyUseCase;
import br.com.teofilob.pendingnotification.domain.exception.PendencyAlreadyProcessingException;
import br.com.teofilob.pendingnotification.domain.model.Pendency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador REST para gerenciamento de pendências.
 * Adapter de entrada (Inbound) da arquitetura hexagonal.
 */
@RestController
@RequestMapping("/api/v1")
public class PendencyController {
    
    private final CreatePendencyUseCase createPendencyUseCase;

    public PendencyController(CreatePendencyUseCase createPendencyUseCase) {
        this.createPendencyUseCase = createPendencyUseCase;
    }

    /**
     * Cria uma nova pendência.
     * 
     * Retorna:
     * - 202 Accepted: Pendência criada com sucesso e enfileirada para processamento
     * - 409 Conflict: Pendência com o mesmo email e título já está em processamento
     * - 400 Bad Request: Dados de entrada inválidos
     * 
     * @param request dados da pendência
     * @return ResponseEntity com os detalhes da pendência criada
     */
    @PostMapping("/pendencies")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreatePendencyRequest request) {
        try {
            Pendency pendency = createPendencyUseCase.execute(
                    request.title(),
                    request.dueDate(),
                    request.email(),
                    request.phone()
            );

            return ResponseEntity.accepted().body(Map.of(
                    "id", pendency.getId(),
                    "title", pendency.getTitle(),
                    "status", pendency.getStatus(),
                    "dueDate", pendency.getDueDate(),
                    "email", pendency.getRecipient().email(),
                    "message", "Pendência criada com sucesso e enfileirada para processamento"
            ));
        } catch (PendencyAlreadyProcessingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Pendência já em processamento",
                    "email", e.getEmail(),
                    "title", e.getTitle(),
                    "message", "Uma pendência com os mesmos dados já está sendo processada. Tente novamente mais tarde."
            ));
        }
    }

    /**
     * DTO de entrada para criação de pendência.
     */
    public record CreatePendencyRequest(
            @NotBlank(message = "O campo 'title' não pode estar vazio")
            String title,
            
            @NotBlank(message = "O campo 'dueDate' não pode estar vazio")
            String dueDate,
            
            @NotBlank(message = "O campo 'email' não pode estar vazio")
            @Email(message = "O campo 'email' deve ser um endereço válido")
            String email,
            
            @NotBlank(message = "O campo 'phone' não pode estar vazio")
            String phone
    ) {
    }

    /**
     * Tratador global de exceções para validação.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Erro de validação",
                "message", e.getMessage()
        ));
    }
}
