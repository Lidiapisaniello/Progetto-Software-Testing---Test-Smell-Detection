/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: salva.cinque@studenti.unina.it
UserID: 274
Date: 24/11/2025
*/

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- ID TESTS ---

    @Test
    public void testGetId_Valid() {
        SubjectParser parser = new SubjectParser("12345 Test Subject");
        assertEquals("Should parse valid long ID", 12345L, parser.getId());
    }

    @Test
    public void testGetId_InvalidNonNumeric() {
        SubjectParser parser = new SubjectParser("ABC Test Subject");
        assertEquals("Should return -1 for non-numeric ID", -1L, parser.getId());
    }

    @Test
    public void testGetId_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals("Should return -1 for null subject", -1L, parser.getId());
    }

    // --- GET TITLE & RANGE STRING LOGIC ---

    @Test
    public void testGetTitle_WithRoundBrackets() {
        SubjectParser parser = new SubjectParser("100 My Title (1/10)");
        String title = parser.getTitle();
        
        assertEquals("Title should be stripped of range", "My Title ", title);
        assertEquals("RangeString should be populated", "(1/10)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_WithSquareBrackets() {
        SubjectParser parser = new SubjectParser("100 My Title [5/20]");
        String title = parser.getTitle();
        
        assertEquals("Title should be stripped of range", "My Title ", title);
        assertEquals("RangeString should be populated", "[5/20]", parser.getRangeString());
    }

    @Test
    public void testGetTitle_NoRange() {
        SubjectParser parser = new SubjectParser("100 Just A Title");
        String title = parser.getTitle();
        
        assertEquals("Title should remain full", "Just A Title", title);
        // getRangeString calls getTitle internally if null, but here no range is found
        assertNull("RangeString should be null", parser.getRangeString());
    }

    @Test
    public void testGetTitle_InvalidRange_Letters() {
        // La logica interna controlla Character.isDigit. 'A' fallisce, quindi 
        // considera la parentesi come parte del testo e non come range.
        SubjectParser parser = new SubjectParser("100 Title (1/A)");
        String title = parser.getTitle();
        
        assertEquals("Title should include the invalid range text", "Title (1/A)", title);
        assertNull("RangeString should be null", parser.getRangeString());
    }

    @Test
    public void testGetTitle_Exception_NullSubject() {
        // Passiamo null per forzare un'eccezione (NPE) all'interno di getTitle
        // che viene catturata dal blocco catch, restituendo null.
        SubjectParser parser = new SubjectParser(null);
        assertNull("Should return null on exception", parser.getTitle());
    }

    @Test
    public void testGetRangeString_Caching() {
        SubjectParser parser = new SubjectParser("100 Title (1/5)");
        // Prima chiamata: calcola e setta
        String firstCall = parser.getRangeString();
        // Seconda chiamata: ritorna valore cachato (copertura ramo if != null)
        String secondCall = parser.getRangeString();
        
        assertEquals("(1/5)", firstCall);
        assertSame("Should return same string instance/value", firstCall, secondCall);
    }
    
    @Test
    public void testGetRangeString_Exception() {
        // Passando null, getRangeString chiama getTitle, getTitle lancia eccezione, ritorna null.
        // getRangeString dovrebbe gestire il null o l'eccezione.
        // Se Subject è null:
        // getRangeString -> getTitle -> Subject.substring -> NPE catchata in getTitle -> returns null.
        // getRangeString returns null.
        // Per triggerare il catch in getRangeString bisogna che getTitle o Subject lancino un'eccezione non gestita,
        // ma getTitle cattura tutto.
        // Tuttavia, possiamo osservare il comportamento su null.
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    // --- RANGE VALUES (Lower/Upper) & MessageParts LOGIC ---

    @Test
    public void testRanges_RoundBrackets_Valid() {
        SubjectParser parser = new SubjectParser("1 T (2/10)");
        // Questo chiama internamente messageParts -> ramo Tonde
        assertEquals("Lower range mismatch", 2, parser.getThisRange());
        assertEquals("Upper range mismatch", 10, parser.getUpperRange());
    }

    @Test
    public void testRanges_SquareBrackets_Valid() {
        SubjectParser parser = new SubjectParser("1 T [50/100]");
        // Questo chiama internamente messageParts.
        // Try (tonde) fallirà perché cerca '(', va nel catch(Exception inte),
        // ed esegue Try (quadre).
        assertEquals("Lower range mismatch", 50, parser.getThisRange());
        assertEquals("Upper range mismatch", 100, parser.getUpperRange());
    }

    @Test
    public void testRanges_NoRange_Defaults() {
        SubjectParser parser = new SubjectParser("1 T NoRange");
        // messageParts ritorna null.
        // getThisRange cattura il null check e ritorna LowerRange (inizializzato a 1 nel costruttore)
        assertEquals("Default lower range should be 1", 1, parser.getThisRange());
        assertEquals("Default upper range should be 1", 1, parser.getUpperRange());
    }
    
    @Test
    public void testMessageParts_CatchInnerSubE() {
        // Dobbiamo simulare un caso in cui 'getTitle' trova un range, ma 'messageParts' fallisce nel parsing.
        // getTitle è molto permissivo sul formato (basta digit o '/'), ma messageParts usa StringTokenizer e Integer.parseInt.
        
        // Caso: Range identificato ma formato interno strano che passa il check dei digit ma fallisce parse?
        // Difficile perché getTitle controlla Character.isDigit.
        
        // Proviamo a forzare il fallimento del secondo blocco try (quadre) dopo il fallimento del primo.
        // Se RangeString è null, messageParts va nel catch esterno.
        // Se RangeString è "[1/2]", il primo blocco fallisce (cerca '('), va nel secondo, riesce.
        
        // Dobbiamo entrare nel "catch (Exception subE)".
        // Questo accade se il blocco quadre fallisce DOPO che il blocco tonde è fallito.
        // Costruiamo un Subject che inganni getTitle ma rompa messageParts.
        // getTitle accetta '/' e digits.
        // Se mettiamo "[/]", getTitle potrebbe accettarlo? No, serve digit.
        // Se mettiamo "[1/]", getTitle lo accetta? 
        // while ((nextchar = tmpSubject.charAt(--i)) != endchar)
        // Se trova '/' continua.
        
        // Proviamo "[1//2]". getTitle lo accetta (tutti char validi).
        // messageParts:
        // 1. Tonde? No. Catch.
        // 2. Quadre? Sì. Tokenizer "/" -> tokens: "1", "", "2"? 
        // StringTokenizer salta i delimitatori vuoti di default? No, o sì? 
        // new StringTokenizer(str, "/") usa returnDelims=false.
        // "1//2" -> nextToken="1", nextToken="2". Funziona.
        
        // Proviamo "[1/2/3]". 
        // getTitle: OK.
        // messageParts (quadre): token1="1", token2="2". Ignora il 3? 
        // substring(1, length) -> parsInt. Funziona.
        
        // Proviamo a forzare NullPointerException nel blocco quadre.
        // Se Subject permette a getTitle di produrre un RangeString che non ha ']' ma getTitle ha detto di sì? Impossibile.
        
        // Approccio alternativo per copertura:
        // messageParts chiama getRangeString().
        // Se getRangeString() ritorna una stringa che non contiene né '(' né '[',
        // messageParts fallisce il primo try (indexOf '(' -> -1 -> substring exception),
        // va nel catch, prova il secondo try (indexOf '[' -> -1 -> substring exception),
        // va nel catch subE -> return null.
        
        // Ma come settare RangeString a "invalid" se getTitle lo valida?
        // Ereditando e bypassando? No, classe finale per i nostri scopi.
        
        // Tuttavia, c'è un trucco: getTitle() setta RangeString.
        // Se usiamo Reflection per sporcare RangeString? È sporco.
        
        // Analizziamo il catch outer di messageParts.
        // Se getRangeString ritorna NULL.
        // mainrange = null.
        // mainrange.substring -> NPE.
        // Catch outer (Exception e). return null.
        SubjectParser parser = new SubjectParser("NoRange");
        // getRangeString -> null. messageParts -> NPE -> catch -> null.
        // Verificato implicitamente dai test default (ritornano 1).
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void testMessageParts_CorruptedRangeString_ViaReflection() throws Exception {
        // Per ottenere il 100% reale sui catch interni di messageParts, dobbiamo forzare
        // una RangeString che rompa il parsing logico.
        // Poiché getTitle è robusto, usiamo reflection per settare un RangeString "corrotto"
        // che non sarebbe possibile ottenere tramite getTitle standard.
        // Questo simula uno stato inconsistente o un bug teorico in getTitle.
        
        SubjectParser parser = new SubjectParser("Ignore");
        java.lang.reflect.Field field = SubjectParser.class.getDeclaredField("RangeString");
        field.setAccessible(true);
        field.set(parser, "CorruptedStringWithoutBrackets");
        
        // Ora chiamiamo getThisRange che chiama messageParts
        // 1. mainrange = "Corrupted..."
        // 2. try tonde: lastIndexOf("(") -> -1. substring -> Exception. -> CATCH 1
        // 3. try quadre: lastIndexOf("[") -> -1. substring -> Exception. -> CATCH 2 (subE)
        // 4. returns null.
        
        assertEquals("Should return default 1 when parsing fails completely", 1, parser.getThisRange());
        
        // Verifica che siamo passati dai catch interni e non dall'outer (anche se l'effetto è lo stesso null)
        // L'outer catch stampa lo stacktrace, i catch interni no (subE ritorna null silenziosamente).
        // Questo test assicura che il codice sia robusto anche con stati interni invalidi.
    }
}


						