package org.hyde;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

@Command(name = "hyde", description = "Jekyll's evil twin static site generator.", subcommands = { New.class,
      Clean.class, Build.class, Serve.class }, versionProvider = Hyde.ManifestVersionProvider.class)
public class Hyde implements Callable<Integer> {

   @Option(names = { "-v", "--version" }, versionHelp = true, description = "Print app version")
   boolean versionRequested;

   public static void main(String... args) {
      int exitCode = new CommandLine(new Hyde()).execute(args);
      if (exitCode != 0) {
         System.exit(exitCode);
      }
   }

   @Override
   public Integer call() throws Exception {
      CommandLine.usage(this, System.out);
      return 0;
   }

   /**
    * {@link IVersionProvider} implementation that returns version information from
    * the jar file's {@code /META-INF/MANIFEST.MF} file.
    */
   static class ManifestVersionProvider implements IVersionProvider {
      public String[] getVersion() throws Exception {
         return new String[] { Hyde.class.getPackage().getImplementationTitle() + " version " + Hyde.class.getPackage().getImplementationVersion() };
      }
   }
}
