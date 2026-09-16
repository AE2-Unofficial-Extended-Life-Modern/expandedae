package lu.kolja.expandedae.mixin.crafting;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import lu.kolja.expandedae.api.cpu.ISearchScreen;
import lu.kolja.expandedae.api.cpu.TableEntrySorters;
import lu.kolja.expandedae.api.misc.GuardedWidget;
import lu.kolja.expandedae.api.misc.NumberUtil;
import lu.kolja.expandedae.api.misc.ScreenStyleHelper;
import lu.kolja.expandedae.client.gui.widgets.ExpActionButton;
import lu.kolja.expandedae.client.gui.widgets.ExpActionItems;
import lu.kolja.expandedae.definition.ExpLang;
import lu.kolja.expandedae.mixin.accessor.AccessorScreenStyle;
import lu.kolja.expandedae.xmod.recipemanager.RecipeManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftConfirmScreen.class, remap = false)
public class MixinCraftConfirmScreen extends AEBaseScreen<CraftConfirmMenu> implements ISearchScreen {
    @Unique
    private GuardedWidget<AETextField> eae$searchField;
    @Unique
    private SettingToggleButton<SortOrder> eae$sortByToggle;
    @Unique
    private SettingToggleButton<SortDir> eae$sortDirToggle;
    @Unique
    private Button eae$addMissing;

    private MixinCraftConfirmScreen(CraftConfirmMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(CraftConfirmMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        ScreenStyleHelper.addWidgetIfAbsent(style, "searchField", widget -> {
            widget.setLeft(162);
            widget.setTop(4);
            widget.setWidth(69);
            widget.setHeight(12);
        });
        this.eae$searchField = GuardedWidget
                .guardedWidget("searchField", ((AccessorScreenStyle) style).getWidgets(), this.widgets::addTextField)
                .runIfPresent(w -> w.setPlaceholder(GuiText.SearchPlaceholder.text()));

        this.eae$sortByToggle = this.addToLeftToolbar(new SettingToggleButton<>(Settings.SORT_BY, SortOrder.AMOUNT, ISearchScreen::eae$toggleButton));
        this.eae$sortDirToggle = this.addToLeftToolbar(new SettingToggleButton<>(Settings.SORT_DIRECTION, SortDir.ASCENDING, ISearchScreen::eae$toggleButton));
        this.eae$addMissing = this.addToLeftToolbar(new ExpActionButton(ExpActionItems.ADD_MISSING, this::eae$addMissing));
    }

    @Inject(
            method = "updateBeforeRender",
            at = @At("TAIL")
    )
    private void updateBeforeRender(CallbackInfo ci) {
        if (this.menu.getPlan() == null) return;
        this.eae$addMissing.visible = this.menu.getPlan().getEntries().stream().anyMatch(e -> e.getMissingAmount() > 0);
    }

    /**
     * Mass redirect to only filter the entries being shown to the player
     */
    @Redirect(
            method = {"updateBeforeRender", "drawFG"},
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/menu/me/crafting/CraftingPlanSummary;getEntries()Ljava/util/List;"
            )
    )
    public List<CraftingPlanSummaryEntry> eae$getEntries(CraftingPlanSummary instance) {
        return this.eae$searchField.isPresent() ? eae$filterEntries(new ArrayList<>(instance.getEntries())) : instance.getEntries();
    }

    /**
     * Filter the entries by the search field and sort them by the current sort settings
     */
    @Unique
    private List<CraftingPlanSummaryEntry> eae$filterEntries(List<CraftingPlanSummaryEntry> entries) {
        var search = this.eae$searchField.widget().getValue();
        entries.sort(TableEntrySorters.Plan.getComparator(eae$getSortBy(), eae$getSortDir()));
        if (!search.isEmpty()) {
            entries.removeIf(
                    entry -> !StringUtils.containsIgnoreCase(entry.getWhat().getDisplayName().getString(), search)
            );
        }
        return entries;
    }

    /**
     * Clear field on right-click
     */
    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (this.eae$searchField.isPresent() && this.eae$searchField.widget().isMouseOver(xCoord, yCoord) && btn == 1)
            this.eae$searchField.widget().setValue("");
        return super.mouseClicked(xCoord, yCoord, btn);
    }

    /**
     * Format the number of bytes with SI units
     */
    @Redirect(
            method = "updateBeforeRender",
            at = @At(value = "INVOKE", target = "Ljava/text/NumberFormat;format(J)Ljava/lang/String;")
    )
    private String formatStorage(NumberFormat instance, long number) {
        return NumberUtil.formatNum(number);
    }

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

    /**
     * Shorten "Bytes Used" to "B Used" so it doesn't overlap into the search field in some cases
     */
    @Redirect(
            method = "updateBeforeRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/GuiText;text([Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 0
            )
    )
    private MutableComponent eae$modifyText(GuiText instance, Object[] objects) {
        return ExpLang.BYTES_USED.text(objects);
    }

    @Unique
    private void eae$addMissing() {
        var plan = menu.getPlan();
        var recipeManager = RecipeManager.INSTANCE;
        if (plan == null || recipeManager == null) return;
        recipeManager.addFavorites(
                plan.getEntries().stream()
                        .filter(e -> e.getMissingAmount() > 0)
                        .map(e -> new GenericStack(e.getWhat(), e.getWhat().getAmountPerOperation()))
                        .toArray(GenericStack[]::new)
        );
    }
}