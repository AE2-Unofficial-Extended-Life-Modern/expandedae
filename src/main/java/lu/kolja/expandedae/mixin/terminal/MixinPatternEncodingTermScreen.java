package lu.kolja.expandedae.mixin.terminal;

import appeng.api.config.ActionItems;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.Tooltips;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.llamalad7.mixinextras.sugar.Local;
import lu.kolja.expandedae.api.misc.KeybindUtil;
import lu.kolja.expandedae.api.patternprovider.IPatternEncodingTerminalMenu;
import lu.kolja.expandedae.definition.ExpLang;
import lu.kolja.expandedae.screen.SetProcessingPatternNameScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = PatternEncodingTermScreen.class, remap = false)
public abstract class MixinPatternEncodingTermScreen<C extends PatternEncodingTermMenu> extends MEStorageScreen<C> {

    public MixinPatternEncodingTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "lambda$new$1",
            at = @At("TAIL")
    )
    private static void encode(PatternEncodingTermMenu menu, ActionItems act, CallbackInfo ci) {
        ((IPatternEncodingTerminalMenu) menu).eae$MovePattern(Screen.hasShiftDown());
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "NEW",
                    target = "appeng/client/gui/me/items/SetProcessingPatternAmountScreen"
            ),
            cancellable = true,
            require = 0,
            remap = true
    )
    private void mouseClicked(double xCoord, double yCoord, int btn, CallbackInfoReturnable<Boolean> cir, @Local(name = "slot") Slot slot, @Local(name = "currentStack") GenericStack currentStack) {
        if (!KeybindUtil.isCtrlDown() || !this.minecraft.options.keyPickItem.matchesMouse(btn)) {
            return;
        }

        if (eae$tryOpenNameScreen(slot)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "handlePickBlock",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void eae$handlePickBlockCompat(Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (!KeybindUtil.isCtrlDown()) {
            return;
        }

        if (eae$tryOpenNameScreen(slot)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean eae$tryOpenNameScreen(@Nullable Slot slot) {
        if (!menu.canModifyAmountForSlot(slot)) {
            return false;
        }

        var currentStack = GenericStack.fromItemStack(slot.getItem());
        if (currentStack == null
                || !currentStack.what().getType().equals(AEKeyType.items())) {
            return false;
        }

        var screen = new SetProcessingPatternNameScreen<>(
                (PatternEncodingTermScreen<?>) (Object) this,
                currentStack,
                newStack -> NetworkHandler.instance().sendToServer(
                        new InventoryActionPacket(
                                InventoryAction.SET_FILTER,
                                slot.index,
                                GenericStack.wrapInItemStack(newStack)
                        )
                )
        );

        switchToScreen(screen);
        return true;
    }

    @Inject(
            method = "renderTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z",
                    ordinal = 1
            ),
            remap = true
    )
    private void renderTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci, @Local(name = "itemTooltip") ArrayList<Component> itemTooltip, @Local(name = "unwrapped") GenericStack unwrapped) {
        if (unwrapped.what().getType().equals(AEKeyType.items())) {
            itemTooltip.add(ExpLang.MODIFY_NAME.text(ExpLang.CTRL_MIDDLE_CLICK.text()).withStyle(Tooltips.MUTED_COLOR));
        }
    }
}
