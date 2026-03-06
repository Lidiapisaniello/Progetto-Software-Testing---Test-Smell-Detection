import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class TestSubjectParser {

    /**
     * White-Box Test for private method messageParts()
     * Logic Covered:
     * - Happy Path (Try Block 1)
     * - Uses input from BlackBox: "100 Message (1/5)"
     * - Verifies internal int[] return structure
     */
    @Test
    public void testMessageParts_InternalLogic_Parentheses() throws Exception {
        // reuse valid input from BlackBox testRangeWithParentheses
        SubjectParser parser = new SubjectParser("100 Message (1/5)");

        // Use Reflection to access private method messageParts()
        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        int[] result = (int[]) method.invoke(parser);

        assertNotNull("Should return int array", result);
        assertEquals("Array size should be 2", 2, result.length);
        assertEquals("Low range internal value", 1, result[0]);
        assertEquals("High range internal value", 5, result[1]);
    }

    /**
     * White-Box Test for private method messageParts()
     * Logic Covered:
     * - Exception Handling & Recovery (Catch Block 1 -> Try Block 2)
     * - The code attempts to parse '(' first. If it fails (due to index/format), it catches and tries '['.
     * - Uses input from BlackBox: "100 Message [10/20]"
     */
    @Test
    public void testMessageParts_InternalLogic_Brackets() throws Exception {
        // reuse valid input from BlackBox testRangeWithBrackets
        SubjectParser parser = new SubjectParser("100 Message [10/20]");

        // Use Reflection to access private method messageParts()
        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        int[] result = (int[]) method.invoke(parser);

        assertNotNull("Should return int array", result);
        assertEquals("Low range internal value", 10, result[0]);
        assertEquals("High range internal value", 20, result[1]);
    }


    /**
     * White-Box Test for private method messageParts()
     * Logic Covered:
     * - Outer Exception Handling (Generic Catch Block)
     * - Forces a NullPointerException inside the method to verify the outer catch block returns null.
     * - Input: null Subject passed to constructor (Boundary value analysis not fully explicit in BB)
     */
    @Test
    public void testMessageParts_OuterException_NullSubject() throws Exception {
        SubjectParser parser = new SubjectParser(null);

        Method method = SubjectParser.class.getDeclaredMethod("messageParts");
        method.setAccessible(true);
        int[] result = (int[]) method.invoke(parser);

        assertNull("Should return null when unexpected exception (NPE) occurs", result);
    }

    /**
     * White-Box Test for getId()
     * Logic Covered:
     * - Exception Handling
     * - StringTokenizer throws NPE if subject is null. The method catches 'Exception' and returns -1.
     * - This covers the catch block in getId() explicitly.
     */
    @Test
    public void testGetId_ExceptionPath_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        // Direct call, no reflection needed for public method, but logic coverage requires specific input
        assertEquals("Should return -1 when Subject is null", -1L, parser.getId());
    }

    /**
     * White-Box Test for getTitle()
     * Logic Covered:
     * - Exception Handling
     * - Subject.indexOf throws NPE if Subject is null. Method catches Exception and returns null.
     */
    @Test
    public void testGetTitle_ExceptionPath_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertNull("Should return null when Subject is null", parser.getTitle());
    }

}