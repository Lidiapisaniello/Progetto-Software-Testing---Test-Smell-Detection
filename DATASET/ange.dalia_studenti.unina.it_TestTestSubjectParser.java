/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Tuo Nome"
Cognome: "Tuo Cognome"
Username: ange.dalia@studenti.unina.it
UserID: 127
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestSubjectParser {
	
	private SubjectParser parser;

	@BeforeClass
	public static void setUpClass() {}
				
	@AfterClass
	public static void tearDownClass() {}
				
	@Before
	public void setUp() {}
				
	@After
	public void tearDown() {}

	// --- TEST ID PARSING ---

	@Test
	public void testGetId_Valid() {
		parser = new SubjectParser("12345 Title Text");
		assertEquals(12345L, parser.getId());
	}

	@Test
	public void testGetId_Invalid() {
		// Non inizia con un numero -> Exception interna -> return -1
		parser = new SubjectParser("NoNumber Title");
		assertEquals(-1L, parser.getId());
	}
	
	@Test
	public void testGetId_Empty() {
		parser = new SubjectParser("");
		assertEquals(-1L, parser.getId());
	}

	// --- TEST RANGE PARSING (HAPPY PATH) ---

	@Test
	public void testRanges_RoundBrackets() {
		// Formato (Low/High)
		parser = new SubjectParser("100 My Title (5/10)");
		
		// La prima chiamata parsa la stringa
		String title = parser.getTitle();
		assertEquals("My Title ", title); // Notare lo spazio residuo spesso lasciato dai parser semplici
		
		assertEquals(5, parser.getThisRange());
		assertEquals(10, parser.getUpperRange());
	}

	@Test
	public void testRanges_SquareBrackets() {
		// Formato [Low/High]
		// Questo attiva il blocco catch interno di messageParts (fallisce (..), prova [..])
		parser = new SubjectParser("200 Another Title [20/50]");
		
		// Chiamiamo getUpperRange direttamente per forzare il parsing implicito
		assertEquals(50, parser.getUpperRange());
		assertEquals(20, parser.getThisRange());
		assertEquals("Another Title ", parser.getTitle());
	}

	// --- TEST TITLE PARSING LOGIC (COMPLEX LOOP) ---

	@Test
	public void testGetTitle_FakeRange_MixedChars() {
		// Il parser scorre all'indietro. Trova ')'.
		// Cerca indietro fino a '('.
		// Se trova un carattere non digit e non '/', abortisce e considera tutto come titolo.
		// Caso: "Title (Ver 1.0)" -> 'V', 'e', 'r' rompono il loop di range.
		String subject = "300 Title With (Ver 1.0)";
		parser = new SubjectParser(subject);
		
		String title = parser.getTitle();
		// Il titolo deve contenere tutto perché non è un range valido
		assertTrue(title.contains("(Ver 1.0)"));
		// Range deve rimanere default (1)
		assertEquals(1, parser.getThisRange());
	}

	@Test
	public void testGetTitle_FakeRange_NoStartBracket() {
		// Trova ')' ma non trova '(' corrispondente prima dell'inizio o trova char invalido
		parser = new SubjectParser("400 Title 5/10)");
		assertEquals("Title 5/10)", parser.getTitle().trim());
	}
	
	@Test
	public void testGetTitle_NoRangeAtAll() {
		parser = new SubjectParser("500 Just Simple Title");
		assertEquals("Just Simple Title", parser.getTitle().trim());
		assertEquals(1, parser.getThisRange());
	}

	@Test
	public void testGetTitle_ExceptionCase() {
		// Passiamo null al costruttore se possibile o stringa che rompe substring
		// SubjectParser non gestisce subject null nel costruttore, ma getTitle fa substring
		parser = new SubjectParser("NoSpace");
		// Subject.indexOf(" ") restituisce -1. substring(-1 + 1) = substring(0). Funziona.
		// Ma loop while potrebbe avere problemi se indici sballati.
		assertNotNull(parser.getTitle());
	}
	
	@Test
	public void testGetRangeString_Directly() {
		parser = new SubjectParser("600 Test (1/2)");
		// Prima chiamata triggera il parsing
		assertEquals("(1/2)", parser.getRangeString());
		// Seconda chiamata ritorna valore cachato
		assertEquals("(1/2)", parser.getRangeString());
	}

	// --- REFLECTION: FORCE COVERAGE ON messageParts ---
	// messageParts è privato e ha una logica try-catch annidata complessa.
	// Usiamo reflection per impostare RangeString a valori che getTitle non produrrebbe mai,
	// per testare robustezza e rami di errore.

	@Test
	public void testMessageParts_InvalidFormat_Crashes() throws Exception {
		parser = new SubjectParser("999 Dummy");
		
		// 1. Iniettiamo una RangeString malformata che getTitle non passerebbe mai
		Field fRangeString = SubjectParser.class.getDeclaredField("RangeString");
		fRangeString.setAccessible(true);
		fRangeString.set(parser, "BrokenString"); 

		// 2. Invochiamo messageParts (privato)
		Method mMessageParts = SubjectParser.class.getDeclaredMethod("messageParts");
		mMessageParts.setAccessible(true);
		int[] result = (int[]) mMessageParts.invoke(parser);

		// Deve ritornare null (tutti i catch eseguiti)
		assertNull(result);
	}

	@Test
	public void testMessageParts_ValidSquare_ViaReflection() throws Exception {
		// Testiamo esplicitamente il parsing delle quadre bypassando getTitle
		parser = new SubjectParser("999 Dummy");
		
		Field fRangeString = SubjectParser.class.getDeclaredField("RangeString");
		fRangeString.setAccessible(true);
		fRangeString.set(parser, "[10/20]"); 

		Method mMessageParts = SubjectParser.class.getDeclaredMethod("messageParts");
		mMessageParts.setAccessible(true);
		int[] result = (int[]) mMessageParts.invoke(parser);

		assertNotNull(result);
		assertEquals(10, result[0]);
		assertEquals(20, result[1]);
	}
	
	@Test
	public void testMessageParts_BrokenInternalParsing() throws Exception {
		// Forziamo l'ingresso nel primo try (trova '(') ma fallimento nel parsing numeri
		parser = new SubjectParser("999 Dummy");
		
		Field fRangeString = SubjectParser.class.getDeclaredField("RangeString");
		fRangeString.setAccessible(true);
		// Ha '(', ma il contenuto non è "int/int"
		fRangeString.set(parser, "(A/B)"); 

		Method mMessageParts = SubjectParser.class.getDeclaredMethod("messageParts");
		mMessageParts.setAccessible(true);
		int[] result = (int[]) mMessageParts.invoke(parser);
		
		// Deve finire nel catch -> prova quadre -> catch -> return null
		assertNull(result);
	}
	
	@Test
	public void testGetTitle_LoopBreakCondition() {
	    // Copre il caso: if ((Character.isDigit(nextchar) == false) && nextchar != '/')
	    // Serve una stringa con parentesi chiusa, ma che contiene un char invalido dentro
	    parser = new SubjectParser("123 T (1a/2)");
	    String t = parser.getTitle();
	    // Non deve aver parsato il range
	    assertTrue(t.contains("(1a/2)"));
	    assertNull(parser.getRangeString());
	}
	
	@Test
    public void testGetRangeString_Exception() throws Exception {
        // getRangeString ha un try/catch che stampa su System.err
        // Per attivarlo, RangeString deve essere null, e getTitle deve lanciare eccezione.
        // getTitle lancia eccezione se Subject è null (ma ctor non lo permette facilmente senza crash prima).
        // Usiamo reflection per settare Subject a null DOPO costruzione.
        
        parser = new SubjectParser("123 Ok");
        Field fSubject = SubjectParser.class.getDeclaredField("Subject");
        fSubject.setAccessible(true);
        fSubject.set(parser, null); // Boom per getTitle
        
        // getRangeString chiama getTitle -> Exception -> Catch -> return null
        assertNull(parser.getRangeString());
    }
}