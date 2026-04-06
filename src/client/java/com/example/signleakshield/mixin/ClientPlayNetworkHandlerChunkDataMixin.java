package com.example.signleakshield.mixin;

import com.example.signleakshield.ExploitState;
import com.example.signleakshield.SignTextExtractor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerChunkDataMixin {
    @Inject(method = "onChunkData", at = @At("HEAD"))
    private void signleakshield$captureChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        packet.getChunkData().getBlockEntities(packet.getChunkX(), packet.getChunkZ()).accept((localPos, type, nbt) -> {
            if (nbt == null) {
                return;
            }

            if (type != BlockEntityType.SIGN && type != BlockEntityType.HANGING_SIGN) {
                return;
            }

            BlockPos pos = localPos.toImmutable();
            ExploitState.SIGNS.put(pos, SignTextExtractor.fromNbt((NbtCompound) nbt));
        });
    }
}
