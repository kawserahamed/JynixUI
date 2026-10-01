package io.javaui.state;

/**
 * Functional observer notified whenever a state changes.
 */
@FunctionalInterface
public interface StateObserver {
    void onStateChanged(Object stateSource);
}
