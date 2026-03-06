/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Manuel"
Cognome: "Loffredo"
Username: man.loffredo@studenti.unina.it
UserID: 216
Date: 30/10/2025
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
		// Preparazione dei dati di input
		// Esegui il metodo da testare
		// Verifica l'output o il comportamento atteso
		// Utilizza assert per confrontare il risultato atteso 
		// con il risultato effettivo
	}
  
  	@Test
  	public void testGetId(){
      SubjectParser subjectParser = new SubjectParser("1 subject title [1/2]");
      long id = subjectParser.getId();
      assertEquals(1L,id);
      
    }
  
  	@Test
  	public void testGetIdInvalid(){
     SubjectParser subjectParser = new SubjectParser("software testing");
  	long id = subjectParser.getId();
  	assertEquals(-1L,id);
}
  
  @Test
  	public void testgetTitle(){
  	SubjectParser subjectParser = new SubjectParser("1 subject title [1/2]");
  	String title = subjectParser.getTitle();
    assertEquals("subject title ", title);
 }
  
  @Test
  	public void testgetTitle1(){
   	SubjectParser subjectParser = new SubjectParser("1 subject title(1/2)");
      String title = subjectParser.getTitle();
    assertEquals("subject title",title);
    
    }
  
  @Test
  	public void testgetTitle2(){
   	SubjectParser subjectParser = new SubjectParser("1 subject title");
      String title = subjectParser.getTitle();
    assertEquals("subject title",title);
    
    }
     @Test
  	public void testgetTitle3(){
   	SubjectParser subjectParser = new SubjectParser("1 subject title[test]");
      String title = subjectParser.getTitle();
    assertEquals("subject title[test]",title);
    
    }
  
  @Test
  public void testgetTitle4(){
    SubjectParser subjectParser = new SubjectParser("1 subject title(12) [test]");
      String title = subjectParser.getTitle();
    assertEquals("subject title [test]",title);
  }
  
  @Test
    public void testgetTitle5(){
    SubjectParser subjectParser = new SubjectParser(null);
      String title = subjectParser.getTitle();
    assertNull(title);
  }
  @Test
    public void testGetTitle_WithMultipleRanges() {
        
        String subject = "123 Title (Commento) [1/10]";
        SubjectParser parser = new SubjectParser(subject);
        
    
        assertEquals("Title (Commento) ", parser.getTitle());
     
        assertEquals("[1/10]", parser.getRangeString());
    }
  

  @Test
  public void testgetRangeString(){
  SubjectParser subjectParser = new SubjectParser("1 subject title 	  (1/2)");
    String RangeString = subjectParser.getRangeString();
    
    assertEquals("(1/2)",RangeString);
    
  }

  
 @Test 
    public void testgetRangeString1(){
  SubjectParser subjectParser = new SubjectParser("1 subject title");
    String RangeString = subjectParser.getRangeString();
    
    assertNull(RangeString);
    
  }
    @Test
  public void testgetRangeString2(){
  SubjectParser subjectParser = new SubjectParser("1 subject title 	  [1/2]");
    String RangeString = subjectParser.getRangeString();
    
    assertEquals("[1/2]",RangeString);
  
  } 
  
  @Test
  public void testgetUpperRange(){
    SubjectParser subjectParser = new SubjectParser("1 subject title [1/2]");
    int a = subjectParser.getUpperRange();
    assertEquals("Valore non trovato",2,a);
   }
  
  @Test
  public void testgetUpperRange1(){
    SubjectParser subjectParser = new SubjectParser("1 subject title");
    int a = subjectParser.getUpperRange();
    assertEquals(1,a);
   }
  
  
   @Test
  public void testgetLowerRange(){
    SubjectParser subjectParser = new SubjectParser("1 subject title (1/2)");
    int a = subjectParser.getThisRange();
    assertEquals("Valore non trovato",1,a);
   }
  
    @Test
  public void testgetLowerRange1(){
    SubjectParser subjectParser = new SubjectParser("1 subject title");
    int a = subjectParser.getThisRange();
    assertEquals(1,a);
   }

  
    
  
				
	// Aggiungi altri metodi di test se necessario
}

						