package dev.wp.emisemble.compat.embers.dawnstone_anvil;

import java.util.ArrayList;
import java.util.List;

import com.rekindled.embers.datagen.EmbersItemTags;
import com.rekindled.embers.recipe.AnvilDisplayRecipe;
import com.rekindled.embers.recipe.IDawnstoneAnvilRecipe;
import com.rekindled.embers.util.Misc;

import dev.wp.emisemble.Emisemble;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The host builds a repair or breakdown list in one pass over every item, so a single item whose repair material throws
 * costs the whole recipe. This walks the same items and keeps everything before and after it.
 */
final class DawnstoneAnvilVariants {
    private DawnstoneAnvilVariants() {}

    static List<IDawnstoneAnvilRecipe> repair(ResourceLocation id) {
        List<IDawnstoneAnvilRecipe> variants = new ArrayList<>();
        forEachTool((tool, damaged, repair) ->
                variants.add(new AnvilDisplayRecipe(id, List.of(tool), List.of(damaged), repair)));
        return variants;
    }

    static List<IDawnstoneAnvilRecipe> breakdown(ResourceLocation id) {
        List<IDawnstoneAnvilRecipe> variants = new ArrayList<>();
        forEachTool((tool, damaged, repair) -> {
            ItemStack recovered = Misc.getPreferredItem(repair.getItems());
            if (!recovered.isEmpty()) {
                variants.add(new AnvilDisplayRecipe(id, List.of(recovered), List.of(damaged), Ingredient.EMPTY));
            }
        });
        return variants;
    }

    private static void forEachTool(Tool visitor) {
        Ingredient blacklist = Ingredient.of(EmbersItemTags.REPAIR_BLACKLIST);
        String firstFailure = null;
        int failures = 0;
        for (Holder<Item> holder : BuiltInRegistries.ITEM.asHolderIdMap()) {
            try {
                ItemStack tool = new ItemStack(holder.value());
                if (!tool.isDamageableItem() || blacklist.test(tool)) {
                    continue;
                }
                Ingredient repair = Misc.getRepairIngredient(holder.value());
                if (repair.isEmpty()) {
                    continue;
                }
                ItemStack damaged = tool.copy();
                damaged.setDamageValue(damaged.getMaxDamage() / 2);
                visitor.visit(tool, damaged, repair);
            } catch (Throwable t) {
                if (firstFailure == null) {
                    firstFailure = BuiltInRegistries.ITEM.getKey(holder.value()) + " (" + t + ")";
                }
                failures++;
            }
        }
        if (failures > 0) {
            Emisemble.LOGGER.warn("embers: rebuilt the anvil list without {} items whose repair material threw, first {}",
                    failures, firstFailure);
        }
    }

    private interface Tool {
        void visit(ItemStack tool, ItemStack damaged, Ingredient repair);
    }
}
