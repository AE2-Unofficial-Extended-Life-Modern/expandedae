package lu.kolja.expandedae.xmod.extendedae;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.ItemDefinition;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.glodblock.github.extendedae.config.EPPConfig;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.definition.ExpBlocks;
import lu.kolja.expandedae.definition.ExpItems;
import lu.kolja.expandedae.item.upgrade.Ext2ExpUpgradeItem;
import lu.kolja.expandedae.item.upgrade.Ext2GigaUpgradeItem;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Expandedae.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExtendedAE {
    public static ItemDefinition<Ext2ExpUpgradeItem> EXT_PATTERN_PROVIDER_UPGRADE;
    public static ItemDefinition<Ext2GigaUpgradeItem> EXT2G_PATTERN_PROVIDER_UPGRADE;

    public static void initItems() {
        EXT_PATTERN_PROVIDER_UPGRADE = ExpItems.item("Extended Pattern Provider Upgrade", "ext_pattern_provider_upgrade", Ext2ExpUpgradeItem::new);
        EXT2G_PATTERN_PROVIDER_UPGRADE = ExpItems.item("Extended2Giga Pattern Provider Upgrade", "ext2g_pattern_provider_upgrade", Ext2GigaUpgradeItem::new);
    }

    public ExtendedAE() {
        Upgrades.add(ExpItems.AUTO_COMPLETE_CARD, EPPItemAndBlock.EX_PATTERN_PROVIDER, 1, "group.ex_pattern_provider.name");
        Upgrades.add(ExpItems.AUTO_COMPLETE_CARD, EPPItemAndBlock.EX_PATTERN_PROVIDER_PART, 1, "group.ex_pattern_provider.name");
        /*
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EPPItemAndBlock.EX_INTERFACE,1, "group.ex_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EPPItemAndBlock.EX_INTERFACE_PART,1, "group.ex_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EPPItemAndBlock.OVERSIZE_INTERFACE,1, "group.oversize_interface.name");
        Upgrades.add(ExpItems.ADVANCED_BLOCKING_CARD, EPPItemAndBlock.OVERSIZE_INTERFACE_PART,1, "group.oversize_interface.name");

        Upgrades.add(ExpItems.STICKY_CARD, EPPItemAndBlock.TAG_STORAGE_BUS,1, "group.tag_storage_bus.name");
        Upgrades.add(ExpItems.STICKY_CARD, EPPItemAndBlock.MOD_STORAGE_BUS,1, "group.mod_storage_bus.name");
        Upgrades.add(ExpItems.STICKY_CARD, EPPItemAndBlock.PRECISE_STORAGE_BUS,1, "group.precise_storage_bus.name");
        */
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onConfigLoad(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals("expatternprovider")) {
            return;
        }

        EPPConfig.tapeWhitelist.add(ExpBlocks.EXP_PATTERN_PROVIDER.id());
        EPPConfig.tapeWhitelist.add(ExpItems.EXP_PATTERN_PROVIDER_PART.id());
        EPPConfig.tapeWhitelist.add(ExpBlocks.EXP_IO_PORT.id());
    }
}