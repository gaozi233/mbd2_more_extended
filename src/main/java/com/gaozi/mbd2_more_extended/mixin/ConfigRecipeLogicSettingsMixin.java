package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.config.MBD2MoreExtendedClientConfig;
import com.lowdragmc.lowdraglib.gui.editor.configurator.Configurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SearchComponentConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SelectorConfigurator;
import com.lowdragmc.mbd2.MBD2;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeType;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;
import com.lowdragmc.mbd2.common.machine.definition.config.ConfigRecipeLogicSettings;
import com.lowdragmc.mbd2.utils.FileUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 把机器配置里的“配方类型”下拉框替换成可输入过滤的搜索结果控件。
 *
 * <p>MBD2 原本使用 {@link SelectorConfigurator}，配方类型很多时只能逐个找。
 * 这里保留原本的候选来源（注册表 + recipe_type/*.rt），改成
 * {@link SearchComponentConfigurator}，输入 namespace/path 片段即可过滤。</p>
 */
@Mixin(value = ConfigRecipeLogicSettings.class, remap = false)
public abstract class ConfigRecipeLogicSettingsMixin {

    @Shadow
    private ResourceLocation recipeType;

    @Inject(method = "buildConfigurator", at = @At("RETURN"), remap = false)
    private void mbd2$replaceRecipeTypeSelector(ConfiguratorGroup group, CallbackInfo ci) {
        if (!MBD2MoreExtendedClientConfig.recipeTypeSearchEnabled()) {
            return;
        }
        List<Configurator> configurators = group.getConfigurators();
        SelectorConfigurator<?> selector = null;
        int index = -1;
        for (int i = 0; i < configurators.size(); i++) {
            if (configurators.get(i) instanceof SelectorConfigurator<?> candidate) {
                selector = candidate;
                index = i;
                break;
            }
        }
        if (selector == null) {
            return;
        }

        Set<ResourceLocation> candidates = mbd2$recipeTypeCandidates();
        ConfigRecipeLogicSettings self = (ConfigRecipeLogicSettings) (Object) this;
        SearchComponentConfigurator<ResourceLocation> search = new SearchComponentConfigurator<>(
                "editor.machine.recipe_type",
                self::recipeType,
                id -> this.recipeType = id,
                MBDRecipeType.DUMMY.getRegistryName(),
                true,
                (searchText, consumer) -> {
                    String query = searchText == null
                            ? ""
                            : searchText.trim().toLowerCase(Locale.ROOT);
                    candidates.stream()
                            .filter(id -> query.isEmpty()
                                    || id.toString().toLowerCase(Locale.ROOT).contains(query))
                            .sorted(java.util.Comparator.comparing(ResourceLocation::toString))
                            .forEach(consumer);
                },
                ResourceLocation::toString
        );
        search.setMax(15);

        group.removeConfigurator(selector);
        group.addConfigurator(index, search);
    }

    /**
     * 和 MBD2 原逻辑一致：DUMMY + 已注册 recipe type + recipe_type/*.rt。
     */
    private static Set<ResourceLocation> mbd2$recipeTypeCandidates() {
        Set<ResourceLocation> candidates = new HashSet<>();
        candidates.add(MBDRecipeType.DUMMY.getRegistryName());
        candidates.addAll(MBDRegistries.RECIPE_TYPES.keys());

        File recipeTypeDir = new File(MBD2.getLocation(), "recipe_type");
        FileUtils.loadNBTFiles(recipeTypeDir, ".rt", (file, tag) -> {
            String name = tag.getCompound("recipe_type").getString("registryName");
            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id != null) {
                candidates.add(id);
            }
        });
        return candidates;
    }
}
