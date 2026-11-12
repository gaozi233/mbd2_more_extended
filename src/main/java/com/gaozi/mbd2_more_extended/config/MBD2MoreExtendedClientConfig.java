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
 * 客户端编辑器增强配置，仅用于客户端 UI，不参与服务端逻辑。
 */
public final class MBD2MoreExtendedClientConfig {

    private static final String FILE_NAME = "mbd2_more_extended-client.properties";

    private static final String KEY_GLOBAL = "editor.globalEnabled";
    private static final String KEY_FILE_LIST_SCROLLBAR = "editor.fileListScrollbar";
    private static final String KEY_RECIPE_TYPE_SEARCH = "editor.recipeTypeSearch";
    private static final String LEGACY_KEY = "editorEnhancements";

    private static boolean globalEnabled = true;
    private static boolean fileListScrollbar = true;
    private static boolean recipeTypeSearch = true;

    private MBD2MoreExtendedClientConfig() {
    }

    public static boolean isGlobalEnabled() {
        return globalEnabled;
    }

    public static boolean isFileListScrollbar() {
        return fileListScrollbar;
    }

    public static boolean isRecipeTypeSearch() {
        return recipeTypeSearch;
    }

    public static boolean fileListScrollbarEnabled() {
        return globalEnabled && fileListScrollbar;
    }

    public static boolean recipeTypeSearchEnabled() {
        return globalEnabled && recipeTypeSearch;
    }

    public static void setGlobalEnabled(boolean enabled) {
        globalEnabled = enabled;
        save();
    }

    public static void setFileListScrollbar(boolean enabled) {
        fileListScrollbar = enabled;
        save();
    }

    public static void setRecipeTypeSearch(boolean enabled) {
        recipeTypeSearch = enabled;
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
            if (properties.containsKey(LEGACY_KEY)) {
                boolean legacy = Boolean.parseBoolean(properties.getProperty(LEGACY_KEY, "true"));
                globalEnabled = legacy;
                fileListScrollbar = legacy;
                recipeTypeSearch = legacy;
                return;
            }
            globalEnabled = Boolean.parseBoolean(properties.getProperty(KEY_GLOBAL, "true"));
            fileListScrollbar = Boolean.parseBoolean(properties.getProperty(KEY_FILE_LIST_SCROLLBAR, "true"));
            recipeTypeSearch = Boolean.parseBoolean(properties.getProperty(KEY_RECIPE_TYPE_SEARCH, "true"));
        } catch (IOException exception) {
            MBD2MoreExtended.LOGGER.warn("Failed to load client config {}", path, exception);
        }
    }

    public static void save() {
        Path path = configPath();
        Properties properties = new Properties();
        properties.setProperty(KEY_GLOBAL, Boolean.toString(globalEnabled));
        properties.setProperty(KEY_FILE_LIST_SCROLLBAR, Boolean.toString(fileListScrollbar));
        properties.setProperty(KEY_RECIPE_TYPE_SEARCH, Boolean.toString(recipeTypeSearch));
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "MBD2 More Extended client editor settings");
            }
        } catch (IOException exception) {
            MBD2MoreExtended.LOGGER.warn("Failed to save client config {}", path, exception);
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }
}
