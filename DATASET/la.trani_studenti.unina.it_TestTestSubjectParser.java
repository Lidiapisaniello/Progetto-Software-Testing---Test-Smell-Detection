/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: la.trani@studenti.unina.it
UserID: 1014
Date: 25/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Test getId() ---

    @Test
    public void testGetId_ValidNumber() {
        SubjectParser parser = new SubjectParser("12345 Test Subject");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetId_NegativeNumber() {
        SubjectParser parser = new SubjectParser("-50 Test Subject");
        assertEquals(-50L, parser.getId());
    }

    @Test
    public void testGetId_InvalidNumber_ReturnsNegativeOne() {
        // Il primo token non è un numero
        SubjectParser parser = new SubjectParser("abc Test Subject");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_NullSubject_ReturnsNegativeOne() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_EmptySubject_ReturnsNegativeOne() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    // --- Test getTitle() e getRangeString() ---

    @Test
    public void testGetTitle_StandardRoundBrackets() {
        // Percorso felice con parentesi tonde
        SubjectParser parser = new SubjectParser("100 My Title (1/5)");
        
        // getTitle viene chiamato internamente se RangeString è null
        assertEquals("(1/5)", parser.getRangeString());
        assertEquals("My Title ", parser.getTitle());
    }

    @Test
    public void testGetTitle_StandardSquareBrackets() {
        // Percorso felice con parentesi quadre
        SubjectParser parser = new SubjectParser("100 My Title [2/10]");
        
        assertEquals("[2/10]", parser.getRangeString());
        assertEquals("My Title ", parser.getTitle());
    }

    @Test
    public void testGetTitle_NoRangePresent() {
        SubjectParser parser = new SubjectParser("100 Just A Title");
        
        assertNull(parser.getRangeString());
        // Nota: getTitle riesegue il parsing ogni volta, ma se non trova range restituisce la stringa pulita
        assertEquals("Just A Title", parser.getTitle());
    }

    @Test
    public void testGetTitle_MultipleRanges_TakesLastOne() {
        // Il loop è all'indietro, quindi dovrebbe prendere l'ultimo range valido
        // e considerare il primo come parte del titolo
        SubjectParser parser = new SubjectParser("100 Title (1/2) (3/4)");
        
        assertEquals("(3/4)", parser.getRangeString());
        // (1/2) viene trattato come testo perché FoundRange diventa true
        assertEquals("Title (1/2) ", parser.getTitle());
    }

    @Test
    public void testGetTitle_InvalidRange_NoSlash() {
        // Manca lo slash, quindi non deve essere considerato un RangeString
        SubjectParser parser = new SubjectParser("100 Title (12)");
        
        assertNull(parser.getRangeString());
        assertEquals("Title (12)", parser.getTitle());
    }

    @Test
    public void testGetTitle_InvalidRange_NonDigitChar() {
        // Contiene 'a', quindi il loop interno si rompe e lo considera parte del titolo
        SubjectParser parser = new SubjectParser("100 Title (1a/2)");
        
        assertNull(parser.getRangeString());
        assertEquals("Title (1a/2)", parser.getTitle());
    }

    @Test
    public void testGetTitle_BrokenBrackets_Nested() {
        // Logica complessa: il parser cerca l'apertura corrispondente.
        // Qui: trova ')', cerca '(', trova '(', ok.
        SubjectParser parser = new SubjectParser("100 Title (1/(2))");
        
        // Il parser interno retrocede da ')' finale. 
        // Incontra ')', non è digit e non è '/', ma il loop while((nextchar = tmpSubject.charAt(--i)) != endchar)
        // continua finché non trova '('. 
        // Tuttavia, all'interno del while c'è un check: Character.isDigit(nextchar).
        // Se trova una parentesi interna, isDigit fallisce -> break loop -> non è un range.
        
        assertNull(parser.getRangeString());
        assertEquals("Title (1/(2))", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_MixedBrackets() {
        // Verifica che una chiusa ] cerchi una aperta [
        SubjectParser parser = new SubjectParser("100 Title (1/2]");
        
        // Trova ']', cerca '[', trova '(', loop finisce o fallisce check.
        // ']' triggers search for '['. Finds '2', '/', '1', '('. 
        // '(' non è '['. isDigit('(') è falso. 
        // Quindi il range viene scartato.
        assertNull(parser.getRangeString()); 
    }

    @Test
    public void testGetTitle_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        // Genera eccezione interna catturata
        assertNull(parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitle_ShortSubject_NoSpace() {
        // indexOf(" ") fallisce o ritorna -1
        SubjectParser parser = new SubjectParser("100");
        // indexOf(" ") == -1. substring(-1 + 1) -> substring(0) -> "100".
        // Loop cerca parentesi in "100". Non ne trova.
        assertEquals("100", parser.getTitle());
    }

    // --- Test getThisRange() e getUpperRange() tramite messageParts() ---

    @Test
    public void testRanges_HappyPath_Round() {
        SubjectParser parser = new SubjectParser("100 T (5/15)");
        assertEquals(5, parser.getThisRange());
        assertEquals(15, parser.getUpperRange());
    }

    @Test
    public void testRanges_HappyPath_Square() {
        // Questo triggera il blocco "catch (Exception inte)" in messageParts
        // perché il primo tentativo cerca "(" e fallisce, quindi prova con "["
        SubjectParser parser = new SubjectParser("100 T [10/20]");
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testRanges_DefaultValues() {
        // Nessun range nel subject -> Defaults a 1
        SubjectParser parser = new SubjectParser("100 T");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Malformed_RangeFoundButParsingFails() {
        // getTitle trova un range valido sintatticamente "(1/)", 
        // ma messageParts fallirà nel parsing numerico.
        
        // "1/" -> Tokenizer ha "1". nextToken() per high fallisce.
        SubjectParser parser = new SubjectParser("100 T (1/)");
        
        // RangeString sarà "(1/)"
        assertEquals("(1/)", parser.getRangeString());
        
        // messageParts lancia eccezione nel primo blocco, 
        // va nel catch inte, prova con '[' (fallisce),
        // va nel catch subE -> return null.
        // I getter ritornano i valori default (che iniziano a 1).
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_Malformed_SlashOnly() {
        // Caso limite: getTitle accetta (/) ?
        // nextchar è '/'. isDigit è false. MA nextchar != '/' è false.
        // Quindi ((false) && false) -> false. Il loop continua.
        // Trova '('. RangeString diventa "(/)".
        SubjectParser parser = new SubjectParser("100 T (/)");
        
        assertEquals("(/)", parser.getRangeString());
        
        // messageParts prova a parsare "/".
        // StringTokenizer con "/" su "/" -> 0 tokens.
        // st.nextToken lancia eccezione.
        // Risultato: default values.
        assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void testRanges_Square_Malformed() {
        // Forza l'entrata nel secondo blocco try (quadre) e fallo fallire
        SubjectParser parser = new SubjectParser("100 T [1/]");
        
        assertEquals("[1/]", parser.getRangeString());
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testMessageParts_NullRangeString() {
        // Se getRangeString ritorna null, messageParts va in eccezione su mainrange.substring
        // Viene catturata dal catch esterno (Exception e) -> printStackTrace -> return null
        SubjectParser parser = new SubjectParser("100 No Range");
        
        assertNull(parser.getRangeString());
        // I metodi getThisRange/getUpperRange gestiscono il null return di messageParts
        assertEquals(1, parser.getThisRange());
    }

    // --- Test Weak Mutation & Edge Cases ---

    @Test
    public void testGetTitle_OnlySpace() {
        SubjectParser parser = new SubjectParser("100 "); // Spazio alla fine
        // tmpSubject diventa vuoto ""
        // loop non parte
        assertEquals("", parser.getTitle());
    }
    
    @Test
    public void testParseFormat_SlashAtStart() {
        // ( /1) -> Parsing int fallirà su stringa vuota o spazio
        SubjectParser parser = new SubjectParser("100 T (/1)");
        assertEquals(1, parser.getThisRange()); // Fallisce e torna default
    }

    @Test
    public void testParseFormat_ComplexTitleWithBrackets() {
        // Verifica che il titolo venga ricostruito correttamente quando ci sono parentesi scartate
        SubjectParser parser = new SubjectParser("100 A (B) [C] D (1/2)");
        // Range è (1/2)
        // Titolo deve contenere il resto, invertito correttamente dallo StringBuffer
        assertEquals("(1/2)", parser.getRangeString());
        assertEquals("A (B) [C] D ", parser.getTitle());
    }
}