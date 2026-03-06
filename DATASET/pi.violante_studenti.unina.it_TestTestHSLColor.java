/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
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

public class TestHSLColor {
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
			@Test
    public void testInitialState() {
        HSLColor color = new HSLColor();
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(0, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
    }

    // ---------------------------------------------------------
    // Test initHSLbyRGB (Conversione RGB -> HSL)
    // ---------------------------------------------------------

    @Test
    public void testRGBtoHSL_Greyscale_LowLum() {
        // R=G=B => Greyscale path (cMax == cMin)
        // Low Lum calculation
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(50, 50, 50);

        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue()); // UNDEFINED
        // Lum calculation: ((0 + 100) * 255 + 255) / 510 roughly... logic simplifies to input value
        assertEquals(50, color.getLuminence());
        assertEquals(50, color.getRed());
    }

    @Test
    public void testRGBtoHSL_RedMax_HueWrapNegative() {
        // Caso complesso: R è max, ma G < B forzerà un Hue negativo temporaneo
        // R=255, G=0, B=100
        // RDelta=0, GDelta=42, BDelta=25 (approx)
        // Hue = BDelta - GDelta = 25 - 42 = -17 -> +255 = 238
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 100);

        assertEquals(255, color.getRed());
        // Verifica che il fix (pHue < 0) sia scattato
        assertTrue("Hue should be adjusted from negative", color.getHue() > 0);
        assertEquals(238, color.getHue()); 
    }

    @Test
    public void testRGBtoHSL_GreenMax() {
        // Max = Green path
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0);
        
        // Hue for Green Max is approx HSLMAX / 3 = 85
        assertEquals(85, color.getHue());
        assertEquals(255, color.getLuminence()); // Pure Green is bright in this model?
        // Logic: cMax=255, cMin=0. cPlus=255. Lum = (255*255 + 255)/510 = 128 (approx half)
        assertEquals(128, color.getLuminence());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testRGBtoHSL_BlueMax() {
        // Max = Blue path
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255);
        
        // Hue for Blue Max is approx 2 * HSLMAX / 3 = 170
        assertEquals(170, color.getHue());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testRGBtoHSL_HighLuminance_SaturationPath() {
        // Test del ramo else dove pLum > HSLMAX/2 per il calcolo della saturazione
        // Input chiaro ma non bianco puro per avere saturazione
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 255, 200); 
        
        assertTrue(color.getLuminence() > 127);
        assertTrue(color.getSaturation() > 0);
    }

    // ---------------------------------------------------------
    // Test initRGBbyHSL (Conversione HSL -> RGB)
    // ---------------------------------------------------------

    @Test
    public void testHSLtoRGB_Greyscale() {
        // S = 0 triggers greyscale logic directly in initRGBbyHSL
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100); // H=any, S=0, L=100
        
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void testHSLtoRGB_LowLuminance_MagicCalculation() {
        // L <= HSLMAX/2 logic
        HSLColor color = new HSLColor();
        // Hue 0 (Red), Sat 255 (Max), Lum 64 (Low)
        color.initRGBbyHSL(0, 255, 64);
        
        assertEquals(64, color.getLuminence());
        // Should result in a dark red
        assertTrue(color.getRed() > 0);
        assertEquals(0, color.getGreen()); // Sat max implies clean channels
        assertEquals(0, color.getBlue());
    }

    @Test
    public void testHSLtoRGB_HighLuminance_MagicCalculation() {
        // L > HSLMAX/2 logic
        HSLColor color = new HSLColor();
        // Hue 0 (Red), Sat 255, Lum 200 (High)
        color.initRGBbyHSL(0, 255, 200);
        
        // Should result in a pastel red / pinkish
        assertTrue(color.getRed() > 200);
        assertTrue(color.getGreen() > 100); // Whitewash effect
        assertTrue(color.getBlue() > 100);
    }

    /**
     * Questo test mira a coprire tutti i rami del metodo privato hueToRGB
     * indirettamente chiamando initRGBbyHSL.
     * hueToRGB viene chiamato 3 volte con offset diversi.
     */
    @Test
    public void testHueToRGB_Coverage() {
        HSLColor color = new HSLColor();
        // Usiamo un L e S standard
        int L = 128; 
        int S = 255;
        
        // 1. Hue che cade nel primo sesto (< 42)
        color.initRGBbyHSL(20, S, L);
        assertTrue(color.getRed() > 0);
        
        // 2. Hue nel secondo blocco (< 127)
        color.initRGBbyHSL(85, S, L); // Greenish
        assertTrue(color.getGreen() > 0);

        // 3. Hue nel terzo blocco (< 170)
        color.initRGBbyHSL(150, S, L); 
        assertTrue(color.getBlue() > 0);

        // 4. Hue nell'ultimo blocco (Else)
        color.initRGBbyHSL(200, S, L);
        assertTrue(color.getBlue() > 0);
    }
    
    // ---------------------------------------------------------
    // Test Setters & Boundaries (Mutation Killing)
    // ---------------------------------------------------------

    @Test
    public void testSetHue_LoopingLogic() {
        HSLColor color = new HSLColor();
        
        // Test doppio loop negativo (uccide mutanti che cambiano while in if)
        // -300 + 255 = -45 -> +255 = 210
        color.setHue(-300);
        assertEquals(210, color.getHue());

        // Test doppio loop positivo
        // 600 - 255 = 345 -> - 255 = 90
        color.setHue(600);
        assertEquals(90, color.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        HSLColor color = new HSLColor();
        
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
        
        color.setSaturation(300);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        HSLColor color = new HSLColor();
        
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
        
        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
    }

    // ---------------------------------------------------------
    // Test Helper Methods (Public API)
    // ---------------------------------------------------------

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.setHue(10);
        color.reverseColor();
        // 10 + 127 (HSLMAX/2 approx is 127) = 137
        assertEquals(137, color.getHue());
        
        // Test wrap around in reverse
        color.setHue(200);
        color.reverseColor();
        // 200 + 127 = 327 -> 327 - 255 = 72
        assertEquals(72, color.getHue());
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        int initialLum = color.getLuminence(); // 100
        
        // Case 0: No change
        color.brighten(0);
        assertEquals(initialLum, color.getLuminence());
        
        // Case 1: Increase
        color.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, color.getLuminence());
        
        // Case 2: Clamping Max
        color.brighten(10.0f);
        assertEquals(255, color.getLuminence());
        
        // Case 3: Negative (Darken) -> Clamped to 0
        color.brighten(-5.0f);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        // Start with Black
        color.initHSLbyRGB(0, 0, 0);
        
        // 1. Percent >= 1 -> Full Replace
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getRed());
        
        // 2. Percent <= 0 -> No Change (stays White from prev step)
        color.blend(0, 0, 0, 0.0f);
        assertEquals(255, color.getRed());
        
        // 3. Mixing (50%)
        // Current: 255, Target: 0. Result: 127
        color.blend(0, 0, 0, 0.5f);
        // int cast truncation: (0 * 0.5) + (255 * 0.5) = 127.5 -> 127
        assertEquals(127, color.getRed());
    }
    
    /**
     * Test specifico per un boundary condition nel metodo initRGBbyHSL.
     * Verifica il "clamping" dei valori RGB se superano RGBMAX.
     * Questo è difficile da scatenare con la matematica standard HSL, 
     * ma proviamo a forzarlo tramite arrotondamenti o valori limite.
     */
    @Test
    public void testRGBClampingGuard() {
        // Sebbene matematicamente difficile da eccedere con input validi,
        // questo test assicura che la logica esegua senza errori anche a massimi livelli.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 255); // Max Sat, Max Lum
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }
	
	// Aggiungi altri metodi di test se necessario
}

						