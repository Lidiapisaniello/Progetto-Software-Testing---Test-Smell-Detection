/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Simone"
Cognome: "Gaglione"
Username: simo.gaglione@studenti.unina.it
UserID: 208
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
	
	private SubjectParser parser; // Variabile di istanza non necessaria se inizializziamo in ogni test, ma manteniamo la struttura standard.

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
		// Eseguito prima di ogni metodo di test. Non utilizziamo una variabile di istanza 'parser' qui,
		// ma ne creiamo una nuova all'interno di ogni metodo di test per garantire l'isolamento.
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
	}
	
	// --- Test per getId() ---
	
	/**
	 * Test case nominale per getId() con ID numerico valido all'inizio del Subject.
	 */
	@Test
	public void testGetId_ValidID() {
		String subject = "12345 Test Subject Title";
		parser = new SubjectParser(subject);
		long expectedId = 12345L;
		assertEquals("L'ID deve essere parsato correttamente", expectedId, parser.getId());
	}
	
	/**
	 * Test case per getId() quando il primo token non è un Long valido (ID non valido).
	 * Questo copre il blocco catch(Exception e) in getId().
	 */
	@Test
	public void testGetId_InvalidIDFormat() {
		String subject = "ABCDE Test Subject Title";
		parser = new SubjectParser(subject);
		long expectedId = -1L; // Valore di ritorno in caso di eccezione
		assertEquals("Deve restituire -1 per un ID non numerico", expectedId, parser.getId());
	}

	/**
	 * Test case per getId() quando il Subject è null (provoca NullPointerException in StringTokenizer).
	 * Questo copre il blocco catch(Exception e) in getId().
	 */
	@Test
	public void testGetId_NullSubject() {
		// Il costruttore SubjectParser(String s) accetta null, ma l'uso di new StringTokenizer(null) genera NPE.
		parser = new SubjectParser(null); 
		long expectedId = -1L; // Valore di ritorno in caso di eccezione
		assertEquals("Deve restituire -1 per Subject nullo", expectedId, parser.getId());
	}
	
	/**
	 * Test case per getId() quando il Subject è una stringa vuota (StringTokenizer non ha token).
	 * Questo copre l'eccezione NoSuchElementException (sottoclasse di Exception) in nextToken().
	 */
	@Test
	public void testGetId_EmptySubject() {
		String subject = "";
		parser = new SubjectParser(subject);
		long expectedId = -1L; // Valore di ritorno in caso di eccezione
		assertEquals("Deve restituire -1 per Subject vuoto", expectedId, parser.getId());
	}

	// --- Test per getTitle() e getRangeString() ---
	
	/**
	 * Test case nominale per getTitle() e getRangeString() con range tra parentesi ().
	 */
	@Test
	public void testGetTitleAndRange_Parentheses() {
		String subject = "12345 Title con parentesi (001/005)";
		parser = new SubjectParser(subject);
		String expectedTitle = "Title con parentesi";
		String expectedRangeString = "(001/005)";
		
		assertEquals("Il titolo deve essere corretto (Parentesi)", expectedTitle, parser.getTitle());
		assertEquals("La RangeString deve essere corretta (Parentesi)", expectedRangeString, parser.getRangeString());
	}

	/**
	 * Test case nominale per getTitle() e getRangeString() con range tra parentesi quadre [].
	 */
	@Test
	public void testGetTitleAndRange_Brackets() {
		String subject = "67890 Title con quadre [001/005]";
		parser = new SubjectParser(subject);
		String expectedTitle = "Title con quadre";
		String expectedRangeString = "[001/005]";
		
		assertEquals("Il titolo deve essere corretto (Quadre)", expectedTitle, parser.getTitle());
		assertEquals("La RangeString deve essere corretta (Quadre)", expectedRangeString, parser.getRangeString());
	}
	
	/**
	 * Test case per getTitle() e getRangeString() quando il range non è presente.
	 * Copre il caso in cui il loop MAINLOOP termina senza trovare un range valido.
	 */
	@Test
	public void testGetTitleAndRange_NoRange() {
		String subject = "11111 Subject senza range";
		parser = new SubjectParser(subject);
		String expectedTitle = "Subject senza range";
		String expectedRangeString = null; // RangeString rimane null se non viene trovato un range
		
		assertEquals("Il titolo deve essere corretto (No Range)", expectedTitle, parser.getTitle());
		assertNull("RangeString deve essere null (No Range)", parser.getRangeString());
	}
	
	/**
	 * Test case per getTitle() con un Subject nullo (copre il blocco catch(Exception parseE) in getTitle()).
	 */
	@Test
	public void testGetTitle_NullSubject() {
		parser = new SubjectParser(null);
		assertNull("getTitle deve restituire null per Subject nullo", parser.getTitle());
		assertNull("getRangeString deve restituire null dopo getTitle con Subject nullo", parser.getRangeString());
	}
	
	/**
	 * Test case per getTitle() e getRangeString() quando il Subject ha solo l'ID.
	 * Copre l'eccezione StringIndexOutOfBoundsException (substring) nel blocco try di getTitle().
	 */
	@Test
	public void testGetTitle_OnlyID() {
		String subject = "12345";
		parser = new SubjectParser(subject);
		assertNull("getTitle deve restituire null per Subject con solo ID", parser.getTitle());
		assertNull("getRangeString deve restituire null dopo getTitle con solo ID", parser.getRangeString());
	}
	
	/**
	 * Test case per getTitle() con un range malformato (es. manca '/').
	 * Questo copre il ramo del 'while' loop in getTitle() in cui il carattere non è valido per il range.
	 */
	@Test
	public void testGetTitle_MalformedRange() {
		String subject = "12345 Title con range malformato (001-005)";
		parser = new SubjectParser(subject);
		String expectedTitle = "Title con range malformato (001-005)"; // Il range non viene riconosciuto, è parte del titolo
		String expectedRangeString = null; 
		
		assertEquals("Il titolo deve includere il range malformato", expectedTitle, parser.getTitle());
		assertNull("RangeString deve essere null per range malformato", parser.getRangeString());
	}

	/**
	 * Test per getRangeString() quando getTitle non è ancora stato chiamato.
	 * Copre il ramo 'if (RangeString == null)' in getRangeString().
	 */
	@Test
	public void testGetRangeString_CallGetTitle() {
		String subject = "12345 Subject con range [001/005]";
		parser = new SubjectParser(subject);
		String expectedRangeString = "[001/005]";
		
		// getRangeString chiamerà getTitle() che imposterà RangeString
		assertEquals("getRangeString deve forzare il calcolo", expectedRangeString, parser.getRangeString());
		// Verifica che anche Title sia stato calcolato correttamente come effetto collaterale
		String expectedTitle = "Subject con range";
		assertEquals("getTitle deve essere calcolato correttamente", expectedTitle, parser.getTitle());
	}
	
	// --- Test per messageParts() indirettamente tramite getThisRange() e getUpperRange() ---
	
	/**
	 * Test case nominale per getThisRange() e getUpperRange() con range tra parentesi ().
	 * Copre il blocco try principale in messageParts().
	 */
	@Test
	public void testGetRanges_ValidParenthesesRange() {
		// Subject: ID Title (Lower/Upper)
		String subject = "12345 Subject [test] (010/025)";
		parser = new SubjectParser(subject);
		int expectedLower = 10;
		int expectedUpper = 25;
		
		assertEquals("LowerRange deve essere 10", expectedLower, parser.getThisRange());
		assertEquals("UpperRange deve essere 25", expectedUpper, parser.getUpperRange());
	}

	/**
	 * Test case nominale per getThisRange() e getUpperRange() con range tra parentesi quadre [].
	 * Copre il blocco catch inte (che chiama il blocco try interno) in messageParts().
	 */
	@Test
	public void testGetRanges_ValidBracketsRange() {
		// Subject: ID Title [Lower/Upper]
		String subject = "67890 Subject (test) [005/015]";
		parser = new SubjectParser(subject);
		int expectedLower = 5;
		int expectedUpper = 15;
		
		assertEquals("LowerRange deve essere 5", expectedLower, parser.getThisRange());
		assertEquals("UpperRange deve essere 15", expectedUpper, parser.getUpperRange());
	}
	
	/**
	 * Test per getThisRange() e getUpperRange() quando il range non è presente.
	 * Copre il blocco catch subE (che chiama return null) in messageParts().
	 */
	@Test
	public void testGetRanges_NoRange() {
		// Il titolo viene calcolato, RangeString è null, messageParts chiama getRangeString() che restituisce null.
		// messageParts() cattura l'eccezione (NullPointerException da mainrange.substring) e ritorna null.
		// getThisRange/getUpperRange non modificano i valori predefiniti (1).
		String subject = "11111 Subject senza range";
		parser = new SubjectParser(subject);
		
		// I valori di default (inizializzati nel costruttore a 1) non dovrebbero cambiare perché messageParts() ritorna null.
		int expectedDefault = 1;
		
		assertEquals("LowerRange deve essere il default (1)", expectedDefault, parser.getThisRange());
		assertEquals("UpperRange deve essere il default (1)", expectedDefault, parser.getUpperRange());
	}
	
	/**
	 * Test per getThisRange() e getUpperRange() con un range tra parentesi () malformato (manca il separatore /).
	 * Copre l'eccezione NoSuchElementException (da st.nextToken()) nel blocco try principale di messageParts().
	 * L'esecuzione passa al blocco catch(inte), che tenta con parentesi quadre, ma fallisce.
	 * Questo copre il percorso di fallimento sia di () che di [].
	 */
	@Test
	public void testGetRanges_MalformedRange_MissingSeparator() {
		String subject = "12345 Subject con range malformato (001-005)";
		parser = new SubjectParser(subject);
		
		// Il titolo diventa "Subject con range malformato (001-005)", RangeString diventa null.
		// MA: se il range *viene* rilevato, ma è malformato (es. " (10-20)"), RangeString è "(10-20)".
		// Riprovare con un Subject in cui getTitle imposta RangeString ma è malformato:
		subject = "12345 Subject con range (10-20)";
		parser = new SubjectParser(subject);
		
		// getTitle(): Rileva ')', poi '(', RangeString = "(10-20)".
		// messageParts(): RangeString = "(10-20)".
		// try: tmpRange = "(10-20)", range = "10-20". st = StringTokenizer("10-20", "/").
		// st.nextToken() = "10-20". st.nextToken() lancia NoSuchElementException.
		// Passa al catch(inte).
		// catch inte: tenta con []. RangeString è "(10-20)", lastIndexOf("[") fallisce.
		// Lancia StringIndexOutOfBoundsException -> Passa a catch(subE).
		// catch subE: low=-1, high=-1, return null.
		
		int expectedDefault = 1;
		
		assertEquals("LowerRange deve essere il default (1) per range malformato", expectedDefault, parser.getThisRange());
		assertEquals("UpperRange deve essere il default (1) per range malformato", expectedDefault, parser.getUpperRange());
	}
	
	/**
	 * Test per getThisRange() e getUpperRange() con un range tra parentesi quadre [] malformato (manca il valore inferiore/superiore).
	 * Copre l'eccezione StringIndexOutOfBoundsException (da substring(1, sLow.length())) nel blocco try interno di messageParts().
	 */
	@Test
	public void testGetRanges_MalformedRange_EmptyValueBrackets() {
		String subject = "12345 Subject con range [/005]";
		parser = new SubjectParser(subject);
		
		// getTitle(): Rileva ']', poi '[', RangeString = "[/005]".
		// messageParts(): RangeString = "[/005]".
		// try: StringIndexOutOfBoundsException (da substring(mainrange.lastIndexOf("(")) -> Passa al catch(inte).
		// catch inte: Prova con []. tmpRange2 = "[/005]", range2 = "/005". st2 = StringTokenizer("/005", "/").
		// sLow2 = "". sHigh2 = "005".
		// Integer.parseInt(sLow2.substring(1, sLow2.length())) -> sLow2.substring(1, 0) lancia StringIndexOutOfBoundsException.
		// Passa a catch(subE).
		// catch subE: low=-1, high=-1, return null.
		
		int expectedDefault = 1;
		
		assertEquals("LowerRange deve essere il default (1) per range malformato (valore vuoto)", expectedDefault, parser.getThisRange());
		assertEquals("UpperRange deve essere il default (1) per range malformato (valore vuoto)", expectedDefault, parser.getUpperRange());
	}
	
	/**
	 * Test per getThisRange() e getUpperRange() con range numerico non intero valido.
	 * Copre l'eccezione NumberFormatException (da Integer.parseInt) nel blocco try interno di messageParts().
	 */
	@Test
	public void testGetRanges_MalformedRange_NonInteger() {
		String subject = "12345 Subject con range [A/B]";
		parser = new SubjectParser(subject);
		
		// getTitle(): RangeString = "[A/B]".
		// messageParts(): try principale fallisce (StringIndexOutOfBoundsException su '('), passa al catch(inte).
		// catch inte: Prova con []. sLow2 = "A". sHigh2 = "B".
		// low = Integer.parseInt(sLow2.substring(1, 1)) -> StringIndexOutOfBoundsException. Passa a catch(subE).
		// Ah, no, `sLow2` sarà `[A` e `sHigh2` sarà `B`. `low = Integer.parseInt(sLow2.substring(1, sLow2.length()))` -> `Integer.parseInt("A")` -> NumberFormatException.
		// Passa a catch(subE).
		// catch subE: low=-1, high=-1, return null.
		
		int expectedDefault = 1;
		
		assertEquals("LowerRange deve essere il default (1) per range con non-intero", expectedDefault, parser.getThisRange());
		assertEquals("UpperRange deve essere il default (1) per range con non-intero", expectedDefault, parser.getUpperRange());
	}
	
	// --- Test per il 100% di copertura del ramo catch(Exception e) in messageParts() ---
	
	/**
	 * Simula una situazione in cui getRangeString() fallisce (nonostante abbia chiamato getTitle()).
	 * Questo copre il catch(Exception e) esterno in messageParts() che stampa lo stack trace.
	 * NOTA: Per coprire questo ramo senza mocking, dobbiamo simulare un'eccezione in getTitle()
	 * o getRangeString() che messageParts() intercetta. Un Subject nullo in getTitle() fa ritornare null,
	 * il che è coperto dal test testGetRanges_NoRange.
	 * Se Subject è solo 'ID', getTitle() lancia StringIndexOutOfBoundsException e ritorna null.
	 * getRangeString() ritorna null. messageParts() lancia NPE in mainrange.substring(). Questo è coperto
	 * da testGetRanges_NoRange.
	 * * Per coprire esplicitamente `e.printStackTrace()` in messageParts(), usiamo un caso che non sia NullPointerException,
	 * ma che forzi l'uscita anticipata nel blocco `try` di `messageParts()`.
	 * Ad esempio, un Subject che imposta un RangeString che causi un'eccezione ma non rientri in `inte` o `subE`.
	 *
	 * Utilizziamo un Subject che contiene un range, ma lo facciamo fallire forzando un Subject nullo dopo la costruzione.
	 * Poiché l'isolamento richiede la creazione di un nuovo parser, questo test non è possibile senza mocking,
	 * a meno che un caso d'uso valido non attivi il catch.
	 * L'attuale implementazione della classe `SubjectParser` porta tutti i casi di fallimento noti (Null Subject, No Range, Malformed Range)
	 * nel catch(subE) interno o nei percorsi gestiti, rendendo difficile (e probabilmente non necessario) simulare
	 * un catch(e) esterno *che non* sia già coperto da un fallimento del codice che precede.
	 * Ad esempio, se getRangeString() restituisce una RangeString valida, ma il Subject è stato "100", getTitle() ritorna null.
	 * getRangeString() ritorna null. messageParts() chiama getRangeString(), ottiene null, fallisce su substring, catch(subE) ritorna null.
	 * **Accettiamo che i casi di fallimento "reali" (ID, Range, Formato) sono coperti.**
	 */
	@Test
	public void testGetRanges_MalformedRange_CoversCatchSubE() {
		// Questo test mira a forzare il percorso che porta a 'return null' in messageParts()
		// dopo aver fallito sia il parsing di () che di [].
		// Usiamo un Subject che abbia solo il titolo e una parentesi chiusa non bilanciata che non viene riconosciuta come range.
		String subject = "12345 Title con )";
		parser = new SubjectParser(subject);
		
		// getTitle() trova ')', non trova '(', lo inserisce in sb. RangeString è null.
		// messageParts() -> getRangeString() -> getTitle() -> RangeString = null. messageParts() NPE. catch(subE) -> return null.
		int expectedDefault = 1;
		
		assertEquals("LowerRange deve essere il default (1)", expectedDefault, parser.getThisRange());
		assertEquals("UpperRange deve essere il default (1)", expectedDefault, parser.getUpperRange());
	}
	
	// --- Test per i valori di default ---

	/**
	 * Verifica che i range siano inizializzati correttamente a 1 nel costruttore.
	 */
	@Test
	public void testInitialDefaultRanges() {
		String subject = "12345 Subject";
		parser = new SubjectParser(subject);
		// Poiché i getter non sono ancora stati chiamati, i valori dovrebbero essere quelli del costruttore.
		assertEquals("Il LowerRange iniziale deve essere 1", 1, parser.getThisRange());
		assertEquals("L'UpperRange iniziale deve essere 1", 1, parser.getUpperRange());
	}

}