/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class CleanIT {

    @Test
    public void cleanShouldDeleteFolder(@TempDir File tempDirectory) {
        assertTrue(tempDirectory.exists());
        File buildDir = new File(tempDirectory, "build");
        buildDir.mkdir();
        assertTrue(buildDir.exists());

        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("clean", tempDirectory.getAbsolutePath());
        assertAll(() -> assertEquals(0, exitCode), () -> assertFalse(buildDir.exists()));
    }

    @Test
    public void cleanShouldDoNothing_withNonExistingDirectory(@TempDir File tempDirectory) {
        assertTrue(tempDirectory.exists());

        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("clean", tempDirectory.getAbsolutePath());
        assertEquals(0, exitCode);
    }
}
