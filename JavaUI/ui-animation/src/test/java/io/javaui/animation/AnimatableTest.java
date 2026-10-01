package io.javaui.animation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnimatableTest {

    @Test
    void animationTicksInterpolateCorrectly() {
        Animatable anim = new Animatable(0f);
        anim.animateTo(100f, 300);

        long start = 1_000_000_000L;
        anim.doFrame(start);

        // Advance 150ms
        anim.doFrame(start + 150_000_000L);
        assertThat(anim.peek()).isGreaterThan(50f); // Ease-out is faster at beginning

        // Complete 300ms
        boolean stillRunning = anim.doFrame(start + 300_000_000L);
        assertThat(stillRunning).isFalse();
        assertThat(anim.peek()).isEqualTo(100f);
    }

    @Test
    void zeroSteadyStateAllocationDuringAnimationTicks() {
        Animatable anim = new Animatable(0f);
        anim.animateTo(1000f, 1000);

        long baseTime = 1_000_000_000L;
        // Warm up JIT
        for (int i = 0; i < 50; i++) {
            anim.doFrame(baseTime + (i * 16_000_000L));
        }

        System.gc();
        long freeBefore = Runtime.getRuntime().freeMemory();

        // Run 100 frames
        for (int i = 50; i < 150; i++) {
            anim.doFrame(baseTime + (i * 16_000_000L));
        }

        long freeAfter = Runtime.getRuntime().freeMemory();
        // Memory delta should be virtually zero (no retained heap allocations)
        assertThat(Math.abs(freeBefore - freeAfter)).isLessThan(64 * 1024);
    }
}
