package io.javaui.state;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongUnaryOperator;

public final class LongState {

    private volatile long value;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();

    public LongState(long initialValue) {
        this.value = initialValue;
    }

    public long get() {
        StateTrackingContext.recordRead(this);
        return value;
    }

    public long peek() {
        return value;
    }

    public void set(long newValue) {
        if (this.value == newValue) {
            return;
        }
        this.value = newValue;
        notifyObservers();
    }

    public void update(LongUnaryOperator operator) {
        set(operator.applyAsLong(this.value));
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
