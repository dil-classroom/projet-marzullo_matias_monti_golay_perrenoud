package org.hyde;

import java.io.File;
import java.util.concurrent.Callable;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Sert la dernière version construite du site via un serveur HTTP local.
 */
@Command(name = "serve")
class Serve implements Callable<Integer> {

   /**
    * Le chemin relatif vers le site à nettoyer. Par défaut, il s'agit du dossier courant.
    */
   @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to build")
   public String site = ".";

   /**
    * Méthode appelée automatiquement lors de l'invocation de "hyde serve".
    * @return 0 une fois que le serveur web est stoppé.
    */
   @Override
   public Integer call() {
      System.out.println("Starting server...");

      // Path vers le dossier build à servir
      File index = new File(site + File.separator + "build");

      // Vérifie que le dossier existe
      if (!index.exists()) {
         System.err.println("Please build project before serving !");
         return 1;
      }

      // Sert le dossier comme serveur web
      Javalin.create(config ->
              config.addStaticFiles(index.toString(), Location.EXTERNAL)
      ).start(8080);

      return 0;
   }
}