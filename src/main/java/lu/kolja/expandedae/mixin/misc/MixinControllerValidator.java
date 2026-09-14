package lu.kolja.expandedae.mixin.misc;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.pathfinding.ControllerValidator;
import lu.kolja.expandedae.ExpConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(value = ControllerValidator.class, remap = false)
public abstract class MixinControllerValidator {
    @Shadow
    private static boolean hasControllerCross(Collection<ControllerBlockEntity> controllers) {
        return false;
    }

    // Vanilla AE2
    @Group(name = "controllerSize", min = 1, max = 1)
    @ModifyConstant(
            method = "visitNode",
            constant = @Constant(intValue = 7),
            require = 0
    )
    private int modifyControllerSize(int constant) {
        return ExpConfig.maxControllerSize;
    }

    // AE2UELM
    @Group(name = "controllerSize", min = 1, max = 1)
    @ModifyConstant(
            method = "accept",
            constant = @Constant(intValue = 7),
            require = 0
    )
    private int modifyControllerSizeAlt(int constant) {
        return ExpConfig.maxControllerSize;
    }

    @Redirect(
            method = "calculateState",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/me/pathfinding/ControllerValidator;hasControllerCross(Ljava/util/Collection;)Z"
            )
    )
    private static boolean modifyControllerSize2(Collection<ControllerBlockEntity> controller) {
        return !ExpConfig.ignoreControllerRules && hasControllerCross(controller);
    }
}
