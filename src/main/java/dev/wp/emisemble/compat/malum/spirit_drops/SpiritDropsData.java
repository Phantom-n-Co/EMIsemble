package dev.wp.emisemble.compat.malum.spirit_drops;

import com.sammy.malum.core.listeners.SpiritDataReloadListener;
import com.sammy.malum.core.systems.spirit.EntitySpiritDropData;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.network.SpiritDropsPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Malum fills its spirit data from a server-side reload listener, so an integrated server supplies it directly and a
 * remote one has to send it. Until a packet arrives the map is empty and no pages are registered.
 */
public final class SpiritDropsData {
    private static final Map<ResourceLocation, List<ItemStack>> SYNCED = new ConcurrentHashMap<>();

    private SpiritDropsData() {}

    /** Entity → drops; the local map wins where it exists, i.e. every integrated-server session. */
    public static Map<ResourceLocation, List<ItemStack>> drops() {
        Map<ResourceLocation, List<ItemStack>> local = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, EntitySpiritDropData> entry : SpiritDataReloadListener.SPIRIT_DATA.entrySet()) {
            List<ItemStack> stacks = SpiritDropsSync.drops(entry.getValue());
            if (!stacks.isEmpty()) {
                local.put(entry.getKey(), stacks);
            }
        }
        return local.isEmpty() ? Map.copyOf(SYNCED) : local;
    }

    /** Stores what the server sent and rebakes EMI when the category is already registered. */
    public static void accept(SpiritDropsPayload payload) {
        try {
            if (!SpiritDataReloadListener.SPIRIT_DATA.isEmpty()) {
                return;
            }
            for (SpiritDropsPayload.Entry entry : payload.entries()) {
                SYNCED.put(entry.entity(), List.copyOf(entry.drops()));
            }
            EmiRecipeManager manager = EmiApi.getRecipeManager();
            if (manager != null && !manager.getCategories().isEmpty()) {
                EmiReloadManager.reload();
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("malum: could not apply the synced spirit drop data", t);
        }
    }
}
