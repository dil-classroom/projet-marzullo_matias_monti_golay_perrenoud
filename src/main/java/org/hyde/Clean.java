package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "clean")

class Clean implements Callable<Integer> {

   @Override
   public Integer call() {
      System.out.println("Commande 'clean'");
      return 0;
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Clean()).execute(args);
      String dirPath = args[1];

      File path = new File(dirPath + "/build");
      if (path.exists()) {
         path.delete();
      }

      System.exit(exitCode);
   }
}
