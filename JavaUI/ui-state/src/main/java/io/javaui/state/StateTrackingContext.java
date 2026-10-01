package io.javaui.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thread-local context for recording fine-grained state reads during lambda evaluation.
 * Enables zero-boilerplate reactive dependency discovery.
 */
public final class StateTrackingContext {

    private static final ThreadLocal<Deque<Consumer<Object>>> READ_LISTENERS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private StateTrackingContext() {}

    /**
     * Records a read of the given state object if a listener is currently active.
     */
    public static void recordRead(Object state) {
        Deque<Consumer<Object>> stack = READ_LISTENERS.get();
        if (!stack.isEmpty()) {
            Consumer<Object> current = stack.peek();
            if (current != null) {
                current.accept(state);
            }
        }
    }

    /**
     * Executes a supplier while capturing all states read within it.
     */
    public static <T> CapturedRead<T> captureReads(Supplier<T> computation) {
        Set<Object> captured = new HashSet<>(4);
        Consumer<Object> listener = captured::add;
        Deque<Consumer<Object>> stack = READ_LISTENERS.get();
        stack.push(listener);
        try {
            T value = computation.get();
            return new CapturedRead<>(value, captured);
        } finally {
            stack.pop();
        }
    }

    public record CapturedRead<T>(T value, Set<Object> observedStates) {}
}
