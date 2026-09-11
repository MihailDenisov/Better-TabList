package com.sennecools.tablist.metrics;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow.CpuUsage;
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick;
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;

final class SparkMetricsProvider {

    private SparkMetricsProvider() {
    }

    static ServerMetrics poll() {
        try {
            Spark spark = SparkProvider.get();
            DoubleStatistic<TicksPerSecond> tps = spark.tps();
            GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt = spark.mspt();
            if (tps == null || mspt == null) {
                return null;
            }

            DoubleAverageInfo mspt1m = mspt.poll(MillisPerTick.MINUTES_1);
            return new ServerMetrics(
                    tps.poll(TicksPerSecond.MINUTES_1),
                    tps.poll(TicksPerSecond.MINUTES_5),
                    mspt1m.mean(),
                    mspt1m.percentile95th(),
                    spark.cpuProcess().poll(CpuUsage.MINUTES_1) * 100.0
            );
        } catch (IllegalStateException | LinkageError exception) {
            return null;
        }
    }
}
