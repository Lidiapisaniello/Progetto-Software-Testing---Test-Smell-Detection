import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Helper Methods to Simplify Testing ---

    private SubjectParser createParser(String subject) {
        return new SubjectParser(subject);
    }

    // --- Tests for Constructor and Initial State ---

    /**
     * Test the constructor's initial state for default LowerRange and UpperRange.
     */
    @Test
    public void testConstructorInitialState() {
        SubjectParser parser = createParser("123 My Test Subject");
        // Initial values are 1, but they are instance variables of the object.
        // We can only check their state after calling a method that would *try* to set them.
        // The getThisRange() and getUpperRange() methods call messageParts() which would
        // check the RangeString, but without a range in the subject, they will return 1.
        // The getters actually update the *instance variables* only if parts are found.
        // Let's rely on the direct getters for coverage purposes.
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    // --- Tests for getId() ---

    /**
     * Test a valid subject string with a numeric ID at the start.
     */
    @Test
    public void testGetIdValid() {
        SubjectParser parser = createParser("987654321 Some Subject Title");
        assertEquals(987654321L, parser.getId());
    }

    /**
     * Test a subject string that does not start with a number (throws NumberFormatException).
     * This hits the catch block for the general Exception.
     */
    @Test
    public void testGetIdInvalid() {
        SubjectParser parser = createParser("Title 123 With No Leading ID");
        assertEquals(-1L, parser.getId());
    }

    // --- Tests for getTitle() and getRangeString() (Interdependent) ---

    /**
     * Test subject with a valid range in parentheses, e.g., (1/10).
     */
    @Test
    public void testGetTitleWithParenthesesRange() {
        String subject = "123 Main Topic Title (1/10)";
        SubjectParser parser = createParser(subject);
        // getTitle() also sets RangeString internally, so we test both.
        assertEquals("Main Topic Title ", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());
    }

    /**
     * Test subject with a valid range in brackets, e.g., [1/10].
     */
    @Test
    public void testGetTitleWithBracketRange() {
        String subject = "123 Important Subject [1/10]";
        SubjectParser parser = createParser(subject);
        assertEquals("Important Subject ", parser.getTitle());
        assertEquals("[1/10]", parser.getRangeString());
    }

    /**
     * Test subject with no range at all.
     */
    @Test
    public void testGetTitleNoRange() {
        String subject = "456 Simple Subject Title";
        SubjectParser parser = createParser(subject);
        // The Title should be everything after the ID.
        // The getTitle loop will append all characters to sb.
        assertEquals("Simple Subject Title", parser.getTitle());
        // If no range is found, RangeString remains null.
        assertNull(parser.getRangeString());
    }

    /**
     * Test subject where the range format is broken (e.g., missing /), causing a range
     * extraction failure, thus treating the potential range as part of the title.
     * The while loop will break if a non-digit/non-slash is found, appending the partial
     * range to the title.
     */
    @Test
    public void testGetTitleBrokenRange() {
        // Subject ends with ')' but the range content is not 'x/y'.
        String subject = "123 Broken Range Title (abc)";
        SubjectParser parser = createParser(subject);
        // The 'continue MAINLOOP' branch is taken in getTitle.
        // getTitle extracts the full string after ID: 'Broken Range Title (abc)'
        assertEquals("Broken Range Title (abc)", parser.getTitle());
        assertNull(parser.getRangeString());
    }
    
    /**
     * Test the getRangeString() method when RangeString is already set.
     * Requires calling getTitle() once to set it.
     */
    @Test
    public void testGetRangeStringAlreadySet() {
        String subject = "789 Cached Range [2/5]";
        SubjectParser parser = createParser(subject);
        // First call to set the range string
        parser.getTitle();
        // Second call should return the cached value without calling getTitle again.
        assertEquals("[2/5]", parser.getRangeString());
    }

    // --- Tests for getThisRange() and getUpperRange() and messageParts() ---
    
    /**
     * Test messageParts(), getThisRange(), and getUpperRange() with a valid PARENTHESES range.
     * This covers the first 'try' block inside messageParts().
     * Range format: (1/10) -> Lower: 1, Upper: 10
     */
    @Test
    public void testGetRangesWithParenthesesRange() {
        String subject = "100 Parentheses Subject (1/10)";
        SubjectParser parser = createParser(subject);
        
        // Ensure range is available by calling getRangeString which calls getTitle()
        parser.getRangeString(); 
        
        // getThisRange() calls messageParts() and updates LowerRange
        assertEquals(1, parser.getThisRange()); 
        // getUpperRange() calls messageParts() and updates UpperRange
        assertEquals(10, parser.getUpperRange());
    }

    /**
     * Test messageParts(), getThisRange(), and getUpperRange() with a valid BRACKET range.
     * This covers the second 'try' block inside messageParts().
     * Range format: [5/20] -> Lower: 5, Upper: 20
     */
    @Test
    public void testGetRangesWithBracketRange() {
        String subject = "200 Bracket Subject [5/20]";
        SubjectParser parser = createParser(subject);
        
        // Ensure range is available by calling getRangeString
        parser.getRangeString(); 
        
        // getThisRange() calls messageParts() and updates LowerRange
        assertEquals(5, parser.getThisRange()); 
        // getUpperRange() calls messageParts() and updates UpperRange
        assertEquals(20, parser.getUpperRange());
    }
    
    /**
     * Test messageParts() when RangeString is null (no range found in subject).
     * This will cause a NullPointerException in messageParts() at mainrange.substring(...),
     * and the general catch block in messageParts() will be hit, returning null.
     * The getters will return the default 1.
     */
    @Test
    public void testGetRangesNoRangeString() {
        String subject = "300 No Range Subject";
        SubjectParser parser = createParser(subject);
        
        // getTitle() is called internally, which leaves RangeString as null.
        // messageParts() throws NullPointerException and returns null.
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Test messageParts() failure: Range uses parentheses but is malformed,
     * e.g., missing '/' or non-numeric content.
     * This will hit the inner catch block (Exception inte) inside messageParts().
     * Example malformation: (A/10) or (1-10)
     */
    @Test
    public void testGetRangesMalformedParenthesesRange() {
        // The range part in getTitle() is too permissive, so the RangeString will be set to (1-10).
        // The parsing in messageParts() will fail on Integer.parseInt(sLow.substring(1, sLow.length()));
        // because sLow.substring(1, sLow.length()) is "1-10" which is not parsable.
        String subject = "400 Malformed Range (1-10)";
        SubjectParser parser = createParser(subject);
        
        // getTitle() sets RangeString to "(1-10)".
        parser.getTitle();
        
        // messageParts() will attempt (1-10), fail in the first try block (inte), 
        // attempt [..] in the second try block, which fails (subE) and returns null.
        assertEquals(1, parser.getThisRange()); // Returns default 1 due to exception.
        assertEquals(1, parser.getUpperRange()); // Returns default 1 due to exception.
    }
    
    /**
     * Test messageParts() failure: Range uses brackets but is malformed.
     * This hits the innermost catch block (Exception subE) in messageParts().
     */
    @Test
    public void testGetRangesMalformedBracketRange() {
        // RangeString is set to [1_10]
        String subject = "500 Malformed Range [1_10]";
        SubjectParser parser = createParser(subject);
        
        parser.getTitle();
        
        // messageParts() will fail in the first try block (inte), 
        // attempt [1_10] in the second try block, which fails (subE) on parsing '/' and returns null.
        assertEquals(1, parser.getThisRange()); // Returns default 1 due to exception.
        assertEquals(1, parser.getUpperRange()); // Returns default 1 due to exception.
    }
}