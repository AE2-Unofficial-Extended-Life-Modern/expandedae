package lu.kolja.expandedae.mixin.compat.appflux;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;
import lu.kolja.expandedae.definition.ExpSettings;
import lu.kolja.expandedae.enums.BlockingMode;
import lu.kolja.expandedae.helper.misc.KeybindUtil;
import lu.kolja.expandedae.helper.pattern.IPatternProvider;
import lu.kolja.expandedae.helper.pattern.IUpgradableMenu;
import lu.kolja.mixinloadconditions.LoadCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.Arrays;

@LoadCondition(loadIf = "appflux")
@Mixin(value = PatternProviderMenu.class, remap = false)
public abstract class AppFluxMixinPatternProviderMenu extends AEBaseMenu implements IUpgradableMenu, IPatternProvider {
    @Unique
    private static final int BASE_FACTOR = 2;

    @Final
    @Shadow(remap = false)
    protected PatternProviderLogic logic;

    @Unique
    @GuiSync(8)
    public BlockingMode eae$blockingMode = BlockingMode.DEFAULT;

    public AppFluxMixinPatternProviderMenu(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void initToolbox(MenuType<?> menuType, int id, Inventory playerInventory, PatternProviderLogicHost host, CallbackInfo ci) {
        this.registerClientAction("modifyPatterns", Boolean.class, this::expandedae$modifyPatterns);
    }

    @Unique
    @Override
    public void expandedae$modifyPatterns(boolean rightClick) {
        if (this.isClientSide()) this.sendClientAction("modifyPatterns", rightClick);
        else {
            for (var slot : this.getSlots(SlotSemantics.ENCODED_PATTERN)) {
                var stack = slot.getItem();
                var detail = PatternDetailsHelper.decodePattern(stack, this.getPlayer().level());
                if (detail instanceof AEProcessingPattern processingPattern) {
                    var input = processingPattern.getSparseInputs().toArray(GenericStack[]::new);
                    var output = processingPattern.getOutputs().toArray(GenericStack[]::new);
                    if (expandedae$checkModify(input, expandedae$getScale(), rightClick) && expandedae$checkModify(output, expandedae$getScale(), rightClick)) {
                        var mulInput = new GenericStack[input.length];
                        var mulOutput = new GenericStack[output.length];
                        expandedae$modifyStacks(input, mulInput, expandedae$getScale(), rightClick);
                        expandedae$modifyStacks(output, mulOutput, expandedae$getScale(), rightClick);
                        var newPattern = PatternDetailsHelper.encodeProcessingPattern(
                                Arrays.stream(mulInput).toList(),
                                Arrays.stream(mulOutput).toList()
                        );
                        slot.set(newPattern);
                    }
                }
            }
        }
    }
    @Unique
    private int expandedae$getScale() {
        return BASE_FACTOR * KeybindUtil.shiftMultiplier() * KeybindUtil.ctrlMultiplier();
    }
    @Unique
    private boolean expandedae$checkModify(GenericStack[] stacks, int scale, boolean division) {
        if (division) {
            for (var stack : stacks) {
                if (stack != null) {
                    if (stack.amount() % scale != 0) {
                        return false;
                    }
                }
            }
        } else {
            for (var stack : stacks) {
                if (stack != null) {
                    long upper = 999999L * stack.what().getAmountPerUnit();
                    if (stack.amount() * scale > upper) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    @Unique
    private void expandedae$modifyStacks(GenericStack[] stacks, GenericStack[] des, int scale, boolean division) {
        for (int i = 0; i < stacks.length; i ++) {
            if (stacks[i] != null) {
                long amt = division ? stacks[i].amount() / scale : stacks[i].amount() * scale;
                des[i] = new GenericStack(stacks[i].what(), amt);
            }
        }
    }

    @Inject(method = "broadcastChanges",
            at = @At(value = "INVOKE",
                    target = "Lappeng/helpers/patternprovider/PatternProviderLogic;getUnlockStack()Lappeng/api/stacks/GenericStack;",
                    remap = false))
    private void broadcastChanges(CallbackInfo ci) {
        eae$blockingMode = logic.getConfigManager().getSetting(ExpSettings.BLOCKING_MODE);
    }

    @Override
    public BlockingMode expandedae$getBlockingMode() {
        return eae$blockingMode;
    }
}
