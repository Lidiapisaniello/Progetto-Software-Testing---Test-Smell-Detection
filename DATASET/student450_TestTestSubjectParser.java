import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestSubjectParser { // Classe rinominata per coincidere con il file TestSubjectParser.java

    // ---------------------------------------------------------
    // getId() Tests
    // ---------------------------------------------------------

    @Test
    public void getIdValidNumericStartTest() {
        String input = "12345 Some Title";
        SubjectParser parser = new SubjectParser(input);
        long result = parser.getId();
        assertEquals(12345L, result);
    }

    @Test
    public void getIdInvalidNonNumericStartTest() {
        String input = "Nan Some Title";
        SubjectParser parser = new SubjectParser(input);
        long result = parser.getId();
        assertEquals(-1L, result);
    }

    @Test
    public void getIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        long result = parser.getId();
        assertEquals(-1L, result);
    }

    // ---------------------------------------------------------
    // getTitle() Tests
    // ---------------------------------------------------------

    @Test
    public void getTitleWithParenthesesRangeTest() {
        // Logic check: getTitle parses the string, extracts (X/Y) into RangeString, 
        // and returns the rest as the title.
        String input = "100 Java Book (1/10)";
        SubjectParser parser = new SubjectParser(input);
        
        String cleanTitle = parser.getTitle();
        
        // The loop builds the title in reverse/insert order excluding the range
        // "Java Book " might have trailing spaces depending on the substring logic in the source
        assertEquals("Java Book ", cleanTitle);
        assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void getTitleWithBracketsRangeTest() {
        String input = "200 Python Book [5/20]";
        SubjectParser parser = new SubjectParser(input);
        
        String cleanTitle = parser.getTitle();
        
        assertEquals("Python Book ", cleanTitle);
        assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void getTitleNoRangePresentTest() {
        String input = "300 Simple Title";
        SubjectParser parser = new SubjectParser(input);
        
        String cleanTitle = parser.getTitle();
        
        // Should return the title part after the ID
        assertEquals("Simple Title", cleanTitle);
        // RangeString remains null/unchanged
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void getTitleMalformattedSubjectTest() {
        // Subject.substring(Subject.indexOf(" ") + 1) will throw if no space found or null
        // The method catches Exception and returns null
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    // ---------------------------------------------------------
    // getRangeString() Tests
    // ---------------------------------------------------------

    @Test
    public void getRangeStringLazyLoadViaSpyTest() {
        // Requirement: Use Mockito for some tests. 
        // Scenario: getRangeString() calls getTitle() internally if RangeString is null.
        // We use a Spy to verify this internal interaction.
        
        SubjectParser realParser = new SubjectParser("100 Test (1/2)");
        SubjectParser spyParser = spy(realParser);

        // First call should trigger getTitle()
        String result = spyParser.getRangeString();
        
        assertEquals("(1/2)", result);
        verify(spyParser, times(1)).getTitle();
        
        // Second call should not trigger getTitle() as it is cached
        spyParser.getRangeString();
        verify(spyParser, times(1)).getTitle(); // Count remains 1
    }

    // ---------------------------------------------------------
    // getThisRange() and getUpperRange() Tests
    // ---------------------------------------------------------

    @Test
    public void getThisRangeWithParenthesesTest() {
        // Range format (Low/High)
        String input = "1 ID (15/30)";
        SubjectParser parser = new SubjectParser(input);
        
        // Default is 1, parsed should be 15
        assertEquals(15, parser.getThisRange());
    }

    @Test
    public void getUpperRangeWithParenthesesTest() {
        String input = "1 ID (15/30)";
        SubjectParser parser = new SubjectParser(input);
        
        // Default is 1, parsed should be 30
        assertEquals(30, parser.getUpperRange());
    }

    @Test
    public void getThisRangeWithBracketsTest() {
        // Range format [Low/High] -> Triggering the catch block in messageParts that tries brackets
        String input = "1 ID [5/10]";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(5, parser.getThisRange());
    }
    
    @Test
    public void getUpperRangeWithBracketsTest() {
        String input = "1 ID [5/10]";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void getRangesMalformedFormatTest() {
        // Case: Range exists but is not numbers, e.g. (A/B)
        // messageParts will fail parsing int, catch exception, and return null
        // getThisRange will catch exception or see null and return default LowerRange (1)
        String input = "1 ID (A/B)";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void getRangesNoRangeStringTest() {
        // Case: No range in string
        String input = "1 ID Only Title";
        SubjectParser parser = new SubjectParser(input);
        
        // messageParts returns null because getRangeString returns null
        // getThisRange returns default 1
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void getRangesBrokenBracketsTest() {
        // Hitting the deep catch blocks in messageParts
        // ( fails, then [ fails
        String input = "1 ID [Broken/Range"; 
        SubjectParser parser = new SubjectParser(input);
        
        // getTitle might extract "[Broken/Range" if it ends correctly, 
        // but messageParts logic requires specific delimiters.
        // This exercises the "catch (Exception subE)" returning null
        assertEquals(1, parser.getThisRange());
    }
    
    // ---------------------------------------------------------
    // Specific Edge Cases for 100% Line Coverage
    // ---------------------------------------------------------

    @Test
    public void getTitleWithMixedCharactersTest() {
        // Hitting the condition: if ((Character.isDigit(nextchar) == false) && nextchar != '/')
        // inside the getTitle loop logic
        String input = "1 T (1/A)"; // 'A' is not digit and not slash
        SubjectParser parser = new SubjectParser(input);
        
        // The parser aborts treating this as a range and treats it as part of title
        String title = parser.getTitle();
        
        assertTrue(title.contains("(1/A)"));
        assertNull(parser.getRangeString());
    }
}