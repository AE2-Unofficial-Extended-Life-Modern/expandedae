package lu.kolja.expandedae.xmod.advancedae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingCPU;
import lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorAdvCraftingCPULogic;
import lu.kolja.expandedae.mixin.compat.advancedae.AAEAccessorExecutingCraftingJob;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;

public class AdvancedAE {
    public AdvancedAE() {
        // To rework later on, they never even worked in the first place so eh
        /*
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEItems.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");

        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.SMALL_ADV_PATTERN_PROVIDER, 1, "group.adv_pattern_provider.name");
        Upgrades.add(AUTO_COMPLETE_CARD, AAEBlocks.ADV_PATTERN_PROVIDER, 1, "group.advanced_pattern_provider.name");
        */
    }

    public static void handleCpu(ICraftingCPU cpu, IPatternDetails details) {
        if (cpu instanceof AdvCraftingCPU advCpu) {
            var task = ((AAEAccessorExecutingCraftingJob) ((AAEAccessorAdvCraftingCPULogic) advCpu.craftingLogic).getJob()).getTasks().get(details);
            if (task != null && task.getValue() <= 1) {
                ((AAEAccessorAdvCraftingCPULogic) advCpu.craftingLogic).eae$finishJob(true);
                advCpu.updateOutput(null);
            }
        }
    }
}
