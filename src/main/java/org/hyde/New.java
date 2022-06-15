/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/** Crée un nouveau squelette de site avec du contenu par défaut. */
@Command(name = "new")
class New implements Callable<Integer> {

    /** Le chemin relatif vers le site à nettoyer. Par défaut, il s'agit du dossier courant. */
    @Parameters(arity = "0..1", paramLabel = "SITE", description = "The site to build")
    public String site = ".";

    /**
     * Méthode appelée automatiquement lors de l'invocation de "hyde new".
     *
     * @return 0 si tout s'est bien passé.
     */
    @Override
    public Integer call() {
        System.out.format("Creating new site... ");

        // Crée le(s) dossier(s) nécessaire(s) si le répertoire choisi n'existe pas
        File directory = new File(site);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Failed to create site directory.");
            return 1;
        }

        // Création du dossier racine
        if (createFolder(new File("")) != 0) return 1;

        // Création du dossier site/template
        if (createFolder(new File("template")) != 0) return 1;

        // Création et remplissage du fichier site/template/template.html
        List<String> template = Arrays.asList(
                "<html lang=\"en\">",
                "<head>",
                "   <meta charset=\"utf-8\">",
                "</head>",
                "<body>",
                "   [[+ 'template/menu.html' ]]",
                "   [[ content ]]",
                "</body>",
                "</html>");
        if (writeFile(new File("template" + File.separator + "template.html"), template) != 0) return 1;

        // Création et remplissage du fichier site/template/menu.html
        List<String> menu = Arrays.asList("<ul>", "<li>", "Accueil", "</li>", "</ul>");
        if (writeFile(new File("template" + File.separator + "menu.html"), menu) != 0) return 1;

        // Création et remplissage du fichier site/index.md
        List<String> index = Arrays.asList(
                "---",
                "titre: Mon premier article",
                "auteur: Bertil Chapuis",
                "date: 2021-03-10",
                "...",
                "# [[ config.titre ]]",
                "## [[ page.titre ]]",
                "### Mon sous-titre",
                "[[ page.auteur ]] - [[ page.date ]]",
                "Le contenu de mon article.",
                "[[ config.creator ]] is the best");
        if (writeFile(new File("index.md"), index) != 0) return 1;

        // Création et remplissage du fichier site/config.yaml
        List<String> config = Arrays.asList("titre: Mon premier site", "creator: John Doe");
        if (writeFile(new File("config.yaml"), config) != 0) return 1;

        System.out.println("Done.");
        return 0;
    }

    private Integer writeFile(File file, List<String> content) {
        File absFile = new File(site + File.separator + file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absFile))) {
            for (String line : content) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            System.err.println("Can't write file '" + file + "'");
            return 1;
        }

        return 0;
    }

    private Integer createFolder(File folder) {
        File absFolder = new File(site + File.separator + folder);

        if (!absFolder.exists() && !absFolder.mkdirs()) {
            System.err.println("Can't create folder '" + folder + "'");
            return 1;
        }

        return 0;
    }
}
