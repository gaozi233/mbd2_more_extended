package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.condition.IBlockConditionDisplay;
import com.gaozi.mbd2_more_extended.condition.IConditionVisibility;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeType;
import com.lowdragmc.mbd2.utils.WidgetUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 配方 GUI 条件描述过滤，以及 {@code @block_condition_x} 槽位的条件方块显示。
 */
@Mixin(value = MBDRecipeType.class, remap = false)
public class MBDRecipeTypeMixin {

    private static final int MAX_DISPLAY_ITEMS = 256;
    private static final String BLOCK_CONDITION_PREFIX = "@block_condition_";

    @Inject(method = "bindXEIRecipeUI", at = @At("TAIL"), remap = false)
    private void mbd2$filterConditionDescription(WidgetGroup root, MBDRecipe recipe, CallbackInfo ci) {
        mbd2$updateConditionDescription(root, recipe);
        mbd2$bindConditionSlots(root, recipe);
    }

    /**
     * JEI/EMI/REI 走的是 {@code createRecipeUI}，不会调用 {@code bindXEIRecipeUI}。
     * 因此这里也要绑定一次条件槽位。
     */
    @Inject(method = "createRecipeUI", at = @At("RETURN"), remap = false)
    private void mbd2$bindCreatedRecipeUI(MBDRecipe recipe, CallbackInfoReturnable<WidgetGroup> cir) {
        WidgetGroup root = cir.getReturnValue();
        mbd2$updateConditionDescription(root, recipe);
        mbd2$bindConditionSlots(root, recipe);
    }

    private static void mbd2$updateConditionDescription(WidgetGroup root, MBDRecipe recipe) {
        if (root == null || recipe == null || recipe.conditions == null) {
            return;
        }
        List<String> lines = recipe.conditions.stream()
                .filter(condition -> !(condition instanceof IConditionVisibility visibility)
                        || visibility.mbd2_more_extended$isShowInGui())
                .map(condition -> condition.getTooltips().getString())
                .toList();
        WidgetUtils.widgetByIdForEach(root, "^@condition$", TextBoxWidget.class,
                textBox -> textBox.setContent(lines));
    }

    /**
     * 把 {@code @block_condition_<index>} 槽位显示为该条件下方的方块/标签物品循环。
     */
    private static void mbd2$bindConditionSlots(WidgetGroup root, MBDRecipe recipe) {
        if (root == null || recipe == null || recipe.conditions == null) {
            return;
        }
        for (Widget widget : WidgetUtils.getWidgetsById(root, "^@block_condition_\\d+$")) {
            if (!(widget instanceof SlotWidget slot)) {
                continue;
            }
            int index = mbd2$conditionIndex(widget.getId());
            IBlockConditionDisplay display = mbd2$findBlockCondition(recipe, index);
            if (display == null) {
                continue;
            }
            List<ItemStack> stacks = mbd2$displayStacks(display);
            if (stacks.isEmpty()) {
                continue;
            }

            slot.setHandlerSlot(new CycleItemStackHandler(List.of(stacks)), 0);
            // 条件槽位不是配方输入，但在 JEI 中作为 CATALYST 参与“用途”查询。
            slot.setIngredientIO(IngredientIO.CATALYST);
            slot.setCanTakeItems(false);
            slot.setCanPutItems(false);
            slot.setDrawHoverTips(true);
            slot.setOnAddedTooltips((ignored, tooltips) -> tooltips.add(display.getDisplayTooltip()));
        }
    }

    private static int mbd2$conditionIndex(String id) {
        if (id == null || !id.startsWith(BLOCK_CONDITION_PREFIX)) {
            return -1;
        }
        try {
            return Integer.parseInt(id.substring(BLOCK_CONDITION_PREFIX.length()));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    /**
     * 先按 conditions 全局索引找；找不到时按“第几个方块条件”兜底，
     * 这样 {@code @block_condition_0} 在前置条件不是方块条件时也能工作。
     */
    private static IBlockConditionDisplay mbd2$findBlockCondition(MBDRecipe recipe, int index) {
        if (index < 0) {
            return null;
        }
        if (index < recipe.conditions.size()
                && recipe.conditions.get(index) instanceof IBlockConditionDisplay display) {
            return display;
        }
        int ordinal = 0;
        for (var condition : recipe.conditions) {
            if (condition instanceof IBlockConditionDisplay display) {
                if (ordinal == index) {
                    return display;
                }
                ordinal++;
            }
        }
        return null;
    }

    private static List<ItemStack> mbd2$displayStacks(IBlockConditionDisplay display) {
        LinkedHashSet<Item> items = new LinkedHashSet<>();
        for (Block block : display.getDisplayBlocks()) {
            mbd2$addItem(items, block);
        }
        for (TagKey<Block> tag : display.getDisplayTags()) {
            BuiltInRegistries.BLOCK.getTag(tag).ifPresent(holders -> {
                for (Holder<Block> holder : holders) {
                    if (items.size() >= MAX_DISPLAY_ITEMS) {
                        return;
                    }
                    mbd2$addItem(items, holder.value());
                }
            });
            if (items.size() >= MAX_DISPLAY_ITEMS) {
                break;
            }
        }

        List<ItemStack> stacks = new ArrayList<>(items.size());
        for (Item item : items) {
            stacks.add(mbd2$displayStack(item, display));
        }
        return stacks;
    }

    private static ItemStack mbd2$displayStack(Item item, IBlockConditionDisplay display) {
        ItemStack stack = new ItemStack(item);
        CompoundTag displayTag = stack.getOrCreateTagElement("display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.translatable("recipe.condition.block.count_range",
                        display.getDisplayMinCount(), display.getDisplayMaxCount()))));
        displayTag.put("Lore", lore);
        return stack;
    }

    private static void mbd2$addItem(LinkedHashSet<Item> items, Block block) {
        Item item = block.asItem();
        if (item != Items.AIR) {
            items.add(item);
        }
    }
}
