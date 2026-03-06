/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: valeriadefalco@studenti.unina.it
UserID: 121
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    private SubjectParser parser;

    @Before
    public void setUp() {
        // Il setup viene gestito nei singoli test per variare l'input del costruttore,
        // oppure inizializzato a null qui per pulizia.
        parser = null;
    }

    @After
    public void tearDown() {
        parser = null;
    }

    /**
     * Case 1: Standard input with valid ID and Round Brackets range (1/10).
     * Verify correct parsing of ID, LowerRange, UpperRange and Title.
     */
    @Test
    public void parseValidInputWithRoundBracketsTest() {
        parser = new SubjectParser("101 Maths (1/10)");
        
        long id = parser.getId();
        int low = parser.getThisRange();
        int high = parser.getUpperRange();
        String title = parser.getTitle(); // Expecting implicit trimming in logic or specific substring

        assertEquals("ID should be 101", 101, id);
        assertEquals("Lower range should be 1", 1, low);
        assertEquals("Upper range should be 10", 10, high);
        // La logica di getTitle rimuove il range ma lascia lo spazio prima di esso se non trimmato esplicitamente
        // "Maths " vs "Maths". Verifichiamo con trim() per sicurezza o accettiamo lo spazio trailing.
        assertNotNull("Title should not be null", title);
        assertEquals("Title extraction failed", "Maths", title.trim());
    }

    /**
     * Case 2: Standard input with valid ID and Square Brackets range [5/15].
     * This forces the code to go into the catch block of messageParts and try the second format.
     */
    @Test
    public void parseValidInputWithSquareBracketsTest() {
        parser = new SubjectParser("202 Science [5/15]");
        
        assertEquals("ID should be 202", 202, parser.getId());
        assertEquals("Lower range should be 5", 5, parser.getThisRange());
        assertEquals("Upper range should be 15", 15, parser.getUpperRange());
        assertEquals("Science", parser.getTitle().trim());
    }

    /**
     * Case 3: Input with ID but NO range.
     * Verify defaults are returned for ranges (1).
     */
    @Test
    public void parseInputWithoutRangeTest() {
        parser = new SubjectParser("303 History");
        
        assertEquals("ID should be 303", 303, parser.getId());
        assertEquals("Default Lower range should be 1", 1, parser.getThisRange());
        assertEquals("Default Upper range should be 1", 1, parser.getUpperRange());
        assertEquals("History", parser.getTitle().trim());
    }

    /**
     * Case 4: Input with non-numeric ID.
     * Verify getId returns -1.
     */
    @Test
    public void parseInvalidNonNumericIdTest() {
        parser = new SubjectParser("ABC Physics (1/5)");
        
        assertEquals("Should return -1 for invalid ID", -1, parser.getId());
        // Even if ID is invalid, range parsing might still work if called directly
        // based on implementation analysis
        assertEquals(1, parser.getThisRange()); 
        assertEquals(5, parser.getUpperRange());
    }

    /**
     * Case 5: Range string contains letters (e.g., 1/a).
     * The parser loop in getTitle should reject this as a range, keeping it in the title.
     * Ranges should remain defaults.
     */
    @Test
    public void parseMalformedRangeWithLettersTest() {
        parser = new SubjectParser("404 Art (1/a)");
        
        assertEquals(404, parser.getId());
        assertEquals("Range parsing should fail, returning default 1", 1, parser.getThisRange());
        assertEquals("Range parsing should fail, returning default 1", 1, parser.getUpperRange());
        // Since it wasn't detected as a range, it remains part of the title
        assertTrue("Title should contain the malformed range", parser.getTitle().contains("(1/a)"));
    }

    /**
     * Case 6: Range missing the slash separator.
     * Should fail parsing logic and default to 1.
     */
    @Test
    public void parseMalformedRangeMissingSlashTest() {
        parser = new SubjectParser("505 PE (10)");
        
        assertEquals(505, parser.getId());
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Case 7: Empty input string.
     * getId should return -1 safely.
     */
    @Test
    public void parseEmptyStringTest() {
        parser = new SubjectParser("");
        assertEquals(-1, parser.getId());
        assertEquals(1, parser.getThisRange());
    }

    /**
     * Case 8: Null input string.
     * Should handle exception internally and return error codes.
     */
    @Test
    public void parseNullStringTest() {
        parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
        assertEquals(1, parser.getThisRange());
        assertNull(parser.getTitle());
    }

    /**
     * Case 9: Input with ID only (no title text, no range).
     */
    @Test
    public void parseIdOnlyTest() {
        parser = new SubjectParser("999");
        assertEquals(999, parser.getId());
        // getTitle logic might behave oddly if no space found, let's allow fail-safe check
        // Code analysis: tmpSubject = Subject.substring(Subject.indexOf(" ") + 1...)
        // If no space, indexOf returns -1, substring(0) takes whole string "999".
        String title = parser.getTitle();
        assertNotNull(title); 
    }

    /**
     * Case 10: Complex title with multiple spaces to ensure tokenizing logic 
     * doesn't break the title extraction.
     */
    @Test
    public void parseComplexTitleWithSpacesTest() {
        parser = new SubjectParser("1000 Complex Title Name [2/4]");
        
        assertEquals(1000, parser.getId());
        assertEquals(2, parser.getThisRange());
        assertEquals(4, parser.getUpperRange());
        assertEquals("Complex Title Name", parser.getTitle().trim());
    }
    
    /**
     * Case 11: Call getRangeString directly.
     * Verify it lazily loads title/range if not null.
     */
    @Test
    public void getRangeStringDirectlyTest() {
        parser = new SubjectParser("101 Maths (5/10)");
        String range = parser.getRangeString();
        // logic extracts inside of parens? No, looking at messageParts:
        // tmpRange = mainrange.substring(lastIndexOf("(")...)
        // getTitle constructs RangeString including parens: tmpbuf.insert(0, endchar);
        assertEquals("(5/10)", range);
    }
}