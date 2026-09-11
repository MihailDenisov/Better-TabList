package com.sennecools.tablist.chat;

import com.sennecools.tablist.TabListVariables;
import com.sennecools.tablist.TextFormatter;
import com.sennecools.tablist.config.TabListConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.UnaryOperator;

public final class ChatFormatter {

    private static final String MESSAGE_PLACEHOLDER = "{message}";
    private static final String RAW_MESSAGE_PLACEHOLDER = "{raw_message}";

    private ChatFormatter() {
    }

    public static Component format(ServerPlayer player, String rawMessage) {
        String template = TabListConfig.chatFormat == null ? MESSAGE_PLACEHOLDER : TabListConfig.chatFormat;
        return format(
                template,
                rawMessage,
                TabListConfig.allowPlayerColors,
                part -> resolveTemplatePart(part, player)
        );
    }

    static Component format(
            String template,
            String rawMessage,
            boolean allowPlayerColors,
            UnaryOperator<String> templateResolver
    ) {
        MutableComponent result = Component.empty();
        Style currentStyle = Style.EMPTY;
        int cursor = 0;
        while (cursor < template.length()) {
            PlaceholderMatch match = findNextMessagePlaceholder(template, cursor);
            if (match == null) {
                TextFormatter.ParsedText parsed = parseTemplatePart(
                        template.substring(cursor),
                        templateResolver,
                        currentStyle
                );
                result.append(parsed.component());
                break;
            }

            TextFormatter.ParsedText parsed = parseTemplatePart(
                    template.substring(cursor, match.index()),
                    templateResolver,
                    currentStyle
            );
            result.append(parsed.component());
            currentStyle = parsed.trailingStyle();

            if (!match.raw() && allowPlayerColors) {
                TextFormatter.ParsedText parsedMessage = TextFormatter.parseColoredText(
                        TextFormatter.convertColorCodes(rawMessage),
                        currentStyle
                );
                result.append(parsedMessage.component());
                currentStyle = parsedMessage.trailingStyle();
            } else {
                result.append(Component.literal(rawMessage).withStyle(currentStyle));
            }
            cursor = match.index() + match.length();
        }

        if (template.isEmpty()) {
            return Component.empty();
        }
        return result;
    }

    private static String resolveTemplatePart(String template, ServerPlayer player) {
        String withPlayerPlaceholders = TabListVariables.resolvePlayerPlaceholders(template, player);
        return TabListVariables.tablistChars(withPlayerPlaceholders, player);
    }

    private static TextFormatter.ParsedText parseTemplatePart(
            String template,
            UnaryOperator<String> templateResolver,
            Style initialStyle
    ) {
        return TextFormatter.parseColoredText(templateResolver.apply(template), initialStyle);
    }

    private static PlaceholderMatch findNextMessagePlaceholder(String template, int start) {
        int messageIndex = template.indexOf(MESSAGE_PLACEHOLDER, start);
        int rawMessageIndex = template.indexOf(RAW_MESSAGE_PLACEHOLDER, start);

        if (messageIndex < 0 && rawMessageIndex < 0) {
            return null;
        }
        if (rawMessageIndex >= 0 && (messageIndex < 0 || rawMessageIndex < messageIndex)) {
            return new PlaceholderMatch(rawMessageIndex, RAW_MESSAGE_PLACEHOLDER.length(), true);
        }
        return new PlaceholderMatch(messageIndex, MESSAGE_PLACEHOLDER.length(), false);
    }

    private record PlaceholderMatch(int index, int length, boolean raw) {
    }
}
