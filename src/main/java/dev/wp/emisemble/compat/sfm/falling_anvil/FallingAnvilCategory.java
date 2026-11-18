package dev.wp.emisemble.compat.sfm.falling_anvil;

import ca.teamdman.sfm.common.registry.registration.SFMBlockTags;
import ca.teamdman.sfm.common.util.SFMComponentUtils;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.sfm.printing_press.PrintingPressCategory;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code sfm:falling_anvil}: what a stack dropped on an anvil turns into. The host invents this uid for pages it
 * derives from the printing press recipes, plus one each for disenchanting and for experience shards.
 */
public final class FallingAnvilCategory {
    static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("sfm", "falling_anvil");
    // The host overrides getWidth/getHeight to 80x54 and draws no background, so the page is bare slots.
    static final int WIDTH = 80;
    static final int HEIGHT = 54;

    static final EmiIngredient ANVILS = EmiIngredient.of(List.of(
            EmiStack.of(Items.ANVIL), EmiStack.of(Items.CHIPPED_ANVIL), EmiStack.of(Items.DAMAGED_ANVIL)));

    // The host's own carrier list: what it offers each enchantment on, in its order.
    private static final List<Item> DISENCHANTABLE = List.of(
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SWORD,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SWORD,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SWORD,
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
            Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SWORD,
            Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.FISHING_ROD, Items.STICK);

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(Items.ANVIL), "gui.jei.category.sfm.falling_anvil");

    private FallingAnvilCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.ANVIL));

        EmiIngredient forming = blocksIn(SFMBlockTags.ANVIL_PRINTING_PRESS_FORMING,
                "gui.jei.category.sfm.falling_anvil.consumed");
        EmiIngredient disenchanting = blocksIn(SFMBlockTags.ANVIL_DISENCHANTING,
                "gui.jei.category.sfm.falling_anvil.not_consumed");

        // The host forms one page per printing press recipe, so these carry over its count without extra data.
        int added = EmiRecipes.addAll(registry, PrintingPressCategory.type(),
                holder -> new FallingAnvilFormPage(holder.id(), holder.value(), forming));

        List<EmiStack> items = new ArrayList<>();
        List<EmiStack> books = new ArrayList<>();
        enchantmentVariants(items, books);
        if (items.isEmpty()) {
            Emisemble.LOGGER.warn("sfm: the level has no enchantments, so no falling anvil disenchanting page");
        } else if (EmiRecipes.add(registry, new FallingAnvilDisenchantPage(items, books, disenchanting))) {
            added++;
        }
        if (EmiRecipes.add(registry, new FallingAnvilShardPage(disenchanting))) {
            added++;
        }

        Emisemble.LOGGER.info("sfm: {} falling anvil recipes registered", added);
        return added;
    }

    /** The blocks a tag names, each carrying the lore line the host appends to it. */
    private static EmiIngredient blocksIn(TagKey<Block> tag, String loreKey) {
        Component line = Component.translatable(loreKey);
        List<EmiStack> stacks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!SFMBlockTags.hasBlockTag(block, tag)) {
                continue;
            }
            ItemStack stack = new ItemStack(block);
            if (stack.isEmpty()) {
                continue;
            }
            SFMComponentUtils.appendLore(stack, line);
            stacks.add(EmiStack.of(stack));
        }
        return EmiIngredient.of(stacks);
    }

    /** Every enchantment the level has, on every carrier it accepts, at each of its levels. */
    private static void enchantmentVariants(List<EmiStack> items, List<EmiStack> books) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        for (Holder.Reference<Enchantment> holder :
                Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().toList()) {
            Enchantment enchantment = holder.value();
            for (Item item : DISENCHANTABLE) {
                ItemStack base = new ItemStack(item);
                // A stick takes every enchantment, which is how the host shows one it cannot apply to a tool.
                if (!enchantment.definition().supportedItems().contains(base.getItemHolder()) && item != Items.STICK) {
                    continue;
                }
                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    ItemStack enchanted = base.copy();
                    enchanted.enchant(holder, level);
                    items.add(EmiStack.of(enchanted));
                    books.add(EmiStack.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level))));
                }
            }
        }
    }
}
