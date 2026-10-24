package com.gaozi.mbd2_more_extended.util;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Ars Nouveau 魔源条（Mana Bar）的 GUI 贴图。
 *
 * <p>贴图来自 Ars 的 {@code textures/gui/manabar_gui_border.png} 与
 * {@code manabar_gui_mana.png}。它们是 256x256 的图集：</p>
 * <ul>
 *     <li>边框/空槽：左上角 (0,0)-(107,17)，实际条框在 (6,6)-(107,17)；</li>
 *     <li>魔源填充：从 (0,0) 开始的紫色纹理，HUD 只截取 6px 高的一段；</li>
 * </ul>
 * <p>这里用 {@link ResourceTexture} 的归一化 UV 截取子区域，绘制时缩放到控件大小。</p>
 */
public final class ArsSourceTextures {

    public static final ResourceLocation MANA_BAR_BORDER =
            new ResourceLocation("ars_nouveau", "textures/gui/manabar_gui_border.png");
    public static final ResourceLocation MANA_BAR_FILL =
            new ResourceLocation("ars_nouveau", "textures/gui/manabar_gui_mana.png");

    /** Ars HUD 里整条 manabar 的尺寸（含 6px 透明外边距）。 */
    public static final int BAR_WIDTH = 108;
    public static final int BAR_HEIGHT = 18;
    /** 内部可填充区域（金色边框以内）。 */
    public static final int BAR_INNER_X = 9;
    public static final int BAR_INNER_Y = 9;
    public static final int BAR_INNER_WIDTH = 96;
    public static final int BAR_INNER_HEIGHT = 6;

    /** Ars 的两张 GUI 贴图都是 256x256 的图集，ResourceTexture 的偏移/尺寸是归一化 UV。 */
    private static final float ATLAS = 256.0F;

    private ArsSourceTextures() {
    }

    private static float uv(float pixels) {
        return pixels / ATLAS;
    }

    /** 整条魔源条背景（金色边框 + 深紫空槽）。 */
    public static IGuiTexture bar() {
        return new ResourceTexture(MANA_BAR_BORDER, 0.0F, 0.0F, uv(BAR_WIDTH), uv(BAR_HEIGHT));
    }

    /** 魔源填充纹理（从图集左上角截取 width x height）。 */
    public static IGuiTexture fill(int width, int height) {
        return new ResourceTexture(MANA_BAR_FILL, 0.0F, 0.0F, uv(width), uv(height));
    }

    /** 18x18 配方预览用的条框（截取左侧条头）。 */
    public static IGuiTexture preview() {
        return new ResourceTexture(MANA_BAR_BORDER, uv(6), uv(6), uv(18), uv(18));
    }

    /** JEI/EMI 配方查看器里的 50x14 条框。 */
    public static IGuiTexture xei() {
        return new ResourceTexture(MANA_BAR_BORDER, uv(6), uv(6), uv(50), uv(14));
    }
}
