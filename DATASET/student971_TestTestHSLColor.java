import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class to achieve maximum line and branch coverage for the HSLColor class.
 *
 * NOTE ON UNREACHABLE CODE:
 * The following private methods in HSLColor are not called by any public method
 * and are therefore unreachable via the public API, resulting in no coverage for them:
 * 1. private void setRed(int iNewValue)
 * 2. private void setGreen(int iNewValue)
 * 3. private void setBlue(int iNewValue)
 * 4. private void greyscale()
 * 5. private void reverseLight()
 *
 * The private utility methods (iMax, iMin, hueToRGB) are tested using reflection
 * to ensure complete coverage of their internal branches, as they are core to the conversion logic.
 */
public class TestHSLColor {

    private HSLColor testColor;
    private static final int HSLMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void testSetup() {
        testColor = new HSLColor();
    }

    // --- Helper Methods using Reflection for Private Utility Methods ---

    /** Tests iMax(a, b) where a > b (branch 1) */
    @Test
    public void testIMax_aGreaterThanB() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        method.setAccessible(true);
        int result = (int) method.invoke(testColor, 10, 5);
        assertEquals(10, result);
    }

    /** Tests iMax(a, b) where a <= b (branch 2: includes a < b and a == b) */
    @Test
    public void testIMax_aLessThanOrEqualToB() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        method.setAccessible(true);
        int resultLess = (int) method.invoke(testColor, 5, 10);
        assertEquals(10, resultLess);
        int resultEqual = (int) method.invoke(testColor, 7, 7);
        assertEquals(7, resultEqual);
    }

    /** Tests iMin(a, b) where a < b (branch 1) */
    @Test
    public void testIMin_aLessThanB() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);
        int result = (int) method.invoke(testColor, 5, 10);
        assertEquals(5, result);
    }

    /** Tests iMin(a, b) where a >= b (branch 2: includes a > b and a == b) */
    @Test
    public void testIMin_aGreaterThanOrEqualToB() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);
        int resultGreater = (int) method.invoke(testColor, 10, 5);
        assertEquals(5, resultGreater);
        int resultEqual = (int) method.invoke(testColor, 7, 7);
        assertEquals(7, resultEqual);
    }

    /**
     * Tests all branches of hueToRGB(mag1, mag2, Hue) for range checking and main logic.
     * Note: HSLMAX = 255
     */
    @Test
    public void testHueToRGB_allBranches() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        method.setAccessible(true);

        int mag1 = 50;
        int mag2 = 200;

        // 1. Hue < 0 range check: Hue = Hue + HSLMAX
        int resultRangeNeg = (int) method.invoke(testColor, mag1, mag2, -10);
        // Becomes 245. 245 > (2*255/3) = 170. Returns mag1 (50).
        assertEquals(mag1, resultRangeNeg);

        // 2. Hue > HSLMAX range check: Hue = Hue - HSLMAX
        int resultRangePos = (int) method.invoke(testColor, mag1, mag2, 265);
        // Becomes 10. 10 < (HSLMAX/6) = 42. Returns first conditional.
        assertTrue(resultRangePos > mag1 && resultRangePos < mag2); // Actual value is approx 90

        // 3. 0 <= Hue < (HSLMAX / 6) (0 to 42)
        int hue1 = 10;
        int result1 = (int) method.invoke(testColor, mag1, mag2, hue1);
        // Should return (mag1 + (((mag2 - mag1) * Hue + 21) / 42))) -> (50 + (150 * 10 + 21) / 42) = 50 + 36 = 86
        assertEquals(86, result1);

        // 4. (HSLMAX / 6) <= Hue < (HSLMAX / 2) (42 to 127) -> returns mag2
        int hue2 = 100;
        int result2 = (int) method.invoke(testColor, mag1, mag2, hue2);
        assertEquals(mag2, result2); // Returns mag2 (200)

        // 5. (HSLMAX / 2) <= Hue < (HSLMAX * 2 / 3) (127 to 170)
        int hue3 = 150;
        int result3 = (int) method.invoke(testColor, mag1, mag2, hue3);
        // Should return (mag1 + (((mag2 - mag1) * (170 - Hue) + 21) / 42)) -> (50 + (150 * 20 + 21) / 42) = 50 + 71 = 121
        assertEquals(121, result3); // Corrected from 122 to 121
        
        // 6. Hue >= (HSLMAX * 2 / 3) (170 to 255) -> returns mag1
        int hue4 = 200;
        int result4 = (int) method.invoke(testColor, mag1, mag2, hue4);
        assertEquals(mag1, result4); // Returns mag1 (50)
    }

    // --- initHSLbyRGB Coverage (RGB -> HSL) ---

    /** Tests greyscale path: cMax == cMin */
    @Test
    public void testInitHSLbyRGB_Greyscale() {
        testColor.initHSLbyRGB(128, 128, 128);
        // cMax = cMin = 128. pSat should be 0, pHue should be UNDEFINED (170).
        assertEquals(0, testColor.getSaturation());
        assertEquals(UNDEFINED, testColor.getHue());
        // pLum = ((256 * 255) + 255) / (2 * 255) = 128
        assertEquals(128, testColor.getLuminence());
    }

    /** Tests L <= HSLMAX / 2 Saturation path (darker colors) */
    @Test
    public void testInitHSLbyRGB_DarkColor_L_LTE_Half() {
        // R=100, G=50, B=0. cMax=100, cMin=0. cMinus=100. cPlus=100. pLum = 50.
        testColor.initHSLbyRGB(100, 50, 0);
        // pLum = 50 which is <= 127 (HSLMAX/2). Saturation uses cPlus (100) in denominator.
        // pSat = (int) (((100 * 255) + 0.5) / 100) = 25500 / 100 = 255
        // FIX: Expected 650 corrected to 255
        assertEquals(255, testColor.getSaturation());
    }

    /** Tests L > HSLMAX / 2 Saturation path (lighter colors) */
    @Test
    public void testInitHSLbyRGB_BrightColor_L_GT_Half() {
        // R=255, G=150, B=100. cMax=255, cMin=100. cMinus=155. cPlus=355.
        // pLum = 178 which is > 127. Saturation uses 510-355=155 in denominator.
        // pSat = (int) ((155 * 255 + 0.5) / 155) = 255
        testColor.initHSLbyRGB(255, 150, 100);
        assertEquals(255, testColor.getSaturation());
    }

    /** Tests Hue calculation where cMax == R */
    @Test
    public void testInitHSLbyRGB_HueRMax_pHueGTEZero() {
        // R=255 (Max), G=100, B=0. cMinus=255. HSLMAX/6 = 42.
        testColor.initHSLbyRGB(255, 100, 0);
        // RDelta=0. GDelta = ((155 * 42) + 0.5) / 255 = 6510 / 255 = 25. BDelta = ((255 * 42) + 0.5) / 255 = 42.
        // pHue = BDelta - GDelta = 42 - 25 = 17
        // FIX: Expected 0 corrected to 17
        assertEquals(17, testColor.getHue());
    }

    /** Tests Hue calculation where cMax == R AND pHue < 0 (requires BDelta < GDelta) */
    @Test
    public void testInitHSLbyRGB_HueRMax_pHueLessThanZero() {
        // R=255 (Max), G=0, B=100. cMinus=255. HSLMAX/6 = 42.
        testColor.initHSLbyRGB(255, 0, 100);
        // RDelta=0. GDelta = 42. BDelta = ((155 * 42) + 0.5) / 255 = 25.
        // pHue = BDelta - GDelta = 25 - 42 = -17.
        // Adjusted: pHue = -17 + 255 = 238.
        assertEquals(238, testColor.getHue());
    }

    /** Tests Hue calculation where cMax == G */
    @Test
    public void testInitHSLbyRGB_HueGMax() {
        // R=0, G=255 (Max), B=100. cMinus=255. HSLMAX/6 = 42.
        testColor.initHSLbyRGB(0, 255, 100);
        // RDelta = 42. GDelta = 0. BDelta = 25. HSLMAX/3 = 85.
        // pHue = (HSLMAX / 3) + RDelta - BDelta = 85 + 42 - 25 = 102
        assertEquals(102, testColor.getHue());
    }

    /** Tests Hue calculation where cMax == B */
    @Test
    public void testInitHSLbyRGB_HueBMax() {
        // R=100, G=0, B=255 (Max). cMinus=255. HSLMAX/6 = 42.
        testColor.initHSLbyRGB(100, 0, 255);
        // RDelta = 25. GDelta = 42. BDelta = 0. 2*HSLMAX/3 = 170.
        // pHue = ((2 * HSLMAX) / 3) + GDelta - RDelta = 170 + 42 - 25 = 187
        assertEquals(187, testColor.getHue());
    }

    // --- initRGBbyHSL Coverage (HSL -> RGB) ---

    /** Tests greyscale path: S == 0 */
    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // H=10, S=0, L=128
        testColor.initRGBbyHSL(10, 0, 128);
        // pRed = (128 * 255) / 255 = 128. pGreen=pBlue=128.
        assertEquals(128, testColor.getRed());
        assertEquals(128, testColor.getGreen());
        assertEquals(128, testColor.getBlue());
    }

    /** Tests L <= HSLMAX / 2 Magic2 calculation (darker) */
    @Test
    public void testInitRGBbyHSL_DarkColor_Magic2_L_LTE_Half() {
        // H=0 (Red), S=255 (Max), L=100 (Dark)
        testColor.initRGBbyHSL(0, 255, 100);
        // Magic2 = (100 * (255 + 255) + 127) / 255 = 51127 / 255 = 200 (approx)
        assertTrue(testColor.getRed() > testColor.getGreen());
        assertTrue(testColor.getRed() > testColor.getBlue());
    }

    /** Tests L > HSLMAX / 2 Magic2 calculation (lighter) */
    @Test
    public void testInitRGBbyHSL_BrightColor_Magic2_L_GT_Half() {
        // H=0 (Red), S=255 (Max), L=200 (Bright)
        testColor.initRGBbyHSL(0, 255, 200);
        // Magic2 = 200 + 255 - ((200 * 255) + 127) / 255 = 455 - 200 = 255 (approx)
        assertTrue(testColor.getRed() > testColor.getGreen());
        assertTrue(testColor.getRed() > testColor.getBlue());
    }

    /** Test for pRed > RGBMAX branch in initRGBbyHSL (unlikely to naturally trigger but for coverage) */
    @Test
    public void testInitRGBbyHSL_pRedBoundary() {
        // Test an extreme valid case that results in R=255 to ensure calculations are correct.
        testColor.initRGBbyHSL(0, 255, 255); // Pure White, R=255, G=255, B=255
        // Red calculation: (hueToRGB(255, 255, 85) * 255 + 127) / 255
        // hueToRGB(255, 255, 85) returns 255. (255 * 255 + 127) / 255 = 255.5 -> 255.
        // The boundary checks for R, G, and B are hard to trigger naturally given the fixed HSLMAX.
        // We cannot reliably cover the `if (pRed > RGBMAX)` lines without reflecting on internal state,
        // which falls outside the scope of testing public/private methods.
        assertEquals(255, testColor.getRed());
        assertEquals(255, testColor.getGreen());
        assertEquals(255, testColor.getBlue());
    }


    // --- Setters and Getters Coverage ---

    @Test
    public void testGetters() {
        testColor.initHSLbyRGB(255, 100, 50); // R=255, G=100, B=50. HSL: 11, 255, 153.

        // FIX: Expected 0 corrected to 11
        assertEquals(11, testColor.getHue());
        assertEquals(255, testColor.getSaturation());
        assertEquals(153, testColor.getLuminence());
        assertEquals(255, testColor.getRed());
        assertEquals(100, testColor.getGreen());
        assertEquals(50, testColor.getBlue());
    }

    /** Tests setHue boundary check for iToValue < 0 */
    @Test
    public void testSetHue_NegativeValue() {
        testColor.initHSLbyRGB(128, 128, 128); // Initial L=128
        testColor.setHue(-10); // -10 becomes 245
        assertEquals(245, testColor.getHue());
    }

    /** Tests setHue boundary check for iToValue > HSLMAX */
    @Test
    public void testSetHue_OverMax() {
        testColor.initHSLbyRGB(128, 128, 128);
        testColor.setHue(260); // 260 becomes 5
        assertEquals(5, testColor.getHue());
    }

    @Test
    public void testSetHue_ValidValue() {
        testColor.initHSLbyRGB(128, 128, 128);
        testColor.setHue(100);
        assertEquals(100, testColor.getHue());
    }

    /** Tests setSaturation boundary check for iToValue < 0 */
    @Test
    public void testSetSaturation_NegativeValue() {
        testColor.setSaturation(-5); // Becomes 0
        assertEquals(0, testColor.getSaturation());
    }

    /** Tests setSaturation boundary check for iToValue > HSLMAX */
    @Test
    public void testSetSaturation_OverMax() {
        testColor.setSaturation(300); // Becomes 255
        assertEquals(HSLMAX, testColor.getSaturation());
    }

    @Test
    public void testSetSaturation_ValidValue() {
        testColor.setSaturation(100);
        assertEquals(100, testColor.getSaturation());
    }

    /** Tests setLuminence boundary check for iToValue < 0 */
    @Test
    public void testSetLuminence_NegativeValue() {
        testColor.setLuminence(-5); // Becomes 0
        assertEquals(0, testColor.getLuminence());
    }

    /** Tests setLuminence boundary check for iToValue > HSLMAX */
    @Test
    public void testSetLuminence_OverMax() {
        testColor.setLuminence(300); // Becomes 255
        assertEquals(HSLMAX, testColor.getLuminence());
    }

    @Test
    public void testSetLuminence_ValidValue() {
        testColor.setLuminence(100);
        assertEquals(100, testColor.getLuminence());
    }

    // --- Modification Methods Coverage ---

    @Test
    public void testReverseColor() {
        testColor.initHSLbyRGB(255, 0, 0); // Initial R=255, G=0, B=0 -> H=0, S=255, L=127.
        testColor.reverseColor();
        // H becomes 0 + (255/2) = 127.
        assertEquals(127, testColor.getHue()); // Corrected from 144 to 127
    }

    /** Tests brighten method for fPercent == 0 (early return) */
    @Test
    public void testBrighten_ZeroPercent() {
        testColor.initHSLbyRGB(10, 10, 10); // L=10
        testColor.brighten(0);
        // Luminance should remain unchanged
        assertEquals(10, testColor.getLuminence());
    }

    /** Tests brighten method for L < 0 branch */
    @Test
    public void testBrighten_NegativeLuminence() {
        testColor.initHSLbyRGB(200, 200, 200); // L=200
        testColor.brighten(-0.1f);
        // L = (int) (200 * -0.1) = -20. Clamped to L=0.
        assertEquals(0, testColor.getLuminence());
    }

    /** Tests brighten method for L > HSLMAX branch */
    @Test
    public void testBrighten_OverMaxLuminence() {
        testColor.initHSLbyRGB(200, 200, 200); // L=200
        testColor.brighten(2.0f);
        // L = (int) (200 * 2.0) = 400. Clamped to HSLMAX (255).
        assertEquals(HSLMAX, testColor.getLuminence());
    }

    /** Tests brighten method for normal case (0 <= L <= HSLMAX) */
    @Test
    public void testBrighten_NormalCase() {
        testColor.initHSLbyRGB(100, 100, 100); // L=100
        testColor.brighten(1.5f);
        // L = (int) (100 * 1.5) = 150.
        assertEquals(150, testColor.getLuminence());
    }

    /** Tests blend method for fPercent >= 1 (full replacement) */
    @Test
    public void testBlend_FullReplacement() {
        testColor.initHSLbyRGB(10, 20, 30);
        testColor.blend(200, 210, 220, 1.0f); // fPercent = 1.0
        // Should be replaced by (200, 210, 220)
        assertEquals(200, testColor.getRed());
        assertEquals(210, testColor.getGreen());
        assertEquals(220, testColor.getBlue());

        testColor.initHSLbyRGB(10, 20, 30);
        testColor.blend(200, 210, 220, 1.5f); // fPercent > 1.0
        // Should be replaced by (200, 210, 220)
        assertEquals(200, testColor.getRed());
        assertEquals(210, testColor.getGreen());
        assertEquals(220, testColor.getBlue());
    }

    /** Tests blend method for fPercent <= 0 (early return) */
    @Test
    public void testBlend_NoBlending() {
        testColor.initHSLbyRGB(10, 20, 30);
        testColor.blend(200, 210, 220, 0.0f); // fPercent = 0.0
        // Should remain (10, 20, 30)
        assertEquals(10, testColor.getRed());
        assertEquals(20, testColor.getGreen());
        assertEquals(30, testColor.getBlue());

        testColor.blend(200, 210, 220, -0.5f); // fPercent < 0.0
        // Should remain (10, 20, 30)
        assertEquals(10, testColor.getRed());
        assertEquals(20, testColor.getGreen());
        assertEquals(30, testColor.getBlue());
    }

    /** Tests blend method for 0 < fPercent < 1 (normal blend) */
    @Test
    public void testBlend_NormalBlending() {
        testColor.initHSLbyRGB(10, 20, 30); // Old R=10, G=20, B=30
        // Blend with (200, 210, 220) at 50%
        // newR = (200 * 0.5) + (10 * 0.5) = 100 + 5 = 105
        // newG = (210 * 0.5) + (20 * 0.5) = 105 + 10 = 115
        // newB = (220 * 0.5) + (30 * 0.5) = 110 + 15 = 125
        testColor.blend(200, 210, 220, 0.5f);

        // The method calls initHSLbyRGB(105, 115, 125). We assert the final RGB state.
        assertEquals(105, testColor.getRed());
        assertEquals(115, testColor.getGreen());
        assertEquals(125, testColor.getBlue());
    }
}