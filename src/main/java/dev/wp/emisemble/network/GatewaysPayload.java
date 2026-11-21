package dev.wp.emisemble.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The gate pearls the server holds, already stamped with their gate, for a client whose own registry has not synced. */
public record GatewaysPayload(List<ItemStack> pearls) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GatewaysPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emisemble", "gateways"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GatewaysPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.pearls().size());
                for (ItemStack pearl : payload.pearls()) {
                    ItemStack.STREAM_CODEC.encode(buf, pearl);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<ItemStack> pearls = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    pearls.add(ItemStack.STREAM_CODEC.decode(buf));
                }
                return new GatewaysPayload(List.copyOf(pearls));
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
