/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: cri.dibenedetto@studenti.unina.it
UserID: 1093
Date: 25/11/2025
*/

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestSubjectParser {

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setErr(originalErr);
    }

    // ==========================================
    // HELPER METHODS (REFLECTION)
    // ==========================================

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        Field field = SubjectParser.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
    
    private Object invokePrivateMethod(Object instance, String methodName) throws Exception {
        Method method = SubjectParser.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(instance);
    }

    // ==========================================
    // 1. TEST ID PARSING
    // ==========================================

    @Test
    public void testGetIdValid() {
        SubjectParser parser = new SubjectParser("12345 Title");
        assertEquals(12345L, parser.getId());
    }

    @Test
    public void testGetIdInvalid() {
        SubjectParser parser = new SubjectParser("ABC Title");
        assertEquals(-1, parser.getId());
    }
    
    @Test
    public void testGetIdNull() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1, parser.getId());
    }

    // ==========================================
    // 2. TEST RANGE PARSING
    // ==========================================

    @Test
    public void testRoundBracketsRange() {
        SubjectParser parser = new SubjectParser("100 Test Subject (1/10)");
        assertEquals(1, parser.getThisRange()); 
        assertEquals(10, parser.getUpperRange());
        String title = parser.getTitle();
        assertTrue(title.contains("Test Subject"));
        assertFalse(title.contains("1/10"));
    }

    @Test
    public void testSquareBracketsRange() {
        SubjectParser parser = new SubjectParser("200 Test Subject [5/20]");
        assertEquals(5, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
        assertTrue(parser.getRangeString().contains("[5/20]"));
    }

    // ==========================================
    // 3. TEST BRANCH & LINE COERAGE "KILLERS"
    // ==========================================

    @Test
    public void testDoubleRange() {
        // 
        // Il parser lavora da destra a sinistra.
        // 1. Trova (3/4) -> Range Valido -> FoundRange = true.
        // 2. Continua a sinistra.
        // 3. Trova (1/2) -> ')' match, ma FoundRange è true -> Ramo ELSE.
        // Il secondo range (quello a sinistra) deve rimanere nel titolo.
        
        SubjectParser parser = new SubjectParser("123 Title (1/2) (3/4)");
        
        // Verifica che abbia preso il range di destra
        assertEquals("(3/4)", parser.getRangeString());
        assertEquals(3, parser.getThisRange());
        
        // Verifica che il range di sinistra sia rimasto nel titolo
        // Nota: il parser legacy lascia spazi strani, usiamo contains per sicurezza
        String title = parser.getTitle();
        assertTrue("Il titolo dovrebbe contenere il primo range ignorato", title.contains("(1/2)"));
    }

    @Test
    public void testDigitsInBracketsNoSlash() {
        // Questo copre il ramo if (indexOf("/") != -1) -> FALSE
        // Stringa con parentesi e numeri, ma senza slash.
        // Il codice entra nel blocco di parsing, finisce il while, ma fallisce il check dello slash.
        // Risultato: il contenuto (123) viene "mangiato" e non aggiunto al titolo.
        
        SubjectParser parser = new SubjectParser("123 Title (123)");
        
        String title = parser.getTitle();
        assertNull(parser.getRangeString());
        // Comportamento osservato: (123) viene rimosso perché considerato "tentativo di range fallito"
        // Attenzione: se il tuo parser si comporta diversamente correggi l'assert, 
        // ma questo input è necessario per coprire quel ramo specifico.
        assertFalse(title.contains("(123)")); 
        assertTrue(title.contains("Title"));
    }

    @Test
    public void testGetTitleLoopLogicComplex() {
        // Attiva 'continue MAINLOOP' (Ramo negativo isDigit)
        SubjectParser parser = new SubjectParser("123 Title (A)");
        String title = parser.getTitle();
        assertTrue(title.contains("(A)"));
        assertNull(parser.getRangeString());

        // Caso spurio misto per robustezza
        SubjectParser parser2 = new SubjectParser("123 T (1/2) a)");
        parser2.getTitle(); 
        assertEquals("(1/2)", parser2.getRangeString());
    }

    // ==========================================
    // 4. TEST EXCEPTION HANDLING & REFLECTION
    // ==========================================

    @Test
    public void testGetRangeStringException() throws Exception {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
        assertTrue(errContent.toString().length() > 0); 
    }
    
    @Test
    public void testGetTitleException() {
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle()); 
    }

    @Test
    public void testMessagePartsOuterException() throws Exception {
        SubjectParser parser = new SubjectParser("Test");
        Object result = invokePrivateMethod(parser, "messageParts");
        assertNull(result); 
    }

    @Test
    public void testMessagePartsInnerCatchBlocks() throws Exception {
        SubjectParser parser = new SubjectParser("Test");
        setPrivateField(parser, "RangeString", "(123)"); 
        Object result = invokePrivateMethod(parser, "messageParts");
        assertNull(result); 
    }
    
    @Test
    public void testMessagePartsSquareBracketsFail() throws Exception {
        SubjectParser parser = new SubjectParser("Test");
        setPrivateField(parser, "RangeString", "[123]"); 
        Object result = invokePrivateMethod(parser, "messageParts");
        assertNull(result); 
    }

    @Test
    public void testGetThisRangeWithNullParts() throws Exception {
        SubjectParser parser = new SubjectParser("Test");
        int val = parser.getThisRange();
        assertEquals(1, val); 
    }
    
    @Test
    public void testGetUpperRangeWithNullParts() throws Exception {
        SubjectParser parser = new SubjectParser("Test");
        int val = parser.getUpperRange();
        assertEquals(1, val); 
    }
    
    @Test
    public void testNoRange() {
        SubjectParser parser = new SubjectParser("300 Simple Title");
        assertEquals(1, parser.getThisRange()); 
        assertNull(parser.getRangeString());
    }

    @Test
    public void testBrokenBrackets() {
        SubjectParser parser = new SubjectParser("500 Title (1/5");
        assertNull(parser.getRangeString());
    }
}
