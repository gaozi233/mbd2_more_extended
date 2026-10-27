package com.gaozi.mbd2_more_extended.mixin.kubejs;

import com.gaozi.mbd2_more_extended.condition.IBlockConditionTags;
import com.gaozi.mbd2_more_extended.condition.IConditionVisibility;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import com.lowdragmc.mbd2.common.recipe.BlockCondition;
import com.lowdragmc.mbd2.integration.kubejs.recipe.MBDRecipeSchema;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * KubeJS 可选 Mixin：给 {@code MBDRecipeJS} 增加链式 {@code showInGui(boolean)}
 * 和支持方块标签的 {@code blocksInStructureAny(...)}。
 *
 * <p>用法：</p>
 * <pre>{@code
 * m.dimension('minecraft:overworld').showInGui(false)
 * m.blocksInStructureAny(1, 8, 'minecraft:stone', '#forge:stone')
 * }</pre>
 */
@Mixin(value = MBDRecipeSchema.MBDRecipeJS.class, remap = false)
public abstract class MBDRecipeJSMixin {

    @Unique
    private RecipeCondition mbd2MoreExtended$lastCondition;

    @Inject(method = "addCondition", at = @At("TAIL"), remap = false)
    private void mbd2$trackLastCondition(RecipeCondition condition,
                                         CallbackInfoReturnable<MBDRecipeSchema.MBDRecipeJS> cir) {
        mbd2MoreExtended$lastCondition = condition;
    }

    /**
     * 和原版 blocksInStructure 参数位置一致，但 entries 支持 Block、方块 ID 和 #tag。
     * 使用独立方法名，避免 KJS 在 Block... 与 Object... 之间产生重载歧义。
     */
    @Unique
    public MBDRecipeSchema.MBDRecipeJS blocksInStructureAny(int minCount, int maxCount, Object... entries) {
        List<Block> blocks = new ArrayList<>();
        List<TagKey<Block>> tags = new ArrayList<>();
        if (entries != null) {
            for (Object entry : entries) {
                if (entry instanceof Block block) {
                    blocks.add(block);
                } else if (entry instanceof TagKey<?> tag) {
                    @SuppressWarnings("unchecked")
                    TagKey<Block> blockTag = (TagKey<Block>) tag;
                    tags.add(blockTag);
                } else if (entry != null) {
                    String text = entry.toString().trim();
                    if (text.startsWith("#")) {
                        ResourceLocation id = ResourceLocation.tryParse(text.substring(1));
                        if (id != null) {
                            tags.add(TagKey.create(Registries.BLOCK, id));
                        }
                    } else {
                        ResourceLocation id = ResourceLocation.tryParse(text);
                        if (id != null) {
                            Block block = BuiltInRegistries.BLOCK.get(id);
                            if (block != null && !block.defaultBlockState().isAir()) {
                                blocks.add(block);
                            }
                        }
                    }
                }
            }
        }

        BlockCondition condition = new BlockCondition(minCount, maxCount, blocks.toArray(Block[]::new));
        ((IBlockConditionTags) condition).setTags(tags.toArray(TagKey[]::new));
        return ((MBDRecipeSchema.MBDRecipeJS) (Object) this).addCondition(condition);
    }

    @Unique
    public MBDRecipeSchema.MBDRecipeJS showInGui(boolean show) {
        if (mbd2MoreExtended$lastCondition instanceof IConditionVisibility visibility) {
            visibility.mbd2_more_extended$setShowInGui(show);
        }
        return (MBDRecipeSchema.MBDRecipeJS) (Object) this;
    }
}
