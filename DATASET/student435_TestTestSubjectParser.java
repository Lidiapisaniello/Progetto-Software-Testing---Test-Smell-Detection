import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Tests for getId() ---

    @Test
    public void getIdValidSubjectTest() {
        SubjectParser parser = new SubjectParser("12345 Valid Subject");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void getIdInvalidSubjectTest() {
        SubjectParser parser = new SubjectParser("NoIdHere Subject");
        assertEquals(-1, parser.getId());
    }

    @Test
    public void getIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    // --- Tests for getThisRange() and getUpperRange() ---

    @Test
    public void getThisRangeWithParenthesesTest() {
        SubjectParser parser = new SubjectParser("123 Subject (10/20)");
        assertEquals(10, parser.getThisRange());
    }

    @Test
    public void getUpperRangeWithParenthesesTest() {
        SubjectParser parser = new SubjectParser("123 Subject (10/20)");
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void getThisRangeWithBracketsTest() {
        SubjectParser parser = new SubjectParser("123 Subject [5/15]");
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void getUpperRangeWithBracketsTest() {
        SubjectParser parser = new SubjectParser("123 Subject [5/15]");
        assertEquals(15, parser.getUpperRange());
    }

    @Test
    public void getThisRangeNoRangeTest() {
        // Should return default value 1
        SubjectParser parser = new SubjectParser("123 Subject Without Range");
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void getUpperRangeNoRangeTest() {
        // Should return default value 1
        SubjectParser parser = new SubjectParser("123 Subject Without Range");
        assertEquals(1, parser.getUpperRange());
    }

    // --- Tests for messageParts() internal logic and Exception Handling ---

    @Test
    public void getThisRangeMalformedRangeTest() {
        // Trigger NumberFormatException inside first try block of messageParts
        // Then triggers Exception inside nested try block (since '[' is missing)
        // returns null internally, getThisRange returns default 1
        SubjectParser parser = new SubjectParser("123 Subject (A/B)");
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void getThisRangeOuterExceptionTest() {
        // Use an anonymous subclass to force an exception in the outer scope of messageParts
        // This covers the catch(Exception e) block at the bottom of messageParts
        SubjectParser parser = new SubjectParser("123 Subject") {
            @Override
            public String getRangeString() {
                throw new RuntimeException("Forced Error");
            }
        };

        assertEquals(1, parser.getThisRange());
    }

    // --- Tests for getRangeString() ---

    @Test
    public void getRangeStringValidTest() {
        SubjectParser parser = new SubjectParser("123 Subject (1/2)");
        assertEquals("(1/2)", parser.getRangeString());
    }

    @Test
    public void getRangeStringExceptionTest() {
        // Passing null causes getTitle() to throw NullPointerException
        // getRangeString catches it and prints to stderr, returning null
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    // --- Tests for getTitle() ---

    @Test
    public void getTitleValidExtractionTest() {
        SubjectParser parser = new SubjectParser("123 My Title (1/2)");
        // Note: The logic leaves a trailing space if the range was at the end
        assertEquals("My Title ", parser.getTitle());
    }

    @Test
    public void getTitleWithBracketsTest() {
        SubjectParser parser = new SubjectParser("123 My Title [1/2]");
        assertEquals("My Title ", parser.getTitle());
    }

    @Test
    public void getTitleMultipleRangesTest() {
        // Should only pick up the last range, treating previous ones as part of the title
        SubjectParser parser = new SubjectParser("123 Title (1/2) (3/4)");
        assertEquals("Title (1/2) ", parser.getTitle());
        assertEquals("(3/4)", parser.getRangeString());
    }

    @Test
    public void getTitleBrokenRangeTest() {
        // The parser encounters 'a' (not digit/slash), so it aborts range parsing
        // and treats "(1/a)" as part of the title.
        SubjectParser parser = new SubjectParser("123 Title (1/a)");
        assertEquals("Title (1/a)", parser.getTitle());
    }

    @Test
    public void getTitleRangeWithoutSlashTest() {
        // The parser finds '(', loops back, but finds no '/'.
        // The code drops these characters and does not add them to the title.
        SubjectParser parser = new SubjectParser("123 Title (123)");
        assertEquals("Title ", parser.getTitle());
    }

    @Test
    public void getTitleExceptionTest() {
        // Null subject throws NPE in substring/indexOf
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }
}