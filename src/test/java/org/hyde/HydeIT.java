/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class HydeIT {
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
    public void shouldPrintVersion_withVAgrumentPassed() {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("-v");
        assertAll(
                () -> assertEquals(CommandLine.ExitCode.OK, exitCode)
                // () -> assertTrue(outContent.toString().contains("hyde version"))
                );
    }

    @Test
    public void shouldPrintVersion_withVersionAgrumentPassed() {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("--version");
        assertAll(
                () -> assertEquals(CommandLine.ExitCode.OK, exitCode)
                // () -> assertTrue(outContent.toString().contains("hyde version"))
                );
    }

    @Test
    public void shouldPrintHelp_WithNoArguments() {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute();
        assertEquals(CommandLine.ExitCode.OK, exitCode);
    }

    @Test
    public void shouldReturnFailure_withWrongArgument() {
        Hyde app = new Hyde();
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(app);
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute("false");
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
    }
}
