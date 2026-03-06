/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: g.lasalvia@studenti.unina.it
UserID: 1155
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // Variabili di istanza per i test
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
        // Eseguito prima di ogni metodo di test
        // Non inizializziamo qui il parser perché ogni test richiede una stringa diversa
    }

    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
        parser = null;
    }

    // --- Test per getId() ---

    @Test
    public void testGetIdValidNumberTest() {
        // Copre il caso standard in cui il Subject inizia con un numero valido
        parser = new SubjectParser("1234 Testing Subject");
        assertEquals("L'ID dovrebbe essere 1234", 1234L, parser.getId());
    }

    @Test
    public void testGetIdInvalidNumberTest() {
        // Copre il ramo catch di getId(): il primo token non è un numero
        parser = new SubjectParser("InvalidID Testing Subject");
        assertEquals("L'ID dovrebbe essere -1 per input non numerico", -1L, parser.getId());
    }

    @Test
    public void testGetIdNullSubjectTest() {
        // Copre il ramo catch di getId(): Subject è null (NullPointerException nel tokenizer)
        parser = new SubjectParser(null);
        assertEquals("L'ID dovrebbe essere -1 per Subject null", -1L, parser.getId());
    }

    @Test
    public void testGetIdEmptySubjectTest() {
        // Copre il ramo catch di getId(): Subject è vuoto (NoSuchElementException nel tokenizer)
        parser = new SubjectParser("");
        assertEquals("L'ID dovrebbe essere -1 per Subject vuoto", -1L, parser.getId());
    }

    // --- Test per getTitle() e Logica di Parsing ---

    @Test
    public void testGetTitleWithParenthesesRangeTest() {
        // Copre la logica principale di getTitle con parentesi tonde
        // Path: Subject valido -> Trovata ')' -> Loop interno trova '(' -> Contenuto valido
        String input = "1001 A Book Title (1/5)";
        parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        assertEquals("Il titolo non dovrebbe includere il range", "A Book Title ", title);
        assertEquals("Il range string dovrebbe essere estratto", "(1/5)", range);
    }

    @Test
    public void testGetTitleWithSquareBracketsRangeTest() {
        // Copre la logica di decisione per parentesi quadre ']'
        String input = "1002 Another Title [2/10]";
        parser = new SubjectParser(input);
        
        assertEquals("Another Title ", parser.getTitle());
        assertEquals("[2/10]", parser.getRangeString());
    }

    @Test
    public void testGetTitleNoRangeTest() {
        // Copre il percorso dove non ci sono parentesi di chiusura
        String input = "1003 Title Without Range";
        parser = new SubjectParser(input);
        
        assertEquals("Title Without Range", parser.getTitle());
        assertNull("RangeString dovrebbe essere null", parser.getRangeString());
    }

    @Test
    public void testGetTitleFalseRangeNonNumericTest() {
        // Copre il loop interno di getTitle: trova parentesi ma contenuto non è digit o '/'
        // Esempio: (Note) -> 'N', 'o', 't', 'e' non sono digit
        String input = "1004 Title (Note)";
        parser = new SubjectParser(input);
        
        // Il parser dovrebbe reinserire i caratteri nel titolo perché non è un range valido
        assertEquals("Title (Note)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitleBrokenRangeTest() {
        // Copre casi malformati, es. parentesi chiusa senza aperta o ordine errato
        // Qui trova ')' ma non trova '(' corrispondente prima di finire o trova caratteri invalidi
        String input = "1005 Title 1/2)";
        parser = new SubjectParser(input);
        
        // Il codice cercherà all'indietro. Trova ')'. Loop indietro fino a spazio o fine.
        // Se non trova '(', il comportamento dipende dal loop while.
        // Qui verifichiamo che non crashi e ritorni la stringa originale parziale.
        // Nota: la logica del parser originale è fragile, questo test fissa il comportamento attuale.
        assertEquals("Title 1/2)", parser.getTitle());
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void testGetTitleMultipleRangesPicksLastTest() {
        // Verifica il comportamento con range multipli. 
        // Dato che il loop è backward (i--), il parser processa prima l'ultimo range.
        // Una volta impostato FoundRange = true, ignora gli altri potenziali range (vengono trattati come testo).
        String input = "1006 Title (1/2) (3/4)";
        parser = new SubjectParser(input);
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        // (3/4) viene parsato come range. (1/2) rimane nel titolo.
        assertEquals("Title (1/2) ", title); 
        assertEquals("(3/4)", range);
    }

    @Test
    public void testGetTitleNoSpaceInSubjectTest() {
        // Copre Subject.indexOf(" ") == -1.
        // substring(0, length) prende tutto.
        String input = "JustOneWord";
        parser = new SubjectParser(input);
        
        assertEquals("JustOneWord", parser.getTitle());
    }

    @Test
    public void testGetTitleExceptionTest() {
        // Forza l'eccezione in getTitle (es. null Subject passato a getTitle dopo costruttore)
        // Nota: getTitle chiama Subject.substring, se Subject è null -> NullPointer -> catch -> return null
        parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    // --- Test per getThisRange() e getUpperRange() che usano messageParts() ---

    @Test
    public void testRangesWithParenthesesTest() {
        // Copre messageParts() -> try block principale -> parsing tonde
        parser = new SubjectParser("1008 T (5/10)");
        // Chiamiamo getTitle per popolare RangeString (anche se i getter lo fanno, è bene essere espliciti nel test flow)
        parser.getTitle(); 
        
        assertEquals("Lower range errato", 5, parser.getThisRange());
        assertEquals("Upper range errato", 10, parser.getUpperRange());
    }

    @Test
    public void testRangesWithSquareBracketsTest() {
        // Copre messageParts() -> catch(inte) -> try block secondario -> parsing quadre
        // Per attivare il catch(inte), il parsing delle tonde deve fallire.
        // Ma qui la stringa ha quadre, quindi substring("("...) fallisce o ritorna stringa errata
        // Se la stringa è [1/2], lastIndexOf("(") ritorna -1 -> Exception -> catch -> prova quadre.
        parser = new SubjectParser("1009 T [20/30]");
        parser.getTitle();
        
        assertEquals(20, parser.getThisRange());
        assertEquals(30, parser.getUpperRange());
    }

    @Test
    public void testRangesDefaultsTest() {
        // Se non c'è range, ritorna i valori di default (1)
        parser = new SubjectParser("1010 No Range Here");
        
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    
    @Test
    public void testGetRangeStringGetterTest() {
        // Copre il caso in cui RangeString è null e viene chiamato getTitle internamente
        parser = new SubjectParser("1012 Lazy Load (1/99)");
        // Non chiamiamo parser.getTitle() esplicitamente
        
        assertEquals("(1/99)", parser.getRangeString());
    }
    
    @Test
    public void testGetRangeStringExceptionTest() {
        // Copre il catch di getRangeString.
        // Se Subject è null, getTitle (chiamato internamente) gestisce l'eccezione e ritorna null.
        // getRangeString non lancia eccezione ma ritorna RangeString (che è null).
        // Per coprire System.err.println(e.getMessage()) in getRangeString, dovremmo forzare un errore NON in getTitle
        // ma nella logica di getRangeString stessa. Tuttavia getRangeString è molto semplice.
        // L'unico modo è se 'this.getTitle()' lanciasse un'eccezione Runtime non gestita (improbabile data la struttura try-catch ovunque).
        // Oppure se 'RangeString' accesso genera errore (impossibile per campo stringa).
        // Tuttavia, testiamo il comportamento base di null return.
        parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }
}