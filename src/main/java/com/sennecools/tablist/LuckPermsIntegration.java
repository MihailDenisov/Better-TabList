package com.sennecools.tablist;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class LuckPermsIntegration {

    private static final PlayerMetaData EMPTY_META_DATA = new PlayerMetaData("", "", "");

    private LuckPermsIntegration() {
    }

    private static User getUser(ServerPlayer player) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            return luckPerms.getUserManager().getUser(player.getUUID());
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static PlayerMetaData getPlayerMetaData(ServerPlayer player) {
        User user = getUser(player);

        if (user == null) {
            return EMPTY_META_DATA;
        }

        CachedMetaData metaData = user.getCachedData().getMetaData();
        return new PlayerMetaData(
                Objects.requireNonNullElse(metaData.getPrefix(), ""),
                Objects.requireNonNullElse(metaData.getSuffix(), ""),
                user.getPrimaryGroup()
        );
    }

    public static int getGroupWeight(ServerPlayer player) {
        User user = getUser(player);

        if (user == null) {
            return 0;
        }

        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .map(Group::getWeight)
                .filter(weight -> weight.isPresent())
                .mapToInt(weight -> weight.getAsInt())
                .max()
                .orElse(0);
    }

    public record PlayerMetaData(String prefix, String suffix, String primaryGroup) {
    }
}
