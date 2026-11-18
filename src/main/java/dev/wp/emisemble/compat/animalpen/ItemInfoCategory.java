package dev.wp.emisemble.compat.animalpen;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import lv.id.bonne.animalpen.platform.PlatformHelper;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Animal Pens' three {@code animal_pen:item_info_*} pages, generated the way its JEI plugin generates them: one page per
 * tag member that has a spawn egg, captioned with the tool's own description.
 */
public final class ItemInfoCategory {
    private static final String NAMESPACE = "animal_pen";

    // The host's uid and its title key disagree on two families, so neither can be derived from the other.
    public static final EmiRecipeCategory ANIMAL_CAGE = category("item_info_animal_cage",
            "jei.animal_pen.item_info_animal_cage", AnimalPensItemRegistry.ANIMAL_CAGE.get());
    public static final EmiRecipeCategory BIRD_CAGE = category("item_info_bird_cage",
            "jei.animal_pen.item_info_bird_catcher", AnimalPensItemRegistry.BIRD_CATCHER.get());
    public static final EmiRecipeCategory WATER_CONTAINER = category("item_info_water_container",
            "jei.animal_pen.item_info_water_animal_container", AnimalPensItemRegistry.ANIMAL_CONTAINER.get());

    private ItemInfoCategory() {}

    private static EmiRecipeCategory category(String path, String titleKey, Item icon) {
        return new TranslatedCategory(ResourceLocation.fromNamespaceAndPath(NAMESPACE, path), EmiStack.of(icon),
                titleKey);
    }

    static int register(EmiRegistry registry, EmiRecipeCategory category, Item tool, TagKey<EntityType<?>> tag,
                        String descriptionKey) {
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(tool));

        Component description = Component.translatable(descriptionKey);
        String family = category.getId().getPath();
        int added = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (!type.is(tag)) {
                continue;
            }
            SpawnEggItem egg = PlatformHelper.getSpawnEgg(type);
            if (egg == null) {
                continue;
            }
            if (EmiRecipes.add(registry, new Page(category, pageId(family, type), tool, description,
                    egg.getDefaultInstance()))) {
                added++;
            }
        }
        return added;
    }

    // Prefixed per family: an entity can belong to more than one capture tag, and EMI keys pages by id alone.
    private static ResourceLocation pageId(String family, EntityType<?> type) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE,
                "/" + family + "/" + BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
    }

    private static final class Page extends BasicEmiRecipe {
        // 176 wide, not the host's blank-drawable 160: EMI draws no tree or favourite buttons here, so that column is free.
        private static final int WIDTH = 176;
        private static final int HEIGHT = 40;

        private static final int TOOL_X = 1;
        private static final int TOOL_Y = 5;
        // Anchored to the right edge as JEI's (135,5) was to its narrower page.
        private static final int EGG_X = WIDTH - 25;
        private static final int EGG_Y = 5;

        private static final int TEXT_X = 25;
        private static final int TEXT_Y = 5;
        // JEI's 130 paints under the egg slot; this is the real gap between the text origin and the slot, less a 6px margin.
        private static final int TEXT_WIDTH = EGG_X - TEXT_X - 6;
        private static final int TEXT_COLOR = 0xFF404040;

        private final Component description;

        private Page(EmiRecipeCategory category, ResourceLocation id, Item tool, Component description,
                     ItemStack egg) {
            super(category, id, WIDTH, HEIGHT);
            this.description = description;
            inputs.add(EmiStack.of(tool));
            outputs.add(EmiStack.of(egg));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            RecipeSlots.add(widgets, inputs.get(0), TOOL_X, TOOL_Y);
            RecipeSlots.add(widgets, outputs.get(0), EGG_X, EGG_Y).recipeContext(this);

            // Host line arithmetic: one line per lineHeight + 1 from y=5, unshadowed; only the wrap width diverges.
            Minecraft minecraft = Minecraft.getInstance();
            int y = TEXT_Y;
            for (FormattedCharSequence line : minecraft.font.split(description, TEXT_WIDTH)) {
                widgets.addText(line, TEXT_X, y, TEXT_COLOR, false);
                y += minecraft.font.lineHeight + 1;
            }
        }

        // A capture lookup, not a craft: the egg is a key and the tool a workstation, so no tree and no favourite star.
        @Override
        public boolean supportsRecipeTree() {
            return false;
        }
    }
}
