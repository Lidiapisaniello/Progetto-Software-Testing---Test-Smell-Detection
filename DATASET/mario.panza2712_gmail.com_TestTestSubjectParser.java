/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: mario.panza2712@gmail.com
UserID: 434
Date: 22/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    /**
     * Scenario Happy Path (Parentesi Tonde):
     * Verifica che il parser estragga correttamente ID, Titolo e Range
     * quando il formato è standard (es. 1/10).
     */
    @Test
    public void testHappyPathParentheses() {
        String input = "12345 Titolo del messaggio (1/10)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("L'ID dovrebbe essere 12345", 12345, parser.getId());
        // Nota: Il parser lascia lo spazio finale prima del range rimosso
        assertEquals("Il titolo dovrebbe essere estratto senza il range", 
                "Titolo del messaggio ", parser.getTitle());
        assertEquals("LowerRange dovrebbe essere 1", 1, parser.getThisRange());
        assertEquals("UpperRange dovrebbe essere 10", 10, parser.getUpperRange());
    }

    /**
     * Scenario Happy Path (Parentesi Quadre):
     * Verifica la logica di fallback nel metodo messageParts che gestisce le quadre [].
     */
    @Test
    public void testHappyPathBrackets() {
        String input = "999 Altro Titolo [5/20]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("L'ID dovrebbe essere 999", 999, parser.getId());
        assertEquals("Il titolo dovrebbe ignorare il contenuto delle quadre", 
                "Altro Titolo ", parser.getTitle());
        assertEquals("LowerRange dovrebbe essere 5", 5, parser.getThisRange());
        assertEquals("UpperRange dovrebbe essere 20", 20, parser.getUpperRange());
    }

    /**
     * Scenario ID Invalido:
     * Verifica che il catch block in getId() restituisca -1 se l'input non inizia con un numero.
     */
    @Test
    public void testInvalidId() {
        String input = "Errore Titolo (1/5)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Un ID non numerico dovrebbe ritornare -1", -1, parser.getId());
        // Verifica che il resto funzioni comunque se la struttura lo permette
        assertEquals("Titolo ", parser.getTitle());
    }

    /**
     * Scenario Nessun Range:
     * Verifica i valori di default quando non ci sono parentesi finali.
     */
    @Test
    public void testNoRange() {
        String input = "100 Solo Titolo";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Titolo dovrebbe essere il testo originale (meno ID)", 
                "Solo Titolo", parser.getTitle());
        assertEquals("LowerRange default deve essere 1", 1, parser.getThisRange());
        assertEquals("UpperRange default deve essere 1", 1, parser.getUpperRange());
    }

    /**
     * Scenario Range Malformato (Eccezioni Interne e Copertura Nested Catch):
     * Usa un numero troppo grande (Integer Overflow) per passare il controllo isDigit() di getTitle,
     * ma fallire Integer.parseInt() in messageParts.
     * Questo forza l'ingresso nel primo catch, poi nel tentativo con le quadre, 
     * e infine nel catch(subE) interno.
     */
    @Test
    public void testMalformedRangeWithIntegerOverflow() {
        // 9999999999 è troppo grande per un int, causerà NumberFormatException dentro messageParts
        String input = "100 Titolo (1/9999999999)";
        SubjectParser parser = new SubjectParser(input);

        // getTitle accetterà la stringa come range valido perché contiene solo cifre
        assertNotNull(parser.getRangeString()); 
        
        // messageParts fallirà il parsing e restituirà i valori di default (che sono rimasti 1)
        // In realtà messageParts ritorna null, quindi i campi LowerRange/UpperRange non vengono aggiornati
        assertEquals("LowerRange deve rimanere al default in caso di errore", 1, parser.getThisRange());
        assertEquals("UpperRange deve rimanere al default in caso di errore", 1, parser.getUpperRange());
    }

    /**
     * Scenario Formato Parziale:
     * Verifica il comportamento robusto contro stringhe spezzate.
     */
    @Test
    public void testPartialFormat() {
        String input = "100 Titolo (1/";
        SubjectParser parser = new SubjectParser(input);
        
        // getTitle non riconoscerà questo come un range valido, quindi rimarrà nel titolo
        assertEquals("Titolo (1/", parser.getTitle());
        assertEquals(1, parser.getThisRange());
    }

    /**
     * Scenario Logica MAINLOOP (Falso Positivo):
     * Verifica che il loop in getTitle continui a scansionare all'indietro se trova
     * parentesi che contengono caratteri non numerici (Fake).
     */
    @Test
    public void testMainLoopBackwardsScan() {
        // (Fake) contiene lettere, quindi il loop deve ignorarlo e continuare a cercare
        // fino a trovare (1/5) che è valido.
        String input = "100 Titolo (Fake) (1/5)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Il range valido (1/5) deve essere processato", 1, parser.getThisRange());
        assertEquals(5, parser.getUpperRange());
        
        // Il (Fake) non essendo un range valido, deve rimanere parte del titolo
        assertEquals("Il titolo deve contenere la parte ignorata", 
                "Titolo (Fake) ", parser.getTitle());
    }
    
    /**
     * Scenario Costruttore Null:
     * Verifica la robustezza contro NullPointerException in tutti i metodi.
     */
    @Test
    public void testNullConstructor() {
        SubjectParser parser = new SubjectParser(null);

        assertEquals("getId deve gestire null ritornando -1", -1, parser.getId());
        assertNull("getTitle deve gestire null ritornando null", parser.getTitle());
        assertEquals("getThisRange deve gestire null ritornando default", 1, parser.getThisRange());
        assertNull("getRangeString deve gestire null ritornando null", parser.getRangeString());
    }
}
						