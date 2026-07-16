package dev.kairos.ui.esp;

import java.util.List;

public interface WorldObjectSource {
    List<WorldObjectEsp> collect(float partialTicks);
}
