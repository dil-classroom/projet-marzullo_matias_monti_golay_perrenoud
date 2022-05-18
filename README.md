# Manuel utilisateur

Ce manuel détaille l'utilisation complète du générateur de site statique Hyde.

### Etape 1 : Créer le répertoire
Utiliser la commande ```hyde new <path>``` afin de créer le dossier contenant les fichiers nécessaire dans le répertoire ```<path>```. Si ce paramètre n'est pas spécifié, le répertoire actuel sera utilisé.

### Etape 2 : Rédiger le site
Dans le nouveau répertoire créé se trouvent les fichiers ```index.md```, qui contient une page d’accueil d'exemple, et ```config.yaml```, qui contient des informations générales. Modifier librement ces 2 fichiers afin de rédiger et configurer le site.

Le fichier ```index.md``` peut commencer par une section de métadonnées. Pour cela il faut commencer le fichier par la ligne ```"---"``` et terminer le bloc de métadonnées par la ligne ```"..."```. La suite du fichier représente le contenu de la page du site qui sera converti en HTML automatiquement.

### Etape 3 : Générer le site
Utiliser la commande ```hyde build <path>``` afin de générer le site statique dans le répertoire ```<path>/build```à partir des fichiers présents dans ```<path>```.
