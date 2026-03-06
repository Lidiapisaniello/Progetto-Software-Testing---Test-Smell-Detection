import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class HSLColorTest {

    // --- initHSLbyRGB Tests ---

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // R=G=B results in Greyscale (cMax == cMin)
        hsl.initHSLbyRGB(100, 100, 100);

        assertEquals("Saturation should be 0 for greyscale", 0, hsl.getSaturation());
        assertEquals("Hue should be UNDEFINED (170)", 170, hsl.getHue());
        assertTrue("Luminence should be calculated", hsl.getLuminence() > 0);
        assertEquals("Red should be stored", 100, hsl.getRed());
    }

    @Test
    public void initHSLbyRGBLowLuminenceCalculationTest() {
        HSLColor hsl = new HSLColor();
        // Low values to trigger pLum <= (HSLMAX / 2)
        // cMax=50, cMin=0. cPlus=50. Lum = (50*255 + 255)/(510) approx 25.
        hsl.initHSLbyRGB(50, 25, 0);

        // Verify we didn't hit UNDEFINED
        assertNotEquals(170, hsl.getHue());
        assertTrue("Luminence should be low", hsl.getLuminence() <= 127);
    }

    @Test
    public void initHSLbyRGBHighLuminenceCalculationTest() {
        HSLColor hsl = new HSLColor();
        // High values to trigger pLum > (HSLMAX / 2) logic in saturation calc
        hsl.initHSLbyRGB(200, 220, 240);

        assertTrue("Luminence should be high", hsl.getLuminence() > 127);
    }

    @Test
    public void initHSLbyRGBHueMaxRedTest() {
        HSLColor hsl = new HSLColor();
        // Red is Max (255), G=0, B=0.
        hsl.initHSLbyRGB(255, 0, 0);
        
        // cMax == R logic
        // Hue should be roughly 0 or 255 (Red)
        // BDelta - GDelta calculation
        assertEquals(255, hsl.getRed());
        int h = hsl.getHue();
        // Pure red is usually 0 or 255 scaled
        assertTrue(h >= 0 && h <= 255);
    }

    @Test
    public void initHSLbyRGBHueMaxGreenTest() {
        HSLColor hsl = new HSLColor();
        // Green is Max
        hsl.initHSLbyRGB(0, 255, 0);
        
        // cMax == G logic
        // pHue = (HSLMAX / 3) + RDelta - BDelta
        int h = hsl.getHue();
        assertTrue(h > 0); 
        assertEquals(255, hsl.getGreen());
    }

    @Test
    public void initHSLbyRGBHueMaxBlueTest() {
        HSLColor hsl = new HSLColor();
        // Blue is Max
        hsl.initHSLbyRGB(0, 0, 255);
        
        // cMax == B logic
        int h = hsl.getHue();
        assertTrue(h > 0);
        assertEquals(255, hsl.getBlue());
    }

    @Test
    public void initHSLbyRGBHueNegativeCorrectionTest() {
        HSLColor hsl = new HSLColor();
        // We need cMax == R, and (BDelta - GDelta) < 0
        // To make BDelta small, B must be close to Max.
        // To make GDelta large, G must be far from Max.
        // R=255, B=255, G=0 -> Magenta? 
        // Let's try R=255, G=0, B=20.
        // cMax=255(R). cMin=0. cMinus=255.
        // GDelta = ((255-0)*42 + 0.5)/255 = 42.
        // BDelta = ((255-20)*42 + 0.5)/255 = 38.
        // Hue = 38 - 42 = -4.
        // Trigger pHue < 0 correction.
        
        hsl.initHSLbyRGB(255, 0, 20);
        
        int h = hsl.getHue();
        assertTrue("Hue should be corrected to positive", h >= 0);
    }

    // --- initRGBbyHSL Tests ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // S = 0 -> Greyscale branch
        hsl.initRGBbyHSL(100, 0, 128);

        assertEquals(128, hsl.getRed());
        assertEquals(128, hsl.getGreen());
        assertEquals(128, hsl.getBlue());
    }

    @Test
    public void initRGBbyHSLLowLuminenceTest() {
        HSLColor hsl = new HSLColor();
        // S != 0, L <= HSLMAX/2
        // H=0, S=255, L=100
        hsl.initRGBbyHSL(0, 255, 100);
        
        assertNotEquals(0, hsl.getRed());
    }

    @Test
    public void initRGBbyHSLHighLuminenceTest() {
        HSLColor hsl = new HSLColor();
        // S != 0, L > HSLMAX/2
        hsl.initRGBbyHSL(0, 255, 200);
        
        assertNotEquals(0, hsl.getRed());
    }

    @Test
    public void initRGBbyHSLClampingRedTest() {
        HSLColor hsl = new HSLColor();
        // Pass specific excessively high Luminance to force pRed > RGBMAX
        // Since initRGBbyHSL is public and takes raw ints, we can overflow the internal math
        hsl.initRGBbyHSL(0, 0, 300); 
        
        assertEquals("Red should be clamped to 255", 255, hsl.getRed());
    }

    @Test
    public void initRGBbyHSLClampingGreenTest() {
        HSLColor hsl = new HSLColor();
        // Force green clamp
        hsl.initRGBbyHSL(85, 0, 300); 
        assertEquals("Green should be clamped to 255", 255, hsl.getGreen());
    }
    
    @Test
    public void initRGBbyHSLClampingBlueTest() {
        HSLColor hsl = new HSLColor();
        // Force blue clamp
        hsl.initRGBbyHSL(170, 0, 300); 
        assertEquals("Blue should be clamped to 255", 255, hsl.getBlue());
    }

    // --- hueToRGB Helper Tests via initRGBbyHSL ---
    
    @Test
    public void hueToRGBRangeCheckNegativeTest() {
        HSLColor hsl = new HSLColor();
        // To trigger Hue < 0 in hueToRGB helper:
        // Magic1, Magic2, H - (HSLMAX/3) is passed for Blue.
        // If H is 0, H - 85 = -85. This triggers `if (Hue < 0)` inside helper.
        hsl.initRGBbyHSL(0, 255, 128);
        // Execution implies success if no exception
        assertTrue(hsl.getBlue() >= 0);
    }

    @Test
    public void hueToRGBRangeCheckOverMaxTest() {
        HSLColor hsl = new HSLColor();
        // To trigger Hue > HSLMAX in hueToRGB helper:
        // Magic1, Magic2, H + (HSLMAX/3) is passed for Red.
        // If H is 250, H + 85 > 255.
        hsl.initRGBbyHSL(250, 255, 128);
        assertTrue(hsl.getRed() >= 0);
    }

    @Test
    public void hueToRGBBranchingCoverageTest() {
        HSLColor hsl = new HSLColor();
        // Coverage for internal Hue bands in hueToRGB:
        // < HSLMAX/6
        // < HSLMAX/2
        // < HSLMAX*2/3
        // Else
        
        // H=20 (approx < 42) covers first if
        hsl.initRGBbyHSL(20, 255, 128);
        
        // H=100 (approx < 127) covers second if
        hsl.initRGBbyHSL(100, 255, 128);
        
        // H=160 (approx < 170) covers third if
        hsl.initRGBbyHSL(160, 255, 128);
        
        // H=200 (else)
        hsl.initRGBbyHSL(200, 255, 128);
    }

    // --- Getters and Setters Tests ---

    @Test
    public void setHueNormalTest() {
        HSLColor hsl = new HSLColor();
        hsl.setHue(100);
        assertEquals(100, hsl.getHue());
    }

    @Test
    public void setHueUnderflowLoopTest() {
        HSLColor hsl = new HSLColor();
        // value < 0
        hsl.setHue(-50);
        // -50 + 255 = 205
        assertEquals(205, hsl.getHue());
    }

    @Test
    public void setHueOverflowLoopTest() {
        HSLColor hsl = new HSLColor();
        // value > 255
        hsl.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hsl.getHue());
    }

    @Test
    public void setSaturationClampingTest() {
        HSLColor hsl = new HSLColor();
        
        hsl.setSaturation(-10);
        assertEquals(0, hsl.getSaturation());

        hsl.setSaturation(300);
        assertEquals(255, hsl.getSaturation());
        
        hsl.setSaturation(100);
        assertEquals(100, hsl.getSaturation());
    }

    @Test
    public void setLuminenceClampingTest() {
        HSLColor hsl = new HSLColor();
        
        hsl.setLuminence(-10);
        assertEquals(0, hsl.getLuminence());

        hsl.setLuminence(300);
        assertEquals(255, hsl.getLuminence());
        
        hsl.setLuminence(100);
        assertEquals(100, hsl.getLuminence());
    }

    // --- Operation Tests ---

    @Test
    public void reverseColorTest() {
        HSLColor hsl = new HSLColor();
        hsl.initRGBbyHSL(50, 100, 100);
        hsl.reverseColor();
        // 50 + 127 = 177
        assertEquals(177, hsl.getHue());
    }

    @Test
    public void brightenZeroPercentTest() {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(100);
        hsl.brighten(0.0f);
        assertEquals(100, hsl.getLuminence());
    }

    @Test
    public void brightenNormalTest() {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(100);
        hsl.brighten(1.5f); // increase by 50%? No, it sets L = L * percent
        assertEquals(150, hsl.getLuminence());
    }

    @Test
    public void brightenClampingTest() {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(100);
        hsl.brighten(5.0f); // Should clamp to 255
        assertEquals(255, hsl.getLuminence());

        hsl.brighten(-1.0f); // Should clamp to 0 (cast to int makes it negative)
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void blendFullPercentTest() {
        HSLColor hsl = new HSLColor();
        // If percent >= 1, completely replaces color
        hsl.blend(255, 255, 255, 1.5f);
        assertEquals(255, hsl.getRed());
    }

    @Test
    public void blendZeroPercentTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        // If percent <= 0, does nothing
        hsl.blend(255, 255, 255, 0.0f);
        assertEquals(0, hsl.getRed());
    }

    @Test
    public void blendMixingTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        // Blend 50% with White (255,255,255)
        hsl.blend(255, 255, 255, 0.5f);
        
        // new = 255*0.5 + 0*0.5 = 127
        assertEquals(127, hsl.getRed());
        assertEquals(127, hsl.getGreen());
        assertEquals(127, hsl.getBlue());
    }

    // --- Private Method Tests (via Reflection) ---
    // Necessary for 100% line coverage as these methods are not called by public API

    @Test
    public void privateSetRedTest() throws Exception {
        HSLColor hsl = new HSLColor();
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        
        // Calling setRed should trigger initHSLbyRGB
        method.invoke(hsl, 255);
        assertEquals(255, hsl.getRed());
    }

    @Test
    public void privateSetGreenTest() throws Exception {
        HSLColor hsl = new HSLColor();
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        
        method.invoke(hsl, 255);
        assertEquals(255, hsl.getGreen());
    }

    @Test
    public void privateSetBlueTest() throws Exception {
        HSLColor hsl = new HSLColor();
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        
        method.invoke(hsl, 255);
        assertEquals(255, hsl.getBlue());
    }

    @Test
    public void privateGreyscaleTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0); // Red
        
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(hsl);
        
        assertEquals(0, hsl.getSaturation());
        assertEquals(170, hsl.getHue()); // UNDEFINED
    }

    @Test
    public void privateReverseLightTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(55); // HSLMAX is 255
        
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(hsl);
        
        // 255 - 55 = 200
        assertEquals(200, hsl.getLuminence());
    }
}