/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giuse.vozza@studenti.unina.it
UserID: 511
Date: 21/11/2025
*/

import static org.junit.Assert.*;
import org.junit.Test;

public class TestSubjectParser {

    // --- Test per getId() ---

    @Test
    public void getIdValidIdTest() {
        // Copre il caso in cui il Subject inizia con un numero valido
        SubjectParser parser = new SubjectParser("12345 Test Subject");
        assertEquals(12345, parser.getId());
    }

    @Test
    public void getIdInvalidIdTest() {
        // Copre il blocco catch di getId() (NumberFormatException o input non valido)
        SubjectParser parser = new SubjectParser("NoNumber Subject");
        assertEquals(-1, parser.getId());
    }

    // --- Test per getTitle() e getRangeString() ---
    // Nota: getRangeString chiama internamente getTitle se RangeString è null

    @Test
    public void getTitleSimpleTextTest() {
        // Copre il flusso base senza parentesi
        SubjectParser parser = new SubjectParser("100 Simple Title");
        String title = parser.getTitle();
        // Nota: La logica del loop costruisce la stringa al contrario ma la restituisce corretta.
        // Verifichiamo che il titolo sia estratto correttamente.
        assertEquals("Simple Title", title);
    }

    @Test
    public void getTitleWithParenthesesRangeTest() {
        // Copre il rilevamento del range con parentesi tonde (testchar == ')')
        SubjectParser parser = new SubjectParser("100 Title (1/10)");
        
        // La logica del parser rimuove il range dal titolo se trovato
        assertEquals("Title ", parser.getTitle());
        assertEquals("(1/10)", parser.getRangeString());
    }

    @Test
    public void getTitleWithSquareBracketsRangeTest() {
        // Copre il rilevamento del range con parentesi quadre (testchar == ']')
        SubjectParser parser = new SubjectParser("100 Title [5/20]");
        
        assertEquals("Title ", parser.getTitle());
        assertEquals("[5/20]", parser.getRangeString());
    }

    @Test
    public void getTitleMultipleRangesTest() {
        // Copre il caso "&& FoundRange == false"
        // Il loop è inverso. Trova l'ultimo range [5/6], imposta FoundRange=true.
        // Poi incontra (1/2), ma FoundRange è true, quindi lo tratta come testo normale.
        SubjectParser parser = new SubjectParser("100 Title (1/2) [5/6]");
        
        // Il range attivo sarà l'ultimo trovato (quello più a destra)
        assertEquals("[5/6]", parser.getRangeString());
        // Il titolo includerà il range ignorato
        assertEquals("Title (1/2) ", parser.getTitle());
    }

    @Test
    public void getTitleWithFalseAlarmBracketsTest() {
        // Copre "Character.isDigit(nextchar) == false" -> continue MAINLOOP
        // Trova ')', torna indietro, trova 'a' (non digit e non '/'), 
        // quindi abortisce il parsing del range e inserisce i caratteri nel buffer del titolo.
        SubjectParser parser = new SubjectParser("100 Title (bad)");
        
        assertEquals("Title (bad)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleWithBracketsNoSlashTest() {
        // Copre "if (tmpbuf.toString().indexOf("/") != -1)" ramo else (implicito nel flusso)
        // Trova (123), tutti digit, ma niente slash. Il range non viene settato.
        SubjectParser parser = new SubjectParser("100 Title (123)");
        
        assertEquals("Title (123)", parser.getTitle());
        assertNull(parser.getRangeString());
    }

    @Test
    public void getTitleExceptionTest() {
        // Copre il blocco catch di getTitle
        // Passando null, Subject.substring lancerà NullPointerException
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getTitle());
    }
    
    @Test
    public void getRangeStringExceptionTest() {
        // Copre il blocco catch di getRangeString
        // Creiamo una situazione in cui getTitle lancia eccezione, ma chiamata via getRangeString
        SubjectParser parser = new SubjectParser(null);
        assertNull(parser.getRangeString());
    }

    // --- Test per getThisRange() e getUpperRange() ---
    // Questi test esercitano il metodo privato messageParts()

    @Test
    public void getThisRangeParenthesesTest() {
        // Copre messageParts -> try primario (parentesi tonde)
        SubjectParser parser = new SubjectParser("100 T (10/20)");
        // Chiama implicitamente messageParts -> parsing riuscito
        assertEquals(10, parser.getThisRange());
        assertEquals(20, parser.getUpperRange());
    }

    @Test
    public void getThisRangeSquareBracketsTest() {
        // Copre messageParts -> catch(Exception inte) -> try secondario (parentesi quadre)
        // Il primo try fallirà perché lastIndexOf("(") ritorna -1 o substring fallisce
        SubjectParser parser = new SubjectParser("100 T [30/40]");
        
        assertEquals(30, parser.getThisRange());
        assertEquals(40, parser.getUpperRange());
    }

    @Test
    public void getThisRangeParsingFailureTest() {
        // Copre messageParts -> catch(Exception subE) -> return null
        // Dobbiamo ingannare il parser: getTitle deve accettarlo (digits e slash presenti),
        // ma messageParts deve fallire nel tokenizing (es. struttura strana)
        
        // "(1/)" viene accettato da getTitle (ha digit e slash).
        // messageParts prova a parsare:
        // 1. Tonde: st.nextToken() OK ("(1"), st.nextToken() FALLISCE (niente dopo slash).
        // 2. Catch inte -> Prova Quadre: fallisce subito (niente '[').
        // 3. Catch subE -> return null.
        SubjectParser parser = new SubjectParser("100 T (1/)");
        
        // Se messageParts ritorna null, i range rimangono ai valori di default (1)
        assertEquals(1, parser.getThisRange()); 
        assertEquals(1, parser.getUpperRange());
    }

    @Test
    public void messagePartsOuterExceptionTest() {
        // Copre messageParts -> catch(Exception e) più esterno
        // Se RangeString è null (perché non c'è range), messageParts prova a fare
        // mainrange.lastIndexOf... mainrange è null -> NullPointerException
        SubjectParser parser = new SubjectParser("100 No Range Here");
        
        // getThisRange chiamerà messageParts, che lancerà NPE, catturata, ritorna null.
        // Il valore resta il default (1).
        assertEquals(1, parser.getThisRange());
    }
}