package com.sennecools.tablist.config;

public enum NameFormattingProvider {
    NONE,
    FTB,
    LP;

    public static NameFormattingProvider fromString(String value) {
        if (value == null) {
            return NONE;
        }

        try {
            return NameFormattingProvider.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}