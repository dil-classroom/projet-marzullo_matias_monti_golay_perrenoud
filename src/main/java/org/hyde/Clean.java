package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "clean")

class Clean implements Callable<Integer> {

   @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to clean")
   public String site = ".";

   @Override
   public Integer call() {
      System.out.println("clean " + site);

      File path = new File(site + "/build");
      if (path.exists()) {
         deleteDirectory(path);
      }

      return 0;
   }

   private boolean deleteDirectory(File path) {
      if (path.exists()) {
         File[] files = path.listFiles();
         for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
               deleteDirectory(files[i]);
            } else {
               files[i].delete();
            }
         }
      }
      return (path.delete());
   }
}
