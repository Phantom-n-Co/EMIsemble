package dev.wp.emisemble.compat.immersiveengineering.transfer;

import dev.wp.emisemble.Emisemble;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorkbenchCraftPayload(int containerId, int slot, int destination, int amount)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorkbenchCraftPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Emisemble.MOD_ID, "workbench_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchCraftPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, WorkbenchCraftPayload::containerId,
                    ByteBufCodecs.VAR_INT, WorkbenchCraftPayload::slot,
                    ByteBufCodecs.VAR_INT, WorkbenchCraftPayload::destination,
                    ByteBufCodecs.VAR_INT, WorkbenchCraftPayload::amount,
                    WorkbenchCraftPayload::new);

    // Gated here rather than at registration: a server may run this mod without the host installed.
    public static void handle(WorkbenchCraftPayload payload, IPayloadContext context) {
        if (Emisemble.isAvailable("immersiveengineering") && context.player() instanceof ServerPlayer player) {
            WorkbenchCraft.apply(player, payload);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
