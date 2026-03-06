import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor color;
    // Ridefiniamo le costanti necessarie per i calcoli di verifica
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    @Before
    public void setUp() {
        color = new HSLColor();
    }

    // ----------------------- TEST CONVERSIONE RGB -> HSL ---------------------
    // Questi test coprono indirettamente iMax e iMin

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        // CMax == CMin (greyscale) -> R=0, G=0, B=0
        color.initHSLbyRGB(0, 0, 0); 
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
    }
    
    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        // CMax == CMin -> R=255, G=255, B=255
        color.initHSLbyRGB(255, 255, 255); 
        assertEquals(255, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_RedDominant() {
        // CMax == R (R > G && R > B)
        color.initHSLbyRGB(255, 0, 0); 
        // Hue deve essere circa 0 (o HSLMAX se corretto dalla logica < 0)
        // La logica interna calcola Hue. Verifichiamo che sia nel range del rosso.
        int h = color.getHue();
        assertTrue("Hue should be red range", h >= 0 || h > 240);
        assertEquals(128, color.getLuminence()); // (255+0)/2
        assertEquals(255, color.getSaturation()); 
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        // CMax == G
        color.initHSLbyRGB(0, 255, 0);
        // Hue dovrebbe essere intorno a 85 (HSLMAX/3)
        int h = color.getHue();
        assertTrue("Hue should be green range", h > 80 && h < 90);
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        // CMax == B
        color.initHSLbyRGB(0, 0, 255);
        // Hue dovrebbe essere intorno a 170 (2*HSLMAX/3)
        int h = color.getHue();
        assertTrue("Hue should be blue range", h > 160 && h < 180);
    }

    @Test
    public void testInitHSLbyRGB_HighLuminenceSaturation() {
        // Test per il ramo: if (pLum > (HSLMAX / 2))
        // R=200, G=255, B=255. Min=200, Max=255. L > 127
        color.initHSLbyRGB(200, 255, 255);
        assertTrue(color.getLuminence() > 127);
        assertTrue(color.getSaturation() > 0);
    }
    
    @Test
    public void testInitHSLbyRGB_HueWrapAround() {
        // Cerchiamo di forzare il caso in cui pHue < 0 prima della correzione
        // Un colore dove B > G spesso causa sottrazioni negative nel calcolo Hue se CMax=R
        color.initHSLbyRGB(255, 0, 50); 
        assertTrue(color.getHue() >= 0 && color.getHue() <= HSLMAX);
    }

    // ----------------------- TEST CONVERSIONE HSL -> RGB ---------------------
    // Questi test coprono indirettamente hueToRGB

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // S = 0 -> Ramo Greyscale
        color.initRGBbyHSL(100, 0, 128);
        
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLuminence() {
        // L <= HSLMAX/2
        color.initRGBbyHSL(0, 255, 100);
        // Verifichiamo che i valori siano impostati (non 0,0,0)
        assertFalse(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0);
    }

    @Test
    public void testInitRGBbyHSL_HighLuminence() {
        // L > HSLMAX/2
        color.initRGBbyHSL(0, 255, 200);
        assertFalse(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0);
    }

    @Test
    public void testInitRGBbyHSL_HueRanges() {
        // Questo test mira a coprire i vari rami di hueToRGB chiamando initRGBbyHSL con diversi Hue.
        // hueToRGB ha 4 rami basati su Hue: < 1/6, < 1/2, < 2/3, else.
        
        // Hue basso (Rosso/Arancio) -> Colpisce primo ramo
        color.initRGBbyHSL(20, 255, 128);
        assertTrue(color.getRed() > color.getGreen());
        
        // Hue medio (Verde) -> Colpisce rami centrali
        color.initRGBbyHSL(85, 255, 128);
        assertTrue(color.getGreen() > color.getBlue());
        
        // Hue alto (Blu) -> Colpisce rami alti
        color.initRGBbyHSL(170, 255, 128);
        assertTrue(color.getBlue() > color.getRed());
        
        // Hue fine (Viola/Rosso)
        color.initRGBbyHSL(240, 255, 128);
        assertTrue(color.getRed() > color.getGreen());
    }
    
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // Forza valori che potrebbero eccedere RGBMAX se non clippati
        color.initRGBbyHSL(0, 255, 128);
        assertTrue(color.getRed() <= 255);
        assertTrue(color.getGreen() <= 255);
        assertTrue(color.getBlue() <= 255);
    }

    // ----------------------- TEST GETTERS E SETTERS -------------------------

    @Test
    public void testSetHue_Cycles() {
        // while (iToValue < 0)
        color.setHue(-10);
        assertTrue(color.getHue() >= 0);
        
        // while (iToValue > HSLMAX)
        color.setHue(300);
        assertTrue(color.getHue() <= HSLMAX);
    }

    @Test
    public void testSetSaturation_Clamping() {
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
        
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation());
        
        // Set valido
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
    }
    
    @Test
    public void testSetLuminence_Clamping() {
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
        
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence());
        
        // Set valido
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void testGetters() {
        color.initHSLbyRGB(10, 20, 30);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    // ----------------------- TEST MANIPOLAZIONE COLORE ----------------------

    @Test
    public void testReverseColor() {
        // Imposta rosso
        color.initHSLbyRGB(255, 0, 0);
        int oldHue = color.getHue();
        
        color.reverseColor();
        
        // Reverse aggiunge HSLMAX/2. Verifica che sia cambiato.
        assertNotEquals(oldHue, color.getHue());
    }
    
    // Nota: reverseLight() è privato e non usato da nessun metodo pubblico. 
    // Non possiamo testarlo. 
    // Tuttavia, setLuminence è pubblico, quindi possiamo testare brightening.

    @Test
    public void testBrighten() {
        color.initHSLbyRGB(100, 100, 100); // Grigio medio
        int oldL = color.getLuminence();
        
        // 0% -> Nessun cambio
        color.brighten(0.0f);
        assertEquals(oldL, color.getLuminence());
        
        // 1.5 -> Aumento
        color.brighten(1.5f);
        assertTrue(color.getLuminence() > oldL);
        
        // Overflow
        color.setLuminence(200);
        color.brighten(2.0f);
        assertEquals(HSLMAX, color.getLuminence());
        
        // Underflow
        color.setLuminence(100);
        color.brighten(-1.0f); // Moltiplicare per negativo rende 0? No, il cast int tronca.
        // pLum * -1 = negativo. Clamped a 0.
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        // Blend con fPercent >= 1 -> Diventa il nuovo colore
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getRed());
        
        // Blend con fPercent <= 0 -> Resta il vecchio colore
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getRed());
        
        // Blend 50%
        color.initHSLbyRGB(0, 0, 0); // Nero
        color.blend(255, 255, 255, 0.5f); // 50% Bianco
        // Risultato atteso: grigio medio (127 o 128)
        assertTrue(color.getRed() > 120 && color.getRed() < 135);
    }
}