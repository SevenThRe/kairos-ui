package dev.kairos.ui.minecraft;

import dev.kairos.ui.components.model.ModuleCatalog;

/** Exposes the single live module catalog to every GUI instance. */
final class KairosCatalog {
    private KairosCatalog() {}
    static ModuleCatalog create() { return KairosMod.getModuleManager().getCatalog(); }
}
