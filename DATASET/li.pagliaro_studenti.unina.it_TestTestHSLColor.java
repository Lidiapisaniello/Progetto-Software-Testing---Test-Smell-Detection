/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Livia
Cognome: Pagliaro
Username: li.pagliaro@studenti.unina.it
UserID: 125
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {
  private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;
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
      hslColor = new HSLColor();
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
  // ---------------------------------------------------------
    // 1. Test Conversione RGB -> HSL (initHSLbyRGB)
    // ---------------------------------------------------------

    @Test
    public void testInitHSLbyRGB_Red() {
        // Ramo: cMax == R
        hslColor.initHSLbyRGB(255, 0, 0);
        assertEquals(0, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence()); // (255+0)/2 = 127.5 -> 127 integer
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        // Ramo: cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue atteso: HSLMAX / 3 = 85
        assertEquals(85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        // Ramo: cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue atteso: (2 * HSLMAX) / 3 = 170
        assertEquals(170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Ramo: cMax == cMin
        hslColor.initHSLbyRGB(128, 128, 128);
        assertEquals(UNDEFINED, hslColor.getHue());
        assertEquals(0, hslColor.getSaturation());
        assertEquals(128, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_LuminenceBranches() {
        // Test per rami: if (pLum <= (HSLMAX / 2))
        
        // Bassa luminosità
        hslColor.initHSLbyRGB(50, 50, 50); 
        assertTrue(hslColor.getLuminence() <= HSLMAX / 2);

        // Alta luminosità
        hslColor.initHSLbyRGB(200, 200, 200);
        assertTrue(hslColor.getLuminence() > HSLMAX / 2);
    }

    // ---------------------------------------------------------
    // 2. Test Conversione HSL -> RGB (initRGBbyHSL)
    // ---------------------------------------------------------

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Ramo: S == 0
        hslColor.initRGBbyHSL(100, 0, 128);
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_NormalColors() {
        // Test Round-trip: RGB -> HSL -> RGB
        // Usiamo un colore arbitrario
        int r = 100, g = 150, b = 200;
        
        hslColor.initHSLbyRGB(r, g, b);
        int h = hslColor.getHue();
        int s = hslColor.getSaturation();
        int l = hslColor.getLuminence();

        // Reinizializza usando i valori calcolati
        hslColor.initRGBbyHSL(h, s, l);

        // Tolleranza delta di 1 o 2 è necessaria a causa delle divisioni intere
        assertEquals(r, hslColor.getRed(), 2);
        assertEquals(g, hslColor.getGreen(), 2);
        assertEquals(b, hslColor.getBlue(), 2);
    }

    @Test
    public void testHueToRGB_Coverage() {
        // Questo test mira a coprire i diversi "zone" della funzione hueToRGB
        // Zone: < 1/6, < 1/2, < 2/3, else
        
        // Hue basso (Rosso-Giallo) -> < 1/6
        hslColor.initRGBbyHSL(20, 255, 128); 
        assertTrue(hslColor.getRed() > 0);

        // Hue medio (Verde-Ciano) -> < 1/2
        hslColor.initRGBbyHSL(100, 255, 128);
        assertTrue(hslColor.getGreen() > 0);

        // Hue medio-alto (Blu) -> < 2/3
        hslColor.initRGBbyHSL(160, 255, 128);
        assertTrue(hslColor.getBlue() > 0);
        
        // Hue alto (Viola) -> else
        hslColor.initRGBbyHSL(240, 255, 128);
    }
    
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // Forza il calcolo RGB a superare i limiti per testare: if (pRed > RGBMAX)
        // Impostando Lum e Sat al massimo con diverse Hue
        hslColor.initRGBbyHSL(0, 255, 255); // Bianco totale
        assertEquals(255, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        assertEquals(255, hslColor.getBlue());
    }

    // ---------------------------------------------------------
    // 3. Test Setters, Getters e Logica di Wrapping
    // ---------------------------------------------------------

    @Test
    public void testSetHue_Wrapping() {
        // Test while (iToValue < 0)
        hslColor.initHSLbyRGB(255, 0, 0); // Hue = 0
        hslColor.setHue(-10); 
        // -10 + 255 = 245
        assertEquals(245, hslColor.getHue());

        // Test while (iToValue > HSLMAX)
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(500);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(-50);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(500);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    // ---------------------------------------------------------
    // 4. Test Metodi di Utilità (Brighten, Blend, Reverse)
    // ---------------------------------------------------------

    @Test
    public void testReverseColor() {
        hslColor.initHSLbyRGB(255, 0, 0); // Hue 0
        hslColor.reverseColor();
        // 0 + 127 (HSLMAX/2 arrotondato) = 127
        assertEquals(HSLMAX/2, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.initHSLbyRGB(100, 100, 100);
        int initialLum = hslColor.getLuminence();
        
        // Aumenta luminosità
        hslColor.brighten(1.2f); 
        assertTrue(hslColor.getLuminence() > initialLum);

        // Test 0 percent (return immediato)
        int lumAfter = hslColor.getLuminence();
        hslColor.brighten(0.0f);
        assertEquals(lumAfter, hslColor.getLuminence());
        
        // Test Overflow
        hslColor.brighten(10.0f);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); // Nero
        
        // Blend 50% con Bianco
        hslColor.blend(255, 255, 255, 0.5f);
        
        // Dovrebbe essere grigio medio (~127)
        assertEquals(127, hslColor.getRed(), 1);
        
        // Test limiti percentuale
        hslColor.blend(255, 0, 0, 1.5f); // > 1
        assertEquals(255, hslColor.getRed()); // Diventa tutto rosso

        hslColor.blend(0, 255, 0, -0.5f); // < 0
        assertEquals(255, hslColor.getRed()); // Resta invariato (Rosso)
    }

    // ---------------------------------------------------------
    // 5. Test Reflection per metodi privati (Massimizzazione Copertura)
    // ---------------------------------------------------------
    
    @Test
    public void testPrivateMethods_SetRedGreenBlue() throws Exception {
        // Poiché setRed/Green/Blue sono privati e non usati internamente,
        // usiamo la reflection per testarli e aumentare la "line coverage".
        
        Method setRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRed.setAccessible(true);
        
        Method setGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreen.setAccessible(true);

        Method setBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlue.setAccessible(true);

        // Test setRed
        hslColor.initHSLbyRGB(0, 0, 0);
        setRed.invoke(hslColor, 255);
        assertEquals(255, hslColor.getRed());
        
        // Test setGreen
        setGreen.invoke(hslColor, 255);
        assertEquals(255, hslColor.getGreen());

        // Test setBlue
        setBlue.invoke(hslColor, 255);
        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void testPrivateMethod_Greyscale() throws Exception {
        hslColor.initHSLbyRGB(255, 0, 0); // Rosso
        
        Method greyscale = HSLColor.class.getDeclaredMethod("greyscale");
        greyscale.setAccessible(true);
        greyscale.invoke(hslColor);
        
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testPrivateMethod_ReverseLight() throws Exception {
        hslColor.initHSLbyRGB(50, 50, 50); // Grigio scuro
        int oldLum = hslColor.getLuminence();
        
        Method reverseLight = HSLColor.class.getDeclaredMethod("reverseLight");
        reverseLight.setAccessible(true);
        reverseLight.invoke(hslColor);
        
        assertEquals(HSLMAX - oldLum, hslColor.getLuminence());
    }
}

						