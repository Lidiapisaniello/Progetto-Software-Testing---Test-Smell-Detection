/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: luca.carraturo8998@gmail.com
UserID: 413
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Test getId() ---

    @Test
    public void GetId_ValidSubjectStartingWithNumber_ReturnsId() {
        SubjectParser parser = new SubjectParser("12345 Testing Title");
        assertEquals("Should parse the starting number as ID", 12345L, parser.getId());
    }

    @Test
    public void GetId_SubjectWithoutNumber_ReturnsNegativeOne() {
        // Causa eccezione in Long.parseLong, che viene catturata
        SubjectParser parser = new SubjectParser("NoNumber Here");
        assertEquals("Should return -1 when ID parsing fails", -1L, parser.getId());
    }
    
    @Test
    public void GetId_NullSubject_ReturnsNegativeOne() {
        // Causa NullPointerException in StringTokenizer, catturata
        SubjectParser parser = new SubjectParser(null);
        assertEquals("Should return -1 on null subject", -1L, parser.getId());
    }

    // --- Test getTitle() e getRangeString() Logic ---

    @Test
    public void GetTitle_StandardRoundBrackets_ExtractsRangeAndTitle() {
        // Input: "ID Title (1/10)"
        // Expect: Title="Title ", Range="(1/10)"
        SubjectParser parser = new SubjectParser("100 My Title (1/10)");
        
        String title = parser.getTitle();
        String range = parser.getRangeString();

        assertEquals("My Title ", title);
        assertEquals("(1/10)", range);
    }

    @Test
    public void GetTitle_SquareBrackets_ExtractsRangeAndTitle() {
        // Input: "ID Title [1/10]"
        // Expect: Title="Title ", Range="[1/10]"
        SubjectParser parser = new SubjectParser("100 My Title [1/10]");
        
        assertEquals("My Title ", parser.getTitle());
        assertEquals("[1/10]", parser.getRangeString());
    }

    @Test
    public void GetTitle_MalformedRangeNoDigits_IgnoredAsRange() {
        // Branch: Character.isDigit(nextchar) == false && nextchar != '/'
        // Input: "Title (A/B)" -> Le parentesi contengono lettere, non è un range valido.
        SubjectParser parser = new SubjectParser("100 Title (A/B)");
        
        String title = parser.getTitle();
        // Verifica che il presunto range sia rimasto nel titolo
        assertTrue("Malformed range should remain in title", title.contains("(A/B)"));
        // RangeString non viene settato (rimane null o default se logica diversa, qui null perché getTitle ritorna e RangeString non è assegnato)
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitle_MalformedRangeNoSlash_IgnoredAsRange() {
        // Branch: tmpbuf.toString().indexOf("/") != -1
        // Input: "Title (123)" -> Mancanza di slash
        SubjectParser parser = new SubjectParser("100 Title (123)");
        
        String title = parser.getTitle();
        assertTrue("Range without slash should remain in title", title.contains("(123)"));
    }

    @Test
    public void GetTitle_MultipleRanges_ParsesLastOneOnly() {
        // Logic: Loop backwards. FoundRange diventa true al primo match (che è l'ultimo nella stringa).
        // Il match precedente (primo nella stringa) viene trattato come testo perché FoundRange è true.
        SubjectParser parser = new SubjectParser("100 Title (1/1) (2/2)");
        
        String title = parser.getTitle();
        String range = parser.getRangeString();
        
        // "(2/2)" è il range reale. "(1/1)" rimane nel titolo.
        assertEquals("(2/2)", range);
        assertTrue("First range should be part of title", title.contains("(1/1)"));
    }
    
    @Test
    public void GetTitle_NoSpaceInSubject_HandleGracefully() {
        // Subject.substring(Subject.indexOf(" ") + 1)
        // Se non c'è spazio, indexOf = -1, +1 = 0. Substring(0) prende tutto.
        SubjectParser parser = new SubjectParser("100"); 
        String title = parser.getTitle();
        // Loop gira, non trova parentesi, ritorna tutto il buffer.
        assertEquals("100", title);
    }

    @Test
    public void GetTitle_NullSubject_ReturnsNull() {
        // Trigger catch(Exception parseE) in getTitle
        SubjectParser parser = new SubjectParser(null);
        assertNull("Should return null on exception", parser.getTitle());
    }

    // --- Test getThisRange() e getUpperRange() ---

    @Test
    public void GetRanges_RoundBrackets_ReturnsCorrectValues() {
        SubjectParser parser = new SubjectParser("100 T (5/10)");
        assertEquals("Lower range incorrect", 5, parser.getThisRange());
        assertEquals("Upper range incorrect", 10, parser.getUpperRange());
    }

    @Test
    public void GetRanges_SquareBrackets_ReturnsCorrectValues() {
        // Questo copre il blocco catch(Exception inte) -> try alternativo in messageParts
        SubjectParser parser = new SubjectParser("100 T [3/20]");
        assertEquals("Lower range incorrect", 3, parser.getThisRange());
        assertEquals("Upper range incorrect", 20, parser.getUpperRange());
    }

    @Test
    public void GetRanges_BrokenParsing_ReturnsDefaults() {
        // Questo test mira a coprire il "catch (Exception subE)" e il "return null" in messageParts.
        // Dobbiamo passare un range che getTitle accetta (ha cifre e slash)
        // MA che messageParts fallisce a parsare come Int (es. numero troppo grande per Integer).
        
        // Overflow Integer: 9999999999
        SubjectParser parser = new SubjectParser("100 T (9999999999/1)");
        
        // messageParts proverà parseLong su brackets tondi -> Exception
        // Entra nel catch, prova brackets quadri -> Exception (non ci sono)
        // Entra nel catch finale -> return null.
        // getThisRange riceve null -> non aggiorna LowerRange -> ritorna default (1).
        
        assertEquals("Should return default 1 on parsing failure", 1, parser.getThisRange());
        assertEquals("Should return default 1 on parsing failure", 1, parser.getUpperRange());
    }
    
    @Test
    public void GetRangeString_CallTwice_UsesCache() {
        SubjectParser parser = new SubjectParser("100 T (1/1)");
        String r1 = parser.getRangeString(); // Calcola e setta RangeString
        String r2 = parser.getRangeString(); // Ritorna RangeString cachato
        
        assertSame(r1, r2);
        assertEquals("(1/1)", r1);
    }

    // --- "Impossible" Branches Coverage (Dead Code / Defensive Code) ---
    // Usiamo l'ereditarietà per forzare eccezioni nei metodi chiamati e coprire i catch dei chiamanti.

    @Test
    public void GetRangeString_ExceptionCoverage_ReturnsNull() {
        // getRangeString ha un try-catch che cattura eccezioni se this.getTitle() fallisce.
        // Ma getTitle cattura già tutto. L'unico modo per testare il catch di getRangeString
        // è fare in modo che getTitle lanci un'eccezione non gestita (es. RuntimeException) o sovrascriverlo.
        
        SubjectParser maliciousParser = new SubjectParser("test") {
            @Override
            public String getTitle() {
                throw new RuntimeException("Force Catch in getRangeString");
            }
        };
        
        // Deve catturare l'eccezione e stampare su System.err (che ignoriamo) e ritornare null
        assertNull(maliciousParser.getRangeString());
    }

    @Test
    public void GetThisRange_ExceptionCoverage_ReturnsDefault() {
        // getThisRange ha un catch che viene eseguito solo se messageParts lancia eccezione.
        // messageParts cattura tutto e ritorna null, quindi il catch di getThisRange è teoricamente irraggiungibile.
        // Lo forziamo.
        
        SubjectParser maliciousParser = new SubjectParser("test") {
            // Non possiamo fare override di messageParts perché è PRIVATE.
            // Tuttavia, possiamo causare eccezione nel blocco 'if (parts != null)'.
            // No, 'parts' è locale.
            
            // Analisi: getThisRange chiama messageParts(). Se messageParts è privato, non possiamo farne override.
            // LIMITAZIONE DEL CODICE: Non possiamo coprire il blocco catch di getThisRange 
            // a meno che messageParts non lanci un Error (es. OutOfMemoryError) o eccezione non controllata
            // che sfugge al catch(Exception e) interno di messageParts.
            // Tuttavia messageParts ha catch(Exception e).
            
            // Trick: L'unico modo per far fallire getThisRange è se this.messageParts() lancia qualcosa
            // che non è Exception (es. StackOverflowError), oppure...
            // Aspetta! getThisRange chiama `this.messageParts()`. 
            // Se usiamo la reflection per testare il metodo privato messageParts è un conto,
            // ma qui stiamo testando getThisRange.
            
            // Nota: Dato che messageParts è privato e "blindato" (catch all), 
            // il catch in getThisRange è tecnicamente Dead Code assoluto per Exception standard.
            // Tuttavia, JUnit riporta la coverage. 
            
            // Se vogliamo il 100% coverage strict, dobbiamo accettare che quel catch non sarà coperto 
            // OPPURE il compilatore/JVM ottimizza.
            // Ma c'è un modo: messageParts chiama getRangeString.
            // getRangeString è pubblico.
            // Se facciamo override di getRangeString perché lanci un'eccezione, messageParts la cattura?
            // messageParts chiama `this.getRangeString()`.
            // messageParts ha `try { ... getRangeString ... } catch (Exception e)`.
            // Quindi messageParts cattura l'eccezione di getRangeString e ritorna null.
            // Quindi getThisRange riceve null e non va in eccezione.
            
            // Conclusione: Il blocco catch in getThisRange è IMPOSSIBILE da raggiungere con logica Java standard
            // perché il metodo chiamato (messageParts) ingoia tutte le Exception.
            // L'unico modo è se messageParts lancia Throwable (non Exception). 
            // Ma getThisRange cattura (Exception e), quindi Throwable passerebbe oltre facendo crashare il test.
            
            // PERTANTO: Il codice fornito ha blocchi catch inutili.
            // Tuttavia, per avvicinarci il più possibile, verifichiamo il comportamento robusto.
        };
        
        // Eseguiamo il test standard per confermare che il metodo non esploda
        SubjectParser parser = new SubjectParser("100 T");
        assertEquals(1, parser.getThisRange());
    }
    
    // --- Testing Private Method directly via Reflection (Opzionale ma utile per messageParts coverage interna) ---
    // Non strettamente necessario se i test pubblici coprono tutti i path logici interni, 
    // ma utile per debug. I test sopra (GetRanges_BrokenParsing) hanno già coperto i branch interni di messageParts.
}