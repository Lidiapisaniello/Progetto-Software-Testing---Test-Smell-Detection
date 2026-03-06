import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int UNDEFINED = 170;

    @Test
    public void testDefaults() {
        HSLColor color = new HSLColor();
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(0, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        HSLColor color = new HSLColor();

        // Black
        color.initHSLbyRGB(0, 0, 0);
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());

        // White
        color.initHSLbyRGB(255, 255, 255);
        assertEquals(255, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());

        // Middle Grey
        color.initHSLbyRGB(128, 128, 128);
        assertEquals(128, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_PrimaryColorsAndHueCalculation() {
        HSLColor color = new HSLColor();

        // Red (Max = R)
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(255, color.getRed());
        assertEquals(0, color.getHue()); // Hue = 0 for pure red
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());

        // Green (Max = G)
        color.initHSLbyRGB(0, 255, 0);
        // Hue formula for G max: (HSLMAX/3) + ... = 85
        assertEquals(85, color.getHue());

        // Blue (Max = B)
        color.initHSLbyRGB(0, 0, 255);
        // Hue formula for B max: (2*HSLMAX/3) + ... = 170
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_SaturationBranches() {
        HSLColor color = new HSLColor();

        // Test pLum <= HSLMAX/2
        // R=100, G=0, B=0 -> Lum=50
        color.initHSLbyRGB(100, 0, 0);
        assertEquals(50, color.getLuminence());
        assertEquals(255, color.getSaturation());

        // Test pLum > HSLMAX/2
        // R=255, G=255, B=0 (Yellow) -> Lum=128
        color.initHSLbyRGB(255, 255, 0);
        assertEquals(128, color.getLuminence());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueWrapping() {
        HSLColor color = new HSLColor();
        // Trigger pHue < 0. Requires Max=R and B > G significantly.
        // R=255, G=0, B=100.
        // Hue approx = -17 + 255 = 238.
        color.initHSLbyRGB(255, 0, 100);
        assertTrue(color.getHue() > 0);
        assertEquals(238, color.getHue());
    }

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        HSLColor color = new HSLColor();
        // S=0
        color.initRGBbyHSL(100, 0, 128);
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void testInitRGBbyHSL_ColorBranches() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2
        color.initRGBbyHSL(0, 255, 60);
        assertEquals(60, color.getLuminence());
        // Pure Red at lower lum
        assertTrue(color.getRed() > 0);
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());

        // L > HSLMAX/2
        color.initRGBbyHSL(0, 255, 180);
        assertEquals(180, color.getLuminence());
        // Pure Red at higher lum (light red)
        assertEquals(255, color.getRed());
        assertTrue(color.getGreen() > 0);
        assertTrue(color.getBlue() > 0);
    }

    @Test
    public void testHueToRGB_Branches() {
        // Cover paths in hueToRGB by selecting specific H values
        // hueToRGB calls: H+85, H, H-85
        HSLColor color = new HSLColor();

        // H=0
        // R (H+85=85): range < 127 (returns mag2)
        // G (H=0): range < 42 (returns mag1 + ...)
        // B (H-85 -> 170): range >= 170 (returns mag1)
        color.initRGBbyHSL(0, 255, 128);
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());

        // Need to hit range < 170 (127..170)
        // Try H=140
        // G arg is 140 -> falls into < 170 branch
        color.initRGBbyHSL(140, 255, 128);
        // Just verify valid state
        assertEquals(140, color.getHue());
    }

    @Test
    public void testSetHue() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0,0,0);

        // Normal
        color.setHue(100);
        assertEquals(100, color.getHue());

        // Underflow loop
        color.setHue(-300); // -300 + 255 = -45 + 255 = 210
        assertEquals(210, color.getHue());

        // Overflow loop
        color.setHue(600); // 600 - 255 = 345 - 255 = 90
        assertEquals(90, color.getHue());
    }

    @Test
    public void testSetSaturation() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128,128,128);

        color.setSaturation(100);
        assertEquals(100, color.getSaturation());

        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());

        color.setSaturation(300);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        HSLColor color = new HSLColor();

        color.setLuminence(100);
        assertEquals(100, color.getLuminence());

        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());

        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.setHue(0);
        color.reverseColor();
        // 0 + 127 = 127
        assertEquals(127, color.getHue());
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100); // Lum 100

        // 0%
        color.brighten(0.0f);
        assertEquals(100, color.getLuminence());

        // 150%
        color.brighten(1.5f);
        assertEquals(150, color.getLuminence());

        // Overflow
        color.brighten(10.0f);
        assertEquals(255, color.getLuminence());

        // Negative (0)
        color.brighten(-1.0f);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Black

        // Percent >= 1
        color.blend(255, 255, 255, 1.1f);
        assertEquals(255, color.getRed());

        // Percent <= 0
        color.blend(0, 0, 0, -0.5f);
        assertEquals(255, color.getRed()); // Unchanged

        // Blend 50% with Black
        // Current is White (255,255,255)
        // Target is Black (0,0,0)
        // new = 0*0.5 + 255*0.5 = 127
        color.blend(0, 0, 0, 0.5f);
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    @Test
    public void testPrivateMethodsViaReflection() throws Exception {
        // To maximize coverage, we invoke private methods that are otherwise dead code
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);

        // setRed
        Method setRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRed.setAccessible(true);
        setRed.invoke(color, 255);
        assertEquals(255, color.getRed());

        // setGreen
        Method setGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreen.setAccessible(true);
        setGreen.invoke(color, 255);
        assertEquals(255, color.getGreen());

        // setBlue
        Method setBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlue.setAccessible(true);
        setBlue.invoke(color, 255);
        assertEquals(255, color.getBlue());

        // reverseLight
        // Current Lum should be 255 (White)
        Method reverseLight = HSLColor.class.getDeclaredMethod("reverseLight");
        reverseLight.setAccessible(true);
        reverseLight.invoke(color);
        assertEquals(0, color.getLuminence());

        // greyscale
        // Reset to a color
        color.initHSLbyRGB(255, 0, 0);
        assertTrue(color.getSaturation() > 0);
        Method greyscale = HSLColor.class.getDeclaredMethod("greyscale");
        greyscale.setAccessible(true);
        greyscale.invoke(color);
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
    }
}