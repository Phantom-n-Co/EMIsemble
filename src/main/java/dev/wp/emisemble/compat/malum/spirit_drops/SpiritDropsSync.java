package dev.wp.emisemble.compat.malum.spirit_drops;

import com.sammy.malum.core.listeners.SpiritDataReloadListener;
import com.sammy.malum.core.systems.spirit.EntitySpiritDropData;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.network.Payloads;
import dev.wp.emisemble.network.SpiritDropsPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Server side of the spirit drop sync: the table goes out on join and on every data pack reload. */
public final class SpiritDropsSync {
    private SpiritDropsSync() {}

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        // Checked before any Malum type is touched, so a server without the host never loads one.
        if (!Emisemble.isAvailable("malum")) {
            return;
        }
        SpiritDropsPayload payload = payload();
        if (payload.entries().isEmpty()) {
            return;
        }
        Payloads.sync(event, payload);
    }

    private static SpiritDropsPayload payload() {
        List<SpiritDropsPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<ResourceLocation, EntitySpiritDropData> entry : SpiritDataReloadListener.SPIRIT_DATA.entrySet()) {
            List<ItemStack> drops = drops(entry.getValue());
            if (!drops.isEmpty()) {
                entries.add(new SpiritDropsPayload.Entry(entry.getKey(), drops));
            }
        }
        return new SpiritDropsPayload(entries);
    }

    static List<ItemStack> drops(EntitySpiritDropData data) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : data.getSpiritStacks()) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}
