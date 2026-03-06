import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class HSLColorTestWhiteBox {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    /**
     * White-Box Strategy: Reflection / Path Coverage
     * Target: Private method iMax(int, int)
     * Logic: Verify both branches (a > b) and (b >= a).
     */
    @Test
    public void testPrivate_iMax() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        method.setAccessible(true);

        int resultA = (int) method.invoke(hslColor, 10, 5);
        assertEquals("Should return first argument when larger", 10, resultA);

        int resultB = (int) method.invoke(hslColor, 3, 8);
        assertEquals("Should return second argument when larger", 8, resultB);
    }

    /**
     * White-Box Strategy: Reflection / Path Coverage
     * Target: Private method iMin(int, int)
     * Logic: Verify both branches (a < b) and (b <= a).
     */
    @Test
    public void testPrivate_iMin() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);

        int resultA = (int) method.invoke(hslColor, 2, 10);
        assertEquals("Should return first argument when smaller", 2, resultA);

        int resultB = (int) method.invoke(hslColor, 20, 5);
        assertEquals("Should return second argument when smaller", 5, resultB);
    }

    /**
     * White-Box Strategy: Reflection
     * Target: Private method greyscale()
     * Logic: Ensure the method correctly sets internal state to specific greyscale constants.
     * Use reflection to access private fields to verify state change.
     */
    @Test
    public void testPrivate_greyscale() throws Exception {
        // Initialize with a color
        hslColor.initHSLbyRGB(255, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(hslColor);

        // Verify private fields via reflection or public getters
        assertEquals("Greyscale Hue should be UNDEFINED (170)", UNDEFINED, hslColor.getHue());
        assertEquals("Greyscale Saturation should be 0", 0, hslColor.getSaturation());
        // Luminence should persist from the red color (128)
        assertEquals("Luminence should be preserved", 128, hslColor.getLuminence());
    }

    /**
     * White-Box Strategy: Reflection / Encapsulation Bypassing
     * Target: Private setters setRed, setGreen, setBlue
     * Logic: These methods are private and trigger a full re-initialization of HSL values.
     * We invoke them to ensure the dependency chain (setX -> initHSLbyRGB -> set Hue/Sat/Lum) works.
     */
    @Test
    public void testPrivate_Setters_RGB() throws Exception {
        // Start with Black
        hslColor.initHSLbyRGB(0, 0, 0);

        Method setRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRed.setAccessible(true);
        setRed.invoke(hslColor, 255);

        assertEquals("Red should be updated", 255, hslColor.getRed());
        // Check side effect on Luminence (Pure Red Lum is ~128)
        assertEquals("Luminence should update after setting Red", 128, hslColor.getLuminence());

        Method setGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreen.setAccessible(true);
        setGreen.invoke(hslColor, 255);
        // Now Yellow (255, 255, 0)
        assertEquals("Green should be updated", 255, hslColor.getGreen());

        Method setBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlue.setAccessible(true);
        setBlue.invoke(hslColor, 255);
        // Now White (255, 255, 255)
        assertEquals("Blue should be updated", 255, hslColor.getBlue());
        assertEquals("Luminence should be max for White", 255, hslColor.getLuminence());
    }

    /**
     * White-Box Strategy: Reflection / Logic verification
     * Target: Private method reverseLight()
     * Logic: This method is not exposed publicly. It inverts Luminence.
     */
    @Test
    public void testPrivate_reverseLight() throws Exception {
        hslColor.initRGBbyHSL(0, 0, 100); // Lum = 100

        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(hslColor);

        // Expected: HSLMAX (255) - 100 = 155
        assertEquals("Luminence should be inverted", 155, hslColor.getLuminence());
    }

    /**
     * White-Box Strategy: Reflection / Condition Coverage
     * Target: Private method hueToRGB
     * Logic: Direct invocation allows testing boundary conditions of 'Hue' argument
     * specifically the < 0 and > HSLMAX normalization logic which is hard to isolate
     * via the public API because initRGBbyHSL pre-calculates arguments.
     */
    @Test
    public void testPrivate_hueToRGB_Normalization() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        method.setAccessible(true);

        // mag1=0, mag2=255.
        // Case 1: Hue < 0. Pass -42. Should normalize to (-42 + 255) = 213.
        // 213 is > 2/3 of 255 (170). So it hits the last return 'mag1' (0) or the interpolation logic?
        // 213 is > 170. It returns mag1.
        int resNeg = (int) method.invoke(hslColor, 0, 255, -42);
        // If it didn't normalize, -42 < 42 (HSLMAX/6), it would return interpolation.
        // If it normalizes to 213, 213 > 170, it returns mag1 (0).
        assertEquals("Negative Hue should be normalized and fall through logic", 0, resNeg);

        // Case 2: Hue > HSLMAX. Pass 300. Normalizes to 300 - 255 = 45.
        // 45 is > 42 (HSLMAX/6) but < 127 (HSLMAX/2). Should return mag2 (255).
        int resOver = (int) method.invoke(hslColor, 0, 255, 300);
        assertEquals("Overflow Hue should be normalized and hit mid-range", 255, resOver);
    }

    /**
     * White-Box Strategy: Path Coverage / Branch Coverage
     * Target: initHSLbyRGB -> Hue Calculation -> Negative Hue Correction
     * Logic: To reach "if (pHue < 0)", we need (BDelta - GDelta) to be negative when Red is Max.
     * This means BDelta < GDelta.
     * BDelta = (cMax - B)... / cMinus. GDelta = (cMax - G)... / cMinus.
     * To make BDelta < GDelta, (cMax - B) must be < (cMax - G), implies B > G.
     * Inputs: Red=Max(255), Green=Low(100), Blue=Mid(200).
     */
    @Test
    public void testInitHSLbyRGB_Branch_NegativeHue() {
        // R=255, G=100, B=200. Max=R.
        // cMax=255, cMin=100. Delta=155.
        // GDelta approx (155 * 42)/155 = 42.
        // BDelta approx (55 * 42)/155 = 14.
        // pHue = 14 - 42 = -28.
        // Logic should add 255 -> 227.
        hslColor.initHSLbyRGB(255, 100, 200);

        int h = hslColor.getHue();
        assertTrue("Hue should be positive (wrapped)", h > 0);
        assertEquals("Hue value check", 227, h);
    }

    /**
     * White-Box Strategy: Condition Coverage / Boundary Analysis
     * Target: initRGBbyHSL -> RGB Clamping
     * Logic: "if (pRed > RGBMAX)".
     * We need to generate a value where the calculation exceeds 255.
     * This occurs when Luminence and Saturation are both maximized.
     * Magic2 calculation with L=255, S=255 results in values > 255 internally before clamping.
     */
    @Test
    public void testInitRGBbyHSL_Branch_Clamping() {
        // L = 255 (White), S = 255. Hue doesn't matter much, use 0.
        // This forces Magic2 to be high, and the conversion formula produces values > 255.
        // The code has explicit "if (pRed > RGBMAX) pRed = RGBMAX" blocks.
        hslColor.initRGBbyHSL(0, 255, 255);

        assertEquals("Red should be clamped to RGBMAX", 255, hslColor.getRed());
        assertEquals("Green should be clamped to RGBMAX", 255, hslColor.getGreen());
        assertEquals("Blue should be clamped to RGBMAX", 255, hslColor.getBlue());
    }

    /**
     * White-Box Strategy: Condition Coverage
     * Target: brighten -> Negative Calculation
     * Logic: "if (L < 0) L = 0".
     * Provide a negative percent such that pLum * percent is negative.
     * Black-box tested 0.5 (dimming), but not negative inversion.
     */
    @Test
    public void testBrighten_Branch_NegativeResult() {
        hslColor.initRGBbyHSL(0, 0, 100); // Lum = 100

        // Apply negative brighten factor
        hslColor.brighten(-0.5f); // 100 * -0.5 = -50

        assertEquals("Luminence should be clamped to 0 from negative result", 0, hslColor.getLuminence());
    }
}