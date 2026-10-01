package io.javaui.material;

import io.javaui.layout.BoxPolicy;
import io.javaui.layout.ColumnPolicy;
import io.javaui.layout.Modifier;
import io.javaui.layout.RowPolicy;
import io.javaui.runtime.Binding;
import io.javaui.runtime.Scope;
import io.javaui.runtime.UI;
import io.javaui.runtime.UINode;
import io.javaui.state.BooleanState;
import io.javaui.state.State;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.javaui.foundation.UIFoundation.*;

/**
 * Material Design component library for JavaUI.
 */
public final class UIMaterial {

    private UIMaterial() {}

    /**
     * Material Card with elevated background and rounded corners.
     */
    public static UI Card(Modifier modifier, UI... children) {
        return scope -> {
            UINode cardNode = new UINode("Card", ColumnPolicy.INSTANCE);
            Modifier styled = modifier.backgroundThen(0xFFFFFFFF, 12f);
            cardNode.setModifier(styled);
            for (UI child : children) {
                if (child != null) {
                    cardNode.addChild(child.materialize(scope));
                }
            }
            return cardNode;
        };
    }

    /**
     * Surface container.
     */
    public static UI Surface(Modifier modifier, int backgroundColor, UI... children) {
        return scope -> {
            UINode surf = new UINode("Surface", BoxPolicy.DEFAULT);
            surf.setModifier(modifier.backgroundThen(backgroundColor));
            for (UI child : children) {
                if (child != null) {
                    surf.addChild(child.materialize(scope));
                }
            }
            return surf;
        };
    }

    /**
     * Material TextField with reactive text binding and change listener.
     */
    public static UI TextField(
            Modifier modifier,
            Supplier<String> textSupplier,
            Consumer<String> onTextChanged,
            String placeholder
    ) {
        return scope -> {
            UINode inputNode = new UINode("TextField", BoxPolicy.DEFAULT);
            inputNode.setAccessibilityRole("EditText");
            inputNode.setModifier(modifier.backgroundThen(0xFFF1F3F4, 8f).paddingThen(12, 10));

            Binding<String> binding = new Binding<>(
                    textSupplier,
                    val -> inputNode.setTextContent(val.isEmpty() ? placeholder : val),
                    inputNode::markDrawDirty
            );
            inputNode.addBinding(binding);
            binding.evaluate();

            return inputNode;
        };
    }

    /**
     * Material Switch toggle component.
     */
    public static UI Switch(Modifier modifier, BooleanState checkedState) {
        return scope -> {
            UINode switchNode = new UINode("Switch", BoxPolicy.DEFAULT);
            switchNode.setAccessibilityRole("Switch");
            switchNode.setModifier(modifier.sizeThen(52, 32).clickableThen(checkedState::toggle));

            Binding<Boolean> binding = new Binding<>(
                    checkedState::get,
                    isChecked -> {
                        switchNode.setCustomColor(isChecked ? 0xFF1976D2 : 0xFFBDBDBD);
                        switchNode.setContentDescription(isChecked ? "On" : "Off");
                    },
                    switchNode::markDrawDirty
            );
            switchNode.addBinding(binding);
            binding.evaluate();

            return switchNode;
        };
    }

    /**
     * Material Checkbox.
     */
    public static UI Checkbox(Modifier modifier, BooleanState checkedState) {
        return scope -> {
            UINode cb = new UINode("Checkbox", BoxPolicy.DEFAULT);
            cb.setAccessibilityRole("CheckBox");
            cb.setModifier(modifier.sizeThen(24, 24).clickableThen(checkedState::toggle));

            Binding<Boolean> binding = new Binding<>(
                    checkedState::get,
                    isChecked -> {
                        cb.setCustomColor(isChecked ? 0xFF1976D2 : 0xFF757575);
                        cb.setContentDescription(isChecked ? "Checked" : "Unchecked");
                    },
                    cb::markDrawDirty
            );
            cb.addBinding(binding);
            binding.evaluate();

            return cb;
        };
    }

    /**
     * Material RadioButton.
     */
    public static UI RadioButton(Modifier modifier, boolean selected, Runnable onSelect) {
        return scope -> {
            UINode rb = new UINode("RadioButton", BoxPolicy.DEFAULT);
            rb.setAccessibilityRole("RadioButton");
            rb.setModifier(modifier.sizeThen(24, 24).clickableThen(onSelect));
            rb.setCustomColor(selected ? 0xFF1976D2 : 0xFF757575);
            return rb;
        };
    }

    /**
     * Material TopAppBar.
     */
    public static UI TopBar(Modifier modifier, String title, UI leadingAction, UI trailingAction) {
        return scope -> {
            UINode bar = new UINode("TopBar", RowPolicy.INSTANCE);
            bar.setModifier(modifier.fillMaxWidth().sizeThen(360, 56).backgroundThen(0xFFFFFFFF).paddingThen(16, 8));

            if (leadingAction != null) {
                bar.addChild(leadingAction.materialize(scope));
            }
            UINode titleNode = Text(title).materialize(scope);
            bar.addChild(titleNode);

            if (trailingAction != null) {
                bar.addChild(trailingAction.materialize(scope));
            }
            return bar;
        };
    }

    /**
     * Modal Dialog component.
     */
    public static UI Dialog(BooleanState isOpen, String title, String message, Runnable onConfirm, Runnable onDismiss) {
        return scope -> {
            UINode dialogOverlay = new UINode("DialogOverlay", BoxPolicy.DEFAULT);
            dialogOverlay.setModifier(Modifier.fillMaxSize().background(0x88000000));

            Binding<Boolean> openBinding = new Binding<>(
                    isOpen::get,
                    open -> {
                        dialogOverlay.clearChildren();
                        if (open) {
                            UI content = Card(
                                    Modifier.size(320, 200).padding(16),
                                    Text(title),
                                    Spacer(Modifier.size(8)),
                                    Text(message),
                                    Spacer(Modifier.size(16)),
                                    Row(
                                            Button("Cancel", onDismiss),
                                            Spacer(Modifier.size(8)),
                                            Button("Confirm", onConfirm)
                                    )
                            );
                            dialogOverlay.addChild(content.materialize(scope));
                        }
                    },
                    dialogOverlay::markDrawDirty
            );
            dialogOverlay.addBinding(openBinding);
            openBinding.evaluate();

            return dialogOverlay;
        };
    }

    /**
     * BottomSheet component.
     */
    public static UI BottomSheet(BooleanState isExpanded, UI content) {
        return scope -> {
            UINode sheet = new UINode("BottomSheet", BoxPolicy.DEFAULT);
            Binding<Boolean> binding = new Binding<>(
                    isExpanded::get,
                    expanded -> {
                        sheet.clearChildren();
                        if (expanded && content != null) {
                            sheet.addChild(content.materialize(scope));
                        }
                    },
                    sheet::markDrawDirty
            );
            sheet.addBinding(binding);
            binding.evaluate();
            return sheet;
        };
    }
}
