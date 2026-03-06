/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: m.ciamarra@studenti.unina.it
UserID: 115
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- ID PARSING TESTS ---

    @Test
    public void testGetId_Valid() {
        SubjectParser parser = new SubjectParser("123 Test Subject");
        assertEquals(123, parser.getId());
    }

    @Test
    public void testGetId_InvalidAlpha() {
        SubjectParser parser = new SubjectParser("Re: Test Subject");
        // Long.parseLong fallisce, catch ritorna -1
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testGetId_Empty() {
        SubjectParser parser = new SubjectParser("");
        // StringTokenizer fallisce, catch ritorna -1
        assertEquals(-1, parser.getId());
    }

    @Test
    public void testGetId_Null() {
        SubjectParser parser = new SubjectParser(null);
        // Costruttore accetta null, StringTokenizer lancia NullPointer, catch ritorna -1
        assertEquals(-1, parser.getId());
    }

    // --- ROUND BRACKETS PARSING (Primary Path) ---

    @Test
    public void testRanges_RoundBrackets_Valid() {
        // Formato (low/high)
        SubjectParser parser = new SubjectParser("100 My Title (1/5)");
        
        // Verifica parsing range
        assertEquals(1, parser.getThisRange());
        assertEquals(5, parser.getUpperRange());
        
        // Verifica che il range sia stato rimosso dal titolo
        // La logica di getTitle lascia lo spazio finale prima del range
        assertEquals("My Title ", parser.getTitle());
        assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void testRanges_RoundBrackets_OnlyOneDigit() {
        // Verifica robustezza substring
        SubjectParser parser = new SubjectParser("100 T (1/2)");
        assertEquals(1, parser.getThisRange());
        assertEquals(2, parser.getUpperRange());
    }

    // --- SQUARE BRACKETS PARSING (Catch Block Path) ---

    @Test
    public void testRanges_SquareBrackets_Valid() {
        // Questo test è CRUCIALE per la coverage.
        // messageParts fallisce il primo try su '(', entra nel catch,
        // e prova il secondo try su '['.
        SubjectParser parser = new SubjectParser("100 My Title [3/7]");
        
        assertEquals(3, parser.getThisRange());
        assertEquals(7, parser.getUpperRange());
        assertEquals("My Title ", parser.getTitle());
    }

    // --- MALFORMED / FAKE RANGES (Mutation Killing) ---

    @Test
    public void testRanges_NoSlash_IsPartOfTitle() {
        // "2023" tra parentesi sembra un range, ma manca la '/'
        // Il ciclo in getTitle dovrebbe scartarlo come range e tenerlo nel titolo.
        SubjectParser parser = new SubjectParser("100 Year (2023)");
        
        // Ranges rimangono default (1, 1)
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
        
        // Il titolo deve contenere (2023)
        assertEquals("Year (2023)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void testRanges_NonDigit_IsPartOfTitle() {
        // (1/A) contiene '/' ma 'A' non è digit.
        // getTitle ha un check Character.isDigit che dovrebbe fallire.
        SubjectParser parser = new SubjectParser("100 Test (1/A)");
        
        assertEquals(1, parser.getThisRange());
        assertEquals("Test (1/A)", parser.getTitle());
    }

    @Test
    public void testRanges_MissingBrackets() {
        // Manca la chiusura
        SubjectParser parser = new SubjectParser("100 Test (1/5");
        
        assertEquals(1, parser.getThisRange());
        // Stringa intera considerata titolo (o quasi, dipende dall'indexOf " ")
        assertEquals("Test (1/5", parser.getTitle());
    }
    
    @Test
    public void testRanges_MissingOpeningBracket() {
        // Solo chiusura
        SubjectParser parser = new SubjectParser("100 Test 1/5)");
        
        assertEquals(1, parser.getThisRange());
        assertEquals("Test 1/5)", parser.getTitle());
    }

    // --- EDGE CASES & EXCEPTIONS ---

    @Test
    public void testGetTitle_NoSpaceSeparator() {
        // getTitle fa substring da indexOf(" ") + 1.
        // Se non c'è spazio, indexOf = -1, +1 = 0. Funziona?
        SubjectParser parser = new SubjectParser("100Title(1/2)");
        // Se Subject="100Title(1/2)", indexOf(" ") è -1.
        // substring(0, length) prende tutto.
        // Poi cerca range.
        
        // Nota: getId fallirà (ritorna 100Title... exception -> -1), ma qui testiamo parsing range.
        assertEquals(1, parser.getThisRange());
        assertEquals(2, parser.getUpperRange());
        assertEquals("100Title", parser.getTitle()); 
    }

    @Test
    public void testMessageParts_Exception_Nested() {
        // Caso impossibile da parsare come range ma con parentesi che confondono
        // Deve ritornare null e mantenere i default
        SubjectParser parser = new SubjectParser("100 T [1/A]"); // [ ] ma non numerico
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testGetRangeString_Directly() {
        // Copertura diretta
        SubjectParser parser = new SubjectParser("100 T (5/5)");
        assertEquals("(5/5)", parser.getRangeString());
        // Seconda chiamata deve ritornare il valore cachato
        assertEquals("(5/5)", parser.getRangeString());
    }
    
    @Test
    public void testNullSubject_Methods() {
        SubjectParser parser = new SubjectParser(null);
        // getTitle lancia eccezione internamente, catchata, ritorna null
        assertNull(parser.getTitle());
        // getRangeString chiama getTitle -> null
        assertNull(parser.getRangeString());
        // getThisRange chiama messageParts -> catch -> null -> non aggiorna LowerRange
        assertEquals(1, parser.getThisRange());
    }
}