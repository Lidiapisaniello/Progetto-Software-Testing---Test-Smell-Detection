/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: dom.mennillo@studenti.unina.it
UserID: 332
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSubjectParser {
	
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SubjectParserTest {

    /**
     * Test case 1: Normal scenario where the subject string starts with a valid positive long ID followed by text.
     * Path covered: StringTokenizer finds token -> Long.parseLong succeeds -> returns ID.
     */
    @Test
    public void testGetId_ValidPositiveIdWithTitle() {
        SubjectParser parser = new SubjectParser("12345 Test Subject Title");
        assertEquals(12345L, parser.getId());
    }

    /**
     * Test case 2: Scenario where the subject string contains only the number.
     * Path covered: StringTokenizer finds token -> Long.parseLong succeeds -> returns ID.
     */
    @Test
    public void testGetId_ValidIdOnly() {
        SubjectParser parser = new SubjectParser("987654321");
        assertEquals(987654321L, parser.getId());
    }

    /**
     * Test case 3: Scenario where the ID is a negative number.
     * Path covered: StringTokenizer finds token ("-100") -> Long.parseLong succeeds -> returns negative ID.
     */
    @Test
    public void testGetId_ValidNegativeId() {
        SubjectParser parser = new SubjectParser("-100 Negative Case");
        assertEquals(-100L, parser.getId());
    }

    /**
     * Test case 4: Scenario with leading whitespace.
     * Path covered: StringTokenizer ignores leading delimiters (spaces) -> finds "555" -> returns 555.
     */
    @Test
    public void testGetId_LeadingWhitespace() {
        SubjectParser parser = new SubjectParser("   555 Whitespace");
        assertEquals(555L, parser.getId());
    }

    /**
     * Test case 5: Scenario where the first token is not a number.
     * Path covered: StringTokenizer finds token "Title" -> Long.parseLong throws NumberFormatException -> Catch -> returns -1.
     */
    @Test
    public void testGetId_NonNumericStart() {
        SubjectParser parser = new SubjectParser("Title 123");
        assertEquals(-1L, parser.getId());
    }

    /**
     * Test case 6: Scenario where the number is mixed with letters (invalid format).
     * Path covered: StringTokenizer finds token "123A" -> Long.parseLong throws NumberFormatException -> Catch -> returns -1.
     */
    @Test
    public void testGetId_InvalidFormatMixed() {
        SubjectParser parser = new SubjectParser("123A Mixed");
        assertEquals(-1L, parser.getId());
    }

    /**
     * Test case 7: Scenario where the subject string is empty.
     * Path covered: StringTokenizer created -> nextToken throws NoSuchElementException -> Catch -> returns -1.
     */
    @Test
    public void testGetId_EmptyString() {
        SubjectParser parser = new SubjectParser("");
        assertEquals(-1L, parser.getId());
    }

    /**
     * Test case 8: Scenario where the subject string is null.
     * Path covered: new StringTokenizer(null) throws NullPointerException -> Catch -> returns -1.
     */
    @Test
    public void testGetId_NullString() {
        SubjectParser parser = new SubjectParser(null);
        assertEquals(-1L, parser.getId());
    }

    /**
     * Test case 9: Scenario where the number is too large for a long (Overflow).
     * Path covered: StringTokenizer finds token -> Long.parseLong throws NumberFormatException -> Catch -> returns -1.
     */
    @Test
    public void testGetId_Overflow() {
        // 9223372036854775808 is Long.MAX_VALUE + 1
        SubjectParser parser = new SubjectParser("9223372036854775808 Overflow");
        assertEquals(-1L, parser.getId());
    }
}
				
	// Aggiungi altri metodi di test se necessario
}

						