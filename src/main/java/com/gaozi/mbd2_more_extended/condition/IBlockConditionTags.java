package com.gaozi.mbd2_more_extended.condition;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 原版 {@code block} 条件的标签访问接口，由 {@code BlockConditionMixin} 注入。
 */
public interface IBlockConditionTags {

    TagKey<Block>[] getTags();

    void setTags(TagKey<Block>[] tags);
}
