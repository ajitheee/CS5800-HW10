package hw10;

import java.util.Iterator;

/** The assignment's custom iterable interface, with a typed return value. */
public interface IterableByUser {
    Iterator<Message> iterator(User userToSearchWith);
}
