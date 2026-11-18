package dev.wp.emisemble.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Myths &amp; Legends research definitions live in a server-side reload listener and are never sent to a client. This
 * carries what a page draws: the labels, the ingredient refs, and the reward stacks already built.
 */
public record ResearchPayload(List<Entry> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResearchPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("emisemble", "research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entries().size());
                for (Entry entry : payload.entries()) {
                    writeEntry(buf, entry);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<Entry> entries = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    entries.add(readEntry(buf));
                }
                return new ResearchPayload(List.copyOf(entries));
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void writeEntry(RegistryFriendlyByteBuf buf, Entry entry) {
        buf.writeResourceLocation(entry.id());
        buf.writeUtf(entry.name());
        buf.writeVarInt(entry.requiredRank());
        buf.writeVarInt(entry.researchTime());
        buf.writeBoolean(entry.repeatable());
        buf.writeVarInt(entry.ingredients().size());
        for (Input input : entry.ingredients()) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, input.display());
            buf.writeBoolean(input.tag() != null);
            if (input.tag() != null) {
                buf.writeResourceLocation(input.tag());
            }
            buf.writeVarInt(input.count());
        }
        buf.writeVarInt(entry.rewards().size());
        for (ItemStack reward : entry.rewards()) {
            ItemStack.STREAM_CODEC.encode(buf, reward);
        }
        for (int count : entry.rewardCounts()) {
            buf.writeVarInt(count);
        }
    }

    private static Entry readEntry(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String name = buf.readUtf();
        int rank = buf.readVarInt();
        int time = buf.readVarInt();
        boolean repeatable = buf.readBoolean();
        int ingredientCount = buf.readVarInt();
        List<Input> ingredients = new ArrayList<>(ingredientCount);
        for (int i = 0; i < ingredientCount; i++) {
            ItemStack display = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            ResourceLocation tag = buf.readBoolean() ? buf.readResourceLocation() : null;
            ingredients.add(new Input(display, tag, buf.readVarInt()));
        }
        int rewardCount = buf.readVarInt();
        List<ItemStack> rewards = new ArrayList<>(rewardCount);
        for (int i = 0; i < rewardCount; i++) {
            rewards.add(ItemStack.STREAM_CODEC.decode(buf));
        }
        List<Integer> counts = new ArrayList<>(rewardCount);
        for (int i = 0; i < rewardCount; i++) {
            counts.add(buf.readVarInt());
        }
        return new Entry(id, name, rank, time, repeatable, List.copyOf(ingredients), List.copyOf(rewards),
                List.copyOf(counts));
    }

    public record Entry(ResourceLocation id, String name, int requiredRank, int researchTime, boolean repeatable,
                        List<Input> ingredients, List<ItemStack> rewards, List<Integer> rewardCounts) {}

    /** A tag ingredient carries only its id; an item ingredient carries the host's own display stack. */
    public record Input(ItemStack display, ResourceLocation tag, int count) {}
}
