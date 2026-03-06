/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Luciano"
Cognome: "Balzano"
Username: luci.balzano@studenti.unina.it
UserID: 110
Date: 26/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
	
  	SubjectParser SP_2;
    SubjectParser SP_1;
    SubjectParser SP_t;
  
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
		SP_1=new SubjectParser("78 TlBCA incondizionato (71/81)");
        SP_2=new SubjectParser("4 test [4/2]");
        SP_t=new SubjectParser("7q pippo [-8/5] ");
	}
				
	@After
	public void tearDown() {
		SP_1=null;
        SP_2=null;
        SP_t=null;
	}
				
	@Test
    public void getId() {
        long id_1=SP_1.getId();
        long id_2=SP_2.getId();
        assertEquals(78L,id_1);
        assertEquals(4L,id_2);
        assertEquals(-1L,SP_t.getId());
    }

    @Test
    public void getThisRange() {
        assertEquals(71,SP_1.getThisRange());
        assertEquals(4,SP_2.getThisRange());
        assertEquals(1,SP_t.getThisRange());
    }

    @Test
    public void getUpperRange() {
        assertEquals(81,SP_1.getUpperRange());
        assertEquals(2,SP_2.getUpperRange());
        assertEquals(1,SP_t.getThisRange());
    }
	
  @Test
    public void getRangeString() {
        assertEquals("(71/81)",SP_1.getRangeString());
        assertEquals("[4/2]",SP_2.getRangeString());
        assertEquals(null,SP_t.getRangeString());
    }

    @Test
    public void getTitle() {
        assertEquals("TlBCA incondizionato ",SP_1.getTitle());
        assertEquals("test ",SP_2.getTitle());
        assertNotNull(SP_t.getTitle());
    }
	// Aggiungi altri metodi di test se necessario
}

						