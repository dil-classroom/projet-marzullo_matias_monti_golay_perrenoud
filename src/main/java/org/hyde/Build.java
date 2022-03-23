package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.io.File;
@Command(name = "build")

class Build implements Callable<Integer> {
   final private static String[] excluded = {"config.yaml", "build"};

   @CommandLine.Parameters(index = "0", description = "The path where to build the site.")
   private File folder;

   @Override
   public Integer call() {
      if (!(folder.isDirectory()) || !folder.exists()) return 1;
      File build = new File(folder.getAbsolutePath() + File.separator + "build");

      if (!build.exists()) {
         if (!build.mkdir()) return 1;
      } else {
         if (!build.isDirectory()) return 1;
      }

      System.out.println(folder);
      return 0;
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}