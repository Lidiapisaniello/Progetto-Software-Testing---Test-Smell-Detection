/*
Nome: Matteo 
Cognome: De Luca 
Username: matteo.deluca3@studenti.unina.it
UserID: 621
Date: 28/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestSubjectParser {

    private static SubjectParser sharedParser;
    private SubjectParser parser;

    @BeforeClass
    public static void setUpClass() {
        sharedParser = new SubjectParser("0 Test (1/1)");
    }

    @AfterClass
    public static void tearDownClass() {
        sharedParser = null;
    }

    @Before
    public void setUp() {
        parser = new SubjectParser("12345 Messaggio di prova (3/10)");
    }

    @After
    public void tearDown() {
        parser = null;
    }

    @Test
    public void testGetId_valido() {
        long id = parser.getId();
        assertEquals(12345L, id);
    }

    @Test
    public void testGetId_nonValido() {
        SubjectParser p = new SubjectParser("abc nessun id valido");
        long id = p.getId();
        assertEquals(-1L, id);
    }

    @Test
    public void testGetTitle_eRangeString_parentesi() {
        String title = parser.getTitle();
        String range = parser.getRangeString();

        assertEquals("Messaggio di prova ", title);
        assertEquals("(3/10)", range);
    }

    @Test
    public void testGetThisRange_eGetUpperRange_parentesi() {
        int lower = parser.getThisRange();
        int upper = parser.getUpperRange();

        assertEquals(3, lower);
        assertEquals(10, upper);
    }

    @Test
    public void testGetTitle_eRangeString_brackets() {
        SubjectParser p = new SubjectParser("999 Titolo alternativo [4/7]");

        String title = p.getTitle();
        String range = p.getRangeString();

        assertEquals("Titolo alternativo ", title);
        assertEquals("[4/7]", range);

        assertEquals(4, p.getThisRange());
        assertEquals(7, p.getUpperRange());
    }

    @Test
    public void testGetRangeSenzaRange() {
        SubjectParser p = new SubjectParser("42 Titolo senza range");
        String title = p.getTitle();
        String range = p.getRangeString();

        assertEquals("Titolo senza range", title);
        assertNull(range);

        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
    }
}


						