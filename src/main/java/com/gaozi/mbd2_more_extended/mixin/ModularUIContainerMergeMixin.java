package com.gaozi.mbd2_more_extended.mixin;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

/**
 * 修复过载物品槽在 Shift 快速移动时按物品原版上限计算，导致负数搬运和刷物品的问题。
 *
 * <p>LDLib 的 {@code mergeItemStack} 使用
 * {@code min(destStack.getMaxStackSize(), destSlot.getMaxStackSize(destStack))}。
 * 当过载槽内已有 79 个铁锭时，前一项是 64，后者是 1000，结果仍是 64，
 * 于是 {@code max - destCount} 变成负数，触发物品复制。</p>
 */
@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class ModularUIContainerMergeMixin {

    /**
     * @author MBD2 More Extended
     * @reason 允许过载槽按自身上限合并，同时避免原实现产生负的搬运数量。
     */
    @Overwrite
    public static boolean mergeItemStack(ItemStack stack, List<Slot> slots, boolean simulate) {
        if (stack.isEmpty()) {
            return false;
        }

        boolean moved = false;
        for (Slot slot : slots) {
            if (!slot.mayPlace(stack)) {
                continue;
            }
            ItemStack destination = slot.getItem();
            if (!ItemStack.isSameItem(stack, destination)
                    || !ItemStack.isSameItemSameTags(stack, destination)) {
                continue;
            }

            int itemMax = destination.getMaxStackSize();
            int slotMax = slot.getMaxStackSize(destination);
            // 普通槽保持原版上限；过载槽的 slotMax 会大于 itemMax，此时以槽位上限为准。
            int maxStack = slotMax > itemMax ? slotMax : Math.min(itemMax, slotMax);
            int amount = Math.min(stack.getCount(), maxStack - destination.getCount());
            if (amount <= 0) {
                continue;
            }

            if (!simulate) {
                destination.grow(amount);
            }
            stack.shrink(amount);
            slot.setChanged();
            moved = true;
            if (stack.isEmpty()) {
                return true;
            }
        }

        for (Slot slot : slots) {
            if (!slot.mayPlace(stack) || slot.hasItem()) {
                continue;
            }
            int amount = Math.min(stack.getCount(), slot.getMaxStackSize(stack));
            if (amount <= 0) {
                continue;
            }
            ItemStack split = stack.split(amount);
            if (!simulate) {
                slot.set(split);
            }
            moved = true;
            if (stack.isEmpty()) {
                return true;
            }
        }
        return moved;
    }
}
