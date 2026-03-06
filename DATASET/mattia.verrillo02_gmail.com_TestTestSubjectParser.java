/*
Nome: "Mattia"
Cognome: "Verrillo"
Username: mattia.verrillo02@gmail.com
UserID: 807
Date: 20/11/2025
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

    // -----------------------------------------------------------
    // Metodo testato: getId()
    // Complessità Ciclomatica metodo originale: 2
    // -----------------------------------------------------------

    @Test
    public void metodo_getId_Test_1() {
        // Path 1: Parsing corretto dell'ID (Happy Path)
        String input = "807 Matematica Lezione 1";
        SubjectParser parser = new SubjectParser(input);
        
        long expected = 807;
        long actual = parser.getId();
        
        assertEquals("L'ID dovrebbe essere parsato correttamente come long", expected, actual);
    }

    @Test
    public void metodo_getId_Test_2() {
        // Path 2: Eccezione durante il parsing (Stringa invece di numero) -> Catch block
        String input = "Errore Matematica Lezione 1";
        SubjectParser parser = new SubjectParser(input);
        
        long expected = -1; // Il catch restituisce -1
        long actual = parser.getId();
        
        assertEquals("L'ID dovrebbe essere -1 in caso di errore di parsing", expected, actual);
    }

    // -----------------------------------------------------------
    // Metodo testato: getTitle()
    // Complessità Ciclomatica metodo originale: 11
    // -----------------------------------------------------------

    @Test
    public void metodo_getTitle_Test_1() {
        // Path 1: Titolo con range formato parentesi tonde (1/2)
        // Copre il loop, la rilevazione di ')', la ricerca di '(', e la validazione cifre
        String input = "807 Lezione Importante (1/2)";
        SubjectParser parser = new SubjectParser(input);
        
        String expectedTitle = "Lezione Importante "; // Nota: lo spazio finale è mantenuto dalla logica
        String actualTitle = parser.getTitle();
        
        assertEquals("Il titolo dovrebbe essere estratto rimuovendo il range", expectedTitle, actualTitle);
    }

    @Test
    public void metodo_getTitle_Test_2() {
        // Path 2: Titolo con range formato parentesi quadre [1/2]
        // Copre il ramo 'else' per le parentesi quadre
        String input = "807 Lezione Importante [3/4]";
        SubjectParser parser = new SubjectParser(input);
        
        String expectedTitle = "Lezione Importante ";
        String actualTitle = parser.getTitle();
        
        assertEquals("Il titolo dovrebbe gestire le parentesi quadre", expectedTitle, actualTitle);
    }

    @Test
    public void metodo_getTitle_Test_3() {
        // Path 3: Nessun range presente
        // Copre il caso in cui il loop finisce senza trovare range validi
        String input = "807 Lezione Senza Range";
        SubjectParser parser = new SubjectParser(input);
        
        String expectedTitle = "Lezione Senza Range";
        String actualTitle = parser.getTitle();
        
        assertEquals("Il titolo dovrebbe essere identico se non c'è range", expectedTitle, actualTitle);
    }
    
    @Test
    public void metodo_getTitle_Test_4() {
        // Path 4: Gestione Exception (Input malformato che causa errore substring)
        // Input senza spazi causerà eccezione nella prima substring: Subject.indexOf(" ")
        String input = "NoSpaceString"; 
        SubjectParser parser = new SubjectParser(input);
        
        String actualTitle = parser.getTitle();
        
        assertNull("Il metodo dovrebbe ritornare null in caso di eccezione", actualTitle);
    }

    // -----------------------------------------------------------
    // Metodo testato: getRangeString()
    // Complessità Ciclomatica metodo originale: 3
    // -----------------------------------------------------------

    @Test
    public void metodo_getRangeString_Test_1() {
        // Path 1: RangeString calcolato tramite getTitle (RangeString è null all'inizio)
        String input = "807 Test (5/10)";
        SubjectParser parser = new SubjectParser(input);
        
        // getRangeString chiama getTitle internamente se null
        String expected = "(5/10)";
        String actual = parser.getRangeString();
        
        assertEquals("Dovrebbe restituire la stringa del range", expected, actual);
    }
    
    @Test
    public void metodo_getRangeString_Test_2() {
        // Path 2: Caso in cui non c'è range (ritorna null)
        String input = "807 Test Semplice";
        SubjectParser parser = new SubjectParser(input);
        
        String actual = parser.getRangeString();
        
        assertNull("Dovrebbe essere null se non ci sono range", actual);
    }

    // -----------------------------------------------------------
    // Metodo testato: getThisRange() (Lower Range)
    // Complessità Ciclomatica metodo originale: 3
    // Testa indirettamente anche messageParts() (CC: 4)
    // -----------------------------------------------------------

    @Test
    public void metodo_getThisRange_Test_1() {
        // Path 1: Parsing corretto con parentesi tonde
        // messageParts -> try block 1
        String input = "807 Test (10/20)";
        SubjectParser parser = new SubjectParser(input);
        
        // Forza il parsing chiamando getRangeString o getTitle implicitamente
        parser.getRangeString(); 
        
        int expected = 10;
        int actual = parser.getThisRange();
        
        assertEquals("Il LowerRange dovrebbe essere 10", expected, actual);
    }

    @Test
    public void metodo_getThisRange_Test_2() {
        // Path 2: Parsing corretto con parentesi quadre
        // messageParts -> catch -> try block 2
        String input = "807 Test [5/15]";
        SubjectParser parser = new SubjectParser(input);
        
        parser.getRangeString();
        
        int expected = 5;
        int actual = parser.getThisRange();
        
        assertEquals("Il LowerRange dovrebbe essere 5 con parentesi quadre", expected, actual);
    }
    
    @Test
    public void metodo_getThisRange_Test_3() {
        // Path 3: Fallimento parsing (parts == null)
        // Ritorna il valore di default (LowerRange inizializzato a 1 nel costruttore)
        String input = "807 Test (A/B)"; // Formato non numerico
        SubjectParser parser = new SubjectParser(input);
        
        parser.getRangeString(); // Tenterà di parsare ma il contenuto non è int
        
        int expected = 1; // Default value
        int actual = parser.getThisRange();
        
        assertEquals("Dovrebbe ritornare il default (1) se il parsing fallisce", expected, actual);
    }

    // -----------------------------------------------------------
    // Metodo testato: getUpperRange()
    // Complessità Ciclomatica metodo originale: 3
    // -----------------------------------------------------------

    @Test
    public void metodo_getUpperRange_Test_1() {
        // Path 1: Parsing corretto Upper Range
        String input = "807 Test (10/99)";
        SubjectParser parser = new SubjectParser(input);
        
        parser.getRangeString();
        
        int expected = 99;
        int actual = parser.getUpperRange();
        
        assertEquals("L'UpperRange dovrebbe essere 99", expected, actual);
    }
    
    @Test
    public void metodo_getUpperRange_Test_2() {
        // Path 2: Fallimento/Nessun range
        String input = "807 Test No Range";
        SubjectParser parser = new SubjectParser(input);
        
        parser.getRangeString();
        
        int expected = 1; // Default value nel costruttore
        int actual = parser.getUpperRange();
        
        assertEquals("Dovrebbe ritornare il default (1) se non c'è range", expected, actual);
    }
}