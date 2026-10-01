package io.javaui.runtime;

import io.javaui.state.BooleanState;
import io.javaui.state.DerivedState;
import io.javaui.state.FloatState;
import io.javaui.state.IntState;
import io.javaui.state.LongState;
import io.javaui.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Owns remembered state, lifecycle effects, and invalidation for a UIComponent.
 * Passed explicitly to component functions.
 */
public final class Scope {

    private final String name;
    private final Runnable invalidator;
    private final List<Object> stateSlots = new ArrayList<>(4);
    private final List<EffectRecord> effects = new ArrayList<>(2);
    private int currentSlotIndex = 0;

    public Scope(String name, Runnable invalidator) {
        this.name = name;
        this.invalidator = invalidator;
    }

    public String getName() {
        return name;
    }

    public void startPass() {
        this.currentSlotIndex = 0;
    }

    public IntState intState(int initial) {
        return remember(() -> new IntState(initial));
    }

    public LongState longState(long initial) {
        return remember(() -> new LongState(initial));
    }

    public FloatState floatState(float initial) {
        return remember(() -> new FloatState(initial));
    }

    public BooleanState booleanState(boolean initial) {
        return remember(() -> new BooleanState(initial));
    }

    public <T> State<T> state(T initial) {
        return remember(() -> new State<>(initial));
    }

    public <T> DerivedState<T> derived(Supplier<T> computation) {
        return remember(() -> new DerivedState<>(computation));
    }

    @SuppressWarnings("unchecked")
    public <T> T remember(Supplier<T> creator) {
        if (currentSlotIndex < stateSlots.size()) {
            T existing = (T) stateSlots.get(currentSlotIndex);
            currentSlotIndex++;
            return existing;
        } else {
            T created = creator.get();
            stateSlots.add(created);
            currentSlotIndex++;
            return created;
        }
    }

    public void effect(Runnable effectRunnable, Object... keys) {
        int index = effects.size();
        if (index < effects.size()) {
            EffectRecord rec = effects.get(index);
            if (!Arrays.equals(rec.keys, keys)) {
                rec.cleanup();
                rec.keys = keys;
                rec.action = effectRunnable;
                effectRunnable.run();
            }
        } else {
            EffectRecord rec = new EffectRecord(keys, effectRunnable);
            effects.add(rec);
            effectRunnable.run();
        }
    }

    public void invalidate() {
        if (invalidator != null) {
            invalidator.run();
        }
    }

    public void dispose() {
        for (EffectRecord rec : effects) {
            rec.cleanup();
        }
        effects.clear();
        stateSlots.clear();
    }

    private static class EffectRecord {
        Object[] keys;
        Runnable action;
        Runnable cleanupAction;

        EffectRecord(Object[] keys, Runnable action) {
            this.keys = keys;
            this.action = action;
        }

        void cleanup() {
            if (cleanupAction != null) {
                cleanupAction.run();
                cleanupAction = null;
            }
        }
    }
}
