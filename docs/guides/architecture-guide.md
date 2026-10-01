# JavaUI Architecture Guide & Deep Dive

## 1. Core Philosophy: Why JavaUI?
Jetpack Compose was designed around Kotlin's language features (trailing lambdas, compiler plugins, inline functions, coroutines). However, thousands of large-scale engineering organizations have massive Java codebases, strict compilation time budgets, or prefer Java's predictability, tooling stability, and low memory profile.
JavaUI is built from first principles for Java 17+:
- **Explicit Scope Ownership**: No hidden thread-local magic; scopes are passed explicitly (`Scope s`).
- **Fine-Grained Bindings**: Subscriptions happen at the property level (`Text(() -> "Count: " + count.get())`). The builder function only runs once unless structural dependencies change.
- **RenderNode HW Acceleration**: Each isolated node draws into an Android `RenderNode` display list. Re-rendering unchanged siblings consumes 0 CPU cycles.
- **Zero Steady-State Allocations**: Measure, layout, and draw hot paths allocate 0 objects per frame.

## 2. Directory & Module Breakdown
```
JynixUI/
├── ui-annotations/          - Pure Java: @UIComponent, @Stable, @Preview
├── ui-state/                - Pure Java: IntState, State<T>, DerivedState, Batch
├── ui-runtime/              - Node tree, Scope, InvalidationQueue, Binding
├── ui-layout/               - Pure Java: Constraints, MeasurePass, LayoutPass, Alignment
├── ui-renderer/             - Android: JavaUIHostView, RenderNodeRenderer, TextCache
├── ui-input/                - Android: Touch, Gestures, FocusManager, TalkBack Virtual Tree, IME
├── ui-animation/            - Animatable, SpringSpec, TweenSpec, zero-alloc tick loop
├── ui-foundation/           - Column, Row, Box, Text, Button, LazyColumn, LazyRow, Image
├── ui-material/             - TopBar, Card, Switch, Checkbox, TextField, BottomSheet, Dialog
├── ui-navigation/           - NavHost, Backstack, NavRoute, transitions
├── ui-interop/              - AndroidView (MapView/WebView), JavaUIFragment, JavaUIView
├── ui-compiler/             - JavaPoet annotation processor
├── ui-gradle-plugin/        - AGP 8.x plugin 'io.javaui' + ASM transforms
├── ui-lint/                 - Android Lint rules for JavaUI
├── ui-android-studio-plugin/- IntelliJ / Android Studio plugin with Layoutlib Preview
├── ui-benchmarks/           - Comparative micro/macro benchmarks (JavaUI vs Compose vs Views)
└── ui-samples/              - 21-Screen enterprise mobility sample application
```

## 3. The Invalidation Cycle
1. **State Mutation**: Background or Main thread calls `count.set(5)` or `count.update(c -> c + 1)`.
2. **Batch Queue**: The state registers into an atomic frame queue.
3. **Choreographer Sync**: The Android main looper calls `doFrame`.
4. **Binding Refresh**: Dependent bindings recompute values and update node fields.
5. **Layout (if size changed)**: Only nodes marked `layoutDirty` re-measure with cached `Constraints`.
6. **Display List Recording**: Only nodes marked `drawDirty` open `RenderNode.beginRecording()`.
7. **Draw**: GPU renders display lists.
