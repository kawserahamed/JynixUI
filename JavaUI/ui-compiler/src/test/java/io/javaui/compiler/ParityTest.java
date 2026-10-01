package io.javaui.compiler;

import io.javaui.annotation.UIComponent;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.IntState;
import org.junit.jupiter.api.Test;

import static io.javaui.foundation.UIFoundation.*;
import static org.assertj.core.api.Assertions.assertThat;

class ParityTest {

    // Uninstrumented component declaration
    @UIComponent
    public static UI PlainCounter(Scope s) {
        IntState count = s.intState(0);
        return Column(
                Modifier.fillMaxWidth().padding(16),
                Text(() -> "Count: " + count.get()),
                Button("Increase", () -> count.update(c -> c + 1))
        );
    }

    // Explicitly keyed simulated instrumented path
    public static UI InstrumentedCounter(Scope s, int compileTimeKey) {
        IntState count = s.remember(() -> new IntState(0));
        return Column(
                Modifier.fillMaxWidth().padding(16),
                Text(() -> "Count: " + count.get()),
                Button("Increase", () -> count.update(c -> c + 1))
        );
    }

    @Test
    void instrumentedAndUninstrumentedPathsProduceIdenticalBehavior() {
        Scope plainScope = new Scope("Plain", () -> {});
        plainScope.startPass();
        UINode plainTree = PlainCounter(plainScope).materialize(plainScope);

        Scope instrumentedScope = new Scope("Instrumented", () -> {});
        instrumentedScope.startPass();
        UINode instrumentedTree = InstrumentedCounter(instrumentedScope, 0xCAFE_BABE).materialize(instrumentedScope);

        // 1. Structural Parity
        assertThat(plainTree.getType()).isEqualTo(instrumentedTree.getType());
        assertThat(plainTree.getChildren()).hasSameSizeAs(instrumentedTree.getChildren());

        UINode plainText = plainTree.getChildren().get(0);
        UINode instText = instrumentedTree.getChildren().get(0);
        assertThat(plainText.getTextContent()).isEqualTo(instText.getTextContent()).isEqualTo("Count: 0");

        // 2. Behavioral & Invalidation Parity
        UINode plainBtn = plainTree.getChildren().get(1);
        UINode instBtn = instrumentedTree.getChildren().get(1);

        plainBtn.getClickListener().run();
        instBtn.getClickListener().run();

        assertThat(plainText.getTextContent()).isEqualTo(instText.getTextContent()).isEqualTo("Count: 1");
    }
}
