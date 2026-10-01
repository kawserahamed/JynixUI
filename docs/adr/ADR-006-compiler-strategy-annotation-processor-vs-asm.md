# ADR-006: Compiler Strategy — Annotation Processor vs ASM Transforms

## Context
In Java, an Annotation Processor (`javax.annotation.processing.Processor`) can generate new source files or resources, but CANNOT modify existing method bodies. In contrast, Kotlin compiler plugins (like the Compose compiler) rewrite AST and IR directly.
We must ensure that:
1. The framework works 100% correctly and idiomatically without any code generation or bytecode transform.
2. Code generation and ASM bytecode transforms are strictly non-breaking performance optimizations.
3. Instrumented code and uninstrumented code behave identically, proven by parity tests.

## Architecture

### 1. Zero-Tooling Baseline (Pure Java Runtime)
Developers write:
```java
public static UI Counter(Scope s) {
    IntState count = s.intState(0);
    return Column(
        Modifier.fillMaxWidth().padding(16),
        Text(() -> "Count: " + count.get()),
        Button("Increase", () -> count.update(c -> c + 1))
    );
}
```
This runs identically on plain javac, unit tests, and production without any plugins.

### 2. Layer 1: Annotation Processor (`ui-compiler`)
Using JavaPoet:
- Scans `@UIComponent` methods.
- Generates static metadata descriptor classes (`*UIComponentMetadata`).
- Assigns stable compile-time integer hashes to components and call sites for rapid slot-matching.
- Generates preview registry entries for Android Studio layoutlib integration.

### 3. Layer 2: Bytecode Optimization via AGP ASM Transform (`ui-gradle-plugin`)
Using `AsmClassVisitorFactory` (Android Gradle Plugin 8.x):
- Injects call-site key constants into `Scope.startComponent(int key, String name)`.
- Replaces closure capturing allocations for non-capturing static lambdas with cached static singletons.
- Eliminates synthetic accessor bridges.
- Skipped automatically in fast debug builds for sub-second hot iteration.

## Decision & Parity Rules
The framework core must never depend on the compiler. Every component must be constructible via direct factory methods. A dedicated parity test suite runs every UI component through both the uninstrumented pipeline and the instrumented bytecode pipeline, asserting exact object identity and update behavior.

## Consequences
- Developers are never blocked by IDE compiler plugin crashes.
- Fast JVM unit testing without Gradle transform overhead.
- Release APKs benefit from static call-site keys and reduced allocation overhead.
