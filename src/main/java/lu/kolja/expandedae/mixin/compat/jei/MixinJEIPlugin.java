package lu.kolja.expandedae.mixin.compat.jei;

import lu.kolja.expandedae.definition.ExpMenus;
import lu.kolja.expandedae.terminal.ExpEncodingTerminalMenu;
import lu.kolja.expandedae.terminal.wtlib.ExpWETMenu;
import lu.kolja.mixinloadconditions.LoadCondition;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tamaized.ae2jeiintegration.integration.modules.jei.JEIPlugin;
import tamaized.ae2jeiintegration.integration.modules.jei.transfer.EncodePatternTransferHandler;

@LoadCondition(loadIf = "ae2jeiintegration")
@Mixin(value = JEIPlugin.class, remap = false)
public class MixinJEIPlugin {

    @Inject(
            method = "registerRecipeTransferHandlers",
            at = @At("RETURN")
    )
    private void registerRecipeTransferHandlers(IRecipeTransferRegistration registration, CallbackInfo ci) {
        var jeiHelpers = registration.getJeiHelpers();
        var ingredientVisibility = jeiHelpers.getIngredientVisibility();
        var transferHelper = registration.getTransferHelper();
        registration.addUniversalRecipeTransferHandler(
                new EncodePatternTransferHandler<>(
                        ExpMenus.EXP_ENCODING_TERMINAL.get(),
                        ExpEncodingTerminalMenu.class,
                        transferHelper,
                        ingredientVisibility
                )
        );
        registration.addUniversalRecipeTransferHandler(
                new EncodePatternTransferHandler<>(
                        ExpWETMenu.TYPE,
                        ExpWETMenu.class,
                        transferHelper,
                        ingredientVisibility
                )
        );
    }
}
