package se.payerl;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * Integrationstest som kör test-projektet med olika pom-konfigurationer
 * för att verifiera att DependencyOrderRule fungerar korrekt.
 * 
 * Test-pomarna finns nu i src/test/resources och kopieras till temporära kataloger för testning.
 */
public class IntegrationTest {

    private static final String TEST_RESOURCES_DIR = "src/test/resources";
    private static final String TEMP_TEST_DIR = "target/integration-test";
    private static final int TIMEOUT_SECONDS = 60;
    
    private File tempTestDirectory;
    private File testResourcesDirectory;
    
    @Before
    public void setUp() throws IOException {
        testResourcesDirectory = new File(TEST_RESOURCES_DIR);
        assertTrue("Test resources katalogen måste existera", testResourcesDirectory.exists());
        assertTrue("Test resources måste vara en katalog", testResourcesDirectory.isDirectory());
        
        // Skapa temporär test-katalog
        tempTestDirectory = new File(TEMP_TEST_DIR);
        if (tempTestDirectory.exists()) {
            deleteDirectory(tempTestDirectory);
        }
        assertTrue("Kunde inte skapa temporär test-katalog", tempTestDirectory.mkdirs());
    }
    
    @After
    public void tearDown() {
        // Rensa temporära filer
        if (tempTestDirectory != null && tempTestDirectory.exists()) {
            deleteDirectory(tempTestDirectory);
        }
    }

    @Test
    public void testCorrectOrderShouldSucceed() throws Exception {
        MavenResult result = runMavenValidate("pom-correct.xml");
        assertTrue("pom-correct.xml ska lyckas", result.isSuccess());
        assertFalse("Ska inte innehålla error-meddelanden", result.getOutput().contains("[ERROR]"));
    }

    @Test
    public void testSimpleOrderShouldSucceed() throws Exception {
        MavenResult result = runMavenValidate("pom-simple.xml");
        assertTrue("pom-simple.xml ska lyckas", result.isSuccess());
        assertFalse("Ska inte innehålla error-meddelanden", result.getOutput().contains("[ERROR]"));
    }

    @Test
    public void testWrongOrderShouldFail() throws Exception {
        MavenResult result = runMavenValidate("pom-wrong.xml");
        assertFalse("pom-wrong.xml ska misslyckas", result.isSuccess());
        assertTrue("Ska innehålla DependencyOrderRule error", 
                   result.getOutput().contains("se.payerl.DependencyOrderRule failed"));
        assertTrue("Ska visa vilket beroende som är fel placerat",
                   result.getOutput().contains("must be before"));
    }

    @Test
    public void testHierarchicalOrderShouldSucceed() throws Exception {
        MavenResult result = runMavenValidate("pom-hierarchical.xml");
        assertTrue("pom-hierarchical.xml ska lyckas", result.isSuccess());
        assertTrue("Ska visa gruppläge-meddelande",
                   result.getOutput().contains("Using group sorting mode"));
    }

    @Test
    public void testMultiScopeOrderShouldSucceed() throws Exception {
        MavenResult result = runMavenValidate("pom-multi-scope.xml");
        assertTrue("pom-multi-scope.xml ska lyckas", result.isSuccess());
        assertTrue("Ska visa multi-scope gruppläge-meddelande",
                   result.getOutput().contains("Using group sorting mode"));
    }

    @Test
    public void testTypeSafeOrderShouldSucceed() throws Exception {
        MavenResult result = runMavenValidate("pom-type-safe.xml");
        assertTrue("pom-type-safe.xml ska lyckas", result.isSuccess());
        assertFalse("Ska inte innehålla error-meddelanden", result.getOutput().contains("[ERROR]"));
    }

    /**
     * Kör Maven validate-kommando med specifik pom-fil
     */
    private MavenResult runMavenValidate(String pomFile) throws Exception {
        // Kopiera pom-fil från test resources till temporär katalog
        Path sourcePom = Paths.get(testResourcesDirectory.getPath(), pomFile);
        Path targetPom = Paths.get(tempTestDirectory.getPath(), "pom.xml");
        
        if (!Files.exists(sourcePom)) {
            throw new RuntimeException("Kunde inte hitta " + pomFile + " i " + testResourcesDirectory.getPath());
        }
        
        Files.copy(sourcePom, targetPom, StandardCopyOption.REPLACE_EXISTING);
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(tempTestDirectory);
        
        // Konfiguration för olika operativsystem
        String mvnCommand = isWindows() ? "mvn.cmd" : "mvn";
        // Använd validate för att köra den konfigurerade execution
        processBuilder.command(mvnCommand, "validate");
        
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Maven-kommando timeout efter " + TIMEOUT_SECONDS + " sekunder");
        }
        
        String output = readProcessOutput(process);
        int exitCode = process.exitValue();
        
        return new MavenResult(exitCode == 0, output, exitCode);
    }
    
    /**
     * Läser output från process
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        while ((bytesRead = process.getInputStream().read(buffer)) != -1) {
            output.append(new String(buffer, 0, bytesRead));
        }
        
        return output.toString();
    }
    
    /**
     * Kontrollerar om vi kör på Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Rekursivt tar bort katalog och allt innehåll
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Resultat från Maven-körning
     */
    private static class MavenResult {
        private final boolean success;
        private final String output;
        private final int exitCode;
        
        public MavenResult(boolean success, String output, int exitCode) {
            this.success = success;
            this.output = output;
            this.exitCode = exitCode;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getOutput() {
            return output;
        }
        
        public int getExitCode() {
            return exitCode;
        }
    }
} 