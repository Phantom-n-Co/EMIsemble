package dev.wp.emisemble.porting;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Alpha-blended page art for ported JEI categories. */
public final class AlphaTextures {
    private AlphaTextures() {}

    public static void add(WidgetHolder widgets, ResourceLocation texture, int sheet, int x, int y, int width,
                           int height, int u, int v) {
        add(widgets, texture, sheet, x, y, width, height, u, v, 1.0F);
    }

    /** Same, at a given opacity — the host's own tinted blits carry it in {@code GuiGraphics#setColor}. */
    public static void add(WidgetHolder widgets, ResourceLocation texture, int sheet, int x, int y, int width,
                           int height, int u, int v, float alpha) {
        add(widgets, texture, sheet, sheet, x, y, width, height, u, v, alpha);
    }

    /** A whole sprite stretched into a larger rect; the blits above tie the source rect to the destination one. */
    public static void scaled(WidgetHolder widgets, ResourceLocation texture, int sheet, int x, int y, int width,
                              int height) {
        widgets.addDrawable(x, y, width, height, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                blit(draw, texture, sheet, sheet, 0, 0, width, height, 0, 0, sheet, sheet, 1.0F));
    }

    /** For a sheet that is not square, which needs its own width and height or the v offsets scale wrongly. */
    public static void add(WidgetHolder widgets, ResourceLocation texture, int sheetWidth, int sheetHeight, int x,
                           int y, int width, int height, int u, int v) {
        add(widgets, texture, sheetWidth, sheetHeight, x, y, width, height, u, v, 1.0F);
    }

    private static void add(WidgetHolder widgets, ResourceLocation texture, int sheetWidth, int sheetHeight, int x,
                            int y, int width, int height, int u, int v, float alpha) {
        widgets.addDrawable(x, y, width, height, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                blit(draw, texture, sheetWidth, sheetHeight, 0, 0, width, height, u, v, alpha));
    }

    /** A left-anchored bar that fills over {@code periodTicks}: the hosts blit a growing slice of the sprite, not frames. */
    public static void progress(WidgetHolder widgets, ResourceLocation texture, int sheet, int x, int y, int width,
                                int height, int u, int v, int periodTicks) {
        widgets.addDrawable(x, y, width, height, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
            int filled = Math.min(width, Mth.ceil(width * ((gameTime() % periodTicks) + delta) / periodTicks));
            if (filled > 0) {
                blit(draw, texture, sheet, sheet, 0, 0, filled, height, u, v, 1.0F);
            }
        });
    }

    /**
     * A bottom-anchored vertical slice: the hosts' bars fill up from the art's base edge, and their flames burn away
     * down onto that same edge, which is why the crop keeps the last rows rather than the first.
     */
    public static void rising(WidgetHolder widgets, ResourceLocation texture, int x, int y, int width, int height,
                              int u, int v, int periodTicks, boolean drains) {
        widgets.addDrawable(x, y, width, height, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
            float elapsed = ((gameTime() % periodTicks) + delta) / periodTicks;
            int filled = Mth.ceil(height * (drains ? 1.0F - elapsed : elapsed));
            if (filled > 0) {
                int top = height - filled;
                // These container sprites are single-purpose files, so their declared texture size is the rect itself.
                blit(draw, texture, width, height, 0, top, width, filled, u, v + top, 1.0F);
            }
        });
    }

    private static void blit(GuiGraphics draw, ResourceLocation texture, int sheetWidth, int sheetHeight, int x,
                             int y, int width, int height, int u, int v, float alpha) {
        blit(draw, texture, sheetWidth, sheetHeight, x, y, width, height, u, v, width, height, alpha);
    }

    /** The source rect is separate from the dest rect here, which is the only way to scale a sprite up. */
    private static void blit(GuiGraphics draw, ResourceLocation texture, int sheetWidth, int sheetHeight, int x,
                             int y, int width, int height, int u, int v, int uWidth, int vHeight, float alpha) {
        draw.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        draw.setColor(1.0F, 1.0F, 1.0F, alpha);
        draw.blit(texture, x, y, width, height, u, v, uWidth, vHeight, sheetWidth, sheetHeight);
        draw.flush();
        draw.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** Level time, so every page of a category animates in step and none of them has to keep a counter. */
    private static long gameTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? 0L : level.getGameTime();
    }
}
