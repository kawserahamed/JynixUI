package io.javaui.state;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Computes a derived value from other states and caches it.
 * Recomputes only when one of its observed dependencies changes.
 */
public final class DerivedState<T> {

    private final Supplier<T> computation;
    private final CopyOnWriteArrayList<StateObserver> observers = new CopyOnWriteArrayList<>();
    private final StateObserver dependencyObserver = this::onDependencyChanged;

    private T cachedValue;
    private boolean isDirty = true;
    private Set<Object> currentDependencies;

    public DerivedState(Supplier<T> computation) {
        this.computation = computation;
    }

    public T get() {
        StateTrackingContext.recordRead(this);
        if (isDirty) {
            recalculate();
        }
        return cachedValue;
    }

    private synchronized void recalculate() {
        // Clean up previous dependency listeners
        if (currentDependencies != null) {
            for (Object dep : currentDependencies) {
                removeDepObserver(dep);
            }
        }

        StateTrackingContext.CapturedRead<T> result = StateTrackingContext.captureReads(computation);
        this.cachedValue = result.value();
        this.currentDependencies = result.observedStates();
        this.isDirty = false;

        // Register on new dependencies
        for (Object dep : currentDependencies) {
            addDepObserver(dep);
        }
    }

    private void onDependencyChanged(Object source) {
        synchronized (this) {
            isDirty = true;
        }
        notifyObservers();
    }

    private void addDepObserver(Object dep) {
        if (dep instanceof IntState s) s.addObserver(dependencyObserver);
        else if (dep instanceof LongState s) s.addObserver(dependencyObserver);
        else if (dep instanceof FloatState s) s.addObserver(dependencyObserver);
        else if (dep instanceof BooleanState s) s.addObserver(dependencyObserver);
        else if (dep instanceof State<?> s) s.addObserver(dependencyObserver);
        else if (dep instanceof DerivedState<?> s) s.addObserver(dependencyObserver);
    }

    private void removeDepObserver(Object dep) {
        if (dep instanceof IntState s) s.removeObserver(dependencyObserver);
        else if (dep instanceof LongState s) s.removeObserver(dependencyObserver);
        else if (dep instanceof FloatState s) s.removeObserver(dependencyObserver);
        else if (dep instanceof BooleanState s) s.removeObserver(dependencyObserver);
        else if (dep instanceof State<?> s) s.removeObserver(dependencyObserver);
        else if (dep instanceof DerivedState<?> s) s.removeObserver(dependencyObserver);
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
