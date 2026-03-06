/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: alessandrocioffi007@gmail.com
UserID: 1376
Date: 19/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {

    @BeforeClass
    public static void setUpClass() {
        // Setup globale (non necessario per questa classe specifica)
    }

    @AfterClass
    public static void tearDownClass() {
        // Teardown globale
    }

    @Before
    public void setUp() {
        // Setup per ogni test
    }

    @After
    public void tearDown() {
        // Teardown per ogni test
    }

    // --- TEST METODO: getId() ---

    @Test
    public void testGetId_Valid() {
        // Caso standard: Subject inizia con un numero
        SubjectParser parser = new SubjectParser("12345 Oggetto della mail");
        assertEquals("Deve ritornare il primo token come long", 12345L, parser.getId());
    }

    @Test
    public void testGetId_InvalidNonNumeric() {
        // Caso errore: Subject non inizia con un numero
        SubjectParser parser = new SubjectParser("NonUnNumero Oggetto");
        assertEquals("Deve ritornare -1 se il parsing fallisce", -1L, parser.getId());
    }

    @Test
    public void testGetId_NullSubject() {
        // Caso limite: Subject è null
        SubjectParser parser = new SubjectParser(null);
        assertEquals("Deve ritornare -1 se subject è null (eccezione catturata)", -1L, parser.getId());
    }

    // --- TEST METODI: getTitle() e getRangeString() ---
    // Questi metodi sono accoppiati: getTitle popola RangeString.

    @Test
    public void testGetTitle_NoRange() {
        // Copre il caso in cui non ci sono parentesi range
        // Copre il ramo else di (testchar == ')' || testchar == ']')
        SubjectParser parser = new SubjectParser("100 Solo Titolo Semplice");
        
        String title = parser.getTitle();
        assertEquals("Il titolo deve essere estratto correttamente", "Solo Titolo Semplice", title);
        assertNull("RangeString deve essere null se non trovato", parser.getRangeString());
    }

    @Test
    public void testGetTitle_WithParenthesesRange() {
        // Copre il parsing con ()
        // Esempio: (1/10)
        SubjectParser parser = new SubjectParser("100 Titolo Mail (1/10)");
        
        String title = parser.getTitle();
        // Nota: La logica del parser rimuove il range, rimane "Titolo Mail " (con lo spazio finale preservato dal loop)
        assertEquals("Titolo Mail ", title); 
        assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_WithSquareBracketsRange() {
        // Copre il parsing con []
        // Copre il ramo 'if (testchar == ')') else endchar = '['
        SubjectParser parser = new SubjectParser("100 Titolo Mail [5/20]");
        
        String title = parser.getTitle();
        assertEquals("Titolo Mail ", title); 
        assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void testGetTitle_MultipleRanges_LastOneWins() {
        // Copre il caso in cui ci sono più "sembianze" di range.
        // Il loop è all'indietro (i--), quindi il primo che trova valido (quello in fondo) diventa IL range.
        // Questo serve per verificare la variabile booleana 'FoundRange'.
        SubjectParser parser = new SubjectParser("100 Titolo (A) test [1/5]");
        
        String title = parser.getTitle();
        // [1/5] viene preso come range. (A) viene trattato come testo normale.
        assertEquals("Titolo (A) test ", title);
        assertEquals("[1/5]", parser.getRangeString());
    }

    @Test
    public void testGetTitle_BrokenRangeFormat() {
        // Copre il while loop interno: while ((nextchar = tmpSubject.charAt(--i)) != endchar)
        // Caso in cui trova una parentesi chiusa ma caratteri non validi prima della aperta
        SubjectParser parser = new SubjectParser("100 Titolo (NoDigits) fine");
        
        String title = parser.getTitle();
        // Il parser incontra ')', torna indietro, trova lettere ('s', 't'...),
        // if ((Character.isDigit(nextchar) == false) && nextchar != '/') -> continue MAINLOOP
        // Quindi il (NoDigits) viene re-inserito nel buffer del titolo e ignorato come range.
        assertEquals("Titolo (NoDigits) fine", title);
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void testGetTitle_ExceptionHandling() {
        // Copre il catch (Exception parseE) in getTitle
        // Subject senza spazi causa eccezione in substring(Subject.indexOf(" ") + 1) se indexOf è -1?
        // No, indexOf ritorna -1, +1 = 0. substring(0) è valida.
        // Proviamo null per forzare NullPointerException
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }

    @Test
    public void testGetTitle_IndexOutOfBoundsInLoop() {
        // Caso mutation difficile: Trova la chiusura ')' ma finisce la stringa prima di trovare la apertura '('
        // Questo dovrebbe causare eccezione nel charAt(--i) e finire nel catch.
        SubjectParser parser = new SubjectParser("100 12345)"); // Solo chiusa, niente aperta
        
        String result = parser.getTitle();
        // Se il codice è robusto o cattura l'eccezione, ritorna null o gestisce l'errore.
        // In questo codice: catch (Exception parseE) -> return null.
        assertNull(result);
    }

    // --- TEST METODI: getThisRange() e getUpperRange() ---
    // Questi testano indirettamente il metodo privato messageParts()

    @Test
    public void testRanges_Parentheses_Valid() {
        // Copre il primo blocco try di messageParts (parentesi tonde)
        SubjectParser parser = new SubjectParser("100 Oggetto (1/10)");
        parser.getTitle(); // Necessario chiamare per popolare RangeString
        
        assertEquals(1, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }

    @Test
    public void testRanges_Brackets_Valid() {
        // Copre il blocco catch(Exception inte) -> try nested (parentesi quadre) di messageParts
        // Forza un'eccezione nel primo blocco (non trova '(') e entra nel secondo (trova '[')
        SubjectParser parser = new SubjectParser("100 Oggetto [2/20]");
        parser.getTitle(); 
        
        assertEquals(2, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void testRanges_Defaults() {
        // Se non c'è range, i valori di default sono 1 e 1 (impostati nel costruttore)
        // messageParts ritorna null, quindi i campi non vengono aggiornati.
        SubjectParser parser = new SubjectParser("100 Oggetto Senza Range");
        parser.getTitle();
        
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testRanges_MessageParts_ExceptionFlow() {
        // Copre il caso in cui messageParts fallisce completamente (ritorna null)
        // Esempio: RangeString esiste ma è malformato in modo che StringTokenizer fallisca
        // getTitle potrebbe aver validato la presenza di '/' e digit, ma messageParts fa un parsing più severo.
        
        // Costruiamo una stringa che getTitle accetta ma messageParts rifiuta.
        // getTitle accetta digits e '/'.
        // messageParts fa Integer.parseInt.
        
        // "100 T (1//2)" -> getTitle accetta '/' multipli?
        // getTitle check: if ((Character.isDigit(nextchar) == false) && nextchar != '/')
        // Quindi (1//2) passa getTitle.
        // In messageParts: StringTokenizer con delimiter "/" su "1//2".
        // token 1: "1", token 2: "2" (StringTokenizer salta i delimitatori vuoti consecutivi di default? No, dipende).
        // Proviamo un caso che rompe Integer.parseInt o substring.
        
        SubjectParser parser = new SubjectParser("100 T (1/1)");
        // Forziamo RangeString a qualcosa di rotto bypassando getTitle se possibile, 
        // ma qui dobbiamo passare per getTitle.
        // Usiamo una stringa che rompe la logica specifica di messageParts.
        // messageParts fa: sLow.substring(1, sLow.length()) assumendo che il primo char sia la parentesi '('
        // Ma il tokenizer è su `range` che è "tmpRange.substring(0, tmpRange.indexOf(")"))".
        // Se RangeString è "(1/10)", tmpRange="(1/10)", range="(1/10".
        // Tokenizer su "(1/10" diviso da "/". Token1="(1", Token2="10".
        // sLow="(1". substring(1) -> "1". Funziona.
        
        // COPERTURA BRANCH RARA:
        // Cosa succede se messageParts va in eccezione totale e ritorna null nel blocco catch esterno?
        // Possiamo simulare chiamando getUpperRange() senza aver mai chiamato getTitle(),
        // ma getRangeString() chiama getTitle() se null.
        
        // Caso: RangeString malformato manualmente non è possibile (campo privato).
        // Usiamo un subject null per getThisRange.
        SubjectParser parserNull = new SubjectParser(null);
        // getRangeString torna null. messageParts -> mainrange è null -> NullPointerException -> catch -> return null.
        // getThisRange riceve null parts -> non aggiorna -> ritorna default 1.
        assertEquals(1, parserNull.getThisRange());
    }
    
    @Test
    public void testMessageParts_CorruptedInternalStructure() {
        // Test specifico per entrare nel catch(Exception subE) di messageParts che ritorna null.
        // Dobbiamo far fallire il blocco '(', entrare nel blocco '[', e far fallire anche quello.
        
        // Esempio: usiamo parentesi graffe o formato misto che getTitle ha "erroneamente" accettato
        // o manipoliamo input che passa getTitle ma fallisce parseInt.
        // getTitle controlla solo isDigit e '/'.
        // Ma messageParts fa Integer.parseInt.
        // Purtroppo getTitle è molto stretto, accetta SOLO cifre e /.
        // Quindi parseInt non fallirà su caratteri non numerici.
        
        // Unico modo per rompere messageParts dopo getTitle:
        // Problemi di indici o Tokenizer vuoti.
        // Esempio: (/)
        SubjectParser parser = new SubjectParser("100 T (/)");
        parser.getTitle(); 
        // getTitle: '/' accettato. digits non trovati ma il check è 'isDigit==false AND != /'.
        // Quindi '/' passa.
        // messageParts: range="(/". Tokenizer su "/".
        // nextToken lancia NoSuchElementException se non ci sono token (es. solo "/").
        // Questo lancia eccezione, va nel catch interno, prova '[' (non c'è), catch interno, return null.
        
        assertEquals(1, parser.getThisRange()); // Default value
    }
}
						