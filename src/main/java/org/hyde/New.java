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

   @Parameters(paramLabel = "SITE", description = "The site to build")
   public String site;

   @Override
   public Integer call() {
      System.out.println("new " + site);

      try {
         File directory = new File("." + site);
         if (!directory.exists() && !directory.mkdirs()) {
            throw new Exception("Failed to create directory");
         }

         File config = new File("." + site + "/config.yaml");
         if (!config.exists() && !config.createNewFile()) {
            throw new Exception("Failed to create config.yaml");
         }

         List<String> defaultConfigContent = Arrays.asList(
                 "titre: Mon premier site", "creator: Gabe Newell"
         );
         Path configPath = Path.of(config.getPath());
         Files.write(configPath, defaultConfigContent, StandardCharsets.UTF_8);

         File index = new File("." + site + "/index.md");
         if (!index.exists() && !index.createNewFile()) {
            throw new Exception("Failed to create index.md");
         }

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
                 "</head>",
                 "<body>",
                 "   [[ content ]]",
                 "   [[+ 'template.html' ]]",
                 "</body>",
                 "</html>"
         );
         Path layoutPath = Path.of(layout.getPath());
         Files.write(layoutPath, layoutContent, StandardCharsets.UTF_8);

         File template = new File("." + site + "/template/template.html");
         if (!template.exists() && !template.createNewFile()) {
            throw new Exception("Failed to create layout.html");
         }

         List<String> templateContent = Arrays.asList(
                 "<p>Cousin à droite, cousin à gauche</p>",
                 "<p>Tout le monde fait ca mon pote</p>"
         );
         Path templatePath = Path.of(template.getPath());
         Files.write(templatePath, templateContent, StandardCharsets.UTF_8);

      } catch(Exception e) {
         e.printStackTrace();
      }

      return 0;
   }
}
