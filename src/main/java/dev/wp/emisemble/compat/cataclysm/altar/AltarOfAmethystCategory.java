package dev.wp.emisemble.compat.cataclysm.altar;

import com.github.L_Ender.cataclysm.crafting.AltarOfAmethystRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code cataclysm:altar_of_amethyst}: one offering in, one blessing out, on the host's 125x59 altar card. */
public final class AltarOfAmethystCategory {
    // The JEI uid, not the recipe type's key (cataclysm:amethyst_bless), because JEMI drops its shim by uid.
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cataclysm", "altar_of_amethyst");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("cataclysm",
            "textures/gui/altar_of_amethyst_jei.png");
    // The host blits the sheet's top 125x59, so that fully painted crop is the card, not the 64 rows the sheet paints.
    static final int ART_WIDTH = 125;
    static final int ART_HEIGHT = 59;
    static final int WIDTH = ART_WIDTH - 8;
    static final int HEIGHT = ART_HEIGHT - 8;

    private static final RecipeType<AltarOfAmethystRecipe> TYPE = ModRecipeTypes.AMETHYST_BLESS.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            CataclysmCompat.stack(CataclysmCompat.ALTAR), CataclysmCompat.name(CataclysmCompat.ALTAR)
                    .append(Component.literal(" "))
                    .append(Component.translatable("cataclysm.gui.altar_of_amethyst_blessing")));

    private AltarOfAmethystCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, CataclysmCompat.stack(CataclysmCompat.ALTAR));
        int added = EmiRecipes.addAll(registry, TYPE, Page::new);
        Emisemble.LOGGER.info("cataclysm: {} altar of amethyst recipes registered", added);
        return added;
    }

    /** The offering and the blessed result, both on sockets the art paints. */
    private static final class Page extends BasicEmiRecipe {
        private static final int BLEED = 4;
        private static final int INPUT_X = 21;
        private static final int OUTPUT_X = 94;
        private static final int ROW_Y = 23;

        private final RecipeHolder<AltarOfAmethystRecipe> holder;
        private final EmiIngredient input;
        private final EmiStack output;

        private Page(RecipeHolder<AltarOfAmethystRecipe> holder) {
            super(CATEGORY, holder.id(), WIDTH, HEIGHT);
            AltarOfAmethystRecipe recipe = holder.value();
            this.holder = holder;
            this.input = EmiIngredient.of(recipe.getIngredients().getFirst());
            this.output = EmiStack.of(recipe.getResultItem(RegistryAccess.EMPTY));
            inputs.add(input);
            outputs.add(output);
        }

        @Override
        public RecipeHolder<?> getBackingRecipe() {
            return holder;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(TEXTURE, -BLEED, -BLEED, ART_WIDTH, ART_HEIGHT, 0, 0);
            RecipeSlots.add(widgets, input, INPUT_X - BLEED, ROW_Y - BLEED);
            RecipeSlots.add(widgets, output, OUTPUT_X - BLEED, ROW_Y - BLEED).recipeContext(this);
        }
    }
}
