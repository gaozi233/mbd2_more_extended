package com.gaozi.mbd2_more_extended.kubejs;

import com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.content.Content;
import com.lowdragmc.mbd2.api.registry.MBDRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MBD2 配方的只读辅助方法，通过 KubeJS 绑定为 {@code MBD2RecipeHelper}。
 *
 * <p>只负责读取现有配方，不接收机器/事件，也不修改 builder。</p>
 */
public final class MBD2RecipeHelper {

    /** 按注册名取 recipe capability；不存在时返回 null。 */
    public RecipeCapability<?> capability(String name) {
        return name == null ? null : MBDRegistries.RECIPE_CAPABILITIES.get(name);
    }

    public List<Content> inputContents(MBDRecipe recipe, String capName) {
        return contents(recipe == null ? null : recipe.inputs, capability(capName), null);
    }

    public List<Content> inputContents(MBDRecipe recipe, String capName, boolean perTick) {
        return contents(recipe == null ? null : recipe.inputs, capability(capName), perTick);
    }

    public List<Content> outputContents(MBDRecipe recipe, String capName) {
        return contents(recipe == null ? null : recipe.outputs, capability(capName), null);
    }

    public List<Content> outputContents(MBDRecipe recipe, String capName, boolean perTick) {
        return contents(recipe == null ? null : recipe.outputs, capability(capName), perTick);
    }

    public Content firstInput(MBDRecipe recipe, String capName, boolean perTick) {
        return first(inputContents(recipe, capName, perTick));
    }

    public Content firstOutput(MBDRecipe recipe, String capName, boolean perTick) {
        return first(outputContents(recipe, capName, perTick));
    }

    public boolean hasInput(MBDRecipe recipe, String capName) {
        return !inputContents(recipe, capName).isEmpty();
    }

    public boolean hasOutput(MBDRecipe recipe, String capName) {
        return !outputContents(recipe, capName).isEmpty();
    }

    /**
     * 仅对数值型 capability 有意义；非数值 content 会被忽略。
     */
    public double inputAmount(MBDRecipe recipe, String capName, boolean perTick) {
        return sum(inputContents(recipe, capName, perTick));
    }

    public double outputAmount(MBDRecipe recipe, String capName, boolean perTick) {
        return sum(outputContents(recipe, capName, perTick));
    }

    private static List<Content> contents(Map<RecipeCapability<?>, List<Content>> map,
                                          RecipeCapability<?> capability,
                                          Boolean perTick) {
        if (map == null || capability == null) {
            return Collections.emptyList();
        }
        List<Content> source = map.get(capability);
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        if (perTick == null) {
            return Collections.unmodifiableList(new ArrayList<>(source));
        }
        List<Content> filtered = new ArrayList<>();
        for (Content content : source) {
            if (content != null && content.perTick == perTick) {
                filtered.add(content);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    private static Content first(List<Content> contents) {
        return contents.isEmpty() ? null : contents.get(0);
    }

    private static double sum(List<Content> contents) {
        double total = 0.0D;
        for (Content content : contents) {
            if (content != null && content.content instanceof Number number) {
                total += number.doubleValue();
            }
        }
        return total;
    }
}
