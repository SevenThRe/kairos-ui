package dev.kairos.ui.esp;

import java.util.List;

/** Implemented by each Minecraft endpoint to supply interpolated entity snapshots. */
public interface EspEntitySource {
    List<EspEntity> collect(float partialTicks);
}
