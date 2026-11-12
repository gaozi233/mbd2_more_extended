package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.world.item.ItemStack;

/**
 * 在允许时让普通 64 堆叠物品超过原版上限。
 */
public class OverstackItemStackTransfer extends ItemStackTransfer {

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
