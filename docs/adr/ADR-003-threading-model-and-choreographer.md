# ADR-003: Threading Model & Frame Synchronization

## Context
Mobile apps receive state updates from background threads (network responses, WebSocket messages, database observers, GPS location updates). Directly mutating UI views from worker threads crashes Android (`CalledFromWrongThreadException`). Conversely, blocking worker threads while waiting for the main thread degrades throughput.

## Options Considered
1. **Require developers to manually call `runOnUiThread` / `Handler`**:
   - Error-prone, leaks UI code into domain layers, creates boilerplate.
2. **Actor/Channel queue with lock-free atomic batching**:
   - State writes from any thread update the underlying atomic cell (`VarHandle` / `AtomicInteger`).
   - The state object appends itself to a thread-safe lock-free invalidation ring buffer.
   - If not already scheduled, a single Choreographer frame callback is posted on the Android Main looper.
   - On the Choreographer `doFrame(frameTimeNanos)` callback:
     1. Drain pending batch queue.
     2. Propagate state changes to dependent bindings and scopes.
     3. Topological sort on dirty scopes to prevent diamond-dependency glitches.
     4. Single measure/layout pass for dirty subtrees.
     5. Re-record dirty `RenderNode` display lists.

## Decision
Adopt Option 2. State writes are thread-safe and non-blocking from any thread. Invalidation is automatically debounced to the next VSYNC tick. Multiple writes within the same 16ms/8ms frame window are collapsed into a single UI update.

## Consequences
- Background processing (e.g. 60 GPS updates/sec in an Uber-style ride screen) safely updates state without crashing or causing redundant layout passes.
- Deterministic frame timing locked to the device refresh rate (60Hz, 90Hz, 120Hz).
