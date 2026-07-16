package dev.kairos.ui.esp;

import dev.kairos.ui.api.render.UiCanvas;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.core.node.UiNode;

public final class EspOverlayScene extends UiNode {
    private final EspRenderer renderer = new EspRenderer();
    private final EspEntitySource entities;
    private final WorldToScreenProjector projector;
    private volatile ThemeTokens theme;
    private EspStyle style;
    private float partialTicks;

    public EspOverlayScene(EspEntitySource entities, WorldToScreenProjector projector, ThemeTokens theme) {
        this.entities = entities;
        this.projector = projector;
        this.theme = theme;
        this.style = EspStyle.kairosModern(theme);
    }

    public EspOverlayScene(EspEntitySource entities, WorldToScreenProjector projector, ThemeRegistry themes) {
        this(entities, projector, themes.getActive().getTokens());
        themes.addListener(new ThemeRegistry.Listener() {
            @Override public void onThemeChanged(ThemePack next) {
                EspOverlayScene.this.theme = next.getTokens();
                EspOverlayScene.this.style = EspStyle.kairosModern(next.getTokens());
            }
        });
    }

    public void setPartialTicks(float partialTicks) { this.partialTicks = partialTicks; }
    public void setStyle(EspStyle style) { this.style = style; }

    @Override protected void render(UiCanvas canvas) {
        canvas.pushClip(getBounds());
        renderer.render(canvas, entities.collect(partialTicks), projector, style, theme);
        canvas.popClip();
    }
}
