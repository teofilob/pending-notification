package br.com.teofilob.pendingnotification.application.ports.out;

import br.com.teofilob.pendingnotification.domain.model.Pendency;

public interface PendencyRepositoryPort {
    Pendency save(Pendency pendency);
}

