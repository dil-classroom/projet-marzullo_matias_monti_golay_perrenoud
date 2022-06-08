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


/**
 * Crée un nouveau squelette de site avec du contenu par défaut.
 */
@Command(name = "new")
class New implements Callable<Integer> {

   /**
    * Le chemin relatif vers le site à nettoyer. Par défaut, il s'agit du dossier courant.
    */
   @Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to build")
   public String site = ".";

   /**
    * Méthode appelée automatiquement lors de l'invocation de "hyde new".
    * @return 0 si tout s'est bien passé.
    */
   @Override
   public Integer call() {
      System.out.format("Creating new site... ");

      try {
         // Crée le(s) dossier(s) nécessaire(s) si le répertoire choisi n'existe pas
         File directory = new File(site);
         if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Failed to create site directory.");
            return -1;
         }

         // Crée le dossier de templates
         File templateDirectory = new File(site + "/template");
         if (!templateDirectory.exists() && !templateDirectory.mkdirs()) {
            System.err.println("Failed to create /template directory.");
            return -1;
         }

         // Crée le fichier de configuration
         File config = new File(site + "/config.yaml");
         if (!config.exists() && !config.createNewFile()) {
            System.err.println("Failed to create config file.");
            return -1;
         }

         // Insère du contenu par défaut dans ce fichier
         List<String> defaultConfigContent = Arrays.asList(
                 "titre: Mon premier site", "creator: John Doe"
         );
         Path configPath = Path.of(config.getPath());
         Files.write(configPath, defaultConfigContent, StandardCharsets.UTF_8);

         // Crée un fichier de contenu
         File index = new File(site + "/index.md");
         if (!index.exists() && !index.createNewFile()) {
            System.err.println("Failed to create index.md.");
            return -1;
         }

         // Insère du contenu par défaut dans ce fichier
         List<String> defaultIndexContent = Arrays.asList(
                 "---",
                 "titre: Mon premier article",
                 "auteur: Bertil Chapuis",
                 "date: 2021-03-10",
                 "...",
                 "#[[ config.titre ]]",
                 "## [[ page.titre ]]",
                 "### Mon sous-titre",
                 "[[ page.auteur ]] - [[ page.date ]]",
                 "Le contenu de mon article.",
                 "![Une image](./image.png)",
                 "[[ config.creator ]] is the best"
         );
         Path indexPath = Path.of(index.getPath());
         Files.write(indexPath, defaultIndexContent, StandardCharsets.UTF_8);

         // Crée un fichier de layout exemple
         File layout = new File(site + "/template/layout.html");
         if (!layout.exists() && !layout.createNewFile()) {
            System.err.println("Failed to create layout.html.");
            return -1;
         }

         List<String> layoutContent = Arrays.asList(
                 "<html lang=\"en\">",
                 "<head>",
                 "   <meta charset=\"utf-8\">",
                 "</head>",
                 "<body>",
                 "   [[ content ]]",
                 "   [[+ 'template.html' ]]",
                 "</body>",
                 "</html>"
         );
         Path layoutPath = Path.of(layout.getPath());
         Files.write(layoutPath, layoutContent, StandardCharsets.UTF_8);

         // Crée un fichier de template exemple
         File template = new File(site + "/template/template.html");
         if (!template.exists() && !template.createNewFile()) {
            System.err.println("Failed to create template.html.");
            return -1;
         }

         List<String> templateContent = Arrays.asList(
                 "<p>Cousin à droite, cousin à gauche</p>",
                 "<p>Tout le monde fait ca mon pote</p>"
         );
         Path templatePath = Path.of(template.getPath());
         Files.write(templatePath, templateContent, StandardCharsets.UTF_8);

      } catch(Exception e) {
         System.out.println("\nAn error occured during site creation.");
         e.printStackTrace();
         return -1;
      }

      System.out.println("Done.");
      return 0;
   }
}
