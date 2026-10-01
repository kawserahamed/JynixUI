# ADR-004: Pure JVM Box-Model Layout Engine

## Context
Standard Android View layout (`onMeasure` / `onLayout`) depends on Android platform binaries (`android.view.View`), making unit tests slow and requiring Robolectric or device emulators. Jetpack Compose layout uses its own multiplatform constraints, but is tightly coupled to Kotlin compiler transforms.

## Options Considered
1. **Rely on Android platform `View.MeasureSpec`**:
   - Tied to Android runtime. Cannot run in standard JVM CI.
2. **Pure Java `Constraints` and `LayoutNode` engine**:
   - Constraints packed into a 64-bit primitive long or compact record (`minWidth`, `maxWidth`, `minHeight`, `maxHeight`).
   - Single-pass measure and layout (O(N) traversal).
   - Intrinsic measurements supported for two-pass cases (`minIntrinsicWidth`, `maxIntrinsicHeight`).
   - Strict `dp` unit enforcement: all `Modifier.padding()`, `Modifier.size()` are written in device-independent pixels; pixel conversion is injected via `Density` during measure pass.

## Decision
Adopt Option 2. Implement pure Java layout in `ui-layout`. The package contains zero imports from `android.*`. Layout nodes implement measure policies for `Column`, `Row`, `Box`, `Stack`, `LazyColumn`, `LazyRow`, and `LazyGrid`.

## Consequences
- 100% of complex layout algorithms, flexbox logic, and alignment math can be tested on standard JVM JUnit tests in under 50ms.
- Sub-pixel rounding issues are prevented via integer fixed-point rounding rules.
