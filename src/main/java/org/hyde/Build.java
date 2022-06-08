package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Command(name = "build")

class Build implements Callable<Integer> {
   public static final List<String> excludedFiles = List.of("config.yaml"); // TODO : Ajouter fichier squelette et autre ?
   public static final List<String> excludedFolders = List.of("build"); // TODO : Ajouter le dossier de templates et layout et include

   @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The path where to build the site.")
   private final Path basePath = Path.of("."); // Le "." sert de valeur par défaut

   /**
    * Fonction "main" appelée par le terminal
    * @return 1 en cas d'erreur, 0 si tout s'est bien passé
    */
   @Override
   public Integer call() {
      File baseFile = new File(String.valueOf(basePath)); // TODO : Use toFile

      // Vérifie que le chemin donné pointe sur une ressource existante
      if (!baseFile.exists()) {
         System.err.println("Given path doesn't point to an existing folder. Please create folder with \"new\" command.");
         return 1;
      }

      // Vérifie que le chemin donné pointe vers un dossier
      if (!(baseFile.isDirectory())) {
         System.err.println("Given path is not a folder.");
         return 1;
      }

      File baseBuild = new File(baseFile.getAbsolutePath() + File.separator + "build");

      // Vérifie que le chemin de build pointe vers un dossier existant ou créable
      if (!baseBuild.exists()) {
         if (!baseBuild.mkdir()) {
            System.err.println("Can't create build folder");
            return 1;
         }
      } else {
         if (!baseBuild.isDirectory()) {
            System.err.println("Build path is not a folder !");
            return 1;
         }
      }

      // Génère le site de manière récursive
      try {
         build(new File(""));
      } catch (IOException e) {
         e.printStackTrace();
         return 1;
      }

      return 0;
   }

   /**
    * Traite récursivement les dossiers et génère les fichiers
    * @param file Chemin à traiter. Relatif à basePath (donné dans la ligne de commande)
    * @throws IOException S'il est impossible d'ouvrir un fichier (fichier à générer, configuration, template) ou de créer le dossier de destination
    */
   private void build(File file) throws IOException {
      // Ignore les fichiers à ignorer
      if (excludedFiles.contains(file.getName())) return;

      // TODO : Ignore les dossiers à ignorer (ainsi que le contenu des dossiers)

      // Créé le path où build
      File buildFile = new File("build" + File.separator + file);

      if (file.isDirectory()) { // Si c'est un dossier
         // Créer le sous-dossier dans build
         if (!buildFile.exists() && buildFile.mkdirs()) {
            System.err.println("Can't create build folders for '" + buildFile + "' !");
            return;
         }

         // Liste le contenu du dossier et rappelle build récursivement
         for (String subfile : Objects.requireNonNull(file.list())) {
            build(new File(file + File.separator + subfile));
         }
      } else { // Si c'est un fichier
         // Vérifier que les dossiers parents existent, sinon les créer
         if (!buildFile.getParentFile().exists() && !buildFile.getParentFile().mkdirs()) {
            System.err.println("Can't create build folders '" + buildFile.getParentFile() + "' !");
            return;
         }

         // Assure que le fichier de destination dans build est créé
         if (!buildFile.exists()) {
            new FileOutputStream(buildFile).close();
         }

         if (file.getName().endsWith(".md")) { // Si c'est un fichier MD
            // Génère le contenu à partir d'un fichier MD
            buildMD(file);
         } else { // Pour tous les autres fichiers
            // Copie le fichier dans build
            Files.copy(
                    Path.of(basePath + File.separator + file),
                    Path.of(basePath + File.separator + buildFile)
            );
         }
      }
   }

   /**
    * Check for file inclusions and charges them
    * @param file path to the HTML file
    * @throws IOException If the reader couldn't read the file
    */
   private void checkFileInclusion(String file) throws IOException {
      if (!file.endsWith(".html")) return;

      BufferedReader mdReader = new BufferedReader(new FileReader(basePath+File.separator+"build"+File.separator+file));
     
      StringBuilder data = new StringBuilder();

      while(mdReader.ready()){
         data.append(mdReader.readLine()).append("\n");
      }
      mdReader.close();
     
      Pattern filePattern = Pattern.compile("\\[\\[\\+ '.+\\.html' ]]");

      Matcher fileMatcher = filePattern.matcher(data.toString());


      while (fileMatcher.find()) {
         String fileName = fileMatcher.group().substring(5, fileMatcher.group().length() - 4);
         System.out.println(fileName);

         BufferedReader fileInclusionReader = new BufferedReader(new FileReader(basePath+File.separator+"template"+File.separator+fileName));
         System.out.println(basePath+File.separator+"template"+File.separator+fileName);
         BufferedWriter mdWriter = new BufferedWriter(new FileWriter(basePath+File.separator+"build"+File.separator+file));
         StringBuilder fileInclusionData = new StringBuilder();

         while(fileInclusionReader.ready()){
            fileInclusionData.append(fileInclusionReader.readLine()).append("\n");
         }

         data = new StringBuilder(data.toString().replaceFirst("\\[\\[\\+ '.+\\.html' ]]", fileInclusionData.toString()));

         mdWriter.write(String.valueOf(data));
         mdWriter.close();
      }
   }


   /**
    * Check for layout template, if present, charges content in there
    * @param data content of the parsed html
    * @throws IOException If the reader couldn't read the file
    */
   private String putContentToLayout(String data) throws IOException {
      File file = new File(basePath+File.separator+"template"+File.separator+"layout.html");
      if(!file.exists())
         return data;
      System.out.println("test");

      BufferedReader fileReader = new BufferedReader(new FileReader(file));
      StringBuilder layoutcontent = new StringBuilder();

      while(fileReader.ready()){
         layoutcontent.append(fileReader.readLine()).append("\n");
      }
      fileReader.close();

      Pattern filePattern = Pattern.compile("\\[\\[ content ]]");

      Matcher fileMatcher = filePattern.matcher(layoutcontent);


      while (fileMatcher.find()) {
         data = String.valueOf(new StringBuilder(layoutcontent.toString().replaceFirst("\\[\\[ content ]]", data)));
      }

      return data;
   }
  
  /*
    * Charges the metadatas requested in the file
    * 
    * @param file path to the md file
    * @throws IOException If the reader couldn't read the config file
    */
   private void metadataTemplating(File file) throws IOException {
      if (!file.toString().endsWith("md"))
         return;

      try (
            BufferedReader configReader = new BufferedReader(new FileReader(basePath + File.separator + "config.yaml"));
            BufferedReader mdReader = new BufferedReader(new FileReader(basePath + File.separator + file));
            BufferedWriter mdWriter = new BufferedWriter(new FileWriter(basePath + File.separator + file))) {

         List<String> configFile = new ArrayList<>();
         StringBuilder data = new StringBuilder();

         while (mdReader.ready()) {
            data.append(mdReader.readLine()).append("\n");
         }

         while (configReader.ready()) {
            configFile.add(configReader.readLine());
         }

         Pattern configPattern = Pattern.compile("\\[\\[ config.\\S+ ]]");
         Pattern pagePattern = Pattern.compile("\\[\\[ page.\\S+ ]]");

         Matcher configMatcher = configPattern.matcher(data.toString());

         while (configMatcher.find()) {
            String configName = configMatcher.group().substring(10, configMatcher.group().length() - 3);
            String configValue = "";

            for (String line : configFile) {
               if (line.contains(configName)) {
                  configValue = line.substring(configName.length() + 1);
                  break;
               }
            }

            if (!configValue.isEmpty())
               data = new StringBuilder(data.toString().replaceFirst("\\[\\[ config.\\S+ ]]", configValue));
         }

         if (data.toString().startsWith("---")) {
            String pageMetadatas = data.substring(0, data.indexOf("..."));
            data = new StringBuilder(data.substring(data.indexOf("...") + 4));
            ;

            Matcher pageMatcher = pagePattern.matcher(data.toString());

            while (pageMatcher.find()) {
               String configName = pageMatcher.group().substring(8, pageMatcher.group().length() - 3);
               String configValue = "";

               if (pageMetadatas.contains(configName)) {
                  configValue = pageMetadatas.substring(pageMetadatas.indexOf(configName) + configName.length() + 1,
                        pageMetadatas.indexOf('\n', pageMetadatas.indexOf(configName)));
               }

               if (!configValue.isEmpty())
                  data = new StringBuilder(data.toString().replaceFirst("\\[\\[ page.\\S+ ]]", configValue));
            }
         }

         mdWriter.write(String.valueOf(data));
      } catch (Exception e) {
         System.err.println("An error occured while converting to HTML");
         System.err.println(e.getMessage());
      }
   }

   /**
    * Builds the HTML code of a page from a .md
    * 
    * @param file a .md, relative path from source folder
    * @throws IOException If the file cannot be opened
    */
   private void buildMD(File file) throws IOException {
      // Checking that the given file is a md !
      if (!file.toString().endsWith("md"))
         return;

      // Absolute path to the file, with given basePath in cmd
      File absFile = new File(basePath + File.separator + file);

      // Checking that the file isn't a directory !
      if (absFile.isDirectory() || !absFile.exists())
         throw new IllegalArgumentException("'" + absFile + "' isn't a valid file !");

      // Path to HTML file
      File buildFile = new File(basePath + File.separator + "build" + File.separator + file + ".html");

      // Checks that the corresponding folder exists in the build folder
      if (!buildFile.getParentFile().exists() && !buildFile.getParentFile().mkdirs()) {
         throw new RuntimeException("Can't create folder '" + buildFile.getParentFile() + "' !");
      }

      // Converts MD to HTML
      BufferedReader reader = new BufferedReader(new FileReader(absFile));
      Parser parser = Parser.builder().build();
      Node document = parser.parseReader(reader);
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      var data = renderer.render(document);

      data = putContentToLayout(data);

      // Dumps the datas to the HTML file
      try (
            FileOutputStream fos = new FileOutputStream(buildFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
         byte[] bytes = data.getBytes();
         bos.write(bytes);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }
}