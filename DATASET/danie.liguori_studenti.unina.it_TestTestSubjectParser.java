
/*
Nome: Daniele
Cognome: Liguori
Username: danie.liguori@studenti.unina.it
UserID: 139
Date: 18/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;A
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field; 
import java.util.NoSuchElementException; // Import necessario per NoSuchElementException

public class TestSubjectParser {

    private SubjectParser parser;

    // Stringhe di test per coprire tutti i percorsi e le eccezioni
    private static final String VALID_PARENTHESES = "123 Test Subject (1/10)";
    private static final String VALID_BRACKETS = "456 Altro Test [15/20]";
    private static final String NO_RANGE_PARTS = "789 Soggetto Senza Range";
    
    // Per testare NumberFormatException nel primo try di messageParts()
    private static final String INVALID_RANGE_FORMAT = "101 Invalid Range (A/B)";
    // Per testare NoSuchElementException nel primo try di messageParts()
    private static final String NO_RANGE_SEPARATOR = "102 Invalid Range (10)";
    // Per coprire la condizione !isDigit && != '/' in getTitle()
    private static final String NON_RANGE_PARENTHESES = "103 (Non Range) Subject";
    
    // Per testare NoSuchElementException in getId()
    private static final String NO_ID_SUBJECT = "Subject Senza ID Iniziale";
    // Per testare NumberFormatException in getId()
    private static final String NON_LONG_ID = "NonLong Subject (1/10)";
    

    @BeforeClass
    public static void setUpClass() {
        // Setup statico, se necessario
    }

    @AfterClass
    public static void tearDownClass() {
        // Pulizia statica, se necessario
    }

    @Before
    public void setUp() {
        // Inizializza con un soggetto valido per la maggior parte dei test
        parser = new SubjectParser(VALID_PARENTHESES);
    }

    @After
    public void tearDown() {
        parser = null;
    }

    // -------------------------------------------------------------------------
    // --- Metodi Helper (Reflection) ---
    // -------------------------------------------------------------------------
    
    /**
     * Helper per recuperare il valore di un campo privato (Reflection).
     */
    private Object getPrivateField(SubjectParser p, String fieldName) throws Exception {
        Field field = SubjectParser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(p);
    }
    
    /**
     * Helper per accedere al metodo privato messageParts() (Reflection).
     */
    private int[] callMessageParts(SubjectParser p) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        return (int[]) method.invoke(p);
    }
    
    /**
     * Helper per settare i campi privati (utile per indurre eccezioni) (Reflection).
     */
    private void setPrivateField(SubjectParser p, String fieldName, Object value) throws Exception {
        Field field = SubjectParser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(p, value);
    }


    // -------------------------------------------------------------------------
    // --- Test per getId() ---
    // -------------------------------------------------------------------------

    @Test
    public void testGetId_ValidLong() {
        parser = new SubjectParser(VALID_PARENTHESES);
        assertEquals(123L, parser.getId());
    }

    @Test
    public void testGetId_NoIdThrowsNoSuchElementException_ReturnsMinusOne() {
        parser = new SubjectParser(NO_ID_SUBJECT);
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_NonLongIdThrowsNumberFormatException_ReturnsMinusOne() {
        parser = new SubjectParser(NON_LONG_ID);
        assertEquals(-1L, parser.getId());
    }

    // -------------------------------------------------------------------------
    // --- Test per getTitle() ---
    // -------------------------------------------------------------------------

    @Test
    public void testGetTitle_ValidParentheses_RangeFound() {
        parser = new SubjectParser(VALID_PARENTHESES);
        assertEquals("Test Subject ", parser.getTitle());
    }

    @Test
    public void testGetTitle_ValidBrackets_RangeFound() {
        parser = new SubjectParser(VALID_BRACKETS);
        assertEquals("Altro Test ", parser.getTitle());
    }

    @Test
    public void testGetTitle_NoRange() {
        parser = new SubjectParser(NO_RANGE_PARTS);
        assertEquals("Soggetto Senza Range", parser.getTitle());
    }

    @Test
    public void testGetTitle_InvalidRangeFormat_CoversContinue() {
        parser = new SubjectParser(NON_RANGE_PARENTHESES);
        assertEquals("(Non Range) Subject", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_ThrowsException_ReturnsNull() throws Exception {
        parser = new SubjectParser(VALID_PARENTHESES);
        // Forza NullPointerException in Subject.substring(...) per colpire il catch
        setPrivateField(parser, "Subject", null);
        assertNull(parser.getTitle());
    }

    // -------------------------------------------------------------------------
    // --- Test per getRangeString() ---
    // -------------------------------------------------------------------------

    @Test
    public void testGetRangeString_CallGetTitle() {
        parser = new SubjectParser(VALID_PARENTHESES);
        // RangeString è inizialmente null, quindi chiama getTitle() per popolarlo
        assertEquals("(1/10)", parser.getRangeString());
    }
    
    // Test per coprire il catch (Exception e) in getRangeString()
    @Test
    public void testGetRangeString_CatchCoverage() throws Exception {
        parser = new SubjectParser(VALID_PARENTHESES);
        
        // 1. Settiamo RangeString a null (stato iniziale)
        setPrivateField(parser, "RangeString", null);
        
        // 2. Forza NullPointerException in getTitle() che viene chiamata internamente.
        setPrivateField(parser, "Subject", null);
        
        // La chiamata a getRangeString() fallirà in getTitle(), l'eccezione verrà catturata
        // dal catch (Exception e) di getRangeString() che ritorna null.
        assertNull(parser.getRangeString());
    }


    // -------------------------------------------------------------------------
    // --- Test per getThisRange() e getUpperRange() ---
    // -------------------------------------------------------------------------

    @Test
    public void testGetThisRange_Valid() {
        parser = new SubjectParser(VALID_PARENTHESES); // Range (1/10) -> Lower=1
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testGetUpperRange_Valid() {
        parser = new SubjectParser(VALID_BRACKETS); // Range [15/20] -> Upper=20
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testGetThisRange_NoRange_UsesDefault() {
        parser = new SubjectParser(NO_RANGE_PARTS);
        assertEquals(1, parser.getThisRange());
    }
    
    // Test per coprire il catch (Exception e) in getThisRange()
    @Test
    public void testGetThisRange_ForceCatchCoverage() throws Exception {
        parser = new SubjectParser(VALID_PARENTHESES);
        
        // 1. Settiamo un valore iniziale non standard per LowerRange
        setPrivateField(parser, "LowerRange", 999);
        int initialLowerRange = (int) getPrivateField(parser, "LowerRange");
        
        // 2. Forza una NullPointerException impostando Subject a null.
        setPrivateField(parser, "Subject", null);
        
        // 3. L'eccezione verrà catturata e LowerRange non verrà modificato.
        int result = parser.getThisRange();
        
        // 4. Verifica che LowerRange abbia mantenuto il suo valore iniziale (999).
        assertEquals(initialLowerRange, result);
        assertEquals(initialLowerRange, (int) getPrivateField(parser, "LowerRange"));
    }
    
    // Test per coprire il catch (Exception e) in getUpperRange()
    @Test
    public void testGetUpperRange_ForceCatchCoverage() throws Exception {
        parser = new SubjectParser(VALID_PARENTHESES);
        
        // 1. Settiamo un valore iniziale non standard per UpperRange
        setPrivateField(parser, "UpperRange", 888);
        int initialUpperRange = (int) getPrivateField(parser, "UpperRange");
        
        // 2. Forza una NullPointerException impostando Subject a null.
        setPrivateField(parser, "Subject", null);
        
        // 3. L'eccezione verrà catturata e UpperRange non verrà modificato.
        int result = parser.getUpperRange();
        
        // 4. Verifica che UpperRange abbia mantenuto il suo valore iniziale (888).
        assertEquals(initialUpperRange, result);
        assertEquals(initialUpperRange, (int) getPrivateField(parser, "UpperRange"));
    }


    // -------------------------------------------------------------------------
    // --- Test per messageParts() (Reflection) ---
    // -------------------------------------------------------------------------

    @Test
    public void testMessageParts_SuccessParentheses() throws Exception {
        parser = new SubjectParser(VALID_PARENTHESES);
        parser.getTitle(); // Popola RangeString
        int[] parts = callMessageParts(parser);
        assertArrayEquals(new int[]{1, 10}, parts);
    }
   
}