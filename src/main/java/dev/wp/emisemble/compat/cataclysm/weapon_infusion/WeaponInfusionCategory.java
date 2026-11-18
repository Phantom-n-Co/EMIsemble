package dev.wp.emisemble.compat.cataclysm.weapon_infusion;

import com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe;
import com.github.L_Ender.cataclysm.init.ModRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.cataclysm.CataclysmCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code cataclysm:weapon_infusion}: a weapon plus an addition, on the 125x18 strip of the host's fusion GUI. */
public final class WeaponInfusionCategory {
    // The JEI uid, not the recipe type's key (cataclysm:weapon_fusion), because JEMI drops its shim by uid.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cataclysm", "weapon_infusion");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/gui/fusion.png");
    static final int ART_U = 26;
    static final int ART_V = 46;
    static final int ART_WIDTH = 125;
    static final int ART_HEIGHT = 18;
    // Fully painted art, so it is the card itself: the page sits 4px inside it and the blit hangs over that edge.
    static final int WIDTH = ART_WIDTH - 8;
    static final int HEIGHT = ART_HEIGHT - 8;

    private static final RecipeType<WeaponfusionRecipe> TYPE = ModRecipeTypes.WEAPON_FUSION.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            CataclysmCompat.stack(CataclysmCompat.FUSION_ANVIL), CataclysmCompat.name(CataclysmCompat.FUSION_ANVIL));

    private WeaponInfusionCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, CataclysmCompat.stack(CataclysmCompat.FUSION_ANVIL));
        int added = EmiRecipes.addAll(registry, TYPE, Page::new);
        Emisemble.LOGGER.info("cataclysm: {} weapon infusion recipes registered", added);
        return added;
    }

    /** Base, addition, result: the art paints all three sockets, so no slot takes chrome. */
    private static final class Page extends BasicEmiRecipe {
        private static final int BLEED = 4;
        private static final int BASE_X = 1;
        private static final int ADDITION_X = 50;
        private static final int OUTPUT_X = 108;
        private static final int ROW_Y = 1;

        private final RecipeHolder<WeaponfusionRecipe> holder;
        private final EmiIngredient base;
        private final EmiIngredient addition;
        private final EmiStack output;

        private Page(RecipeHolder<WeaponfusionRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            WeaponfusionRecipe recipe = holder.value();
            this.holder = holder;
            this.base = EmiIngredient.of(recipe.getbaseIngredient());
            this.addition = EmiIngredient.of(recipe.getAdditionIngredient());
            this.output = EmiStack.of(recipe.getResultItem(RegistryAccess.EMPTY));
            inputs.add(base);
            inputs.add(addition);
            outputs.add(output);
        }

        @Override
        public RecipeHolder<?> getBackingRecipe() {
            return holder;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(TEXTURE, -BLEED, -BLEED, ART_WIDTH, ART_HEIGHT, ART_U, ART_V);
            RecipeSlots.add(widgets, base, BASE_X - BLEED, ROW_Y - BLEED);
            RecipeSlots.add(widgets, addition, ADDITION_X - BLEED, ROW_Y - BLEED);
            RecipeSlots.add(widgets, output, OUTPUT_X - BLEED, ROW_Y - BLEED).recipeContext(this);
        }
    }
}
