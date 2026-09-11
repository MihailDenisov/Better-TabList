package com.sennecools.tablist.neoforge.mixin;

import com.sennecools.tablist.chat.ChatTypeOverride;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @ModifyArg(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage("
                            + "Lnet/minecraft/network/chat/PlayerChatMessage;"
                            + "Lnet/minecraft/server/level/ServerPlayer;"
                            + "Lnet/minecraft/network/chat/ChatType$Bound;)V"
            ),
            index = 2
    )
    private ChatType.Bound tablist$selectChatType(ChatType.Bound vanillaChatType) {
        return ChatTypeOverride.select(vanillaChatType);
    }
}
