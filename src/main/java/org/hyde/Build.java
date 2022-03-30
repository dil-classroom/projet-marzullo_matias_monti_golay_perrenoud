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


   private File baseFile;
   private File buildFile;


   @Override
   public Integer call() {
      baseFile = new File(String.valueOf(basePath));
      if (!(baseFile.isDirectory()) || !baseFile.exists()) return 1;
      buildFile = new File(baseFile.getAbsolutePath() + File.separator + "build");

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
      }

      return 0;
   }

   private void build(File file) throws IOException {
      if (excluded.contains(file.getName())) return;

      var absPath = new File(basePath+File.separator+file);

      if (absPath.isFile()) {
         buildMD(file);
      } else {
         // Créé un dossier correspondant dans le dossier build
         File absBuildFolder = new File(buildFile + File.separator + file);
         if (!absBuildFolder.exists() && !absBuildFolder.mkdirs()) {
            throw new RuntimeException("Can't create folder '"+absBuildFolder+"' !");
         }

         // Liste le contenu du dossier
         for (String subfile : Objects.requireNonNull(absPath.list())) {
            build(new File(String.valueOf(basePath.relativize(Path.of(absPath + File.separator + subfile)))));
         }
      }
   }

   private void buildMD(File file) throws IOException {
      File absFile = new File(basePath+File.separator+file);

      if (absFile.isDirectory()) throw new IllegalArgumentException("'"+absFile+"' is a folder !");

      if (!absFile.getParentFile().exists()) absFile.getParentFile().mkdirs();

      BufferedReader reader = new BufferedReader(new FileReader(absFile));
      Parser parser = Parser.builder().build();
      Node document = parser.parseReader(reader);
      HtmlRenderer renderer = HtmlRenderer.builder().build();

      var data = renderer.render(document);

      File buildFile = new File(basePath + File.separator + "build" + File.separator + file + ".html");

      try(FileOutputStream fos = new FileOutputStream(buildFile);
          BufferedOutputStream bos = new BufferedOutputStream(fos)) {
         // convert string to byte array
         byte[] bytes = data.getBytes();
         //write byte array to file
         bos.write(bytes);
         bos.close();
         fos.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void main(String... args) {
      int exitCode = new CommandLine(new Build()).execute(args);
      System.exit(exitCode);
   }
}