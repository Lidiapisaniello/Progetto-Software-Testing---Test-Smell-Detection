/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Ilaria"
Cognome: "Saulino"
Username: i.saulino@studenti.unina.it
UserID: 1133
Date: 24/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;

public class TestSubjectParser {

    // --- getId() Tests ---

    @Test
    public void getIdValidNumericStartTest() {
        SubjectParser parser = new SubjectParser("123 Some Subject");
        assertEquals(123, parser.getId());
    }

    @Test
    public void getIdInvalidAlphaStartTest() {
        SubjectParser parser = new SubjectParser("ABC Some Subject");
        assertEquals(-1, parser.getId());
    }

    @Test
    public void getIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    // --- getRangeString(), getThisRange(), getUpperRange() Normal Flow Tests ---

    @Test
    public void getRangesWithParenthesesTest() {
        SubjectParser parser = new SubjectParser("100 Subject Title (5/10)");
        
        // Verify Range String extraction
        assertEquals("(5/10)", parser.getRangeString());
        
        // Verify Parsing of extracted range
        assertEquals(5, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
        assertEquals("Subject Title ", parser.getTitle());
    }

    @Test
    public void getRangesWithBracketsTest() {
        SubjectParser parser = new SubjectParser("200 Subject Title [20/30]");
        
        assertEquals("[20/30]", parser.getRangeString());
        assertEquals(20, parser.getThisRange());
        assertEquals(30, parser.getUpperRange());
        assertEquals("Subject Title ", parser.getTitle());
    }

    @Test
    public void getRangesNoneFoundTest() {
        SubjectParser parser = new SubjectParser("300 Subject With No Range");
        
        assertNull(parser.getRangeString());
        // Should return defaults (initialized to 1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
        assertEquals("Subject With No Range", parser.getTitle());
    }

    // --- getTitle() Complex Logic & Branch Coverage Tests ---

    @Test
    public void getTitleWithMultipleRangesPicksLastTest() {
        // Logic requires finding the last valid range. 
        // The first range (1/2) should be part of the title, the second (3/4) is the range.
        SubjectParser parser = new SubjectParser("400 Title (1/2) End (3/4)");
        
        assertEquals("(3/4)", parser.getRangeString());
        assertEquals("Title (1/2) End ", parser.getTitle());
        assertEquals(3, parser.getThisRange());
    }

    @Test
    public void getTitleIgnoresMalformedRangeNoSlashTest() {
        // (12) has no slash. 
        // Based on the source code logic: 
        // 1. It identifies ')' and scans back to '('.
        // 2. It finds only digits.
        // 3. It checks for '/', finds none.
        // 4. It exits the check WITHOUT adding the buffer back to 'sb'.
        // This effectively SWALLOWS the text "(12)".
        // We assert this actual behavior to ensure coverage and regression testing.
        SubjectParser parser = new SubjectParser("500 Title (12)");
        
        assertNull(parser.getRangeString());
        assertEquals("Title ", parser.getTitle());
    }

    @Test
    public void getTitleIgnoresMalformedRangeNonDigitContentTest() {
        // (a/b) contains non-digits ('a').
        // The loop detects 'a', determines it's not a valid range candidate,
        // and immediately inserts the buffer back into the title.
        // Thus, (a/b) is preserved.
        SubjectParser parser = new SubjectParser("600 Title (a/b)");
        
        assertNull(parser.getRangeString());
        assertEquals("Title (a/b)", parser.getTitle());
    }
    
    @Test
    public void getTitleIgnoresIncompleteRangeTest() {
        // Case: "Title )1/2(" - structural mismatch.
        // The parser encounters ')', scans back, hits a space (non-digit), 
        // and aborts the range check, preserving the text.
        SubjectParser parser = new SubjectParser("700 Title )1/2(");
        assertNull(parser.getRangeString());
        assertEquals("Title )1/2(", parser.getTitle());
    }

    @Test
    public void getTitleHandlesNestedOrFalseEndsTest() {
        // Checks logic: if ((testchar == ')' || testchar == ']') && FoundRange == false)
        // We need a case where a closing bracket is found BUT FoundRange is ALREADY true.
        // This forces the 'else' path inside the main loop after range is found.
        SubjectParser parser = new SubjectParser("800 Title [1/2] RealRange (3/4)");
        
        assertEquals("(3/4)", parser.getRangeString());
        assertEquals("Title [1/2] RealRange ", parser.getTitle());
    }

    @Test
    public void getTitleExceptionHandlingTest() {
        // Subject with no space throws Exception in getTitle because of substring logic
        // Subject.substring(Subject.indexOf(" ") + 1...
        // If Subject is null, it throws NPE immediately inside getTitle.
        SubjectParser parser = new SubjectParser(null);
        
        assertNull(parser.getTitle());
    }

    // --- messageParts() Logic & Exception Coverage ---
    
    @Test
    public void messagePartsParsesParenthesesFailureTest() {
        // We need a case where getRangeString returns a string, but it fails parentheses parsing,
        // falling back to bracket logic.
        SubjectParser parser = new SubjectParser("900 T [5/5]");
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void messagePartsParsesParenthesesFoundButInvalidContentTest() {
        // Case: RangeString is "(/)".
        // getTitle extracts "(/)" as RangeString because it has the structure, 
        // but messageParts will fail parsing integers.
        
        SubjectParser parser = new SubjectParser("901 T (/)"); 
        
        assertEquals("(/)", parser.getRangeString());
        // Result: Default values (1).
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void messagePartsCompleteFailureTest() throws Exception {
        // We need to cover the catch(subE) block in messageParts.
        // This requires getRangeString() to return a String that fails BOTH "(" and "[" parsing.
        // Since getTitle() logic is robust, we use Java Reflection to force a bad RangeString.
        
        SubjectParser parser = new SubjectParser("999 Mock Test");
        
        // 1. Access private field 'RangeString'
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        
        // 2. Set it to garbage that has NO brackets (causing lastIndexOf to return -1 and substring to throw)
        rangeStringField.set(parser, "GARBAGE_DATA_NO_BRACKETS");
        
        // 3. Call method. 
        // messageParts calls getRangeString -> returns "GARBAGE..."
        // catch(inte) is triggered (no '(').
        // catch(subE) is triggered (no '[').
        // Returns null internally, so getThisRange keeps default.
        
        int result = parser.getThisRange();
        
        assertEquals(1, result);
    }
    
    // --- getRangeString() Exception Coverage ---

    @Test
    public void getRangeStringExceptionTest() {
        // Force an exception inside getRangeString.
        // It calls getTitle(). Passing null to constructor makes getTitle throw NPE.
        // getRangeString catches this and prints to stderr, returning null.
        
        SubjectParser parser = new SubjectParser(null);
        
        assertNull(parser.getRangeString());
    }
}