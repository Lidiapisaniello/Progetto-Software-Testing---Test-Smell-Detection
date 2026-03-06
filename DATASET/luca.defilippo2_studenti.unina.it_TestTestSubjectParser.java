/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Luca"
Cognome: "De Filippo"
Username: luca.defilippo2@studenti.unina.it
UserID: 688
Date: 21/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --------------------------------------------------------------------------------
    // Tests for getId()
    // --------------------------------------------------------------------------------

    @Test
    public void getIdValidNumberTest() {
        SubjectParser parser = new SubjectParser("12345 Test Subject");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void getIdInvalidNumberTest() {
        SubjectParser parser = new SubjectParser("NotANumber Test Subject");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getIdNullSubjectTest() {
        // This triggers the catch(Exception e) block in getId because StringTokenizer throws NPE
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    // --------------------------------------------------------------------------------
    // Tests for getTitle() and getRangeString()
    // --------------------------------------------------------------------------------

    @Test
    public void getTitleSimpleNoRangeTest() {
        // Scenario: Standard subject, no range indicators
        SubjectParser parser = new SubjectParser("100 Just A Simple Title");
        String title = parser.getTitle();
        // The parser strips the ID (first token) based on space index logic
        assertEquals("Just A Simple Title", title);
        // RangeString should remain null (or not set to a range)
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleWithParenthesesRangeTest() {
        // Scenario: Ends with valid (x/y)
        SubjectParser parser = new SubjectParser("100 Title With Range (1/10)");
        
        // getTitle is expected to strip the range from the returned string
        assertEquals("Title With Range ", parser.getTitle());
        // And set the internal RangeString
        assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void getTitleWithBracketsRangeTest() {
        // Scenario: Ends with valid [x/y]
        // This hits the 'tokentype' check for ']'
        SubjectParser parser = new SubjectParser("100 Title With Range [5/20]");
        
        assertEquals("Title With Range ", parser.getTitle());
        assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void getTitleWithMultipleRangesTest() {
        // Scenario: Two ranges. Only the last one should be captured as the range.
        // This tests the branch: if ((testchar == ')' || testchar == ']') && FoundRange == false)
        // When the loop hits the second (middle) range, FoundRange is true, so it goes to 'else' 
        // and appends it to the title.
        SubjectParser parser = new SubjectParser("100 Title (1/2) (3/4)");
        
        // The last one (3/4) is extracted. The first one (1/2) remains in title.
        assertEquals("Title (1/2) ", parser.getTitle());
        assertEquals("(3/4)", parser.getRangeString());
    }

    @Test
    public void getTitleWithInvalidCharactersInRangeTest() {
        // Scenario: Range format looks like range but contains letters: (1a/5)
        // This hits the loop condition: if ((Character.isDigit(nextchar) == false) && nextchar != '/')
        // It should break out of the inner loop and continue MAINLOOP, treating it as title text.
        SubjectParser parser = new SubjectParser("100 Title With Bad Range (1a/5)");
        
        assertEquals("Title With Bad Range (1a/5)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleWithPartialRangeTest() {
        // Scenario: Closing bracket but no opening bracket before string start (or mismatch)
        // The logic in SubjectParser encounters ')' but fails to find a matching '(' 
        // because it hits a space ' ' which triggers the digit check failure inside the loop.
        // The loop continues MAINLOOP, treating the sequence as part of the title.
        SubjectParser parser = new SubjectParser("100 Title 5/10)");
        
        // Since it wasn't identified as a range, it remains in the title.
        assertEquals("Title 5/10)", parser.getTitle());
    }

    @Test
    public void getTitleSubjectNullTest() {
        // Triggers exception in getTitle immediately (NPE on substring)
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
        assertNull(parser.getRangeString());
    }

    // --------------------------------------------------------------------------------
    // Tests for Parsing Logic in messageParts() via getThisRange/getUpperRange
    // --------------------------------------------------------------------------------

    @Test
    public void getRangesFromParenthesesTest() {
        // Tests valid parsing of (Low/High)
        // Logic: messageParts -> try '(' block -> success
        SubjectParser parser = new SubjectParser("100 T (10/20)");
        
        assertEquals(10, parser.getThisRange()); // LowerRange
        assertEquals(20, parser.getUpperRange()); // UpperRange
    }

    @Test
    public void getRangesFromBracketsTest() {
        // Tests valid parsing of [Low/High]
        // Logic: messageParts -> try '(' block -> fails (lastIndexOf returns -1) -> catch
        // -> try '[' block -> success
        SubjectParser parser = new SubjectParser("100 T [30/40]");
        
        assertEquals(30, parser.getThisRange());
        assertEquals(40, parser.getUpperRange());
    }

    @Test
    public void getRangesDefaultsTest() {
        // Scenario: No range present
        SubjectParser parser = new SubjectParser("100 Just Title");
        
        // Defaults set in constructor
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void getRangesMalformedNumbersTest() {
        // Scenario: Range structure is accepted by getTitle but fails parsing in messageParts
        // We use "(/10)". 
        // getTitle sees ')', loops back over '0','1','/' and hits '('. 
        // All chars are digits or '/', so valid range. RangeString = "(/10)".
        // messageParts splits "(/10" by "/". Token 1: "(10". 
        // Token 2 is missing. st.nextToken() throws NoSuchElementException.
        // This triggers the catch(Exception inte) block.
        
        SubjectParser parser = new SubjectParser("100 T (/10)");
        
        // messageParts throws exception -> returns null
        // getThisRange catches exception or sees null -> returns default
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void getRangesMalformedBracketsFallThroughTest() {
        // Scenario: It looks like a bracket range [1/1], but we want to ensure
        // the first try block (for parenthesis) fails cleanly and allows the second block to run.
        SubjectParser parser = new SubjectParser("100 T [5/5]");
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void getRangesNestedExceptionTest() {
        // We need to hit the specific path where the first try block fails, 
        // the second try block starts, but ALSO fails.
        
        // We use "(1/)" which getTitle accepts.
        // messageParts first block: split "(1/". Tokens: "(1". Second token missing. Fails.
        // Enters catch(Exception inte).
        // Enters second try block (brackets).
        // lastIndexOf("[") returns -1. substring throws exception.
        // Enters catch(Exception subE).
        // Returns null.
        
        SubjectParser parser = new SubjectParser("100 T (1/)");
        
        // This should result in defaults because parsing fails completely
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void getThisRangeNullPartsTest() {
        // This test targets the specific check: if (parts != null) inside getThisRange
        // We need messageParts() to return null.
        // Subject = null causes getRangeString to be null.
        // messageParts: mainrange = null -> throws Exception -> returns null.
        SubjectParser parser = new SubjectParser(null);
        
        assertEquals(1, parser.getThisRange()); // Returns default LowerRange
    }

    @Test
    public void getUpperRangeNullPartsTest() {
        // Similar to above, targets: if (parts != null) inside getUpperRange
        SubjectParser parser = new SubjectParser(null);
        
        assertEquals(1, parser.getUpperRange()); // Returns default UpperRange
    }
}