package dev.wp.emisemble.compat.thaumaturge.subtypes;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.wp.emisemble.Emisemble;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/** Ten items whose identity is a data component, so EMI's component-blind default would merge their variants. */
public final class SubtypeComparisons {
    private SubtypeComparisons() {}

    public static void register(EmiRegistry registry) {
        // Must run during plugin registration: the comparison defaults are reset just before it and baked after.
        apply(registry, TTItems.SALIS_MUNDUS.get(), TTDataComponents.ASPECTS.get());
        apply(registry, TTItems.PHIAL.get(), TTDataComponents.ASPECTS.get());
        apply(registry, TTItems.LABEL.get(), TTDataComponents.ASPECT_FILTER.get());
        apply(registry, TTItems.TUBE_FILTER.get(), TTDataComponents.ASPECT_FILTER.get());
        apply(registry, TTItems.ESSENTIA_CRYSTAL.get(), TTDataComponents.CRYSTAL_ASPECT.get());
        apply(registry, TTItems.CELESTIAL_NOTES.asItem(), TTDataComponents.CELESTIAL_BODY.get());
        apply(registry, TTItems.RESEARCH_NOTE.get(), TTDataComponents.RESEARCH_NOTE.get());
        apply(registry, TTItems.WAND.get(), TTDataComponents.WAND_PARTS.get());
        apply(registry, TTItems.VERDANT_CHARM.get(), TTDataComponents.VERDANT_TYPE.get());
        apply(registry, TTItems.GOLEM_PLACER.get(), TTDataComponents.GOLEM_PROPERTIES.get());
    }

    // One component per item, as in JEI: the whole patch would also split on vis and charge, which mutate in play.
    private static <T> void apply(EmiRegistry registry, Item item, DataComponentType<T> type) {
        try {
            registry.setDefaultComparison(item, Comparison.compareData(stack -> stack.get(type)));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("thaumaturge: could not set the component comparison for {}",
                    BuiltInRegistries.ITEM.getKey(item), t);
        }
    }
}
