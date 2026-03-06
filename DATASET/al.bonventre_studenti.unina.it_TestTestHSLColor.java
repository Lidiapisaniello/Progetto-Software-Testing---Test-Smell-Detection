/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: al.bonventre@studenti.unina.it
UserID: 183
Date: 25/11/2025
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
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
	}
				
	@Before
	public void setUp() {
		// Eseguito prima di ogni metodo di test
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
	}
				
	// --- Tests for initHSLbyRGB (RGB -> HSL) ---

	@Test
	public void testInitHSLbyRGB_Greyscale_Sat() {
		// Path: cMax == cMin (R=G=B) -> Saturation should be 0
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(100, 100, 100);
		assertEquals(0, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_Greyscale_Hue() {
		// Path: cMax == cMin (R=G=B) -> Hue should be UNDEFINED (170)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(120, 120, 120);
		assertEquals(170, color.getHue());
	}

	@Test
	public void testInitHSLbyRGB_LowLum_Sat() {
		// Path: Not Greyscale, Lum <= HSLMAX/2 (127)
		// R=50, G=0, B=0 -> Lum approx 25 -> Sat formula 1
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(50, 0, 0);
		// cMinus=50, cPlus=50 -> Sat = (50*255 + 0.5)/50 = 255
		assertEquals(255, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_HighLum_Sat() {
		// Path: Not Greyscale, Lum > HSLMAX/2
		// R=255, G=200, B=200 -> Lum approx 227 -> Sat formula 2
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(255, 200, 200);
		// cMinus=55, cPlus=455. 2*255 - 455 = 55. Sat = 255.
		assertEquals(255, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_RedMax_Hue() {
		// Path: cMax == R (Red dominant) -> Hue calculated from B-G
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(200, 50, 50);
		assertEquals(0, color.getHue());
	}

	@Test
	public void testInitHSLbyRGB_GreenMax_Hue() {
		// Path: cMax == G (Green dominant)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(50, 200, 50);
		// Expect Hue ~85 (HSLMAX/3)
		assertEquals(85, color.getHue());
	}

	@Test
	public void testInitHSLbyRGB_BlueMax_Hue() {
		// Path: cMax == B (Blue dominant)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(50, 50, 200);
		// Expect Hue ~170 (2*HSLMAX/3)
		assertEquals(170, color.getHue());
	}

	@Test
	public void testInitHSLbyRGB_NegativeHueAdjustment() {
		// Path: cMax == R, but B > G -> Delta creates negative result
		// Logic: if pHue < 0 then pHue += HSLMAX
		HSLColor color = new HSLColor();
		// R=255, G=0, B=100.
		color.initHSLbyRGB(255, 0, 100);
		// Hue calc results in negative, then wraps +255.
		// Result should be valid positive Hue.
		assertTrue(color.getHue() > 0);
	}

	// --- Tests for initRGBbyHSL (HSL -> RGB) ---

	@Test
	public void testInitRGBbyHSL_Greyscale_R() {
		// Path: Saturation == 0
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 0, 100); // Hue undefined, Sat 0, Lum 100
		// R, G, B should equal normalized Lum
		assertEquals(100, color.getRed());
	}

	@Test
	public void testInitRGBbyHSL_Greyscale_G() {
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 0, 100);
		assertEquals(100, color.getGreen());
	}

	@Test
	public void testInitRGBbyHSL_Greyscale_B() {
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 0, 100);
		assertEquals(100, color.getBlue());
	}

	@Test
	public void testInitRGBbyHSL_LowLum_Magic2() {
		// Path: Sat != 0, Lum <= HSLMAX/2
		// This affects Magic2 calculation
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 255, 100); // Full Red, Lum 100
		// Verify Red component is set correctly (high)
		assertEquals(200, color.getRed());
	}

	@Test
	public void testInitRGBbyHSL_HighLum_Magic2() {
		// Path: Sat != 0, Lum > HSLMAX/2
		// This affects Magic2 calculation differently
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 255, 200); // Full Red, High Lum
		// Verify Red is high
		assertEquals(244, color.getRed());
	}

	@Test
	public void testInitRGBbyHSL_ClampRed() {
		// Path: pRed > RGBMAX check. 
		// Trying to force overflow (difficult with integer math constraints but theoretical path)
		HSLColor color = new HSLColor();
		// Set generic values, mainly testing the code executes the clamp check
		color.initRGBbyHSL(0, 255, 255); // White
		assertEquals(255, color.getRed());
	}

	// --- Indirect Tests for hueToRGB (Private) via initRGBbyHSL ---
	// hueToRGB is called for R, G, B with shifted Hue values.
	// We vary Hue input to initRGBbyHSL to hit internal branches of hueToRGB.

	@Test
	public void testHueToRGB_Region1() {
		// hueToRGB Region: Hue < HSLMAX/6 (42.5)
		// We focus on Green component. Green calls hueToRGB with exact H.
		// Input H = 20. 20 < 42.5.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(20, 255, 127);
		// Expect value calculated by first formula
		assertTrue(color.getGreen() > 0);
	}

	@Test
	public void testHueToRGB_Region2() {
		// hueToRGB Region: HSLMAX/6 <= Hue < HSLMAX/2 (42.5 to 127.5)
		// Input H = 85 (Green). 85 is in range.
		// Returns Magic2.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(85, 255, 127);
		assertEquals(254, color.getGreen());
	}

	@Test
	public void testHueToRGB_Region3() {
		// hueToRGB Region: HSLMAX/2 <= Hue < 2*HSLMAX/3 (127.5 to 170)
		// Input H = 150.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(150, 255, 127);
		assertTrue(color.getGreen() > 0);
	}

	@Test
	public void testHueToRGB_Region4() {
		// hueToRGB Region: Hue >= 2*HSLMAX/3 (170)
		// Input H = 200.
		// Returns Magic1.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(200, 255, 127);
		assertEquals(0, color.getGreen());
	}

	@Test
	public void testHueToRGB_Underflow() {
		// hueToRGB handles Hue < 0.
		// initRGBbyHSL calls Blue with (H - HSLMAX/3).
		// If H=0, called with -85.
		// hueToRGB adds HSLMAX.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(0, 255, 127);
		assertEquals(0, color.getBlue());
	}

	@Test
	public void testHueToRGB_Overflow() {
		// hueToRGB handles Hue > HSLMAX.
		// initRGBbyHSL calls Red with (H + HSLMAX/3).
		// If H=200, called with 285.
		// hueToRGB subtracts HSLMAX.
		HSLColor color = new HSLColor();
		color.initRGBbyHSL(200, 255, 127);
		// Red component should be calculated correctly after wrap
		assertTrue(color.getRed() > 0);
	}

	// --- Getters & Setters ---

	@Test
	public void testSetHue_Normal() {
		HSLColor color = new HSLColor();
		color.setHue(100);
		assertEquals(100, color.getHue());
	}

	@Test
	public void testSetHue_UnderflowLoop() {
		// while (iToValue < 0) iToValue += HSLMAX
		HSLColor color = new HSLColor();
		color.setHue(-300); // -300 + 255 = -45 + 255 = 210
		assertEquals(210, color.getHue());
	}

	@Test
	public void testSetHue_OverflowLoop() {
		// while (iToValue > HSLMAX) iToValue -= HSLMAX
		HSLColor color = new HSLColor();
		color.setHue(600); // 600 - 255 = 345 - 255 = 90
		assertEquals(90, color.getHue());
	}

	@Test
	public void testSetSaturation_ClampLow() {
		HSLColor color = new HSLColor();
		color.setSaturation(-10);
		assertEquals(0, color.getSaturation());
	}

	@Test
	public void testSetSaturation_ClampHigh() {
		HSLColor color = new HSLColor();
		color.setSaturation(300);
		assertEquals(255, color.getSaturation());
	}

	@Test
	public void testSetLuminence_ClampLow() {
		HSLColor color = new HSLColor();
		color.setLuminence(-50);
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testSetLuminence_ClampHigh() {
		HSLColor color = new HSLColor();
		color.setLuminence(500);
		assertEquals(255, color.getLuminence());
	}

	// --- Utility Methods ---

	@Test
	public void testReverseColor() {
		HSLColor color = new HSLColor();
		color.setHue(0);
		color.reverseColor(); // Adds HSLMAX/2 (127)
		assertEquals(127, color.getHue());
	}

	@Test
	public void testBrighten_ZeroPercent() {
		// if (fPercent == 0) return
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(100, 100, 100); // L=100
		color.brighten(0.0f);
		assertEquals(100, color.getLuminence());
	}

	@Test
	public void testBrighten_Normal() {
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(100, 100, 100); // L=100
		color.brighten(1.5f); // 100 * 1.5 = 150
		assertEquals(150, color.getLuminence());
	}

	@Test
	public void testBrighten_ClampLow() {
		// if (L < 0)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(100, 100, 100);
		color.brighten(-1.0f); // Negative percent
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testBrighten_ClampHigh() {
		// if (L > HSLMAX)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(100, 100, 100);
		color.brighten(5.0f); // 500
		assertEquals(255, color.getLuminence());
	}

	@Test
	public void testBlend_PercentHigh() {
		// if (fPercent >= 1) initHSLbyRGB(R, G, B)
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(0, 0, 0);
		color.blend(255, 255, 255, 1.5f); // Should become White
		assertEquals(255, color.getLuminence());
	}

	@Test
	public void testBlend_PercentLow() {
		// if (fPercent <= 0) return
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(0, 0, 0); // Black
		color.blend(255, 255, 255, -0.5f); // Should stay Black
		assertEquals(0, color.getLuminence());
	}

	@Test
	public void testBlend_Mix() {
		// Normal mixing
		HSLColor color = new HSLColor();
		color.initHSLbyRGB(0, 0, 0); // Black
		// Blend 50% with White (255,255,255)
		// newR = 255*0.5 + 0 = 127
		color.blend(255, 255, 255, 0.5f);
		assertEquals(127, color.getRed());
	}

    @Test
    public void testGetRed() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
    }

    @Test
    public void testGetGreen() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(20, color.getGreen());
    }

    @Test
    public void testGetBlue() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(30, color.getBlue());
    }
}