package dev.wp.emisemble.compat.hexerei.fluidmixing;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** The fluid page's art, sliced from the JEI sheet. */
final class FluidMixingPageArt {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("hexerei", "textures/gui/fluid_mixing_gui_jei.png");
    static final int SHEET = 256;
    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    static final int WIDTH = 178;
    static final int HEIGHT = 101;

    static final int LABEL_COLOR = 0xFF404040;

    private static final int TEXT_WIDTH = 131;
    private static final float LINE_HEIGHT = 4.5F;

    private static final Component CONVERTS_FLUID =
            Component.translatable("gui.jei.category.mixing_cauldron.convert_fluid");

    private FluidMixingPageArt() {}

    static void page(WidgetHolder widgets) {
        blit(widgets, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);
    }

    static void inputSocket(WidgetHolder widgets) {
        blit(widgets, 16, 53, 16, 32, 208, 12);
    }

    static void outputSocket(WidgetHolder widgets) {
        blit(widgets, 148, 47, 12, 10, 238, 50);
    }

    static void outputPanel(WidgetHolder widgets, boolean converts) {
        if (converts) {
            blit(widgets, 134, 12, 47, 82, 209, 146);
        } else {
            blit(widgets, 134, 12, 47, 82, 209, 64);
        }
    }

    static void convertsFluidLabel(WidgetHolder widgets) {
        widgets.addDrawable(135, 34, 40, 8, (draw, mouseX, mouseY, delta) -> {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.scale(0.6F, 0.6F, 0.6F);
            draw.drawString(Minecraft.getInstance().font, CONVERTS_FLUID, 0, 0, LABEL_COLOR, false);
            pose.popPose();
        });
    }

    static void outputName(WidgetHolder widgets, Component name) {
        widgets.addDrawable(-4, -4, WIDTH, 9, (draw, mouseX, mouseY, delta) -> {
            Font font = Minecraft.getInstance().font;
            int width = font.width(name);
            if (width > TEXT_WIDTH) {
                float percent = width / (float) TEXT_WIDTH;
                PoseStack pose = draw.pose();
                pose.pushPose();
                pose.scale(1.0F / percent, 1.0F / percent, 1.0F / percent);
                draw.drawString(font, name, Math.round(7.0F * percent),
                        Math.round((5.0F + LINE_HEIGHT) * percent - LINE_HEIGHT), LABEL_COLOR, false);
                pose.popPose();
            } else {
                draw.drawString(font, name, 7, 5, LABEL_COLOR, false);
            }
        });
    }

    private static void blit(WidgetHolder widgets, int x, int y, int width, int height, int u, int v) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, x, y, width, height, u, v);
    }
}
