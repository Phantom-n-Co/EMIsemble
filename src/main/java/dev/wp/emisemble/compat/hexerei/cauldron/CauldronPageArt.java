package dev.wp.emisemble.compat.hexerei.cauldron;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import net.joefoxe.hexerei.data.recipes.MoonPhases.MoonCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** The mixing cauldron page's art: sheet regions, moon sprites and the scaled labels. */
final class CauldronPageArt {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("hexerei", "textures/gui/mixing_cauldron_gui_jei.png");
    private static final ResourceLocation MOON_PHASES =
            ResourceLocation.fromNamespaceAndPath("hexerei", "textures/gui/moon_phases.png");

    private static final int SHEET = 256;

    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    static final int WIDTH = 178;
    static final int HEIGHT = 101;

    static final int INPUT_TANK_X = 16;
    static final int INPUT_TANK_Y = 53;
    static final int INPUT_TANK_WIDTH = 16;
    static final int INPUT_TANK_HEIGHT = 32;

    static final int OUTPUT_TANK_X = 148;
    static final int OUTPUT_TANK_Y = 67;
    static final int OUTPUT_TANK_WIDTH = 12;
    static final int OUTPUT_TANK_HEIGHT = 10;

    private static final int OUTPUT_PANEL_X = 134;
    private static final int OUTPUT_PANEL_Y = 12;
    private static final int OUTPUT_PANEL_WIDTH = 47;
    private static final int OUTPUT_PANEL_HEIGHT = 82;

    private static final int TITLE_X = 7;
    private static final int TITLE_Y = 5;
    private static final int TITLE_MAX_WIDTH = 131;
    private static final float TITLE_LINE_HEIGHT = 9.0F / 2.0F;

    private static final Component CONVERTS_FLUID = Component.translatable("gui.jei.category.mixing_cauldron.convert_fluid");
    private static final float LABEL_SCALE = 0.6F;
    private static final float LABEL_X = 231.574F;
    private static final float LABEL_Y = 96.628F;

    private static final int MOON_X = 80;
    private static final int MOON_Y = 34;
    private static final int MOON_SIZE = 14;
    private static final int MOON_SPRITE = 8;

    private static final int LABEL_COLOR = 0xFF404040;

    private CauldronPageArt() {
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
    }

    static void inputGauge(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, INPUT_TANK_X, INPUT_TANK_Y, INPUT_TANK_WIDTH, INPUT_TANK_HEIGHT,
                208, 12);
    }

    static void outputRim(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, OUTPUT_TANK_X, OUTPUT_TANK_Y, OUTPUT_TANK_WIDTH,
                OUTPUT_TANK_HEIGHT, 238, 50);
    }

    static void outputPanel(WidgetHolder widgets, boolean convertsFluid) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, OUTPUT_PANEL_X, OUTPUT_PANEL_Y, OUTPUT_PANEL_WIDTH,
                OUTPUT_PANEL_HEIGHT, 209, convertsFluid ? 146 : 64);
    }

    static void convertsLabel(WidgetHolder widgets) {
        Font font = Minecraft.getInstance().font;
        int width = Math.round(font.width(CONVERTS_FLUID) * LABEL_SCALE);
        int height = Math.round(font.lineHeight * LABEL_SCALE);
        widgets.addDrawable(Math.round(LABEL_X * LABEL_SCALE) - 4, Math.round(LABEL_Y * LABEL_SCALE) - 4,
                Math.max(1, width), Math.max(1, height), (draw, mouseX, mouseY, delta) -> {
                    PoseStack pose = draw.pose();
                    pose.pushPose();
                    pose.scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
                    draw.drawString(font, CONVERTS_FLUID, 0, 0, LABEL_COLOR, false);
                    pose.popPose();
                });
    }

    static void title(WidgetHolder widgets, Component name) {
        widgets.addDrawable(-4, -4, OUTPUT_PANEL_X, TITLE_Y + 9, (draw, mouseX, mouseY, delta) -> {
            Font font = Minecraft.getInstance().font;
            int width = font.width(name);
            PoseStack pose = draw.pose();
            if (width > TITLE_MAX_WIDTH) {
                float percent = width / (float) TITLE_MAX_WIDTH;
                pose.pushPose();
                pose.scale(1.0F / percent, 1.0F / percent, 1.0F);
                draw.drawString(font, name, Math.round(TITLE_X * percent),
                        Math.round((TITLE_Y + TITLE_LINE_HEIGHT) * percent - 4.5F), LABEL_COLOR, false);
                pose.popPose();
            } else {
                draw.drawString(font, name, TITLE_X, TITLE_Y, LABEL_COLOR, false);
            }
        });
    }

    static void moonPhase(WidgetHolder widgets, MoonCondition phase) {
        int u;
        int v;
        switch (phase) {
            case FULL_MOON -> {
                u = 12;
                v = 12;
            }
            case WANING_GIBBOUS -> {
                u = 44;
                v = 12;
            }
            case LAST_QUARTER -> {
                u = 76;
                v = 12;
            }
            case WANING_CRESCENT -> {
                u = 108;
                v = 12;
            }
            case NEW_MOON -> {
                u = 12;
                v = 44;
            }
            case WAXING_CRESCENT -> {
                u = 44;
                v = 44;
            }
            case FIRST_QUARTER -> {
                u = 76;
                v = 44;
            }
            case WAXING_GIBBOUS -> {
                u = 108;
                v = 44;
            }
            default -> {
                return;
            }
        }
        widgets.addTexture(MOON_PHASES, MOON_X, MOON_Y, MOON_SIZE, MOON_SIZE, u, v,
                MOON_SPRITE, MOON_SPRITE, SHEET, SHEET);
    }
}
