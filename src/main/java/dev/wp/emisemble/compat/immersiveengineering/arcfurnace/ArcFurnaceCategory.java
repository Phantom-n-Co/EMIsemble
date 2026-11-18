package dev.wp.emisemble.compat.immersiveengineering.arcfurnace;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.compat.immersiveengineering.IeRecipes;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

/** {@code immersiveengineering:arc_furnace} and the {@code arc_recycling} uid the host invents for its other half. */
public final class ArcFurnaceCategory {
    static final ResourceLocation ARROW_TEXTURE = IEApi.ieLoc("textures/gui/jei_elements.png");
    static final int ARROW_TEXTURE_SIZE = 128;
    static final int WIDTH = 148;
    static final int HEIGHT = 54;
    static final int ARROW_X = 40;
    static final int ARROW_Y = 10;
    static final int ARROW_U = 19;
    static final int ARROW_V = 4;
    static final int ARROW_WIDTH = 24;
    static final int ARROW_HEIGHT = 18;

    private static final ResourceLocation RECYCLE_TEXTURE = IEApi.ieLoc("textures/gui/recycle.png");
    private static final float BADGE_SCALE = 0.5F;
    private static final float BADGE_ICON_SIZE = 18.0F;
    private static final float CATEGORY_ICON_SCALE = 16.0F / BADGE_ICON_SIZE;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.ARC_FURNACE.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.ARC_FURNACE.get()), ICON, "block.immersiveengineering.arc_furnace");

    // The host's uid, spelled out: there is no such recipe type here, and the type's own id is taken by CATEGORY.
    public static final EmiRecipeCategory RECYCLING = new TranslatedCategory(
            IEApi.ieLoc("arc_recycling"), ArcFurnaceCategory::categoryIcon,
            Component.translatable("block.immersiveengineering.arc_furnace")
                    .append(Component.translatable(Lib.DESC + "jei.category.recycling")));

    private ArcFurnaceCategory() {}

    public static int register(EmiRegistry registry) {
        int plain = register(registry, CATEGORY, ArcFurnaceRecipe::isNotSpecialType);
        int recycling = register(registry, RECYCLING, recipe -> recipe.isSpecialType(ArcRecyclingRecipe.SPECIAL_TYPE));
        Emisemble.LOGGER.info("immersiveengineering: {} arc furnace recipes, {} of them recycling",
                plain + recycling, recycling);
        return plain + recycling;
    }

    // Same filter as the host: one half of the special-type split, both halves gated on listInJEI so a pack can hide one.
    private static int register(EmiRegistry registry, EmiRecipeCategory category, Predicate<ArcFurnaceRecipe> half) {
        try {
            registry.addCategory(category);
            registry.addWorkstation(category, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the {} category", category.getId(), t);
            return 0;
        }
        return IeRecipes.addAll(registry, ArcFurnaceRecipe.RECIPES,
                recipe -> recipe.listInJEI() && half.test(recipe),
                holder -> new ArcFurnaceEmiRecipe(category, IeIds.of(holder), holder.value()));
    }

    /** The host's DoubleIcon: the machine icon with the recycle badge at half size over its bottom-right corner. */
    /** EMI draws a category icon in a 16x16 box and the host's DoubleIcon is 18 wide, so shrink it to fit. */
    private static void categoryIcon(GuiGraphics draw, int x, int y, float delta) {
        PoseStack pose = draw.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(CATEGORY_ICON_SCALE, CATEGORY_ICON_SCALE, 1.0F);
        recycleIcon(draw, 0, 0, delta);
        pose.popPose();
    }

    private static void recycleIcon(GuiGraphics draw, int x, int y, float delta) {
        PoseStack pose = draw.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        ICON.render(draw, 1, 1, delta);
        pose.pushPose();
        // z 100 renders the badge behind the machine icon in EMI's page depth range, so keep it at the icon's own depth.
        pose.translate(BADGE_ICON_SIZE - BADGE_SCALE * BADGE_ICON_SIZE,
                BADGE_ICON_SIZE - BADGE_SCALE * BADGE_ICON_SIZE, 0.0F);
        pose.scale(BADGE_SCALE, BADGE_SCALE, 1.0F);
        draw.blit(RECYCLE_TEXTURE, 0, 0, 0, 0, 18, 18, 18, 18);
        pose.popPose();
        pose.popPose();
    }
}
