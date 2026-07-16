//#if MC<11600
package dev.kairos.ui.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.montoyo.mcef.api.IScheme;
import net.montoyo.mcef.api.ISchemeResponseData;
import net.montoyo.mcef.api.ISchemeResponseHeaders;
import net.montoyo.mcef.api.SchemePreResponse;

/** Serves only bundled UI assets plus the single user-overridable theme stylesheet. */
public final class KairosResourceScheme implements IScheme {
    private InputStream input;
    private String mime = "application/octet-stream";

    @Override public SchemePreResponse processRequest(String url) {
        closeQuietly();
        String prefix = "kairos://ui/";
        if (url == null || !url.startsWith(prefix)) return SchemePreResponse.NOT_HANDLED;
        String path = url.substring(prefix.length());
        int query = path.indexOf('?');
        if (query >= 0) path = path.substring(0, query);
        if (path.isEmpty()) path = "index.html";
        if (path.startsWith("/") || path.contains("..") || path.contains("\\")) {
            return SchemePreResponse.HANDLED_CANCEL;
        }

        try {
            if ("theme.css".equals(path)) {
                File override = new File(new File(System.getProperty("user.dir"), "kairos-ui"), "web-theme.css");
                if (override.isFile()) input = new FileInputStream(override);
            }
            if (input == null) {
                input = KairosResourceScheme.class.getResourceAsStream("/assets/kairos_ui/web/" + path);
            }
        } catch (IOException exception) {
            input = null;
        }
        if (input == null) return SchemePreResponse.NOT_HANDLED;
        mime = mime(path);
        return SchemePreResponse.HANDLED_CONTINUE;
    }

    @Override public void getResponseHeaders(ISchemeResponseHeaders response) {
        response.setMimeType(mime);
        response.setStatus(200);
        response.setStatusText("OK");
        response.setResponseLength(-1);
    }

    @Override public boolean readResponse(ISchemeResponseData response) {
        if (input == null) return false;
        try {
            int read = input.read(response.getDataArray(), 0, response.getBytesToRead());
            if (read <= 0) closeQuietly();
            response.setAmountRead(Math.max(0, read));
            return read > 0;
        } catch (IOException exception) {
            closeQuietly();
            return false;
        }
    }

    private void closeQuietly() {
        if (input == null) return;
        try { input.close(); } catch (IOException ignored) {}
        input = null;
    }

    private static String mime(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
//#endif
