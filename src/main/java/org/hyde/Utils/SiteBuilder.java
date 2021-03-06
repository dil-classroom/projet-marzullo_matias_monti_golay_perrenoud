/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class SiteBuilder {
    private final Path basePath;
    private static final List<String> excludedFiles = List.of("config.yaml", ".", "..");
    private static final List<String> excludedFilePatterns = List.of("\\._.*");
    private static final List<String> excludedFolders = List.of("build", "template", ".", "..");

    /**
     * @param basePath chemin vers le dossier racine du projet à générer, doit être un dossier existant
     */
    public SiteBuilder(Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Traite récursivement les dossiers et génère les fichiers
     *
     * @param file Chemin à traiter. Relatif à basePath (donné dans la ligne de commande)
     * @return 0 en cas de succès, 1 en cas d'erreur
     * @throws IOException Voir throws de la fonction buildMD
     */
    public Integer build(File file) throws IOException {
        if (file == null) file = new File("");

        // Path absolu du fichier
        File absFile = new File(basePath + File.separator + file);

        if (!absFile.isDirectory()) { // Ignore les fichiers à ignorer
            if (excludedFiles.contains(file.getName())) return 0;

            for (String patt : excludedFilePatterns) if (file.getName().matches(patt)) return 0;
        } else if (excludedFolders.contains(file.getName())) // Ignore les dossiers à ignorer
        return 0;

        // Créé le path où build
        File absBuildFile = new File(basePath + File.separator + "build" + File.separator + file);

        if (absFile.isDirectory()) { // Si c'est un dossier
            System.out.println("Handling folder '" + file + "'");

            // Créer le sous-dossier dans build
            if (!absBuildFile.exists() && !absBuildFile.mkdirs()) {
                System.err.println("Can't create build folders for '" + absBuildFile + "' !");
                return 1;
            }

            // Liste le contenu du dossier et rappelle build récursivement
            int retour = 0;
            for (String subfile : Objects.requireNonNull(absFile.list())) {
                if (build(new File(file + File.separator + subfile)) != 0) retour = 1;
            }
            return retour;
        } else { // Si c'est un fichier
            System.out.println("Building file '" + file + "'");

            // Vérifier que les dossiers parents existent, sinon les créer
            if (!absBuildFile.getParentFile().exists()
                    && !absBuildFile.getParentFile().mkdirs()) {
                System.err.println("Can't create build folders '" + absBuildFile.getParentFile() + "' !");
                return 1;
            }

            if (file.getName().endsWith(".md")) { // Si c'est un fichier MD
                // Génère le contenu à partir d'un fichier MD
                return buildMD(file);
            } else { // Pour tous les autres fichiers
                // Copie le fichier dans build
                try {
                    Files.copy(absFile.toPath(), absBuildFile.toPath());
                } catch (IOException e) {
                    System.err.println("Can't copy file '" + file + "'");
                    return 1;
                }

                return 0;
            }
        }
    }

    /**
     * Charge la configuration globale
     *
     * @return retourne la configuration chargée
     * @throws IOException Si le fichier de configuration n'est pas lisible
     */
    private HashMap<String, String> getConfig() throws IOException {
        var config = new HashMap<String, String>();

        // Ouvre le fichier config.yaml et vérifie qu'il existe
        File configFile = new File(basePath + File.separator + "config.yaml");
        if (!configFile.exists()) {
            return config;
        }

        // Lis le fichier ligne par ligne
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Transforme la ligne en clé:valeur
                var key_value = lineToConfig(line);

                // Si la ligne ne contient pas de headers, continue
                if (key_value == null) continue;

                // Ajoute la valeur à la configuration chargée
                config.put(key_value[0], key_value[1]);
            }
        }

        return config;
    }

    /**
     * Traite une ligne de texte qui devrait contenir une clé:valeur
     *
     * @param line Ligne à traiter
     * @return clé:valeur ou null la ligne est invalide ou à ignorer
     */
    private String[] lineToConfig(String line) {
        // Si la ligne est vide, ignore
        if (line.isEmpty()) return null;

        // Ignore les commentaires
        if (line.startsWith("#")) return null;

        // Assure que la ligne contient un :
        if (!line.contains(":")) {
            System.err.println("Invalid line in config : '" + line + "'");
            return null;
        }

        // Split la ligne par le ":" pour séparer clé et valeur
        String[] key_value = line.split(":", 2);

        // Retire un éventuel espace de fin sur la clé
        if (key_value[0].endsWith(" ")) key_value[0] = key_value[0].substring(0, key_value[0].length() - 1);

        // Retire un éventuel espace de départ sur la valeur
        if (key_value[1].startsWith(" ")) key_value[1] = key_value[1].substring(1);

        // Retourne la clé et la valeur
        return key_value;
    }

    /**
     * Génère la page HTML à partir d'un MD avec le fichier de template
     *
     * @param file chemin relatif à build
     * @return 0 en cas de succès, 1 en cas d'erreur
     * @throws IOException Venant de la fonction getConfig
     */
    private Integer buildMD(File file) throws IOException {
        // Récupère la configuration globale du projet
        var globalConfig = getConfig();

        // Récupère la configuration locale du fichier
        HashMap<String, String> localConfig = getLocalConfig(file);

        // Si getLocalConfig retourne 1, c'est que le fichier est malformé et il faut l'ignorer !
        if (localConfig == null) return 1;

        // Lecture et traitement du md
        String HTML_content;
        try (BufferedReader reader = new BufferedReader(new FileReader(basePath + File.separator + file))) {
            // Lit le md dans un String en retirant les headers de configuration
            if (!localConfig.isEmpty()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("...")) {
                        break;
                    }
                }
            }

            // Transforme le contenu du MD en HTML
            Parser parser = Parser.builder().build();
            Node document = parser.parseReader(reader);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            HTML_content = renderer.render(document);
        } catch (IOException e) {
            System.err.println("Can't read file '" + file + "' !");
            return 1;
        }

        // Si template.html existe, lit le fichier et insère le contenu
        File templateFile = new File(basePath + File.separator + "template" + File.separator + "template.html");
        if (templateFile.exists()) {
            StringBuilder sb = new StringBuilder();
            boolean gotContent = false;

            // Lit le fichier de template
            // Insère le contenu à sa place
            try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("[[ content ]]")) {
                        line = line.replace("[[ content ]]", HTML_content);
                        gotContent = true;
                    }

                    sb.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException e) {
                System.err.println("Can't read template file !");
                return 1;
            }

            // Si le fichier template ne contient pas le tag "content", impossible de générer le
            // site !
            if (!gotContent)
                throw new RuntimeException(
                        "Template file doesn't contain a '[[ content ]]' tag ! Can't build the" + " site.");

            HTML_content = sb.toString();
        }

        // File inclusion
        HTML_content = fileInclusion(HTML_content);
        if (HTML_content == null) return 1;

        // Remplacement de variable
        HTML_content = varReplacement(HTML_content, localConfig, globalConfig);

        // Écrit le contenu généré dans le fichier de destination
        File htmlFile = new File(file.toString().substring(0, file.toString().length() - 2) + "html");
        File outputAbsFile = new File(basePath + File.separator + "build" + File.separator + htmlFile);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputAbsFile))) {
            writer.write(HTML_content);
        } catch (IOException e) {
            System.err.println("Can't write file '" + htmlFile + "'");
            return 1;
        }

        return 0;
    }

    /**
     * Gère l'inclusion de fichier dans un contenu HTML
     *
     * @param data Contenu HTML à traiter
     * @return Nouveau contenu HTML avec les inclusions ou null si une erreur mineure est survenue
     */
    private String fileInclusion(String data) {
        String patt = "\\[\\[\\+ '(.+\\.html)' ]]";
        Pattern pattern = Pattern.compile(patt);
        Matcher matcher = pattern.matcher(data);

        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            // Charge le fichier dont le nom est dans matcher.group(1)
            File repFile = new File(basePath + File.separator + matcher.group(1));
            StringBuilder fileContent = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new FileReader(repFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append(System.getProperty("line.separator"));
                }
            } catch (IOException e) {
                System.err.println("Can't include file '" + repFile + "'");
                return null;
            }

            // Remplace le contenu
            matcher.appendReplacement(sb, fileContent.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Remplace les variables dans un contenu HMTL
     *
     * @param data Le contenu HMTL à traiter
     * @param local Les variables locales au fichier
     * @param global Les variables globales au site
     * @return Le contenu HMTL traité
     */
    private String varReplacement(String data, HashMap<String, String> local, HashMap<String, String> global) {
        String patt = "\\[\\[ (page|config).(\\S+) ]]";
        Pattern pattern = Pattern.compile(patt);
        Matcher matcher = pattern.matcher(data);

        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            if (matcher.group(1).equals("page")) {
                matcher.appendReplacement(
                        sb,
                        local.getOrDefault(
                                matcher.group(2), "MISSING KEY '" + matcher.group(1) + "." + matcher.group(2) + "'"));
            } else {
                matcher.appendReplacement(
                        sb,
                        global.getOrDefault(
                                matcher.group(2), "MISSING KEY '" + matcher.group(1) + "." + matcher.group(2) + "'"));
            }
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Lit les headers de configuration d'un fichier .md
     *
     * @param file doit être un chemin relatif vers le fichier à lire
     * @return La configuration parsée ou null si une erreur mineure est survenue
     */
    private HashMap<String, String> getLocalConfig(File file) {
        var config = new HashMap<String, String>();

        try (BufferedReader reader = new BufferedReader(new FileReader(basePath + File.separator + file))) {
            // Si le fichier ne commence pas par le séparateur, il n'y a pas de config
            if (!reader.readLine().equals("---")) return config;

            // Tant qu'il y a des lignes à lire
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("...")) {
                    return config;
                }

                // Transforme la ligne en clé:valeur
                var key_value = lineToConfig(line);

                // Si la ligne ne contient pas de headers, continue
                if (key_value == null) continue;

                // Ajoute la valeur à la configuration chargée
                config.put(key_value[0], key_value[1]);
            }
        } catch (IOException e) {
            System.err.println("Can't read config on top of '" + file + "'");
            return null;
        }

        return null;
    }
}
