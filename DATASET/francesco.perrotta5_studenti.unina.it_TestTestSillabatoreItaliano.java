/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Francesco"
Cognome: "Perrotta"
Username: francesco.perrotta5@studenti.unina.it
UserID: 1620
Date: 17/11/2025
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
		// Eseguito prima di ogni metodo di test
		// Inizializza l'oggetto da testare
        sillabatore = new SillabatoreItaliano();
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia (imposta a null per chiarezza)
        sillabatore = null;
	}
    
    // -------------------------------------------------------------------
    // METODI DI TEST SPECIFICI
    // -------------------------------------------------------------------

    /**
     * Test V-CV (consonante singola va alla sillaba successiva)
     */
	@Test
	public void testSillabazioneBase_VCV() {
		assertEquals("ca-sa", sillabatore.sillaba("casa"));
        assertEquals("te-le-fo-no", sillabatore.sillaba("telefono"));
        assertEquals("a-mo", sillabatore.sillaba("amo"));
	}

    /**
     * Test per la divisione delle doppie e 'cq' (sempre divisi C-C).
     */
    @Test
    public void testDivisioneDoppieECQ() {
        // Doppie
        assertEquals("pal-la", sillabatore.sillaba("palla"));
        assertEquals("mat-to", sillabatore.sillaba("matto"));
        // 'cq'
        assertEquals("ac-qua", sillabatore.sillaba("acqua"));
        assertEquals("ac-qui-sta-re", sillabatore.sillaba("acquistare"));
    }
    
    /**
     * Test per gruppi consonantici indivisibili (liquidi: C + l/r) - V-CCV.
     */
    @Test
    public void testGruppiLiquidiIndivisibili_VCCV() {
        // C + R (V-CRV)
        assertEquals("a-pri-re", sillabatore.sillaba("aprire"));
        assertEquals("po-tro-ne", sillabatore.sillaba("potrone"));
        // C + L (V-CLV)
        assertEquals("a-tle-ta", sillabatore.sillaba("atleta"));
    }

    /**
     * Test per i gruppi speciali indivisibili: gli, gn, sc, ch, gh.
     */
    @Test
    public void testGruppiSpeciali() {
        assertEquals("a-gno", sillabatore.sillaba("agno"));
        assertEquals("fa-glia", sillabatore.sillaba("faglia"));
        assertEquals("pe-sce", sillabatore.sillaba("pesce"));
        assertEquals("chi-mo-no", sillabatore.sillaba("chimono"));
    }
    
    /**
     * Test per gruppi consonantici divisibili (l, m, n, r, c + C) - VC-CV.
     */
    @Test
    public void testGruppiConsonanticiDivisibili_VCCV() {
        assertEquals("al-ta-re", sillabatore.sillaba("altare"));
        assertEquals("om-bra", sillabatore.sillaba("ombra")); // Qui 'mb' viene diviso come m-b (om-bra)
        assertEquals("an-co-ra", sillabatore.sillaba("ancora"));
        assertEquals("for-za", sillabatore.sillaba("forza"));
    }
    
    /**
     * Test per 's' impura (S + C, sempre unite alla sillaba successiva) - V-SCV.
     */
    @Test
    public void testSImpura() {
        assertEquals("a-sta", sillabatore.sillaba("asta"));
        assertEquals("re-sto", sillabatore.sillaba("resto"));
        assertEquals("i-spi-ra-re", sillabatore.sillaba("ispirare")); 
    }
    
    /**
     * Test per lo Iato (divisione di vocali forti/accentate: V-V).
     */
    @Test
    public void testIato() {
        assertEquals("po-e-ta", sillabatore.sillaba("poeta")); // o-e (forti)
        assertEquals("a-e-re-o", sillabatore.sillaba("aereo")); // a-e, e-o
    }
    
    /**
     * Test per il Dittongo (vocali che restano unite: VV).
     */
    @Test
    public void testDittongo() {
        assertEquals("pie-de", sillabatore.sillaba("piede")); // ie (debole-forte)
        assertEquals("au-ra", sillabatore.sillaba("aura"));   // au (forte-debole)
        assertEquals("fiu-me", sillabatore.sillaba("fiume")); // iu (debole-debole)
    }

    /**
     * Test con input non validi (null, vuoto, spazi).
     */
    @Test
    public void testInputNonValido() {
        assertEquals("", sillabatore.sillaba(null));
        assertEquals("", sillabatore.sillaba(""));
        assertEquals("", sillabatore.sillaba("   "));
    }
    
    /**
     * Test con parole che iniziano con consonanti multiple (gestite dalla logica VCV/CCV).
     */
    @Test
    public void testInizioConConsonantiMultiple() {
        assertEquals("stra-da", sillabatore.sillaba("strada"));
        assertEquals("cri-ne", sillabatore.sillaba("crine"));
    }
}