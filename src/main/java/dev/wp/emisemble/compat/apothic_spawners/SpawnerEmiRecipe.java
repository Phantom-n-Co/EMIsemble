package dev.wp.emisemble.compat.apothic_spawners;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apothic_spawners.modifiers.SpawnerModifier;
import dev.shadowsoffire.apothic_spawners.modifiers.StatModifier;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/** One modifier page: the item held, and the stats the spawner gains from it. */
public final class SpawnerEmiRecipe extends BasicEmiRecipe {
    private static final int MAINHAND_X = 11;
    private static final int MAINHAND_Y = 11;
    private static final int OFFHAND_X = 11;
    private static final int OFFHAND_Y = 48;

    private static final int COVER_X = 1;
    private static final int COVER_Y = 31;
    private static final int COVER_WIDTH = 28;
    private static final int COVER_HEIGHT = 34;
    private static final int COVER_U = 0;
    private static final int COVER_V = 88;

    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 12;
    private static final int ARROW_U = 0;
    private static final int ARROW_V = 75;

    private static final int SPAWNER_X = 31;
    private static final int SPAWNER_Y = 29;

    private static final int TEXT_LEFT = 168;
    private static final int TEXT_COLOR = 0x333333;
    private static final int TEXT_HOVER_COLOR = 0x8080FF;

    private final EmiIngredient mainhand;
    private final EmiIngredient offhand;
    private final boolean consumesOffhand;
    private final List<StatModifier<?>> modifiers;

    SpawnerEmiRecipe(ResourceLocation id, SpawnerModifier recipe) {
        super(ApothicSpawnersCompat.CATEGORY, id, ApothicSpawnersCompat.WIDTH, ApothicSpawnersCompat.HEIGHT);

        this.mainhand = EmiIngredient.of(recipe.getMainhandInput());
        Ingredient offhandInput = recipe.getOffhandInput();
        this.offhand = offhandInput == Ingredient.EMPTY ? null : EmiIngredient.of(offhandInput);
        this.consumesOffhand = recipe.consumesOffhand();
        this.modifiers = recipe.getStatModifiers();

        inputs.add(mainhand);
        if (offhand != null) {
            inputs.add(offhand);
        }
        catalysts.add(EmiStack.of(Items.SPAWNER));
        outputs.add(EmiStack.of(Items.SPAWNER));
    }

    // The spawner is catalyst and "output" so a lookup on the block finds the page, but nothing is crafted from it.
    @Override
    public boolean hideCraftable() {
        return true;
    }

    // The spawner is painted by the page, not placed as a slot, so there is no output widget to root a tree in.
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, ApothicSpawnersCompat.TEXTURE, ApothicSpawnersCompat.SHEET,
                0, 0, ApothicSpawnersCompat.WIDTH, ApothicSpawnersCompat.HEIGHT, 0, 0);
        if (offhand == null) {
            // The host paints over the unused off-hand socket with this slice of the sheet.
            AlphaTextures.add(widgets, ApothicSpawnersCompat.TEXTURE, ApothicSpawnersCompat.SHEET,
                    COVER_X, COVER_Y, COVER_WIDTH, COVER_HEIGHT, COVER_U, COVER_V);
        }

        RecipeSlots.add(widgets, mainhand, MAINHAND_X, MAINHAND_Y);
        if (offhand != null) {
            RecipeSlots.add(widgets, offhand, OFFHAND_X, OFFHAND_Y);
        }

        List<Line> lines = lines();
        widgets.addDrawable(0, 0, ApothicSpawnersCompat.WIDTH, ApothicSpawnersCompat.HEIGHT,
                (draw, mouseX, mouseY, delta) -> drawPage(draw, mouseX, mouseY, lines));

        for (Line line : lines) {
            widgets.addTooltipText(line.tooltip(), line.x(), line.y(), line.width(), line.height());
        }

        widgets.addTooltipText(List.of(ApothicSpawnersCompat.lang("misc", "mainhand")), -1, 13, ARROW_WIDTH, ARROW_HEIGHT);
        if (offhand != null) {
            List<Component> tooltip = new ArrayList<>(2);
            tooltip.add(ApothicSpawnersCompat.lang("misc", "offhand"));
            if (!consumesOffhand) {
                tooltip.add(ApothicSpawnersCompat.lang("misc", "not_consumed").withStyle(ChatFormatting.GRAY));
            }
            widgets.addTooltipText(tooltip, -1, 50, ARROW_WIDTH, ARROW_HEIGHT);
        }
        widgets.addTooltipText(List.of(ApothicSpawnersCompat.lang("misc", "rclick_spawner")), 33, 30, 16, 16);
    }

    private void drawPage(GuiGraphics draw, int mouseX, int mouseY, List<Line> lines) {
        if (hoverArrow(mouseX, mouseY, 13)) {
            blit(draw, -1, 13, ARROW_WIDTH, ARROW_HEIGHT, ARROW_U, ARROW_V);
        } else if (offhand != null && hoverArrow(mouseX, mouseY, 50)) {
            blit(draw, -1, 50, ARROW_WIDTH, ARROW_HEIGHT, ARROW_U, ARROW_V);
        }

        draw.pose().pushPose();
        draw.pose().translate(0, 0.5F, 0);
        draw.renderFakeItem(new ItemStack(Items.SPAWNER), SPAWNER_X, SPAWNER_Y);
        draw.pose().popPose();

        Font font = Minecraft.getInstance().font;
        for (Line line : lines) {
            boolean hover = mouseX >= line.x() && mouseX < line.x() + line.width()
                    && mouseY >= line.y() && mouseY < line.y() + line.height();
            draw.drawString(font, line.text(), line.x(), line.y(), hover ? TEXT_HOVER_COLOR : TEXT_COLOR, false);
        }
    }

    private List<Line> lines() {
        Font font = Minecraft.getInstance().font;
        int top = ApothicSpawnersCompat.HEIGHT / 2 - modifiers.size() * (font.lineHeight + 2) / 2 + 2;

        List<Line> lines = new ArrayList<>(modifiers.size());
        for (StatModifier<?> modifier : modifiers) {
            Component text = describe(modifier);
            int width = font.width(text);
            lines.add(new Line(text, tooltip(modifier), TEXT_LEFT - width, top, width, font.lineHeight + 1));
            top += font.lineHeight + 2;
        }
        return lines;
    }

    // The host's switch: ADD takes a sign (booleans become + / -), SET prints stat: value or stat: on/off.
    private static Component describe(StatModifier<?> modifier) {
        String value = modifier.getFormattedValue();
        return switch (modifier.mode()) {
            case ADD -> {
                if ("true".equals(value)) {
                    value = "+";
                } else if ("false".equals(value)) {
                    value = "-";
                } else if (modifier.value() instanceof Number number && number.intValue() > 0) {
                    value = "+" + value;
                }
                yield ApothicSpawnersCompat.lang("misc", "concat", value, modifier.stat().name());
            }
            case SET -> {
                if (modifier.value() instanceof Number) {
                    yield ApothicSpawnersCompat.lang("misc", "value_concat", modifier.stat().name(), value);
                }
                yield "true".equals(value)
                        ? ApothicSpawnersCompat.lang("misc", "on", modifier.stat().name())
                        : ApothicSpawnersCompat.lang("misc", "off", modifier.stat().name());
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Component> tooltip(StatModifier<?> modifier) {
        List<Component> lines = new ArrayList<>(5);
        lines.add(modifier.stat().name().withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE));
        lines.add(modifier.stat().desc().withStyle(ChatFormatting.GRAY));

        if (modifier.value() instanceof Number && modifier.mode() == StatModifier.Mode.ADD) {
            StatModifier<Number> numeric = (StatModifier<Number>) modifier;
            if (numeric.min().isPresent() || numeric.max().isPresent()) {
                lines.add(Component.literal(" "));
            }
            numeric.min().ifPresent(min -> lines.add(
                    ApothicSpawnersCompat.lang("misc", "min_value", numeric.stat().formatValue(min))
                            .withStyle(ChatFormatting.GRAY)));
            numeric.max().ifPresent(max -> lines.add(
                    ApothicSpawnersCompat.lang("misc", "max_value", numeric.stat().formatValue(max))
                            .withStyle(ChatFormatting.GRAY)));
        }
        return lines;
    }

    private static boolean hoverArrow(int mouseX, int mouseY, int y) {
        return mouseX >= -1 && mouseX < 9 && mouseY >= y && mouseY < y + ARROW_HEIGHT;
    }

    // The host's plain blit plus the blend its arrow edge needs; addDrawable is already at the widget origin.
    private static void blit(GuiGraphics draw, int x, int y, int width, int height, int u, int v) {
        draw.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        draw.blit(ApothicSpawnersCompat.TEXTURE, x, y, width, height, u, v, width, height,
                ApothicSpawnersCompat.SHEET, ApothicSpawnersCompat.SHEET);
        draw.flush();
        RenderSystem.disableBlend();
    }

    /** One right-aligned stat line and the hit box that opens its tooltip. */
    private record Line(Component text, List<Component> tooltip, int x, int y, int width, int height) {}
}
