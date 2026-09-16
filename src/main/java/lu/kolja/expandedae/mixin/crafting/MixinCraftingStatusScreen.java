package lu.kolja.expandedae.mixin.crafting;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingStatusMenu;
import lu.kolja.expandedae.api.misc.GuardedWidget;
import lu.kolja.expandedae.api.misc.ICancellable;
import lu.kolja.expandedae.api.misc.ScreenStyleHelper;
import lu.kolja.expandedae.definition.ExpLang;
import lu.kolja.expandedae.mixin.accessor.AccessorScreenStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mixin(value = CraftingStatusScreen.class, remap = false)
public class MixinCraftingStatusScreen extends CraftingCPUScreen<CraftingStatusMenu> {
    @Unique
    private GuardedWidget<Button> expandedae$cancelAll;

    @Unique
    private boolean eae$confirmCancel = false;

    private MixinCraftingStatusScreen(CraftingStatusMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void init(CraftingStatusMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        ScreenStyleHelper.addWidgetIfAbsent(style, "cancelAll", widget -> {
            widget.setLeft(-86);
            widget.setBottom(17);
            widget.setWidth(69);
            widget.setHeight(17);
        });
        this.expandedae$cancelAll = GuardedWidget.guardedWidget("cancelAll", ((AccessorScreenStyle) style).getWidgets(),
                id -> this.widgets.addButton(
                id,
                ExpLang.CANCEL_ALL.text(),
                this::eae$confirmCancel
        )).runIfPresent(w -> w.setTooltip(Tooltip.create(ExpLang.CANCEL_ALL_HINT.text())));
    }
    /**
     * Reset the cancel all button to its default state
     */
    @Unique
    private Runnable eae$reset = () -> {
        this.eae$confirmCancel = false;
        this.expandedae$cancelAll.widget().setMessage(ExpLang.CANCEL_ALL.text());
        this.expandedae$cancelAll.widget().setTooltip(Tooltip.create(ExpLang.CANCEL_ALL_HINT.text()));
    };

    /**
     * When the cancel all button is pressed, it'll ask for confirmation, and if confirmed, will cancel all jobs,
     * or if not pressed, will revert after 3 seconds
     */
    @Unique
    private void eae$confirmCancel() {
        if (eae$confirmCancel) {
            ((ICancellable) menu).expandedae$cancelAll();
            eae$reset.run();
        } else {
            this.expandedae$cancelAll.widget().setMessage(ExpLang.CANCEL_CONFIRM.text());
            this.expandedae$cancelAll.widget().setTooltip(Tooltip.create(ExpLang.CANCEL_CONFIRM_HINT.text()));
            this.eae$confirmCancel = true;
            CompletableFuture.runAsync(eae$reset, CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));
        }
    }

    /**
     * Disable the cancel all button when there are no CPUs with jobs
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float btn) {
        this.expandedae$cancelAll.runIfPresent(w -> w.active = menu.cpuList.cpus().stream().anyMatch(cpu -> cpu.currentJob() != null));
        super.render(guiGraphics, mouseX, mouseY, btn);
    }
}