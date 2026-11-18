package dev.wp.emisemble.compat.apothic_enchanting;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.payloads.SetRavenStatsPayload;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.RavenEnchantmentMenu;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** {@code apothic_enchanting}: the infusion category, its anvil pages, info pages and raven transfer handler. */
public final class ApothicEnchantingCompat {
    public static final String NAMESPACE = "apothic_enchanting";

    // The _jei sheet, not the container background: slot frames, name strip and stat bars.
    static final ResourceLocation TEXTURE = id("textures/gui/enchanting_jei.png");
    static final int SHEET = 256;
    static final int WIDTH = 170;
    static final int HEIGHT = 56;

    // The host's JEI uid, which is not Ench.RecipeTypes.INFUSION's key: JEMI suppresses a shimmed category by uid.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            id("enchanting"), EmiStack.of(Items.ENCHANTING_TABLE), "recipes.apothic_enchanting.infusion");

    /** The table power the scrap page quotes: a level-30 table, the top row of a vanilla enchanting table. */
    private static final int TABLE_POWER = 30;

    private static final int RAVEN_SLOT = 0;
    private static final int RAVEN_INVENTORY = 2;
    private static final int RAVEN_INVENTORY_SIZE = 36;

    private ApothicEnchantingCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Items.ENCHANTING_TABLE));
        registry.addWorkstation(CATEGORY, EmiStack.of(Ench.Items.APOTHIC_ENCHANTING_TABLE.value()));
        registry.addWorkstation(CATEGORY, EmiStack.of(Ench.Items.RAVEN_ENCHANTING_TABLE.value()));

        int recipes = infusion(registry);
        anvilPages(registry);
        infoPages(registry);
        registry.addRecipeHandler(Ench.Menus.RAVEN_ENCHANTING_TABLE, new RavenHandler());
        Emisemble.LOGGER.info("{}: raven table transfer handler registered", NAMESPACE);
        return recipes;
    }

    private static int infusion(EmiRegistry registry) {
        List<RecipeHolder<InfusionRecipe>> holders =
                new ArrayList<>(registry.getRecipeManager().getAllRecipesFor(Ench.RecipeTypes.INFUSION));
        holders.sort(Comparator.comparingDouble(holder -> holder.value().getRequirements().eterna()));

        int added = 0;
        for (RecipeHolder<InfusionRecipe> holder : holders) {
            // Built inside the guard: one hostile recipe must not cost the whole category.
            try {
                registry.addRecipe(new EnchantingEmiRecipe(holder.id(), holder.value()));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("{}: skipping infusion recipe {}", NAMESPACE, holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("{}: {} infusion recipes registered", NAMESPACE, added);
        return added;
    }

    // EMI's anvil category, not a uid of ours: JEI's anvil pages are viewer-side and JEMI can show them twice.
    private static int anvilPages(EmiRegistry registry) {
        if (VanillaEmiRecipeCategories.ANVIL_REPAIRING == null) {
            Emisemble.LOGGER.warn("{}: EMI's anvil category is not up yet, anvil pages skipped", NAMESPACE);
            return 0;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("{}: no level, anvil pages skipped", NAMESPACE);
            return 0;
        }

        Registry<Enchantment> enchants = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        // Its own source, not level.random: EMI registers plugins off the render thread and a pack with C2ME rejects
        // another thread reading that one, which drops the whole plugin.
        Holder<Enchantment> curse = enchants.getRandomElementOf(EnchantmentTags.CURSE, RandomSource.create())
                .orElse(enchants.getAny().get());

        // The host enchants the sword twice and never the book; the tome keeps ceil(n / 2), so one enchantment in gives
        // that one out - put the same enchantment on the sword and on the book it produces.
        ItemStack enchantedSword = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentInstance scrap = scrapTarget(enchants, enchantedSword);
        enchantedSword.enchant(scrap.enchantment, scrap.level);

        ItemStack cursedSword = new ItemStack(Items.DIAMOND_SWORD);
        cursedSword.enchant(curse, 1);

        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(scrap.enchantment, scrap.level);

        int added = 0;
        if (EmiRecipes.add(registry, new AnvilPage(id("/prismatic_cobweb"),
                EmiStack.of(cursedSword),
                EmiIngredient.of(Ingredient.of(Ench.Items.PRISMATIC_WEB.value())),
                EmiStack.of(new ItemStack(Items.DIAMOND_SWORD))))) {
            added++;
        }
        if (EmiRecipes.add(registry, new AnvilPage(id("/scrap_tome"),
                EmiStack.of(enchantedSword),
                EmiIngredient.of(Ingredient.of(Ench.Items.SCRAP_TOME.value())),
                EmiStack.of(enchantedBook)))) {
            added++;
        }
        if (EmiRecipes.add(registry, new AnvilPage(id("/anvil_repair"),
                EmiStack.of(new ItemStack(Blocks.DAMAGED_ANVIL)),
                EmiIngredient.of(Ingredient.of(Blocks.IRON_BLOCK)),
                EmiStack.of(new ItemStack(Blocks.ANVIL))))) {
            added++;
        }
        Emisemble.LOGGER.info("{}: {} anvil pages registered", NAMESPACE, added);
        return added;
    }

/** The enchantment the scrap page feeds the tome: sharpness on the diamond sword, at the level a level-30 table */
    private static EnchantmentInstance scrapTarget(Registry<Enchantment> enchants, ItemStack stack) {
        Holder<Enchantment> sharpness = enchants.getHolder(Enchantments.SHARPNESS).orElse(null);
        if (sharpness != null) {
            List<EnchantmentInstance> legal =
                    EnchantmentHelper.getAvailableEnchantmentResults(TABLE_POWER, stack, Stream.of(sharpness));
            if (!legal.isEmpty()) {
                return legal.getFirst();
            }
        }
        return new EnchantmentInstance(
                enchants.getRandomElementOf(EnchantmentTags.IN_ENCHANTING_TABLE, RandomSource.create())
                        .orElse(enchants.getAny().get()), 1);
    }

    private static int infoPages(EmiRegistry registry) {
        int added = infoPage(registry, Items.ENCHANTING_TABLE, "info.apothic_enchanting.enchanting",
                "/enchanting_table_info");
        added += infoPage(registry, Ench.Items.LIBRARY.value(), "info.apothic_enchanting.library", "/library_info");
        Emisemble.LOGGER.info("{}: {} info pages registered", NAMESPACE, added);
        return added;
    }

    private static int infoPage(EmiRegistry registry, Item item, String key, String path) {
        return EmiRecipes.add(registry, new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(item)),
                List.<Component>of(Component.translatable(key)),
                id(path))) ? 1 : 0;
    }

    /** Slot 0 is the infusion input, 2..37 the player's inventory, as the host's JEI handler declares them. */
    private static final class RavenHandler implements StandardRecipeHandler<RavenEnchantmentMenu> {
        @Override
        public List<Slot> getInputSources(RavenEnchantmentMenu menu) {
            // The infusion input first: EMI empties it into the inventory before it fills.
            return TransferSlots.sources(getCraftingSlots(menu), slots(menu, RAVEN_INVENTORY, RAVEN_INVENTORY_SIZE));
        }

        @Override
        public List<Slot> getCraftingSlots(RavenEnchantmentMenu menu) {
            return slots(menu, RAVEN_SLOT, 1);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == CATEGORY;
        }

        @Override
        public boolean craft(EmiRecipe recipe, EmiCraftContext<RavenEnchantmentMenu> context) {
            if (!StandardRecipeHandler.super.craft(recipe, context)) {
                return false;
            }
            if (!(recipe instanceof EnchantingEmiRecipe page)) {
                return true;
            }

            // The menu's stats are client-side only, so a fill has to push the recipe's sliders to the server too.
            Stats requirements = page.requirements();
            int eterna = Mth.ceil(requirements.eterna());
            int quanta = Mth.ceil(requirements.quanta());
            int arcana = Mth.ceil(requirements.arcana());

            context.getScreenHandler().setRavenStats(eterna, quanta, arcana);
            PacketDistributor.sendToServer(new SetRavenStatsPayload(eterna, quanta, arcana));
            return true;
        }

        private static List<Slot> slots(RavenEnchantmentMenu menu, int start, int count) {
            int end = Math.min(start + count, menu.slots.size());
            if (start >= end) {
                return List.of();
            }
            return menu.slots.subList(start, end);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
