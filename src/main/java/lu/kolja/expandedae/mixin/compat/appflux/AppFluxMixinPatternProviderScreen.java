package lu.kolja.expandedae.mixin.compat.appflux;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.menu.implementations.PatternProviderMenu;
import lu.kolja.expandedae.client.gui.widgets.ExpActionButton;
import lu.kolja.expandedae.client.gui.widgets.ExpActionItems;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.pattern.IPatternProvider;
import lu.kolja.expandedae.helper.pattern.ISmartBlocking;
import lu.kolja.mixinloadconditions.LoadCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

@LoadCondition(loadIf = "appflux")
@Mixin(value = PatternProviderScreen.class, remap = false)
public abstract class AppFluxMixinPatternProviderScreen<C extends PatternProviderMenu> extends AEBaseScreen<C> implements ISmartBlocking {

    @Unique
    private ServerSettingToggleButton<BlockingMode> eae$blockingMode;

    private AppFluxMixinPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "<init>(Lappeng/menu/implementations/PatternProviderMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;Lappeng/client/gui/style/ScreenStyle;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void init(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        ExpActionButton modifyPatterns = new ExpActionButton(ExpActionItems.MODIFY_PATTERNS, 16, 16, act -> ((IPatternProvider) menu).expandedae$modifyPatterns(
                ((AEBaseScreen<?>) Minecraft.getInstance().screen).isHandlingRightClick()
        ));
        this.addToLeftToolbar(modifyPatterns);
        this.eae$blockingMode = new ServerSettingToggleButton<>(
                ExpSettings.BLOCKING_MODE,
                BlockingMode.DEFAULT
        );
        this.addToLeftToolbar(this.eae$blockingMode);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"), remap = false)
    private void updateBeforeRender(CallbackInfo ci) {
        this.eae$blockingMode.set(((IPatternProvider) menu).expandedae$getBlockingMode());
    }

    @Override
    public void expandedae$resetBlocking() {
        this.eae$blockingMode.set(BlockingMode.DEFAULT);
    }

    @Override
    public BlockingMode expandedae$getBlockingMode() {
        return eae$blockingMode.getCurrentValue();
    }

    @Override
    public void expandedae$setBlocking(BlockingMode blockingMode) {
        eae$blockingMode.set(blockingMode);
    }
    /*
    @Override
    public void expandedae$hideBlocking() {
        eae$blockingMode.setVisibility(false);
    }
    */

    @Override
    public void expandedae$showBlocking() {
        eae$blockingMode.setVisibility(true);
    }
}
