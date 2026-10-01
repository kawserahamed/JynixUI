package io.javaui.runtime;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Queues and debounces dirty scopes and nodes for processing on the next frame.
 */
public final class InvalidationQueue {

    private final Queue<Scope> dirtyScopes = new ArrayDeque<>();
    private final Set<Scope> enqueuedScopes = new HashSet<>();

    public synchronized void markScopeDirty(Scope scope) {
        if (enqueuedScopes.add(scope)) {
            dirtyScopes.offer(scope);
        }
    }

    public synchronized void drain(java.util.function.Consumer<Scope> action) {
        Scope s;
        while ((s = dirtyScopes.poll()) != null) {
            enqueuedScopes.remove(s);
            action.accept(s);
        }
    }

    public synchronized boolean isEmpty() {
        return dirtyScopes.isEmpty();
    }
}
