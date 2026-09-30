package lu.kolja.expandedae.mixin.cpu;

import appeng.client.gui.widgets.CPUSelectionList;
import appeng.menu.me.crafting.CraftingStatusMenu;
import lu.kolja.expandedae.helper.misc.NumberUtil;
import lu.kolja.mixinloadconditions.LoadCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@LoadCondition(loadIf = "ae2", matchVersion = "(,19.2.18)")
@Mixin(value = CPUSelectionList.class, remap = false)
public class MixinCPUSelectionList {

    /**
     * @author Kolja
     * @reason Truncates CPU Crafting Storages with Formatting
     */
    @Overwrite(remap = false)
    private String formatStorage(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return NumberUtil.formatNum(cpu.storage());
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;")
    )
    private String formatProcessorCount(int coProcessorCount) {
        return NumberUtil.formatNum(coProcessorCount);
    }
}