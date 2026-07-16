package dev.kairos.ui.components.hud;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Thread-safe notification lifecycle store. Rendering remains stateless and deterministic for a supplied time. */
public final class NotificationCenter {
    private final List<KairosNotification> entries = new ArrayList<KairosNotification>();
    private final int capacity;
    private long nextId = 1L;

    public NotificationCenter() { this(8); }

    public NotificationCenter(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
    }

    public synchronized KairosNotification push(String title, String message, NotificationKind kind,
                                                long durationMillis) {
        return pushAt(title, message, kind, durationMillis, System.currentTimeMillis());
    }

    public synchronized KairosNotification pushAt(String title, String message, NotificationKind kind,
                                                  long durationMillis, long nowMillis) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("title");
        if (message == null) throw new IllegalArgumentException("message");
        if (kind == null) throw new IllegalArgumentException("kind");
        if (durationMillis < 500L) throw new IllegalArgumentException("durationMillis");
        purge(nowMillis);
        while (entries.size() >= capacity) entries.remove(0);
        KairosNotification notification = new KairosNotification(nextId++, title, message, kind,
            nowMillis, durationMillis);
        entries.add(notification);
        return notification;
    }

    public synchronized List<KairosNotification> snapshot(long nowMillis) {
        purge(nowMillis);
        return new ArrayList<KairosNotification>(entries);
    }

    public synchronized void dismiss(long id) {
        Iterator<KairosNotification> iterator = entries.iterator();
        while (iterator.hasNext()) if (iterator.next().getId() == id) iterator.remove();
    }

    public synchronized void clear() { entries.clear(); }

    private void purge(long nowMillis) {
        Iterator<KairosNotification> iterator = entries.iterator();
        while (iterator.hasNext()) if (iterator.next().isExpired(nowMillis)) iterator.remove();
    }
}
