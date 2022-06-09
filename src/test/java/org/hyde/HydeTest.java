package org.hyde;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HydeTest {
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
   
}
