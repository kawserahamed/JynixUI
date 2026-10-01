package io.javaui.runtime;

import java.util.function.Consumer;

/**
 * Lightweight description of a UI element or subtree.
 * Created by declarative builder functions and materialized into active UINodes by the runtime.
 */
@FunctionalInterface
public interface UI {
    UINode materialize(Scope scope);

    default UI with(Consumer<UINode> configurator) {
        return scope -> {
            UINode node = this.materialize(scope);
            configurator.accept(node);
            return node;
        };
    }
}
