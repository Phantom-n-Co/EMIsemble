package dev.wp.emisemble.compat.eidolon.ritual;

import alexthw.eidolon_repraised.api.ritual.FocusItemPresentRequirement;
import alexthw.eidolon_repraised.api.ritual.HealthRequirement;
import alexthw.eidolon_repraised.api.ritual.IRequirement;
import alexthw.eidolon_repraised.api.ritual.Ritual;
import alexthw.eidolon_repraised.codex.RitualPage;
import alexthw.eidolon_repraised.recipe.ItemRitualRecipe;
import alexthw.eidolon_repraised.recipe.RitualRecipe;
import alexthw.eidolon_repraised.recipe.SummonRitualRecipe;
import alexthw.eidolon_repraised.registries.EidolonRecipes;
import alexthw.eidolon_repraised.registries.Registry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

/** {@code eidolon_repraised:rituals}: the brazier circle, its reagent and the ritual's symbol and name. */
public final class RitualCategory {
    // The JEI uid, not a recipe type key: the host's category draws five ritual recipe types as one page.
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "rituals");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(Registry.BRAZIER.get()), "jei.eidolon_repraised.ritual");

    private static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/jei_page_bg.png");
    private static final ResourceLocation ART =
            ResourceLocation.fromNamespaceAndPath("eidolon_repraised", "textures/gui/codex_ritual_page.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 138;
    private static final int HEIGHT = 172;

    private RitualCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Registry.BRAZIER.get()));

        int added = 0;
        added += EmiRecipes.addAll(registry, EidolonRecipes.CRAFTING_RITUAL_TYPE.get(), RitualEmiRecipe::new);
        added += EmiRecipes.addAll(registry, EidolonRecipes.SUMMON_RITUAL_TYPE.get(), RitualEmiRecipe::new);
        added += EmiRecipes.addAll(registry, EidolonRecipes.COMMAND_RITUAL_TYPE.get(), RitualEmiRecipe::new);
        added += EmiRecipes.addAll(registry, EidolonRecipes.LOCATION_RITUAL_TYPE.get(), RitualEmiRecipe::new);
        added += EmiRecipes.addAll(registry, EidolonRecipes.RITUAL_TYPE.get(), RitualEmiRecipe::new);

        Emisemble.LOGGER.info("eidolon_repraised: {} ritual recipes registered", added);
        return added;
    }

    private static final class RitualEmiRecipe extends BasicEmiRecipe {
        private final RecipeHolder<? extends RitualRecipe> holder;
        private final RitualRecipe recipe;
        private final Ritual ritual;
        private final List<RitualPage.RitualIngredient> ring;
        private final EmiStack output;

        private RitualEmiRecipe(RecipeHolder<? extends RitualRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            this.holder = holder;
            this.recipe = holder.value();
            // A generic ritual whose key has no registered Ritual throws here, as it would in JEI.
            this.ritual = recipe.getRitualWithRequirements();
            if (ritual == null) {
                throw new IllegalStateException("No ritual registered for " + holder.id());
            }
            this.ring = new ArrayList<>();
            RitualPage.rearrangeIngredients(recipe, ring);
            this.output = output();

            inputs.add(EmiIngredient.of(recipe.getReagent()));
            for (RitualPage.RitualIngredient ingredient : ring) {
                inputs.add(EmiIngredient.of(ingredient.stack));
            }
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }

        private EmiStack output() {
            if (recipe instanceof ItemRitualRecipe item) {
                return EmiStack.of(item.getResult());
            }
            if (recipe instanceof SummonRitualRecipe summon) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(summon.getEntityRL());
                SpawnEggItem egg = SpawnEggItem.byId(type);
                return egg == null ? EmiStack.EMPTY : EmiStack.of(new ItemStack(egg));
            }
            return EmiStack.EMPTY;
        }

        @Override
        public RecipeHolder<?> getBackingRecipe() {
            return holder;
        }

        private EmiIngredient focusPresent() {
            for (IRequirement requirement : ritual.getInvariants()) {
                if (requirement instanceof FocusItemPresentRequirement present) {
                    return EmiIngredient.of(present.getMatch());
                }
            }
            return EmiStack.EMPTY;
        }

        @Override
        public List<EmiIngredient> getCatalysts() {
            EmiIngredient focus = focusPresent();
            return focus.isEmpty() ? List.of() : List.of(focus);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, PAGE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            AlphaTextures.add(widgets, ART, SHEET, 5, 4, 128, 160, 0, 0);
            // Under the slots: JEI paints it over the result, which leaves the result invisible.
            widgets.addDrawable(0, 0, WIDTH, HEIGHT,
                    (draw, mouseX, mouseY, delta) -> RitualPage.renderRitualSymbol(draw, 5, 4, ritual));
            widgets.addText(ritual.getName(), 5 + 69, 4 + 5, 0xFF000000, false)
                    .horizontalAlign(TextWidget.Alignment.CENTER);

            // Copied from the host's own page: integer division in the step, and the slots offset from the frames.
            int count = ring.size();
            float angleStep = count == 0 ? 0 : Math.min(30, 180 / count);
            double rootAngle = 90 - (count - 1) * angleStep / 2;
            for (int i = 0; i < count; i++) {
                double angle = Math.toRadians(rootAngle + angleStep * i);
                int frameX = 5 + (int) (64 + 48 * Math.cos(angle));
                int frameY = 4 + (int) (88 + 48 * Math.sin(angle));
                RitualPage.RitualIngredient ingredient = ring.get(i);
                if (ingredient.isFocus) {
                    AlphaTextures.add(widgets, ART, SHEET, frameX - 13, frameY - 13, 26, 24, 128, 0);
                } else {
                    AlphaTextures.add(widgets, ART, SHEET, frameX - 8, frameY - 8, 16, 16, 154, 0);
                }
                RecipeSlots.add(widgets, EmiIngredient.of(ingredient.stack),
                        (int) (69 + 48 * Math.cos(angle)) - 8, (int) (91 + 48 * Math.sin(angle)) - 8);
            }

            EmiIngredient focus = focusPresent();
            if (!focus.isEmpty()) {
                AlphaTextures.add(widgets, ART, SHEET, 5 + 86 - 5, 4 + 80 - 5, 26, 24, 128, 0);
                RecipeSlots.add(widgets, focus, 91, 82);
            }
            RecipeSlots.add(widgets, EmiIngredient.of(recipe.getReagent()), 60, 85);
            if (!output.isEmpty()) {
                RecipeSlots.add(widgets, output, 62, 45).recipeContext(this);
            }

            for (IRequirement requirement : ritual.getRequirements()) {
                if (requirement instanceof HealthRequirement health) {
                    // The host passes the float to a %d key; the game formats it, so the text matches JEI's.
                    Component line = Component.translatable("eidolon_repraised.jei.health_sacrifice",
                            health.getHealth() / 2);
                    widgets.addText(line, 5 + 8, 4 + 150, 0xFF0000, false);
                    break;
                }
            }
        }
    }
}
