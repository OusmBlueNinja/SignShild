package com.example.signleakshield;

import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public final class TextSanitizer {
    private static final String[] VANILLA_TRANSLATION_PREFIXES = {
        "advancement.",
        "attribute.",
        "block.minecraft.",
        "chat.",
        "commands.",
        "death.",
        "effect.minecraft.",
        "enchantment.minecraft.",
        "entity.minecraft.",
        "gui.",
        "item.minecraft.",
        "itemGroup.",
        "key.",
        "menu.",
        "options.",
        "pack.",
        "recipe.",
        "resourcePack.",
        "stat.minecraft.",
        "subtitles.",
        "tutorial."
    };

    private TextSanitizer() {
    }

    public static boolean isSuspicious(Text text) {
        if (text == null) {
            return false;
        }

        TextContent content = text.getContent();

        if (content instanceof TranslatableTextContent translatable) {
            return !isVanillaTranslationKey(translatable.getKey());
        }

        if (content instanceof KeybindTextContent keybind) {
            return !isVanillaKeybindKey(keybind.getKey());
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
            out.append(keybind.getKey());
        } else {
            out.append(text.getString());
        }

        for (Text sibling : text.getSiblings()) {
            append(sibling, out);
        }
    }

    private static boolean isVanillaTranslationKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        for (String prefix : VANILLA_TRANSLATION_PREFIXES) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }

        return key.contains(".minecraft.");
    }

    private static boolean isVanillaKeybindKey(String key) {
        if (key == null || !key.startsWith("key.")) {
            return false;
        }

        return key.indexOf('.', 4) < 0;
    }
}
