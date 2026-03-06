/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: cri.dibenedetto@studenti.unina.it
UserID: 1093
Date: 24/11/2025
*/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class HSLColorTest {

    // Tolerance for floating point comparisons
    private static final float DELTA = 0.1f;

    // ---------------------------------------------------------
    // initHSLbyRGB Tests - Primary Colors (Branch Coverage: Max value selection)
    // ---------------------------------------------------------

    @Test
    public void initHSLbyRGBRedColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=255, G=0, B=0 -> Hue=0, Sat=100, Lum=50
        hslColor.initHSLbyRGB(255, 0, 0);

        assertEquals("Hue should be 0 for pure Red", 0.0f, hslColor.getHue(), DELTA);
        assertEquals("Saturation should be 100 for pure Red", 100.0f, hslColor.getSaturation(), DELTA);
        assertEquals("Luminence should be 50 for pure Red", 50.0f, hslColor.getLuminence(), DELTA);
    }

    @Test
    public void initHSLbyRGBGreenColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=0, G=255, B=0 -> Hue=120, Sat=100, Lum=50
        hslColor.initHSLbyRGB(0, 255, 0);

        assertEquals("Hue should be 120 for pure Green", 120.0f, hslColor.getHue(), DELTA);
        assertEquals("Saturation should be 100 for pure Green", 100.0f, hslColor.getSaturation(), DELTA);
        assertEquals("Luminence should be 50 for pure Green", 50.0f, hslColor.getLuminence(), DELTA);
    }

    @Test
    public void initHSLbyRGBBlueColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=0, G=0, B=255 -> Hue=240, Sat=100, Lum=50
        hslColor.initHSLbyRGB(0, 0, 255);

        assertEquals("Hue should be 240 for pure Blue", 240.0f, hslColor.getHue(), DELTA);
        assertEquals("Saturation should be 100 for pure Blue", 100.0f, hslColor.getSaturation(), DELTA);
        assertEquals("Luminence should be 50 for pure Blue", 50.0f, hslColor.getLuminence(), DELTA);
    }

    // ---------------------------------------------------------
    // initHSLbyRGB Tests - Secondary Colors (Branch Coverage: Combined Max)
    // ---------------------------------------------------------

    @Test
    public void initHSLbyRGBYellowColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=255, G=255, B=0 -> Hue=60
        hslColor.initHSLbyRGB(255, 255, 0);
        
        assertEquals("Hue should be 60 for Yellow", 60.0f, hslColor.getHue(), DELTA);
        assertEquals("Saturation should be 100", 100.0f, hslColor.getSaturation(), DELTA);
        assertEquals("Luminence should be 50", 50.0f, hslColor.getLuminence(), DELTA);
    }

    @Test
    public void initHSLbyRGBCyanColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=0, G=255, B=255 -> Hue=180
        hslColor.initHSLbyRGB(0, 255, 255);

        assertEquals("Hue should be 180 for Cyan", 180.0f, hslColor.getHue(), DELTA);
    }

    @Test
    public void initHSLbyRGBMagentaColorTest() {
        HSLColor hslColor = new HSLColor();
        // R=255, G=0, B=255 -> Hue=300
        hslColor.initHSLbyRGB(255, 0, 255);

        assertEquals("Hue should be 300 for Magenta", 300.0f, hslColor.getHue(), DELTA);
    }

    // ---------------------------------------------------------
    // initHSLbyRGB Tests - Boundary Values (Black, White, Gray)
    // ---------------------------------------------------------

    @Test
    public void initHSLbyRGBBlackBoundaryTest() {
        HSLColor hslColor = new HSLColor();
        // Black: R=0, G=0, B=0 -> L=0
        hslColor.initHSLbyRGB(0, 0, 0);

        assertEquals("Luminence should be 0 for Black", 0.0f, hslColor.getLuminence(), DELTA);
        assertEquals("Saturation should be 0 for Black", 0.0f, hslColor.getSaturation(), DELTA);
    }

    @Test
    public void initHSLbyRGBWhiteBoundaryTest() {
        HSLColor hslColor = new HSLColor();
        // White: R=255, G=255, B=255 -> L=100
        hslColor.initHSLbyRGB(255, 255, 255);

        assertEquals("Luminence should be 100 for White", 100.0f, hslColor.getLuminence(), DELTA);
        assertEquals("Saturation should be 0 for White", 0.0f, hslColor.getSaturation(), DELTA);
    }

    @Test
    public void initHSLbyRGBGrayAchromaticTest() {
        HSLColor hslColor = new HSLColor();
        // Gray: R=128, G=128, B=128 (Delta = 0 case)
        hslColor.initHSLbyRGB(128, 128, 128);

        assertEquals("Saturation should be 0 for Gray", 0.0f, hslColor.getSaturation(), DELTA);
        // Luminance calc: (min+max)/2 -> (128+128)/2 / 255 * 100 approx 50.2
        assertEquals("Luminence check", 50.2f, hslColor.getLuminence(), DELTA);
    }

    // ---------------------------------------------------------
    // Setters and Getters Direct Tests
    // ---------------------------------------------------------

    @Test
    public void setHueTypicalValueTest() {
        HSLColor hslColor = new HSLColor();
        hslColor.setHue(123.5f);
        assertEquals(123.5f, hslColor.getHue(), DELTA);
    }

    @Test
    public void setSaturationTypicalValueTest() {
        HSLColor hslColor = new HSLColor();
        hslColor.setSaturation(45.0f);
        assertEquals(45.0f, hslColor.getSaturation(), DELTA);
    }

    @Test
    public void setLuminenceTypicalValueTest() {
        HSLColor hslColor = new HSLColor();
        hslColor.setLuminence(75.5f);
        assertEquals(75.5f, hslColor.getLuminence(), DELTA);
    }
    
    // ---------------------------------------------------------
    // Edge Case: Conversion Logic Branches
    // ---------------------------------------------------------

    @Test
    public void initHSLbyRGBArbitraryColorTest() {
        HSLColor hslColor = new HSLColor();
        // A specific color to test arithmetic precision
        // R=50, G=100, B=150
        // Max=150(B), Min=50(R). Delta=100.
        // L = (150+50)/2 = 100 -> 100/255 = ~39.2%
        // S = 100 / (1 - |2*0.392 - 1|) ... approx logic
        
        hslColor.initHSLbyRGB(50, 100, 150);
        
        assertEquals("Hue check for R50 G100 B150", 210.0f, hslColor.getHue(), 1.0f); 
        assertEquals("Saturation check for R50 G100 B150", 50.0f, hslColor.getSaturation(), 1.0f); 
        assertEquals("Luminence check for R50 G100 B150", 39.2f, hslColor.getLuminence(), 1.0f);
    }
}