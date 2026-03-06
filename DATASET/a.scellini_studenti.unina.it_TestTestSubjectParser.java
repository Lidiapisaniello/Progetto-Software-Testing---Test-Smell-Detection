/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonella"
Cognome: "Scellini"
Username: a.scellini@studenti.unina.it
UserID: 165
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    private static final int DEFAULT_RANGE = 1;
    private static final int UNDEFINED_ID = -1;

    @Test
    public void testConstructor_InitialRangesAreOne() {
        SubjectParser parser = new SubjectParser("Any valid subject");
        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testGetId_ValidID() {
        SubjectParser parser = new SubjectParser("12345 This is the title");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetId_NonNumericID_ReturnsUndefined() {
        SubjectParser parser = new SubjectParser("ABC Title");
        assertEquals(UNDEFINED_ID, parser.getId());
    }

    @Test
    public void testGetId_EmptySubject_ReturnsUndefined() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(UNDEFINED_ID, parser.getId());
    }

    @Test
    public void testGetId_OnlySpaces_ReturnsUndefined() {
        SubjectParser parser = new SubjectParser("    ");
        assertEquals(UNDEFINED_ID, parser.getId());
    }

    @Test
    public void testGetId_SubjectWithoutSpace() {
        SubjectParser parser = new SubjectParser("1");
        assertEquals(1L, parser.getId());
    }


    @Test
    public void testParenthesesRange_ValidFormat() {
        SubjectParser parser = new SubjectParser("1 Test subject (1/10)");

      
        assertEquals("Test subject ", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());

        assertEquals(1, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void testBracketRange_ValidFormat() {
        SubjectParser parser = new SubjectParser("2 Title with [5/20]");

        assertEquals("Title with ", parser.getTitle());
        assertEquals("[5/20]", parser.getRangeString());

        assertEquals(5, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testMalformedParenthesesRange_NotRecognized() {
        SubjectParser parser = new SubjectParser("3 Malformed range (1/a)");

        assertEquals("Malformed range (1/a)", parser.getTitle());
        assertNull(parser.getRangeString());

        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testNoRangeInSubject() {
        SubjectParser parser = new SubjectParser("4 Simple title without range");

        assertEquals("Simple title without range", parser.getTitle());
        assertNull(parser.getRangeString());

        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testTitleOnlyId() {
        SubjectParser parser = new SubjectParser("1");

        assertEquals("1", parser.getTitle());
        assertNull(parser.getRangeString());

        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testRangeAtStart_IsUsedAsRange() {
        SubjectParser parser = new SubjectParser("5 (1/10) Title");

        assertEquals(" Title", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());

        assertEquals(1, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void testRangeNegativeBracket_NotRecognizedAsRange() {
        SubjectParser parser = new SubjectParser("8 Range [-1/1000000]");

        assertEquals("Range [-1/1000000]", parser.getTitle());
        assertNull(parser.getRangeString());

        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testMultipleRanges_UsesLastValidRange() {
        SubjectParser parser = new SubjectParser("9 Title (1/5) (10/20)");

        assertEquals("Title (1/5) ", parser.getTitle());
        assertEquals("(10/20)", parser.getRangeString());

        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testRangeWithSpacesInside_NotRecognized() {
        SubjectParser parser = new SubjectParser("10 Title [ 1 / 10 ]");

        assertEquals("Title [ 1 / 10 ]", parser.getTitle());
        assertNull(parser.getRangeString());

        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }


    @Test
    public void testGetRange_DirectCallTriggersTitleParsing() {
        SubjectParser parser = new SubjectParser("1 Test subject (1/10)");

        assertEquals(1, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());

        assertEquals("Test subject ", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());
    }
}
