package io.javaui.navigation;

import io.javaui.layout.BoxPolicy;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Binding;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.State;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Backstack navigation controller for declarative screen routing.
 */
public final class NavController {

    private final Deque<String> backstack = new ArrayDeque<>();
    private final State<String> currentRouteState;
    private final Map<String, Object> arguments = new HashMap<>();

    public NavController(String initialRoute) {
        this.currentRouteState = new State<>(initialRoute);
        this.backstack.push(initialRoute);
    }

    public void navigate(String route) {
        backstack.push(route);
        currentRouteState.set(route);
    }

    public boolean popBack() {
        if (backstack.size() > 1) {
            backstack.pop();
            String prev = backstack.peek();
            currentRouteState.set(prev);
            return true;
        }
        return false;
    }

    public String getCurrentRoute() {
        return currentRouteState.get();
    }

    public State<String> getCurrentRouteState() {
        return currentRouteState;
    }

    public static UI NavHost(NavController controller, Modifier modifier, Function<String, UI> routeGraph) {
        return scope -> {
            UINode container = new UINode("NavHost", BoxPolicy.DEFAULT);
            container.setModifier(modifier);

            Binding<String> routeBinding = new Binding<>(
                    controller::getCurrentRoute,
                    route -> {
                        container.clearChildren();
                        UI screenUI = routeGraph.apply(route);
                        if (screenUI != null) {
                            container.addChild(screenUI.materialize(scope));
                        }
                    },
                    container::markDrawDirty
            );
            container.addBinding(routeBinding);
            routeBinding.evaluate();

            return container;
        };
    }
}
