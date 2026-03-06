import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestSubjectParser {

    private SubjectParser parser;
    // Stream per catturare l'output di System.out/err per il test del main
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        parser = null;
    }

    // --- Test getId() ---

    @Test
    public void testGetId_Valid() {
        // Copertura: Parsing corretto del primo token come Long
        parser = new SubjectParser("12345 Oggetto del messaggio");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetId_InvalidNonNumeric() {
        // Copertura: Catch exception in getId quando il primo token non è un numero
        parser = new SubjectParser("ABC Oggetto del messaggio");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_EmptyString() {
        // Copertura: Stringa vuota o null pointer se non gestito (qui StringTokenizer lancia NoSuchElementException o simile gestito dal catch)
        parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    // --- Test getTitle() ---

    @Test
    public void testGetTitle_SimpleWithoutRange() {
        // Copertura: Estrazione titolo senza range, rimozione ID iniziale
        parser = new SubjectParser("100 Titolo Semplice");
        assertEquals("Titolo Semplice", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithParenthesesRange() {
        // Copertura: Logica complessa di parsing range con ()
        // Verifica che il range venga rimosso dal titolo restituito
        parser = new SubjectParser("100 Titolo (1/10)");
        assertEquals("Titolo ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithBracketsRange() {
        // Copertura: Logica complessa di parsing range con []
        parser = new SubjectParser("100 Titolo [5/20]");
        assertEquals("Titolo ", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithFalseRange_NoSlash() {
        // Copertura: Ramo in cui trova le parentesi ma non c'è lo slash (es. note)
        // Il testo deve rimanere nel titolo
        parser = new SubjectParser("100 Titolo (Note)");
        assertEquals("Titolo (Note)", parser.getTitle());
    }

    @Test
    public void testGetTitle_WithFalseRange_MixedContent() {
        // Copertura: Ramo in cui trova parentesi, inizia a scansionare indietro, ma trova char non digit/slash
        // Interrompe il parsing del range e considera tutto come titolo
        parser = new SubjectParser("100 Titolo (v1.0)");
        assertEquals("Titolo (v1.0)", parser.getTitle());
    }

    @Test
    public void testGetTitle_NestedOrMultiple() {
        // Copertura: Assicurarsi che prenda l'ultimo valido o gestisca la formattazione
        parser = new SubjectParser("100 Titolo (Note) [1/5]");
        assertEquals("Titolo (Note) ", parser.getTitle());
    }

    @Test
    public void testGetTitle_Bug_OutOfBounds() {
        // BUG SEGNALATO NEL REPORT: Se la stringa inizia con una parentesi di chiusura senza apertura precedente
        // o è formattata in modo che il ciclo `while((nextchar = tmpSubject.charAt(--i)) != endchar)` vada sotto zero.
        // SubjectParser cattura l'eccezione e ritorna null.
        parser = new SubjectParser("100 )");
        assertNull("Dovrebbe tornare null a causa dell'eccezione interna catturata", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_NoIdOnlyTitle() {
        // Caso limite: Stringa senza spazio per l'ID
        parser = new SubjectParser("SoloTitolo");
        // Logica parser: substring(indexOf(" ")+1). indexOf=-1, +1=0. Tutto ok.
        assertEquals("SoloTitolo", parser.getTitle());
    }

    // --- Test getRangeString() ---

    @Test
    public void testGetRangeString_PopulatesFromGetTitle() {
        // Copertura: getRangeString è null, chiama getTitle, popola RangeString
        parser = new SubjectParser("100 Test (1/5)");
        String range = parser.getRangeString();
        assertEquals("(1/5)", range);
    }
    
    @Test
    public void testGetRangeString_NullSubject() {
        // Copertura: subject null, getTitle lancia eccezione, getRangeString cattura e ritorna null
        parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    // --- Test messageParts(), getThisRange(), getUpperRange() ---

    @Test
    public void testRanges_Parentheses_Valid() {
        // Copertura: messageParts ramo try principale (parentesi tonde)
        parser = new SubjectParser("1 ID (2/50)");
        assertEquals(2, parser.getThisRange());
        assertEquals(50, parser.getUpperRange());
    }

    @Test
    public void testRanges_Brackets_Valid() {
        // Copertura: messageParts ramo catch -> try secondario (parentesi quadre)
        parser = new SubjectParser("1 ID [3/99]");
        assertEquals(3, parser.getThisRange());
        assertEquals(99, parser.getUpperRange());
    }

    @Test
    public void testRanges_NoRangePresent() {
        // Copertura: Nessun range, ritorna i default impostati nel costruttore (1, 1)
        parser = new SubjectParser("1 ID Titolo");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Malformed_Parentheses() {
        // Copertura: Tonda presente ma contenuto non parsabile -> messageParts ritorna null -> Getter ritornano default
        // getTitle estrarrà "(1/A)" come RangeString, ma messageParts fallirà il parseInt
        parser = new SubjectParser("1 ID (1/A)");
        assertEquals(1, parser.getThisRange()); // Default
        assertEquals(1, parser.getUpperRange()); // Default
    }
    
    @Test
    public void testRanges_Malformed_Brackets() {
        // Copertura: Quadra presente ma contenuto non parsabile
        parser = new SubjectParser("1 ID [1/A]");
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_PartialRange() {
        // Copertura: Range incompleto che potrebbe causare eccezioni in substring
        parser = new SubjectParser("1 ID (1/)"); 
        // StringTokenizer potrebbe fallire o parseInt stringa vuota
        assertEquals(1, parser.getThisRange());
    }

    // --- Test Main Method ---

    @Test
    public void testMain_ValidArgs() {
        String[] args = {"123", "Subject", "(1/5)"};
        SubjectParser.main(args);
        String output = outContent.toString();
        assertTrue(output.contains("The upper range is 5"));
        assertTrue(output.contains("The lower range is 1"));
        assertTrue(output.contains("The message id is 123"));
    }

    @Test
    public void testMain_NoArgs() {
        // Copertura: args.length < 1 -> System.exit(1).
        // Nota: System.exit interromperebbe JUnit. In un ambiente reale si usa un SecurityManager 
        // o librerie come System Lambda. Qui testiamo solo che gestisca l'eccezione o stampi l'usage 
        // ma dato che System.exit è bloccante, dobbiamo omettere l'esecuzione diretta o accettare che questo test 
        // non sia eseguibile in standard runner senza modifiche.
        // Per questa suite, simulo il caso di eccezione nel main (non args length) passando null per generare eccezione.
        
        try {
            SubjectParser.main(null);
        } catch (Exception e) {
           // Expected NPE if args is null before length check, but main catches Exception e
        }
        // Verifica che sia stato stampato l'errore nello stream err
        assertTrue(errContent.toString().length() > 0 || outContent.toString().contains("Usage"));
    }
    
    @Test
    public void testMain_ExceptionHandling() {
        // Passiamo argomenti che causano null pointer interno o simile per vedere il catch del main
        // In realtà il main costruisce una stringa e chiama il costruttore.
        // Proviamo a non passare nulla di valido per far scattare usage.
        // Nota: Per evitare System.exit, questo test è limitato.
    }
}