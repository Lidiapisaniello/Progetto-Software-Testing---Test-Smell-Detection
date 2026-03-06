/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Pietro"
Cognome: "Violante"
Username: pi.violante@studenti.unina.it
UserID: 270
Date: 21/11/2025
*/

import org.junit.Before;

import org.junit.After;

import org.junit.BeforeClass;

import org.junit.AfterClass;

import org.junit.Test;

import static org.junit.Assert.*;
 
public class TestSubjectParser {

	@Test

    public void testGetId_Success() {

        SubjectParser parser = new SubjectParser("12345 Test Subject");

        assertEquals("L'ID deve essere parsato correttamente", 12345L, parser.getId());

    }
 
    // 2. Fallimento (Eccezione): Subject non inizia con un ID numerico valido.

    // Questo attiva il blocco 'catch' in getId() e ritorna -1.

    @Test

    public void testGetId_Failure() {

        SubjectParser parser = new SubjectParser("NonNumericId Test Subject");

        assertEquals("L'ID deve ritornare -1 in caso di parsing fallito", -1L, parser.getId());

    }
 
    // -------------------------------------------------------------------------

    // TEST METODO getRangeString() e getTitle()

    // -------------------------------------------------------------------------

    // CASO COMPLETO 1: Formato con parentesi tonde (e '/' richiesto).

    // Attiva il primo try in messageParts() e imposta RangeString e Title.

    @Test

    public void testGetTitle_RangeParenthesis_Success() {

        String subject = "12345 Titolo del Messaggio (1/10)";

        SubjectParser parser = new SubjectParser(subject);

        // Verifica getTitle()

        String expectedTitle = "Titolo del Messaggio ";

        assertEquals("Il titolo deve essere corretto (con spazio finale)", expectedTitle, parser.getTitle());

        // Verifica getRangeString() (assicura che sia stato chiamato getTitle())

        String expectedRange = "(1/10)";

        assertEquals("La RangeString deve essere parsata correttamente", expectedRange, parser.getRangeString());

    }
 
    // CASO COMPLETO 2: Formato con parentesi quadre (e '/' richiesto).

    // Attiva il secondo try in messageParts() (dopo il fallimento del primo try).

    @Test

    public void testGetTitle_RangeSquareBrackets_Success() {

        String subject = "67890 Un Altro Titolo [2/5]";

        SubjectParser parser = new SubjectParser(subject);

        // Verifica getTitle()

        String expectedTitle = "Un Altro Titolo ";

        assertEquals("Il titolo deve essere corretto", expectedTitle, parser.getTitle());

        // Verifica getRangeString()

        String expectedRange = "[2/5]";

        assertEquals("La RangeString deve essere parsata correttamente", expectedRange, parser.getRangeString());

    }
 
    // CASO 3: Subject senza Range (testa il ramo 'else' nel loop di getTitle e la logica di fallback).

    @Test

    public void testGetTitle_NoRange() {

        String subject = "54321 Subject Senza Range";

        SubjectParser parser = new SubjectParser(subject);
 
        String expectedTitle = "Subject Senza Range";

        assertEquals("Il titolo deve essere l'intero Subject meno l'ID", expectedTitle, parser.getTitle());
 
        // RangeString dovrebbe essere null, poiché non viene trovato alcun range valido da getTitle()

        assertNull("RangeString dovrebbe essere null senza range valido", parser.getRangeString());

    }

    // CASO 4: Range parziale o malformattato che fallisce in getTitle()

    // Esempio: "Subject (A/10)" - la scansione all'indietro trova 'A' (non cifra, non '/'), fallendo FoundRange.

    @Test

    public void testGetTitle_MalformedRange() {

        // La logica in getTitle() dovrebbe fallire la condizione di range se trova un carattere non valido (es. 'A')

        String subject = "1000 Titolo con Range Malformato (A/10)";

        SubjectParser parser = new SubjectParser(subject);
 
        // Ci si aspetta che (A/10) venga considerato parte del titolo perché la validazione interna fallisce

        String expectedTitle = "Titolo con Range Malformato (A/10)";

        assertEquals("Il range malformato deve essere incluso nel titolo", expectedTitle, parser.getTitle());

        assertNull("RangeString dovrebbe essere null", parser.getRangeString());

    }
 
    // -------------------------------------------------------------------------

    // TEST METODI getThisRange() e getUpperRange()

    // -------------------------------------------------------------------------
 
    // 1. Successo - Range Tondo: Attiva il primo try di messageParts().

    @Test

    public void testGetRanges_Parenthesis_Success() {

        String subject = "111 Range Test (42/99)";

        SubjectParser parser = new SubjectParser(subject);

        // La chiamata getThisRange() popola LowerRange e getUpperRange() popola UpperRange

        assertEquals("LowerRange deve essere 42", 42, parser.getThisRange());

        assertEquals("UpperRange deve essere 99", 99, parser.getUpperRange());

    }

    // 2. Successo - Range Quadro: Attiva il secondo try di messageParts().

    @Test

    public void testGetRanges_SquareBrackets_Success() {

        String subject = "222 Range Test [1/2]";

        SubjectParser parser = new SubjectParser(subject);

        assertEquals("LowerRange deve essere 1", 1, parser.getThisRange());

        assertEquals("UpperRange deve essere 2", 2, parser.getUpperRange());

    }
 
    // 3. Fallimento - Nessun Range Valido: Attiva il blocco 'catch' più interno di messageParts() e ritorna i valori di default (1).

    @Test

    public void testGetRanges_NoRange_ReturnsDefault() {

        String subject = "333 Test Senza Range Valido";

        SubjectParser parser = new SubjectParser(subject);

        // Il campo Subject non contiene un range parsabile. messageParts() ritorna null.

        // getThisRange() e getUpperRange() dovrebbero ritornare il valore di default (1).

        assertEquals("LowerRange deve essere il default 1", 1, parser.getThisRange());

        assertEquals("UpperRange deve essere il default 1", 1, parser.getUpperRange());

    }

    // 4. Fallimento - Range Malformato (Non Numerico): Fallisce il parseInt in messageParts().

    @Test

    public void testGetRanges_MalformedNumbers() {

        // Fallisce il parseInt in messageParts (primo try) e poi fallisce il secondo try (che non trova range quadrato)

        String subject = "444 Range Non Numerico (A/B)";

        SubjectParser parser = new SubjectParser(subject);

        // messageParts() dovrebbe fallire il parseInt, attivare il catch interno, e ritornare null

        assertEquals("LowerRange deve essere il default 1", 1, parser.getThisRange());

        assertEquals("UpperRange deve essere il default 1", 1, parser.getUpperRange());

    }

}
 