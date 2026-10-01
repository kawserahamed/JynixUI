package io.javaui.state;

import java.util.concurrent.CopyOnWriteArrayList;

public final class BooleanState {

    private volatile boolean value;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();

    public BooleanState(boolean initialValue) {
        this.value = initialValue;
    }

    public boolean get() {
        StateTrackingContext.recordRead(this);
        return value;
    }

    public boolean peek() {
        return value;
    }

    public void set(boolean newValue) {
        if (this.value == newValue) {
            return;
        }
        this.value = newValue;
        notifyObservers();
    }

    public void toggle() {
        set(!this.value);
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
