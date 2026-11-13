package com.gaozi.mbd2_more_extended.mixinplugin;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 控制依赖其他模组 API 的 Mixin 是否应用。
 */
public class MBD2MoreExtendedMixinPlugin implements IMixinConfigPlugin {

    private static final String KUBEJS_MIXIN = "MBDRecipeJSMixin";

    /**
     * These Mixins support LDLib 1.0.40+ with dual official/SRG target names.
     */
    private static final Set<String> LDLIB_040_MIXINS = Set.of(
            "TreeListWidgetScrollMixin",
            "SlotWidgetItemTransferMaxStackMixin",
            "ModularUIContainerMergeMixin",
            "ModularUIContainerQuickMoveMixin",
            "SlotWidgetOverstackDisplayMixin",
            "LDLibJEISlotTooltipMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
        // 不需要额外初始化。
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(KUBEJS_MIXIN)) {
            LoadingModList modList = LoadingModList.get();
            return modList != null && modList.getModFileById("kubejs") != null;
        }

        if (LDLIB_040_MIXINS.stream().anyMatch(mixinClassName::endsWith)) {
            return isLdLibAtLeast("1.0.40");
        }
        return true;
    }

    private static boolean isLdLibAtLeast(String requiredVersion) {
        LoadingModList modList = LoadingModList.get();
        if (modList == null) {
            return false;
        }
        IModFileInfo ldlib = modList.getModFileById("ldlib");
        if (ldlib == null) {
            return false;
        }
        return compareVersions(ldlib.versionString(), requiredVersion) >= 0;
    }

    private static int compareVersions(String actual, String required) {
        int[] left = numericParts(actual);
        int[] right = numericParts(required);
        int size = Math.max(left.length, right.length);
        for (int i = 0; i < size; i++) {
            int a = i < left.length ? left[i] : 0;
            int b = i < right.length ? right[i] : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }

    private static int[] numericParts(String version) {
        if (version == null || version.isBlank()) {
            return new int[0];
        }
        String[] parts = version.split("[^0-9]+");
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                count++;
            }
        }
        int[] result = new int[count];
        int index = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    result[index++] = Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                    return new int[0];
                }
            }
        }
        return result;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // 不需要处理目标集合。
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
        // 不需要在应用前修改目标类。
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        // 不需要在应用后修改目标类。
    }
}
