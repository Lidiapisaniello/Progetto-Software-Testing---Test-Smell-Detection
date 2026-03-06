/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Gemini"
Cognome: "AI"
Username: al.bonventre@studenti.unina.it
UserID: 183
Date: 25/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima dell'inizio dei test nella classe
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
    }

    @Before
    public void setUp() {
        // Eseguito prima di ogni metodo di test
    }

    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
    }

    // --- getId() Tests ---

    @Test
    public void testGetId_ValidId() {
        SubjectParser parser = new SubjectParser("123 Test Subject");
        assertEquals(123, parser.getId());
    }

    @Test
    public void testGetId_InvalidIdFormat() {
        SubjectParser parser = new SubjectParser("ABC Test Subject");
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testGetId_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testGetId_EmptySubject() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1, parser.getId());
    }

    // --- getTitle() Tests (Logic and Path Coverage) ---

    @Test
    public void testGetTitle_SimpleNoRange() {
        SubjectParser parser = new SubjectParser("123 Just A Title");
        assertEquals("Just A Title", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithParensRange() {
        SubjectParser parser = new SubjectParser("123 Title (1/5)");
        assertEquals("Title", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithBracketsRange() {
        SubjectParser parser = new SubjectParser("123 Title [1/5]");
        assertEquals("Title", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithInvalidCharInRange_Parens() {
        // Covers condition: (Character.isDigit(nextchar) == false) inside loop
        SubjectParser parser = new SubjectParser("123 Title (1/A)");
        assertEquals("Title (1/A)", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithInvalidCharInRange_Brackets() {
        // Covers alternate bracket path
        SubjectParser parser = new SubjectParser("123 Title [1/A]");
        assertEquals("Title [1/A]", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithNoSlash_Parens() {
        // Covers condition: tmpbuf.toString().indexOf("/") != -1
        SubjectParser parser = new SubjectParser("123 Title (12)");
        assertEquals("Title (12)", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithNoSlash_Brackets() {
        SubjectParser parser = new SubjectParser("123 Title [12]");
        assertEquals("Title [12]", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithSpaceInRange() {
        // Covers loop break on non-digit/non-slash
        SubjectParser parser = new SubjectParser("123 Title (1 / 2)");
        assertEquals("Title (1 / 2)", parser.getTitle());
    }

    @Test
    public void testGetTitle_MultipleRanges_LastOneValid() {
        // Covers FoundRange logic: parses last one, treats previous as text
        SubjectParser parser = new SubjectParser("123 Title (Old) (1/2)");
        assertEquals("Title (Old)", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    @Test
    public void testGetTitle_SubjectNoSpace() {
        // indexOf(" ") returns -1, handled by exception or logic
        SubjectParser parser = new SubjectParser("NoSpace");
        assertEquals("NoSpace", parser.getTitle());
    }

    // --- getRangeString() Tests ---

    @Test
    public void testGetRangeString_Parens() {
        SubjectParser parser = new SubjectParser("123 Title (1/2)");
        assertEquals("(1/2)", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_Brackets() {
        SubjectParser parser = new SubjectParser("123 Title [1/2]");
        assertEquals("[1/2]", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_None() {
        SubjectParser parser = new SubjectParser("123 Title");
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetRangeString_FromCache() {
        // Ensures second call returns cached RangeString without recounting
        SubjectParser parser = new SubjectParser("123 Title (1/2)");
        parser.getTitle(); // Populates RangeString
        assertEquals("(1/2)", parser.getRangeString());
    }
    
    @Test
    public void testGetRangeString_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    // --- getThisRange() and getUpperRange() Tests (covers messageParts) ---

    @Test
    public void testRanges_DefaultValues() {
        // Default constructor sets 1
        SubjectParser parser = new SubjectParser("123 Title");
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void testUpperRange_DefaultValues() {
        SubjectParser parser = new SubjectParser("123 Title");
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Parens_Lower() {
        SubjectParser parser = new SubjectParser("123 Title (5/10)");
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void testRanges_Parens_Upper() {
        SubjectParser parser = new SubjectParser("123 Title (5/10)");
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void testRanges_Brackets_Lower() {
        SubjectParser parser = new SubjectParser("123 Title [20/30]");
        assertEquals(20, parser.getThisRange());
    }

    @Test
    public void testRanges_Brackets_Upper() {
        SubjectParser parser = new SubjectParser("123 Title [20/30]");
        assertEquals(30, parser.getUpperRange());
    }

    @Test
    public void testRanges_MalformedParts_MissingNumber() {
        // ( /2) is technically a valid range string structure for getTitle, 
        // but messageParts will fail on Integer.parseInt("")
        SubjectParser parser = new SubjectParser("123 Title (/2)");
        // Should return default (1) because parsing fails inside messageParts
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testRanges_MalformedParts_MissingNumber_Upper() {
        SubjectParser parser = new SubjectParser("123 Title (/2)");
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void testRanges_NullSubject_HandledSafely() {
        SubjectParser parser = new SubjectParser(null);
        // messageParts catches Exception when accessing null range string
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void testMessageParts_BracketsFallback() {
        // This tests the catch block inside messageParts. 
        // Input has brackets. First try block looks for parens '(', fails, catches, tries brackets.
        SubjectParser parser = new SubjectParser("123 Title [10/20]");
        // Verify it successfully parses via the fallback block
        assertEquals(10, parser.getThisRange());
    }

    @Test
    public void testMessageParts_BothFail() {
        // Logic to force both inner try blocks in messageParts to fail but getTitle to succeed.
        // Case: "(/2)". 
        // 1. Try Parens: "(/2". split("/"). token1="(". substring(1) is "". parseInt error.
        // 2. Catch. Try Brackets. lastIndexOf("[") is -1. Exception.
        // 3. Catch. Return null.
        SubjectParser parser = new SubjectParser("123 Title (/2)");
        // LowerRange remains default
        assertEquals(1, parser.getThisRange());
    }
}