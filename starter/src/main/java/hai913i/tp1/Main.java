package hai913i.tp1;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;

import hai913i.tp1.metrics.MetricsCalculator;
import hai913i.tp1.model.MethodeInfo;
import hai913i.tp1.model.TypeInfo;
import hai913i.tp1.visitor.ASTStructurePrinterVisitor;
import hai913i.tp1.visitor.CallExtractorVisitor;
import hai913i.tp1.visitor.StructureExtractorVisitor;
import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET");
            System.exit(2);
        }
        Path project = Path.of(args[0]);
        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(3);
            return;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " "
                            + problem.getMessage());
                }
            }
        }
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);

        // --- TEST ÉTAPE A1 ---
        System.out.println("\n=== TEST A1 : Exploration AST ===");
        ASTStructurePrinterVisitor printerVisitor = new ASTStructurePrinterVisitor();

        for (ParsedFile file : files) {
            String fileName = file.path().getFileName().toString();

            if (fileName.equals("Dvd.java") || fileName.equals("Category.java")) {
                System.out.println("\n--- Arbre pour " + fileName + " ---");
                file.unit().accept(printerVisitor);
            }
        }

        // --- ÉTAPE A2 & B1 : EXTRACTION ET MODÈLE DE FAITS ---
        System.out.println("\n=== DÉBUT EXTRACTION STRUCTURE (A2 & B1) ===");

        StructureExtractorVisitor extractor = new StructureExtractorVisitor();

        for (ParsedFile file : files) {
            extractor.setCurrentUnit(file.unit());
            file.unit().accept(extractor);
        }

        System.out.println("\n=== BILAN POINT DE CONTRÔLE A2 ===");
        System.out.println("Nombre total de types    : " + extractor.typeCount + " (Classes: " + extractor.classCount + ", Interfaces: " + extractor.interfaceCount + ", Enums: " + extractor.enumCount + ")");
        System.out.println("Nombre total de méthodes : " + extractor.methodCount + " (dont constructeurs: " + extractor.constructorCount + ")");
        System.out.println("Nombre total d'attributs : " + extractor.fieldCount);

        // Récupération de notre modèle de faits (B1)
        List<TypeInfo> projectModel = extractor.extractedTypes;
        // Déplacer plus bas pour garder la cohérence avec les points de contrôle.
        // System.out.println("Taille du modèle de faits (B1) : " + projectModel.size() + " types chargés en mémoire.");

        // --- ÉTAPE A3 : EXTRACTION DES APPELS ---
        System.out.println("\n=== DÉBUT EXTRACTION DES APPELS (A3) ===");

        int totalCalls = 0;
        int totalMethodInvocations = 0;
        int totalSuperInvocations = 0;
        int totalInternalCalls = 0;
        int totalExternalCalls = 0;
        int totalUnresolvedCalls = 0;

        for (ParsedFile file : files) {
            CallExtractorVisitor callVisitor = new CallExtractorVisitor(file.unit());
            file.unit().accept(callVisitor);

            totalCalls += callVisitor.totalCalls;
            totalMethodInvocations += callVisitor.methodInvocationCount;
            totalSuperInvocations += callVisitor.superMethodInvocationCount;
            totalInternalCalls += callVisitor.internalCalls;
            totalExternalCalls += callVisitor.externalCalls;
            totalUnresolvedCalls += callVisitor.unresolvedCalls;
        }

        System.out.println("\n=== BILAN POINT DE CONTRÔLE A3 ===");
        System.out.println("Nombre total d'appels      : " + totalCalls + " (" + totalMethodInvocations + " MethodInvocations, " + totalSuperInvocations + " SuperMethodInvocations)");
        System.out.println("Appels internes au projet  : " + totalInternalCalls);
        System.out.println("Appels externes (JDK/lib)  : " + totalExternalCalls);
        System.out.println("Appels non résolus         : " + totalUnresolvedCalls);

        System.out.println("\n=== BILAN POINT DE CONTRÔLE B1 ===");
        System.out.println("Taille du modèle de faits : " + projectModel.size() + " types chargés en mémoire.");

        // Calcul des lignes de code totales (LOC)
        int totalLinesOfCode = 0;
        for (ParsedFile file : files) {
            // Calcul rapide du nombre de lignes du fichier source
            totalLinesOfCode += java.nio.file.Files.readAllLines(file.path()).size();
        }

// --- ÉTAPE B2 : CALCUL DES MÉTRIQUES SUR LE MODÈLE (B1) ---
        System.out.println("\n=== BILAN DES MÉTRIQUES (B2) ===");

        // Calcul des lignes de code réelles de l'application (LOC)
        int totalAppLinesOfCode = 0;
        for (ParsedFile file : files) {
            totalAppLinesOfCode += Files.readAllLines(file.path()).size();
        }

        MetricsCalculator metrics = new MetricsCalculator(projectModel, totalAppLinesOfCode);

        // 1 à 7 : Métriques de base
        System.out.println("1. Nombre de classes                : " + metrics.getClassCount());
        System.out.println("2. Nombre de lignes de code (LOC)   : " + metrics.getTotalAppLinesOfCode());
        System.out.println("3. Nombre total de méthodes         : " + metrics.getTotalMethods());
        System.out.println("4. Nombre total de paquetages       : " + metrics.getTotalPackages());
        System.out.println("5. Moyenne méthodes / classe        : " + String.format("%.2f", metrics.getAverageMethodsPerClass()));
        System.out.println("6. Moyenne LOC / méthode (avec corps): " + String.format("%.2f", metrics.getAverageLinesOfCodePerMethodWithBody()));
        System.out.println("7. Moyenne attributs / classe       : " + String.format("%.2f", metrics.getAverageFieldsPerClass()));

        // 8. Top 10% classes avec le plus de méthodes
        System.out.println("\n8. Top 10% des classes (méthodes) :");
        for (TypeInfo type : metrics.getTop10PercentClassesByMethods()) {
            System.out.println("   - " + type.qualifiedName() + " (" + type.methods().size() + " méthodes)");
        }

        // 9. Top 10% classes avec le plus d'attributs
        System.out.println("\n9. Top 10% des classes (attributs) :");
        for (TypeInfo type : metrics.getTop10PercentClassesByFields()) {
            System.out.println("   - " + type.qualifiedName() + " (" + type.fields().size() + " attributs)");
        }

        // 10. Intersection des deux catégories
        System.out.println("\n10. Classes présentes dans les deux top 10% :");
        List<TypeInfo> both = metrics.getClassesInBothTop10Percent();
        if (both.isEmpty()) {
            System.out.println("   (Aucune classe dans les deux catégories)");
        } else {
            for (TypeInfo type : both) {
                System.out.println("   - " + type.qualifiedName());
            }
        }

        // 11. Classes avec plus de X méthodes (seuil passé en argument ou par défaut = 5)
        int thresholdX = 5;
        if (args.length >= 2) {
            try {
                thresholdX = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Seuil invalide, utilisation de la valeur par défaut (5).");
            }
        }
        System.out.println("\n11. Classes ayant plus de " + thresholdX + " méthodes :");
        for (TypeInfo type : metrics.getClassesWithMoreThanXMethods(thresholdX)) {
            System.out.println("   - " + type.qualifiedName() + " (" + type.methods().size() + " méthodes)");
        }

        // 12. Top 10% des méthodes par LOC pour chaque classe
        System.out.println("\n12. Top 10% des méthodes les plus longues par classe :");
        Map<String, List<MethodeInfo>> topMethodsPerClass = metrics.getTop10PercentMethodsByLocPerClass();
        for (String className : topMethodsPerClass.keySet()) {
            List<MethodeInfo> topM = topMethodsPerClass.get(className);
            if (!topM.isEmpty()) {
                System.out.println("   * " + className + " :");
                for (MethodeInfo m : topM) {
                    System.out.println("     - " + m.name() + " (" + m.linesOfCode() + " LOC)");
                }
            }
        }

        // 13. Paramètres maximaux
        System.out.println("\n13. Nombre maximal de paramètres : " + metrics.getMaxParametersCount());
        System.out.println("    Méthodes concernées :");
        for (String methodDesc : metrics.getMethodsWithMaxParameters()) {
            System.out.println("   - " + methodDesc);
        }
    }
}