import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;

public class TestSubjectParser {

    private SubjectParser parser;

    /**
     * Set up: Inizializza l'oggetto SubjectParser prima di ogni test.
     * Non è strettamente necessario per la classe data, ma è incluso
     * per dimostrare l'uso di @Before se necessario.
     */
    @Before
    public void setUp() {
        // Nessuna inizializzazione comune richiesta per tutti i test.
        // L'oggetto 'parser' verrà inizializzato all'interno di ogni test
        // con il Subject specifico necessario per il caso di test.
    }

    /**
     * Test della funzionalità getId() con un Subject valido.
     */
    @Test
    public void testGetId_ValidSubject_ReturnsCorrectId() {
        // Arrange
        String subject = "1234567890 Titolo del Messaggio";
        parser = new SubjectParser(subject);
        long expectedId = 1234567890L;

        // Act
        long actualId = parser.getId();

        // Assert
        Assert.assertEquals(expectedId, actualId);
    }

    /**
     * Test della funzionalità getId() quando la stringa Subject non inizia con un numero.
     * Questo testa il blocco catch (Exception e) del metodo getId().
     */
    @Test
    public void testGetId_InvalidSubject_ReturnsMinusOne() {
        // Arrange
        String subject = "NonNumerico Titolo del Messaggio";
        parser = new SubjectParser(subject);
        long expectedId = -1L;

        // Act
        long actualId = parser.getId();

        // Assert
        Assert.assertEquals(expectedId, actualId);
    }

    /**
     * Test della funzionalità getTitle() con un range tra parentesi (/).
     */
    @Test
    public void testGetTitle_SubjectWithParenthesesRange_ReturnsCorrectTitle() {
        // Arrange
        String range = "(1/10)";
        String subject = "123 Testo del Titolo " + range + " Post-Range";
        parser = new SubjectParser(subject);
        String expectedTitle = "Testo del Titolo Post-Range";

        // Act
        String actualTitle = parser.getTitle();

        // Assert
        Assert.assertEquals(expectedTitle, actualTitle.trim());
        // Verifica anche che RangeString sia stato impostato correttamente
        Assert.assertEquals(range, parser.getRangeString());
    }

    /**
     * Test della funzionalità getTitle() con un range tra parentesi quadre [].
     */
    @Test
    public void testGetTitle_SubjectWithBracketsRange_ReturnsCorrectTitle() {
        // Arrange
        String range = "[1/10]";
        String subject = "123 Testo del Titolo " + range + " Post-Range";
        parser = new SubjectParser(subject);
        String expectedTitle = "Testo del Titolo Post-Range";

        // Act
        String actualTitle = parser.getTitle();

        // Assert
        Assert.assertEquals(expectedTitle, actualTitle.trim());
        // Verifica anche che RangeString sia stato impostato correttamente
        Assert.assertEquals(range, parser.getRangeString());
    }

    /**
     * Test della funzionalità getTitle() senza range (il RangeString resta null finché non si chiama getTitle() o getRangeString()).
     * Copre il caso in cui il ciclo MAINLOOP termina senza trovare un range valido.
     */
    @Test
    public void testGetTitle_SubjectWithoutRange_ReturnsFullTitle() {
        // Arrange
        String subject = "123 Titolo del Messaggio Completo";
        parser = new SubjectParser(subject);
        String expectedTitle = "Titolo del Messaggio Completo";

        // Act
        String actualTitle = parser.getTitle();

        // Assert
        Assert.assertEquals(expectedTitle, actualTitle);
        // RangeString dovrebbe rimanere null, ma getRangeString lo chiama e restituisce null.
        Assert.assertNull(parser.getRangeString());
    }

    /**
     * Test della funzionalità getTitle() per coprire l'Exception al primo Subject.indexOf(" ").
     * Ad esempio, una stringa Subject vuota o molto corta.
     * Questo testa il blocco catch (Exception parseE) del metodo getTitle().
     */
    @Test
    public void testGetTitle_EmptySubject_ReturnsNull() {
        // Arrange
        String subject = "";
        parser = new SubjectParser(subject);

        // Act
        String actualTitle = parser.getTitle();

        // Assert
        Assert.assertNull(actualTitle);
    }

    /**
     * Test per getRangeString() quando RangeString è già stato popolato da getTitle().
     */
    @Test
    public void testGetRangeString_RangePreviouslySet_ReturnsCorrectRange() {
        // Arrange
        String expectedRange = "[1/10]";
        String subject = "123 Testo del Titolo " + expectedRange;
        parser = new SubjectParser(subject);
        parser.getTitle(); // Popola RangeString

        // Act
        String actualRange = parser.getRangeString();

        // Assert
        Assert.assertEquals(expectedRange, actualRange);
    }

    /**
     * Test per getRangeString() quando RangeString è null.
     * Questo chiama getTitle(), che nel caso di assenza di range, non imposta RangeString
     * ma garantisce il passaggio per il blocco 'if (RangeString == null)'.
     */
    @Test
    public void testGetRangeString_RangeNotSet_ReturnsNull() {
        // Arrange
        String subject = "123 Titolo Senza Range";
        parser = new SubjectParser(subject);

        // Act
        String actualRange = parser.getRangeString();

        // Assert
        Assert.assertNull(actualRange);
    }

    // --- Test per getThisRange() e getUpperRange() ---

    /**
     * Test di getThisRange() e getUpperRange() con un range valido tra parentesi.
     * Copre il primo blocco try/catch in messageParts().
     */
    @Test
    public void testGetRanges_ValidParenthesesRange_ReturnsCorrectValues() {
        // Arrange
        String subject = "123 Testo (5/15)";
        parser = new SubjectParser(subject);
        parser.getTitle(); // Assicura che RangeString sia popolato

        // Act
        int lowerRange = parser.getThisRange();
        int upperRange = parser.getUpperRange();

        // Assert
        Assert.assertEquals(5, lowerRange);
        Assert.assertEquals(15, upperRange);
    }

    /**
     * Test di getThisRange() e getUpperRange() con un range valido tra parentesi quadre.
     * Copre il secondo blocco try/catch interno in messageParts().
     */
    @Test
    public void testGetRanges_ValidBracketsRange_ReturnsCorrectValues() {
        // Arrange
        String subject = "123 Testo [2/20]";
        parser = new SubjectParser(subject);
        parser.getTitle(); // Assicura che RangeString sia popolato

        // Act
        int lowerRange = parser.getThisRange();
        int upperRange = parser.getUpperRange();

        // Assert
        Assert.assertEquals(2, lowerRange);
        Assert.assertEquals(20, upperRange);
    }

    /**
     * Test di getThisRange() e getUpperRange() quando non c'è un range valido nel Subject.
     * Copre il blocco catch (Exception subE) in messageParts().
     * Questo costringe messageParts() a restituire null.
     */
    @Test
    public void testGetRanges_NoValidRange_ReturnsDefaultValues() {
        // Arrange
        String subject = "123 Titolo Senza Range";
        parser = new SubjectParser(subject);
        parser.getTitle(); // Assicura che RangeString sia null

        // Act
        int lowerRange = parser.getThisRange();
        int upperRange = parser.getUpperRange();

        // Assert
        // I valori dovrebbero essere quelli di default dal costruttore
        Assert.assertEquals(1, lowerRange);
        Assert.assertEquals(1, upperRange);
    }

    /**
     * Test per coprire il blocco catch esterno in messageParts() (ad esempio, se Subject è null e getRangeString fallisce).
     * Nota: Subject non può essere null in questo setup, quindi si forza una condizione di errore interna.
     * Per coprire la stampa dello stack trace esterna, non è necessario alterare l'oggetto.
     * Si nota che messageParts() è chiamato sia da getThisRange() che da getUpperRange().
     */
    @Test
    public void testGetRanges_ExceptionInMessageParts_ReturnsDefaultValues() {
        // Arrange
        // Questo Subject causerà un'eccezione NullPointerException in getTitle()
        // sebbene SubjectParser lo gestisca, RangeString rimane null,
        // portando messageParts() a chiamare getRangeString(), che chiamerà getTitle()
        // che lancia l'eccezione interna in caso di Subject non formattato correttamente.
        String subject = "";
        parser = new SubjectParser(subject);

        // Act
        int lowerRange = parser.getThisRange();
        int upperRange = parser.getUpperRange();

        // Assert
        // I valori dovrebbero essere quelli di default (1)
        Assert.assertEquals(1, lowerRange);
        Assert.assertEquals(1, upperRange);
    }


    /**
     * Clean up: Esegue la pulizia dopo ogni test.
     */
    @After
    public void tearDown() {
        parser = null;
    }
}