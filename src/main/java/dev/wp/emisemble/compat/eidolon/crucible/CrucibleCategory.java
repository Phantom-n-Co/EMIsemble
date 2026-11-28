package dev.wp.emisemble.compat.eidolon.crucible;

import alexthw.eidolon_repraised.codex.Page;
import alexthw.eidolon_repraised.recipe.CrucibleRecipe;
import alexthw.eidolon_repraised.registries.EidolonRecipes;
import alexthw.eidolon_repraised.registries.Registry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code eidolon_repraised:crucible}: one row per step, the host's own row art, counts and stir icons. */
public final class CrucibleCategory {
    public static final ResourceLocation ID = EmiRecipes.categoryId(EidolonRecipes.CRUCIBLE_TYPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(Registry.CRUCIBLE.get()), "jei.eidolon_repraised.crucible");

    private static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/jei_page_bg.png");
    private static final ResourceLocation ART =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/codex_crucible_page.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 138;
    private static final int HEIGHT = 172;
    private static final int ROW_ART_Y = 5;
    private static final int ROW_SLOT_X = 28;
    private static final int ROW_STEP = 17;

    private CrucibleCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Registry.CRUCIBLE.get()));

        int added = EmiRecipes.addAll(registry, EidolonRecipes.CRUCIBLE_TYPE.get(), CrucibleEmiRecipe::new);

        Emisemble.LOGGER.info("eidolon_repraised: {} crucible recipes registered", added);
        return added;
    }

    /** Consecutive copies of one item share a slot with their count, as the host's own list does. */
    private static List<EmiIngredient> condense(CrucibleRecipe.Step step) {
        List<EmiIngredient> condensed = new ArrayList<>();
        List<ItemStack> counted = new ArrayList<>();
        List<Ingredient> countedBy = new ArrayList<>();
        for (Ingredient ingredient : step.matches()) {
            ItemStack stack = ingredient.getItems().length == 0 ? ItemStack.EMPTY : ingredient.getItems()[0].copy();
            if (stack.isEmpty()) {
                continue;
            }
            int last = counted.size() - 1;
            if (last >= 0 && ItemStack.isSameItemSameComponents(counted.get(last), stack)
                    && counted.get(last).getCount() < stack.getMaxStackSize()) {
                counted.get(last).grow(1);
                continue;
            }
            counted.add(stack);
            countedBy.add(ingredient);
        }
        for (int i = 0; i < counted.size(); i++) {
            List<EmiStack> options = new ArrayList<>();
            for (ItemStack option : countedBy.get(i).getItems()) {
                options.add(EmiStack.of(option, counted.get(i).getCount()));
            }
            condensed.add(EmiIngredient.of(options));
        }
        return condensed;
    }

    private static final class CrucibleEmiRecipe extends BasicEmiRecipe {
        private final RecipeHolder<CrucibleRecipe> holder;
        private final List<List<EmiIngredient>> steps;
        private final int[] stirs;
        private final EmiStack output;

        private CrucibleEmiRecipe(RecipeHolder<CrucibleRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.holder = holder;
            List<CrucibleRecipe.Step> recipeSteps = holder.value().getSteps();
            this.steps = new ArrayList<>(recipeSteps.size());
            this.stirs = new int[recipeSteps.size()];
            for (int i = 0; i < recipeSteps.size(); i++) {
                steps.add(condense(recipeSteps.get(i)));
                stirs[i] = recipeSteps.get(i).stirs();
            }
            this.output = EmiStack.of(holder.value().getResultItem());
            for (List<EmiIngredient> step : steps) {
                inputs.addAll(step);
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        @Override
        public RecipeHolder<?> getBackingRecipe() {
            return holder;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, PAGE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            int yoff = 80 - (steps.size() * 20 + 32) / 2;
            for (int i = 0; i < steps.size(); i++) {
                int rowY = ROW_ART_Y + yoff + i * 20;
                AlphaTextures.add(widgets, ART, SHEET, 4, rowY, 128, 20, 128, 0);
                int x = ROW_SLOT_X;
                for (EmiIngredient input : steps.get(i)) {
                    AlphaTextures.add(widgets, ART, SHEET, x, rowY + 1, 16, 17, 176, 32);
                    RecipeSlots.add(widgets, input, x, rowY - 1);
                    x += ROW_STEP;
                }
                for (int stir = 0; stir < stirs[i]; stir++) {
                    AlphaTextures.add(widgets, ART, SHEET, x, rowY + 1, 16, 17, 192, 32);
                    x += ROW_STEP;
                }
                String level = I18n.get("enchantment.level." + (i + 1)) + ".";
                widgets.addDrawable(11, rowY + 8, 24, 11,
                        (draw, mouseX, mouseY, delta) -> Page.drawText(draw, level, 0, 0));
            }
            AlphaTextures.add(widgets, ART, SHEET, 4, ROW_ART_Y + yoff + steps.size() * 20, 128, 32, 128, 64);
            if (!output.isEmpty()) {
                RecipeSlots.add(widgets, output, 60, yoff + steps.size() * 20 + 14).recipeContext(this);
            }
        }
    }
}
