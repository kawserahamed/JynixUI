package io.javaui.state;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * Generic reactive state holder for immutable objects.
 */
public final class State<T> {

    private volatile T value;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();

    public State(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        StateTrackingContext.recordRead(this);
        return value;
    }

    public T peek() {
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) {
            return;
        }
        this.value = newValue;
        notifyObservers();
    }

    public void update(UnaryOperator<T> operator) {
        set(operator.apply(this.value));
    }

    public void addObserver(StateObserver observer) {
        observers.addIfAbsent(observer);
    }

    public void removeObserver(StateObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (StateObserver observer : observers) {
            observer.onStateChanged(this);
        }
    }
}
