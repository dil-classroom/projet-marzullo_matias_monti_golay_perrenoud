# Manuel utilisateur

Ce manuel détaille l'utilisation complète du générateur de site statique Hyde.

## 1. Procédure d'installation



## 2. Commandes supportées

Les commandes suivantes sont supportées :

### hyde new \<path>
Crée un nouveau dossier dans le répertoire ```<path>``` contenant les fichiers nécessaires à la réalisation du site statique.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde build \<path> \<--watch>
Génère les fichiers HTML du site statique à partir des fichiers présents dans le répertoire ```<path>``` et les place dans un nouveau sous-dossier ```<path>/build```.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.
Le paramètre ```--watch``` indique au programme d'exécuter automatiquement cette commande lorsque une modifications est effectuée dans les fichiers sources du site.

### hyde serve \<path> \<--watch>
Permet de visualiser le site défini par les fichiers se trouvant dans le sous-dossier ```<path>/build```, ceux-ci étant générés par la commande ```build```.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.
Le paramètre ```--watch``` indique au programme d'exécuter automatiquement cette commande lorsque une modifications est effectuée dans les fichiers sources du site.

### hyde clean \<path>
Supprime le répertoire ```<path>/build``` ainsi que tous ses sous-dossiers.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde publish \<path>
Transfère les fichiers générés par la commande ```hyde build``` sur un serveur distant afin d'être accessibles depuis internet.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde -v, hyde --version
Affiche la version actuelle du générateur de site statique.

## 3. Cas d'utilisation typique

### Etape 1 : Créer le répertoire
Utiliser la commande ```hyde new <path>``` afin de créer le dossier et les fichiers du site statique.

### Etape 2 : Rédiger le site
Dans le nouveau répertoire créé se trouvent les fichiers d'exemple ```index.md```, qui contient une page d’accueil, et ```config.yaml```, qui contient des informations générales. Modifier librement ces 2 fichiers afin de rédiger et configurer le site.

Le fichier ```index.md``` peut commencer par une section de métadonnées. Pour cela il faut commencer le fichier par la ligne ```"---"``` et terminer le bloc de métadonnées par la ligne ```"..."```. La suite du fichier représente le contenu de la page du site qui sera converti en HTML automatiquement.

### Etape 3 : Générer le site
Utiliser la commande ```hyde build <path>``` afin de générer le site statique à partir des fichiers présents dans ```<path>```.

### Etape 4 : 
