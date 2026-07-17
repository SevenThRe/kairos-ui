package dev.kairos.ui.web;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 * Three-slot, latest-frame-wins handoff between the CEF paint thread and the
 * Minecraft GL thread. Only damaged rows are copied from CEF's full BGRA view.
 */
public final class PixelFrameMailbox {
    private final DirtyRegionPlanner planner;
    private final Object producerLock = new Object();
    private final Deque<Slot> free = new ArrayDeque<Slot>();
    private Slot ready;
    private long sequence;
    private long publishedFrames;
    private long droppedFrames;
    private long fullFrames;
    private long copiedBytes;
    private int lastWidth;
    private int lastHeight;

    public PixelFrameMailbox() {
        this(new DirtyRegionPlanner(), 3);
    }

    public PixelFrameMailbox(DirtyRegionPlanner planner, int slots) {
        if (planner == null) throw new NullPointerException("planner");
        if (slots < 2) throw new IllegalArgumentException("at least two slots are required");
        this.planner = planner;
        for (int i = 0; i < slots; i++) free.addLast(new Slot());
    }

    public void publish(ByteBuffer source, int width, int height,
                        Collection<DirtyRect> damage, boolean forceFull) {
        if (source == null) throw new NullPointerException("source");
        int expected = checkedBytes(width, height);
        if (source.limit() < expected) throw new IllegalArgumentException("pixel buffer is too small");

        // CEF is normally a single producer, but this lock also makes that contract
        // explicit without blocking the independent GL consumer during row copies.
        synchronized (producerLock) {
            List<DirtyRect> combined = new ArrayList<DirtyRect>();
            if (damage != null) combined.addAll(damage);

            Slot target;
            boolean plannedFull = forceFull;
            synchronized (this) {
                if (ready != null && ready.frame != null) {
                    for (PixelFrame.Patch patch : ready.frame.patches()) combined.add(patch.rect());
                    plannedFull = plannedFull || ready.frame.isFull();
                    droppedFrames++;
                }
                plannedFull = plannedFull || width != lastWidth || height != lastHeight;

                target = free.pollFirst();
                if (target == null) {
                    target = ready;
                    ready = null;
                } else if (ready != null) {
                    free.addLast(ready);
                    ready = null;
                }
            }
            if (target == null) target = new Slot();

            DirtyRegionPlanner.Plan plan = planner.plan(width, height, combined, plannedFull);
            long nextSequence;
            synchronized (this) {
                nextSequence = ++sequence;
            }
            target.write(nextSequence, source, width, height, plan);

            synchronized (this) {
                ready = target;
                lastWidth = width;
                lastHeight = height;
                publishedFrames++;
                copiedBytes += target.frame.copiedBytes();
                if (plan.isFull()) fullFrames++;
            }
        }
    }

    public synchronized Lease acquireLatest() {
        if (ready == null) return null;
        Slot slot = ready;
        ready = null;
        return new Lease(this, slot);
    }

    /** Drops a no-longer-visible popup frame without touching an in-flight lease. */
    public synchronized void discardPending() {
        if (ready == null) return;
        ready.frame = null;
        free.addLast(ready);
        ready = null;
    }

    private synchronized void release(Slot slot) {
        slot.frame = null;
        free.addLast(slot);
    }

    public synchronized Stats stats() {
        return new Stats(publishedFrames, droppedFrames, fullFrames, copiedBytes);
    }

    private static int checkedBytes(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("invalid surface size");
        long bytes = (long) width * (long) height * 4L;
        if (bytes > Integer.MAX_VALUE) throw new IllegalArgumentException("surface is too large");
        return (int) bytes;
    }

    private static final class Slot {
        private ByteBuffer storage = ByteBuffer.allocateDirect(1);
        private PixelFrame frame;

        private void write(long sequence, ByteBuffer source, int width, int height,
                           DirtyRegionPlanner.Plan plan) {
            int needed = 0;
            for (DirtyRect rect : plan.rects()) needed += checkedBytes(rect.width(), rect.height());
            ensureCapacity(Math.max(1, needed));
            storage.clear();

            List<PixelFrame.Patch> patches = new ArrayList<PixelFrame.Patch>();
            ByteBuffer input = source.asReadOnlyBuffer();
            int offset = 0;
            for (DirtyRect rect : plan.rects()) {
                int rowBytes = checkedBytes(rect.width(), 1);
                int length = checkedBytes(rect.width(), rect.height());
                patches.add(new PixelFrame.Patch(rect, offset, length));
                for (int row = 0; row < rect.height(); row++) {
                    int sourceOffset = ((rect.y() + row) * width + rect.x()) * 4;
                    input.position(sourceOffset);
                    input.limit(sourceOffset + rowBytes);
                    storage.put(input);
                    input.limit(source.limit());
                }
                offset += length;
            }
            storage.flip();
            frame = new PixelFrame(sequence, width, height, plan.isFull(), storage, patches);
        }

        private void ensureCapacity(int capacity) {
            if (storage.capacity() >= capacity) return;
            int grown = Math.max(capacity, storage.capacity() + storage.capacity() / 2);
            storage = ByteBuffer.allocateDirect(grown);
        }
    }

    public static final class Lease implements AutoCloseable {
        private PixelFrameMailbox owner;
        private Slot slot;

        private Lease(PixelFrameMailbox owner, Slot slot) {
            this.owner = owner;
            this.slot = slot;
        }

        public PixelFrame frame() {
            if (slot == null) throw new IllegalStateException("lease is closed");
            return slot.frame;
        }

        @Override
        public void close() {
            PixelFrameMailbox currentOwner = owner;
            Slot currentSlot = slot;
            owner = null;
            slot = null;
            if (currentOwner != null) currentOwner.release(currentSlot);
        }
    }

    public static final class Stats {
        private final long publishedFrames;
        private final long droppedFrames;
        private final long fullFrames;
        private final long copiedBytes;

        private Stats(long publishedFrames, long droppedFrames, long fullFrames, long copiedBytes) {
            this.publishedFrames = publishedFrames;
            this.droppedFrames = droppedFrames;
            this.fullFrames = fullFrames;
            this.copiedBytes = copiedBytes;
        }

        public long publishedFrames() { return publishedFrames; }
        public long droppedFrames() { return droppedFrames; }
        public long fullFrames() { return fullFrames; }
        public long copiedBytes() { return copiedBytes; }
    }
}
