package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.MBD2MoreExtended;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * 临时调试：确认超过 64 的客户端槽位是否真的拿到了数量。
 */
@Mixin(value = SlotWidget.class, remap = false)
public abstract class SlotWidgetOverstackDebugMixin {

    private static final Set<String> DEBUG_LOGGED = new HashSet<>();

    @Shadow
    protected ItemStack lastItem;

    @Inject(method = "drawInBackground", at = @At("HEAD"), remap = false)
    private void mbd2$debugSlotStack(CallbackInfo ci) {        SlotWidget self = (SlotWidget) (Object) this;
        ItemStack live = self.getItem();
        int liveCount = live == null ? -1 : live.getCount();
        int lastCount = lastItem == null ? -1 : lastItem.getCount();
        if (liveCount > 64 || lastCount > 64) {
            Slot slot = self.getHandler();
            String handlerInfo = "none";
            if (slot instanceof SlotWidget.WidgetSlotItemTransfer transfer) {
                IItemTransfer handler = transfer.getItemHandler();
                ItemStack handlerStack = handler.getStackInSlot(0);
                handlerInfo = handler.getClass().getSimpleName()
                        + "@" + System.identityHashCode(handler)
                        + ":" + handlerStack.getCount();
            }
            String key = self.getId() + "|" + liveCount + "|" + lastCount + "|" + handlerInfo;
            if (DEBUG_LOGGED.add(key)) {
                MBD2MoreExtended.LOGGER.info(
                        "[overstack-debug] slotWidget id={} live={} last={} empty={} ref={} handler={}",
                        self.getId(), liveCount, lastCount, liveCount <= 0,
                        slot == null ? "null" : slot.getClass().getSimpleName(), handlerInfo);
            }
        }
    }

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
            ItemStack stored = handler.getStackInSlot(slot.index);
            if (!stored.isEmpty() && stored.getCount() > 64) {
                if (DEBUG_LOGGED.add("fallback|" + self.getId() + "|" + stored.getCount())) {
                    MBD2MoreExtended.LOGGER.info(
                            "[overstack-debug] slotWidget fallback id={} count={}",
                            self.getId(), stored.getCount());
                }
                cir.setReturnValue(stored);
            }
        }
    }
}
