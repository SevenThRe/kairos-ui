package dev.kairos.ui.example;

import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.esp.EspEntity;
import dev.kairos.ui.esp.EspEntitySource;
import dev.kairos.ui.esp.EspOverlayScene;
import dev.kairos.ui.esp.ScreenPoint;
import dev.kairos.ui.esp.WorldBounds;
import dev.kairos.ui.esp.WorldToScreenProjector;
import java.util.Arrays;
import java.util.List;

final class DemoEsp {
    private DemoEsp() {}

    static EspOverlayScene create(ThemeTokens theme) {
        final List<EspEntity> sample = Arrays.asList(
            new EspEntity("enemy-1", "Steve", new WorldBounds(318, 236, 0, 388, 514, 1),
                17f, 20f, 4.2f, false, false),
            new EspEntity("friend-1", "Alex", new WorldBounds(586, 278, 0, 650, 512, 1),
                20f, 20f, 8.7f, true, false),
            new EspEntity("enemy-2", "Rival", new WorldBounds(838, 302, 0, 896, 516, 1),
                7f, 20f, 13.4f, false, false));
        EspEntitySource source = new EspEntitySource() {
            @Override public List<EspEntity> collect(float partialTicks) { return sample; }
        };
        WorldToScreenProjector projector = new WorldToScreenProjector() {
            @Override public ScreenPoint project(double x, double y, double z) {
                return new ScreenPoint((float) x, (float) y, (float) z, true);
            }
        };
        return new EspOverlayScene(source, projector, theme);
    }
}
