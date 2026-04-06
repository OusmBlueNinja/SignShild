package com.example.signleakshield;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class VanillaKeybinds {
    private static volatile Set<String> vanillaKeys;

    private VanillaKeybinds() {
    }

    public static boolean isVanillaKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        Set<String> keys = vanillaKeys;
        if (keys == null) {
            keys = loadVanillaKeys();
            if (keys != null) {
                vanillaKeys = keys;
            }
        }

        return keys != null && keys.contains(key);
    }

    private static Set<String> loadVanillaKeys() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return null;
        }

        Set<String> keys = new HashSet<>();

        for (Field field : GameOptions.class.getDeclaredFields()) {
            Class<?> type = field.getType();
            if (!KeyBinding.class.isAssignableFrom(type) && !KeyBinding[].class.isAssignableFrom(type)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(client.options);
                collectKeyBindings(keys, value);
            } catch (IllegalAccessException | RuntimeException ignored) {
            }
        }

        return Collections.unmodifiableSet(keys);
    }

    private static void collectKeyBindings(Set<String> keys, Object value) {
        if (value instanceof KeyBinding keyBinding) {
            String translationKey = keyBinding.getTranslationKey();
            if (translationKey != null && !translationKey.isEmpty()) {
                keys.add(translationKey);
            }
            return;
        }

        if (value instanceof KeyBinding[] keyBindings) {
            for (KeyBinding keyBinding : keyBindings) {
                if (keyBinding == null) {
                    continue;
                }

                String translationKey = keyBinding.getTranslationKey();
                if (translationKey != null && !translationKey.isEmpty()) {
                    keys.add(translationKey);
                }
            }
        }
    }
}
