package io.javaui.foundation;

import io.javaui.layout.Constraints;
import io.javaui.layout.Density;
import io.javaui.layout.LayoutContext;
import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import org.junit.jupiter.api.Test;

import static io.javaui.foundation.UIFoundation.*;
import static org.assertj.core.api.Assertions.assertThat;

class LazyColumnTest {

    @Test
    void tenThousandItemsNeverCreateTenThousandActiveNodes() {
        Scope scope = new Scope("LazyListTest", () -> {});
        scope.startPass();

        final int TOTAL_ITEMS = 10_000;

        UI lazyColumnUI = LazyColumn.LazyColumn(
                Modifier.fillMaxWidth().size(360, 640),
                TOTAL_ITEMS,
                index -> "item_" + index,
                (s, index) -> Row(
                        Modifier.fillMaxWidth().size(360, 60).padding(8),
                        Text("Item #" + index)
                )
        );

        UINode root = lazyColumnUI.materialize(scope);

        LayoutContext context = new LayoutContext(Density.of(2.0f, 1.0f));
        Constraints constraints = Constraints.fixed(720, 1280);

        // First layout pass
        root.getLayoutNode().measure(constraints, context);

        // Verify active children count in root container
        int activeNodes = root.getChildren().size();

        // Viewport is 1280px high, estimated item height is 120px => ~12 items visible, plus buffer <= 25 nodes
        assertThat(activeNodes).isLessThan(25);
        assertThat(activeNodes).isGreaterThan(5);

        // Active node count is orders of magnitude smaller than 10,000
        assertThat(activeNodes).isNotEqualTo(TOTAL_ITEMS);
    }
}
