package dev.kairos.ui.web;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Packed BGRA patches for one browser surface revision. */
public final class PixelFrame {
    private final long sequence;
    private final int width;
    private final int height;
    private final boolean full;
    private final ByteBuffer pixels;
    private final List<Patch> patches;

    PixelFrame(long sequence, int width, int height, boolean full,
               ByteBuffer pixels, List<Patch> patches) {
        this.sequence = sequence;
        this.width = width;
        this.height = height;
        this.full = full;
        this.pixels = pixels.asReadOnlyBuffer();
        this.patches = Collections.unmodifiableList(new ArrayList<Patch>(patches));
    }

    public long sequence() { return sequence; }
    public int width() { return width; }
    public int height() { return height; }
    public boolean isFull() { return full; }
    public List<Patch> patches() { return patches; }
    public int copiedBytes() { return pixels.limit(); }

    public ByteBuffer pixels(Patch patch) {
        ByteBuffer view = pixels.asReadOnlyBuffer();
        view.position(patch.offset());
        view.limit(patch.offset() + patch.length());
        return view.slice();
    }

    public static final class Patch {
        private final DirtyRect rect;
        private final int offset;
        private final int length;

        Patch(DirtyRect rect, int offset, int length) {
            this.rect = rect;
            this.offset = offset;
            this.length = length;
        }

        public DirtyRect rect() { return rect; }
        public int offset() { return offset; }
        public int length() { return length; }
    }
}
