/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Oriana"
Cognome: "Buccelli"
Username: o.buccelli@studenti.unina.it
UserID: 609
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
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
				
	@Test
	public void getIdValidNumberTest() {
		SubjectParser parser = new SubjectParser("101 Software Engineering");
		assertEquals(101, parser.getId());
	}

	@Test
	public void getIdInvalidFormatTest() {
		// Scenario: Il primo token non è un numero
		SubjectParser parser = new SubjectParser("InvalidID Software Engineering");
		assertEquals(-1, parser.getId());
	}
	
	@Test
	public void getIdNullSubjectTest() {
		// Scenario: Subject è null, StringTokenizer lancia NPE, catturata da catch
		SubjectParser parser = new SubjectParser(null);
		assertEquals(-1, parser.getId());
	}

	@Test
	public void getTitleWithParenthesesRangeTest() {
		// Scenario: Range valido con parentesi tonde
		SubjectParser parser = new SubjectParser("101 Intro to Testing (1/5)");
		String title = parser.getTitle();
		// getTitle rimuove l'ID e preserva il resto
		assertEquals("Intro to Testing (1/5)", title);
		assertEquals("(1/5)", parser.getRangeString());
	}

	@Test
	public void getTitleWithBracketsRangeTest() {
		// Scenario: Range valido con parentesi quadre
		SubjectParser parser = new SubjectParser("101 Intro to Testing [2/5]");
		String title = parser.getTitle();
		assertEquals("Intro to Testing [2/5]", title);
		assertEquals("[2/5]", parser.getRangeString());
	}

	@Test
	public void getTitleNoRangeTest() {
		// Scenario: Nessun range presente
		SubjectParser parser = new SubjectParser("101 Just A Title");
		assertEquals("Just A Title", parser.getTitle());
		assertNull(parser.getRangeString());
	}
	
	@Test
	public void getTitleNullSubjectTest() {
		// Scenario: Subject è null, getTitle prova a fare substring e lancia eccezione
		SubjectParser parser = new SubjectParser(null);
		assertNull(parser.getTitle());
	}

	@Test
	public void getTitleIgnoreNonRangeParenthesesTest() {
		// Scenario: Parentesi che contengono caratteri non numerici (escluso /)
		// Questo testa il ramo 'continue MAINLOOP' nel ciclo di getTitle
		SubjectParser parser = new SubjectParser("101 Title (Notes) End");
		assertEquals("Title (Notes) End", parser.getTitle());
		// Non deve aver rilevato un range
		assertNull(parser.getRangeString());
	}

	@Test
	public void getTitleIgnoreParenthesesWithoutSlashTest() {
		// Scenario: Parentesi con numeri ma senza lo slash '/'
		// Questo testa la condizione if (tmpbuf.toString().indexOf("/") != -1)
		SubjectParser parser = new SubjectParser("101 Title (2023)");
		assertEquals("Title (2023)", parser.getTitle());
		assertNull(parser.getRangeString());
	}

	@Test
	public void getThisRangeFromParenthesesTest() {
		// Scenario: Estrazione LowerRange da (x/y)
		SubjectParser parser = new SubjectParser("101 Sub (5/10)");
		// getThisRange chiama internamente getRangeString -> getTitle -> messageParts
		assertEquals(5, parser.getThisRange());
	}

	@Test
	public void getThisRangeFromBracketsTest() {
		// Scenario: Estrazione LowerRange da [x/y]
		// Questo forza il fallimento del primo try (tonde) e l'ingresso nel secondo try (quadre) in messageParts
		SubjectParser parser = new SubjectParser("101 Sub [3/8]");
		assertEquals(3, parser.getThisRange());
	}

	@Test
	public void getUpperRangeFromParenthesesTest() {
		// Scenario: Estrazione UpperRange da (x/y)
		SubjectParser parser = new SubjectParser("101 Sub (5/20)");
		assertEquals(20, parser.getUpperRange());
	}

	@Test
	public void getUpperRangeFromBracketsTest() {
		// Scenario: Estrazione UpperRange da [x/y]
		SubjectParser parser = new SubjectParser("101 Sub [3/99]");
		assertEquals(99, parser.getUpperRange());
	}

	@Test
	public void getThisRangeMalformedTokenTest() {
		// Scenario: getTitle trova un range valido (numeri e slash), ma messageParts fallisce il parsing.
		// Caso: (1/) -> getTitle accetta '/' ma StringTokenizer in messageParts fallisce perché manca il secondo token.
		// Questo testa il catch(Exception subE) interno che ritorna null.
		SubjectParser parser = new SubjectParser("101 Sub (1/)");
		// Default LowerRange è 1
		assertEquals(1, parser.getThisRange());
		// Default UpperRange è 1
		assertEquals(1, parser.getUpperRange());
	}

	@Test
	public void getRangeStringLazyLoadTest() {
		// Scenario: getRangeString è inizialmente null, chiama getTitle per valorizzarlo
		SubjectParser parser = new SubjectParser("101 Sub (1/2)");
		// Prima chiamata
		assertEquals("(1/2)", parser.getRangeString());
		// Seconda chiamata (già valorizzato)
		assertEquals("(1/2)", parser.getRangeString());
	}
	
	@Test
	public void getRangeStringExceptionTest() {
		// Scenario: Generare eccezione dentro getRangeString chiamando getTitle su null
		SubjectParser parser = new SubjectParser(null);
		assertNull(parser.getRangeString());
	}

    @Test
    public void getThisRangeNullPartsTest() {
        // Scenario: messageParts ritorna null (perché subject è null o malformato)
        // getThisRange deve gestire parts == null senza crashare
        SubjectParser parser = new SubjectParser(null);
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void getUpperRangeNullPartsTest() {
        // Scenario: messageParts ritorna null
        // getUpperRange deve gestire parts == null senza crashare
        SubjectParser parser = new SubjectParser(null);
        assertEquals(1, parser.getUpperRange());
    }
}