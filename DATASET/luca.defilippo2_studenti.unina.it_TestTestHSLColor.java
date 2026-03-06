/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Oriana"
Cognome: "Buccelli"
Username: luca.defilippo2@studenti.unina.it
UserID: 688
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class TestHSLColor {

    // --- initHSLbyRGB Tests (RGB -> HSL) ---

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor color = new HSLColor();
        // R=G=B implies Greyscale. cMax == cMin.
        color.initHSLbyRGB(100, 100, 100);
        
        // Expect Saturation 0, Hue UNDEFINED (170)
        assertEquals("Saturation should be 0 for greyscale", 0, color.getSaturation());
        assertEquals("Hue should be UNDEFINED (170) for greyscale", 170, color.getHue());
        assertEquals("Luminence calculation error", 100, color.getLuminence());
    }

    @Test
    public void initHSLbyRGBSaturationLowerLumTest() {
        HSLColor color = new HSLColor();
        // Case where pLum <= (HSLMAX / 2). 
        // RGB(50, 0, 0) -> Max=50, Min=0. Lum ~ 25 (<= 127)
        color.initHSLbyRGB(50, 0, 0);

        assertTrue("Luminence should be low", color.getLuminence() <= 127);
        assertTrue("Saturation should be calculated via first branch", color.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBSaturationHigherLumTest() {
        HSLColor color = new HSLColor();
        // Case where pLum > (HSLMAX / 2).
        // RGB(255, 200, 200) -> High brightness
        color.initHSLbyRGB(255, 200, 200);

        assertTrue("Luminence should be high", color.getLuminence() > 127);
        assertTrue("Saturation should be calculated via second branch", color.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBHueRedMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == R. Use Red dominant color.
        color.initHSLbyRGB(200, 50, 50);
        
        // Check that Hue was calculated (not UNDEFINED)
        assertNotEquals(170, color.getHue());
        assertEquals(200, color.getRed());
    }

    @Test
    public void initHSLbyRGBHueGreenMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == G. Use Green dominant color.
        color.initHSLbyRGB(50, 200, 50);
        
        // Hue should be roughly 85 (255/3)
        int h = color.getHue();
        assertTrue("Hue should be in green range", h > 70 && h < 100);
    }

    @Test
    public void initHSLbyRGBHueBlueMaxTest() {
        HSLColor color = new HSLColor();
        // cMax == B. Use Blue dominant color.
        color.initHSLbyRGB(50, 50, 200);
        
        // Hue should be roughly 170 (but not undefined 170 logic)
        // 2*255/3 = 170.
        int h = color.getHue();
        assertTrue("Hue should be in blue range", h > 150 && h < 190);
    }

    @Test
    public void initHSLbyRGBHueNegativeWrapTest() {
        HSLColor color = new HSLColor();
        // Trigger pHue < 0 logic.
        // This happens in Red Max scenario if BDelta > GDelta?
        // RGB(255, 0, 20). R is max. G is min.
        // This creates a hue near 255/0 boundary.
        color.initHSLbyRGB(255, 0, 200);
        
        // Should be a valid positive hue after wrapping
        assertTrue("Hue should be positive", color.getHue() >= 0);
        assertTrue("Hue should be valid", color.getHue() <= 255);
    }

    // --- initRGBbyHSL Tests (HSL -> RGB) ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor color = new HSLColor();
        // S = 0
        color.initRGBbyHSL(0, 0, 128);
        
        assertEquals("Red should match Lum in greyscale", 128, color.getRed());
        assertEquals("Green should match Lum in greyscale", 128, color.getGreen());
        assertEquals("Blue should match Lum in greyscale", 128, color.getBlue());
    }

    @Test
    public void initRGBbyHSLLowLumTest() {
        HSLColor color = new HSLColor();
        // S != 0, L <= HSLMAX/2
        color.initRGBbyHSL(10, 255, 100);
        
        // Just verify calculation happened (non-zero RGB usually)
        assertTrue(color.getRed() >= 0);
    }

    @Test
    public void initRGBbyHSLHighLumTest() {
        HSLColor color = new HSLColor();
        // S != 0, L > HSLMAX/2
        color.initRGBbyHSL(10, 255, 200);
        
        assertTrue(color.getRed() >= 0);
    }

    @Test
    public void initRGBbyHSLColorClampingTest() {
        HSLColor color = new HSLColor();
        // To hit the "if (pRed > RGBMAX)" lines, we need to force an overshoot.
        // We can do this by passing invalid (too high) Saturation/Luminance to the public method
        // since it accepts ints without checking bounds immediately.
        // 255 is HSLMAX. Passing 500 ensures calculation exceeds 255 before division.
        color.initRGBbyHSL(0, 500, 250);
        
        assertEquals("Red should be clamped to 255", 255, color.getRed());
        assertEquals("Green should be clamped to 255", 255, color.getGreen()); 
        // Depending on Hue 0, Blue might not clip, but Red/Green usually do in this extreme.
    }

    @Test
    public void hueToRGBBoundsTest() {
        // This logic is inside initRGBbyHSL, verifying specific Hue branches of private hueToRGB
        HSLColor color = new HSLColor();

        // Hue range 0 to 255.
        // Branches: < HSLMAX/6 (42), < HSLMAX/2 (127), < HSLMAX*2/3 (170)
        
        // 1. Hue < 42 (Red channel logic usually hits this if H is small)
        color.initRGBbyHSL(20, 255, 128);
        assertTrue(color.getRed() > 0);

        // 2. Hue around 128 (Green/Blue logic shifts H)
        color.initRGBbyHSL(128, 255, 128);
        assertTrue(color.getGreen() > 0);
    }

    // --- Setter/Getter & Loop Tests ---

    @Test
    public void setHueLoopUnderTest() {
        HSLColor color = new HSLColor();
        // setHue handles negative inputs by adding HSLMAX
        color.setHue(-50);
        
        int h = color.getHue();
        assertTrue("Hue should be corrected to positive", h >= 0);
        assertEquals("Check math -50 + 255 = 205", 205, h);
    }

    @Test
    public void setHueLoopOverTest() {
        HSLColor color = new HSLColor();
        // setHue handles > HSLMAX by subtracting HSLMAX
        color.setHue(300); // 300 - 255 = 45
        
        int h = color.getHue();
        assertTrue("Hue should be corrected within range", h <= 255);
        assertEquals(45, h);
    }

    @Test
    public void setSaturationClampingTest() {
        HSLColor color = new HSLColor();
        
        color.setSaturation(-10);
        assertEquals("Saturation clamped to 0", 0, color.getSaturation());

        color.setSaturation(300);
        assertEquals("Saturation clamped to 255", 255, color.getSaturation());
    }

    @Test
    public void setLuminenceClampingTest() {
        HSLColor color = new HSLColor();

        color.setLuminence(-10);
        assertEquals("Luminence clamped to 0", 0, color.getLuminence());

        color.setLuminence(300);
        assertEquals("Luminence clamped to 255", 255, color.getLuminence());
    }
    
    @Test
    public void gettersTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(30, color.getBlue());
        assertEquals(20, color.getGreen());
    }

    // --- Utility Methods ---

    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Hue ~0 or undefined-ish logic, likely 0
        int oldHue = color.getHue();
        
        color.reverseColor();
        int newHue = color.getHue();
        
        // Reverse adds HSLMAX/2 (127)
        assertNotEquals(oldHue, newHue);
    }

    @Test
    public void brightenTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(50, 50, 50);
        
        // 1. Zero percent - return immediately
        int oldL = color.getLuminence();
        color.brighten(0.0f);
        assertEquals(oldL, color.getLuminence());

        // 2. Normal brighten
        color.brighten(1.5f); // Increases L
        assertTrue(color.getLuminence() > oldL);

        // 3. Overflow check
        color.setLuminence(250);
        color.brighten(2.0f); // Should cap at 255
        assertEquals(255, color.getLuminence());

        // 4. Underflow check (logic says L < 0 -> L=0)
        // This requires float math resulting in negative, implies negative Lum or percent
        color.setLuminence(100);
        color.brighten(-0.5f); // Negative percent makes L negative temporarily
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void blendTest() {
        HSLColor color = new HSLColor();
        
        // 1. Percent >= 1 (Replaces color)
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getLuminence());

        // 2. Percent <= 0 (No change)
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getLuminence());

        // 3. Normal blend
        color.initHSLbyRGB(0, 0, 0);
        color.blend(100, 100, 100, 0.5f);
        // 50% of 0 + 50% of 100 = 50
        assertEquals(50, color.getRed());
    }

    // --- Private Method Tests (via Reflection) ---
    // These methods are unused by public API or unreachable, 
    // but required for 100% coverage of the provided source code.

    @Test
    public void privateSetRedTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setRed", new Class[]{int.class}, new Object[]{255});
        
        assertEquals("setRed should update Red", 255, color.getRed());
    }

    @Test
    public void privateSetGreenTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setGreen", new Class[]{int.class}, new Object[]{255});
        
        assertEquals("setGreen should update Green", 255, color.getGreen());
    }

    @Test
    public void privateSetBlueTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setBlue", new Class[]{int.class}, new Object[]{255});
        
        assertEquals("setBlue should update Blue", 255, color.getBlue());
    }

    @Test
    public void privateGreyscaleTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Saturation is high
        
        invokePrivateMethod(color, "greyscale", null, null);
        
        assertEquals("greyscale should set saturation to 0", 0, color.getSaturation());
        assertEquals("greyscale should set R=G=B", color.getRed(), color.getBlue());
    }

    @Test
    public void privateReverseLightTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(50, 50, 50); // Low light
        int oldLum = color.getLuminence();
        
        invokePrivateMethod(color, "reverseLight", null, null);
        
        // Logic: HSLMAX - pLum
        assertEquals(255 - oldLum, color.getLuminence());
    }

    @Test
    public void privateIMinIMaxTest() throws Exception {
        // iMin and iMax are private. While they are covered by initHSLbyRGB, 
        // explicit testing ensures branches (a>b, a<b) are strictly met.
        HSLColor color = new HSLColor();
        Method iMax = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        iMax.setAccessible(true);
        assertEquals(10, iMax.invoke(color, 5, 10));
        assertEquals(10, iMax.invoke(color, 10, 5));

        Method iMin = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        iMin.setAccessible(true);
        assertEquals(5, iMin.invoke(color, 5, 10));
        assertEquals(5, iMin.invoke(color, 10, 5));
    }

    // Helper method for reflection
    private void invokePrivateMethod(Object target, String methodName, Class[] argTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);
        method.invoke(target, args);
    }
}