import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for SubjectParser.
 * This class aims for high line coverage by testing various valid and invalid input formats.
 */
public class TestSubjectParser {

    // Test constructor and default range values
    @Test
    public void testConstructorDefaults() {
        SubjectParser parser = new SubjectParser("123 Test");
        // Test default values before any parsing
        assertEquals("Default lower range should be 1", 1, parser.getThisRange());
        assertEquals("Default upper range should be 1", 1, parser.getUpperRange());
    }

    // --- getId() Tests ---

    @Test
    public void testGetId_Success() {
        SubjectParser parser = new SubjectParser("1234567890 Hello World");
        assertEquals("Should parse the leading ID", 1234567890L, parser.getId());
    }

    @Test
    public void testGetId_Failure_NotANumber() {
        SubjectParser parser = new SubjectParser("Hello 12345");
        assertEquals("Should return -1 if first token is not a number", -1L, parser.getId());
    }

    @Test
    public void testGetId_Failure_EmptyString() {
        SubjectParser parser = new SubjectParser("");
        assertEquals("Should return -1 for an empty string", -1L, parser.getId());
    }

    @Test
    public void testGetId_Failure_OnlySpaces() {
        SubjectParser parser = new SubjectParser("   ");
        assertEquals("Should return -1 for only spaces", -1L, parser.getId());
    }

    // --- getTitle() Tests ---

    @Test
    public void testGetTitle_Simple() {
        SubjectParser parser = new SubjectParser("1000 This is a simple title");
        assertEquals("Should return the full title", "This is a simple title", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithParenthesisRange() {
        SubjectParser parser = new SubjectParser("1001 Title with (1/5) range");
        // Note: The parser logic includes the space before the range
        assertEquals("Should return title, excluding range", "Title with ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithBracketRange() {
        SubjectParser parser = new SubjectParser("1002 Title with [2/10] range");
        assertEquals("Should return title, excluding range", "Title with ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithInvalidParenthesis_NotARange() {
        SubjectParser parser = new SubjectParser("1003 Title (with notes) (1/2)");
        assertEquals("Should treat (with notes) as part of title", "Title (with notes) ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithInvalidBracket_NotARange() {
        SubjectParser parser = new SubjectParser("1004 Title [important] [1/2]");
        assertEquals("Should treat [important] as part of title", "Title [important] ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithMixedValidInvalid() {
        SubjectParser parser = new SubjectParser("1005 Title (abc) [1/5]");
        assertEquals("Should find [1/5] and exclude it", "Title (abc) ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithMixedValidInvalid_Reversed() {
        SubjectParser parser = new SubjectParser("1006 Title [abc] (1/5)");
        assertEquals("Should find (1/5) and exclude it", "Title [abc] ", parser.getTitle());
    }

    @Test
    public void testGetTitle_Failure_NoSpace() {
        // This causes an exception in tmpSubject.substring()
        SubjectParser parser = new SubjectParser("12345");
        assertNull("Should return null on parsing exception", parser.getTitle());
    }

    // --- getRangeString() Tests ---

    @Test
    public void testGetRangeString_WithParenthesis() {
        SubjectParser parser = new SubjectParser("2000 Test (1/5)");
        assertEquals("Should find parenthesis range", "(1/5)", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_WithBracket() {
        SubjectParser parser = new SubjectParser("2001 Test [2/10]");
        assertEquals("Should find bracket range", "[2/10]", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_NoRange() {
        SubjectParser parser = new SubjectParser("2002 Test without range");
        assertNull("Should return null if no range is found", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_InvalidRange() {
        SubjectParser parser = new SubjectParser("2003 Test (invalid)");
        assertNull("Should return null if range is invalid", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_Memoization() {
        SubjectParser parser = new SubjectParser("2004 Test (3/3)");
        // First call populates RangeString via getTitle()
        assertEquals("First call should find range", "(3/3)", parser.getRangeString());
        // Second call should return the stored RangeString without calling getTitle()
        assertEquals("Second call should return cached range", "(3/3)", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_Failure_NoSpace() {
        // This covers the catch (Exception e) in getRangeString()
        SubjectParser parser = new SubjectParser("12345");
        assertNull("Should return null if getTitle() throws exception", parser.getRangeString());
    }

    // --- getThisRange() and getUpperRange() Tests ---

    @Test
    public void testRanges_WithParenthesis() {
        SubjectParser parser = new SubjectParser("3000 Range Test (3/7)");
        assertEquals("Should parse lower range from ()", 3, parser.getThisRange());
        assertEquals("Should parse upper range from ()", 7, parser.getUpperRange());
    }

    @Test
    public void testRanges_WithBracket() {
        SubjectParser parser = new SubjectParser("3001 Range Test [4/8]");
        assertEquals("Should parse lower range from []", 4, parser.getThisRange());
        assertEquals("Should parse upper range from []", 8, parser.getUpperRange());
    }

    @Test
    public void testRanges_NoRange() {
        SubjectParser parser = new SubjectParser("3002 No Range Test");
        // This tests the path where messageParts() returns null
        assertEquals("Should return default lower range", 1, parser.getThisRange());
        assertEquals("Should return default upper range", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_InvalidRange_ParenthesisLetter() {
        SubjectParser parser = new SubjectParser("3003 Invalid (1/b)");
        // This tests messageParts() -> catch(Exception inte) -> catch(Exception subE)
        assertEquals("Should return default lower on parse error", 1, parser.getThisRange());
        assertEquals("Should return default upper on parse error", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_InvalidRange_BracketLetter() {
        SubjectParser parser = new SubjectParser("3004 Invalid [a/10]");
        // This tests messageParts() -> catch(Exception inte) -> catch(Exception subE)
        assertEquals("Should return default lower on parse error", 1, parser.getThisRange());
        assertEquals("Should return default upper on parse error", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Malformed_NoSlashParen() {
        SubjectParser parser = new SubjectParser("3005 Malformed (5)");
        // This tests messageParts() -> catch(Exception inte) -> catch(Exception subE)
        assertEquals("Should return default lower on Tokenizer error", 1, parser.getThisRange());
        assertEquals("Should return default upper on Tokenizer error", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Malformed_NoSlashBracket() {
        SubjectParser parser = new SubjectParser("3006 Malformed [6]");
        // This tests messageParts() -> catch(Exception inte) -> catch(Exception subE)
        assertEquals("Should return default lower on Tokenizer error", 1, parser.getThisRange());
        assertEquals("Should return default upper on Tokenizer error", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_AfterGetTitle() {
        SubjectParser parser = new SubjectParser("3007 Test (5/10)");
        // Call getTitle() first to populate RangeString
        parser.getTitle();
        // Now get ranges, which uses the pre-populated RangeString
        assertEquals("Should get lower range after getTitle()", 5, parser.getThisRange());
        assertEquals("Should get upper range after getTitle()", 10, parser.getUpperRange());
    }

    @Test
    public void testRanges_EmptyString() {
        SubjectParser parser = new SubjectParser("");
        // Covers getRangeString() returning null, and messageParts() catching NPE
        assertEquals("Should return default lower for empty string", 1, parser.getThisRange());
        assertEquals("Should return default upper for empty string", 1, parser.getUpperRange());
    }

    @Test
    public void testRanges_WithOnlyId() {
        SubjectParser parser = new SubjectParser("12345");
        // Covers getRangeString() returning null, and messageParts() catching NPE
        assertEquals("Should return default lower for ID only", 1, parser.getThisRange());
        assertEquals("Should return default upper for ID only", 1, parser.getUpperRange());
    }
}