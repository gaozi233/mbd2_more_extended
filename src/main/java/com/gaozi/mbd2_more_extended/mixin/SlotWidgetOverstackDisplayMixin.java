package com.gaozi.mbd2_more_extended.mixin;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 槽位引用被一次空同步覆盖时，仍从过载储物读取真实堆叠用于显示。
 *
 * <p>只处理超过 64 的堆叠，避免影响普通物品槽。</p>
 */
@Mixin(value = SlotWidget.class, remap = false)
public abstract class SlotWidgetOverstackDisplayMixin {

    @Inject(method = "getItem", at = @At("RETURN"), cancellable = true, remap = false)
    private void mbd2$fallbackToOverstackStorage(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result != null && !result.isEmpty()) {
            return;
        }

        SlotWidget self = (SlotWidget) (Object) this;
        Slot slot = self.getHandler();
        if (slot instanceof SlotWidget.WidgetSlotItemTransfer transfer) {
            IItemTransfer handler = transfer.getItemHandler();
            ItemStack stored = handler.getStackInSlot(slot.getSlotIndex());
            if (!stored.isEmpty() && stored.getCount() > 64) {
                cir.setReturnValue(stored);
            }
        }
    }
}
