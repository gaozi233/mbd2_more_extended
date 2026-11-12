package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.Configurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.NumberConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTraitDefinition;

import java.util.List;

/**
 * 可突破普通物品 64 堆叠上限的物品槽特质定义。
 */
@LDLRegister(name = "overstack_item_slot", group = "trait", priority = -100)
public class OverstackItemSlotTraitDefinition extends ItemSlotCapabilityTraitDefinition {

    @Configurable(
            name = "config.definition.trait.overstack_item_slot.allow_overstack",
            tips = "config.definition.trait.overstack_item_slot.allow_overstack.tooltip"
    )
    private boolean allowOverstack = true;

    @Configurable(
            name = "config.definition.trait.overstack_item_slot.mode",
            tips = "config.definition.trait.overstack_item_slot.mode.tooltip"
    )
    private OverstackMode mode = OverstackMode.EXACT;

    @Override
    public ItemSlotCapabilityTrait createTrait(MBDMachine machine) {
        return new OverstackItemSlotTrait(machine, this);
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture("∞");
    }

    /**
     * 原版物品槽的 slotLimit 被限制在 1..64；这里替换成允许更高数值的配置项。
     */
    @Override
    public void buildConfigurator(ConfiguratorGroup group) {
        super.buildConfigurator(group);

        List<Configurator> configurators = group.getConfigurators();
        int slotLimitIndex = -1;
        for (int i = 0; i < configurators.size(); i++) {
            Configurator configurator = configurators.get(i);
            if (configurator.getName() != null
                    && configurator.getName().endsWith("item_slot.slot_limit")) {
                slotLimitIndex = i;
                break;
            }
        }
        if (slotLimitIndex >= 0) {
            group.removeConfigurator(configurators.get(slotLimitIndex));
        }

        NumberConfigurator slotLimitConfigurator = new NumberConfigurator(
                "config.definition.trait.overstack_item_slot.slot_limit",
                this::getSlotLimit,
                number -> setSlotLimit(number == null ? getSlotLimit() : number.intValue()),
                getSlotLimit(),
                false
        );
        slotLimitConfigurator.setRange(1, Integer.MAX_VALUE);
        slotLimitConfigurator.setTips("config.definition.trait.overstack_item_slot.slot_limit.tooltip");
        if (slotLimitIndex >= 0) {
            group.addConfigurator(slotLimitIndex, slotLimitConfigurator);
        } else {
            group.addConfigurators(slotLimitConfigurator);
        }
    }

    public boolean isAllowOverstack() {
        return allowOverstack;
    }

    public OverstackMode getMode() {
        return mode;
    }

    public enum OverstackMode {
        /**
         * n 作为 64 堆叠物品的上限，其他物品按 x * n / 64 缩放。
         */
        EXACT,
        /**
         * n 作为倍数，所有物品上限为 x * n。
         */
        MULTIPLIER
    }
}
