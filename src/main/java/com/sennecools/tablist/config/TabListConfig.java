package com.sennecools.tablist.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.sennecools.tablist.Constants;
import com.sennecools.tablist.platform.Services;

import java.nio.file.Path;
import java.util.List;

public class TabListConfig {

    private static final String DEFAULT_DISPLAY_NAME_FORMAT =
            "{dimension} {name} {health} &7#AFK";
    private static final String LEGACY_DISPLAY_NAME_FORMAT = "{name} &7#AFK";

    // Runtime values
    public static String serverName;
    public static List<String> headerFrames;
    public static List<String> footerFrames;
    public static int updateInterval;
    public static int animationInterval;
    public static String displayNameFormat;
    public static NameFormattingProvider nameFormattingProvider;
    public static String sortMode;
    public static boolean afkEnabled;
    public static int afkTimeout;
    public static MetricsProvider metricsProvider;
    public static double tpsGoodThreshold;
    public static double tpsWarningThreshold;
    public static double msptGoodThreshold;
    public static double msptWarningThreshold;
    public static double cpuGoodThreshold;
    public static double cpuWarningThreshold;
    public static boolean chatEnabled;
    public static String chatFormat;
    public static boolean allowPlayerColors;

    public static void load() {
        Path configPath = Services.PLATFORM.getConfigDir().resolve("tablist.toml");

        try (CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                .autosave()
                .preserveInsertionOrder()
                .build()) {

            config.load();

            boolean needsSave = false;

            // ── Appearance ──
            needsSave |= setDefaultIfMissing(config, "appearance.server_name", "Your Server",
                    "Your server's name. Use #SERVERNAME in header/footer to insert it.");
            needsSave |= setDefaultIfMissing(config, "appearance.header", List.of(
                    "#N        &#FF5555&l#SERVERNAME        #N&#AAAAAA&m            #N",
                    "#N        &#5555FF&l#SERVERNAME        #N&#AAAAAA&m            #N"
            ), "Text shown above the player list. Multiple entries create animation frames.");
            needsSave |= setDefaultIfMissing(config, "appearance.footer", List.of(
                    "&fOnline: &e#PLAYERCOUNT&7/&e#MAXPLAYERS #N&7TPS: #CTPS &7| MSPT: &#55FFFF#MSPT #N&7Memory: &#AA55FF#MEMORY &7| Uptime: &#FFAA00#UPTIME",
                    "&fOnline: &e#PLAYERCOUNT&7/&e#MAXPLAYERS #N&7TPS: #CTPS &7| Ping: &#55FFFF#PING&7ms #N&7Memory: &#AA55FF#MEMORY &7| Uptime: &#FFAA00#UPTIME"
            ), "Text shown below the player list. Multiple entries create animation frames.");
            needsSave |= setDefaultIfMissing(
                    config,
                    "appearance.display_name_format",
                    DEFAULT_DISPLAY_NAME_FORMAT,
                    "Display name format. Supports {name}, {rank}, {prefix}, {suffix}, "
                            + "{primary_group}, {dimension}, {health}, {world}, {ping}, and {gamemode} "
                            + "placeholders + & color codes."
            );
            if (LEGACY_DISPLAY_NAME_FORMAT.equals(
                    config.getOrElse("appearance.display_name_format", DEFAULT_DISPLAY_NAME_FORMAT))) {
                config.set("appearance.display_name_format", DEFAULT_DISPLAY_NAME_FORMAT);
                needsSave = true;
            }
            needsSave |= setDefaultIfMissing(config, "appearance.update_interval", 500,
                    "How often (ms) the tab list refreshes. Range: 1-10000. Default: 500.");
            needsSave |= setDefaultIfMissing(config, "appearance.animation_interval", 4,
                    "Update cycles between animation frame changes. Range: 1-200. Default: 4.");

            // ── Sorting ──
            needsSave |= setDefaultIfMissing(config, "sorting.sort_mode", "NONE",
                    "How to sort players: NONE, ALPHABETICAL, or RANK.");

            // ── Name formatting integration ──
            String defaultFormattingProvider = config.contains("ftbranks.enable_ftbranks_formatting")
                    && config.getOrElse("ftbranks.enable_ftbranks_formatting", true)
                    ? "FTB"
                    : "NONE";
            needsSave |= setDefaultIfMissing(
                    config,
                    "appearance.name_formatting_provider",
                    defaultFormattingProvider,
                    "Name formatting provider: NONE, FTB, or LP."
            );

            // ── AFK ──
            needsSave |= setDefaultIfMissing(config, "afk.afk_enabled", true,
                    "Enable AFK detection. AFK players have greyed-out names.");
            needsSave |= setDefaultIfMissing(config, "afk.afk_timeout", 300,
                    "Seconds of inactivity before AFK. Range: 10-3600. Default: 300.");

            // ── Performance metrics ──
            needsSave |= setDefaultIfMissing(config, "performance.metrics_provider", "VANILLA",
                    "Metrics provider: VANILLA or SPARK. SPARK falls back to VANILLA when unavailable.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.tps_good", 18.0,
                    "TPS values at or above this threshold are green.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.tps_warning", 15.0,
                    "TPS values at or above this threshold are yellow; lower values are red.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.mspt_good", 40.0,
                    "MSPT values at or below this threshold are green.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.mspt_warning", 50.0,
                    "MSPT values at or below this threshold are yellow; higher values are red.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.cpu_good", 60.0,
                    "CPU percentages at or below this threshold are green.");
            needsSave |= setDefaultIfMissing(config, "performance.colors.cpu_warning", 85.0,
                    "CPU percentages at or below this threshold are yellow; higher values are red.");

            // ── Chat formatting ──
            needsSave |= setDefaultIfMissing(config, "chat.enabled", true,
                    "Enable server chat formatting.");
            needsSave |= setDefaultIfMissing(config, "chat.format",
                    "{prefix}{name}{suffix}&7: &f{message}",
                    "Chat format. Supports player/server placeholders plus {message} and {raw_message}.");
            needsSave |= setDefaultIfMissing(config, "chat.allow_player_colors", false,
                    "Allow players to use legacy and hex color codes in their messages.");

            if (needsSave) {
                config.save();
            }

            // Load runtime values
            serverName = config.getOrElse("appearance.server_name", "Your Server");
            headerFrames = List.copyOf(config.getOrElse("appearance.header", List.of("")));
            footerFrames = List.copyOf(config.getOrElse("appearance.footer", List.of("")));
            updateInterval = clamp(config.getOrElse("appearance.update_interval", 500), 1, 10000);
            animationInterval = clamp(config.getOrElse("appearance.animation_interval", 4), 1, 200);
            displayNameFormat = config.getOrElse(
                    "appearance.display_name_format",
                    DEFAULT_DISPLAY_NAME_FORMAT
            );
            String providerValue = config.getOrElse("appearance.name_formatting_provider", "NONE");
            nameFormattingProvider = NameFormattingProvider.fromString(providerValue);
            if (providerValue == null
                    || !providerValue.trim().equalsIgnoreCase(nameFormattingProvider.name())) {
                Constants.LOGGER.warn(
                        "Unknown name_formatting_provider '{}'. Falling back to NONE.",
                        providerValue
                );
            }
            sortMode = config.getOrElse("sorting.sort_mode", "NONE");
            afkEnabled = config.getOrElse("afk.afk_enabled", true);
            afkTimeout = clamp(config.getOrElse("afk.afk_timeout", 300), 10, 3600);
            String metricsProviderValue = config.getOrElse("performance.metrics_provider", "VANILLA");
            metricsProvider = MetricsProvider.fromString(metricsProviderValue);
            if (metricsProviderValue == null
                    || !metricsProviderValue.trim().equalsIgnoreCase(metricsProvider.name())) {
                Constants.LOGGER.warn(
                        "Unknown metrics_provider '{}'. Falling back to VANILLA.",
                        metricsProviderValue
                );
            }
            tpsGoodThreshold = config.getOrElse("performance.colors.tps_good", 18.0);
            tpsWarningThreshold = config.getOrElse("performance.colors.tps_warning", 15.0);
            msptGoodThreshold = config.getOrElse("performance.colors.mspt_good", 40.0);
            msptWarningThreshold = config.getOrElse("performance.colors.mspt_warning", 50.0);
            cpuGoodThreshold = config.getOrElse("performance.colors.cpu_good", 60.0);
            cpuWarningThreshold = config.getOrElse("performance.colors.cpu_warning", 85.0);
            chatEnabled = config.getOrElse("chat.enabled", true);
            chatFormat = config.getOrElse(
                    "chat.format",
                    "{prefix}{name}{suffix}&7: &f{message}"
            );
            allowPlayerColors = config.getOrElse("chat.allow_player_colors", false);

            Constants.LOGGER.info("TabList config loaded. Update interval: {} ms", updateInterval);
        }
    }

    private static <T> boolean setDefaultIfMissing(CommentedConfig config, String path, T defaultValue, String comment) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            config.setComment(path, comment);
            return true;
        }
        return false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
