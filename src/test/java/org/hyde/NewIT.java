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
                () -> assertEquals(CommandLine.ExitCode.OK, exitCode),
                () -> assertTrue(new File(tempDirectory, "/template").exists()),
                () -> assertTrue(new File(tempDirectory, "config.yaml").exists()),
                () -> assertTrue(new File(tempDirectory, "index.md").exists()));
    }

    @Test
    public void shouldReturnFailure_withReadOnlyDir(@TempDir File tempDirectory) {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        tempDirectory.setWritable(false);

        assertEquals(CommandLine.ExitCode.SOFTWARE, cmd.execute("new", tempDirectory.getAbsolutePath()));
    }

    @Test
    public void shouldReturnFailure_withReadOnlyParentDir(@TempDir File tempDirectory) {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        File rwDir = new File(tempDirectory, "/rw");
        rwDir.mkdir();
        tempDirectory.setWritable(false);
        File roDir = new File(tempDirectory, "/ro");

        assertAll(
                () -> assertEquals(CommandLine.ExitCode.SOFTWARE, cmd.execute("new", roDir.getAbsolutePath())),
                () -> assertEquals(CommandLine.ExitCode.OK, cmd.execute("new", rwDir.getAbsolutePath())));
        tempDirectory.setWritable(true);
    }

    @Test
    public void shouldReturnFailure_withReadOnlySiteDir(@TempDir File tempDirectory) {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        File rootDir = new File(tempDirectory, "/root");
        rootDir.mkdir();
        rootDir.setWritable(false);

        assertEquals(CommandLine.ExitCode.SOFTWARE, cmd.execute("new", rootDir.getAbsolutePath()));
    }
}
