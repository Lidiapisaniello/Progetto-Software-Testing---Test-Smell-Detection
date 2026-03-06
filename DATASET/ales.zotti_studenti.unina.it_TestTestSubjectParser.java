/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alessandra"
Cognome: "Zotti"
Username: ales.zotti@studenti.unina.it
UserID: 245
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
	
	// Stringhe di esempio per i test
	private static final String VALID_SUBJECT_PARENTHESES = "123456 Titolo della mail (1/10)";
	private static final String VALID_SUBJECT_BRACKETS = "987654 Titolo della mail [1/10]";
	private static final String INVALID_SUBJECT_ID = "Non ID Titolo della mail (1/10)";
	private static final String SUBJECT_NO_RANGE = "555 Titolo senza range";
	private static final String SUBJECT_MALFORMED_RANGE_PARENTHESES = "666 Titolo (abc/def)";
	private static final String SUBJECT_MALFORMED_RANGE_BRACKETS = "777 Titolo [abc/def]";
	private static final String SUBJECT_PARENTHESES_NO_SLASH = "888 Titolo (1 10)"; 
	private static final String SUBJECT_BRACKETS_NO_SLASH = "999 Titolo [1 10]";   
	private static final String SUBJECT_PARENTHESES_WITH_TEXT = "100 Titolo (parte1/parte2) con testo extra"; 
	private static final String SUBJECT_BRACKETS_WITH_TEXT = "101 Titolo [parte1/parte2] con testo extra";     
	
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
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
	}
	
	// -------------------------------------------------------------------------
	// ## Test per getId() 🆔
	// -------------------------------------------------------------------------
	
	@Test
	public void testGetId_ValidSubject() {
		parser = new SubjectParser(VALID_SUBJECT_PARENTHESES);
		// L'ID è "123456"
		assertEquals(123456L, parser.getId());
	}
	
	@Test
	public void testGetId_InvalidSubject() {
		parser = new SubjectParser(INVALID_SUBJECT_ID);
		// Il primo token ("Non") non è parsabile come long, il try-catch restituisce -1
		assertEquals(-1L, parser.getId());
	}

	@Test
	public void testGetId_NullSubject() {
		parser = new SubjectParser(null);
		// StringTokenizer(null) lancia NullPointerException, catturata da try-catch, restituisce -1
		assertEquals(-1L, parser.getId());
	}

	// -------------------------------------------------------------------------
	// ## Test per getTitle() e getRangeString() ✉️
	// -------------------------------------------------------------------------
	
	@Test
	public void testGetTitleAndRange_ValidParentheses() {
		parser = new SubjectParser(VALID_SUBJECT_PARENTHESES);
		// Comportamento atteso: 'Titolo della mail ' e RangeString "(1/10)"
		assertEquals("Titolo della mail ", parser.getTitle());
		assertEquals("(1/10)", parser.getRangeString());
	}

	@Test
	public void testGetTitleAndRange_ValidBrackets() {
		parser = new SubjectParser(VALID_SUBJECT_BRACKETS);
		// Comportamento atteso: 'Titolo della mail ' e RangeString "[1/10]"
		assertEquals("Titolo della mail ", parser.getTitle());
		assertEquals("[1/10]", parser.getRangeString());
	}
	
	@Test
	public void testGetTitleAndRange_NoRange() {
		parser = new SubjectParser(SUBJECT_NO_RANGE);
		// Comportamento atteso: 'Titolo senza range' e RangeString nullo
		assertEquals("Titolo senza range", parser.getTitle());
		assertNull(parser.getRangeString());
	}

	@Test
	public void testGetTitleAndRange_RangeNotAtEndParentheses() {
		parser = new SubjectParser(SUBJECT_PARENTHESES_WITH_TEXT);
		// getTitle() trova il range ma il ciclo interno fallisce, la range string non viene settata.
		assertEquals("Titolo (parte1/parte2) con testo extra", parser.getTitle());
		assertNull(parser.getRangeString());
	}
	
	@Test
	public void testGetTitleAndRange_RangeNotAtEndBrackets() {
		parser = new SubjectParser(SUBJECT_BRACKETS_WITH_TEXT);
		// Comportamento analogo al caso precedente.
		assertEquals("Titolo [parte1/parte2] con testo extra", parser.getTitle());
		assertNull(parser.getRangeString());
	}
	
	@Test
	public void testGetRangeString_AlreadyPopulated() {
		parser = new SubjectParser(VALID_SUBJECT_PARENTHESES);
		// Esegue getTitle() per popolare RangeString
		parser.getTitle();
		
		// Seconda chiamata a getRangeString: restituisce il valore memorizzato senza richiamare getTitle
		assertEquals("(1/10)", parser.getRangeString());
	}
	
	@Test
	public void testGetRangeString_SubjectIsNull() {
		parser = new SubjectParser(null);
		// getTitle() lancia eccezione (NullPointerException), catturata da getRangeString, che restituisce null
		assertNull(parser.getRangeString());
	}
	
	@Test
	public void testGetTitle_SubjectTooShort_NoException() {
		parser = new SubjectParser("1"); 
		// Il Subject "1" non scatena un'eccezione ma esegue il codice fino a restituire "1"
		assertEquals("1", parser.getTitle());
	}
	
	// -------------------------------------------------------------------------
	// ## Test per getThisRange()/getUpperRange() (via messageParts()) 🔢
	// -------------------------------------------------------------------------

	@Test
	public void testGetRange_ValidParentheses() {
		parser = new SubjectParser(VALID_SUBJECT_PARENTHESES);
		// Range (1/10)
		assertEquals(1, parser.getThisRange());
		assertEquals(10, parser.getUpperRange());
	}

	@Test
	public void testGetRange_ValidBrackets() {
		parser = new SubjectParser(VALID_SUBJECT_BRACKETS);
		// Range [1/10]
		assertEquals(1, parser.getThisRange());
		assertEquals(10, parser.getUpperRange());
	}

	@Test
	public void testGetRange_NoRangeInSubject() {
		parser = new SubjectParser(SUBJECT_NO_RANGE);
		// messageParts è null. Restituisce valori di default (1, 1)
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}

	@Test
	public void testGetRange_MalformedRangeParentheses_AlphaNumeric() {
		parser = new SubjectParser(SUBJECT_MALFORMED_RANGE_PARENTHESES);
		// Parsificazione fallisce in Integer.parseInt (primo try). Restituisce default (1, 1)
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}

	@Test
	public void testGetRange_MalformedRangeBrackets_AlphaNumeric() {
		parser = new SubjectParser(SUBJECT_MALFORMED_RANGE_BRACKETS);
		// Parsificazione fallisce in Integer.parseInt (secondo try). Restituisce default (1, 1)
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}
	
	@Test
	public void testGetRange_Parentheses_NoSlash() {
		parser = new SubjectParser(SUBJECT_PARENTHESES_NO_SLASH);
		// StringTokenizer(range, "/") lancia NoSuchElementException (primo try). Restituisce default (1, 1)
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}
	
	@Test
	public void testGetRange_Brackets_NoSlash() {
		parser = new SubjectParser(SUBJECT_BRACKETS_NO_SLASH);
		// Primo try fallisce. Secondo try: StringTokenizer(range2, "/") lancia NoSuchElementException. Restituisce default (1, 1)
		assertEquals(1, parser.getThisRange());
		assertEquals(1, parser.getUpperRange());
	}
}