package dev.wp.emisemble.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * A client without this mod must still be able to play on a server that has it, and NeoForge refuses a payload whose
 * channel the client never negotiated, so every sync skips the players that cannot receive it.
 */
public final class Payloads {
    private Payloads() {}

    public static void sync(OnDatapackSyncEvent event, CustomPacketPayload payload) {
        event.getRelevantPlayers().forEach(player -> {
            if (NetworkRegistry.hasChannel(player.connection, payload.type().id())) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        });
    }
}
