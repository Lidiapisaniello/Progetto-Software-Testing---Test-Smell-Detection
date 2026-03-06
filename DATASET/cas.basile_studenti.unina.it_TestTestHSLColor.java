/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Castrese
Cognome: Basile
Username: cas.basile@studenti.unina.it
UserID: 466
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

    @BeforeClass
    public static void setUpClass() {
        // Setup statico non necessario
    }
                
    @AfterClass
    public static void tearDownClass() {
        // Teardown statico non necessario
    }
                
    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }
                
    @After
    public void tearDown() {
        hslColor = null;
    }

    // ==========================================
    // TEST: initHSLbyRGB (RGB -> HSL)
    // ==========================================

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // Caso: Rosso predominante (cMax == R)
        hslColor.initHSLbyRGB(255, 0, 0);
        assertEquals("Red should be 255", 255, hslColor.getRed());
        // Hue region 0 (Red)
        assertEquals(0, hslColor.getHue(), 2);
        assertTrue(hslColor.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // Caso: Verde predominante (cMax == G)
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue region ~ 85
        assertEquals(255, hslColor.getGreen());
        assertEquals(85, hslColor.getHue(), 2); 
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // Caso: Blu predominante (cMax == B)
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue region ~ 170
        assertEquals(255, hslColor.getBlue());
        assertEquals(170, hslColor.getHue(), 2);
    }

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Caso: R=G=B (cMax == cMin) -> Saturation 0
        hslColor.initHSLbyRGB(100, 100, 100);
        assertEquals(0, hslColor.getSaturation());
        assertEquals(170, hslColor.getHue()); // UNDEFINED = 170
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_LowLuminance() {
        // Branch: pLum <= (HSLMAX / 2)
        hslColor.initHSLbyRGB(50, 20, 20); 
        assertTrue(hslColor.getLuminence() <= 127);
    }

    @Test
    public void testInitHSLbyRGB_HighLuminance() {
        // Branch: pLum > (HSLMAX / 2)
        hslColor.initHSLbyRGB(240, 240, 200);
        assertTrue(hslColor.getLuminence() > 127);
    }

    @Test
    public void testInitHSLbyRGB_Calculations() {
        // Test generico per verificare consistenza calcoli
        hslColor.initHSLbyRGB(100, 150, 200);
        int h = hslColor.getHue();
        int s = hslColor.getSaturation();
        int l = hslColor.getLuminence();
        
        // Ricostruiamo RGB da HSL e vediamo se torniamo vicini
        HSLColor check = new HSLColor();
        check.initRGBbyHSL(h, s, l);
        
        assertEquals(hslColor.getRed(), check.getRed(), 5); // Tolleranza per arrotondamenti
        assertEquals(hslColor.getGreen(), check.getGreen(), 5);
        assertEquals(hslColor.getBlue(), check.getBlue(), 5);
    }

    // ==========================================
    // TEST: initRGBbyHSL (HSL -> RGB)
    // ==========================================

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Saturation = 0
        hslColor.initRGBbyHSL(0, 0, 100);
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Normal_LowLum() {
        // L <= HSLMAX/2
        hslColor.initRGBbyHSL(85, 200, 100);
        assertEquals(100, hslColor.getLuminence());
        assertTrue(hslColor.getGreen() > 0); 
    }

    @Test
    public void testInitRGBbyHSL_Normal_HighLum() {
        // L > HSLMAX/2
        hslColor.initRGBbyHSL(85, 200, 200);
        assertEquals(200, hslColor.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_ClampingLogic() {
        // Questo test cerca di forzare valori alti per vedere se la logica interna (hueToRGB) funziona
        hslColor.initRGBbyHSL(0, 255, 128); 
        assertTrue(hslColor.getRed() <= 255);
        assertTrue(hslColor.getGreen() <= 255);
        assertTrue(hslColor.getBlue() <= 255);
    }

    // ==========================================
    // TEST: hueToRGB (Indirect Testing via initRGBbyHSL)
    // ==========================================

    @Test
    public void testHueToRGB_Sector1() {
        // Copre il ramo: if (Hue < (HSLMAX / 6))
        hslColor.initRGBbyHSL(20, 255, 128);
        assertTrue(hslColor.getRed() > 0);
    }

    @Test
    public void testHueToRGB_Sector2() {
        // Copre il ramo: if (Hue < (HSLMAX / 2))
        hslColor.initRGBbyHSL(100, 255, 128);
        assertTrue(hslColor.getGreen() > 0);
    }

    @Test
    public void testHueToRGB_Sector3() {
        // Copre il ramo: if (Hue < (HSLMAX * 2 / 3))
        hslColor.initRGBbyHSL(150, 255, 128);
        assertTrue(hslColor.getBlue() > 0);
    }
    
    @Test
    public void testHueToRGB_Sector4() {
        // Copre il ramo else finale
        hslColor.initRGBbyHSL(200, 255, 128);
        assertTrue(hslColor.getBlue() > 0);
    }

    // ==========================================
    // TEST: Getters, Setters & Bounds
    // ==========================================

    @Test
    public void testSetHue_WrapAround() {
        hslColor.setHue(300); // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
        
        hslColor.setHue(-10); // 255 - 10 = 245
        assertEquals(245, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
        
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(-50);
        assertEquals(0, hslColor.getLuminence());
    }

    // ==========================================
    // TEST: Public Utility Methods
    // ==========================================

    @Test
    public void testReverseColor() {
        hslColor.setHue(100);
        hslColor.reverseColor();
        // reverseColor aggiunge HSLMAX/2
        // 100 + 127 = 227
        assertTrue(hslColor.getHue() != 100);
        assertEquals(227, hslColor.getHue(), 1);
    }

    @Test
    public void testBrighten() {
        hslColor.setLuminence(100);
        
        // Percent 0 -> No change
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
        
        // Normal increase
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());
        
        // Overflow check
        hslColor.brighten(5.0f);
        assertEquals(255, hslColor.getLuminence());
        
        // Negative/Underflow check
        hslColor.brighten(-1.0f); 
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        // fPercent >= 1 -> Full Replacement
        hslColor.blend(255, 255, 255, 1.5f);
        assertEquals(255, hslColor.getRed());
        
        // fPercent <= 0 -> No Change
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, -0.5f);
        assertEquals(0, hslColor.getRed());
        
        // 0.5 Mix
        hslColor.initHSLbyRGB(0, 0, 0); // Start Black
        hslColor.blend(100, 100, 100, 0.5f); // Blend with Grey
        assertEquals(50, hslColor.getRed()); // Expect Dark Grey
    }
}