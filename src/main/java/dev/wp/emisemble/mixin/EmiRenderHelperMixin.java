package dev.wp.emisemble.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.EmiRenderHelper")
public class EmiRenderHelperMixin {
    // Runs before the nine-patch is drawn; a cancelled call leaves the page's own art as the whole card.
    @Inject(method = "renderRecipeBackground", at = @At("HEAD"), cancellable = true)
    private static void emiNatives$skipOwnFrame(EmiRecipe recipe, EmiDrawContext context, int x, int y, CallbackInfo ci) {
        if (recipe instanceof OwnFrameRecipe) {
            ci.cancel();
        }
    }
}
