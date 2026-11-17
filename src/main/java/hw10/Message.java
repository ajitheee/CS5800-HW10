package hw10;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** An immutable message created and delivered by the mediator. */
public final class Message {
    private final User sender;
    private final List<User> recipients;
    private final LocalDateTime timestamp;
    private final String content;

    public Message(User sender, List<User> recipients, String content) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.recipients = List.copyOf(recipients);
        if (this.recipients.isEmpty()) {
            throw new IllegalArgumentException("A message needs at least one recipient.");
        }
        this.content = Objects.requireNonNull(content, "content");
        this.timestamp = LocalDateTime.now();
    }

    public User getSender() {
        return sender;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    /** Message is the originator; User retains this snapshot for undo. */
    public MessageMemento save() {
        return new MessageMemento(this);
    }

    @Override
    public String toString() {
        String names = recipients.stream().map(User::getName)
                .collect(Collectors.joining(", "));
        return "[" + timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "] " + sender.getName() + " -> " + names + ": " + content;
    }
}
