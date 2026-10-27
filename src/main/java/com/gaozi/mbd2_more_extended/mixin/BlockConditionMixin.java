package com.gaozi.mbd2_more_extended.mixin;

import com.gaozi.mbd2_more_extended.condition.IBlockConditionDisplay;
import com.gaozi.mbd2_more_extended.condition.IBlockConditionTags;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SearchComponentConfigurator;
import com.lowdragmc.mbd2.api.machine.IMultiController;
import com.lowdragmc.mbd2.api.pattern.MultiblockState;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import com.lowdragmc.mbd2.api.recipe.RecipeLogic;
import com.lowdragmc.mbd2.common.recipe.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 把方块标签整合进 MBD2 原版 {@code block} 条件。
 *
 * <p>保留原版的 minCount/maxCount/blocks 语义，额外支持 {@code tags} 字段；
 * 条件判断、JSON/网络/NBT 序列化、tooltip 和配置面板都会处理标签。</p>
 */
@Mixin(value = BlockCondition.class, remap = false)
public abstract class BlockConditionMixin extends RecipeCondition implements IBlockConditionDisplay, IBlockConditionTags {

    @Shadow
    private int minCount;

    @Shadow
    private int maxCount;

    @Shadow
    private Block[] blocks;

    @Unique
    private TagKey<Block>[] mbd2MoreExtended$tags = new TagKey[0];

    @Override
    public Block[] getDisplayBlocks() {
        return blocks;
    }

    @Override
    public TagKey<Block>[] getDisplayTags() {
        return mbd2MoreExtended$tags;
    }

    @Override
    public int getDisplayMinCount() {
        return minCount;
    }

    @Override
    public int getDisplayMaxCount() {
        return maxCount;
    }

    @Override
    public Component getDisplayTooltip() {
        return getTooltips();
    }

    @Override
    public TagKey<Block>[] getTags() {
        return mbd2MoreExtended$tags;
    }

    @Override
    public void setTags(TagKey<Block>[] tags) {
        mbd2MoreExtended$tags = tags == null ? new TagKey[0] : tags;
    }

    @Inject(method = "test", at = @At("HEAD"), cancellable = true, remap = false)
    private void mbd2$testWithTags(MBDRecipe recipe, RecipeLogic recipeLogic,
                                   CallbackInfoReturnable<Boolean> cir) {
        int count = 0;
        if (recipeLogic != null && recipeLogic.getMachine() instanceof IMultiController controller) {
            Level level = controller.getLevel();
            MultiblockState state = controller.getMultiblockState();
            if (level != null && state != null) {
                for (BlockPos pos : state.getCache()) {
                    BlockState blockState = level.getBlockState(pos);
                    if (mbd2$matches(blockState)) {
                        count++;
                    }
                }
            }
        }
        cir.setReturnValue(count >= minCount && count <= maxCount);
    }

    @Unique
    private boolean mbd2$matches(BlockState state) {
        Block block = state.getBlock();
        for (Block candidate : blocks) {
            if (candidate == block) {
                return true;
            }
        }
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "serialize", at = @At("RETURN"), remap = false)
    private void mbd2$serializeTags(CallbackInfoReturnable<JsonObject> cir) {
        JsonArray array = new JsonArray();
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            array.add(tag.location().toString());
        }
        if (!array.isEmpty()) {
            cir.getReturnValue().add("tags", array);
        }
    }

    @Inject(method = "deserialize", at = @At("TAIL"), remap = false)
    private void mbd2$deserializeTags(JsonObject json, CallbackInfoReturnable<RecipeCondition> cir) {
        List<TagKey<Block>> tags = new ArrayList<>();
        if (json.has("tags")) {
            for (var element : json.getAsJsonArray("tags")) {
                ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
                if (id != null) {
                    tags.add(TagKey.create(Registries.BLOCK, id));
                }
            }
        }
        mbd2MoreExtended$tags = tags.toArray(TagKey[]::new);
    }

    @Inject(method = "toNetwork", at = @At("TAIL"), remap = false)
    private void mbd2$toNetworkTags(FriendlyByteBuf buf, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        buf.writeVarInt(mbd2MoreExtended$tags.length);
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            buf.writeResourceLocation(tag.location());
        }
    }

    @Inject(method = "fromNetwork", at = @At("TAIL"), remap = false)
    private void mbd2$fromNetworkTags(FriendlyByteBuf buf, CallbackInfoReturnable<RecipeCondition> cir) {
        int size = buf.readVarInt();
        TagKey<Block>[] tags = new TagKey[size];
        for (int i = 0; i < size; i++) {
            tags[i] = TagKey.create(Registries.BLOCK, buf.readResourceLocation());
        }
        mbd2MoreExtended$tags = tags;
    }

    @Inject(method = "toNBT", at = @At("TAIL"), remap = false)
    private void mbd2$toNbtTags(CallbackInfoReturnable<CompoundTag> cir) {
        ListTag list = new ListTag();
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            list.add(StringTag.valueOf(tag.location().toString()));
        }
        cir.getReturnValue().put("tags", list);
    }

    @Inject(method = "fromNBT", at = @At("TAIL"), remap = false)
    private void mbd2$fromNbtTags(CompoundTag tag, CallbackInfoReturnable<RecipeCondition> cir) {
        ListTag list = tag.getList("tags", Tag.TAG_STRING);
        TagKey<Block>[] tags = new TagKey[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                tags[i] = TagKey.create(Registries.BLOCK, id);
            }
        }
        mbd2MoreExtended$tags = tags;
    }

    @Inject(method = "getTooltips", at = @At("RETURN"), cancellable = true, remap = false)
    private void mbd2$tooltipWithTags(CallbackInfoReturnable<Component> cir) {
        if (mbd2MoreExtended$tags.length == 0) {
            return;
        }
        MutableComponent names = Component.empty();
        boolean first = true;
        for (Block block : blocks) {
            if (!first) {
                names.append(" || ");
            }
            names.append(block.getName());
            first = false;
        }
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            if (!first) {
                names.append(" || ");
            }
            names.append(Component.literal("#" + tag.location()));
            first = false;
        }
        cir.setReturnValue(Component.translatable("recipe.condition.block.tooltip", names, minCount, maxCount));
    }

    /**
     * 添加标签过滤器；原版的 minCount/maxCount/blocks 仍由默认注解解析器生成。
     */
    public void buildConfigurator(ConfiguratorGroup group) {
        super.buildConfigurator(group);

        ArrayConfiguratorGroup<ResourceLocation> tagsConfigurator = new ArrayConfiguratorGroup<>(
                "config.predicate.tags",
                false,
                () -> new ArrayList<>(mbd2$tagIds()),
                (supplier, consumer) -> new SearchComponentConfigurator<>(
                        "", supplier, consumer, BlockTags.SAND.location(), true,
                        (search, result) -> {
                            ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();
                            if (tagManager == null) {
                                return;
                            }
                            for (ITag<Block> tag : tagManager) {
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                ResourceLocation id = tag.getKey().location();
                                if (id.toString().toLowerCase().contains(search.toLowerCase())) {
                                    result.accept(id);
                                }
                            }
                        },
                        ResourceLocation::toString),
                true);
        tagsConfigurator.setAddDefault(() -> BlockTags.SAND.location());
        tagsConfigurator.setOnUpdate(this::mbd2$setTagIds);
        tagsConfigurator.setTips("config.recipe.condition.block.tags.tooltip");

        group.addConfigurators(tagsConfigurator);
    }

    @Unique
    private List<ResourceLocation> mbd2$tagIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (TagKey<Block> tag : mbd2MoreExtended$tags) {
            ids.add(tag.location());
        }
        return ids;
    }

    @Unique
    private void mbd2$setTagIds(List<ResourceLocation> ids) {
        List<TagKey<Block>> tags = new ArrayList<>();
        for (ResourceLocation id : ids) {
            if (id != null) {
                tags.add(TagKey.create(Registries.BLOCK, id));
            }
        }
        mbd2MoreExtended$tags = tags.toArray(TagKey[]::new);
    }
}
