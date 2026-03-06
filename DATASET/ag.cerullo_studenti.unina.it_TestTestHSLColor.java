/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Agostino"
Cognome: "Cerullo"
Username: ag.cerullo@studenti.unina.it
UserID: 507
Date: 21/11/2025
*/

import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Method;

public class HSLColorTest {

    // --- Test initHSLbyRGB (Converts RGB to HSL) ---

    @Test
    public void InitHSLbyRGBGrayscaleTest() {
        HSLColor color = new HSLColor();
        // R=G=B triggers the greyscale branch (cMax == cMin)
        color.initHSLbyRGB(100, 100, 100);
        
        Assert.assertEquals("Saturation should be 0 for grayscale", 0, color.getSaturation());
        Assert.assertEquals("Hue should be UNDEFINED (170)", 170, color.getHue());
        Assert.assertEquals("Luminence should be calculated correctly", 100, color.getLuminence());
    }

    @Test
    public void InitHSLbyRGBRedMaxTest() {
        HSLColor color = new HSLColor();
        // Red is Max triggers (cMax == R)
        color.initHSLbyRGB(255, 0, 0);
        
        Assert.assertEquals(0, color.getHue()); // Red is usually 0 deg (0 in 0-255 scale)
        Assert.assertEquals(255, color.getSaturation());
        Assert.assertEquals(127, color.getLuminence()); // ~50% light
    }

    @Test
    public void InitHSLbyRGBGreenMaxTest() {
        HSLColor color = new HSLColor();
        // Green is Max triggers (cMax == G)
        color.initHSLbyRGB(0, 255, 0);
        
        // Hue calculation: (HSLMAX / 3) + RDelta - BDelta
        // HSLMAX/3 = 85.
        Assert.assertEquals(85, color.getHue());
        Assert.assertEquals(255, color.getSaturation());
    }

    @Test
    public void InitHSLbyRGBBlueMaxTest() {
        HSLColor color = new HSLColor();
        // Blue is Max triggers (cMax == B)
        color.initHSLbyRGB(0, 0, 255);
        
        // Hue calculation: ((2 * HSLMAX) / 3) + GDelta - RDelta
        // 170 approx.
        Assert.assertEquals(170, color.getHue()); 
        Assert.assertEquals(255, color.getSaturation());
    }

    @Test
    public void InitHSLbyRGBLowLuminanceTest() {
        HSLColor color = new HSLColor();
        // Triggers pLum <= (HSLMAX / 2) saturation formula
        color.initHSLbyRGB(20, 10, 10); 
        
        Assert.assertTrue(color.getLuminence() <= 127);
        Assert.assertEquals(15, color.getLuminence());
        Assert.assertEquals(85, color.getSaturation()); // Calculated based on low lum formula
    }

    @Test
    public void InitHSLbyRGBHighLuminanceTest() {
        HSLColor color = new HSLColor();
        // Triggers pLum > (HSLMAX / 2) saturation formula
        color.initHSLbyRGB(240, 220, 220);
        
        Assert.assertTrue(color.getLuminence() > 127);
        Assert.assertEquals(230, color.getLuminence());
    }

    @Test
    public void InitHSLbyRGBNegativeHueBranchTest() {
        HSLColor color = new HSLColor();
        // To trigger (pHue < 0) inside Red Max branch:
        // We need Red = Max, and Blue > Green (so BDelta < GDelta).
        color.initHSLbyRGB(255, 100, 200);
        
        // Initial Hue calc would be negative, then + HSLMAX
        int hue = color.getHue();
        Assert.assertTrue("Hue should be normalized to positive", hue >= 0);
        Assert.assertEquals(221, hue);
    }

    // --- Test initRGBbyHSL (Converts HSL to RGB) ---

    @Test
    public void InitRGBbyHSLGrayscaleTest() {
        HSLColor color = new HSLColor();
        // Saturation 0 triggers grayscale branch
        color.initRGBbyHSL(100, 0, 128);
        
        Assert.assertEquals(128, color.getRed());
        Assert.assertEquals(128, color.getGreen());
        Assert.assertEquals(128, color.getBlue());
    }

    @Test
    public void InitRGBbyHSLLowLuminanceTest() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2 branch in Magic2 calc
        color.initRGBbyHSL(0, 255, 100);
        
        Assert.assertEquals(100, color.getLuminence());
        // Check that RGB were set (Red should be high for Hue 0)
        Assert.assertEquals(200, color.getRed()); 
        Assert.assertEquals(0, color.getGreen());
        Assert.assertEquals(0, color.getBlue());
    }

    @Test
    public void InitRGBbyHSLHighLuminanceTest() {
        HSLColor color = new HSLColor();
        // L > HSLMAX/2 branch in Magic2 calc
        color.initRGBbyHSL(85, 255, 200);
        
        Assert.assertEquals(200, color.getLuminence());
        // Green should be dominant at Hue 85
        Assert.assertEquals(145, color.getRed());
        Assert.assertEquals(255, color.getGreen()); 
        Assert.assertEquals(145, color.getBlue());
    }

    @Test
    public void InitRGBbyHSLClampingTest() {
        HSLColor color = new HSLColor();
        // To hit the if (pRed > RGBMAX) checks, we use max saturation and max luminance.
        // This forces the calculation to hit the upper bounds.
        color.initRGBbyHSL(0, 255, 255); // White
        
        Assert.assertEquals(255, color.getRed());
        Assert.assertEquals(255, color.getGreen());
        Assert.assertEquals(255, color.getBlue());
    }

    // --- Test hueToRGB (Private logic accessed via initRGBbyHSL) ---
    
    @Test
    public void HueToRGBBranchesTest() {
        HSLColor color = new HSLColor();
        
        // We vary Hue to hit different branches in hueToRGB:
        // 1. Hue < HSLMAX/6
        // 2. Hue < HSLMAX/2
        // 3. Hue < HSLMAX*2/3
        // 4. Else
        
        // Hue = 0 hits branch 1 for Main, branch 4 for Blue (H-1/3), branch 2 for Red?
        color.initRGBbyHSL(0, 255, 128); 
        Assert.assertEquals(255, color.getRed());
        
        // Hue = 85 (1/3) hits branch 2
        color.initRGBbyHSL(85, 255, 128);
        Assert.assertEquals(255, color.getGreen());
        
        // Hue = 170 (2/3) hits branch 3 boundaries
        color.initRGBbyHSL(170, 255, 128);
        Assert.assertEquals(255, color.getBlue());
    }

    // --- Getters and Setters ---

    @Test
    public void SetHueLoopsTest() {
        HSLColor color = new HSLColor();
        
        // Test underflow loop (pHue < 0)
        color.setHue(-300); // -300 + 255 = -45, -45 + 255 = 210
        Assert.assertEquals(210, color.getHue());
        
        // Test overflow loop (pHue > HSLMAX)
        color.setHue(600); // 600 - 255 - 255 = 90
        Assert.assertEquals(90, color.getHue());
    }

    @Test
    public void SetSaturationBoundsTest() {
        HSLColor color = new HSLColor();
        
        color.setSaturation(-50);
        Assert.assertEquals(0, color.getSaturation());
        
        color.setSaturation(500);
        Assert.assertEquals(255, color.getSaturation());
    }

    @Test
    public void SetLuminenceBoundsTest() {
        HSLColor color = new HSLColor();
        
        color.setLuminence(-50);
        Assert.assertEquals(0, color.getLuminence());
        
        color.setLuminence(500);
        Assert.assertEquals(255, color.getLuminence());
    }

    @Test
    public void ReverseColorTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Hue 0
        
        color.reverseColor();
        // Should add HSLMAX / 2 (127)
        Assert.assertEquals(127, color.getHue());
    }

    // --- Brighten & Blend ---

    @Test
    public void BrightenZeroPercentTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        
        color.brighten(0);
        Assert.assertEquals(100, color.getLuminence());
    }

    @Test
    public void BrightenNormalTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        
        color.brighten(1.5f); // 100 * 1.5 = 150
        Assert.assertEquals(150, color.getLuminence());
    }

    @Test
    public void BrightenBoundsTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(200);
        
        color.brighten(2.0f); // 400 -> Clamped to 255
        Assert.assertEquals(255, color.getLuminence());
        
        color.brighten(-0.5f); // Negative result -> Clamped to 0
        Assert.assertEquals(0, color.getLuminence());
    }

    @Test
    public void BlendTests() {
        HSLColor color = new HSLColor();
        // Set initial color Black
        color.initHSLbyRGB(0, 0, 0); 
        
        // Case 1: percent >= 1 -> sets to new color immediately
        color.blend(255, 255, 255, 1.5f);
        Assert.assertEquals(255, color.getLuminence()); // White
        
        // Reset
        color.initHSLbyRGB(0, 0, 0);
        
        // Case 2: percent <= 0 -> does nothing
        color.blend(255, 255, 255, -0.5f);
        Assert.assertEquals(0, color.getLuminence()); // Still Black
        
        // Case 3: Blending
        color.initHSLbyRGB(0, 0, 0); // Start Black
        // Blend 50% with White (255, 255, 255) -> Result (127, 127, 127)
        color.blend(255, 255, 255, 0.5f);
        Assert.assertEquals(127, color.getRed());
    }

    // --- Reflection Tests for Private Unused Methods ---
    // These methods are private and not called by public methods in the provided code,
    // but are required for 100% coverage.

    @Test
    public void PrivateGreyscaleTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // High saturation
        
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(color);
        
        Assert.assertEquals(0, color.getSaturation());
        Assert.assertEquals(170, color.getHue()); // UNDEFINED
    }

    @Test
    public void PrivateReverseLightTest() throws Exception {
        HSLColor color = new HSLColor();
        color.setLuminence(50);
        
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(color);
        
        // Should be HSLMAX (255) - 50 = 205
        Assert.assertEquals(205, color.getLuminence());
    }

    @Test
    public void PrivateSetRedTest() throws Exception {
        HSLColor color = new HSLColor();
        // Initial Black
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        Assert.assertEquals(255, color.getRed());
        // Changing Red should update HSL (Lum should go up from 0)
        Assert.assertNotEquals(0, color.getLuminence());
    }

    @Test
    public void PrivateSetGreenTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        Assert.assertEquals(255, color.getGreen());
        Assert.assertNotEquals(0, color.getLuminence());
    }

    @Test
    public void PrivateSetBlueTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);
        
        Assert.assertEquals(255, color.getBlue());
        Assert.assertNotEquals(0, color.getLuminence());
    }
}

						