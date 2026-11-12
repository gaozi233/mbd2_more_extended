package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * 在允许时让普通 64 堆叠物品超过原版上限。
 */
public class OverstackItemStackTransfer extends ItemStackTransfer {

    private static final String OVERSTACK_COUNT = "mbd2_overstack_count";

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
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= getSlots()) {
                continue;
            }
            ItemStack stack = super.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getCount() > Byte.MAX_VALUE) {
                // ItemStack 的 NBT Count 在原版里是 byte，超过 127 会变成负数。
                // 把 Count 写成 1，并把真实数量放到旁边；读取后再恢复。
                entry.putByte("Count", (byte) 1);
                entry.putInt(OVERSTACK_COUNT, stack.getCount());
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag entry = itemList.getCompound(i);
            int slot = entry.getInt("Slot");
            int count = entry.getInt(OVERSTACK_COUNT);
            if (count > 0 && slot >= 0 && slot < getSlots()) {
                ItemStack stack = super.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    stack.setCount(count);
                }
            }
        }
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
