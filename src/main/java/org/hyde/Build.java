package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import java.util.List;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Command(name = "build")

class Build implements Callable<Integer> {
   public static final List<String> excluded = List.of("config.yaml", "build");

   @CommandLine.Parameters(index = "0", description = "The path where to build the site.")
   private Path basePath;

   @Override
   public Integer call() {
      File baseFile = new File(String.valueOf(basePath));
      if (!(baseFile.isDirectory()) || !baseFile.exists()) return 1;
      File buildFile = new File(baseFile.getAbsolutePath() + File.separator + "build");

      // Checks or creates that the build folder exists
      if (!buildFile.exists()) {
         if (!buildFile.mkdir()) return 1;
      } else {
         if (!buildFile.isDirectory()) return 1;
      }

      // Starts the recursive building of the folder
      try {
         build(new File(""));
      } catch (IOException e) {
         e.printStackTrace();
         return 1;
      }

      return 0;
   }

   private void build(File file) throws IOException {
      if (excluded.contains(file.getName())) return;

      var absPath = new File(basePath+File.separator+file);

      if (absPath.isFile()) {
         buildMD(file);
      } else {
         // Liste le contenu du dossier
         for (String subfile : Objects.requireNonNull(absPath.list())) {
            build(new File(String.valueOf(basePath.relativize(Path.of(absPath + File.separator + subfile)))));
         }
      }
   }

   /**
    * Builds the HTML code of a page from a .md
    * @param file a .md, relative path from source folder
    * @throws IOException If the file cannot be opened
    */
   private void buildMD(File file) throws IOException {
      // Checking that the given file is a md !
      if (!file.toString().endsWith("md"))
         throw new IllegalArgumentException("Only accepting md files !");

      // Absolute path to the file, with given basePath in cmd
      File absFile = new File(basePath+File.separator+file);

      // Checking that the file isn't a directory !
      if (absFile.isDirectory() || !absFile.exists()) throw new IllegalArgumentException("'"+absFile+"' isn't a valid file !");

      // Path to HTML file
      File buildFile = new File(basePath + File.separator + "build" + File.separator + file + ".html");

      // Checks that the corresponding folder exists in the build folder
      if (!buildFile.getParentFile().exists() && !buildFile.getParentFile().mkdirs()) {
         throw new RuntimeException("Can't create folder '"+buildFile.getParentFile()+"' !");
      }

      // Converts MD to HTML
      BufferedReader reader = new BufferedReader(new FileReader(absFile));
      Parser parser = Parser.builder().build();
      Node document = parser.parseReader(reader);
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      var data = renderer.render(document);

      // Dumps the datas to the HTML file
      try(
              FileOutputStream fos = new FileOutputStream(buildFile);
              BufferedOutputStream bos = new BufferedOutputStream(fos)
      ) {
         byte[] bytes = data.getBytes();
         bos.write(bytes);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}