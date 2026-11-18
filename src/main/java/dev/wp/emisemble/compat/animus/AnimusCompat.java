package dev.wp.emisemble.compat.animus;

import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.InfoText;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/** Animus's sanguine dynamo category, its workstation, the guide book's search aliases and the six info pages. */
public final class AnimusCompat {
    public static final String NAMESPACE = "animusnv";

    public static final EmiRecipeCategory SANGUINE_DYNAMO = new TranslatedCategory(
            EmiRecipes.categoryId(AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get()),
            EmiStack.of(AnimusBlocks.BLOCK_SANGUINE_DYNAMO.get()),
            "jei.animusnv.category.sanguine_dynamo");

    private static final List<String> SCROLLS = List.of("sanguine_scroll_rasa", "sanguine_scroll_robur",
            "sanguine_scroll_animata", "sanguine_scroll_spiritus", "sanguine_scroll_aetherea");

    private AnimusCompat() {}

    public static void register(EmiRegistry registry) {
        registry.addCategory(SANGUINE_DYNAMO);
        registry.addWorkstation(SANGUINE_DYNAMO, EmiStack.of(AnimusBlocks.BLOCK_SANGUINE_DYNAMO.get()));

        int added = EmiRecipes.addAll(registry, AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get(),
                SanguineDynamoEmiRecipe::new);
        Emisemble.LOGGER.info("{}: {} sanguine dynamo recipes registered", NAMESPACE, added);

        EmiStack guide = EmiStack.of(AnimusItems.GUIDE_BOOK.get());
        registry.addAlias(guide, Component.translatable("jei.animusnv.alias.guide"));
        registry.addAlias(guide, Component.translatable("jei.animusnv.alias.book"));
        registry.addAlias(guide, Component.translatable("jei.animusnv.alias.manual"));

        info(registry, "antilife", EmiStack.of(AnimusItems.ANTILIFE_BUCKET.get()), "jei.animusnv.antilife.info");
        info(registry, "living_terra", EmiStack.of(AnimusItems.LIVING_TERRA_BUCKET.get()),
                "jei.animusnv.living_terra.info");
        info(registry, "blood_apple", EmiStack.of(AnimusItems.BLOOD_APPLE.get()), "jei.animusnv.blood_apple.info");
        info(registry, "antilife_block", EmiStack.of(AnimusBlocks.BLOCK_ANTILIFE.get()),
                "jei.animusnv.antilife_block.info");

        EmiStack rectifier = compatibleItem("evilcraft", "sanguine_rectifier");
        if (rectifier != null) {
            info(registry, "sanguine_rectifier", rectifier, "jei.animusnv.sanguine_rectifier.info");
        }

        List<EmiStack> scrolls = new ArrayList<>();
        if (ModList.get().isLoaded("irons_spellbooks")) {
            for (String path : SCROLLS) {
                EmiStack scroll = item(path);
                if (scroll != null) {
                    scrolls.add(scroll);
                }
            }
        }
        if (!scrolls.isEmpty()) {
            info(registry, "sanguine_scroll", EmiIngredient.of(scrolls), "jei.animusnv.sanguine_scroll.info");
        }

        // The host drops these two from JEI's ingredient list when Malum is absent, since they only work with it.
        if (!ModList.get().isLoaded("malum")) {
            ResourceLocation scythe = AnimusItems.RUNIC_SENTIENT_SCYTHE.getId();
            ResourceLocation hand = AnimusItems.HAND_OF_DEATH.getId();
            registry.removeEmiStacks(stack -> scythe.equals(stack.getId()) || hand.equals(stack.getId()));
        }
    }

    private static void info(EmiRegistry registry, String path, EmiIngredient stacks, String key) {
        if (stacks.isEmpty()) {
            return;
        }
        ResourceLocation id = EmiRecipes.synthetic(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "info/" + path));
        EmiRecipes.add(registry, new EmiInfoRecipe(List.of(stacks), InfoText.lines(Component.translatable(key)), id));
    }

    /** An item the host registers only for one of its compat modules, looked up the way the host's plugin does. */
    private static EmiStack compatibleItem(String modId, String path) {
        return ModList.get().isLoaded(modId) ? item(path) : null;
    }

    private static EmiStack item(String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(NAMESPACE, path));
        return item == Items.AIR ? null : EmiStack.of(item);
    }
}
