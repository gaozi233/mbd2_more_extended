package com.gaozi.mbd2_more_extended.trait;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;

/**
 * 使用 {@link OverstackItemStackTransfer} 的物品槽特质。
 */
public class OverstackItemSlotTrait extends ItemSlotCapabilityTrait {

    public OverstackItemSlotTrait(MBDMachine machine, OverstackItemSlotTraitDefinition definition) {
        super(machine, definition);
    }

    @Override
    protected ItemStackTransfer createStorage() {
        OverstackItemSlotTraitDefinition definition = (OverstackItemSlotTraitDefinition) getDefinition();
        OverstackItemStackTransfer storage = new OverstackItemStackTransfer(definition);
        if (definition.getItemFilterSettings().isEnable()) {
            storage.setFilter(definition.getItemFilterSettings()::test);
        }
        return storage;
    }
}
