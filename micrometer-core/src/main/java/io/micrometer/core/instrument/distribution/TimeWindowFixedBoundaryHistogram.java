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
package io.micrometer.core.instrument.distribution;

import io.micrometer.core.instrument.Clock;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A histogram implementation that does not support precomputed percentiles but supports
 * aggregable percentile histograms and SLA boundaries. There is no need for a high dynamic range
 * histogram and its more expensive memory footprint if all we are interested in is fixed histogram counts.
 *
 * @author Jon Schneider
 * @since 1.0.3
 */
public class TimeWindowFixedBoundaryHistogram
        extends AbstractTimeWindowHistogram<TimeWindowFixedBoundaryHistogram.FixedBoundaryHistogram, Void> {
    private final long[] buckets;

    public TimeWindowFixedBoundaryHistogram(Clock clock, DistributionStatisticConfig config, boolean supportsAggregablePercentiles) {
        super(clock, config, FixedBoundaryHistogram.class, supportsAggregablePercentiles);

        NavigableSet<Long> histogramBuckets = distributionStatisticConfig.getHistogramBuckets(supportsAggregablePercentiles);

        Boolean percentileHistogram = distributionStatisticConfig.isPercentileHistogram();
        if (percentileHistogram != null && percentileHistogram) {
            histogramBuckets.addAll(PercentileHistogramBuckets.buckets(distributionStatisticConfig));
        }

        this.buckets = histogramBuckets.stream().filter(Objects::nonNull).mapToLong(Long::longValue).toArray();
        initRingBuffer();
    }

    @Override
    FixedBoundaryHistogram newBucket() {
        return new FixedBoundaryHistogram();
    }

    @Override
    void recordLong(FixedBoundaryHistogram bucket, long value) {
        bucket.record(value);
    }

    @Override
    final void recordDouble(FixedBoundaryHistogram bucket, double value) {
        recordLong(bucket, (long) Math.ceil(value));
    }

    @Override
    void resetBucket(FixedBoundaryHistogram bucket) {
        bucket.reset();
    }

    @Override
    Void newAccumulatedHistogram(FixedBoundaryHistogram[] ringBuffer) {
        return null;
    }

    @Override
    void accumulate() {
        // do nothing -- we aren't using swaps for source and accumulated
    }

    @Override
    void resetAccumulatedHistogram() {
    }

    @Override
    double valueAtPercentile(double percentile) {
        return 0;
    }

    @Override
    double countAtValue(long value) {
        return currentHistogram().countAtValue(value);
    }

    @Override
    void outputSummary(PrintStream printStream, double bucketScaling) {
        printStream.format("%14s %10s\n\n", "Bucket", "TotalCount");

        String bucketFormatString = "%14.1f %10d\n";

        for (int i = 0; i < buckets.length; i++) {
            printStream.format(Locale.US, bucketFormatString,
                    buckets[i] / bucketScaling,
                    currentHistogram().values[i].get());
        }

        printStream.write('\n');
    }

    class FixedBoundaryHistogram {
        /**
         * For recording efficiency, this is a normal histogram. We turn these values into
         * cumulative counts only on calls to {@link #countAtValue(long)}.
         */
        final AtomicLong[] values;

        FixedBoundaryHistogram() {
            this.values = new AtomicLong[buckets.length];
            for (int i = 0; i < values.length; i++)
                values[i] = new AtomicLong(0);
        }

        long countAtValue(long value) {
            int index = Arrays.binarySearch(buckets, value);
            if (index < 0)
                return 0;
            long count = 0;
            for (int i = 0; i <= index; i++)
                count += values[i].get();
            return count;
        }

        void reset() {
            for (AtomicLong value : values) value.set(0);
        }

        void record(long value) {
            int index = leastLessThanOrEqualTo(value);
            if (index > -1)
                values[index].incrementAndGet();
        }

        /**
         * The least bucket that is less than or equal to a sample.
         */
        int leastLessThanOrEqualTo(long key) {
            int low = 0;
            int high = buckets.length - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                if (buckets[mid] < key)
                    low = mid + 1;
                else if (buckets[mid] > key)
                    high = mid - 1;
                else
                    return mid; // exact match
            }

            return low >= buckets.length ? -1 : low;
        }
    }
}