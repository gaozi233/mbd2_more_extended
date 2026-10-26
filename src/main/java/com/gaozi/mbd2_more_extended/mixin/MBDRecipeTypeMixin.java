package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.condition.IConditionVisibility;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeType;
import com.lowdragmc.mbd2.utils.WidgetUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 过滤配方 GUI 的 {@code @condition} 条件描述，只保留 showInGui 为 true 的条件。
 */
@Mixin(value = MBDRecipeType.class, remap = false)
public class MBDRecipeTypeMixin {

    @Inject(method = "bindXEIRecipeUI", at = @At("TAIL"), remap = false)
    private void mbd2$filterConditionDescription(WidgetGroup root, MBDRecipe recipe, CallbackInfo ci) {
        if (root == null || recipe == null || recipe.conditions == null) {
            return;
        }
        List<String> lines = recipe.conditions.stream()
                .filter(condition -> !(condition instanceof IConditionVisibility visibility)
                        || visibility.mbd2_more_extended$isShowInGui())
                .map(condition -> condition.getTooltips().getString())
                .toList();
        WidgetUtils.widgetByIdForEach(root, "^@condition$", TextBoxWidget.class,
                textBox -> textBox.setContent(lines));
    }
}
