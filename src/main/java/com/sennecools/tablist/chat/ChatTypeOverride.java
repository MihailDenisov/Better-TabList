package com.sennecools.tablist.chat;

import com.sennecools.tablist.config.TabListConfig;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.Optional;

public final class ChatTypeOverride {

    private ChatTypeOverride() {
    }

    public static ChatType.Bound select(ChatType.Bound vanillaChatType) {
        return TabListConfig.chatEnabled ? ContentOnlyChatType.BOUND : vanillaChatType;
    }

    private static final class ContentOnlyChatType {
        private static final ChatTypeDecoration DECORATION = new ChatTypeDecoration(
                "%s",
                List.of(ChatTypeDecoration.Parameter.CONTENT),
                Style.EMPTY
        );
        private static final Holder<ChatType> TYPE = Holder.direct(new ChatType(DECORATION, DECORATION));
        private static final ChatType.Bound BOUND = new ChatType.Bound(
                TYPE,
                CommonComponents.EMPTY,
                Optional.empty()
        );

        private ContentOnlyChatType() {
        }
    }
}
