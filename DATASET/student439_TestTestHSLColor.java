import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {

    // Helper method to invoke private methods using reflection
    private void invokePrivateMethod(HSLColor instance, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(instance, args);
    }

    // --- initHSLbyRGB Tests ---

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor color = new HSLColor();
        // R=G=B triggers greyscale path
        color.initHSLbyRGB(100, 100, 100);
        
        // Expect Saturation 0, Hue 170 (UNDEFINED)
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue());
        assertEquals(100, color.getRed()); // Check R,G,B stored
    }

    @Test
    public void initHSLbyRGBRedMaxTest() {
        HSLColor color = new HSLColor();
        // Max is Red
        color.initHSLbyRGB(255, 0, 0);
        
        // Hue calculation for Red Max
        // Hue = BDelta - GDelta. 
        // Here R=255, G=0, B=0. Max=255, Min=0.
        // Hue should be roughly 0.
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(127, color.getLuminence()); // (255 + 255)/510 * 255 ~ 127
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        HSLColor color = new HSLColor();
        // Max is Green
        color.initHSLbyRGB(0, 255, 0);
        
        // Hue calculation for Green Max: (HSLMAX/3) + RDelta - BDelta
        // 255/3 = 85.
        assertEquals(85, color.getHue());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        HSLColor color = new HSLColor();
        // Max is Blue
        color.initHSLbyRGB(0, 0, 255);
        
        // Hue calculation for Blue Max: (2*HSLMAX/3) + GDelta - RDelta
        // 170.
        assertEquals(170, color.getHue());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void initHSLbyRGBLowLumTest() {
        HSLColor color = new HSLColor();
        // Luminance <= HSLMAX/2 (127)
        // R=50, G=50, B=50 -> Lum=50. 
        // Need diff colors for Sat calculation path: pLum <= (HSLMAX / 2)
        // L = (100+0 + 255)/510 = ~0.
        // Let's try 50, 0, 0. Max 50, Min 0. Plus 50. Lum = (50*255+255)/510 = 25.
        // 25 <= 127. Uses first Sat formula.
        color.initHSLbyRGB(50, 0, 0);
        
        assertEquals(25, color.getLuminence());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void initHSLbyRGBHighLumTest() {
        HSLColor color = new HSLColor();
        // Luminance > HSLMAX/2
        // 200, 255, 200. Max 255, Min 200. Plus 455.
        // Lum = (455*255 + 255) / 510 = 227 > 127.
        // Uses second Sat formula.
        color.initHSLbyRGB(200, 255, 200);
        
        assertTrue(color.getLuminence() > 127);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void initHSLbyRGBHueNegativeCorrectionTest() {
        HSLColor color = new HSLColor();
        // Trigger pHue < 0 to test correction (pHue + HSLMAX).
        // Requires Max=R, and B > G to make BDelta < GDelta (roughly).
        // R=255, G=0, B=100.
        // cMax=255, cMin=0.
        // pHue calculation will be negative.
        color.initHSLbyRGB(255, 0, 100);
        
        // Hue should be corrected to positive
        assertTrue(color.getHue() >= 0);
        // 255 red, 100 blue is a pinkish/magenta color, high hue (near 255 or wrapped)
        // Actually magenta is around 213-255 range in 0-255 scale.
        assertEquals(222, color.getHue());
    }

    // --- initRGBbyHSL Tests ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor color = new HSLColor();
        // S = 0 triggers greyscale path
        color.initRGBbyHSL(0, 0, 100);
        
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void initRGBbyHSLLowLumTest() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2 triggers Magic2 formula 1
        color.initRGBbyHSL(0, 255, 100);
        
        assertEquals(100, color.getLuminence());
        // Check that RGB was calculated (not grey)
        assertNotEquals(color.getRed(), color.getGreen());
    }

    @Test
    public void initRGBbyHSLHighLumTest() {
        HSLColor color = new HSLColor();
        // L > HSLMAX/2 triggers Magic2 formula 2
        color.initRGBbyHSL(0, 255, 200);
        
        assertEquals(200, color.getLuminence());
        assertNotEquals(color.getRed(), color.getGreen());
    }

    @Test
    public void initRGBbyHSLClampTest() {
        HSLColor color = new HSLColor();
        // Force calculation to exceed 255 to test clamping logic.
        // Using valid HSL 0-255 usually doesn't clamp.
        // Passing L > 255 triggers it because math uses L directly.
        // S must be != 0 to enter the calculation block.
        color.initRGBbyHSL(0, 1, 300);
        
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    // --- hueToRGB Branch Tests (via initRGBbyHSL) ---

    @Test
    public void hueToRGBBranchCoverageTest() {
        HSLColor color = new HSLColor();
        
        // To cover different branches in hueToRGB, we vary Hue.
        // hueToRGB is called for R (H+85), G (H), B (H-85).
        
        // 1. H=0. 
        // G uses H=0 (Hue < HSLMAX/6) -> First return.
        // R uses H=85 (Hue < HSLMAX/2) -> Second return.
        // B uses H=-85 -> wraps to 170 (Hue < 2/3 is false? 170 is 2/3 * 255).
        // 170 is exactly 2/3 of 255. Logic is Hue < (HSLMAX*2/3). 170 < 170 is false.
        // So returns mag1.
        color.initRGBbyHSL(0, 255, 127);
        
        // 2. Need to hit Third Return (Hue < 2/3).
        // Try H=160.
        // G uses H=160. 160 < 170. 160 > 127. Hits Third return.
        color.initRGBbyHSL(160, 255, 127);
        
        // 3. Need to hit Wrap Positive (Hue > HSLMAX)
        // Try H=250.
        // R uses H=250+85 = 335. Wraps in hueToRGB.
        color.initRGBbyHSL(250, 255, 127);
    }

    // --- Setter/Getter Tests ---

    @Test
    public void setHueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0); // Green, Hue 85
        
        color.setHue(10);
        assertEquals(10, color.getHue());
        // Should recalculate RGB
        assertNotEquals(0, color.getRed()); // likely changed
    }

    @Test
    public void setHueLoopUnderflowTest() {
        HSLColor color = new HSLColor();
        // Test while loop for negative hue
        // -300 + 255 = -45 + 255 = 210.
        color.setHue(-300);
        assertEquals(210, color.getHue());
    }

    @Test
    public void setHueLoopOverflowTest() {
        HSLColor color = new HSLColor();
        // Test while loop for positive hue
        // 600 - 255 = 345 - 255 = 90.
        color.setHue(600);
        assertEquals(90, color.getHue());
    }

    @Test
    public void setSaturationTest() {
        HSLColor color = new HSLColor();
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
    }

    @Test
    public void setSaturationUnderflowTest() {
        HSLColor color = new HSLColor();
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void setSaturationOverflowTest() {
        HSLColor color = new HSLColor();
        color.setSaturation(300);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void setLuminenceTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void setLuminenceUnderflowTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void setLuminenceOverflowTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void getRedTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
    }

    @Test
    public void getGreenTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(20, color.getGreen());
    }

    @Test
    public void getBlueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(30, color.getBlue());
    }

    // --- Action Methods Tests ---

    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        color.setHue(0);
        color.reverseColor();
        // 0 + 127 = 127
        assertEquals(127, color.getHue());
    }

    @Test
    public void brightenZeroTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(0f);
        assertEquals(100, color.getLuminence()); // No change
    }

    @Test
    public void brightenNormalTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(1.5f); // 150
        assertEquals(150, color.getLuminence());
    }

    @Test
    public void brightenOverflowTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(5.0f); // 500 -> clamped to 255
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void brightenUnderflowTest() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        color.brighten(-0.5f); // -50 -> clamped to 0
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void blendUpperLimitTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        // If percent >= 1, sets to new RGB
        color.blend(255, 255, 255, 1.5f);
        assertEquals(255, color.getRed());
    }

    @Test
    public void blendLowerLimitTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        // If percent <= 0, return (no change)
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getRed());
    }

    @Test
    public void blendMixTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Black
        // Blend 50% with White (255, 255, 255)
        // Result should be 127
        color.blend(255, 255, 255, 0.5f);
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    // --- Private Methods Tests (Reflection) ---
    // These methods are private and unused internally in the provided code,
    // but required for 100% coverage.

    @Test
    public void setRedPrivateTest() throws Exception {
        HSLColor color = new HSLColor();
        // Init with zeroes
        color.initHSLbyRGB(0, 0, 0);
        
        // Invoke private setRed(100)
        invokePrivateMethod(color, "setRed", new Class<?>[]{int.class}, new Object[]{100});
        
        assertEquals(100, color.getRed());
        assertEquals(0, color.getGreen());
    }

    @Test
    public void setGreenPrivateTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setGreen", new Class<?>[]{int.class}, new Object[]{100});
        
        assertEquals(0, color.getRed());
        assertEquals(100, color.getGreen());
    }

    @Test
    public void setBluePrivateTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setBlue", new Class<?>[]{int.class}, new Object[]{100});
        
        assertEquals(0, color.getRed());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void reverseLightPrivateTest() throws Exception {
        HSLColor color = new HSLColor();
        // Lum = 100
        color.setLuminence(100);
        
        // Reverse: 255 - 100 = 155
        invokePrivateMethod(color, "reverseLight", null, null);
        
        assertEquals(155, color.getLuminence());
    }

    @Test
    public void greyscalePrivateTest() throws Exception {
        HSLColor color = new HSLColor();
        // Set some color
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(255, color.getSaturation());
        
        invokePrivateMethod(color, "greyscale", null, null);
        
        // Should set Sat to 0 (Hue undefined 170)
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue());
    }
}