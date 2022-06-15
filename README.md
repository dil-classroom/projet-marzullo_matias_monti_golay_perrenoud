# Hyde - Générateur de site
  * [1. Procédure d'installation](#1-procédure-dinstallation)
  * [2. Commandes supportées](#2-commandes-supportées)
    + [hyde new \<path>](#hyde-new-path)
    + [hyde build \<path>](#hyde-build-path)
    + [hyde serve \<path>](#hyde-serve-path)
    + [hyde clean \<path>](#hyde-clean-path)
    + [hyde -v, hyde --version](#hyde--v-hyde---version)
  * [3. Cas d'utilisation typique](#3-cas-dutilisation-typique)
    + [Etape 1 : Créer le répertoire](#etape-1--créer-le-répertoire)
    + [Etape 2 : Rédiger le site](#etape-2--rédiger-le-site)
    + [Etape 3 : Générer le site](#etape-3--générer-le-site)

![GitHub release (latest by date)](https://img.shields.io/github/v/release/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud?style=flat-square)
![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud?include_prereleases&label=prerelease&style=flat-square)

![LGTM Grade](https://img.shields.io/lgtm/grade/java/github/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud?style=flat-square)
![GitHub](https://img.shields.io/github/license/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud?style=flat-square)

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud/Create%20release%20on%20Milestone%20closed?label=Build%20and%20release&style=flat-square)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud/Maven%20run%20tests?label=Maven%20tests&style=flat-square)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud/Spotless%20check?label=Spotless&style=flat-square)

# Manuel utilisateur

Ce manuel détaille l'utilisation complète du générateur de site statique Hyde.

## 1. Procédure d'installation

L'exécutable du générateur est disponible sur la page des releases : https://github.com/dil-classroom/projet-marzullo_matias_monti_golay_perrenoud/releases .
Vous pouvez ajouter l'exécutable à votre variable d'environnement `PATH` ou le lancer avec `./hyde`.  


## 2. Commandes supportées
Les commandes supportées sont les suivantes :

### hyde new \<path>
Crée un nouveau dossier dans le répertoire ```<path>``` contenant les fichiers nécessaires à la réalisation du site statique.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde build \<path>
Génère les fichiers HTML du site statique à partir des fichiers présents dans le répertoire ```<path>``` et les place dans un nouveau sous-dossier ```<path>/build```.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde serve \<path>
Permet de visualiser le site défini par les fichiers se trouvant dans le sous-dossier ```<path>/build```, ceux-ci étant générés par la commande ```build```.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde clean \<path>
Supprime le répertoire ```<path>/build``` ainsi que tous ses sous-dossiers.
Si le paramètre ```<path>``` n'est pas spécifié, le répertoire courant est utilisé.

### hyde -v, hyde --version
Affiche la version actuelle du générateur de site statique.

## 3. Cas d'utilisation typique

### Etape 1 : Créer le répertoire
Utiliser la commande ```hyde new <path>``` afin de créer le dossier et les fichiers du site statique.

### Etape 2 : Rédiger le site
Dans le nouveau répertoire créé se trouvent les fichiers d'exemple ```index.md```, qui contient une page d’accueil, et ```config.yaml```, qui contient les metadonnées du site. Modifier librement ces 2 fichiers afin de rédiger et configurer le site.

Le fichier ```index.md``` peut commencer par une section de métadonnées qui seront propre à la page. Pour cela il faut commencer le fichier par la ligne ```"---"``` et terminer le bloc de métadonnées par la ligne ```"..."```. La suite du fichier représente le contenu de la page du site qui sera converti en HTML automatiquement.

### Etape 3 : Générer le site
Utiliser la commande ```hyde build <path>``` afin de générer le site statique à partir des fichiers présents dans ```<path>```.
