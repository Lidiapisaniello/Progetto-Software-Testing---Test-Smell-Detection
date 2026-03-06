import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

public class TestSubjectParser {

    /**
     * Test Case 1: Standard valid input with parentheses range.
     * Covers: getId, getTitle, getRangeString, getThisRange, getUpperRange.
     * Path: Valid parsing via StringTokenizer, valid '(' range detection.
     */
    @Test
    public void testValidSubjectWithParensRange() {
        String input = "101 Physics Report (1/5)";
        SubjectParser parser = new SubjectParser(input);

        // Test ID parsing
        assertEquals(101, parser.getId());

        // Test Range parsing (triggering messageParts via getThisRange/getUpperRange)
        // messageParts -> try block 1 (parentheses)
        assertEquals(1, parser.getThisRange());
        assertEquals(5, parser.getUpperRange());

        // Test String extraction
        // Note: getTitle() strips the ID and the detected range
        assertEquals("(1/5)", parser.getRangeString());
        assertEquals("Physics Report ", parser.getTitle());
    }

    /**
     * Test Case 2: Standard valid input with square brackets range.
     * Covers: messageParts (Catch Exception inte -> Try square brackets).
     */
    @Test
    public void testValidSubjectWithSquareBracketRange() {
        String input = "202 Chemistry Lab [2/10]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(202, parser.getId());
        
        // This triggers the catch block in messageParts for '(' and proceeds to '[' logic
        assertEquals(2, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
        
        assertEquals("[2/10]", parser.getRangeString());
        assertEquals("Chemistry Lab ", parser.getTitle());
    }

    /**
     * Test Case 3: Subject with no range information.
     * Covers: getTitle (FoundRange = false), getThisRange (returns default).
     */
    @Test
    public void testSubjectWithoutRange() {
        String input = "303 General Discussion";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(303, parser.getId());
        
        // Should return defaults (1, 1) as initialized in constructor
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
        
        assertEquals("General Discussion", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    /**
     * Test Case 4: Invalid ID format.
     * Covers: getId (catch Exception).
     */
    @Test
    public void testInvalidId() {
        String input = "NaN Invalid ID Subject";
        SubjectParser parser = new SubjectParser(input);

        // Long.parseLong will fail, catch block returns -1
        assertEquals(-1, parser.getId());
        assertEquals("Invalid ID Subject", parser.getTitle());
    }

    /**
     * Test Case 5: Malformed/Fake ranges (looks like range but contains invalid chars).
     * Covers: getTitle (backtracking loop logic when non-digit/non-slash found).
     */
    @Test
    public void testMalformedRangesInTitle() {
        // "(a/b)" contains letters, so getTitle should reject it as a range and keep it as title
        String input = "404 Error Log (a/b)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(404, parser.getId());
        // The potential range is treated as part of the title
        assertEquals("Error Log (a/b)", parser.getTitle());
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange());
    }

    /**
     * Test Case 6: Range syntax without slash.
     * Covers: getTitle (tmpbuf.toString().indexOf("/") check).
     */
    @Test
    public void testRangeWithoutSlash() {
        String input = "505 Notes (123)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(505, parser.getId());
        // No slash found, so not considered a RangeString
        assertEquals("Notes (123)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    /**
     * Test Case 7: Null Subject.
     * Covers: Constructor, getId (catch), getTitle (catch).
     */
    @Test
    public void testNullSubject() {
        SubjectParser parser = new SubjectParser(null);

        assertEquals(-1, parser.getId());
        assertNull(parser.getTitle()); // Throws NPE internally, caught, returns null
        assertNull(parser.getRangeString());
    }

    /**
     * Test Case 8: Empty Subject or Subject without spaces.
     * Covers: getTitle (substring logic).
     */
    @Test
    public void testSubjectNoSpace() {
        String input = "100";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(100, parser.getId());
        // getTitle does: Subject.substring(Subject.indexOf(" ") + 1...
        // indexOf(" ") is -1, +1 = 0. Substring(0) is "100".
        // Loop runs, no brackets. Returns "100".
        assertEquals("100", parser.getTitle()); 
    }

    /**
     * Test Case 9: Numeric Overflow in Range.
     * Covers: messageParts (inner catch blocks due to Integer.parseInt failure).
     * Since getTitle only checks for digits, a number too large for int passes getTitle
     * but fails inside messageParts.
     */
    @Test
    public void testRangeIntegerOverflow() {
        // 9999999999 is larger than Integer.MAX_VALUE
        String input = "606 Big Data (1/9999999999)"; 
        SubjectParser parser = new SubjectParser(input);

        assertEquals(606, parser.getId());
        
        // getTitle accepts digits/slashes, so RangeString is set
        assertEquals("(1/9999999999)", parser.getRangeString());
        
        // getThisRange calls messageParts. 
        // messageParts throws NumberFormatException inside, returns null.
        // getThisRange catches/handles null parts, returns current LowerRange (1).
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
    
    /**
     * Test Case 10: Mixed brackets nesting or sequence.
     * Ensures correct parsing when multiple bracket-like chars exist.
     */
    @Test
    public void testComplexTitleStructure() {
        String input = "707 Mixed (Note) [5/20]";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(707, parser.getId());
        assertEquals("[5/20]", parser.getRangeString());
        // The earlier (Note) is part of the title
        assertEquals("Mixed (Note) ", parser.getTitle());
        
        assertEquals(5, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }
    
    /**
     * Test Case 11: Valid Range format but logic execution flow for null checks.
     * Ensuring getRangeString calls getTitle if RangeString is null.
     */
    @Test
    public void testGetRangeStringLogic() {
        String input = "808 Laziness (2/4)";
        SubjectParser parser = new SubjectParser(input);
        
        // Calling getRangeString directly first (which internally calls getTitle)
        assertEquals("(2/4)", parser.getRangeString());
        
        // Verify state matches
        assertEquals(2, parser.getThisRange());
    }
}