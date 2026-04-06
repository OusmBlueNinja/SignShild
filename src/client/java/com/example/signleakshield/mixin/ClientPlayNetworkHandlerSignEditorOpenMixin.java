package com.example.signleakshield.mixin;

import com.example.signleakshield.ExploitState;
import com.example.signleakshield.SignLeakShieldTraceLog;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerSignEditorOpenMixin {
    @Inject(method = "onSignEditorOpen", at = @At("HEAD"))
    private void signleakshield$rememberForcedOpen(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        SignLeakShieldTraceLog.info(
            "Sign editor open received: pos=%s front=%s",
            packet.getPos(),
            packet.isFront()
        );
        ExploitState.rememberForcedOpen(packet.getPos(), packet.isFront());
    }
}
