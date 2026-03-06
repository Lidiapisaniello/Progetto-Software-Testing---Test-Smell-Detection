/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alessio"
Cognome: "Marotta"
Username: alessio.marotta@studenti.unina.it
UserID: 1159
Date: 24/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    /**
     * Testa il flusso felice standard con parentesi tonde.
     * Verifica: getId, getTitle, getThisRange (Lower), getUpperRange (Upper).
     */
    @Test
    public void testStandardParsingWithParentheses() {
        // Input: ID "123", Titolo "Test Subject", Range (1/5)
        SubjectParser parser = new SubjectParser("123 Test Subject (1/5)");

        assertEquals("ID deve essere 123", 123, parser.getId());
        
        // Nota: getTitle ha uno spazio finale o iniziale a seconda di come viene costruito il buffer
        // Analisi codice: "Test Subject " (lo spazio prima della parentesi viene incluso nel titolo)
        String title = parser.getTitle();
        assertNotNull(title);
        assertTrue("Il titolo deve contenere il testo", title.contains("Test Subject"));
        
        assertEquals("RangeString deve essere settato", "(1/5)", parser.getRangeString());
        assertEquals("Lower range deve essere 1", 1, parser.getThisRange());
        assertEquals("Upper range deve essere 5", 5, parser.getUpperRange());
    }

    /**
     * Testa il flusso felice con parentesi quadre.
     * Copre il ramo 'else' in getTitle dove si decide il tipo di parentesi e il blocco catch/try secondario in messageParts.
     */
    @Test
    public void testStandardParsingWithBrackets() {
        SubjectParser parser = new SubjectParser("456 Another Title [10/20]");

        assertEquals(456, parser.getId());
        String title = parser.getTitle();
        assertTrue(title.contains("Another Title"));
        
        assertEquals("[10/20]", parser.getRangeString());
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    /**
     * Testa la logica "MAINLOOP" che scansiona all'indietro.
     * Se ci sono due range potenziali, dovrebbe prendere l'ultimo (che è il primo scansionando all'indietro)
     * e trattare il precedente come parte del titolo.
     */
    @Test
    public void testMultipleRanges() {
        // Scansionando da destra a sinistra, (3/4) viene preso come Range.
        // (1/2) viene incontrato dopo che FoundRange è true, quindi finisce nel titolo.
        SubjectParser parser = new SubjectParser("789 Start (1/2) End (3/4)");

        assertEquals("(3/4)", parser.getRangeString());
        assertEquals(3, parser.getThisRange());
        assertEquals(4, parser.getUpperRange());
        
        String title = parser.getTitle();
        // Verifica che il primo range sia diventato parte del titolo
        assertTrue("Il primo range deve essere parte del titolo", title.contains("(1/2)"));
    }

    /**
     * Testa input malformati che sembrano range ma contengono caratteri non validi.
     * Copre il ramo in getTitle: if ((Character.isDigit(nextchar) == false) && nextchar != '/')
     */
    @Test
    public void testMalformedRangeInTitle() {
        // 'a' non è cifra né slash, quindi il loop interrotto resetta e considera tutto come titolo.
        SubjectParser parser = new SubjectParser("100 Bad (1/a)");

        // Non deve rilevare range
        assertNull("Non dovrebbe esserci un RangeString valido", parser.getRangeString());
        
        // I range dovrebbero rimanere ai valori di default (o precedenti se non settati, ma qui è nuova istanza)
        // Il costruttore setta 1 e 1. Se messageParts ritorna null, restano 1.
        assertEquals(1, parser.getThisRange()); 
        
        String title = parser.getTitle();
        assertTrue("Il titolo deve contenere la parte malformata", title.contains("(1/a)"));
    }

    /**
     * Testa critico per la Coverage dei blocchi catch in messageParts.
     * Creiamo una situazione dove getTitle accetta la stringa (contiene cifre e slash),
     * ma messageParts fallisce nel parsing (es. manca il secondo numero).
     * * Input: "(1/)" -> getTitle lo accetta.
     * messageParts prova a parsare "(1/)":
     * 1. Try primario: StringTokenizer tokenizza "1", poi fallisce o Integer.parseInt fallisce. -> Catch (inte)
     * 2. Try secondario (dentro catch inte): cerca "[" ma non c'è. -> Catch (subE)
     * 3. Ritorna null.
     */
    @Test
    public void testBrokenRangeStructureExceptionPath() {
        SubjectParser parser = new SubjectParser("999 Broken (1/)");
        
        // getTitle verrà chiamato internamente da getRangeString.
        // (1/) passa il controllo di getTitle perché ha ')' '1' '/' '('.
        String rs = parser.getRangeString();
        assertEquals("(1/)", rs);

        // Ora chiamiamo i metodi che usano messageParts
        // messageParts fallirà e ritornerà null, quindi i valori restano quelli del costruttore (1)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    /**
     * Testa la gestione di ID non numerici.
     * Copre il catch in getId().
     */
    @Test
    public void testInvalidId() {
        SubjectParser parser = new SubjectParser("NotANumber Title");
        assertEquals("Dovrebbe ritornare -1 per ID non valido", -1, parser.getId());
    }

    /**
     * Testa input Null o stringhe vuote.
     * Copre i catch null pointer exception sparsi nel codice.
     */
    @Test
    public void testNullAndEmptyHandling() {
        SubjectParser parserNull = new SubjectParser(null);
        
        // getId -> StringTokenizer(null) -> NPE -> catch -> return -1
        assertEquals(-1, parserNull.getId());
        
        // getRangeString -> getTitle -> substring su null -> NPE -> catch -> return null
        assertNull(parserNull.getRangeString());
        
        // getThisRange -> messageParts -> getRangeString (null) -> substring su null -> NPE -> catch -> return null
        // result: 1 (default)
        assertEquals(1, parserNull.getThisRange());
    }
    
    /**
     * Testa la casistica in cui non esiste uno spazio per separare ID e Subject.
     * getTitle fa Subject.substring(Subject.indexOf(" ") + 1...), se non c'è spazio
     * indexOf torna -1, +1 = 0. Prende tutta la stringa.
     */
    @Test
    public void testNoSpaceSubject() {
        SubjectParser parser = new SubjectParser("JustText");
        // getId fallirà (non è numero)
        assertEquals(-1, parser.getId());
        
        // getTitle prenderà tutto
        String title = parser.getTitle();
        assertNotNull(title);
        assertTrue(title.contains("JustText"));
    }
    
    /**
     * Caso limite: Parentesi chiusa senza aperta valida o ordine inverso.
     * Copre rami del loop di parsing.
     */
    @Test
    public void testUnbalancedBrackets() {
        // Trova ']', cerca '[', ma incontra fine stringa o caratteri non validi
        SubjectParser parser = new SubjectParser("123 A 5/10]");
        
        // Non dovrebbe rilevare range valido
        assertNull(parser.getRangeString());
        assertTrue(parser.getTitle().contains("5/10]"));
    }
}