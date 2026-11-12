package com.gaozi.mbd2_more_extended.client;

import com.gaozi.mbd2_more_extended.config.MBD2MoreExtendedClientConfig;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端编辑器增强设置界面。
 *
 * <p>只负责本地开关，不会发送网络包，也不会改变服务端逻辑。</p>
 */
public final class MBD2MoreExtendedSettingsDialog {

    private static final IUIHolder HOLDER = new IUIHolder() {
        @Override
        public ModularUI createUI(Player player) {
            return null;
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return true;
        }

        @Override
        public void markAsDirty() {
        }
    };

    private MBD2MoreExtendedSettingsDialog() {
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        WidgetGroup root = new WidgetGroup(0, 0, width, height);
        root.setClientSideWidget();

        DialogWidget dialog = new DialogWidget(root, true);
        dialog.setOnClosed(() -> minecraft.setScreen(null));

        WidgetGroup content = DialogWidget.createContainer(
                dialog, 260, 170, "MBD2 More Extended");
        int buttonWidth = 220;
        int x = 20;

        ButtonWidget globalButton = new ButtonWidget(
                x, 25, buttonWidth, 20, buttonTexture(globalText(), true), data -> {
                    MBD2MoreExtendedClientConfig.setGlobalEnabled(
                            !MBD2MoreExtendedClientConfig.isGlobalEnabled());
                    refresh(dialog, content);
                });
        globalButton.setId("mbd2_global_toggle");

        ButtonWidget scrollbarButton = new ButtonWidget(
                x, 55, buttonWidth, 20, buttonTexture(scrollbarText(), true), data -> {
                    MBD2MoreExtendedClientConfig.setFileListScrollbar(
                            !MBD2MoreExtendedClientConfig.isFileListScrollbar());
                    refresh(dialog, content);
                });
        scrollbarButton.setId("mbd2_scrollbar_toggle");

        ButtonWidget searchButton = new ButtonWidget(
                x, 85, buttonWidth, 20, buttonTexture(searchText(), true), data -> {
                    MBD2MoreExtendedClientConfig.setRecipeTypeSearch(
                            !MBD2MoreExtendedClientConfig.isRecipeTypeSearch());
                    refresh(dialog, content);
                });
        searchButton.setId("mbd2_search_toggle");

        ButtonWidget closeButton = new ButtonWidget(
                x, 125, buttonWidth, 20, buttonTexture("关闭", true), data -> {
                    dialog.close();
                    minecraft.setScreen(null);
                });
        closeButton.setId("mbd2_close");

        content.addWidget(globalButton);
        content.addWidget(scrollbarButton);
        content.addWidget(searchButton);
        content.addWidget(closeButton);
        refresh(dialog, content);

        ModularUI ui = new ModularUI(root, HOLDER, minecraft.player);
        minecraft.setScreen(new ModularUIGuiContainer(ui, 0));
    }

    private static void refresh(DialogWidget dialog, WidgetGroup content) {
        boolean global = MBD2MoreExtendedClientConfig.isGlobalEnabled();

        ButtonWidget globalButton = (ButtonWidget) content.getFirstWidgetById("mbd2_global_toggle");
        ButtonWidget scrollbarButton = (ButtonWidget) content.getFirstWidgetById("mbd2_scrollbar_toggle");
        ButtonWidget searchButton = (ButtonWidget) content.getFirstWidgetById("mbd2_search_toggle");

        if (globalButton != null) {
            globalButton.setButtonTexture(buttonTexture(globalText(), true));
        }
        if (scrollbarButton != null) {
            scrollbarButton.setButtonTexture(buttonTexture(scrollbarText(), global));
            scrollbarButton.setActive(global);
        }
        if (searchButton != null) {
            searchButton.setButtonTexture(buttonTexture(searchText(), global));
            searchButton.setActive(global);
        }
    }

    private static String globalText() {
        return "全局：" + (MBD2MoreExtendedClientConfig.isGlobalEnabled() ? "开启" : "关闭");
    }

    private static String scrollbarText() {
        return "文件列表滚动条：" + (MBD2MoreExtendedClientConfig.isFileListScrollbar() ? "开启" : "关闭");
    }

    private static String searchText() {
        return "配方类型搜索：" + (MBD2MoreExtendedClientConfig.isRecipeTypeSearch() ? "开启" : "关闭");
    }

    private static IGuiTexture buttonTexture(String text, boolean enabled) {
        return new GuiTextureGroup(
                (enabled ? ColorPattern.T_GRAY : ColorPattern.T_DARK_GRAY).rectTexture(),
                new TextTexture(text)
        );
    }
}
