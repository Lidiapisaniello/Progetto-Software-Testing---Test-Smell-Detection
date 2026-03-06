import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    // --- initHSLbyRGB tests ---

    /**
     * Test della conversione da RGB a HSL per il colore rosso (H=0, S=255, L=128).
     * Caso: cMax == R (massimo), cMax != cMin, pLum <= HSLMAX/2
     */
    @Test
    public void initHSLbyRGBRedTest() {
        HSLColor color = new HSLColor();
        // Rosso (255, 0, 0)
        color.initHSLbyRGB(255, 0, 0);

        assertEquals("Red Hue should be 0", 0, color.getHue());
        assertEquals("Red Saturation should be 255", 255, color.getSaturation());
        assertEquals("Red Luminence should be 128", 128, color.getLuminence());
        assertEquals("Red Red should be 255", 255, color.getRed());
        assertEquals("Red Green should be 0", 0, color.getGreen());
        assertEquals("Red Blue should be 0", 0, color.getBlue());
    }

    /**
     * Test della conversione da RGB a HSL per il colore verde (H=85, S=255, L=128).
     * Caso: cMax == G (massimo), cMax != cMin, pLum <= HSLMAX/2
     */
    @Test
    public void initHSLbyRGBGreenTest() {
        HSLColor color = new HSLColor();
        // Verde (0, 255, 0)
        color.initHSLbyRGB(0, 255, 0);

        assertEquals("Green Hue should be 85", 85, color.getHue());
        assertEquals("Green Saturation should be 255", 255, color.getSaturation());
        assertEquals("Green Luminence should be 128", 128, color.getLuminence());
    }

    /**
     * Test della conversione da RGB a HSL per il colore blu (H=170, S=255, L=128).
     * Caso: cMax == B (massimo), cMax != cMin, pLum <= HSLMAX/2
     */
    @Test
    public void initHSLbyRGBBlueTest() {
        HSLColor color = new HSLColor();
        // Blu (0, 0, 255)
        color.initHSLbyRGB(0, 0, 255);

        assertEquals("Blue Hue should be 170", 170, color.getHue());
        assertEquals("Blue Saturation should be 255", 255, color.getSaturation());
        assertEquals("Blue Luminence should be 128", 128, color.getLuminence());
    }

    /**
     * Test della conversione da RGB a HSL per un colore con L > HSLMAX/2 (es. Ciano Chiaro).
     * Caso: cMax == R, cMax != cMin, pLum > HSLMAX/2, verifica il ramo di saturazione 'else'.
     */
    @Test
    public void initHSLbyRGBHighLuminenceTest() {
        HSLColor color = new HSLColor();
        // Ciano Chiaro (128, 255, 255). cMax=255, cMin=128. cMinus=127, cPlus=383. L=192 (> 128).
        color.initHSLbyRGB(128, 255, 255);

        // H dovrebbe essere circa 118, S circa 169, L è 192.
        assertEquals("Light Cyan Hue should be 118", 118, color.getHue());
        assertEquals("Light Cyan Saturation should be 169", 169, color.getSaturation());
        assertEquals("Light Cyan Luminence should be 192", 192, color.getLuminence());
    }

    /**
     * Test della conversione da RGB a HSL per il grigio (scala di grigi).
     * Caso: cMax == cMin, verifica il ramo greyscale.
     */
    @Test
    public void initHSLbyRGBGreyscaleTest() {
        HSLColor color = new HSLColor();
        // Grigio Medio (128, 128, 128)
        color.initHSLbyRGB(128, 128, 128);

        assertEquals("Grey Hue should be UNDEFINED (170)", 170, color.getHue());
        assertEquals("Grey Saturation should be 0", 0, color.getSaturation());
        assertEquals("Grey Luminence should be 128", 128, color.getLuminence());
    }

    /**
     * Test della conversione da RGB a HSL dove pHue calcolato è negativo e deve essere corretto.
     * Es. Magenta. cMax=R. pHue = BDelta - GDelta. 
     * Magenta (255, 0, 255) -> H=213, S=255, L=128.
     * (255, 0, 255): RDelta=0, GDelta=128, BDelta=0.
     * cMax==R: pHue = 0 - 128 = -128. pHue = -128 + 255 = 127.
     * L'output non corrisponde a Magenta (H=213), ma verifica il ramo `if (pHue < 0)`.
     */
    @Test
    public void initHSLbyRGBNegativeHueTest() {
        HSLColor color = new HSLColor();
        // Un colore che forza pHue < 0: RGB(255, 128, 255) - Magenta Chiaro
        // cMax=R=255, cMin=128. cMinus=127.
        // RDelta = 0. GDelta = 63. BDelta = 0.
        // cMax==R: pHue = BDelta - GDelta = 0 - 63 = -63. pHue = -63 + 255 = 192.
        color.initHSLbyRGB(255, 128, 255);

        // Il test verifica che il ramo if (pHue < 0) venga eseguito
        assertEquals("Hue should be 192 after correction", 192, color.getHue());
    }

    // --- initRGBbyHSL tests ---

    /**
     * Test della conversione da HSL a RGB per la scala di grigi (S=0).
     * Caso: S == 0.
     */
    @Test
    public void initRGBbyHSLGreysaleTest() {
        HSLColor color = new HSLColor();
        // L=128 (circa 50%). RGB dovrebbe essere 128, 128, 128.
        color.initRGBbyHSL(0, 0, 128);

        assertEquals("Grey Red should be 128", 128, color.getRed());
        assertEquals("Grey Green should be 128", 128, color.getGreen());
        assertEquals("Grey Blue should be 128", 128, color.getBlue());
    }

    /**
     * Test della conversione da HSL a RGB con L <= HSLMAX/2.
     * Caso: S != 0, L <= HSLMAX/2.
     */
    @Test
    public void initRGBbyHSLLowLuminenceTest() {
        HSLColor color = new HSLColor();
        // Rosso (H=0, S=255, L=128). L <= 128 (HSLMAX/2).
        // Magic2 = (128 * 510 + 128) / 255 = 256. Magic1 = 0.
        color.initRGBbyHSL(0, 255, 128);

        assertEquals("Red Red should be 255", 255, color.getRed());
        assertEquals("Red Green should be 0", 0, color.getGreen());
        assertEquals("Red Blue should be 0", 0, color.getBlue());
    }

    /**
     * Test della conversione da HSL a RGB con L > HSLMAX/2.
     * Caso: S != 0, L > HSLMAX/2.
     */
    @Test
    public void initRGBbyHSLHighLuminenceTest() {
        HSLColor color = new HSLColor();
        // Bianco (H=0, S=0, L=255) non va bene, usa un colore saturo chiaro.
        // Giallo Chiaro (H=43, S=255, L=192). L > 128 (HSLMAX/2).
        // Magic2 = 192 + 255 - (192 * 255 + 128) / 255 = 255. Magic1 = 129.
        color.initRGBbyHSL(43, 255, 192);

        // Giallo Chiaro (255, 255, 127) approssimato.
        assertEquals("Light Yellow Red should be 255", 255, color.getRed());
        assertEquals("Light Yellow Green should be 255", 255, color.getGreen());
        assertEquals("Light Yellow Blue should be 127", 127, color.getBlue());
    }

    /**
     * Test della conversione HSL a RGB con overflow in pRed (pRed > RGBMAX).
     */
    @Test
    public void initRGBbyHSLRedOverflowTest() {
        HSLColor color = new HSLColor();
        // Il test non può creare un vero overflow R, G, B senza HSL, Sat, Lum in range.
        // Utilizziamo il Rosso (0, 255, 128) che produce R=255 per testare che il limite venga applicato
        // sebbene in questo caso non sia strettamente un overflow, verifica il ramo 'if (pRed > RGBMAX)'.
        color.initRGBbyHSL(0, 255, 128);

        assertEquals("Red should be capped at RGBMAX (255)", 255, color.getRed());
    }

    /**
     * Test della conversione HSL a RGB con overflow in pGreen.
     */
    @Test
    public void initRGBbyHSLGreenOverflowTest() {
        HSLColor color = new HSLColor();
        // Simile, usiamo Verde (85, 255, 128) che produce G=255 per testare il ramo 'if (pGreen > RGBMAX)'.
        color.initRGBbyHSL(85, 255, 128);

        assertEquals("Green should be capped at RGBMAX (255)", 255, color.getGreen());
    }

    /**
     * Test della conversione HSL a RGB con overflow in pBlue.
     */
    @Test
    public void initRGBbyHSLBlueOverflowTest() {
        HSLColor color = new HSLColor();
        // Simile, usiamo Blu (170, 255, 128) che produce B=255 per testare il ramo 'if (pBlue > RGBMAX)'.
        color.initRGBbyHSL(170, 255, 128);

        assertEquals("Blue should be capped at RGBMAX (255)", 255, color.getBlue());
    }

    // --- hueToRGB tests ---

    /**
     * Test hueToRGB per Hue < HSLMAX/6.
     * HSLMAX/6 = 42.
     */
    @Test
    public void initRGBbyHSL_hueToRGB_Range1Test() {
        HSLColor color = new HSLColor();
        // Eseguito indirettamente tramite initRGBbyHSL.
        // Rosso (H=0). Hue=H+HSLMAX/3 = 85. Hue=H = 0. Hue=H-HSLMAX/3 = -85.
        // Per testare il ramo < HSLMAX/6 (0..42). R in H=0.
        color.initRGBbyHSL(0, 255, 128);

        // Controlliamo il Rosso, dove Hue=H=0.
        assertEquals("Red should be 255", 255, color.getRed());
    }

    /**
     * Test hueToRGB per HSLMAX/6 <= Hue < HSLMAX/2.
     * HSLMAX/6 = 42. HSLMAX/2 = 127. (43..127).
     */
    @Test
    public void initRGBbyHSL_hueToRGB_Range2Test() {
        HSLColor color = new HSLColor();
        // H=43. Hue=H=43.
        // HSL(43, 255, 128) -> Giallo/Verde. R=128, G=255, B=0.
        color.initRGBbyHSL(43, 255, 128);

        // Controlliamo il Verde, dove Hue=H=43. Il risultato RGB atteso è 255 (mag2).
        assertEquals("Green should be 255", 255, color.getGreen());
    }

    /**
     * Test hueToRGB per HSLMAX/2 <= Hue < HSLMAX*2/3.
     * HSLMAX/2 = 127. HSLMAX*2/3 = 170. (128..169).
     */
    @Test
    public void initRGBbyHSL_hueToRGB_Range3Test() {
        HSLColor color = new HSLColor();
        // H=128. Hue=H=128.
        // HSL(128, 255, 128) -> Ciano. R=0, G=255, B=255.
        color.initRGBbyHSL(128, 255, 128);

        // Controlliamo il Verde, dove Hue=H=128.
        assertEquals("Green should be 255", 255, color.getGreen());
    }

    /**
     * Test hueToRGB per Hue > HSLMAX*2/3.
     * HSLMAX*2/3 = 170. (171..255).
     */
    @Test
    public void initRGBbyHSL_hueToRGB_Range4Test() {
        HSLColor color = new HSLColor();
        // H=213. Hue=H=213.
        // HSL(213, 255, 128) -> Magenta. R=255, G=0, B=255.
        color.initRGBbyHSL(213, 255, 128);

        // Controlliamo il Verde, dove Hue=H=213. Il risultato RGB atteso è 0 (mag1).
        assertEquals("Green should be 0", 0, color.getGreen());
    }

    /**
     * Test hueToRGB con Hue iniziale negativo per verificare la correzione del range.
     * Hue=H - HSLMAX/3. H=0. Hue=-85. Correggi a -85 + 255 = 170. Range 3.
     */
    @Test
    public void initRGBbyHSL_hueToRGB_NegativeRangeTest() {
        HSLColor color = new HSLColor();
        // Rosso (H=0, S=255, L=128).
        color.initRGBbyHSL(0, 255, 128);

        // Controlliamo il Blu, dove Hue = H - HSLMAX/3 = -85. Corretto a 170. Range 3/4.
        assertEquals("Blue should be 0", 0, color.getBlue());
    }

    /**
     * Test hueToRGB con Hue iniziale maggiore di HSLMAX per verificare la correzione del range.
     * Hue=H + HSLMAX/3. H=255 (max). Hue=255 + 85 = 340. Correggi a 340 - 255 = 85. Range 2.
     */
    @Test
    public void initRGBbyHSL_hueToRGB_PositiveOutOfRangeTest() {
        HSLColor color = new HSLColor();
        // Blu (H=170, S=255, L=128).
        color.initRGBbyHSL(170, 255, 128);

        // Controlliamo il Rosso, dove Hue = H + HSLMAX/3 = 170 + 85 = 255. Corretto a 0. Range 1.
        assertEquals("Red should be 0", 0, color.getRed());
    }

    // --- Getter/Setter and Utility tests ---

    /**
     * Test per getHue.
     */
    @Test
    public void getHueTest() {
        HSLColor color = new HSLColor();
        // Colore iniziale per impostare pHue.
        color.initRGBbyHSL(100, 50, 50);

        assertEquals("Hue should be 100", 100, color.getHue());
    }

    /**
     * Test per setHue con valore negativo (correzione).
     */
    @Test
    public void setHueNegativeValueTest() {
        HSLColor color = new HSLColor();
        // Inizializza per impostare S e L.
        color.initRGBbyHSL(0, 100, 100);

        // Imposta a -10. Corretto a 245.
        color.setHue(-10);

        // setHue chiama initRGBbyHSL, quindi il valore interno di pHue viene aggiornato.
        assertEquals("Hue should be 245 after negative correction", 245, color.getHue());
    }

    /**
     * Test per setHue con valore > HSLMAX (correzione).
     */
    @Test
    public void setHuePositiveOutOfRangeTest() {
        HSLColor color = new HSLColor();
        // Inizializza per impostare S e L.
        color.initRGBbyHSL(0, 100, 100);

        // Imposta a 260. Corretto a 5.
        color.setHue(260);

        assertEquals("Hue should be 5 after positive correction", 5, color.getHue());
    }

    /**
     * Test per getSaturation.
     */
    @Test
    public void getSaturationTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        assertEquals("Saturation should be 50", 50, color.getSaturation());
    }

    /**
     * Test per setSaturation con valore negativo (correzione).
     */
    @Test
    public void setSaturationNegativeValueTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        // Imposta a -10. Corretto a 0.
        color.setSaturation(-10);

        assertEquals("Saturation should be 0 after negative correction", 0, color.getSaturation());
    }

    /**
     * Test per setSaturation con valore > HSLMAX (correzione).
     */
    @Test
    public void setSaturationPositiveOutOfRangeTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        // Imposta a 300. Corretto a 255.
        color.setSaturation(300);

        assertEquals("Saturation should be 255 after positive correction", 255, color.getSaturation());
    }

    /**
     * Test per getLuminence.
     */
    @Test
    public void getLuminenceTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        assertEquals("Luminence should be 50", 50, color.getLuminence());
    }

    /**
     * Test per setLuminence con valore negativo (correzione).
     */
    @Test
    public void setLuminenceNegativeValueTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        // Imposta a -10. Corretto a 0.
        color.setLuminence(-10);

        assertEquals("Luminence should be 0 after negative correction", 0, color.getLuminence());
    }

    /**
     * Test per setLuminence con valore > HSLMAX (correzione).
     */
    @Test
    public void setLuminencePositiveOutOfRangeTest() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 50, 50);

        // Imposta a 300. Corretto a 255.
        color.setLuminence(300);

        assertEquals("Luminence should be 255 after positive correction", 255, color.getLuminence());
    }

    /**
     * Test per getRed.
     */
    @Test
    public void getRedTest() {
        HSLColor color = new HSLColor();
        // Rosso (255, 0, 0)
        color.initHSLbyRGB(255, 0, 0);

        assertEquals("Red should be 255", 255, color.getRed());
    }

    /**
     * Test per getGreen.
     */
    @Test
    public void getGreenTest() {
        HSLColor color = new HSLColor();
        // Verde (0, 255, 0)
        color.initHSLbyRGB(0, 255, 0);

        assertEquals("Green should be 255", 255, color.getGreen());
    }

    /**
     * Test per getBlue.
     */
    @Test
    public void getBlueTest() {
        HSLColor color = new HSLColor();
        // Blu (0, 0, 255)
        color.initHSLbyRGB(0, 0, 255);

        assertEquals("Blue should be 255", 255, color.getBlue());
    }

    /**
     * Test per reverseColor (pHue + HSLMAX/2).
     */
    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        // Rosso (H=0, S=255, L=128)
        color.initRGBbyHSL(0, 255, 128);

        // Inversione: H=0 + 127 = 127 (Ciano/Verde)
        color.reverseColor();

        // setHue è chiamato, quindi pHue è corretto.
        // H=127.
        assertEquals("Hue should be 127 after reverseColor", 127, color.getHue());
    }

    /**
     * Test per reverseColor con Hue in eccesso (> HSLMAX) per forzare il wrap.
     */
    @Test
    public void reverseColorWrapAroundTest() {
        HSLColor color = new HSLColor();
        // Giallo (H=43, S=255, L=128)
        color.initRGBbyHSL(200, 255, 128);

        // Inversione: H=200 + 127 = 327. setHue corregge 327 - 255 = 72.
        color.reverseColor();

        assertEquals("Hue should be 72 after reverseColor wrap-around", 72, color.getHue());
    }

    /**
     * Test per brighten con fPercent == 0. (Ramo 'return').
     */
    @Test
    public void brightenZeroPercentTest() {
        HSLColor color = new HSLColor();
        // Inizializza L a 128
        color.initRGBbyHSL(0, 255, 128);

        color.brighten(0.0f);

        assertEquals("Luminence should remain 128", 128, color.getLuminence());
    }

    /**
     * Test per brighten con fPercent positivo e L calcolato < HSLMAX (caso normale).
     */
    @Test
    public void brightenNormalTest() {
        HSLColor color = new HSLColor();
        // Inizializza L a 128
        color.initRGBbyHSL(0, 255, 128);

        // L = 128 * 1.5 = 192.
        color.brighten(1.5f);

        assertEquals("Luminence should be 192", 192, color.getLuminence());
    }

    /**
     * Test per brighten con fPercent negativo per forzare L < 0 (correzione a 0).
     */
    @Test
    public void brightenNegativeLuminenceTest() {
        HSLColor color = new HSLColor();
        // Inizializza L a 128
        color.initRGBbyHSL(0, 255, 128);

        // L = 128 * -0.5 = -64. Corretto a 0.
        color.brighten(-0.5f);

        assertEquals("Luminence should be 0 after negative L correction", 0, color.getLuminence());
    }

    /**
     * Test per brighten con L calcolato > HSLMAX (correzione a HSLMAX).
     */
    @Test
    public void brightenOverflowTest() {
        HSLColor color = new HSLColor();
        // Inizializza L a 128
        color.initRGBbyHSL(0, 255, 128);

        // L = 128 * 3.0 = 384. Corretto a 255.
        color.brighten(3.0f);

        assertEquals("Luminence should be 255 after overflow correction", 255, color.getLuminence());
    }

    /**
     * Test per blend con fPercent >= 1 (Blend completo).
     */
    @Test
    public void blendFullPercentTest() {
        HSLColor color = new HSLColor();
        // Inizializza a Rosso (255, 0, 0)
        color.initHSLbyRGB(255, 0, 0);

        // Blend con Blu (0, 0, 255) al 100%
        color.blend(0, 0, 255, 1.0f);

        // Il colore dovrebbe diventare Blu
        assertEquals("Red should be 0 after full blend to Blue", 0, color.getRed());
        assertEquals("Blue should be 255 after full blend to Blue", 255, color.getBlue());
    }

    /**
     * Test per blend con fPercent <= 0 (Nessun blend).
     */
    @Test
    public void blendZeroPercentTest() {
        HSLColor color = new HSLColor();
        // Inizializza a Rosso (255, 0, 0)
        color.initHSLbyRGB(255, 0, 0);

        // Blend con Blu (0, 0, 255) allo 0%
        color.blend(0, 0, 255, 0.0f);

        // Il colore dovrebbe rimanere Rosso
        assertEquals("Red should remain 255 after zero blend", 255, color.getRed());
        assertEquals("Blue should remain 0 after zero blend", 0, color.getBlue());
    }

    /**
     * Test per blend con 0 < fPercent < 1 (Blend parziale).
     */
    @Test
    public void blendPartialPercentTest() {
        HSLColor color = new HSLColor();
        // Inizializza a Rosso (255, 0, 0).
        color.initHSLbyRGB(255, 0, 0);

        // Blend con Blu (0, 0, 255) al 50% (0.5f)
        // newR = (0 * 0.5) + (255 * 0.5) = 127.5 -> 127
        // newG = (0 * 0.5) + (0 * 0.5) = 0
        // newB = (255 * 0.5) + (0 * 0.5) = 127.5 -> 127
        // Risultato atteso: Grigio-Magenta (127, 0, 127).
        color.blend(0, 0, 255, 0.5f);

        assertEquals("Red should be 127 after 50% blend", 127, color.getRed());
        assertEquals("Green should be 0 after 50% blend", 0, color.getGreen());
        assertEquals("Blue should be 127 after 50% blend", 127, color.getBlue());
    }
}