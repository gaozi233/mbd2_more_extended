package com.gaozi.mbd2_more_extended.condition;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 供配方 UI 的 {@code @block_condition_x} 槽位读取方块条件内容。
 */
public interface IBlockConditionDisplay {

    Block[] getDisplayBlocks();

    TagKey<Block>[] getDisplayTags();

    Component getDisplayTooltip();
}
