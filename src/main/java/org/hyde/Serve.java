package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "serve")

class Serve implements Callable<Integer> {

   @Override
   public Integer call() {
      System.out.println("Commande 'serve'");
      return 0;
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Serve()).execute(args);
      System.exit(exitCode);
   }
}