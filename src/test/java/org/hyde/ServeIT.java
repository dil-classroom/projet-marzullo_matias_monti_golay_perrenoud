/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class ServeIT {
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
    public void shouldFail_withNoContent() {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("serve");

        assertEquals(CommandLine.ExitCode.SOFTWARE, exitCode);
    }

    @Test
    public void shouldServe_withNewSite(@TempDir File tempDirectory) throws IOException {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        File buildDir = new File(tempDirectory, "/build");
        buildDir.mkdir();
        File indexFile = new File(buildDir, "index.html");
        indexFile.createNewFile();

        int exitCode = cmd.execute("serve", tempDirectory.getAbsolutePath());

        assertEquals(CommandLine.ExitCode.OK, exitCode);
    }
}
