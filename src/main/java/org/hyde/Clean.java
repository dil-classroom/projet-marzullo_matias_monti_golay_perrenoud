package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;

@Command(name = "clean")

class Clean implements Callable<Integer> {

   @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to clean")
   public String site = ".";

   @Override
   public Integer call() {
      System.out.println("clean " + site);

      File path = new File(site + "/build");
      if (path.exists()) {
         try {
            FileUtils.deleteDirectory(path);
         } catch (IOException e) {
            System.err.println("Cannot delete directory " + path.getAbsolutePath());
         }
      }

      return 0;
   }
}
