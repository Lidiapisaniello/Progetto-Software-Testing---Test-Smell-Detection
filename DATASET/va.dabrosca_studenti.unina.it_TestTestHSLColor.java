/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: va.dabrosca@studenti.unina.it
UserID: 838
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class TestHSLColor {
    
    private HSLColor hslColor;
    // Costanti riflesse dalla classe per coerenza nei test
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
        // Inizializza una nuova istanza prima di ogni test
        hslColor = new HSLColor();
    }
                
    @After
    public void tearDown() {
        // Pulizia dopo ogni test
        hslColor = null;
    }

    // --- TEST PUBLIC METHODS & LOGIC FLOW ---

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Branch: cMax == cMin (Grigio)
        // R=100, G=100, B=100
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED (170) for greyscale", UNDEFINED, hslColor.getHue());
        // Lum calculation: ((200 * 255) + 255) / (2 * 255) approx 100
        // cPlus = 200. (200*255 + 255) / 510 = 100.5 -> 100
        assertEquals(100, hslColor.getLuminence());
        assertEquals(100, hslColor.getRed());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // Branch: cMax == R
        hslColor.initHSLbyRGB(255, 0, 0);
        // Hue red is usually 0 (or 255 scaled)
        // Saturation should be max
        assertEquals(0, hslColor.getHue()); 
        assertEquals(HSLMAX, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence()); // Approx 255/2
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // Branch: cMax == G
        // Hue calculation for Green: (HSLMAX / 3) + ...
        hslColor.initHSLbyRGB(0, 255, 0);
        int expectedHue = HSLMAX / 3; // 85
        assertEquals(expectedHue, hslColor.getHue());
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // Branch: cMax == B
        // Hue calculation for Blue: (2 * HSLMAX) / 3 + ...
        hslColor.initHSLbyRGB(0, 0, 255);
        int expectedHue = (2 * HSLMAX) / 3; // 170
        assertEquals(expectedHue, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueFix() {
        // Branch: pHue < 0 check inside initHSLbyRGB.
        // Occurs when cMax == R but B > G significantly (Magenta side of Red)
        // R=255, G=0, B=200.
        hslColor.initHSLbyRGB(255, 0, 200);
        assertTrue("Hue should be positive due to wraparound logic", hslColor.getHue() >= 0);
        assertTrue("Hue should be in valid range", hslColor.getHue() <= HSLMAX);
    }

    @Test
    public void testInitHSLbyRGB_LuminanceBranches() {
        // Branch: pLum <= (HSLMAX / 2)
        // Dark color
        hslColor.initHSLbyRGB(50, 0, 0); // cMin=0, cMax=50, cPlus=50. Lum approx 25.
        assertTrue(hslColor.getLuminence() <= HSLMAX/2);
        int satDark = hslColor.getSaturation();

        // Branch: pLum > (HSLMAX / 2)
        // Light color
        hslColor.initHSLbyRGB(255, 200, 200); // Very light red
        assertTrue(hslColor.getLuminence() > HSLMAX/2);
        int satLight = hslColor.getSaturation();
        
        assertTrue(satDark >= 0);
        assertTrue(satLight >= 0);
    }

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Branch: S == 0
        hslColor.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_LowLuminance() {
        // Branch: L <= HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 100);
        assertEquals(0, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
        assertEquals(100, hslColor.getLuminence());
        // Check if RGB were updated
        assertTrue(hslColor.getRed() > 0);
    }

    @Test
    public void testInitRGBbyHSL_Color_HighLuminance() {
        // Branch: L > HSLMAX/2
        // This triggers the 'else' in Magic2 calculation
        hslColor.initRGBbyHSL(170, 200, 200);
        assertEquals(200, hslColor.getLuminence());
    }
    
    @Test
    public void testInitRGBbyHSL_ClampRGB() {
        // Attempt to force values that might round up > 255 if not clamped
        // Although math usually keeps it in check, we ensure coverage of 
        // "if (pRed > RGBMAX)" checks.
        // High Lum, High Sat often pushes boundaries.
        hslColor.initRGBbyHSL(0, 255, 255); // White
        assertEquals(255, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        assertEquals(255, hslColor.getBlue());
    }

    // --- TEST SETTERS & GETTERS & LOOPS ---

    @Test
    public void testSetHue_Loops() {
        hslColor.initHSLbyRGB(255, 0, 0); // Start Red
        
        // Case: Hue < 0 (Loop addition)
        hslColor.setHue(-85); 
        // -85 + 255 = 170. 170 is usually Blue.
        assertEquals(170, hslColor.getHue());

        // Case: Hue > HSLMAX (Loop subtraction)
        hslColor.setHue(340); 
        // 340 - 255 = 85. 85 is usually Green.
        assertEquals(85, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamps() {
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamps() {
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(500);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }
    
    @Test
    public void testReverseColor() {
        // Logic: setHue(pHue + (HSLMAX / 2))
        hslColor.initRGBbyHSL(0, 255, 128); // Red
        hslColor.reverseColor();
        // 0 + 127 = 127 (Cyan-ish)
        assertEquals(127, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        // Case: 0%
        hslColor.initRGBbyHSL(0, 0, 100);
        hslColor.brighten(0.0f);
        assertEquals(100, hslColor.getLuminence());

        // Case: Positive
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());

        // Case: Overflow clamping
        hslColor.brighten(10.0f); // 1500 -> 255
        assertEquals(HSLMAX, hslColor.getLuminence());
        
        // Case: Negative (result < 0)
        hslColor.brighten(-5.0f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); // Black

        // Case: fPercent >= 1 -> Full replace
        hslColor.blend(255, 255, 255, 1.5f);
        assertEquals(255, hslColor.getRed());

        // Case: fPercent <= 0 -> No change
        hslColor.blend(0, 0, 0, -0.5f);
        assertEquals(255, hslColor.getRed()); // Still white from prev step

        // Case: 0.5 -> Blend
        hslColor.initHSLbyRGB(0, 0, 0); // Reset to black
        // Blend with White at 50%
        hslColor.blend(255, 255, 255, 0.5f);
        // Expect approx 127
        assertTrue(hslColor.getRed() >= 127 && hslColor.getRed() <= 128);
    }

    // --- TEST PRIVATE METHODS USING REFLECTION ---

    @Test
    public void testPrivate_iMax_iMin() throws Exception {
        Method mMax = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        mMax.setAccessible(true);
        assertEquals(10, mMax.invoke(hslColor, 5, 10));
        assertEquals(10, mMax.invoke(hslColor, 10, 5));

        Method mMin = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        mMin.setAccessible(true);
        assertEquals(5, mMin.invoke(hslColor, 5, 10));
        assertEquals(5, mMin.invoke(hslColor, 10, 5));
    }

    @Test
    public void testPrivate_greyscale() throws Exception {
        hslColor.initHSLbyRGB(255, 0, 0); // Red
        Method mGreyscale = HSLColor.class.getDeclaredMethod("greyscale");
        mGreyscale.setAccessible(true);
        mGreyscale.invoke(hslColor);

        assertEquals(UNDEFINED, hslColor.getHue());
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void testPrivate_reverseLight() throws Exception {
        hslColor.initHSLbyRGB(100, 100, 100); // Lum ~100
        int initialLum = hslColor.getLuminence();
        
        Method mReverse = HSLColor.class.getDeclaredMethod("reverseLight");
        mReverse.setAccessible(true);
        mReverse.invoke(hslColor);

        assertEquals(HSLMAX - initialLum, hslColor.getLuminence());
    }

    @Test
    public void testPrivate_hueToRGB() throws Exception {
        // Questo metodo è fondamentale per testare tutti i branch di conversione colore
        Method mHueToRGB = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        mHueToRGB.setAccessible(true);

        int mag1 = 100;
        int mag2 = 200;

        // Branch 1: Hue < 0 (Check normalization)
        // Pass -10 -> Hue becomes -10 + 255 = 245.
        // 245 is > 2/3 (170). Should return mag1 (100).
        Object resNeg = mHueToRGB.invoke(hslColor, mag1, mag2, -10);
        assertEquals(100, resNeg);

        // Branch 2: Hue > HSLMAX (Check normalization)
        // Pass 300 -> Hue becomes 300 - 255 = 45.
        // 45 is > 1/6 (42) but < 1/2 (127). Should return mag2 (200).
        Object resOver = mHueToRGB.invoke(hslColor, mag1, mag2, 300);
        assertEquals(200, resOver);

        // Branch 3: Hue < HSLMAX / 6 (42)
        // Pass 20. Returns mag1 + ...
        Object resFirstSixth = mHueToRGB.invoke(hslColor, mag1, mag2, 20);
        assertNotEquals(mag1, resFirstSixth);
        assertNotEquals(mag2, resFirstSixth);

        // Branch 4: Hue < HSLMAX / 2 (127)
        // Pass 100. Returns mag2.
        Object resHalf = mHueToRGB.invoke(hslColor, mag1, mag2, 100);
        assertEquals(mag2, resHalf);

        // Branch 5: Hue < HSLMAX * 2 / 3 (170)
        // Pass 150. Returns mag1 + ...
        Object resTwoThirds = mHueToRGB.invoke(hslColor, mag1, mag2, 150);
        // Calculation verification or just ensure it runs without error and returns int
        assertTrue((Integer)resTwoThirds >= mag1 && (Integer)resTwoThirds <= mag2);

        // Branch 6: Else (Hue >= 170)
        // Pass 200. Returns mag1.
        Object resElse = mHueToRGB.invoke(hslColor, mag1, mag2, 200);
        assertEquals(mag1, resElse);
    }

    @Test
    public void testPrivate_settersRGB() throws Exception {
        // Test setRed
        Method mSetRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        mSetRed.setAccessible(true);
        mSetRed.invoke(hslColor, 255);
        assertEquals(255, hslColor.getRed());

        // Test setGreen
        Method mSetGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        mSetGreen.setAccessible(true);
        mSetGreen.invoke(hslColor, 255);
        assertEquals(255, hslColor.getGreen());

        // Test setBlue
        Method mSetBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        mSetBlue.setAccessible(true);
        mSetBlue.invoke(hslColor, 255);
        assertEquals(255, hslColor.getBlue());
    }
}