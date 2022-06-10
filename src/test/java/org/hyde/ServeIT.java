package org.hyde;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

      assertEquals(1, exitCode);
   }

   @Test
   public void shouldServe_withNewSite(@TempDir File tempDirectory) {
      Hyde app = new Hyde();
      StringWriter sw = new StringWriter();
      CommandLine cmd = new CommandLine(app);
      cmd.setOut(new PrintWriter(sw));

      int exitCode = cmd.execute("new", tempDirectory.getAbsolutePath());
      assertEquals(0, exitCode);

      cmd.execute("build", tempDirectory.getAbsolutePath());
      assertEquals(0, exitCode);

      exitCode = cmd.execute("serve", tempDirectory.getAbsolutePath());

      assertEquals(0, exitCode);
   }
}