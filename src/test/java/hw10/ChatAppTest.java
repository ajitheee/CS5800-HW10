package hw10;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ChatAppTest {
    private ChatServer server;
    private User alice;
    private User bob;
    private User charlie;

    @BeforeEach
    void setUp() {
        server = new ChatServer();
        alice = new User("Alice", server);
        bob = new User("Bob", server);
        charlie = new User("Charlie", server);
        server.registerUser(alice);
        server.registerUser(bob);
        server.registerUser(charlie);
    }

    @Test
    void deliversToOneOrMultipleRecipientsExactlyOnce() {
        assertTrue(alice.sendMessage("Hello", bob, charlie, bob));
        Message message = alice.getChatHistory().getLastMessageSent(alice);
        assertSame(alice, message.getSender());
        assertEquals("Hello", message.getContent());
        assertNotNull(message.getTimestamp());
        assertEquals(List.of(bob, charlie), message.getRecipients());
        assertEquals(List.of(message), bob.getChatHistory().getMessages());
        assertEquals(List.of(message), charlie.getChatHistory().getMessages());
    }

    @Test
    void blockingIsDirectionalAndDoesNotPreventOtherGroupDeliveries() {
        bob.blockUser(charlie);
        assertTrue(charlie.sendMessage("Group", alice, bob));
        assertTrue(bob.getChatHistory().getMessages().isEmpty());
        assertEquals(1, alice.getChatHistory().getMessages().size());
        assertEquals(List.of(alice), charlie.getChatHistory()
                .getLastMessageSent(charlie).getRecipients());
        assertTrue(bob.sendMessage("Other direction still works", charlie));
        assertEquals(2, charlie.getChatHistory().getMessages().size());
    }

    @Test
    void anEntirelyBlockedSendDoesNotReplaceTheLastUndoableMessage() {
        charlie.sendMessage("Delivered", alice);
        bob.blockUser(charlie);
        assertFalse(charlie.sendMessage("Blocked", bob));
        assertTrue(charlie.undoLastMessage());
        assertTrue(alice.getChatHistory().getMessages().isEmpty());
        assertTrue(bob.getChatHistory().getMessages().isEmpty());
        assertTrue(charlie.getChatHistory().getMessages().isEmpty());
    }

    @Test
    void undoRecallsTheLastSentMessageFromAllHistoriesAfterAnIncomingMessage() {
        alice.sendMessage("Keep this", bob);
        Message retained = alice.getChatHistory().getLastMessageSent(alice);
        alice.sendMessage("Recall this", bob, charlie);
        bob.sendMessage("Newer incoming message", alice);
        Message incoming = bob.getChatHistory().getLastMessageSent(bob);

        assertTrue(alice.undoLastMessage());
        assertEquals(List.of(retained, incoming), alice.getChatHistory().getMessages());
        assertEquals(List.of(retained, incoming), bob.getChatHistory().getMessages());
        assertTrue(charlie.getChatHistory().getMessages().isEmpty());
        assertSame(retained, alice.getChatHistory().getLastMessageSent(alice));
    }

    @Test
    void repeatedUndoDistinguishesIdenticalMessagesAndHandlesAnEmptyHistory() {
        assertFalse(alice.undoLastMessage());
        alice.sendMessage("Same text", bob);
        Message first = alice.getChatHistory().getLastMessageSent(alice);
        alice.sendMessage("Same text", bob);
        assertTrue(alice.undoLastMessage());
        assertEquals(List.of(first), alice.getChatHistory().getMessages());
        assertEquals(List.of(first), bob.getChatHistory().getMessages());
        assertTrue(alice.undoLastMessage());
        assertFalse(alice.undoLastMessage());
        assertNull(alice.getChatHistory().getLastMessageSent(alice));
    }

    @Test
    void snapshotPreservesContentAndTimestamp() {
        alice.sendMessage("Snapshot", bob);
        Message message = alice.getChatHistory().getLastMessageSent(alice);
        MessageMemento snapshot = alice.getLastSentSnapshot();
        assertEquals(message.getContent(), snapshot.getContent());
        assertEquals(message.getTimestamp(), snapshot.getTimestamp());
        alice.undoLastMessage();
        assertEquals("Snapshot", snapshot.getContent());
        assertEquals(message.getTimestamp(), snapshot.getTimestamp());
    }

    @Test
    void iteratorIncludesSentAndReceivedMessagesAndSkipsUnrelatedMessages() {
        alice.sendMessage("To Bob", bob);
        bob.sendMessage("From Bob", alice);
        charlie.sendMessage("Not related to Bob", alice);
        charlie.sendMessage("Bob is a group recipient", alice, bob);
        List<String> contents = new ArrayList<>();
        Iterator<Message> iterator = alice.iterator(bob);
        while (iterator.hasNext()) {
            assertTrue(iterator.hasNext());
            contents.add(iterator.next().getContent());
        }
        assertEquals(List.of("To Bob", "From Bob", "Bob is a group recipient"), contents);
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorSearchesByUserNameAndNextWorksWithoutHasNext() {
        alice.sendMessage("Unrelated", charlie);
        bob.sendMessage("Found by name", alice);
        User searchKey = new User("Bob", server);
        Iterator<Message> iterator = alice.iterator(searchKey);
        assertEquals("Found by name", iterator.next().getContent());
        assertFalse(iterator.hasNext());
    }

    @Test
    void emptyAndNoMatchIteratorsFollowTheIteratorContract() {
        Iterator<Message> empty = alice.iterator(bob);
        assertFalse(empty.hasNext());
        assertThrows(NoSuchElementException.class, empty::next);
        alice.sendMessage("Only Charlie", charlie);
        Iterator<Message> noMatch = alice.iterator(bob);
        assertFalse(noMatch.hasNext());
        assertThrows(NoSuchElementException.class, noMatch::next);
    }

    @Test
    void iteratorUsesASnapshotAndNewIteratorsReflectUndo() {
        alice.sendMessage("Recalled later", bob);
        Iterator<Message> existing = alice.iterator(bob);
        assertTrue(existing.hasNext());
        alice.undoLastMessage();
        assertEquals("Recalled later", existing.next().getContent());
        assertFalse(alice.iterator(bob).hasNext());
    }

    @Test
    void unregisterPreventsSendingAndReceivingButUndoStillCleansOfflineHistory() {
        alice.sendMessage("Recall even while Bob is offline", bob);
        server.unregisterUser(bob);
        assertFalse(charlie.sendMessage("Not delivered", bob));
        assertThrows(IllegalStateException.class, () -> bob.sendMessage("No", alice));
        assertTrue(alice.undoLastMessage());
        assertTrue(bob.getChatHistory().getMessages().isEmpty());
        server.registerUser(bob);
        assertTrue(charlie.sendMessage("Delivered after registration", bob));
        assertEquals(1, bob.getChatHistory().getMessages().size());
    }

    @Test
    void registrationRejectsDuplicateNamesAndTheWrongMediator() {
        assertThrows(IllegalArgumentException.class,
                () -> server.registerUser(new User("Alice", server)));
        assertThrows(IllegalArgumentException.class,
                () -> new ChatServer().registerUser(alice));
        server.unregisterUser(new User("Alice", server));
        assertTrue(alice.sendMessage("Original Alice is still registered", bob));
    }

    @Test
    void invalidRecipientsDoNotCausePartialDelivery() {
        assertThrows(IllegalArgumentException.class, () -> alice.sendMessage("No recipients"));
        assertThrows(IllegalArgumentException.class, () -> alice.sendMessage("Self", bob, alice));
        assertThrows(NullPointerException.class,
                () -> server.sendMessage(alice, java.util.Arrays.asList(bob, null), "Null"));
        assertTrue(bob.getChatHistory().getMessages().isEmpty());
        assertTrue(alice.getChatHistory().getMessages().isEmpty());
    }

    @Test
    void messageRecipientsAndHistoryCannotBeModifiedThroughGetters() {
        alice.sendMessage("Immutable", bob);
        Message message = alice.getChatHistory().getLastMessageSent(alice);
        assertThrows(UnsupportedOperationException.class,
                () -> message.getRecipients().add(charlie));
        assertThrows(UnsupportedOperationException.class,
                () -> alice.getChatHistory().getMessages().clear());
    }
}
