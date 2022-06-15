/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.commonmark.node.*;
import org.hyde.Utils.RecursiveFileWatcher;
import org.hyde.Utils.SiteBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "build")
class Build implements Callable<Integer> {
    public static final List<String> excluded = List.of("config.yaml", "build", "template");

    @CommandLine.Parameters(index = "0", description = "The path where to build the site.")
    private Path basePath; // Path vers le dossier racine où build

    @CommandLine.Option(
            names = {"--watch"},
            description = "If given, rebuild site when updates occur.")
    private boolean watch;

    @Override
    public Integer call() {
        // Check that the given path is an existing directory
        File baseFile = basePath.toFile();
        if (!baseFile.isDirectory() || !baseFile.exists()) return 1;

        // Check that the build folder exists or try to create it
        File buildFile = new File(baseFile.getAbsolutePath() + File.separator + "build");
        if (!buildFile.exists()) {
            if (!buildFile.mkdir()) return 1;
        } else {
            if (!buildFile.isDirectory()) return 1;
        }

        // Starts the recursive building of the folder
        try {
            new SiteBuilder(basePath).build(null);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        if (watch) {
            System.out.println("Entering watch mode...");
            try {
                RecursiveFileWatcher watcher = new RecursiveFileWatcher(basePath);
                RecursiveFileWatcher.FileEvent fileEvent;
                while ((fileEvent = watcher.fetchEvent()) != null) {
                    System.out.println("Got event " + fileEvent.kind.name() + " for file " + fileEvent.path.toString());

                    // Update in the build directory are ignored
                    if (fileEvent.path.startsWith("build" + File.separator)) continue;

                    // Rebuild entire tree when updating config files
                    else if (fileEvent.path.endsWith("config.yaml") || fileEvent.path.endsWith("layout.html"))
                        new SiteBuilder(basePath).build(null);
                    else if (ENTRY_CREATE.equals(fileEvent.kind) || ENTRY_MODIFY.equals(fileEvent.kind))
                        new SiteBuilder(basePath).build(fileEvent.path.toFile());
                    else if (ENTRY_DELETE.equals(fileEvent.kind)) {
                        // TODO: delete old files
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }
}
