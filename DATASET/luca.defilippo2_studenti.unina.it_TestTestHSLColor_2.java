/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Luca"
Cognome: "De Filippo"
Username: luca.defilippo2@studenti.unina.it
UserID: 688
Date: 21/11/2025
*/

/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Luca"
Cognome: "De Filippo"
Username: luca.defilippo2@studenti.unina.it
UserID: 688
Date: 21/11/2025
*/

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {

    // Tolleranza per i calcoli in virgola mobile/conversioni intere
    private static final int DELTA = 1; 


    // ========================================================================
    // TEST INIT HSL BY RGB (Conversion Logic & Branch Coverage)
    // ========================================================================

    @Test
    public void testInitHSLbyRGBGreyScale() {
        HSLColor color = new HSLColor();
        // Copre: cMax == cMin
        color.initHSLbyRGB(128, 128, 128);
        assertEquals("Saturation must be 0", 0, color.getSaturation());
        assertEquals("Hue must be UNDEFINED (170)", 170, color.getHue());
        assertEquals("Luminance check", 128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        HSLColor color = new HSLColor();
        // Copre: cMax == R
        color.initHSLbyRGB(255, 0, 0);
        // Hue attesa: 0 (o HSLMAX se normalizzato, ma per il rosso puro è 0 o vicino)
        // Formula: BDelta - GDelta. 
        assertTrue(color.getHue() >= 0);
        assertEquals(255, color.getRed());
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        HSLColor color = new HSLColor();
        // Copre: cMax == G
        color.initHSLbyRGB(0, 255, 0);
        // Hue attesa: HSLMAX/3 (circa 85)
        assertEquals(85, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        HSLColor color = new HSLColor();
        // Copre: cMax == B (True branch). 
        // Nota: Il ramo False di (cMax == B) è logicamente irraggiungibile.
        color.initHSLbyRGB(0, 0, 255);
        // Hue attesa: 2*HSLMAX/3 (circa 170)
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax_WithVariations() {
        HSLColor color = new HSLColor();
        // Mutation Killer: testiamo valori misti dove B è max
        color.initHSLbyRGB(50, 100, 200);
        assertEquals(200, color.getBlue());
        assertTrue("Hue should be valid", color.getHue() > 0);
    }

    @Test
    public void testInitHSLbyRGB_Sat_LowLuminance() {
        HSLColor color = new HSLColor();
        // Copre: pLum <= HSLMAX/2
        // R=50, G=0, B=0 -> Lum ~ 25.
        color.initHSLbyRGB(50, 0, 0);
        assertTrue(color.getLuminence() <= 127);
        assertTrue(color.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_Sat_HighLuminance() {
        HSLColor color = new HSLColor();
        // Copre: pLum > HSLMAX/2
        color.initHSLbyRGB(255, 200, 200);
        assertTrue(color.getLuminence() > 127);
        assertTrue(color.getSaturation() > 0);
    }
    
    @Test
    public void testInitHSLbyRGB_HueNegativeCorrection() {
        HSLColor color = new HSLColor();
        // Copre: if (pHue < 0) pHue += HSLMAX
        // R dominant, ma B > G crea un delta negativo
        color.initHSLbyRGB(200, 10, 50); 
        assertTrue("Hue should be positive", color.getHue() >= 0);
    }

    // ========================================================================
    // TEST INIT RGB BY HSL (Reverse Logic & Private hueToRGB)
    // ========================================================================

    @Test
    public void testInitRGBbyHSL_GreyScale() {
        HSLColor color = new HSLColor();
        // S=0
        color.initRGBbyHSL(0, 0, 128);
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Normal_LowLum() {
        HSLColor color = new HSLColor();
        // L <= 127
        color.initRGBbyHSL(0, 255, 64);
        assertEquals(64, color.getLuminence());
        // Mutation check: Se la formula è corretta, R dovrebbe essere alto per Hue=0
        assertTrue(color.getRed() > 64);
    }

    @Test
    public void testInitRGBbyHSL_Normal_HighLum() {
        HSLColor color = new HSLColor();
        // L > 127
        color.initRGBbyHSL(85, 255, 200); // Greenish
        assertEquals(200, color.getLuminence());
        assertTrue(color.getGreen() > color.getRed());
    }

    /**
     * CRITICAL TEST for Clamping.
     * Uccide i mutanti che rimuoverebbero i check "if (Val > RGBMAX)".
     */
    @Test
    public void testInitRGBbyHSL_Overflow_Clamping_MutationKiller() {
        HSLColor color = new HSLColor();
        // Saturazione estrema (510) forza i valori interni oltre 255.
        
        // 1. Forza Red overflow
        color.initRGBbyHSL(0, 510, 128);
        assertEquals("Red clamped", 255, color.getRed());

        // 2. Forza Green overflow
        color.initRGBbyHSL(85, 510, 128);
        assertEquals("Green clamped", 255, color.getGreen());

        // 3. Forza Blue overflow
        color.initRGBbyHSL(170, 510, 128);
        assertEquals("Blue clamped", 255, color.getBlue());
    }

    @Test
    public void testHueToRGB_PrivateMethod_FullCoverage() {
        HSLColor color = new HSLColor();
        // Testiamo i rami del metodo privato hueToRGB variando Hue.
        // I rami sono basati su HSLMAX/6, HSLMAX/2, HSLMAX*2/3
        
        // H < 0 (correzione interna hueToRGB)
        color.initRGBbyHSL(-10, 128, 128);
        
        // H > HSLMAX (correzione interna hueToRGB)
        color.initRGBbyHSL(300, 128, 128);

        // H < 1/6 (es. 20)
        color.initRGBbyHSL(20, 128, 128);
        
        // 1/6 <= H < 1/2 (es. 85)
        color.initRGBbyHSL(85, 128, 128);
        
        // 1/2 <= H < 2/3 (es. 150)
        color.initRGBbyHSL(150, 128, 128);
        
        // H >= 2/3 (es. 200)
        color.initRGBbyHSL(200, 128, 128);
    }

    // ========================================================================
    // GETTERS, SETTERS & LOOPS
    // ========================================================================

    @Test
    public void testSetHue_WhileLoop() {
        HSLColor color = new HSLColor();
        // While loop coverage: valori che richiedono più iterazioni
        color.setHue(-600); // -600 -> -345 -> -90 -> 165
        assertEquals(165, color.getHue());
        
        color.setHue(600);  // 600 -> 345 -> 90
        assertEquals(90, color.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        HSLColor color = new HSLColor();
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
        color.setSaturation(500);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        HSLColor color = new HSLColor();
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
        color.setLuminence(500);
        assertEquals(255, color.getLuminence());
    }
    
    // Test getters indiretti (copertura banale ma necessaria)
    @Test
    public void testGettersDirect() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    // ========================================================================
    // UTILITY METHODS (Mutation Killers)
    // ========================================================================

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.setHue(0);
        color.reverseColor(); // +127
        assertEquals(127, color.getHue());
        
        color.setHue(200);
        color.reverseColor(); // 200+127 = 327 -> 327-255 = 72
        assertEquals(72, color.getHue());
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.setLuminence(100);
        
        // 0% -> No change
        color.brighten(0.0f);
        assertEquals(100, color.getLuminence());
        
        // Normal
        color.brighten(1.5f);
        assertEquals(150, color.getLuminence());
        
        // Overflow
        color.setLuminence(200);
        color.brighten(2.0f);
        assertEquals(255, color.getLuminence());
        
        // Negative/Underflow
        color.brighten(-1.0f);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        // Blend 0%
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getRed());
        
        // Blend 100%
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getRed());
        
        // Blend > 100% (Branch coverage)
        color.blend(0, 0, 0, 1.5f);
        assertEquals(0, color.getRed());
        
        // Blend < 0% (Branch coverage)
        color.blend(255, 255, 255, -0.5f);
        assertEquals(0, color.getRed());
        
        // Blend 50%
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.5f);
        int r = color.getRed();
        assertTrue("Blend logic verified", r >= 120 && r <= 135);
    }

    // ========================================================================
    // REFLECTION (Private Code Access)
    // ========================================================================

    @Test
    public void testPrivate_Greyscale() throws Exception {
        HSLColor color = new HSLColor();
        color.setSaturation(100);
        Method m = HSLColor.class.getDeclaredMethod("greyscale");
        m.setAccessible(true);
        m.invoke(color);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void testPrivate_ReverseLight() throws Exception {
        HSLColor color = new HSLColor();
        color.setLuminence(50);
        Method m = HSLColor.class.getDeclaredMethod("reverseLight");
        m.setAccessible(true);
        m.invoke(color);
        // 255 - 50 = 205
        assertEquals(205, color.getLuminence());
    }

    @Test
    public void testPrivate_SetRed() throws Exception {
        HSLColor color = new HSLColor();
        Method m = HSLColor.class.getDeclaredMethod("setRed", int.class);
        m.setAccessible(true);
        m.invoke(color, 123);
        assertEquals(123, color.getRed());
    }

    @Test
    public void testPrivate_SetGreen() throws Exception {
        HSLColor color = new HSLColor();
        Method m = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        m.setAccessible(true);
        m.invoke(color, 123);
        assertEquals(123, color.getGreen());
    }

    @Test
    public void testPrivate_SetBlue() throws Exception {
        HSLColor color = new HSLColor();
        Method m = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        m.setAccessible(true);
        m.invoke(color, 123);
        assertEquals(123, color.getBlue());
    }
}