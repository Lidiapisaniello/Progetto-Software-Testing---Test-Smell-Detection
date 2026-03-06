import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    /**
     * Test del "Happy Path" con parentesi tonde.
     * Verifica: Parsing ID, estrazione Titolo, estrazione Range (Lower/Upper).
     * Copre il primo blocco 'try' di messageParts().
     */
    @Test
    public void testStandardRoundBrackets() {
        String input = "12345 Il Mio Oggetto (1/10)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Verifica ID", 12345L, parser.getId());
        assertEquals("Verifica Range String", "(1/10)", parser.getRangeString());
        assertEquals("Verifica Titolo", "Il Mio Oggetto ", parser.getTitle());
        assertEquals("Verifica Lower Range", 1, parser.getThisRange());
        assertEquals("Verifica Upper Range", 10, parser.getUpperRange());
    }

    /**
     * Test con parentesi quadre.
     * CRITICO: Questo test forza 'messageParts' a lanciare un'eccezione nel primo blocco
     * (perché non trova la parentesi tonda) e ad entrare nel blocco 'catch' per gestire le quadre.
     */
    @Test
    public void testSquareBrackets() {
        String input = "999 Oggetto Alternativo [5/20]";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Verifica ID", 999L, parser.getId());
        assertEquals("Verifica Range String", "[5/20]", parser.getRangeString());
        // Verifica che il titolo sia pulito (nota lo spazio finale lasciato dalla logica di parsing)
        assertEquals("Verifica Titolo", "Oggetto Alternativo ", parser.getTitle());
        assertEquals("Verifica Lower Range", 5, parser.getThisRange());
        assertEquals("Verifica Upper Range", 20, parser.getUpperRange());
    }

    /**
     * Test per input senza alcun range specificato.
     * Verifica che i valori di default (1 e 1) siano mantenuti e RangeString sia null.
     */
    @Test
    public void testNoRange() {
        String input = "100 Solo Titolo Senza Range";
        SubjectParser parser = new SubjectParser(input);

        assertEquals(100L, parser.getId());
        assertNull("RangeString dovrebbe essere null", parser.getRangeString());
        assertEquals("Il titolo dovrebbe essere tutto il testo (eccetto ID)", "Solo Titolo Senza Range", parser.getTitle());
        // Default values
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Test per ID non numerico.
     * Copre il blocco catch di getId().
     */
    @Test
    public void testInvalidId() {
        String input = "ABC Titolo (1/2)";
        SubjectParser parser = new SubjectParser(input);

        assertEquals("Dovrebbe restituire -1 per ID non valido", -1L, parser.getId());
        assertEquals("(1/2)", parser.getRangeString());
    }

    /**
     * Test per gestione Input Null.
     * Copre i blocchi catch più esterni per NullPointerException.
     */
    @Test
    public void testNullInput() {
        SubjectParser parser = new SubjectParser(null);

        assertEquals(-1L, parser.getId());
        assertNull(parser.getRangeString());
        assertNull(parser.getTitle());
        // Dovrebbe ritornare i default dato che tutto fallisce
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Test 'Fake Range': Sembra un range ma contiene lettere.
     * Copre la logica "continue MAINLOOP" in getTitle dove verifica Character.isDigit.
     * Poiché contiene 'A', il parser dovrebbe considerarlo parte del titolo e non un range.
     */
    @Test
    public void testMalformedRangeWithLetters() {
        String input = "500 Titolo (1/A)";
        SubjectParser parser = new SubjectParser(input);

        // RangeString deve essere null perché (1/A) non è un range valido
        assertNull(parser.getRangeString());
        // Il "finto" range diventa parte del titolo
        assertEquals("Titolo (1/A)", parser.getTitle());
    }

    /**
     * Test 'Fake Range': Parentesi corrette, numeri corretti, ma manca lo slash separator.
     * Copre l'if "indexOf('/') != -1" in getTitle.
     */
    @Test
    public void testRangeWithoutSlash() {
        String input = "600 Titolo (123)";
        SubjectParser parser = new SubjectParser(input);

        assertNull(parser.getRangeString());
        assertEquals("Titolo (123)", parser.getTitle());
    }

    /**
     * Test per range malformato che passa il getTitle ma fallisce il parsing numerico in messageParts.
     * Questo è un caso limite complesso:
     * getTitle accetta cifre e '/', quindi "(9999999999/1)" è valido come stringa,
     * ma Integer.parseInt lancerà eccezione per overflow.
     * Questo copre il 'return null' nested dentro messageParts.
     */
    @Test
    public void testNumericOverflowInRange() {
        // 9999999999 è più grande di Integer.MAX_VALUE
        String input = "700 Overflow (9999999999/1)";
        SubjectParser parser = new SubjectParser(input);

        // getTitle lo accetta come stringa
        assertNotNull(parser.getRangeString());
        
        // Ma getThisRange fallisce il parsing int e ritorna il default (che è stato settato a 1 nel costruttore)
        // In realtà, se messageParts ritorna null, getThisRange non aggiorna LowerRange.
        assertEquals(1, parser.getThisRange());
    }
    
    /**
     * Test specifico per stringa vuota.
     * StringTokenizer lancerà eccezione in getId.
     */
    @Test
    public void testEmptyString() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
        // getTitle fallirà su substring o indexOf
        assertNull(parser.getTitle());
    }

    /**
     * Test robustezza: Solo ID senza spazi.
     * getTitle usa "Subject.indexOf(" ")", se manca lo spazio potrebbe lanciare eccezione
     * o gestire l'indice -1. In questo codice: -1 + 1 = 0, substring(0) funziona, 
     * ma la logica successiva potrebbe variare.
     */
    @Test
    public void testOnlyIdNoTitle() {
        String input = "12345";
        SubjectParser parser = new SubjectParser(input);
        
        // getId parsa il token
        assertEquals(12345L, parser.getId());
        
        // getTitle prende substring da 0, cicla, non trova parentesi.
        // Ritorna la stringa originale (o buffer costruito)
        String title = parser.getTitle();
        assertNotNull(title);
        assertEquals("12345", title); 
    }
    
    /**
     * Test Edge Case: Range valido ma nidificato o testo dopo il range.
     * La logica del loop è inversa (dalla fine all'inizio), quindi dovrebbe prendere l'ultimo range valido.
     */
    @Test
    public void testTextAfterRange() {
        // Il parser cicla dalla fine. Se trova caratteri non range prima di ')' o ']', li mette nel titolo.
        // Appena trova ')' inizia a parsare il range.
        String input = "800 Titolo (1/2) Suffisso"; 
        SubjectParser parser = new SubjectParser(input);
        
        // Qui il parser inizia da 'o' di Suffisso. Non è ')' o ']'.
        // Continua finché non trova ')'.
        // Quindi il Range viene trovato correttamente, il resto è titolo.
        assertEquals("(1/2)", parser.getRangeString());
        assertEquals("Titolo  Suffisso", parser.getTitle()); // Nota: lo spazio del range viene "mangiato" o preservato a seconda della logica
        assertEquals(1, parser.getThisRange());
    }
}