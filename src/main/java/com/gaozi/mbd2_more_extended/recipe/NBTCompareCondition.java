package com.gaozi.mbd2_more_extended.recipe;

import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.NumberConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SelectorConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.mbd2.api.machine.IMachine;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.api.recipe.RecipeCondition;
import com.lowdragmc.mbd2.api.recipe.RecipeLogic;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import java.util.List;

/**
 * 从机器的 customData 中读取一个 NBT 键，与指定值按运算符比较。
 *
 * <p>条件本身不持有机器对象，只保存 key / operator / value 三个可序列化字段，
 * 因此可以安全地存盘、重载并同步到客户端。</p>
 *
 * <p>示例（JSON）：</p>
 * <pre>{@code
 * {
 *   "type": "nbt_compare",
 *   "data": {
 *     "key": "magnetizer_level",
 *     "operator": ">=",
 *     "value": 36.0
 *   }
 * }
 * }</pre>
 */
public class NBTCompareCondition extends RecipeCondition {

    /** 注册到 MBD2 {@code RECIPE_CONDITIONS} 的唯一标识，不能与原版条件重名。 */
    public static final String TYPE = "nbt_compare";

    private String key = "";
    private Operator operator = Operator.GE;
    private double value = 0.0D;

    /** 供 MBD2 反序列化时反射创建使用，必须保留公开无参构造。 */
    public NBTCompareCondition() {
    }

    public NBTCompareCondition(String key, String operator, double value) {
        this(key, Operator.fromSymbol(operator), value);
    }

    public NBTCompareCondition(String key, Operator operator, double value) {
        this.key = key == null ? "" : key;
        this.operator = operator == null ? Operator.GE : operator;
        this.value = value;
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
        return Component.translatable("recipe.condition.nbt_compare.tooltip",
                key, operator.getSymbol(), formatValue());
    }

    @Override
    public IGuiTexture getIcon() {
        // 不额外提供贴图，直接用文字图标，和 MBD2 自带的 machine_custom_data 条件一致。
        return new TextTexture("N");
    }

    /**
     * 配方编辑器里选中该条件后显示的配置面板：
     * NBT 键（文本框）、运算符（下拉框）、比较值（数字框）。
     */
    @Override
    public void buildConfigurator(ConfiguratorGroup group) {
        super.buildConfigurator(group);

        StringConfigurator keyConfigurator = new StringConfigurator(
                getTranslationKey() + ".key", this::getKey, this::setKey, "", false);
        keyConfigurator.setTips("config.recipe.condition.nbt_compare.key.tooltip");

        SelectorConfigurator<Operator> operatorConfigurator = new SelectorConfigurator<>(
                getTranslationKey() + ".operator",
                this::getOperator, this::setOperator, Operator.GE, false,
                List.of(Operator.values()), Operator::getSymbol);
        operatorConfigurator.setTips("config.recipe.condition.nbt_compare.operator.tooltip");

        NumberConfigurator valueConfigurator = new NumberConfigurator(
                getTranslationKey() + ".value",
                () -> getValue(), number -> setValue(number == null ? 0.0D : number.doubleValue()),
                0.0D, true);
        // NumberConfigurator 默认把 min/max 都设成当前值（未设置时就是 0.0 ~ 0.0），
        // 必须显式给出范围，否则编辑器里数值只能在 0 和 0 之间调。
        valueConfigurator.setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        valueConfigurator.setTips("config.recipe.condition.nbt_compare.value.tooltip");

        group.addConfigurators(keyConfigurator, operatorConfigurator, valueConfigurator);
    }

    @Override
    public boolean test(MBDRecipe recipe, RecipeLogic recipeLogic) {
        if (key.isEmpty() || recipeLogic == null) {
            return false;
        }
        IMachine machine = recipeLogic.getMachine();
        if (!(machine instanceof MBDMachine mbdMachine)) {
            return false;
        }
        CompoundTag customData = mbdMachine.getCustomData();
        if (customData == null || !customData.contains(key)) {
            return false;
        }
        Double actual = asDouble(customData.get(key));
        // 键不存在或不是数字时条件不通过，而不是抛异常打断配方检查。
        return actual != null && operator.test(actual, value);
    }

    // ------------------------------------------------------------------
    // 序列化：JSON（配方 JSON / KubeJS）
    // ------------------------------------------------------------------

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.addProperty("key", key);
        json.addProperty("operator", operator.getSymbol());
        json.addProperty("value", value);
        return json;
    }

    @Override
    public RecipeCondition deserialize(JsonObject json) {
        super.deserialize(json);
        key = GsonHelper.getAsString(json, "key", "");
        operator = Operator.fromSymbol(GsonHelper.getAsString(json, "operator", Operator.GE.getSymbol()));
        value = GsonHelper.getAsDouble(json, "value", 0.0D);
        return this;
    }

    // ------------------------------------------------------------------
    // 序列化：网络（客户端-服务端同步）
    // ------------------------------------------------------------------

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeUtf(key);
        buf.writeUtf(operator.getSymbol());
        buf.writeDouble(value);
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        key = buf.readUtf();
        operator = Operator.fromSymbol(buf.readUtf());
        value = buf.readDouble();
        return this;
    }

    // ------------------------------------------------------------------
    // 序列化：NBT（存档 / 重载）
    // ------------------------------------------------------------------

    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = super.toNBT();
        tag.putString("key", key);
        tag.putString("operator", operator.getSymbol());
        tag.putDouble("value", value);
        return tag;
    }

    @Override
    public RecipeCondition fromNBT(CompoundTag tag) {
        super.fromNBT(tag);
        key = tag.getString("key");
        operator = Operator.fromSymbol(tag.getString("operator"));
        value = tag.getDouble("value");
        return this;
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key == null ? "" : key;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator == null ? Operator.GE : operator;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private String formatValue() {
        if (!Double.isInfinite(value) && !Double.isNaN(value)
                && value == Math.rint(value) && Math.abs(value) < 9.007_199_254_740_992E15D) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static Double asDouble(Tag tag) {
        if (tag instanceof NumericTag numeric) {
            return numeric.getAsDouble();
        }
        // 允许脚本往 customData 里写数字字符串。
        if (tag instanceof StringTag stringTag) {
            try {
                return Double.parseDouble(stringTag.getAsString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /** 支持的比较运算符。 */
    public enum Operator {
        GE(">="),
        GT(">"),
        LE("<="),
        LT("<"),
        EQ("=="),
        NE("!=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean test(double left, double right) {
            return switch (this) {
                case GE -> left >= right;
                case GT -> left > right;
                case LE -> left <= right;
                case LT -> left < right;
                case EQ -> left == right;
                case NE -> left != right;
            };
        }

        /**
         * 按符号解析运算符，同时兼容枚举名（如 {@code GE}、{@code ne}）。
         * 无法识别时回退到 {@link #GE}，保证旧数据不会让条件直接失效。
         */
        public static Operator fromSymbol(String symbol) {
            if (symbol != null) {
                String value = symbol.trim();
                for (Operator operator : values()) {
                    if (operator.symbol.equals(value)) {
                        return operator;
                    }
                }
                for (Operator operator : values()) {
                    if (operator.name().equalsIgnoreCase(value)) {
                        return operator;
                    }
                }
            }
            return GE;
        }
    }
}
