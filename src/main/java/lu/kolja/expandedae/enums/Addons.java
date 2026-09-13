package lu.kolja.expandedae.enums;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public enum Addons {
    EXT("expatternprovider"),
    MEGA("megacells"),
    APPFLUX("appflux"),
    ADV("advanced_ae"),
    APPMEK("appmek"),
    ARSENG("arseng"),
    APPBOT("appbot"),
    GTCEU("gtceu");

    public final String mod;
    public final boolean isLoaded;

    Addons(String mod) {
        this.mod = mod;
        this.isLoaded = isLoaded(mod);
    }

    private boolean isLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream()
                    .map(ModInfo::getModId)
                    .anyMatch(modId::equals);
        } else {
            return ModList.get().isLoaded(modId);
        }
    }

    public Component getUnavailableTooltip() {
        return Component.literal("Mod " + mod + " not installed!").withStyle(ChatFormatting.DARK_RED);
    }
}
