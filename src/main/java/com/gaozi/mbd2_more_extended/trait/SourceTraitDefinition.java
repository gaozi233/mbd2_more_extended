package com.gaozi.mbd2_more_extended.trait;

import com.gaozi.mbd2_more_extended.util.ArsSourceTextures;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.gui.editor.configurator.BooleanConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.NumberConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.SelectorConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.IUIProviderTrait;
import com.lowdragmc.mbd2.common.trait.ITrait;
import com.lowdragmc.mbd2.common.trait.RecipeCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.RecipeCapabilityTraitDefinition;
import com.lowdragmc.mbd2.utils.WidgetUtils;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 魔源存储特质的定义。
 *
 * <p>两组方向互不影响：</p>
 * <ul>
 *     <li>“Ars 交互方向”只控制与 Ars 魔源网络（SourceManager / 魔源链接 / 仪式）的交互；</li>
 *     <li>“配方处理器方向”控制 MBD2 配方处理（IN 消耗 / OUT 产出 / BOTH 两者），并支持槽位名与“唯一”标记。</li>
 * </ul>
 * <p>容量/传输速率同样在这里配置，并生成机器 UI 模板。</p>
 */
@LDLRegister(name = "ars_source_storage", group = "trait", priority = -100)
public class SourceTraitDefinition extends RecipeCapabilityTraitDefinition implements IUIProviderTrait {

    @Configurable(name = "config.definition.trait.ars_source_storage.capacity")
    @NumberRange(range = {1.0D, 1.0E9D})
    private int capacity = 10000;

    @Configurable(name = "config.definition.trait.ars_source_storage.transfer_rate",
            tips = "config.definition.trait.ars_source_storage.transfer_rate.tooltip")
    @NumberRange(range = {1.0D, 1.0E9D})
    private int transferRate = 1000;

    @Configurable(name = "config.definition.trait.ars_source_storage.io",
            tips = "config.definition.trait.ars_source_storage.io.tooltip")
    private SourceIO ioMode = SourceIO.IN;

    // ------------------------------------------------------------------
    // 机器 UI
    // ------------------------------------------------------------------

    @Override
    public RecipeCapabilityTrait createTrait(MBDMachine machine) {
        return new SourceTrait(machine, this);
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture("S");
    }

    // ------------------------------------------------------------------
    // 配置面板：只保留魔源自己的设置，不显示 MBD2 的面交互配置
    // ------------------------------------------------------------------

    @Override
    public void buildConfigurator(ConfiguratorGroup group) {
        StringConfigurator nameConfigurator = new StringConfigurator(
                "config.definition.trait.name", this::getName, this::setName, "", false);

        NumberConfigurator priorityConfigurator = new NumberConfigurator(
                "config.definition.trait.priority",
                this::getPriority, number -> setPriority(number == null ? 0 : number.intValue()),
                0, false);
        priorityConfigurator.setTips("config.definition.trait.priority.tooltip");

        NumberConfigurator capacityConfigurator = new NumberConfigurator(
                "config.definition.trait.ars_source_storage.capacity",
                this::getCapacity, number -> setCapacity(number == null ? 1 : number.intValue()),
                capacity, false);
        capacityConfigurator.setTips("config.definition.trait.ars_source_storage.capacity.tooltip");

        NumberConfigurator transferConfigurator = new NumberConfigurator(
                "config.definition.trait.ars_source_storage.transfer_rate",
                this::getTransferRate, number -> setTransferRate(number == null ? 1 : number.intValue()),
                transferRate, false);
        transferConfigurator.setTips("config.definition.trait.ars_source_storage.transfer_rate.tooltip");

        SelectorConfigurator<SourceIO> ioConfigurator = new SelectorConfigurator<>(
                "config.definition.trait.ars_source_storage.io",
                this::getIoMode, this::setIoMode, SourceIO.IN, false,
                List.of(SourceIO.values()), SourceIO::name);
        ioConfigurator.setTips("config.definition.trait.ars_source_storage.io.tooltip");

        SelectorConfigurator<IO> recipeIoConfigurator = new SelectorConfigurator<>(
                "config.definition.trait.recipe_handler",
                this::getRecipeHandlerIO, this::setRecipeHandlerIO, IO.IN, false,
                List.of(IO.IN, IO.OUT, IO.BOTH), IO::getName);
        recipeIoConfigurator.setTips("config.definition.trait.recipe_handler.tooltip");

        StringConfigurator slotNamesConfigurator = new StringConfigurator(
                "config.definition.trait.slot_names",
                () -> String.join(", ", getSlotNames()),
                value -> setSlotNames(value == null || value.isBlank() ? new String[0]
                        : Arrays.stream(value.split(","))
                                .map(String::trim)
                                .filter(name -> !name.isEmpty())
                                .toArray(String[]::new)),
                "", false);
        slotNamesConfigurator.setTips("config.definition.trait.slot_names.tooltip");

        BooleanConfigurator distinctConfigurator = new BooleanConfigurator(
                "config.definition.trait.distinct",
                this::isDistinct, this::setDistinct, false, false);
        distinctConfigurator.setTips("config.definition.trait.distinct.tooltip.0",
                "config.definition.trait.distinct.tooltip.1");

        group.addConfigurators(nameConfigurator, priorityConfigurator,
                capacityConfigurator, transferConfigurator, ioConfigurator,
                recipeIoConfigurator, slotNamesConfigurator, distinctConfigurator);
    }

    // ------------------------------------------------------------------
    // 配置字段
    // ------------------------------------------------------------------

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public int getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = Math.max(1, transferRate);
    }

    public SourceIO getIoMode() {
        return ioMode;
    }

    public void setIoMode(SourceIO ioMode) {
        this.ioMode = ioMode == null ? SourceIO.IN : ioMode;
    }

    // ------------------------------------------------------------------
    // 机器 UI
    // ------------------------------------------------------------------

    @Override
    public void createTraitUITemplate(WidgetGroup group) {
        String prefix = uiPrefixName();

        WidgetGroup bar = new WidgetGroup(0, 0,
                ArsSourceTextures.BAR_WIDTH, ArsSourceTextures.BAR_HEIGHT);
        bar.setBackground(ArsSourceTextures.bar());

        ProgressWidget progress = new ProgressWidget(ProgressWidget.JEIProgress,
                ArsSourceTextures.BAR_INNER_X, ArsSourceTextures.BAR_INNER_Y,
                ArsSourceTextures.BAR_INNER_WIDTH, ArsSourceTextures.BAR_INNER_HEIGHT,
                new ProgressTexture(IGuiTexture.EMPTY,
                        ArsSourceTextures.fill(ArsSourceTextures.BAR_INNER_WIDTH,
                                ArsSourceTextures.BAR_INNER_HEIGHT)));
        progress.setId(prefix);
        bar.addWidget(progress);

        TextTextureWidget text = new TextTextureWidget(
                ArsSourceTextures.BAR_INNER_X, 5,
                ArsSourceTextures.BAR_INNER_WIDTH, 8)
                .setText("0/0")
                .textureStyle(texture -> texture.setDropShadow(true));
        text.setId(prefix + "_text");
        bar.addWidget(text);

        group.addWidget(bar);
    }

    @Override
    public void initTraitUI(ITrait trait, WidgetGroup group) {
        if (!(trait instanceof SourceTrait sourceTrait)) {
            return;
        }
        String prefix = uiPrefixName();

        WidgetUtils.widgetByIdForEach(group, "^%s$".formatted(prefix), ProgressWidget.class, progress -> {
            progress.setProgressSupplier(() ->
                    (double) sourceTrait.getSource() / Math.max(1, sourceTrait.getMaxSource()));
            progress.setDynamicHoverTips(value -> Component.translatable(
                    "config.definition.trait.ars_source_storage.ui_hover",
                    (int) Math.round(value * sourceTrait.getMaxSource()),
                    sourceTrait.getMaxSource()).getString());
        });

        WidgetUtils.widgetByIdForEach(group, "^%s_text$".formatted(prefix), TextTextureWidget.class, text ->
                text.setText(() -> Component.literal(
                        sourceTrait.getSource() + "/" + sourceTrait.getMaxSource())));
    }

    /** 魔源交互方向：不区分面。BOTH 类似魔源罐：可接收也可被抽取，但不主动推送。 */
    public enum SourceIO {
        IN,
        OUT,
        BOTH
    }
}
