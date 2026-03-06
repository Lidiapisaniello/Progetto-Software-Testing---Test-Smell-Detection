/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alex"
Cognome: "Galiero"
Username: alex.galiero@studenti.unina.it
UserID: 212
Date: 25/10/2025
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
 
	}
				
	@AfterClass
	public static void tearDownClass() {
 
	}
				
	@Before
	public void setUp() {
 
	}
				
	@After
	public void tearDown() {
 
	}
				
	@Test
	public void testMetodo() {
 
	}
  
  	@Test
  	public void testGetId(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto [1/2]");
      long id = subjectParser.getId();
      assertEquals(1L,id);
      
    }
  
  	@Test
  	public void testGetIdInv(){
      SubjectParser subjectParser = new SubjectParser("software testing exam");
      long id = subjectParser.getId();
      assertEquals(-1L,id);
    }
  
    @Test
  	public void testgetTitle(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto [1/2]");
      String title = subjectParser.getTitle();
      assertEquals("titolo del soggetto ", title);
    }
  
    @Test
  	public void testgetTitle1(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto(1/2)");
      String title = subjectParser.getTitle();
      assertEquals("titolo del soggetto",title); 
    }
  
    @Test
  	public void testgetTitle2(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto");
      String title = subjectParser.getTitle();
      assertEquals("titolo del soggetto",title);
    }

    @Test
  	public void testgetTitle3(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto[ciao]");
      String title = subjectParser.getTitle();
      assertEquals("titolo del soggetto[ciao]",title);
    }
  
    @Test
    public void testgetTitle4(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto(12) [ciao]");
      String title = subjectParser.getTitle();
      assertEquals("titolo del soggetto [ciao]",title);
    }
  
    @Test
    public void testgetTitle5(){
      SubjectParser subjectParser = new SubjectParser(null);
      String title = subjectParser.getTitle();
      assertNull(title);
    }
  
    @Test
    public void testGetTitleWithMultipleRanges() {
      String subject = "123 Titolo (Commento) [5/10]";
      SubjectParser parser = new SubjectParser(subject);
      assertEquals("Titolo (Commento) ", parser.getTitle());
      assertEquals("[5/10]", parser.getRangeString());
    }

    @Test
    public void testgetRangeString(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto 	  (1/2)");
      String RangeString = subjectParser.getRangeString();
      assertEquals("(1/2)",RangeString); 
    }
  
    @Test 
    public void testgetRangeString1(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto");
      String RangeString = subjectParser.getRangeString(); 
      assertNull(RangeString);
    }
    @Test
    public void testgetRangeString2(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto 	  [1/2]");
      String RangeString = subjectParser.getRangeString();
      assertEquals("[1/2]",RangeString);
    } 
  
    @Test
    public void testgetUpperRange(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto [1/2]");
      int a = subjectParser.getUpperRange();
      assertEquals("Valore non trovato",2,a);
    }
  
    @Test
    public void testgetUpperRange1(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto");
      int a = subjectParser.getUpperRange();
      assertEquals(1,a);
    }
  
    @Test
    public void testgetLowerRange(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto (1/2)");
      int a = subjectParser.getThisRange();
      assertEquals("Valore non trovato",1,a);
    }
  
    @Test
    public void testgetLowerRange1(){
      SubjectParser subjectParser = new SubjectParser("1 titolo del soggetto");
      int a = subjectParser.getThisRange();
      assertEquals(1,a);
    }

}

						