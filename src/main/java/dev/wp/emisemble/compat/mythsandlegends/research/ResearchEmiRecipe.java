package dev.wp.emisemble.compat.mythsandlegends.research;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.network.ResearchPayload;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/** One research page: the ingredients, whatever the research hands back, and the host's own labels above them. */
public final class ResearchEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final String[] RANK_KEYS = {
            "text.mythsandlegends.rank.novice", "text.mythsandlegends.rank.apprentice",
            "text.mythsandlegends.rank.scholar", "text.mythsandlegends.rank.sage",
            "text.mythsandlegends.rank.master"};

    private static final int SLOT_X = 9;
    private static final int GROUP_STEP = 30;
    private static final int INGREDIENTS_Y = 67;
    private static final int REWARDS_Y = 102;
    private static final int LABEL_X = 4;
    private static final int COUNT_DX = 11;
    private static final int COUNT_DY = 14;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int INGREDIENT_COUNT_COLOR = 0xFFFF5555;
    private static final int REWARD_COUNT_COLOR = 0xFF00CC00;

    private final ResearchPayload.Entry entry;

    public ResearchEmiRecipe(EmiRecipeCategory category, ResearchPayload.Entry entry) {
        // Synthetic with its own prefix: a research shares its id with a real recipe, and one of them is named
        // stone_sanctum, which would otherwise collide with the ritual's own page.
        super(category, ResourceLocation.fromNamespaceAndPath(entry.id().getNamespace(),
                        "/research/" + entry.id().getPath()),
                ResearchCategory.WIDTH, ResearchCategory.HEIGHT);
        this.entry = entry;
        for (ResearchPayload.Input input : entry.ingredients()) {
            EmiIngredient stack = ingredient(input);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        for (int i = 0; i < entry.rewards().size(); i++) {
            outputs.add(EmiStack.of(entry.rewards().get(i)));
        }
    }

    // EMI draws its tree and favourite buttons off this; the inherited lists are empty so it is already false, and
    // saying so keeps a future input or output list from bringing the chrome back.
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, ResearchCategory.TEXTURE, ResearchCategory.SHEET, -4, -4,
                ResearchCategory.WIDTH + 8, ResearchCategory.HEIGHT + 8, 0, 0);
        widgets.addText(Component.literal("Title: " + entry.name()), LABEL_X, 6, TEXT_COLOR, false);
        widgets.addText(Component.literal("Rank: ").append(rankName(entry.requiredRank())), LABEL_X, 19,
                TEXT_COLOR, false);
        widgets.addText(Component.literal("Time: " + duration(entry.researchTime())), LABEL_X, 32, TEXT_COLOR,
                false);
        widgets.addText(Component.literal("Repeatable: " + (entry.repeatable() ? "Yes" : "No")), LABEL_X, 45,
                TEXT_COLOR, false);
        widgets.addText(Component.literal("Ingredients:"), LABEL_X, 57, TEXT_COLOR, false);
        widgets.addText(Component.literal("Rewards:"), LABEL_X, 91, TEXT_COLOR, false);

        int x = SLOT_X;
        for (ResearchPayload.Input input : entry.ingredients()) {
            EmiIngredient stack = ingredient(input);
            if (stack.isEmpty()) {
                RecipeSlots.addEmpty(widgets, x, INGREDIENTS_Y);
            } else {
                RecipeSlots.add(widgets, stack, x, INGREDIENTS_Y);
            }
            // JEI prints the amount under every ingredient and reward, single ones included.
            widgets.addText(Component.literal(Integer.toString(input.count())), x + COUNT_DX,
                    INGREDIENTS_Y + COUNT_DY, INGREDIENT_COUNT_COLOR, true);
            x += GROUP_STEP;
        }

        x = SLOT_X;
        for (int i = 0; i < entry.rewards().size(); i++) {
            RecipeSlots.add(widgets, EmiStack.of(entry.rewards().get(i)), x, REWARDS_Y).recipeContext(this);
            widgets.addText(Component.literal(Integer.toString(entry.rewardCounts().get(i))), x + COUNT_DX,
                    REWARDS_Y + COUNT_DY, REWARD_COUNT_COLOR, true);
            x += GROUP_STEP;
        }
    }

    private static EmiIngredient ingredient(ResearchPayload.Input input) {
        if (input.tag() != null) {
            return EmiIngredient.of(TagKey.create(Registries.ITEM, input.tag()), input.count());
        }
        return input.display().isEmpty() ? EmiStack.EMPTY : EmiStack.of(input.display());
    }

    // The host throws on a rank outside 1..5, which would cost the whole page; the bare number is the honest fallback.
    private static Component rankName(int rank) {
        if (rank < 1 || rank > RANK_KEYS.length) {
            return Component.literal(Integer.toString(rank));
        }
        return Component.translatable(RANK_KEYS[rank - 1]);
    }

    // The host's two shapes: "%dm %02ds" past a minute, whole seconds below it.
    private static String duration(int researchTime) {
        int seconds = researchTime / 20;
        int minutes = seconds / 60;
        return minutes > 0 ? String.format("%dm %02ds", minutes, seconds % 60) : seconds + "s";
    }
}
