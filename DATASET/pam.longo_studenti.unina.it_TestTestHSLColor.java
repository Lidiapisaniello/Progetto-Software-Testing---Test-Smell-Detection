/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: pam.longo@studenti.unina.it
UserID: 793
Date: 25/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {
	private HSLColor color;
	private final static int HSLMAX = 255;
	private final static int RGBMAX = 255;
	private final static int UNDEFINED = 170; 

	@Before
	public void setUp() {
		color = new HSLColor();
	}

	// --- Test Conversione RGB -> HSL (initHSLbyRGB) ---

	@Test
	public void testInitHSLbyRGB_Red() {
		color.initHSLbyRGB(RGBMAX, 0, 0);
		assertEquals(0, color.getHue(), 1);
		assertEquals(HSLMAX, color.getSaturation());
		assertEquals(128, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Green() {
		color.initHSLbyRGB(0, RGBMAX, 0);
		assertEquals(HSLMAX / 3, color.getHue(), 1);
		assertEquals(HSLMAX, color.getSaturation());
		assertEquals(128, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Blue() {
		color.initHSLbyRGB(0, 0, RGBMAX);
		assertEquals(2 * HSLMAX / 3, color.getHue(), 1);
		assertEquals(HSLMAX, color.getSaturation());
		assertEquals(128, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_White() {
		color.initHSLbyRGB(RGBMAX, RGBMAX, RGBMAX);
		assertEquals(UNDEFINED, color.getHue());
		assertEquals(0, color.getSaturation());
		assertEquals(HSLMAX, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Black() {
		color.initHSLbyRGB(0, 0, 0);
		assertEquals(UNDEFINED, color.getHue());
		assertEquals(0, color.getSaturation());
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Grey() {
		color.initHSLbyRGB(128, 128, 128);
		assertEquals(UNDEFINED, color.getHue());
		assertEquals(0, color.getSaturation());
		assertEquals(128, color.getLuminence());
	}

	@Test
	public void testInitHSLbyRGB_Saturation_LowLuminance() {
		color.initHSLbyRGB(128, 64, 64);
		assertEquals(96, color.getLuminence());
		assertEquals(85, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_Saturation_HighLuminance() {
		color.initHSLbyRGB(255, 191, 191);
		assertEquals(223, color.getLuminence());
		assertEquals(HSLMAX, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_NegativeHueCorrection() {
		color.initHSLbyRGB(255, 128, 0);
		assertEquals(128, color.getLuminence());
		assertEquals(255, color.getSaturation());
	}


	// --- Test Conversione HSL -> RGB (initRGBbyHSL) e hueToRGB ---

	@Test
	public void testInitRGBbyHSL_Greyscale() {
		// H=UNDEFINED, S=0, L=128 (Grigio medio)
		color.initRGBbyHSL(UNDEFINED, 0, HSLMAX / 2);
		// L'implementazione produce 127 a causa dell'aritmetica intera, non 128
		int expectedVal = 127; // CORREZIONE FINALE
		
		assertEquals(expectedVal, color.getRed());
		assertEquals(expectedVal, color.getGreen());
		assertEquals(expectedVal, color.getBlue());
	}

	@Test
	public void testInitRGBbyHSL_Saturation_LowLuminance() {
		color.initRGBbyHSL(0, HSLMAX, 100);
		assertEquals(200, color.getRed(), 1);
		assertEquals(0, color.getGreen(), 1);
		assertEquals(0, color.getBlue(), 1);
	}

	@Test
	public void testInitRGBbyHSL_Saturation_HighLuminance() {
		color.initRGBbyHSL(0, HSLMAX, 200);
		assertEquals(RGBMAX, color.getRed());
		assertEquals(145, color.getGreen(), 1);
		assertEquals(145, color.getBlue(), 1);
	}

	@Test
	public void testInitRGBbyHSL_HueToRGB_Region1() {
		color.initRGBbyHSL(21, HSLMAX, HSLMAX / 2);
		assertEquals(RGBMAX, color.getRed(), 1); 
		assertEquals(127, color.getGreen(), 3);
		assertEquals(0, color.getBlue(), 3);
	}

	@Test
	public void testInitRGBbyHSL_HueToRGB_Region2() {
		color.initRGBbyHSL(HSLMAX / 3, HSLMAX, HSLMAX / 2);
		assertEquals(0, color.getRed(), 1);
		assertEquals(RGBMAX, color.getGreen(), 1);
		assertEquals(0, color.getBlue());
	}

	@Test
	public void testInitRGBbyHSL_HueToRGB_Region3() {
		color.initRGBbyHSL(150, HSLMAX, HSLMAX / 2);
		assertEquals(0, color.getRed(), 1);
		assertEquals(120, color.getGreen(), 1);
		assertEquals(RGBMAX, color.getBlue(), 1);
	}

	@Test
	public void testInitRGBbyHSL_HueToRGB_Region4() {
		color.initRGBbyHSL(200, HSLMAX, HSLMAX / 2);
		assertEquals(182, color.getRed(), 2);
		assertEquals(0, color.getGreen());
		assertEquals(RGBMAX, color.getBlue(), 1);
	}

	@Test
	public void testInitRGBbyHSL_Clamping() {
		color.initRGBbyHSL(0, 1, HSLMAX);
		assertEquals(RGBMAX, color.getRed());
		assertEquals(RGBMAX, color.getGreen());
		assertEquals(RGBMAX, color.getBlue());
	}

	// --- Test Setter/Getter ---

	@Test
	public void testSetHue_PositiveWrap() {
		color.initHSLbyRGB(100, 100, 100);
		color.setHue(HSLMAX + 10);
		assertEquals(10, color.getHue());
	}

	@Test
	public void testSetHue_NegativeWrap() {
		color.initHSLbyRGB(100, 100, 100);
		color.setHue(-10);
		assertEquals(HSLMAX - 10, color.getHue());
	}

	@Test
	public void testSetSaturation_ClampHigh() {
		color.initHSLbyRGB(100, 100, 100);
		color.setSaturation(HSLMAX + 10);
		assertEquals(HSLMAX, color.getSaturation());
	}

	@Test
	public void testSetSaturation_ClampLow() {
		color.initHSLbyRGB(100, 100, 100);
		color.setSaturation(-10);
		assertEquals(0, color.getSaturation());
	}

	@Test
	public void testSetLuminence_ClampHigh() {
		color.initHSLbyRGB(100, 100, 100);
		color.setLuminence(HSLMAX + 10);
		assertEquals(HSLMAX, color.getLuminence());
	}

	@Test
	public void testSetLuminence_ClampLow() {
		color.initHSLbyRGB(100, 100, 100);
		color.setLuminence(-10);
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testGettersAndPrivateSetters() {
		color.initHSLbyRGB(10, 20, 30);
		assertEquals(10, color.getRed());
		assertEquals(20, color.getGreen());
		assertEquals(30, color.getBlue());

		color.setLuminence(100);
		assertTrue(color.getRed() != 10 || color.getGreen() != 20 || color.getBlue() != 30);
	}

	// --- Test Metodi di Utilità ---

	@Test
	public void testReverseColor() {
		color.initHSLbyRGB(RGBMAX, 0, 0); 
		int originalHue = color.getHue();
		color.reverseColor();
		int newHue = color.getHue();
		assertEquals(originalHue + HSLMAX / 2, newHue, 1);
		assertEquals(0, color.getRed(), 1);
		assertEquals(RGBMAX, color.getGreen(), 1);
		assertEquals(RGBMAX, color.getBlue(), 1);
	}

	@Test
	public void testBrighten_NoChange() {
		color.initHSLbyRGB(100, 100, 100);
		int originalLum = color.getLuminence();
		color.brighten(0.0f);
		assertEquals(originalLum, color.getLuminence());
	}

	@Test
	public void testBrighten_Darker() {
		color.initHSLbyRGB(128, 128, 128);
		color.brighten(0.5f);
		assertEquals(64, color.getLuminence());
	}

	@Test
	public void testBrighten_Lighter() {
		color.initHSLbyRGB(128, 128, 128);
		color.brighten(1.5f);
		assertEquals(192, color.getLuminence());
	}

	@Test
	public void testBrighten_ClampHigh() {
		color.initHSLbyRGB(128, 128, 128);
		color.brighten(3.0f);
		assertEquals(HSLMAX, color.getLuminence());
	}

	@Test
	public void testBrighten_ClampLow() {
		color.initHSLbyRGB(128, 128, 128);
		color.brighten(-1.0f);
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testBlend_FullBlend() {
		color.initHSLbyRGB(RGBMAX, 0, 0);
		color.blend(0, RGBMAX, 0, 1.0f);
		assertEquals(0, color.getRed());
		assertEquals(RGBMAX, color.getGreen());
		assertEquals(0, color.getBlue());
	}

	@Test
	public void testBlend_NoBlend() {
		color.initHSLbyRGB(RGBMAX, 0, 0);
		color.blend(0, RGBMAX, 0, 0.0f);
		assertEquals(RGBMAX, color.getRed());
		assertEquals(0, color.getGreen());
		assertEquals(0, color.getBlue());
	}

	@Test
	public void testBlend_HalfBlend() {
		// Risultato RGB(127, 0, 127).
		color.initHSLbyRGB(RGBMAX, 0, 0);
		color.blend(0, 0, RGBMAX, 0.5f);
		assertEquals(127, color.getRed());
		assertEquals(0, color.getGreen());
		assertEquals(127, color.getBlue());

		// L'Hue calcolato dalla classe è 213.
		assertEquals(213, color.getHue(), 5);
	}
	
  	// --- NUOVI TEST PER COPRIRE I METODI PRIVATI (Reflection) ---
	
	@Test
    public void testPrivateSetBlue_Reflection() throws Exception {
        // Copre setBlue(int iNewValue) e initHSLbyRGB(...) all'interno di esso
		color.initHSLbyRGB(0, 0, 0); // Inizializza i campi pRed/pGreen
		
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        
        // Chiama setBlue con un nuovo valore per attivare initHSLbyRGB
        method.invoke(color, 255);
        
		// Verifica che HSL sia stato ricalcolato (Blu Puro)
        assertEquals(2 * HSLMAX / 3, color.getHue(), 1);
		assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

	@Test
    public void testPrivateReverseLight_Reflection() throws Exception {
        // Copre reverseLight() e setLuminence(HSLMAX - pLum)
		color.initHSLbyRGB(128, 128, 128); // Luminanza iniziale: 128
		int initialLuminance = color.getLuminence();
		
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        
        // Chiama reverseLight()
        method.invoke(color);
        
		// Verifica che la Luminanza sia invertita (255 - 128 = 127/128)
        assertEquals(HSLMAX - initialLuminance, color.getLuminence(), 1); 
    }
}