import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestSubjectParser {

    @Test
    public void getIdValidIdTest() {
        SubjectParser parser = new SubjectParser("12345 Valid Subject");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void getIdInvalidNonNumericIdTest() {
        SubjectParser parser = new SubjectParser("ABC Valid Subject");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getTitleSimpleSubjectNoRangeTest() {
        SubjectParser parser = new SubjectParser("100 Simple Subject");
        String title = parser.getTitle();
        assertEquals("Simple Subject", title);
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleWithParensRangeTest() {
        SubjectParser parser = new SubjectParser("100 Subject (1/5)");
        String title = parser.getTitle();
        // The parser trims the range from the title but keeps the space if it was before the range
        // Logic: substring(index(" ") + 1) -> "Subject (1/5)". 
        // Loop removes "(1/5)". Remaining sb is "Subject ".
        assertEquals("Subject ", title);
        assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void getTitleWithBracketsRangeTest() {
        SubjectParser parser = new SubjectParser("100 Subject [10/20]");
        String title = parser.getTitle();
        assertEquals("Subject ", title);
        assertEquals("[10/20]", parser.getRangeString());
    }

    @Test
    public void getTitleWithFakeRangeNonDigitTest() {
        // 'A' is not a digit, so the parser should abort range extraction and treat it as part of title
        SubjectParser parser = new SubjectParser("100 Subject (1/A)");
        assertEquals("Subject (1/A)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleWithIncompleteRangeNoSlashTest() {
        // No slash '/', so it shouldn't be recognized as a valid range
        SubjectParser parser = new SubjectParser("100 Subject (123)");
        assertEquals("Subject (123)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    @Test
    public void getRangeStringDefaultBehaviorTest() {
        // If RangeString is null, it calls getTitle() internally
        SubjectParser parser = new SubjectParser("100 Subject (1/5)");
        assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void getRangeStringExceptionTest() {
        // We use a Spy to throw an exception when getTitle is called to test the catch block in getRangeString
        SubjectParser parser = new SubjectParser("100 Subject");
        SubjectParser spyParser = spy(parser);
        
        doThrow(new RuntimeException("Forced Error")).when(spyParser).getTitle();
        
        assertNull(spyParser.getRangeString());
    }

    @Test
    public void getThisRangeValidParensTest() {
        SubjectParser parser = new SubjectParser("100 Subject (5/10)");
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void getUpperRangeValidParensTest() {
        SubjectParser parser = new SubjectParser("100 Subject (5/10)");
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void getThisRangeValidBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Subject [20/30]");
        assertEquals(20, parser.getThisRange());
    }

    @Test
    public void getUpperRangeValidBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Subject [20/30]");
        assertEquals(30, parser.getUpperRange());
    }

    @Test
    public void messagePartsFallbackToBracketsTest() {
        // Scenario: logic tries parens first, fails, then tries brackets.
        // We spy getRangeString to return a string that fails the first parsing block (parens) 
        // but succeeds in the second block (brackets).
        // Format: "(invalid)[2/4]" 
        // 1. lastIndexOf("(") finds start. Parsing fails because of "invalid".
        // 2. Catch block triggers. Tries lastIndexOf("[") and succeeds.
        
        SubjectParser parser = new SubjectParser("dummy");
        SubjectParser spyParser = spy(parser);
        doReturn("(invalid)[2/4]").when(spyParser).getRangeString();

        assertEquals(2, spyParser.getThisRange());
        assertEquals(4, spyParser.getUpperRange());
    }

    @Test
    public void messagePartsFullFailureTest() {
        // Scenario: Both parens and brackets parsing fail.
        // Returns null, so ranges remain default (1).
        
        SubjectParser parser = new SubjectParser("dummy");
        SubjectParser spyParser = spy(parser);
        doReturn("(invalid)[invalid]").when(spyParser).getRangeString();

        assertEquals(1, spyParser.getThisRange());
    }

    @Test
    public void messagePartsNullRangeStringTest() {
        // Scenario: getRangeString returns null.
        // messageParts throws NPE on null.substring, caught by outer catch block.
        // Returns null. Range remains default.
        
        SubjectParser parser = new SubjectParser("100 No Range Subject");
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void messagePartsSubStringExceptionTest() {
        // Scenario: Trigger exception in the second try-catch block (brackets logic)
        // by having a bracket but invalid indices/content that wasn't caught by the first block.
        // getRangeString returns just "[", causes exception in substring/processing.
        
        SubjectParser parser = new SubjectParser("dummy");
        SubjectParser spyParser = spy(parser);
        doReturn("[").when(spyParser).getRangeString();
        
        // This should return default 1, verifying the 'catch (Exception subE)' block returns null
        assertEquals(1, spyParser.getThisRange());
    }
}