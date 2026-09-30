package hai913i.tp1;

import java.nio.file.Path;
import java.util.List;

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
        System.out.println("Taille du modèle de faits (B1) : " + projectModel.size() + " types chargés en mémoire.");

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
    }
}