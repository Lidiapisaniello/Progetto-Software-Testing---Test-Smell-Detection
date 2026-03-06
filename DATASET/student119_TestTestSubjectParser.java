import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.StringTokenizer;

public class TestSubjectParser {

    // --- Reflection Helpers ---

    /** Gets a private integer field value. */
    private int getIntField(SubjectParser parser, String fieldName) throws Exception {
        Field field = SubjectParser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(parser);
    }

    /** Gets a private String field value. */
    private String getStringField(SubjectParser parser, String fieldName) throws Exception {
        Field field = SubjectParser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(parser);
    }

    /** Helper to trigger the private messageParts method for detailed testing. */
    private int[] callMessageParts(SubjectParser parser) throws Exception {
        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        return (int[]) method.invoke(parser);
    }

    // --- Constructor Test ---

    @Test
    public void SubjectParserConstructorTest() throws Exception {
        String subject = "123 Test Subject";
        SubjectParser parser = new SubjectParser(subject);
        
        assertEquals(subject, getStringField(parser, "Subject"));
        assertEquals(1, getIntField(parser, "UpperRange"));
        assertEquals(1, getIntField(parser, "LowerRange"));
    }

    // --- getId Tests ---

    // Path 1: Successful parsing of Long ID
    @Test
    public void getIdSuccessTest() {
        SubjectParser parser = new SubjectParser("9876543210 Subject Line");
        assertEquals(9876543210L, parser.getId());
    }

    // Path 2: Failure parsing (non-numeric first token)
    @Test
    public void getIdFailureNonNumericTest() {
        SubjectParser parser = new SubjectParser("ABC Subject Line");
        assertEquals(-1L, parser.getId());
    }
    
    // Path 2: Failure parsing (empty subject) - StringTokenizer throws NoSuchElementException
    @Test
    public void getIdFailureEmptySubjectTest() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    // --- getTitle Tests (Covers core logic of identifying range and non-range text) ---

    // Success Path: Range in parentheses (X/Y)
    @Test
    public void getTitleSuccessParenthesesRangeTest() throws Exception {
        String subject = "123 The document title (5/10)";
        SubjectParser parser = new SubjectParser(subject);
        
        // getTitle() should extract title excluding the range and set RangeString
        assertEquals("The document title ", parser.getTitle());
        assertEquals("(5/10)", getStringField(parser, "RangeString")); // Sets RangeString
    }

    // Success Path: Range in brackets [X/Y]
    @Test
    public void getTitleSuccessBracketRangeTest() throws Exception {
        String subject = "123 Final Report [2/50]";
        SubjectParser parser = new SubjectParser(subject);
        
        // getTitle() should extract title excluding the range and set RangeString
        assertEquals("Final Report ", parser.getTitle());
        assertEquals("[2/50]", getStringField(parser, "RangeString")); // Sets RangeString
    }
    
    // Path 6: No range found (Default insertion)
    @Test
    public void getTitleNoRangeFoundTest() throws Exception {
        String subject = "123 Just a regular title";
        SubjectParser parser = new SubjectParser(subject);
        
        // getTitle() returns the full remaining subject
        assertEquals("Just a regular title", parser.getTitle());
        assertNull(getStringField(parser, "RangeString"));
    }
    
    // Path 4: Invalid range content (Aborted by Character.isDigit check)
    @Test
    public void getTitleInvalidRangeContentAbortsTest() throws Exception {
        String subject = "123 Title (A/B) suffix";
        SubjectParser parser = new SubjectParser(subject);
        
        // The loop should abort when it finds 'A' or 'B' (non-digit, non-slash) 
        // and append the entire buffer back to the title (P4/continue MAINLOOP).
        assertEquals("Title (A/B) suffix", parser.getTitle());
        assertNull(getStringField(parser, "RangeString")); 
    }
    
    // Path 5 Fails: Range delimiters but missing internal slash (indexOf("/") == -1)
    @Test
    public void getTitleRangeNoSlashFailsTest() throws Exception {
        String subject = "123 Title (1-5)";
        SubjectParser parser = new SubjectParser(subject);
        
        // Finds parentheses, but checks fails (indexOf("/") == -1), so range is not saved.
        assertEquals("Title (1-5)", parser.getTitle());
        assertNull(getStringField(parser, "RangeString"));
    }

    // Path 7: Exception (Subject = "ID" -> substring throws StringIndexOutOfBoundsException)
    @Test
    public void getTitleExceptionPathTest() throws Exception {
        SubjectParser parser = new SubjectParser("1"); // Subject = "1". Subject.indexOf(" ") is -1.
        
        // Throws StringIndexOutOfBoundsException, caught by parseE
        assertNull(parser.getTitle());
    }

    // --- getRangeString Tests ---

    // Path 2: Cache Miss (calls getTitle to populate RangeString)
    @Test
    public void getRangeStringCacheMissTest() throws Exception {
        String subject = "123 Title (1/1)";
        SubjectParser parser = new SubjectParser(subject);
        
        // RangeString is initially null, calling this forces getTitle() to run.
        String result = parser.getRangeString();
        
        assertEquals("(1/1)", result);
        assertEquals("(1/1)", getStringField(parser, "RangeString")); // Verify cache set
    }
    
    // Path 1: Cache Hit
    @Test
    public void getRangeStringCacheHitTest() throws Exception {
        String subject = "123 Title (1/1)";
        SubjectParser parser = new SubjectParser(subject);
        
        // First call populates cache
        parser.getTitle(); 
        
        // Second call uses cached value
        String result = parser.getRangeString();
        
        assertEquals("(1/1)", result);
    }
    
    // Path 3: Exception (Triggered by getTitle failing and returning null, forcing getRangeString to fail on line 125)
    @Test
    public void getRangeStringExceptionPathTest() throws Exception {
        SubjectParser parser = new SubjectParser("1"); // Subject that forces getTitle to fail
        
        // getTitle() returns null. getRangeString tries to call getTitle() again if RangeString is null.
        String result = parser.getRangeString(); 
        assertNull(result);
    }

    // --- messageParts Tests (Internal Logic) ---

    // Path 1: Success (Parenthesis: (X/Y))
    @Test
    public void messagePartsSuccessParenthesesTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title (99/199)");
        parser.getTitle(); // Sets RangeString
        
        int[] parts = callMessageParts(parser);
        assertNotNull(parts);
        assertEquals(99, parts[0]);
        assertEquals(199, parts[1]);
    }
    
    // Path 2: Success (Bracket: [X/Y]) - Enters inner catch, succeeds in second try
    @Test
    public void messagePartsSuccessBracketTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title [5/15]");
        parser.getTitle(); // Sets RangeString
        
        int[] parts = callMessageParts(parser);
        assertNotNull(parts);
        assertEquals(5, parts[0]);
        assertEquals(15, parts[1]);
    }
    
    // Path 3: Failure (Both parenthesis and bracket parsing fail, subE catch block reached)
    @Test
    public void messagePartsFailureBothPathsTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title with (1-5] invalid delimiters");
        parser.getTitle(); // Sets RangeString to "(1-5]"
        
        // First try fails (no slash/format error)
        // Second try fails (no '[' or format error)
        int[] parts = callMessageParts(parser);
        assertNull(parts); // Returns null via catch (Exception subE)
    }

    // Path 4: Outer Exception (e.g., getRangeString returns null)
    @Test
    public void messagePartsOuterExceptionTest() throws Exception {
        SubjectParser parser = new SubjectParser("1"); // Subject that forces getTitle/getRangeString to fail and return null
        parser.getTitle(); 
        
        // RangeString is null. callMessageParts throws NullPointerException on 'mainrange.substring'
        int[] parts = callMessageParts(parser); // Outer try-catch triggered
        assertNull(parts); // Returns null via catch (Exception e)
    }

    // --- getThisRange and getUpperRange Tests ---

    // Success Path: Updates LowerRange and UpperRange (uses Paren parsing)
    @Test
    public void getThisRangeSuccessUpdateTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title (5/15)");
        
        assertEquals(5, parser.getThisRange());
        assertEquals(5, getIntField(parser, "LowerRange")); // Check state update
        assertEquals(1, getIntField(parser, "UpperRange")); // Should still be default
    }

    @Test
    public void getUpperRangeSuccessUpdateTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title (5/15)");
        
        assertEquals(15, parser.getUpperRange());
        assertEquals(15, getIntField(parser, "UpperRange")); // Check state update
        assertEquals(1, getIntField(parser, "LowerRange")); // Should still be default
    }
    
    // Failure Path: messageParts returns null (No range found) -> Returns default (1)
    @Test
    public void getThisRangeFailureDefaultTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title No Range");
        
        assertEquals(1, parser.getThisRange()); // Should return 1 (default)
        assertEquals(1, getIntField(parser, "LowerRange")); 
    }
    
    @Test
    public void getUpperRangeFailureDefaultTest() throws Exception {
        SubjectParser parser = new SubjectParser("123 Title No Range");
        
        assertEquals(1, parser.getUpperRange()); // Should return 1 (default)
        assertEquals(1, getIntField(parser, "UpperRange")); 
    }
}