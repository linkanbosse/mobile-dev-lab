package se.mobiledev.lab;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class GameAssetsTest {
    private static final File ASSETS = new File("src/main/assets");

    @Test
    public void gameKeepsTitleAndSaveKey() throws Exception {
        File game = new File(ASSETS, "index.html");
        assertTrue("index.html saknas", game.isFile());
        String html = new String(Files.readAllBytes(game.toPath()), StandardCharsets.UTF_8);
        assertTrue(html.contains("<title>Nalle Blaster</title>"));
        // Changing the key would orphan existing player saves.
        assertTrue(html.contains("const KEY='nalle-blaster-v1'"));
    }

    @Test
    public void bundledFontFilesExist() {
        for (String name : new String[] {"fredoka.css", "fredoka-latin.woff2", "OFL.txt"}) {
            File f = new File(ASSETS, "fonts/" + name);
            assertTrue(name + " saknas", f.isFile() && f.length() > 0);
        }
    }
}
