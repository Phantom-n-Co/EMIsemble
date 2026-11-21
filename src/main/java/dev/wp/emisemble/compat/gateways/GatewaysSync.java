package dev.wp.emisemble.compat.gateways;

import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.network.GatewaysPayload;
import dev.wp.emisemble.network.Payloads;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The pearls come from the host's DynamicRegistry, which a client receives after EMI has already registered its pages,
 * so the server sends them on datapack sync and the client builds the page from that copy.
 */
public final class GatewaysSync {
    private static volatile List<ItemStack> synced = List.of();

    private GatewaysSync() {}

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        // Checked before any host type is touched, so a server without Gateways never loads one.
        if (!Emisemble.isAvailable("gateways")) {
            return;
        }
        List<ItemStack> pearls = local();
        if (pearls.isEmpty()) {
            return;
        }
        GatewaysPayload payload = new GatewaysPayload(pearls);
        Payloads.sync(event, payload);
    }

    /** The host registry when it is already populated, the server's copy otherwise. */
    public static List<ItemStack> pearls() {
        List<ItemStack> local = local();
        List<ItemStack> fromServer = synced;
        return local.size() >= fromServer.size() ? local : fromServer;
    }

    public static void accept(GatewaysPayload payload) {
        synced = payload.pearls();
    }

    private static List<ItemStack> local() {
        List<ItemStack> pearls = new ArrayList<>();
        try {
            GatePearlItem.generateGatePearlStacks(pearls::add);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("gateways: could not enumerate the gate pearls", t);
            pearls.clear();
        }
        return pearls;
    }
}
