package io.javaui.state;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Batches state mutations until the next frame tick to avoid redundant intermediate work.
 */
public final class Batch {

    private static final ConcurrentLinkedQueue<Runnable> PENDING_MUTATIONS = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean FRAME_SCHEDULED = new AtomicBoolean(false);
    private static volatile BatchScheduler scheduler = new DefaultJvmScheduler();

    private Batch() {}

    public static void setScheduler(BatchScheduler newScheduler) {
        scheduler = newScheduler;
    }

    public static BatchScheduler getScheduler() {
        return scheduler;
    }

    public static void enqueue(Runnable mutation) {
        PENDING_MUTATIONS.offer(mutation);
        if (FRAME_SCHEDULED.compareAndSet(false, true)) {
            scheduler.scheduleFrame(Batch::drainPending);
        }
    }

    public static void drainPending() {
        FRAME_SCHEDULED.set(false);
        Runnable r;
        while ((r = PENDING_MUTATIONS.poll()) != null) {
            r.run();
        }
    }

    /**
     * Executes a batch immediately (useful for synchronous tests).
     */
    public static void runBatchNow(Runnable block) {
        block.run();
        drainPending();
    }

    private static class DefaultJvmScheduler implements BatchScheduler {
        @Override
        public void scheduleFrame(Runnable task) {
            // Immediate execution in JVM unit test environment
            task.run();
        }

        @Override
        public boolean isMainThread() {
            return true;
        }
    }
}
