package com.sennecools.tablist.chat;

import com.sennecools.tablist.TextFormatter;
import com.sennecools.tablist.config.TabListConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatFormatterTest {

    @AfterEach
    void resetConfig() {
        TabListConfig.chatEnabled = false;
    }

    @Test
    void configuredTemplateProducesThePlayerNameOnlyOnce() {
        Component formatted = format("{name}: {message}", part -> part.replace("{name}", "Steve"));

        assertEquals("Steve: Hello", formatted.getString());
    }

    @Test
    void luckPermsPrefixAndTemplateColorsArePreserved() {
        Component formatted = format(
                "{prefix}{name}&7: &f{message}",
                part -> TextFormatter.convertColorCodes(part
                        .replace("{prefix}", "&c[Admin] ")
                        .replace("{name}", "Steve"))
        );

        assertEquals("[Admin] Steve: Hello", formatted.getString());
        assertEquals(ChatFormatting.RED.getColor(), colorOf(formatted, "[Admin] "));
        assertEquals(ChatFormatting.WHITE.getColor(), colorOf(formatted, "Hello"));
    }

    @Test
    void worldAndLuckPermsPlaceholdersProduceTheWholeLine() {
        Component formatted = format(
                "&8[&7{world}&8] {prefix}{name}{suffix}&7: &f{message}",
                part -> TextFormatter.convertColorCodes(part
                        .replace("{world}", "survival")
                        .replace("{prefix}", "&c[Admin] ")
                        .replace("{name}", "Steve")
                        .replace("{suffix}", ""))
        );

        assertEquals("[survival] [Admin] Steve: Hello", formatted.getString());
    }

    @Test
    void disabledFormattingKeepsTheVanillaBoundChatType() {
        TabListConfig.chatEnabled = false;

        assertNull(ChatTypeOverride.select(null));
    }

    @Test
    void missingLuckPermsMetadataCanResolveToEmptyStrings() {
        Component formatted = format(
                "{prefix}{name}{suffix}: {message}",
                part -> part
                        .replace("{prefix}", "")
                        .replace("{name}", "Steve")
                        .replace("{suffix}", "")
        );

        assertEquals("Steve: Hello", formatted.getString());
    }

    private static Component format(String template, UnaryOperator<String> resolver) {
        return ChatFormatter.format(template, "Hello", false, resolver);
    }

    private static Integer colorOf(Component component, String expectedText) {
        return component.visit((style, text) -> text.contains(expectedText)
                ? Optional.ofNullable(style.getColor()).map(color -> color.getValue())
                : Optional.empty(), Style.EMPTY).orElse(null);
    }
}
