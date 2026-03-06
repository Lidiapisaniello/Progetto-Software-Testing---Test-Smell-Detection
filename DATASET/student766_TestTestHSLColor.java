import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class TestHSLColor {

    // --- Public Methods Tests ---

    @Test
    public void testInitHSLbyRGBGreyscale() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue()); // UNDEFINED
        // Lum = ((200 * 255) + 255) / (2 * 255) = 100 roughly.
        // Formula: ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX)
        // cPlus = 200. (200*255 + 255) / 510 = 51255 / 510 = 100.
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGBRedMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0);
        // Max=255, Min=0. cMinus=255, cPlus=255.
        // Lum = (255*255 + 255)/510 = 127.
        // Sat: Lum(127) <= 127. Sat = (255*255 + 0.5)/255 = 255.
        // Hue: Max=R. BDelta=0, GDelta=0. Hue=0.
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(127, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGBGreenMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0);
        // Hue should be around 85 (255/3).
        // HSLMAX/3 = 85.
        assertEquals(85, color.getHue());
    }

    @Test
    public void testInitHSLbyRGBBlueMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255);
        // Hue should be around 170 (2*255/3).
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGBLumHigh() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 255, 200);
        // Light color, Lum > 127.
        assertTrue(color.getLuminence() > 127);
    }

    @Test
    public void testInitRGBbyHSLGreyscale() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100);
        // S=0 -> R=G=B
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSLLumLow() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 100);
        // L <= 127
        assertEquals(100, color.getLuminence());
        assertEquals(255, color.getSaturation());
        assertEquals(0, color.getHue());
        // Verify RGB are set (not checking exact math, just that they are updated)
        assertTrue(color.getRed() > 0);
    }

    @Test
    public void testInitRGBbyHSLLumHigh() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 200);
        // L > 127
        assertEquals(200, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSLClamping() {
        // Force values that might overflow if not clamped?
        // The code clamps pRed > RGBMAX.
        // Hard to trigger with valid inputs, but we can try edge cases.
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 127);
        assertTrue(color.getRed() <= 255);
    }

    @Test
    public void testSetHue() {
        HSLColor color = new HSLColor();
        color.setHue(300); // > 255, should wrap
        // 300 - 255 = 45.
        assertEquals(45, color.getHue());

        color.setHue(-50); // < 0, should wrap
        // 255 - 50 = 205.
        assertEquals(205, color.getHue());
    }

    @Test
    public void testSetSaturation() {
        HSLColor color = new HSLColor();
        color.setSaturation(300); // > 255
        assertEquals(255, color.getSaturation());

        color.setSaturation(-50); // < 0
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        HSLColor color = new HSLColor();
        color.setLuminence(300); // > 255
        assertEquals(255, color.getLuminence());

        color.setLuminence(-50); // < 0
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 100);
        color.reverseColor();
        // 0 + 127 = 127.
        assertEquals(127, color.getHue());
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100);

        color.brighten(0); // No change
        assertEquals(100, color.getLuminence());

        color.brighten(1.5f); // 150
        assertEquals(150, color.getLuminence());

        color.brighten(3.0f); // > 255 -> 255
        assertEquals(255, color.getLuminence());

        color.brighten(-1.0f); // < 0 -> 0 (logic: L = 255 * -1 = -255 -> 0)
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);

        // fPercent >= 1
        color.blend(255, 255, 255, 1.5f);
        assertEquals(255, color.getLuminence()); // Should be white

        // fPercent <= 0
        color.blend(0, 0, 0, -0.5f);
        assertEquals(255, color.getLuminence()); // No change

        // Normal blend
        color.initHSLbyRGB(0, 0, 0);
        color.blend(100, 100, 100, 0.5f);
        // 50, 50, 50
        assertEquals(50, color.getRed());
    }

    @Test
    public void testGetters() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    // --- Private Methods Tests (Reflection) ---

    @Test
    public void testGreyscalePrivate() throws Exception {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 100); // Sat = 255

        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(color);

        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue()); // UNDEFINED
    }

    @Test
    public void testSetRedPrivate() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);

        assertEquals(255, color.getRed());
        // Should update HSL too
        assertEquals(127, color.getLuminence());
    }

    @Test
    public void testSetGreenPrivate() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);

        assertEquals(255, color.getGreen());
    }

    @Test
    public void testSetBluePrivate() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(color, 255);

        assertEquals(255, color.getBlue());
    }

    @Test
    public void testReverseLightPrivate() throws Exception {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100);

        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(color);

        // 255 - 100 = 155
        assertEquals(155, color.getLuminence());
    }
}