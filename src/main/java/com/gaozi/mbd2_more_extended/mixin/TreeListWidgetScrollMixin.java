package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.config.MBD2MoreExtendedClientConfig;
import com.lowdragmc.lowdraglib.gui.widget.TreeListWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 给 LDLib 的 {@link TreeListWidget} 增加可见滚动条和拖动支持。
 *
 * <p>原本 TreeListWidget 只支持滚轮修改 scrollOffset，不绘制滚动条。
 * 这个 Mixin 覆盖编辑器文件选择对话框中的项目/资源列表。</p>
 */
@Mixin(value = TreeListWidget.class, remap = false)
public abstract class TreeListWidgetScrollMixin {

    @Unique
    private static final int MBD2_SCROLLBAR_WIDTH = 5;

    @Unique
    private static final int MBD2_SCROLLBAR_MIN_THUMB = 12;

    @Shadow
    protected int scrollOffset;

    @Shadow
    protected List<?> list;

    @Shadow
    protected int lineHeight;

    @Unique
    private boolean mbd2MoreExtended$draggingScrollbar;

    @Unique
    private double mbd2MoreExtended$dragOffset;

    @Inject(method = "drawInBackground", at = @At("TAIL"), remap = false)
    private void mbd2$drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!MBD2MoreExtendedClientConfig.editorEnhancements()) {
            return;
        }
        int viewHeight = mbd2$self().getSizeHeight();
        int contentHeight = mbd2$contentHeight();
        if (viewHeight <= 0 || contentHeight <= viewHeight) {
            return;
        }

        int x = mbd2$self().getPositionX() + mbd2$self().getSizeWidth() - MBD2_SCROLLBAR_WIDTH;
        int y = mbd2$self().getPositionY();
        int right = x + MBD2_SCROLLBAR_WIDTH;
        int bottom = y + viewHeight;
        int thumbHeight = mbd2$thumbHeight(viewHeight, contentHeight);
        int thumbY = y + mbd2$thumbY(viewHeight, contentHeight, thumbHeight);

        graphics.fill(x, y, right, bottom, 0x66000000);
        int color = mbd2MoreExtended$draggingScrollbar ? 0xFFE0E0E0 : 0xFFB0B0B0;
        graphics.fill(x + 1, thumbY, right - 1, thumbY + thumbHeight, color);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void mbd2$scrollbarClick(double mouseX, double mouseY, int button,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!MBD2MoreExtendedClientConfig.editorEnhancements()) {
            return;
        }
        if (button != 0 || !mbd2$isOnScrollbar(mouseX, mouseY)) {
            return;
        }
        int viewHeight = mbd2$self().getSizeHeight();
        int contentHeight = mbd2$contentHeight();
        if (contentHeight <= viewHeight) {
            return;
        }

        int thumbHeight = mbd2$thumbHeight(viewHeight, contentHeight);
        int thumbY = mbd2$thumbY(viewHeight, contentHeight, thumbHeight);
        double localY = mouseY - mbd2$self().getPositionY();
        if (localY >= thumbY && localY <= thumbY + thumbHeight) {
            mbd2MoreExtended$dragOffset = localY - thumbY;
        } else {
            mbd2MoreExtended$dragOffset = thumbHeight / 2.0D;
            mbd2$scrollToMouse(localY, viewHeight, contentHeight, thumbHeight);
        }
        mbd2MoreExtended$draggingScrollbar = true;
        cir.setReturnValue(true);
    }

    /**
     * 添加到 TreeListWidget，覆盖 Widget 的默认实现。
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!MBD2MoreExtendedClientConfig.editorEnhancements()
                || !mbd2MoreExtended$draggingScrollbar) {
            return false;
        }
        int viewHeight = mbd2$self().getSizeHeight();
        int contentHeight = mbd2$contentHeight();
        int thumbHeight = mbd2$thumbHeight(viewHeight, contentHeight);
        double localY = mouseY - mbd2$self().getPositionY();
        mbd2$scrollToMouse(localY, viewHeight, contentHeight, thumbHeight);
        return true;
    }

    /**
     * 添加到 TreeListWidget，覆盖 Widget 的默认实现。
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!MBD2MoreExtendedClientConfig.editorEnhancements()) {
            mbd2MoreExtended$draggingScrollbar = false;
            return false;
        }
        if (mbd2MoreExtended$draggingScrollbar) {
            mbd2MoreExtended$draggingScrollbar = false;
            return true;
        }
        return false;
    }

    @Unique
    private Widget mbd2$self() {
        return (Widget) (Object) this;
    }

    @Unique
    private int mbd2$contentHeight() {
        return list == null ? 0 : list.size() * lineHeight;
    }

    @Unique
    private int mbd2$thumbHeight(int viewHeight, int contentHeight) {
        int height = (int) ((double) viewHeight * viewHeight / contentHeight);
        return Mth.clamp(height, MBD2_SCROLLBAR_MIN_THUMB, viewHeight);
    }

    @Unique
    private int mbd2$thumbY(int viewHeight, int contentHeight, int thumbHeight) {
        int maxScroll = Math.max(1, contentHeight - viewHeight);
        int track = Math.max(1, viewHeight - thumbHeight);
        return Mth.clamp((int) ((double) scrollOffset / maxScroll * track), 0, track);
    }

    @Unique
    private boolean mbd2$isOnScrollbar(double mouseX, double mouseY) {
        return mouseX >= mbd2$self().getPositionX() + mbd2$self().getSizeWidth() - MBD2_SCROLLBAR_WIDTH
                && mouseX <= mbd2$self().getPositionX() + mbd2$self().getSizeWidth()
                && mouseY >= mbd2$self().getPositionY()
                && mouseY <= mbd2$self().getPositionY() + mbd2$self().getSizeHeight();
    }

    @Unique
    private void mbd2$scrollToMouse(double mouseY, int viewHeight, int contentHeight, int thumbHeight) {
        int maxScroll = Math.max(0, contentHeight - viewHeight);
        int track = Math.max(1, viewHeight - thumbHeight);
        double ratio = (mouseY - mbd2MoreExtended$dragOffset) / track;
        scrollOffset = Mth.clamp((int) Math.round(ratio * maxScroll), 0, maxScroll);
    }
}
