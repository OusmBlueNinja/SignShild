package com.example.signleakshield.mixin;

import com.example.signleakshield.ExploitState;
import com.example.signleakshield.SignLeakShieldTraceLog;
import com.example.signleakshield.SignTextExtractor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerBlockEntityUpdateMixin {
    @Inject(method = "onBlockEntityUpdate(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;)V", at = @At("HEAD"))
    private void signleakshield$captureSign(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        if (packet.getNbt() == null) {
            return;
        }

        if (packet.getBlockEntityType() != BlockEntityType.SIGN && packet.getBlockEntityType() != BlockEntityType.HANGING_SIGN) {
            return;
        }

        BlockPos pos = packet.getPos();
        ExploitState.SIGNS.put(pos.toImmutable(), SignTextExtractor.fromNbt(packet.getNbt()));
    }
}
