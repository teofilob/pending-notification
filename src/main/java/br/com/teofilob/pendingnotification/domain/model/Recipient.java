package br.com.teofilob.pendingnotification.domain.model;

import java.util.Objects;

public record Recipient(String email, String phone) {
    public Recipient {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
    }
}

