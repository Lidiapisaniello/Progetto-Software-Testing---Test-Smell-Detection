/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Manuel"
Cognome: "Loffredo"
Username: manuel.loffredo9@gmail.com
UserID: 1419
Date: 21/11/2025
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
		// Non inizializziamo qui il parser perché ogni test richiede una stringa diversa nel costruttore
	}
				
	@After
	public void tearDown() {
		parser = null;
	}
				
	@Test
	public void testGetIdSuccess() {
		// Test ID numerico standard
		parser = new SubjectParser("12345 Test Subject");
		assertEquals(12345L, parser.getId());
	}

	@Test
	public void testGetIdFailureNotANumber() {
		// Test ID non numerico -> deve ritornare -1
		parser = new SubjectParser("ABC Test Subject");
		assertEquals(-1L, parser.getId());
	}
	
	@Test
	public void testGetIdFailureNullSubject() {
		// Test Subject null -> catch exception -> ritorna -1
		parser = new SubjectParser(null);
		assertEquals(-1L, parser.getId());
	}

	@Test
	public void testStandardRangeParenthesis() {
		// Test formato (X/Y)
		// La logica di getTitle separa l'ID (prima dello spazio) dal resto.
		parser = new SubjectParser("100 My Title (1/5)");
		
		// Verifica ID
		assertEquals(100L, parser.getId());
		
		// Verifica RangeString (deve essere popolato da getTitle o chiamata diretta)
		assertEquals("(1/5)", parser.getRangeString());
		
		// Verifica parsing range
		assertEquals(1, parser.getThisRange()); // Lower
		assertEquals(5, parser.getUpperRange()); // Upper
		
		// Verifica Titolo (Range rimosso, rimane il testo)
		// NOTA: Il parser costruisce il titolo in reverse. Se c'è uno spazio prima della parentesi,
		// la logica attuale tende a preservarlo o meno a seconda di come è stato splittato.
		// "My Title (1/5)" -> "My Title " (con spazio finale spesso residuo in questa implementazione)
		String title = parser.getTitle();
		assertTrue("Il titolo dovrebbe contenere 'My Title'", title.trim().equals("My Title"));
	}

	@Test
	public void testStandardRangeBrackets() {
		// Test formato [X/Y]
		parser = new SubjectParser("200 Another Title [10/20]");
		
		assertEquals("[10/20]", parser.getRangeString());
		assertEquals(10, parser.getThisRange());
		assertEquals(20, parser.getUpperRange());
		assertTrue(parser.getTitle().contains("Another Title"));
	}
	
	@Test
	public void testNoRangePresent() {
		// Nessun range specificato
		parser = new SubjectParser("300 Just A Title");
		
		// RangeString dovrebbe essere null se non trovato
		assertNull(parser.getRangeString());
		
		// Default values inizializzati nel costruttore sono 1
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
		
		// Il titolo deve essere l'intera stringa dopo l'ID
		assertEquals("Just A Title", parser.getTitle());
	}

	@Test
	public void testInvalidRangeFormatIncludedInTitle() {
		// Formato che sembra un range ma contiene caratteri non validi (lettere)
		// La logica interna `if ((Character.isDigit(nextchar) == false) && nextchar != '/')`
		// dovrebbe fallire, causando l'inserimento del blocco nel titolo.
		parser = new SubjectParser("400 Title with (1a/5)");
		
		String extractedTitle = parser.getTitle();
		
		// RangeString deve essere null perché il parsing "validante" è fallito
		assertNull(parser.getRangeString());
		
		// Il "finto" range deve far parte del titolo
		assertTrue(extractedTitle.contains("(1a/5)"));
	}

	@Test
	public void testDoubleRangeTakesLast() {
		// Mutation & Logic test: Il ciclo è `i--` (backwards). 
		// Se ci sono due range, dovrebbe prendere l'ultimo come Range effettivo 
		// e trattare il primo come parte del titolo (perché `FoundRange` diventa true).
		parser = new SubjectParser("500 Title (1/2) [3/4]");
		
		// Range atteso: l'ultimo [3/4]
		assertEquals("[3/4]", parser.getRangeString());
		assertEquals(3, parser.getThisRange());
		assertEquals(4, parser.getUpperRange());
		
		// Titolo atteso: deve contenere il primo range
		String title = parser.getTitle();
		assertTrue(title.contains("(1/2)"));
	}
	
	@Test
	public void testPartialRangeSymbols() {
		// Test con simboli incompleti per coprire i rami else del parsing caratteri
		// Caso: parentesi chiusa senza aperta corrispondente valida
		parser = new SubjectParser("600 Title 1/5)");
		
		assertNull(parser.getRangeString());
		assertTrue(parser.getTitle().contains("1/5)"));
	}
	
	@Test
	public void testMessagePartsExceptionPaths() {
		// Questo test cerca di forzare il return null dentro messageParts 
		// simulando una situazione anomala dove getRangeString non è null ma il formato è rotto.
		// Tuttavia, getRangeString viene settato solo se il formato è validato da getTitle.
		// L'unico modo per rompere messageParts è avere un rangeString valido sintatticamente 
		// ma che fallisce parseInt (es. overflow integer) o logica interna.
		
		// Caso limite: stringa valida per getTitle ma che potrebbe creare problemi.
		// Proviamo un range con numeri enormi o formattazione limite.
		// Nota: getTitle controlla solo isDigit e '/'. 
		
		// Se getRangeString è null, messageParts lancia eccezione interna su mainrange.substring
		// e ritorna null.
		parser = new SubjectParser("700 No Range");
		// Forza caricamento
		parser.getTitle(); 
		// RangeString è null.
		// getThisRange chiama messageParts -> messageParts usa getRangeString() che è null.
		// mainrange.lastIndexOf("(") su null lancia NullPointerException.
		// Il catch(Exception e) esterno in messageParts cattura e ritorna null.
		// getThisRange riceve null, catch eccezione, non aggiorna LowerRange.
		// LowerRange rimane 1.
		assertEquals(1, parser.getThisRange());
	}

	@Test
	public void testGetTitleExceptionPath() {
		// Test per coprire il catch in getTitle
		parser = new SubjectParser(null);
		assertNull(parser.getTitle());
		assertNull(parser.getRangeString());
	}
	
	@Test
	public void testMalformedSubjectNoSpace() {
		// Subject senza spazi: "123"
		// Subject.indexOf(" ") ritorna -1.
		// Subject.substring(0) prende tutta la stringa.
		parser = new SubjectParser("123");
		
		// getId funziona (tutta la stringa è numero)
		assertEquals(123L, parser.getId());
		
		// getTitle parserà "123" cercando parentesi
		String title = parser.getTitle();
		assertEquals("123", title);
	}

    @Test
    public void testValidParenthesisParsingLogic() {
        // Test specifico per la logica in messageParts che usa substring
        // Copre il ramo `try { ... substring ... indexOf(")") }`
        parser = new SubjectParser("800 T (10/20)");
        parser.getTitle(); // parse
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testValidBracketsParsingLogic() {
        // Test specifico per il secondo blocco try in messageParts (quello con [ ])
        // Questo viene eseguito quando il primo try (parentesi tonde) fallisce o lancia eccezione
        parser = new SubjectParser("900 T [30/40]");
        parser.getTitle(); // parse
        assertEquals(30, parser.getThisRange());
        assertEquals(40, parser.getUpperRange());
    }
}