import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // ==========================================
    // 1. TEST getId
    // ==========================================

    @Test
    public void getIdValidIdTest() {
        // Scenario: ID numerico valido all'inizio della stringa
        String input = "12345 Oggetto della mail";
        SubjectParser parser = new SubjectParser(input);
        
        long result = parser.getId();
        
        assertEquals(12345L, result);
    }

    @Test
    public void getIdInvalidIdTest() {
        // Scenario: La stringa non inizia con un numero
        String input = "NonUnNumero Oggetto";
        SubjectParser parser = new SubjectParser(input);
        
        long result = parser.getId();
        
        // Il catch restituisce -1
        assertEquals(-1L, result);
    }

    @Test
    public void getIdNullSubjectTest() {
        // Scenario: Subject è null
        SubjectParser parser = new SubjectParser(null);
        
        long result = parser.getId();
        
        // StringTokenizer lancia NullPointerException, catch restituisce -1
        assertEquals(-1L, result);
    }

    // ==========================================
    // 2. TEST getTitle & getRangeString
    // ==========================================

    @Test
    public void getTitleWithParenthesesRangeTest() {
        // Scenario: Range valido con parentesi tonde (1/10)
        String input = "100 Titolo del messaggio (1/10)";
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();

        // Nota: getTitle rimuove il range dalla stringa originale
        assertEquals("Titolo del messaggio ", title);
        assertEquals("(1/10)", range);
    }

    @Test
    public void getTitleWithBracketsRangeTest() {
        // Scenario: Range valido con parentesi quadre [2/20]
        String input = "100 Titolo con quadre [2/20]";
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        assertEquals("Titolo con quadre ", title);
        assertEquals("[2/20]", range);
    }

    @Test
    public void getTitleNoRangeTest() {
        // Scenario: Nessun range presente nel soggetto
        String input = "100 Titolo semplice senza range";
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        assertEquals("Titolo semplice senza range", title);
        // RangeString rimane null se non trovato
        assertNull(range);
    }

    @Test
    public void getTitleWithMalformedRangeContentTest() {
        // Scenario: Parentesi presenti ma contenuto non valido (es. lettere invece di numeri)
        // Il loop in getTitle controlla Character.isDigit, quindi dovrebbe ignorarlo e considerarlo parte del titolo
        String input = "100 Titolo (A/B)";
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        // Viene considerato parte del titolo perché contiene caratteri non numerici
        assertEquals("Titolo (A/B)", title);
        assertNull(range);
    }
    
    @Test
    public void getTitleWithIncompleteRangeTest() {
        // Scenario: Parentesi chiusa ma non aperta correttamente o formato errato
        String input = "100 Titolo 1/10)"; 
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        // Il parser cerca l'apertura '(' se trova ')'. Se non la trova loop finisce.
        assertEquals("Titolo 1/10)", title);
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleExceptionHandlingTest() {
        // Scenario: Subject null passato al metodo getTitle
        // Provoca eccezione in substring o indexOf, catturata e ritorna null
        SubjectParser parser = new SubjectParser(null);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        assertNull(title);
        assertNull(range);
    }
    
    @Test
    public void getTitleNoSpaceSeparatorTest() {
        // Scenario: Subject senza spazi ("Subject.indexOf(" ") + 1" lancerà eccezione o substring fallirà)
        String input = "SoloUnaParola";
        SubjectParser parser = new SubjectParser(input);
        
        String title = parser.getTitle(); // Lancia IndexOutOfBounds internamente
        
        assertNull(title);
    }

    // ==========================================
    // 3. TEST getThisRange & getUpperRange
    // ==========================================

    @Test
    public void getThisRangeParenthesisSuccessTest() {
        // Scenario: Estrazione LowerRange da parentesi tonde
        String input = "1 Test (5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getThisRange();
        
        assertEquals(5, result);
    }

    @Test
    public void getUpperRangeParenthesisSuccessTest() {
        // Scenario: Estrazione UpperRange da parentesi tonde
        String input = "1 Test (5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getUpperRange();
        
        assertEquals(10, result);
    }

    @Test
    public void getThisRangeBracketsSuccessTest() {
        // Scenario: Estrazione LowerRange da parentesi quadre
        // Questo copre il blocco catch(Exception inte) interno a messageParts
        String input = "1 Test [3/9]";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getThisRange();
        
        assertEquals(3, result);
    }

    @Test
    public void getUpperRangeBracketsSuccessTest() {
        // Scenario: Estrazione UpperRange da parentesi quadre
        String input = "1 Test [3/9]";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getUpperRange();
        
        assertEquals(9, result);
    }

    @Test
    public void getThisRangeDefaultsTest() {
        // Scenario: Nessun range valido, deve ritornare il default (1)
        String input = "1 Test senza range";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getThisRange();
        
        assertEquals(1, result); // Valore di default nel costruttore
    }

    @Test
    public void getUpperRangeDefaultsTest() {
        // Scenario: Nessun range valido, deve ritornare il default (1)
        String input = "1 Test senza range";
        SubjectParser parser = new SubjectParser(input);
        
        int result = parser.getUpperRange();
        
        assertEquals(1, result);
    }

    @Test
    public void messagePartsParsingFailureTest() {
        // Scenario: getRangeString ritorna qualcosa, ma messageParts fallisce il parsing numerico.
        // Questo è complesso perché getTitle fa già validazione.
        // Dobbiamo creare una situazione dove getTitle accetta la stringa, ma Integer.parseInt fallisce.
        // getTitle accetta digit e '/'.
        // Es: (1//2) -> getTitle accetta. StringTokenizer su "/" -> token1="1", token2="" (o errore su nextToken).
        
        String input = "1 Test (1//2)"; 
        SubjectParser parser = new SubjectParser(input);
        
        // getTitle estrarrà (1//2). messageParts proverà a parsare.
        // parseInt fallirà o st.nextToken fallirà.
        // messageParts ritornerà null.
        // getThisRange catturerà l'eccezione o vedrà null e non aggiornerà LowerRange.
        
        int lower = parser.getThisRange();
        int upper = parser.getUpperRange();
        
        assertEquals(1, lower); // Default
        assertEquals(1, upper); // Default
    }
    
    @Test
    public void messagePartsNestedExceptionTest() {
        // Scenario: Copertura del blocco catch(Exception subE) in messageParts.
        // Serve un caso che fallisca il parsing con () E fallisca il parsing con [].
        // Se passiamo input che genera eccezione in getRangeString (es. null), messageParts ritorna null.
        
        SubjectParser parser = new SubjectParser(null); // RangeString sarà null/eccezione
        
        int lower = parser.getThisRange();
        assertEquals(1, lower);
    }
}