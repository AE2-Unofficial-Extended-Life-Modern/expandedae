package lu.kolja.expandedae.mixin.compat.jei;

import lu.kolja.mixinloadconditions.LoadCondition;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@LoadCondition(loadIf = "jei")
@Mixin(value = BookmarkOverlay.class, remap = false)
public interface AccessorBookmarkOverlay {
    @Accessor("bookmarkList")
    BookmarkList getBookmarkList();
}
