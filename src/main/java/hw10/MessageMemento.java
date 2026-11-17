package hw10;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable snapshot saved at send time. Undo recalls the exact saved message
 * from each history, restoring the state in which that message was absent.
 */
public final class MessageMemento {
    private final String content;
    private final LocalDateTime timestamp;
    private final Message original;

    MessageMemento(Message message) {
        content = message.getContent();
        timestamp = message.getTimestamp();
        original = message;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Identity distinguishes separate sends even when their content is identical.
    boolean matches(Message message) {
        return original == message;
    }

    List<User> getRecipients() {
        return original.getRecipients();
    }
}
