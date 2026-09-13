package lu.kolja.expandedae.xmod.recipemanager;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jei.GenericEntryStackHelper;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.mixin.compat.jei.AccessorBookmarkOverlay;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IngredientBookmark;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class JEI implements IModPlugin, RecipeManager {
    private static final ResourceLocation UID = Expandedae.makeId("jei");

    private static IIngredientManager ingredientManager;
    private static BookmarkList bookmarkList;

    public JEI() {
    }

    @Override
    public void addFavorites(GenericStack... stacks) {
        if (ingredientManager == null || bookmarkList == null) {
            Expandedae.LOGGER.warn("Could not add JEI bookmarks because the JEI runtime is not available.");
            return;
        }

        for (var stack : stacks) {
            var typedIngredient = GenericEntryStackHelper.stackToIngredient(ingredientManager, stack);
            if (typedIngredient == null) {
                Expandedae.LOGGER.warn("Could not convert {} to a JEI bookmark ingredient.", stack.what());
                continue;
            }

            bookmarkList.add(IngredientBookmark.create(typedIngredient, ingredientManager));
        }
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ingredientManager = jeiRuntime.getIngredientManager();
        bookmarkList = getBookmarkList(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        ingredientManager = null;
        bookmarkList = null;
    }

    private static @Nullable BookmarkList getBookmarkList(IJeiRuntime jeiRuntime) {
        var overlay = jeiRuntime.getBookmarkOverlay();
        if (!(overlay instanceof AccessorBookmarkOverlay bookmarkOverlay)) {
            return null;
        }

        return bookmarkOverlay.getBookmarkList();
    }
}
