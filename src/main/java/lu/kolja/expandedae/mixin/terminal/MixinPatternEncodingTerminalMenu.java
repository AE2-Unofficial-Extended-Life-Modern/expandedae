package lu.kolja.expandedae.mixin.terminal;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.definitions.AEItems;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigInventory;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import lu.kolja.expandedae.api.patternprovider.IPatternEncodingTerminalMenu;
import lu.kolja.expandedae.definition.ExpItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public abstract class MixinPatternEncodingTerminalMenu extends MEStorageMenu implements IMenuCraftingPacket, IPatternEncodingTerminalMenu {
    @Unique
    private final String ACTION_MOVE_PATTERN = "movePattern";

    @Final
    @Shadow
    @Mutable
    private RestrictedInputSlot encodedPatternSlot;

    @Shadow
    @Final
    private RestrictedInputSlot blankPatternSlot;

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedInputsInv;

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedOutputsInv;

    protected MixinPatternEncodingTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Inject(method = "encode", at = @At("RETURN"))
    private void encode(CallbackInfo ci) {
        final IGridNode node = this.getNetworkNode();
        AtomicReference<Player> player = new AtomicReference<>();
        this.getActionSource().player().ifPresent(player::set);
        if (encodedPatternSlot.getItem() != ItemStack.EMPTY) {

            var terminalItem = expandedae$getTerminalItem(player.get());
            if (terminalItem == null) return;
            if (terminalItem.getItem() instanceof IUpgradeableItem item) {
                IUpgradeInventory inventory = item.getUpgrades(terminalItem);
                if (!inventory.isInstalled(ExpItems.PATTERN_REFILLER_CARD)) return;
            }

            var blankPatternSlotCount = blankPatternSlot.getItem().getCount();
            if (node == null) return;
            int changed = (int) Objects.requireNonNull(node).getGrid().getStorageService().getInventory().extract(
                    AEItemKey.of(AEItems.BLANK_PATTERN),
                    64 - blankPatternSlotCount,
                    Actionable.MODULATE,
                    this.getActionSource()
            );
            blankPatternSlot.set(new ItemStack(AEItems.BLANK_PATTERN, blankPatternSlotCount + changed));
            blankPatternSlot.setChanged();
        }
    }

    @Unique
    @Nullable
    private ItemStack expandedae$getTerminalItem(Player player) {
        var locator = WUTHandler.findTerminal(player, "pattern_encoding");
        if (locator == null) return null;
        return WUTHandler.getItemStackFromLocator(player, locator);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"),
            remap = false)
    private void initHooks(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host,
                           boolean bindInventory, CallbackInfo ci) {
        registerClientAction("modifyPattern", Integer.class, this::eae$ModifyPattern);
        registerClientAction(ACTION_MOVE_PATTERN, Boolean.class, this::eae$MovePattern);
    }

    @Unique
    public void eae$MovePattern(Boolean data) {
        if (isClientSide()) {
            sendClientAction(ACTION_MOVE_PATTERN, data);
        } else {
            if (!data) return;
            var player = this.getPlayer();
            // Need to do this check first because #addItem ignores that there are no free slots if the player is in creative mode
            if (player.getInventory().getFreeSlot() > 0) {
                player.addItem(encodedPatternSlot.getItem());
                encodedPatternSlot.set(ItemStack.EMPTY);
                encodedPatternSlot.setChanged();
            }
        }
    }

    @Unique
    @Override
    public void eae$ModifyPattern(Integer data) {
        if (isClientSide()) {
            sendClientAction("modifyPattern", data);
        } else {
            // modify
            var output = eae$isValid(encodedOutputsInv, data);
            if (output == null) {
                return;
            }
            var input = eae$isValid(encodedInputsInv, data);
            if (input == null) {
                return;
            }
            for (int slot = 0; slot < output.length; ++slot) {
                if (output[slot] != null) {
                    encodedOutputsInv.setStack(slot, output[slot]);
                }
            }
            for (int slot = 0; slot < input.length; ++slot) {
                if (input[slot] != null) {
                    encodedInputsInv.setStack(slot, input[slot]);
                }
            }
        }
    }

    @Unique
    private static GenericStack[] eae$isValid(ConfigInventory inv, int data) {
        boolean flag = data > 0;
        if (!flag) {
            data = -data;
        }
        GenericStack[] result = new GenericStack[inv.size()];
        for (int slot = 0; slot < inv.size(); ++slot) {
            GenericStack stack = inv.getStack(slot);
            if (stack != null) {
                if (flag) {
                    if (data * stack.amount() > Integer.MAX_VALUE) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), data * stack.amount());
                    }
                } else {
                    if (stack.amount() % data != 0) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), stack.amount() / data);
                    }
                }
            }
        }
        return result;
    }
}
