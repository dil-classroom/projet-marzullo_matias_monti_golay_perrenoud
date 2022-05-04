package org.hyde;

import java.util.concurrent.Callable;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "serve")

class Serve implements Callable<Integer> {

   @CommandLine.Parameters(paramLabel = "SITE", description = "The site to build")
   public String site;

   @Override
   public Integer call() {
      System.out.println("Commande 'serve'");

      Javalin.create(config -> {
         config.addStaticFiles(site + "/build/index.md.html", Location.EXTERNAL);
      }).start(8080);

      return 0;
   }
}