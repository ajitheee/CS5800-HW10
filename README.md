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

## Output

This is the output from running `ChatAppDriver`. A copy is saved in [output/ChatAppDriver-output.txt](output/ChatAppDriver-output.txt). Timestamps will change with each run.

```text
Alice registered.
Bob registered.
Charlie registered.

--- SEND TO ONE OR MORE USERS ---
Bob received from Alice: Hello Bob!
Alice received from Bob: Hi Alice!
Alice received from Charlie: Hello everyone!
Bob received from Charlie: Hello everyone!

--- BLOCK MESSAGES THROUGH THE MEDIATOR ---
Bob blocked Charlie.
Blocked: Charlie -> Bob
Blocked: Charlie -> Bob
Alice received from Charlie: Only Alice receives this group message.

--- UNDO THE LAST SENT MESSAGE USING A MEMENTO ---
Bob received from Alice: This group message will be undone.
Charlie received from Alice: This group message will be undone.
Alice received from Bob: Alice, did you send something?
Alice undid: "This group message will be undone." (sent at 2026-11-16T22:37:17.222673900)
The recalled message is absent from all three histories.

--- VIEW EACH USER'S CHAT HISTORY ---

Chat history for Alice:
[22:37:17] Alice -> Bob: Hello Bob!
[22:37:17] Bob -> Alice: Hi Alice!
[22:37:17] Charlie -> Alice, Bob: Hello everyone!
[22:37:17] Charlie -> Alice: Only Alice receives this group message.
[22:37:17] Bob -> Alice: Alice, did you send something?

Chat history for Bob:
[22:37:17] Alice -> Bob: Hello Bob!
[22:37:17] Bob -> Alice: Hi Alice!
[22:37:17] Charlie -> Alice, Bob: Hello everyone!
[22:37:17] Bob -> Alice: Alice, did you send something?

Chat history for Charlie:
[22:37:17] Charlie -> Alice, Bob: Hello everyone!
[22:37:17] Charlie -> Alice: Only Alice receives this group message.

Alice's last remaining sent message:
[22:37:17] Alice -> Bob: Hello Bob!

--- ITERATE ALICE'S HISTORY BY BOB'S USER NAME ---
[22:37:17] Alice -> Bob: Hello Bob!
[22:37:17] Bob -> Alice: Hi Alice!
[22:37:17] Charlie -> Alice, Bob: Hello everyone!
[22:37:17] Bob -> Alice: Alice, did you send something?

--- UNREGISTER A USER ---
Charlie unregistered.
Not delivered to Charlie: not registered.

HW10 demonstration complete.

Exit code: 0
```

## Tests

```text
mvn test
```

There are 14 tests covering message delivery, blocking, undo, history filtering, and registration.
