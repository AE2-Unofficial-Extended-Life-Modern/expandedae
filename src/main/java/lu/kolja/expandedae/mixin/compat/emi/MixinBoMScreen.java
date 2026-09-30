package lu.kolja.expandedae.mixin.compat.emi;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.bom.MaterialNode;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.BoMScreen;
import lu.kolja.expandedae.api.patternprovider.IPatternEncodingTerminalMenu;
import lu.kolja.expandedae.client.gui.widgets.ExpIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BoMScreen.class, remap = false)
public abstract class MixinBoMScreen extends Screen {
    @Shadow
    public AbstractContainerScreen<?> old;

    @Shadow
    private List<AccessorNode> nodes;

    @Shadow
    public abstract void onClose();

    protected MixinBoMScreen(Component title) {
        super(title);
    }

    @Unique
    private Bounds eae$encodeAll = new Bounds(0, 0, 16, 16);

    @Inject(
            method = "recalculateTree",
            at = @At("TAIL")
    )
    private void eae$positionButton(CallbackInfo ci) {
        this.eae$encodeAll = new Bounds(
                this.width - 36,
                this.height - 18,
                16,
                16
        );
    }

    @Inject(
            method = "render",
            at = @At("TAIL"),
            remap = true
    )
    private void eae$renderButton(GuiGraphics raw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!(this.old instanceof PatternEncodingTermScreen<?>)) return;

        var context = EmiDrawContext.wrap(raw);

        if (this.eae$encodeAll.contains(mouseX, mouseY)) {
            context.setColor(0.5F, 0.6F, 1.0F, 1.0F);
        }

        var icon = ExpIcon.ADD_MISSING;
        context.drawTexture(
                ExpIcon.TEXTURE,
                this.eae$encodeAll.x(),
                this.eae$encodeAll.y(),
                icon.x, icon.y,
                icon.width, icon.height
        );

        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void eae$clickButton(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && this.eae$encodeAll.contains((int) mouseX, (int) mouseY)
                && this.old instanceof PatternEncodingTermScreen<?> pet) {
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            eae$encodeAll(pet);
            this.onClose();

            cir.setReturnValue(true);
        }
    }

    @Unique
    private void eae$encodeAll(PatternEncodingTermScreen<?> pet) {
        var recipes = this.nodes.stream()
                .map(node -> node.eae$getNode().recipe)
                .filter(recipe -> recipe != null && recipe.getBackingRecipe() != null)
                .distinct()
                .toList();

        var menu = (IPatternEncodingTerminalMenu) pet.getMenu();
        // mirror it because of slight client desync
        var availablePatterns = menu.eae$getBlankPatternCount();

        int toEncode = recipes.size();
        for (var node : recipes) {
            if (availablePatterns <= 0) {
                getMinecraft().player.displayClientMessage(
                        Component.literal("Not enough patterns, recipes not encoded: §b" + toEncode),
                        false
                );
                return;
            }

            eae$encodeRecipe(pet, node, node.getBackingRecipe());
            pet.getMenu().encode();
            menu.eae$MovePattern(true);

            availablePatterns--;
            toEncode--;
        }
        getMinecraft().player.displayClientMessage(
                Component.literal("Successfully encoded §b" + recipes.size() + "§r recipes"),
                false
        );
    }

    @Unique
    public void eae$encodeRecipe(PatternEncodingTermScreen<?> pet, EmiRecipe emiRecipe, Recipe<?> recipe) {
        if ((EncodingHelper.isSupportedCraftingRecipe(recipe) || emiRecipe.getCategory().equals(VanillaEmiRecipeCategories.CRAFTING))
                && recipe.canCraftInDimensions(3, 3)) {
            EncodingHelper.encodeCraftingRecipe(pet.getMenu(), recipe, eae$getCraftingIngredients(emiRecipe), stack -> true);
        } else {
            EncodingHelper.encodeProcessingRecipe(pet.getMenu(), EmiStackHelper.ofInputs(emiRecipe), EmiStackHelper.ofOutputs(emiRecipe));
        }
    }

    @Mixin(targets = "dev.emi.emi.screen.BoMScreen$Node", remap = false)
    interface AccessorNode {
        @Accessor("node")
        MaterialNode eae$getNode();
    }

    /**
     * Copied from {@link appeng.integration.modules.emi.EmiEncodePatternHandler#getGuiIngredientsForCrafting(EmiRecipe)}
     */
    @Unique
    private List<List<GenericStack>> eae$getCraftingIngredients(EmiRecipe emiRecipe) {
        var result = new ArrayList<List<GenericStack>>(9);
        for (int i = 0; i < 9; i++) {
            var stacks = new ArrayList<GenericStack>();

            if (i < emiRecipe.getInputs().size()) {
                for (var emiStack : emiRecipe.getInputs().get(i).getEmiStacks()) {
                    var genericStack = EmiStackHelper.toGenericStack(emiStack);
                    if (genericStack != null && genericStack.what() instanceof AEItemKey) {
                        stacks.add(genericStack);
                    }
                }
            }

            result.add(stacks);
        }

        return result;
    }
}
