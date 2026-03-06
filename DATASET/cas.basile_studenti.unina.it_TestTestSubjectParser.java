/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Castrese
Cognome: Basile
Username: cas.basile@studenti.unina.it
UserID: 466
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
    
    // Non è necessario un campo istanza fisso perché ogni test
    // istanzia il parser con una stringa diversa per esercitare diverse logiche.

    @BeforeClass
    public static void setUpClass() {
        // Setup statico non necessario
    }
                
    @AfterClass
    public static void tearDownClass() {
        // Teardown statico non necessario
    }
                
    @Before
    public void setUp() {
        // Setup per metodo non necessario, istanziamo nel test
    }
                
    @After
    public void tearDown() {
        // Teardown non necessario
    }

    // ==========================================
    // TEST: ID Parsing (getId)
    // ==========================================

    @Test
    public void testGetId_Valid() {
        // Stringa che inizia con un numero valido
        SubjectParser parser = new SubjectParser("12345 Test Subject");
        assertEquals(12345, parser.getId());
    }

    @Test
    public void testGetId_Invalid_Character() {
        // Stringa che inizia con caratteri non numerici
        SubjectParser parser = new SubjectParser("ABC Test Subject");
        // Il catch restituisce -1
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testGetId_Empty() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1, parser.getId());
    }

    // ==========================================
    // TEST: Range Parsing with Round Brackets ( )
    // ==========================================

    @Test
    public void testRoundBracketsRange() {
        // Formato (Low/High)
        // Logica: getTitle analizza la stringa, trova il range, setta RangeString
        SubjectParser parser = new SubjectParser("100 My Title (1/10)");
        
        // Verifica RangeString
        assertEquals("(1/10)", parser.getRangeString());
        
        // Verifica Parsing Numerico (messageParts logic)
        assertEquals(1, parser.getThisRange());  // Lower
        assertEquals(10, parser.getUpperRange()); // Upper
        
        // Verifica che il titolo sia stato pulito (logica complessa di getTitle)
        // La logica di getTitle ricostruisce la stringa escludendo il range se trovato
        String title = parser.getTitle();
        assertTrue(title.contains("My Title"));
        assertFalse(title.contains("(1/10)"));
    }

    // ==========================================
    // TEST: Range Parsing with Square Brackets [ ]
    // ==========================================

    @Test
    public void testSquareBracketsRange() {
        // Formato [Low/High] - Attiva il blocco catch interno di messageParts
        SubjectParser parser = new SubjectParser("200 Another Title [5/20]");
        
        // Verifica RangeString
        assertEquals("[5/20]", parser.getRangeString());
        
        // Verifica Parsing Numerico
        assertEquals(5, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
        
        // Verifica rimozione dal titolo
        String title = parser.getTitle();
        assertTrue(title.contains("Another Title"));
        assertFalse(title.contains("[5/20]"));
    }

    // ==========================================
    // TEST: No Range or Malformed Range
    // ==========================================

    @Test
    public void testNoRangePresent() {
        SubjectParser parser = new SubjectParser("300 Just A Normal Title");
        
        // RangeString dovrebbe essere null o gestito internamente
        // Se RangeString è null, getRangeString chiama getTitle. 
        // Se non trova range, RangeString rimane null.
        assertNull(parser.getRangeString());
        
        // Valori di default definiti nel costruttore (1, 1)
        // Poiché messageParts ritorna null/exception, i getter ritornano i valori correnti (1, 1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
        
        // Il titolo dovrebbe essere completo (meno l'ID iniziale che viene strippato in getTitle)
        // getTitle: Subject.substring(Subject.indexOf(" ") + 1...)
        assertEquals("Just A Normal Title", parser.getTitle());
    }

    @Test
    public void testMalformedRange_Letters() {
        // Formato che sembra un range ma contiene lettere
        // La logica di getTitle controlla Character.isDigit. Se fallisce, non lo considera range.
        SubjectParser parser = new SubjectParser("400 Title (A/B)");
        
        // Non dovrebbe essere rilevato come RangeString valido
        assertNull(parser.getRangeString());
        assertEquals("Title (A/B)", parser.getTitle());
    }

    @Test
    public void testMalformedRange_Incomplete() {
        // Parentesi aperta ma non chiusa correttamente o sintassi errata per messageParts
        // messageParts si aspetta "/"
        SubjectParser parser = new SubjectParser("500 Title (1-10)"); 
        
        // getTitle cerca '/' -> if (tmpbuf.toString().indexOf("/") != -1)
        // Qui non c'è slash, quindi FoundRange = false.
        assertNull(parser.getRangeString());
    }

    // ==========================================
    // TEST: Edge Cases & Exception Handling in messageParts
    // ==========================================

    @Test
    public void testMixedContent() {
        // Caso complesso per getTitle mainloop
        SubjectParser parser = new SubjectParser("600 Title [10/20] tail");
        
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
        assertEquals("[10/20]", parser.getRangeString());
    }
    
    @Test
    public void testGetTitle_NoSpaceAfterID() {
        // Subject.indexOf(" ") potrebbe essere -1
        SubjectParser parser = new SubjectParser("123");
        // getTitle fa substring(indexOf(" ")+1). Se -1 -> +1 = 0. Tutta la stringa.
        assertEquals("123", parser.getTitle());
    }
    
    @Test
    public void testMessageParts_ExceptionHandling() {
        // Forziamo un caso in cui getTitle trova un range, ma messageParts fallisce nel parsing
        // getTitle richiede ')' o ']' e cifre o '/'.
        // Proviamo "(1/)" -> è valido per getTitle, ma Integer.parseInt("") fallirà in messageParts
        
        SubjectParser parser = new SubjectParser("700 Title (1/)");
        // getTitle dovrebbe identificarlo come RangeString perché ha '/' e cifre/simboli validi
        String range = parser.getRangeString();
        assertEquals("(1/)", range);
        
        // messageParts fallirà nel parsing di "high" (vuoto) -> Exception -> ritorna null
        // I valori rimangono quelli di default (1, 1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
}