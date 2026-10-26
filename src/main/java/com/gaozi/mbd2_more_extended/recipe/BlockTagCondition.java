package com.gaozi.mbd2_more_extended.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.BlockConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.NumberConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SearchComponentConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.mbd2.api.machine.IMultiController;
import com.lowdragmc.mbd2.api.pattern.MultiblockState;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import com.lowdragmc.mbd2.api.recipe.RecipeLogic;
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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 多方块“包含方块/方块标签”的配方条件。
 *
 * <p>与 MBD2 自带的 {@code block} 条件不同，本条件额外支持方块标签，
 * 且可以通过配方 UI 里的 {@code @block_condition_x} 槽位显示条件内容。</p>
 *
 * <p>JSON 示例：</p>
 * <pre>{@code
 * {
 *   "type": "block_ext",
 *   "data": {
 *     "minCount": 1,
 *     "maxCount": 8,
 *     "blocks": ["minecraft:stone"],
 *     "tags": ["forge:stone"]
 *   }
 * }
 * }</pre>
 */
public class BlockTagCondition extends RecipeCondition {

    public static final String TYPE = "block_ext";

    private int minCount = 0;
    private int maxCount = 0;
    private Block[] blocks = new Block[0];
    private TagKey<Block>[] tags = new TagKey[0];

    /** MBD2 反射反序列化需要公开无参构造。 */
    public BlockTagCondition() {
    }

    public BlockTagCondition(int minCount, int maxCount, String... entries) {
        this.minCount = Math.max(0, minCount);
        this.maxCount = Math.max(this.minCount, maxCount);
        List<Block> blockList = new ArrayList<>();
        List<TagKey<Block>> tagList = new ArrayList<>();
        if (entries != null) {
            for (String entry : entries) {
                addEntry(blockList, tagList, entry);
            }
        }
        this.blocks = blockList.toArray(Block[]::new);
        this.tags = tagList.toArray(TagKey[]::new);
    }

    public BlockTagCondition(int minCount, int maxCount, Block[] blocks, TagKey<Block>[] tags) {
        this.minCount = Math.max(0, minCount);
        this.maxCount = Math.max(this.minCount, maxCount);
        this.blocks = blocks == null ? new Block[0] : blocks;
        this.tags = tags == null ? new TagKey[0] : tags;
    }

    // ------------------------------------------------------------------
    // RecipeCondition
    // ------------------------------------------------------------------

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        MutableComponent names = Component.empty();
        boolean first = true;
        for (Block block : blocks) {
            if (!first) {
                names.append(" || ");
            }
            names.append(block.getName());
            first = false;
        }
        for (TagKey<Block> tag : tags) {
            if (!first) {
                names.append(" || ");
            }
            names.append(Component.literal("#" + tag.location()));
            first = false;
        }
        return Component.translatable("recipe.condition.block.tooltip", names, minCount, maxCount);
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture("B");
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup group) {
        super.buildConfigurator(group);

        NumberConfigurator minConfigurator = new NumberConfigurator(
                "config.recipe.condition.block.min",
                this::getMinCount,
                number -> setMinCount(number == null ? 0 : number.intValue()),
                minCount, false);
        minConfigurator.setRange(0, Integer.MAX_VALUE);
        minConfigurator.setTips("config.recipe.condition.block_ext.min_count.tooltip");

        NumberConfigurator maxConfigurator = new NumberConfigurator(
                "config.recipe.condition.block.max",
                this::getMaxCount,
                number -> setMaxCount(number == null ? 0 : number.intValue()),
                maxCount, false);
        maxConfigurator.setRange(0, Integer.MAX_VALUE);
        maxConfigurator.setTips("config.recipe.condition.block_ext.max_count.tooltip");

        // 和 MBD2 自带 block 条件一致的方块过滤器。
        ArrayConfiguratorGroup<Block> blocksConfigurator = new ArrayConfiguratorGroup<>(
                "config.recipe.condition.block.blocks",
                false,
                () -> new ArrayList<>(Arrays.asList(blocks)),
                (supplier, consumer) -> new BlockConfigurator("", supplier, consumer, Blocks.AIR, false),
                true);
        blocksConfigurator.setAddDefault(() -> Blocks.AIR);
        blocksConfigurator.setOnUpdate(list -> blocks = list.toArray(Block[]::new));
        blocksConfigurator.setTips("config.recipe.condition.block_ext.blocks.tooltip");

        // 标签过滤器：搜索 Forge 方块标签，显示为 ResourceLocation。
        ArrayConfiguratorGroup<ResourceLocation> tagsConfigurator = new ArrayConfiguratorGroup<>(
                "config.predicate.tags",
                false,
                () -> new ArrayList<>(tagIds()),
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
        tagsConfigurator.setOnUpdate(this::setTagsFromList);
        tagsConfigurator.setTips("config.recipe.condition.block_ext.tags.tooltip");

        group.addConfigurators(minConfigurator, maxConfigurator, blocksConfigurator, tagsConfigurator);
    }

    @Override
    public boolean test(MBDRecipe recipe, RecipeLogic recipeLogic) {
        if (recipeLogic == null || !(recipeLogic.getMachine() instanceof IMultiController controller)) {
            return false;
        }
        Level level = controller.getLevel();
        MultiblockState multiblockState = controller.getMultiblockState();
        if (level == null || multiblockState == null) {
            return false;
        }
        Collection<BlockPos> cache = multiblockState.getCache();
        if (cache == null || cache.isEmpty()) {
            return false;
        }
        int count = 0;
        for (BlockPos pos : cache) {
            BlockState state = level.getBlockState(pos);
            if (matches(state)) {
                count++;
            }
        }
        return count >= minCount && count <= maxCount;
    }

    private boolean matches(BlockState state) {
        Block block = state.getBlock();
        for (Block candidate : blocks) {
            if (candidate == block) {
                return true;
            }
        }
        for (TagKey<Block> tag : tags) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // JSON
    // ------------------------------------------------------------------

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.addProperty("minCount", minCount);
        json.addProperty("maxCount", maxCount);
        json.add("blocks", blockIdsToJson());
        json.add("tags", tagIdsToJson());
        return json;
    }

    @Override
    public RecipeCondition deserialize(JsonObject json) {
        super.deserialize(json);
        minCount = Math.max(0, GsonHelper.getAsInt(json, "minCount", 0));
        maxCount = Math.max(minCount, GsonHelper.getAsInt(json, "maxCount", 0));

        List<Block> blockList = new ArrayList<>();
        List<TagKey<Block>> tagList = new ArrayList<>();
        if (json.has("blocks")) {
            for (var element : GsonHelper.getAsJsonArray(json, "blocks")) {
                addEntry(blockList, tagList, element.getAsString());
            }
        }
        if (json.has("tags")) {
            for (var element : GsonHelper.getAsJsonArray(json, "tags")) {
                addTag(tagList, element.getAsString());
            }
        }
        blocks = blockList.toArray(Block[]::new);
        tags = tagList.toArray(TagKey[]::new);
        return this;
    }

    // ------------------------------------------------------------------
    // Network
    // ------------------------------------------------------------------

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeVarInt(minCount);
        buf.writeVarInt(maxCount);
        buf.writeVarInt(blocks.length);
        for (Block block : blocks) {
            buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
        }
        buf.writeVarInt(tags.length);
        for (TagKey<Block> tag : tags) {
            buf.writeResourceLocation(tag.location());
        }
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        minCount = buf.readVarInt();
        maxCount = buf.readVarInt();
        int blockSize = buf.readVarInt();
        Block[] blockArray = new Block[blockSize];
        for (int i = 0; i < blockSize; i++) {
            Block block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());
            blockArray[i] = block;
        }
        blocks = blockArray;
        int tagSize = buf.readVarInt();
        TagKey<Block>[] tagArray = new TagKey[tagSize];
        for (int i = 0; i < tagSize; i++) {
            tagArray[i] = TagKey.create(Registries.BLOCK, buf.readResourceLocation());
        }
        tags = tagArray;
        return this;
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = super.toNBT();
        tag.putInt("minCount", minCount);
        tag.putInt("maxCount", maxCount);
        tag.put("blocks", stringList(blockIdsAsStrings()));
        tag.put("tags", stringList(tagIdsAsStrings()));
        return tag;
    }

    @Override
    public RecipeCondition fromNBT(CompoundTag tag) {
        super.fromNBT(tag);
        minCount = Math.max(0, tag.getInt("minCount"));
        maxCount = Math.max(minCount, tag.getInt("maxCount"));

        List<Block> blockList = new ArrayList<>();
        List<TagKey<Block>> tagList = new ArrayList<>();
        ListTag blockTags = tag.getList("blocks", Tag.TAG_STRING);
        for (int i = 0; i < blockTags.size(); i++) {
            addEntry(blockList, tagList, blockTags.getString(i));
        }
        ListTag tagTags = tag.getList("tags", Tag.TAG_STRING);
        for (int i = 0; i < tagTags.size(); i++) {
            addTag(tagList, tagTags.getString(i));
        }
        blocks = blockList.toArray(Block[]::new);
        tags = tagList.toArray(TagKey[]::new);
        return this;
    }

    // ------------------------------------------------------------------
    // Config helpers
    // ------------------------------------------------------------------

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = Math.max(0, minCount);
        this.maxCount = Math.max(this.minCount, this.maxCount);
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = Math.max(this.minCount, maxCount);
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public void setBlocks(Block[] blocks) {
        this.blocks = blocks == null ? new Block[0] : blocks;
    }

    public TagKey<Block>[] getTags() {
        return tags;
    }

    public void setTags(TagKey<Block>[] tags) {
        this.tags = tags == null ? new TagKey[0] : tags;
    }

    private String blocksAsString() {
        List<String> ids = new ArrayList<>();
        for (Block block : blocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id != null) {
                ids.add(id.toString());
            }
        }
        return String.join(", ", ids);
    }

    private void setBlocksFromString(String value) {
        List<Block> blockList = new ArrayList<>();
        List<TagKey<Block>> tagList = new ArrayList<>();
        for (String entry : splitEntries(value)) {
            addEntry(blockList, tagList, entry);
        }
        blocks = blockList.toArray(Block[]::new);
        tags = tagList.toArray(TagKey[]::new);
    }

    private String tagsAsString() {
        List<String> ids = new ArrayList<>();
        for (TagKey<Block> tag : tags) {
            ids.add(tag.location().toString());
        }
        return String.join(", ", ids);
    }

    private List<ResourceLocation> tagIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (TagKey<Block> tag : tags) {
            ids.add(tag.location());
        }
        return ids;
    }

    private void setTagsFromList(List<ResourceLocation> ids) {
        List<TagKey<Block>> tagList = new ArrayList<>();
        for (ResourceLocation id : ids) {
            if (id != null) {
                tagList.add(TagKey.create(Registries.BLOCK, id));
            }
        }
        tags = tagList.toArray(TagKey[]::new);
    }

    private void setTagsFromString(String value) {
        List<TagKey<Block>> tagList = new ArrayList<>();
        for (String entry : splitEntries(value)) {
            addTag(tagList, entry);
        }
        tags = tagList.toArray(TagKey[]::new);
    }

    private static List<String> splitEntries(String value) {
        List<String> entries = new ArrayList<>();
        if (value == null) {
            return entries;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    private static void addEntry(List<Block> blockList, List<TagKey<Block>> tagList, String value) {
        if (value == null) {
            return;
        }
        String entry = value.trim();
        if (entry.isEmpty()) {
            return;
        }
        if (entry.startsWith("#")) {
            addTag(tagList, entry.substring(1));
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(entry);
        if (id == null) {
            return;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block != null && !block.defaultBlockState().isAir()) {
            blockList.add(block);
        }
    }

    private static void addTag(List<TagKey<Block>> tagList, String value) {
        if (value == null) {
            return;
        }
        String entry = value.trim();
        if (entry.startsWith("#")) {
            entry = entry.substring(1);
        }
        ResourceLocation id = ResourceLocation.tryParse(entry);
        if (id != null) {
            tagList.add(TagKey.create(Registries.BLOCK, id));
        }
    }

    private JsonArray blockIdsToJson() {
        JsonArray array = new JsonArray();
        for (String id : blockIdsAsStrings()) {
            array.add(id);
        }
        return array;
    }

    private JsonArray tagIdsToJson() {
        JsonArray array = new JsonArray();
        for (String id : tagIdsAsStrings()) {
            array.add(id);
        }
        return array;
    }

    private List<String> blockIdsAsStrings() {
        List<String> ids = new ArrayList<>();
        for (Block block : blocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id != null) {
                ids.add(id.toString());
            }
        }
        return ids;
    }

    private List<String> tagIdsAsStrings() {
        List<String> ids = new ArrayList<>();
        for (TagKey<Block> tag : tags) {
            ids.add(tag.location().toString());
        }
        return ids;
    }

    private static ListTag stringList(List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }
}
