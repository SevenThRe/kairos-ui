package dev.kairos.ui.esp;

/** OpenGL column-major view-projection matrix projector with top-left screen coordinates. */
public final class MatrixProjector implements WorldToScreenProjector {
    private final float[] matrix;
    private final float viewportX;
    private final float viewportY;
    private final float viewportWidth;
    private final float viewportHeight;

    public MatrixProjector(float[] viewProjection, float viewportX, float viewportY,
                           float viewportWidth, float viewportHeight) {
        if (viewProjection == null || viewProjection.length != 16) throw new IllegalArgumentException("matrix");
        if (viewportWidth <= 0f || viewportHeight <= 0f) throw new IllegalArgumentException("viewport");
        this.matrix = viewProjection.clone();
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    @Override public ScreenPoint project(double worldX, double worldY, double worldZ) {
        float x = (float) worldX;
        float y = (float) worldY;
        float z = (float) worldZ;
        float clipX = matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12];
        float clipY = matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13];
        float clipZ = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14];
        float clipW = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15];
        if (clipW <= 0.0001f) return ScreenPoint.hidden();
        float ndcX = clipX / clipW;
        float ndcY = clipY / clipW;
        float ndcZ = clipZ / clipW;
        float screenX = viewportX + (ndcX * 0.5f + 0.5f) * viewportWidth;
        float screenY = viewportY + (0.5f - ndcY * 0.5f) * viewportHeight;
        // Keep off-screen points projectable so an AABB crossing a viewport edge
        // can still be clipped correctly by EspOverlayScene.
        boolean visible = ndcZ >= -1f && ndcZ <= 1f;
        return new ScreenPoint(screenX, screenY, ndcZ * 0.5f + 0.5f, visible);
    }
}
