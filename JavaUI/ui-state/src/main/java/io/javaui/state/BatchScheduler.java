package io.javaui.state;

/**
 * Interface responsible for scheduling UI batch drains on the UI frame loop (e.g. Choreographer on Android).
 */
public interface BatchScheduler {
    void scheduleFrame(Runnable task);
    boolean isMainThread();
}
