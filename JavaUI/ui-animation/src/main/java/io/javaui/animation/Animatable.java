package io.javaui.animation;

import io.javaui.state.FloatState;

/**
 * High-performance, zero-allocation animation driver for primitive float values.
 */
public final class Animatable {

    private final FloatState valueState;
    private float startValue;
    private float targetValue;
    private long startTimeNanos = -1;
    private long durationNanos = 300_000_000L; // default 300ms
    private boolean isRunning = false;

    public Animatable(float initialValue) {
        this.valueState = new FloatState(initialValue);
        this.startValue = initialValue;
        this.targetValue = initialValue;
    }

    public float get() {
        return valueState.get();
    }

    public float peek() {
        return valueState.peek();
    }

    public void animateTo(float target, int durationMillis) {
        this.startValue = valueState.peek();
        this.targetValue = target;
        this.durationNanos = durationMillis * 1_000_000L;
        this.startTimeNanos = -1;
        this.isRunning = true;
    }

    /**
     * Ticks the animation frame.
     * Guaranteed ZERO OBJECT ALLOCATION in steady-state loop.
     *
     * @param frameTimeNanos current timestamp from Choreographer
     * @return true if still running, false if completed
     */
    public boolean doFrame(long frameTimeNanos) {
        if (!isRunning) return false;

        if (startTimeNanos < 0) {
            startTimeNanos = frameTimeNanos;
        }

        long elapsed = frameTimeNanos - startTimeNanos;
        if (elapsed >= durationNanos) {
            valueState.set(targetValue);
            isRunning = false;
            return false;
        }

        float fraction = (float) elapsed / (float) durationNanos;
        // Cubic ease-out calculation without allocating objects: 1 - (1 - t)^3
        float inv = 1.0f - fraction;
        float eased = 1.0f - (inv * inv * inv);

        float current = startValue + (targetValue - startValue) * eased;
        valueState.set(current);
        return true;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
