package io.javaui.interop;

import android.content.Context;
import android.view.View;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interop component that hosts an arbitrary platform Android View (e.g. MapView, WebView)
 * inside the declarative JavaUI layout tree.
 */
public final class AndroidView {

    private AndroidView() {}

    public static <T extends View> UI AndroidView(
            Modifier modifier,
            Function<Context, T> viewFactory,
            Consumer<T> update
    ) {
        return scope -> {
            UINode node = new UINode("AndroidViewInterop", null);
            node.setModifier(modifier);

            // Life cycle and update binding
            scope.effect(() -> {
                // Invoked on mount and subsequent state invalidation
            });

            return node;
        };
    }
}
