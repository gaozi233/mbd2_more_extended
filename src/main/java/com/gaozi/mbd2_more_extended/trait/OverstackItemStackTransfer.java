package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.gaozi.mbd2_more_extended.MBD2MoreExtended;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.HashSet;
import java.util.Set;

/**
 * 在允许时让普通 64 堆叠物品超过原版上限。
 */
public class OverstackItemStackTransfer extends ItemStackTransfer {

    private static final Set<String> DEBUG_LOGGED = new HashSet<>();

    private final OverstackItemSlotTraitDefinition definition;

    public OverstackItemStackTransfer(OverstackItemSlotTraitDefinition definition) {
        super(definition.getSlotSize());
        this.definition = definition;
    }

    @Override
    public int getSlotLimit(int slot) {
        return definition.getSlotLimit();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        ItemStack old = super.getStackInSlot(slot);
        if (!old.isEmpty() && old.getCount() > 64 && stack.isEmpty()) {
            MBD2MoreExtended.LOGGER.info(
                    "[overstack-debug] storage clear side={} thread={} slot={} old={}",
                    FMLEnvironment.dist, Thread.currentThread().getName(), slot, old.getCount(),
                    new Exception("overstack-clear"));
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = super.getStackInSlot(slot);
        if (!stack.isEmpty() && stack.getCount() > 64) {
            String key = FMLEnvironment.dist + "|" + slot + "|" + stack.getCount()
                    + "|" + System.identityHashCode(this);
            if (DEBUG_LOGGED.add(key)) {
                MBD2MoreExtended.LOGGER.info(
                        "[overstack-debug] storage side={} slot={} count={}",
                        FMLEnvironment.dist, slot, stack.getCount());
            }
        }
        return stack;
    }

    /**
     * GUI 的 Slot 会用物品自身的 maxStackSize 计算鼠标操作上限，这里给界面提供同一套过载公式。
     */
    public int getOverstackLimit(ItemStack stack) {
        return getStackLimit(0, stack);
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        int itemMax = stack.getMaxStackSize();
        int value = definition.getSlotLimit();
        if (!definition.isAllowOverstack() || value <= 0) {
            return Math.min(value, itemMax);
        }

        long result;
        if (definition.getMode() == OverstackItemSlotTraitDefinition.OverstackMode.MULTIPLIER) {
            result = (long) itemMax * value;
        } else {
            // EXACT：64 堆叠物品的上限就是 value，其他物品按 x * value / 64 向下取整。
            result = (long) itemMax * value / 64L;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, result));
    }
}
