package com.example.signleakshield.mixin;

import com.example.signleakshield.ExploitState;
import com.example.signleakshield.SignLeakShieldTraceLog;
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

import java.util.Arrays;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @ModifyVariable(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> signleakshield$rewriteOutgoing(Packet<?> packet) {
        if (packet instanceof UpdateSignC2SPacket signPacket) {
            ExploitState.ForcedOpenContext forcedOpen = ExploitState.pendingForcedOpen;
            ExploitState.CapturedSignData captured = ExploitState.SIGNS.get(signPacket.getPos());
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet observed: pos=%s front=%s forcedOpen=%s captured=%s packetText=%s",
                signPacket.getPos(),
                signPacket.isFront(),
                forcedOpen == null ? "null" : forcedOpen.pos() + "/front=" + forcedOpen.front() + "/ageMs=" + (System.currentTimeMillis() - forcedOpen.timeMs()),
                captured == null ? "null" : "suspicious=" + captured.isSuspicious(),
                Arrays.toString(signPacket.getText())
            );
        }

        if (!(packet instanceof UpdateSignC2SPacket signPacket)) {
            return packet;
        }

        ExploitState.ForcedOpenContext forcedOpen = ExploitState.pendingForcedOpen;
        if (forcedOpen == null) {
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet not rewritten: no pending forced-open context for pos=%s front=%s",
                signPacket.getPos(),
                signPacket.isFront()
            );
            return packet;
        }

        BlockPos pos = signPacket.getPos();
        if (!forcedOpen.pos().equals(pos)) {
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet not rewritten: pos mismatch packet=%s forcedOpen=%s",
                pos,
                forcedOpen.pos()
            );
            return packet;
        }

        if (forcedOpen.front() != signPacket.isFront()) {
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet not rewritten: front mismatch packet=%s forcedOpen=%s",
                signPacket.isFront(),
                forcedOpen.front()
            );
            return packet;
        }

        long ageMs = System.currentTimeMillis() - forcedOpen.timeMs();
        if (ageMs < 0L || ageMs > 5000L) {
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet not rewritten: forced-open age out of range pos=%s front=%s ageMs=%s",
                pos,
                signPacket.isFront(),
                ageMs
            );
            return packet;
        }

        ExploitState.CapturedSignData captured = ExploitState.SIGNS.get(pos);
        if (captured == null || !captured.isSuspicious()) {
            SignLeakShieldTraceLog.info(
                "Outgoing sign packet allowed: captured sign missing or not blacklisted pos=%s front=%s capturedPresent=%s",
                pos,
                signPacket.isFront(),
                captured != null
            );
            return packet;
        }

        Text[] textLines;
        textLines = signPacket.isFront() ? captured.getFront() : captured.getBack();
        String[] originalResponse = signPacket.getText();

        String line1 = TextSanitizer.sanitize(textLines[0]);
        String line2 = TextSanitizer.sanitize(textLines[1]);
        String line3 = TextSanitizer.sanitize(textLines[2]);
        String line4 = TextSanitizer.sanitize(textLines[3]);
        String[] returnedResponse = new String[] { line1, line2, line3, line4 };

        SignLeakShieldTraceLog.info(
            "Blocked forced sign translation event at %s front=%s: receivedSign=%s, unmodifiedResponse=%s, returned=%s",
            pos,
            signPacket.isFront(),
            describeTextLines(textLines),
            Arrays.toString(originalResponse),
            Arrays.toString(returnedResponse)
        );

        ExploitState.clearForcedOpen();
        return new UpdateSignC2SPacket(pos, signPacket.isFront(), line1, line2, line3, line4);
    }

    private static String describeTextLines(Text[] lines) {
        String[] values = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            values[i] = lines[i] != null ? lines[i].getString() : "";
        }

        return Arrays.toString(values);
    }
}
