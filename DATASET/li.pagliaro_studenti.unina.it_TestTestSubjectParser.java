/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Livia
Cognome: Pagliaro
Username: li.pagliaro@studenti.unina.it
UserID: 125
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestSubjectParser {
  private SubjectParser parser;
	@BeforeClass
	public static void setUpClass() {
		// Eseguito una volta prima dell'inizio dei test nella classe
		// Inizializza risorse condivise 
		// o esegui altre operazioni di setup
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
		// Effettua la pulizia delle risorse condivise 
		// o esegui altre operazioni di teardown
	}
				
	@Before
	public void setUp() {
		// Eseguito prima di ogni metodo di test
		// Preparazione dei dati di input specifici per il test
      parser = new SubjectParser("0 Default Subject");
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia delle risorse o ripristino dello stato iniziale
      parser = null;
	}
				
	@Test
	public void testMetodo() {
		// Preparazione dei dati di input
		// Esegui il metodo da testare
		// Verifica l'output o il comportamento atteso
		// Utilizza assert per confrontare il risultato atteso 
		// con il risultato effettivo
	}
				
	// Aggiungi altri metodi di test se necessario
  // --- 1. Happy Path & Standard Cases ---

    @Test
    public void testStandardParenthesesRange() {
        // Sovrascriviamo parser con il caso specifico
        parser = new SubjectParser("12345 Oggetto della mail (1/5)");

        assertEquals("ID corretto", 12345L, parser.getId());
        assertEquals("Range inferiore corretto", 1, parser.getThisRange());
        assertEquals("Range superiore corretto", 5, parser.getUpperRange());
        assertEquals("Stringa range corretta", "(1/5)", parser.getRangeString());
        // getTitle deve aver rimosso il range e l'ID
        assertEquals("Titolo pulito corretto", "Oggetto della mail ", parser.getTitle());
    }

    @Test
    public void testStandardSquareBracketsRange() {
        // Testa il blocco 'catch' interno che gestisce le parentesi quadre
        parser = new SubjectParser("999 Altro Oggetto [3/10]");

        assertEquals(999L, parser.getId());
        assertEquals(3, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
        assertEquals("[3/10]", parser.getRangeString());
        assertEquals("Altro Oggetto ", parser.getTitle());
    }

    @Test
    public void testNoRangePresent() {
        // Caso senza range: verifica i default
        parser = new SubjectParser("888 Solo un titolo semplice");

        assertEquals(888L, parser.getId());
        assertEquals(1, parser.getThisRange()); // Default da costruttore
        assertEquals(1, parser.getUpperRange()); // Default da costruttore
        assertNull("RangeString deve essere null", parser.getRangeString());
        assertEquals("Solo un titolo semplice", parser.getTitle());
    }

    // --- 2. Edge Cases & Error Handling ---

    @Test
    public void testInvalidId() {
        // Primo token non numerico -> Eccezione gestita in getId()
        parser = new SubjectParser("Re: Oggetto senza ID numerico");

        assertEquals(-1L, parser.getId());
        assertEquals("Oggetto senza ID numerico", parser.getTitle());
    }

    @Test
    public void testNullSubject() {
        parser = new SubjectParser(null);
        
        assertEquals(-1L, parser.getId());
        assertNull(parser.getTitle()); // Gestisce NPE internamente
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testEmptySubject() {
        parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testSingleWordSubject() {
        // Caso limite per substring logic in getTitle
        parser = new SubjectParser("123");
        
        assertEquals(123L, parser.getId());
        assertEquals("123", parser.getTitle()); 
    }

    // --- 3. Complex Parsing logic (Weak Mutation Coverage) ---

    @Test
    public void testFakeRangeWithText() {
        // Mutation: Rompere il ciclo in getTitle (Character.isDigit check)
        parser = new SubjectParser("100 Titolo con (note) testuali");

        assertEquals(100L, parser.getId());
        assertNull("Non deve rilevare (note) come range numerico", parser.getRangeString());
        assertEquals("Titolo con (note) testuali", parser.getTitle());
    }

    @Test
    public void testFakeRangeWithMixedChars() {
        // Mutation: Caratteri misti (1a/5) devono fallire il check
        parser = new SubjectParser("100 Titolo (1a/5)");

        assertNull(parser.getRangeString());
        assertEquals("Titolo (1a/5)", parser.getTitle());
    }

    @Test
    public void testBrokenRangeFormat() {
        // Manca lo slash '/', il parser deve ignorarlo come range
        parser = new SubjectParser("100 Titolo (100)"); 
        
        assertNull(parser.getRangeString());
        assertEquals("Titolo (100)", parser.getTitle());
    }
    
    @Test
    public void testMultipleRangesTakesLast() {
        // Verifica logica inversa (loop i--)
        parser = new SubjectParser("100 Re: (1/2) Titolo finale (3/4)");
        
        // Deve prendere l'ultimo trovato partendo dalla fine
        assertEquals("(3/4)", parser.getRangeString());
        assertEquals(3, parser.getThisRange());
        // Il titolo rimuove l'ultimo range, ma mantiene il primo
        assertEquals("Re: (1/2) Titolo finale ", parser.getTitle());
    }

    @Test
    public void testRangeWithoutSpacePrefix() {
        // Boundary condition sulla substring
        parser = new SubjectParser("100 Titolo(1/2)");
        
        assertEquals("(1/2)", parser.getRangeString());
        assertEquals("Titolo", parser.getTitle());
    }

    // --- 4. Private Method Testing (Reflection) ---

    @Test
    public void testPrivateMessagePartsParsingLogic() throws Exception {
        // Usa l'istanza creata nel @Before
        parser = new SubjectParser("100 Test Dummy");
        
        // 1. Accesso al campo privato RangeString per iniettare casi limite
        Field rangeStringField = SubjectParser.class.getDeclaredField("RangeString");
        rangeStringField.setAccessible(true);
        
        // 2. Accesso al metodo privato messageParts
        Method messagePartsMethod = SubjectParser.class.getDeclaredMethod("messageParts");
        messagePartsMethod.setAccessible(true);

        // -- Scenario A: Formato numerico invalido (es. doppi slash) --
        rangeStringField.set(parser, "(1//2)"); 
        int[] result = (int[]) messagePartsMethod.invoke(parser);
        assertNull("Dovrebbe ritornare null su formato non valido", result);

        // -- Scenario B: Test esplicito parentesi quadre via reflection --
        // Serve a garantire che il blocco catch interno funzioni anche isolato da getTitle
        rangeStringField.set(parser, "[10/20]");
        result = (int[]) messagePartsMethod.invoke(parser);
        assertNotNull(result);
        assertEquals(10, result[0]);
        assertEquals(20, result[1]);

        // -- Scenario C: Stringa troppo corta o malformata (Eccezione brutale) --
        rangeStringField.set(parser, "("); 
        result = (int[]) messagePartsMethod.invoke(parser);
        assertNull("Dovrebbe catturare l'eccezione interna e ritornare null", result);
    }
}

						