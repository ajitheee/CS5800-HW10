package hw10;

import java.util.Iterator;

/** Demonstrates all HW10 requirements with exactly three users. */
public final class ChatAppDriver {
    private ChatAppDriver() {
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        User alice = new User("Alice", server);
        User bob = new User("Bob", server);
        User charlie = new User("Charlie", server);
        server.registerUser(alice);
        server.registerUser(bob);
        server.registerUser(charlie);

        System.out.println("\n--- SEND TO ONE OR MORE USERS ---");
        alice.sendMessage("Hello Bob!", bob);
        bob.sendMessage("Hi Alice!", alice);
        charlie.sendMessage("Hello everyone!", alice, bob);

        System.out.println("\n--- BLOCK MESSAGES THROUGH THE MEDIATOR ---");
        bob.blockUser(charlie);
        charlie.sendMessage("Bob will not receive this message.", bob);
        charlie.sendMessage("Only Alice receives this group message.", alice, bob);

        System.out.println("\n--- UNDO THE LAST SENT MESSAGE USING A MEMENTO ---");
        alice.sendMessage("This group message will be undone.", bob, charlie);
        // A newer incoming message must not change Alice's last sent message.
        bob.sendMessage("Alice, did you send something?", alice);
        alice.undoLastMessage();
        System.out.println("The recalled message is absent from all three histories.");

        System.out.println("\n--- VIEW EACH USER'S CHAT HISTORY ---");
        alice.viewChatHistory();
        bob.viewChatHistory();
        charlie.viewChatHistory();
        System.out.println("\nAlice's last remaining sent message:");
        System.out.println(alice.getChatHistory().getLastMessageSent(alice));

        System.out.println("\n--- ITERATE ALICE'S HISTORY BY BOB'S USER NAME ---");
        Iterator<Message> iterator = alice.iterator(bob);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        System.out.println("\n--- UNREGISTER A USER ---");
        server.unregisterUser(charlie);
        alice.sendMessage("Charlie is offline, so this will not be delivered.", charlie);
        System.out.println("\nHW10 demonstration complete.");
    }
}
