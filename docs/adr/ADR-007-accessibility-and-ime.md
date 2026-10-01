# ADR-007: Accessibility, Focus, D-Pad, and IME Input

## Context
A single-host-view architecture (`JavaUIHostView`) collapses all UI elements into one platform view. Without explicit engineering, Android's accessibility system (TalkBack), hardware keyboards, D-pads (TV/foldables), and software input method editors (IME) cannot discover or interact with individual elements.

## Options Considered
1. **Overlay invisible dummy Android Views**:
   - Heavy memory cost, defeats zero-allocation goal, causes synchronization lag with layout.
2. **First-class Virtual Hierarchy via `AccessibilityNodeProvider` & Custom `InputConnection`**:
   - `JavaUIHostView` overrides `getAccessibilityNodeProvider()`.
   - Traverses `UINode` tree and exposes virtual view IDs.
   - Maps semantics: `role` (Button, Header, Checkbox, Text), `contentDescription`, `actions` (Click, Focus, Scroll), and screen bounds.
   - For text input, `TextField` acquires virtual focus; `JavaUIHostView.onCreateInputConnection(EditorInfo)` routes to an optimized `JavaUIInputConnection` supporting composing spans, batch edits, software keyboard actions (Done/Next/Search), and cursor bounds tracking.
   - Focus manager implements 2D geometric navigation for arrow keys and D-pad.

## Decision
Adopt Option 2. Build `ui-input` with complete support for TalkBack virtual trees, D-pad traversal, hardware keyboard accelerators, and full IME software keyboard interaction.

## Consequences
- 100% TalkBack accessibility compliance out-of-the-box.
- Enterprise apps can pass strict enterprise accessibility audits.
- Full support for Android TV, foldable arrow navigation, and external keyboards.
