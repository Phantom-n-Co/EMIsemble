package dev.wp.emisemble.compat.enigmaticlegacyplus.spellstone;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** The spellstone page art: an atlas sprite, not a sheet region, so no AlphaTextures. */
final class SpellstonePageArt {
    private static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("enigmaticlegacyplus", "jei/spellstone_table_gui");
    private static final ResourceLocation ALL_DIFFERENT =
            ResourceLocation.fromNamespaceAndPath("enigmaticlegacyplus", "jei/spellstone_table_icon");

    static final int WIDTH = 132;
    static final int HEIGHT = 80;

    static final int ALL_DIFFERENT_X = 124;
    static final int ALL_DIFFERENT_Y = 0;
    static final int ALL_DIFFERENT_SIZE = 8;

    private SpellstonePageArt() {}

    static void page(WidgetHolder widgets) {
        sprite(widgets, PAGE, 0, 0, WIDTH, HEIGHT);
    }

    static void allDifferentBadge(WidgetHolder widgets) {
        sprite(widgets, ALL_DIFFERENT, ALL_DIFFERENT_X, ALL_DIFFERENT_Y, ALL_DIFFERENT_SIZE, ALL_DIFFERENT_SIZE);
    }

    private static void sprite(WidgetHolder widgets, ResourceLocation sprite, int x, int y, int width, int height) {
        widgets.addDrawable(x, y, width, height, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
            draw.flush();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            draw.blitSprite(sprite, 0, 0, width, height);
            draw.flush();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        });
    }
}
