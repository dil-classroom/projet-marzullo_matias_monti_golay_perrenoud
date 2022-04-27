package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "serve")

class Serve implements Callable<Integer> {

   @CommandLine.Parameters(paramLabel = "SITE", description = "The site to build")
   public String site;

   @Override
   public Integer call() {
      System.out.println("Commande 'serve'");

      File htmlFile = new File(site + "/build/index.md.html");
      System.out.println(htmlFile.getAbsolutePath());

      try{
         Desktop.getDesktop().browse(htmlFile.toURI());
      }
      catch(IOException e){
         System.out.println("Cannot open the file");
      }

      return 0;
   }
}