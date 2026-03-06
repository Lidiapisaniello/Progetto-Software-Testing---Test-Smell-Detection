/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Il Tuo Nome"
Cognome: "Il Tuo Cognome"
Username: m.berrino@studenti.unina.it
UserID: 223
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
        System.out.println("--- Inizio Test SubjectParser ---");
    }
                
    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
        System.out.println("--- Fine Test SubjectParser ---");
    }
                
    @Before
    public void setUp() {
        // Eseguito prima di ogni metodo di test
    }
                
    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
    }

    // --- TEST CASES ---

    @Test
    public void testValidIdAndRangeParentheses() {
        // Caso standard: ID numerico e range tra parentesi tonde (es. 1/3)
        String input = "1001 Oggetto del messaggio (1/3)";
        SubjectParser parser = new SubjectParser(input);

        // Verifica ID
        assertEquals("L'ID dovrebbe essere 1001", 1001, parser.getId());

        // Verifica Range
        assertEquals("Il limite inferiore dovrebbe essere 1", 1, parser.getThisRange());
        assertEquals("Il limite superiore dovrebbe essere 3", 3, parser.getUpperRange());
        assertEquals("La stringa del range dovrebbe essere (1/3)", "(1/3)", parser.getRangeString());

        // Verifica Titolo (Nota: il parser tende a lasciare lo spazio finale prima del range)
        // Usiamo trim() per sicurezza nel confronto, oppure includiamo lo spazio se il parser lo lascia
        String title = parser.getTitle();
        assertNotNull("Il titolo non dovrebbe essere null", title);
        assertTrue("Il titolo dovrebbe contenere il testo corretto", title.trim().equals("Oggetto del messaggio"));
    }

    @Test
    public void testValidIdAndRangeBrackets() {
        // Caso alternativo: ID numerico e range tra parentesi quadre [2/5]
        String input = "2020 Altro Oggetto [2/5]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("L'ID dovrebbe essere 2020", 2020, parser.getId());
        
        // Verifica Range
        assertEquals("Il limite inferiore dovrebbe essere 2", 2, parser.getThisRange());
        assertEquals("Il limite superiore dovrebbe essere 5", 5, parser.getUpperRange());
        assertEquals("La stringa del range dovrebbe essere [2/5]", "[2/5]", parser.getRangeString());
    }

    @Test
    public void testNoRangePresent() {
        // Caso in cui non c'è nessun range nel soggetto
        String input = "300 Esempio senza range";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(300, parser.getId());
        
        // Se non c'è range, i valori di default nel costruttore sono 1
        assertEquals("Default Lower deve essere 1", 1, parser.getThisRange());
        assertEquals("Default Upper deve essere 1", 1, parser.getUpperRange());
        assertNull("La stringa range deve essere null", parser.getRangeString());
        
        // Il titolo dovrebbe essere tutto il testo dopo l'ID
        assertTrue(parser.getTitle().trim().equals("Esempio senza range"));
    }

    @Test
    public void testInvalidId() {
        // Caso in cui il primo token non è un numero
        String input = "Errore Oggetto (1/1)";
        SubjectParser parser = new SubjectParser(input);

        // Il metodo getId cattura l'eccezione e ritorna -1
        assertEquals("Un ID non numerico deve ritornare -1", -1, parser.getId());
    }

    @Test
    public void testMalformedRangeString() {
        // Caso in cui le parentesi ci sono ma il contenuto non è valido numeri
        String input = "400 Oggetto (A/B)";
        SubjectParser parser = new SubjectParser(input);

        // getTitle tenterà di parsare, fallirà nel trovare digit e restituirà il testo
        // messageParts fallirà nel parsing int
        
        assertEquals(400, parser.getId());
        
        // Dovrebbe ritornare i valori di default perché il parsing fallisce
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testTitleWithBracketsButNoRange() {
        // Un titolo che contiene parentesi ma non è un range (es. note)
        // Il parser controlla se dentro le parentesi ci sono numeri e slash
        String input = "500 Oggetto con (note) e dettagli";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(500, parser.getId());
        assertNull("Non dovrebbe rilevare un range valido", parser.getRangeString());
        
        String title = parser.getTitle();
        // Verifica che le parentesi facciano parte del titolo e non siano state rimosse
        assertTrue("Il titolo deve contenere '(note)'", title.contains("(note)"));
    }
    
    @Test
    public void testGetTitleFirstCallLogic() {
        // Test specifico per verificare che getRangeString chiami getTitle se RangeString è null
        String input = "600 Test Lazy Init (1/10)";
        SubjectParser parser = new SubjectParser(input);
        
        // Non chiamiamo parser.getTitle() esplicitamente prima
        // Chiamiamo direttamente getRangeString che dovrebbe innescare il parsing
        String range = parser.getRangeString();
        
        assertEquals("(1/10)", range);
        assertEquals(1, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }
}