import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class TestSubjectParser {

    // --- getId() Tests ---

    @Test
    public void GetIdValidTest() {
        String subject = "12345 Valid Title";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void GetIdInvalidTextTest() {
        String subject = "InvalidId Valid Title";
        SubjectParser parser = new SubjectParser(subject);
        // Expect -1 when Long.parseLong fails
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void GetIdNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        // Expect -1 when StringTokenizer throws NullPointerException inside getId
        assertEquals(-1L, parser.getId());
    }

    // --- getTitle() and getRangeString() Tests ---

    @Test
    public void GetTitleParenthesesRangeTest() {
        String subject = "100 Title Check (1/5)";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("Title Check ", parser.getTitle());
        assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void GetTitleBracketsRangeTest() {
        String subject = "100 Title Check [1/5]";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("Title Check ", parser.getTitle());
        assertEquals("[1/5]", parser.getRangeString());
    }

    @Test
    public void GetTitleNoRangeTest() {
        String subject = "100 Title Check No Range";
        SubjectParser parser = new SubjectParser(subject);
        
        // Loop runs, finds no range, returns full string (minus ID part)
        assertEquals("Title Check No Range", parser.getTitle());
        // RangeString remains null
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitleBrokenRangeNonDigitTest() {
        // Test loop logic: Character.isDigit(nextchar) == false check
        // "a" is not a digit, so it breaks the inner while loop and continues MAINLOOP
        String subject = "100 Title (1a/5)";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("Title (1a/5)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitleBrokenRangeNoSlashTest() {
        // Test logic: tmpbuf.toString().indexOf("/") != -1 check
        // It looks like a range (digits inside parens), but missing slash
        String subject = "100 Title (15)";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("Title (15)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitleMultiplePossibleRangesTest() {
        // Logic starts from end (i--). Should pick the last one.
        String subject = "100 Title (1/2) real (3/4)";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("Title (1/2) real ", parser.getTitle());
        assertEquals("(3/4)", parser.getRangeString());
    }

    @Test
    public void GetTitleNullSubjectTest() {
        SubjectParser parser = new SubjectParser(null);
        // Subject.substring throws NPE, caught by catch(Exception parseE), returns null
        assertNull(parser.getTitle());
    }
    
    @Test
    public void GetTitleNoSpaceTest() {
        // Logic: Subject.substring(Subject.indexOf(" ") + 1...
        // If no space, indexOf is -1, +1 is 0. Works fine.
        String subject = "OneWordSubject";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals("OneWordSubject", parser.getTitle());
    }

    // --- getThisRange() and getUpperRange() / messageParts() Tests ---

    @Test
    public void GetThisRangeStandardTest() {
        String subject = "100 Title (2/5)";
        SubjectParser parser = new SubjectParser(subject);
        
        // Parses (2/5) successfully in first try block of messageParts
        assertEquals(2, parser.getThisRange());
        assertEquals(5, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeBracketsTest() {
        String subject = "100 Title [3/6]";
        SubjectParser parser = new SubjectParser(subject);
        
        // 1. getTitle identifies [3/6].
        // 2. messageParts tries parsing with '(', fails.
        // 3. Catches exception (catch inte).
        // 4. Tries parsing with '[', succeeds.
        assertEquals(3, parser.getThisRange());
        assertEquals(6, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeNullRangeTest() {
        String subject = "100 Title No Range";
        SubjectParser parser = new SubjectParser(subject);
        
        // getRangeString returns null.
        // messageParts tries mainrange.substring -> throws NPE.
        // Caught by outer catch(Exception e) in messageParts, returns null.
        // getThisRange sees null parts, returns default (1).
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeOverflowTest() {
        // This is a critical test to hit the inner-most catch block of messageParts.
        // 1. getTitle sees digits, accepts regex, sets RangeString = (9999999999/1).
        // 2. messageParts enters first try block.
        // 3. Integer.parseInt("9999999999") throws NumberFormatException.
        // 4. Enters catch(Exception inte).
        // 5. Tries logic for brackets [...].
        // 6. RangeString is (...), so lastIndexOf("[") is -1 or fails substring.
        // 7. Enters catch(Exception subE).
        // 8. Returns null.
        
        String subject = "100 Title (9999999999/1)";
        SubjectParser parser = new SubjectParser(subject);
        
        // Expect defaults because parsing failed completely
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
}