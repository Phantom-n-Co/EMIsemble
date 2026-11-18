package dev.wp.emisemble.compat.functionalstorage.dissolution;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import com.hrznstudio.titanium.module.BlockWithTile;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** {@code functionalstorage:dissolution}: what a compacting drawer turns into its compacted form. */
public final class DissolutionCategory {
    // The host's JEI uid, not the registry key of its recipe type (functionalstorage:custom_compacting).
    static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("functionalstorage", "dissolution");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("functionalstorage", "textures/block/compacting_drawer_front.png");
    // The host's page is a blank 64x64 rect that its draw() fills with this 16x16 face, i.e. the sprite at 4x.
    static final int SHEET = 16;
    static final int WIDTH = 64;
    static final int HEIGHT = 64;

    // The host titles the category with a literal and returns no icon, so this is the same text it renders.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(FunctionalStorage.COMPACTING_DRAWER.asItem()), Component.literal("Custom Compacting"));

    private DissolutionCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        for (BlockWithTile drawer : List.of(FunctionalStorage.COMPACTING_DRAWER,
                FunctionalStorage.FRAMED_COMPACTING_DRAWER, FunctionalStorage.SIMPLE_COMPACTING_DRAWER,
                FunctionalStorage.FRAMED_SIMPLE_COMPACTING_DRAWER)) {
            registry.addWorkstation(CATEGORY, EmiStack.of(drawer.asItem()));
        }
        int added = EmiRecipes.addAll(registry, type(),
                holder -> new DissolutionEmiRecipe(holder.id(), holder.value()));
        Emisemble.LOGGER.info("functionalstorage: {} dissolution recipes registered", added);
        return added;
    }

    @SuppressWarnings("unchecked")
    private static RecipeType<CustomCompactingRecipe> type() {
        return (RecipeType<CustomCompactingRecipe>) FunctionalStorage.CUSTOM_COMPACTING_RECIPE_TYPE.value();
    }
}
