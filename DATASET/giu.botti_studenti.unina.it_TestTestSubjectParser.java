/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giu.botti@studenti.unina.it
UserID: 604
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
    
    private SubjectParser parser;

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
        // Risorse inizializzate in ogni test specificamente
    }

    @After
    public void tearDown() {
        parser = null;
    }

    // --- Test per getId() ---

    @Test
    public void getIdValidIdTest() {
        parser = new SubjectParser("12345 Titolo Messaggio");
        assertEquals(12345, parser.getId());
    }

    @Test
    public void getIdInvalidIdTest() {
        // Caso in cui il primo token non è un numero
        parser = new SubjectParser("NoID Titolo Messaggio");
        assertEquals(-1, parser.getId());
    }

    @Test
    public void getIdEmptySubjectTest() {
        // Caso stringa vuota o nulla
        parser = new SubjectParser("");
        assertEquals(-1, parser.getId());
    }
    
    @Test
    public void getIdNullSubjectTest() {
        // Caso stringa null
        parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    // --- Test per Range (Parentesi Tonde) ---

    @Test
    public void getRangeStringParensTest() {
        // Test Happy Path con parentesi tonde (1/5)
        parser = new SubjectParser("100 Oggetto della mail (1/5)");
        
        // Verifica stringa range
        assertEquals("(1/5)", parser.getRangeString());
        
        // Verifica estrazione valori numerici
        assertEquals(1, parser.getThisRange()); // LowerRange
        assertEquals(5, parser.getUpperRange()); // UpperRange
    }

    // --- Test per Range (Parentesi Quadre) ---
    // Questo test è FONDAMENTALE per coprire il blocco catch(Exception inte) in messageParts
    
    @Test
    public void getRangeStringBracketsTest() {
        // Test Happy Path con parentesi quadre [2/10]
        // Questo fallirà il primo try in messageParts (che cerca '(') e attiverà il catch per cercare '['
        parser = new SubjectParser("200 Altro Oggetto [2/10]");
        
        assertEquals("[2/10]", parser.getRangeString());
        
        assertEquals(2, parser.getThisRange()); // LowerRange
        assertEquals(10, parser.getUpperRange()); // UpperRange
    }

    // --- Test per Titolo (Pulizia Range) ---

    @Test
    public void getTitleExtractionTest() {
        // Verifica che il titolo venga pulito dal range e dall'ID iniziale
        parser = new SubjectParser("300 Titolo Pulito (1/1)");
        // getTitle toglie l'ID "300 " e rimuove il range "(1/1)"
        // Nota: la logica di getTitle ricostruisce la stringa, verifichiamo spazi residui
        String title = parser.getTitle();
        // Solitamente SubjectParser lascia uno spazio o taglia esattamente prima del range
        assertTrue(title.startsWith("Titolo Pulito"));
        assertFalse(title.contains("(1/1)"));
    }

    @Test
    public void getTitleNoRangeTest() {
        // Caso senza range alla fine
        parser = new SubjectParser("400 Solo Titolo");
        assertNull(parser.getRangeString()); // Nessun range trovato
        
        // I range dovrebbero rimanere ai default (1)
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
        
        // Il titolo dovrebbe essere estratto comunque (senza ID)
        assertEquals("Solo Titolo", parser.getTitle());
    }

    // --- Test Complessi e Copertura Branch (MAINLOOP) ---

    @Test
    public void getTitleFakeRangeTest() {
        // Test cruciale per il 'continue MAINLOOP' in getTitle.
        // Mettiamo qualcosa che sembra un range ma non lo è (es: contiene lettere o niente slash)
        
        // Caso 1: Parentesi con anno (niente slash)
        parser = new SubjectParser("500 Vacanze (2023)");
        assertNull("Non dovrebbe rilevare (2023) come range", parser.getRangeString());
        assertTrue(parser.getTitle().contains("(2023)"));

        // Caso 2: Parentesi con lettere (non cifre)
        parser = new SubjectParser("500 Note (1/A)");
        assertNull("Non dovrebbe rilevare (1/A) come range", parser.getRangeString());
        assertTrue(parser.getTitle().contains("(1/A)"));
    }
    
    @Test
    public void getTitleNestedBracketsTest() {
        // Caso con parentesi multiple o annidate, il parser va all'indietro
        // Deve prendere l'ultimo valido
        parser = new SubjectParser("600 Test (Commento) (3/4)");
        assertEquals("(3/4)", parser.getRangeString());
        assertTrue(parser.getTitle().contains("(Commento)"));
    }

    // --- Test per Errori in messageParts (Weak Mutation) ---

    @Test
    public void messagePartsOverflowTest() {
        // Questo test serve per entrare nei catch più profondi di messageParts.
        // Dobbiamo creare una stringa che getTitle accetta come Range (cifre e slash),
        // ma che Integer.parseInt fallisce (es. numero troppo grande per int).
        
        // "2147483648" è MAX_INT + 1, causerà NumberFormatException in messageParts
        parser = new SubjectParser("999 Overflow Test (1/2147483648)");
        
        // getTitle accetterà la stringa perché contiene solo cifre e slash
        String rangeString = parser.getRangeString();
        assertNotNull(rangeString);
        
        // getThisRange chiamerà messageParts -> Integer.parseInt lancerà eccezione -> return null
        // LowerRange non verrà aggiornato (resterà default 1)
        assertEquals(1, parser.getThisRange()); 
    }
    
    @Test
    public void getRangeStringExceptionTest() {
        // Forzare eccezione in getRangeString (es. Subject null su istanza creata male?)
        // E' difficile rompere getRangeString se non rompendo getTitle.
        // getTitle lancia eccezione se Subject.substring fallisce (es index out of bound)
        
        // Se Subject non ha spazi -> indexOf(" ") = -1 -> substring(0) ok.
        // Proviamo a rompere getTitle con subject null (già testato in getId, ma qui per coverage)
        parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void getTitleParsingExceptionTest() {
        // Test per coprire il catch(Exception parseE) in getTitle
        // Se il subject è null, substring lancia NullPointerException
        parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }
}