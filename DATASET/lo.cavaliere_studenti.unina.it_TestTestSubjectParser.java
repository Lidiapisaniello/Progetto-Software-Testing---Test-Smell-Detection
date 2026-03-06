/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Lorenzo
Cognome: Cavaliere
Username: lo.cavaliere@studenti.unina.it
UserID: 645
Date: 24/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Scenario 1: Standard Path (Parentheses) ---
    // Copre il flusso principale di messageParts (primo blocco try)
    @Test
    public void testStandardParenthesesFormat() {
        String input = "1001 Mathematical Analysis (1/5)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("ID should be parsed correctly", 1001, parser.getId());
        // getTitle attiva il parsing e popola RangeString
        assertEquals("Title should be extracted correctly", "Mathematical Analysis ", parser.getTitle());
        
        // Verifica parsing del range (x/y)
        assertEquals("Lower range should be 1", 1, parser.getThisRange());
        assertEquals("Upper range should be 5", 5, parser.getUpperRange());
        assertEquals("Range string should be stored", "(1/5)", parser.getRangeString());
    }

    // --- Scenario 2: Alternative Path (Brackets) ---
    // Copre il blocco CATCH -> TRY interno di messageParts
    // messageParts prova a cercare '(', fallisce, entra nel catch e cerca '['
    @Test
    public void testStandardBracketsFormat() {
        String input = "2002 Computer Science [3/10]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(2002, parser.getId());
        assertEquals("Computer Science ", parser.getTitle());
        
        // Qui testiamo che la logica di fallback sulle parentesi quadre funzioni
        assertEquals(3, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
        assertEquals("[3/10]", parser.getRangeString());
    }

    // --- Scenario 3: No Range Present ---
    // Copre il caso in cui FoundRange rimane false nel loop di getTitle
    @Test
    public void testSubjectWithoutRange() {
        String input = "3003 Just A Title Without Numbers";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(3003, parser.getId());
        // Il titolo deve contenere tutta la stringa eccetto l'ID
        assertEquals("Just A Title Without Numbers", parser.getTitle());
        
        // I range devono rimanere ai valori di default impostati nel costruttore
        assertEquals(1, parser.getThisRange()); // Default Lower
        assertEquals(1, parser.getUpperRange()); // Default Upper
        assertNull("RangeString should be null", parser.getRangeString());
    }

    // --- Scenario 4: Invalid ID Parsing ---
    // Copre il catch block in getId()
    @Test
    public void testInvalidId() {
        String input = "NotANumber Invalid ID Test (1/2)";
        SubjectParser parser = new SubjectParser(input);

        // Long.parseLong lancerà eccezione, il catch deve ritornare -1
        assertEquals(-1, parser.getId());
        assertEquals("Invalid ID Test ", parser.getTitle());
    }

    // --- Scenario 5: Complex Title Scanning (Hard-to-Cover Loop Logic) ---
    // Copre la logica "FoundRange == false" e il loop inverso in getTitle.
    // Simula parentesi che SEMBRANO un range ma non lo sono (contengono lettere).
    @Test
    public void testTitleWithFakeRangeParams() {
        String input = "4004 Title with (invalid) params (2/4)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(4004, parser.getId());
        
        // Il parser deve capire che "(invalid)" fa parte del titolo e "(2/4)" è il range vero
        assertEquals("Title with (invalid) params ", parser.getTitle());
        assertEquals(2, parser.getThisRange());
        assertEquals(4, parser.getUpperRange());
    }
    
    // --- Scenario 6: Malformed/Partial Range (Nested Exception Coverage) ---
    // Copre il caso peggiore in messageParts (Catch -> Catch -> return null)
    // Simula un range che rompe sia il parsing con '(' che quello con '['
    @Test
    public void testMalformedRangeStructure() {
        // Formato rotto: manca la chiusura o i numeri non sono validi
        String input = "5005 Bad Range Title (1/x)"; 
        SubjectParser parser = new SubjectParser(input);

        // getTitle proverà a estrarre "(1/x)", ma messageParts fallirà nel parsing dei numeri
        assertEquals("Bad Range Title ", parser.getTitle());
        
        // messageParts ritorna null, quindi i valori non vengono aggiornati (restano default)
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }

    // --- Scenario 7: Mixed/Confusing Syntax ---
    // Test di robustezza per il loop di tokenizzazione
    @Test
    public void testMixedSyntax() {
        // Un caso dove ci sono parentesi quadre nel titolo ma tonde per il range
        String input = "6006 Title [With] Brackets (5/5)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Title [With] Brackets ", parser.getTitle());
        assertEquals(5, parser.getThisRange());
    }

    // --- Scenario 8: Null/Empty Input Safety ---
    // Copre i catch più esterni per NullPointerException o IndexOutOfBounds
    @Test
    public void testEmptyInput() {
        // Stringa vuota
        SubjectParser parser = new SubjectParser(""); 
        assertEquals(-1, parser.getId()); // StringTokenizer fallisce
        assertNull(parser.getTitle()); // substring fallisce
        
        // Stringa nulla (se il costruttore lo permette, altrimenti crasha prima)
        // Guardando il codice: Subject = s. Se s è null, getId crasha nel try ma catch ritorna -1.
        SubjectParser parserNull = new SubjectParser(null);
        assertEquals(-1, parserNull.getId());
    }
    
    // --- Scenario 9: Range Only (Corner Case) ---
    // Stringa che ha a malapena ID e Range
    @Test
    public void testMinimalString() {
        String input = "1 (1/1)";
        SubjectParser parser = new SubjectParser(input);
        
        assertEquals(1, parser.getId());
        // getTitle usa substring dopo il primo spazio. Se c'è solo ID e Range:
        // Input: "1 (1/1)" -> substring da index 2 -> "(1/1)"
        // Il loop trova il range, lo estrae. Resta stringa vuota o spazio.
        String title = parser.getTitle();
        assertNotNull(title);
        assertEquals(1, parser.getThisRange());
    }
}