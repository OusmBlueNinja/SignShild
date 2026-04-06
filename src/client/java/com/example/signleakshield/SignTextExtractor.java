package com.example.signleakshield;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public final class SignTextExtractor {
    private SignTextExtractor() {
    }

    public static ExploitState.CapturedSignData fromNbt(NbtCompound nbt) {
        Text[] front = readSide(nbt, "front_text");
        Text[] back = readSide(nbt, "back_text");
        boolean suspicious = containsSuspicious(front) || containsSuspicious(back);
        return new ExploitState.CapturedSignData(front, back, suspicious);
    }

    private static boolean containsSuspicious(Text[] lines) {
        for (Text line : lines) {
            if (TextSanitizer.isSuspicious(line)) {
                return true;
            }
        }
        return false;
    }

    private static Text[] readSide(NbtCompound root, String sideKey) {
        Text[] lines = new Text[] { Text.empty(), Text.empty(), Text.empty(), Text.empty() };

        if (!root.contains(sideKey, NbtElement.COMPOUND_TYPE)) {
            return lines;
        }

        NbtCompound side = root.getCompound(sideKey);
        if (!side.contains("messages", NbtElement.LIST_TYPE)) {
            return lines;
        }

        NbtList messages = side.getList("messages", NbtElement.STRING_TYPE);
        for (int i = 0; i < 4 && i < messages.size(); i++) {
            String raw = messages.getString(i);
            try {
                Text parsed = Text.Serialization.fromJson(raw, null);
                lines[i] = parsed != null ? parsed : Text.literal(raw);
            } catch (Throwable ignored) {
                lines[i] = Text.literal(raw);
            }
        }

        return lines;
    }
}
