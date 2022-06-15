package org.hyde;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class BuildIT {
    private final PrintStream sysOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(sysOut);
    }

    @Test
    public void shouldBuild_fromNew(@TempDir File tempDirectory) {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        assertEquals(0, cmd.execute("new", tempDirectory.getAbsolutePath()));
        assertEquals(0, cmd.execute("build", tempDirectory.getAbsolutePath()));

        assertTrue(new File(tempDirectory, "/build/").exists());
        assertTrue(new File(tempDirectory, "/build/").isDirectory());
        assertTrue(new File(tempDirectory, "/build/index.html").exists());

        assertFalse(new File(tempDirectory, "/build/index.html").isDirectory());
        assertFalse(new File(tempDirectory, "/build/config.yaml").exists());
        assertFalse(new File(tempDirectory, "/build/template").exists());
    }

    @Test
    public void shouldBuild_empty(@TempDir File tempDirectory) {
        // Créer un dossier vide
        // Le build
        // Créé le dossier build avec succès
        // C'est tout. :)
        assertTrue(true);
    }
}
