package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.trait.OverstackItemStackTransfer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复过载物品槽在 GUI 中只能按物品原版 maxStackSize 操作的问题。
 *
 * <p>LDLib 的 {@code WidgetSlotItemTransfer.getMaxStackSize(ItemStack)} 默认按物品自身
 * maxStackSize 计算，即使存储层的 slotLimit 更大，鼠标点击也最多只能放入 64 个。
 * 这里仅对该存储是 {@link OverstackItemStackTransfer} 的槽位使用过载公式。</p>
 */
@Mixin(value = SlotWidget.WidgetSlotItemTransfer.class, remap = false)
public abstract class SlotWidgetItemTransferMaxStackMixin {

    @Shadow
    @Final
    private IItemTransfer itemHandler;

    @Inject(
            method = {
                    "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
                    "m_5866_(Lnet/minecraft/world/item/ItemStack;)I"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void mbd2$useOverstackLimit(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (itemHandler instanceof OverstackItemStackTransfer transfer) {
            cir.setReturnValue(transfer.getOverstackLimit(stack));
        }
    }
}
