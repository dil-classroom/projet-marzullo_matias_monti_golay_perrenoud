/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class NewIT {
    @Test
    public void shouldCreateNewDirectory(@TempDir File tempDirectory) {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("new", tempDirectory.getAbsolutePath());

        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertTrue(new File(tempDirectory, "/template").exists()),
                () -> assertTrue(new File(tempDirectory, "config.yaml").exists()),
                () -> assertTrue(new File(tempDirectory, "index.md").exists()));
    }
}
