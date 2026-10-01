package io.javaui.foundation;

import io.javaui.annotation.UIComponent;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.IntState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.javaui.foundation.UIFoundation.*;
import static org.assertj.core.api.Assertions.assertThat;

class CounterSampleTest {

    // Builder counter tracking how many times the Counter method executed
    private static final AtomicInteger builderRunCount = new AtomicInteger();

    @UIComponent
    public static UI Counter(Scope s) {
        builderRunCount.incrementAndGet();
        IntState count = s.intState(0);

        return Column(
                Modifier.fillMaxWidth().padding(16),
                Text(() -> "Count: " + count.get()),
                Button("Increase", () -> count.update(c -> c + 1))
        );
    }

    @Test
    void stateChangeUpdatesOnlyDependentBindingWithoutReRunningBuilder() {
        builderRunCount.set(0);

        Scope scope = new Scope("CounterScreen", () -> {});
        scope.startPass();

        // Materialize initial tree
        UINode root = Counter(scope).materialize(scope);
        assertThat(builderRunCount.get()).isEqualTo(1);

        // Find the Text node child
        UINode textNode = root.getChildren().get(0);
        UINode buttonNode = root.getChildren().get(1);

        assertThat(textNode.getTextContent()).isEqualTo("Count: 0");

        // Trigger the button click
        buttonNode.getClickListener().run();

        // Verify the Text node updated immediately
        assertThat(textNode.getTextContent()).isEqualTo("Count: 1");

        // CRITICAL CHECK: Builder function did NOT re-run!
        assertThat(builderRunCount.get()).isEqualTo(1);

        // Multiple updates
        buttonNode.getClickListener().run();
        buttonNode.getClickListener().run();

        assertThat(textNode.getTextContent()).isEqualTo("Count: 3");
        assertThat(builderRunCount.get()).isEqualTo(1); // Still exactly 1!
    }
}
