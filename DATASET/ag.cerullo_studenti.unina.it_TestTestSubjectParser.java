/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Agostino"
Cognome: "Cerullo"
Username: ag.cerullo@studenti.unina.it
UserID: 507
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Test per il metodo getId() ---

    @Test
    public void GetIdValidNumberTest() {
        SubjectParser parser = new SubjectParser("12345 Oggetto del messaggio");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void GetIdInvalidNumberTest() {
        // Caso in cui il primo token non è un numero
        SubjectParser parser = new SubjectParser("NonUnNumero Oggetto");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void GetIdNullSubjectTest() {
        // Caso in cui il Subject è null
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    // --- Test per il metodo getTitle() e getRangeString() ---

    @Test
    public void GetTitleSimpleWithoutRangeTest() {
        SubjectParser parser = new SubjectParser("100 Titolo Semplice");
        // getTitle rimuove l'ID e restituisce il resto
        assertEquals("Titolo Semplice", parser.getTitle());
        // RangeString deve essere null se non ci sono parentesi
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitleWithRoundBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo (1/10)");
        // getTitle dovrebbe rimuovere la parte del range
        assertEquals("Titolo ", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void GetTitleWithSquareBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo [5/20]");
        assertEquals("Titolo ", parser.getTitle());
        assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void GetTitleWithNestedLikeTextTest() {
        // Test per verificare la logica di parsing all'indietro (backward parsing)
        // "Titolo (abc)" non è un range valido perché contiene lettere
        SubjectParser parser = new SubjectParser("100 Titolo (abc)");
        assertEquals("Titolo (abc)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void GetTitleWithBrokenBracketsTest() {
        // Parentesi chiusa ma non aperta correttamente o contenuto non valido
        SubjectParser parser = new SubjectParser("100 Titolo 1/10)");
        assertEquals("Titolo 1/10)", parser.getTitle());
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void GetTitleExceptionHandlingTest() {
        // Una stringa vuota o senza spazi potrebbe causare problemi nel substring iniziale
        // Subject.substring(Subject.indexOf(" ") + 1, ...)
        SubjectParser parser = new SubjectParser("NoSpaceString");
        // indexOf(" ") è -1, +1 = 0. substring(0) restituisce l'intera stringa.
        // Il loop funziona e restituisce la stringa.
        assertNotNull(parser.getTitle());
        
        // Caso null per forzare l'eccezione e il catch in getTitle
        SubjectParser parserNull = new SubjectParser(null);
        assertNull(parserNull.getTitle());
    }

    // --- Test per getThisRange() e getUpperRange() e messageParts() ---

    @Test
    public void GetThisRangeDefaultTest() {
        // Nessun range specificato, dovrebbe restituire il default (1)
        SubjectParser parser = new SubjectParser("100 Titolo");
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void GetUpperRangeDefaultTest() {
        // Nessun range specificato, dovrebbe restituire il default (1)
        SubjectParser parser = new SubjectParser("100 Titolo");
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeRoundBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo (2/5)");
        // messageParts parsifica "2" come LowerRange
        assertEquals(2, parser.getThisRange());
    }

    @Test
    public void GetUpperRangeRoundBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo (2/5)");
        // messageParts parsifica "5" come UpperRange
        assertEquals(5, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeSquareBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo [3/8]");
        // messageParts entra nel blocco catch(Exception inte) e prova le quadre
        assertEquals(3, parser.getThisRange());
    }

    @Test
    public void GetUpperRangeSquareBracketsTest() {
        SubjectParser parser = new SubjectParser("100 Titolo [3/8]");
        assertEquals(8, parser.getUpperRange());
    }

    @Test
    public void GetThisRangeWithNumberFormatOverflowTest() {
        // Questo test è CRUCIALE per la copertura del 100%.
        // Deve innescare il catch(Exception inte) a causa del numero troppo grande,
        // e poi innescare il catch(Exception subE) perché non trova parentesi quadre.
        // Long.MAX_VALUE in una stringa farà fallire Integer.parseInt
        SubjectParser parser = new SubjectParser("100 Titolo (9999999999/9999999999)");
        
        // getTitle riconoscerà il range (sono cifre), ma parseInt fallirà.
        parser.getTitle(); // Forza il parsing
        
        // Il parsing fallisce, restituisce null, quindi i valori restano i default (1)
        assertEquals(1, parser.getThisRange());
    }

    @Test
    public void GetRangeStringExceptionWrapperTest() {
        // Copre il catch in getRangeString quando getTitle fallisce o Subject è null
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void MessagePartsOuterExceptionTest() {
        // Copre il catch(Exception e) più esterno in messageParts
        // Se Subject è null, getRangeString restituisce null.
        // messageParts chiama getRangeString(), ottiene null, e null.lastIndexOf lancia NPE.
        SubjectParser parser = new SubjectParser(null);
        
        // getThisRange chiama messageParts -> eccezione -> catch -> return 1 (default)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
}

						