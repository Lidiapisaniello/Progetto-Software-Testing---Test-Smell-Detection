/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: s.giustizieri@studenti.unina.it
UserID: 273
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {
	
    private HSLColor hslColor;
    private final int HSLMAX = 255;
    private final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- Test initHSLbyRGB (RGB -> HSL) ---

    @Test
    public void testInitHSLbyRGB_Grayscale() {
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Grayscale Saturation should be 0", 0, hslColor.getSaturation());
        assertEquals("Grayscale Hue should be UNDEFINED", UNDEFINED, hslColor.getHue());
        assertEquals("Luminance check", 100, hslColor.getLuminence());
        assertEquals(100, hslColor.getRed());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        hslColor.initHSLbyRGB(255, 0, 0);
        
        assertEquals(255, hslColor.getRed());
        // CORREZIONE: Il calcolo intero esatto ((255*255)+255)/510 da 128, non 127
        assertEquals(128, hslColor.getLuminence()); 
        
        // Verifica Hue (Rosso è 0 o vicino a 0/255 nel cerchio)
        assertTrue(hslColor.getHue() < 10 || hslColor.getHue() > 240); 
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        hslColor.initHSLbyRGB(0, 255, 0);
        assertEquals(85, hslColor.getHue(), 1); // ~1/3 di 255
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        hslColor.initHSLbyRGB(0, 0, 255);
        assertEquals(170, hslColor.getHue(), 1); // ~2/3 di 255
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueCorrection() {
        // Forza Hue negativo prima della correzione: Max=R, ma B > G
        hslColor.initHSLbyRGB(200, 10, 100);
        
        assertTrue("Hue should be normalized to positive", hslColor.getHue() >= 0);
        assertTrue("Hue should be valid range", hslColor.getHue() <= HSLMAX);
    }

    @Test
    public void testInitHSLbyRGB_LowLuminanceBranch() {
        // pLum <= (HSLMAX / 2)
        hslColor.initHSLbyRGB(10, 20, 30);
        assertTrue(hslColor.getLuminence() <= (HSLMAX / 2));
        assertTrue(hslColor.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_HighLuminanceBranch() {
        // pLum > (HSLMAX / 2)
        hslColor.initHSLbyRGB(230, 240, 250);
        assertTrue(hslColor.getLuminence() > (HSLMAX / 2));
    }

    // --- Test initRGBbyHSL (HSL -> RGB) ---

    @Test
    public void testInitRGBbyHSL_Grayscale() {
        // Se S == 0
        hslColor.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_LowLum() {
        hslColor.initRGBbyHSL(0, 255, 100); 
        assertEquals(100, hslColor.getLuminence());
        assertNotEquals(hslColor.getRed(), hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_HighLum() {
        hslColor.initRGBbyHSL(0, 255, 200); 
        assertEquals(200, hslColor.getLuminence());
        assertTrue(hslColor.getRed() > 200);
    }

    // --- NUOVI TEST PER COPERTURA OVERFLOW ---
    // Questi test passano valori L fuori scala (>255) per forzare l'attivazione
    // delle guardie 'if (pRed > RGBMAX)' etc.

    @Test
    public void testInitRGBbyHSL_OverflowGuard_Red() {
        // Hue 0 (Rosso) + Luminance esagerata (500)
        // Questo causerà un calcolo di pRed > 255
        hslColor.initRGBbyHSL(0, 255, 500); 
        assertEquals(255, hslColor.getRed()); // Deve essere bloccato a 255
    }

    @Test
    public void testInitRGBbyHSL_OverflowGuard_Green() {
        // Hue 85 (Verde) + Luminance esagerata
        hslColor.initRGBbyHSL(85, 255, 500);
        assertEquals(255, hslColor.getGreen()); // Deve essere bloccato a 255
    }

    @Test
    public void testInitRGBbyHSL_OverflowGuard_Blue() {
        // Hue 170 (Blu) + Luminance esagerata
        hslColor.initRGBbyHSL(170, 255, 500);
        assertEquals(255, hslColor.getBlue()); // Deve essere bloccato a 255
    }

    // --- Test hueToRGB (copertura rami interni) ---
    
    @Test
    public void testHueToRGB_Sectors() {
        // Sector 1: Hue < HSLMAX/6
        hslColor.initRGBbyHSL(20, 255, 128); 
        // Sector 2: Hue < HSLMAX/2
        hslColor.initRGBbyHSL(85, 255, 128); 
        // Sector 3: Hue < HSLMAX*2/3
        hslColor.initRGBbyHSL(150, 255, 128); 
        // Sector 4: Else
        hslColor.initRGBbyHSL(220, 255, 128);
    }

    @Test
    public void testHueToRGB_RangeCorrection() {
        // Correzione Hue < 0
        hslColor.initRGBbyHSL(0, 255, 128);
        // Correzione Hue > HSLMAX
        hslColor.initRGBbyHSL(255, 255, 128);
    }

    // --- Test Getters & Setters & Loops ---

    @Test
    public void testSetHue_Loops() {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        // Test while (iToValue < 0)
        hslColor.setHue(-300); 
        assertEquals(210, hslColor.getHue());

        // Test while (iToValue > HSLMAX)
        hslColor.setHue(600); 
        assertEquals(90, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
        
        hslColor.setSaturation(50);
        assertEquals(50, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(-50);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(500);
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(100);
        assertEquals(100, hslColor.getLuminence());
    }
    
    @Test
    public void testGettersBasic() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }

    // --- Test Utility Methods ---

    @Test
    public void testReverseColor() {
        hslColor.initHSLbyRGB(255, 0, 0);
        int oldHue = hslColor.getHue();
        hslColor.reverseColor();
        int expected = oldHue + (255/2);
        assertEquals(expected, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.initHSLbyRGB(100, 100, 100); 
        
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
        
        hslColor.brighten(0.5f);
        assertEquals(50, hslColor.getLuminence());
        
        hslColor.setLuminence(100);
        hslColor.brighten(5.0f); // Overflow
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(100);
        hslColor.brighten(-1.0f); // Underflow
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); 
        
        // fPercent >= 1
        hslColor.blend(255, 255, 255, 1.5f);
        assertEquals(255, hslColor.getRed());
        
        // fPercent <= 0
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, -0.5f);
        assertEquals(0, hslColor.getRed());
        
        // Blend normale
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(100, 100, 100, 0.5f);
        assertEquals(50, hslColor.getRed());
    }

    // --- REFLECTION SECTION (Metodi Privati) ---

    @Test
    public void testPrivate_Greyscale() throws Exception {
        hslColor.initHSLbyRGB(255, 0, 0);
        Method m = HSLColor.class.getDeclaredMethod("greyscale");
        m.setAccessible(true);
        m.invoke(hslColor);
        
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testPrivate_ReverseLight() throws Exception {
        hslColor.setLuminence(100);
        Method m = HSLColor.class.getDeclaredMethod("reverseLight");
        m.setAccessible(true);
        m.invoke(hslColor);
        assertEquals(155, hslColor.getLuminence());
    }

    @Test
    public void testPrivate_SetRed() throws Exception {
        hslColor.initHSLbyRGB(0, 50, 50);
        Method m = HSLColor.class.getDeclaredMethod("setRed", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255);
        assertEquals(255, hslColor.getRed());
    }

    @Test
    public void testPrivate_SetGreen() throws Exception {
        hslColor.initHSLbyRGB(50, 0, 50);
        Method m = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255);
        assertEquals(255, hslColor.getGreen());
    }

    @Test
    public void testPrivate_SetBlue() throws Exception {
        hslColor.initHSLbyRGB(50, 50, 0);
        Method m = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255);
        assertEquals(255, hslColor.getBlue());
    }
    
    @Test
    public void testPrivate_IMax_IMin() throws Exception {
        Method maxM = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        maxM.setAccessible(true);
        assertEquals(10, maxM.invoke(hslColor, 5, 10));
        assertEquals(10, maxM.invoke(hslColor, 10, 5));

        Method minM = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        minM.setAccessible(true);
        assertEquals(5, minM.invoke(hslColor, 5, 10));
        assertEquals(5, minM.invoke(hslColor, 10, 5));
    }
}

						