package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.io.File;
@Command(name = "build")

class Build implements Callable<Integer> {
   public static final List<String> excluded = List.of("config.yaml", "build");

   @CommandLine.Parameters(index = "0", description = "The path where to build the site.")
   private File folder;
   private File buildPath;

   @Override
   public Integer call() {
      if (!(folder.isDirectory()) || !folder.exists()) return 1;
      buildPath = new File(folder.getAbsolutePath() + File.separator + "build");

      if (!buildPath.exists()) {
         if (!buildPath.mkdir()) return 1;
      } else {
         if (!buildPath.isDirectory()) return 1;
      }

      build(folder);

      return 0;
   }

   private static void build(File file) {
      if (excluded.contains(file.getName())) return;

      if (file.isFile()) {
         buildMD(file);
      } else {
         for (File subfile : file.listFiles()) build(subfile);
      }
   }

   private static void buildMD(File file) {
      // Load le .md
      // Build le .html
      // Dump le contenu dans le .html correspondant :)
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}