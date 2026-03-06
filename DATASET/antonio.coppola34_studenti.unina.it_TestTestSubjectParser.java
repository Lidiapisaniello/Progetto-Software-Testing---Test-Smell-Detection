/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonio"
Cognome: "Coppola"
Username: antonio.coppola34@studenti.unina.it
UserID: 203
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    private final static int DEFAULT_RANGE = 1;

    // --- Test getId() ---

    @Test
    public void testGetId_Success() {
        // ID numerico valido
        SubjectParser parser = new SubjectParser("123456 Il mio Soggetto (1/10)");
        assertEquals(123456L, parser.getId());
    }

    @Test
    public void testGetId_NegativeIfMissing() {
        // Mancanza dell'ID (StringTokenizer fallisce)
        SubjectParser parser = new SubjectParser("Il mio Soggetto (1/10)");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_NegativeIfNonNumeric() {
        // ID non numerico (Long.parseLong fallisce)
        SubjectParser parser = new SubjectParser("ABCDEF Il mio Soggetto (1/10)");
        assertEquals(-1L, parser.getId());
    }

    // --- Test getTitle() ---

    @Test
    public void testGetTitle_WithParenthesesRange() {
        // Soggetto con intervallo in parentesi tonde
        SubjectParser parser = new SubjectParser("99 Test con Range (1/5)");
        // L'ID viene rimosso, l'intervallo viene rimosso dal ciclo all'indietro
        assertEquals("Test con Range ", parser.getTitle());
        // Nota: la logica originale della classe è imperfetta e lascia uno spazio finale
    }

    @Test
    public void testGetTitle_WithBracketRange() {
        // Soggetto con intervallo in parentesi quadre
        SubjectParser parser = new SubjectParser("99 Test con Range [2/8]");
        assertEquals("Test con Range ", parser.getTitle());
    }

    @Test
    public void testGetTitle_NoRange() {
        // Nessun intervallo presente
        SubjectParser parser = new SubjectParser("100 Soggetto senza range");
        assertEquals("Soggetto senza range", parser.getTitle());
    }

    @Test
    public void testGetTitle_InvalidRangeFormat() {
        // Range incompleto: (1/A)
        SubjectParser parser = new SubjectParser("101 Soggetto (1/A)");
        assertEquals("Soggetto (1/A)", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_RangeWithNonNumericCharacterBeforeSlash() {
        // Caso in cui il loop interno fallisce (testchar non è cifra o '/')
        SubjectParser parser = new SubjectParser("102 Soggetto con carattere A (1/A)");
        // Il parser si aspetta che i caratteri all'interno siano solo numeri e '/'.
        // Quando incontra 'A', esce dal loop interno, aggiungendo il range al titolo.
        assertEquals("Soggetto con carattere A (1/A)", parser.getTitle());
    }

    // --- Test getRangeString() ---

    @Test
    public void testGetRangeString_WithParentheses() {
        SubjectParser parser = new SubjectParser("99 Test con Range (1/5)");
        // getTitle viene chiamato internamente per inizializzare RangeString
        parser.getTitle(); 
        assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_WithBrackets() {
        SubjectParser parser = new SubjectParser("99 Test con Range [2/8]");
        parser.getTitle();
        assertEquals("[2/8]", parser.getRangeString());
    }

    @Test
    public void testGetRangeString_NullIfNoRange() {
        SubjectParser parser = new SubjectParser("100 Soggetto senza range");
        parser.getTitle();
        assertNull(parser.getRangeString());
    }

    // --- Test messageParts() (Copertura della logica di estrazione Intervallo) ---
    // Questi test coprono getThisRange() e getUpperRange() indirettamente.

    @Test
    public void testGetRanges_ParenthesesFormat_Success() {
        SubjectParser parser = new SubjectParser("100 Titolo (5/15)");
        
        // La chiamata a getTitle() è necessaria per popolare RangeString
        parser.getTitle(); 

        // getThisRange/getUpperRange chiamano messageParts()
        assertEquals(5, parser.getThisRange());
        assertEquals(15, parser.getUpperRange());
        
        // Verifica che i valori siano memorizzati
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void testGetRanges_BracketsFormat_Success() {
        SubjectParser parser = new SubjectParser("100 Titolo [2/30]");
        
        parser.getTitle(); 

        // getUpperRange copre il blocco catch(Exception inte) e l'analisi con parentesi quadre
        assertEquals(2, parser.getThisRange());
        assertEquals(30, parser.getUpperRange());
    }

    @Test
    public void testGetRanges_InvalidFormat_FallsBackToDefault() {
        // Formato con parentesi, ma non numerico o incompleto: (A/10)
        SubjectParser parser = new SubjectParser("100 Titolo (A/10)");
        
        parser.getTitle(); // Inizializza RangeString a "(A/10)"

        // Fallisce nel primo blocco try (parseInt di 'A'), entra nel catch(inte)
        // Fallisce nel secondo blocco try (parentesi quadre non presenti), entra nel catch(subE)
        // messageParts() ritorna null.
        
        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testGetRanges_NoRange_FallsBackToDefault() {
        // Nessun range presente, messageParts() ritorna null subito.
        SubjectParser parser = new SubjectParser("100 Titolo senza range");
        
        // getTitle() chiamato, RangeString = null
        
        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }

    @Test
    public void testGetRanges_PartialRangeString_Failure() {
        // RangeString parziale che causa StringIndexOutOfBoundsException nel primo try
        SubjectParser parser = new SubjectParser("100 Titolo (1/");
        
        parser.getTitle(); // RangeString = null (non ha trovato la parentesi chiusa)
        
        assertEquals(DEFAULT_RANGE, parser.getThisRange());
        assertEquals(DEFAULT_RANGE, parser.getUpperRange());
    }
}
						