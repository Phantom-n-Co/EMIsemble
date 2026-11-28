package dev.wp.emisemble.compat.modopedia;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import net.favouriteless.modopedia.api.registries.common.BookRegistry;
import net.favouriteless.modopedia.common.init.MDataComponents;
import net.favouriteless.modopedia.common.init.MItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Every Modopedia guide book is one item plus a book component, and only the component-less one reaches EMI. */
public final class ModopediaCompat {
    private ModopediaCompat() {}

    public static void register(EmiRegistry registry) {
        Item book = MItems.BOOK.get();
        DataComponentType<ResourceLocation> component = MDataComponents.BOOK.get();
        registry.setDefaultComparison(book, Comparison.compareComponents());

        List<EmiStack> indexed = EmiApi.getIndexStacks();
        int added = 0;
        int known = 0;
        for (ResourceLocation id : BookRegistry.get().getBookIds()) {
            ItemStack stack = new ItemStack(book);
            stack.set(component, id);
            EmiStack variant = EmiStack.of(stack);
            if (variant.isEmpty()) {
                continue;
            }
            if (indexed.stream().anyMatch(indexedStack -> indexedStack.isEqual(variant))) {
                known++;
                continue;
            }
            registry.addEmiStack(variant);
            added++;
        }

        Emisemble.LOGGER.info("modopedia: {} book variants added, {} already indexed", added, known);
    }
}
