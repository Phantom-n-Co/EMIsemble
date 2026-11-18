package dev.wp.emisemble.compat.mythsandlegends.research;

import com.ovinter.mythsandlegends.api.research.EnchantedBookReward;
import com.ovinter.mythsandlegends.api.research.IngredientDefinition;
import com.ovinter.mythsandlegends.api.research.ItemReward;
import com.ovinter.mythsandlegends.api.research.PotionReward;
import com.ovinter.mythsandlegends.api.research.RecipeReward;
import com.ovinter.mythsandlegends.api.research.ResearchDefinition;
import com.ovinter.mythsandlegends.api.research.Reward;
import com.ovinter.mythsandlegends.item.custom.ResearchNotesItem;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final ResearchDefinition definition;
    private final List<EmiStack> rewardSlots = new ArrayList<>();
    // JEI folds every recipe reward into one notes page, so these no longer line up with the reward list.
    private final List<Integer> rewardCounts = new ArrayList<>();

    public ResearchEmiRecipe(EmiRecipeCategory category, ResearchDefinition definition) {
        // Synthetic with its own prefix: a research shares its id with a real recipe, and one of them is named
        // stone_sanctum, which would otherwise collide with the ritual's own page.
        super(category, ResourceLocation.fromNamespaceAndPath(definition.id().getNamespace(),
                        "/research/" + definition.id().getPath()),
                ResearchCategory.WIDTH, ResearchCategory.HEIGHT);
        this.definition = definition;
        for (IngredientDefinition ingredient : definition.ingredients()) {
            EmiIngredient stack = ingredient(ingredient);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        boolean notesDrawn = false;
        for (Reward reward : definition.rewards()) {
            ItemStack stack;
            int count = 1;
            if (reward instanceof ItemReward item) {
                stack = item.stack().copy();
                count = item.count();
            } else if (reward instanceof RecipeReward) {
                if (notesDrawn) {
                    continue;
                }
                stack = ResearchNotesItem.create(definition.id());
                notesDrawn = true;
            } else if (reward instanceof EnchantedBookReward book) {
                stack = enchantedBook(book);
                count = book.count();
            } else if (reward instanceof PotionReward potion) {
                stack = potionStack(potion);
                count = potion.count();
            } else {
                continue;
            }
            if (stack.isEmpty()) {
                continue;
            }
            EmiStack output = EmiStack.of(stack);
            rewardSlots.add(output);
            rewardCounts.add(count);
            outputs.add(output);
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
        widgets.addText(Component.literal("Title: " + definition.name()), LABEL_X, 6, TEXT_COLOR, false);
        // The host's literal carries a trailing space, which javap hides.
        widgets.addText(Component.literal("Rank: ").append(rankName(definition.requiredRank())), LABEL_X, 19,
                TEXT_COLOR, false);
        widgets.addText(Component.literal("Time: " + duration(definition.researchTime())), LABEL_X, 32, TEXT_COLOR,
                false);
        widgets.addText(Component.literal("Repeatable: " + (definition.repeatable() ? "Yes" : "No")), LABEL_X, 45,
                TEXT_COLOR, false);
        widgets.addText(Component.literal("Ingredients:"), LABEL_X, 57, TEXT_COLOR, false);
        widgets.addText(Component.literal("Rewards:"), LABEL_X, 91, TEXT_COLOR, false);

        int x = SLOT_X;
        for (IngredientDefinition ingredient : definition.ingredients()) {
            EmiIngredient stack = ingredient(ingredient);
            if (stack.isEmpty()) {
                RecipeSlots.addEmpty(widgets, x, INGREDIENTS_Y);
            } else {
                RecipeSlots.add(widgets, stack, x, INGREDIENTS_Y);
            }
            // JEI prints the amount under every ingredient and reward, single ones included.
            widgets.addText(Component.literal(Integer.toString(ingredient.count())), x + COUNT_DX,
                    INGREDIENTS_Y + COUNT_DY, INGREDIENT_COUNT_COLOR, true);
            x += GROUP_STEP;
        }

        x = SLOT_X;
        for (int i = 0; i < rewardSlots.size(); i++) {
            RecipeSlots.add(widgets, rewardSlots.get(i), x, REWARDS_Y).recipeContext(this);
            widgets.addText(Component.literal(Integer.toString(rewardCounts.get(i))), x + COUNT_DX,
                    REWARDS_Y + COUNT_DY, REWARD_COUNT_COLOR, true);
            x += GROUP_STEP;
        }
    }

    private static EmiIngredient ingredient(IngredientDefinition definition) {
        if (definition.item() != null) {
            return EmiStack.of(definition.createDisplayStack());
        }
        if (definition.tag() != null) {
            return EmiIngredient.of(TagKey.create(Registries.ITEM, definition.tag()), definition.count());
        }
        return EmiStack.EMPTY;
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

    private static ItemStack enchantedBook(EnchantedBookReward reward) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, reward.count());
        RegistryAccess access = registryAccess();
        if (access == null) {
            return stack;
        }
        access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ResourceKey.create(Registries.ENCHANTMENT, reward.enchantment()))
                .ifPresent(holder -> stack.enchant(holder, reward.level()));
        return stack;
    }

    private static ItemStack potionStack(PotionReward reward) {
        ItemStack stack = new ItemStack(reward.container().item(), reward.count());
        RegistryAccess access = registryAccess();
        if (access == null) {
            return stack;
        }
        if (reward.potionId() != null) {
            access.registryOrThrow(Registries.POTION)
                    .getHolder(ResourceKey.create(Registries.POTION, reward.potionId()))
                    .ifPresent(holder -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
            return stack;
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (PotionReward.CustomEffect effect : reward.customEffects()) {
            access.registryOrThrow(Registries.MOB_EFFECT)
                    .getHolder(ResourceKey.create(Registries.MOB_EFFECT, effect.effect()))
                    .ifPresent(holder -> effects.add(new MobEffectInstance(holder, effect.duration(),
                            effect.amplifier())));
        }
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects));
        return stack;
    }

    // Registries are datapack-bound, so the reward stacks can only be built while a level exists.
    private static RegistryAccess registryAccess() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }
}
