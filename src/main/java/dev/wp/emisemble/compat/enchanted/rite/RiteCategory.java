package dev.wp.emisemble.compat.enchanted.rite;

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
import net.favouriteless.enchanted.client.EnchantedClient;
import net.favouriteless.enchanted.common.enchanted.circle_magic.CircleMagicShape;
import net.favouriteless.enchanted.common.enchanted.circle_magic.RiteType;
import net.favouriteless.enchanted.common.init.EData;
import net.favouriteless.enchanted.common.init.EItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** {@code enchanted:rite}: the items laid on a ritual circle, drawn over the circle art the rite requires. */
public final class RiteCategory {
    public static final String NAMESPACE = "enchanted";
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "rite");
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(EItems.RITUAL_CHALK.get()), "jei.category.enchanted.circle_magic");

    private RiteCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(EItems.GOLDEN_CHALK.get()));

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("{}: no level loaded, rite pages stay unregistered", NAMESPACE);
            return 0;
        }

        // Rites are registry entries, not recipes, so this walks the host's own registry rather than a recipe type.
        Registry<RiteType> rites = level.registryAccess().registryOrThrow(EData.RITE_TYPES_REGISTRY);
        int added = 0;
        for (Map.Entry<ResourceKey<RiteType>, RiteType> entry : rites.entrySet()) {
            if (entry.getValue().getOutputs() == null) {
                continue;
            }
            // The page is built before EmiRecipes.add sees it, so this is the only guard for a broken rite.
            try {
                if (EmiRecipes.add(registry, new Page(entry.getKey().location(), entry.getValue()))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping rite {}", entry.getKey().location(), t);
            }
        }

        Emisemble.LOGGER.info("{}: {} rite recipes registered", NAMESPACE, added);
        return added;
    }

    private static final class Page extends BasicEmiRecipe {
        private static final ResourceLocation TEXTURE =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/jei/circle_magic.png");
        private static final ResourceLocation GOLD_GLYPH =
                ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/gold_glyph.png");
        private static final int SHEET = 256;
        private static final int CIRCLE_SHEET = 100;
        private static final int CIRCLE_SIZE = 100;
        private static final int CIRCLE_Y = 14;

        private static final int WIDTH = 180;
        private static final int HEIGHT = 120;
        private static final int CENTER_X = 47;
        private static final int CENTER_Y = 60;
        private static final int START_RADIUS = 15;
        private static final int RADIUS_INCREMENT = 15;
        // Each ring point is the 16x16 ingredient's centre, so the rect sits 8 back along both axes.
        private static final int SLOT_INSET = 8;

        private static final int OUTPUT_X = 119;
        private static final int OUTPUT_COLUMNS = 3;
        private static final int OUTPUT_STEP = 17;
        private static final int OUTPUT_CENTER_Y = 61;

        private static final int ARROW_X = 95;
        private static final int ARROW_Y = 53;
        private static final int TIME_MS = 6000;

        private static final int NAME_Y = 0;
        private static final int POWER_Y = 112;
        private static final int DARK_GRAY = 0xFF404040;
        // The host hardcodes this prefix rather than translating it.
        private static final String POWER_PREFIX = "Required Altar Power : ";

        private final ResourceLocation riteKey;
        private final List<EmiStack> ingredients;
        private final List<EmiStack> results;
        private final List<ResourceLocation> circles;
        private final int power;

        private Page(ResourceLocation riteKey, RiteType rite) {
            super(CATEGORY, EmiRecipes.synthetic(riteKey), WIDTH, HEIGHT);
            this.riteKey = riteKey;
            this.ingredients = rite.getItems().stream().map(EmiStack::of).toList();
            this.results = rite.getOutputs().stream().map(EmiStack::of).toList();
            this.circles = circlesOf(rite);
            this.power = rite.getPower();

            inputs.addAll(ingredients);
            outputs.addAll(results);
        }

        // The host keeps the circles on the category and clears them per recipe; a bake cannot, so each page owns its own.
        private static List<ResourceLocation> circlesOf(RiteType rite) {
            List<ResourceLocation> circles = new ArrayList<>();
            for (Map.Entry<Holder<CircleMagicShape>, Block> entry : rite.getShapes().entrySet()) {
                ResourceKey<CircleMagicShape> shape = entry.getKey().unwrapKey().orElse(null);
                if (shape == null) {
                    continue;
                }
                // The chalk block is the colour: each chalk ships its own sprite, so this blit must never be tinted.
                ResourceLocation texture = EnchantedClient.getShapeGuiTexture(shape, entry.getValue());
                if (texture != null) {
                    circles.add(texture);
                }
            }
            return circles;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
            for (ResourceLocation circle : circles) {
                AlphaTextures.add(widgets, circle, CIRCLE_SHEET, 0, CIRCLE_Y, CIRCLE_SIZE, CIRCLE_SIZE, 0, 0);
            }
            AlphaTextures.add(widgets, GOLD_GLYPH, CIRCLE_SHEET, 0, CIRCLE_Y, CIRCLE_SIZE, CIRCLE_SIZE, 0, 0);
            widgets.addFillingArrow(ARROW_X, ARROW_Y, TIME_MS);

            addRingSlots(widgets);
            addResultSlots(widgets);

            centred(widgets, Component.translatable("rite." + riteKey.getNamespace() + "." + riteKey.getPath()),
                    NAME_Y);
            centred(widgets, Component.literal(POWER_PREFIX + power), POWER_Y);
        }

        private void addRingSlots(WidgetHolder widgets) {
            int index = 0;
            int remaining = ingredients.size();
            for (int circleNum = 1; remaining > 0; circleNum++) {
                int radius = START_RADIUS + (circleNum - 1) * RADIUS_INCREMENT;
                int limit = (int) Math.round(Math.pow(6.0, circleNum));
                int count = Math.min(remaining, limit);

                for (int i = 0; i < count; i++) {
                    float angle = i * (float) (Math.PI * 2) / count + (float) Math.PI;
                    int x = CENTER_X + Math.round(Mth.sin(-angle) * radius) - SLOT_INSET;
                    int y = CENTER_Y + Math.round(Mth.cos(-angle) * radius) - SLOT_INSET;
                    RecipeSlots.add(widgets, ingredients.get(index++), x, y);
                    remaining--;
                }
            }
        }

        private void addResultSlots(WidgetHolder widgets) {
            int rows = (int) Math.ceil(results.size() / (float) OUTPUT_COLUMNS);
            int top = OUTPUT_CENTER_Y - Math.round(rows * OUTPUT_STEP / 2.0F);

            for (int i = 0; i < results.size(); i++) {
                int x = OUTPUT_X + i % OUTPUT_COLUMNS * OUTPUT_STEP;
                int y = top + i / OUTPUT_COLUMNS * OUTPUT_STEP;
                RecipeSlots.add(widgets, results.get(i), x, y).recipeContext(this);
            }
        }

        private static void centred(WidgetHolder widgets, Component text, int y) {
            widgets.addText(text, WIDTH / 2 - Minecraft.getInstance().font.width(text) / 2, y, DARK_GRAY, false);
        }
    }
}
