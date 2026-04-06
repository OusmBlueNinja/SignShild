package com.example.signleakshield;

import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public final class TextSanitizer {
    private static final String[] SUSPICIOUS_KEY_PARTS = {
        "gey.glazed.",
        "key.meteor-client.",
        "meteor-client",
        "meteorline",
        "meteorclient",
        "metiorclient",
        "meteordevelopment.meteorclient",
        "itemscroller",
        "moremousetweaks",
        "invmove",
        "autototem",
        "smartoffhand",
        "freecam",
        "jsmacros",
        "inventoryprofiles",
        "tweakeroo",
        "soundboard",
        "accurateblockplacement"
    };

    private TextSanitizer() {
    }

    public static boolean isSuspicious(Text text) {
        if (text == null) {
            return false;
        }

        TextContent content = text.getContent();

        if (content instanceof TranslatableTextContent translatable) {
            if (isBlacklistedKey(translatable.getKey())) {
                return true;
            }

            for (Object arg : translatable.getArgs()) {
                if (isSuspiciousArgument(arg)) {
                    return true;
                }
            }

            return false;
        }

        if (content instanceof KeybindTextContent keybind) {
            String key = keybind.getKey();
            return key != null && !VanillaKeybinds.isVanillaKey(key);
        }

        for (Text sibling : text.getSiblings()) {
            if (isSuspicious(sibling)) {
                return true;
            }
        }

        return false;
    }

    public static String sanitize(Text text) {
        StringBuilder out = new StringBuilder();
        append(text, out);
        return out.toString();
    }

    private static void append(Text text, StringBuilder out) {
        if (text == null) {
            return;
        }

        TextContent content = text.getContent();

        if (content instanceof PlainTextContent plain) {
            out.append(plain.string());
        } else if (content instanceof TranslatableTextContent translatable) {
            String fallback = translatable.getFallback();
            if (fallback != null && !fallback.isEmpty()) {
                out.append(fallback);
            } else {
                out.append(translatable.getKey());
            }
        } else if (content instanceof KeybindTextContent keybind) {
            String key = keybind.getKey();
            out.append(key != null ? key : "");
        } else {
            out.append(text.getString());
        }

        for (Text sibling : text.getSiblings()) {
            append(sibling, out);
        }
    }

    private static boolean isBlacklistedKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        String lower = key.toLowerCase();
        for (String part : SUSPICIOUS_KEY_PARTS) {
            if (lower.contains(part)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSuspiciousArgument(Object arg) {
        if (arg == null) {
            return false;
        }

        if (arg instanceof Text text) {
            return isSuspicious(text);
        }

        if (arg instanceof KeybindTextContent keybind) {
            return !VanillaKeybinds.isVanillaKey(keybind.getKey());
        }

        if (arg instanceof TranslatableTextContent translatable) {
            if (isBlacklistedKey(translatable.getKey())) {
                return true;
            }

            for (Object nested : translatable.getArgs()) {
                if (isSuspiciousArgument(nested)) {
                    return true;
                }
            }
        }

        if (arg instanceof CharSequence sequence) {
            return isBlacklistedKey(sequence.toString());
        }

        return false;
    }
}
