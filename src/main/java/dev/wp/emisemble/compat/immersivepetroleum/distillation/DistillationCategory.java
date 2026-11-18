package dev.wp.emisemble.compat.immersivepetroleum.distillation;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersivepetroleum.ImmersivePetroleumCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.resources.ResourceLocation;

/** {@code immersivepetroleum:distillation}. */
public final class DistillationCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "textures/gui/jei/distillationtower.png");
    static final int WIDTH = 120;
    static final int HEIGHT = 77;

    private static final EmiStack ICON = EmiStack.of(IPContent.Multiblock.DISTILLATIONTOWER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ImmersivePetroleumCompat.categoryId("distillation"), ICON, "block.immersivepetroleum.distillation_tower");

    private DistillationCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersivepetroleum: could not register the distillation category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IPRecipeTypes.DISTILLATION.get(),
                holder -> new DistillationEmiRecipe(CATEGORY, holder.id(), holder.value()));
        Emisemble.LOGGER.info("immersivepetroleum: {} distillation recipes", added);
        return added;
    }
}
