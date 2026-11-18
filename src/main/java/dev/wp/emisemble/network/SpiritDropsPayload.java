package dev.wp.emisemble.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Malum's spirit drop table lives in a server-side reload listener, so a client on its own never has it. This ships the
 * same entries the server would draw from.
 */
public record SpiritDropsPayload(List<Entry> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpiritDropsPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emisemble", "spirit_drops"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritDropsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entries().size());
                for (Entry entry : payload.entries()) {
                    buf.writeResourceLocation(entry.entity());
                    buf.writeVarInt(entry.drops().size());
                    for (ItemStack stack : entry.drops()) {
                        ItemStack.STREAM_CODEC.encode(buf, stack);
                    }
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<Entry> entries = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ResourceLocation entity = buf.readResourceLocation();
                    int count = buf.readVarInt();
                    List<ItemStack> drops = new ArrayList<>(count);
                    for (int j = 0; j < count; j++) {
                        drops.add(ItemStack.STREAM_CODEC.decode(buf));
                    }
                    entries.add(new Entry(entity, List.copyOf(drops)));
                }
                return new SpiritDropsPayload(List.copyOf(entries));
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(ResourceLocation entity, List<ItemStack> drops) {}
}
