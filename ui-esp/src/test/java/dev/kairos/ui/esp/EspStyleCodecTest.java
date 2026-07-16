package dev.kairos.ui.esp;

import java.io.StringReader;

public final class EspStyleCodecTest {
    public static void main(String[] args) throws Exception {
        EspStyle original = EspStyle.competitivePixel();
        EspStyle decoded = EspStyleCodec.decode(new StringReader(EspStyleCodec.encode(original)));
        if (!decoded.hardOutline || !decoded.itemLabel || decoded.boxMode != EspStyle.BoxMode.FULL) {
            throw new AssertionError("ESP style round trip");
        }
        System.out.println("EspStyleCodecTest passed");
    }
}
