# CS5800 HW10 - Behavioral Design Patterns

A Java chat application using the Mediator, Memento, and Iterator patterns. Users can send messages, block other users, undo their last sent message, and view their chat history.

## Design patterns

- **Mediator:** `ChatServer` handles registration, message delivery, and blocking. Users send messages through the server.
- **Memento:** `MessageMemento` saves a message's content and timestamp when it is sent. Undo removes that message from the sender's and recipients' histories.
- **Iterator:** `searchMessagesByUser` filters a user's history to messages sent or received by a specific user. Both `User` and `ChatHistory` implement `IterableByUser`.

## How to run

Use JDK 17 or newer. Open the project in IntelliJ IDEA, let Maven load, and run `ChatAppDriver`.

To run from the terminal:

```text
mvn compile
java -cp target/classes hw10.ChatAppDriver
```

The driver uses Alice, Bob, and Charlie to demonstrate single and group messages, blocking, undo, chat history, and searching by user. It also shows what happens when a user unregisters.

## Tests

```text
mvn test
```

There are 14 tests covering message delivery, blocking, undo, history filtering, and registration.
