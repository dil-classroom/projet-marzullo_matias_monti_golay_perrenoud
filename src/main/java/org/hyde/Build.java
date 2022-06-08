package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Command(name = "build")

class Build implements Callable<Integer> {
   public static final List<String> excludedFiles = List.of("config.yaml");
   public static final List<String> excludedFolders = List.of("build", "template");

   @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The path where to build the site.")
   private final Path basePath = Path.of("."); // Le "." sert de valeur par défaut

   /**
    * Fonction "main" appelée par le terminal
    * @return 1 en cas d'erreur, 0 si tout s'est bien passé
    */
   @Override
   public Integer call() {
      File baseFile = basePath.toFile();

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
         System.err.println("An error happened during the generation of the content");
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
    * Charge la configuration globale
    * @return retourne la configuration chargée
    * @throws IOException Si le fichier de configuration n'est pas lisible
    */
   private HashMap<String, String> getConfig() throws IOException {
      var config = new HashMap<String, String>();

      // Ouvre le fichier config.yaml et vérifie qu'il existe
      File configFile = new File(basePath + File.separator + "config.yaml");
      if (!configFile.exists()) {
         return config;
      }

      // Lis le fichier ligne par ligne
      try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
         String line;
         while((line = reader.readLine()) != null) {
            // Transforme la ligne en clé:valeur
            var key_value = lineToConfig(line);

            // Si la ligne ne contient pas de headers, continue
            if (key_value == null) continue;

            // Ajoute la valeur à la configuration chargée
            config.put(key_value[0], key_value[1]);
         }
      }

      return config;
   }

   /**
    * Traite une ligne de texte qui devrait contenir une clé:valeur
    * @param line Ligne à traiter
    * @return clé:valeur ou null la ligne est invalide ou à ignorer
    */
   private String[] lineToConfig(String line) {
      // Si la ligne est vide, ignore
      if (line.isEmpty()) return null;

      // Ignore les commentaires
      if (line.startsWith("#")) return null;

      // Assure que la ligne contient un :
      if (!line.contains(":")) {
         System.err.println("Invalid line in config : '"+line+"'");
         return null;
      }

      // Split la ligne par le ":" pour séparer clé et valeur
      String[] key_value = line.split(":", 2);

      // Retire un éventuel espace de fin sur la clé
      if (key_value[0].endsWith(" ")) key_value[0] = key_value[0].substring(0, key_value[0].length()-2);

      // Retire un éventuel espace de départ sur la valeur
      if (key_value[1].startsWith(" ")) key_value[1] = key_value[1].substring(1);

      // Retourne la clé et la valeur
      return key_value;
   }

   /**
    * Génère la page HTML à partir d'un MD avec le fichier de template
    * @param file chemin relatif à build
    * @throws IOException En cas d'erreur avec la lecture ou la création d'un fichier
    */
   private void buildMD(File file) throws IOException {
      // Récupère la configuration globale du projet
      var globalConfig = getConfig();

      // Récupère la configuration locale du fichier
      var localConfig = getLocalConfig(new File(basePath + File.separator + file));

      // Lecture et traitement du md
      String data;
      try (BufferedReader reader = new BufferedReader(new FileReader(basePath + File.separator + file))) {
         StringBuilder content = new StringBuilder();

         // Lit le md dans un String en retirant les headers de configuration
         boolean configEndFound = localConfig.isEmpty();
         String line;
         while ((line = reader.readLine()) != null) {
            if (!configEndFound && line.equals("...")) {
               configEndFound = true;
               continue;
            }
            content.append(line).append(System.getProperty("line.separator"));
         }

         // Transforme le contenu du MD en HTML
         Parser parser = Parser.builder().build();
         Node document = parser.parse(content.toString());
         HtmlRenderer renderer = HtmlRenderer.builder().build();
         data = renderer.render(document);
      }

      // Si template.html existe, lit le fichier et insère le contenu
      File templateFile = new File(basePath + File.separator + "template" + File.separator + "template.html");
      if (templateFile.exists()) {
         StringBuilder sb = new StringBuilder();
         boolean gotContent = false;

         // Lit le fichier de template et insère le contenu à sa place
         try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.contains("[[ content ]]")) {
                  line = line.replace("[[ content ]]", data);
                  gotContent = true;
               }

               sb.append(line).append(System.getProperty("line.separator"));
            }
         }

         if (!gotContent) {
            throw new RuntimeException("Template file doesn't contain a \"[[ content ]] tag.\" ");
         }

         data = sb.toString();
      }

      // Écrit les données dans le fichier de build
      try (BufferedWriter write = new BufferedWriter(
              new FileWriter(
                      basePath + File.separator + "build" + File.separator + file
              )
      )) {
         write.write(data);
      }

      // TODO : Appeler les inclusions de fichier sur le fichier dans build
      // TODO : Appeler les remplacements de variables sur le fichier dans build
   }

   /**
    * Lit les headers de configuration d'un fichier .md
    * @param file doit être un chemin ABSOLU vers le fichier
    * @return La configuration parsée
    * @throws IOException En cas d'erreur avec le fichier
    */
   private HashMap<String, String> getLocalConfig(File file) throws IOException {
      var config = new HashMap<String, String>();

      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         // Si le fichier ne commence pas par le séparateur, il n'y a pas de config
         if (!reader.readLine().equals("---")) return config;

         // Tant qu'il y a des lignes à lire
         String line;
         while((line = reader.readLine()) != null) {
            if (line.equals("...")) {
               return config;
            }

            // Transforme la ligne en clé:valeur
            var key_value = lineToConfig(line);

            // Si la ligne ne contient pas de headers, continue
            if (key_value == null) continue;

            // Ajoute la valeur à la configuration chargée
            config.put(key_value[0], key_value[1]);
         }
      }

      throw new RuntimeException("Unterminated headers in md file '" + file + "'.");
   }

   // TODO : End of new code
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

   /*
   /**
    * Builds the HTML code of a page from a .md
    * 
    * @param file a .md, relative path from source folder
    * @throws IOException If the file cannot be opened
    * /
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

   }*/
}