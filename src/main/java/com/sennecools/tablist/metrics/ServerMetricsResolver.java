package com.sennecools.tablist.metrics;

import com.sennecools.tablist.config.MetricsProvider;
import com.sennecools.tablist.config.TabListConfig;
import com.sennecools.tablist.platform.Services;
import net.minecraft.server.MinecraftServer;

import java.lang.management.ManagementFactory;
import java.util.Locale;

public final class ServerMetricsResolver {

    private static final String UNAVAILABLE = "N/A";

    private ServerMetricsResolver() {
    }

    public static String replacePlaceholders(String text, MinecraftServer server) {
        if (text == null || !containsMetricsPlaceholder(text)) {
            return text;
        }

        ServerMetrics metrics = resolve(server);
        return text
                .replace("#CTPS_1M", coloredTps(metrics.tps1m()))
                .replace("#CTPS_5M", coloredTps(metrics.tps5m()))
                .replace("#CMSPT_P95", coloredMspt(metrics.mspt95p()))
                .replace("#CTPS1M", coloredTps(metrics.tps1m()))
                .replace("#CTPS5M", coloredTps(metrics.tps5m()))
                .replace("#CMSPT95P", coloredMspt(metrics.mspt95p()))
                .replace("#CMSPT", coloredMspt(metrics.mspt()))
                .replace("#CCPU", coloredCpu(metrics.cpu()))
                .replace("#TPS_1M", format(metrics.tps1m()))
                .replace("#TPS_5M", format(metrics.tps5m()))
                .replace("#MSPT_P95", format(metrics.mspt95p()))
                .replace("#TPS1M", format(metrics.tps1m()))
                .replace("#TPS5M", format(metrics.tps5m()))
                .replace("#MSPT95P", format(metrics.mspt95p()))
                .replace("#CTPS", coloredTps(metrics.tps1m()))
                .replace("#TPS", format(metrics.tps1m()))
                .replace("#MSPT", format(metrics.mspt()))
                .replace("#CPU", format(metrics.cpu()));
    }

    private static ServerMetrics resolve(MinecraftServer server) {
        if (TabListConfig.metricsProvider == MetricsProvider.SPARK
                && Services.PLATFORM.isModLoaded("spark")) {
            ServerMetrics sparkMetrics = SparkMetricsProvider.poll();
            if (sparkMetrics != null) {
                return sparkMetrics;
            }
        }

        return vanillaMetrics(server);
    }

    private static ServerMetrics vanillaMetrics(MinecraftServer server) {
        double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = mspt == 0.0 ? 20.0 : Math.min(1000.0 / mspt, 20.0);
        return new ServerMetrics(tps, tps, mspt, mspt, getProcessCpuLoad());
    }

    private static double getProcessCpuLoad() {
        if (ManagementFactory.getOperatingSystemMXBean()
                instanceof com.sun.management.OperatingSystemMXBean operatingSystem) {
            return operatingSystem.getProcessCpuLoad() * 100.0;
        }
        return Double.NaN;
    }

    private static boolean containsMetricsPlaceholder(String text) {
        return text.contains("#TPS")
                || text.contains("#CTPS")
                || text.contains("#MSPT")
                || text.contains("#CMSPT")
                || text.contains("#CPU")
                || text.contains("#CCPU");
    }

    private static String coloredTps(double value) {
        return color(value, PerformanceColorFormatter.tps(value));
    }

    private static String coloredMspt(double value) {
        return color(value, PerformanceColorFormatter.mspt(value));
    }

    private static String coloredCpu(double value) {
        return color(value, PerformanceColorFormatter.cpu(value));
    }

    private static String color(double value, String color) {
        return Double.isFinite(value) ? color + format(value) : UNAVAILABLE;
    }

    private static String format(double value) {
        return Double.isFinite(value) ? String.format(Locale.ROOT, "%.1f", value) : UNAVAILABLE;
    }
}
