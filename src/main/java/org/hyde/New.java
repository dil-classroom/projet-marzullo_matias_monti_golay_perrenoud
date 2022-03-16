package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "new")

class New implements Callable<Integer> {

   @Override
   public Integer call() {
      System.out.println("Commande 'new'");
      return 0;
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new New()).execute(args);
      System.exit(exitCode);
   }
}