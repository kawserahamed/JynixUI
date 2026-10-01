package io.javaui.material;

import io.javaui.layout.Modifier;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.BooleanState;
import org.junit.jupiter.api.Test;

import static io.javaui.foundation.UIFoundation.*;
import static io.javaui.material.UIMaterial.*;
import static org.assertj.core.api.Assertions.assertThat;

class MaterialComponentsTest {

    @Test
    void switchTogglesAndUpdatesNodeColor() {
        Scope scope = new Scope("SwitchTest", () -> {});
        scope.startPass();

        BooleanState checked = new BooleanState(false);
        UI switchUI = Switch(Modifier.DEFAULT, checked);
        UINode node = switchUI.materialize(scope);

        // Initially unchecked (gray)
        assertThat(node.getCustomColor()).isEqualTo(0xFFBDBDBD);
        assertThat(node.getContentDescription()).isEqualTo("Off");

        // Trigger click
        node.getClickListener().run();

        // Now checked (blue)
        assertThat(checked.get()).isTrue();
        assertThat(node.getCustomColor()).isEqualTo(0xFF1976D2);
        assertThat(node.getContentDescription()).isEqualTo("On");
    }

    @Test
    void cardMaterializesChildrenProperly() {
        Scope scope = new Scope("CardTest", () -> {});
        scope.startPass();

        UI cardUI = Card(Modifier.fillMaxWidth().padding(8), Text("Card Title"), Text("Card Body"));
        UINode cardNode = cardUI.materialize(scope);

        assertThat(cardNode.getType()).isEqualTo("Card");
        assertThat(cardNode.getChildren()).hasSize(2);
        assertThat(cardNode.getChildren().get(0).getTextContent()).isEqualTo("Card Title");
    }
}
