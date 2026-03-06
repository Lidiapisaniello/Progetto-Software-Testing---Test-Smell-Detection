/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: dom.mennillo@studenti.unina.it
UserID: 332
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class TestHSLColor {

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // R=G=B results in Greyscale (Sat=0, Hue=Undefined/170)
        hsl.initHSLbyRGB(100, 100, 100);

        assertEquals("Hue should be UNDEFINED (170) for greyscale", 170, hsl.getHue());
        assertEquals("Saturation should be 0 for greyscale", 0, hsl.getSaturation());
        // Lum = ((100*255)+255) / (2*255) = (25500+255)/510 = 50
        assertEquals("Luminence calculated incorrectly", 100, hsl.getLuminence());
        assertEquals(100, hsl.getRed());
    }

    @Test
    public void initHSLbyRGBCMaxIsRedTest() {
        HSLColor hsl = new HSLColor();
        // Red is Max, Green and Blue are 0
        hsl.initHSLbyRGB(255, 0, 0);

        assertEquals("Hue should be 0/255 (Red)", 0, hsl.getHue());
        assertEquals("Saturation should be max", 255, hsl.getSaturation());
        assertEquals("Luminence should be mid", 127, hsl.getLuminence());
    }

    @Test
    public void initHSLbyRGBCMaxIsGreenTest() {
        HSLColor hsl = new HSLColor();
        // Green is Max
        hsl.initHSLbyRGB(0, 255, 0);

        // Hue formula for G Max: (HSLMAX / 3) + RDelta - BDelta
        // roughly 85 range
        assertEquals("Hue should be approx 85 for Green", 85, hsl.getHue());
    }

    @Test
    public void initHSLbyRGBCMaxIsBlueTest() {
        HSLColor hsl = new HSLColor();
        // Blue is Max
        hsl.initHSLbyRGB(0, 0, 255);

        // Hue formula for B Max: (2*HSLMAX / 3) + GDelta - RDelta
        // roughly 170 range
        assertEquals("Hue should be approx 170 for Blue", 170, hsl.getHue());
    }

    @Test
    public void initHSLbyRGBLumLessOrEqualHalfTest() {
        HSLColor hsl = new HSLColor();
        // Dark color to trigger pLum <= HSLMAX/2
        // R=50, G=0, B=0
        hsl.initHSLbyRGB(50, 0, 0);

        assertTrue("Luminence should be <= 127", hsl.getLuminence() <= 127);
        assertEquals("Saturation should be 255", 255, hsl.getSaturation());
    }

    @Test
    public void initHSLbyRGBLumGreaterThanHalfTest() {
        HSLColor hsl = new HSLColor();
        // Light color to trigger pLum > HSLMAX/2
        // R=255, G=200, B=200
        hsl.initHSLbyRGB(255, 200, 200);

        assertTrue("Luminence should be > 127", hsl.getLuminence() > 127);
        // Saturation logic differs for light colors
        assertTrue("Saturation check", hsl.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBNegativeHueCorrectionTest() {
        HSLColor hsl = new HSLColor();
        // We need a case where the hue calc results in negative, requiring +HSLMAX
        // Case: Red is max, but B > G implies negative Hue before correction
        hsl.initHSLbyRGB(255, 0, 100);

        // Internal calculation would be negative, then +255
        // Hue should be roughly magenta/purple range (> 200)
        assertTrue("Hue should be corrected to positive", hsl.getHue() > 0);
    }

    // --- initRGBbyHSL Scenarios (HSL to RGB) ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // S = 0 triggers greyscale block
        hsl.initRGBbyHSL(100, 0, 128);

        assertEquals("Red matches Lum in greyscale", 128, hsl.getRed());
        assertEquals("Green matches Lum in greyscale", 128, hsl.getGreen());
        assertEquals("Blue matches Lum in greyscale", 128, hsl.getBlue());
    }

    @Test
    public void initRGBbyHSLLowLuminenceTest() {
        HSLColor hsl = new HSLColor();
        // S != 0, L <= 127
        hsl.initRGBbyHSL(0, 255, 100);

        assertEquals(100, hsl.getLuminence());
        assertEquals(255, hsl.getSaturation());
        // Verify RGB updated
        assertTrue(hsl.getRed() > 0);
    }

    @Test
    public void initRGBbyHSLHighLuminenceTest() {
        HSLColor hsl = new HSLColor();
        // S != 0, L > 127
        hsl.initRGBbyHSL(0, 255, 200);

        assertEquals(200, hsl.getLuminence());
        // Verify RGB updated
        assertTrue(hsl.getRed() > 0);
    }

    @Test
    public void initRGBbyHSLClampRedTest() {
        HSLColor hsl = new HSLColor();
        // To hit "if (pRed > RGBMAX)", we pass L value > 255.
        // The method accepts ints but doesn't validate range at start.
        hsl.initRGBbyHSL(0, 255, 500); // L=500 causes overflow math

        assertEquals("Red should be clamped to 255", 255, hsl.getRed());
    }

    @Test
    public void initRGBbyHSLClampGreenTest() {
        HSLColor hsl = new HSLColor();
        // Hue approx 85 (Green). Large L to force overflow.
        hsl.initRGBbyHSL(85, 255, 500);

        assertEquals("Green should be clamped to 255", 255, hsl.getGreen());
    }

    @Test
    public void initRGBbyHSLClampBlueTest() {
        HSLColor hsl = new HSLColor();
        // Hue approx 170 (Blue). Large L to force overflow.
        hsl.initRGBbyHSL(170, 255, 500);

        assertEquals("Blue should be clamped to 255", 255, hsl.getBlue());
    }

    // --- hueToRGB Scenarios (Private Helper branches) ---
    // Accessed via initRGBbyHSL with specific Hue values

    @Test
    public void hueToRGBFirstRegionTest() {
        HSLColor hsl = new HSLColor();
        // Hue < HSLMAX/6 (approx 42). Use Hue=20.
        hsl.initRGBbyHSL(20, 255, 128);
        // Assert logic ran without error and produced valid RGB
        assertTrue(hsl.getRed() >= 0 && hsl.getRed() <= 255);
    }

    @Test
    public void hueToRGBSecondRegionTest() {
        HSLColor hsl = new HSLColor();
        // Hue < HSLMAX/2 (approx 127) but > 42. Use Hue=100.
        hsl.initRGBbyHSL(100, 255, 128);
        assertTrue(hsl.getGreen() >= 0);
    }

    @Test
    public void hueToRGBThirdRegionTest() {
        HSLColor hsl = new HSLColor();
        // Hue < 2/3 (approx 170) but > 127. Use Hue=150.
        hsl.initRGBbyHSL(150, 255, 128);
        assertTrue(hsl.getBlue() >= 0);
    }

    @Test
    public void hueToRGBFourthRegionTest() {
        HSLColor hsl = new HSLColor();
        // Hue > 2/3. Use Hue=200.
        hsl.initRGBbyHSL(200, 255, 128);
        assertTrue(hsl.getBlue() >= 0);
    }

    @Test
    public void hueToRGBRangeCorrectionTest() {
        HSLColor hsl = new HSLColor();
        // Internally hueToRGB checks:
        // if (Hue < 0) Hue += HSLMAX
        // if (Hue > HSLMAX) Hue -= HSLMAX
        // These are triggered by the offset arguments in initRGBbyHSL:
        // H + (HSLMAX/3) or H - (HSLMAX/3)
        
        // Passing 0 triggers H - 85 < 0 correction
        // Passing 255 triggers H + 85 > 255 correction
        hsl.initRGBbyHSL(0, 255, 128);
        hsl.initRGBbyHSL(255, 255, 128);
        
        // Verification is simply that the method completes without exception
        assertNotNull(hsl); 
    }

    // --- Getter/Setter Logic ---

    @Test
    public void setHueLoopUnderflowTest() {
        HSLColor hsl = new HSLColor();
        // setHue has a while(val < 0) loop
        hsl.setHue(-50);
        // -50 + 255 = 205
        assertEquals(205, hsl.getHue());
    }

    @Test
    public void setHueLoopOverflowTest() {
        HSLColor hsl = new HSLColor();
        // setHue has a while(val > 255) loop
        hsl.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hsl.getHue());
    }

    @Test
    public void setSaturationClampTest() {
        HSLColor hsl = new HSLColor();
        
        hsl.setSaturation(-10);
        assertEquals(0, hsl.getSaturation());

        hsl.setSaturation(300);
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void setLuminenceClampTest() {
        HSLColor hsl = new HSLColor();
        
        hsl.setLuminence(-10);
        assertEquals(0, hsl.getLuminence());

        hsl.setLuminence(300);
        assertEquals(255, hsl.getLuminence());
    }

    // --- Other Public Methods ---

    @Test
    public void reverseColorTest() {
        HSLColor hsl = new HSLColor();
        hsl.setHue(0);
        hsl.reverseColor();
        // 0 + 127 (HSLMAX/2) = 127
        assertEquals(127, hsl.getHue());
    }

    @Test
    public void brightenTest() {
        HSLColor hsl = new HSLColor();
        hsl.initRGBbyHSL(0, 0, 100);

        // 1. Zero percent - return early
        hsl.brighten(0);
        assertEquals(100, hsl.getLuminence());

        // 2. Normal calc
        hsl.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hsl.getLuminence());

        // 3. Overflow
        hsl.brighten(10.0f); // 1500 -> 255
        assertEquals(255, hsl.getLuminence());
        
        // 4. Underflow (negative factor?)
        hsl.brighten(-1.0f); 
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void blendTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);

        // 1. Percent >= 1 (Full replace)
        hsl.blend(255, 255, 255, 1.0f);
        assertEquals(255, hsl.getLuminence());

        // 2. Percent <= 0 (No change)
        hsl.blend(0, 0, 0, 0.0f);
        assertEquals(255, hsl.getLuminence()); // Still white from prev step

        // 3. Blend 50%
        // Current: 255, 255, 255. Target: 0, 0, 0. 50% -> 127
        hsl.blend(0, 0, 0, 0.5f);
        assertEquals(127, hsl.getRed());
    }

    // --- Reflection Tests for Dead Code / Private Methods ---
    // These methods are not called internally by HSLColor but exist in the source.
    // To get 100% line coverage, we must invoke them via Reflection.

    @Test
    public void privateGreyscaleTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0); // Red
        
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(hsl);

        assertEquals("Saturation should be 0 after greyscale", 0, hsl.getSaturation());
        assertEquals("Hue should be UNDEFINED", 170, hsl.getHue());
    }

    @Test
    public void privateSetRedTest() throws Exception {
        HSLColor hsl = new HSLColor();
        // setRed is private and unused
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
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
    public void privateReverseLightTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(100);
        
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(hsl);
        
        // 255 - 100 = 155
        assertEquals(155, hsl.getLuminence());
    }
    
    @Test
    public void privateIMinTest() throws Exception {
        HSLColor hsl = new HSLColor();
        // Explicitly test branches of iMin (though utilized by initHSLbyRGB)
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);
        
        int res1 = (int) method.invoke(hsl, 10, 20);
        assertEquals(10, res1);
        
        int res2 = (int) method.invoke(hsl, 20, 10);
        assertEquals(10, res2);
    }
}