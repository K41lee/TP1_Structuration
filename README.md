# Analyseur Statique Java (HAI913I - TP1)

Outil en ligne de commande permettant de lire le code source d'un projet Java, d'en extraire la structure, de calculer des métriques et de construire son graphe d'appel.

## 1. Prérequis et Environnement
* **JDK :** Java 21
* **Outil de construction :** Apache Maven 3.6.9+
* **Dépendance principale :** Eclipse JDT Core `org.eclipse.jdt:org.eclipse.jdt.core:3.46.0`
* **Système d'exploitation :** Linux

## 2. Structure du projet
- `pom.xml` : Fichier de configuration Maven
- `src/main/java/hai913i/tp1/Main.java` : Point d'entrée de la ligne de commande
- `src/main/java/hai913i/tp1/parse/` : Configuration d'ASTParser et lecture des fichiers sources
- `src/main/java/hai913i/tp1/visitors/` : Visiteurs d'AST (Eclipse JDT)
- `src/main/java/hai913i/tp1/model/` : Modèle de faits (classes, méthodes, attributs)

## 3. Construction
Pour compiler et générer l'archive JAR exécutable depuis une copie fraîche depuis le projet starter :

`mvn clean package`

L'exécutable produit se trouve dans `starter/target/hai913i-tp1-analyzer.jar`.

## 4. Exécution (Ligne de commande)
L'outil s'exécute via une interface en ligne de commande (CLI). La syntaxe exige le chemin du projet à analyser et l'action à réaliser. Le paramètre `X` pour le calcul des métriques est optionnel.

**Syntaxe :**
```bash
java -jar starter/target/hai913i-tp1-analyzer.jar <CHEMIN_DU_PROJET> <ACTION> [SEUIL_X]
```
Arguments :

    <CHEMIN_DU_PROJET> : Chemin (relatif ou absolu) vers le dossier du projet Java à analyser.
        

    <ACTION> : Spécifie le résultat attendu. Les valeurs possibles sont :

        structure : Affiche l'extraction de la structure (classes, interfaces, méthodes, attributs).

        appels : Affiche le bilan des appels de méthodes extraits.

        metrics : Calcule et affiche les métriques logicielles.

        graph : Construit le graphe d'appel et l'exporte au format DOT.

        all : Exécute et affiche l'ensemble des actions ci-dessus.

    [SEUIL_X] : (Optionnel) Un entier positif définissant le seuil pour la métrique 11 (classes ayant plus de X méthodes). Si non renseigné, la valeur par défaut est 5.

Exemples d'utilisation :

Extraire uniquement la structure d'un projet :
```bash
java -jar starter/target/hai913i-tp1-analyzer.jar validation/ structure
```

Calculer les métriques avec le seuil X par défaut (5) :
```bash
java -jar starter/target/hai913i-tp1-analyzer.jar validation/ metrics
```

Générer l'analyse complète (structure, appels, métriques et graphe d'appel) avec un seuil X fixé à 4 :
```bash
java -jar starter/target/hai913i-tp1-analyzer.jar validation/ all 4
```

Si le chemin vers votre projet contient des espaces, veillez à l'entourer de guillemets. 
```bash
java -jar starter/target/hai913i-tp1-analyzer.jar "chemin/avec espaces" all
```

## 5. Gestion des erreurs et Dépannage

L'outil a été conçu pour être tolérant et robuste face aux imprévus :

    Chemin invalide ou dossier sans code source : Si le chemin n'existe pas ou ne contient aucun fichier .java, l'outil s'arrête avec un code d'erreur clair indiquant que le répertoire est vide ou introuvable.

    Paramètre mal formé : Si une chaîne de caractères (ex: abc) est fournie au lieu d'un entier pour le SEUIL_X, l'outil affiche un message explicite sur l'utilisation attendue et s'arrête proprement.

    Erreurs de syntaxe dans le projet ciblé : Si certains fichiers Java analysés contiennent des erreurs de compilation (fichiers cassés ou incomplets), l'outil affiche un avertissement en console (avec la ligne concernée) mais poursuit l'extraction et l'analyse sur le reste du projet valide sans s'interrompre.

## 6. Visualisation du Graphe d'Appel (Graphviz)

Lors de l'exécution, l'outil génère un fichier textuel graph.dot. Vous pouvez le convertir en image vectorielle grâce à Graphviz :
```bash
dot -Tsvg graph.dot -o graphe_appels.svg
```

Installation sur Debian/Ubuntu : 
```bash
sudo apt install graphviz
```
