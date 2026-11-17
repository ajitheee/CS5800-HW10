# CS5800 HW10 Behavioral Design Patterns

Java chat application implementing the **Mediator**, **Memento**, and **Iterator** patterns from `HW10_BehavioralPatterns.docx`.

## Requirements and implementation

| Assignment requirement | Implementation |
| --- | --- |
| Message sender, recipient(s), timestamp, and content | Immutable `Message` |
| Send and receive through a central mediator | `User.sendMessage` delegates to `ChatServer`; only the server delivers to recipients |
| Register and unregister users | `ChatServer.registerUser` and `unregisterUser` |
| Send to one or more users | `sendMessage(String content, User... recipients)` |
| Block messages from a specific user | `User.blockUser` delegates to `ChatServer.blockUser` |
| Undo the last sent message | `MessageMemento` saved at send time; server recalls that message from all affected histories |
| Store messages and get the last message sent | `ChatHistory.addMessage` and `getLastMessageSent(User sender)` |
| View a user's chat history | `User.viewChatHistory` |
| Iterate messages sent or received by a named user | `searchMessagesByUser implements Iterator<Message>` |
| Custom iterable interface and user wrapper | Both `ChatHistory` and `User` implement `IterableByUser` |
| Demonstrate the features with three users | `ChatAppDriver`: Alice, Bob, and Charlie |

The iterator class retains the assignment's exact spelling, `searchMessagesByUser`. The supplied interface is parameterized as `Iterator<Message>` for type safety.

## Design patterns

- **Mediator:** `ChatServer` controls registration, message delivery, per-recipient blocking, and coordinated message recall. A `User` never invokes another user's communication methods.
- **Memento:** `Message` is the originator, `MessageMemento` is an immutable snapshot containing the content and timestamp, and `User` is the caretaker. A snapshot is saved when a message is successfully sent. Undo uses that saved snapshot to identify and remove the exact message from the sender's and recipients' histories. This restores the absence of that send while preserving later incoming messages. Separate sends with identical content remain distinct. Repeated undo recalls earlier remaining sends.
- **Iterator:** `IterableByUser.iterator(User)` creates `searchMessagesByUser`. The iterator returns messages whose sender or any delivered recipient has the requested user name, in history order. `User.iterator` wraps its own `ChatHistory.iterator`.

Blocking is directional: if Bob blocks Charlie, Bob does not receive Charlie's messages; other unblocked recipients still do. Message recipient lists contain users who actually received the message. A send delivered to nobody creates no history entry or undo snapshot. Unregistered users cannot send or receive. Undo also removes a recalled message from recipients who have since unregistered.

Each iterator takes a copy of the history at creation. The assignment permits assuming the collection does not change between `hasNext()` and `next()`; this copy also keeps an existing iterator stable if undo occurs. A newly created iterator reflects the current history.

## Open and run in IntelliJ IDEA

Requires **JDK 17 or newer**. The application itself has no external dependencies; JUnit is used only for tests.

1. Open this folder or its `pom.xml` in IntelliJ IDEA as a Maven project.
2. Select a JDK 17 or newer as the Project SDK and allow Maven to load the project.
3. Run `hw10.ChatAppDriver.main`, or select the shared **ChatAppDriver** run configuration.

The driver demonstrates single and group delivery, blocked delivery, partial group delivery, group-message undo after a newer incoming message, all three histories, filtering Alice's history by Bob's name, and unregistering Charlie. Timestamps depend on the run time. The recalled message appears during the original delivery and undo log, but is absent from the final histories.

## Run from a terminal

With Maven installed, run these commands from the project directory:

```text
mvn clean test
java -cp target/classes hw10.ChatAppDriver
```

IntelliJ also provides Maven through its Maven tool window.

To compile and run the application using only the JDK:

```text
javac --release 17 -d out src/main/java/hw10/*.java
java -cp out hw10.ChatAppDriver
```

On PowerShell, expand the source file list explicitly if wildcard arguments are not expanded:

```powershell
javac --release 17 -d out (Get-ChildItem src/main/java/hw10/*.java).FullName
java -cp out hw10.ChatAppDriver
```

## Tests and project layout

`mvn clean test` runs 14 behavioral tests covering delivery, blocking, mementos, repeated undo, filtering and iterator exhaustion, registration, offline recipients, and immutable views.

```text
CS5800-HW10/
|-- .gitignore
|-- .run/ChatAppDriver.run.xml
|-- pom.xml
|-- README.md
`-- src/
    |-- main/java/hw10/
    |   |-- ChatAppDriver.java
    |   |-- ChatHistory.java
    |   |-- ChatServer.java
    |   |-- IterableByUser.java
    |   |-- Message.java
    |   |-- MessageMemento.java
    |   |-- User.java
    |   `-- searchMessagesByUser.java
    `-- test/java/hw10/ChatAppTest.java
```

Build output and machine-specific IDE settings are excluded by `.gitignore`. The small shared run configuration is included so the driver is easy to run in IntelliJ.
