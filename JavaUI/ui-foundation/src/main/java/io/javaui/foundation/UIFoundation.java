package io.javaui.foundation;

import io.javaui.layout.BoxPolicy;
import io.javaui.layout.ColumnPolicy;
import io.javaui.layout.Modifier;
import io.javaui.layout.RowPolicy;
import io.javaui.runtime.Binding;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;

import java.util.function.Supplier;

/**
 * Standard factory methods for core JavaUI elements.
 * Designed for static import (e.g. {@code import static io.javaui.foundation.UIFoundation.*;}).
 */
public final class UIFoundation {

    private UIFoundation() {}

    /**
     * Vertical container.
     */
    public static UI Column(Modifier modifier, UI... children) {
        return scope -> {
            UINode node = new UINode("Column", ColumnPolicy.INSTANCE);
            node.setModifier(modifier);
            for (UI childUI : children) {
                if (childUI != null) {
                    node.addChild(childUI.materialize(scope));
                }
            }
            return node;
        };
    }

    public static UI Column(UI... children) {
        return Column(Modifier.DEFAULT, children);
    }

    /**
     * Horizontal container.
     */
    public static UI Row(Modifier modifier, UI... children) {
        return scope -> {
            UINode node = new UINode("Row", RowPolicy.INSTANCE);
            node.setModifier(modifier);
            for (UI childUI : children) {
                if (childUI != null) {
                    node.addChild(childUI.materialize(scope));
                }
            }
            return node;
        };
    }

    public static UI Row(UI... children) {
        return Row(Modifier.DEFAULT, children);
    }

    /**
     * Box container with stacking.
     */
    public static UI Box(Modifier modifier, UI... children) {
        return scope -> {
            UINode node = new UINode("Box", BoxPolicy.DEFAULT);
            node.setModifier(modifier);
            for (UI childUI : children) {
                if (childUI != null) {
                    node.addChild(childUI.materialize(scope));
                }
            }
            return node;
        };
    }

    public static UI Box(UI... children) {
        return Box(Modifier.DEFAULT, children);
    }

    /**
     * Empty spacer that occupies dimension.
     */
    public static UI Spacer(Modifier modifier) {
        return scope -> {
            UINode node = new UINode("Spacer", null);
            node.setModifier(modifier);
            return node;
        };
    }

    /**
     * Static text (never re-evaluated).
     */
    public static UI Text(String staticText) {
        return Text(Modifier.DEFAULT, staticText);
    }

    public static UI Text(Modifier modifier, String staticText) {
        return scope -> {
            UINode node = new UINode("Text", null);
            node.setModifier(modifier);
            node.setTextContent(staticText);
            node.addBinding(Binding.ofStatic(staticText, node::setTextContent));
            return node;
        };
    }

    /**
     * Reactive text binding.
     * When observed state mutates, ONLY this text node updates. The parent builder function does not re-run.
     */
    public static UI Text(Supplier<String> reactiveSupplier) {
        return Text(Modifier.DEFAULT, reactiveSupplier);
    }

    public static UI Text(Modifier modifier, Supplier<String> reactiveSupplier) {
        return scope -> {
            UINode node = new UINode("Text", null);
            node.setModifier(modifier);
            Binding<String> binding = new Binding<>(
                    reactiveSupplier,
                    node::setTextContent,
                    node::markDrawDirty
            );
            node.addBinding(binding);
            binding.evaluate(); // Initial calculation and dependency subscription
            return node;
        };
    }

    /**
     * Interactive Button with label and click handler.
     */
    public static UI Button(String label, Runnable onClick) {
        return Button(Modifier.DEFAULT, label, onClick);
    }

    public static UI Button(Modifier modifier, String label, Runnable onClick) {
        return scope -> {
            UINode node = new UINode("Button", BoxPolicy.DEFAULT);
            node.setModifier(modifier.clickableThen(onClick));
            node.setAccessibilityRole("Button");
            node.setContentDescription(label);

            UINode textChild = new UINode("Text", null);
            textChild.setTextContent(label);
            node.addChild(textChild);
            return node;
        };
    }

    public static UI Button(Modifier modifier, Supplier<String> dynamicLabel, Runnable onClick) {
        return scope -> {
            UINode node = new UINode("Button", BoxPolicy.DEFAULT);
            node.setModifier(modifier.clickableThen(onClick));
            node.setAccessibilityRole("Button");

            UINode textChild = new UINode("Text", null);
            Binding<String> binding = new Binding<>(
                    dynamicLabel,
                    val -> {
                        textChild.setTextContent(val);
                        node.setContentDescription(val);
                    },
                    node::markDrawDirty
            );
            node.addBinding(binding);
            binding.evaluate();
            node.addChild(textChild);
            return node;
        };
    }

    /**
     * Horizontal or vertical divider line.
     */
    public static UI Divider(Modifier modifier, int color) {
        return scope -> {
            UINode node = new UINode("Divider", null);
            node.setModifier(modifier.backgroundThen(color));
            return node;
        };
    }

    public static UI Divider() {
        return Divider(Modifier.fillMaxWidth().size(1), 0xFFE0E0E0);
    }

    /**
     * Visual icon element.
     */
    public static UI Icon(String name, Modifier modifier) {
        return scope -> {
            UINode node = new UINode("Icon", null);
            node.setModifier(modifier);
            node.setContentDescription(name);
            node.setAccessibilityRole("Image");
            return node;
        };
    }

    /**
     * Image element.
     */
    public static UI Image(String sourceUrlOrRes, String description, Modifier modifier) {
        return scope -> {
            UINode node = new UINode("Image", null);
            node.setModifier(modifier);
            node.setContentDescription(description);
            node.setAccessibilityRole("Image");
            return node;
        };
    }
}
