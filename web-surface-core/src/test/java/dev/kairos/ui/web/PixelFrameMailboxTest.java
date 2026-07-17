package dev.kairos.ui.web;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public final class PixelFrameMailboxTest {
    public static void main(String[] args) {
        copiesOnlyDirtyPixelsAfterFirstFrame();
        mergesDamageWhenPendingFrameIsDropped();
        promotesLargeDamageToFullFrame();
        System.out.println("PixelFrameMailboxTest passed");
    }

    private static void copiesOnlyDirtyPixelsAfterFirstFrame() {
        PixelFrameMailbox mailbox = new PixelFrameMailbox();
        ByteBuffer pixels = pixels(4, 4);
        mailbox.publish(pixels, 4, 4, Collections.singletonList(new DirtyRect(0, 0, 4, 4)), true);
        PixelFrameMailbox.Lease initial = mailbox.acquireLatest();
        check(initial.frame().isFull(), "first frame must be full");
        initial.close();

        mailbox.publish(pixels, 4, 4, Collections.singletonList(new DirtyRect(1, 1, 2, 2)), false);
        PixelFrameMailbox.Lease lease = mailbox.acquireLatest();
        PixelFrame frame = lease.frame();
        check(!frame.isFull(), "small damage must remain partial");
        check(frame.copiedBytes() == 16, "2x2 BGRA patch must copy 16 bytes");
        ByteBuffer patch = frame.pixels(frame.patches().get(0));
        check((patch.get(0) & 0xFF) == 5, "packed patch must begin at source pixel 5");
        check((patch.get(8) & 0xFF) == 9, "second packed row must begin at source pixel 9");
        lease.close();
    }

    private static void mergesDamageWhenPendingFrameIsDropped() {
        PixelFrameMailbox mailbox = new PixelFrameMailbox();
        ByteBuffer pixels = pixels(8, 8);
        mailbox.publish(pixels, 8, 8, Collections.singletonList(new DirtyRect(0, 0, 8, 8)), true);
        PixelFrameMailbox.Lease initial = mailbox.acquireLatest();
        initial.close();

        mailbox.publish(pixels, 8, 8, Collections.singletonList(new DirtyRect(0, 0, 1, 1)), false);
        mailbox.publish(pixels, 8, 8, Collections.singletonList(new DirtyRect(7, 7, 1, 1)), false);
        PixelFrameMailbox.Lease lease = mailbox.acquireLatest();
        check(lease.frame().patches().size() == 2, "replacement frame must preserve pending damage");
        check(mailbox.stats().droppedFrames() == 1, "one pending frame must be counted as dropped");
        lease.close();
    }

    private static void promotesLargeDamageToFullFrame() {
        DirtyRegionPlanner planner = new DirtyRegionPlanner(32, 0.40D, 0);
        DirtyRegionPlanner.Plan plan = planner.plan(10, 10,
            Arrays.asList(new DirtyRect(0, 0, 5, 10), new DirtyRect(9, 9, 1, 1)), false);
        check(plan.isFull(), "damage over the configured threshold must become full");
    }

    private static ByteBuffer pixels(int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int i = 0; i < width * height; i++) {
            buffer.put((byte) i).put((byte) i).put((byte) i).put((byte) 0xFF);
        }
        return (ByteBuffer) buffer.flip();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
