/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Inserisci il tuo nome"
Cognome: "Inserisci il tuo cognome"
Username: antonio.auricchio5@studenti.unina.it
UserID: 597
Date: 22/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // Nota: Le annotazioni @Before, @After, @BeforeClass, @AfterClass sono state rimosse
    // come richiesto, pur essendo presenti nel template fornito.

    // ====================================================================================
    // Test per il metodo getId()
    // ====================================================================================

    @Test
    public void getIdValidIdTest() {
        // Subject valido: inizia con un ID numerico
        SubjectParser parser = new SubjectParser("12345 Questo e' un titolo");
        long expected = 12345L;
        long actual = parser.getId();
        assertEquals("getId() dovrebbe restituire l'ID numerico corretto", expected, actual);
    }

    @Test
    public void getIdInvalidIdReturnsNegativeOneTest() {
        // Subject non valido: inizia con una stringa non numerica
        SubjectParser parser = new SubjectParser("ABCDE Questo e' un titolo");
        long expected = -1L;
        long actual = parser.getId();
        assertEquals("getId() dovrebbe restituire -1 se l'ID non e' un long valido", expected, actual);
    }

    @Test
    public void getIdNullSubjectReturnsNegativeOneTest() {
        // Subject null (simulazione, anche se il costruttore accetta solo String e lo assegna)
        // Viene testata la gestione delle eccezioni all'interno di getId()
        SubjectParser parser = new SubjectParser(null);
        long expected = -1L;
        long actual = parser.getId();
        assertEquals("getId() dovrebbe restituire -1 per un Subject nullo o che causa Exception", expected, actual);
    }

    @Test
    public void getIdEmptySubjectReturnsNegativeOneTest() {
        // Subject vuoto: StringTokenizer fallisce
        SubjectParser parser = new SubjectParser("");
        long expected = -1L;
        long actual = parser.getId();
        assertEquals("getId() dovrebbe restituire -1 per un Subject vuoto", expected, actual);
    }

    // ====================================================================================
    // Test per il metodo getRangeString()
    // ====================================================================================

    @Test
    public void getRangeStringParenthesesRangeTest() {
        // Range tra parentesi: (x/y)
        SubjectParser parser = new SubjectParser("1 Title with (2/10) range");
        // La chiamata a getTitle() è implicita per popolare RangeString
        parser.getTitle();
        String expected = "(2/10)";
        String actual = parser.getRangeString();
        assertEquals("getRangeString() dovrebbe trovare la stringa di range tra parentesi", expected, actual);
    }

    @Test
    public void getRangeStringBracketsRangeTest() {
        // Range tra parentesi quadre: [x/y]
        SubjectParser parser = new SubjectParser("1 Title with [2/10] range");
        // La chiamata a getTitle() è implicita per popolare RangeString
        parser.getTitle();
        String expected = "[2/10]";
        String actual = parser.getRangeString();
        assertEquals("getRangeString() dovrebbe trovare la stringa di range tra parentesi quadre", expected, actual);
    }

    @Test
    public void getRangeStringNoRangeTest() {
        // Nessun range, RangeString rimane null dopo la chiamata a getTitle()
        SubjectParser parser = new SubjectParser("1 Title without range");
        parser.getTitle();
        String actual = parser.getRangeString();
        assertNull("getRangeString() dovrebbe restituire null se non viene trovato nessun range", actual);
    }

    @Test
    public void getRangeStringRangeStringPrecalculatedTest() {
        // Verifica che RangeString sia restituito se è già calcolato
        SubjectParser parser = new SubjectParser("1 Title with (2/10)");
        parser.getTitle(); // Calcola e setta RangeString
        String expected = "(2/10)";
        String actual = parser.getRangeString();
        assertEquals("getRangeString() dovrebbe restituire RangeString se e' gia' calcolato", expected, actual);
    }

    @Test
    public void getRangeStringNullSubjectTest() {
        // Subject nullo che scatena una NullPointerException in getTitle()
        SubjectParser parser = new SubjectParser(null);
        String actual = parser.getRangeString(); // Chiama getTitle() internamente
        assertNull("getRangeString() dovrebbe restituire null se getTitle fallisce a causa di Subject nullo", actual);
    }

    // ====================================================================================
    // Test per il metodo getTitle()
    // ====================================================================================

    @Test
    public void getTitleWithParenthesesRangeTest() {
        // Titolo con range tra parentesi alla fine
        SubjectParser parser = new SubjectParser("1 The actual title (2/10)");
        String expected = "The actual title ";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe estrarre il titolo escludendo il range tra parentesi", expected, actual);
    }

    @Test
    public void getTitleWithBracketsRangeTest() {
        // Titolo con range tra parentesi quadre alla fine
        SubjectParser parser = new SubjectParser("1 The actual title [2/10]");
        String expected = "The actual title ";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe estrarre il titolo escludendo il range tra parentesi quadre", expected, actual);
    }

    @Test
    public void getTitleNoRangeTest() {
        // Titolo senza range
        SubjectParser parser = new SubjectParser("1 Simple Title");
        String expected = "Simple Title";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe restituire l'intero titolo se nessun range e' presente", expected, actual);
        assertNull("RangeString dovrebbe essere null quando nessun range e' trovato", parser.getRangeString());
    }

    @Test
    public void getTitleRangeWithExtraTextTest() {
        // Test per il ramo di codice dove viene trovato il carattere di chiusura ma non e' un range valido
        SubjectParser parser = new SubjectParser("1 Title with )something( not a range");
        String expected = "Title with )something( not a range";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe restituire tutto se la sequenza chiusa non e' un range valido", expected, actual);
        assertNull("RangeString dovrebbe essere null quando non e' un range valido", parser.getRangeString());
    }

    @Test
    public void getTitleRangeWithNoStartTokenTest() {
        // Test per il ramo in cui il ciclo while (nextchar != endchar) non trova il token di inizio.
        SubjectParser parser = new SubjectParser("1 Title )no-start"); // Il loop 'while' arriva all'inizio della substring senza trovare '('.
        String expected = "Title )no-start";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe restituire l'intero titolo se il token di inizio non e' trovato", expected, actual);
    }

    @Test
    public void getTitleRangeWithNonNumericInsideTest() {
        // Range con caratteri non numerici o '/' all'interno
        SubjectParser parser = new SubjectParser("1 Title (a/10)");
        String expected = "Title (a/10)";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe includere la parte tra parentesi se contiene caratteri non validi per un range", expected, actual);
        assertNull("RangeString dovrebbe essere null in caso di range non valido", parser.getRangeString());
    }
    
    @Test
    public void getTitleRangeWithMissingSlashTest() {
        // Range senza lo slash '/'
        SubjectParser parser = new SubjectParser("1 Title (10)");
        String expected = "Title (10)";
        String actual = parser.getTitle();
        assertEquals("getTitle() dovrebbe includere la parte tra parentesi se manca '/'", expected, actual);
        assertNull("RangeString dovrebbe essere null in caso di range non valido senza slash", parser.getRangeString());
    }

    @Test
    public void getTitleSubjectIsNullTest() {
        // Subject nullo che scatena NullPointerException in getTitle()
        SubjectParser parser = new SubjectParser(null);
        String actual = parser.getTitle();
        assertNull("getTitle() dovrebbe restituire null se Subject e' nullo", actual);
    }

    // ====================================================================================
    // Test per il metodo getThisRange() (LowerRange)
    // ====================================================================================

    @Test
    public void getThisRangeValidParenthesesTest() {
        // Range valido tra parentesi (1/10)
        SubjectParser parser = new SubjectParser("1 Title with (2/10)");
        int expected = 2;
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire il limite inferiore da range tra parentesi", expected, actual);
    }

    @Test
    public void getThisRangeValidBracketsTest() {
        // Range valido tra parentesi quadre [3/15]
        SubjectParser parser = new SubjectParser("1 Title with [3/15]");
        int expected = 3;
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire il limite inferiore da range tra parentesi quadre", expected, actual);
    }

    @Test
    public void getThisRangeNoRangeTest() {
        // Nessun range: messageParts() restituisce null
        SubjectParser parser = new SubjectParser("1 Title without range");
        int expected = 1; // Valore di default
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire il valore di default (1) se non trova il range", expected, actual);
    }

    @Test
    public void getThisRangeInvalidRangeStringTest() {
        // RangeString non valido (es. numeri non validi o formato errato)
        // Viene gestito all'interno di messageParts() e dovrebbe restituire il default
        SubjectParser parser = new SubjectParser("1 Title with (A/B)");
        // L'errore viene catturato in messageParts -> subE, messageParts restituisce null
        int expected = 1; // Valore di default
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire il valore di default (1) se il range non e' valido", expected, actual);
    }

    @Test
    public void getThisRangeRangeStringSubstringFailTest() {
        // Test edge case che fallisce nel primo blocco try/catch di messageParts (Parentesi)
        SubjectParser parser = new SubjectParser("1 Title with 2/10)"); // Manca la parentesi aperta. indexOf('(') == -1. lastIndexOf('(') == -1. substring fallisce
        int expected = 1; // Valore di default
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire 1 se la substring del range tra parentesi fallisce", expected, actual);
    }

    @Test
    public void getThisRangeRangeStringSubstringFailSecondTryTest() {
        // Test edge case che fallisce nel secondo blocco try/catch di messageParts (Parentesi Quadre)
        SubjectParser parser = new SubjectParser("1 Title with 2/10]"); // Manca la parentesi quadra aperta. indexOf('[') == -1. lastIndexOf('[') == -1. substring fallisce
        int expected = 1; // Valore di default
        int actual = parser.getThisRange();
        assertEquals("getThisRange() dovrebbe restituire 1 se la substring del range tra parentesi quadre fallisce", expected, actual);
    }

    // ====================================================================================
    // Test per il metodo getUpperRange()
    // ====================================================================================

    @Test
    public void getUpperRangeValidParenthesesTest() {
        // Range valido tra parentesi (1/10)
        SubjectParser parser = new SubjectParser("1 Title with (2/10)");
        int expected = 10;
        int actual = parser.getUpperRange();
        assertEquals("getUpperRange() dovrebbe restituire il limite superiore da range tra parentesi", expected, actual);
    }

    @Test
    public void getUpperRangeValidBracketsTest() {
        // Range valido tra parentesi quadre [3/15]
        SubjectParser parser = new SubjectParser("1 Title with [3/15]");
        int expected = 15;
        int actual = parser.getUpperRange();
        assertEquals("getUpperRange() dovrebbe restituire il limite superiore da range tra parentesi quadre", expected, actual);
    }

    @Test
    public void getUpperRangeNoRangeTest() {
        // Nessun range: messageParts() restituisce null
        SubjectParser parser = new SubjectParser("1 Title without range");
        int expected = 1; // Valore di default
        int actual = parser.getUpperRange();
        assertEquals("getUpperRange() dovrebbe restituire il valore di default (1) se non trova il range", expected, actual);
    }

    @Test
    public void getUpperRangeInvalidRangeStringTest() {
        // RangeString non valido (es. numeri non validi o formato errato)
        // Viene gestito all'interno di messageParts() e dovrebbe restituire il default
        SubjectParser parser = new SubjectParser("1 Title with (A/B)");
        // L'errore viene catturato in messageParts -> subE, messageParts restituisce null
        int expected = 1; // Valore di default
        int actual = parser.getUpperRange();
        assertEquals("getUpperRange() dovrebbe restituire il valore di default (1) se il range non e' valido", expected, actual);
    }
}