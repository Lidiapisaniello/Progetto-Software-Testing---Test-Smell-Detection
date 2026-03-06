import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    @Test
    public void testIdValid() {
        SubjectParser sp = new SubjectParser("123 Test Subject");
        assertEquals(123, sp.getId());
    }

    @Test
    public void testIdInvalid() {
        SubjectParser sp = new SubjectParser("Invalid ID");
        assertEquals(-1, sp.getId());
    }

    @Test
    public void testIdEmpty() {
        SubjectParser sp = new SubjectParser("");
        assertEquals(-1, sp.getId());
    }

    @Test
    public void testIdNull() {
        SubjectParser sp = new SubjectParser(null);
        assertEquals(-1, sp.getId());
    }

    @Test
    public void testStandardParenthesesRange() {
        SubjectParser sp = new SubjectParser("100 Subject (10/20)");
        assertEquals("Subject ", sp.getTitle());
        assertEquals("(10/20)", sp.getRangeString());
        assertEquals(10, sp.getThisRange());
        assertEquals(20, sp.getUpperRange());
    }

    @Test
    public void testStandardSquareBracketsRange() {
        SubjectParser sp = new SubjectParser("200 Subject [30/40]");
        assertEquals("Subject ", sp.getTitle());
        assertEquals("[30/40]", sp.getRangeString());
        assertEquals(30, sp.getThisRange());
        assertEquals(40, sp.getUpperRange());
    }

    @Test
    public void testNoRange() {
        SubjectParser sp = new SubjectParser("300 Subject No Range");
        assertEquals("Subject No Range", sp.getTitle());
        assertNull(sp.getRangeString());
        assertEquals(1, sp.getThisRange()); // Defaults
        assertEquals(1, sp.getUpperRange()); // Defaults
    }

    @Test
    public void testMalformedRangeNoSlash() {
        SubjectParser sp = new SubjectParser("400 Subject (123)");
        // Missing slash means it's treated as part of the title
        assertEquals("Subject (123)", sp.getTitle());
        assertNull(sp.getRangeString());
    }

    @Test
    public void testMalformedRangeNonDigit() {
        SubjectParser sp = new SubjectParser("500 Subject (1/a)");
        // Non-digit character invalidates the range parsing in getTitle
        assertEquals("Subject (1/a)", sp.getTitle());
        assertNull(sp.getRangeString());
    }

    @Test
    public void testMissingOpenParenthesis() {
        SubjectParser sp = new SubjectParser("600 Subject 1/2)");
        // Finds closing ')' but runs out of bounds looking for '('
        // Causes IndexOutOfBoundsException in getTitle, caught, returns null
        assertNull(sp.getTitle());
        assertNull(sp.getRangeString());
    }

    @Test
    public void testOnlyClosingBracket() {
        SubjectParser sp = new SubjectParser("700 Subject ]");
        // Finds closing ']' but runs out of bounds looking for '['
        assertNull(sp.getTitle());
    }

    @Test
    public void testMixedBrackets() {
        SubjectParser sp = new SubjectParser("800 Subject (1/2]");
        // Ends with ']', looks for '[', finds '('. Mismatch.
        // Treated as text.
        assertEquals("Subject (1/2]", sp.getTitle());
        assertNull(sp.getRangeString());
    }

    @Test
    public void testMultipleRanges() {
        SubjectParser sp = new SubjectParser("900 Subject (1/2) (3/4)");
        // Parses from end. (3/4) is the range. (1/2) is title text.
        assertEquals("(3/4)", sp.getRangeString());
        assertEquals("Subject (1/2) ", sp.getTitle());
        assertEquals(3, sp.getThisRange());
        assertEquals(4, sp.getUpperRange());
    }

    @Test
    public void testRangeWithDoubleSlash() {
        SubjectParser sp = new SubjectParser("1000 Subject (1//2)");
        // getTitle allows slash. StringTokenizer splits 1 and 2.
        assertEquals("(1//2)", sp.getRangeString());
        assertEquals(1, sp.getThisRange());
        assertEquals(2, sp.getUpperRange());
    }

    @Test
    public void testRangeMessagePartsFailure() {
        SubjectParser sp = new SubjectParser("1100 Subject (1/)");
        // getTitle accepts it as a range string
        assertEquals("(1/)", sp.getRangeString());
        // messageParts fails to parse tokens (missing high), returns defaults
        assertEquals(1, sp.getThisRange());
        assertEquals(1, sp.getUpperRange());
    }

    @Test
    public void testSubjectNoSpace() {
        SubjectParser sp = new SubjectParser("NoSpace");
        assertEquals("NoSpace", sp.getTitle());
        assertNull(sp.getRangeString());
    }
    
    @Test
    public void testSubjectEmptyTitlePart() {
        SubjectParser sp = new SubjectParser("123 ");
        assertEquals("", sp.getTitle());
    }

    @Test
    public void testOpenBracketOnly() {
        SubjectParser sp = new SubjectParser("1300 Subject [");
        assertEquals("Subject [", sp.getTitle());
    }

    @Test
    public void testRangeStringDirectAccess() {
        // calling getRangeString triggers getTitle internally if null
        SubjectParser sp = new SubjectParser("1400 Subject (5/6)");
        assertEquals("(5/6)", sp.getRangeString());
    }

    @Test
    public void testGetRangesWhenNull() {
        // Ensure getThisRange/getUpperRange handle null return from messageParts gracefully
        SubjectParser sp = new SubjectParser("1500 Subject (1/)"); 
        // (1/) causes messageParts to return null
        assertEquals(1, sp.getThisRange());
        assertEquals(1, sp.getUpperRange());
    }
}