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
   public static final List<String> excludedFiles = List.of("config.yaml", ".", "..");
   public static final List<String> excludedFolders = List.of("build", "template", ".", "..");

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
         return build(new File(""));
      } catch (IOException e) {
         System.err.println("An error happened during the generation of the content");
         e.printStackTrace();
         return 1;
      }
   }

   /**
    * Traite récursivement les dossiers et génère les fichiers
    * @param file Chemin à traiter. Relatif à basePath (donné dans la ligne de commande)
    * @throws IOException S'il est impossible d'ouvrir un fichier (fichier à générer, configuration, template) ou de créer le dossier de destination
    */
   private Integer build(File file) throws IOException {
      if (file == null) file = new File("");

      // Path absolu du fichier
      File absFile = new File(basePath + File.separator + file);

      // Ignore les fichiers à ignorer
      // Ignore les dossiers à ignorer
      if ((!absFile.isDirectory() && excludedFiles.contains(file.getName())) || excludedFolders.contains(file.getName()))
         return 0;


      // Créé le path où build
      File absBuildFile = new File(basePath + File.separator + "build" + File.separator + file);

      if (absFile.isDirectory()) { // Si c'est un dossier
         System.out.println("Handling folder '" + file + "'");

         // Créer le sous-dossier dans build
         if (!absBuildFile.exists() && !absBuildFile.mkdirs()) {
            System.err.println("Can't create build folders for '" + absBuildFile + "' !");
            return 1;
         }

         // Liste le contenu du dossier et rappelle build récursivement
         int retour = 0;
         for (String subfile : Objects.requireNonNull(absFile.list())) {
            if (build(new File(file + File.separator + subfile)) != 0)
               retour = 1;
         }
         return retour;
      } else { // Si c'est un fichier
         System.out.println("Building file '" + file + "'");

         // Vérifier que les dossiers parents existent, sinon les créer
         if (!absBuildFile.getParentFile().exists() && !absBuildFile.getParentFile().mkdirs()) {
            System.err.println("Can't create build folders '" + absBuildFile.getParentFile() + "' !");
            return 1;
         }

         if (file.getName().endsWith(".md")) { // Si c'est un fichier MD
            // Génère le contenu à partir d'un fichier MD
            return buildMD(file);
         } else { // Pour tous les autres fichiers
            // Copie le fichier dans build
            Files.copy(
                    absFile.toPath(),
                    absBuildFile.toPath()
            );

            return 0;
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
      if (key_value[0].endsWith(" ")) key_value[0] = key_value[0].substring(0, key_value[0].length()-1);

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
   private Integer buildMD(File file) throws IOException {
      // Récupère la configuration globale du projet
      var globalConfig = getConfig();

      // Récupère la configuration locale du fichier
      var localConfig = getLocalConfig(file);

      // Lecture et traitement du md
      String HTML_content;
      try (BufferedReader reader = new BufferedReader(new FileReader(basePath + File.separator + file))) {
         // Lit le md dans un String en retirant les headers de configuration
         if (!localConfig.isEmpty()) {
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.equals("...")) {
                  break;
               }
            }
         }

         // Transforme le contenu du MD en HTML
         Parser parser = Parser.builder().build();
         Node document = parser.parseReader(reader);
         HtmlRenderer renderer = HtmlRenderer.builder().build();
         HTML_content = renderer.render(document);
      }

      // Si template.html existe, lit le fichier et insère le contenu
      File templateFile = new File(basePath + File.separator + "template" + File.separator + "template.html");
      if (templateFile.exists()) {
         StringBuilder sb = new StringBuilder();
         boolean gotContent = false;

         // Lit le fichier de template
         // Insère le contenu à sa place
         try (
                 BufferedReader reader = new BufferedReader(new FileReader(templateFile))
         ) {
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.contains("[[ content ]]")) {
                  line = line.replace("[[ content ]]", HTML_content);
                  gotContent = true;
               }

               sb.append(line).append(System.getProperty("line.separator"));
            }
         }

         // Si le fichier template
         if (!gotContent)
            throw new RuntimeException("Template file doesn't contain a \"[[ content ]] tag.\" ");

         HTML_content = sb.toString();
      }

      // File inclusion
      HTML_content = fileInclusion(HTML_content);

      // Remplacement de variable
      HTML_content = varReplacement(HTML_content, localConfig, globalConfig);

      // Écrit le contenu généré dans le fichier de destination
      File htmlFile = new File(file.toString().substring(0, file.toString().length() - 2) + "html");
      File outputAbsFile = new File(basePath + File.separator + "build" + File.separator + htmlFile);
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputAbsFile))) {
         writer.write(HTML_content);
      }

      return 0;
   }

   private String fileInclusion(String data) throws IOException {
      String patt = "\\[\\[\\+ '(.+\\.html)' ]]";
      Pattern pattern = Pattern.compile(patt);
      Matcher matcher = pattern.matcher(data);

      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         // Charge le fichier dont le nom est dans matcher.group(1)
         File repFile = new File(basePath + File.separator + matcher.group(1));
         StringBuilder fileContent = new StringBuilder();

         try (BufferedReader reader = new BufferedReader(new FileReader(repFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
               fileContent.append(line).append(System.getProperty("line.separator"));
            }
         }

         // Remplace le contenu
         matcher.appendReplacement(sb, fileContent.toString());
      }
      matcher.appendTail(sb);

      return sb.toString();
   }

   private String varReplacement(String data, HashMap<String, String> local, HashMap<String, String> global) {
      String patt = "\\[\\[ (page|config).(\\S+) ]]";
      Pattern pattern = Pattern.compile(patt);
      Matcher matcher = pattern.matcher(data);

      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         if (matcher.group(1).equals("page")) {
            matcher.appendReplacement(sb, local.getOrDefault(matcher.group(2), "MISSING KEY '"+matcher.group(1)+"."+matcher.group(2)+"'"));
         } else {
            matcher.appendReplacement(sb, global.getOrDefault(matcher.group(2), "MISSING KEY '"+matcher.group(1)+"."+matcher.group(2)+"'"));
         }
      }

      matcher.appendTail(sb);

      return sb.toString();
   }

   /**
    * Lit les headers de configuration d'un fichier .md
    * @param file doit être un chemin relatif vers le fichier à lire
    * @return La configuration parsée
    * @throws IOException En cas d'erreur avec le fichier
    */
   private HashMap<String, String> getLocalConfig(File file) throws IOException {
      var config = new HashMap<String, String>();

      try (BufferedReader reader = new BufferedReader(new FileReader(basePath + File.separator + file))) {
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
}