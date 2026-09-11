package com.sennecools.tablist;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextFormatter {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fA-Fk-oK-OrR])");
    private static final Pattern HEALTH_DISPLAY_PATTERN = Pattern.compile("\\[\\d+\u2764]");
    private static final Pattern GRADIENT_MINIMESSAGE_PATTERN = Pattern.compile(
            "<gradient:(#[0-9a-fA-F]{6}(?::#[0-9a-fA-F]{6})+)>(.*?)</gradient>"
    );
    private static final Pattern GRADIENT_TAB_PATTERN = Pattern.compile(
            "<(#[0-9a-fA-F]{6})>(.*?)</(#[0-9a-fA-F]{6})>"
    );
    private static final Pattern HEX_CODE_IN_TEXT_PATTERN = Pattern.compile("&x(&[0-9a-fA-F]){6}");

    private TextFormatter() {
    }

    public static String convertColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        text = processGradients(text);

        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuilder converted = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char character : hex.toCharArray()) {
                replacement.append('\u00A7').append(character);
            }
            hexMatcher.appendReplacement(converted, Matcher.quoteReplacement(replacement.toString()));
        }
        hexMatcher.appendTail(converted);

        return COLOR_CODE_PATTERN.matcher(converted.toString()).replaceAll("\u00A7$1");
    }

    /**
     * Parses legacy and hex color codes into component styles.
     */
    public static Component parseColoredText(String text) {
        return parseColoredText(text, Style.EMPTY).component();
    }

    public static ParsedText parseColoredText(String text, Style initialStyle) {
        if (text == null || text.isEmpty()) {
            return new ParsedText(Component.empty(), initialStyle);
        }

        MutableComponent result = Component.empty();
        StringBuilder currentText = new StringBuilder();
        Style currentStyle = initialStyle;

        int index = 0;
        while (index < text.length()) {
            char character = text.charAt(index);

            if (character == '\u00A7' && index + 1 < text.length()) {
                if (!currentText.isEmpty()) {
                    result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
                    currentText = new StringBuilder();
                }

                char code = text.charAt(index + 1);
                if ((code == 'x' || code == 'X') && index + 13 < text.length()) {
                    StringBuilder hex = new StringBuilder();
                    boolean validHex = true;
                    for (int digit = 0; digit < 6; digit++) {
                        int digitIndex = index + 2 + (digit * 2);
                        if (digitIndex + 1 < text.length() && text.charAt(digitIndex) == '\u00A7') {
                            char hexCharacter = text.charAt(digitIndex + 1);
                            if (isHexChar(hexCharacter)) {
                                hex.append(hexCharacter);
                            } else {
                                validHex = false;
                                break;
                            }
                        } else {
                            validHex = false;
                            break;
                        }
                    }

                    if (validHex) {
                        currentStyle = currentStyle.withColor(
                                TextColor.fromRgb(Integer.parseInt(hex.toString(), 16))
                        );
                        index += 14;
                        continue;
                    }
                }

                ChatFormatting formatting = getFormatting(code);
                if (formatting != null) {
                    if (formatting.isColor()) {
                        currentStyle = Style.EMPTY.withColor(formatting);
                    } else if (formatting == ChatFormatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = applyFormatting(currentStyle, formatting);
                    }
                    index += 2;
                    continue;
                }
            }

            currentText.append(character);
            index++;
        }

        if (!currentText.isEmpty()) {
            result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
        }

        return new ParsedText(result, currentStyle);
    }

    public record ParsedText(Component component, Style trailingStyle) {
    }

    static String restoreHealthColors(String text) {
        return HEALTH_DISPLAY_PATTERN.matcher(text).replaceAll(
                match -> "\u00A7c" + match.group() + "\u00A77"
        );
    }

    private static boolean isHexChar(char character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    private static ChatFormatting getFormatting(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> ChatFormatting.BLACK;
            case '1' -> ChatFormatting.DARK_BLUE;
            case '2' -> ChatFormatting.DARK_GREEN;
            case '3' -> ChatFormatting.DARK_AQUA;
            case '4' -> ChatFormatting.DARK_RED;
            case '5' -> ChatFormatting.DARK_PURPLE;
            case '6' -> ChatFormatting.GOLD;
            case '7' -> ChatFormatting.GRAY;
            case '8' -> ChatFormatting.DARK_GRAY;
            case '9' -> ChatFormatting.BLUE;
            case 'a' -> ChatFormatting.GREEN;
            case 'b' -> ChatFormatting.AQUA;
            case 'c' -> ChatFormatting.RED;
            case 'd' -> ChatFormatting.LIGHT_PURPLE;
            case 'e' -> ChatFormatting.YELLOW;
            case 'f' -> ChatFormatting.WHITE;
            case 'k' -> ChatFormatting.OBFUSCATED;
            case 'l' -> ChatFormatting.BOLD;
            case 'm' -> ChatFormatting.STRIKETHROUGH;
            case 'n' -> ChatFormatting.UNDERLINE;
            case 'o' -> ChatFormatting.ITALIC;
            case 'r' -> ChatFormatting.RESET;
            default -> null;
        };
    }

    private static Style applyFormatting(Style style, ChatFormatting formatting) {
        return switch (formatting) {
            case BOLD -> style.withBold(true);
            case ITALIC -> style.withItalic(true);
            case UNDERLINE -> style.withUnderlined(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case OBFUSCATED -> style.withObfuscated(true);
            default -> style;
        };
    }

    private static String processGradients(String text) {
        Matcher miniMatcher = GRADIENT_MINIMESSAGE_PATTERN.matcher(text);
        StringBuilder converted = new StringBuilder();
        while (miniMatcher.find()) {
            String[] colorHexes = miniMatcher.group(1).split(":");
            List<int[]> stops = new ArrayList<>();
            for (String hex : colorHexes) {
                stops.add(parseHexColor(hex));
            }
            miniMatcher.appendReplacement(
                    converted,
                    Matcher.quoteReplacement(applyGradient(miniMatcher.group(2), stops))
            );
        }
        miniMatcher.appendTail(converted);

        Matcher tabMatcher = GRADIENT_TAB_PATTERN.matcher(converted.toString());
        converted = new StringBuilder();
        while (tabMatcher.find()) {
            List<int[]> stops = List.of(
                    parseHexColor(tabMatcher.group(1)),
                    parseHexColor(tabMatcher.group(3))
            );
            tabMatcher.appendReplacement(
                    converted,
                    Matcher.quoteReplacement(applyGradient(tabMatcher.group(2), stops))
            );
        }
        tabMatcher.appendTail(converted);
        return converted.toString();
    }

    private static int[] parseHexColor(String hex) {
        String digits = hex.startsWith("#") ? hex.substring(1) : hex;
        return new int[]{
                Integer.parseInt(digits.substring(0, 2), 16),
                Integer.parseInt(digits.substring(2, 4), 16),
                Integer.parseInt(digits.substring(4, 6), 16)
        };
    }

    private static String applyGradient(String innerText, List<int[]> stops) {
        String stripped = HEX_CODE_IN_TEXT_PATTERN.matcher(innerText).replaceAll("");
        List<Character> visibleCharacters = new ArrayList<>();

        for (int index = 0; index < stripped.length(); index++) {
            if (stripped.charAt(index) == '&' && index + 1 < stripped.length()
                    && isLegacyCode(stripped.charAt(index + 1))) {
                index++;
                continue;
            }
            visibleCharacters.add(stripped.charAt(index));
        }

        if (visibleCharacters.isEmpty()) {
            return stripped;
        }

        List<String> activeFormattingAtCharacter = collectActiveFormatting(stripped);
        StringBuilder result = new StringBuilder();
        int segments = stops.size() - 1;

        for (int index = 0; index < visibleCharacters.size(); index++) {
            double progress = visibleCharacters.size() == 1
                    ? 0.0
                    : (double) index / (visibleCharacters.size() - 1);
            double segmentPosition = progress * segments;
            int segmentIndex = Math.min((int) segmentPosition, segments - 1);
            double localProgress = segmentPosition - segmentIndex;

            int[] start = stops.get(segmentIndex);
            int[] end = stops.get(segmentIndex + 1);
            int red = interpolate(start[0], end[0], localProgress);
            int green = interpolate(start[1], end[1], localProgress);
            int blue = interpolate(start[2], end[2], localProgress);

            result.append(String.format(
                    Locale.ROOT,
                    "\u00A7x\u00A7%x\u00A7%x\u00A7%x\u00A7%x\u00A7%x\u00A7%x",
                    (red >> 4) & 0xF, red & 0xF,
                    (green >> 4) & 0xF, green & 0xF,
                    (blue >> 4) & 0xF, blue & 0xF
            ));
            if (index < activeFormattingAtCharacter.size()) {
                result.append(activeFormattingAtCharacter.get(index));
            }
            result.append(visibleCharacters.get(index));
        }

        return result.toString();
    }

    private static List<String> collectActiveFormatting(String text) {
        List<String> formatting = new ArrayList<>();
        String runningFormatting = "";

        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '&' && index + 1 < text.length()) {
                char code = text.charAt(index + 1);
                if (isColorOrReset(code)) {
                    runningFormatting = "";
                    index++;
                    continue;
                }
                if (isModifier(code)) {
                    runningFormatting += "\u00A7" + code;
                    index++;
                    continue;
                }
            }
            formatting.add(runningFormatting);
        }

        return formatting;
    }

    private static boolean isLegacyCode(char code) {
        return isColorOrReset(code) || isModifier(code);
    }

    private static boolean isColorOrReset(char code) {
        char normalized = Character.toLowerCase(code);
        return (normalized >= '0' && normalized <= '9')
                || (normalized >= 'a' && normalized <= 'f')
                || normalized == 'r';
    }

    private static boolean isModifier(char code) {
        char normalized = Character.toLowerCase(code);
        return normalized >= 'k' && normalized <= 'o';
    }

    private static int interpolate(int start, int end, double progress) {
        return Math.max(0, Math.min(255, (int) Math.round(start + (end - start) * progress)));
    }
}
