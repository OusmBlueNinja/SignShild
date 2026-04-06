package com.example.signleakshield.mixin;

import com.example.signleakshield.ExploitState;
import com.example.signleakshield.TextSanitizer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @ModifyVariable(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> signleakshield$rewriteOutgoing(Packet<?> packet) {
        if (!(packet instanceof UpdateSignC2SPacket signPacket)) {
            return packet;
        }

        ExploitState.ForcedOpenContext forcedOpen = ExploitState.pendingForcedOpen;
        if (forcedOpen == null) {
            return packet;
        }

        BlockPos pos = signPacket.getPos();
        if (!forcedOpen.pos().equals(pos)) {
            return packet;
        }

        if (forcedOpen.front() != signPacket.isFront()) {
            return packet;
        }

        long ageMs = System.currentTimeMillis() - forcedOpen.timeMs();
        if (ageMs < 0L || ageMs > 5000L) {
            return packet;
        }

        ExploitState.CapturedSignData captured = ExploitState.SIGNS.get(pos);
        if (captured == null || !captured.isSuspicious()) {
            return packet;
        }

        Text[] lines = signPacket.isFront() ? captured.getFront() : captured.getBack();
        String line1 = TextSanitizer.sanitize(lines[0]);
        String line2 = TextSanitizer.sanitize(lines[1]);
        String line3 = TextSanitizer.sanitize(lines[2]);
        String line4 = TextSanitizer.sanitize(lines[3]);

        ExploitState.clearForcedOpen();
        return new UpdateSignC2SPacket(pos, signPacket.isFront(), line1, line2, line3, line4);
    }
}
