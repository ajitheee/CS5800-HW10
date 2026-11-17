package hw10;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

/** A colleague of ChatServer and the caretaker of sent-message mementos. */
public final class User implements IterableByUser {
    private final String name;
    private final ChatServer server;
    private final ChatHistory chatHistory = new ChatHistory();
    private final Deque<MessageMemento> sentMessages = new ArrayDeque<>();

    public User(String name, ChatServer server) {
        this.name = Objects.requireNonNull(name, "name").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("A user needs a name.");
        }
        this.server = Objects.requireNonNull(server, "server");
    }

    public String getName() {
        return name;
    }

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public boolean sendMessage(String content, User... recipients) {
        return server.sendMessage(this, Arrays.asList(recipients), content);
    }

    // Only the mediator delivers messages; users never call each other.
    void receiveMessage(Message message) {
        chatHistory.addMessage(message);
        System.out.println(name + " received from " + message.getSender().getName()
                + ": " + message.getContent());
    }

    void recordSentMessage(Message message) {
        chatHistory.addMessage(message);
        sentMessages.push(message.save());
    }

    MessageMemento getLastSentSnapshot() {
        return sentMessages.peek();
    }

    void discardLastSentSnapshot() {
        sentMessages.pop();
    }

    boolean usesServer(ChatServer candidate) {
        return server == candidate;
    }

    public boolean undoLastMessage() {
        return server.undoLastMessage(this);
    }

    public void blockUser(User user) {
        server.blockUser(this, user);
    }

    public void viewChatHistory() {
        System.out.println("\nChat history for " + name + ":");
        chatHistory.printHistory();
    }

    @Override
    public Iterator<Message> iterator(User userToSearchWith) {
        return chatHistory.iterator(userToSearchWith);
    }
}
