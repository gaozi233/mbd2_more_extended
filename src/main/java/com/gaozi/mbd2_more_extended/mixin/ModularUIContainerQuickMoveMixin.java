package com.gaozi.mbd2_more_extended.mixin;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 允许过载物品槽作为 Shift 快速移动的源槽位。
 *
 * <p>LDLib 的 {@code quickMoveStack} 会用源物品自身的 maxStackSize 限制单次搬运量；
 * 过载槽内超过 64 时会被这条检查直接拦下。这里仅把上限提升到源槽位自身的上限。</p>
 */
@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class ModularUIContainerQuickMoveMixin {

    @Unique
    private static final ThreadLocal<Integer> MBD2_SOURCE_LIMIT = new ThreadLocal<>();

    @Inject(method = {"quickMoveStack", "m_7648_"}, at = @At("HEAD"), require = 1, remap = false)
    private void mbd2$captureSourceLimit(Player player, int index,
                                        CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = ((AbstractContainerMenu) (Object) this).slots.get(index);
        ItemStack stack = slot.getItem();
        MBD2_SOURCE_LIMIT.set(Math.max(stack.getMaxStackSize(), slot.getMaxStackSize(stack)));
    }

    @Inject(method = {"quickMoveStack", "m_7648_"}, at = @At("RETURN"), require = 1, remap = false)
    private void mbd2$clearSourceLimit(Player player, int index,
                                       CallbackInfoReturnable<ItemStack> cir) {
        MBD2_SOURCE_LIMIT.remove();
    }

    @Redirect(
            method = {"quickMoveStack", "m_7648_"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"),
            require = 1,
            remap = false
    )
    private int mbd2$useSourceSlotLimit(ItemStack stack) {
        Integer limit = MBD2_SOURCE_LIMIT.get();
        return limit == null ? stack.getMaxStackSize() : Math.max(stack.getMaxStackSize(), limit);
    }
}
