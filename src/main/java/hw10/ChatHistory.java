package hw10;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** Stores one user's sent and received messages in delivery order. */
public final class ChatHistory implements IterableByUser {
    private final List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(Objects.requireNonNull(message, "message"));
    }

    public Message getLastMessageSent(User sender) {
        Objects.requireNonNull(sender, "sender");
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getSender() == sender) {
                return messages.get(i);
            }
        }
        return null;
    }

    void removeMessage(MessageMemento snapshot) {
        messages.removeIf(snapshot::matches);
    }

    public List<Message> getMessages() {
        return List.copyOf(messages);
    }

    public void printHistory() {
        if (messages.isEmpty()) {
            System.out.println("(no messages)");
        } else {
            messages.forEach(System.out::println);
        }
    }

    @Override
    public Iterator<Message> iterator(User userToSearchWith) {
        return new searchMessagesByUser(messages, userToSearchWith);
    }
}
