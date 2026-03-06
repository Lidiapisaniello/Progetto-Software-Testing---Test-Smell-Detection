/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Nome dello Studente"
Cognome: "Cognome dello Studente"
Username: g.lasalvia@studenti.unina.it
UserID: 1155
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSillabatoreItaliano {
	
    private SillabatoreItaliano sillabatore;

	@BeforeClass
	public static void setUpClass() {
		// Eseguito una volta prima dell'inizio dei test nella classe
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
	}
				
	@Before
	public void setUp() {
		// Inizializzazione dell'oggetto da testare prima di ogni metodo di test
        sillabatore = new SillabatoreItaliano();
	}
				
	@After
	public void tearDown() {
		// Pulizia delle risorse (se necessaria)
	}
				
    // ----------------------------------------------------------------------------------
    // Test per l'Input e la Sanitizzazione (copertura Path 1, 2, 3)
    // ----------------------------------------------------------------------------------

	@Test
	public void sillabaNullInputTest() {
		// Verifica il caso di input null (Path 1: if (parola == null))
        String expected = "";
        String actual = sillabatore.sillaba(null);
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaEmptyInputTest() {
		// Verifica il caso di input stringa vuota (Path 2: || parola.isEmpty())
        String expected = "";
        String actual = sillabatore.sillaba("");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaInputWithWhitespaceAndUppercaseTest() {
		// Verifica il trim e toLowerCase (Path 3)
        String expected = "pal-la";
        String actual = sillabatore.sillaba(" pAlLa ");
        assertEquals(expected, actual);
	}

    // ----------------------------------------------------------------------------------
    // Test per le Regole di Pre-Divisione (copertura Path 4, 5)
    // ----------------------------------------------------------------------------------

	@Test
	public void sillabaDoubleConsonantTest() {
		// Verifica la divisione delle doppie (Path 4: parola.replaceAll("([bcdfghlmnpqrstzv])\\1", "$1-$1"))
        String expected = "car-ro";
        String actual = sillabatore.sillaba("carro");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaCQSplitTest() {
		// Verifica la divisione del gruppo 'cq' (Path 5: sillabata.replaceAll("cq", "c-q"))
        String expected = "ac-qua";
        String actual = sillabatore.sillaba("acqua");
        assertEquals(expected, actual);
	}
    
    // ----------------------------------------------------------------------------------
    // Test per V-C-V (Path 16, 10)
    // ----------------------------------------------------------------------------------

	@Test
	public void sillabaSimpleVCVTest() {
		// Verifica la divisione V-C-V (Path 16: consLength == 1)
        String expected = "ca-sa";
        String actual = sillabatore.sillaba("casa");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaInitialVowelAndVCVTest() {
		// Verifica la separazione della vocale iniziale (Path 10) e V-C-V
        String expected = "a-mo-re";
        String actual = sillabatore.sillaba("amore");
        assertEquals(expected, actual);
	}

    // ----------------------------------------------------------------------------------
    // Test per V-CCV (copertura Path 7a/b, 17, 18)
    // ----------------------------------------------------------------------------------
    
	@Test
	public void sillabaGroupLiquidiVCCVTest() {
		// Verifica V-CCV con gruppo indivisibile 'pr' (Path 17: GRUPPI_UNITI_LIQUIDI)
        String expected = "a-pri-re";
        String actual = sillabatore.sillaba("aprire");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaGroupBaseGNTest() {
		// Verifica V-CCV con gruppo indivisibile 'gn' (Path 17: GRUPPI_UNITI_BASE)
        String expected = "la-va-gna";
        String actual = sillabatore.sillaba("lavagna");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaGroupSeparableVCCVTest() {
		// Verifica V-CCV con gruppo divisibile 'mp' (Path 18: else branch for CC)
        String expected = "cam-po";
        String actual = sillabatore.sillaba("campo");
        assertEquals(expected, actual);
	}
    
    // ----------------------------------------------------------------------------------
    // Test per V-CCCV (copertura Path 19)
    // ----------------------------------------------------------------------------------
    
	@Test
	public void sillabaVCCCVTest() {
		// Verifica V-CCCV (Path 19: gruppoCons.length() >= 3, divisione VC-CCV)
        String expected = "mos-tro";
        String actual = sillabatore.sillaba("mostro");
        assertEquals(expected, actual);
	}

    // ----------------------------------------------------------------------------------
    // Test per V-V (copertura Path 14, 15)
    // ----------------------------------------------------------------------------------

	@Test
	public void sillabaIatoStrongStrongTest() {
		// Verifica V-V Iato (Path 14: v1 e v2 in "aeo")
        String expected = "po-e-ta";
        String actual = sillabatore.sillaba("poeta");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaDittongoVowelsUnitedTest() {
		// Verifica V-V Dittongo (Path 15/fall-through: iu non si divide)
        String expected = "fiu-me";
        String actual = sillabatore.sillaba("fiume");
        assertEquals(expected, actual);
	}
    
    // ----------------------------------------------------------------------------------
    // Test per Casi Limite e Logica Finale (copertura Path 11, 12)
    // ----------------------------------------------------------------------------------

	@Test
	public void sillabaOneSyllableWordTest() {
		// Parola monosillabica (Path 12/fall-through: una sola sillaba)
        String expected = "tre";
        String actual = sillabatore.sillaba("tre");
        assertEquals(expected, actual);
	}
    
	@Test
	public void sillabaInitialCCTwoSyllablesTest() {
		// Parola che inizia con gruppo consonantico (Path 11/12/16)
        String expected = "pra-to";
        String actual = sillabatore.sillaba("prato");
        assertEquals(expected, actual);
	}

}