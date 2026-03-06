import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.StringTokenizer;

public class TestSubjectParser {

    // Test for SubjectParser(String s) constructor and getId()
    // Normal flow: Subject starts with a valid long ID, followed by a space and title/range.
    @Test
    public void getIdValidIdTest() {
        SubjectParser parser = new SubjectParser("1234567890 This is a test subject (1/10)");
        assertEquals(1234567890L, parser.getId());
    }

    // Test getId() for a non-numeric first token (Exception path)
    @Test
    public void getIdNonNumericIdTest() {
        SubjectParser parser = new SubjectParser("ABCDEF This is a test subject (1/10)");
        assertEquals(-1L, parser.getId());
    }

    // Test getId() for an empty Subject (Exception path due to NoSuchElementException from StringTokenizer)
    @Test
    public void getIdEmptySubjectTest() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    // Test getRangeString() - private RangeString is null, calls getTitle() (Normal flow)
    @Test
    public void getRangeStringNullTest() throws Exception {
        // We need a Subject that causes getTitle() to set RangeString.
        SubjectParser parser = new SubjectParser("123 Subject title (5/10)");
        String expectedRange = "(5/10)";
        String actualRange = parser.getRangeString();
        assertEquals(expectedRange, actualRange);

        // Verify that getTitle() was called and RangeString was set
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertEquals(expectedRange, rangeStringField.get(parser));
    }

    // Test getRangeString() - private RangeString is not null (Normal flow)
    @Test
    public void getRangeStringNotNullTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject title (5/10)");

        // Set RangeString manually to simulate it being set previously
        String preSetRange = "[2/5]";
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        rangeStringField.set(parser, preSetRange);

        assertEquals(preSetRange, parser.getRangeString());
    }

    // Test getRangeString() - Exception path within getRangeString (e.g., if getTitle() throws exception)
    // The current getTitle() doesn't seem to throw an exception that would only be caught by getRangeString's catch block,
    // but we can simulate a null RangeString from getTitle() when RangeString is null and getTitle() is called.
    // However, the current code structure makes it hard to hit the System.err.println(e.getMessage()); path without
    // forcing an exception from getTitle(). Since getTitle() returns null in its exception path,
    // let's test a scenario where RangeString is null and getTitle() returns null.
    // The existing structure seems to handle exceptions within getTitle(), not propagate them to getRangeString's try/catch.
    // Thus, testing the nominal flow where getTitle() sets RangeString covers the primary intent.
    // Let's ensure the getTitle() exception path is covered.

    // Test getTitle() - Valid range with parentheses at the end (Normal flow)
    @Test
    public void getTitleValidParenthesesRangeTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 This is a subject title (5/10)");
        String expectedTitle = "This is a subject title ";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
        // Verify RangeString is set
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertEquals("(5/10)", rangeStringField.get(parser));
    }

    // Test getTitle() - Valid range with brackets at the end (Alternative range check path)
    @Test
    public void getTitleValidBracketsRangeTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Another subject [2/5]");
        String expectedTitle = "Another subject ";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
        // Verify RangeString is set
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertEquals("[2/5]", rangeStringField.get(parser));
    }

    // Test getTitle() - No range found (No range check, 'FoundRange' remains false, loop completes)
    @Test
    public void getTitleNoRangeTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Simple Title");
        String expectedTitle = "Simple Title";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
        // Verify RangeString is null
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertNull(rangeStringField.get(parser));
    }

    // Test getTitle() - Range-like sequence that is not a valid range (continue MAINLOOP branch in while loop)
    @Test
    public void getTitleInvalidRangeCharTest() throws Exception {
        // e.g., ')' is found, but inside the potential range, there is a non-digit/non-slash character.
        SubjectParser parser = new SubjectParser("123 Title (1/A)");
        // The while loop will iterate backwards from ')'
        // nextchar will be 'A'.
        // The condition (Character.isDigit(nextchar) == false) && nextchar != '/' is TRUE for 'A'.
        // The inner StringBuffer 'tmpbuf' will contain "(1/A)"
        // It's inserted into 'sb' and the MAINLOOP continues.
        String expectedTitle = "Title (1/A)";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
        // Verify RangeString is null
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertNull(rangeStringField.get(parser));
    }

    // Test getTitle() - Subject too short (Exception path: Subject.substring(Subject.indexOf(" ") + 1, Subject.length()) fails)
    @Test
    public void getTitleSubjectTooShortTest() {
        SubjectParser parser = new SubjectParser("123"); // tmpSubject will be ""
        // Loop runs, no range found, returns ""
        String expectedTitle = "";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
    }

    // Test getTitle() - Edge case: Subject ends with an open parenthesis/bracket
    @Test
    public void getTitleOpenRangeAtEndTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title ("); // testchar = '(', FoundRange = false. This char is not ')' or ']', so it is appended to sb.
        String expectedTitle = "Title (";
        String actualTitle = parser.getTitle();
        assertEquals(expectedTitle, actualTitle);
        // Verify RangeString is null
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        assertNull(rangeStringField.get(parser));
    }

    // Test getTitle() - Exception path (e.g., ArrayIndexOutOfBounds from invalid subject structure, though the loop seems robust)
    // The most likely exception is an out-of-bounds index access in the while loop if the opening char is not found.
    @Test
    public void getTitleMissingOpenCharTest() {
        // tmpSubject: "Test 5/10)"
        SubjectParser parser = new SubjectParser("123 Test 5/10)");
        // i starts at ')'
        // endchar is '('
        // while loop runs, nextchar = '0', '/', '1', ' ', 't', 's', 'e', 'T'
        // 'T' is not '('. i will go below 0 (index out of bounds) and throw Exception.
        String actualTitle = parser.getTitle();
        assertNull(actualTitle); // parseE.printStackTrace() path
    }


    // Test messageParts() via reflection (Internal parsing logic)

    // Helper method to invoke the private messageParts()
    private int[] invokeMessageParts(SubjectParser parser) throws Exception {
        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        return (int[]) method.invoke(parser);
    }

    // Set the RangeString field via reflection
    private void setRangeString(SubjectParser parser, String value) throws Exception {
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        rangeStringField.set(parser, value);
    }

    // Test messageParts() - Valid (low/high) format (Parentheses block)
    @Test
    public void messagePartsValidParenthesesRangeTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject (5/10)");
        // getRangeString() called inside messageParts() will call getTitle() and set RangeString
        int[] result = invokeMessageParts(parser);
        assertNotNull(result);
        assertEquals(5, result[0]); // low
        assertEquals(10, result[1]); // high
    }

    // Test messageParts() - Valid [low/high] format (Brackets block, alternative path for success)
    @Test
    public void messagePartsValidBracketsRangeTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject [2/5]");
        // We ensure RangeString is set to the bracket format by calling getTitle()
        parser.getTitle();
        int[] result = invokeMessageParts(parser);
        assertNotNull(result);
        assertEquals(2, result[0]); // low
        assertEquals(5, result[1]); // high
    }

    // Test messageParts() - Invalid range format (First try block fails, then second try block fails, return null)
    @Test
    public void messagePartsInvalidFormatTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject Title");
        // Manually set RangeString to a non-range string to test failure logic
        setRangeString(parser, "INVALID_RANGE"); // Does not contain '(' or '['
        int[] result = invokeMessageParts(parser);
        assertNull(result); // low and high set to -1, then returns null (subE catch block)
    }

    // Test messageParts() - Range string is null (Exception path - getRangeString fails, e.printStackTrace() is called, returns null)
    @Test
    public void messagePartsRangeStringNullTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title");
        // getRangeString() inside messageParts() will return null since getTitle() does not set RangeString
        int[] result = invokeMessageParts(parser);
        assertNull(result); // e.printStackTrace() path
    }

    // Test messageParts() - Non-numeric range parts (NumberFormatException in first try block)
    @Test
    public void messagePartsNonNumericParenthesesTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject (5/B)");
        parser.getTitle(); // Sets RangeString
        int[] result = invokeMessageParts(parser);
        // inte catch block executes. Then second try block is executed.
        // It tries to parse "(5/B)" as a bracket range which fails.
        assertNull(result);
    }

    // Test messageParts() - Non-numeric range parts (NumberFormatException in second try block)
    @Test
    public void messagePartsNonNumericBracketsTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject [5/B]");
        parser.getTitle(); // Sets RangeString
        int[] result = invokeMessageParts(parser);
        // First try block fails (RangeString starts with '['), inte catch block executes.
        // Second try block throws NumberFormatException (subE catch block).
        assertNull(result);
    }

    // Test messageParts() - Missing '/' separator in parentheses (StringTokenizer error)
    @Test
    public void messagePartsMissingSeparatorParenthesesTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject (5-10)");
        parser.getTitle(); // Sets RangeString
        int[] result = invokeMessageParts(parser);
        // StringTokenizer(range, "/") fails to get two tokens, throws NoSuchElementException, inte catch block.
        // Second try block fails (RangeString starts with '(') and throws StringIndexOutOfBoundsException, subE catch block.
        assertNull(result);
    }

    // Test messageParts() - Missing '/' separator in brackets (StringTokenizer error)
    @Test
    public void messagePartsMissingSeparatorBracketsTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Subject [5-10]");
        parser.getTitle(); // Sets RangeString
        int[] result = invokeMessageParts(parser);
        // First try block fails (RangeString starts with '['), inte catch block.
        // Second try block: StringTokenizer(range2, "/") fails to get two tokens, throws NoSuchElementException, subE catch block.
        assertNull(result);
    }

    // Test getThisRange() and getUpperRange()

    // Test getThisRange() - Normal flow (messageParts returns a non-null array)
    @Test
    public void getThisRangeValidTest() {
        SubjectParser parser = new SubjectParser("123 Subject (5/10)");
        assertEquals(5, parser.getThisRange());
    }

    // Test getUpperRange() - Normal flow (messageParts returns a non-null array)
    @Test
    public void getUpperRangeValidTest() {
        SubjectParser parser = new SubjectParser("123 Subject (5/10)");
        assertEquals(10, parser.getUpperRange());
    }

    // Test getThisRange() - Exception path (messageParts returns null)
    @Test
    public void getThisRangeInvalidRangeTest() {
        SubjectParser parser = new SubjectParser("123 Subject Title"); // No range, messageParts returns null
        // Initial LowerRange is 1 (from constructor)
        assertEquals(1, parser.getThisRange()); // Returns initial LowerRange
    }

    // Test getUpperRange() - Exception path (messageParts returns null)
    @Test
    public void getUpperRangeInvalidRangeTest() {
        SubjectParser parser = new SubjectParser("123 Subject Title"); // No range, messageParts returns null
        // Initial UpperRange is 1 (from constructor)
        assertEquals(1, parser.getUpperRange()); // Returns initial UpperRange
    }

    // Test getThisRange() - Exception path within messageParts (e.g. NumberFormatException)
    // The current structure of getThisRange() and getUpperRange() catches exceptions from within their try blocks.
    // Since messageParts() has internal catch blocks that return null on failure, we need a way to force an exception
    // that messageParts() *doesn't* handle, which is not easily achievable with reflection without changing the method's behavior.
    // However, if messageParts() returns null, the `if (parts != null)` condition is false, and the initial value is returned.
    // The test cases above cover the two main branches: `parts != null` (true) and `parts != null` (false).
    // The internal exception handling of getThisRange() and getUpperRange() is covered by the null return from messageParts() which falls through the `if` and catches the exception if one were thrown.
    // If messageParts() returns null, no exception is thrown, and the initial range is returned, which is covered by the "InvalidRangeTest" cases.

}