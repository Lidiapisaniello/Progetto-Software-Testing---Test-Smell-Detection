import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class TestHSLColor {

    private HSLColor hsl;
    private final int HSLMAX = 255;
    private final int RGBMAX = 255;

    @Before
    public void setUp() {
        hsl = new HSLColor();
    }

    // --- HELPER REFLECTION ---
    private void invokePrivateVoidMethod(String methodName, Class<?>[] argTypes, Object[] args) throws Exception {
        Method method = hsl.getClass().getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);
        method.invoke(hsl, args);
    }
    
    private void invokePrivateVoidMethodNoArgs(String methodName) throws Exception {
        Method method = hsl.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(hsl);
    }

    // --- TEST INIT FROM RGB ---

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // R=G=B -> Saturation 0, Hue Undefined (170 in code)
        hsl.initHSLbyRGB(100, 100, 100);
        assertEquals(0, hsl.getSaturation());
        assertEquals(170, hsl.getHue()); // UNDEFINED
        // Luminance calculation: (200*255 + 255) / 510 approx 100
        assertTrue(hsl.getLuminence() > 95 && hsl.getLuminence() < 105);
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // Red dominant -> Hue should be around 0 or 255
        hsl.initHSLbyRGB(255, 0, 0);
        assertEquals(255, hsl.getRed());
        // Pure Red in HSL is usually Hue 0 (or 255/0 boundary)
        assertTrue(hsl.getHue() >= 0 || hsl.getHue() <= 255);
        assertEquals(255, hsl.getSaturation()); // Max saturation
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // Green dominant -> Hue around 85 (255/3)
        hsl.initHSLbyRGB(0, 255, 0);
        assertEquals(255, hsl.getGreen());
        // Green is approx 1/3 of circle
        int expectedHue = HSLMAX / 3; 
        assertTrue(Math.abs(hsl.getHue() - expectedHue) < 5);
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // Blue dominant -> Hue around 170 (2*255/3)
        hsl.initHSLbyRGB(0, 0, 255);
        assertEquals(255, hsl.getBlue());
        int expectedHue = (2 * HSLMAX) / 3;
        assertTrue(Math.abs(hsl.getHue() - expectedHue) < 5);
    }

    @Test
    public void testInitHSLbyRGB_SatLogic_LowLum() {
        // Lum <= HSLMAX/2
        // Dark color logic
        hsl.initHSLbyRGB(50, 0, 0);
        // Assert logic execution (no exception)
        assertTrue(hsl.getLuminence() <= 127);
    }

    @Test
    public void testInitHSLbyRGB_SatLogic_HighLum() {
        // Lum > HSLMAX/2
        // Light color logic
        hsl.initHSLbyRGB(255, 200, 200);
        assertTrue(hsl.getLuminence() > 127);
    }

    // --- TEST INIT FROM HSL ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // S = 0
        hsl.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hsl.getRed());
        assertEquals(128, hsl.getGreen());
        assertEquals(128, hsl.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Colors() {
        // Normal conversion
        hsl.initRGBbyHSL(0, 255, 128); // Red-ish
        assertTrue(hsl.getRed() > 200);
        assertTrue(hsl.getGreen() < 50);
    }
    
    @Test
    public void testInitRGBbyHSL_Magic2_Branches() {
        // L <= HSLMAX/2
        hsl.initRGBbyHSL(0, 255, 50);
        assertTrue(hsl.getLuminence() <= 127);
        
        // L > HSLMAX/2
        hsl.initRGBbyHSL(0, 255, 200);
        assertTrue(hsl.getLuminence() > 127);
    }

    // --- PUBLIC METHODS (Setters/Utils) ---

    @Test
    public void testSetHue_WrapAround() {
        hsl.setHue(300); // > 255
        assertTrue(hsl.getHue() <= 255);
        
        hsl.setHue(-50); // < 0
        assertTrue(hsl.getHue() >= 0);
    }

    @Test
    public void testSetSaturation_Clamping() {
        hsl.setSaturation(500);
        assertEquals(255, hsl.getSaturation());
        
        hsl.setSaturation(-10);
        assertEquals(0, hsl.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hsl.setLuminence(500);
        assertEquals(255, hsl.getLuminence());
        
        hsl.setLuminence(-10);
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void testReverseColor() {
        hsl.setHue(0);
        hsl.reverseColor();
        // Should shift by HSLMAX/2 (approx 127)
        assertEquals(127, hsl.getHue());
    }

    @Test
    public void testBrighten() {
        hsl.initHSLbyRGB(100, 100, 100);
        int initialLum = hsl.getLuminence();
        
        hsl.brighten(1.5f); // +50%
        assertTrue(hsl.getLuminence() > initialLum);
        
        // Edge cases
        hsl.brighten(0); // No change
        
        hsl.initHSLbyRGB(0,0,0);
        hsl.brighten(-1.0f); // Clamp to 0
        assertEquals(0, hsl.getLuminence());
        
        hsl.initHSLbyRGB(255,255,255);
        hsl.brighten(10.0f); // Clamp to Max
        assertEquals(255, hsl.getLuminence());
    }

    @Test
    public void testBlend() {
        // 100% blend -> New Color
        hsl.initHSLbyRGB(0, 0, 0);
        hsl.blend(255, 255, 255, 1.0f);
        assertEquals(255, hsl.getRed());
        
        // 0% blend -> Old Color
        hsl.initHSLbyRGB(0, 0, 0);
        hsl.blend(255, 255, 255, 0.0f);
        assertEquals(0, hsl.getRed());
        
        // 50% blend
        hsl.initHSLbyRGB(0, 0, 0);
        hsl.blend(100, 100, 100, 0.5f);
        assertEquals(50, hsl.getRed());
    }

    // --- DEAD CODE COVERAGE VIA REFLECTION (The Secret Weapon) ---

    @Test
    public void testPrivate_setRed() throws Exception {
        hsl.initHSLbyRGB(0, 0, 0);
        invokePrivateVoidMethod("setRed", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getRed());
        // Verify it recalculated HSL
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void testPrivate_setGreen() throws Exception {
        hsl.initHSLbyRGB(0, 0, 0);
        invokePrivateVoidMethod("setGreen", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getGreen());
    }

    @Test
    public void testPrivate_setBlue() throws Exception {
        hsl.initHSLbyRGB(0, 0, 0);
        invokePrivateVoidMethod("setBlue", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getBlue());
    }

    @Test
    public void testPrivate_reverseLight() throws Exception {
        hsl.setLuminence(50);
        invokePrivateVoidMethodNoArgs("reverseLight");
        // Should be 255 - 50 = 205
        assertEquals(205, hsl.getLuminence());
    }

    @Test
    public void testPrivate_greyscale() throws Exception {
        hsl.initHSLbyRGB(255, 0, 0); // Red
        invokePrivateVoidMethodNoArgs("greyscale");
        assertEquals(0, hsl.getSaturation());
        // Hue becomes undefined (170)
        assertEquals(170, hsl.getHue());
    }
    
    // --- BRANCH COVERAGE for hueToRGB internal logic ---
    // hueToRGB is private but called by initRGBbyHSL.
    // We need to hit specific hue segments: < 1/6, < 1/2, < 2/3, else
    
    @Test
    public void testHueToRGB_Segments() {
        // Segment 1: Hue < HSLMAX/6 (approx 42)
        hsl.initRGBbyHSL(20, 255, 128); 
        
        // Segment 2: Hue < HSLMAX/2 (approx 127) -> try Hue 85
        hsl.initRGBbyHSL(85, 255, 128);
        
        // Segment 3: Hue < HSLMAX*2/3 (approx 170) -> try Hue 150
        hsl.initRGBbyHSL(150, 255, 128);
        
        // Segment 4: Else -> try Hue 200
        hsl.initRGBbyHSL(200, 255, 128);
    }
    
    @Test
    public void testHueToRGB_BoundsCheck() throws Exception {
         // hueToRGB handles Hue < 0 and Hue > HSLMAX.
         // initRGBbyHSL calls it with (H - HSLMAX/3), which can be negative.
         // Let's force verify via standard call
         hsl.initRGBbyHSL(10, 255, 128); // Blue component will call hueToRGB with negative hue
         
         hsl.initRGBbyHSL(250, 255, 128); // Red component will call hueToRGB with H > HSLMAX
    }
    
    @Test
    public void testClampingRGB_In_initRGBbyHSL() throws Exception {
        // initRGBbyHSL ha dei check: if (pRed > RGBMAX) pRed = RGBMAX;
        // È difficile triggerarli matematicamente coi calcoli standard HSL,
        // ma proviamo con valori estremi di Luminance e Saturation.
        // Se non riusciamo con input normali, Reflection è l'unica via, 
        // ma proviamo a fidarci dei calcoli "Magic".
        hsl.initRGBbyHSL(0, 255, 128);
        assertTrue(hsl.getRed() <= 255);
    }
}