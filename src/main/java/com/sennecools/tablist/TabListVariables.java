package com.sennecools.tablist;

import com.sennecools.tablist.config.TabListConfig;
import com.sennecools.tablist.config.NameFormattingProvider;
import com.sennecools.tablist.metrics.ServerMetricsResolver;
import com.sennecools.tablist.platform.Services;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TabListVariables {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String tablistChars(String template, ServerPlayer player) {
        //? if >=1.21.9 {
        /*MinecraftServer server = player.level().getServer();*/
        //?} else {
        MinecraftServer server = player.getServer();
        //?}
        if (server == null || template == null) return "";

        String output = template;

        output = ServerMetricsResolver.replacePlaceholders(output, server);

        if (output.contains("#SERVERNAME")) {
            String name = TabListConfig.serverName != null ? TabListConfig.serverName : "";
            output = output.replace("#SERVERNAME", name);
        }
        if (output.contains("#PLAYERCOUNT")) {
            output = output.replace("#PLAYERCOUNT", String.valueOf(getPlayerCount(server)));
        }
        if (output.contains("#MEMORY")) {
            output = output.replace("#MEMORY", getMemoryUsage());
        }
        if (output.contains("#UPTIME")) {
            output = output.replace("#UPTIME", getServerUptime());
        }
        if (output.contains("#PING")) {
            output = output.replace("#PING", String.valueOf(getPlayerPing(player)));
        }
        if (output.contains("#RANK")) {
            output = output.replace("#RANK", getPlayerRank(player));
        }
        if (output.contains("#MAXPLAYERS")) {
            output = output.replace("#MAXPLAYERS", String.valueOf(server.getMaxPlayers()));
        }
        if (output.contains("#PLAYERNAME")) {
            //? if >=1.21.9 {
            /*output = output.replace("#PLAYERNAME", player.getGameProfile().name());*/
            //?} else {
            output = output.replace("#PLAYERNAME", player.getGameProfile().getName());
            //?}
        }
        if (output.contains("#WORLD")) {
            //? if >=1.21.11 {
            /*output = output.replace("#WORLD", player.level().dimension().identifier().getPath());*/
            //?} else {
            output = output.replace("#WORLD", player.level().dimension().location().getPath());
            //?}
        }
        if (output.contains("#AFK")) {
            boolean afk = TabListConfig.afkEnabled
                    && TabListUpdater.INSTANCE != null
                    && TabListUpdater.INSTANCE.isPlayerAFK(player);
            output = output.replace("#AFK", afk ? "AFK" : "");
        }
        if (output.contains("#DATE") || output.contains("#TIME")) {
            LocalDateTime now = LocalDateTime.now();
            if (output.contains("#DATE")) {
                output = output.replace("#DATE", now.format(DATE_FORMATTER));
            }
            if (output.contains("#TIME")) {
                output = output.replace("#TIME", now.format(TIME_FORMATTER));
            }
        }
        output = output.replace("#N", "\n");

        return TextFormatter.convertColorCodes(output);
    }

    public static String resolveDisplayName(ServerPlayer player) {
        String displayName = buildDefaultDisplayName(player);

        if (isPlayerAFK(player)) {
            displayName = "\u00A77" + displayName.replaceAll("\u00A7[0-9a-fA-Fk-oK-OrRxX]", "");
            displayName = restoreStatusColors(displayName);
        }

        return displayName;
    }

    private static String buildDefaultDisplayName(ServerPlayer player) {
        NameFormattingProvider provider = TabListConfig.nameFormattingProvider;

        if (provider == null) {
            provider = NameFormattingProvider.NONE;
        }

        // FTB Ranks formatting
        if (provider == NameFormattingProvider.FTB && isFTBRanksLoaded()) {
            String formatted = FTBRanksIntegration.getFormattedDisplayName(player);

            if (formatted != null) {
                return TextFormatter.convertColorCodes(applyPlayerStatusPlaceholders(formatted, player));
            }
        }

        String format = TabListConfig.displayNameFormat;

        if (format == null) {
            format = "{name}";
        }

        return TextFormatter.convertColorCodes(resolvePlayerPlaceholders(format, player));
    }

    public static String resolvePlayerPlaceholders(String format, ServerPlayer player) {
        if (format == null) {
            return "";
        }

        NameFormattingProvider provider = TabListConfig.nameFormattingProvider;
        if (provider == null) {
            provider = NameFormattingProvider.NONE;
        }

        String result = format.replace("{name}", player.getGameProfile().getName());

        switch (provider) {
            case FTB -> {
                result = result.replace(
                        "{rank}",
                        getPlayerRank(player)
                );

                result = result.replace("{prefix}", "");
                result = result.replace("{suffix}", "");
                result = result.replace("{primary_group}", "");
            }

            case LP -> {
                if (isLuckPermsLoaded()) {
                    LuckPermsIntegration.PlayerMetaData metaData =
                            LuckPermsIntegration.getPlayerMetaData(player);
                    result = result.replace("{prefix}", metaData.prefix());
                    result = result.replace("{suffix}", metaData.suffix());
                    result = result.replace("{primary_group}", metaData.primaryGroup());
                    result = result.replace("{rank}", metaData.primaryGroup());
                } else {
                    result = result.replace("{prefix}", "");
                    result = result.replace("{suffix}", "");
                    result = result.replace("{primary_group}", "");
                    result = result.replace("{rank}", "");
                }
            }

            case NONE -> {
                result = result.replace("{prefix}", "");
                result = result.replace("{suffix}", "");
                result = result.replace("{primary_group}", "");
                result = result.replace("{rank}", "");
            }
        }

        return applyPlayerStatusPlaceholders(result, player);
    }

    private static String applyPlayerStatusPlaceholders(String format, ServerPlayer player) {
        return format
                .replace("{dimension}", getDimensionSymbol(player))
                .replace("{health}", getHealthSymbol(player))
                .replace("{world}", player.level().dimension().location().getPath())
                .replace("{ping}", String.valueOf(getPlayerPing(player)))
                .replace("{gamemode}", player.gameMode.getGameModeForPlayer().getName())
                .replace("#AFK", isPlayerAFK(player) ? "AFK" : "");
    }

    private static boolean isPlayerAFK(ServerPlayer player) {
        return TabListConfig.afkEnabled
                && TabListUpdater.INSTANCE != null
                && TabListUpdater.INSTANCE.isPlayerAFK(player);
    }

    private static String getDimensionSymbol(ServerPlayer player) {
        if (Level.OVERWORLD.equals(player.level().dimension())) {
            return "&a\u24CC&r";
        }
        if (Level.NETHER.equals(player.level().dimension())) {
            return "&c\u24C3&r";
        }
        if (Level.END.equals(player.level().dimension())) {
            return "&d\u24BA&r";
        }

        return "&7" + player.level().dimension().location().getPath() + "&r";
    }

    private static String getHealthSymbol(ServerPlayer player) {
        int health = Math.max(0, (int) Math.ceil(
                player.getHealth() + player.getAbsorptionAmount()
        ));
        return "&c[" + health + "\u2764]&r";
    }

    private static String restoreStatusColors(String displayName) {
        String colored = displayName
                .replace("\u24CC", "\u00A7a\u24CC\u00A77")
                .replace("\u24C3", "\u00A7c\u24C3\u00A77")
                .replace("\u24BA", "\u00A7d\u24BA\u00A77");

        return TextFormatter.restoreHealthColors(colored);
    }

    private static String getPlayerRank(ServerPlayer player) {
        NameFormattingProvider provider = TabListConfig.nameFormattingProvider;

        if (provider == NameFormattingProvider.FTB && isFTBRanksLoaded()) {
            return FTBRanksIntegration.getPlayerRankName(player);
        }

        if (provider == NameFormattingProvider.LP && isLuckPermsLoaded()) {
            return LuckPermsIntegration.getPlayerMetaData(player).primaryGroup();
        }

        return "";
    }

    static int getPlayerRankPower(ServerPlayer player) {
        if (TabListConfig.nameFormattingProvider == NameFormattingProvider.FTB
                && isFTBRanksLoaded()) {
            return FTBRanksIntegration.getPlayerRankPower(player);
        }

        if (TabListConfig.nameFormattingProvider == NameFormattingProvider.LP
                && isLuckPermsLoaded()) {
            return LuckPermsIntegration.getGroupWeight(player);
        }

        return 0;
    }

    private static boolean isFTBRanksLoaded() {
        return Services.PLATFORM.isModLoaded("ftbranks");
    }

    private static boolean isLuckPermsLoaded() {
        return Services.PLATFORM.isModLoaded("luckperms");
    }

    private static int getPlayerCount(MinecraftServer server) {
        return server.getPlayerList().getPlayerCount();
    }

    private static String getMemoryUsage() {
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        return String.format(Locale.ROOT, "%.1f MB / %.1f MB", usedMemory / 1048576.0, maxMemory / 1048576.0);
    }

    private static String getServerUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = (uptimeMillis / 1000) % 60;
        long minutes = (uptimeMillis / (1000 * 60)) % 60;
        long hours = (uptimeMillis / (1000 * 60 * 60)) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);
        return days > 0
                ? String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds)
                : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static int getPlayerPing(ServerPlayer player) {
        return player.connection.latency();
    }

}
