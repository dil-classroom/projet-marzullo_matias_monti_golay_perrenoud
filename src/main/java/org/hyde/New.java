package org.hyde;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "new")

class New implements Callable<Integer> {

   @Override
   public Integer call() {
      System.out.println("Commande 'new'");
      return 0;
   }
}