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
	public void testId_Valid() {
		SubjectParser p = new SubjectParser("1234 Title (1/10)");
		assertEquals(1234L, p.getId());
	}
    
    @Test
	public void testId_Invalid_Text() {
		SubjectParser p = new SubjectParser("Title (1/10)");
		assertEquals(-1L, p.getId());
	}

    @Test
	public void testId_Invalid_Empty() {
		SubjectParser p = new SubjectParser("");
		assertEquals(-1L, p.getId());
	}
    
    @Test // Copre successo parentesi, getTitle/getRangeString, e aggiornamento LowerRange/UpperRange (messageParts Case 1)
	public void testFullCycle_ParenthesesRange() {
		String subject = "1234 Title (5/10)";
		SubjectParser p = new SubjectParser(subject);
        
		assertEquals(1234L, p.getId());
		assertEquals("Title ", p.getTitle());
        assertEquals("(5/10)", p.getRangeString());
        
        // Chiama messageParts() e aggiorna i range
        assertEquals(5, p.getThisRange());
        assertEquals(10, p.getUpperRange());
        
        // Verifica la persistenza dello stato (messageParts ricalcola ma i valori sono gli stessi)
        assertEquals(5, p.getThisRange()); 
        assertEquals(10, p.getUpperRange());
	}

    @Test // Copre successo parentesi quadre, fallback di messageParts (Case 2)
	public void testFullCycle_BracketsRange() {
		String subject = "5678 Another Title [20/30]";
		SubjectParser p = new SubjectParser(subject);
        
		assertEquals(5678L, p.getId());
		assertEquals("Another Title ", p.getTitle());
        assertEquals("[20/30]", p.getRangeString());
        
        // Chiama messageParts() e aggiorna i range
        assertEquals(20, p.getThisRange());
        assertEquals(30, p.getUpperRange());
	}
    
    @Test // Copre nessun range trovato, valori di default (1, 1), e RangeString/messageParts null
	public void testNoRange() {
		String subject = "9012 Simple Title";
		SubjectParser p = new SubjectParser(subject);
        
		assertEquals("Simple Title", p.getTitle());
        assertNull(p.getRangeString());
        
        // messageParts() restituisce null
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
	}
    
    @Test // Copre l'invalidità del range (caratteri non numerici) e il `continue MAINLOOP` in getTitle
	public void testTitle_InvalidRangeFormat() {
		String subject = "3456 Title (A/B)";
		SubjectParser p = new SubjectParser(subject);
        
		// Il titolo viene restituito con la parte non valida, RangeString resta null
		assertEquals("Title (A/B)", p.getTitle());
        assertNull(p.getRangeString());
        
        // messageParts() restituisce null
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
	}
    
    @Test // Copre l'inner catch block (NumberFormatException) in messageParts
	public void testRange_ParsingError_Division() {
		String subject = "1111 Title (1/)";
		SubjectParser p = new SubjectParser(subject);
        
        // getTitle() ha successo e imposta RangeString
		assertEquals("", p.getTitle());
        assertEquals("(1/)", p.getRangeString());
        
        // messageParts() fallisce con NumberFormatException nel blocco try di Case 1, tenta Case 2 (fallisce), restituisce null
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
	}
    
    @Test // Copre il blocco catch (parseE) in getTitle() e il catch esterno (e.printStackTrace) in messageParts
	public void testTitle_ExceptionAndMessagePartsOuterCatch() {
		// Soggetto che provoca StringIndexOutOfBoundsException nel loop `while` di getTitle()
		String subject = "1 )";
		SubjectParser p = new SubjectParser(subject);
        
        // getTitle() lancia eccezione e ritorna null
        assertNull(p.getTitle());
        
        // getRangeString() ritorna null
        assertNull(p.getRangeString());
        
        // messageParts() riceve null, lancia NullPointerException (catturata dall'outer catch), ritorna null
        assertEquals(1, p.getThisRange());
        assertEquals(1, p.getUpperRange());
	}
}