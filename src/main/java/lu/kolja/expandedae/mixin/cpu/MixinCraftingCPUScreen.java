package lu.kolja.expandedae.mixin.cpu;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusTableRenderer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatus;
import appeng.menu.me.crafting.CraftingStatusEntry;
import com.google.common.collect.ImmutableList;
import lu.kolja.expandedae.api.cpu.ISearchScreen;
import lu.kolja.expandedae.api.cpu.TableEntrySorters;
import lu.kolja.expandedae.api.misc.GuardedWidget;
import lu.kolja.expandedae.api.misc.ScreenStyleHelper;
import lu.kolja.expandedae.mixin.accessor.AccessorScreenStyle;
import lu.kolja.expandedae.network.ExpNetworkHandler;
import lu.kolja.expandedae.network.implementations.HighlightDataPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftingCPUScreen.class, remap = false)
public abstract class MixinCraftingCPUScreen<T extends CraftingCPUMenu> extends AEBaseScreen<T> implements ISearchScreen {
    @Unique
    private SettingToggleButton<SortOrder> eae$sortByToggle;
    @Unique
    private SettingToggleButton<SortDir> eae$sortDirToggle;

    @Shadow protected abstract List<CraftingStatusEntry> getVisualEntries();

    @Shadow public abstract void postUpdate(CraftingStatus status);

    @Shadow private CraftingStatus status;
    @Shadow @Final private CraftingStatusTableRenderer table;
    @Unique
    private GuardedWidget<AETextField> eae$searchField;

    private MixinCraftingCPUScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(CallbackInfo ci) {
        ScreenStyleHelper.addWidgetIfAbsent(style, "searchField", widget -> {
            widget.setLeft(144);
            widget.setTop(4);
            widget.setWidth(69);
            widget.setHeight(12);
        });
        this.eae$searchField = GuardedWidget
                .guardedWidget("searchField", ((AccessorScreenStyle) style).getWidgets(), this.widgets::addTextField)
                .runIfPresent(w -> w.setPlaceholder(GuiText.SearchPlaceholder.text()))
                .runIfPresent(w -> w.setResponder(this::eae$updateSearch));

        this.eae$sortByToggle = this.addToLeftToolbar(new SettingToggleButton<>(Settings.SORT_BY, SortOrder.AMOUNT, ISearchScreen::eae$toggleButton));
        this.eae$sortDirToggle = this.addToLeftToolbar(new SettingToggleButton<>(Settings.SORT_DIRECTION, SortDir.ASCENDING, ISearchScreen::eae$toggleButton));
    }

    @Unique
    private void eae$updateSearch(String search) {
        this.postUpdate(status);
    }

    /**
     * Hide the search field when there are no entries to show
     */
    @Inject(
            method = "updateBeforeRender",
            at = @At("TAIL")
    )
    private void updateBeforeRender(CallbackInfo ci) {
        this.eae$searchField.runIfPresent(w -> w.visible = !this.getVisualEntries().isEmpty());
    }

    /**
     * When the duration is stuck, it'll default to a very large number that would overlap into the search field,
     * this replaces it with "∞" in that case
     */
    @Redirect(
            method = "updateBeforeRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;append(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0
            ),
            remap = true
    )
    private MutableComponent eae$formatDuration(MutableComponent instance, String duration) {
        return instance.append(duration.equals(" - 2562047:47:16") ? " - ∞" : duration);
    }

    /**
     * Mass redirect to only filter the entries being shown to the player
     */
    @Redirect(
            method = {"updateBeforeRender", "drawFG"},
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/crafting/CraftingStatus;getEntries()Ljava/util/List;"
            )
    )
    private List<CraftingStatusEntry> eae$getEntries(CraftingStatus status) {
        return this.eae$searchField.isPresent() ? eae$filterEntries(status) : status.getEntries();
    }

    /**
     * Filter the entries by the search field and sort them by the current sort settings
     */
    @Unique
    private List<CraftingStatusEntry> eae$filterEntries(CraftingStatus status) {
        var search = this.eae$searchField.widget().getValue();
        List<CraftingStatusEntry> entries = new ArrayList<>(status.getEntries());
        entries.sort(TableEntrySorters.Status.getComparator(eae$getSortBy(), eae$getSortDir()));
        if (!search.isEmpty()) {
            entries.removeIf(
                    entry -> !StringUtils.containsIgnoreCase(entry.getWhat().getDisplayName().getString(), search)
            );
        }
        return ImmutableList.copyOf(entries);
    }

    /**
     * Clear field on right-click
     */
    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (this.eae$searchField.isPresent() && this.eae$searchField.widget().isMouseOver(xCoord, yCoord) && btn == 1) {
            eae$clearSearch();
        }
        if (btn == 0 && Screen.hasShiftDown()) {
            var hovered = table.getHoveredStack();
            if (hovered != null) {
                var packet = new HighlightDataPacket.HighlightWhat(hovered.stack().what());
                ExpNetworkHandler.HANDLER.sendToServer(packet);
                return true;
            }
        }
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Unique
    @Override
    public void eae$clearSearch() {
        this.eae$searchField.runIfPresent(w -> w.setValue(""));
    }

    @Override
    public SortOrder eae$getSortBy() {
        return eae$sortByToggle.getCurrentValue();
    }

    @Override
    public SortDir eae$getSortDir() {
        return eae$sortDirToggle.getCurrentValue();
    }
}