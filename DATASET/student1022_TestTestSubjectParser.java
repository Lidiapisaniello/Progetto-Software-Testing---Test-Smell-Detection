import static org.junit.Assert.*;
import org.junit.Test;

public class TestSubjectParser {

    // --- TEST ID PARSING ---

    @Test
    public void testGetId_Valid() {
        SubjectParser sp = new SubjectParser("12345 Simple Title");
        assertEquals(12345L, sp.getId());
    }

    @Test
    public void testGetId_InvalidNumeric() {
        SubjectParser sp = new SubjectParser("ABC Simple Title");
        assertEquals(-1L, sp.getId());
    }

    @Test
    public void testGetId_Empty() {
        SubjectParser sp = new SubjectParser("");
        assertEquals(-1L, sp.getId());
    }

    @Test
    public void testGetId_Null() {
        SubjectParser sp = new SubjectParser(null);
        assertEquals(-1L, sp.getId());
    }

    @Test
    public void testGetId_OnlyNumber() {
        SubjectParser sp = new SubjectParser("999");
        assertEquals(999L, sp.getId());
    }

    // --- TEST RANGE PARSING (PARENTHESES) ---

    @Test
    public void testRange_Parentheses_Valid() {
        SubjectParser sp = new SubjectParser("100 My Book (5/20)");
        assertEquals(5, sp.getThisRange());
        assertEquals(20, sp.getUpperRange());
        assertEquals("(5/20)", sp.getRangeString());
        // Verifica che il titolo sia pulito (spazio finale trim o mantenuto a seconda logica)
        // La logica backward costruisce il titolo escludendo il range
        assertEquals("My Book ", sp.getTitle()); 
    }

    @Test
    public void testRange_Parentheses_ParsingError() {
        // Caso: (A/B) -> parseInt fallisce, catch interno, prova [], fallisce, return null
        SubjectParser sp = new SubjectParser("100 Bad Range (A/B)");
        // Default values
        assertEquals(1, sp.getThisRange()); 
        assertEquals(1, sp.getUpperRange());
    }

    // --- TEST RANGE PARSING (BRACKETS) ---

    @Test
    public void testRange_Brackets_Valid() {
        SubjectParser sp = new SubjectParser("100 My Book [3/15]");
        assertEquals(3, sp.getThisRange());
        assertEquals(15, sp.getUpperRange());
        assertEquals("[3/15]", sp.getRangeString());
        assertEquals("My Book ", sp.getTitle());
    }

    // --- TEST RANGE LOGIC & EDGE CASES ---

    @Test
    public void testRange_Mixed_BackwardScan() {
        // La classe scansiona al contrario. Dovrebbe trovare l'ultimo range chiuso.
        // Qui trova prima [3/4] perché è alla fine.
        SubjectParser sp = new SubjectParser("100 Title (1/2) [3/4]");
        assertEquals(3, sp.getThisRange());
        assertEquals(4, sp.getUpperRange());
        assertEquals("[3/4]", sp.getRangeString());
        // Nota: il titolo includerà il primo range perché non è stato processato come tale
        assertEquals("Title (1/2) ", sp.getTitle());
    }

    @Test
    public void testRange_Fake_NoSlash() {
        // Sembra un range ma manca la slash.
        // getTitle: trova ')', scansiona indietro, non trova '/', quindi lo tratta come testo normale.
        SubjectParser sp = new SubjectParser("100 Title (2022)");
        assertEquals("Title (2022)", sp.getTitle());
        assertNull(sp.getRangeString()); // RangeString rimane null
        assertEquals(1, sp.getThisRange()); // Default
    }
    
    @Test
    public void testRange_Fake_Letters() {
        // Contiene lettere, quindi Character.isDigit fallisce nel loop di getTitle
        SubjectParser sp = new SubjectParser("100 Title (Draft)");
        assertEquals("Title (Draft)", sp.getTitle());
        assertNull(sp.getRangeString());
    }

    @Test
    public void testRange_Malformed_OpenOnly() {
        SubjectParser sp = new SubjectParser("100 Title (1/10");
        assertEquals("Title (1/10", sp.getTitle());
        assertEquals(1, sp.getThisRange());
    }

    @Test
    public void testRange_Malformed_CloseOnly() {
        SubjectParser sp = new SubjectParser("100 Title 1/10)");
        // Trova ')', cerca '(', non lo trova, loop finisce.
        assertEquals("Title 1/10)", sp.getTitle());
        assertEquals(1, sp.getThisRange());
    }

    // --- TEST EXCEPTION BRANCHES IN MESSAGEPARTS ---
    
    @Test
    public void testMessageParts_NullRangeString_Handled() {
        // Se Subject è tale che getRangeString restituisce null
        SubjectParser sp = new SubjectParser("100 Just Title");
        assertNull(sp.getRangeString());
        
        // messageParts chiamerà getRangeString() -> null.
        // mainrange.lastIndexOf("(") lancerà NullPointerException catturata.
        assertEquals(1, sp.getThisRange()); // Default non toccato
    }
    
    @Test
    public void testMessageParts_CatchBlock_BracketsFallback() {
        // Questo test mira a coprire il catch(Exception inte) dentro messageParts
        // Dobbiamo ingannare getTitle per fargli credere che sia un range valido,
        // ma poi far fallire il parsing dei numeri in messageParts.
        
        // Se scriviamo (1/A), getTitle potrebbe accettarlo se 'A' non viene controllato strettamente come digit 
        // nel loop di getTitle? 
        // getTitle controlla Character.isDigit(nextchar) == false && nextchar != '/'
        // Quindi (1/A) verrebbe scartato da getTitle e trattato come titolo.
        
        // Proviamo a forzare l'entrata nel catch usando un range valido per getTitle ma invalido per parseInt?
        // Integer.parseInt accetta solo cifre.
        // getTitle accetta cifre e '/'.
        // È difficile entrare nel catch interno se getTitle e parseInt hanno regole simili, 
        // MA getRangeString usa getTitle per popolare la variabile.
        
        // Se usiamo i brackets [1/A], getTitle lo scarta.
        
        // L'unico modo per avere un RangeString popolato che poi fallisce parseInt 
        // è se RangeString è stato settato male o se parseInt è più restrittivo di getTitle.
        // Fortunatamente, getTitle permette SOLO cifre e '/'.
        // Ma parseInt fallisce se ci sono spazi? getTitle non ammette spazi.
        // parseInt fallisce se il numero è troppo grande (Overflow)?
        
        String bigNum = "2147483648"; // Max Int + 1
        SubjectParser sp = new SubjectParser("100 Title (" + bigNum + "/1)");
        
        // getTitle accetta cifre e /, quindi accetta la stringa.
        String rs = sp.getRangeString(); 
        assertNotNull(rs);
        
        // Ora messageParts prova a fare parseInt(bigNum). Fallisce (NumberFormatException).
        // Entra nel catch(inte).
        // Prova a fare il parsing con [ ]. Fallisce.
        // Ritorna null.
        assertEquals(1, sp.getThisRange());
    }
    
    @Test
    public void testGetRangeString_Exception() {
        // getRangeString chiama getTitle. 
        // Se Subject è null, getTitle lancia eccezione, getRangeString la cattura e ritorna null.
        SubjectParser sp = new SubjectParser(null);
        assertNull(sp.getRangeString());
    }
    
    @Test
    public void testGetTitle_Exception() {
         // Subject null gestito dal catch in getTitle
         SubjectParser sp = new SubjectParser(null);
         assertNull(sp.getTitle());
    }
    
    @Test
    public void testSubStringNoSpace() {
        // Copertura Subject.substring(Subject.indexOf(" ") + 1)
        // Se non c'è spazio, indexOf = -1, +1 = 0. Prende tutta la stringa.
        SubjectParser sp = new SubjectParser("OnlyOneWord");
        // getTitle proverà a cercare range in "OnlyOneWord"
        assertEquals("OnlyOneWord", sp.getTitle());
    }
}				