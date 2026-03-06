/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Lorenzo
Cognome: Cavaliere
Username: lo.cavaliere@studenti.unina.it
UserID: 645
Date: 25/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    // --- Scenario 1: RGB to HSL - Greyscale Path ---
    // Copre il ramo "if (cMax == cMin)" in initHSLbyRGB
    @Test
    public void testRGBtoHSL_Greyscale() {
        HSLColor color = new HSLColor();
        // R=G=B => Grigio
        color.initHSLbyRGB(128, 128, 128);

        assertEquals("Red should match input", 128, color.getRed());
        assertEquals("Green should match input", 128, color.getGreen());
        assertEquals("Blue should match input", 128, color.getBlue());

        // In greyscale, Saturation is 0 and Hue is UNDEFINED (170)
        assertEquals("Saturation should be 0 for greyscale", 0, color.getSaturation());
        assertEquals("Hue should be UNDEFINED for greyscale", 170, color.getHue());
        // Luminance calculation: ((256*255) + 255) / (2*255) approx
        // cMax=128, cMin=128. cPlus=256. (256*255 + 255) / 510 = 128.
        assertEquals("Luminence calculation verification", 128, color.getLuminence());
    }

    // --- Scenario 2: RGB to HSL - Red Dominant (Hue Sector 1) ---
    // Copre il ramo "if (cMax == R)" in initHSLbyRGB
    @Test
    public void testRGBtoHSL_RedDominant() {
        HSLColor color = new HSLColor();
        // Rosso puro
        color.initHSLbyRGB(255, 0, 0);

        // cMax=255, cMin=0. cMinus=255.
        // Hue calculation for Red dominant usually results in 0 (or near 0/255)
        int hue = color.getHue();
        // HSLMAX is 255. Pure Red is 0 or 255 (cyclic). 
        // Logic: Hue = BDelta - GDelta. GDelta & BDelta depends on cMinus.
        // Qui ci aspettiamo 0 o HSLMAX.
        assertTrue("Hue for pure Red should be 0 or 255", hue == 0 || hue == 255);
        assertEquals("Saturation for pure color should be max", 255, color.getSaturation());
        assertEquals("Luminence should be approx half", 127, color.getLuminence()); // (255*255 + 255)/510 = 127.5 -> 127
    }

    // --- Scenario 3: RGB to HSL - Green Dominant (Hue Sector 2) ---
    // Copre il ramo "else if (cMax == G)" in initHSLbyRGB
    @Test
    public void testRGBtoHSL_GreenDominant() {
        HSLColor color = new HSLColor();
        // Verde puro
        color.initHSLbyRGB(0, 255, 0);

        // Hue should be around 1/3 of 255 (approx 85)
        assertEquals(85, color.getHue());
        assertEquals(255, color.getSaturation());
    }

    // --- Scenario 4: RGB to HSL - Blue Dominant (Hue Sector 3) ---
    // Copre il ramo "else if (cMax == B)" in initHSLbyRGB
    @Test
    public void testRGBtoHSL_BlueDominant() {
        HSLColor color = new HSLColor();
        // Blu puro
        color.initHSLbyRGB(0, 0, 255);

        // Hue should be around 2/3 of 255 (approx 170)
        assertEquals(170, color.getHue());
        assertEquals(255, color.getSaturation());
    }

    // --- Scenario 5: RGB to HSL - Saturation Logic (Luminance Thresholds) ---
    // Copre i rami if/else basati su pLum <= HSLMAX/2
    @Test
    public void testRGBtoHSL_LuminanceThresholds() {
        HSLColor color = new HSLColor();
        
        // Case 1: Low Luminance (Dark) -> pLum <= 127
        // R=50, G=50, B=0 -> cMax=50, cMin=0, cPlus=50. Lum ~ 25.
        color.initHSLbyRGB(50, 50, 0);
        assertTrue("Luminance should be low", color.getLuminence() <= 127);
        // Verify Saturation calculation path for low lum
        
        // Case 2: High Luminance (Light) -> pLum > 127
        // R=200, G=200, B=255 -> cMax=255, cMin=200, cPlus=455. Lum ~ 227.
        color.initHSLbyRGB(200, 200, 255);
        assertTrue("Luminance should be high", color.getLuminence() > 127);
    }

    // --- Scenario 6: HSL to RGB - Greyscale Path ---
    // Copre il ramo "if (S == 0)" in initRGBbyHSL
    @Test
    public void testHSLtoRGB_Greyscale() {
        HSLColor color = new HSLColor();
        // H=Any, S=0, L=128
        color.initRGBbyHSL(100, 0, 128);

        assertEquals("Red should be derived from Lum", 128, color.getRed());
        assertEquals("Green should be derived from Lum", 128, color.getGreen());
        assertEquals("Blue should be derived from Lum", 128, color.getBlue());
    }

    // --- Scenario 7: HSL to RGB - Color Path & hueToRGB Helper ---
    // Questo test mira a coprire i vari rami nel metodo privato hueToRGB
    // Chiamato indirettamente tramite initRGBbyHSL
    @Test
    public void testHSLtoRGB_ColorPaths() {
        HSLColor color = new HSLColor();

        // hueToRGB ha rami per Hue < HSLMAX/6, < HSLMAX/2, < HSLMAX*2/3, else.
        // HSLMAX = 255.
        // Settori: < 42, < 127, < 170, >= 170.

        // 1. Test Hue in Sector 1 (< 42)
        color.initRGBbyHSL(20, 255, 128);
        // Verify results are consistent (not 0)
        assertTrue(color.getRed() > 0 || color.getGreen() > 0);

        // 2. Test Hue in Sector 2 (< 127) -> approx 85 (Greenish)
        color.initRGBbyHSL(85, 255, 128);
        assertEquals(255, color.getGreen()); // Green peak

        // 3. Test Hue in Sector 3 (< 170) -> approx 150 (Blueish-Cyan)
        color.initRGBbyHSL(150, 255, 128); 
        
        // 4. Test Hue in Sector 4 (>= 170) -> approx 200 (Purple/Magenta)
        color.initRGBbyHSL(200, 255, 128);
        assertTrue(color.getBlue() > 100 && color.getRed() > 100);
    }

    // --- Scenario 8: HSL to RGB - Luminance Calculation Branch ---
    // Copre il ramo "if (L <= HSLMAX / 2)" in initRGBbyHSL per Magic2
    @Test
    public void testHSLtoRGB_LuminanceBranch() {
        HSLColor color = new HSLColor();

        // Low Lum
        color.initRGBbyHSL(0, 255, 50);
        assertTrue(color.getLuminence() <= 127);
        // High Lum
        color.initRGBbyHSL(0, 255, 200);
        assertTrue(color.getLuminence() > 127);
    }

    // --- Scenario 9: Setters & Validation Logic ---
    @Test
    public void testSettersAndNormalization() {
        HSLColor color = new HSLColor();

        // Hue Normalization (while loops)
        color.setHue(300); // > 255 -> Should subtract 255 -> 45
        assertEquals(45, color.getHue());

        color.setHue(-50); // < 0 -> Should add 255 -> 205
        assertEquals(205, color.getHue());

        // Saturation Clamping
        color.setSaturation(300); // > 255 -> 255
        assertEquals(255, color.getSaturation());
        color.setSaturation(-10); // < 0 -> 0
        assertEquals(0, color.getSaturation());

        // Luminence Clamping
        color.setLuminence(300); // > 255 -> 255
        assertEquals(255, color.getLuminence());
        color.setLuminence(-10); // < 0 -> 0
        assertEquals(0, color.getLuminence());
    }

    // --- Scenario 10: Utility Methods (Reverse, Brighten, Blend) ---
    @Test
    public void testUtilityMethods() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100); // Gray

        // Reverse Color
        int originalHue = color.getHue(); // 170
        color.reverseColor();
        // 170 + 127 = 297 -> wrap -> 42
        assertEquals(42, color.getHue());

        // Brighten
        color.setLuminence(100);
        color.brighten(0.5f); // 100 * 0.5 = 50
        assertEquals(50, color.getLuminence());
        
        // Brighten Edge Cases
        color.brighten(0); // No change
        assertEquals(50, color.getLuminence());
        
        // Blend
        // Blend 0% -> No change
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getRed());

        // Blend 100% -> Full new color
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getRed());

        // Blend 50%
        color.initHSLbyRGB(0, 0, 0); // Black
        color.blend(255, 255, 255, 0.5f); // Grey
        // newR = (255*0.5) + (0*0.5) = 127
        assertEquals(127, color.getRed());
    }
    
    // --- Scenario 11: Boundary Conditions in initRGBbyHSL ---
    // Copre i check "if (pRed > RGBMAX)" etc.
    // È difficile triggerarli matematicamente con input validi perché la matematica dovrebbe prevenire overflow,
    // ma proviamo con valori saturi.
    @Test
    public void testRGBClamping() {
        HSLColor color = new HSLColor();
        // Proviamo a forzare valori alti
        color.initRGBbyHSL(0, 255, 255); // White
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }
}