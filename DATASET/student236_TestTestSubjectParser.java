import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SubjectParser}.
 *
 * The tests try to exercise every public method, covering the
 * different branches (normal flow, malformed input, exception paths).
 */
public class TestSubjectParser {

    private SubjectParser parserValid;          // well‑formed subject
    private SubjectParser parserNoRange;        // missing range part
    private SubjectParser parserBadRange;       // malformed range
    private SubjectParser parserBadId;          // non‑numeric id

    @Before
    public void setUp() {
        // 1️⃣  valid:   "123 My Title (2/5)"
        parserValid = new SubjectParser("123 My Title (2/5)");

     // 2️⃣  no range: "456 Another Title"
        parserNoRange = new SubjectParser("456 Another Title");

        // 3️⃣  bad range syntax: "789 BadTitle (a/b)"
        parserBadRange = new SubjectParser("789 BadTitle (a/b)");

        // 4️⃣  non‑numeric id: "ABC Invalid (1/2)"
        parserBadId = new SubjectParser("ABC Invalid (1/2)");
    }

    /* ------------------------------------------------------------------ *
     *  getId()
     * ------------------------------------------------------------------ */

    @Test
    public void testGetId_Valid() {
        assertEquals(123L, parserValid.getId());
    }

    @Test
    public void testGetId_NonNumericId_ReturnsMinusOne() {
        // The constructor stores the raw string, getId() catches the NumberFormatException
        assertEquals(-1L, parserBadId.getId());
    }

    /* --------------------------------------------------------------- *
     *  getTitle()
     * ------------------------------------------------------------------ */

    @Test
    public void testGetTitle_Valid() {
        // Expected title is everything before the range token
        assertEquals("My Title ", parserValid.getTitle());
    }

    @Test
    public void testGetTitle_NoRange() {
        // When there is no range marker, the whole remainder after the id is the title
        assertEquals("Another Title", parserNoRange.getTitle());
    }

    @Test
    public void testGetTitle_MalformedRange_IgnoresRange() {
        // The parser stops at the first non‑digit/non‑'/' character inside the range,
        // therefore the malformed "(a/b)" is ignored and the title includes the stray chars.
        assertEquals("BadTitle (a/b)", parserBadRange.getTitle());
    }

    /* ------------------------------------------------------------------ *
     *  getRangeString()
     * ------------------------------------------------------------------ */

    @Test
    public void testGetRangeString_Valid() {
        // getTitle() must be called first because RangeString is lazily populated
        parserValid.getTitle();               // forces extraction of RangeString
        assertEquals("(2/5)", parserValid.getRangeString());
    }

    @Test
    public void testGetRangeString_NoRange_ReturnsNull() {
        parserNoRange.getTitle();             // still tries to locate a range
        assertNull(parserNoRange.getRangeString());
    }

    @Test
    public void testGetRangeString_MalformedRange_ReturnsNull() {
        parserBadRange.getTitle();
        assertNull(parserBadRange.getRangeString());
    }

    /* ------------------------------------------------------------------ *
     *  getThisRange()  – lower bound
     * ------------------------------------------------------------------ */

    @Test
    public void testGetThisRange_Valid() {
        // LowerRange defaults to 1, but after parsing it should become 2
        assertEquals(2, parserValid.getThisRange());
    }

    @Test
    public void testGetThisRange_NoRange_KeepsDefault() {
        // No range → messageParts() returns null → lower stays at default 1
        assertEquals(1, parserNoRange.getThisRange());
    }

    @Test
    public void testGetThisRange_MalformedRange_KeepsDefault() {
        // Parsing fails → lower stays at default 1
        assertEquals(1, parserBadRange.getThisRange());
    }

    /* ------------------------------------------------------------------ *
     *  getUpperRange() – upper bound
     * ------------------------------------------------------------------ */

    @Test
    public void testGetUpperRange_Valid() {
        assertEquals(5, parserValid.getUpperRange());
    }

    @Test
    public void testGetUpperRange_NoRange_KeepsDefault() {
        assertEquals(1, parserNoRange.getUpperRange());
    }

    @Test
    public void testGetUpperRange_MalformedRange_KeepsDefault() {
        assertEquals(1, parserBadRange.getUpperRange());
    }

    /* ------------------------------------------------------------------ *
     *  messageParts() – indirect testing via the two public getters above.
     * ------------------------------------------------------------------ */

    @Test
    public void testMessageParts_WithBracketNotation() {
        // Bracket style range: "[3/7]"
        SubjectParser bracketParser = new SubjectParser("321 Title [3/7]");
        // Force title extraction so that RangeString is set
        bracketParser.getTitle();

        // Lower and upper should reflect the bracket values
        assertEquals(3, bracketParser.getThisRange());
        assertEquals(7, bracketParser.getUpperRange());
    }
}