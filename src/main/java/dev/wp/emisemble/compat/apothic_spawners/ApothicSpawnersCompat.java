package dev.wp.emisemble.compat.apothic_spawners;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apothic_spawners.ASConfig;
import dev.shadowsoffire.apothic_spawners.ASObjects;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** {@code apothic_spawners}: the spawner modifier category and the info pages on the spawner and every egg. */
public final class ApothicSpawnersCompat {
    public static final String NAMESPACE = "apothic_spawners";

    // The host's JEI uid (plural), not ASObjects.SPAWNER_MODIFIER's key (singular): JEMI suppresses the shim by uid.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            id("spawner_modifiers"), EmiStack.of(Items.SPAWNER), "title.apothic_spawners.spawner");

    static final ResourceLocation TEXTURE = id("textures/gui/spawner_jei.png");
    static final int SHEET = 256;
    static final int WIDTH = 169;
    static final int HEIGHT = 75;

    private ApothicSpawnersCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.SPAWNER));

        List<RecipeHolder<SpawnerModifier>> holders =
                new ArrayList<>(registry.getRecipeManager().getAllRecipesFor(ASObjects.SPAWNER_MODIFIER.get()));
        // Host order: descending ids, which walks spawner_modifiers/_inverse/* to the front.
        holders.sort((left, right) -> -left.id().compareNamespaced(right.id()));

        int added = 0;
        for (RecipeHolder<SpawnerModifier> holder : holders) {
            // Built inside the guard: one hostile recipe must not cost the whole category.
            try {
                registry.addRecipe(new SpawnerEmiRecipe(holder.id(), holder.value()));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("{}: skipping spawner modifier {}", NAMESPACE, holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("{}: {} spawner modifier recipes registered", NAMESPACE, added);

        int info = spawnerInfo(registry) + capturingInfo(registry);
        Emisemble.LOGGER.info("{}: {} info pages registered", NAMESPACE, info);
        return added;
    }

    private static int spawnerInfo(EmiRegistry registry) {
        Component text;
        if (ASConfig.spawnerSilkLevel == -1) {
            text = lang("info", "spawner.no_silk");
        } else if (ASConfig.spawnerSilkLevel == 0) {
            // The host asks for "always_drop", which it does not ship; the key it ships is "spawner.always_drop".
            text = lang("info", "spawner.always_drop");
        } else {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return 0;
            }
            Holder<Enchantment> silk = level.holder(Enchantments.SILK_TOUCH).orElse(null);
            // The host registers no page when the level has no silk touch, rather than an empty one.
            if (silk == null) {
                return 0;
            }
            text = lang("info", "spawner", Enchantment.getFullname(silk, ASConfig.spawnerSilkLevel)
                    .copy().withStyle(ChatFormatting.DARK_BLUE));
        }

        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(Items.SPAWNER)),
                InfoText.lines(text),
                id("/spawner_info"))) ? 1 : 0;
    }

    private static int capturingInfo(EmiRegistry registry) {
        List<EmiIngredient> eggs = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SpawnEggItem) {
                eggs.add(EmiStack.of(item));
            }
        }
        if (eggs.isEmpty()) {
            return 0;
        }

        Component text = lang("info", "capturing",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ASConfig.capturingDropChance * 100));
        // EMI shows 18 slots and folds the rest into the last one, so one ingredient is the only way to keep them all.
        boolean added = EmiRecipes.add(registry, new EmiInfoRecipe(
                List.of(EmiIngredient.of(eggs)),
                InfoText.lines(text),
                id("/capturing_info")));
        if (added) {
            Emisemble.LOGGER.info("{}: {} spawn eggs on one capturing info page", NAMESPACE, eggs.size());
        }
        return added ? 1 : 0;
    }

    // The host prefixes the type itself, so these are info.apothic_spawners.* and misc.apothic_spawners.*.
    static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(type + ".apothic_spawners." + path, args);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
