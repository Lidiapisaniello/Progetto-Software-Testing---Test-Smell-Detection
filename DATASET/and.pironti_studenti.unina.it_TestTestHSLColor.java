/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Andrea"
Cognome: "Pironti"
Username: and.pironti@studenti.unina.it
UserID: 653
Date: 27/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HSLColorTest {

    private HSLColor color;
    private final static int HSLMAX = 255;
    private final static int UNDEFINED = 170;

    /**
     * Metodo @Before: viene eseguito prima di *ogni* metodo @Test.
     * Garantisce che ogni test inizi con un oggetto 'color' pulito e nuovo.
     */
    @Before
    public void setUp() {
        color = new HSLColor();
    }

    // --- Test per initHSLbyRGB (Conversione da RGB a HSL) ---
	//HSL sta per Hue (Tonalità o Tinta),Saturation (Saturazione),Lightness (Luminosità)
  
    /**
     * Testa il caso "greyscale" (R=G=B) con il colore nero.
     * Copre il branch: if (cMax == cMin)
     * Si aspetta Saturation = 0 e Hue = UNDEFINED.
     */
  
    @Test
    public void testRGBtoHSL_GreyscaleBlack() {
        color.initHSLbyRGB(0, 0, 0); 
        assertEquals("Black Hue", UNDEFINED, color.getHue());
        assertEquals("Black Sat", 0, color.getSaturation());
        assertEquals("Black Lum", 0, color.getLuminence());
    }

    /**
     * Testa il caso "greyscale" (R=G=B) con il colore bianco.
     * Copre il branch: if (cMax == cMin)
     * Si aspetta Saturation = 0 e Hue = UNDEFINED.
     */
    @Test
    public void testRGBtoHSL_GreyscaleWhite() {
        color.initHSLbyRGB(255, 255, 255); 
        assertEquals("White Hue", UNDEFINED, color.getHue());
        assertEquals("White Sat", 0, color.getSaturation());
        assertEquals("White Lum", 255, color.getLuminence());
    }
    
    /**
     * Testa il caso "greyscale" (R=G=B) con un grigio medio.
     * Copre il branch: if (cMax == cMin)
     */
    @Test
    public void testRGBtoHSL_GreyscaleMedium() {
        color.initHSLbyRGB(128, 128, 128); 
        assertEquals("Grey Hue", UNDEFINED, color.getHue());
        assertEquals("Grey Sat", 0, color.getSaturation());
        assertEquals("Grey Lum", 128, color.getLuminence());
    }

    /**
     * Testa un colore primario (Rosso).
     * Copre il branch: if (cMax == R) nel calcolo dell'Hue.
     */
    @Test
    public void testRGBtoHSL_PrimaryRed() {
        color.initHSLbyRGB(255, 0, 0);
        assertEquals("Red Hue", 0, color.getHue());
        assertEquals("Red Sat", 255, color.getSaturation());
        assertEquals("Red Lum", 128, color.getLuminence());
    }

    /**
     * Testa un colore primario (Verde).
     * Copre il branch: else if (cMax == G) nel calcolo dell'Hue.
     */
    @Test
    public void testRGBtoHSL_PrimaryGreen() {
        color.initHSLbyRGB(0, 255, 0);
        assertEquals("Green Hue", HSLMAX / 3, color.getHue()); // 85
        assertEquals("Green Sat", 255, color.getSaturation());
        assertEquals("Green Lum", 128, color.getLuminence());
    }

    /**
     * Testa un colore primario (Blu).
     * Copre il branch: else if (cMax == B) nel calcolo dell'Hue.
     */
    @Test
    public void testRGBtoHSL_PrimaryBlue() {
        color.initHSLbyRGB(0, 0, 255);
        assertEquals("Blue Hue", (2 * HSLMAX) / 3, color.getHue()); // 170
        assertEquals("Blue Sat", 255, color.getSaturation());
        assertEquals("Blue Lum", 128, color.getLuminence());
    }

    /**
     * Testa un colore scuro (non-greyscale).
     * Copre il branch: if (pLum <= (HSLMAX / 2)) nel calcolo della Saturazione.
     */
    @Test
    public void testRGBtoHSL_DarkColor() {
        color.initHSLbyRGB(100, 0, 0); // Rosso scuro
        assertEquals("Dark Red Hue", 0, color.getHue());
        assertEquals("Dark Red Sat", 255, color.getSaturation());
        assertEquals("Dark Red Lum", 50, color.getLuminence());
    }

    /**
     * Testa un colore chiaro (non-greyscale).
     * Copre il branch: else (pLum > (HSLMAX / 2)) nel calcolo della Saturazione.
     */
    @Test
    public void testRGBtoHSL_LightColor() {
        color.initHSLbyRGB(255, 128, 128); // Rosso chiaro (Rosa)
        assertEquals("Light Red Hue", 0, color.getHue());
        assertEquals("Light Red Sat", 255, color.getSaturation());
        assertEquals("Light Red Lum", 192, color.getLuminence());
    }

    /**
     * Testa un caso limite cruciale.
     * Scegliamo un RGB (255, 0, 100) che nel calcolo dell'Hue produce
     * un valore negativo (BDelta - GDelta = 25 - 42 = -17).
     * Copre il branch: if (pHue < 0)
     */
    @Test
    public void testRGBtoHSL_HueNegativeWrap() {
        color.initHSLbyRGB(255, 0, 100); 
        // pHue = -17, quindi pHue = -17 + 255 = 238
        assertEquals("Magenta Hue", 238, color.getHue());
    }

    // --- Test per initRGBbyHSL (Conversione da HSL a RGB) ---

    /**
     * Testa la conversione HSL->RGB per un colore greyscale.
     * Copre il branch: if (S == 0)
     */
    @Test
    public void testHSLtoRGB_Greyscale() {
        color.initRGBbyHSL(123, 0, 100); // Hue (123) è irrilevante se S=0
        assertEquals("Grey R", 100, color.getRed());
        assertEquals("Grey G", 100, color.getGreen());
        assertEquals("Grey B", 100, color.getBlue());
    }

    /**
     * Testa la conversione HSL->RGB per un colore con luminanza bassa.
     * Copre il branch: if (L <= HSLMAX / 2)
     */
    @Test
    public void testHSLtoRGB_ColorLowLum() {
        color.initRGBbyHSL(0, 255, 50); // Rosso scuro (dal test precedente)
        assertEquals("Dark Red R", 100, color.getRed());
        assertEquals("Dark Red G", 0, color.getGreen());
        assertEquals("Dark Red B", 0, color.getBlue());
    }

    /**
     * Testa la conversione HSL->RGB per un colore con luminanza alta.
     * Copre il branch: else (L > HSLMAX / 2)
     */
    @Test
    public void testHSLtoRGB_ColorHighLum() {
        color.initRGBbyHSL(0, 255, 192); // Rosso chiaro (dal test precedente)
        assertEquals("Light Red R", 255, color.getRed());
        assertEquals("Light Red G", 128, color.getGreen());
        assertEquals("Light Red B", 128, color.getBlue());
    }

    // --- Test per hueToRGB (metodo privato, testato tramite initRGBbyHSL) ---
    // Dobbiamo scegliere H, S, L in modo da coprire tutti i branch di hueToRGB.

    /**
     * Test complesso per coprire i branch di hueToRGB.
     * Scegliendo H=0 (Rosso), le chiamate a hueToRGB saranno:
     * 1. pRed:   hueToRGB(..., H + 85) -> Hue = 85.
     * Copre: if (Hue < (HSLMAX / 2)) return mag2;
     * 2. pGreen: hueToRGB(..., H) -> Hue = 0.
     * Copre: if (Hue < (HSLMAX / 6)) return (mag1 + ...);
     * 3. pBlue:  hueToRGB(..., H - 85) -> Hue = -85.
     * Copre: if (Hue < 0) Hue = Hue + HSLMAX; -> Hue = 170.
     * Poi copre l'ultimo: return mag1;
     */
    @Test
    public void testHueToRGB_Branches_Case1() {
        color.initRGBbyHSL(0, 255, 128); // Rosso
        assertEquals("Red R", 255, color.getRed());
        assertEquals("Red G", 0, color.getGreen());
        assertEquals("Red B", 0, color.getBlue());
    }

    /**
     * Test complesso per coprire i branch di hueToRGB.
     * Scegliendo H=150 (vicino al Blu), le chiamate a hueToRGB saranno:
     * 1. pRed:   hueToRGB(..., H + 85) -> Hue = 235.
     * Copre l'ultimo: return mag1;
     * 2. pGreen: hueToRGB(..., H) -> Hue = 150.
     * Copre: if (Hue < (HSLMAX * 2 / 3)) return (mag1 + ...); (150 < 170)
     * 3. pBlue:  hueToRGB(..., H - 85) -> Hue = 65.
     * Copre: if (Hue < (HSLMAX / 2)) return mag2; (65 < 127)
     */
    @Test
    public void testHueToRGB_Branches_Case2() {
        color.initRGBbyHSL(150, 255, 128); // Bluastro
        assertEquals("Bluish R", 0, color.getRed());
        assertEquals("Bluish G", 128, color.getGreen());
        assertEquals("Bluish B", 255, color.getBlue());
    }
    
    /**
     * Testa il branch 'Hue > HSLMAX' in hueToRGB.
     * Chiamando initRGBbyHSL con H=300 (non valido, ma possibile),
     * la chiamata per pGreen userà Hue = 300.
     * Questo attiverà: else if (Hue > HSLMAX) Hue = Hue - HSLMAX; (Hue = 45).
     * Poi 45 < (HSLMAX / 2) -> return mag2.
     */
    @Test
    public void testHueToRGB_HueWrapAround() {
        color.initRGBbyHSL(300, 255, 128); 
        // 300 è come 45 (Giallo-Arancio).
        assertTrue(color.getRed() > 200);
        assertTrue(color.getGreen() > 200);
        assertTrue(color.getBlue() < 50);
    }
    
    /**
     * Testa i branch di "clamping" (limitazione) in initRGBbyHSL.
     * if (pRed > RGBMAX) pRed = RGBMAX;
     * Questi sono probabilmente "dead code" (irraggiungibili) se la matematica
     * è corretta e H,S,L sono in [0, 255].
     * Proviamo a forzarli usando valori HSL "illegali" (> 255).
     */
    @Test
    public void testHSLtoRGB_Clamping() {
        color.initRGBbyHSL(0, 500, 500); // Valori S e L "illegali"
        
        // Verifichiamo solo che i valori RGB non superino 255.
        assertTrue("Red clamped", color.getRed() <= 255);
        assertTrue("Green clamped", color.getGreen() <= 255);
        assertTrue("Blue clamped", color.getBlue() <= 255);
    }


    // --- Test per i Setters Pubblici (setHue, setSaturation, setLuminence) ---

    /**
     * Testa il setHue, in particolare i cicli 'while' per il "wrap-around".
     */
    @Test
    public void testSetHue() {
        color.initHSLbyRGB(255, 0, 0); // H=0 (Rosso)
        
        // Test normale
        color.setHue(85); // Verde
        assertEquals("SetHue Green G", 255, color.getGreen());
        
        // Test wrap around < 0 (copre 'while (iToValue < 0)')
        color.setHue(-170); // -170 + 255 = 85 (Verde)
        assertEquals("SetHue < 0 G", 255, color.getGreen());
        
        // Test wrap around > HSLMAX (copre 'while (iToValue > HSLMAX)')
        color.setHue(85 + 255); // 340 - 255 = 85 (Verde)
        assertEquals("SetHue > MAX G", 255, color.getGreen());
    }

    /**
     * Testa setSaturation, in particolare i branch di "clamping" (limitazione).
     */
    @Test
    public void testSetSaturation() {
        color.initHSLbyRGB(255, 0, 0); // S=255
        
        // Test normale
        color.setSaturation(128);
        assertEquals("SetSat normal", 128, color.getSaturation());
        
        // Test clamp < 0 (copre 'if (iToValue < 0)')
        color.setSaturation(-100);
        assertEquals("SetSat < 0", 0, color.getSaturation());
        
        // Test clamp > HSLMAX (copre 'else if (iToValue > HSLMAX)')
        color.setSaturation(500);
        assertEquals("SetSat > MAX", 255, color.getSaturation());
    }

    /**
     * Testa setLuminence, in particolare i branch di "clamping" (limitazione).
     */
    @Test
    public void testSetLuminence() {
        color.initHSLbyRGB(255, 0, 0); // L=128
        
        // Test normale
        color.setLuminence(100);
        assertEquals("SetLum normal", 100, color.getLuminence());
        
        // Test clamp < 0 (copre 'if (iToValue < 0)')
        color.setLuminence(-100);
        assertEquals("SetLum < 0", 0, color.getLuminence());
        
        // Test clamp > HSLMAX (copre 'else if (iToValue > HSLMAX)')
        color.setLuminence(500);
        assertEquals("SetLum > MAX", 255, color.getLuminence());
    }

    // --- Test per i Metodi di Manipolazione del Colore ---

    /**
     * Testa reverseColor(), che aggiunge HSLMAX/2 all'Hue.
     * Questo testa implicitamente di nuovo setHue.
     */
    @Test
    public void testReverseColor() {
        color.initHSLbyRGB(255, 0, 0); // H=0 (Rosso)
        color.reverseColor(); // H = 0 + 127 = 127
        assertEquals("Reverse Hue", 127, color.getHue());
        // Il colore opposto del Rosso è il Ciano
        assertEquals("Reverse R", 0, color.getRed());
        assertEquals("Reverse G", 255, color.getGreen());
        assertEquals("Reverse B", 255, color.getBlue());
    }

    /**
     * Testa brighten(float fPercent) e tutti i suoi casi limite.
     */
    @Test
    public void testBrighten() {
        color.initHSLbyRGB(128, 128, 128); // L=128
        
        // Test fPercent == 0 (copre 'if (fPercent == 0) return;')
        color.brighten(0.0f);
        assertEquals("Brighten 0%", 128, color.getLuminence());
        
        // Test fPercent < 0 (copre 'if (L < 0) L = 0;')
        color.brighten(-1.0f); // L = 128 * -1.0 = -128 -> L = 0
        assertEquals("Brighten < 0", 0, color.getLuminence());
        
        // Reset per il prossimo test
        color.setLuminence(128); 
        
        // Test fPercent > 1 (copre 'if (L > HSLMAX) L = HSLMAX;')
        color.brighten(3.0f); // L = 128 * 3.0 = 384 -> L = 255
        assertEquals("Brighten > MAX", 255, color.getLuminence());
        
        // Reset per il prossimo test
        color.setLuminence(100); 
        
        // Test normale (nessun clamping)
        color.brighten(1.5f); // L = 100 * 1.5 = 150
        assertEquals("Brighten normal", 150, color.getLuminence());
    }

    /**
     * Testa blend(R, G, B, float fPercent) e i suoi casi limite.
     */
    @Test
    public void testBlend() {
        color.initHSLbyRGB(0, 0, 0); // Inizia con Nero
        
        // Test fPercent >= 1 (copre 'if (fPercent >= 1) ... return;')
        color.blend(100, 100, 100, 1.0f);
        assertEquals("Blend 100% R", 100, color.getRed());
        
        // Reset per il prossimo test
        color.initHSLbyRGB(0, 0, 0); 
        
        // Test fPercent <= 0 (copre 'if (fPercent <= 0) return;')
        color.blend(100, 100, 100, 0.0f);
        assertEquals("Blend 0% R", 0, color.getRed());
        
        // Test fPercent < 0 (copre di nuovo 'if (fPercent <= 0) return;')
        color.initHSLbyRGB(0, 0, 0); 
        color.blend(100, 100, 100, -1.0f); 
        assertEquals("Blend < 0% R", 0, color.getRed());
        
        // Test di un blend al 50% (il caso "normale")
        color.initHSLbyRGB(0, 0, 0); // Inizia con Nero
        color.blend(200, 100, 50, 0.5f);
        // newR = (200 * 0.5) + (0 * 0.5) = 100
        // newG = (100 * 0.5) + (0 * 0.5) = 50
        // newB = (50 * 0.5) + (0 * 0.5) = 25
        assertEquals("Blend 50% R", 100, color.getRed());
        assertEquals("Blend 50% G", 50, color.getGreen());
        assertEquals("Blend 50% B", 25, color.getBlue());
    }
}