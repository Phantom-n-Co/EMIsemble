package dev.wp.emisemble.compat.thaumaturge.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** One research construct: turning block model, material list, entry icon and aspect cost. An information page. */
public final class ResearchConstructEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 144;
    static final int HEIGHT = 136;

    private static final int TITLE_X = 4;
    private static final int TITLE_Y = 4;
    private static final int TITLE_COLOUR = 0xFF404040;

    private static final int PREVIEW_CENTRE_X = 66;
    private static final int PREVIEW_CENTRE_Y = 40;
    private static final int PREVIEW_BAND_Y = 0;
    private static final int PREVIEW_BAND_HEIGHT = 76;
    private static final float PREVIEW_MAX_WIDTH = 110.0F;
    private static final float PREVIEW_MAX_HEIGHT = 66.0F;
    private static final float PREVIEW_MAX_SCALE = 12.0F;

    // JEI's material grid: (5,78) stepping 22 across and 20 down, six per row.
    private static final int MATERIAL_X = 4;
    private static final int MATERIAL_Y = 77;
    private static final int MATERIAL_STEP_X = 22;
    private static final int MATERIAL_STEP_Y = 20;
    private static final int MATERIAL_PER_ROW = 6;

    // JEI's result slot at (124,38); the barrier sits on the item, so it keeps the item's coordinates.
    private static final int RESULT_X = 123;
    private static final int RESULT_Y = 37;
    private static final int BARRIER_X = 124;
    private static final int BARRIER_Y = 38;
    private static final int BARRIER_TOOLTIP_X = 122;
    private static final int BARRIER_TOOLTIP_Y = 36;
    private static final int BARRIER_TOOLTIP_SIZE = 20;

    // JEI's aspect row: y 118, 20 apart, centred on the page.
    private static final int ASPECT_Y = 117;
    private static final int ASPECT_STEP = 20;
    private static final int ASPECT_CENTRE_X = 72;

    private final ResourceLocation entryId;
    private final int stageIndex;
    private final IResearchEntry entry;
    private final ResearchConstruct construct;
    private final List<EmiIngredient> materialSlots = new ArrayList<>();
    private final Map<BlockPos, BlockState> blocks;
    private final ItemStack icon;

    public ResearchConstructEmiRecipe(ResourceLocation entryId, int stageIndex, IResearchEntry entry, ResearchConstruct construct) {
        super(ResearchConstructCategory.CATEGORY, syntheticId(entryId, stageIndex), WIDTH, HEIGHT);
        this.entryId = entryId;
        this.stageIndex = stageIndex;
        this.entry = entry;
        this.construct = construct;
        this.blocks = ResearchConstructPreview.blocks(construct);
        this.icon = firstItemIcon(entry);

        Map<String, Integer> materials = new LinkedHashMap<>();
        for (String cell : construct.cells()) {
            if (!cell.isEmpty()) {
                materials.merge(cell, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> material : materials.entrySet()) {
            EmiIngredient representation = representation(material.getKey(), material.getValue());
            if (!representation.isEmpty()) {
                materialSlots.add(representation);
            }
        }
        inputs.addAll(materialSlots);
        for (AspectInstance cost : construct.cost().entries()) {
            inputs.add(new AspectEmiStack(cost.aspect(), cost.amount()));
        }
        if (!icon.isEmpty()) {
            outputs.add(EmiStack.of(icon));
        }
    }

    // Synthetic, in EMI's namespace:/path form: it cannot collide with a real recipe id.
    private static ResourceLocation syntheticId(ResourceLocation entryId, int stageIndex) {
        return ResourceLocation.fromNamespaceAndPath(entryId.getNamespace(), "/" + entryId.getPath() + "/construct/" + stageIndex);
    }

    private static ItemStack firstItemIcon(IResearchEntry entry) {
        return entry.icons().stream()
                .filter(entryIcon -> entryIcon.kind() == ResearchIcon.Kind.ITEM)
                .map(ResearchIcon::id)
                .map(BuiltInRegistries.ITEM::get)
                .filter(item -> item != Items.AIR)
                .findFirst()
                .map(Item::getDefaultInstance)
                .orElse(ItemStack.EMPTY);
    }

    public ResourceLocation entryId() {
        return entryId;
    }

    public ResearchConstruct construct() {
        return construct;
    }

    /** The materials are a build list and the result is an icon, so no recipe tree and nothing craftable. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, PREVIEW_BAND_Y, WIDTH, PREVIEW_BAND_HEIGHT, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                ResearchConstructPreview.render(draw, blocks, PREVIEW_CENTRE_X, PREVIEW_CENTRE_Y,
                        PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT, PREVIEW_MAX_SCALE));

        widgets.addText(Component.translatable(entry.nameKey()), TITLE_X, TITLE_Y, TITLE_COLOUR, false);

        for (int i = 0; i < materialSlots.size(); i++) {
            int slotX = MATERIAL_X + (i % MATERIAL_PER_ROW) * MATERIAL_STEP_X;
            int slotY = MATERIAL_Y + (i / MATERIAL_PER_ROW) * MATERIAL_STEP_Y;
            widgets.addSlot(materialSlots.get(i), slotX, slotY).drawBack(false);
        }

        List<AspectInstance> cost = construct.cost().entries();
        int totalWidth = cost.isEmpty() ? 0 : ASPECT_STEP * (cost.size() - 1) + 16;
        int costX = ASPECT_CENTRE_X - totalWidth / 2 - 1;
        for (int i = 0; i < cost.size(); i++) {
            AspectInstance aspect = cost.get(i);
            widgets.addSlot(new AspectEmiStack(aspect.aspect(), aspect.amount()), costX + i * ASPECT_STEP, ASPECT_Y)
                    .drawBack(false).recipeContext(this);
        }

        boolean gated = !passesGate();
        if (gated) {
            // Before the result slot: EMI takes the first hovered widget that has tooltip lines.
            widgets.addTooltip(this::missingResearchTooltip,
                    BARRIER_TOOLTIP_X, BARRIER_TOOLTIP_Y, BARRIER_TOOLTIP_SIZE, BARRIER_TOOLTIP_SIZE);
        }

        if (!icon.isEmpty()) {
            widgets.addSlot(EmiStack.of(icon), RESULT_X, RESULT_Y).drawBack(false);
        }

        if (gated) {
            widgets.addDrawable(BARRIER_X, BARRIER_Y, 16, 16,
                    (GuiGraphics draw, int mouseX, int mouseY, float delta) -> draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }

        // Last, so every slot keeps its own tooltip and this one answers for the open page around them.
        widgets.addTooltip(this::summaryTooltip, 0, 0, WIDTH, HEIGHT);
    }

    private List<ClientTooltipComponent> summaryTooltip(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(entry.nameKey()));

        List<AspectInstance> cost = construct.cost().entries();
        if (!cost.isEmpty()) {
            MutableComponent total = Component.empty();
            for (int i = 0; i < cost.size(); i++) {
                AspectInstance instance = cost.get(i);
                if (i > 0) {
                    total.append(Component.literal(", "));
                }
                total.append(AspectComponents.name(instance.aspect()).withStyle(style -> style.withColor(instance.aspect().value().color())));
                total.append(Component.literal(" " + instance.amount()));
            }
            lines.add(Component.translatable("gui.thaumaturge.thaumonomicon.research_cost")
                    .append(Component.literal(" "))
                    .append(total));
        }

        return EmiRecipes.tooltip(lines);
    }

    private List<ClientTooltipComponent> missingResearchTooltip(int mouseX, int mouseY) {
        if (passesGate()) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jei.thaumaturge.research.missing_research").withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("- ").append(Component.translatable(entry.nameKey())).withStyle(ChatFormatting.RED));
        return EmiRecipes.tooltip(lines);
    }

    private ResearchGate gate() {
        return new ResearchGate(entryId, Optional.of(stageIndex), false);
    }

    private boolean passesGate() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ResearchGate.passes(player, gate());
    }

    /** Every candidate a material spec can be, each carrying the total cell count JEI stamps on the alternatives. */
    private static EmiIngredient representation(String spec, int count) {
        List<EmiStack> stacks = new ArrayList<>();
        for (Item item : items(spec)) {
            stacks.add(EmiStack.of(new ItemStack(item), count));
        }
        return EmiIngredient.of(stacks);
    }

    /** The preview needs one block per cell, so a spec resolves to its first candidate — the host picks one too. */
    static ItemStack firstStack(String spec) {
        List<Item> items = items(spec);
        return items.isEmpty() ? ItemStack.EMPTY : new ItemStack(items.get(0));
    }

    // A spec is either an item id or #tag; anything unresolvable contributes nothing and the caller skips the slot.
    private static List<Item> items(String spec) {
        if (spec.isEmpty()) {
            return List.of();
        }
        List<Item> items = new ArrayList<>();
        if (spec.charAt(0) == '#') {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(spec.substring(1)));
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                items.add(holder.value());
            }
        } else {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(spec));
            if (item != Items.AIR) {
                items.add(item);
            }
        }
        return items;
    }
}
