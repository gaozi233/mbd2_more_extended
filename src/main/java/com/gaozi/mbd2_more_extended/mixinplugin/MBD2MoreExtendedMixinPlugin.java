package com.gaozi.mbd2_more_extended.mixinplugin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 仅在 KubeJS 已加载时应用 {@code MBDRecipeJSMixin}。
 *
 * <p>KubeJS 是本模组的可选依赖；如果直接让 Mixin 配置加载 KubeJS 相关
 * Mixin，在没装 KubeJS 的客户端/服务端上可能出现目标类缺失。</p>
 */
public class MBD2MoreExtendedMixinPlugin implements IMixinConfigPlugin {

    private static final String KUBEJS_MIXIN = "MBDRecipeJSMixin";

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
        return true;
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
