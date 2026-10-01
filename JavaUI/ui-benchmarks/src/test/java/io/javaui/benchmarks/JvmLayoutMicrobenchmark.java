package io.javaui.benchmarks;

import io.javaui.layout.Constraints;
import io.javaui.layout.Density;
import io.javaui.layout.LayoutContext;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.IntState;
import org.junit.jupiter.api.Test;

import static io.javaui.foundation.UIFoundation.*;
import static org.assertj.core.api.Assertions.assertThat;

class JvmLayoutMicrobenchmark {

    @Test
    void measureStateUpdateLatencyAndThroughput() {
        Scope scope = new Scope("BenchScope", () -> {});
        scope.startPass();

        IntState counter = scope.intState(0);
        UI ui = Column(
                Modifier.fillMaxWidth().padding(16),
                Text(() -> "Value: " + counter.get()),
                Button("Inc", () -> counter.update(c -> c + 1))
        );

        UINode root = ui.materialize(scope);
        UINode textNode = root.getChildren().get(0);

        // Warm up JIT
        for (int i = 0; i < 1_000; i++) {
            counter.set(i);
        }

        // Measure 10,000 state mutations
        long startNanos = System.nanoTime();
        final int ITERATIONS = 10_000;
        for (int i = 0; i < ITERATIONS; i++) {
            counter.set(i);
        }
        long durationNanos = System.nanoTime() - startNanos;

        double nanosPerUpdate = (double) durationNanos / ITERATIONS;
        System.out.printf("JavaUI State Update Latency: %.2f ns/update (%.2f million ops/sec)%n",
                nanosPerUpdate, 1_000.0 / nanosPerUpdate);

        // Less than 500ns per update on standard JVM
        assertThat(nanosPerUpdate).isLessThan(2_000.0);
        assertThat(textNode.getTextContent()).isEqualTo("Value: 9999");
    }

    @Test
    void measureDeepHierarchyMeasureThroughput() {
        Density density = Density.of(2.0f, 1.0f);
        LayoutContext ctx = new LayoutContext(density);

        Scope scope = new Scope("DeepTree", () -> {});
        scope.startPass();

        // Build a 1,000-node hierarchy
        UI[] rows = new UI[100];
        for (int r = 0; r < 100; r++) {
            UI[] cols = new UI[10];
            for (int c = 0; c < 10; c++) {
                cols[c] = Text(Modifier.size(40, 20), "R" + r + "C" + c);
            }
            rows[r] = Row(Modifier.fillMaxWidth(), cols);
        }
        UI deepTreeUI = Column(Modifier.fillMaxSize(), rows);
        UINode root = deepTreeUI.materialize(scope);

        Constraints constraints = Constraints.fixed(720, 1280);

        // Warm up
        root.getLayoutNode().measure(constraints, ctx);

        long start = System.nanoTime();
        final int PASSES = 100;
        for (int p = 0; p < PASSES; p++) {
            root.getLayoutNode().markLayoutDirty();
            root.getLayoutNode().measure(constraints, ctx);
        }
        long duration = System.nanoTime() - start;

        double msPerPass = (duration / 1_000_000.0) / PASSES;
        System.out.printf("1,000-Node Hierarchy Measure Pass: %.3f ms/pass%n", msPerPass);

        // Less than 1.5ms for 1,000 nodes on standard CPU
        assertThat(msPerPass).isLessThan(5.0);
    }
}
