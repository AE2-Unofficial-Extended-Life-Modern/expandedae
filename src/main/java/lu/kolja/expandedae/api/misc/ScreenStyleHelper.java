package lu.kolja.expandedae.api.misc;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import lu.kolja.expandedae.mixin.accessor.AccessorScreenStyle;

import java.util.function.Consumer;

public final class ScreenStyleHelper {
    private ScreenStyleHelper() {}

    /**
     * So we do not have to overwrite the actual screen json, we add the widget via code
     * <p>
     * If the widget is provided by a resource pack, we will not overwrite it
     */
    public static void addWidgetIfAbsent(
            ScreenStyle style,
            String id,
            Consumer<WidgetStyle> configurator
    ) {
        var widgets = ((AccessorScreenStyle) style).getWidgets();

        widgets.computeIfAbsent(id, ignored -> {
            var widget = new WidgetStyle();
            configurator.accept(widget);
            return widget;
        });
    }
}