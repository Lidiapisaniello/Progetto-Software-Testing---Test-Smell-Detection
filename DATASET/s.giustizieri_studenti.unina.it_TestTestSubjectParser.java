/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: s.giustizieri@studenti.unina.it
UserID: 273
Date: 24/11/2025
*/

import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;

public class TestSubjectParser {

    // ---------------------------------------------------------
    // Test getId()
    // ---------------------------------------------------------

    @Test
    public void testGetId_Valid() {
        SubjectParser parser = new SubjectParser("12345 Oggetto del messaggio");
        Assert.assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetId_InvalidNumeric() {
        SubjectParser parser = new SubjectParser("ABC Oggetto del messaggio");
        Assert.assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_NullSubject() {
        SubjectParser parser = new SubjectParser(null);
        Assert.assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_EmptySubject() {
        SubjectParser parser = new SubjectParser("");
        Assert.assertEquals(-1L, parser.getId());
    }

    // ---------------------------------------------------------
    // Test getTitle() e Parsing Logico
    // ---------------------------------------------------------

    @Test
    public void testGetTitle_SimpleNoRange() {
        SubjectParser parser = new SubjectParser("100 Semplice Titolo");
        // La logica rimuove il primo token ("100") e lo spazio successivo.
        Assert.assertEquals("Semplice Titolo", parser.getTitle());
        Assert.assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitle_WithRoundBracketsRange() {
        SubjectParser parser = new SubjectParser("100 Titolo (1/10)");
        // Nota: getTitle() rimuove il range dalla stringa restituita
        Assert.assertEquals("Titolo ", parser.getTitle()); 
        Assert.assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_WithSquareBracketsRange() {
        SubjectParser parser = new SubjectParser("100 Titolo [5/20]");
        Assert.assertEquals("Titolo ", parser.getTitle());
        Assert.assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void testGetTitle_RangeInMiddle() {
        // Il parser lavora all'indietro. Se trova un range valido, lo estrae.
        // "Finale" sarà considerato parte del titolo perché è dopo il range (guardando da sinistra)
        // o prima del range (guardando da destra).
        SubjectParser parser = new SubjectParser("100 Inizio (1/5) Fine");
        Assert.assertEquals("Inizio  Fine", parser.getTitle());
        Assert.assertEquals("(1/5)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_MultipleRanges() {
        // Il parser scansiona da destra a sinistra. Trova (2/2), lo imposta come range.
        // Poi continua, trova (1/1). Poiché FoundRange è true, (1/1) viene trattato come testo del titolo.
        SubjectParser parser = new SubjectParser("100 Test (1/1) (2/2)");
        Assert.assertEquals("Test (1/1) ", parser.getTitle());
        Assert.assertEquals("(2/2)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_FakeRanges() {
        // Caso 1: Lettere nel range
        // Il codice rileva 'b' (non digit), quindi interrompe il parsing del range 
        // e rimette tutto nel titolo.
        SubjectParser parser1 = new SubjectParser("100 Test (a/b)");
        Assert.assertEquals("Test (a/b)", parser1.getTitle());
        Assert.assertNull(parser1.getRangeString());

        // Caso 2: Niente slash (ma solo numeri)
        // ATTENZIONE: Il codice originale ha un difetto qui. 
        // Se trova parentesi con solo numeri ma senza slash, le Rimuove dal titolo
        // ma NON le imposta come RangeString.
        SubjectParser parser2 = new SubjectParser("100 Test (123)");
        
        // Correzione: ci aspettiamo che "(123)" venga rimosso lasciando "Test "
        Assert.assertEquals("Test ", parser2.getTitle()); 
        Assert.assertNull(parser2.getRangeString());
        
        // Caso 3: Parentesi non bilanciate o mischiate
        SubjectParser parser3 = new SubjectParser("100 Test (1/5]");
        Assert.assertEquals("Test (1/5]", parser3.getTitle());
        Assert.assertNull(parser3.getRangeString());
    }

    @Test
    public void testGetTitle_NoSpaceInSubject() {
        // Se non c'è spazio, substring(indexOf(" ")+1) potrebbe comportarsi in modo strano
        // indexOf = -1, +1 = 0. Prende tutta la stringa.
        SubjectParser parser = new SubjectParser("SoloTesto");
        Assert.assertEquals("SoloTesto", parser.getTitle());
    }
    
    @Test
    public void testGetTitle_ExceptionHandling() {
        // Forza null per entrare nel catch di getTitle
        SubjectParser parser = new SubjectParser(null);
        Assert.assertNull(parser.getTitle());
    }

    // ---------------------------------------------------------
    // Test getUpperRange() e getThisRange() (e messageParts)
    // ---------------------------------------------------------

    @Test
    public void testRanges_DefaultValues() {
        SubjectParser parser = new SubjectParser("100 Test");
        Assert.assertEquals(1, parser.getThisRange());
        Assert.assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_RoundBrackets_Parsing() {
        SubjectParser parser = new SubjectParser("100 T (5/10)");
        // Forza il parsing chiamando i getter
        Assert.assertEquals(5, parser.getThisRange());
        Assert.assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void testRanges_SquareBrackets_Parsing() {
        // Questo testa il primo blocco catch in messageParts()
        // Poiché il parsing delle tonde fallirà, entrerà nel blocco per le quadre
        SubjectParser parser = new SubjectParser("100 T [20/30]");
        Assert.assertEquals(20, parser.getThisRange());
        Assert.assertEquals(30, parser.getUpperRange());
    }

    // ---------------------------------------------------------
    // Test Avanzati / Mutation / Copertura Eccezioni
    // ---------------------------------------------------------

    @Test
    public void testMessageParts_NullReturn_ViaReflection() throws Exception {
        // OBIETTIVO: Coprire il "catch (Exception subE)" interno e il return null in messageParts.
        // Problema: getTitle() è troppo bravo a validare, quindi RangeString è sempre ben formattato o null.
        // Soluzione: Usiamo Reflection per iniettare un RangeString "sporco" che inganna messageParts.
        
        SubjectParser parser = new SubjectParser("100 Dummy");
        
        // Impostiamo RangeString a qualcosa che non ha né ( né [ validi per l'algoritmo di messageParts
        // ma non è null.
        setPrivateField(parser, "RangeString", "INVALID_STRING");

        // messageParts verrà chiamato. 
        // 1. lastIndexOf("(") -> -1. substring fallisce. Catch 1.
        // 2. lastIndexOf("[") -> -1. substring fallisce. Catch 2 (subE).
        // 3. Returns null.
        // 4. getThisRange riceve null, non aggiorna LowerRange, ritorna il default (1).
        
        Assert.assertEquals(1, parser.getThisRange());
        Assert.assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testMessageParts_PartialFailure_ViaReflection() throws Exception {
        // OBIETTIVO: Coprire il caso in cui messageParts lancia eccezione globale e ritorna null
        // nel catch più esterno.
        
        SubjectParser parser = new SubjectParser("100 Dummy");
        
        // Impostiamo RangeString a null esplicitamente (anche se è default) per sicurezza
        // Se RangeString è null, messageParts fa:
        // mainrange = getRangeString() -> null (se getTitle fallisce o non trova nulla)
        // mainrange.lastIndexOf -> NullPointerException
        // Catch esterno -> printStackTrace -> return null
        
        // Per forzare getRangeString a ritornare null senza ricalcolare getTitle (che potrebbe trovare roba),
        // iniettiamo null e assicuriamoci che getTitle fallisca o non venga chiamato?
        // getRangeString chiama getTitle se è null.
        // Quindi dobbiamo far fallire getRangeString.
        
        // Alternativa: Iniettiamo un oggetto in RangeString che causa errore? No, è una String.
        
        // Proviamo a iniettare una stringa che passa i substring ma fallisce il parsing INT
        // Questo colpisce l'interno dei try ma lancia NumberFormatException
        
        setPrivateField(parser, "RangeString", "(A/B)"); 
        // 1. Trova "(". Estrae "A/B".
        // 2. Tokenizza. sLow="A", sHigh="B".
        // 3. Integer.parseInt("A") -> NumberFormatException.
        // 4. Va al primo catch.
        // 5. Cerca "[". Non lo trova (o lo trova se mettiamo (A/B)[C/D]).
        // 6. Se non trova [, catch interno, return null.
        
        Assert.assertEquals(1, parser.getThisRange());
    }
    
    @Test
    public void testGetRangeString_Exception() throws Exception {
        // È molto difficile far fallire getRangeString poiché cattura Exception
        // e l'unica logica è "if null call getTitle".
        // Tuttavia, per completezza, verifichiamo che se RangeString è già settato, lo ritorni.
        SubjectParser parser = new SubjectParser("100 T");
        setPrivateField(parser, "RangeString", "Forced");
        Assert.assertEquals("Forced", parser.getRangeString());
    }

    // Metodo helper per Reflection
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}