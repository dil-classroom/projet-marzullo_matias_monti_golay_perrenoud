package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "build")

class Build implements Callable<Integer> {

   @Override
   public Integer call() {
      System.out.println("Commande 'build'");
      return 0;
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}