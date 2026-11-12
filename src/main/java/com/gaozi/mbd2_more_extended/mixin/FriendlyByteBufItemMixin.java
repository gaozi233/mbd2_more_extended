package com.gaozi.mbd2_more_extended.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让原版物品网络编码支持超过 127 的堆叠。
 *
 * <p>原版会用 byte 写 count，128 变成 -128；这里对超过 127 的数量使用一个
 * AIR 槽位作为哨兵，并把完整 ItemStack NBT 放在后面。普通数量和原版格式完全一致，
 * 因此不会影响未超量的物品同步。</p>
 */
@Mixin(value = FriendlyByteBuf.class)
public abstract class FriendlyByteBufItemMixin {

    @Inject(method = "writeItem", at = @At("HEAD"), cancellable = true)
    private void mbd2$writeOverstackItem(ItemStack stack,
                                         CallbackInfoReturnable<FriendlyByteBuf> cir) {
        if (!stack.isEmpty() && stack.getCount() > Byte.MAX_VALUE) {
            FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
            // 原版格式：非空标记 + 物品 id + byte 数量 + NBT。
            // 用 AIR 作为哨兵，真正的 ItemStack 放在 NBT 中。
            buf.writeBoolean(true);
            buf.writeId(BuiltInRegistries.ITEM, Items.AIR);
            buf.writeByte(1);
            buf.writeNbt(stack.save(new CompoundTag()));
            cir.setReturnValue(buf);
        }
    }

    /**
     * @author MBD2 More Extended
     * @reason 识别 writeItem 中的 AIR 哨兵并恢复超过 127 的完整 ItemStack。
     */
    @Overwrite(aliases = "m_130267_", remap = false)
    public ItemStack readItem() {
        FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        }

        Item item = buf.readById(BuiltInRegistries.ITEM);
        int count = buf.readByte();
        CompoundTag tag = buf.readNbt();

        if (item == Items.AIR && tag != null) {
            ItemStack overstack = ItemStack.of(tag);
            if (!overstack.isEmpty()) {
                return overstack;
            }
        }

        ItemStack stack = new ItemStack(item, count);
        stack.readShareTag(tag);
        return stack;
    }
}
