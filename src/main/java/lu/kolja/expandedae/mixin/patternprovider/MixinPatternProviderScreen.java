package lu.kolja.expandedae.mixin.patternprovider;

import appeng.api.upgrades.Upgrades;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import lu.kolja.expandedae.client.gui.widgets.ExpActionButton;
import lu.kolja.expandedae.client.gui.widgets.ExpActionItems;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.pattern.IPatternProvider;
import lu.kolja.expandedae.helper.pattern.ISmartBlocking;
import lu.kolja.expandedae.helper.pattern.IUpgradableMenu;
import lu.kolja.mixinloadconditions.LoadCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

@LoadCondition(ignoreIf = "appflux")
@Mixin(value = PatternProviderScreen.class, remap = false)
public abstract class MixinPatternProviderScreen<C extends PatternProviderMenu> extends AEBaseScreen<C> implements ISmartBlocking {

    @Unique
    private ServerSettingToggleButton<BlockingMode> eae$blockingMode;

    private MixinPatternProviderScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void init(PatternProviderMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        this.widgets.add("upgrades", new UpgradesPanel(
                menu.getSlots(SlotSemantics.UPGRADE),
                this::eae_$getCompatibleUpgrades));
        ExpActionButton modifyPatterns = new ExpActionButton(ExpActionItems.MODIFY_PATTERNS, 16, 16,act -> ((IPatternProvider) menu).expandedae$modifyPatterns(
                ((AEBaseScreen<?>) Minecraft.getInstance().screen).isHandlingRightClick()
        ));
        this.addToLeftToolbar(modifyPatterns);
        if (((IUpgradableMenu) menu).expandedae$getToolbox().isPresent()) {
            this.widgets.add("toolbox", new ToolboxPanel(style, ((IUpgradableMenu) menu).expandedae$getToolbox().getName()));
        }
        this.eae$blockingMode = new ServerSettingToggleButton<>(
                ExpSettings.BLOCKING_MODE,
                BlockingMode.DEFAULT
        );
        this.addToLeftToolbar(this.eae$blockingMode);
    }

    @Unique
    private List<Component> eae_$getCompatibleUpgrades() {
        //((IPatternProvider) menu).expandedae$hideBlocking();
        var list = new ArrayList<Component>();
        list.add(GuiText.CompatibleUpgrades.text());
        list.addAll(Upgrades.getTooltipLinesForMachine(((IUpgradableMenu) menu).expandedae$getUpgrades().getUpgradableItem()));
        return list;
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