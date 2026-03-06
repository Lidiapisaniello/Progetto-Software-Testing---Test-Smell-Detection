import org.junit.Test;
import static org.junit.Assert.*;

// Note: The SubjectParser class provided in the prompt must be in the same package or imported.
// For this test file to be runnable, I will assume it is available in the compilation context.

public class TestSubjectParser {

    /**
     * Unreachable Code Analysis:
     * * 1. SubjectParser.java:51 (catch block in getThisRange)
     * 2. SubjectParser.java:62 (catch block in getUpperRange)
     * 3. SubjectParser.java:98 (catch block in getRangeString)
     * * Rationale: The methods getThisRange() and getUpperRange() rely on messageParts(), 
     * and getRangeString() relies on getTitle(). Both messageParts() and getTitle() 
     * have broad internal 'catch (Exception ...)' blocks (lines 89 and 152 respectively) 
     * and return gracefully (null or a value) without re-throwing or throwing checked 
     * exceptions. Since they handle their own exceptions, the outer catch blocks in 
     * getThisRange(), getUpperRange(), and getRangeString() cannot be reached.
     */

    // --- Tests for getId() ---

    @Test
    public void testGetId_Success() {
        // Covers: try block (successful parse).
        SubjectParser parser = new SubjectParser("1234567890 The Subject");
        assertEquals(1234567890L, parser.getId());
    }

    @Test
    public void testGetId_Exception() {
        // Covers: catch block (failed parse, e.g., not a number or empty subject).
        SubjectParser parser = new SubjectParser("NotAnId The Subject");
        assertEquals(-1L, parser.getId());
        
        // Also test for an empty string subject
        parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    // --- Tests for getTitle() and getRangeString() ---

    @Test
    public void testGetTitle_Success_ParenthesesRange() {
        // Covers: Path where range (X/Y) is found and correctly isolated.
        // Also covers: getRangeString() when RangeString is null.
        String subject = "123 My Important Title with trailing text (1/100)";
        SubjectParser parser = new SubjectParser(subject);

        String title = parser.getTitle();
        assertEquals("My Important Title with trailing text", title.trim());
        
        String rangeString = parser.getRangeString();
        assertEquals(" (1/100)", rangeString);
        
        // Ensure getRangeString returns existing value on subsequent calls (not null path)
        assertEquals(" (1/100)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_Success_BracketsRange() {
        // Covers: Path where range [X/Y] is found and correctly isolated.
        String subject = "999 Another Title [50/75]";
        SubjectParser parser = new SubjectParser(subject);

        String title = parser.getTitle();
        assertEquals("Another Title", title.trim());
        
        String rangeString = parser.getRangeString();
        assertEquals(" [50/75]", rangeString);
    }
    
    @Test
    public void testGetTitle_Success_NoRange_SubjectWithSpace() {
        // Covers: Simple subject where no ')' or ']' is found (or no valid range at the end).
        String subject = "101 Simple Subject Line";
        SubjectParser parser = new SubjectParser(subject);

        String title = parser.getTitle();
        // The parser keeps the trailing space if it exists after the ID and before the title starts.
        assertEquals("Simple Subject Line", title.trim()); 
        
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitle_ExceptionPath() throws Exception {
        // To trigger the catch block in getTitle(), we need an exception.
        // A StringIndexOutOfBoundsException can be forced by having a subject that only contains an ID, 
        // leading to Subject.indexOf(" ") returning -1, and tmpSubject being empty.
        // However, Subject.indexOf(" ") + 1 results in 0, so it seems safe for an ID-only subject.
        // The most likely exception is an IndexOutOfBoundsException inside the while loop if the
        // endchar ('(' or '[') is missing, which fully covers the catch block (line 152).
        
        String subject = "102 Malformed Subject (1/"; 
        SubjectParser parser = new SubjectParser(subject);

        // The while loop (line 130) will throw IndexOutOfBoundsException when it reaches the beginning 
        // of the string without finding '(', triggering the catch block.
        assertNull(parser.getTitle());
    }
    
    @Test
    public void testGetTitle_NoRangeFound_ContainsClosingParenButInvalidContent() {
        // Covers: The continue MAINLOOP branch (line 134) where a ')' is found, 
        // but the content inside the potential range is non-digit/non-slash, causing 
        // the range search to abort and include the block in the title.
        String subject = "103 Title (Invalid Range)";
        SubjectParser parser = new SubjectParser(subject);

        // The 'R' in "Range" is not a digit or '/', so the range extraction fails and continues.
        String title = parser.getTitle();
        assertEquals("Title (Invalid Range)", title.trim());
        assertNull(parser.getRangeString());
    }


    // --- Tests for messageParts(), getThisRange(), getUpperRange() ---
    
    @Test
    public void testMessageParts_Success_ParenthesesRange() {
        // Covers: messageParts() internal try block, successfully parsing (X/Y)
        String subject = "201 Message (3/99)";
        SubjectParser parser = new SubjectParser(subject);
        
        // Calling getTitle() is necessary to set RangeString for messageParts()
        parser.getTitle(); 
        
        int[] parts = parser.messageParts(); // Calls getRangeString() internally
        assertNotNull(parts);
        assertEquals(3, parts[0]);
        assertEquals(99, parts[1]);
        
        // Also covers the setters: getThisRange() and getUpperRange() (positive path)
        assertEquals(3, parser.getThisRange());
        assertEquals(99, parser.getUpperRange());
        assertEquals(3, parser.getThisRange()); // Call again to ensure state is maintained
        assertEquals(99, parser.getUpperRange());
    }

    @Test
    public void testMessageParts_Success_BracketsRange() {
        // Covers: messageParts() internal catch block, falling back to and successfully parsing [X/Y]
        String subject = "202 Message [10/20]";
        SubjectParser parser = new SubjectParser(subject);
        
        parser.getTitle();
        
        int[] parts = parser.messageParts(); // Calls getRangeString() internally
        assertNotNull(parts);
        assertEquals(10, parts[0]);
        assertEquals(20, parts[1]);
        
        // Also covers the setters: getThisRange() and getUpperRange() (positive path)
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testMessageParts_Failure_NoRange() {
        // Covers: Full failure to find a range, returning null (line 92).
        String subject = "203 Message No Range Here";
        SubjectParser parser = new SubjectParser(subject);
        
        parser.getTitle(); // Sets RangeString to null
        
        int[] parts = parser.messageParts();
        assertNull(parts);
        
        // Covers the setters: getThisRange() and getUpperRange() (null path, Lower/UpperRange remain 1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testMessageParts_Failure_MalformedRangeInsideParentheses() {
        // Covers: Exception in the first try block, then falling to the second try block, 
        // and that second block also fails (subE catch block on line 92).
        String subject = "204 Malformed Subject (A/B)"; // NumberFormatException expected in first try block (line 82)
        SubjectParser parser = new SubjectParser(subject);
        
        parser.getTitle(); 

        // This attempts the first try (parentheses), gets a NumberFormatException on line 82.
        // Then it attempts the second try (brackets), which fails with a StringIndexOutOfBoundsException 
        // because there are no brackets, finally hitting the subE catch (line 92).
        int[] parts = parser.messageParts();
        assertNull(parts);
    }
    
    @Test
    public void testMessageParts_Exception_RangeStringReturnsNull() {
        // Covers: The outer catch block in messageParts() (line 96).
        // This is extremely difficult to reach without reflection, as getRangeString() handles 
        // its own exceptions. The only way is if getRangeString() *returns* null and the subsequent 
        // usage causes an exception, but getRangeString() is called on line 68 and mainrange
        // is used in .substring on lines 72 and 84. If mainrange is null, a NullPointerException 
        // is thrown, which is caught by the outer catch on line 96.
        
        // By constructing the object without an ID, and relying on getTitle() failing
        // to set RangeString, we simulate a null RangeString without setting it explicitly.
        // However, getRangeString() returns null (and doesn't throw) if getTitle() fails.
        // If Subject = "My Subject", getTitle() doesn't throw.
        
        // To cover the catch on line 96, we need a NullPointerException when accessing mainrange.
        // Since getRangeString() only returns null if getTitle() fails and getTitle() only 
        // returns null if it catches an exception (line 152), this path is plausible.
        
        String subject = "Malformed Subject (1/"; // getTitle() will return null (exception path)
        SubjectParser parser = new SubjectParser(subject);
        
        // We do not call getTitle() because messageParts calls getRangeString which calls getTitle() 
        // only if RangeString is null.

        // Simulating the exception path by using a malformed subject that causes getTitle() to return null, 
        // which makes getRangeString() return null, leading to an NPE on line 72 of messageParts().
        parser.getTitle(); // This ensures RangeString is null
        
        // The Subject "Malformed Subject (1/" causes getTitle() to throw IOOBE and return null.
        // getRangeString() then returns null. messageParts() gets mainrange = null.
        // mainrange.substring(...) on line 72 throws NPE, triggering the catch block on line 96.
        assertNull(parser.messageParts());
        
        // Note: The printStackTrace on line 97 of SubjectParser.java will be executed.
    }
}