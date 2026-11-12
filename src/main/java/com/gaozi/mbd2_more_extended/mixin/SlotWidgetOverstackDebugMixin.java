package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.MBD2MoreExtended;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void mbd2$debugSlotStack(CallbackInfo ci) {
        SlotWidget self = (SlotWidget) (Object) this;
        ItemStack live = self.getItem();
        int liveCount = live == null ? -1 : live.getCount();
        int lastCount = lastItem == null ? -1 : lastItem.getCount();
        if (liveCount > 64 || lastCount > 64) {
            String key = self.getId() + "|" + liveCount + "|" + lastCount;
            if (DEBUG_LOGGED.add(key)) {
                MBD2MoreExtended.LOGGER.info(
                        "[overstack-debug] slotWidget id={} live={} last={} empty={}",
                        self.getId(), liveCount, lastCount, liveCount <= 0);
            }
        }
    }
}
