import org.junit.Test;

public class TestSubjectParser {

    // --- Metodi Helper ---

    // Soggetto con ID, Titolo, e range tra parentesi tonde
    private final static String SUBJECT_NORMAL_PARENS = "1234 Il mio messaggio (10/50)";
    // Soggetto con ID, Titolo, e range tra parentesi quadre
    private final static String SUBJECT_NORMAL_BRACKETS = "5678 Messaggio importante [2/10]";
    // Soggetto senza ID (dovrebbe fallire getId)
    private final static String SUBJECT_NO_ID = "Messaggio senza ID (1/1)";
    // Soggetto senza range (dovrebbe usare i valori di default)
    private final static String SUBJECT_NO_RANGE = "9999 Solo testo";
    // Soggetto con ID ma formato di range non valido (dovrebbe fallire messageParts)
    private final static String SUBJECT_INVALID_RANGE = "1111 Titolo con (10-50)";
    // Soggetto con ID ma formato di range non valido (dovrebbe fallire messageParts)
    private final static String SUBJECT_INVALID_RANGE_2 = "1111 Titolo con [10-50]";
    // Soggetto con carattere di range a metà del titolo (non dovrebbe essere riconosciuto)
    private final static String SUBJECT_FALSE_RANGE = "2222 Titolo con parentesi (e slash/a) e nessun range";
    // Soggetto con ID ma range parziale (per forzare l'eccezione interna in getTitle)
    private final static String SUBJECT_PARTIAL_RANGE = "3333 Messaggio con [10/";


    // --- Test per getId() ---

    @Test
    public void TestGetId_NormalCase() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_PARENS);
        parser.getId();
    }

    @Test
    public void TestGetId_ExceptionCase() {
        SubjectParser parser = new SubjectParser(SUBJECT_NO_ID);
        parser.getId();
    }


    // --- Test per getThisRange() e getUpperRange() (copre messageParts) ---

    // Copre il parsing del formato (10/50) (Parentesi tonde)
    @Test
    public void TestGetRange_NormalParens() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_PARENS);
        parser.getThisRange();
        parser.getUpperRange();
    }

    // Copre il parsing del formato [2/10] (Parentesi quadre)
    @Test
    public void TestGetRange_NormalBrackets() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_BRACKETS);
        parser.getThisRange();
        parser.getUpperRange();
    }

    // Copre il caso in cui non sia presente nessun range valido.
    @Test
    public void TestGetRange_NoRange() {
        SubjectParser parser = new SubjectParser(SUBJECT_NO_RANGE);
        parser.getThisRange();
        parser.getUpperRange();
    }
    
    // Copre la prima eccezione interna in messageParts (il range non è numerico o ha un formato strano)
    @Test
    public void TestGetRange_InvalidFormat_ParensException() {
        SubjectParser parser = new SubjectParser(SUBJECT_INVALID_RANGE); // usa '-' invece di '/'
        parser.getThisRange();
        parser.getUpperRange();
    }

    // Copre la seconda eccezione interna in messageParts (il range non è numerico o ha un formato strano)
    @Test
    public void TestGetRange_InvalidFormat_BracketsException() {
        SubjectParser parser = new SubjectParser(SUBJECT_INVALID_RANGE_2); // usa '-' invece di '/'
        parser.getThisRange();
        parser.getUpperRange();
    }


    // --- Test per getTitle() ---

    // Caso con range valido in parentesi tonde
    @Test
    public void TestGetTitle_WithParensRange() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_PARENS);
        parser.getTitle();
    }

    // Caso con range valido in parentesi quadre
    @Test
    public void TestGetTitle_WithBracketsRange() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_BRACKETS);
        parser.getTitle();
    }

    // Caso senza range
    @Test
    public void TestGetTitle_WithoutRange() {
        SubjectParser parser = new SubjectParser(SUBJECT_NO_RANGE);
        parser.getTitle();
    }

    // Caso di range fittizio nel titolo (copre il 'continue MAINLOOP')
    @Test
    public void TestGetTitle_FalseRange() {
        SubjectParser parser = new SubjectParser(SUBJECT_FALSE_RANGE);
        parser.getTitle();
    }
    
    // Caso per forzare l'eccezione di parsing in getTitle (es. indice fuori range)
    @Test
    public void TestGetTitle_ParsingException() {
        SubjectParser parser = new SubjectParser(SUBJECT_PARTIAL_RANGE); 
        parser.getTitle();
    }


    // --- Test per getRangeString() ---

    // Copre il caso in cui RangeString è null e viene calcolato da getTitle()
    @Test
    public void TestGetRangeString_Calculated() {
        SubjectParser parser = new SubjectParser(SUBJECT_NORMAL_PARENS);
        parser.getRangeString(); // Primo calcolo (chiama getTitle())
        parser.getRangeString(); // Secondo richiamo (usa RangeString già impostato)
    }

    // Copre il caso in cui non sia presente nessun range (RangeString resta null)
    @Test
    public void TestGetRangeString_NoRangeFound() {
        SubjectParser parser = new SubjectParser(SUBJECT_NO_RANGE);
        parser.getRangeString(); 
    }
}