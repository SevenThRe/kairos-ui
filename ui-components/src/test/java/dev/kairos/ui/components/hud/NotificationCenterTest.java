package dev.kairos.ui.components.hud;

import java.util.List;

public final class NotificationCenterTest {
    public static void main(String[] args) {
        NotificationCenter center = new NotificationCenter(2);
        KairosNotification first = center.pushAt("First", "one", NotificationKind.INFO, 1000L, 100L);
        center.pushAt("Second", "two", NotificationKind.SUCCESS, 1000L, 200L);
        center.pushAt("Third", "three", NotificationKind.WARNING, 1000L, 300L);
        List<KairosNotification> active = center.snapshot(400L);
        require(active.size() == 2, "capacity evicts oldest notification");
        require(active.get(0).getId() != first.getId(), "first notification was evicted");
        require(active.get(1).getVisibility(400L) > 0f, "enter animation has visibility");
        require(center.snapshot(1400L).isEmpty(), "expired notifications are purged");
        System.out.println("NotificationCenterTest passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
