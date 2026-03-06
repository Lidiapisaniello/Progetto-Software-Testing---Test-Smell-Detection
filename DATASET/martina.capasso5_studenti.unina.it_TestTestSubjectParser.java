/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Martina"
Cognome: "Capasso"
Username: martina.capasso5@studenti.unina.it
UserID: 135
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

// Import necessario per accedere ai campi privati
import java.lang.reflect.Field;
import java.util.NoSuchElementException; // Import esplicito sebbene non strettamente nec.

public class TestSubjectParser {

    /**
     * Metodo helper per accedere a un campo privato tramite Reflection.
     */
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    /**
     * Metodo helper per impostare un campo privato (necessario per forzare eccezioni).
     */
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @BeforeClass
    public static void setUpClass() {
    }
            
    @AfterClass
    public static void tearDownClass() {
    }
            
    @Before
    public void setUp() {
    }
            
    @After
    public void tearDown() {
    }
            
    // --- 1. Test del Costruttore e Stato Iniziale ---
            
    @Test
    public void testConstructorInitialization() throws Exception {
        String subjectStr = "123 Test";
        SubjectParser parser = new SubjectParser(subjectStr);
        
        assertEquals("Il campo 'Subject' non è stato inizializzato correttamente", 
            subjectStr, getPrivateField(parser, "Subject"));
        assertEquals("Il campo 'UpperRange' non è stato inizializzato a 1", 
            1, getPrivateField(parser, "UpperRange"));
        assertEquals("Il campo 'LowerRange' non è stato inizializzato a 1", 
            1, getPrivateField(parser, "LowerRange"));
        assertNull("Il campo 'RangeString' (cache) dovrebbe essere null all'inizio", 
            getPrivateField(parser, "RangeString"));
    }

    // --- 2. Test di getId() [Copre tutti i catch] ---

    @Test
    public void testGetId_Valid() {
        SubjectParser parser = new SubjectParser("1234567890 Test Subject");
        assertEquals(1234567890L, parser.getId());
    }

    @Test
    public void testGetId_NotANumber_NFE() {
        // Copre NumberFormatException -> catch (Exception e)
        SubjectParser parser = new SubjectParser("ABCDE Test Subject");
        assertEquals(-1L, parser.getId());
    }

    @Test
    public void testGetId_EmptySubject_NSEE() {
        // Copre NoSuchElementException -> catch (Exception e)
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }
    
    @Test
    public void testGetId_NullSubject_NPE() {
        // Copre NullPointerException -> catch (Exception e)
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }
    
    @Test
    public void testGetId_OnlySpaces_NSEE() {
        // Copre NoSuchElementException -> catch (Exception e)
        SubjectParser parser = new SubjectParser("   ");
        assertEquals(-1L, parser.getId());
    }

    // --- 3. Test di getTitle() e getRangeString() [Percorsi logici] ---

    @Test
    public void testGetTitleAndRange_Parentheses() {
        SubjectParser parser = new SubjectParser("987 Titolo (5/10)");
        assertEquals("Titolo ", parser.getTitle());
        assertEquals("(5/10)", parser.getRangeString());
    }
    
    @Test
    public void testGetTitleAndRange_Brackets() {
        SubjectParser parser = new SubjectParser("987 Titolo [20/30]");
        assertEquals("Titolo ", parser.getTitle());
        assertEquals("[20/30]", parser.getRangeString());
    }

    @Test
    public void testGetTitle_NoRange() {
        SubjectParser parser = new SubjectParser("987 Titolo senza range");
        assertEquals("Titolo senza range", parser.getTitle());
        assertNull(parser.getRangeString());
    }
    
    @Test
    public void testGetTitle_MultipleRanges_FindsLast() {
        // getTitle() parsa all'indietro, quindi trova l'ultimo range.
        SubjectParser parser = new SubjectParser("Titolo [1/2] (3/4)");
        assertEquals("Titolo [1/2] ", parser.getTitle());
        assertEquals("(3/4)", parser.getRangeString());
    }

    @Test
    public void testGetTitle_ParenthesesNumericNotARange() {
        // Test per il path 'continue MAINLOOP' (manca '/')
        SubjectParser parser = new SubjectParser("987 Titolo (123)");
        // Il parser identifica (123) come "range" ma non trova '/', quindi lo scarta.
        // Questo è un bug della classe, ma noi testiamo il comportamento.
        assertEquals("Titolo ", parser.getTitle()); 
        assertNull(parser.getRangeString());
    }

    @Test
    public void testGetTitle_InvalidCharsInParentheses() {
        // Test per il path 'continue MAINLOOP' (carattere non valido 'a')
        SubjectParser parser = new SubjectParser("987 Titolo (1a/2)");
        assertEquals("Titolo (1a/2)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    // --- 4. Test di messageParts() [Percorsi logici] ---

    @Test
    public void testGetRanges_NoRange_NPEInMessageParts() {
        // getRangeString() torna null -> messageParts() lancia NPE -> catch(e) esterno
        SubjectParser parser = new SubjectParser("987 Titolo senza range");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testGetRanges_InvalidRange_AllCatch() {
        // Copre catch(inte) E catch(subE) in messageParts()
        // getTitle() trova [c/d].
        // messageParts() prova '()', fallisce (SIOOBE) -> catch(inte).
        // Prova '[]', fallisce (NFE su "c") -> catch(subE) -> return null.
        SubjectParser parser = new SubjectParser("987 Titolo (a/b) [c/d]");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }
    
    @Test
    public void testGetRanges_InvalidRange_NSEE() {
        // Copre catch(inte) E catch(subE) per NoSuchElementException
        SubjectParser parser = new SubjectParser("987 Titolo (1) [2]");
        assertEquals(1, parser.getThisRange());
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void testMessageParts_FallbackToBrackets() {
        // Test path: try() fallisce (catch inte), try[] riesce.
        SubjectParser parser = new SubjectParser("987 Titolo (invalid) [5/10]");
        assertEquals(5, parser.getThisRange());
        assertEquals(10, parser.getUpperRange());
    }

    // --- 5. Test Eccezioni con Reflection e Override [CHIAVE PER IL COVERAGE] ---

    @Test
    public void testGetTitle_CatchBlock_ViaReflection() throws Exception {
        // Copre catch(Exception parseE) in getTitle()
        SubjectParser parser = new SubjectParser("Dummy");
        
        // Imposta Subject a null per forzare un NullPointerException
        setPrivateField(parser, "Subject", null);
        
        assertNull("getTitle() non ha restituito null in caso di NPE (reflection)", 
            parser.getTitle());
    }

    @Test
    public void testGetRangeString_CatchBlock_ViaOverride() {
        // Copre catch(Exception e) in getRangeString()
        // Creiamo un parser "rotto" che lancia un'eccezione quando getTitle() viene chiamato
        SubjectParser brokenParser = new SubjectParser("Dummy") {
            @Override
            public String getTitle() {
                // Questa eccezione sarà catturata da getRangeString()
                throw new RuntimeException("Forza il catch in getRangeString");
            }
        };
        
        // La cache RangeString è null, quindi getRangeString() chiama getTitle(),
        // che lancia l'eccezione, che viene catturata.
        assertNull(brokenParser.getRangeString());
    }
 
    @Test
    public void testMessageParts_OuterCatch_ViaOverride() {
        // Copre catch(Exception e) esterno in messageParts()
        // Creiamo un parser "rotto" che lancia un'eccezione quando getRangeString() viene chiamato
        SubjectParser brokenParser = new SubjectParser("Dummy") {
            @Override
            public String getRangeString() {
                // Questa eccezione sarà catturata dal try/catch esterno di messageParts()
                throw new RuntimeException("Forza il catch esterno in messageParts");
            }
        };
        
        // getUpperRange() -> messageParts() -> getRangeString() (rotto) -> Exception
        // -> catch(Exception e) in messageParts() -> return null.
        // getUpperRange() riceve null e ritorna il default 1.
        assertEquals(1, brokenParser.getUpperRange());
    }
    
    // --- 6. Test Caching ---
    
    @Test
    public void testGetRangeString_CacheHit() throws Exception {
        SubjectParser parser = new SubjectParser("987 Titolo (1/10)");

        // 1. Prima chiamata: popola la cache RangeString
        assertEquals("(1/10)", parser.getRangeString());
        
        // 2. Modifichiamo il campo Subject
        setPrivateField(parser, "Subject", "MODIFICATO (questo non deve essere letto)");
        
        // 3. Seconda chiamata: deve leggere dalla cache
        assertEquals("RangeString non letto dalla cache", 
            "(1/10)", parser.getRangeString());
    }
}

