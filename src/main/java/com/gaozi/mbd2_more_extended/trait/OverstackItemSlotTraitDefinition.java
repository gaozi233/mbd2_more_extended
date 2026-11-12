package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTraitDefinition;

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

    @Override
    public ItemSlotCapabilityTrait createTrait(MBDMachine machine) {
        return new OverstackItemSlotTrait(machine, this);
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture("∞");
    }

    public boolean isAllowOverstack() {
        return allowOverstack;
    }
}
