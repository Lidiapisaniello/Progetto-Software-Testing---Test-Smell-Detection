/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Giuseppe"
Cognome: "Miraglia"
Username: giusepp.miraglia@studenti.unina.it
UserID: 766
Date: 22/11/2025
*/

import org.junit.Test;
import org.example.SubjectParser;
import static org.junit.Assert.*;

public class LLM_SubjectParser {

    @Test
    public void testGetIdValid() {
        SubjectParser parser = new SubjectParser("12345");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetIdInvalid() {
        SubjectParser parser = new SubjectParser("abc");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetIdNull() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetTitleWithRoundBrackets() {
        SubjectParser parser = new SubjectParser("123 Title (1/2)");
        assertEquals("Title ", parser.getTitle());
        assertEquals("(1/2)", parser.getRangeString());
    }

    @Test
    public void testGetTitleWithSquareBrackets() {
        SubjectParser parser = new SubjectParser("123 Title [3/4]");
        assertEquals("Title ", parser.getTitle());
        assertEquals("[3/4]", parser.getRangeString());
    }

    @Test
    public void testGetTitleNoRange() {
        SubjectParser parser = new SubjectParser("123 Title");
        assertEquals("Title", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitleMalformed() {
        // Covers logic where it finds brackets but content is not a range
        SubjectParser parser = new SubjectParser("123 Title (abc)");
        assertEquals("Title (abc)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitleException() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    @Test
    public void testRangesRoundBrackets() {
        SubjectParser parser = new SubjectParser("123 Title (10/20)");
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testRangesSquareBrackets() {
        SubjectParser parser = new SubjectParser("123 Title [30/40]");
        assertEquals(30, parser.getThisRange());
        assertEquals(40, parser.getUpperRange());
    }

    @Test
    public void testRangesInvalidFormat() {
        SubjectParser parser = new SubjectParser("123 Title (a/b)");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRangesPartial() {
        SubjectParser parser = new SubjectParser("123 Title (1/)");
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testGetRangeStringDirectly() {
        SubjectParser parser = new SubjectParser("123 Title (5/6)");
        assertEquals("(5/6)", parser.getRangeString());
    }

    @Test
    public void testGetRangeStringException() {
        // Force exception in getRangeString by overriding getTitle to throw
        SubjectParser parser = new SubjectParser("test") {
            @Override
            public String getTitle() {
                throw new RuntimeException("Forced Exception");
            }
        };
        assertNull(parser.getRangeString());
    }

    @Test
    public void testMessagePartsException() {
        // Force exception in messageParts by overriding getRangeString to throw
        SubjectParser parser = new SubjectParser("test") {
            @Override
            public String getRangeString() {
                throw new RuntimeException("Forced Exception");
            }
        };
        // getThisRange calls messageParts
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testMessagePartsNullRange() {
        // If getRangeString returns null, messageParts throws NPE internally and
        // returns null
        SubjectParser parser = new SubjectParser("Title No Range");
        assertEquals(1, parser.getThisRange());
    }
}
