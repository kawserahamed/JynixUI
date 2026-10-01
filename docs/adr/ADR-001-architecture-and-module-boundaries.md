# ADR-001: Architecture and Module Boundaries

## Context
Large-scale Android applications (such as Uber, Grab, Netflix) feature hundreds of modules and engineering teams. Jetpack Compose brings a modern declarative paradigm but binds heavily to Kotlin compiler plugins (IR manipulation), Kotlin runtime metadata, and frequent recomposition cascades. Java developers in enterprise ecosystems require a high-performance declarative UI framework written in pure Java (Java 17 language level) without Kotlin runtime baggage, supporting high-speed incremental compilation, predictable debuggability, and zero-allocation hot paths.

## Options Considered
1. **Monolithic Framework Jar**: Single artifact containing all logic. Rejected due to poor separation of concerns, inability to run layout/state tests on plain JVMs, and slow incremental builds.
2. **Layered Decoupled Modules**:
   - `ui-annotations`: Zero-dependency marker annotations.
   - `ui-state`: Pure Java primitive & object signals, graph dependency tracker.
   - `ui-layout`: Pure Java box constraints, measure/layout algorithm (JVM testable).
   - `ui-runtime`: Node tree, dirty-flag invalidator, scope lifecycle.
   - `ui-renderer`: Android `RenderNode` and `Canvas` hardware-accelerated drawing.
   - `ui-input`: Android touch, gestures, focus, IME, accessibility.
   - `ui-foundation` & `ui-material`: Reusable components.
   - `ui-compiler` & `ui-gradle-plugin`: Build tooling and bytecode optimization.

## Decision
Adopt Option 2. Keep `ui-annotations`, `ui-state`, and `ui-layout` strictly free of Android SDK classes (`android.*`). This allows 100% of state graph updates, layout math, flex calculations, and constraint solver logic to run in sub-millisecond JVM unit tests without Robolectric or emulator overhead.

## Consequences
- Clean separation between mathematical layout logic and platform rendering.
- JVM CI runs in seconds instead of minutes.
- Android platform dependencies are strictly quarantined to `ui-renderer`, `ui-input`, and integration modules.
- Multi-module apps can include only what they need.
