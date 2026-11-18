package dev.wp.emisemble.compat.immersiveengineering.mixer;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.compat.immersiveengineering.IeRecipes;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code immersiveengineering:mixer}. A blank page with the machine's own tank art blitted behind the input fluid,
 * so the fluids need no chrome and only the item inputs get a slot frame.
 */
public final class MixerCategory {
    static final int WIDTH = 155;
    static final int HEIGHT = 60;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/mixer.png");
    static final int SHEET = 256;

    private static final String POTION_ID_PREFIX = "mixer_potion/";

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.MIXER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.MIXER.get()), ICON, "block.immersiveengineering.mixer");
    // JEI registers the generated potion mixes as a second category of their own, so these pages need its uid or JEMI
    // keeps its shim beside ours. Titled the way the host titles it, as the recycling category is.
    public static final EmiRecipeCategory POTIONS = new TranslatedCategory(IEApi.ieLoc("mixer_potions"), ICON,
            Component.translatable("block.immersiveengineering.mixer")
                    .append(Component.translatable("desc.immersiveengineering.jei.category.potions")));

    private MixerCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
            registry.addCategory(POTIONS);
            registry.addWorkstation(POTIONS, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the mixer category", t);
            return 0;
        }
        // The cache, not the manager: the potion list is a generated-list placeholder there and would be skipped.
        Map<String, List<RecipeHolder<MixerRecipe>>> families = new TreeMap<>();
        int added = 0;
        for (RecipeHolder<MixerRecipe> holder : IeRecipes.all(MixerRecipe.RECIPES)) {
            try {
                String shape = MixerPotionPage.shape(holder.value());
                if (shape == null) {
                    if (EmiRecipes.add(registry, new MixerEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()))) {
                        added++;
                    }
                } else {
                    families.computeIfAbsent(shape, key -> new ArrayList<>()).add(holder);
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping mixer recipe {}", holder.id(), t);
            }
        }

        int collapsed = 0;
        int folded = 0;
        for (List<RecipeHolder<MixerRecipe>> family : families.values()) {
            try {
                // A family of one has nothing to cycle, and keeping its own id keeps that page where it always was.
                if (family.size() == 1) {
                    RecipeHolder<MixerRecipe> only = family.get(0);
                    if (EmiRecipes.add(registry, new MixerEmiRecipe(CATEGORY, IeIds.of(only), only.value()))) {
                        added++;
                    }
                    continue;
                }
                ResourceLocation id = IeIds.synthetic(CATEGORY.getId().getNamespace(), POTION_ID_PREFIX + collapsed);
                if (EmiRecipes.add(registry, MixerPotionPage.of(POTIONS, id, values(family)))) {
                    added++;
                    folded += family.size();
                    collapsed++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping a mixer potion family", t);
            }
        }
        Emisemble.LOGGER.info("immersiveengineering: {} mixer recipes ({} potion families cycle {} generated mixes)",
                added, collapsed, folded);
        return added;
    }

    private static List<MixerRecipe> values(List<RecipeHolder<MixerRecipe>> family) {
        List<MixerRecipe> recipes = new ArrayList<>(family.size());
        for (RecipeHolder<MixerRecipe> holder : family) {
            recipes.add(holder.value());
        }
        return recipes;
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 40, 0, 74, 60, 68, 8);
    }
}
