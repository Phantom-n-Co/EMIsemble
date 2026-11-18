package dev.wp.emisemble.compat.embers.alchemy;

import java.util.ArrayList;
import java.util.List;

import com.rekindled.embers.ConfigManager;
import com.rekindled.embers.recipe.IAlchemyRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

/** An alchemy page: the centre item, the ring of inputs with the aspect each accepts, and the result. */
public final class AlchemyPage extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AlchemyCategory.NAMESPACE, "textures/gui/jei_alchemy.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 126;
    private static final int HEIGHT = 108;
    private static final int CENTER_X = 32;
    private static final int CENTER_INPUT_Y = 37;
    private static final int OUTPUT_X = 101;
    private static final int OUTPUT_Y = 37;
    private static final int RING_RADIUS = 27;
    private static final int RING_RADIUS_SPACIOUS = 30;
    private static final int ASPECT_GAP = 16;
    private static final int PILLAR_U = 126;
    private static final int SLOT_SIZE = 16;
    private static final int MARGIN = 2;
    private static final int PALETTE_Y = 90;
    private static final int PALETTE_CENTER_X = 63;

    private final IAlchemyRecipe recipe;
    private final List<EmiIngredient> ringAspects = new ArrayList<>();
    private final List<EmiIngredient> aspectPalette = new ArrayList<>();

    public AlchemyPage(EmiRecipeCategory category, RecipeHolder<IAlchemyRecipe> holder) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();

        int aspecti = recipe.getAspects().size();
        int inputs = recipe.getInputs().size();
        boolean randomizeAspectus = ConfigManager.randomizeAlchemyAspectus();
        List<Ingredient> exactAspectus = randomizeAspectus ? List.of() : recipe.getCode(0L);

        Ingredient[][] aspectusCombinations = new Ingredient[0][0];
        if (randomizeAspectus) {
            aspectusCombinations = new Ingredient[inputs][(int) Math.pow(aspecti, inputs)];
            for (int i = 0; i < inputs; i++) {
                for (int j = 0; j < ((int) Math.pow(aspecti, inputs)); j++) {
                    aspectusCombinations[i][j] = recipe.getAspects().get((j / ((int) Math.pow(aspecti, i))) % aspecti);
                }
            }
        }

        this.inputs.add(EmiIngredient.of(recipe.getCenterInput()));
        for (int i = 0; i < inputs; i++) {
            this.inputs.add(EmiIngredient.of(recipe.getInputs().get(i)));
            Ingredient aspectus = randomizeAspectus ? CompoundIngredient.of(aspectusCombinations[i])
                    : exactAspectus.get(i);
            this.ringAspects.add(EmiIngredient.of(aspectus));
        }
        this.catalysts.addAll(this.ringAspects);
        if (randomizeAspectus) {
            for (int i = 0; i < aspecti; i++) {
                this.aspectPalette.add(EmiIngredient.of(recipe.getAspects().get(i)));
            }
            this.catalysts.addAll(this.aspectPalette);
        }
        this.outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    // The palette adds a row below the ring; without it the page ends where the ring and its aspects do.
    @Override
    public int getDisplayHeight() {
        if (aspectPalette.isEmpty()) {
            return centerY() + ringRadius() + ASPECT_GAP + SLOT_SIZE + MARGIN;
        }
        return PALETTE_Y + SLOT_SIZE + MARGIN;
    }

    private int ringRadius() {
        return aspectPalette.isEmpty() ? RING_RADIUS_SPACIOUS : RING_RADIUS;
    }

    private int centerY() {
        return MARGIN + ringRadius();
    }

    private int aspectY() {
        return centerY() + ASPECT_GAP;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, getDisplayHeight(), 0, 0);
        RecipeSlots.addFramed(widgets, inputs.get(0), CENTER_X, CENTER_INPUT_Y);
        RecipeSlots.addFramed(widgets, outputs.get(0), OUTPUT_X, OUTPUT_Y).recipeContext(this);

        Vec3 ring = new Vec3(0, ringRadius(), 0);
        int ringInputs = ringAspects.size();
        for (int i = 0; i < ringInputs; i++) {
            Vec3 rotated = ring.zRot((float) (i * 2.0 * Math.PI / ringInputs));
            int x = (int) (CENTER_X + rotated.x());
            int y = (int) (centerY() + rotated.y());
            RecipeSlots.addFramed(widgets, inputs.get(i + 1), x, y);
            AlphaTextures.add(widgets, TEXTURE, SHEET, x, (int) (aspectY() + rotated.y()), SLOT_SIZE, SLOT_SIZE,
                    PILLAR_U, 0);
            RecipeSlots.add(widgets, ringAspects.get(i), x, (int) (aspectY() + rotated.y()));
        }
        for (int i = 0; i < aspectPalette.size(); i++) {
            RecipeSlots.addFramed(widgets, aspectPalette.get(i),
                    PALETTE_CENTER_X - 8 * aspectPalette.size() + 16 * i, PALETTE_Y);
        }
    }
}
