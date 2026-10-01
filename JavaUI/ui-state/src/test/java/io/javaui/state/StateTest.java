package io.javaui.state;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;

class StateTest {

    @Test
    void intStateUpdatesAndNotifiesObserver() {
        IntState count = new IntState(0);
        AtomicInteger notifications = new AtomicInteger();

        count.addObserver(src -> notifications.incrementAndGet());

        count.set(1);
        count.update(c -> c + 5);

        assertThat(count.get()).isEqualTo(6);
        assertThat(notifications.get()).isEqualTo(2);
    }

    @Test
    void derivedStateRecomputesOnlyWhenDirty() {
        IntState a = new IntState(10);
        IntState b = new IntState(20);
        AtomicInteger calculationCount = new AtomicInteger();

        DerivedState<Integer> sum = new DerivedState<>(() -> {
            calculationCount.incrementAndGet();
            return a.get() + b.get();
        });

        assertThat(sum.get()).isEqualTo(30);
        assertThat(calculationCount.get()).isEqualTo(1);

        // Multiple reads do not recompute
        assertThat(sum.get()).isEqualTo(30);
        assertThat(calculationCount.get()).isEqualTo(1);

        // Changing a dependency marks dirty
        a.set(15);
        assertThat(sum.get()).isEqualTo(35);
        assertThat(calculationCount.get()).isEqualTo(2);
    }
}
