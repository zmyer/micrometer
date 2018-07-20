/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.step;

import io.micrometer.core.instrument.Clock;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Subtly different from {@code com.netflix.spectator.impl.StepDouble} in that we want to be able
 * to increment BEFORE rolling over the interval.
 *
 * @author Jon Schneider
 */
public class StepDouble {
    private final Clock clock;
    private final long stepMillis;
    private final DoubleAdder current = new DoubleAdder();
    private final AtomicLong lastInitPos;
    private volatile double previous = 0.0;

    public StepDouble(Clock clock, long stepMillis) {
        this.clock = clock;
        this.stepMillis = stepMillis;
        lastInitPos = new AtomicLong(clock.wallTime() / stepMillis);
    }

    private void rollCount(long now) {
        final long stepTime = now / stepMillis;
        final long lastInit = lastInitPos.get();
        if (lastInit < stepTime && lastInitPos.compareAndSet(lastInit, stepTime)) {
            final double v = current.sumThenReset();
            // Need to check if there was any activity during the previous step interval. If there was
            // then the init position will move forward by 1, otherwise it will be older. No activity
            // means the previous interval should be set to the `init` value.
            previous = (lastInit == stepTime - 1) ? v : 0.0;
        }
    }

    public DoubleAdder getCurrent() {
        return current;
    }

    /**
     * @return The value for the last completed interval.
     */
    public double poll() {
        rollCount(clock.wallTime());
        return previous;
    }
}