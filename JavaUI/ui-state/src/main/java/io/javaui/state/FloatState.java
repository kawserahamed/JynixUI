package io.javaui.state;

import java.util.concurrent.CopyOnWriteArrayList;

public final class FloatState {

    private volatile float value;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();

    public FloatState(float initialValue) {
        this.value = initialValue;
    }

    public float get() {
        StateTrackingContext.recordRead(this);
        return value;
    }

    public float peek() {
        return value;
    }

    public void set(float newValue) {
        if (Float.floatToIntBits(this.value) == Float.floatToIntBits(newValue)) {
            return;
        }
        this.value = newValue;
        notifyObservers();
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
