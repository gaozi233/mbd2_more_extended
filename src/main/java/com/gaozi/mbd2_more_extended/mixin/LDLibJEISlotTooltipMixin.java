package com.gaozi.mbd2_more_extended.mixin;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * LDLib 的 JEI ingredient renderer 同时实现了两套 tooltip API：
 * <ul>
 *     <li>{@code getTooltip(T, TooltipFlag)}：返回空列表</li>
 *     <li>{@code getTooltip(ITooltipBuilder, T, TooltipFlag)}：返回完整 tooltip</li>
 * </ul>
 *
 * <p>部分 JEI 版本会调用前者，导致 MBD2 配方 GUI 的输入/输出/条件槽位
 * 完全没有物品 tooltip。这里让旧 API 也返回槽位的完整 tooltip。</p>
 */
@Mixin(targets = "com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory$2")
public abstract class LDLibJEISlotTooltipMixin {

    @Shadow(remap = false)
    @Final
    private IRecipeIngredientSlot val$slot;

    @Inject(method = "getTooltip(Ljava/lang/Object;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void mbd2$returnSlotTooltip(Object ingredient, TooltipFlag flag,
                                        CallbackInfoReturnable<List<Component>> cir) {
        cir.setReturnValue(val$slot.getFullTooltipTexts());
    }
}
