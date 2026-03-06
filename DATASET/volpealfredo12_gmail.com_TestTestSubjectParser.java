/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: volpealfredo12@gmail.com
UserID: 680
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
		// Eseguito prima di ogni metodo di test
		// Non inizializzo qui il parser per permettere input diversi per ogni test
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		parser = null;
	}

	/**
	 * Test Case 1: Formato standard con parentesi tonde.
	 * Copre il flusso principale positivo:
	 * - getId parsing corretto.
	 * - getTitle parsing con ')' e '('.
	 * - messageParts primo blocco try.
	 */
	@Test
	public void testStandardParentheses() {
		String input = "101 Test Subject Title (1/10)";
		parser = new SubjectParser(input);

		assertEquals(101, parser.getId());
		// Verifica che il range sia stato rimosso dal titolo
		String title = parser.getTitle();
		assertNotNull(title);
		assertTrue(title.trim().equals("Test Subject Title")); 
		
		assertEquals("(1/10)", parser.getRangeString());
		assertEquals(1, parser.getThisRange());
		assertEquals(10, parser.getUpperRange());
	}

	/**
	 * Test Case 2: Formato standard con parentesi quadre.
	 * Copre il flusso alternativo in messageParts e getTitle:
	 * - getTitle parsing con ']' e '['.
	 * - messageParts lancia eccezione nel primo try, cattura e risolve nel secondo try (blocchi [ ]).
	 */
	@Test
	public void testStandardBrackets() {
		String input = "202 Another Title [5/20]";
		parser = new SubjectParser(input);

		assertEquals(202, parser.getId());
		
		String title = parser.getTitle();
		assertNotNull(title);
		assertTrue(title.trim().equals("Another Title"));
		
		assertEquals("[5/20]", parser.getRangeString());
		assertEquals(5, parser.getThisRange());
		assertEquals(20, parser.getUpperRange());
	}

	/**
	 * Test Case 3: Nessun range presente.
	 * Copre il caso in cui getTitle scorre tutta la stringa senza trovare marker validi.
	 * Verifica i valori di default (1, 1).
	 */
	@Test
	public void testNoRange() {
		String input = "303 Just A Simple Title";
		parser = new SubjectParser(input);

		assertEquals(303, parser.getId());
		assertEquals("Just A Simple Title", parser.getTitle()); // Titolo invariato (spazi gestiti dal parser)
		assertNull(parser.getRangeString());
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}

	/**
	 * Test Case 4: ID non valido (non numerico).
	 * Copre il blocco catch di getId().
	 */
	@Test
	public void testInvalidId() {
		String input = "NotANumber Title (1/2)";
		parser = new SubjectParser(input);

		assertEquals(-1, parser.getId());
		// Il resto dovrebbe funzionare comunque
		assertEquals(1, parser.getThisRange());
	}

	/**
	 * Test Case 5: Input nullo.
	 * Copre tutti i blocchi catch principali (NullPointerException) nei vari metodi.
	 * Garantisce che l'applicazione non crashi.
	 */
	@Test
	public void testNullInput() {
		parser = new SubjectParser(null);

		assertEquals(-1, parser.getId());
		assertNull(parser.getTitle());
		assertNull(parser.getRangeString());
		
		// messageParts ritornerà null internamente, quindi i getter tornano i valori di default impostati nel costruttore
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}

	/**
	 * Test Case 6: Finto range nel titolo (parentesi con testo non numerico).
	 * Copre la logica complessa dentro il loop di getTitle:
	 * if ((Character.isDigit(nextchar) == false) && nextchar != '/') -> continue MAINLOOP
	 */
	@Test
	public void testFakeRangeText() {
		String input = "404 Title with (Draft) info";
		parser = new SubjectParser(input);

		// (Draft) non è un range valido (contiene lettere), quindi deve far parte del titolo
		String title = parser.getTitle();
		assertNotNull(title);
		// Il parser potrebbe lasciare o meno spazi a seconda dell'implementazione esatta, controlliamo che contenga il testo
		assertTrue(title.contains("Draft"));
		assertNull(parser.getRangeString());
	}
	
	/**
	 * Test Case 7: Finto range (parentesi senza slash).
	 * Copre la condizione: if (tmpbuf.toString().indexOf("/") != -1)
	 */
	@Test
	public void testFakeRangeNoSlash() {
		String input = "505 Title (123)";
		parser = new SubjectParser(input);

		// (123) contiene cifre ma non lo slash, quindi non è un range
		String title = parser.getTitle();
		assertTrue(title.contains("(123)"));
		assertNull(parser.getRangeString());
	}

	/**
	 * Test Case 8: Range annidati o multipli, deve prendere l'ultimo valido.
	 * Verifica la robustezza del loop inverso.
	 */
	@Test
	public void testMultipleBrackets() {
		// Qui abbiamo un (fake) prima e un (real) alla fine.
		String input = "606 Title (v1) Final (2/4)";
		parser = new SubjectParser(input);

		assertEquals("(2/4)", parser.getRangeString());
		assertEquals(2, parser.getThisRange());
		assertEquals(4, parser.getUpperRange());
		// Il titolo risultante dovrebbe includere (v1) ma non (2/4)
		String title = parser.getTitle();
		assertTrue(title.contains("(v1)"));
	}

	/**
	 * Test Case 9: Subject senza spazi (Solo ID).
	 * Copre il caso di substring in getTitle.
	 */
	@Test
	public void testSubjectNoSpace() {
		String input = "707";
		parser = new SubjectParser(input);
		
		assertEquals(707, parser.getId());
		// getTitle farà substring da indexOf(" ")+1. Se indexOf è -1, fa substring(0).
		// Se la stringa è solo numeri, il loop non troverà parentesi.
		String title = parser.getTitle();
		assertNotNull(title);
	}

	/**
	 * Test Case 10: Test diretto di getRangeString quando RangeString è già popolato.
	 * Copre il ramo "if (RangeString == null)" -> false.
	 */
	@Test
	public void testGetRangeStringCaching() {
		parser = new SubjectParser("808 Title (3/6)");
		// Prima chiamata popola RangeString
		parser.getTitle(); 
		// Seconda chiamata dovrebbe ritornare il valore cachato senza ricalcolare
		assertEquals("(3/6)", parser.getRangeString());
	}

	/**
	 * Test Case 11: Range Malformato per messageParts.
	 * Simula un caso dove getTitle ha estratto qualcosa, ma messageParts fallisce nel parsing degli interi.
	 * Nota: è difficile da raggiungere normalmente perché getTitle filtra molto, ma proviamo con numeri
	 * che superano il range degli interi o formati strani che passano il controllo base.
	 * Copre catch(Exception inte) -> catch(Exception subE) -> return null.
	 */
	@Test
	public void testMalformedRangeParsing() {
		// Creiamo una situazione limite.
		// Usiamo un parser "vergine" e forziamo un parse su stringa vuota o strana se possibile,
		// ma dato che SubjectParser non ha setter per RangeString, ci affidiamo all'input.
		// Input che sembra un range ma ha numeri non parsabili come int (es. overflow o vuoti se il controllo digit è lasco).
		// Il controllo isDigit in getTitle è carattere per carattere, quindi numeri enormi passano.
		
		String input = "909 Title (9999999999/9999999999)"; // Long value, Integer.parseInt lancerà NumberFormatException
		parser = new SubjectParser(input);
		
		// getTitle accetterà la stringa perché contiene solo cifre e /.
		assertNotNull(parser.getRangeString());
		
		// messageParts proverà a parsare, fallirà parseInt, andrà nel catch, 
		// proverà parse con [], fallirà, andrà nel catch finale, ritornerà null.
		// getThisRange catturerà null e lascerà il default.
		assertEquals(1, parser.getThisRange()); 
	}
	
	/**
	 * Test Case 12: Input vuoto.
	 * Verifica comportamento con stringa vuota.
	 */
	@Test
	public void testEmptyString() {
		parser = new SubjectParser("");
		assertEquals(-1, parser.getId()); // StringTokenizer fallirà
		assertNotNull(parser.getTitle()); // substring(0) su "" è ""
	}
}