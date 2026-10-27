package com.gaozi.mbd2_more_extended.config;

import com.gaozi.mbd2_more_extended.MBD2MoreExtended;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 客户端编辑器增强开关。
 *
 * <p>控制本模组新增的编辑器 Mixin 功能：
 * 文件列表滚动条、配方类型搜索。关闭后保留 MBD2/LDLib 原始行为。</p>
 */
public final class MBD2MoreExtendedClientConfig {

    private static final String FILE_NAME = "mbd2_more_extended-client.properties";
    private static final String KEY_EDITOR_ENHANCEMENTS = "editorEnhancements";

    private static boolean editorEnhancements = true;

    private MBD2MoreExtendedClientConfig() {
    }

    public static boolean editorEnhancements() {
        return editorEnhancements;
    }

    public static void setEditorEnhancements(boolean enabled) {
        editorEnhancements = enabled;
        save();
    }

    public static void load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            editorEnhancements = Boolean.parseBoolean(
                    properties.getProperty(KEY_EDITOR_ENHANCEMENTS, "true"));
        } catch (IOException exception) {
            MBD2MoreExtended.LOGGER.warn("Failed to load client config {}", path, exception);
        }
    }

    private static void save() {
        Path path = configPath();
        Properties properties = new Properties();
        properties.setProperty(KEY_EDITOR_ENHANCEMENTS, Boolean.toString(editorEnhancements));
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "MBD2 More Extended client settings");
            }
        } catch (IOException exception) {
            MBD2MoreExtended.LOGGER.warn("Failed to save client config {}", path, exception);
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }
}
