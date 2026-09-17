package br.com.teofilob.pendingnotification.application.ports.in;

import br.com.teofilob.pendingnotification.domain.model.Pendency;

public interface CreatePendencyUseCase {
    Pendency execute(String title, String dueDate, String email, String phone);
}

