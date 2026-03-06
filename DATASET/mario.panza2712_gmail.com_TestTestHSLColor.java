/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: mario.panza2712@gmail.com
UserID: 434
Date: 22/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {

    private final int HSLMAX = 255;
    private final int UNDEFINED = 170;

    // --- Helper Methods for Reflection ---

    private void invokePrivateVoidMethod(HSLColor instance, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(instance, args);
    }

    private int invokePrivateIntMethod(HSLColor instance, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (int) method.invoke(instance, args);
    }

    // --- initHSLbyRGB Tests (Branch Coverage Focus) ---

    @Test
    public void initHSLbyRGBMaxIsRedTest() {
        HSLColor hsl = new HSLColor();
        // R > G && R > B -> Entra in if (cMax == R)
        hsl.initHSLbyRGB(200, 100, 50);
        // cMax=200, cMin=50, Delta=150. 
        // GDelta ~ 28, BDelta ~ 42. pHue = 42 - 28 = 14.
        assertEquals(14, hsl.getHue());
    }

    @Test
    public void initHSLbyRGBMaxIsGreenTest() {
        HSLColor hsl = new HSLColor();
        // G > R && G > B -> Entra in else if (cMax == G)
        hsl.initHSLbyRGB(50, 200, 100);
        // cMax=200, cMin=50. pHue = 85 + 42 - 28 = 99.
        assertEquals(99, hsl.getHue());
    }

    @Test
    public void initHSLbyRGBMaxIsBlueTest() {
        HSLColor hsl = new HSLColor();
        // B > R && B > G -> Entra in else if (cMax == B)
        // R=0, G=0, B=255. 
        // cMax=255, cMin=0. cMinus=255.
        // RDelta = 42, GDelta = 42, BDelta = 0.
        // pHue = 170 + GDelta(42) - RDelta(42) = 170.
        hsl.initHSLbyRGB(0, 0, 255);
        assertEquals(170, hsl.getHue());
        assertEquals(255, hsl.getSaturation());
        assertEquals(128, hsl.getLuminence());
    }

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(128, 128, 128);
        assertEquals(UNDEFINED, hsl.getHue());
        assertEquals(0, hsl.getSaturation());
    }

    @Test
    public void initHSLbyRGBLumLowTest() {
        HSLColor hsl = new HSLColor();
        // pLum <= 127
        hsl.initHSLbyRGB(100, 50, 0); 
        assertEquals(50, hsl.getLuminence());
    }

    @Test
    public void initHSLbyRGBLumHighTest() {
        HSLColor hsl = new HSLColor();
        // pLum > 127
        hsl.initHSLbyRGB(255, 205, 155); 
        assertEquals(205, hsl.getLuminence());
    }

    @Test
    public void initHSLbyRGBNegativeHueCorrectionTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 255); // Magenta
        // pHue calcolato è negativo, entra in if (pHue < 0)
        assertEquals(213, hsl.getHue());
    }

    // --- Setter Overflow Tests (Branch Coverage Focus) ---

    @Test
    public void setLuminenceOverflowTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        // Branch: else if (iToValue > HSLMAX)
        hsl.setLuminence(300); 
        assertEquals(255, hsl.getLuminence());
    }

    @Test
    public void setLuminenceUnderflowTest() {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(-50);
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void setSaturationOverflowTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        // Branch: else if (iToValue > HSLMAX)
        hsl.setSaturation(300);
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void setSaturationUnderflowTest() {
        HSLColor hsl = new HSLColor();
        hsl.setSaturation(-50);
        assertEquals(0, hsl.getSaturation());
    }

    @Test
    public void setHueCycleTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0);
        hsl.setHue(300); // > 255 -> -255 -> 45
        assertEquals(45, hsl.getHue());
        hsl.setHue(-10); // < 0 -> +255 -> 245
        assertEquals(245, hsl.getHue());
    }

    // --- initRGBbyHSL Tests ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        hsl.initRGBbyHSL(100, 0, 128);
        assertEquals(128, hsl.getRed());
    }

    @Test
    public void initRGBbyHSLLumLowTest() {
        HSLColor hsl = new HSLColor();
        hsl.initRGBbyHSL(0, 255, 64);
        assertTrue(hsl.getRed() > 0);
    }

    @Test
    public void initRGBbyHSLLumHighTest() {
        HSLColor hsl = new HSLColor();
        hsl.initRGBbyHSL(170, 255, 200);
        assertTrue(hsl.getBlue() > 0);
    }

    // --- Overflow Logic in initRGBbyHSL (Branch Coverage) ---

    @Test
    public void initRGBbyHSLRedOverflowTest() {
        HSLColor color = new HSLColor();
        // H=0, S=300 (overshoot), L=128 forces pRed > 255
        color.initRGBbyHSL(0, 300, 128);
        assertEquals(255, color.getRed());
    }

    @Test
    public void initRGBbyHSLGreenOverflowTest() {
        HSLColor color = new HSLColor();
        // H=85, S=300, L=128 forces pGreen > 255
        color.initRGBbyHSL(85, 300, 128);
        assertEquals(255, color.getGreen());
    }

    @Test
    public void initRGBbyHSLBlueOverflowTest() {
        HSLColor color = new HSLColor();
        // H=170, S=300, L=128 forces pBlue > 255
        color.initRGBbyHSL(170, 300, 128);
        assertEquals(255, color.getBlue());
    }

    // --- Private Methods Tests (Reflection) ---

    @Test
    public void hueToRGBBoundaryTests() throws Exception {
        HSLColor hsl = new HSLColor();
        // Test interno per i rami di hueToRGB
        // Hue < 0
        assertEquals(100, invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{100, 200, -10}));
        // Hue > HSLMAX
        assertEquals(200, invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{100, 200, 300}));
        // Hue < HSLMAX/6
        assertTrue(invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{0, 255, 20}) > 0);
        // Hue < HSLMAX/2
        assertEquals(255, invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{0, 255, 100}));
        // Hue < HSLMAX*2/3
        assertTrue(invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{0, 255, 150}) > 0);
        // Hue >= HSLMAX*2/3
        assertEquals(10, invokePrivateIntMethod(hsl, "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{10, 255, 200}));
    }

    @Test
    public void iMinMaxTests() throws Exception {
        HSLColor hsl = new HSLColor();
        assertEquals(20, invokePrivateIntMethod(hsl, "iMax", new Class[]{int.class, int.class}, new Object[]{10, 20}));
        assertEquals(20, invokePrivateIntMethod(hsl, "iMax", new Class[]{int.class, int.class}, new Object[]{20, 10}));
        assertEquals(10, invokePrivateIntMethod(hsl, "iMin", new Class[]{int.class, int.class}, new Object[]{10, 20}));
        assertEquals(10, invokePrivateIntMethod(hsl, "iMin", new Class[]{int.class, int.class}, new Object[]{20, 10}));
    }

    @Test
    public void privateSettersTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0,0,0);
        invokePrivateVoidMethod(hsl, "setRed", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getRed());
        invokePrivateVoidMethod(hsl, "setGreen", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getGreen());
        invokePrivateVoidMethod(hsl, "setBlue", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, hsl.getBlue());
    }

    @Test
    public void reverseLightTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(50);
        invokePrivateVoidMethod(hsl, "reverseLight", null, null);
        assertEquals(205, hsl.getLuminence());
    }
    
    @Test
    public void greyscalePrivateTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0);
        invokePrivateVoidMethod(hsl, "greyscale", null, null);
        assertEquals(UNDEFINED, hsl.getHue());
    }

    // --- Public Methods Logic Tests ---

    @Test
    public void reverseColorTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0);
        hsl.reverseColor();
        assertEquals(127, hsl.getHue());
    }

    @Test
    public void brightenTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(100, 100, 100);
        hsl.brighten(0.0f);
        assertEquals(100, hsl.getLuminence());
        hsl.brighten(1.5f); // 150
        assertEquals(150, hsl.getLuminence());
        hsl.brighten(5.0f); // Overflow
        assertEquals(255, hsl.getLuminence());
        hsl.brighten(-1.0f); // Underflow
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void blendTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        hsl.blend(255, 255, 255, 1.5f); // >= 1
        assertEquals(255, hsl.getLuminence());
        hsl.blend(0, 0, 0, -0.5f); // <= 0
        assertEquals(255, hsl.getLuminence());
        hsl.initHSLbyRGB(0, 0, 0);
        hsl.blend(255, 255, 255, 0.5f); // 50%
        assertEquals(127, hsl.getRed());
    }
}					