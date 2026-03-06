/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: marcodeluca94@gmail.com
UserID: 168
Date: 23/10/2025
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
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia delle risorse o ripristino dello stato iniziale
	}
				
	@Test
	public void testMetodo() {
      
  SubjectParser p = new SubjectParser("1234 Prova parsing (2/5)");

        assertEquals(1234L, p.getId(), "ID dovrebbe essere 1234");
        assertEquals("Prova parsing ", p.getTitle(), "Title mantiene lo spazio prima della parentesi");
        assertEquals("(2/5)", p.getRangeString(), "RangeString deve essere '(2/5)'");
        assertEquals(2, p.getThisRange(), "LowerRange dovrebbe essere 2");
        assertEquals(5, p.getUpperRange(), "UpperRange dovrebbe essere 5");
		// Preparazione dei dati di input
		// Esegui il metodo da testare
		// Verifica l'output o il comportamento atteso
		// Utilizza assert per confrontare il risultato atteso 
		// con il risultato effettivo
      	
	}
				
	// Aggiungi altri metodi di test se necessario
}

						