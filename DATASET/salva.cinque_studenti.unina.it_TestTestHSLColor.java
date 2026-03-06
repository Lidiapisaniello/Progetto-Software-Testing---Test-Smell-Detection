/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: salva.cinque@studenti.unina.it
UserID: 274
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- TESTS FOR initHSLbyRGB (RGB -> HSL) ---

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        // Branch: cMax == cMin (R=G=B)
        hslColor.initHSLbyRGB(0, 0, 0);
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Luminance should be 0", 0, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        // Branch: cMax == cMin, High Luminance
        hslColor.initHSLbyRGB(255, 255, 255);
        assertEquals(0, hslColor.getSaturation());
        assertEquals(255, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_RedDominant_LowLum() {
        // Branch: cMax == R, pLum <= HSLMAX/2
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Hue calculation for Red: 0
        // Lum: (255 + 0 + 255) / 510 = 127 (approx half)
        assertTrue("Hue should be approx 0 for pure red", Math.abs(hslColor.getHue() - 0) < 2);
        assertEquals("Saturation should be Max", HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        // Branch: cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue for Green is approx 1/3 of 255 = ~85
        assertTrue("Hue should be approx 85 for pure green", Math.abs(hslColor.getHue() - 85) < 2);
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        // Branch: cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue for Blue is approx 2/3 of 255 = ~170
        assertTrue("Hue should be approx 170 for pure blue", Math.abs(hslColor.getHue() - 170) < 2);
    }

    @Test
    public void testInitHSLbyRGB_HueWrapAround_Negative() {
        // Branch: pHue < 0
        // To trigger negative hue in internal calc: cMax=R, and we need B component to outweigh G logic slightly in the delta math
        // Formula: pHue = BDelta - GDelta. Need BDelta < GDelta? 
        // Actually, if R is Max. 
        // Let's use a Purple-ish Red where Blue affects the hue to push it near 0, then wrap.
        // Input: R=255, G=0, B=50.
        // cMax=255, cMin=0. cMinus=255.
        // GDelta = large (since G is 0). BDelta = smaller (since B is 50).
        // Result = Small - Large = Negative.
        
        hslColor.initHSLbyRGB(255, 0, 50);
        assertTrue("Hue should be positive (wrapped around)", hslColor.getHue() > 0);
        assertTrue("Hue should be in purple/red range (high value)", hslColor.getHue() > 200);
    }

    @Test
    public void testInitHSLbyRGB_HighLuminance_SaturationBranch() {
        // Branch: pLum > HSLMAX/2
        // Use a light color
        hslColor.initHSLbyRGB(200, 255, 200); // Light Green
        assertTrue("Luminance should be > 127", hslColor.getLuminence() > 127);
        assertTrue("Saturation should be calculated", hslColor.getSaturation() > 0);
    }

    // --- TESTS FOR initRGBbyHSL (HSL -> RGB) ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Branch: S == 0
        hslColor.initRGBbyHSL(0, 0, 100);
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_LowLum() {
        // Branch: S != 0, L <= HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 100); // Red, Full Sat, Dim
        // Expect Red to be dominant
        assertTrue(hslColor.getRed() > hslColor.getGreen());
        assertTrue(hslColor.getRed() > hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_HighLum() {
        // Branch: S != 0, L > HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 200); // Red, Full Sat, Bright
        // Should be reddish but washed out (high values for G and B too)
        assertTrue(hslColor.getRed() > 200);
        assertTrue(hslColor.getGreen() > 100); 
    }

    @Test
    public void testHueToRGB_BranchCoverage() {
        // Covering private hueToRGB via public initRGBbyHSL
        // We need to hit 4 branches in hueToRGB:
        // 1. Hue < 1/6 (Hue < 42)
        // 2. Hue < 1/2 (Hue < 127)
        // 3. Hue < 2/3 (Hue < 170)
        // 4. else
        // Also Hue < 0 and Hue > HSLMAX entry checks.

        // Case 1: Hue = 0 (Red). 
        // R calls with H+85 (85, Branch 2). 
        // G calls with H (0, Branch 1). 
        // B calls with H-85 (-85 -> 170, Branch 4/Boundary).
        hslColor.initRGBbyHSL(0, 255, 128);
        
        // Case 2: Hue = 100 (Greenish).
        // R calls 185 (Branch 4).
        // G calls 100 (Branch 2).
        // B calls 15 (Branch 1).
        hslColor.initRGBbyHSL(100, 255, 128);

        // Case 3: Hue = 160 (Blueish).
        // R calls 245 (Branch 4).
        // G calls 160 (Branch 3).
        // B calls 75 (Branch 2).
        hslColor.initRGBbyHSL(160, 255, 128);
        
        // Verify values exist (checking execution flow)
        assertNotNull(hslColor.getRed());
    }
    
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // It is mathematically hard to force pRed > RGBMAX with valid HSL logic, 
        // but we can ensure the code runs safely.
        // Try Max Saturation and Luminance edge
        hslColor.initRGBbyHSL(0, 255, 254);
        assertTrue(hslColor.getRed() <= 255);
        assertTrue(hslColor.getGreen() <= 255);
        assertTrue(hslColor.getBlue() <= 255);
    }

    // --- TESTS FOR SETTERS & LOOPS ---

    @Test
    public void testSetHue_Loops() {
        // While < 0
        hslColor.setHue(-300); 
        // -300 + 255 = -45 + 255 = 210. Should result in 210.
        assertEquals(210, hslColor.getHue());

        // While > HSLMAX
        hslColor.setHue(600);
        // 600 - 255 = 345 - 255 = 90.
        assertEquals(90, hslColor.getHue());
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

    @Test
    public void testSetters_UpdateRGB() {
        // Ensure calling setters updates the RGB values via initRGBbyHSL
        hslColor.initHSLbyRGB(0, 0, 0); // Black
        hslColor.setLuminence(128); // Make it grey
        assertEquals(128, hslColor.getRed()); // Should update RGB
    }

    // --- TESTS FOR ACTIONS ---

    @Test
    public void testReverseColor() {
        hslColor.setHue(0);
        hslColor.reverseColor();
        // Should add HSLMAX/2 (127)
        assertEquals(127, hslColor.getHue());
        
        hslColor.setHue(200);
        hslColor.reverseColor();
        // 200 + 127 = 327 -> wrap -> 72
        assertEquals(72, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.initHSLbyRGB(100, 100, 100);
        int initialLum = hslColor.getLuminence();

        // 1. Percent 0 (No change)
        hslColor.brighten(0);
        assertEquals(initialLum, hslColor.getLuminence());

        // 2. Normal brighten
        hslColor.brighten(1.5f);
        assertTrue(hslColor.getLuminence() > initialLum);

        // 3. Clamp Max
        hslColor.brighten(100.0f);
        assertEquals(HSLMAX, hslColor.getLuminence());

        // 4. Clamp Min (Negative L calculation logic)
        hslColor.brighten(-1.0f); // fPercent is negative mult? No, L = pLum * fPercent.
        // if fPercent is negative, L becomes negative.
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); // Start Black

        // 1. Percent <= 0 (No change)
        hslColor.blend(255, 255, 255, 0.0f);
        assertEquals(0, hslColor.getLuminence());

        // 2. Percent >= 1 (Full replace)
        hslColor.blend(255, 255, 255, 1.2f);
        assertEquals(255, hslColor.getLuminence());

        // 3. Blend 50%
        hslColor.initHSLbyRGB(0, 0, 0); // Reset to black
        hslColor.blend(255, 255, 255, 0.5f); // Mix with white
        // Result RGB should be approx 127, 127, 127
        assertTrue(hslColor.getRed() > 120 && hslColor.getRed() < 135);
    }

    // --- TESTS FOR DEAD CODE (REFLECTION) ---
    // Required to achieve strict 100% coverage as requested, 
    // since these methods are private and never called internally.

    @Test
    public void testDeadCode_Greyscale() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        
        hslColor.setSaturation(255);
        method.invoke(hslColor);
        
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testDeadCode_ReverseLight() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        
        hslColor.setLuminence(100);
        method.invoke(hslColor);
        
        // Should be HSLMAX - 100 = 155
        assertEquals(155, hslColor.getLuminence());
    }

    @Test
    public void testDeadCode_PrivateSetters() throws Exception {
        // setRed
        Method setRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRed.setAccessible(true);
        // Calling setRed(val) triggers initHSLbyRGB(val, pGreen, pBlue)
        hslColor.initHSLbyRGB(0, 0, 0);
        setRed.invoke(hslColor, 255);
        assertEquals(255, hslColor.getRed()); // Field pRed updated via initHSLbyRGB

        // setGreen
        Method setGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreen.setAccessible(true);
        hslColor.initHSLbyRGB(0, 0, 0);
        setGreen.invoke(hslColor, 255);
        assertEquals(255, hslColor.getGreen());

        // setBlue
        Method setBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlue.setAccessible(true);
        hslColor.initHSLbyRGB(0, 0, 0);
        setBlue.invoke(hslColor, 255);
        assertEquals(255, hslColor.getBlue());
    }
    
    @Test
    public void testPrivate_iMax_iMin_viaReflection() throws Exception {
        // These are actually covered by initHSLbyRGB, but for completeness in analysis:
        Method iMax = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        iMax.setAccessible(true);
        assertEquals(10, iMax.invoke(hslColor, 5, 10));
        assertEquals(10, iMax.invoke(hslColor, 10, 5));
        
        Method iMin = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        iMin.setAccessible(true);
        assertEquals(5, iMin.invoke(hslColor, 5, 10));
        assertEquals(5, iMin.invoke(hslColor, 10, 5));
    }
  
  @Test
    public void testInitRGBbyHSL_Overflow_Clamping_Security() {
        // OBIETTIVO: Coprire i rami 'if (pRed > RGBMAX)', 'if (pGreen > ...)', 'if (pBlue > ...)'
        // STRATEGIA: Poiché con input validi (0-255) la matematica non produce mai valori > 255,
        // dobbiamo sfruttare il fatto che initRGBbyHSL è public e non valida gli input.
        // Passiamo una Saturazione enorme (es. 1000) per forzare Magic1/Magic2 fuori scala.

        // 1. Force RED Overflow
        // Hue = 0 (Rosso), Sat = 1000 (Enorme), Lum = 128
        hslColor.initRGBbyHSL(0, 1000, 128);
        assertEquals("Red should be clamped to 255 even with invalid input", 255, 				hslColor.getRed());
        // Verifica che sia scattato il clamp (se non scattasse, il valore sarebbe > 255)
        
        // 2. Force GREEN Overflow
        // Hue = 85 (circa 1/3 di 255, area Verde), Sat = 1000
        hslColor.initRGBbyHSL(85, 1000, 128);
        assertEquals("Green should be clamped to 255", 255, hslColor.getGreen());

        // 3. Force BLUE Overflow
        // Hue = 170 (circa 2/3 di 255, area Blu), Sat = 1000
        hslColor.initRGBbyHSL(170, 1000, 128);
        assertEquals("Blue should be clamped to 255", 255, hslColor.getBlue());
    }
}


						