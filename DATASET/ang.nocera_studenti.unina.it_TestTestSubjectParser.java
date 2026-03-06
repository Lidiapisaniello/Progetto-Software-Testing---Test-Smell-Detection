/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Angelo
Cognome: Nocera
Username: ang.nocera@studenti.unina.it
UserID: 401
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    private SubjectParser sp;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Inizio Suite di Test");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("Fine Suite di Test");
    }

    @Before
    public void setUp() {
        sp = null;
    }

    @After
    public void tearDown() {
        sp = null;
    }

    /**
     * Test 1: Happy Path Standard (Parentesi Tonde).
     */
    @Test
    public void testStandardRoundBrackets() {
        sp = new SubjectParser("1001 Oggetto Standard (1/5)");

        assertEquals("L'ID deve essere parsato correttamente", 1001L, sp.getId());
        assertEquals("Il titolo deve essere pulito", "Oggetto Standard", sp.getTitle().trim());
        assertEquals("RangeString deve essere estratto", "(1/5)", sp.getRangeString());
        assertEquals("Lower range corretto", 1, sp.getThisRange());
        assertEquals("Upper range corretto", 5, sp.getUpperRange());
    }

    /**
     * Test 2: Happy Path Alternativo (Parentesi Quadre).
     */
    @Test
    public void testStandardSquareBrackets() {
        sp = new SubjectParser("2002 Oggetto Quadre [2/10]");

        assertEquals(2002L, sp.getId());
        assertEquals("Oggetto Quadre", sp.getTitle().trim());
        assertEquals("[2/10]", sp.getRangeString());
        assertEquals(2, sp.getThisRange());
        assertEquals(10, sp.getUpperRange());
    }

    /**
     * Test 3: Robustezza Input Null.
     */
    @Test
    public void testNullInput() {
        sp = new SubjectParser(null);

        assertEquals("Input null deve ritornare ID -1", -1L, sp.getId());
        assertNull("Input null deve ritornare Title null", sp.getTitle());
        assertNull("Input null deve ritornare RangeString null", sp.getRangeString());
        assertEquals(1, sp.getThisRange());
        assertEquals(1, sp.getUpperRange());
    }

    /**
     * Test 4: Robustezza Input Vuoto.
     */
    @Test
    public void testEmptyInput() {
        sp = new SubjectParser("");

        assertEquals("Input vuoto deve ritornare ID -1", -1L, sp.getId());
        assertEquals("Input vuoto deve ritornare titolo vuoto", "", sp.getTitle());
        assertNull(sp.getRangeString());
    }

    /**
     * Test 5: ID Non Numerico.
     */
    @Test
    public void testNonNumericId() {
        sp = new SubjectParser("ABC Titolo (1/5)");

        assertEquals("ID non numerico deve ritornare -1", -1L, sp.getId());
        assertEquals("Titolo", sp.getTitle().trim());
        assertEquals(1, sp.getThisRange());
    }

    /**
     * Test 6: Weak Mutation - Caratteri non validi nel "presunto" range.
     */
    @Test
    public void testInvalidCharactersInRange() {
        sp = new SubjectParser("3003 Titolo (A/B)");

        // (A/B) contiene lettere, quindi viene considerato parte del titolo
        assertEquals("Titolo (A/B)", sp.getTitle().trim());
        assertNull("RangeString deve essere null", sp.getRangeString());
        assertEquals(1, sp.getThisRange()); 
    }

    /**
     * Test 7: Range senza Slash.
     * NOTA CRITICA: Il codice legacy ha un difetto. Rimuove il testo tra parentesi
     * se contiene numeri, anche se manca lo slash. Il test DEVE riflettere questo
     * comportamento per passare (Characterization Test).
     */
    @Test
    public void testRangeWithoutSlash() {
        sp = new SubjectParser("4004 Titolo (100)");

        // Correggiamo l'aspettativa: ci aspettiamo "Titolo", NON "Titolo (100)"
        assertEquals("Titolo", sp.getTitle().trim());
        assertNull(sp.getRangeString());
    }

    /**
     * Test 8: Exception Swallowing in messageParts (Overflow).
     */
    @Test
    public void testNumericOverflowInRange() {
        sp = new SubjectParser("5005 Titolo (1/9999999999)");

        assertEquals("(1/9999999999)", sp.getRangeString());
        assertEquals("Deve ritornare default 1 su errore parsing", 1, sp.getThisRange());
        assertEquals("Deve ritornare default 1 su errore parsing", 1, sp.getUpperRange());
    }

    /**
     * Test 9: Assenza di Range.
     */
    @Test
    public void testNoRangePresent() {
        sp = new SubjectParser("6006 Solo Titolo");

        assertEquals(6006L, sp.getId());
        assertEquals("Solo Titolo", sp.getTitle().trim());
        assertNull(sp.getRangeString());
    }

    /**
     * Test 10: Assenza di spazio dopo ID.
     */
    @Test
    public void testNoSpaceAfterId() {
        sp = new SubjectParser("SoloTesto");

        assertEquals(-1L, sp.getId()); 
        assertEquals("SoloTesto", sp.getTitle().trim());
    }
}