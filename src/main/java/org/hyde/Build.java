/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import java.io.*;
import java.nio.file.Path;

import java.util.concurrent.Callable;

import org.hyde.Utils.SiteBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "build")
class Build implements Callable<Integer> {
    @CommandLine.Parameters(arity = "0..1", paramLabel = "SITE", description = "The path where to build the site.")
    private final Path basePath = Path.of("."); // Le "." sert de valeur par défaut

    /**
     * Fonction "main" appelée par le terminal
     *
     * @return 1 en cas d'erreur, 0 si tout s'est bien passé
     */
    @Override
    public Integer call() {
        File baseFile = basePath.toFile();

        // Vérifie que le chemin donné pointe sur une ressource existante
        if (!baseFile.exists()) {
            System.err.println(
                    "Given path doesn't point to an existing folder. Please create folder with" + " \"new\" command.");
            return 1;
        }

        // Vérifie que le chemin donné pointe vers un dossier
        if (!(baseFile.isDirectory())) {
            System.err.println("Given path is not a folder.");
            return 1;
        }

        File baseBuild = new File(baseFile.getAbsolutePath() + File.separator + "build");

        // Vérifie que le chemin de build pointe vers un dossier existant ou créable
        if (!baseBuild.exists()) {
            if (!baseBuild.mkdir()) {
                System.err.println("Can't create build folder");
                return 1;
            }
        } else {
            if (!baseBuild.isDirectory()) {
                System.err.println("Build path is not a folder !");
                return 1;
            }
        }

        // Génère le site de manière récursive
        try {
            return new SiteBuilder(basePath).build(null);
        } catch (IOException e) {
            System.err.println("An error happened during the generation of the content");
            e.printStackTrace();
            return 1;
        }
    }
}
