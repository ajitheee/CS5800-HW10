package hw10;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Lowercase class name retained exactly as specified by HW10. */
public final class searchMessagesByUser implements Iterator<Message> {
    private final List<Message> messages;
    private final String userName;
    private int position;

    public searchMessagesByUser(List<Message> messages, User userToSearchWith) {
        this.messages = List.copyOf(messages);
        userName = Objects.requireNonNull(userToSearchWith, "userToSearchWith").getName();
    }

    @Override
    public boolean hasNext() {
        while (position < messages.size() && !involvesUser(messages.get(position))) {
            position++;
        }
        return position < messages.size();
    }

    @Override
    public Message next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more messages for " + userName);
        }
        return messages.get(position++);
    }

    private boolean involvesUser(Message message) {
        return message.getSender().getName().equals(userName)
                || message.getRecipients().stream()
                .anyMatch(recipient -> recipient.getName().equals(userName));
    }
}
