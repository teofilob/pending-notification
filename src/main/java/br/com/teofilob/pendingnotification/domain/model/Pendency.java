package br.com.teofilob.pendingnotification.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Pendency {
    private final UUID id;
    private final String title;
    private final LocalDateTime dueDate;
    private final Recipient recipient;
    private final NotificationStatus status;

    public Pendency(String title, LocalDateTime dueDate, Recipient recipient) {
        this(UUID.randomUUID(), title, dueDate, recipient, NotificationStatus.PENDING);
    }

    public Pendency(UUID id, String title, LocalDateTime dueDate, Recipient recipient, NotificationStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = validateTitle(title);
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.recipient = Objects.requireNonNull(recipient, "recipient must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public NotificationStatus getStatus() {
        return status;
    }
}

