package lu.kolja.expandedae.xmod.extendedae;

import appeng.api.upgrades.Upgrades;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.common.items.ItemMEPackingTape;
import lu.kolja.expandedae.definition.ExpBlocks;
import lu.kolja.expandedae.definition.ExpItems;

public class ExtendedAE {
    public ExtendedAE() {
        /*
        ItemMEPackingTape.registerPackableDevice(ExpBlocks.EXP_PATTERN_PROVIDER.id());
        ItemMEPackingTape.registerPackableDevice(ExpItems.EXP_PATTERN_PROVIDER_PART.id());
        */
        Upgrades.add(ExpItems.AUTO_COMPLETE_CARD, EAESingletons.EX_PATTERN_PROVIDER, 1, "group.ex_pattern_provider.name");
        Upgrades.add(ExpItems.AUTO_COMPLETE_CARD, EAESingletons.EX_PATTERN_PROVIDER_PART, 1, "group.ex_pattern_provider.name");
        /*
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EAESingletons.EX_INTERFACE,1, "group.ex_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EAESingletons.EX_INTERFACE_PART,1, "group.ex_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EAESingletons.OVERSIZE_INTERFACE,1, "group.oversize_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EAESingletons.OVERSIZE_INTERFACE_PART,1, "group.oversize_interface.name");

        Upgrades.add(ExpItems.STICKY_CARD, EAESingletons.TAG_STORAGE_BUS,1, "group.tag_storage_bus.name");
        Upgrades.add(ExpItems.STICKY_CARD, EAESingletons.MOD_STORAGE_BUS,1, "group.mod_storage_bus.name");
        Upgrades.add(ExpItems.STICKY_CARD, EAESingletons.PRECISE_STORAGE_BUS,1, "group.precise_storage_bus.name");
         */
    }
}
