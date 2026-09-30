package lu.kolja.expandedae.mixin.compat.advancedae;

import appeng.api.crafting.IPatternDetails;
import lu.kolja.mixinloadconditions.LoadCondition;
import net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@LoadCondition(loadIf = "advanced_ae")
@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface AAEAccessorExecutingCraftingJob {
    @Accessor("tasks")
    Map<IPatternDetails, AAEAccessorTaskProgress> getTasks();

    @LoadCondition(loadIf = "advanced_ae")
    @Mixin(targets = "net.pedroksl.advanced_ae.common.logic.ExecutingCraftingJob$TaskProgress", remap = false)
    interface AAEAccessorTaskProgress {
        @Accessor("value")
        long getValue();
    }
}
