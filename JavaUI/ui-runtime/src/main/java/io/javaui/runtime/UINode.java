package io.javaui.runtime;

import io.javaui.layout.LayoutNode;
import io.javaui.layout.LayoutPolicy;
import io.javaui.layout.Modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete node in the active JavaUI tree.
 */
public class UINode {

    private final String type;
    private final LayoutNode layoutNode;
    private final List<UINode> children = new ArrayList<>();
    private final List<Binding<?>> bindings = new ArrayList<>();

    private UINode parent;
    private boolean drawDirty = true;
    private String textContent = "";
    private int customColor = 0;
    private Runnable clickListener = null;
    private String contentDescription = null;
    private String accessibilityRole = null;

    public UINode(String type, LayoutPolicy policy) {
        this.type = type;
        this.layoutNode = new LayoutNode(policy);
    }

    public String getType() {
        return type;
    }

    public LayoutNode getLayoutNode() {
        return layoutNode;
    }

    public void setModifier(Modifier modifier) {
        layoutNode.setModifier(modifier);
        extractModifierHandlers(modifier);
        markDrawDirty();
    }

    public void addChild(UINode child) {
        children.add(child);
        child.parent = this;
        layoutNode.addChild(child.layoutNode);
        markDrawDirty();
    }

    public void removeChild(UINode child) {
        children.remove(child);
        child.parent = null;
        layoutNode.removeChild(child.layoutNode);
        markDrawDirty();
    }

    public void clearChildren() {
        for (UINode child : children) {
            child.dispose();
        }
        children.clear();
        layoutNode.clearChildren();
        markDrawDirty();
    }

    public List<UINode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public UINode getParent() {
        return parent;
    }

    public void addBinding(Binding<?> binding) {
        bindings.add(binding);
    }

    public void setTextContent(String text) {
        this.textContent = text != null ? text : "";
        layoutNode.markLayoutDirty();
        markDrawDirty();
    }

    public String getTextContent() {
        return textContent;
    }

    public void setCustomColor(int color) {
        this.customColor = color;
        markDrawDirty();
    }

    public int getCustomColor() {
        return customColor;
    }

    public void setClickListener(Runnable listener) {
        this.clickListener = listener;
    }

    public Runnable getClickListener() {
        return clickListener;
    }

    public void setContentDescription(String desc) {
        this.contentDescription = desc;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setAccessibilityRole(String role) {
        this.accessibilityRole = role;
    }

    public String getAccessibilityRole() {
        return accessibilityRole;
    }

    public void markDrawDirty() {
        this.drawDirty = true;
        if (parent != null && !parent.drawDirty) {
            parent.markDrawDirty();
        }
    }

    public void clearDrawDirty() {
        this.drawDirty = false;
    }

    public boolean isDrawDirty() {
        return drawDirty;
    }

    private void extractModifierHandlers(Modifier modifier) {
        for (Modifier.Element elem : modifier.getElements()) {
            if (elem instanceof Modifier.ClickableElement ce) {
                this.clickListener = ce.onClick();
                if (ce.contentDescription() != null) {
                    this.contentDescription = ce.contentDescription();
                }
            } else if (elem instanceof Modifier.SemanticsElement se) {
                this.accessibilityRole = se.role();
                this.contentDescription = se.contentDescription();
            }
        }
    }

    public void dispose() {
        for (Binding<?> b : bindings) {
            b.dispose();
        }
        bindings.clear();
        for (UINode child : children) {
            child.dispose();
        }
    }
}
