/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Federica
Cognome: Musella
Username: federica.musella4@studenti.unina.it
UserID: 131
Date: 23/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

public class TestSubjectParser {

    // --- SETUP ---
    @BeforeClass public static void setUpClass() {}
    @AfterClass public static void tearDownClass() {}
    @Before public void setUp() {}
    @After public void tearDown() {}

    // =========================================================================
    // 1. STATE & CONSTRUCTOR (Robustezza e Weak Mutation)
    // =========================================================================

    @Test
    public void testInitialStateDefaults() {
        // Verifica che i default siano 1 e non 0.
        // Se un mutante rimuovesse l'inizializzazione nel costruttore, questo fallirebbe.
        SubjectParser parser = new SubjectParser("Text");
        assertEquals("Default LowerRange deve essere 1", 1, parser.getThisRange());
        assertEquals("Default UpperRange deve essere 1", 1, parser.getUpperRange());
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testNullSubjectConstructor() {
        // Copre la gestione safe (try-catch) quando subject è null
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
        assertNull(parser.getTitle());
        assertNull(parser.getRangeString());
    }

    // =========================================================================
    // 2. LOGIC & BRANCH COVERAGE (Obiettivo: 100% Branch Coverage)
    // =========================================================================

    /**
     * TEST CRITICO PER IL 100% BRANCH COVERAGE.
     * Copre il ramo 'else' implicito di 'if (parts != null)' in getUpperRange.
     */
    @Test
    public void testGetUpperRangeWithNullParts() throws Exception {
        SubjectParser parser = new SubjectParser("100 Test");
        // Forziamo messageParts a restituire null iniettando una stringa che fa fallire il parsing
        // ma viene catturata internamente ("(Fail)" causa NumberFormatException gestita).
        setPrivateField(parser, "RangeString", "(Fail)");
        
        // messageParts() torna null -> l'IF viene saltato -> Branch Coperto
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * TEST CRITICO PER IL 100% BRANCH COVERAGE.
     * Stesso concetto per getThisRange.
     */
    @Test
    public void testGetThisRangeWithNullParts() throws Exception {
        SubjectParser parser = new SubjectParser("100 Test");
        setPrivateField(parser, "RangeString", "(Fail)");
        assertEquals(1, parser.getThisRange());
    }

    // =========================================================================
    // 3. DEEP EXCEPTION HANDLING (Obiettivo: Max Instruction Coverage)
    // =========================================================================

    @Test
    public void testMessagePartsNestedExceptionLogic() throws Exception {
        SubjectParser parser = new SubjectParser("100 Test");

        // Scenario 1: Fallimento primo TRY (parentesi tonde) -> catch -> prova TRY secondario
        // "(A/B)" contiene lettere -> parseInt fallisce -> catch interno attivato
        setPrivateField(parser, "RangeString", "(A/B)");
        assertEquals(1, parser.getThisRange());

        // Scenario 2: Fallimento TRY secondario (parentesi quadre) -> catch interno -> return null
        // "100 [A/B]" -> parseInt fallisce nel blocco di fallback
        setPrivateField(parser, "RangeString", "100 [A/B]"); 
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testMessagePartsOuterException() {
        // Copre il catch(Exception) più esterno di messageParts (quello che stampa stacktrace)
        // Usiamo una classe anonima per causare NPE dentro messageParts
        SubjectParser parser = new SubjectParser("fail") {
            @Override
            public String getRangeString() {
                return null; // Ritorna null
            }
        };
        // messageParts riceve null -> mainrange.substring -> NPE -> Catch Esterno
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testGetRangeStringException() {
        // Copre il catch di getRangeString forzando un errore in getTitle
        SubjectParser parser = new SubjectParser("fail") {
            @Override
            public String getTitle() {
                throw new RuntimeException("Forced Crash");
            }
        };
        assertNull(parser.getRangeString());
    }

    // =========================================================================
    // 4. PARSING LOGIC & STRING MUTATION (Obiettivo: Weak Mutation)
    // =========================================================================

    @Test
    public void testGetIdValidAndInvalid() {
        // ID Valido
        SubjectParser parser = new SubjectParser("123456789 Oggetto");
        assertEquals(123456789L, parser.getId());
        
        // ID Invalido
        SubjectParser parserInvalid = new SubjectParser("NoNumber Oggetto");
        assertEquals(-1, parserInvalid.getId());
    }
    
    @Test
    public void testSubjectWithoutSpace() {
        // Copre il caso in cui non c'è separatore tra ID e Titolo
        // Verifica che substring non vada in errore
        SubjectParser parser = new SubjectParser("123NoSpace");
        assertEquals("123NoSpace", parser.getTitle());
    }

    @Test
    public void testRangeTypes() {
        // Test parentesi tonde standard
        SubjectParser parser = new SubjectParser("100 Title (1/5)");
        assertEquals(1, parser.getThisRange());
        assertEquals(5, parser.getUpperRange());
        assertEquals("(1/5)", parser.getRangeString());

        // Test parentesi quadre standard
        SubjectParser parser2 = new SubjectParser("100 Title [10/20]");
        assertEquals(10, parser2.getThisRange());
        assertEquals("[10/20]", parser2.getRangeString());
    }

    @Test
    public void testMultipleRangesLoop() {
        // Logica inversa del loop: deve prendere l'ultimo range e lasciare il primo nel titolo
        // Questo uccide i mutanti che cambiano la direzione del loop o il break condition
        SubjectParser parser = new SubjectParser("100 A (1/2) B (3/4)");
        assertEquals(3, parser.getThisRange());
        assertTrue("Il primo range deve restare nel titolo", parser.getTitle().contains("(1/2)"));
    }

    @Test
    public void testTitleWhitespacePreservation() {
        // Mutation check: verifica che non vengano mangiati spazi interni.
        // Input: "100" (ID) + " " (separatore consumato) + "  Titolo"
        SubjectParser parser = new SubjectParser("100   Titolo   Spaziato (1/2)");
        String title = parser.getTitle();
        // Ci aspettiamo 2 spazi iniziali perché il primo è stato usato come separatore ID/Titolo
        assertTrue("Deve preservare spazi interni", title.contains("  Titolo   Spaziato"));
    }

    @Test
    public void testEdgeCasesLoop() {
        // Caso: Parentesi valide ma senza slash (non è un range) -> if (indexOf("/") != -1)
        SubjectParser parser = new SubjectParser("100 Title (1)");
        assertFalse(parser.getTitle().contains("(1)")); 
        assertNull(parser.getRangeString());
        
        // Caso: Caratteri invalidi -> continue MAINLOOP
        SubjectParser parser2 = new SubjectParser("100 Title (a)");
        assertTrue(parser2.getTitle().contains("(a)"));
        
        // Caso: Parentesi sbilanciate -> Exception interna al loop while
        SubjectParser parser3 = new SubjectParser("100 123)");
        assertNull(parser3.getTitle());
    }
    
    @Test
    public void testRangeCaching() {
        // Copre if (RangeString == null) -> false (il valore è già calcolato)
        SubjectParser parser = new SubjectParser("100 (1/2)");
        parser.getRangeString(); // Prima chiamata (Calcola)
        assertEquals("(1/2)", parser.getRangeString()); // Seconda chiamata (Cache)
    }

    // =========================================================================
    // HELPER (Reflection)
    // =========================================================================

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}