package com.example.signleakshield;

import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public final class TextSanitizer {
    private TextSanitizer() {
    }

    public static boolean isSuspicious(Text text) {
        if (text == null) {
            return false;
        }

        TextContent content = text.getContent();

        if (content instanceof TranslatableTextContent || content instanceof KeybindTextContent) {
            return true;
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
}
