package com.gaozi.mbd2_more_extended.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让容器槽位同步支持超过 127 的堆叠。
 *
 * <p>原版 {@link ClientboundContainerSetSlotPacket} 使用
 * {@link FriendlyByteBuf#writeItem(ItemStack)}，而它用 byte 写 count。
 * 128 会以 -128 写入，客户端读出来就是空栈，直到重新打开 UI 触发全量同步。</p>
 */
@Mixin(value = ClientboundContainerSetSlotPacket.class, remap = false)
public abstract class ContainerSetSlotPacketOverstackMixin {

    @Redirect(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/FriendlyByteBuf;"
            ),
            remap = false
    )
    private FriendlyByteBuf mbd2$writeSlotItem(FriendlyByteBuf buf, ItemStack stack) {
        if (stack.getCount() > Byte.MAX_VALUE) {
            // 先写“超量标记”，再写完整 NBT；普通数量保持原版格式。
            buf.writeBoolean(true);
            buf.writeNbt(stack.save(new CompoundTag()));
        } else {
            buf.writeBoolean(false);
            buf.writeItem(stack);
        }
        return buf;
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;readItem()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack mbd2$readSlotItem(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            CompoundTag tag = buf.readNbt();
            return tag == null ? ItemStack.EMPTY : ItemStack.of(tag);
        }
        return buf.readItem();
    }
}
