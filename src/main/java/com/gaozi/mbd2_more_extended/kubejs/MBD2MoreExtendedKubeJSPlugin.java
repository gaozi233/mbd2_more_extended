package com.gaozi.mbd2_more_extended.kubejs;

import com.gaozi.mbd2_more_extended.recipe.BlockTagCondition;
import com.gaozi.mbd2_more_extended.recipe.NBTCompareCondition;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.CustomFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KubeJS 可选集成：
 * <ul>
 *     <li>{@code nbtCompare(key, operator, value)}</li>
 *     <li>{@code blockExt(minCount, maxCount, entries)}，entries 支持
 *     {@code "minecraft:stone"} 和 {@code "#forge:stone"}</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>{@code
 * m.addCondition(blockExt(1, 8, ['minecraft:stone', '#forge:stone']))
 * }</pre>
 *
 * <p>该类只有在 KubeJS 存在时才会被 KubeJS 通过 {@code kubejs.plugins.txt} 加载，
 * 本模组不安装 KubeJS 时不会加载到这个类。</p>
 */
public class MBD2MoreExtendedKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("nbtCompare", new CustomFunction("nbtCompare", (context, args) -> {
            if (args.length != 3) {
                throw new IllegalArgumentException("nbtCompare(key, operator, value) expects exactly 3 arguments");
            }
            String key = args[0] == null ? "" : args[0].toString();
            String operator = args[1] == null ? ">=" : args[1].toString();
            double value = args[2] instanceof Number number ? number.doubleValue() : 0.0D;
            return new NBTCompareCondition(key, operator, value);
        }, new Class<?>[]{String.class, String.class, double.class}));

        event.add("blockExt", new CustomFunction("blockExt", (context, args) -> {
            if (args.length != 3) {
                throw new IllegalArgumentException("blockExt(minCount, maxCount, entries) expects exactly 3 arguments");
            }
            int minCount = args[0] instanceof Number number ? number.intValue() : 0;
            int maxCount = args[1] instanceof Number number ? number.intValue() : 0;
            List<String> entries = new ArrayList<>();
            Object raw = args[2];
            if (raw != null) {
                if (raw instanceof CharSequence) {
                    entries.addAll(Arrays.asList(raw.toString().split(",")));
                } else {
                    Object converted = Context.jsToJava(context, raw, String[].class);
                    if (converted instanceof String[] array) {
                        entries.addAll(Arrays.asList(array));
                    } else {
                        entries.add(raw.toString());
                    }
                }
                entries.replaceAll(String::trim);
            }
            return new BlockTagCondition(minCount, maxCount, entries.toArray(String[]::new));
        }, new Class<?>[]{double.class, double.class, Object.class}));
    }
}
