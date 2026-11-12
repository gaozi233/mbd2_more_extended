package com.gaozi.mbd2_more_extended.mixin;

import com.lowdragmc.lowdraglib.syncdata.payload.ItemStackPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * 让 LDLib 的 ItemStack 同步支持超过 127 的数量。
 *
 * <p>原版 {@link FriendlyByteBuf#writeItem(ItemStack)} 用 byte 写 count，
 * 128 会变成 -128，客户端读出来就是空栈；重新打开 UI 时走 NBT 全量同步才恢复。
 * 这里只对超过 127 的堆叠改用完整 NBT 传输，127 及以下保持原格式。</p>
 */
@Mixin(value = ItemStackPayload.class, remap = false)
public abstract class ItemStackPayloadMixin {

    @Shadow
    protected Object payload;

    /**
     * @author MBD2 More Extended
     * @reason 支持过载物品槽超过 127 的堆叠数量同步。
     */
    @Overwrite
    public void writePayload(FriendlyByteBuf buf) {
        ItemStack stack = (ItemStack) payload;
        if (stack.getCount() > Byte.MAX_VALUE) {
            buf.writeBoolean(true);
            buf.writeNbt(stack.save(new CompoundTag()));
        } else {
            buf.writeBoolean(false);
            buf.writeItem(stack);
        }
    }

    /**
     * @author MBD2 More Extended
     * @reason 与 writePayload 对应的超量堆叠读取。
     */
    @Overwrite
    public void readPayload(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            CompoundTag tag = buf.readNbt();
            payload = tag == null ? ItemStack.EMPTY : ItemStack.of(tag);
        } else {
            payload = buf.readItem();
        }
    }
}
