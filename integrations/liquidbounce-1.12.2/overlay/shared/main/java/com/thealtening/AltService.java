/*
 * Compatibility adapter for LiquidBounce b73's former TheAltening API.
 */
package com.thealtening;

import com.thealtening.auth.TheAlteningAuthentication;
import com.thealtening.auth.service.AlteningServiceType;

public final class AltService {

    public enum EnumAltService {
        MOJANG,
        THEALTENING
    }

    private final TheAlteningAuthentication authentication =
            TheAlteningAuthentication.mojang();

    public EnumAltService getCurrentService() {
        return authentication.getService() == AlteningServiceType.THEALTENING
                ? EnumAltService.THEALTENING
                : EnumAltService.MOJANG;
    }

    /** Retains the retired API's checked exceptions for source compatibility. */
    public void switchService(final EnumAltService service)
            throws NoSuchFieldException, IllegalAccessException {
        authentication.updateService(service == EnumAltService.THEALTENING
                ? AlteningServiceType.THEALTENING
                : AlteningServiceType.MOJANG);
    }
}
