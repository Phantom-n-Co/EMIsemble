package dev.wp.emisemble.compat.cataclysm;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.compat.cataclysm.altar.AltarOfAmethystCategory;
import dev.wp.emisemble.compat.cataclysm.transfer.WeaponFusionHandler;
import dev.wp.emisemble.compat.cataclysm.weapon_infusion.WeaponInfusionCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** L_Ender's Cataclysm: the two categories its JEI plugin registers, both under the {@code cataclysm} namespace. */
public final class CataclysmCompat {
    public static final ResourceLocation FUSION_ANVIL = ResourceLocation.fromNamespaceAndPath("cataclysm", "mechanical_fusion_anvil");
    public static final ResourceLocation ALTAR = ResourceLocation.fromNamespaceAndPath("cataclysm", "altar_of_amethyst");

    private CataclysmCompat() {}

    public static void register(EmiRegistry registry) {
        WeaponInfusionCategory.register(registry);
        AltarOfAmethystCategory.register(registry);
        WeaponFusionHandler.register(registry);
    }

    public static EmiStack stack(ResourceLocation item) {
        return EmiStack.of(hostItem(item));
    }

    public static MutableComponent name(ResourceLocation item) {
        return Component.translatable(hostItem(item).getDescriptionId());
    }

    // Resolved by id rather than through the host's block registry class, which is Registrate-based and jarjar'd.
    private static Item hostItem(ResourceLocation item) {
        return BuiltInRegistries.ITEM.get(item);
    }
}
