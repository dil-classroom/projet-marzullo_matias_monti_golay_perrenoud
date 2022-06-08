package org.hyde;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

/**
 * Classe principale et point d'entrée de l'application.
 */
@Command(name = "hyde", description = "Jekyll's evil twin static site generator.", subcommands = { New.class,
      Clean.class, Build.class, Serve.class }, versionProvider = Hyde.ManifestVersionProvider.class)
public class Hyde implements Callable<Integer> {

   /**
    * Paramètre ligne de commande imprimant la version de l'application.
    */
   @Option(names = { "-v", "--version" }, versionHelp = true, description = "Print app version")
   boolean versionRequested;

   /**
    * Méthode d'entrée du programme.
    * @param args Les arguments reçus en ligne de commande.
    */
   public static void main(String... args) {
      int exitCode = new CommandLine(new Hyde()).execute(args);
      if (exitCode != 0) {
         System.exit(exitCode);
      }
   }

   /**
    * Méthode appelée automatiquement lors de l'invocation de "hyde".
    * @return 0 si tout s'est bien passé.
    */
   @Override
   public Integer call() {
      CommandLine.usage(this, System.out);
      return 0;
   }

   /**
    * Implémentation de {@link IVersionProvider}  qui retourne les informations de versions
    * contenues dans le fichier {@code /META-INF/MANIFEST.MF} de l'archive jar.
    */
   static class ManifestVersionProvider implements IVersionProvider {
      public String[] getVersion() {
         return new String[] {
              Hyde.class.getPackage().getImplementationTitle() +
              " version " + Hyde.class.getPackage().getImplementationVersion()
         };
      }
   }
}
