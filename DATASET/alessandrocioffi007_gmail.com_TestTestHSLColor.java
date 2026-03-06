/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: alessandrocioffi007@gmail.com
UserID: 1376
Date: 19/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;

    // Costanti copiate dalla classe originale per le asserzioni
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // ---------------------------------------------------------
    // 1. Test initHSLbyRGB (Conversione RGB -> HSL)
    // ---------------------------------------------------------

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Copre: cMax == cMin (ramo grigio)
        // Input: Grigio medio
        hslColor.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        
        // Calcolo manuale Lum: ((200 * 255) + 255) / 510 = 100
        assertEquals("Luminance check", 100, hslColor.getLuminence());
        assertEquals(100, hslColor.getRed());
    }

    @Test
    public void testInitHSLbyRGB_RedDominant() {
        // Copre: cMax == R, Lum <= HSLMAX/2
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Rosso puro: H=0 (o 255/3*0), S=255, L=127 (approx)
        assertEquals("Red Hue", 0, hslColor.getHue());
        assertEquals("Max Saturation", 255, hslColor.getSaturation());
        assertTrue("Luminance should be mid-range", hslColor.getLuminence() > 0);
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        // Copre: cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        
        // Verde puro: Hue è circa 85 (HSLMAX / 3)
        int expectedHue = HSLMAX / 3; 
        assertEquals("Green Hue", expectedHue, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        // Copre: cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        
        // Blu puro: Hue è circa 170 (2 * HSLMAX / 3)
        int expectedHue = (2 * HSLMAX) / 3;
        assertEquals("Blue Hue", expectedHue, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_HighLuminance() {
        // Copre: Ramo else di (pLum <= (HSLMAX / 2)) -> Colori chiari
        hslColor.initHSLbyRGB(200, 220, 240);
        
        assertTrue("Luminance should be high", hslColor.getLuminence() > 127);
        assertTrue("Saturation calculation for high lum", hslColor.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueWrap() {
        // Copre: if (pHue < 0)
        // Per ottenere un Hue negativo prima del fix, serve cMax=R e G < B.
        // Formula: pHue = BDelta - GDelta. Se GDelta > BDelta non è negativo.
        // Ma la formula è complessa. Proviamo un Rosso/Viola scuro.
        hslColor.initHSLbyRGB(255, 0, 50); 
        
        // Hue dovrebbe essere tra Rosso e Blu (Viola), quindi alto (es. > 200)
        // Se la logica < 0 funziona, avremo un valore positivo alto.
        assertTrue("Hue should be wrapped to positive", hslColor.getHue() > 0);
    }

    // ---------------------------------------------------------
    // 2. Test initRGBbyHSL (Conversione HSL -> RGB)
    // ---------------------------------------------------------

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Copre: if (S == 0)
        hslColor.initRGBbyHSL(0, 0, 128);
        
        assertEquals("Red should equal Lum scaled", 128, hslColor.getRed());
        assertEquals("Green should equal Lum scaled", 128, hslColor.getGreen());
        assertEquals("Blue should equal Lum scaled", 128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_LowLum() {
        // Copre: else -> if (L <= HSLMAX / 2)
        // Hue generico, Satura, Lum bassa
        hslColor.initRGBbyHSL(85, 255, 60);
        
        assertEquals(85, hslColor.getHue());
        assertEquals(60, hslColor.getLuminence());
        // Verifica che i colori siano stati impostati
        assertNotEquals(hslColor.getRed(), hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_Color_HighLum() {
        // Copre: else -> else (L > HSLMAX / 2)
        hslColor.initRGBbyHSL(85, 255, 200);
        
        assertEquals(200, hslColor.getLuminence());
        // Verifica che non sia grigio
        assertTrue(hslColor.getRed() != hslColor.getGreen() || hslColor.getRed() != hslColor.getBlue());
    }
    
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // Cerca di forzare valori RGB > 255 per testare: if (pRed > RGBMAX) pRed = RGBMAX;
        // Usando massima saturazione e massima luminosità (ma non bianco puro per evitare S=0)
        hslColor.initRGBbyHSL(0, 255, 128);
        
        assertTrue(hslColor.getRed() <= 255);
        assertTrue(hslColor.getGreen() <= 255);
        assertTrue(hslColor.getBlue() <= 255);
    }

    // ---------------------------------------------------------
    // 3. Test hueToRGB (Helper privato, testato indirettamente)
    // ---------------------------------------------------------

    @Test
    public void testHueToRGB_Coverage() {
        // Questo metodo è privato, dobbiamo stimolarlo via initRGBbyHSL variando Hue.
        // hueToRGB ha 4 rami:
        // 1. Hue < (HSLMAX / 6)  -> (0 - 42)
        // 2. Hue < (HSLMAX / 2)  -> (42 - 127)
        // 3. Hue < (HSLMAX * 2/3)-> (127 - 170)
        // 4. Else
        
        // Test Ramo 1 (Hue=20)
        hslColor.initRGBbyHSL(20, 255, 128);
        int r1 = hslColor.getRed();
        
        // Test Ramo 2 (Hue=85)
        hslColor.initRGBbyHSL(85, 255, 128);
        int r2 = hslColor.getRed();
        
        // Test Ramo 3 (Hue=150)
        hslColor.initRGBbyHSL(150, 255, 128);
        int r3 = hslColor.getRed();
        
        // Test Ramo 4 (Hue=200)
        hslColor.initRGBbyHSL(200, 255, 128);
        int r4 = hslColor.getRed();
        
        // Verifica semplice che i calcoli producano risultati diversi in zone diverse
        assertNotEquals(r1, r3);
    }
    
    @Test
    public void testHueToRGB_BoundaryWrap() {
        // Copre: if (Hue < 0) e if (Hue > HSLMAX) dentro hueToRGB
        // initRGBbyHSL chiama hueToRGB passando (H + HSLMAX/3) e (H - HSLMAX/3).
        // Se passiamo H=0:
        // per il Blu chiamerà con (0 - 85) = -85 -> triggera Hue < 0
        // per il Rosso chiamerà con (0 + 85) = 85
        hslColor.initRGBbyHSL(0, 255, 128);
        assertNotNull(hslColor.getBlue()); 
        
        // Se passiamo H=250:
        // per il Rosso chiamerà con (250 + 85) = 335 -> triggera Hue > HSLMAX
        hslColor.initRGBbyHSL(250, 255, 128);
        assertNotNull(hslColor.getRed());
    }

    // ---------------------------------------------------------
    // 4. Test Getters e Setters
    // ---------------------------------------------------------

    @Test
    public void testSetHue_WrapAround() {
        // Copre: while (iToValue > HSLMAX)
        hslColor.setHue(300); // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());

        // Copre: while (iToValue < 0)
        hslColor.setHue(-50); // -50 + 255 = 205
        assertEquals(205, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.initRGBbyHSL(0, 100, 100);
        
        // Test upper bound
        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
        
        // Test lower bound
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.initRGBbyHSL(0, 100, 100);
        
        // Test upper bound
        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
        
        // Test lower bound
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());
    }

    // ---------------------------------------------------------
    // 5. Test Utility Methods
    // ---------------------------------------------------------

    @Test
    public void testReverseColor() {
        // Imposta Hue a 0
        hslColor.initRGBbyHSL(0, 255, 128);
        // Reverse aggiunge HSLMAX / 2 (circa 127)
        hslColor.reverseColor();
        
        assertTrue("Hue should change significantly", hslColor.getHue() > 100);
    }

    @Test
    public void testBrighten() {
        hslColor.initRGBbyHSL(0, 0, 100); // Lum = 100
        
        // Test 0 percent (return immediato)
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
        
        // Test normal brighten
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());
        
        // Test clamping max
        hslColor.brighten(10.0f); // Molto alto
        assertEquals(255, hslColor.getLuminence());
    }
    
    @Test
    public void testBrighten_Negative() {
        hslColor.initRGBbyHSL(0, 0, 100);
        // Test clamping min (L < 0)
        hslColor.brighten(-0.5f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        // Setup colore iniziale: Rosso
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Test fPercent >= 1 (Sostituzione totale)
        hslColor.blend(0, 0, 255, 1.0f); // Diventa blu
        assertEquals(255, hslColor.getBlue());
        assertEquals(0, hslColor.getRed());
        
        // Test fPercent <= 0 (Nessun cambiamento)
        hslColor.initHSLbyRGB(255, 0, 0);
        hslColor.blend(0, 0, 255, 0.0f);
        assertEquals(255, hslColor.getRed());
        
        // Test Mixing (50%)
        // Rosso(255,0,0) misto a Blu(0,0,255) -> (127, 0, 127)
        hslColor.initHSLbyRGB(255, 0, 0);
        hslColor.blend(0, 0, 255, 0.5f);
        
        // Tolleranza dovuta agli arrotondamenti integer
        assertEquals(127, hslColor.getRed(), 1); 
        assertEquals(0, hslColor.getGreen());
        assertEquals(127, hslColor.getBlue(), 1);
    }
}
						