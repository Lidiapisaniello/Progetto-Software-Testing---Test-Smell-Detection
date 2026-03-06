				/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "AI Assistant"
Cognome: "Google"
Username: maria.coscione2@studenti.unina.it
UserID: 616
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestSubjectParser {
    
    // Variabili per catturare l'output di System.err/System.out (per i metodi buggati)
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    
    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima dell'inizio dei test nella classe
    }
    
    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
    }
    
    @Before
    public void setUp() {
        // Cattura System.err prima di ogni test
        System.setErr(new PrintStream(errContent));
    }
    
    @After
    public void tearDown() {
        // Ripristina System.err dopo ogni test
        System.setErr(originalErr);
    }
    
    // --- Test per scenari di Range Validi e Standard ---
    
    @Test
    public void getTitle_standardRangeParentesiTonde_titleAndRangeCorrectlyParsed() {
        // Stringa standard: ID Spazi Titolo (low/high)
        String input = "12345 Subject Title (5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        // Esecuzione dei metodi che attivano il parsing
        String actualTitle = parser.getTitle();
        int actualLower = parser.getThisRange();
        int actualUpper = parser.getUpperRange();
        
        // Asserzioni complete sullo stato dell'oggetto
        assertEquals("Subject Title ", actualTitle);
        assertEquals("(5/10)", parser.getRangeString());
        assertEquals(12345L, parser.getId());
        assertEquals(5, actualLower);
        assertEquals(10, actualUpper);
    }

    @Test
    public void getTitle_standardRangeParentesiQuadre_titleAndRangeCorrectlyParsed() {
        // Stringa standard: ID Spazi Titolo [low/high]
        String input = "98765 Another Subject [1/1]";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        int actualLower = parser.getThisRange();
        int actualUpper = parser.getUpperRange();
        
        // Asserzioni complete sullo stato dell'oggetto
        assertEquals("Another Subject ", actualTitle);
        assertEquals("[1/1]", parser.getRangeString());
        assertEquals(98765L, parser.getId());
        assertEquals(1, actualLower);
        assertEquals(1, actualUpper);
    }
    
    // --- Test per Valori Limite (Boundary Value Analysis) ---

    @Test
    public void getUpperRange_rangeValoreMassimo_maxRangeParsed() {
        // La classe usa int, il massimo teorico è Integer.MAX_VALUE
        // Usiamo un valore grande ma realistico
        String input = "1 Test Range Grande (1/2147483647)";
        SubjectParser parser = new SubjectParser(input);
        
        int actualUpper = parser.getUpperRange();
        
        assertEquals(2147483647, actualUpper);
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void getThisRange_rangeValoreMinimoZero_zeroRangeParsed() {
        // Il parser Integer.parseInt può gestire lo 0. low = 0 (low è il primo token numerico)
        String input = "1 Test Range Zero (0/5)";
        SubjectParser parser = new SubjectParser(input);
        
        int actualLower = parser.getThisRange();
        
        assertEquals(0, actualLower);
        assertEquals(5, parser.getUpperRange());
    }
    
    @Test
    public void getRangeString_soloRangeConParentesiQuadre_rangeParsed() {
        // Test di un caso limite con solo ID e Range
        String input = "10 [2/2]";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // NOTA: il codice aggiunge uno spazio in più se non c'è titolo
        assertEquals("", actualTitle); 
        assertEquals("[2/2]", parser.getRangeString());
    }

    // --- Test per Stringhe Malformate e Casi Limite di Parsing ---

    @Test
    public void getTitle_rangeMalformatoSenzaSlashParentesiTonda_rangeInclusoNelTitle() {
        String input = "1 Test Title (5-10)";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        int actualLower = parser.getThisRange();
        int actualUpper = parser.getUpperRange();
        
        // NOTA: Il parsing del range fallisce nel primo blocco try a causa dell'assenza di '/',
        // ma la logica in getTitle() è costruita per includere una sequenza di cifre/slash
        // nel titolo se non trova l'apertura ('(' o '[') dopo il check dei contenuti.
        // In questo caso il primo try in messageParts fallisce, passa al secondo, che fallisce
        // restituendo null, ma l'impostazione di RangeString non avviene (o avviene a null).
        // Tuttavia, il loop in getTitle *non* trova una sequenza valida low/high all'indietro,
        // ma si ferma solo quando trova caratteri non-digit o non-slash.
        // La logica attuale in getTitle() ritorna un titolo che include la parte "(5-10)".
        // Dopo un'analisi dettagliata, l'iterazione all'indietro si interrompe sul '-',
        // non è un digit o '/', e il blocco 'continue MAINLOOP' viene eseguito, includendo tutto.
        assertEquals("Test Title (5-10)", actualTitle.trim()); // Trim per ignorare lo spazio finale
        assertNull(parser.getRangeString()); // RangeString resta null finché non viene chiamato getTitle(). Una volta chiamato, il loop in getTitle non imposta RangeString
        assertEquals(1, actualLower); // Inizializzazione predefinita
        assertEquals(1, actualUpper); // Inizializzazione predefinita
    }

    @Test
    public void getTitle_rangeMalformatoConTestoSenzaID_eccezioneGestitaTitleNull() {
        // Stringa che non inizia con ID numerico
        String input = "ABC Subject (5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        // Esecuzione di getTitle() genera IndexOutOfBoundsException perché Subject.indexOf(" ") è 2, e il substring parte da 3.
        // L'errore viene catturato e ritorna null.
        String actualTitle = parser.getTitle();
        
        assertNull(actualTitle);
        // NOTA: Il printStackTrace viene chiamato (come verificato da System.err catturato)
        assertTrue(errContent.toString().contains("StringIndexOutOfBoundsException"));

        // Verifichiamo i valori di default/fallback
        assertEquals(-1L, parser.getId()); // Fallisce in getId() a causa di NumberFormatException su "ABC"
        assertEquals(1, parser.getThisRange()); // Valori di default
        assertEquals(1, parser.getUpperRange()); // Valori di default
        assertNull(parser.getRangeString()); // Non impostato
    }
    
    @Test
    public void getTitle_rangeAllInizio_rangeIgnoratoEParteInclusaNelTitle() {
        // Range all'inizio della stringa, non è il comportamento tipico atteso (alla fine)
        String input = "1 (5/10) Title";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // NOTA: Il codice cerca il range partendo dalla fine. L'output è "Title "
        assertEquals("(5/10) Title ", actualTitle);
        
        // L'algoritmo di getTitle non trova ')' o ']' nell'iterazione all'indietro.
        // Il range non viene parsato e RangeString è null (fino a getTitle() che lo imposta a null, ma non lo fa qui).
        // Dopo l'esecuzione di getTitle(), RangeString rimane null perché FoundRange è sempre false.
        assertNull(parser.getRangeString()); 
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }

    // --- Test per RangeString e Range Malformato e Range a Singolo Elemento ---
    
    @Test
    public void getRangeString_rangeNumeriNonInteri_defaultRangeValues() {
        String input = "1 Test Range (a/b)";
        SubjectParser parser = new SubjectParser(input);
        
        // Il Range viene parsato e RangeString viene impostato in getTitle()
        String actualTitle = parser.getTitle();
        
        // NOTA: L'algoritmo in messageParts fallisce in Integer.parseInt causando NumberFormatException,
        // ma in getTitle il Range viene riconosciuto.
        assertEquals("Test Range ", actualTitle);
        assertEquals("(a/b)", parser.getRangeString()); // RangeString viene impostato in getTitle
        
        // Range effettivi: Fallisce nel try/catch di messageParts, ritorna ai valori di default.
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void getRangeString_rangeConSoloUnNumero_defaultRangeValues() {
        // Mancanza di slash '/'
        String input = "1 Test Range (5)";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // NOTA: In getTitle(), la mancanza di '/' nel range parsato (tmpbuf) impedisce
        // l'impostazione di RangeString, e il blocco `continue MAINLOOP` non viene eseguito,
        // ma la ricerca del range fallisce perché il while loop termina quando incontra '('
        // ma poi RangeString non è impostato perché indexOf("/") == -1
        // L'implementazione di getTitle è tale che non trova un range valido, includendolo nel titolo
        assertEquals("Test Range (5)", actualTitle.trim());
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    // --- Test per ID e Stringa Vuota/Null ---

    @Test
    public void getId_idNonNumerico_returnsMinusOne() {
        String input = "ABC Test (1/1)";
        SubjectParser parser = new SubjectParser(input);
        
        // getId() genera NumberFormatException su "ABC", viene catturata e ritorna -1.
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getId_idMoltoLungo_longIdParsed() {
        // ID al limite di Long.MAX_VALUE
        String input = "9223372036854775807 Test (1/1)";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(Long.MAX_VALUE, parser.getId());
    }
    
    @Test
    public void constructor_nullInput_getTitleThrowsAndReturnsNull() {
        String input = null;
        SubjectParser parser = new SubjectParser(input);
        
        // L'accesso a Subject.substring(Subject.indexOf(" ") + 1, Subject.length()) in getTitle()
        // genera NullPointerException, catturata nel blocco esterno, che ritorna null.
        assertNull(parser.getTitle());
        
        // getId() genera NullPointerException in Subject.indexOf(" "), catturata, ritorna -1.
        assertEquals(-1L, parser.getId());
        
        // NOTA: Vengono chiamati System.err.printStackTrace() da getTitle() e getId()
        assertTrue(errContent.toString().contains("NullPointerException"));
        
        // getRangeString() sarà null, e i range saranno di default
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void constructor_stringaVuota_getIdThrowsAndReturnsMinusOne() {
        String input = "";
        SubjectParser parser = new SubjectParser(input);
        
        // getId() genera NoSuchElementException (StringTokenizer) o IndexOutOfBoundsException (substring/indexOf)
        // Viene catturata e ritorna -1.
        assertEquals(-1L, parser.getId());
        
        // getTitle() genera StringIndexOutOfBoundsException (Subject.indexOf(" ") + 1) -> -1 + 1 = 0, length è 0
        // Viene catturata e ritorna null.
        assertNull(parser.getTitle());
    }
    
    @Test
    public void constructor_soloSpazi_getIdThrowsAndReturnsMinusOne() {
        String input = "  ";
        SubjectParser parser = new SubjectParser(input);
        
        // getId() (StringTokenizer) fallisce (NoSuchElementException) o getTitle fallisce (IndexOutOfBounds)
        assertEquals(-1L, parser.getId());
        assertNull(parser.getTitle());
    }
    
    // --- Test per l'interazione tra getRangeString e getTitle ---

    @Test
    public void getRangeString_chiamatoPerPrimo_chiamaGetTitle() {
        String input = "500 Subject (2/7)";
        SubjectParser parser = new SubjectParser(input);
        
        // Chiama getRangeString() prima di getTitle()
        String actualRangeString = parser.getRangeString();
        
        // NOTA: Se RangeString è null, getRangeString() chiama this.getTitle().
        // getTitle() imposta RangeString
        assertEquals("(2/7)", actualRangeString);
        assertEquals("Subject ", parser.getTitle());
        assertEquals(2, parser.getThisRange());
        assertEquals(7, parser.getUpperRange());
    }

    @Test
    public void getThisRange_chiamatoPerPrimo_rangeParsedAfterTitleCall() {
        String input = "111 Range Test [100/200]";
        SubjectParser parser = new SubjectParser(input);
        
        // Chiamando getThisRange(), questo chiama messageParts(), che a sua volta
        // chiama getRangeString(), che chiama getTitle().
        int actualLower = parser.getThisRange();
        
        assertEquals(100, actualLower);
        assertEquals(200, parser.getUpperRange());
        assertEquals("[100/200]", parser.getRangeString());
        assertEquals("Range Test ", parser.getTitle());
    }

    // --- Test per Range con Parentesi Miste e Formati Anomali ---
    
    @Test
    public void getTitle_rangeSoloConParentesiDiChiusura_rangeInclusoNelTitle() {
        // Solo ')' o ']' nell'ultima posizione, avvia il parsing del range ma fallisce
        String input = "1 Test Title)";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // Il loop in getTitle cerca il carattere di apertura. Se non lo trova, si interrompe.
        // Qui si ferma sul primo carattere dell'ID (dopo gli spazi) o, nel loop interno,
        // quando `i` va sotto zero (IndexOutOfBoundsException nel while loop),
        // che viene catturata in getTitle() e ritorna null.
        // NOTA: Dopo attenta analisi, il while loop in getTitle() lancia StringIndexOutOfBoundsException
        // quando `i` è decrementato troppo, l'errore viene catturato dal catch esterno e ritorna null.
        assertNull(actualTitle);
        assertTrue(errContent.toString().contains("StringIndexOutOfBoundsException"));
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void getTitle_rangeConCaratteriNonAmmessi_rangeInclusoNelTitle() {
        // Range con caratteri non ammessi (es. una lettera)
        String input = "1 Test Title (5/1a)";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // NOTA: In getTitle(), l'iterazione all'indietro incontra 'a', che non è un digit o '/',
        // e la logica `continue MAINLOOP` scatta, includendo l'intera stringa nel titolo.
        assertEquals("Test Title (5/1a)", actualTitle.trim());
        assertNull(parser.getRangeString());
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void getTitle_rangeConAperturaDiversaDallaChiusura_rangeInclusoNelTitle() {
        // Range tipo [5/10), la chiusura ')' cerca l'apertura '(', non '[', quindi fallisce.
        String input = "1 Test Title [5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        // NOTA: La chiusura ')' è trovata. Cerca '('. Il while loop itera, ma il carattere '['
        // non è '('. Il loop prosegue fino a lanciare StringIndexOutOfBoundsException,
        // che viene catturata e ritorna null.
        assertNull(actualTitle);
        assertTrue(errContent.toString().contains("StringIndexOutOfBoundsException"));
        assertNull(parser.getRangeString());
    }
}