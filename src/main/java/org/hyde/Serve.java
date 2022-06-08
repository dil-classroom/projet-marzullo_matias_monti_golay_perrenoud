package org.hyde;

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

      Javalin.create(config -> {
         config.addStaticFiles(site + "/build/index.md.html", Location.EXTERNAL);
      }).start(8080);

      return 0;
   }
}