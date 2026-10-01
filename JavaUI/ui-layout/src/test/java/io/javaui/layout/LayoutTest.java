package io.javaui.layout;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LayoutTest {

    @Test
    void columnMeasuresAndPlacesChildrenVertically() {
        Density density = Density.of(2.0f, 1.0f); // 1dp = 2px
        LayoutContext ctx = new LayoutContext(density);

        LayoutNode root = new LayoutNode(ColumnPolicy.INSTANCE);
        root.setModifier(Modifier.padding(8)); // 8dp * 2 = 16px padding on all sides

        LayoutNode child1 = new LayoutNode(null);
        child1.setModifier(Modifier.size(50, 30)); // 100px x 60px

        LayoutNode child2 = new LayoutNode(null);
        child2.setModifier(Modifier.size(80, 40)); // 160px x 80px

        root.addChild(child1);
        root.addChild(child2);

        Constraints rootConstraints = Constraints.loose(1000, 1000);
        root.measure(rootConstraints, ctx);

        // child1 measured
        assertThat(child1.getWidth()).isEqualTo(100);
        assertThat(child1.getHeight()).isEqualTo(60);

        // child2 measured
        assertThat(child2.getWidth()).isEqualTo(160);
        assertThat(child2.getHeight()).isEqualTo(80);

        // Root inner width = max(100, 160) = 160 + 32 (padding) = 192px
        // Root inner height = 60 + 80 = 140 + 32 (padding) = 172px
        assertThat(root.getWidth()).isEqualTo(192);
        assertThat(root.getHeight()).isEqualTo(172);

        // Steady state measure with same constraints is zero work
        assertThat(root.isLayoutDirty()).isFalse();
    }
}
