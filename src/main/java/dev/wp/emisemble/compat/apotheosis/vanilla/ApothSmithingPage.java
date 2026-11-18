package dev.wp.emisemble.compat.apotheosis.vanilla;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.wp.emisemble.Emisemble;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

import java.util.List;
import java.util.function.Predicate;

/** A smithing page rebuilt around a worked example rather than the host extension's dummy samples. */
final class ApothSmithingPage implements EmiRecipe {
    /**
     * Tried in order until one answers the page: gear a player actually sockets and affixes. The host's own dummy
     * list spans a golden sword, a diamond pickaxe, a stone axe, an iron chestplate and a bow.
     */
    private static final List<Item> CANDIDATES = List.of(
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_CHESTPLATE, Items.BOW);

    /** The purity every socket page uses, matching the extension's withdrawal samples. */
    private static final Purity SAMPLE_PURITY = Purity.FLAWED;

    private static final int WIDTH = 112;
    private static final int HEIGHT = 18;

    private final ResourceLocation id;
    private final EmiIngredient base;
    private final EmiIngredient addition;
    private final List<EmiStack> outputs;

    private ApothSmithingPage(ResourceLocation id, EmiIngredient base, EmiIngredient addition, List<EmiStack> outputs) {
        this.id = id;
        this.base = base;
        this.addition = addition;
        this.outputs = List.copyOf(outputs);
    }

    private ApothSmithingPage(ResourceLocation id, ItemStack base, ItemStack addition, ItemStack output) {
        this(id, EmiStack.of(base), EmiStack.of(addition), List.of(EmiStack.of(output)));
    }

    /**
     * {@code SocketingRecipe}, which the host gives no extension: the recipe's result stack is empty and its base and
     * template ingredients are empty too, so both viewers draw a bare gem item and no result.
     */
    static ApothSmithingPage socketing(ResourceLocation id) {
        ItemStack base = socketableSample();
        ItemStack gem = gemFor(base);
        return new ApothSmithingPage(id, base, gem, socketGem(base, gem));
    }

    /** {@code AddSocketsExtension}: a plain item in, the same item one socket richer. */
    static ApothSmithingPage addSockets(ResourceLocation id, AddSocketsRecipe recipe) {
        ItemStack base = socketableSample();
        // The probe came back with a socket so the page could prove the item takes one; the input itself has none.
        base.remove(Apoth.Components.SOCKETS);
        ItemStack result = base.copy();
        // AddSocketsRecipe#assemble: one socket on top of whatever the item already had.
        SocketHelper.setSockets(result, SocketHelper.getSockets(base) + 1);
        result.set(DataComponents.LORE, lore(Apotheosis.lang("text", "socket_limit", recipe.getMaxSockets())));
        return new ApothSmithingPage(id, EmiStack.of(base), EmiIngredient.of(recipe.getInput()),
                List.of(EmiStack.of(result)));
    }

    /** {@code WithdrawalExtension}: a real gem is pulled back out, the sockets stay behind. */
    static ApothSmithingPage withdrawal(ResourceLocation id) {
        ItemStack base = socketableSample();
        ItemStack gem = gemFor(base);
        ItemStack socketed = socketGem(base, gem);
        ItemStack result = socketed.copy();
        // WithdrawalRecipe#assemble: empty the gem list, leave the socket count alone.
        SocketHelper.setGems(result, SocketedGems.EMPTY);
        result.set(DataComponents.LORE, lore(Apotheosis.lang("text", "gems_returned")));
        return new ApothSmithingPage(id, socketed, new ItemStack(Apoth.Items.SIGIL_OF_WITHDRAWAL.value()), result);
    }

    /** {@code UnnamingExtension}: a real affix name in, the name blanked but still tinted in the rarity's colour. */
    static ApothSmithingPage unnaming(ResourceLocation id) {
        ItemStack base = affixSample(stack -> stack.has(Apoth.Components.AFFIX_NAME));
        ItemStack result = base.copy();
        // UnnamingExtension's own output: translatable("%2$s", "", "") in the item's rarity colour, so the tint stays.
        LootRarity rarity = AffixHelper.getRarity(result).getOptional().orElseGet(ApothSmithingPage::requireRarity);
        AffixHelper.setName(result, Component.translatable("%2$s", "", "")
                .withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false)));
        return new ApothSmithingPage(id, base, new ItemStack(Apoth.Items.SIGIL_OF_UNNAMING.value()), result);
    }

/**
 * {@code MaliceExtension}: {@code MaliceRecipe#assemble} only stamps the marker — the client's tooltip hook turns
 * it
 */
    static ApothSmithingPage malice(ResourceLocation id) {
        ItemStack base = affixSample(AffixHelper::canApplyMalice);
        ItemStack result = base.copy();
        result.set(Apoth.Components.MALICE_MARKER, true);
        return new ApothSmithingPage(id, base, new ItemStack(Apoth.Items.SIGIL_OF_MALICE.value()), result);
    }

    /** {@code SupremacyExtension}: every affix raised by the recipe's own factor. */
    static ApothSmithingPage supremacy(ResourceLocation id) {
        ItemStack base = affixSample(AffixHelper::hasAffixes);
        ItemStack result = base.copy();
        AffixHelper.applySupremacy(result); // SupremacyRecipe#assemble
        return new ApothSmithingPage(id, base, new ItemStack(Apoth.Items.SIGIL_OF_SUPREMACY.value()), result);
    }

/** The first candidate the host gives a socket to, as a stack that already carries one. Probing the socket count */
    private static ItemStack socketableSample() {
        for (Item item : CANDIDATES) {
            ItemStack stack = new ItemStack(item);
            SocketHelper.setSockets(stack, 1);
            if (!SocketHelper.getGems(stack).isEmpty()) {
                return stack;
            }
        }
        throw new IllegalStateException("no sample item takes a socket");
    }

/**
 * The first gem the host's own socketing test accepts for {@code base}: a real gem stack, not the bare gem item.
 */
    private static ItemStack gemFor(ItemStack base) {
        for (ItemStack gem : gems()) {
            if (SocketHelper.canSocketGemInItem(base, gem)) {
                return gem;
            }
        }
        throw new IllegalStateException("no gem fits " + base.getItem());
    }

/**
 * Every gem at the sample purity, through the host's own {@code GemIngredient} filter rather than a raw registry
 * scan.
 */
    private static List<ItemStack> gems() {
        return new GemIngredient(SAMPLE_PURITY).getItems().filter(gem -> !gem.isEmpty()).toList();
    }

    /** The host's own socketing call; an empty result means it would not have taken the gem. */
    private static ItemStack socketGem(ItemStack base, ItemStack gem) {
        ItemStack result = SocketHelper.socketGemInItem(base.copy(), gem.copy());
        if (result.isEmpty()) {
            throw new IllegalStateException("the host refused to socket " + gem.getItem() + " into " + base.getItem());
        }
        return result;
    }

    /** The first candidate rolled at the top rarity that satisfies the page's own precondition. */
    private static ItemStack affixSample(Predicate<ItemStack> requirement) {
        LootRarity rarity = requireRarity();
        // Seeded, so every client and every reload rolls the same item.
        RandomSource random = new LegacyRandomSource(0L);
        for (Item item : CANDIDATES) {
            ItemStack stack = new ItemStack(item);
            try {
                LootController.createLootItem(stack, rarity, GenContext.dummy(random));
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("apotheosis: could not roll a sample affix item for {}", item, t);
                continue;
            }
            if (requirement.test(stack)) {
                return stack;
            }
        }
        throw new IllegalStateException("no sample item matched the page's requirement");
    }

    private static LootRarity requireRarity() {
        LootRarity rarity = topRarity();
        if (rarity == null) {
            throw new IllegalStateException("rarities are unavailable, so no affixed sample can be rolled");
        }
        return rarity;
    }

    private static LootRarity topRarity() {
        try {
            List<LootRarity> rarities = RarityRegistry.getSortedRarities();
            return rarities.isEmpty() ? null : rarities.getLast();
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("apotheosis: rarities are unavailable, smithing samples stay unaffixed", t);
            return null;
        }
    }

/**
 * The host's own annotation: {@code AddSocketsExtension} and {@code WithdrawalExtension} set it as the output's
 * lore.
 */
    private static ItemLore lore(MutableComponent line) {
        MutableComponent styled = line.withStyle(ChatFormatting.AQUA);
        return new ItemLore(List.of(styled), List.of(styled));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.SMITHING;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    /** The template slot is empty: none of these recipes takes one, matching the host's {@code Ingredient.EMPTY}. */
    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.EMPTY, this.base, this.addition);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    /** An affixed item cannot be decomposed into a crafting tree, and the socket pages' inputs are samples too. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
        widgets.addSlot(EmiStack.EMPTY, 0, 0);
        widgets.addSlot(this.base, 18, 0);
        widgets.addSlot(this.addition, 36, 0);
        widgets.addSlot(this.outputs.size() == 1 ? this.outputs.get(0) : new ListEmiIngredient(this.outputs, 1), 94, 0)
                .recipeContext(this);
    }
}
