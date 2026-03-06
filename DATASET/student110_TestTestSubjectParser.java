import static org.junit.Assert.*;
import org.junit.Test;

public class TestSubjectParser {

    @Test
    public void testStandardParenthesesRange() {
        String input = "1 Subject Title (1/2)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("ID should be 1", 1, parser.getId());
        assertEquals("Title check", "Subject Title ", parser.getTitle()); 
        assertEquals("Range String check", "(1/2)", parser.getRangeString());
        assertEquals("Lower Range check", 1, parser.getThisRange());
        assertEquals("Upper Range check", 2, parser.getUpperRange());
    }

    @Test
    public void testStandardSquareBracketsRange() {
        String input = "200 Another Title [3/4]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(200, parser.getId());
        assertEquals("[3/4]", parser.getRangeString());
        assertEquals(3, parser.getThisRange());
        assertEquals(4, parser.getUpperRange());
        assertEquals("Another Title ", parser.getTitle());
    }

    @Test
    public void testNoRange() {
        String input = "300 Just A Title";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(300, parser.getId());
        assertEquals("Just A Title", parser.getTitle());
        assertNull("RangeString should be null", parser.getRangeString());
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testInvalidId() {
        String input = "NotANumber Title (1/2)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Should return -1 for invalid ID", -1, parser.getId());
        assertEquals("(1/2)", parser.getRangeString());
    }

    @Test
    public void testNullInput() {
        SubjectParser parser = new SubjectParser(null);

        assertEquals(-1, parser.getId());
        assertNull(parser.getTitle());
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void testFakeRangeWithLetters() {
        String input = "400 Title (a/b)";
        SubjectParser parser = new SubjectParser(input);
        assertEquals("Title (a/b)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    /**
     * CORRETTO: Il parser rileva che non è un range valido (trova uno spazio prima della parentesi aperta)
     * e quindi include "1/2)" nel titolo invece di andare in errore.
     */
    @Test
    public void testBrokenRangeParenthesis() {
        String input = "500 Title 1/2)"; 
        SubjectParser parser = new SubjectParser(input);

        // Asserzione corretta: il parsing tratta la parte finale come testo
        assertEquals("Title 1/2)", parser.getTitle());
        assertNull("Range should not be detected", parser.getRangeString());
    }

    @Test
    public void testTrickyRangeParsingFailure() {
        String input = "600 Title (/2)";
        SubjectParser parser = new SubjectParser(input);

        String range = parser.getRangeString();
        assertNotNull(range); 
        assertEquals("(/2)", range);

        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void testMixedBrackets() {
        String input = "700 Title (1/2]";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals("Title (1/2]", parser.getTitle());
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void testMultiSlashRange() {
        String input = "800 Title (1/2/3)";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals("(1/2/3)", parser.getRangeString());
        assertEquals(1, parser.getThisRange());
        assertEquals(2, parser.getUpperRange());
    }

    /**
     * CORRETTO: Java substring(0,0) ritorna stringa vuota, non lancia eccezione.
     * Quindi getTitle ritorna "" invece di null.
     */
    @Test
    public void testEmptyString() {
        SubjectParser parser = new SubjectParser("");
        
        // getId fallisce nel parsing del long e ritorna -1 (corretto)
        assertEquals(-1, parser.getId());
        
        // getTitle ritorna stringa vuota, non null
        assertEquals("", parser.getTitle()); 
    }
}
