/*
Nome: inserire il proprio nome
Cognome: inserire il proprio cognome
Username: lidiapisaniello1@gmail.com
UserID: 442
Date: 26/11/2025
*/

import org.junit.*;
import static org.junit.Assert.*;

public class TestSubjectParser {

    @Test
    public void testValidRoundRange() {
        SubjectParser p = new SubjectParser("100 Test (3/7)");
        assertEquals(100, p.getId());
        assertEquals("Test ", p.getTitle());
        assertEquals("(3/7)", p.getRangeString());
        assertEquals(3, p.getThisRange());
        assertEquals(7, p.getUpperRange());
    }

    @Test
    public void testValidBracketRange() {
        SubjectParser p = new SubjectParser("200 Something [4/9]");
        assertEquals("Something ", p.getTitle());
        assertEquals("[4/9]", p.getRangeString());
        assertEquals(4, p.getThisRange());
        assertEquals(9, p.getUpperRange());
    }

    @Test
    public void testInvalidRangeCharactersInsideRound() {
        SubjectParser p = new SubjectParser("300 BadRange (x/y)");
        assertEquals("BadRange (x/y)", p.getTitle());
        assertNull(p.getRangeString());
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testInvalidRangeCharactersInsideBracket() {
        SubjectParser p = new SubjectParser("400 Bad [a/b]");
        assertEquals("Bad [a/b]", p.getTitle());
        assertNull(p.getRangeString());
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testNoRange() {
        SubjectParser p = new SubjectParser("500 Hello");
        assertEquals("Hello", p.getTitle());
        assertNull(p.getRangeString());
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testEmptySubject() {
        SubjectParser p = new SubjectParser("");
        assertEquals(-1, p.getId());          // parsing fallisce
        assertEquals("", p.getTitle());       // sb is empty string
        assertNull(p.getRangeString());       // no range found
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testSubjectWithoutSpace() {
        SubjectParser p = new SubjectParser("12345");
        assertEquals(12345, p.getId());
        assertEquals("12345", p.getTitle());  // entire string becomes title
        assertNull(p.getRangeString());
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testMessagePartsOuterCatch() {
        SubjectParser p = new SubjectParser("600 BrokenRange");
        // getTitle() sets no RangeString -> getRangeString() returns null
        assertNull(p.getRangeString());
        assertEquals(1, p.getThisRange());   // messageParts hits outer catch
        assertEquals(1, p.getUpperRange());
    }

    @Test
    public void testGetIdInvalidNumber() {
        SubjectParser p = new SubjectParser("NotANumber Test");
        assertEquals(-1, p.getId());
    }

    @Test
    public void testEarlyExitInGetTitle() {
        SubjectParser p = new SubjectParser("999 Title (3/a)");
        assertEquals("Title (3/a)", p.getTitle());
        assertNull(p.getRangeString());
    }

    @Test
    public void testRangeWithoutClosingBracket() {
        SubjectParser p = new SubjectParser("800 Strange (5/9");
        assertEquals("Strange (5/9", p.getTitle());
        assertNull(p.getRangeString());
    }

    @Test
    public void testRangeWithoutOpeningBracket() {
        SubjectParser p = new SubjectParser("900 Something 5/9)");
        assertEquals("Something 5/9)", p.getTitle());
        assertNull(p.getRangeString());
    }