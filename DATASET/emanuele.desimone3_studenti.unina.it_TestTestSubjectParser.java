/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Emanuele"
Cognome: "De Simone"
Username: emanuele.desimone3@studenti.unina.it
UserID: 428
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
	public static void setUpClass() {}
				
	@AfterClass
	public static void tearDownClass() {}
				
	@Before
	public void setUp() {
	}
				
	@After
	public void tearDown() {
		parser = null;
	}
	
	// =========================================================================
	// TEST getId()
	// =========================================================================

	@Test
	public void testGetId_Success() {
		parser = new SubjectParser("12345 Il titolo della mail");
		assertEquals("L'ID deve essere parsato correttamente", 12345L, parser.getId());
	}
	
	@Test
	public void testGetId_Failure_NoID() {
		parser = new SubjectParser("Titolo senza ID");
		assertEquals("Se l'ID non è parsabile, deve tornare -1", -1L, parser.getId());
	}
	
	@Test
	public void testGetId_Failure_EmptySubject() {
		parser = new SubjectParser("");
		assertEquals("Se il Subject è vuoto, deve tornare -1", -1L, parser.getId());
	}


	// =========================================================================
	// TEST messageParts() - Successo e Assenza
	// =========================================================================
	
	@Test
	public void testMessageParts_RoundBrackets_Success() {
		parser = new SubjectParser("9000 Progetto finale (10/50)");
		assertEquals("LowerRange deve essere 10", 10, parser.getThisRange());
		assertEquals("UpperRange deve essere 50", 50, parser.getUpperRange());
	}

	@Test
	public void testMessageParts_SquareBrackets_Success() {
		parser = new SubjectParser("9001 Test copertura [2/8]");
		assertEquals("LowerRange deve essere 2", 2, parser.getThisRange());
		assertEquals("UpperRange deve essere 8", 8, parser.getUpperRange());
	}
	
	@Test
	public void testMessageParts_NoRange() {
		parser = new SubjectParser("9002 Titolo semplice");
		assertEquals("LowerRange di default deve essere 1", 1, parser.getThisRange());
		assertEquals("UpperRange di default deve essere 1", 1, parser.getUpperRange());
	}
    
	// =========================================================================
	// TEST messageParts() - Copertura Eccezioni Interno/subE
	// =========================================================================
	
	@Test
	public void testMessageParts_Catch_NumberFormatException_Round() {
        // Fallisce parseInt nel primo try -> Esegue catch(inte)
		parser = new SubjectParser("9010 Titolo (a/50)");
		assertEquals("LowerRange deve rimanere default (1)", 1, parser.getThisRange());
	}

	@Test
	public void testMessageParts_Catch_NumberFormatException_Square() {
        // Fallisce parseInt nel secondo try -> Esegue catch(subE)
		parser = new SubjectParser("9004 Titolo [a/50]");
		assertEquals("LowerRange di default (dopo errore parsing) deve essere 1", 1, parser.getThisRange());
	}

	@Test
	public void testMessageParts_Catch_NoSuchElementException() {
        // Fallisce StringTokenizer.nextToken() -> Esegue catch(inte)
		parser = new SubjectParser("9012 Titolo (10/)");
		assertEquals("LowerRange deve rimanere default (1)", 1, parser.getThisRange());
	}
	
	@Test
	public void testMessageParts_Catch_StringIndexOutOfBounds_Square() {
        // Fallisce substring nel secondo try -> Esegue catch(subE)
		parser = new SubjectParser("9011 Titolo [");
		assertEquals("LowerRange deve rimanere default (1)", 1, parser.getThisRange());
	}

	@Test
	public void testMessageParts_RoundBrackets_ParseError() {
        // Esegue catch(inte) e poi catch(subE)
		parser = new SubjectParser("9003 Titolo (10_50)");
		assertEquals("LowerRange di default (dopo errore parsing) deve essere 1", 1, parser.getThisRange());
	}
	
	@Test
    public void testMessageParts_Catch_ExternalException() {
        // Copre i rami try-catch vuoti in getThisRange/getUpperRange se l'eccezione non si propaga (PUNTO 1).
		parser = new SubjectParser("9002 Titolo semplice");
		assertEquals("getThisRange copre messageParts interno", 1, parser.getThisRange());
    }


	// =========================================================================
	// TEST getTitle() e getRangeString() - Copertura Catch
	// =========================================================================

	@Test
	public void testGetTitle_NoRange() {
		parser = new SubjectParser("9005 Il titolo semplice");
		String title = parser.getTitle();
		assertEquals("Il titolo deve essere 'Il titolo semplice'", "Il titolo semplice", title);
		assertNull("RangeString deve essere null", parser.getRangeString());
	}
	
	@Test
	public void testGetTitle_InvalidRangeCharacters() {
		parser = new SubjectParser("9006 Titolo (10/50x)");
		String title = parser.getTitle();
		assertEquals("Il titolo deve includere il range se non è numerico", "Titolo (10/50x)", title);
		assertNull("RangeString deve essere null se non è numerico", parser.getRangeString());
	}
	
	@Test
	public void testGetTitle_Catch_parseE_LoopException() {
        // Subject: "123 )". Lancia IndexOutOfBoundsException nel loop -> Esegue catch(parseE) in getTitle.
		parser = new SubjectParser("123 )");
		String title = parser.getTitle();
		assertNull("La malformazione del Range nel loop deve lanciare eccezione e tornare null", title);
	}

	@Test
	public void testGetRangeString_CallGetTitle() {
		parser = new SubjectParser("9008 Titolo (1/50)");
		String range = parser.getRangeString(); 
		assertEquals("RangeString deve essere parsato", "(1/50)", range);
		assertEquals("RangeString deve essere restituito direttamente", "(1/50)", parser.getRangeString());
	}
	
	@Test
	public void testGetRangeString_Catch_Exception() {
        // Subject = null -> Lancia NullPointerException in getTitle, catturata da getRangeString (PUNTO 3)
		SubjectParser nullParser = new SubjectParser(null);
		String range = nullParser.getRangeString();
		assertNull("Se getTitle fallisce, getRangeString ritorna null", range);
	}

    @Test
    public void testGetTitle_Failure_SubjectTooShort() {
        // Test di regressione/correzione
        parser = new SubjectParser("123");
        String title = parser.getTitle();
        assertEquals("Se Subject è solo l'ID e non ha spazio, getTitle restituisce l'ID stesso", "123", title);
    }

	@Test
	public void testGetTitle_Catch_parseE_SubjectTooShortWithSpace() {
        // Test di regressione/correzione
		parser = new SubjectParser("123 ");
		String title = parser.getTitle();
		assertEquals("Un Subject con solo ID e spazio deve ritornare una stringa vuota", "", title);
	}

	// =========================================================================
	// TEST Limiti di getThisRange/getUpperRange (Valori default)
	// =========================================================================
	
	@Test
	public void testInitialValues() {
		parser = new SubjectParser("Test");
		assertEquals("LowerRange iniziale deve essere 1", 1, parser.getThisRange());
		assertEquals("UpperRange iniziale deve essere 1", 1, parser.getUpperRange());
	}
}