package dev.wp.emisemble.compat.cookingforblockheads.cow_jar;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** {@code cookingforblockheads:cow_jar}: an anvil beside the cow-in-a-jar art, on the 150x110 page. */
public final class CowJarCategory {
    // The host creates its own JEI-only RecipeType, so the uid is a literal with no registry key behind it.
    public static final String NAMESPACE = "cookingforblockheads";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "cow_jar");
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei_cow_jar.png");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ID, EmiStack.of(new ItemStack(ModBlocks.cowJar)), "jei.cookingforblockheads.cow_jar");

    private CowJarCategory() {}

    public static int register(EmiRegistry registry) {
        // Everything is inside the try: a throw escaping here costs the plugin's whole registration set.
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, EmiStack.of(new ItemStack(ModBlocks.cowJar)));

            // The host registers no page at all while cow jars are switched off in its config.
            if (!CookingForBlockheadsConfig.getActive().cowJarEnabled) {
                return 0;
            }
            return EmiRecipes.add(registry, new Page()) ? 1 : 0;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("cookingforblockheads: could not register the cow_jar category", t);
            return 0;
        }
    }

    /** The host's page is a fixed one: the recipe carries no data beyond the two jars it shows. */
    private static final class Page extends BasicEmiRecipe {
        private static final int WIDTH = 150;
        private static final int HEIGHT = 110;

        private final EmiStack milk;
        private final EmiStack cowJar;

        private Page() {
            super(CATEGORY, EmiRecipes.synthetic(ID), WIDTH, HEIGHT);
            EmiStack anvil = EmiStack.of(new ItemStack(Items.ANVIL));
            this.milk = EmiStack.of(new ItemStack(ModBlocks.milkJar));
            this.cowJar = EmiStack.of(new ItemStack(ModBlocks.cowJar));
            inputs.add(anvil);
            outputs.add(milk);
            outputs.add(cowJar);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, 256, 0, 0, WIDTH, HEIGHT, 0, 0);

            // Only the anvil slot is bare; the host asked for a slot frame behind each output.
            RecipeSlots.add(widgets, inputs.get(0), 65, 1);
            RecipeSlots.addFramed(widgets, milk, 65, 77).recipeContext(this);
            RecipeSlots.addFramed(widgets, cowJar, 123, 77).recipeContext(this);
        }
    }
}
