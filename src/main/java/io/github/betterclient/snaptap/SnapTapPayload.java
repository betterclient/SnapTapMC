package io.github.betterclient.snaptap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class SnapTapPayload implements CustomPayload {
    private static final Identifier ID = Identifier.of("snaptap", "update_status");
    public static CustomPayload.Id<SnapTapPayload> PAYLOAD_ID = new Id<>(ID);
    public boolean allowed = false;

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }

    public boolean allowed() {
        return allowed;
    }

    public static class Codec implements PacketCodec<PacketByteBuf, SnapTapPayload> {
        @Override
        public void encode(PacketByteBuf buf, SnapTapPayload value) {
            buf.writeBoolean(value.allowed());
        }

        @Override
        public SnapTapPayload decode(PacketByteBuf buf) {
            SnapTapPayload payload = new SnapTapPayload();
            payload.allowed = buf.readBoolean();
            return payload;
        }
    }
}
