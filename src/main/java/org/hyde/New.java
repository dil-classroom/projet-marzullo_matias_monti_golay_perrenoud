package org.hyde;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "new")
class New implements Callable<Integer> {

   @Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to build")
   public String site = ".";

   @Override
   public Integer call() {
      System.out.println("new " + site);

      try {
         // Crée le(s) dossier(s) nécessaire(s) si le répertoire choisi n'existe pas
         File directory = new File(site);
         if (!directory.exists() && !directory.mkdirs()) {
            throw new Exception("Failed to create directory");
         }

         // Crée le fichier de configuration
         File config = new File(site + "/config.yaml");
         if (!config.exists() && !config.createNewFile()) {
            throw new Exception("Failed to create config.yaml");
         }

         // Insère du contenu par défaut dans ce fichier
         List<String> defaultConfigContent = Arrays.asList(
                 "Default", "config"
         );
         Path configPath = Path.of(config.getPath());
         Files.write(configPath, defaultConfigContent, StandardCharsets.UTF_8);

         // Crée un fichier de contenu
         File index = new File(site + "/index.md");
         if (!index.exists() && !index.createNewFile()) {
            throw new Exception("Failed to create index.md");
         }

         // Insère du contenu par défaut dans ce fichier
         List<String> defaultIndexContent = Arrays.asList(
                 "titre: Mon premier article",
                 "auteur: Bertil Chapuis",
                 "date: 2021-03-10",
                 "---",
                 "# Mon premier article",
                 "## Mon sous-titre",
                 "Le contenu de mon article.",
                 "![Une image](./image.png)"
         );
         Path indexPath = Path.of(index.getPath());
         Files.write(indexPath, defaultIndexContent, StandardCharsets.UTF_8);

         File templateDirectory = new File("." + site + "/template");
         if (!templateDirectory.exists() && !templateDirectory.mkdirs()) {
            throw new Exception("Failed to create directory template");
         }

         File layout = new File("." + site + "/template/layout.html");
         if (!layout.exists() && !layout.createNewFile()) {
            throw new Exception("Failed to create layout.html");
         }

         List<String> layoutContent = Arrays.asList(
                 "<html lang=\"en\">",
                 "<head>",
                 "   <meta charset=\"utf-8\">",
                 "   <title>[[ config.site_name ]]  | [[ page.site_name ]] </title>",
                 "</head>",
                 "<body>",
                 "   [[+ 'menu.html' ]]",
                 "   [[ content ]]",
                 "</body>",
                 "</html>"
         );
         Path layoutPath = Path.of(layout.getPath());
         Files.write(layoutPath, layoutContent, StandardCharsets.UTF_8);

      } catch(Exception e) {
         e.printStackTrace();
      }

      return 0;
   }
}
