/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Simone
Cognome: De Crescenzo
Username: sim.decrescenzo@studenti.unina.it
UserID: 354
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

public class TestHSLColor {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // ---------------------------------------------------------
    // TEST: initHSLbyRGB (Da RGB a HSL)
    // ---------------------------------------------------------

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // R=G=B -> Deve risultare in Greyscale (Sat=0, Hue=UNDEFINED)
        hslColor.initHSLbyRGB(100, 100, 100);
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hslColor.getHue());
        // Lum calcolato: (200*255 + 255) / (2*255) approx 100
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_RedDominant() {
        // cMax == R
        hslColor.initHSLbyRGB(255, 0, 0);
        // Hue per rosso puro è solitamente 0 (o vicino a 0/255 a seconda della scala)
        // Nel codice: Hue = BDelta - GDelta. 
        assertEquals("Hue should be 0/close to 0 for pure red", 0, hslColor.getHue());
        assertEquals("Sat should be max for pure color", 255, hslColor.getSaturation());
        assertEquals("Lum should be mid for pure color", 127, hslColor.getLuminence()); // (255*255+255)/510 = 127.5 -> 127
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        // cMax == G
        hslColor.initHSLbyRGB(0, 255, 0);
        // Verifica che entri nel ramo cMax == G
        // Hue atteso intorno a 85 (1/3 di 255)
        int expectedHue = HSLMAX / 3; 
        assertEquals(expectedHue, hslColor.getHue(), 1);
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        // cMax == B
        hslColor.initHSLbyRGB(0, 0, 255);
        // Verifica che entri nel ramo cMax == B
        // Hue atteso intorno a 170 (2/3 di 255)
        int expectedHue = (2 * HSLMAX) / 3;
        assertEquals(expectedHue, hslColor.getHue(), 1);
    }

    @Test
    public void testInitHSLbyRGB_LuminenceBranches() {
        // Test ramo pLum <= HSLMAX/2 (Scuro)
        hslColor.initHSLbyRGB(50, 0, 0); 
        assertTrue(hslColor.getLuminence() <= 127);

        // Test ramo pLum > HSLMAX/2 (Chiaro)
        hslColor.initHSLbyRGB(255, 200, 200);
        assertTrue(hslColor.getLuminence() > 127);
    }
    
    @Test
    public void testInitHSLbyRGB_NegativeHueWrap() {
        // Serve un caso in cui (cMax == R) e (BDelta - GDelta) < 0
        // Questo accade quando il colore tende verso il magenta/viola (Alto R, Alto B, Basso G)
        // ma R è max.
        hslColor.initHSLbyRGB(255, 0, 100);
        // Qui Hue verrebbe calcolato negativo e poi corretto (pHue + HSLMAX)
        assertTrue("Hue should be positive after wrap", hslColor.getHue() >= 0);
    }

    // ---------------------------------------------------------
    // TEST: initRGBbyHSL (Da HSL a RGB)
    // ---------------------------------------------------------

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Saturation = 0
        hslColor.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LuminenceHighLow() {
        // Copre il ramo L <= HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 50);
        int rLow = hslColor.getRed();
        
        // Copre il ramo L > HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 200);
        int rHigh = hslColor.getRed();
        
        assertNotEquals(rLow, rHigh);
    }

    @Test
    public void testInitRGBbyHSL_HueSectors() {
        // Il metodo hueToRGB ha 4 rami principali basati sul valore di Hue.
        // Testiamo diversi Hue per entrare in ogni 'if' di quel metodo.
        
        int sat = 255;
        int lum = 128;

        // 1. Hue < HSLMAX / 6 (es. 20) -> Rosso/Arancio
        hslColor.initRGBbyHSL(20, sat, lum);
        assertTrue(hslColor.getRed() > 0);

        // 2. Hue < HSLMAX / 2 (ma > 1/6) -> Verde (es. 85)
        hslColor.initRGBbyHSL(85, sat, lum);
        assertTrue(hslColor.getGreen() > 0);

        // 3. Hue < HSLMAX * 2/3 (es. 150) -> Blu/Ciano
        hslColor.initRGBbyHSL(150, sat, lum);
        assertTrue(hslColor.getBlue() > 0);

        // 4. Else (es. 220) -> Viola/Magenta
        hslColor.initRGBbyHSL(220, sat, lum);
        assertTrue(hslColor.getRed() > 0 && hslColor.getBlue() > 0);
    }
    
    @Test
    public void testInitRGBbyHSL_HueBounds() {
        // hueToRGB gestisce Hue < 0 e Hue > HSLMAX internamente
        // Chiamiamo initRGBbyHSL passando un Hue che nel calcolo interno (H + 1/3) sborda
        hslColor.initRGBbyHSL(250, 255, 128);
        // Nessuna eccezione, valori validi
        assertTrue(hslColor.getRed() >= 0 && hslColor.getRed() <= RGBMAX);
    }

    // ---------------------------------------------------------
    // TEST: Getters & Setters (Logic & Clamping)
    // ---------------------------------------------------------

    @Test
    public void testSetHue_Logic() {
        // Test wrapping positivo
        hslColor.setHue(300); // 300 > 255 -> 300 - 255 = 45
        assertEquals(45, hslColor.getHue());

        // Test wrapping negativo
        hslColor.setHue(-50); // -50 < 0 -> -50 + 255 = 205
        assertEquals(205, hslColor.getHue());
        
        // Loop check (valori molto grandi)
        hslColor.setHue(600); // 600 - 255 - 255 = 90
        assertEquals(90, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
        
        hslColor.setSaturation(100);
        assertEquals(100, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(300);
        assertEquals(HSLMAX, hslColor.getLuminence());
        
        hslColor.setLuminence(100);
        assertEquals(100, hslColor.getLuminence());
    }
    
    @Test
    public void testGettersSimple() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }

    // ---------------------------------------------------------
    // TEST: Utility Methods
    // ---------------------------------------------------------

    @Test
    public void testReverseColor() {
        hslColor.setHue(10);
        hslColor.reverseColor();
        // 10 + 127 (HSLMAX/2) = 137
        assertEquals(137, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.setLuminence(100);
        
        // Caso 0%
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
        
        // Caso normale
        hslColor.brighten(1.5f); // 150
        assertEquals(150, hslColor.getLuminence());
        
        // Caso overflow
        hslColor.brighten(10.0f); 
        assertEquals(HSLMAX, hslColor.getLuminence());
        
        // Caso underflow (se pLum fosse negativo, impossibile dai setter, ma logica presente)
        // Testiamo il clamping a 0
        hslColor.setLuminence(10);
        hslColor.brighten(-5.0f); // Diventa negativo
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); // Nero
        
        // fPercent >= 1 -> Diventa il colore target
        hslColor.blend(255, 255, 255, 1.5f);
        assertEquals(255, hslColor.getLuminence());
        
        // fPercent <= 0 -> Rimane invariato
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, -0.5f);
        assertEquals(0, hslColor.getLuminence());
        
        // Blending 50% tra nero (0) e bianco (255)
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 0.5f);
        // RGB risultante dovrebbe essere ca. 127,127,127
        assertTrue(hslColor.getRed() > 120 && hslColor.getRed() < 135);
    }

    // ---------------------------------------------------------
    // TEST: Private Methods (Reflection)
    // Questi metodi sono privati e non usati dalla classe stessa,
    // ma per "massimizzare la copertura" come richiesto, li invochiamo via reflection.
    // ---------------------------------------------------------

    @Test
    public void testPrivateMethodsViaReflection() throws Exception {
        // 1. Test greyscale()
        hslColor.initHSLbyRGB(255, 0, 0); // Rosso
        Method mGreyscale = HSLColor.class.getDeclaredMethod("greyscale");
        mGreyscale.setAccessible(true);
        mGreyscale.invoke(hslColor);
        
        // Dopo greyscale, R=G=B
        assertEquals(hslColor.getRed(), hslColor.getGreen());
        assertEquals(hslColor.getGreen(), hslColor.getBlue());

        // 2. Test reverseLight()
        hslColor.setLuminence(100);
        Method mReverseLight = HSLColor.class.getDeclaredMethod("reverseLight");
        mReverseLight.setAccessible(true);
        mReverseLight.invoke(hslColor);
        assertEquals(HSLMAX - 100, hslColor.getLuminence());

        // 3. Test setRed, setGreen, setBlue (Privati)
        Method mSetRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
        mSetRed.setAccessible(true);
        mSetRed.invoke(hslColor, 200);
        assertEquals(200, hslColor.getRed());

        Method mSetGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        mSetGreen.setAccessible(true);
        mSetGreen.invoke(hslColor, 150);
        assertEquals(150, hslColor.getGreen());

        Method mSetBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        mSetBlue.setAccessible(true);
        mSetBlue.invoke(hslColor, 50);
        assertEquals(50, hslColor.getBlue());
    }
}