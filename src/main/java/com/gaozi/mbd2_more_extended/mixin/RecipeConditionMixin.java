package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.condition.IConditionVisibility;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 给 MBD2 的所有 {@link RecipeCondition} 增加“是否显示在 GUI 条件描述中”的开关。
 *
 * <p>字段带 {@link Configurable}，因此条件编辑器会自动生成复选框；
 * 序列化写入 JSON/网络/NBT，KubeJS 可通过 {@code condition.showInGui(false)}
 * 或 {@code recipeBuilder.showInGui(false)} 修改。</p>
 */
@Mixin(value = RecipeCondition.class, remap = false)
public abstract class RecipeConditionMixin implements IConditionVisibility {

    @Unique
    @Configurable(
            name = "config.recipe.condition.show_in_gui",
            tips = {"config.recipe.condition.show_in_gui.tooltip"},
            collapse = false,
            persisted = true
    )
    private boolean mbd2MoreExtended$showInGui = true;

    @Override
    public boolean mbd2_more_extended$isShowInGui() {
        return mbd2MoreExtended$showInGui;
    }

    @Override
    public void mbd2_more_extended$setShowInGui(boolean show) {
        mbd2MoreExtended$showInGui = show;
    }

    /**
     * KubeJS 可直接在条件对象上链式调用：{@code blocksInStructureAny(...).showInGui(false)}。
     */
    @Unique
    public RecipeCondition showInGui(boolean show) {
        mbd2MoreExtended$showInGui = show;
        return (RecipeCondition) (Object) this;
    }

    @Inject(method = "serialize", at = @At("RETURN"), remap = false)
    private void mbd2$serializeShowInGui(CallbackInfoReturnable<JsonObject> cir) {
        JsonObject json = cir.getReturnValue();
        if (!mbd2MoreExtended$showInGui) {
            json.addProperty("showInGui", false);
        }
    }

    @Inject(method = "deserialize", at = @At("TAIL"), remap = false)
    private void mbd2$deserializeShowInGui(JsonObject json, CallbackInfoReturnable<RecipeCondition> cir) {
        mbd2MoreExtended$showInGui = !json.has("showInGui") || json.get("showInGui").getAsBoolean();
    }

    @Inject(method = "toNetwork", at = @At("TAIL"), remap = false)
    private void mbd2$toNetworkShowInGui(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(mbd2MoreExtended$showInGui);
    }

    @Inject(method = "fromNetwork", at = @At("TAIL"), remap = false)
    private void mbd2$fromNetworkShowInGui(FriendlyByteBuf buf, CallbackInfoReturnable<RecipeCondition> cir) {
        mbd2MoreExtended$showInGui = buf.readBoolean();
    }

    @Inject(method = "toNBT", at = @At("TAIL"), remap = false)
    private void mbd2$toNbtShowInGui(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("showInGui", mbd2MoreExtended$showInGui);
    }

    @Inject(method = "fromNBT", at = @At("TAIL"), remap = false)
    private void mbd2$fromNbtShowInGui(CompoundTag tag, CallbackInfoReturnable<RecipeCondition> cir) {
        mbd2MoreExtended$showInGui = !tag.contains("showInGui") || tag.getBoolean("showInGui");
    }
}
