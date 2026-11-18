package dev.wp.emisemble.compat.immersiveengineering.cloche;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code immersiveengineering:cloche} and its satellite {@code immersiveengineering:fertilizer}. */
public final class ClocheCategory {
    static final int WIDTH = 176;
    static final int HEIGHT = 77;
    static final int FERTILIZER_WIDTH = 150;
    static final int FERTILIZER_HEIGHT = 50;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/cloche.png");
    static final ResourceLocation ELEMENTS =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/jei_elements.png");
    static final int SHEET = 256;

    static final EmiStack ICON = EmiStack.of(IEBlocks.MetalDevices.CLOCHE);
    private static final EmiStack FERTILIZER_ICON = EmiStack.of(IEItems.Misc.FERTILIZER.get());
    private static final float BADGE_SCALE = 0.6F;
    private static final float BADGE_ICON_SIZE = 18.0F;
    private static final float CATEGORY_ICON_SCALE = 16.0F / BADGE_ICON_SIZE;

    public static final EmiRecipeCategory CLOCHE = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.CLOCHE.get()), ICON, "block.immersiveengineering.cloche");

    public static final EmiRecipeCategory FERTILIZER = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.FERTILIZER.get()), ClocheCategory::categoryIcon,
            "desc.immersiveengineering.jei.category.fertilizer");

    /** EMI draws a category icon in a 16x16 box and the host's DoubleIcon is 18 wide, so shrink it to fit. */
    private static void categoryIcon(GuiGraphics draw, int x, int y, float delta) {
        PoseStack pose = draw.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(CATEGORY_ICON_SCALE, CATEGORY_ICON_SCALE, 1.0F);
        badgeIcon(draw, delta);
        pose.popPose();
    }

    private ClocheCategory() {}

    public static int register(EmiRegistry registry) {
        List<EmiStack> fertilizers = fertilizers(registry);
        int added = 0;
        try {
            registry.addCategory(CLOCHE);
            registry.addWorkstation(CLOCHE, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the cloche category", t);
            return 0;
        }
        added += EmiRecipes.addAll(registry, IERecipeTypes.CLOCHE.get(),
                holder -> new ClocheEmiRecipe(CLOCHE, IeIds.of(holder), holder.value(), fertilizers));

        try {
            registry.addCategory(FERTILIZER);
            registry.addWorkstation(FERTILIZER, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the cloche fertiliser category", t);
            return added;
        }
        added += EmiRecipes.addAll(registry, IERecipeTypes.FERTILIZER.get(),
                holder -> new ClocheFertilizerEmiRecipe(FERTILIZER, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} cloche and fertiliser recipes", added);
        return added;
    }

    /** The fertilisers the cloche slot offers, read from the recipe manager so the slot needs no loaded level. */
    private static List<EmiStack> fertilizers(EmiRegistry registry) {
        List<EmiStack> fertilizers = new ArrayList<>();
        try {
            for (RecipeHolder<ClocheFertilizer> holder
                    : registry.getRecipeManager().getAllRecipesFor(IERecipeTypes.FERTILIZER.get())) {
                for (ItemStack stack : holder.value().input.getItems()) {
                    if (!stack.isEmpty()) {
                        fertilizers.add(EmiStack.of(stack));
                    }
                }
            }
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not read the cloche fertilisers", t);
        }
        return fertilizers;
    }

    /** The host's DoubleIcon: the cloche with a fertiliser stuck over its bottom-right corner. */
    static void badgeIcon(GuiGraphics draw, float delta) {
        ICON.render(draw, 1, 1, delta);
        PoseStack pose = draw.pose();
        pose.pushPose();
        // z 100 sits behind the cloche icon in EMI's page depth range; the badge must be the visible one.
        pose.translate(BADGE_ICON_SIZE - BADGE_SCALE * 16, BADGE_ICON_SIZE - BADGE_SCALE * 16, 0.0F);
        pose.scale(BADGE_SCALE, BADGE_SCALE, 1.0F);
        FERTILIZER_ICON.render(draw, 0, 0, delta);
        pose.popPose();
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
    }
}
