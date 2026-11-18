package dev.wp.emisemble.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** The frame is also a widget: WidgetGroup seeds every recipe with a RecipeBackground at (-4,-4) before addWidgets. */
@Mixin(targets = "dev.emi.emi.screen.WidgetGroup")
public abstract class WidgetGroupMixin {
    @Shadow
    public List<Widget> widgets;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void emiNatives$dropFrame(EmiRecipe recipe, int x, int y, int width, int height, CallbackInfo ci) {
        if (recipe instanceof OwnFrameRecipe) {
            widgets.removeIf(w -> w.getClass().getName().equals("dev.emi.emi.widget.RecipeBackground"));
        }
    }
}
