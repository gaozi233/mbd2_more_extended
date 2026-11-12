package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.world.item.ItemStack;

/**
 * 在允许时让普通 64 堆叠物品超过原版上限，但保留工具、药水等低堆叠物品的限制。
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
        int slotMax = definition.getSlotLimit();
        if (definition.isAllowOverstack() && itemMax >= 64 && slotMax > itemMax) {
            return slotMax;
        }
        return Math.min(slotMax, itemMax);
    }
}
