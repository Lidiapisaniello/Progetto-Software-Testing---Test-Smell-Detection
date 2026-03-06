/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: la.trani@studenti.unina.it
UserID: 1014
Date: 25/11/2025
*/
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- Tests for initHSLbyRGB ---

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // cMax == cMin case
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Red should be 100", 100, hslColor.getRed());
        assertEquals("Green should be 100", 100, hslColor.getGreen());
        assertEquals("Blue should be 100", 100, hslColor.getBlue());
        assertEquals("Saturation should be 0", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED (170)", UNDEFINED, hslColor.getHue());
        // (100+100)*255 + 255 / 2*255 = ~100
        assertEquals("Luminence should be calculated correctly", 100, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // cMax == R case
        hslColor.initHSLbyRGB(255, 0, 0);
        
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen());
        assertEquals(0, hslColor.getBlue());
        
        // Lum: ((255*255)+255)/510 = 128
        assertEquals(128, hslColor.getLuminence());
        // Sat: Lum > 127 -> (255*255 + 0.5) / (510 - 255) = 255
        assertEquals(255, hslColor.getSaturation());
        // Hue: R is Max. G=0, B=0. Delta match. Hue ~ 0.
        assertEquals(0, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // cMax == G case
        hslColor.initHSLbyRGB(0, 255, 0);
        
        // Hue calculation for G max involves HSLMAX/3 (approx 85)
        // With pure green, expect Hue ~ 85
        assertEquals(85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // cMax == B case
        hslColor.initHSLbyRGB(0, 0, 255);
        
        // Hue calculation for B max involves 2*HSLMAX/3 (approx 170)
        assertEquals(170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_LowLuminance() {
        // Covers pLum <= (HSLMAX / 2)
        // R=50, G=50, B=0 -> cMax=50, cMin=0. Plus=50.
        // Lum = (50*255 + 255) / 510 = 25. (25 <= 127)
        hslColor.initHSLbyRGB(50, 50, 0);
        
        assertTrue("Luminence should be low", hslColor.getLuminence() <= 127);
        assertEquals("Saturation should be max for pure mix", 255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_HighLuminance() {
        // Covers pLum > (HSLMAX / 2)
        hslColor.initHSLbyRGB(200, 200, 255);
        assertTrue("Luminence should be high", hslColor.getLuminence() > 127);
    }

    @Test
    public void testInitHSLbyRGB_HueNegativeWrap() {
        // Forces pHue < 0 inside the calculation before correction
        // Requires cMax == R, and BDelta > GDelta?
        // Or specific delta combination. 
        // Let's try R=255, G=0, B=20.
        // cMax=255, cMin=0. cMinus=255.
        // Hue = BDelta - GDelta.
        // GDelta approx 42. BDelta approx (255-20)*42/255 = 39.
        // Hue = 39 - 42 = -3. Should wrap to 255 + (-3) = 252.
        
        hslColor.initHSLbyRGB(255, 0, 20);
        int h = hslColor.getHue();
        assertTrue("Hue should be positive after wrap", h >= 0);
        assertTrue("Hue should be high (wrapped red)", h > 200);
    }

    // --- Tests for initRGBbyHSL (and indirectly hueToRGB) ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // S == 0 case
        hslColor.initRGBbyHSL(0, 0, 100);
        
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_LowLum_MagicLogic() {
        // L <= HSLMAX/2
        // H=0 (Red), S=255, L=64
        hslColor.initRGBbyHSL(0, 255, 64);
        
        // Check reasonable conversion back
        assertEquals(0, hslColor.getHue());
        assertEquals(64, hslColor.getLuminence());
        assertTrue(hslColor.getRed() > hslColor.getBlue()); 
    }

    @Test
    public void testInitRGBbyHSL_HighLum_MagicLogic() {
        // L > HSLMAX/2
        // H=85 (Green), S=255, L=200
        hslColor.initRGBbyHSL(85, 255, 200);
        
        assertEquals(85, hslColor.getHue());
        assertEquals(200, hslColor.getLuminence());
        assertTrue(hslColor.getGreen() > hslColor.getRed());
    }

    // --- Tests for hueToRGB internal branches ---
    // hueToRGB is private, so we exercise it via initRGBbyHSL with specific H values.
    // The method handles wrapping H < 0 and H > HSLMAX.
    // It has regions: < 1/6, < 1/2, < 2/3, else.
    // HSLMAX = 255. 
    // 1/6 = 42. 1/2 = 127. 2/3 = 170.

    @Test
    public void testHueToRGB_FirstRegion() {
        // Hue < 42. 
        // We set H=0. 
        // pGreen calls hueToRGB with H. 0 < 42. Hits first if.
        hslColor.initRGBbyHSL(10, 255, 128); 
        // Just verify it runs without exception and produces valid RGB
        assertTrue(hslColor.getRed() >= 0 && hslColor.getRed() <= 255);
    }

    @Test
    public void testHueToRGB_SecondRegion() {
        // 42 <= Hue < 127.
        // H = 85 (Green).
        // pGreen calls hueToRGB(..., 85). 85 > 42 and 85 < 127. Returns Magic2.
        hslColor.initRGBbyHSL(85, 255, 128);
        assertEquals(255, hslColor.getGreen()); // Max saturation/lum logic
    }

    @Test
    public void testHueToRGB_ThirdRegion() {
        // 127 <= Hue < 170.
        // H = 150.
        hslColor.initRGBbyHSL(150, 255, 128);
        assertTrue(hslColor.getBlue() > 0);
    }

    @Test
    public void testHueToRGB_FourthRegion() {
        // Hue >= 170.
        // H = 200.
        hslColor.initRGBbyHSL(200, 255, 128);
        assertTrue(hslColor.getBlue() > hslColor.getRed());
    }
    
    @Test
    public void testHueToRGB_OverflowClamp() {
        // This targets the specific check: if (pRed > RGBMAX) pRed = RGBMAX;
        // It's hard to trigger with valid HSL math because HSL logic usually stays within bounds,
        // but we verify the code handles standard boundaries correctly.
        hslColor.initRGBbyHSL(0, 255, 128);
        assertEquals(255, hslColor.getRed()); // Should be exactly max, not overflow
    }

    // --- Tests for Getters and Setters with Logic ---

    @Test
    public void testSetHue_CyclePositive() {
        // Hue > HSLMAX
        hslColor.setHue(255 + 10); // 265
        assertEquals(10, hslColor.getHue());
        
        hslColor.setHue(520); // 255 * 2 + 10
        assertEquals(10, hslColor.getHue());
    }

    @Test
    public void testSetHue_CycleNegative() {
        // Hue < 0
        hslColor.setHue(-10); // Should become 245
        assertEquals(245, hslColor.getHue());
        
        hslColor.setHue(-265); // -255 - 10 -> 245
        assertEquals(245, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(100);
        assertEquals(100, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());

        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(100);
        assertEquals(100, hslColor.getLuminence());

        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());

        hslColor.setLuminence(-50);
        assertEquals(0, hslColor.getLuminence());
    }

    // --- Tests for Utility Methods ---

    @Test
    public void testReverseColor() {
        hslColor.setHue(0);
        hslColor.reverseColor();
        // 0 + 127 (HSLMAX/2 integer div) = 127
        assertEquals(127, hslColor.getHue());
        
        hslColor.setHue(200);
        hslColor.reverseColor();
        // 200 + 127 = 327 -> wrap -> 327 - 255 = 72
        assertEquals(72, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.initHSLbyRGB(100, 100, 100);
        int initialLum = hslColor.getLuminence(); // 100
        
        // Test 0 percent (return early)
        hslColor.brighten(0.0f);
        assertEquals(initialLum, hslColor.getLuminence());

        // Test normal brightening
        hslColor.brighten(1.5f);
        assertEquals(150, hslColor.getLuminence()); // 100 * 1.5

        // Test ceiling
        hslColor.brighten(10.0f); // 1500 -> clamp 255
        assertEquals(255, hslColor.getLuminence());

        // Test floor (negative percent? Implementation casts to int directly)
        // If pLum is 255, fPercent -1.0 -> -255 -> clamp 0
        hslColor.setLuminence(100);
        hslColor.brighten(-0.5f); // -50 -> 0
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        // Setup initial color: Red
        hslColor.initHSLbyRGB(255, 0, 0);

        // Blend with Green (0, 255, 0) at 0.5
        hslColor.blend(0, 255, 0, 0.5f);

        // Expected: R = 255*0.5 + 0*0.5 = 127
        // G = 0*0.5 + 255*0.5 = 127
        // B = 0
        assertEquals(127, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(0, hslColor.getBlue());
    }

    @Test
    public void testBlend_Boundaries() {
        hslColor.initHSLbyRGB(255, 0, 0);

        // Percent >= 1 -> Full replacement
        hslColor.blend(0, 0, 255, 1.5f);
        assertEquals(0, hslColor.getRed());
        assertEquals(255, hslColor.getBlue());

        // Percent <= 0 -> No change
        hslColor.blend(0, 255, 0, -0.5f);
        assertEquals(255, hslColor.getBlue()); // Remains blue
        assertEquals(0, hslColor.getGreen());
    }
    
    @Test
    public void testIMaxIMinCoverage() {
        // Implicitly testing iMax and iMin via initHSLbyRGB
        // Case: a > b
        hslColor.initHSLbyRGB(200, 100, 50); 
        assertEquals(200, hslColor.getRed()); // Max is R
        
        // Case: a < b
        hslColor.initHSLbyRGB(50, 100, 200); 
        assertEquals(200, hslColor.getBlue()); // Max is B
        
        // Case: a == b
        hslColor.initHSLbyRGB(100, 100, 100); // Trigger equality paths
        assertEquals(100, hslColor.getLuminence());
    }
}