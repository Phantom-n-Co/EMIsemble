package dev.wp.emisemble.compat.apothic_enchanting;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.ApothEnchantmentScreen;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One infusion page: the input and result, the rune name the host scrumbles, and its three stat bars. */
public final class EnchantingEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 6;
    private static final int INPUT_Y = 6;
    private static final int OUTPUT_X = 37;
    private static final int OUTPUT_Y = 6;

    private static final int LABEL_X = 16;
    private static final int ETERNA_Y = 26;
    private static final int QUANTA_Y = 36;
    private static final int ARCANA_Y = 46;
    private static final int ETERNA_COLOR = 0x3DB53D;
    private static final int QUANTA_COLOR = 0xFC5454;
    private static final int ARCANA_COLOR = 0xA800A8;

    private static final int BAR_X = 56;
    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 5;
    private static final int FILL_V = 56;
    private static final int REMAINDER_V = 90;

    private static final int STRIP_X = 57;
    private static final int STRIP_Y = 4;
    private static final int STRIP_WIDTH = 108;
    private static final int STRIP_HEIGHT = 19;
    private static final int STRIP_V = 71;

    private static final int NAME_X = 77;
    private static final int NAME_Y = 6;
    private static final int NAME_MAX_WIDTH = 86;
    private static final int LEVEL_DY = 7;
    private static final int NAME_COLOR = 6839882;
    private static final int NAME_HOVER_COLOR = 16777088;
    private static final int LEVEL_COLOR = 8453920;

    private final EmiIngredient input;
    private final EmiStack output;
    private final List<EmiStack> charmPool;
    private final int unique;
    private final Stats requirements;
    private final Stats maxRequirements;
    private final int seed;

    EnchantingEmiRecipe(ResourceLocation id, InfusionRecipe recipe) {
        super(ApothicEnchantingCompat.CATEGORY, id, ApothicEnchantingCompat.WIDTH, ApothicEnchantingCompat.HEIGHT);
        this.input = EmiIngredient.of(recipe.getInput());
        // Both slots share it: EMI re-seeds a generated slot from (second ^ unique).
        this.unique = id.hashCode();
        this.charmPool = charmPool(recipe);
        this.output = charmPool == null ? EmiStack.of(recipe.getOutput()) : unbreakable(charmPool.get(0));
        this.requirements = recipe.getRequirements();
        this.maxRequirements = recipe.getMaxRequirements();
        // The host seeds EnchantmentNames with the recipe itself, not with its id.
        this.seed = recipe.hashCode();

        inputs.add(input);
        if (charmPool == null) {
            outputs.add(output);
        } else {
            for (EmiStack charm : charmPool) {
                outputs.add(unbreakable(charm));
            }
        }
    }

    // CharmInfusionRecipe hands back its input with UNBREAKABLE added, so the recipe's own result has no potion.
    // Only that recipe takes charms of the item it returns, which is what identifies it here.
    private static List<EmiStack> charmPool(InfusionRecipe recipe) {
        ItemStack result = recipe.getOutput();
        if (!result.has(DataComponents.UNBREAKABLE)) {
            return null;
        }
        List<EmiStack> pool = new ArrayList<>();
        for (ItemStack stack : recipe.getInput().getItems()) {
            if (!stack.is(result.getItem()) || stack.has(DataComponents.UNBREAKABLE)
                    || !stack.has(DataComponents.POTION_CONTENTS)) {
                return null;
            }
            pool.add(EmiStack.of(stack));
        }
        return pool.isEmpty() ? null : List.copyOf(pool);
    }

    // CharmInfusionRecipe#assemble: the input copied, its damage cleared, UNBREAKABLE set.
    private static EmiStack unbreakable(EmiStack charm) {
        ItemStack stack = charm.getItemStack().copy();
        stack.setDamageValue(0);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return EmiStack.of(stack);
    }

    private EmiStack charmIn(Random random) {
        return this.charmPool.get(random.nextInt(this.charmPool.size()));
    }

    private EmiStack charmOut(Random random) {
        return unbreakable(charmIn(random));
    }

    /** The stats the raven table has to be tuned to; read by the transfer handler. */
    Stats requirements() {
        return requirements;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, ApothicEnchantingCompat.TEXTURE, ApothicEnchantingCompat.SHEET,
                0, 0, ApothicEnchantingCompat.WIDTH, ApothicEnchantingCompat.HEIGHT, 0, 0);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        if (charmPool == null) {
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        } else {
            // One seed for both slots: the charm that went in is the charm that comes out, now unbreakable.
            widgets.addGeneratedSlot(this::charmIn, this.unique, INPUT_X - 1, INPUT_Y - 1).drawBack(false);
            widgets.addGeneratedSlot(this::charmOut, this.unique, OUTPUT_X - 1, OUTPUT_Y - 1).drawBack(false)
                    .recipeContext(this);
        }

        widgets.addText(label("enchant.eterna"), LABEL_X, ETERNA_Y, ETERNA_COLOR, false);
        widgets.addText(label("enchant.quanta"), LABEL_X, QUANTA_Y, QUANTA_COLOR, false);
        widgets.addText(label("enchant.arcana"), LABEL_X, ARCANA_Y, ARCANA_COLOR, false);

        widgets.addDrawable(0, 0, ApothicEnchantingCompat.WIDTH, ApothicEnchantingCompat.HEIGHT, this::drawPage);

        addStatTooltip(widgets, ETERNA_Y, eternaTooltip());
        addStatTooltip(widgets, QUANTA_Y, quantaTooltip());
        addStatTooltip(widgets, ARCANA_Y, arcanaTooltip());
        addNameTooltip(widgets);
    }

    private void drawPage(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        boolean hover = over(mouseX, mouseY, STRIP_X, STRIP_Y, STRIP_WIDTH, STRIP_HEIGHT);
        if (hover) {
            blit(draw, STRIP_X, STRIP_Y, STRIP_WIDTH, STRIP_HEIGHT, 0, STRIP_V);
        }

        Font font = Minecraft.getInstance().font;
        int level = (int) requirements.eterna();
        String value = Integer.toString(level);
        int nameWidth = NAME_MAX_WIDTH - font.width(value);

        EnchantmentNames names = EnchantmentNames.getInstance();
        names.initSeed(seed);
        FormattedText name = names.getRandomName(font, nameWidth);
        int color = hover ? NAME_HOVER_COLOR : NAME_COLOR;
        int y = NAME_Y;
        for (FormattedCharSequence line : font.split(name, nameWidth)) {
            draw.drawString(font, line, NAME_X, y, color, false);
            y += 9;
        }
        draw.drawString(font, value, NAME_X + nameWidth, NAME_Y + LEVEL_DY, LEVEL_COLOR, false);

        int eterna = ApothEnchantmentScreen.getBarLength(requirements.eterna());
        int quanta = ApothEnchantmentScreen.getBarLength(requirements.quanta());
        int arcana = ApothEnchantmentScreen.getBarLength(requirements.arcana());
        if (requirements.eterna() > 0) {
            blit(draw, BAR_X, ETERNA_Y + 1, eterna, BAR_HEIGHT, 0, FILL_V);
        }
        if (requirements.quanta() > 0) {
            blit(draw, BAR_X, QUANTA_Y + 1, quanta, BAR_HEIGHT, 0, FILL_V + 5);
        }
        if (requirements.arcana() > 0) {
            blit(draw, BAR_X, ARCANA_Y + 1, arcana, BAR_HEIGHT, 0, FILL_V + 10);
        }
        // The remainder reads from the filled length, so its u offset is where the fill stopped.
        if (maxRequirements.eterna() > 0) {
            blit(draw, BAR_X + eterna, ETERNA_Y + 1,
                    ApothEnchantmentScreen.getBarLength(maxRequirements.eterna() - requirements.eterna()), BAR_HEIGHT,
                    eterna, REMAINDER_V);
        }
        if (maxRequirements.quanta() > 0) {
            blit(draw, BAR_X + quanta, QUANTA_Y + 1,
                    ApothEnchantmentScreen.getBarLength(maxRequirements.quanta() - requirements.quanta()), BAR_HEIGHT,
                    quanta, REMAINDER_V + 5);
        }
        if (maxRequirements.arcana() > 0) {
            blit(draw, BAR_X + arcana, ARCANA_Y + 1,
                    ApothEnchantmentScreen.getBarLength(maxRequirements.arcana() - requirements.arcana()), BAR_HEIGHT,
                    arcana, REMAINDER_V + 10);
        }
    }

    // The host's gfx.blit with blend around it; addDrawable has already translated to the widget origin.
    private static void blit(GuiGraphics draw, int x, int y, int width, int height, int u, int v) {
        if (width <= 0) {
            return;
        }
        draw.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        draw.blit(ApothicEnchantingCompat.TEXTURE, x, y, width, height, u, v, width, height,
                ApothicEnchantingCompat.SHEET, ApothicEnchantingCompat.SHEET);
        draw.flush();
        RenderSystem.disableBlend();
    }

    private void addNameTooltip(WidgetHolder widgets) {
        Holder<Enchantment> infusion = infusion();
        if (infusion == null) {
            return;
        }
        Component hint = Component.translatable("container.enchant.clue", Enchantment.getFullname(infusion, 1))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        widgets.addTooltipText(List.of(hint), STRIP_X, STRIP_Y, STRIP_WIDTH, STRIP_HEIGHT);
    }

    private void addStatTooltip(WidgetHolder widgets, int rowY, List<Component> lines) {
        // The host's hit box is the bar strip, one pixel into the label row above it.
        widgets.addTooltipText(lines, BAR_X, rowY, BAR_WIDTH, 6);
    }

    private List<Component> eternaTooltip() {
        List<Component> lines = new ArrayList<>(3);
        lines.add(label("enchant.eterna").withStyle(ChatFormatting.GREEN));
        if (maxRequirements.eterna() == requirements.eterna()) {
            lines.add(lang("info", "eterna_exact", requirements.eterna(), 100).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(lang("info", "eterna_at_least", requirements.eterna(), 100).withStyle(ChatFormatting.GRAY));
            if (maxRequirements.eterna() > -1) {
                lines.add(lang("info", "eterna_at_most", maxRequirements.eterna(), 100)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private List<Component> quantaTooltip() {
        List<Component> lines = new ArrayList<>(3);
        lines.add(label("enchant.quanta").withStyle(ChatFormatting.RED));
        if (maxRequirements.quanta() == requirements.quanta()) {
            lines.add(lang("info", "percent_exact", requirements.quanta()).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(lang("info", "percent_at_least", requirements.quanta()).withStyle(ChatFormatting.GRAY));
            if (maxRequirements.quanta() > -1) {
                lines.add(lang("info", "percent_at_most", maxRequirements.quanta()).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private List<Component> arcanaTooltip() {
        List<Component> lines = new ArrayList<>(3);
        lines.add(label("enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE));
        if (maxRequirements.arcana() == requirements.arcana()) {
            lines.add(lang("info", "percent_exact", requirements.arcana()).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(lang("info", "percent_at_least", requirements.arcana()).withStyle(ChatFormatting.GRAY));
            if (maxRequirements.arcana() > -1) {
                lines.add(lang("info", "percent_at_most", maxRequirements.arcana()).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private static Holder<Enchantment> infusion() {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        // The host throws on a missing enchantment; a page without the hint is cheaper than no category.
        return Minecraft.getInstance().level.holder(Ench.Enchantments.INFUSION).orElse(null);
    }

    private static MutableComponent label(String path) {
        return lang("gui", path);
    }

    // The host prefixes the type itself, so these are gui.apothic_enchanting.* and info.apothic_enchanting.*.
    private static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(type + ".apothic_enchanting." + path, args);
    }

    // JEI compares > on the near edge and <= on the far one; kept so the name strip hover matches.
    private static boolean over(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX > x && mouseX <= x + width && mouseY > y && mouseY <= y + height;
    }
}
