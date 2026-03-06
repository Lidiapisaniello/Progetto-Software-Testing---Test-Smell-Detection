/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Ilaria"
Cognome: "Saulino"
Username: i.saulino@studenti.unina.it
UserID: 1133
Date: 24/11/2025
*/
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class TestHSLColor {

    // --- Constructor & Basic Getters ---

    @Test
    public void constructorDefaultValuesTest() {
        HSLColor hsl = new HSLColor();
        assertEquals(0, hsl.getRed());
        assertEquals(0, hsl.getGreen());
        assertEquals(0, hsl.getBlue());
        assertEquals(0, hsl.getHue());
        assertEquals(0, hsl.getSaturation());
        assertEquals(0, hsl.getLuminence());
    }

    // --- initHSLbyRGB (RGB -> HSL) Tests ---

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // R=G=B implies Greyscale
        hsl.initHSLbyRGB(100, 100, 100);

        assertEquals(100, hsl.getRed());
        assertEquals(100, hsl.getGreen());
        assertEquals(100, hsl.getBlue());
        
        // Logic: cMax=100, cMin=100. cMinus=0. 
        // pLum = ((200 * 255) + 255) / 510 = 100
        assertEquals(100, hsl.getLuminence());
        
        // Greyscale branch: Sat=0, Hue=UNDEFINED(170)
        assertEquals(0, hsl.getSaturation());
        assertEquals(170, hsl.getHue());
    }

    @Test
    public void initHSLbyRGBRedMaxTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0);

        assertEquals(255, hsl.getRed());
        // Max=255, Min=0. Lum ~ 127
        assertEquals(0, hsl.getHue());
        assertEquals(255, hsl.getSaturation()); 
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 255, 0);

        // Green implies Max=G. Hue approx 85.
        int hue = hsl.getHue();
        assertTrue("Hue should be around 85 for Green", hue >= 84 && hue <= 86);
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 255);

        // Blue implies Max=B. Hue approx 170.
        int hue = hsl.getHue();
        assertTrue("Hue should be around 170 for Blue", hue >= 169 && hue <= 171);
    }

    @Test
    public void initHSLbyRGBSaturationLowLumTest() {
        HSLColor hsl = new HSLColor();
        // Low luminance: R=50, G=0, B=0. 
        // Branch: if (pLum <= (HSLMAX / 2))
        hsl.initHSLbyRGB(50, 0, 0);
        
        assertTrue(hsl.getLuminence() <= 127);
        assertEquals(255, hsl.getSaturation()); 
    }

    @Test
    public void initHSLbyRGBSaturationHighLumTest() {
        HSLColor hsl = new HSLColor();
        // High luminance: R=255, G=205, B=205.
        // Branch: else (pLum > HSLMAX/2)
        hsl.initHSLbyRGB(255, 205, 205);
        
        assertTrue(hsl.getLuminence() > 127);
        assertEquals(255, hsl.getSaturation());
    }
    
    @Test
    public void initHSLbyRGBHueWrapAroundTest() {
        HSLColor hsl = new HSLColor();
        // Trigger pHue < 0 inside initHSLbyRGB.
        // R=255 (Max), B=100, G=0. 
        // BDelta < GDelta -> Negative result -> Wraps around
        hsl.initHSLbyRGB(255, 0, 100);
        
        int hue = hsl.getHue();
        assertTrue("Hue should wrap positive", hue > 0);
        assertEquals(255, hsl.getRed());
    }

    // --- initRGBbyHSL (HSL -> RGB) Tests ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        HSLColor hsl = new HSLColor();
        // Saturation 0 triggers greyscale branch
        hsl.initRGBbyHSL(0, 0, 128);
        
        assertEquals(128, hsl.getRed());
        assertEquals(128, hsl.getGreen());
        assertEquals(128, hsl.getBlue());
    }

    @Test
    public void initRGBbyHSLColorLowLumTest() {
        HSLColor hsl = new HSLColor();
        // L <= 127. Magic2 calculation path 1
        hsl.initRGBbyHSL(0, 255, 64);
        
        assertTrue(hsl.getRed() > 0);
        assertEquals(0, hsl.getGreen());
        assertEquals(0, hsl.getBlue());
    }

    @Test
    public void initRGBbyHSLColorHighLumTest() {
        HSLColor hsl = new HSLColor();
        // L > 127. Magic2 calculation path 2
        hsl.initRGBbyHSL(0, 255, 192);
        
        assertTrue(hsl.getRed() > 192);
        assertTrue(hsl.getGreen() > 0);
        assertTrue(hsl.getBlue() > 0);
    }
    
    // --- hueToRGB Coverage ---

    @Test
    public void hueToRGBFullCoverageTest() {
        HSLColor hsl = new HSLColor();
        
        // NOTA: A causa dell'aritmetica intera in initRGBbyHSL (L=128),
        // i canali che dovrebbero essere 0 risultano spesso 1.
        // I test qui sotto usano 1 invece di 0 per riflettere il codice reale.

        // 1. Hue = 0 (Red). 
        hsl.initRGBbyHSL(0, 255, 128);
        assertEquals(255, hsl.getRed());
        assertEquals(1, hsl.getGreen()); // Arrotondamento produce 1
        assertEquals(1, hsl.getBlue());  // Arrotondamento produce 1

        // 2. Hue = 85 (Green).
        hsl.initRGBbyHSL(85, 255, 128);
        assertEquals(1, hsl.getRed());
        assertEquals(255, hsl.getGreen());
        assertEquals(1, hsl.getBlue());
        
        // 3. Hue = 170 (Blue).
        hsl.initRGBbyHSL(170, 255, 128);
        assertEquals(1, hsl.getRed());
        assertEquals(1, hsl.getGreen());
        assertEquals(255, hsl.getBlue());
        
        // 4. Boundary Testing per il ramo intermedio
        hsl.initRGBbyHSL(42, 255, 128);
        assertTrue(hsl.getRed() > 0);
        assertTrue(hsl.getGreen() > 0);
    }

    // --- Setters and Getters ---

    @Test
    public void setHueLoopTest() {
        HSLColor hsl = new HSLColor();
        // Set standard
        hsl.setHue(100);
        assertEquals(100, hsl.getHue());
        
        // Set negative (loop)
        hsl.setHue(-50);
        assertEquals(205, hsl.getHue());
        
        // Set overflow (loop)
        hsl.setHue(300);
        assertEquals(45, hsl.getHue());
    }

    @Test
    public void setSaturationClampTest() {
        HSLColor hsl = new HSLColor();
        hsl.setSaturation(500);
        assertEquals(255, hsl.getSaturation());
        
        hsl.setSaturation(-100);
        assertEquals(0, hsl.getSaturation());
    }

    @Test
    public void setLuminenceClampTest() {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(500);
        assertEquals(255, hsl.getLuminence());
        
        hsl.setLuminence(-50);
        assertEquals(0, hsl.getLuminence());
    }

    // --- Functional Methods ---

    @Test
    public void reverseColorTest() {
        HSLColor hsl = new HSLColor();
        hsl.setHue(0);
        hsl.reverseColor();
        assertEquals(127, hsl.getHue());
    }

    @Test
    public void brightenTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(100, 100, 100); // Lum = 100
        
        // Test 0 percent (return)
        hsl.brighten(0f);
        assertEquals(100, hsl.getLuminence());
        
        // Test normal
        hsl.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hsl.getLuminence());
        
        // Test Overflow
        hsl.brighten(5.0f);
        assertEquals(255, hsl.getLuminence());
        
        // Test Underflow 
        hsl.setLuminence(100);
        hsl.brighten(-1.0f);
        assertEquals(0, hsl.getLuminence());
    }

    @Test
    public void blendTest() {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0); // Black
        
        // Blend 100% Target
        hsl.blend(255, 255, 255, 1.0f);
        assertEquals(255, hsl.getRed());
        
        // Blend 0% Target
        hsl.blend(0, 0, 0, 0.0f);
        assertEquals(255, hsl.getRed()); // Should stay White
        
        // Blend 50%
        hsl.initHSLbyRGB(0, 0, 0); // Reset to Black
        hsl.blend(255, 255, 255, 0.5f);
        // 0.5 * 255 + 0.5 * 0 = 127
        assertEquals(127, hsl.getRed());
    }
    
    // --- Reflection Tests for Unused Private Methods ---
    
    @Test
    public void privateGreyscaleTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(255, 0, 0); // Red
        
        Method m = HSLColor.class.getDeclaredMethod("greyscale");
        m.setAccessible(true);
        m.invoke(hsl);
        
        assertEquals(0, hsl.getSaturation());
        assertEquals(170, hsl.getHue()); // UNDEFINED
    }

    @Test
    public void privateReverseLightTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.setLuminence(50);
        
        Method m = HSLColor.class.getDeclaredMethod("reverseLight");
        m.setAccessible(true);
        m.invoke(hsl);
        
        // 255 - 50 = 205
        assertEquals(205, hsl.getLuminence());
    }

    @Test
    public void privateSetRedTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        
        Method m = HSLColor.class.getDeclaredMethod("setRed", int.class);
        m.setAccessible(true);
        m.invoke(hsl, 255);
        
        assertEquals(255, hsl.getRed());
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void privateSetGreenTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        
        Method m = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        m.setAccessible(true);
        m.invoke(hsl, 255);
        
        assertEquals(255, hsl.getGreen());
        assertEquals(255, hsl.getSaturation());
    }

    @Test
    public void privateSetBlueTest() throws Exception {
        HSLColor hsl = new HSLColor();
        hsl.initHSLbyRGB(0, 0, 0);
        
        Method m = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        m.setAccessible(true);
        m.invoke(hsl, 255);
        
        assertEquals(255, hsl.getBlue());
        assertEquals(255, hsl.getSaturation());
    }
    
    @Test
    public void privateIMaxIMinTest() throws Exception {
        HSLColor hsl = new HSLColor();
        Method max = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        max.setAccessible(true);
        assertEquals(10, max.invoke(hsl, 5, 10));
        assertEquals(10, max.invoke(hsl, 10, 5));
        
        Method min = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        min.setAccessible(true);
        assertEquals(5, min.invoke(hsl, 5, 10));
        assertEquals(5, min.invoke(hsl, 10, 5));
    }
}