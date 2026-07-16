package dev.kairos.ui.example;

import dev.kairos.ui.api.theme.ThemeTokens;
import dev.kairos.ui.esp.EspEntity;
import dev.kairos.ui.esp.EspEntitySource;
import dev.kairos.ui.esp.EspOverlayScene;
import dev.kairos.ui.esp.ScreenPoint;
import dev.kairos.ui.esp.WorldBounds;
import dev.kairos.ui.esp.WorldToScreenProjector;
import dev.kairos.ui.esp.EspStyle;
import dev.kairos.ui.esp.WorldObjectEsp;
import dev.kairos.ui.esp.WorldObjectSource;
import java.util.Arrays;
import java.util.List;

final class DemoEsp {
    private DemoEsp() {}

    static EspOverlayScene create(ThemeTokens theme) {
        final List<EspEntity> sample = Arrays.asList(
            new EspEntity("enemy-1", "JavaRival", new WorldBounds(318, 236, 0, 388, 514, 1),
                17f, 20f, 4.2f, false, false, "Diamond Sword", 82),
            new EspEntity("friend-1", "Alex", new WorldBounds(586, 278, 0, 650, 512, 1),
                20f, 20f, 8.7f, true, false, "Iron Sword", 61),
            new EspEntity("enemy-2", "Rival", new WorldBounds(838, 302, 0, 896, 516, 1),
                7f, 20f, 13.4f, false, false, "Wooden Sword", 24));
        EspEntitySource source = new EspEntitySource() {
            @Override public List<EspEntity> collect(float partialTicks) { return sample; }
        };
        WorldToScreenProjector projector = new WorldToScreenProjector() {
            @Override public ScreenPoint project(double x, double y, double z) {
                return new ScreenPoint((float) x, (float) y, (float) z, true);
            }
        };
        final List<WorldObjectEsp> objects = Arrays.asList(
            new WorldObjectEsp("bed-1", "BED", WorldObjectEsp.Kind.BED,
                new WorldBounds(712, 438, 0, 782, 478, 1), 0xFFFFE45B),
            new WorldObjectEsp("chest-1", "CHEST", WorldObjectEsp.Kind.CHEST,
                new WorldBounds(958, 390, 0, 1010, 458, 1), 0xFFFFD15C),
            new WorldObjectEsp("item-1", "ITEM", WorldObjectEsp.Kind.ITEM,
                new WorldBounds(186, 425, 0, 214, 464, 1), 0xFFFF565E));
        WorldObjectSource objectSource = new WorldObjectSource() {
            @Override public List<WorldObjectEsp> collect(float partialTicks) { return objects; }
        };
        EspOverlayScene scene = new EspOverlayScene(source, projector, objectSource, theme);
        scene.setStyle(EspStyle.competitivePixel());
        return scene;
    }
}
