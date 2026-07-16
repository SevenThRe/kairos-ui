package dev.kairos.ui.minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class KairosNotificationQueue {
    static final class Entry {
        final String title;
        final String message;
        final int color;
        final long createdAt;
        final long duration;
        Entry(String title, String message, int color, long createdAt, long duration) {
            this.title = title;
            this.message = message;
            this.color = color;
            this.createdAt = createdAt;
            this.duration = duration;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    synchronized void push(String title, String message, int color) {
        entries.add(new Entry(title, message, color, System.currentTimeMillis(), 3200L));
        while (entries.size() > 5) entries.remove(0);
    }

    synchronized List<Entry> snapshot(long now) {
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (now - entry.createdAt >= entry.duration) iterator.remove();
        }
        return new ArrayList<Entry>(entries);
    }
}
