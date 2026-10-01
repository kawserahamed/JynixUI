package io.javaui.state;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntUnaryOperator;

/**
 * High-performance unboxed primitive integer reactive state.
 * Eliminates java.lang.Integer boxing allocations on every frame.
 */
public final class IntState {

    private volatile int value;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();

    public IntState(int initialValue) {
        this.value = initialValue;
    }

    public int get() {
        StateTrackingContext.recordRead(this);
        return value;
    }

    public int peek() {
        return value;
    }

    public void set(int newValue) {
        if (this.value == newValue) {
            return;
        }
        this.value = newValue;
        notifyObservers();
    }

    public void update(IntUnaryOperator operator) {
        set(operator.applyAsInt(this.value));
    }

    public void addObserver(StateObserver observer) {
        observers.addIfAbsent(observer);
    }

    public void removeObserver(StateObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        if (observers.isEmpty()) return;
        for (StateObserver observer : observers) {
            observer.onStateChanged(this);
        }
    }

    @Override
    public String toString() {
        return "IntState{" + value + "}";
    }
}
