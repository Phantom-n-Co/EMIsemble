package dev.wp.emisemble.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.wp.emisemble.porting.EmiVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

/** Both list reads filter at return, so a recipe disappears the moment its host condition flips. */
@Mixin(targets = "dev.emi.emi.registry.EmiRecipes$Manager")
public abstract class EmiRecipeManagerMixin {
    @Inject(method = "getRecipes()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void emiNatives$filterAll(CallbackInfoReturnable<List<EmiRecipe>> cir) {
        Predicate<EmiRecipe> hidden = EmiVisibility.hiddenRecipe();
        if (hidden != null) {
            cir.setReturnValue(cir.getReturnValue().stream().filter(hidden.negate()).toList());
        }
    }

    @Inject(method = "getRecipes(Ldev/emi/emi/api/recipe/EmiRecipeCategory;)Ljava/util/List;", at = @At("RETURN"),
            cancellable = true)
    private void emiNatives$filterCategory(EmiRecipeCategory category, CallbackInfoReturnable<List<EmiRecipe>> cir) {
        Predicate<EmiRecipe> hidden = EmiVisibility.hiddenRecipe();
        if (hidden != null) {
            cir.setReturnValue(cir.getReturnValue().stream().filter(hidden.negate()).toList());
        }
    }
}
