package dev.wp.emisemble.compat.thaumaturge.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Function;
import java.util.function.Predicate;

/** The two dust-trigger categories: one recipe type, split by recipe class as the host's JEI plugin splits it. */
public final class DustTriggerCompat {
    public static final ResourceLocation DUST_TRIGGER_ID = EmiRecipes.categoryId(TTRecipeTypes.DUST_TRIGGER.get());
    /** Synthetic: the multiblock type exists only inside the host's JEI plugin, so nothing holds its registry id. */
    public static final ResourceLocation MULTIBLOCK_ID =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "multiblock_dust_trigger");

    public static final EmiRecipeCategory DUST_TRIGGER = new TranslatedCategory(
            DUST_TRIGGER_ID, salisMundus(), "jei.thaumaturge.category.dust_trigger");
    public static final EmiRecipeCategory MULTIBLOCK = new TranslatedCategory(
            MULTIBLOCK_ID, salisMundus(), "jei.thaumaturge.category.multiblock_dust_trigger");

    private DustTriggerCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(DUST_TRIGGER);
        registry.addWorkstation(DUST_TRIGGER, salisMundus());
        int dustTrigger = addAll(registry,
                recipe -> recipe instanceof DustTriggerSimpleRecipe || recipe instanceof DustTriggerTagRecipe,
                DustTriggerEmiRecipe::new);

        registry.addCategory(MULTIBLOCK);
        registry.addWorkstation(MULTIBLOCK, salisMundus());
        registry.addWorkstation(MULTIBLOCK, EmiStack.of(TTItems.ADVANCED_ALCHEMICAL_FURNACE.get()));
        int multiblock = addAll(registry, recipe -> recipe instanceof DustTriggerMultiblockRecipe,
                MultiblockDustTriggerEmiRecipe::new);

        Emisemble.LOGGER.info("thaumaturge: {} dust trigger and {} multiblock dust trigger recipes registered",
                dustTrigger, multiblock);
        return dustTrigger + multiblock;
    }

    static EmiStack salisMundus() {
        return EmiStack.of(TTItems.SALIS_MUNDUS.get());
    }

    /** The item a trigger produces; a blueprint carries the structure only, never the reward. */
    static ItemStack result(DustTrigger recipe) {
        if (recipe instanceof DustTriggerSimpleRecipe simple) {
            return simple.result();
        }
        if (recipe instanceof DustTriggerTagRecipe tag) {
            return tag.result();
        }
        if (recipe instanceof DustTriggerMultiblockRecipe multi) {
            return multi.result();
        }
        return ItemStack.EMPTY;
    }

    private static int addAll(EmiRegistry registry, Predicate<DustTrigger> filter,
                              Function<RecipeHolder<DustTrigger>, EmiRecipe> page) {
        int added = 0;
        for (RecipeHolder<DustTrigger> holder : registry.getRecipeManager().getAllRecipesFor(TTRecipeTypes.DUST_TRIGGER.get())) {
            if (!filter.test(holder.value())) {
                continue;
            }
            try {
                registry.addRecipe(page.apply(holder));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping dust trigger recipe {}", holder.id(), t);
            }
        }
        return added;
    }
}
