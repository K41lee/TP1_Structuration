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

## 4. Exécution
Lancez l'outil depuis la racine de votre projet d'analyseur en fournissant le chemin vers le projet Java cible :

`java -jar starter/target/hai913i-tp1-analyzer.jar <CHEMIN_DU_PROJET_A_ANALYSER>`

### Exemple :
`java -jar starter/target/hai913i-tp1-analyzer.jar ../resources/validation`