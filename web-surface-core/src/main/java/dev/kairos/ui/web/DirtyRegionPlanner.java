package dev.kairos.ui.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Coalesces CEF damage and chooses when a full upload is cheaper. */
public final class DirtyRegionPlanner {
    public static final int DEFAULT_MAX_RECTS = 32;
    public static final double DEFAULT_FULL_THRESHOLD = 0.42D;
    public static final int DEFAULT_MERGE_GAP = 2;

    private final int maxRects;
    private final double fullThreshold;
    private final int mergeGap;

    public DirtyRegionPlanner() {
        this(DEFAULT_MAX_RECTS, DEFAULT_FULL_THRESHOLD, DEFAULT_MERGE_GAP);
    }

    public DirtyRegionPlanner(int maxRects, double fullThreshold, int mergeGap) {
        if (maxRects < 1) throw new IllegalArgumentException("maxRects must be positive");
        if (fullThreshold <= 0.0D || fullThreshold > 1.0D) {
            throw new IllegalArgumentException("fullThreshold must be in (0, 1]");
        }
        this.maxRects = maxRects;
        this.fullThreshold = fullThreshold;
        this.mergeGap = Math.max(0, mergeGap);
    }

    public Plan plan(int width, int height, Collection<DirtyRect> damage, boolean forceFull) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("invalid surface size");
        DirtyRect full = new DirtyRect(0, 0, width, height);
        if (forceFull) return new Plan(true, Collections.singletonList(full));

        List<DirtyRect> merged = new ArrayList<DirtyRect>();
        if (damage != null) {
            for (DirtyRect raw : damage) {
                if (raw == null) continue;
                DirtyRect candidate = raw.clamp(width, height);
                if (candidate.isEmpty()) continue;
                mergeInto(merged, candidate);
            }
        }

        if (merged.isEmpty()) return new Plan(false, Collections.<DirtyRect>emptyList());

        long dirtyArea = 0L;
        for (DirtyRect rect : merged) dirtyArea += rect.area();
        long surfaceArea = (long) width * (long) height;
        if (merged.size() > maxRects || dirtyArea >= Math.ceil(surfaceArea * fullThreshold)) {
            return new Plan(true, Collections.singletonList(full));
        }
        return new Plan(false, merged);
    }

    private void mergeInto(List<DirtyRect> regions, DirtyRect candidate) {
        boolean changed;
        do {
            changed = false;
            for (int i = regions.size() - 1; i >= 0; i--) {
                DirtyRect existing = regions.get(i);
                if (candidate.touches(existing, mergeGap)) {
                    candidate = candidate.union(existing);
                    regions.remove(i);
                    changed = true;
                }
            }
        } while (changed);
        regions.add(candidate);
    }

    public static final class Plan {
        private final boolean full;
        private final List<DirtyRect> rects;

        private Plan(boolean full, List<DirtyRect> rects) {
            this.full = full;
            this.rects = Collections.unmodifiableList(new ArrayList<DirtyRect>(rects));
        }

        public boolean isFull() { return full; }
        public List<DirtyRect> rects() { return rects; }
    }
}
