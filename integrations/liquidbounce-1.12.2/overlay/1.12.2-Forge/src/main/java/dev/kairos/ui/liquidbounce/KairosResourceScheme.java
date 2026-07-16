package dev.kairos.ui.liquidbounce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.montoyo.mcef.api.IScheme;
import net.montoyo.mcef.api.ISchemeResponseData;
import net.montoyo.mcef.api.ISchemeResponseHeaders;
import net.montoyo.mcef.api.SchemePreResponse;

/** Serves the bundled UI and an optional local CSS override from kairos-ui/custom.css. */
public final class KairosResourceScheme implements IScheme {
    private InputStream input;
    private String mimeType = "application/octet-stream";

    @Override
    public SchemePreResponse processRequest(String url) {
        closeQuietly();
        if (url == null || !url.startsWith(KairosWebBridge.UI_ROOT)) {
            return SchemePreResponse.NOT_HANDLED;
        }

        String path = url.substring(KairosWebBridge.UI_ROOT.length());
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        if (path.isEmpty()) {
            path = "index.html";
        }
        if (path.startsWith("/") || path.contains("..") || path.contains("\\")) {
            return SchemePreResponse.HANDLED_CANCEL;
        }

        try {
            if ("custom.css".equals(path)) {
                File custom = new File(new File(System.getProperty("user.dir"), "kairos-ui"), "custom.css");
                if (custom.isFile()) {
                    input = new FileInputStream(custom);
                }
            }
            if (input == null) {
                input = KairosResourceScheme.class.getResourceAsStream("/assets/kairos_ui/web/" + path);
            }
        } catch (IOException ignored) {
            input = null;
        }

        if (input == null) {
            return SchemePreResponse.NOT_HANDLED;
        }
        mimeType = mime(path);
        return SchemePreResponse.HANDLED_CONTINUE;
    }

    @Override
    public void getResponseHeaders(ISchemeResponseHeaders response) {
        response.setMimeType(mimeType);
        response.setStatus(200);
        response.setStatusText("OK");
        response.setResponseLength(-1);
    }

    @Override
    public boolean readResponse(ISchemeResponseData response) {
        if (input == null) {
            return false;
        }
        try {
            int read = input.read(response.getDataArray(), 0, response.getBytesToRead());
            response.setAmountRead(Math.max(0, read));
            if (read <= 0) {
                closeQuietly();
            }
            return read > 0;
        } catch (IOException ignored) {
            closeQuietly();
            return false;
        }
    }

    private void closeQuietly() {
        if (input == null) {
            return;
        }
        try {
            input.close();
        } catch (IOException ignored) {
        }
        input = null;
    }

    private static String mime(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }
}

