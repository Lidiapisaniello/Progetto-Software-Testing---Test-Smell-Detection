import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    // --- Test per il costruttore SubjectParser(String s) ---

    @Test
    public void SubjectParserValidSubjectTest() {
        String subject = "1234 This is a test subject";
        SubjectParser parser = new SubjectParser(subject);
        // Non possiamo asserire direttamente sulle variabili private Subject, UpperRange, LowerRange
        // Ma possiamo asserire che l'oggetto è stato creato con successo
        assertNotNull(parser);
        // Per copertura, testiamo un metodo pubblico che usa i valori iniziali (o li imposta)
        // getThisRange e getUpperRange senza range nel subject dovrebbero restituire 1 (valore di default)
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    // --- Test per getId() ---

    @Test
    public void getIdValidLongTest() {
        String subject = "987654321 Test with a long id";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(987654321L, parser.getId());
    }

    @Test
    public void getIdNegativeLongTest() {
        String subject = "-12345 Test with a negative id";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(-12345L, parser.getId());
    }

    @Test
    public void getIdInvalidFormatTest() {
        String subject = "NoIdHere Test with invalid format";
        SubjectParser parser = new SubjectParser(subject);
        // L'eccezione NumberFormatException viene catturata
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getIdNullSubjectTest() {
        // Test per assicurare che Subject = null nel costruttore generi un'eccezione
        SubjectParser parser = new SubjectParser(null);
        // StringTokenizer(null) genera NullPointerException, catturata nel try/catch di getId()
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void getIdEmptySubjectTest() {
        String subject = "";
        SubjectParser parser = new SubjectParser(subject);
        // StringTokenizer("") non lancia eccezioni ma nextToken() lancia NoSuchElementException, catturata
        assertEquals(-1L, parser.getId());
    }

    // --- Test per getTitle() ---

    @Test
    public void getTitleNoRangeTest() {
        String subject = "1000 Subject without range";
        SubjectParser parser = new SubjectParser(subject);
        // tmpSubject = "Subject without range"
        assertEquals("Subject without range", parser.getTitle());
    }

    @Test
    public void getTitleRangeInParenthesisTest() {
        String subject = "1001 Subject with (1/5) range";
        SubjectParser parser = new SubjectParser(subject);
        // " (1/5)" viene riconosciuto come range e rimosso.
        assertEquals("Subject with range", parser.getTitle());
        assertEquals("(1/5)", parser.getRangeString()); // Verifica che RangeString sia impostata
    }

    @Test
    public void getTitleRangeInBracketsTest() {
        String subject = "1002 Subject with [3/7] range";
        SubjectParser parser = new SubjectParser(subject);
        // " [3/7]" viene riconosciuto come range e rimosso.
        assertEquals("Subject with range", parser.getTitle());
        assertEquals("[3/7]", parser.getRangeString()); // Verifica che RangeString sia impostata
    }

    @Test
    public void getTitleRangeInvalidContentParenthesisTest() {
        // Contenuto tra () non valido (non solo numeri e /), il range viene trattato come parte del titolo
        String subject = "1003 Subject with (a/5) invalid range";
        SubjectParser parser = new SubjectParser(subject);
        // La logica si interrompe non appena trova 'a' in `(a/5)` e aggiunge tutto il resto al titolo.
        assertEquals("Subject with (a/5) invalid range", parser.getTitle());
        assertNull(parser.getRangeString()); // RangeString NON deve essere impostato in questo caso
    }

    @Test
    public void getTitleRangeInvalidContentBracketsTest() {
        // Contenuto tra [] non valido, il range viene trattato come parte del titolo
        String subject = "1004 Subject with [3/b] invalid range";
        SubjectParser parser = new SubjectParser(subject);
        // La logica si interrompe non appena trova 'b' in `[3/b]` e aggiunge tutto il resto al titolo.
        assertEquals("Subject with [3/b] invalid range", parser.getTitle());
        assertNull(parser.getRangeString()); // RangeString NON deve essere impostato
    }

    @Test
    public void getTitleMultipleParenthesisRangeTest() {
        // Assicuriamo che venga preso solo l'ultimo blocco valido di range
        String subject = "1005 Subject (1/2) with (3/4) ranges";
        SubjectParser parser = new SubjectParser(subject);
        // L'ultimo "(3/4)" è riconosciuto e rimosso
        assertEquals("Subject (1/2) with ranges", parser.getTitle());
        assertEquals("(3/4)", parser.getRangeString());
    }

    @Test
    public void getTitleMultipleBracketsRangeTest() {
        // Assicuriamo che venga preso solo l'ultimo blocco valido di range
        String subject = "1006 Subject [1/2] with [3/4] ranges";
        SubjectParser parser = new SubjectParser(subject);
        // L'ultimo "[3/4]" è riconosciuto e rimosso
        assertEquals("Subject [1/2] with ranges", parser.getTitle());
        assertEquals("[3/4]", parser.getRangeString());
    }

    @Test
    public void getTitleRangeWithNoSlashParenthesisTest() {
        // Assicuriamo che la verifica `indexOf("/") != -1` sia raggiunta e che l'oggetto non venga considerato Range
        String subject = "1007 Subject with (5) no slash range";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals("Subject with (5) no slash range", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleRangeWithNoSlashBracketsTest() {
        // Assicuriamo che la verifica `indexOf("/") != -1` sia raggiunta e che l'oggetto non venga considerato Range
        String subject = "1008 Subject with [5] no slash range";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals("Subject with [5] no slash range", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleEmptySubjectTest() {
        String subject = "12345"; // Subject senza spazio dopo l'ID
        SubjectParser parser = new SubjectParser(subject);
        // Subject.indexOf(" ") + 1 è 5. Subject.substring(5, 5) -> ""
        assertEquals("", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleSubjectWithOnlyIdTest() {
        String subject = "12345 "; // Subject con solo ID e spazio
        SubjectParser parser = new SubjectParser(subject);
        // tmpSubject = ""
        assertEquals("", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void getTitleNullSubjectTest() {
        // Subject = null nel costruttore, Subject.indexOf(" ") lancerà NullPointerException.
        // Poiché è un metodo pubblico, Subject.substring(Subject.indexOf(" ") + 1, Subject.length());
        // Lancia StringIndexOutOfBoundsException (se Subject == null, poi .length() ecc.)
        // Tuttavia, nel codice provided, Subject.indexOf(" ") + 1 su null lancia NullPointerException
        // che viene catturata e viene restituito null.
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    // --- Test per getRangeString() ---

    @Test
    public void getRangeStringNoRangeTest() {
        String subject = "1000 No range here";
        SubjectParser parser = new SubjectParser(subject);
        // getTitle() viene chiamato, RangeString rimane null.
        parser.getTitle(); // Chiamiamo getTitle per inizializzare RangeString
        assertNull(parser.getRangeString());
    }

    @Test
    public void getRangeStringValidParenthesisTest() {
        String expectedRange = "(123/456)";
        String subject = "1000 Range is " + expectedRange;
        SubjectParser parser = new SubjectParser(subject);
        // getTitle() imposta RangeString
        parser.getTitle();
        assertEquals(expectedRange, parser.getRangeString());
    }

    @Test
    public void getRangeStringValidBracketsTest() {
        String expectedRange = "[123/456]";
        String subject = "1000 Range is " + expectedRange;
        SubjectParser parser = new SubjectParser(subject);
        // getTitle() imposta RangeString
        parser.getTitle();
        assertEquals(expectedRange, parser.getRangeString());
    }

    @Test
    public void getRangeStringAlreadySetTest() {
        String expectedRange = "(99/100)";
        String subject = "1000 Range is " + expectedRange;
        SubjectParser parser = new SubjectParser(subject);
        // Imposta RangeString la prima volta
        parser.getTitle();
        // Chiama di nuovo getRangeString (non dovrebbe chiamare getTitle di nuovo)
        assertEquals(expectedRange, parser.getRangeString());
    }

    // --- Test per getThisRange() e getUpperRange() (che utilizzano messageParts()) ---

    // Scenario 1: Range in parentesi (range valido)
    @Test
    public void getThisRangeValidParenthesisTest() {
        String subject = "1009 Subject with range (4/10)";
        SubjectParser parser = new SubjectParser(subject);
        // getThisRange chiama getRangeString che a sua volta chiama getTitle e imposta RangeString
        // messageParts cerca "(", "/", ")"
        assertEquals(4, parser.getThisRange());
    }

    @Test
    public void getUpperRangeValidParenthesisTest() {
        String subject = "1009 Subject with range (4/10)";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(10, parser.getUpperRange());
    }

    // Scenario 2: Range in parentesi (numeri negativi/zero non possibili in base a parseInt ma testiamo)
    @Test
    public void getThisRangeNegativeLowerParenthesisTest() {
        String subject = "1010 Subject with range (-4/10)";
        SubjectParser parser = new SubjectParser(subject);
        // messageParts prende il range: "-4/10"
        // sLow = "-4", Integer.parseInt(sLow.substring(1, sLow.length())) -> parseInt("4")
        assertEquals(4, parser.getThisRange());
    }

    @Test
    public void getUpperRangeNegativeLowerParenthesisTest() {
        String subject = "1010 Subject with range (-4/10)";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(10, parser.getUpperRange());
    }

    // Scenario 3: Range in parentesi (upper range negativo)
    @Test
    public void getThisRangeNegativeUpperParenthesisTest() {
        String subject = "1011 Subject with range (4/-10)";
        SubjectParser parser = new SubjectParser(subject);
        // sLow="4", sHigh="-10". low=4, high=-10.
        assertEquals(4, parser.getThisRange());
    }

    @Test
    public void getUpperRangeNegativeUpperParenthesisTest() {
        String subject = "1011 Subject with range (4/-10)";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(-10, parser.getUpperRange());
    }


    // Scenario 4: Range in parentesi (formato errato - manca il lower)
    @Test
    public void getThisRangeInvalidRangeFormatParenthesisTest() {
        String subject = "1012 Subject with range (/10)";
        SubjectParser parser = new SubjectParser(subject);
        // StringTokenizer.nextToken() chiamato 2 volte su "/10" -> 1) "" (vuoto) 2) "10"
        // Prima parseInt fallisce su ""
        // Si va nel catch (Exception inte) -> Si prova il blocco []
        assertEquals(1, parser.getThisRange()); // Restituisce il valore di default 1
    }

    // Scenario 5: Range in quadre (range valido)
    @Test
    public void getThisRangeValidBracketsTest() {
        String subject = "1013 Subject with range [5/15]";
        SubjectParser parser = new SubjectParser(subject);
        // messageParts cerca "[", "/", "]"
        assertEquals(5, parser.getThisRange());
    }

    @Test
    public void getUpperRangeValidBracketsTest() {
        String subject = "1013 Subject with range [5/15]";
        SubjectParser parser = new SubjectParser(subject);
        assertEquals(15, parser.getUpperRange());
    }

    // Scenario 6: Range non riconosciuto (va al catch più esterno di messageParts)
    @Test
    public void getThisRangeNoRangeRecognizedTest() {
        String subject = "1014 Subject with no range block";
        SubjectParser parser = new SubjectParser(subject);
        // getRangeString() restituisce null
        // messageParts() lancia NullPointerException su mainrange.substring()
        // Cattura l'eccezione, e ritorna il valore di default 1 (LowerRange inizializzato a 1)
        assertEquals(1, parser.getThisRange());
    }

    // Scenario 7: Range in parentesi (manca upper)
    @Test
    public void getThisRangeMissingUpperParenthesisTest() {
        String subject = "1015 Subject with range (4/)";
        SubjectParser parser = new SubjectParser(subject);
        // st.nextToken() chiamato 2 volte. La seconda fallisce (NoSuchElementException)
        // Cattura l'eccezione, e ritorna il valore di default 1
        assertEquals(1, parser.getThisRange());
    }

    // Scenario 8: Range in quadre (formato errato - no slash)
    @Test
    public void getThisRangeInvalidRangeNoSlashBracketsTest() {
        String subject = "1016 Subject with range [5-15]";
        SubjectParser parser = new SubjectParser(subject);
        // getTitle() non imposta RangeString perché manca "/"
        // getRangeString() -> null
        // messageParts() -> NullPointerException, ritorna null.
        assertEquals(1, parser.getThisRange());
    }

    // Scenario 9: Range in quadre (formato errato - non numerico)
    @Test
    public void getThisRangeInvalidRangeNotNumberBracketsTest() {
        String subject = "1017 Subject with range [a/b]";
        SubjectParser parser = new SubjectParser(subject);
        // getTitle() imposta RangeString="[a/b]"
        // messageParts() prova a fare Integer.parseInt("a") o "b" -> NumberFormatException (catturata)
        // messageParts() ritorna null
        assertEquals(1, parser.getThisRange());
    }
}