package com.sennecools.tablist.metrics;

import com.sennecools.tablist.config.TabListConfig;

final class PerformanceColorFormatter {

    private PerformanceColorFormatter() {
    }

    static String tps(double value) {
        return decreasing(value, TabListConfig.tpsGoodThreshold, TabListConfig.tpsWarningThreshold);
    }

    static String mspt(double value) {
        return increasing(value, TabListConfig.msptGoodThreshold, TabListConfig.msptWarningThreshold);
    }

    static String cpu(double value) {
        return increasing(value, TabListConfig.cpuGoodThreshold, TabListConfig.cpuWarningThreshold);
    }

    private static String decreasing(double value, double good, double warning) {
        if (value >= good) {
            return "&a";
        }
        return value >= warning ? "&e" : "&c";
    }

    private static String increasing(double value, double good, double warning) {
        if (value <= good) {
            return "&a";
        }
        return value <= warning ? "&e" : "&c";
    }
}
