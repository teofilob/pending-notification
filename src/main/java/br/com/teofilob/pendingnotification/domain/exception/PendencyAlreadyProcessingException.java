package br.com.teofilob.pendingnotification.domain.exception;

/**
 * Exceção de domínio lançada quando uma pendência já está sendo processada.
 * Ocorre quando tenta-se criar uma pendência com o mesmo email e title que já está em processamento no Redis.
 */
public class PendencyAlreadyProcessingException extends RuntimeException {

    private final String email;
    private final String title;

    public PendencyAlreadyProcessingException(String email, String title) {
        super("Pendência já em processamento: email=" + email + ", title=" + title);
        this.email = email;
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public String getTitle() {
        return title;
    }
}

