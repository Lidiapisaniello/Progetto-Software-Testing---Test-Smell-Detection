/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: ange.dalia@studenti.unina.it
UserID: 127
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
	// Costanti replicate dalla classe per le asserzioni
	private final static int HSLMAX = 255;
	private final static int RGBMAX = 255;
	private final static int UNDEFINED = 170;

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
		// Inizializza l'oggetto prima di ogni test
		hslColor = new HSLColor();
	}
				
	@After
	public void tearDown() {
		// Pulizia
	}

	// --- TEST INIT HSL BY RGB (COPERTURA RAMI PRINCIPALI) ---

	@Test
	public void testInitHSLbyRGB_RedDominant() {
		// Max=R, Min=G/B (Caso cMax == R)
		hslColor.initHSLbyRGB(255, 0, 0);
		assertEquals("Red Hue", 0, hslColor.getHue());
		assertEquals("Red Sat", 255, hslColor.getSaturation());
		assertEquals("Red Lum", 128, hslColor.getLuminence());
		assertEquals(255, hslColor.getRed()); // Verifica stato interno
	}

	@Test
	public void testInitHSLbyRGB_GreenDominant() {
		// Max=G (Caso cMax == G)
		hslColor.initHSLbyRGB(0, 255, 0);
		// Hue calcolato: (255/3) + ... = 85
		assertEquals("Green Hue", 85, hslColor.getHue()); 
		assertEquals("Green Sat", 255, hslColor.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_BlueDominant() {
		// Max=B (Caso cMax == B)
		hslColor.initHSLbyRGB(0, 0, 255);
		// Hue calcolato: (2*255/3) + ... = 170
		assertEquals("Blue Hue", 170, hslColor.getHue());
		assertEquals("Blue Sat", 255, hslColor.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_Greyscale_Black() {
		// Caso cMax == cMin (R=G=B=0)
		hslColor.initHSLbyRGB(0, 0, 0);
		assertEquals("Black Hue", UNDEFINED, hslColor.getHue());
		assertEquals("Black Sat", 0, hslColor.getSaturation());
		assertEquals("Black Lum", 0, hslColor.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Greyscale_White() {
		// Caso cMax == cMin (R=G=B=255)
		hslColor.initHSLbyRGB(255, 255, 255);
		assertEquals("White Hue", UNDEFINED, hslColor.getHue());
		assertEquals("White Sat", 0, hslColor.getSaturation());
		assertEquals("White Lum", 255, hslColor.getLuminence());
	}

	// --- TEST BOUNDARY & LOGICA MATEMATICA (WEAK MUTATION) ---

	@Test
	public void testLuminenceBranching() {
		// Testa il ramo if (pLum <= (HSLMAX / 2)) vs else
		// HSLMAX/2 = 127.
		
		// Caso Lum <= 127
		hslColor.initHSLbyRGB(50, 0, 0);
		assertTrue("Lum dovrebbe essere <= 127", hslColor.getLuminence() <= 127);
		
		// Caso Lum > 127 (Un colore molto chiaro)
		hslColor.initHSLbyRGB(255, 200, 200);
		assertTrue("Lum dovrebbe essere > 127", hslColor.getLuminence() > 127);
	}

	@Test
	public void testHueWrapNegativeInCalc() {
		// Questo test cerca di forzare pHue < 0 all'interno di initHSLbyRGB prima del fix finale.
		// Richiede Max=R e BDelta > GDelta (quindi B molto più distante dal Max rispetto a G, o viceversa logica delta)
		// R=255, G=0, B=100 -> Hue risultante deve essere wrappato positivo.
		hslColor.initHSLbyRGB(255, 0, 100); 
		assertTrue("Hue deve essere positivo dopo il wrap", hslColor.getHue() >= 0);
		assertTrue("Hue deve essere < 255", hslColor.getHue() <= 255);
	}

	// --- TEST INIT RGB BY HSL (REVERSE) ---

	@Test
	public void testInitRGBbyHSL_Greyscale() {
		// Se Saturation == 0
		hslColor.initRGBbyHSL(100, 0, 128);
		assertEquals(128, hslColor.getRed());
		assertEquals(128, hslColor.getGreen());
		assertEquals(128, hslColor.getBlue());
	}

	@Test
	public void testInitRGBbyHSL_Colors() {
		// Testa il ramo Saturation > 0 e Luminance <= HSLMAX/2
		hslColor.initRGBbyHSL(0, 255, 100); // Rosso scuro saturo
		assertTrue(hslColor.getRed() > 0);
		assertEquals(0, hslColor.getGreen());
		assertEquals(0, hslColor.getBlue());

		// Testa il ramo Luminance > HSLMAX/2
		hslColor.initRGBbyHSL(0, 255, 200); // Rosso chiaro saturo
		assertTrue(hslColor.getRed() > 200);
		assertTrue(hslColor.getGreen() > 100); // Il bianco "invade"
	}

	@Test
	public void testHueToRGB_InternalLogic() {
		// Questo test mira a coprire i rami del metodo privato hueToRGB
		// Hue < HSLMAX/6 (42)
		hslColor.initRGBbyHSL(20, 255, 128);
		
		// Hue < HSLMAX/2 (127) ma > 42
		hslColor.initRGBbyHSL(85, 255, 128);
		
		// Hue < HSLMAX*2/3 (170) ma > 127
		hslColor.initRGBbyHSL(150, 255, 128);
		
		// Hue > 170
		hslColor.initRGBbyHSL(200, 255, 128);
		
		// Verifica base che non crashi e produca RGB validi
		assertTrue(hslColor.getRed() >= 0 && hslColor.getRed() <= 255);
	}
	
	@Test
	public void testHueToRGB_Clamping() {
		// Verifica che initRGBbyHSL gestisca il clamping se i valori calcolati superano 255
		// (Difficile da forzare con logica intera corretta, ma testiamo i limiti)
		hslColor.initRGBbyHSL(0, 255, 128); 
		assertTrue(hslColor.getRed() <= 255);
	}

	// --- TEST SETTERS & UTILS ---

	@Test
	public void testSetHue_Loops() {
		hslColor.initHSLbyRGB(255, 0, 0);
		
		// Test wrap under (while < 0)
		hslColor.setHue(-300); // -300 + 255 = -45 + 255 = 210
		assertTrue(hslColor.getHue() >= 0);
		
		// Test wrap over (while > 255)
		hslColor.setHue(600); // 600 - 255 - 255 = 90
		assertTrue(hslColor.getHue() <= 255);
	}

	@Test
	public void testSetSaturation_Clamp() {
		hslColor.initHSLbyRGB(255, 0, 0);
		
		hslColor.setSaturation(-50);
		assertEquals(0, hslColor.getSaturation());
		
		hslColor.setSaturation(300);
		assertEquals(255, hslColor.getSaturation());
	}

	@Test
	public void testSetLuminence_Clamp() {
		hslColor.initHSLbyRGB(255, 0, 0);
		
		hslColor.setLuminence(-10);
		assertEquals(0, hslColor.getLuminence());
		
		hslColor.setLuminence(500);
		assertEquals(255, hslColor.getLuminence());
	}

	@Test
	public void testReverseColor() {
		hslColor.setHue(0);
		hslColor.reverseColor();
		// 0 + 127 = 127
		assertEquals(127, hslColor.getHue());
	}
	
	@Test
	public void testReverseLight() {
		hslColor.setLuminence(100);
		// Metodo private reverseLight non è accessibile direttamente,
		// ma possiamo testare se c'è un metodo pubblico che lo usa?
		// No, reverseLight è private e non usato da metodi pubblici standard a parte reverseLight stesso.
		// Se la classe non espone reverseLight, non possiamo testarlo direttamente senza Reflection.
		// Tuttavia, leggendo il codice fornito, reverseLight è private e NON è chiamato da nessuno. 
		// Se è Dead Code, non impatta la coverage pubblica. 
		// Se invece fosse public o usato, lo testeremmo.
		// Ignoriamo per ora, a meno che non si voglia usare Reflection.
	}

	@Test
	public void testBrighten() {
		hslColor.initHSLbyRGB(100, 100, 100); // L=100
		
		hslColor.brighten(0.0f);
		assertEquals(100, hslColor.getLuminence()); // No change
		
		hslColor.brighten(1.5f);
		assertEquals(150, hslColor.getLuminence());
		
		// Test Max Clamp
		hslColor.brighten(10.0f);
		assertEquals(255, hslColor.getLuminence());
		
		// Test Min check logic (L < 0) - difficile da ottenere moltiplicando per float positivo,
		// ma se passiamo negativo?
		hslColor.setLuminence(100);
		hslColor.brighten(-0.5f); // Diventa negativo, clamp a 0
		assertEquals(0, hslColor.getLuminence());
	}

	@Test
	public void testBlend() {
		hslColor.initHSLbyRGB(0, 0, 0); // Start Black
		
		// Blend 100% -> Nuovo colore
		hslColor.blend(255, 255, 255, 1.0f);
		assertEquals(255, hslColor.getRed());
		
		// Blend 0% -> Vecchio colore
		hslColor.initHSLbyRGB(0, 0, 0);
		hslColor.blend(255, 255, 255, 0.0f);
		assertEquals(0, hslColor.getRed());
		
		// Blend 50%
		hslColor.initHSLbyRGB(0, 0, 0);
		hslColor.blend(200, 200, 200, 0.5f);
		assertEquals(100, hslColor.getRed());
	}
	
	@Test
	public void testGetters() {
		// Coverage banale per i getter
		hslColor.initHSLbyRGB(10, 20, 30);
		assertEquals(10, hslColor.getRed());
		assertEquals(20, hslColor.getGreen());
		assertEquals(30, hslColor.getBlue());
	}
}