package com.gaozi.mbd2_more_extended.kubejs;

import com.gaozi.mbd2_more_extended.recipe.NBTCompareCondition;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.rhino.CustomFunction;

/**
 * KubeJS 可选集成：
 * <ul>
 *     <li>{@code nbtCompare(key, operator, value)}</li>
 *     <li>{@code MBD2Helper} 机器朝向/坐标辅助对象</li>
 *     <li>{@code MBD2RecipeHelper} 配方只读辅助对象</li>
 * </ul>
 *
 * <p>支持标签的 {@code blocksInStructureAny(...)} 由
 * {@code MBDRecipeJSMixin} 在 KubeJS 存在时注入。</p>
 *
 * <p>该类只有在 KubeJS 存在时才会被 KubeJS 通过 {@code kubejs.plugins.txt} 加载，
 * 本模组不安装 KubeJS 时不会加载到这个类。</p>
 */
public class MBD2MoreExtendedKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("MBD2Helper", new MBD2MachineHelper());
        event.add("MBD2RecipeHelper", new MBD2RecipeHelper());
        event.add("nbtCompare", new CustomFunction("nbtCompare", (context, args) -> {
            if (args.length != 3) {
                throw new IllegalArgumentException("nbtCompare(key, operator, value) expects exactly 3 arguments");
            }
            String key = args[0] == null ? "" : args[0].toString();
            String operator = args[1] == null ? ">=" : args[1].toString();
            double value = args[2] instanceof Number number ? number.doubleValue() : 0.0D;
            return new NBTCompareCondition(key, operator, value);
        }, new Class<?>[]{String.class, String.class, double.class}));
    }
}
