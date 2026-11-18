package dev.wp.emisemble.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.blueprint.BlueprintCategory;
import dev.wp.emisemble.compat.immersiveengineering.crusher.CrusherCategory;
import dev.wp.emisemble.compat.immersiveengineering.fermenter.FermenterCategory;
import dev.wp.emisemble.compat.immersiveengineering.metalpress.MetalPressCategory;
import dev.wp.emisemble.compat.immersiveengineering.sawmill.SawmillCategory;
import dev.wp.emisemble.compat.immersiveengineering.squeezer.SqueezerCategory;
import dev.wp.emisemble.compat.immersiveengineering.alloy.AlloySmelterCategory;
import dev.wp.emisemble.compat.immersiveengineering.arcfurnace.ArcFurnaceCategory;
import dev.wp.emisemble.compat.immersiveengineering.blastfurnace.BlastFurnaceCategory;
import dev.wp.emisemble.compat.immersiveengineering.cokeoven.CokeOvenCategory;
import dev.wp.emisemble.compat.immersiveengineering.bottling.BottlingCategory;
import dev.wp.emisemble.compat.immersiveengineering.cloche.ClocheCategory;
import dev.wp.emisemble.compat.immersiveengineering.mixer.MixerCategory;
import dev.wp.emisemble.compat.immersiveengineering.refinery.RefineryCategory;
import dev.wp.emisemble.compat.immersiveengineering.transfer.CraftingTableHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * IE's categories plus the two registrations a category port leaves out: item subtypes and the crafting table
 * handler.
 */
public final class ImmersiveEngineeringCompat {
    private ImmersiveEngineeringCompat() {}

    static void register(EmiRegistry registry) {
        registerSubtypes(registry);

        int added = CrusherCategory.register(registry)
                + SawmillCategory.register(registry)
                + SqueezerCategory.register(registry)
                + FermenterCategory.register(registry)
                + MetalPressCategory.register(registry)
                + BlueprintCategory.register(registry)
                + AlloySmelterCategory.register(registry)
                + CokeOvenCategory.register(registry)
                + BlastFurnaceCategory.register(registry)
                + ArcFurnaceCategory.register(registry)
                + MixerCategory.register(registry)
                + RefineryCategory.register(registry)
                + BottlingCategory.register(registry)
                + ClocheCategory.register(registry);
        int handlers = CraftingTableHandler.register(registry);
        Emisemble.LOGGER.info("immersiveengineering: {} recipes registered, {} transfer handlers", added, handlers);
    }

/**
 * The two nbt-heavy items the host splits subtypes for and the blueprint work does not cover: a potion bucket's
 * Must run during plugin registration: EMI resets the comparison defaults just before it and bakes after.
 */
    private static void registerSubtypes(EmiRegistry registry) {
        try {
            registry.setDefaultComparison(IEItems.Misc.POTION_BUCKET.get(), Comparison.compareData(
                    stack -> stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()));
            for (var type : ConveyorHandler.getConveyorTypes()) {
                Item item = ConveyorHandler.getBlock(type).asItem();
                registry.setDefaultComparison(item,
                        Comparison.compareData(stack -> stack.get(IEDataComponents.DEFAULT_COVER.get())));
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not set the potion bucket and conveyor comparisons", t);
        }
    }
}
