/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Tuo Nome"
Cognome: "Tuo Cognome"
Username: m.berrino@studenti.unina.it (o il tuo username)
UserID: 223 (o il tuo ID)
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- Test initHSLbyRGB & Getter di base ---

    @Test
    public void testInitHSLbyRGB_Grayscale() {
        // R=G=B -> cMax == cMin -> Saturation = 0, Hue = UNDEFINED (170)
        hslColor.initHSLbyRGB(100, 100, 100);
        assertEquals(0, hslColor.getSaturation());
        assertEquals(170, hslColor.getHue());
        assertEquals(100, hslColor.getLuminence()); // (200*255 + 255) / 510 approx calculation
        // Verifica coerenza RGB
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void testInitHSLbyRGB_RedMax() {
        // Max is Red
        hslColor.initHSLbyRGB(255, 0, 0);
        assertEquals(0, hslColor.getHue()); // Hue dovrebbe essere 0 per il rosso puro
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence()); // (255*255+255)/(2*255) = 127
    }

    @Test
    public void testInitHSLbyRGB_GreenMax() {
        // Max is Green
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue calculation: (HSLMAX / 3) + ... = 85 + ...
        assertEquals(85, hslColor.getHue()); 
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax() {
        // Max is Blue
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue calculation: (2 * HSLMAX / 3) + ... = 170 + ...
        assertEquals(170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueWrap() {
        // Questo test mira a coprire: if (pHue < 0) pHue = pHue + HSLMAX;
        // Serve un caso dove Red è Max, ma B > G, rendendo (BDelta - GDelta) negativo o simile logicamente.
        // Rosso dominante, ma con Blu maggiore di Verde.
        hslColor.initHSLbyRGB(255, 0, 50);
        // Hue sarà calcolato e se negativo verrà wrappato.
        // Verifichiamo che sia nel range valido [0, 255]
        assertTrue(hslColor.getHue() >= 0 && hslColor.getHue() <= 255);
    }

    @Test
    public void testInitHSLbyRGB_LuminanceThreshold() {
        // Copertura del branch: if (pLum <= (HSLMAX / 2))
        // HSLMAX/2 = 127.
        
        // Caso Lum <= 127
        hslColor.initHSLbyRGB(50, 50, 50);
        assertTrue(hslColor.getLuminence() <= 127);

        // Caso Lum > 127
        hslColor.initHSLbyRGB(200, 200, 200);
        assertTrue(hslColor.getLuminence() > 127);
    }

    // --- Test initRGBbyHSL & hueToRGB (Metodo privato critico) ---

    @Test
    public void testInitRGBbyHSL_Grayscale() {
        // Saturation = 0
        hslColor.initRGBbyHSL(100, 0, 128);
        // Deve settare RGB basandosi sulla luminanza
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLuminance() {
        // L <= HSLMAX / 2 (127)
        // S != 0
        hslColor.initRGBbyHSL(0, 255, 64); // Rosso scuro saturo
        // Verifiche generiche che i valori siano stati calcolati
        assertTrue(hslColor.getRed() > 0);
    }

    @Test
    public void testInitRGBbyHSL_HighLuminance() {
        // L > HSLMAX / 2
        hslColor.initRGBbyHSL(0, 255, 200); // Rosso chiaro saturo
        assertTrue(hslColor.getRed() > 200);
    }

    // Questo set di test mira al metodo privato hueToRGB tramite initRGBbyHSL
    // hueToRGB ha 4 branch basati su Hue: < 1/6, < 1/2, < 2/3, else.
    // HSLMAX = 255.
    // 1/6 = 42.5
    // 1/2 = 127.5
    // 2/3 = 170
    
    @Test
    public void testHueToRGB_BranchCoverage() {
        int sat = 255;
        int lum = 127;
        
        // 1. Hue in first sixth (Hue < 42) -> Red component uses this often for pure red (Hue=0 or 255 wrap)
        hslColor.initRGBbyHSL(0, sat, lum); 
        assertEquals(255, hslColor.getRed());

        // 2. Hue in second region (42 < Hue < 127) -> Green logic often falls here
        hslColor.initRGBbyHSL(85, sat, lum); // Greenish
        assertEquals(255, hslColor.getGreen());

        // 3. Hue in third region (127 < Hue < 170) -> Blue logic fallback
        hslColor.initRGBbyHSL(170, sat, lum); // Blueish
        assertEquals(255, hslColor.getBlue());
        
        // 4. Hue wrapping (Hue < 0 or Hue > HSLMAX in hueToRGB parameters)
        // initRGBbyHSL chiama hueToRGB con (H + HSLMAX/3) e (H - HSLMAX/3).
        // Se H=0:
        // Red: chiama con 0 + 85 = 85
        // Blue: chiama con 0 - 85 = -85 -> hueToRGB corregge < 0 aggiungendo HSLMAX.
        hslColor.initRGBbyHSL(0, 255, 127);
        assertEquals(0, hslColor.getBlue()); // Verifica corretta gestione wrap negativo
    }
    
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // Test per forzare i valori RGB > 255 all'interno di initRGBbyHSL (i vari check if pRed > RGBMAX)
        // È difficile triggerare matematicamente > 255 se la matematica è perfetta, 
        // ma proviamo con valori limite massimi.
        hslColor.initRGBbyHSL(0, 255, 255); // Bianco puro
        assertEquals(255, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        assertEquals(255, hslColor.getBlue());
    }

    // --- Test Setters (Boundary & Mutation) ---

    @Test
    public void testSetHue() {
        hslColor.initHSLbyRGB(0, 0, 0);
        
        // Normal set
        hslColor.setHue(100);
        assertEquals(100, hslColor.getHue());

        // While loop < 0 (single iteration)
        hslColor.setHue(-50); 
        assertEquals(205, hslColor.getHue()); // -50 + 255

        // While loop < 0 (multiple iterations)
        hslColor.setHue(-300);
        // -300 + 255 = -45; -45 + 255 = 210
        assertEquals(210, hslColor.getHue());

        // While loop > HSLMAX (single iteration)
        hslColor.setHue(300);
        assertEquals(45, hslColor.getHue()); // 300 - 255
        
        // While loop > HSLMAX (multiple iterations)
        hslColor.setHue(600);
        // 600 - 255 = 345; 345 - 255 = 90
        assertEquals(90, hslColor.getHue());
    }

    @Test
    public void testSetSaturation() {
        hslColor.initHSLbyRGB(100, 100, 100);
        
        hslColor.setSaturation(50);
        assertEquals(50, hslColor.getSaturation());

        // Lower bound
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        // Upper bound
        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        hslColor.initHSLbyRGB(100, 100, 100);

        hslColor.setLuminence(50);
        assertEquals(50, hslColor.getLuminence());

        // Lower bound
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        // Upper bound
        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
    }

    // --- Test Metodi di Utilità (Reverse, Brighten, Blend) ---

    @Test
    public void testReverseColor() {
        // pHue + (HSLMAX / 2)
        hslColor.setHue(0);
        hslColor.reverseColor();
        assertEquals(127, hslColor.getHue()); // 0 + 127 = 127
        
        // Test wrap around via setHue logic implicit
        hslColor.setHue(200);
        hslColor.reverseColor();
        // 200 + 127 = 327 -> 327 - 255 = 72
        assertEquals(72, hslColor.getHue());
    }

    @Test
    public void testReverseLight() {
        // Nota: reverseLight è private! 
        // Non possiamo chiamarlo direttamente.
        // Tuttavia, analizzando il codice fornito, reverseLight NON viene chiamato da nessun metodo pubblico.
        // Se la classe è esattamente quella fornita, questo è "Dead Code" e non può essere testato direttamente
        // senza Reflection o modifiche al codice.
        // Dato che il compito chiede test unitari black/grey box sulla classe fornita, 
        // e solitamente in questi esercizi si ignora il codice morto privato o si usa reflection.
        // SE il metodo fosse stato pubblico o chiamato internamente, avremmo testato così:
        /*
        hslColor.setLuminence(100);
        // hslColor.reverseLight(); // Non compilerebbe
        // expected: 255 - 100 = 155
        */
    }

    @Test
    public void testBrighten() {
        hslColor.initHSLbyRGB(100, 100, 100); // Lum approx 100
        
        // 0 percent (Return early)
        int oldLum = hslColor.getLuminence();
        hslColor.brighten(0f);
        assertEquals(oldLum, hslColor.getLuminence());

        // Normal brighten
        hslColor.setLuminence(100);
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());

        // Overflow
        hslColor.setLuminence(200);
        hslColor.brighten(2.0f); // 400 -> clamped to 255
        assertEquals(255, hslColor.getLuminence());

        // Underflow (percent negative?) -> Math: Lum * negative
        hslColor.setLuminence(100);
        hslColor.brighten(-0.5f); // -50 -> clamped to 0
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        hslColor.initHSLbyRGB(0, 0, 0); // Black base

        // fPercent >= 1 -> initHSLbyRGB(R, G, B) directly
        hslColor.blend(255, 255, 255, 1.0f);
        assertEquals(255, hslColor.getLuminence()); // Became white

        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 1.5f); // > 1
        assertEquals(255, hslColor.getLuminence());

        // fPercent <= 0 -> return (no change)
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 0.0f);
        assertEquals(0, hslColor.getLuminence()); // Stays black

        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, -0.5f);
        assertEquals(0, hslColor.getLuminence()); // Stays black

        // Blending 50%
        // Base: 0, Target: 200. 50% -> 100
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(200, 200, 200, 0.5f);
        assertEquals(100, hslColor.getRed());
    }
    
    // Test "indiretto" per i setter privati setRed, setGreen, setBlue
    // Questi metodi sono chiamati in initRGBbyHSL. Ma nel codice fornito
    // setRed, setGreen, setBlue NON vengono usati in initRGBbyHSL (che assegna direttamente a pRed/pGreen/pBlue)
    // E non vengono usati in initHSLbyRGB.
    // Vedendo il codice:
    // private void setRed(int iNewValue) { initHSLbyRGB(iNewValue, pGreen, pBlue); }
    // Questi metodi privati sembrano non utilizzati internamente nel codice fornito, ma sono accessor.
    // Se fossero pubblici li testeremmo. Essendo privati e non chiamati, è codice morto per i test esterni,
    // a meno che non usiamo Reflection. Dato lo scope dell'esercizio, ci concentriamo sulla copertura raggiungibile.
}