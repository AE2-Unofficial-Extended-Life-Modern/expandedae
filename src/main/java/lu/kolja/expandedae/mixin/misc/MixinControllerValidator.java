package lu.kolja.expandedae.mixin.misc;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.pathfinding.ControllerValidator;
import java.util.Collection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lu.kolja.expandedae.ExpConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ControllerValidator.class, remap = false)
public abstract class MixinControllerValidator {

    @Shadow
    private static boolean hasControllerCross(Collection<ControllerBlockEntity> controllers) {
        return false;
    }

    @ModifyConstant(
            method = "visitNode",
            constant = @Constant(intValue = 7)
    )
    private static int modifyMaxSize(int constant) {
        return ExpConfig.maxControllerSize;
    }

    @WrapOperation(
            method = "calculateState",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/me/pathfinding/ControllerValidator;hasControllerCross(Ljava/util/Collection;)Z"
            )
    )
    private static boolean ignoreControllerRules(Collection<ControllerBlockEntity> controller, Operation<Boolean> original) {
        return !ExpConfig.ignoreControllerRules && original.call(controller);
    }
}