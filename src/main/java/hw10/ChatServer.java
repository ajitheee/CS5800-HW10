package hw10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Mediator: owns registration, routing, blocking, and coordinated undo. */
public final class ChatServer {
    private final Map<String, User> users = new HashMap<>();
    private final Map<User, Set<User>> blockedUsers = new HashMap<>();

    public void registerUser(User user) {
        Objects.requireNonNull(user, "user");
        if (!user.usesServer(this)) {
            throw new IllegalArgumentException("User belongs to a different chat server.");
        }
        User existing = users.putIfAbsent(user.getName(), user);
        if (existing != null && existing != user) {
            throw new IllegalArgumentException("User name already registered: " + user.getName());
        }
        blockedUsers.computeIfAbsent(user, ignored -> new HashSet<>());
        if (existing == null) {
            System.out.println(user.getName() + " registered.");
        }
    }

    public void unregisterUser(User user) {
        Objects.requireNonNull(user, "user");
        if (users.remove(user.getName(), user)) {
            System.out.println(user.getName() + " unregistered.");
        }
    }

    public void blockUser(User blocker, User blocked) {
        requireRegistered(blocker);
        requireRegistered(blocked);
        if (blocker == blocked) {
            throw new IllegalArgumentException("A user cannot block themselves.");
        }
        blockedUsers.get(blocker).add(blocked);
        System.out.println(blocker.getName() + " blocked " + blocked.getName() + ".");
    }

    public boolean sendMessage(User sender, List<User> recipients, String content) {
        requireRegistered(sender);
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(recipients, "recipients");
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one recipient.");
        }

        Set<User> uniqueRecipients = new LinkedHashSet<>(recipients);
        // Validate the whole request before making any deliveries.
        for (User recipient : uniqueRecipients) {
            Objects.requireNonNull(recipient, "recipient");
            if (recipient == sender) {
                throw new IllegalArgumentException("Choose another user as recipient.");
            }
        }

        List<User> deliveredTo = new ArrayList<>();
        for (User recipient : uniqueRecipients) {
            if (users.get(recipient.getName()) != recipient) {
                System.out.println("Not delivered to " + recipient.getName() + ": not registered.");
            } else if (blockedUsers.get(recipient).contains(sender)) {
                System.out.println("Blocked: " + sender.getName() + " -> " + recipient.getName());
            } else {
                deliveredTo.add(recipient);
            }
        }
        if (deliveredTo.isEmpty()) {
            return false;
        }

        Message message = new Message(sender, deliveredTo, content);
        sender.recordSentMessage(message);
        for (User recipient : deliveredTo) {
            recipient.receiveMessage(message);
        }
        return true;
    }

    public boolean undoLastMessage(User sender) {
        requireRegistered(sender);
        MessageMemento snapshot = sender.getLastSentSnapshot();
        if (snapshot == null) {
            System.out.println(sender.getName() + " has no sent message to undo.");
            return false;
        }
        sender.getChatHistory().removeMessage(snapshot);
        for (User recipient : snapshot.getRecipients()) {
            recipient.getChatHistory().removeMessage(snapshot);
        }
        sender.discardLastSentSnapshot();
        System.out.println(sender.getName() + " undid: \"" + snapshot.getContent()
                + "\" (sent at " + snapshot.getTimestamp() + ")");
        return true;
    }

    private void requireRegistered(User user) {
        Objects.requireNonNull(user, "user");
        if (users.get(user.getName()) != user) {
            throw new IllegalStateException("User is not registered: " + user.getName());
        }
    }
}
