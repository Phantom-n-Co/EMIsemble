package dev.wp.emisemble.compat.arsadditions.locate_structure;

import com.github.jarva.arsadditions.common.item.data.WayfinderData;
import com.github.jarva.arsadditions.common.recipe.LocateStructureRecipe;
import com.github.jarva.arsadditions.common.ritual.RitualLocateStructure;
import com.github.jarva.arsadditions.setup.registry.AddonDataComponentRegistry;
import com.github.jarva.arsadditions.setup.registry.AddonItemRegistry;
import com.github.jarva.arsadditions.setup.registry.AddonRecipeRegistry;
import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.items.RitualTablet;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.RingSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** {@code ars_additions:locate_structure}: a ritual tablet and its augments find a structure. */
public final class LocateStructureCategory {
    private static final String NAMESPACE = "ars_additions";
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(AddonRecipeRegistry.LOCATE_STRUCTURE_TYPE.get()),
            EmiStack.of(BlockRegistry.RITUAL_BLOCK),
            // The host names the page after the ritual it belongs to, not after the recipe type.
            "item.ars_additions." + RitualLocateStructure.RESOURCE_LOCATION.getPath());

    private LocateStructureCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(BlockRegistry.RITUAL_BLOCK));
        EmiStack tablet = tablet();
        if (!tablet.isEmpty()) {
            registry.addWorkstation(CATEGORY, tablet);
        }

        int added = EmiRecipes.addAll(registry, AddonRecipeRegistry.LOCATE_STRUCTURE_TYPE.get(), Page::new);

        Emisemble.LOGGER.info("{}: {} locate_structure recipes registered", NAMESPACE, added);
        return added;
    }

    private static EmiStack tablet() {
        RitualTablet item = RitualRegistry.getRitualItemMap().get(RitualLocateStructure.RESOURCE_LOCATION);
        return item == null ? EmiStack.EMPTY : EmiStack.of(item);
    }

    private static final class Page extends BasicEmiRecipe {
        // The host's page is blank, so every slot keeps EMI's frame.
        private static final int WIDTH = 114;
        private static final int HEIGHT = 108;
        private static final int CENTRE_X = 48;
        private static final int CENTRE_Y = 45;
        private static final int RING_X = 48;
        private static final int RING_Y = 13;
        private static final int OUTPUT_X = 86;
        private static final int OUTPUT_Y = 10;
        private static final int TEXT_Y = 100;

        // The host passes 10 as the colour, whose zero alpha the font widens to opaque.
        private static final int TEXT_COLOUR = 10;

        private final List<EmiIngredient> ring = new ArrayList<>();
        private final EmiStack tablet;
        private final EmiStack wayfinder;
        private final Component name;

        private Page(RecipeHolder<LocateStructureRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            LocateStructureRecipe recipe = holder.value();

            this.name = recipe.getName();
            this.tablet = tablet();
            for (Ingredient ingredient : recipe.getIngredients()) {
                ring.add(EmiIngredient.of(ingredient));
            }
            inputs.addAll(ring);
            if (!tablet.isEmpty()) {
                inputs.add(tablet);
            }

            ItemStack stack = new ItemStack(AddonItemRegistry.WAYFINDER.get());
            stack.set(AddonDataComponentRegistry.WAYFINDER_DATA.get(), new WayfinderData(name));
            this.wayfinder = EmiStack.of(stack);
            if (!wayfinder.isEmpty()) {
                outputs.add(wayfinder);
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            // Every page starts the ring at the top; the host's own accumulator carries the rotation over between pages.
            RingSlots.add(widgets, ring, RING_X, RING_Y, CENTRE_X, CENTRE_Y);
            RecipeSlots.addFramed(widgets, tablet, CENTRE_X, CENTRE_Y);
            RecipeSlots.addFramed(widgets, wayfinder, OUTPUT_X, OUTPUT_Y).recipeContext(this);
            widgets.addText(name, 0, TEXT_Y, TEXT_COLOUR, false);
        }
    }
}
