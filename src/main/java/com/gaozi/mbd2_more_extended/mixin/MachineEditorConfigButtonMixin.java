package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.config.MBD2MoreExtendedClientConfig;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.ui.MenuPanel;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.mbd2.common.gui.editor.MachineEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 MBD 机器编辑器顶部菜单栏添加本模组的配置开关。
 */
@Mixin(value = MachineEditor.class, remap = false)
public abstract class MachineEditorConfigButtonMixin {

    @Inject(method = "initEditorViews", at = @At("TAIL"), remap = false)
    private void mbd2$addConfigButton(CallbackInfo ci) {
        MachineEditor editor = (MachineEditor) (Object) this;
        MenuPanel menu = editor.getMenuPanel();
        if (menu == null || menu.getFirstWidgetById("mbd2_config_button") != null) {
            return;
        }

        int width = 74;
        int height = Math.max(12, MenuPanel.HEIGHT - 2);
        ButtonWidget button = new ButtonWidget(
                menu.getSizeWidth() - width - 2,
                1,
                width,
                height,
                new GuiTextureGroup(
                        ColorPattern.T_GRAY.rectTexture(),
                        new TextTexture(() -> "MBD2+ " + (MBD2MoreExtendedClientConfig.editorEnhancements()
                                ? "ON"
                                : "OFF"))
                ),
                data -> MBD2MoreExtendedClientConfig.setEditorEnhancements(
                        !MBD2MoreExtendedClientConfig.editorEnhancements())
        );
        button.setId("mbd2_config_button");
        button.setHoverTooltips("MBD2 More Extended", "切换编辑器 Mixin 增强");
        menu.addWidget(button);
    }
}
