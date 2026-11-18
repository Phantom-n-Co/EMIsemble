package dev.wp.emisemble.compat.hexerei.woodcutter;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.joefoxe.hexerei.data.recipes.WoodcutterRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One woodcutter page: the input at the recipe's own count, the result on the right. */
final class WoodcutterEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(WoodcutterCategory.NAMESPACE, "textures/gui/drying_rack_jei.png");
    static final int SHEET = 256;

    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    static final int WIDTH = 92;
    static final int HEIGHT = 45;

    private static final int INPUT_X = 10;
    private static final int INPUT_Y = 12;
    private static final int OUTPUT_X = 66;
    private static final int OUTPUT_Y = 12;

    private static final float TEXT_SCALE = 0.6F;
    private static final int NAME_COLOUR = 0xFF404040;
    private static final float NAME_X = 8.33F;
    private static final float NAME_Y = 6.664F;

    private final EmiIngredient input;
    private final EmiStack output;
    private final ItemStack outputStack;
    private final @Nullable RecipeHolder<WoodcutterRecipe> holder;

    WoodcutterEmiRecipe(EmiRecipeCategory category, RecipeHolder<WoodcutterRecipe> holder) {
        this(category, holder.id(), holder.value(), holder);
    }

    WoodcutterEmiRecipe(EmiRecipeCategory category, ResourceLocation id, WoodcutterRecipe recipe) {
        this(category, id, recipe, null);
    }

    private WoodcutterEmiRecipe(EmiRecipeCategory category, ResourceLocation id, WoodcutterRecipe recipe,
                                @Nullable RecipeHolder<WoodcutterRecipe> holder) {
        super(category, id, WIDTH, HEIGHT);
        this.holder = holder;

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        Ingredient ingredient = ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.get(0);
        this.input = counted(ingredient, recipe.ingredientCount);

        this.outputStack = resultOf(recipe);
        this.output = EmiStack.of(outputStack);

        inputs.add(input);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    // The page shows the recipe's own log count, so the ingredient has to carry it rather than read as one.
    private static EmiIngredient counted(Ingredient ingredient, int count) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            return EmiStack.EMPTY;
        }
        if (items.length == 1) {
            return EmiStack.of(withCount(items[0], count));
        }

        List<EmiStack> stacks = new ArrayList<>(items.length);
        for (ItemStack item : items) {
            stacks.add(EmiStack.of(withCount(item, count)));
        }
        return new ListEmiIngredient(stacks, count);
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static ItemStack resultOf(WoodcutterRecipe recipe) {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
        widgets.addDrawable(-4, -4, WIDTH, HEIGHT, this::drawText);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }

    private void drawText(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;
        draw.pose().pushPose();
        draw.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        draw.drawString(font, outputStack.getHoverName().getString(), NAME_X, NAME_Y, NAME_COLOUR, false);
        draw.pose().popPose();
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }
}
