package dev.kairos.ui.platform;

import dev.kairos.ui.api.theme.ThemeCodec;
import dev.kairos.ui.api.theme.ThemePack;
import dev.kairos.ui.api.theme.ThemeRegistry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

/** Loads user theme packs from kairos-ui/themes and persists the active theme id. */
public final class ThemeDirectory {
    private final File root;
    private final File themes;
    private final File selection;

    public ThemeDirectory(File root) {
        if (root == null) throw new IllegalArgumentException("root");
        this.root = root;
        this.themes = new File(root, "themes");
        this.selection = new File(root, "selected-theme.txt");
    }

    public synchronized int loadInto(ThemeRegistry registry) throws IOException {
        ensureDirectories();
        File[] files = themes.listFiles();
        if (files == null) return 0;
        Arrays.sort(files, new Comparator<File>() {
            @Override public int compare(File left, File right) { return left.getName().compareTo(right.getName()); }
        });
        int loaded = 0;
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".properties")) continue;
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            try { registry.register(ThemeCodec.decode(reader)); loaded++; }
            catch (RuntimeException exception) { throw new IOException("Invalid Kairos theme " + file, exception); }
            finally { reader.close(); }
        }
        String selected = readSelection();
        if (selected != null && registry.get(selected) != null) registry.activate(selected);
        return loaded;
    }

    public synchronized void saveTheme(ThemePack pack) throws IOException {
        ensureDirectories();
        write(new File(themes, pack.getId() + ".properties"), ThemeCodec.encode(pack));
    }

    public synchronized ThemePack select(ThemeRegistry registry, String id) throws IOException {
        ThemePack active = registry.activate(id);
        ensureDirectories();
        write(selection, id + "\n");
        return active;
    }

    public File getThemesDirectory() { return themes; }

    private void ensureDirectories() throws IOException {
        if (!themes.isDirectory() && !themes.mkdirs() && !themes.isDirectory()) {
            throw new IOException("Cannot create " + themes);
        }
    }

    private String readSelection() throws IOException {
        if (!selection.isFile()) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(selection), StandardCharsets.UTF_8));
        try {
            String value = reader.readLine();
            return value == null || value.trim().isEmpty() ? null : value.trim();
        } finally { reader.close(); }
    }

    private static void write(File file, String value) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        try { writer.write(value); }
        finally { writer.close(); }
    }
}
