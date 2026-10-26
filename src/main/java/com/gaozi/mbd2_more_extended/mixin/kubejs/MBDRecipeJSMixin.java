package com.gaozi.mbd2_more_extended.mixin.kubejs;

import com.gaozi.mbd2_more_extended.condition.IConditionVisibility;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import com.lowdragmc.mbd2.integration.kubejs.recipe.MBDRecipeSchema;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * KubeJS 可选 Mixin：给 {@code MBDRecipeJS} 增加链式 {@code showInGui(boolean)}。
 *
 * <p>用法：</p>
 * <pre>{@code
 * m.dimension('minecraft:overworld').showInGui(false)
 * m.addCondition(blockExt(1, 8, ['minecraft:stone'])).showInGui(false)
 * }</pre>
 */
@Mixin(value = MBDRecipeSchema.MBDRecipeJS.class, remap = false)
public abstract class MBDRecipeJSMixin {

    @Unique
    private RecipeCondition mbd2MoreExtended$lastCondition;

    @Inject(method = "addCondition", at = @At("TAIL"), remap = false)
    private void mbd2$trackLastCondition(RecipeCondition condition,
                                         CallbackInfoReturnable<MBDRecipeSchema.MBDRecipeJS> cir) {
        mbd2MoreExtended$lastCondition = condition;
    }

    @Unique
    public MBDRecipeSchema.MBDRecipeJS showInGui(boolean show) {
        if (mbd2MoreExtended$lastCondition instanceof IConditionVisibility visibility) {
            visibility.mbd2_more_extended$setShowInGui(show);
        }
        return (MBDRecipeSchema.MBDRecipeJS) (Object) this;
    }
}
