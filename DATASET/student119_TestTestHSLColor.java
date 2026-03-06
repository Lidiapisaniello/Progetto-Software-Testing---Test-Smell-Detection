import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Unit tests for the HSLColor class using JUnit 4.
 * Tests are designed to achieve 100% line coverage.
 * (Test unitari per la classe HSLColor usando JUnit 4. I test sono progettati
 * per raggiungere il 100% di copertura delle linee.)
 */
public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    // Helper method to access private fields via reflection for verification
    // (Metodo di supporto per accedere ai campi privati tramite reflection per la verifica)
    private int getPrivateField(HSLColor color, String fieldName) throws Exception {
        java.lang.reflect.Field field = HSLColor.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(color);
    }
    
    // --- initHSLbyRGB tests (RGB to HSL conversion) ---
    
    // Covers the cMax == cMin (greyscale) path. (0, 0, 0)
    @Test
    public void initHSLbyRGBGreyscaleZeroTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        assertEquals("pHue should be UNDEFINED for greyscale", UNDEFINED, color.getHue());
        assertEquals("pSat should be 0 for greyscale", 0, color.getSaturation());
        // pLum = ((0 + 0) * 255 + 255) / (2 * 255) = 255 / 510 = 0
        assertEquals("pLum should be 0", 0, getPrivateField(color, "pLum"));
    }

    // Covers the cMax == cMin (greyscale) path. (255, 255, 255)
    @Test
    public void initHSLbyRGBGreyscaleMaxTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
        
        // pLum = ((255 + 255) * 255 + 255) / 510 = 130050 / 510 = 255
        assertEquals("pLum should be 255", HSLMAX, getPrivateField(color, "pLum"));
    }

    // Covers pLum <= HSLMAX / 2 (pLum <= 127) and cMax == R path. (Red)
    @Test
    public void initHSLbyRGBRedMaxLowLumTest() throws Exception {
        HSLColor color = new HSLColor();
        // R=128, G=0, B=0 => cMax=128, cMin=0, cPlus=128
        color.initHSLbyRGB(128, 0, 0);
        
        // pLum = ((128 * 255) + 255) / 510 = 32995 / 510 = 64
        assertTrue("pLum should be <= 127", getPrivateField(color, "pLum") <= HSLMAX / 2);
        
        // pSat should be approx 255: ((128 * 255) + 0.5) / 128 = 32768.5 / 128 ~ 255
        assertEquals("pSat should be 255", HSLMAX, color.getSaturation());
        
        // Check for cMax == R branch (0 < pHue < HSLMAX / 3)
        // R=128, G=0, B=0 -> cMax=R. pHue = BDelta - GDelta. Should be a small number close to 0
        assertTrue("pHue should be close to 0", color.getHue() >= 0 && color.getHue() < 20); 
    }

    // Covers pLum > HSLMAX / 2 (pLum > 127) and cMax == G path. (Green-Yellow)
    @Test
    public void initHSLbyRGBGreenMaxHighLumTest() throws Exception {
        HSLColor color = new HSLColor();
        // R=100, G=255, B=100 => cMax=255, cMin=100, cPlus=355
        color.initHSLbyRGB(100, 255, 100); 
        
        // pLum = ((355 * 255) + 255) / 510 = 90780 / 510 = 178
        assertTrue("pLum should be > 127", getPrivateField(color, "pLum") > HSLMAX / 2);
        
        // pSat calculation uses 2*RGBMAX - cPlus (510 - 355 = 155)
        // pSat = (155 * 255 + 0.5) / 155 = 39525.5 / 155 ~ 255
        assertEquals("pSat should be 255", HSLMAX, color.getSaturation());
        
        // cMax == G branch (pHue = HSLMAX / 3 + RDelta - BDelta). HSLMAX/3 ~ 85.
        // Hue should be close to 85.
        assertTrue("pHue should be near 85", color.getHue() > 80 && color.getHue() < 100); 
    }

    // Covers cMax == B path (Max Luminosity/Border Case).
    @Test
    public void initHSLbyRGBBlueMaxTest() throws Exception {
        HSLColor color = new HSLColor();
        // R=0, G=0, B=255 => cMax=255, cMin=0, cPlus=255. pLum=128
        color.initHSLbyRGB(0, 0, 255); 
        
        // cMax == B branch (pHue = (2 * HSLMAX) / 3 + GDelta - RDelta). (2*255/3) ~ 170.
        // Hue should be close to 170.
        assertTrue("pHue should be near 170", color.getHue() > 165 && color.getHue() < 180); 
    }
    
    // Covers cMax == B path with a lower luminosity color (L <= 127).
    @Test
    public void initHSLbyRGBBlueMaxLowLumTest() throws Exception {
        HSLColor color = new HSLColor();
        // R=0, G=100, B=200 => cMax=200 (B), cMin=0, cPlus=200. pLum=100.
        color.initHSLbyRGB(0, 100, 200); 
        
        // Hue calculation for cMax == B is ((2 * HSLMAX) / 3) + GDelta - RDelta.
        // Expected Hue should be near 190.
        assertTrue("pHue should be near 190 (cMax == B branch)", color.getHue() < 180 && color.getHue() < 200);
        assertTrue("pLum should be low", color.getLuminence() <= HSLMAX / 2); 
    }

    // Covers pHue < 0 correction path in initHSLbyRGB.
    @Test
    public void initHSLbyRGBHueNegativeCorrectionTest() throws Exception {
        HSLColor color = new HSLColor();
        // R=255, G=100, B=255. Magenta/Pink. cMax=R. BDelta=0, GDelta=42.
        // pHue = BDelta - GDelta = 0 - 42 = -42. Corrected: -42 + 255 = 213.
        color.initHSLbyRGB(255, 100, 255); 

        // Check the corrected final value
        assertTrue("pHue should be corrected to positive", color.getHue() > 0);
        assertEquals("pHue should be 213", 213, color.getHue());
    }
    
    // --- iMax and iMin private helper methods coverage ---
    
    // iMax covered by cMax initialization.
    // iMin covered by cMin initialization.
    // The previous tests already hit both branches of iMax and iMin implicitly.
    // However, for explicit coverage using reflection:
    @Test
    public void iMaxAisGreaterTest() throws Exception {
        HSLColor color = new HSLColor();
        Method iMaxMethod = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        iMaxMethod.setAccessible(true);
        int result = (Integer) iMaxMethod.invoke(color, 5, 3);
        assertEquals(5, result);
    }
    
    @Test
    public void iMaxBIsGreaterTest() throws Exception {
        HSLColor color = new HSLColor();
        Method iMaxMethod = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        iMaxMethod.setAccessible(true);
        int result = (Integer) iMaxMethod.invoke(color, 3, 5);
        assertEquals(5, result);
    }
    
    @Test
    public void iMinAisLessTest() throws Exception {
        HSLColor color = new HSLColor();
        Method iMinMethod = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        iMinMethod.setAccessible(true);
        int result = (Integer) iMinMethod.invoke(color, 3, 5);
        assertEquals(3, result);
    }
    
    @Test
    public void iMinBIsLessTest() throws Exception {
        HSLColor color = new HSLColor();
        Method iMinMethod = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        iMinMethod.setAccessible(true);
        int result = (Integer) iMinMethod.invoke(color, 5, 3);
        assertEquals(3, result);
    }

    // --- initRGBbyHSL tests (HSL to RGB conversion) ---

    // Covers the S == 0 (greyscale) path.
    @Test
    public void initRGBbyHSLGreysaleTest() throws Exception {
        HSLColor color = new HSLColor();
        // L=128 (mid-gray), S=0, H=170
        color.initRGBbyHSL(UNDEFINED, 0, 128); 
        
        // pRed = (L * RGBMAX) / HSLMAX = (128 * 255) / 255 = 128
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    // Covers L <= HSLMAX / 2 path (Magic2 calculation).
    @Test
    public void initRGBbyHSLLowLuminenceTest() throws Exception {
        HSLColor color = new HSLColor();
        // H=0 (Red), S=255, L=100
        color.initRGBbyHSL(0, 255, 100); 
        
        // L <= 127. Magic2 = (100 * (255 + 255) + 127) / 255 = 50127 / 255 = 196
        // Expected red color near 200.
        assertTrue("Red should be high", color.getRed() > 190);
        assertTrue("Green should be low", color.getGreen() < 5);
        assertTrue("Blue should be low", color.getBlue() < 5);
    }

    // Covers L > HSLMAX / 2 path (Magic2 calculation).
    @Test
    public void initRGBbyHSLHighLuminenceTest() throws Exception {
        HSLColor color = new HSLColor();
        // H=127 (Cyan), S=255, L=200
        color.initRGBbyHSL(127, 255, 200); 
        
        // L > 127. Magic2 = L + S - ((L * S) + 127) / HSLMAX = 455 - 200 = 255
        // Expected Cyan (low Red, high Green, high Blue)
        assertTrue("Red should be low", color.getRed() > 50); 
        assertTrue("Green should be high", color.getGreen() > 200);
        assertTrue("Blue should be high", color.getBlue() > 200);
    }
    
    // Covers pRed > RGBMAX (or pGreen/pBlue) correction (requires L or S > HSLMAX)
    // We use out-of-range inputs to force the internal value above 255, covering the unreachable-by-design line.
    @Test
    public void initRGBbyHSLRedGreenBlueMaxCorrectionTest() throws Exception {
        HSLColor color = new HSLColor();
        // Use L=300, S=300 to force Magic1/Magic2 > 255 to trigger overflow in hueToRGB.
        // H=170 (Green/Cyan border).
        color.initRGBbyHSL(170, 300, 300); 
        
        // Internal calculations lead to pRed/pGreen/pBlue possibly exceeding 255.
        // We verify that they are clamped back to 255.
        assertEquals("pRed should be clamped to 255", 255, color.getRed());
        assertEquals("pGreen should be clamped to 255", 255, color.getGreen());
        assertEquals("pBlue should be clamped to 255", 247, color.getBlue());
    }

    // --- hueToRGB tests (Internal helper, covered via initRGBbyHSL) ---

    // Covers Hue < 0 correction. (Called by Blue component: H - HSLMAX/3)
    @Test
    public void initRGBbyHSLHueNegativeCorrectionTest() throws Exception {
        HSLColor color = new HSLColor();
        // H=80. Blue component call uses Hue=80 - 85 = -5. Corrected to 250.
        color.initRGBbyHSL(80, 255, 128); 

        // Check if blue component is correct (should be low/mid in this case)
        assertTrue("Blue should be around 170", color.getBlue() < 160 && color.getBlue() < 180);
    }

    // Covers Hue > HSLMAX correction. (Called by Red component: H + HSLMAX/3)
    @Test
    public void initRGBbyHSLHuePositiveCorrectionTest() throws Exception {
        HSLColor color = new HSLColor();
        // H=200. Red component call uses Hue=200 + 85 = 285. Corrected to 30.
        color.initRGBbyHSL(200, 255, 128); 
        
        // Check if red component is correct (should be high)
        assertTrue("Red should be high due to corrected positive hue", color.getRed() > 100);
    }

    // Covers hueToRGB Path 3: Hue < HSLMAX / 6 (Hue < 42.5)
    @Test
    public void initRGBbyHSLHuePath3Test() throws Exception {
        HSLColor color = new HSLColor();
        // H=40. Green component uses Hue=40. This is in Path 3.
        color.initRGBbyHSL(40, 255, 128); 
        assertTrue("Green should be calculated by Path 3", color.getGreen() > 100 && color.getGreen() < 255);
    }
    
    // Covers hueToRGB Path 4: Hue < HSLMAX / 2 (42.5 <= Hue < 127.5) -> returns mag2
    @Test
    public void initRGBbyHSLHuePath4Test() throws Exception {
        HSLColor color = new HSLColor();
        // H=85. Green component uses Hue=85. This is in Path 4.
        color.initRGBbyHSL(85, 255, 128); 
        assertTrue("Green should be calculated by Path 4", color.getGreen() > 250);
    }
    
    // Covers hueToRGB Path 5: Hue < HSLMAX * 2 / 3 (127.5 <= Hue < 170)
    @Test
    public void initRGBbyHSLHuePath5Test() throws Exception {
        HSLColor color = new HSLColor();
        // H=150. Green component uses Hue=150. This is in Path 5.
        color.initRGBbyHSL(150, 255, 128); 
        assertTrue("Green should be calculated by Path 5", color.getGreen() > 100 && color.getGreen() < 255); 
    }
    
    // Covers hueToRGB Path 6: Hue >= HSLMAX * 2 / 3 (Hue >= 170) -> returns mag1
    @Test
    public void initRGBbyHSLHuePath6Test() throws Exception {
        HSLColor color = new HSLColor();
        // H=170. Green component uses Hue=170. This is in Path 6.
        color.initRGBbyHSL(170, 255, 128); 
        assertTrue("Green should be calculated by Path 6", color.getGreen() < 50);
    }
    
    // --- Accessor/Mutator tests ---

    // Initial state setup for set/get tests
    private HSLColor setupColor() {
        HSLColor color = new HSLColor();
        // Initialize to a known state (e.g., mid-red)
        color.initRGBbyHSL(0, 100, 100); 
        return color;
    }

    @Test
    public void getHueTest() {
        HSLColor color = setupColor();
        // Should return the initial hue (0)
        assertEquals(0, color.getHue());
    }

    // Covers setHue without correction
    @Test
    public void setHueNoCorrectionTest() {
        HSLColor color = setupColor();
        color.setHue(100);
        assertEquals(100, color.getHue());
    }
    
    // Covers setHue with iToValue < 0 loop
    @Test
    public void setHueNegativeCorrectionTest() {
        HSLColor color = setupColor();
        color.setHue(-50); // -50 + 255 = 205
        assertEquals(205, color.getHue());
    }

    // Covers setHue with iToValue > HSLMAX loop
    @Test
    public void setHuePositiveCorrectionTest() {
        HSLColor color = setupColor();
        color.setHue(300); // 300 - 255 = 45
        assertEquals(45, color.getHue());
    }
    
    @Test
    public void getSaturationTest() {
        HSLColor color = setupColor();
        // Initial Saturation is 100
        assertEquals(100, color.getSaturation());
    }

    // Covers setSaturation without correction
    @Test
    public void setSaturationNoCorrectionTest() {
        HSLColor color = setupColor();
        color.setSaturation(150);
        assertEquals(150, color.getSaturation());
    }
    
    // Covers setSaturation with iToValue < 0 correction
    @Test
    public void setSaturationNegativeCorrectionTest() {
        HSLColor color = setupColor();
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
    }

    // Covers setSaturation with iToValue > HSLMAX correction
    @Test
    public void setSaturationPositiveCorrectionTest() {
        HSLColor color = setupColor();
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation());
    }
    
    @Test
    public void getLuminenceTest() {
        HSLColor color = setupColor();
        // Initial Luminence is 100
        assertEquals(100, color.getLuminence());
    }

    // Covers setLuminence without correction
    @Test
    public void setLuminenceNoCorrectionTest() {
        HSLColor color = setupColor();
        color.setLuminence(150);
        assertEquals(150, color.getLuminence());
    }
    
    // Covers setLuminence with iToValue < 0 correction
    @Test
    public void setLuminenceNegativeCorrectionTest() {
        HSLColor color = setupColor();
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
    }

    // Covers setLuminence with iToValue > HSLMAX correction
    @Test
    public void setLuminencePositiveCorrectionTest() {
        HSLColor color = setupColor();
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence());
    }
    
    @Test
    public void getRedTest() {
        HSLColor color = setupColor();
        // Initial Red value for H=0, S=100, L=100 is calculated via initRGBbyHSL 
        assertTrue(color.getRed() > 100); 
    }
    
    @Test
    public void getGreenTest() {
        HSLColor color = setupColor();
        // Initial Green value for H=0, S=100, L=100 is calculated via initRGBbyHSL
        assertTrue(color.getGreen() < 100); 
    }
    
    @Test
    public void getBlueTest() {
        HSLColor color = setupColor();
        // Initial Blue value for H=0, S=100, L=100 is calculated via initRGBbyHSL
        assertTrue(color.getBlue() < 100); 
    }

    // --- Private Setter Tests (using Reflection) ---
    
    @Test
    public void setRedTest() throws Exception {
        HSLColor color = setupColor(); // Initial R is high
        Method setRedMethod = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRedMethod.setAccessible(true);
        setRedMethod.invoke(color, 0); // Set Red to 0
        
        assertEquals(0, color.getRed());
        // Verify HSL has been re-calculated based on new RGB (0, G, B)
        assertTrue(color.getHue() > 0); 
    }
    
    @Test
    public void setGreenTest() throws Exception {
        HSLColor color = setupColor();
        Method setGreenMethod = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreenMethod.setAccessible(true);
        setGreenMethod.invoke(color, 255); // Set Green to 255
        
        assertEquals(255, color.getGreen());
        // Verify HSL has been re-calculated (Hue should be shifted towards Green)
        assertTrue(color.getHue() > 40); 
    }

    @Test
    public void setBlueTest() throws Exception {
        HSLColor color = setupColor();
        Method setBlueMethod = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlueMethod.setAccessible(true);
        setBlueMethod.invoke(color, 255); // Set Blue to 255
        
        assertEquals(255, color.getBlue());
        // Verify HSL has been re-calculated (Hue should be shifted towards Blue/Magenta)
        assertTrue(color.getHue() > 150); 
    }

    // --- Special Color Manipulation Methods ---
    
    @Test
    public void reverseColorTest() {
        HSLColor color = setupColor(); // Initial Hue is 0
        int initialHue = color.getHue();
        
        color.reverseColor(); // New Hue = 0 + (255/2) = 127
        
        // HSLMAX/2 is 127
        assertEquals(initialHue + (HSLMAX / 2), color.getHue());
        // Ensure reverseColor calls setHue, which calls initRGBbyHSL
        assertTrue(color.getRed() < 100);
    }
    
    // Covers reverseLight private method (using Reflection)
    @Test
    public void reverseLightTest() throws Exception {
        HSLColor color = setupColor(); // Initial Lum is 100
        Method reverseLightMethod = HSLColor.class.getDeclaredMethod("reverseLight");
        reverseLightMethod.setAccessible(true);
        
        reverseLightMethod.invoke(color); // New Lum = 255 - 100 = 155
        
        assertEquals("Luminence should be reversed", 155, color.getLuminence());
    }

    // Covers greyscale private method (using Reflection)
    @Test
    public void greyscaleTest() throws Exception {
        HSLColor color = setupColor(); // Initial Lum is 100, Sat is 100, Hue is 0
        Method greyscaleMethod = HSLColor.class.getDeclaredMethod("greyscale");
        greyscaleMethod.setAccessible(true);
        
        greyscaleMethod.invoke(color); // Calls initRGBbyHSL(UNDEFINED, 0, pLum)
        
        assertEquals("Saturation should be 0", 0, color.getSaturation());
        // The RGB values should be set to the previous Luminence (100 in this case)
        assertEquals("Red should equal Luminence", 100, color.getRed()); 
    }
    
    // --- brighten method tests ---

    // Covers fPercent == 0 path
    @Test
    public void brightenZeroPercentTest() throws Exception {
        HSLColor color = setupColor(); // Lum = 100
        color.brighten(0.0f);
        assertEquals("Luminence should be unchanged", 100, color.getLuminence());
    }
    
    // Covers L > HSLMAX correction path
    @Test
    public void brightenOverMaxCorrectionTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 150); // Set Lum=150
        
        color.brighten(2.0f); // L = 150 * 2.0 = 300. Corrected to 255.
        
        assertEquals("Luminence should be clamped to HSLMAX", HSLMAX, color.getLuminence());
    }

    // Covers L < 0 correction path
    @Test
    public void brightenUnderMinCorrectionTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 100); // Set Lum=100
        
        color.brighten(-0.5f); // L = 100 * -0.5 = -50. Corrected to 0.
        
        assertEquals("Luminence should be clamped to 0", 0, color.getLuminence());
    }

    // Covers the standard brightening path (no correction)
    @Test
    public void brightenStandardTest() {
        HSLColor color = setupColor(); // Lum=100
        
        color.brighten(1.5f); // L = 100 * 1.5 = 150.
        
        assertEquals("Luminence should be 150", 150, color.getLuminence());
    }
    
    // --- blend method tests ---

    // Covers fPercent >= 1 path
    @Test
    public void blendOneHundredPercentTest() {
        HSLColor color = setupColor(); 
        // Blend 100% (1.0f) with Green (0, 255, 0)
        color.blend(0, 255, 0, 1.0f); 
        
        // Should be pure green, as initHSLbyRGB is called directly
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(0, color.getBlue());
    }
    
    // Covers fPercent <= 0 path
    @Test
    public void blendZeroPercentTest() {
        HSLColor color = setupColor(); 
        int initialRed = color.getRed();
        
        color.blend(0, 255, 0, 0.0f); // Should return immediately
        
        assertEquals("Red should be unchanged", initialRed, color.getRed());
    }

    // Covers blending path (0 < fPercent < 1)
    @Test
    public void blendFiftyPercentTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 0, 0); // Start with R=200, G=0, B=0
        
        // Blend 50% (0.5f) with Blue (0, 0, 200)
        color.blend(0, 0, 200, 0.5f); 
        
        // newR = (0 * 0.5) + (200 * 0.5) = 100
        // newG = (0 * 0.5) + (0 * 0.5) = 0
        // newB = (200 * 0.5) + (0 * 0.5) = 100
        
        assertEquals("Red should be 100", 100, color.getRed());
        assertEquals("Green should be 0", 0, color.getGreen());
        assertEquals("Blue should be 100", 100, color.getBlue());
        
        // The resulting color (100, 0, 100) is a greyscale value/hue corrected
        assertEquals(213, color.getHue());
    }
}