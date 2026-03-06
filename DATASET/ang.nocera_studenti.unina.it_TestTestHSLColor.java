/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: ang.nocera@studenti.unina.it
UserID: 401
Date: 20/11/2025
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
				
    // --- TEST RGB -> HSL ---

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // R=G=B -> Saturazione 0, Hue Undefined
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        // Lum = (200 * 255 + 255) / 510 approx 100
        assertTrue("Luminance should be approx 100", Math.abs(hslColor.getLuminence() - 100) < 2);
    }

    @Test
    public void testInitHSLbyRGB_RedDominant() {
        // Caso cMax == R
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Hue per rosso puro è 0 (o vicino a 0/255 in HSL)
        assertEquals(0, hslColor.getHue()); 
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence()); // (255+0)/2 approx
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        // Caso cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        
        // Hue = HSLMAX / 3 = 85
        assertEquals(85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        // Caso cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        
        // Hue = 2 * HSLMAX / 3 = 170
        assertEquals(170, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueCorrection() {
        // Per forzare Hue < 0 prima della correzione, serve cMax == R e B > G.
        // Formula: pHue = BDelta - GDelta.
        // Se R=Max, B > G allora (Max-B) < (Max-G) -> BDelta < GDelta -> Result negativo.
        hslColor.initHSLbyRGB(200, 10, 100);
        
        // Verifichiamo che sia stato normalizzato (positivo)
        assertTrue("Hue should be positive", hslColor.getHue() >= 0);
        assertEquals(233, hslColor.getHue()); // Valore calcolato atteso
    }

    @Test
    public void testInitHSLbyRGB_LowLuminanceSaturation() {
        // Ramo: pLum <= (HSLMAX / 2)
        hslColor.initHSLbyRGB(10, 20, 30);
        assertTrue("Luminance should be low", hslColor.getLuminence() <= 127);
        assertTrue("Saturation should be calculated", hslColor.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_HighLuminanceSaturation() {
        // Ramo: pLum > (HSLMAX / 2)
        hslColor.initHSLbyRGB(200, 220, 240);
        assertTrue("Luminance should be high", hslColor.getLuminence() > 127);
        assertTrue("Saturation should be calculated", hslColor.getSaturation() > 0);
    }

    // --- TEST HSL -> RGB ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // S = 0
        hslColor.initRGBbyHSL(0, 0, 100);
        
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLuminance() {
        // L <= HSLMAX / 2. Magic2 = (L * (HSLMAX + S) + ...)
        hslColor.initRGBbyHSL(0, 255, 60); // Rosso scuro saturo
        
        // Verifica che RGB siano stati calcolati
        assertTrue(hslColor.getRed() > 0);
        assertEquals(0, hslColor.getGreen()); // Rosso puro scuro
        assertEquals(0, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_HighLuminance() {
        // L > HSLMAX / 2. Magic2 = L + S - ...
        hslColor.initRGBbyHSL(0, 255, 200); // Rosso chiaro saturo
        
        assertTrue(hslColor.getRed() > 200);
        assertTrue(hslColor.getGreen() > 0); // Diventa rosino/bianco
        assertTrue(hslColor.getBlue() > 0);
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_Branches() {
        // hueToRGB ha 4 rami basati sul valore di Hue normalizzato
        // 1. Hue < HSLMAX/6
        // 2. Hue < HSLMAX/2
        // 3. Hue < HSLMAX*2/3
        // 4. else
        
        // Usando H=127 (Cyan/Greenish) e S=255, L=128 attiviamo rami diversi per R, G, B
        // perché initRGBbyHSL chiama hueToRGB con H+85, H, H-85.
        hslColor.initRGBbyHSL(127, 255, 128);
        
        // R: H+85 = 212 (> 170, Ramo 4) -> Minimo (0)
        // G: H = 127 (Ramo 3 limite o Ramo 2) -> Massimo (255)
        // B: H-85 = 42 (Ramo 2) -> Massimo (255)
        
        assertEquals(0, hslColor.getRed());
        assertEquals(255, hslColor.getGreen()); 
        assertEquals(255, hslColor.getBlue());
    }
    
    @Test
    public void testInitRGBbyHSL_HueWrap() {
        // Test hueToRGB Hue < 0 e Hue > HSLMAX logic
        // Impostiamo un HSL che costringe hueToRGB a gestire overflow/underflow
        // H=0 -> Blue component chiama con H - 85 = -85 (Underflow check)
        hslColor.initRGBbyHSL(0, 255, 128);
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen());
        assertEquals(0, hslColor
}

						