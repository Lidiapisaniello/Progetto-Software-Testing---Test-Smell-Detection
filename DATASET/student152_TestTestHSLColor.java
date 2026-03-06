import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    /**
     * Testa la conversione da RGB a HSL per un colore primario (Rosso).
     * Copre il ramo: cMax == R
     */
    @Test
    public void testInitHSLbyRGB_Red() {
        // R=255, G=0, B=0
        hslColor.initHSLbyRGB(255, 0, 0);

        assertEquals("Red component should be 255", 255, hslColor.getRed());
        assertEquals("Green component should be 0", 0, hslColor.getGreen());
        assertEquals("Blue component should be 0", 0, hslColor.getBlue());
        
        // Hue per Rosso puro è 0 (o molto vicino a 0/255 a seconda dell'algoritmo)
        // Saturation dovrebbe essere massima (255)
        // Luminance dovrebbe essere a metà (127 o 128)
        
        // Verifica valori HSL calcolati
        assertEquals("Hue for Red should be 0", 0, hslColor.getHue());
        assertEquals("Saturation for Red should be 255", 255, hslColor.getSaturation());
        // Calcolo atteso: ((255+255) + 255) / 510 = 127
        assertEquals("Luminence for Red should be 127", 127, hslColor.getLuminence());
    }

    /**
     * Testa la conversione da RGB a HSL per un colore primario (Verde).
     * Copre il ramo: cMax == G
     */
    @Test
    public void testInitHSLbyRGB_Green() {
        // R=0, G=255, B=0
        hslColor.initHSLbyRGB(0, 255, 0);

        // Hue per Verde è circa 1/3 di 255 -> ~85
        assertEquals("Hue for Green should be approx 85", 85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence());
    }

    /**
     * Testa la conversione da RGB a HSL per un colore primario (Blu).
     * Copre il ramo: cMax == B
     */
    @Test
    public void testInitHSLbyRGB_Blue() {
        // R=0, G=0, B=255
        hslColor.initHSLbyRGB(0, 0, 255);

        // Hue per Blu è circa 2/3 di 255 -> ~170
        assertEquals("Hue for Blue should be approx 170", 170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence());
    }

    /**
     * Testa il caso di scala di grigi (R=G=B).
     * Copre il ramo: if (cMax == cMin)
     */
    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Grigio medio
        hslColor.initHSLbyRGB(128, 128, 128);

        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED (170) for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Luminence should reflect input", 128, hslColor.getLuminence());
    }

    /**
     * Testa il calcolo della saturazione quando Luminance > HSLMAX / 2.
     * Copre il ramo: else in (pLum <= (HSLMAX / 2))
     */
    @Test
    public void testInitHSLbyRGB_HighLuminance() {
        // Colore molto chiaro: R=200, G=255, B=200
        // cMax=255, cMin=200. cPlus=455. Lum = (455*255+255)/510 = 227 (> 127)
        hslColor.initHSLbyRGB(200, 255, 200);

        assertTrue("Luminance should be high", hslColor.getLuminence() > 127);
        // Verifica che Saturation sia calcolata correttamente nel ramo else
        // Sat = (55 * 255 + 0.5) / (510 - 455) = 14025.5 / 55 = 255
        assertEquals(255, hslColor.getSaturation());
    }

    /**
     * Testa il caso in cui Hue risulta negativo prima della correzione.
     * Questo accade tipicamente quando cMax == R e Blue > Green.
     * Copre il ramo: if (pHue < 0)
     */
    @Test
    public void testInitHSLbyRGB_NegativeHueCorrection() {
        // R=255 (Max), G=0, B=100. 
        // Formula Hue: BDelta - GDelta. 
        // Qui GDelta sarà grande (perché G è 0) e BDelta più piccolo.
        // Risultato Hue negativo che deve essere corretto (+HSLMAX).
        hslColor.initHSLbyRGB(255, 0, 100);
        
        // Hue deve essere positivo dopo la correzione
        assertTrue("Hue should be positive", hslColor.getHue() >= 0);
        assertTrue("Hue should be less than MAX", hslColor.getHue() <= HSLMAX);
    }

    /**
     * Testa l'inizializzazione RGB tramite valori HSL (caso Scala di Grigi).
     * Copre il ramo: if (S == 0) in initRGBbyHSL
     */
    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Hue qualsiasi, Sat=0, Lum=100
        hslColor.initRGBbyHSL(50, 0, 100);

        assertEquals("Red should equal Lum scaled", 100, hslColor.getRed());
        assertEquals("Green should equal Red", 100, hslColor.getGreen());
        assertEquals("Blue should equal Red", 100, hslColor.getBlue());
    }

    /**
     * Testa initRGBbyHSL con bassa luminanza (<= HSLMAX/2).
     * Copre il ramo: if (L <= HSLMAX / 2)
     */
    @Test
    public void testInitRGBbyHSL_ColorLowLum() {
        // H=0 (Rosso), S=255, L=64 (Scuro)
        hslColor.initRGBbyHSL(0, 255, 64);

        // Ci aspettiamo un rosso scuro
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getGreen());
        assertTrue("Red should be dominant", hslColor.getRed() > hslColor.getBlue());
        // Verifica che i valori siano stati aggiornati
        assertEquals(128, hslColor.getRed()); // Approx double of 64 due to full saturation
        assertEquals(0, hslColor.getGreen());
        assertEquals(0, hslColor.getBlue());
    }

    /**
     * Testa initRGBbyHSL con alta luminanza.
     * Copre il ramo: else di if (L <= HSLMAX / 2)
     */
    @Test
    public void testInitRGBbyHSL_ColorHighLum() {
        // H=0 (Rosso), S=255, L=192 (Rosso Chiaro/Pastello)
        hslColor.initRGBbyHSL(0, 255, 192);

        assertTrue("Red should be max", hslColor.getRed() >= 255); // Clampato a 255
        assertTrue("Green should be present (whitewash)", hslColor.getGreen() > 0);
        assertTrue("Blue should be present (whitewash)", hslColor.getBlue() > 0);
    }
    
    /**
     * Testa il metodo privato hueToRGB attraverso initRGBbyHSL per coprire i vari branch.
     * hueToRGB ha 4 return statements basati sul valore di Hue.
     */
    @Test
    public void testHueToRGB_Branches() {
        // Hue < 1/6 (0..42) -> Già testato con Rosso (H=0)
        
        // Hue < 1/2 (ma > 1/6) -> Verde puro cade qui in alcuni calcoli o Giallo
        // Testiamo un Giallo/Verde
        hslColor.initRGBbyHSL(80, 255, 127);
        assertEquals(80, hslColor.getHue());

        // Hue < 2/3 (ma > 1/2) -> Blu/Ciano cade qui
        hslColor.initRGBbyHSL(150, 255, 127);
        assertEquals(150, hslColor.getHue());
        
        // Hue > 2/3 -> Viola/Magenta
        hslColor.initRGBbyHSL(220, 255, 127);
        assertEquals(220, hslColor.getHue());
    }
    
    /**
     * Testa il wrapping (ciclicità) del valore Hue nel setter.
     * Copre i loop while in setHue.
     */
    @Test
    public void testSetHue_WrapAround() {
        hslColor.initHSLbyRGB(255, 0, 0); // Init base
        
        // Test underflow (valore negativo)
        hslColor.setHue(-50);
        // -50 + 255 = 205
        assertEquals("Hue should wrap positive", 205, hslColor.getHue());
        
        // Test overflow (valore > 255)
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals("Hue should wrap within range", 45, hslColor.getHue());

        // Test multiple wraps (es. molto negativo)
        hslColor.setHue(-300); 
        // -300 + 255 = -45 -> -45 + 255 = 210
        assertEquals("Hue should wrap multiple times", 210, hslColor.getHue());
    }

    /**
     * Testa il clamping della Saturazione.
     */
    @Test
    public void testSetSaturation_Bounds() {
        hslColor.initHSLbyRGB(100, 100, 100);
        
        hslColor.setSaturation(300); // Sopra il massimo
        assertEquals(255, hslColor.getSaturation());
        
        hslColor.setSaturation(-50); // Sotto il minimo
        assertEquals(0, hslColor.getSaturation());
    }

    /**
     * Testa il clamping della Luminanza.
     */
    @Test
    public void testSetLuminence_Bounds() {
        hslColor.initHSLbyRGB(100, 100, 100);
        
        hslColor.setLuminence(500);
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(-1);
        assertEquals(0, hslColor.getLuminence());
    }

    /**
     * Testa reverseColor.
     * Verifica che la Hue venga spostata di HSLMAX / 2.
     */
    @Test
    public void testReverseColor() {
        // Imposta Rosso (Hue 0)
        hslColor.initHSLbyRGB(255, 0, 0);
        
        hslColor.reverseColor();
        
        // 0 + 127 = 127 (Ciano)
        assertEquals(127, hslColor.getHue());
        
        // Reverse di nuovo
        hslColor.reverseColor();
        // 127 + 127 = 254
        assertEquals(254, hslColor.getHue());
    }

    /**
     * Testa il metodo brighten.
     */
    @Test
    public void testBrighten() {
        // Grigio medio (L=128)
        hslColor.initHSLbyRGB(128, 128, 128);
        int initialLum = hslColor.getLuminence();
        
        // Aumento del 50% (1.5)
        hslColor.brighten(1.5f);
        
        // Atteso: 128 * 1.5 = 192
        assertEquals(192, hslColor.getLuminence());
        
        // Test limite superiore
        hslColor.brighten(2.0f);
        assertEquals(255, hslColor.getLuminence()); // Clamped
        
        // Test fPercent = 0 (nessun cambiamento, early return)
        hslColor.setLuminence(100);
        hslColor.brighten(0.0f);
        assertEquals(100, hslColor.getLuminence());
        
        // Test oscuramento (valore < 1.0)
        // Nota: il metodo si chiama brighten ma moltiplica semplicemente.
        hslColor.setLuminence(100);
        hslColor.brighten(0.5f);
        assertEquals(50, hslColor.getLuminence());
    }

    /**
     * Testa il metodo blend.
     */
    @Test
    public void testBlend() {
        // Partenza: Rosso
        hslColor.initHSLbyRGB(255, 0, 0);
        
        // Blend con Blu al 0% (Mantiene Rosso)
        hslColor.blend(0, 0, 255, 0.0f);
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getBlue());

        // Blend con Blu al 100% (Diventa Blu)
        hslColor.blend(0, 0, 255, 1.0f);
        assertEquals(0, hslColor.getRed());
        assertEquals(255, hslColor.getBlue());

        // Blend con Verde al 50% (Mix Blu/Verde -> Ciano scuro)
        // Current (Blu): 0, 0, 255
        // Target (Verde): 0, 255, 0
        // NewR = 0
        // NewG = 255*0.5 + 0 = 127
        // NewB = 0*0.5 + 255*0.5 = 127
        hslColor.blend(0, 255, 0, 0.5f);
        
        assertEquals(0, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(127, hslColor.getBlue());
    }
    
    /**
     * Testa Getters non coperti esplicitamente in altri test.
     * Utile per verificare che ritornino lo stato corrente corretto.
     */
    @Test
    public void testGettersConsistency() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }
}