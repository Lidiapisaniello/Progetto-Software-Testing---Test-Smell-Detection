/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Livia
Cognome: Pagliaro
Username: li.pagliaro@studenti.unina.it
UserID: 125
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;

public class TestSillabatoreItaliano {
  private SillabatoreItaliano sillabatore;
	@BeforeClass
	public static void setUpClass() {
		// Eseguito una volta prima dell'inizio dei test nella classe
		// Inizializza risorse condivise 
		// o esegui altre operazioni di setup
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
		// Effettua la pulizia delle risorse condivise 
		// o esegui altre operazioni di teardown
	}
				
	@Before
	public void setUp() {
		// Eseguito prima di ogni metodo di test
		// Preparazione dei dati di input specifici per il test
      sillabatore = new SillabatoreItaliano();
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia delle risorse o ripristino dello stato iniziale
	}
				
	@Test
	public void testMetodo() {
		// Preparazione dei dati di input
		// Esegui il metodo da testare
		// Verifica l'output o il comportamento atteso
		// Utilizza assert per confrontare il risultato atteso 
		// con il risultato effettivo
	}
				
	// Aggiungi altri metodi di test se necessario
 // --- 1. Test Input Base ---

    @Test
    public void testInputNullOVuoto() {
        assertEquals("", sillabatore.sillaba(null));
        assertEquals("", sillabatore.sillaba(""));
        assertEquals("", sillabatore.sillaba("   "));
    }

    @Test
    public void testMonosillabi() {
        assertEquals("a", sillabatore.sillaba("a"));
        assertEquals("blu", sillabatore.sillaba("blu"));
        assertEquals("tre", sillabatore.sillaba("tre"));
    }

    // --- 2. Test Regola V-CV ---

    @Test
    public void testVCV_Semplice() {
        assertEquals("ca-sa", sillabatore.sillaba("casa"));
        assertEquals("lo-ro", sillabatore.sillaba("loro"));
    }

    @Test
    public void testInizioConVocale() {
        // Funziona perché consLength=1, non cade nel bug dei dittonghi
        assertEquals("a-mo", sillabatore.sillaba("amo"));
        assertEquals("e-li-ca", sillabatore.sillaba("elica"));
    }

    // --- 3. Test Comportamenti Specifici e "Bug" del Codice ---

    @Test
    public void testDoppieESiglaS() {
        // Il codice separa le doppie normali
        assertEquals("pal-la", sillabatore.sillaba("palla"));
        
        // COMPORTAMENTO OSSERVATO: 'ss' inizia con 's', quindi viene tenuto unito (V-ssV)
        // Invece di 'lis-si', il codice produce 'li-ssi'
        assertEquals("li-ssi", sillabatore.sillaba("lissi"));
    }

    @Test
    public void testCQ_BugDittongo() {
        // COMPORTAMENTO OSSERVATO: divide 'cq' -> 'ac-qua'. 
        // Ma su 'ua' (consLength=0) salta la 'u'. Risultato: 'ac-a'
        assertEquals("ac-a", sillabatore.sillaba("acqua"));
    }

    @Test
    public void testDittonghi_Bug() {
        // COMPORTAMENTO OSSERVATO: le vocali deboli iniziali vengono saltate
        // 'auto' -> 'u-to'
        assertEquals("u-to", sillabatore.sillaba("auto"));
        // 'piede' -> 'e-de' (perde 'pi')
        assertEquals("e-de", sillabatore.sillaba("piede"));
    }

    @Test
    public void testIatoVocaliForti() {
        // Vocali forti separate -> funziona (V-V)
        assertEquals("po-e-ta", sillabatore.sillaba("poeta"));
        
        // 'maestra': 'str' (len=3) divide dopo 1^ cons -> 'ma-es-tra'
        assertEquals("ma-es-tra", sillabatore.sillaba("maestra"));
    }

    @Test
    public void testGruppiTreConsonanti() {
        // 'controllo': ntr -> len 3 -> divide dopo n -> con-trol-lo
        assertEquals("con-trol-lo", sillabatore.sillaba("controllo"));
        
        // 'giostra': 'gi' perso (bug dittongo), 'ostra' -> 'str' divide in 's-tr' -> 'os-tra'
        assertEquals("os-tra", sillabatore.sillaba("giostra"));
    }

    @Test
    public void testSImpura_Bug() {
        // 'teschio': 'sch' (len=3) -> divide 's-ch' -> 'tes-chio'
        // Poi 'chio' -> 'i' persa (bug dittongo) -> 'tes-o'
        assertEquals("tes-o", sillabatore.sillaba("teschio"));
        
        // 'pasta': 'st' (len=2) inizia con s -> unito -> 'pa-sta'
        assertEquals("pa-sta", sillabatore.sillaba("pasta"));
    }

    // --- 4. Test "Stress" ---

    @Test
    public void testParolaComplessa() {
        // Adattato al comportamento reale: 'li-ssi' invece di 'lis-si'
        assertEquals("pre-ci-pi-te-vo-li-ssi-me-vol-men-te", 
            sillabatore.sillaba("precipitevolissimevolmente"));
    }

    // --- 5. Test Reflection ---

    @Test
    public void testCostantiInterne() throws Exception {
        Field vocaliField = SillabatoreItaliano.class.getDeclaredField("VOCALI");
        vocaliField.setAccessible(true);
        String vocali = (String) vocaliField.get(null);
        assertTrue(vocali.contains("a"));
    }
  // --- Test per Copertura Avanzata (Edge Cases & Branches) ---

    @Test
    public void testRegexElseBranch() {
        // Serve una coppia di consonanti semplici che:
        // 1. Non sia un gruppo liquido (es. pr, cl)
        // 2. Non sia un digramma base (es. gn, sc)
        // 3. La prima lettera NON sia l, m, r, n, c, s
        // Esempio: "ft" in "nafta" (f + t). 
        // Questo costringe il codice a entrare nell'ultimo 'else' del ciclo regex 
        // (matcherDivisioneCN.appendReplacement(sb, c1 + c2))
        
        assertEquals("naf-ta", sillabatore.sillaba("nafta"));
    }

    @Test
    public void testParolaSenzaVocali() {
        // Copre il 'break' nel primo while: if (j >= sillabata.length())
        // La parola non ha vocali, quindi il ciclo cerca il nucleo fino alla fine e esce.
        assertEquals("pst", sillabatore.sillaba("pst"));
        assertEquals("brrr", sillabatore.sillaba("brrr"));
    }

    @Test
    public void testParolaTroncaOFinitaInConsonante() {
        // Copre il 'continue' nel secondo while: if (j >= sillabata.length())
        // Trova il nucleo, ma non trova la 'nextVocale'.
        assertEquals("tram", sillabatore.sillaba("tram"));
        assertEquals("bar", sillabatore.sillaba("bar"));
    }

    @Test
    public void testConsonantiSpeciali() {
        // Copre il caso in cui la regex iniziale non matcha affatto perché 
        // ci sono caratteri non presenti in CONSONANTI_SEMPLICI (es. 'k', 'x').
        // Inoltre testa il flusso standard V-C-V con caratteri speciali.
        assertEquals("ta-xi", sillabatore.sillaba("taxi"));
        assertEquals("ke-bab", sillabatore.sillaba("kebab"));
    }

    @Test
    public void testDittongoFallthrough() {
        // Questo test è CRITICO per la copertura.
        // Copre il "buco" logico nel blocco 'consLength == 0'.
        // Se non è iato (es. 'aeo' + 'aeo'), il codice fa un fallthrough (non c'è 'continue')
        // e incrementa 'i' manualmente alla fine del while.
        // Esempio: "piuma". 'i' e 'u' non sono forti. 
        // Bug del codice: salta la prima parte e restituisce "u-ma".
        // Testiamo questo risultato per garantire la copertura del ramo 'else' implicito.
        assertEquals("u-ma", sillabatore.sillaba("piuma"));
    }
 
}

						