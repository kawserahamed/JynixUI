package io.javaui.runtime;

import io.javaui.state.StateTrackingContext;
import io.javaui.state.IntState;
import io.javaui.state.LongState;
import io.javaui.state.FloatState;
import io.javaui.state.BooleanState;
import io.javaui.state.State;
import io.javaui.state.DerivedState;
import io.javaui.state.StateObserver;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Connects a reactive expression to a node property.
 * When an observed state changes, only this property binding re-executes.
 */
public final class Binding<T> {

    private final Supplier<T> supplier;
    private final Consumer<T> consumer;
    private final Runnable onInvalidated;
    private final StateObserver observer = this::onObservedStateChanged;

    private T lastValue;
    private Set<Object> observedStates;
    private boolean isStatic = false;

    public Binding(Supplier<T> supplier, Consumer<T> consumer, Runnable onInvalidated) {
        this.supplier = supplier;
        this.consumer = consumer;
        this.onInvalidated = onInvalidated;
    }

    public static <T> Binding<T> ofStatic(T value, Consumer<T> consumer) {
        Binding<T> b = new Binding<>(() -> value, consumer, null);
        b.isStatic = true;
        consumer.accept(value);
        b.lastValue = value;
        return b;
    }

    public void evaluate() {
        if (isStatic) return;

        // Capture all state reads within the supplier
        StateTrackingContext.CapturedRead<T> result = StateTrackingContext.captureReads(supplier);
        T newValue = result.value();

        // Update dependencies
        updateObservedStates(result.observedStates());

        // Apply if changed
        if (!Objects.equals(lastValue, newValue)) {
            this.lastValue = newValue;
            consumer.accept(newValue);
            if (onInvalidated != null) {
                onInvalidated.run();
            }
        }
    }

    private void onObservedStateChanged(Object stateSource) {
        evaluate();
    }

    private void updateObservedStates(Set<Object> newStates) {
        if (this.observedStates != null) {
            for (Object old : this.observedStates) {
                if (!newStates.contains(old)) {
                    removeObserverFrom(old);
                }
            }
        }
        for (Object s : newStates) {
            if (this.observedStates == null || !this.observedStates.contains(s)) {
                addObserverTo(s);
            }
        }
        this.observedStates = newStates;
    }

    private void addObserverTo(Object s) {
        if (s instanceof IntState is) is.addObserver(observer);
        else if (s instanceof LongState ls) ls.addObserver(observer);
        else if (s instanceof FloatState fs) fs.addObserver(observer);
        else if (s instanceof BooleanState bs) bs.addObserver(observer);
        else if (s instanceof State<?> gs) gs.addObserver(observer);
        else if (s instanceof DerivedState<?> ds) ds.addObserver(observer);
    }

    private void removeObserverFrom(Object s) {
        if (s instanceof IntState is) is.removeObserver(observer);
        else if (s instanceof LongState ls) ls.removeObserver(observer);
        else if (s instanceof FloatState fs) fs.removeObserver(observer);
        else if (s instanceof BooleanState bs) bs.removeObserver(observer);
        else if (s instanceof State<?> gs) gs.removeObserver(observer);
        else if (s instanceof DerivedState<?> ds) ds.removeObserver(observer);
    }

    public void dispose() {
        if (observedStates != null) {
            for (Object s : observedStates) {
                removeObserverFrom(s);
            }
            observedStates = null;
        }
    }

    public T getLastValue() {
        return lastValue;
    }
}
