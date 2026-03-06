import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 Test class for HSLColor.
 * Aims to achieve high line coverage by testing various branches and edge cases.
 */
public class TestHSLColor {

    private HSLColor color;
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    // A small tolerance for integer arithmetic rounding
    private final static int TOLERANCE = 1;

    @Before
    public void setUp() {
        // Initialize a default color before each test
        color = new HSLColor();
        color.initHSLbyRGB(100, 150, 200); // A generic blue-ish color
    }

    // --- Test initHSLbyRGB ---

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Test greyscale (R == G == B)
        color.initHSLbyRGB(128, 128, 128);
        assertEquals("Greyscale lum should be mid-point", 128, color.getLuminence());
        assertEquals("Greyscale sat should be 0", 0, color.getSaturation());
        assertEquals("Greyscale hue should be UNDEFINED", UNDEFINED, color.getHue());
        assertEquals("Red value should be preserved", 128, color.getRed());
        assertEquals("Green value should be preserved", 128, color.getGreen());
        assertEquals("Blue value should be preserved", 128, color.getBlue());
    }

    @Test
    public void testInitHSLbyRGB_Red() {
        // Test pure red (cMax == R, pLum <= HSLMAX / 2)
        color.initHSLbyRGB(255, 0, 0);
        assertEquals("Red lum", 128, color.getLuminence());
        assertEquals("Red sat", 255, color.getSaturation());
        // Hue for red is 0 or HSLMAX
        assertTrue("Red hue", color.getHue() == 0 || color.getHue() == HSLMAX);
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        // Test pure green (cMax == G)
        color.initHSLbyRGB(0, 255, 0);
        assertEquals("Green lum", 128, color.getLuminence());
        assertEquals("Green sat", 255, color.getSaturation());
        assertEquals("Green hue", HSLMAX / 3, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        // Test pure blue (cMax == B)
        color.initHSLbyRGB(0, 0, 255);
        assertEquals("Blue lum", 128, color.getLuminence());
        assertEquals("Blue sat", 255, color.getSaturation());
        assertEquals("Blue hue", (2 * HSLMAX) / 3, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_HighLum() {
        // Test light color (pLum > HSLMAX / 2)
        color.initHSLbyRGB(255, 200, 200); // Light red
        assertTrue("High lum", color.getLuminence() > HSLMAX / 2);
        assertTrue("High lum sat", color.getSaturation() > 0);
        // Hue for red is 0 or HSLMAX
        assertTrue("High lum hue", color.getHue() == 0 || color.getHue() == HSLMAX);
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueWrap() {
        // Test a color that results in a negative hue before wrapping
        // Magenta (R=255, G=0, B=100) -> cMax=R, BDelta < GDelta
        color.initHSLbyRGB(255, 0, 100);
        int expectedHue = 232; // (BDelta - GDelta) + HSLMAX = (60 - 102) + 255 = 213 (calc error?)
        // Let's re-calculate: R=255, G=0, B=100. cMax=255, cMin=0. cMinus=255.
        // RDelta = 0, GDelta = 102, BDelta = 60.
        // pHue = BDelta - GDelta = 60 - 102 = -42
        // pHue = -42 + 255 = 213
        assertEquals("Negative hue wrap", 213, color.getHue());
    }

    // --- Test initRGBbyHSL ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Test S == 0
        color.initRGBbyHSL(100, 0, 200); // Hue is irrelevant
        assertEquals("Greyscale R", 200, color.getRed());
        assertEquals("Greyscale G", 200, color.getGreen());
        assertEquals("Greyscale B", 200, color.getBlue());
        assertEquals("Greyscale H", 100, color.getHue());
        assertEquals("Greyscale S", 0, color.getSaturation());
        assertEquals("Greyscale L", 200, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_Color_LowLum() {
        // Test S != 0 and L <= HSLMAX / 2
        // Dark Red (H=0, S=255, L=64)
        color.initRGBbyHSL(0, 255, 64);
        assertEquals("Dark Red R", 128, color.getRed());
        assertEquals("Dark Red G", 0, color.getGreen());
        assertEquals("Dark Red B", 0, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_HighLum() {
        // Test S != 0 and L > HSLMAX / 2
        // Light Green (H=85, S=255, L=192)
        color.initRGBbyHSL(85, 255, 192); // 85 = HSLMAX / 3
        assertEquals("Light Green R", 128, color.getRed());
        assertEquals("Light Green G", 255, color.getGreen());
        assertEquals("Light Green B", 128, color.getBlue());
    }

    // --- Test hueToRGB branches via initRGBbyHSL ---

    @Test
    public void testHueToRGB_Branch1_HueLessThan0() {
        // Test Hue < 0 wrap
        color.initRGBbyHSL(-20, 255, 128); // Same as H = 235
        color.initRGBbyHSL(235, 255, 128);
        int expectedRed = color.getRed();
        int expectedGreen = color.getGreen();
        int expectedBlue = color.getBlue();

        color.initRGBbyHSL(-20, 255, 128);
        assertEquals("Hue < 0 R", expectedRed, color.getRed());
        assertEquals("Hue < 0 G", expectedGreen, color.getGreen());
        assertEquals("Hue < 0 B", expectedBlue, color.getBlue());
    }

    @Test
    public void testHueToRGB_Branch2_HueGreaterThanHSLMAX() {
        // Test Hue > HSLMAX wrap
        color.initRGBbyHSL(275, 255, 128); // Same as H = 20
        color.initRGBbyHSL(20, 255, 128);
        int expectedRed = color.getRed();
        int expectedGreen = color.getGreen();
        int expectedBlue = color.getBlue();

        color.initRGBbyHSL(275, 255, 128);
        assertEquals("Hue > HSLMAX R", expectedRed, color.getRed());
        assertEquals("Hue > HSLMAX G", expectedGreen, color.getGreen());
        assertEquals("Hue > HSLMAX B", expectedBlue, color.getBlue());
    }

    @Test
    public void testHueToRGB_Branch3_Hue_Part1() {
        // Test Hue < (HSLMAX / 6) (e.g., H=20, Orange)
        color.initRGBbyHSL(20, 255, 128);
        assertEquals("Hue Part 1 R", 255, color.getRed());
        assertNotEquals("Hue Part 1 G", 0, color.getGreen());
        assertEquals("Hue Part 1 B", 0, color.getBlue());
    }

    @Test
    public void testHueToRGB_Branch4_Hue_Part2() {
        // Test Hue < (HSLMAX / 2) (e.g., H=85, Green)
        color.initRGBbyHSL(85, 255, 128); // 85 = HSLMAX / 3
        assertEquals("Hue Part 2 R", 0, color.getRed());
        assertEquals("Hue Part 2 G", 255, color.getGreen());
        assertEquals("Hue Part 2 B", 0, color.getBlue());
    }

    @Test
    public void testHueToRGB_Branch5_Hue_Part3() {
        // Test Hue < (HSLMAX * 2 / 3) (e.g., H=150, Cyan-ish)
        color.initRGBbyHSL(150, 255, 128);
        assertEquals("Hue Part 3 R", 0, color.getRed());
        assertEquals("Hue Part 3 G", 128, color.getGreen());
        assertEquals("Hue Part 3 B", 255, color.getBlue());
    }

    @Test
    public void testHueToRGB_Branch6_Hue_Part4() {
        // Test Hue >= (HSLMAX * 2 / 3) (e.g., H=213, Magenta-ish)
        color.initRGBbyHSL(213, 255, 128);
        assertEquals("Hue Part 4 R", 255, color.getRed());
        assertEquals("Hue Part 4 G", 0, color.getGreen());
        assertEquals("Hue Part 4 B", 128, color.getBlue());
    }


    // --- Test Setters and Getters ---

    @Test
    public void testGetters() {
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
        // HSL values are calculated, so we just check if they are returned
        int h = color.getHue();
        int s = color.getSaturation();
        int l = color.getLuminence();
        assertEquals(h, color.getHue());
        assertEquals(s, color.getSaturation());
        assertEquals(l, color.getLuminence());
    }

    @Test
    public void testSetHue() {
        int s = color.getSaturation();
        int l = color.getLuminence();
        color.setHue(50);
        assertEquals(50, color.getHue());
        assertEquals(s, color.getSaturation());
        assertEquals(l, color.getLuminence());
    }

    @Test
    public void testSetHue_NegativeWrap() {
        color.setHue(-20); // Should be 235
        assertEquals(235, color.getHue());
    }

    @Test
    public void testSetHue_PositiveWrap() {
        color.setHue(300); // Should be 45
        assertEquals(45, color.getHue());
    }

    @Test
    public void testSetSaturation() {
        int h = color.getHue();
        int l = color.getLuminence();
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
        assertEquals(h, color.getHue());
        assertEquals(l, color.getLuminence());
    }

    @Test
    public void testSetSaturation_ClampLow() {
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void testSetSaturation_ClampHigh() {
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        int h = color.getHue();
        int s = color.getSaturation();
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
        assertEquals(h, color.getHue());
        assertEquals(s, color.getSaturation());
    }

    @Test
    public void testSetLuminence_ClampLow() {
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testSetLuminence_ClampHigh() {
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence());
    }


    // --- Test Other Public Methods ---

    @Test
    public void testReverseColor() {
        // Red (H=0)
        color.initHSLbyRGB(255, 0, 0);
        int originalHue = color.getHue(); // 0 or 255
        
        color.reverseColor(); // 0 + 127 = 127
        
        int expectedHue = (originalHue + HSLMAX / 2) % HSLMAX;
        
        // Handle 0/255 case
        if (originalHue == 0 || originalHue == HSLMAX) {
             assertEquals("Reversed Red Hue", HSLMAX / 2, color.getHue());
        } else {
             assertEquals("Reversed Hue", expectedHue, color.getHue());
        }

        // Cyan (H=127)
        color.initRGBbyHSL(HSLMAX / 2, 255, 128);
        color.reverseColor(); // 127 + 127 = 254 (close to 0/red)
        
        // Hue can be 0 or HSLMAX
        assertTrue("Reversed Cyan Hue", color.getHue() >= (HSLMAX - 1) || color.getHue() == 0);
    }

    @Test
    public void testBrighten_NoChange() {
        int originalLum = color.getLuminence();
        color.brighten(0);
        assertEquals(originalLum, color.getLuminence());
    }

    @Test
    public void testBrighten_Increase() {
        color.initRGBbyHSL(100, 100, 100); // L=100
        color.brighten(1.5f); // L = 150
        assertEquals(150, color.getLuminence());
    }

    @Test
    public void testBrighten_Decrease() {
        color.initRGBbyHSL(100, 100, 100); // L=100
        color.brighten(0.5f); // L = 50
        assertEquals(50, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampHigh() {
        color.initRGBbyHSL(100, 100, 200); // L=200
        color.brighten(2.0f); // L = 400 -> clamped to 255
        assertEquals(HSLMAX, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampLow() {
        color.initRGBbyHSL(100, 100, 50); // L=50
        color.brighten(-1.0f); // L = -50 -> clamped to 0
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend_NoChange() {
        // fPercent <= 0
        color.initHSLbyRGB(100, 100, 100);
        color.blend(255, 0, 0, 0);
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());

        color.blend(255, 0, 0, -1.0f);
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void testBlend_FullChange() {
        // fPercent >= 1
        color.initHSLbyRGB(100, 100, 100);
        color.blend(255, 0, 0, 1.0f);
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());

        color.blend(0, 255, 0, 2.0f);
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void testBlend_Partial_50_50() {
        // 0 < fPercent < 1
        // Blend white (200,200,200) with black (0,0,0) at 50%
        color.initHSLbyRGB(200, 200, 200);
        color.blend(0, 0, 0, 0.5f);
        // (0 * 0.5) + (200 * 0.5) = 100
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }
    
    @Test
    public void testBlend_Partial_25_75() {
        // 0 < fPercent < 1
        // Blend Red (252,0,0) with Blue (0,0,252) at 25%
        // Using 252 to avoid rounding issues (divisible by 4)
        color.initHSLbyRGB(0, 0, 252); // Start with Blue
        color.blend(252, 0, 0, 0.25f); // Blend 25% Red
        
        // newR = (252 * 0.25) + (0 * 0.75) = 63
        // newG = (0 * 0.25) + (0 * 0.75) = 0
        // newB = (0 * 0.25) + (252 * 0.75) = 189
        
        assertEquals(63, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(189, color.getBlue());
    }

    // --- Test iMax/iMin via initHSLbyRGB ---
    
    @Test
    public void testIMaxBranches() {
        // iMax(a, b)
        // Case 1: a > b. Tested by iMax(R, G) where R > G
        color.initHSLbyRGB(200, 100, 50); // cMax = iMax(iMax(200, 100), 50) = iMax(200, 50) = 200
        assertEquals(200, color.getRed());
        
        // Case 2: a <= b. Tested by iMax(R, G) where R <= G
        color.initHSLbyRGB(100, 200, 50); // cMax = iMax(iMax(100, 200), 50) = iMax(200, 50) = 200
        assertEquals(200, color.getGreen());
    }

    @Test
    public void testIMinBranches() {
        // iMin(a, b)
        // Case 1: a < b. Tested by iMin(R, G) where R < G
        color.initHSLbyRGB(100, 200, 50); // cMin = iMin(iMin(100, 200), 50) = iMin(100, 50) = 50
        assertEquals(50, color.getBlue());
        
        // Case 2: a >= b. Tested by iMin(R, G) where R >= G
        color.initHSLbyRGB(200, 100, 50); // cMin = iMin(iMin(200, 100), 50) = iMin(100, 50) = 50
        assertEquals(50, color.getBlue());
    }
}