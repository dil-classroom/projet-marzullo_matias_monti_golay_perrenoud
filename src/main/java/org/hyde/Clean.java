/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** Nettoie un site des artéfacts créés par la commande "build". */
@Command(name = "clean")
class Clean implements Callable<Integer> {

    /** Le chemin relatif vers le site à nettoyer. Par défaut, il s'agit du dossier courant. */
    @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to clean")
    public String site = ".";

    /**
     * Méthode appelée automatiquement lors de l'invocation de "hyde clean".
     *
     * @return 0 si tout s'est bien passé, -1 si le dossier n'a pas pu etre supprimé.
     */
    @Override
    public Integer call() {
        System.out.format("Cleaning site %s... ", site);

        File path = new File(site + "/build");
        if (path.exists()) {
            try {
                FileUtils.deleteDirectory(path);
            } catch (IOException e) {
                System.err.format("\nError: Cannot delete directory %s", path.getAbsolutePath());
                return -1;
            }
        }

        System.out.println("Done.");
        return 0;
    }
}
