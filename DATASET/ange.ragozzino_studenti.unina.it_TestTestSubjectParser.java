/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: ange.ragozzino@studenti.unina.it
UserID: 249
Date: 22/11/2025
*/
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // Variabili di appoggio se necessarie, ma per weak mutation preferisco 
    // istanziare oggetti freschi in ogni test per evitare side-effects.

    @BeforeClass
    public static void setUpClass() {
        // Setup globale non necessario per questa classe stateless
    }

    @AfterClass
    public static void tearDownClass() {
        // Teardown globale
    }

    @Before
    public void setUp() {
        // Setup per singolo test
    }

    @After
    public void tearDown() {
        // Teardown per singolo test
    }

    /**
     * Test del parsing dell'ID standard.
     * Copre: getId() caso di successo.
     */
    @Test
    public void testGetId_Valid() {
        SubjectParser parser = new SubjectParser("12345 Subject Title");
        assertEquals("L'ID deve essere parsato correttamente", 12345L, parser.getId());
    }

    /**
     * Test ID non numerico.
     * Copre: getId() -> catch block -> return -1.
     */
    @Test
    public void testGetId_Invalid_NotANumber() {
        SubjectParser parser = new SubjectParser("ABC Subject Title");
        assertEquals("Un ID non numerico deve ritornare -1", -1L, parser.getId());
    }

    /**
     * Test ID su stringa nulla.
     * Copre: getId() -> NullPointerException nel catch -> return -1.
     */
    @Test
    public void testGetId_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals("Un subject null deve ritornare -1 per l'ID", -1L, parser.getId());
    }

    /**
     * Test completo con parentesi tonde (Standard Path).
     * Copre:
     * - getTitle() loop normale
     * - messageParts() primo try block (parsing con "(")
     * - getRangeString() normale
     * - getThisRange() e getUpperRange() update riuscito.
     */
    @Test
    public void testFullParse_Parentheses() {
        // Formato: ID Titolo (Low/High)
        SubjectParser parser = new SubjectParser("100 Book Title (10/20)");

        // Verifica ID
        assertEquals(100L, parser.getId());

        // Verifica Range String
        assertEquals("(10/20)", parser.getRangeString());

        // Verifica Titolo (nota: lo spazio finale rimane spesso nella logica del parser)
        String title = parser.getTitle();
        assertNotNull(title);
        assertTrue("Il titolo dovrebbe contenere 'Book Title'", title.trim().equals("Book Title"));

        // Verifica Valori Range
        assertEquals("Lower range errato", 10, parser.getThisRange());
        assertEquals("Upper range errato", 20, parser.getUpperRange());
    }

    /**
     * Test completo con parentesi quadre (Secondary Path).
     * È cruciale per la copertura: messageParts fallisce il parsing di "(" 
     * e entra nel catch per provare il parsing di "[".
     */
    @Test
    public void testFullParse_Brackets() {
        // Formato: ID Titolo [Low/High]
        SubjectParser parser = new SubjectParser("200 Another Title [5/15]");

        // Verifica che rangeString sia corretto
        assertEquals("[5/15]", parser.getRangeString());

        // Questo triggera messageParts -> exception su "(" -> catch -> successo su "["
        assertEquals("Lower range con quadre errato", 5, parser.getThisRange());
        assertEquals("Upper range con quadre errato", 15, parser.getUpperRange());
    }

    /**
     * Test senza range definito.
     * Copre:
     * - getTitle() senza trovare match
     * - getRangeString() ritorna null
     * - getThisRange/UpperRange ritornano i valori di default (1).
     */
    @Test
    public void testNoRange() {
        SubjectParser parser = new SubjectParser("300 Just A Title");

        assertNull("RangeString deve essere null se non c'è range", parser.getRangeString());
        assertEquals("Il titolo deve rimanere invariato", "Just A Title", parser.getTitle());
        
        // Default values
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Test Range Malformato: Lettere al posto di numeri.
     * Copre:
     * - getTitle() -> Character.isDigit() == false -> continue MAINLOOP
     * - Il range viene considerato parte del titolo.
     */
    @Test
    public void testMalformedRange_Letters() {
        SubjectParser parser = new SubjectParser("400 Title (A/B)");

        // Le lettere rompono il loop di parsing del range in getTitle
        assertNull("Non deve rilevare un range valido", parser.getRangeString());
        // Tutto diventa parte del titolo
        assertEquals("Title (A/B)", parser.getTitle());
    }

    /**
     * Test Range Malformato: Mancanza dello slash '/'.
     * Copre:
     * - getTitle() -> indexOf("/") == -1
     */
    @Test
    public void testMalformedRange_NoSlash() {
        SubjectParser parser = new SubjectParser("500 Title (123)");

        assertNull(parser.getRangeString());
        assertEquals("Title (123)", parser.getTitle());
    }

    /**
     * Test: Numero troppo grande per Integer (Overflow).
     * Copre:
     * - getTitle() accetta le cifre (sono digit validi)
     * - getRangeString() ritorna la stringa lunga
     * - messageParts() -> Integer.parseInt lancia Exception
     * - catch in messageParts -> ritorna default o null
     * - getThisRange preserva il valore di default.
     */
    @Test
    public void testIntegerOverflowInMessageParts() {
        // 9999999999 è più grande di Integer.MAX_VALUE
        SubjectParser parser = new SubjectParser("600 Title (9999999999/1)");

        // getTitle lo accetta perché sono caratteri digit e c'è lo slash
        assertNotNull(parser.getRangeString());
        
        // messageParts fallirà nel parseInt e catturerà l'eccezione
        // Risultato: i valori rimangono quelli di default (1)
        assertEquals(1, parser.getThisRange()); 
    }

    /**
     * Test comportamento misto/annidato.
     * Il parser scorre all'indietro. Deve prendere l'ultimo valido.
     */
    @Test
    public void testMultipleRanges() {
        // Prende (2/2), il resto è titolo
        SubjectParser parser = new SubjectParser("700 Title (1/1) (2/2)");

        assertEquals("(2/2)", parser.getRangeString());
        assertEquals(2, parser.getThisRange());
        // Il titolo conterrà la prima parte
        assertTrue(parser.getTitle().contains("(1/1)"));
    }

    /**
     * Test Exception Handling su getTitle con subject null.
     * Copre: catch (Exception parseE) in getTitle.
     */
    @Test
    public void testGetTitle_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        // getTitle fa substring su null -> NullPointer -> catch -> return null
        assertNull(parser.getTitle());
        // getRangeString chiama getTitle -> return null
        assertNull(parser.getRangeString());
    }

    /**
     * Test input senza spazi (SubjectParser si aspetta "ID Title").
     * Copre: getTitle -> Subject.indexOf(" ") + 1.
     * Se non c'è spazio, indexOf è -1, +1 = 0. Prende tutta la stringa.
     */
    @Test
    public void testSubjectNoSpace() {
        SubjectParser parser = new SubjectParser("OnlyOneWord");
        // getId fallisce
        assertEquals(-1L, parser.getId());
        
        // getTitle prende tutto come titolo
        assertEquals("OnlyOneWord", parser.getTitle());
    }

    /**
     * Test Edge Case: Parentesi miste (aperta tonda, chiusa quadra e viceversa).
     * Il loop cerca il match esatto.
     */
    @Test
    public void testMismatchedBrackets() {
        SubjectParser parser = new SubjectParser("800 Title (1/2]");
        // Trova ']', cerca '[' ma trova '('. Loop continua o fallisce il match.
        // In questo codice, se trova ']' imposta endchar='['. 
        // Quando incontra '(', non è uguale a '[', quindi continua a parsare come titolo.
        
        assertNull("Range non deve essere rilevato con parentesi miste", parser.getRangeString());
        assertEquals("Title (1/2]", parser.getTitle());
    }

    /**
     * Test Edge Case: Range vuoto o slash iniziale.
     * Verifica robustezza StringTokenizer in messageParts.
     */
    @Test
    public void testWeirdSlashes() {
        // Caso (/5) -> tokenizer potrebbe fallire o dare stringa vuota per il primo token
        // sLow.substring(1...) su stringa vuota lancia eccezione
        SubjectParser parser = new SubjectParser("900 Title (/5)");
        
        // getTitle potrebbe accettarlo se lo vede come digit (no, / non è digit, ma il codice permette '/' in "&& nextchar != '/'")
        // Vediamo il codice: Character.isDigit(nextchar) == false && nextchar != '/' -> break
        // Quindi (/5) viene accettato da getTitle come stringa range.
        
        // In messageParts: StringTokenizer su "/5" (senza la tonda iniziale presa dal substring)
        // token 1: "5" (perché / è delimitatore).
        // sLow="5". sHigh throws NoSuchElementException.
        // Catch -> default values.
        
        assertEquals(1, parser.getThisRange());
    }
}