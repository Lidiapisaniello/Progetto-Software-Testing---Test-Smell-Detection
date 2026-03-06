import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {

    // --------------------------------------------------------------------------------
    // initHSLbyRGB Tests
    // --------------------------------------------------------------------------------

    @Test
    public void initHSLbyRGBGrayscaleTest() {
        HSLColor color = new HSLColor();
        // R=G=B results in Grayscale (cMax == cMin)
        color.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Hue should be UNDEFINED (170) for grayscale", 170, color.getHue());
        assertEquals("Saturation should be 0 for grayscale", 0, color.getSaturation());
        // Lum calc: ((100*255 + 100*255)/510) -> simplified logic check
        // Real logic: cMin=100, cMax=100. cPlus=200. Lum = ((200*255)+255)/(2*255) = 100
        assertEquals("Luminance calculation mismatch", 100, color.getLuminence());
    }

    @Test
    public void initHSLbyRGBLowLuminanceTest() {
        HSLColor color = new HSLColor();
        // Low luminance triggers: if (pLum <= (HSLMAX / 2))
        // R=0, G=0, B=50. Max=50, Min=0. Plus=50. Lum ~ 25.
        color.initHSLbyRGB(0, 0, 50);
        
        assertEquals(25, color.getLuminence());
        assertTrue("Saturation should be calculated for low lum", color.getSaturation() > 0);
        // Max is B, so Hue calculated via Blue branch
        // Hue region check
        assertTrue(color.getHue() >= 0 && color.getHue() <= 255);
    }

    @Test
    public void initHSLbyRGBHighLuminanceTest() {
        HSLColor color = new HSLColor();
        // High luminance triggers: else block of (pLum <= (HSLMAX / 2))
        // R=200, G=200, B=255. Max=255, Min=200. Plus=455. Lum > 127.
        color.initHSLbyRGB(200, 200, 255);
        
        assertTrue("Luminance should be > 127", color.getLuminence() > 127);
        assertTrue("Saturation should be calculated for high lum", color.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBRedMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == R
        color.initHSLbyRGB(200, 50, 50);
        // Red is max, Hue should be roughly 0 (or near 255/0 boundary)
        assertEquals(200, color.getRed());
        // Verify we hit the cMax == R branch implicitly by checking Hue range
        // Hue = BDelta - GDelta.
        int hue = color.getHue();
        assertTrue(hue >= 0);
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == G
        color.initHSLbyRGB(50, 200, 50);
        
        // Hue should be around 85 (255/3)
        int hue = color.getHue();
        assertTrue("Hue should be roughly 1/3 of HSLMAX", hue > 80 && hue < 90);
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == B
        color.initHSLbyRGB(50, 50, 200);
        
        // Hue should be around 170 (2*255/3)
        int hue = color.getHue();
        assertTrue("Hue should be roughly 2/3 of HSLMAX", hue > 160 && hue < 180);
    }

    @Test
    public void initHSLbyRGBNegativeHueCorrectionTest() {
        HSLColor color = new HSLColor();
        // We need pHue < 0 to trigger the addition of HSLMAX.
        // Logic: if (cMax == R) pHue = BDelta - GDelta;
        // If GDelta > BDelta, Hue is negative.
        // GDelta is derived from (Max-G). BDelta from (Max-B).
        // To make GDelta large, G must be small (Min).
        // To make BDelta small, B must be large (near Max).
        // R (Max) = 255, B = 200, G = 0.
        color.initHSLbyRGB(255, 0, 200);
        
        // Verify R is preserved
        assertEquals(255, color.getRed());
        // Hue logic would naturally result in negative without the fix
        // Ensure result is normalized positive
        assertTrue("Hue should be normalized to positive", color.getHue() >= 0);
    }

    // --------------------------------------------------------------------------------
    // initRGBbyHSL Tests
    // --------------------------------------------------------------------------------

    @Test
    public void initRGBbyHSLGrayscaleTest() {
        HSLColor color = new HSLColor();
        // S = 0
        color.initRGBbyHSL(100, 0, 128);
        
        assertEquals("Red should equal scaled Lum", 128, color.getRed());
        assertEquals("Green should equal Red", 128, color.getGreen());
        assertEquals("Blue should equal Red", 128, color.getBlue());
    }

    @Test
    public void initRGBbyHSLLowLuminanceTest() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2
        // H=0 (Red), S=255 (Full), L=64 (Dark)
        color.initRGBbyHSL(0, 255, 64);
        
        assertEquals(0, color.getHue());
        assertTrue(color.getRed() > 0);
        // Pure red hue means G and B should be low/zero
        assertTrue(color.getGreen() < color.getRed());
        assertTrue(color.getBlue() < color.getRed());
    }

    @Test
    public void initRGBbyHSLHighLuminanceTest() {
        HSLColor color = new HSLColor();
        // L > HSLMAX/2
        // H=0 (Red), S=255, L=200 (Bright)
        color.initRGBbyHSL(0, 255, 200);
        
        assertTrue(color.getRed() > 0);
        assertTrue(color.getGreen() > 100); // Should be whitish
    }

    @Test
    public void initRGBbyHSLClampUpperBoundsTest() {
        HSLColor color = new HSLColor();
        // To trigger 'if (pRed > RGBMAX)', we need inputs that force the calculation 
        // to exceed 255. 
        // Using L=300 (high) and S=1 (low non-zero) ensures the subtraction term 
        // in Magic2 is minimized, keeping Magic2 around 300, which forces 
        // all RGB channels to ~300 before clamping.
        
        color.initRGBbyHSL(0, 1, 300); 
        
        assertEquals("Red should be clamped to RGBMAX", 255, color.getRed());
        assertEquals("Green should be clamped to RGBMAX", 255, color.getGreen());
        assertEquals("Blue should be clamped to RGBMAX", 255, color.getBlue());
    }

    // --------------------------------------------------------------------------------
    // hueToRGB Logic Tests (via initRGBbyHSL)
    // --------------------------------------------------------------------------------

    @Test
    public void hueToRGBFirstSixthTest() {
        HSLColor color = new HSLColor();
        // Hue < HSLMAX / 6 (approx 42)
        // Hue = 20
        color.initRGBbyHSL(20, 255, 128);
        // Just ensuring no crash and values set, coverage tool will confirm branch
        assertTrue(color.getRed() > 0);
    }

    @Test
    public void hueToRGBSecondRegionTest() {
        HSLColor color = new HSLColor();
        // HSLMAX/6 <= Hue < HSLMAX/2
        // Range approx 42 to 127. Let's pick 85 (Greenish)
        color.initRGBbyHSL(85, 255, 128);
        
        // This targets the 'return mag2' logic inside hueToRGB for the main channel
        assertTrue(color.getGreen() > 0);
    }

    @Test
    public void hueToRGBThirdRegionTest() {
        HSLColor color = new HSLColor();
        // HSLMAX/2 <= Hue < HSLMAX*2/3
        // Range approx 127 to 170. Let's pick 150.
        color.initRGBbyHSL(150, 255, 128);
        
        assertTrue(color.getBlue() > 0);
    }

    @Test
    public void hueToRGBFourthRegionTest() {
        HSLColor color = new HSLColor();
        // Hue >= HSLMAX*2/3
        // Let's pick 200.
        color.initRGBbyHSL(200, 255, 128);
        
        assertTrue(color.getBlue() > 0);
    }
    
    @Test
    public void hueToRGBRangeAdjustmentsTest() {
        HSLColor color = new HSLColor();
        // hueToRGB modifies Hue if < 0 or > HSLMAX.
        // initRGBbyHSL calls hueToRGB with (H + HSLMAX/3) and (H - HSLMAX/3).
        // If H=0:
        //  Red channel passes H + 85 -> 85 (Normal)
        //  Green channel passes H -> 0 (Normal)
        //  Blue channel passes H - 85 -> -85 (Triggers Hue < 0 logic inside hueToRGB)
        
        color.initRGBbyHSL(0, 255, 128);
        // Validates the < 0 branch implicitly
        
        // If H=250:
        //  Red channel passes 250 + 85 = 335 (Triggers Hue > HSLMAX logic inside hueToRGB)
        color.initRGBbyHSL(250, 255, 128);
        // Validates the > HSLMAX branch implicitly
    }

    // --------------------------------------------------------------------------------
    // Getter / Setter Tests
    // --------------------------------------------------------------------------------

    @Test
    public void setHueTest() {
        HSLColor color = new HSLColor();
        color.setHue(100);
        assertEquals(100, color.getHue());
    }

    @Test
    public void setHueNegativeLoopTest() {
        HSLColor color = new HSLColor();
        // Logic: while (iToValue < 0) iToValue += HSLMAX;
        // -10 + 255 = 245
        color.setHue(-10);
        assertEquals(245, color.getHue());
    }

    @Test
    public void setHuePositiveLoopTest() {
        HSLColor color = new HSLColor();
        // Logic: while (iToValue > HSLMAX) iToValue -= HSLMAX;
        // 300 - 255 = 45
        color.setHue(300);
        assertEquals(45, color.getHue());
    }

    @Test
    public void setSaturationTest() {
        HSLColor color = new HSLColor();
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
    }

    @Test
    public void setSaturationBoundsTest() {
        HSLColor color = new HSLColor();
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
        
        color.setSaturation(500);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void setLuminenceTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void setLuminenceBoundsTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
        
        color.setLuminence(500);
        assertEquals(255, color.getLuminence());
    }

    // --------------------------------------------------------------------------------
    // Utility Method Tests (reverse, brighten, blend)
    // --------------------------------------------------------------------------------

    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 100); // Hue = 0
        color.reverseColor();
        // 0 + 127 = 127 (HSLMAX/2 is integer division 255/2 = 127)
        assertEquals(127, color.getHue());
    }

    @Test
    public void brightenZeroPercentTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(0.0f);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void brightenNormalTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(0.5f); // 100 * 0.5 = 50
        assertEquals(50, color.getLuminence());
    }

    @Test
    public void brightenBoundsTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        
        // Result negative (unlikely with float mult of positive int, but logically handled)
        color.brighten(-0.1f);
        assertEquals(0, color.getLuminence());
        
        // Result too high
        color.setLuminence(200);
        color.brighten(2.0f); // 400
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void blendFullPercentTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        // fPercent >= 1 -> initHSLbyRGB(R,G,B)
        color.blend(255, 255, 255, 1.5f);
        assertEquals(255, color.getRed());
    }

    @Test
    public void blendZeroPercentTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        // fPercent <= 0 -> return
        color.blend(255, 255, 255, -0.5f);
        assertEquals(100, color.getRed());
    }

    @Test
    public void blendNormalTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Current
        // Target: 100, 100, 100. Percent 0.5.
        // Result: (100*0.5) + (0 * 0.5) = 50
        color.blend(100, 100, 100, 0.5f);
        assertEquals(50, color.getRed());
        assertEquals(50, color.getGreen());
        assertEquals(50, color.getBlue());
    }

    // --------------------------------------------------------------------------------
    // Reflection Tests for Private Methods
    // --------------------------------------------------------------------------------
    // The following methods are private in the source class and never called publicly.
    // To achieve 100% line coverage of the class provided, we must use Reflection.

    @Test
    public void reflectionSetRedTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        assertEquals(255, color.getRed());
    }

    @Test
    public void reflectionSetGreenTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        assertEquals(255, color.getGreen());
    }

    @Test
    public void reflectionSetBlueTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        assertEquals(255, color.getBlue());
    }

    @Test
    public void reflectionGreyscaleTest() throws Exception {
        HSLColor color = new HSLColor();
        // Set a color
        color.initRGBbyHSL(0, 255, 128); // Bright Red
        
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(color);
        
        // Greyscale sets Hue to UNDEFINED (170) and Saturation to 0
        assertEquals(170, color.getHue());
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void reflectionReverseLightTest() throws Exception {
        HSLColor color = new HSLColor();
        color.setLuminence(50);
        
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(color);
        
        // HSLMAX (255) - 50 = 205
        assertEquals(205, color.getLuminence());
    }
}