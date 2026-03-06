import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    /**
     * Tests getId() when the Subject starts with a valid number.
     * Covers: Constructor, getId (success path).
     */
    @Test
    public void getIdValidNumberTest() {
        SubjectParser parser = new SubjectParser("439 UserID Subject");
        assertEquals(439, parser.getId());
    }

    /**
     * Tests getId() when the Subject does not start with a number.
     * Covers: getId (exception catch block).
     */
    @Test
    public void getIdInvalidNumberTest() {
        SubjectParser parser = new SubjectParser("UserID 439 Subject");
        assertEquals(-1, parser.getId());
    }

    /**
     * Tests getId() when the Subject is null.
     * Covers: getId (exception catch block due to NPE).
     */
    @Test
    public void getIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    /**
     * Tests getTitle() and getRangeString() with a standard parenthesized range.
     * Logic: (10/20) is parsed, removed from title, and set as range.
     */
    @Test
    public void getTitleWithParenthesesRangeTest() {
        SubjectParser parser = new SubjectParser("100 My Subject (10/20)");
        
        // Verifies title extracts text and excludes the range
        assertEquals("My Subject ", parser.getTitle());
        // Verifies range string is captured
        assertEquals("(10/20)", parser.getRangeString());
    }

    /**
     * Tests getTitle() and getRangeString() with a square bracket range.
     * Logic: [10/20] is parsed, removed from title, and set as range.
     */
    @Test
    public void getTitleWithSquareBracketsRangeTest() {
        SubjectParser parser = new SubjectParser("100 My Subject [10/20]");
        
        assertEquals("My Subject ", parser.getTitle());
        assertEquals("[10/20]", parser.getRangeString());
    }

    /**
     * Tests getTitle() where parens contain text (not digits/slash).
     * Logic: Loop enters detection, finds 'A', aborts range detection, treats as title text.
     * Covers: getTitle loop break condition (Character.isDigit == false).
     */
    @Test
    public void getTitleWithFakeRangeLettersTest() {
        SubjectParser parser = new SubjectParser("100 Subject (ABC)");
        
        // (ABC) should remain part of the title
        assertEquals("Subject (ABC)", parser.getTitle());
        // RangeString should remain null (returns null on get)
        assertNull(parser.getRangeString());
    }

    /**
     * Tests getTitle() where parens contain digits but no slash.
     * Logic: Loop parses (123), finishes loop, checks indexOf("/"). Returns -1. 
     * Note: Based on code logic, if it looks like a range but has no slash, it isn't added to SB.
     * Covers: getTitle (tmpbuf.toString().indexOf("/") == -1 branch).
     */
    @Test
    public void getTitleWithNumericNoSlashTest() {
        SubjectParser parser = new SubjectParser("100 Subject (123)");
        
        // The parser logic swallows (123) because it matches digit criteria but fails slash check
        assertEquals("Subject ", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    /**
     * Tests getTitle() with multiple potential ranges.
     * Logic: Iterates backwards. Finds (2/2) -> Range. Sets FoundRange=true.
     * Then encounters (1/1). FoundRange is true, so treats (1/1) as title text.
     * Covers: getTitle (FoundRange == true branch).
     */
    @Test
    public void getTitleWithMultipleRangesTest() {
        SubjectParser parser = new SubjectParser("100 Subject (1/1) (2/2)");
        
        assertEquals("Subject (1/1) ", parser.getTitle());
        assertEquals("(2/2)", parser.getRangeString());
    }
    
    /**
     * Tests getTitle() with null subject to trigger exception.
     * Covers: getTitle (catch Exception).
     */
    @Test
    public void getTitleExceptionTest() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    /**
     * Tests getRangeString() directly when Title hasn't been called yet.
     * Covers: getRangeString lazy loading logic.
     */
    @Test
    public void getRangeStringLazyLoadTest() {
        SubjectParser parser = new SubjectParser("100 Subject (5/5)");
        // Calling getRangeString should internally trigger getTitle parsing
        assertEquals("(5/5)", parser.getRangeString());
    }
    
    /**
     * Tests getRangeString() handling exception.
     * Since getTitle catches its own exceptions, we simulate this path 
     * by creating a state where Subject is null.
     */
    @Test
    public void getRangeStringExceptionTest() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    /**
     * Tests getThisRange() and getUpperRange() with valid parentheses format.
     * Covers: messageParts (first try block success).
     */
    @Test
    public void getRangesParenthesesSuccessTest() {
        SubjectParser parser = new SubjectParser("100 S (5/10)");
        
        assertEquals(5, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }

    /**
     * Tests getThisRange() and getUpperRange() with valid square brackets format.
     * Covers: messageParts (outer catch -> inner try block success).
     */
    @Test
    public void getRangesBracketsSuccessTest() {
        SubjectParser parser = new SubjectParser("100 S [15/20]");
        
        assertEquals(15, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    /**
     * Tests messageParts() failure logic.
     * Input "(1/)" is identified as a RangeString by getTitle.
     * In messageParts:
     * 1. Outer try: parses "1/", st.nextToken() gets "1", next token fails. Exception caught.
     * 2. Inner try: looks for "[". Fails (-1). Substring exception. Caught.
     * 3. Returns null.
     * Covers: messageParts (nested exception handling).
     */
    @Test
    public void getRangesParsingFailureTest() {
        SubjectParser parser = new SubjectParser("100 S (1/)");
        
        // Parsing fails, returns defaults (1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Tests messageParts() when no range is present (RangeString is null).
     * Covers: messageParts (outer try fails immediately on substring null).
     */
    @Test
    public void getRangesNoRangePresentTest() {
        SubjectParser parser = new SubjectParser("100 Simple Subject");
        
        // getRangeString is null, messageParts throws NPE internally, caught, returns null.
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
    
    /**
     * Tests getThisRange/getUpperRange behavior when parsing yields null parts.
     * Covers: null checks in getThisRange/getUpperRange.
     */
    @Test
    public void getRangesNullPartsCheckTest() {
        SubjectParser parser = new SubjectParser("100 S");
        // Ensure we get defaults and no exceptions
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
}