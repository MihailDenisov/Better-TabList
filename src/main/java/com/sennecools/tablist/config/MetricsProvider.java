package com.sennecools.tablist.config;

import java.util.Locale;

public enum MetricsProvider {
    VANILLA,
    SPARK;

    public static MetricsProvider fromString(String value) {
        if (value == null) {
            return VANILLA;
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return VANILLA;
        }
    }
}
