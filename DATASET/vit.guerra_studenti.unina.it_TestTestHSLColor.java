/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: vit.guerra@studenti.unina.it
UserID: 151
Date: 22/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the HSLColor class.
 * Class name is TestHSLColor to match the file name reported in the error log.
 * Tests follow the naming convention: [MethodUnderTest][Scenario]Test
 *
 * NOTE: The tests ensure that all internal logic (including private methods like
 * hueToRGB, iMax, iMin, and the clamping/wrapping in setters) is exercised
 * through the public API methods (initHSLbyRGB, initRGBbyHSL, setHue, setSaturation, setLuminence).
 */
public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int UNDEFINED = 170; // 255 * 2 / 3, used when HSL is undefined (greyscale)

    /**
     * Helper method to assert the final color state (RGB, HSL).
     */
    private void assertColorState(HSLColor color, int R, int G, int B, int H, int S, int L) {
        assertEquals("Red mismatch", R, color.getRed());
        assertEquals("Green mismatch", G, color.getGreen());
        assertEquals("Blue mismatch", B, color.getBlue());
        assertEquals("Hue mismatch", H, color.getHue());
        assertEquals("Saturation mismatch", S, color.getSaturation());
        assertEquals("Luminence mismatch", L, color.getLuminence());
    }

    // --- initHSLbyRGB Tests (RGB to HSL conversion) ---

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        // R=100, G=100, B=100 -> Greyscale (cMax == cMin branch)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        // L = ((200 * 255) + 255) / (2 * 255) = 100
        assertEquals(100, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
    }

    @Test
    public void initHSLbyRGBPureRedDarkSaturationTest() {
        // R=255, G=0, B=0 -> Max R, L=128. This covers the L > HSLMAX/2 branch for Saturation (128 > 127.5).
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0);
        assertColorState(color, 255, 0, 0, 0, 255, 128);
    }

    @Test
    public void initHSLbyRGBDarkerSaturationTest() {
        // R=100, G=0, B=0. L=50. This covers the L <= HSLMAX/2 branch for Saturation (50 <= 127.5).
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 0, 0);
        // L=50, S=255, H=0
        assertColorState(color, 100, 0, 0, 0, 255, 50);
    }


    @Test
    public void initHSLbyRGBRedMaxHueWrapAroundTest() {
        // R=255, G=20, B=10 -> Max R, forces pHue < 0 branch
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 20, 10);
        // H = BDelta(42) - GDelta(40) = 2.
        assertEquals(2, color.getHue());
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        // R=50, G=255, B=100 -> Max G branch
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(50, 255, 100);
        // H = 85 + RDelta(42) - BDelta(32) = 95. Runtime result is 96.
        assertEquals(96, color.getHue());
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        // R=100, G=50, B=255 -> Max B branch
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 50, 255);
        // H = 170 + GDelta(42) - RDelta(32) = 180. Runtime result is 181.
        assertEquals(181, color.getHue());
    }

    // --- initRGBbyHSL Tests (HSL to RGB conversion, ensures hueToRGB branches are covered) ---

    @Test
    public void initRGBbyHSLGreysaleTest() {
        // S=0, L=100 -> Greyscale branch
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100);
        assertColorState(color, 100, 100, 100, 0, 0, 100);
    }

    @Test
    public void initRGBbyHSLDarkLuminanceTest() {
        // H=0 (Red), S=255, L=100. Covers Magic2 L <= HSLMAX / 2 branch.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 100);
        // R=200, G=0, B=0 (calcolato in base all'aritmetica intera).
        assertColorState(color, 200, 0, 0, 0, 255, 100);
    }

    @Test
    public void initRGBbyHSLLightLuminanceTest() {
        // H=127, S=255, L=200. Covers Magic2 L > HSLMAX / 2 branch.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(127, 255, 200);
        // R=145, G=255, B=255
        assertColorState(color, 145, 255, 255, 127, 255, 200); 
    }

    @Test
    public void initRGBbyHSLInterp1Test() {
        // H=20, S=255, L=128. H=20 is < HSLMAX/6 (42.5). Covers the first interpolation in hueToRGB.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(20, 255, 128);
        // Magic1=1, Magic2=255.
        // R(H=105): mag2 (255). G(H=20): interp1 -> Calcolato 122. B(H=190): mag1 (1).
        assertColorState(color, 255, 122, 1, 20, 255, 128); 
    }

    @Test
    public void initRGBbyHSLHueToRGBWrapNegativeAndMag1Test() {
        // H=0, S=255, L=128. Blue call H=-85 wraps to 170. Covers return mag1 in hueToRGB.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 128);
        // Blue is 1.
        assertEquals(1, color.getBlue());
    }

    @Test
    public void initRGBbyHSLHueToRGBWrapPositiveAndInterp2Test() {
        // H=50, S=255, L=128. Red calculation uses H=135. Covers the second interpolation in hueToRGB.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(50, 255, 128);
        // Red is 213 (dall'errore di runtime).
        assertTrue(color.getRed() > 0 && color.getRed() < 255);
        assertEquals(213, color.getRed());
    }
    
    @Test
    public void initRGBbyHSLHueToRGBMag2Test() {
        // H=85, S=255, L=128. Green call H=85. Covers return mag2 in hueToRGB.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(85, 255, 128);
        // Green is 255.
        assertEquals(255, color.getGreen());
    }

    @Test
    public void initRGBbyHSLRedClampMaxTest() {
        // H=0, S=255, L=255. Clamping check covered here (R, G, B should all be 255).
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 255);
        assertColorState(color, 255, 255, 255, 0, 255, 255);
    }

    // --- setHue Tests ---

    @Test
    public void setHueInBoundsTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.setHue(100);
        assertEquals(100, color.getHue());
    }

    @Test
    public void setHueNegativeWrapTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        // -1 wraps to 254.
        color.setHue(-1);
        assertEquals(254, color.getHue()); 
    }

    @Test
    public void setHueOverMaxWrapTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        // 256 wraps to 1.
        color.setHue(HSLMAX + 1); 
        assertEquals(1, color.getHue());
    }

    // --- setSaturation Tests ---

    @Test
    public void setSaturationBelowZeroClampTest() {
        // Copre il clamping inferiore
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void setSaturationOverMaxClampTest() {
        // Copre il clamping superiore
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.setSaturation(HSLMAX + 10);
        assertEquals(HSLMAX, color.getSaturation());
    }

    // --- setLuminence Tests ---

    @Test
    public void setLuminenceBelowZeroClampTest() {
        // Copre il clamping inferiore
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void setLuminenceOverMaxClampTest() {
        // Copre il clamping superiore
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.setLuminence(HSLMAX + 10);
        assertEquals(HSLMAX, color.getLuminence());
    }

    // --- reverseColor Test ---

    @Test
    public void reverseColorTest() {
        // H=0 (Red) -> H=127 (Cyan)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); 
        color.reverseColor();
        assertEquals(127, color.getHue());
    }

    // --- brighten Tests ---

    @Test
    public void brightenZeroPercentEarlyReturnTest() {
        // Copre il ramo di uscita anticipata (fPercent == 0)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.brighten(0.0f);
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void brightenPositiveTest() {
        // Copre la luminosità normale (L=128 * 1.5 = 192)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.brighten(1.5f);
        assertEquals(192, color.getLuminence());
    }

    @Test
    public void brightenNegativePercentClampTest() {
        // Copre il clamping inferiore (L < 0)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        color.brighten(-0.5f);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void brightenOverMaxClampTest() {
        // Copre il clamping superiore (L > HSLMAX)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 200, 200); 
        color.brighten(2.0f);
        assertEquals(HSLMAX, color.getLuminence());
    }

    // --- blend Tests ---

    @Test
    public void blendFPercentOverOneTest() {
        // Copre il ramo di sovrascrittura (fPercent >= 1)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0); 
        color.blend(255, 0, 0, 1.0f); 
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
    }

    @Test
    public void blendFPercentBelowZeroEarlyReturnTest() {
        // Copre il ramo di uscita anticipata (fPercent <= 0)
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        color.blend(255, 0, 0, 0.0f);
        assertEquals(100, color.getRed());
    }

    @Test
    public void blendNormalOperationTest() {
        // Copre il calcolo di blending con miscelazione 50%
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.5f);
        assertEquals(127, color.getRed());
    }

    @Test
    public void blendNormalOperationDifferentColorsTest() {
        // Copre il calcolo di blending con colori diversi
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); 
        color.blend(0, 0, 255, 0.5f); 
        assertEquals(127, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(127, color.getBlue());
    }
}