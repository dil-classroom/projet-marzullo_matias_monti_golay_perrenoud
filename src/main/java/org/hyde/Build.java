package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.util.concurrent.Callable;

import java.util.List;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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

      try {
         build(folder);
      } catch (IOException ignored) {
         return 1;
      }

      return 0;
   }

   private static void build(File file) throws IOException {
      if (excluded.contains(file.getName())) return;

      if (file.isFile()) {
         buildMD(file);
      } else {
         file.mkdir();
         for (File subfile : file.listFiles()) build(subfile);
      }
   }

   private static void buildMD(File file) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      Parser parser = Parser.builder().build();
      Node document = parser.parseReader(reader);
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      // renderer.render(document); // return txt to write in file

      // Dump le contenu dans le .html correspondant :)
      // TODO
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}