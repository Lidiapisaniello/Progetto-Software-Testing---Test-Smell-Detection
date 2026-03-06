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

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Case: cMax == cMin -> Greyscale
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Red should be 100", 100, hslColor.getRed());
        assertEquals("Green should be 100", 100, hslColor.getGreen());
        assertEquals("Blue should be 100", 100, hslColor.getBlue());
        
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED (170) for greyscale", UNDEFINED, hslColor.getHue());
        // Calculation: ((200 * 255) + 255) / (2 * 255) = (51000 + 255) / 510 = 100
        assertEquals("Luminance calculation verification", 100, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // Case: cMax == R (Pure Red)
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Lum: ((255*255) + 255) / 510 = 127
        assertEquals(127, hslColor.getLuminence());
        // Sat: (pLum <= 127). ((255*255) + 0.5) / 255 = 255
        assertEquals(255, hslColor.getSaturation());
        // Hue: R is max. GDelta=42, BDelta=42. Hue = 0.
        assertEquals(0, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // Case: cMax == G (Pure Green)
        hslColor.initHSLbyRGB(0, 255, 0);
        
        assertEquals(255, hslColor.getGreen());
        // Hue: (HSLMAX / 3) + RDelta - BDelta. 85 + 42 - 42 = 85.
        assertEquals(85, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // Case: cMax == B (Pure Blue)
        hslColor.initHSLbyRGB(0, 0, 255);
        
        assertEquals(255, hslColor.getBlue());
        // Hue: (2 * HSLMAX) / 3 + GDelta - RDelta. 170 + 42 - 42 = 170.
        assertEquals(170, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_HueWrapAround() {
        // Testing logic where pHue < 0 -> pHue + HSLMAX
        // Need a case where cMax == R, and BDelta < GDelta.
        // R=Max, G is closer to Max than B is to Max.
        // R=255, G=200, B=0.
        // Max=255, Min=0. Minus=255.
        // GDelta = ((55 * 42) + 0.5)/255 = 9.
        // BDelta = ((255 * 42) + 0.5)/255 = 42.
        // Hue = 42 - 9 = 33. (Positive).
        
        // Try R=255, G=0, B=100.
        // Max=255, Min=0. Minus=255.
        // GDelta = 42.
        // BDelta = ((155*42)+0.5)/255 = 25.
        // Hue = 25 - 42 = -17.
        // -17 + 255 = 238.
        
        hslColor.initHSLbyRGB(255, 0, 100);
        assertEquals(238, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_HighLuminanceSaturation() {
        // Case: pLum > (HSLMAX / 2)
        // White is Lum 255, but Sat 0.
        // Need color: R=255, G=255, B=200.
        // Max=255, Min=200. Plus=455. Minus=55.
        // Lum = (455*255 + 255)/510 = 227. ( > 127).
        // Sat = (55*255 + 0.5) / (510 - 455) = 14025.5 / 55 = 255.
        
        hslColor.initHSLbyRGB(255, 255, 200);
        assertEquals(227, hslColor.getLuminence());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // S == 0
        hslColor.initRGBbyHSL(0, 0, 128);
        
        // Expected RGB: (128 * 255) / 255 = 128
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLuminance() {
        // L <= HSLMAX/2
        // H=0 (Red), S=255, L=100
        hslColor.initRGBbyHSL(0, 255, 100);
        
        // Magic2 = (100 * (255+255) + 127) / 255 = 200.
        // Magic1 = 200 - 200 = 0.
        // R should be high, G/B low.
        
        assertTrue(hslColor.getRed() > 0);
        assertEquals(0, hslColor.getGreen()); // derived from calculation
        assertEquals(0, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_HighLuminance() {
        // L > HSLMAX/2
        // H=0, S=255, L=200.
        hslColor.initRGBbyHSL(0, 255, 200);
        
        assertTrue(hslColor.getRed() > hslColor.getGreen());
        assertTrue(hslColor.getRed() > hslColor.getBlue());
    }

    @Test
    public void testHueToRGB_CoverageRegions() {
        // This tests internal private method hueToRGB via public initRGBbyHSL
        // We need to trigger different return statements in hueToRGB(mag1, mag2, Hue)
        
        // Region 1: Hue < HSLMAX/6 (approx 42) -> Red (H=0)
        hslColor.initRGBbyHSL(0, 255, 128);
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen());
        
        // Region 2: Hue < HSLMAX/2 (approx 127) -> Green (H=85)
        // 85 is approx 1/3, which is > 1/6 and < 1/2.
        hslColor.initRGBbyHSL(85, 255, 128);
        assertEquals(0, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        
        // Region 3: Hue < HSLMAX*2/3 (approx 170) -> Blue-ish transition
        // 150 is between 127 and 170.
        hslColor.initRGBbyHSL(150, 255, 128);
        assertTrue(hslColor.getBlue() > 0);
        
        // Region 4: Else (Hue > 2/3) -> Blue (H=170 or greater)
        hslColor.initRGBbyHSL(170, 255, 128);
        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void testSetHue_WrapsAndModulo() {
        hslColor.initHSLbyRGB(255, 0, 0); // Base red
        
        // Test upper overflow
        hslColor.setHue(300); // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
        
        // Test lower underflow
        hslColor.setHue(-10); // -10 + 255 = 245
        assertEquals(245, hslColor.getHue());
        
        // Verify it updated RGB
        hslColor.setHue(0); // Back to red
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getBlue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
        
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());
        
        // 0 Saturation triggers greyscale logic
        assertEquals(hslColor.getRed(), hslColor.getBlue());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(-50);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testReverseColor() {
        // R=255 (H=0). Reverse should add HSLMAX/2 (127) -> 127.
        hslColor.initHSLbyRGB(255, 0, 0);
        hslColor.reverseColor();
        assertEquals(127, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        // Set initial lum to 100
        hslColor.initHSLbyRGB(100, 100, 100); 
        
        // 0 percent - return early, no change
        hslColor.brighten(0.0f);
        assertEquals(100, hslColor.getLuminence());
        
        // 1.5 percent
        hslColor.brighten(1.5f);
        // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());
        
        // Overflow test
        hslColor.brighten(10.0f);
        assertEquals(255, hslColor.getLuminence());
        
        // Underflow test (negative factor not explicitly blocked by param check, but math handles it)
        hslColor.brighten(-1.0f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        // Start Black
        hslColor.initHSLbyRGB(0, 0, 0);
        
        // Blend > 1 -> Full replace
        hslColor.blend(255, 255, 255, 1.5f);
        assertEquals(255, hslColor.getRed());
        
        // Blend <= 0 -> No change
        hslColor.blend(0, 0, 0, 0.0f);
        assertEquals(255, hslColor.getRed());
        
        // Blend 0.5 (Midpoint between White 255 and Black 0 -> 127)
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 0.5f);
        assertEquals(127, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(127, hslColor.getBlue());
    }
    
    @Test
    public void testGetters() {
        // Coverage for simple getters
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }
}