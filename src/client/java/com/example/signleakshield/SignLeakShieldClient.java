package com.example.signleakshield;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SignLeakShieldClient implements ClientModInitializer {
    public static final String MOD_ID = "signleakshield";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        SignLeakShieldTraceLog.reset();
        SignLeakShieldTraceLog.info("Sign Leak Shield initialized");
    }
}
