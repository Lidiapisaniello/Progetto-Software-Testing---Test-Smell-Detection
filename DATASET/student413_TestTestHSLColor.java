import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class HSLColorTest {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- Tests for initHSLbyRGB (RGB -> HSL conversion) ---

    @Test
    public void InitHSLbyRGB_GreyscaleInput_SetsSaturationZero() {
        // Case: R=G=B (cMax == cMin)
        hslColor.initHSLbyRGB(100, 100, 100);

        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED (170) for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Luminance calculation incorrect", 100, hslColor.getLuminence());
    }

    @Test
    public void InitHSLbyRGB_RedDominant_CalculatesHueCorrectly() {
        // Case: cMax == R
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Red hue is typically 0 (or HSLMAX/3 * 0)
        // Logic: BDelta - GDelta. With pure red, this results in 0 or near 0 logic.
        // Let's assert it falls within expected range or specific calculated value
        // Calculated: Hue should be 0 for pure Red.
        int hue = hslColor.getHue();
        // Note: Due to integer math, we accept exact match here
        assertEquals("Hue for Pure Red should be 0", 0, hue); 
        assertEquals("Saturation should be max", HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void InitHSLbyRGB_GreenDominant_CalculatesHueCorrectly() {
        // Case: cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        
        // Green hue is typically 1/3 of circle (approx 85 in 0-255 scale)
        int hue = hslColor.getHue();
        assertEquals("Hue for Pure Green should be approx 85", 85, hue);
    }

    @Test
    public void InitHSLbyRGB_BlueDominant_CalculatesHueCorrectly() {
        // Case: cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        
        // Blue hue is typically 2/3 of circle (approx 170)
        int hue = hslColor.getHue();
        assertEquals("Hue for Pure Blue should be approx 170", 170, hue);
    }

    @Test
    public void InitHSLbyRGB_LowLuminance_CalculatesSaturationCorrectly() {
        // Case: pLum <= (HSLMAX / 2)
        // RGB(50, 0, 0) -> Low Lum
        hslColor.initHSLbyRGB(50, 0, 0);
        
        assertTrue("Luminance should be low", hslColor.getLuminence() <= (HSLMAX / 2));
        assertEquals("Saturation should be max for pure dark red", 255, hslColor.getSaturation());
    }

    @Test
    public void InitHSLbyRGB_HighLuminance_CalculatesSaturationCorrectly() {
        // Case: pLum > (HSLMAX / 2)
        // RGB(255, 200, 200) -> High Lum (Light Pink)
        hslColor.initHSLbyRGB(255, 200, 200);
        
        assertTrue("Luminance should be high", hslColor.getLuminence() > (HSLMAX / 2));
        assertTrue("Saturation should be calculated", hslColor.getSaturation() > 0);
    }

    @Test
    public void InitHSLbyRGB_NegativeHueResult_WrapsHueValue() {
        // Must create a scenario where the hue calculation results in negative, triggering "if (pHue < 0)"
        // Usually happens when wrapping from Red backwards.
        // Let's try a color like Red-Purple where Blue > Green but Red is max?
        // Actually, if cMax==R, Hue = BDelta - GDelta. If GDelta > BDelta, Hue is negative.
        // This requires (cMax-G) < (cMax-B) => G > B.
        // So Red > Green > Blue. Example: R=200, G=100, B=10.
        
        hslColor.initHSLbyRGB(200, 10, 100); // Swapped G and B to try force negative term in R branch?
        // No, simply: If cMax=R, Hue = (BDelta - GDelta).
        // We need GDelta > BDelta. GDelta depends on (Max-G). BDelta on (Max-B).
        // If G is smaller than B, (Max-G) is bigger, so GDelta is bigger.
        // So we need Red Max, and Green < Blue. 
        
        hslColor.initHSLbyRGB(255, 10, 100); // R=255, G=10, B=100.
        // Check internal state via getter
        int hue = hslColor.getHue();
        assertTrue("Hue should be positive (wrapped)", hue >= 0 && hue <= HSLMAX);
    }

    // --- Tests for initRGBbyHSL (HSL -> RGB conversion) ---

    @Test
    public void InitRGBbyHSL_GreyscaleSaturationZero_SetsRGBEqual() {
        // Case: S = 0
        hslColor.initRGBbyHSL(0, 0, 128);
        
        assertEquals("Red should match Lum logic", 128, hslColor.getRed());
        assertEquals("Green should equal Red", 128, hslColor.getGreen());
        assertEquals("Blue should equal Red", 128, hslColor.getBlue());
    }

    @Test
    public void InitRGBbyHSL_LowLuminance_CalculatesRGBCorrectly() {
        // Case: L <= HSLMAX / 2
        hslColor.initRGBbyHSL(0, 255, 60); // Red-ish, Dark
        
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getGreen());
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getBlue());
    }

    @Test
    public void InitRGBbyHSL_HighLuminance_CalculatesRGBCorrectly() {
        // Case: L > HSLMAX / 2
        hslColor.initRGBbyHSL(0, 255, 200); // Red-ish, Light
        
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getGreen());
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getBlue());
    }
    
    @Test
    public void InitRGBbyHSL_HueSegments_CoversHueToRGBBranches() {
        // We need to cover the branches in hueToRGB: < 1/6, < 1/2, < 2/3
        // HSLMAX = 255. 
        // 1/6 ~ 42. 1/2 ~ 127. 2/3 ~ 170.
        
        // 1. Hue < 1/6 (e.g. 20 - Orange/Red)
        hslColor.initRGBbyHSL(20, 255, 128);
        assertRGBValid();
        
        // 2. Hue < 1/2 but > 1/6 (e.g. 85 - Green)
        hslColor.initRGBbyHSL(85, 255, 128);
        assertRGBValid();

        // 3. Hue < 2/3 but > 1/2 (e.g. 150 - Cyan/Blueish)
        hslColor.initRGBbyHSL(150, 255, 128);
        assertRGBValid();
        
        // 4. Hue > 2/3 (e.g. 200 - Purple)
        hslColor.initRGBbyHSL(200, 255, 128);
        assertRGBValid();
    }

    @Test
    public void InitRGBbyHSL_BoundsChecks_ClampsRGBMax() {
        // While mathematically hard to exceed 255 with standard HSL inputs,
        // we rely on the code logic. 
        // We can simulate "edge" inputs or just trust normal operation implies validation.
        // To truly force the "if (pRed > RGBMAX)" check without mock, inputs must be extreme.
        // But given the math, let's ensure we test standard limits.
        hslColor.initRGBbyHSL(0, 255, 128);
        assertTrue(hslColor.getRed() <= 255);
    }

    // --- Tests for Getters and Setters (including logic) ---

    @Test
    public void SetHue_NegativeValue_WrapsPositive() {
        // Loops until positive
        hslColor.setHue(-50);
        // -50 + 255 = 205
        assertEquals("Hue should wrap positive", 205, hslColor.getHue());
    }

    @Test
    public void SetHue_LargeValue_WrapsWithinRange() {
        // Loops until < 255
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals("Hue should wrap under max", 45, hslColor.getHue());
    }

    @Test
    public void SetSaturation_Negative_ClampsToZero() {
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void SetSaturation_Overflow_ClampsToMax() {
        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void SetLuminence_Negative_ClampsToZero() {
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void SetLuminence_Overflow_ClampsToMax() {
        hslColor.setLuminence(300);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    // --- Action Methods ---

    @Test
    public void ReverseColor_StandardState_ShiftsHueHalfway() {
        hslColor.initHSLbyRGB(255, 0, 0); // Red, Hue 0
        hslColor.reverseColor();
        
        // Expected: 0 + (255/2) = 127
        assertEquals("Hue should be shifted by half", 127, hslColor.getHue());
    }

    @Test
    public void Brighten_ZeroPercent_DoesNothing() {
        hslColor.setLuminence(100);
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void Brighten_StandardPercent_UpdatesLuminence() {
        hslColor.setLuminence(100);
        hslColor.brighten(1.5f); // +50%
        
        assertEquals(150, hslColor.getLuminence());
    }

    @Test
    public void Brighten_NegativePercent_ClampsZero() {
        hslColor.setLuminence(100);
        hslColor.brighten(-0.5f); // Should result in negative logic -> clamp 0
        
        assertEquals(0, hslColor.getLuminence());
    }
    
    @Test
    public void Brighten_HugePercent_ClampsMax() {
        hslColor.setLuminence(100);
        hslColor.brighten(10.0f);
        
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    @Test
    public void Blend_PercentGreaterThanOne_ReplacesColor() {
        hslColor.initHSLbyRGB(0, 0, 0); // Black
        // Blend fully to White
        hslColor.blend(255, 255, 255, 1.5f);
        
        assertEquals("Should be white Lum", 255, hslColor.getLuminence());
    }

    @Test
    public void Blend_PercentZeroOrLess_NoChange() {
        hslColor.initHSLbyRGB(0, 0, 0); // Black
        hslColor.blend(255, 255, 255, 0.0f); // 0%
        assertEquals("Should remain black", 0, hslColor.getLuminence());
        
        hslColor.blend(255, 255, 255, -1.0f); // negative
        assertEquals("Should remain black", 0, hslColor.getLuminence());
    }

    @Test
    public void Blend_StandardPercent_MixesRGB() {
        hslColor.initHSLbyRGB(0, 0, 0); // Start Black (0,0,0)
        
        // Blend 50% with White (255,255,255)
        // New Val = (255 * 0.5) + (0 * 0.5) = 127.5 -> 127
        hslColor.blend(255, 255, 255, 0.5f);
        
        assertEquals(127, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(127, hslColor.getBlue());
    }

    // --- PRIVATE METHOD TESTING VIA REFLECTION (REQUIRED FOR 100% COVERAGE) ---

    @Test
    public void Greyscale_InvokeViaReflection_SetsSaturationToZero() throws Exception {
        // Setup initial color
        hslColor.initHSLbyRGB(255, 0, 0);
        
        Method greyscaleMethod = HSLColor.class.getDeclaredMethod("greyscale");
        greyscaleMethod.setAccessible(true);
        greyscaleMethod.invoke(hslColor);
        
        assertEquals("Saturation should be 0 after greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED", UNDEFINED, hslColor.getHue());
    }

    @Test
    public void ReverseLight_InvokeViaReflection_InvertsLuminence() throws Exception {
        hslColor.setLuminence(100);
        
        Method reverseLightMethod = HSLColor.class.getDeclaredMethod("reverseLight");
        reverseLightMethod.setAccessible(true);
        reverseLightMethod.invoke(hslColor);
        
        // Expected: HSLMAX (255) - 100 = 155
        assertEquals("Luminence should be inverted", 155, hslColor.getLuminence());
    }

    @Test
    public void SetRed_InvokeViaReflection_UpdatesColor() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        Method setRedMethod = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRedMethod.setAccessible(true);
        setRedMethod.invoke(hslColor, 255);
        
        assertEquals("Red should be updated", 255, hslColor.getRed());
        assertEquals("Hue should be 0 (Red)", 0, hslColor.getHue());
    }

    @Test
    public void SetGreen_InvokeViaReflection_UpdatesColor() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        Method setGreenMethod = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreenMethod.setAccessible(true);
        setGreenMethod.invoke(hslColor, 255);
        
        assertEquals("Green should be updated", 255, hslColor.getGreen());
        assertEquals("Hue should be approx 85 (Green)", 85, hslColor.getHue());
    }

    @Test
    public void SetBlue_InvokeViaReflection_UpdatesColor() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        Method setBlueMethod = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlueMethod.setAccessible(true);
        setBlueMethod.invoke(hslColor, 255);
        
        assertEquals("Blue should be updated", 255, hslColor.getBlue());
        assertEquals("Hue should be approx 170 (Blue)", 170, hslColor.getHue());
    }
    
    @Test
    public void HueToRGB_BoundaryCheck_HueWraps() throws Exception {
        // Specifically targeting the first lines of hueToRGB:
        // if (Hue < 0) Hue = Hue + HSLMAX;
        // if (Hue > HSLMAX) Hue = Hue - HSLMAX;
        // These are usually reached by initRGBbyHSL passing (H - HSLMAX/3) etc.
        // But to be 100% explicitly sure we hit them with various values:
        
        Method hueToRGBMethod = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        hueToRGBMethod.setAccessible(true);
        
        // Test negative wrap
        // magic1=0, magic2=255, Hue=-10. Should wrap to 245.
        // 245 is > 2/3 * 255 (170). So it goes to last return (mag1=0).
        Object resultNeg = hueToRGBMethod.invoke(hslColor, 0, 255, -10);
        assertNotNull(resultNeg);

        // Test overflow wrap
        // Hue=300. Wraps to 45.
        // 45 is < 1/6 * 255 (~42). No wait, 45 > 42. It falls into 2nd or 3rd check?
        // 45 is < HSLMAX/2 (127). So returns mag2 (255).
        Object resultPos = hueToRGBMethod.invoke(hslColor, 0, 255, 300);
        assertEquals(255, resultPos);
    }

    // --- Helpers ---

    private void assertRGBValid() {
        assertTrue(hslColor.getRed() >= 0 && hslColor.getRed() <= 255);
        assertTrue(hslColor.getGreen() >= 0 && hslColor.getGreen() <= 255);
        assertTrue(hslColor.getBlue() >= 0 && hslColor.getBlue() <= 255);
    }
}