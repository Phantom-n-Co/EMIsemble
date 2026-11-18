package dev.wp.emisemble.compat.hexerei.fluidmixing;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.joefoxe.hexerei.block.ModBlocks;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe;
import net.joefoxe.hexerei.fluid.PotionMixingRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

/** {@code hexerei:fluid_mixing} and {@code hexerei:potion_mixing}: two categories off one plugin method. */
public final class FluidMixingCompat {
    public static final EmiRecipeCategory FLUID_MIXING = new TranslatedCategory(EmiRecipes.categoryId(FluidMixingRecipe.Type.INSTANCE), workstation(), "gui.jei.category.fluid_mixing");

    public static final EmiRecipeCategory POTION_MIXING = new TranslatedCategory(ResourceLocation.fromNamespaceAndPath("hexerei", "potion_mixing"), workstation(), "gui.jei.category.potion_mixing");

    private FluidMixingCompat() {}

    public static int register(EmiRegistry registry) {
        EmiStack cauldron = workstation();
        int fluid = registerFluidMixing(registry, cauldron);
        int potion = registerPotionMixing(registry, cauldron);
        Emisemble.LOGGER.info("hexerei: {} fluid mixing and {} potion mixing recipes", fluid, potion);
        return fluid + potion;
    }

    private static int registerFluidMixing(EmiRegistry registry, EmiStack cauldron) {
        addCategory(registry, FLUID_MIXING, cauldron);
        try {
            return EmiRecipes.addAll(registry, FluidMixingRecipe.Type.INSTANCE,
                    holder -> new FluidMixingEmiRecipe(FLUID_MIXING, holder.id(), holder.value()));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Could not read hexerei fluid mixing recipes", t);
            return 0;
        }
    }

    private static int registerPotionMixing(EmiRegistry registry, EmiStack cauldron) {
        addCategory(registry, POTION_MIXING, cauldron);
        List<FluidMixingRecipe> pages;
        try {
            pages = potionRecipes();
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Could not build hexerei potion mixing recipes", t);
            return 0;
        }

        int added = 0;
        for (int i = 0; i < pages.size(); i++) {
            try {
                FluidMixingRecipe recipe = pages.get(i);
                if (recipe != null
                        && EmiRecipes.add(registry, new FluidMixingEmiRecipe(POTION_MIXING, syntheticId(i), recipe))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping hexerei potion mixing page {}", i, t);
            }
        }
        return added;
    }

    private static List<FluidMixingRecipe> potionRecipes() {
        List<FluidMixingRecipe> recipes = PotionMixingRecipes.ALL;
        if (recipes != null && !recipes.isEmpty()) {
            return recipes;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("hexerei potion mixing needs a client level; none yet, skipping");
            return List.of();
        }
        recipes = PotionMixingRecipes.createRecipes(level.potionBrewing());
        PotionMixingRecipes.ALL = recipes;
        return recipes;
    }

    private static void addCategory(EmiRegistry registry, EmiRecipeCategory category, EmiStack cauldron) {
        try {
            registry.addCategory(category);
            registry.addWorkstation(category, cauldron);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Could not register hexerei category {}", category.getId(), t);
        }
    }

    private static ResourceLocation syntheticId(int index) {
        return ResourceLocation.fromNamespaceAndPath("hexerei", "/potion_mixing/" + index);
    }

    private static EmiStack workstation() {
        return EmiStack.of(ModBlocks.MIXING_CAULDRON.get());
    }
}
