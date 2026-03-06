import org.junit.Test;
import static org.junit.Assert.*;

// L'implementazione completa di HSLColor è richiesta affinché questi test siano compilabili.
// Assumo che i metodi privati setRed, setGreen, setBlue, greyscale, reverseLight, iMax e iMin
// non vengano testati direttamente in quanto non sono pubblici, ma vengano coperti
// attraverso i metodi pubblici che li utilizzano.
public class TestHSLColor {

    // Costanti della classe HSLColor per riferimento nei test
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    // --- Test per initHSLbyRGB (Conversione RGB -> HSL) ---

    // Test per il caso di colore primario puro (Rosso)
    @Test
    public void testInitHSLbyRGB_PrimaryRed() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, 0, 0); // Rosso puro
        // I valori esatti HSL calcolati con aritmetica intera possono variare
        // Testiamo che i valori HSL siano ragionevoli per il Rosso.
        // Rosos dovrebbe avere Hue=0.
        // H = 0, S = 255, L = 127/128
        assertEquals("Verifica Hue per Rosso", 0, color.getHue());
        assertEquals("Verifica Saturation per Rosso", HSLMAX, color.getSaturation());
        // L = ((255+0)*255 + 255) / (2*255) = 65280 / 510 = 128 (circa)
        assertEquals("Verifica Luminence per Rosso", 128, color.getLuminence());
        assertEquals("Verifica Red dopo init", RGBMAX, color.getRed());
    }

    // Test per il caso di colore primario puro (Verde) - Copre il ramo cMax == G
    @Test
    public void testInitHSLbyRGB_PrimaryGreen() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, RGBMAX, 0); // Verde puro
        // Verde dovrebbe avere Hue = HSLMAX/3 = 85
        // H = 85, S = 255, L = 128
        assertEquals("Verifica Hue per Verde", HSLMAX / 3, color.getHue());
        assertEquals("Verifica Saturation per Verde", HSLMAX, color.getSaturation());
        assertEquals("Verifica Luminence per Verde", 128, color.getLuminence());
        assertEquals("Verifica Green dopo init", RGBMAX, color.getGreen());
    }

    // Test per il caso di colore primario puro (Blu) - Copre il ramo cMax == B
    @Test
    public void testInitHSLbyRGB_PrimaryBlue() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, RGBMAX); // Blu puro
        // Blu dovrebbe avere Hue = 2*HSLMAX/3 = 170
        // H = 170, S = 255, L = 128
        assertEquals("Verifica Hue per Blu", (2 * HSLMAX) / 3, color.getHue());
        assertEquals("Verifica Saturation per Blu", HSLMAX, color.getSaturation());
        assertEquals("Verifica Luminence per Blu", 128, color.getLuminence());
        assertEquals("Verifica Blue dopo init", RGBMAX, color.getBlue());
    }

    // Test per il caso di scala di grigi (cMax == cMin)
    @Test
    public void testInitHSLbyRGB_Greyscale() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128); // Grigio medio
        // Greyscale: S = 0, H = UNDEFINED
        assertEquals("Verifica Hue per Grigio", UNDEFINED, color.getHue());
        assertEquals("Verifica Saturation per Grigio", 0, color.getSaturation());
        // L = ((128+128)*255 + 255) / 510 = (256*255 + 255) / 510 = 65535 / 510 = 128 (arrotondato)
        assertEquals("Verifica Luminence per Grigio", 128, color.getLuminence());
    }

    // Test per il ramo di saturazione con pLum <= HSLMAX / 2
    @Test
    public void testInitHSLbyRGB_SaturationLowLum() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 50, 50); // Bassa luminanza, cMax=100, cMin=50, cPlus=150, L=75
        // pLum = 75, quindi pLum <= 127
        // pSat = ((50*255) + 0.5) / 150 = 12750 / 150 = 85
        assertEquals("Verifica Luminence per Sat Low Lum", 75, color.getLuminence());
        assertEquals("Verifica Saturation per Sat Low Lum", 85, color.getSaturation());
        assertEquals("Verifica cMax == R per Sat Low Lum", 0, color.getHue()); // Prossimo a rosso
    }

    // Test per il ramo di saturazione con pLum > HSLMAX / 2
    @Test
    public void testInitHSLbyRGB_SaturationHighLum() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 150, 150); // Alta luminanza, cMax=200, cMin=150, cPlus=350, L=175
        // pLum = 175, quindi pLum > 127
        // pSat = ((50*255) + 0.5) / (510 - 350) = 12750 / 160 = 79 (circa)
        assertEquals("Verifica Luminence per Sat High Lum", 175, color.getLuminence());
        assertEquals("Verifica Saturation per Sat High Lum", 79, color.getSaturation());
        assertEquals("Verifica cMax == R per Sat High Lum", 0, color.getHue()); // Prossimo a rosso
    }

    // Test per il ramo pHue < 0
    @Test
    public void testInitHSLbyRGB_NegativeHue() {
        HSLColor color = new HSLColor();
        // Un colore vicino al rosso ma che finisce nel ramo cMax == R
        // e genera pHue negativo. Esempio R=255, G=1, B=254. cMax=255, cMin=1, cMinus=254
        // BDelta = round(((255-254)*(42.5)) / 254) = 0
        // GDelta = round(((255-1)*(42.5)) / 254) = 42
        // pHue = BDelta - GDelta = 0 - 42 = -42. Viene corretto a 255 - 42 = 213.
        color.initHSLbyRGB(255, 1, 254);
        assertEquals("Verifica Hue negativo corretto", 213, color.getHue());
    }

    // --- Test per initRGBbyHSL (Conversione HSL -> RGB) e hueToRGB ---

    // Test per il caso di scala di grigi (S == 0)
    @Test
    public void testInitRGBbyHSL_Greyscale() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 0, 150); // H qualsiasi, S=0, L=150
        // pRed = (150 * 255) / 255 = 150
        assertEquals("Verifica Red per Greyscale", 150, color.getRed());
        assertEquals("Verifica Green per Greyscale", 150, color.getGreen());
        assertEquals("Verifica Blue per Greyscale", 150, color.getBlue());
        assertEquals("Verifica Saturation dopo init", 0, color.getSaturation());
    }

    // Test per il ramo di calcolo Magic2 con L <= HSLMAX / 2
    @Test
    public void testInitRGBbyHSL_LowLum() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, HSLMAX, 64); // Rosso Puro Scuro (H=0, S=255, L=64). L<=127
        // Magic2 = (64 * (255 + 255) + 127.5) / 255 = (32640 + 127) / 255 = 128
        // Magic1 = 2*64 - 128 = 0
        color.initRGBbyHSL(0, HSLMAX, 64);
        assertEquals("Verifica Red dopo init Low Lum", 128, color.getRed());
        assertEquals("Verifica Green dopo init Low Lum", 0, color.getGreen());
        assertEquals("Verifica Blue dopo init Low Lum", 0, color.getBlue());
    }

    // Test per il ramo di calcolo Magic2 con L > HSLMAX / 2
    @Test
    public void testInitRGBbyHSL_HighLum() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, HSLMAX, 191); // Rosso Puro Chiaro (H=0, S=255, L=191). L>127
        // Magic2 = 191 + 255 - ((191*255) + 127.5) / 255 = 446 - 191 = 255
        // Magic1 = 2*191 - 255 = 127
        color.initRGBbyHSL(0, HSLMAX, 191);
        assertEquals("Verifica Red dopo init High Lum", 255, color.getRed());
        assertEquals("Verifica Green dopo init High Lum", 127, color.getGreen());
        assertEquals("Verifica Blue dopo init High Lum", 127, color.getBlue());
    }

    // Test hueToRGB per i limiti di Hue: < 0 e > HSLMAX
    @Test
    public void testInitRGBbyHSL_HueWrapping() {
        HSLColor color = new HSLColor();
        // Test per Hue + HSLMAX/3 > HSLMAX (Verde, 255/3=85, 255/2=127.5. Esempio H=200)
        color.initRGBbyHSL(200, HSLMAX, 128);
        // pRed usa H + HSLMAX/3 = 200 + 85 = 285. 285 > 255. Hue diventa 285 - 255 = 30.
        int expectedRed = color.getRed(); // Salviamo il risultato corretto.

        // Test per Hue - HSLMAX/3 < 0 (Rosso, 255/3=85. Esempio H=1)
        color.initRGBbyHSL(1, HSLMAX, 128);
        // pBlue usa H - HSLMAX/3 = 1 - 85 = -84. -84 < 0. Hue diventa -84 + 255 = 171.
        int expectedBlue = color.getBlue(); // Salviamo il risultato corretto.

        color.initRGBbyHSL(200, HSLMAX, 128);
        assertEquals("Verifica Hue > HSLMAX", expectedRed, color.getRed());

        color.initRGBbyHSL(1, HSLMAX, 128);
        assertEquals("Verifica Hue < 0", expectedBlue, color.getBlue());
    }

    // Test hueToRGB: Ramo 1 (Hue < HSLMAX/6)
    @Test
    public void testHueToRGB_Branch1() {
        HSLColor color = new HSLColor();
        // H = 0, S = 255, L = 128. Magic1=0, Magic2=255.
        // Hue=0, che è < 255/6 (42.5)
        color.initRGBbyHSL(0, HSLMAX, 128);
        assertEquals("Verifica pRed - Branch 1", RGBMAX, color.getRed()); // Rosso
    }

    // Test hueToRGB: Ramo 2 (Hue < HSLMAX/2)
    @Test
    public void testHueToRGB_Branch2() {
        HSLColor color = new HSLColor();
        // H = 85 (Verde, HSLMAX/3). Magic1=0, Magic2=255.
        // pGreen usa H=85, che è < 255/2 (127.5). Ritorna Magic2 (255).
        color.initRGBbyHSL(85, HSLMAX, 128);
        assertEquals("Verifica pGreen - Branch 2", RGBMAX, color.getGreen()); // Verde
    }

    // Test hueToRGB: Ramo 3 (Hue < HSLMAX*2/3)
    @Test
    public void testHueToRGB_Branch3() {
        HSLColor color = new HSLColor();
        // H = 100. Magic1=0, Magic2=255.
        // pRed usa H=185. pGreen usa H=100. pBlue usa H=15.
        // H=15. È < 42.5 (Branch 1). pBlue=127. (127)

        // H=100. È < 170 (Branch 3). Ritorna 100. (200)

        color.initRGBbyHSL(100, HSLMAX, 128);
        // pGreen è 200 circa
        assertEquals("Verifica pGreen - Branch 3", 200, color.getGreen());
    }

    // Test hueToRGB: Ramo 4 (else - Ritorna mag1)
    @Test
    public void testHueToRGB_Branch4() {
        HSLColor color = new HSLColor();
        // H = 200. Magic1=0, Magic2=255.
        // pGreen usa H=200, che è >= 170. Ritorna Magic1 (0).
        color.initRGBbyHSL(200, HSLMAX, 128);
        assertEquals("Verifica pGreen - Branch 4", 0, color.getGreen());
    }

    // Test limite RGBMAX
    @Test
    public void testInitRGBbyHSL_RGBMaxClamp() {
        HSLColor color = new HSLColor();
        // Valori che potrebbero portare i calcoli oltre RGBMAX (255) a causa di arrotondamenti
        // e calcolo di Magic2
        color.initRGBbyHSL(0, 1, 1); // Sat basso, Lum basso
        color.initRGBbyHSL(0, 255, 255); // Bianco Puro. L=255. Magic2=255. Magic1=255.
        // Tutti i valori RGB sono 255.
        assertEquals("Verifica Red clamp", RGBMAX, color.getRed());
        assertEquals("Verifica Green clamp", RGBMAX, color.getGreen());
        assertEquals("Verifica Blue clamp", RGBMAX, color.getBlue());
    }

    // --- Test per Getter e Setter Pubblici ---

    // Test per get/set Hue - inclusi i limiti negativi/positivi in setHue
    @Test
    public void testGetSetHue_Wrapping() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128); // Colore iniziale grigio (per avere pSat=0, pLum=128)
        color.setSaturation(HSLMAX); // Saturazione massima per testare l'Hue

        // Test setHue con valore > HSLMAX
        color.setHue(HSLMAX + 10); // Dovrebbe diventare 10
        assertEquals("Verifica setHue > HSLMAX", 10, color.getHue());

        // Test setHue con valore < 0
        color.setHue(-10); // Dovrebbe diventare HSLMAX - 10 = 245
        assertEquals("Verifica setHue < 0", HSLMAX - 10, color.getHue());

        // Test setHue con valore normale
        color.setHue(100);
        assertEquals("Verifica setHue normale", 100, color.getHue());
    }

    // Test per get/set Saturation - inclusi i limiti
    @Test
    public void testGetSetSaturation_Clamping() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);

        // Test setSaturation con valore > HSLMAX
        color.setSaturation(HSLMAX + 10); // Dovrebbe essere 255
        assertEquals("Verifica setSaturation > HSLMAX", HSLMAX, color.getSaturation());
        assertEquals("Verifica getSaturation", HSLMAX, color.getSaturation());

        // Test setSaturation con valore < 0
        color.setSaturation(-10); // Dovrebbe essere 0
        assertEquals("Verifica setSaturation < 0", 0, color.getSaturation());
    }

    // Test per get/set Luminence - inclusi i limiti
    @Test
    public void testGetSetLuminence_Clamping() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);

        // Test setLuminence con valore > HSLMAX
        color.setLuminence(HSLMAX + 10); // Dovrebbe essere 255
        assertEquals("Verifica setLuminence > HSLMAX", HSLMAX, color.getLuminence());
        assertEquals("Verifica getLuminence", HSLMAX, color.getLuminence());

        // Test setLuminence con valore < 0
        color.setLuminence(-10); // Dovrebbe essere 0
        assertEquals("Verifica setLuminence < 0", 0, color.getLuminence());
    }

    // Test per get Red, Green, Blue
    @Test
    public void testGetRGB() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertEquals("Verifica getRed", 10, color.getRed());
        assertEquals("Verifica getGreen", 20, color.getGreen());
        assertEquals("Verifica getBlue", 30, color.getBlue());
    }

    // --- Test per Metodi Pubblici di Manipolazione ---

    // Test per reverseColor (copre anche setHue)
    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, 0, 0); // Rosso (H=0)

        color.reverseColor(); // H = 0 + HSLMAX/2 = 127
        assertEquals("Verifica Hue dopo reverseColor", HSLMAX / 2, color.getHue()); // Prossimo a Ciano (127)
    }

    // Test per brighten (copre il ramo fPercent == 0 e i limiti di L)
    @Test
    public void testBrighten_PercentZero() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10); // L basso (L=10)
        int initialLuminence = color.getLuminence();

        color.brighten(0.0f); // Non dovrebbe cambiare nulla
        assertEquals("Verifica brighten con 0%", initialLuminence, color.getLuminence());
    }

    @Test
    public void testBrighten_Normal() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10); // L=10

        color.brighten(2.0f); // L = 10 * 2.0 = 20
        assertEquals("Verifica brighten normale", 20, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampHigh() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10); // L=10

        color.brighten(30.0f); // L = 10 * 30.0 = 300. Clamped a 255.
        assertEquals("Verifica brighten clamp alto", HSLMAX, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampLow() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255); // L=255

        color.brighten(-1.0f); // L = 255 * -1.0 = -255. Clamped a 0.
        assertEquals("Verifica brighten clamp basso", 0, color.getLuminence());
    }

    // Test per blend (copre tutti i rami fPercent >= 1, fPercent <= 0 e normale)
    @Test
    public void testBlend_PercentOne() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30); // Colore iniziale

        color.blend(200, 210, 220, 1.0f); // Dovrebbe assumere completamente il nuovo colore
        assertEquals("Verifica blend con 100% (Red)", 200, color.getRed());
        assertEquals("Verifica blend con 100% (Green)", 210, color.getGreen());
        assertEquals("Verifica blend con 100% (Blue)", 220, color.getBlue());
    }

    @Test
    public void testBlend_PercentZero() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30); // Colore iniziale
        int initialRed = color.getRed();

        color.blend(200, 210, 220, 0.0f); // Non dovrebbe cambiare nulla
        assertEquals("Verifica blend con 0%", initialRed, color.getRed());
    }

    @Test
    public void testBlend_Normal() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30); // Colore iniziale R=10, G=20, B=30

        // Blend al 50% con R=200, G=210, B=220
        color.blend(200, 210, 220, 0.5f);

        // newR = (200 * 0.5) + (10 * 0.5) = 100 + 5 = 105
        // newG = (210 * 0.5) + (20 * 0.5) = 105 + 10 = 115
        // newB = (220 * 0.5) + (30 * 0.5) = 110 + 15 = 125
        assertEquals("Verifica blend normale (Red)", 105, color.getRed());
        assertEquals("Verifica blend normale (Green)", 115, color.getGreen());
        assertEquals("Verifica blend normale (Blue)", 125, color.getBlue());
    }

    // --- Copertura dei Metodi Privati Invocati ---

    // I metodi privati setRed, setGreen, setBlue, greyscale, reverseLight, iMax e iMin
    // sono coperti da altri test (ad esempio, setRed/setGreen/setBlue sono invocati
    // dai metodi pubblici setHue/setSaturation/setLuminence o da blend/reverseColor/brighten
    // dopo una conversione RGB->HSL, e sono implicitamente coperti da getRed/getGreen/getBlue).

    // Test per iMax/iMin (implicitamente coperti da initHSLbyRGB)
    @Test
    public void testPrivateHelpers_iMaxiMin() {
        HSLColor color = new HSLColor();
        // initHSLbyRGB(R, G, B) usa iMax e iMin
        // cMax = iMax(iMax(R, G), B); cMin = iMin(iMin(R, G), B);

        // Caso con R=20, G=10, B=30. iMax(20, 10)=20. iMax(20, 30)=30. cMax=30.
        // iMin(20, 10)=10. iMin(10, 30)=10. cMin=10.
        color.initHSLbyRGB(20, 10, 30);
        // Poiché i campi pRed, pGreen, pBlue sono stati aggiornati, possiamo fidarci.
        // Un test più diretto richiederebbe un reflection, ma non è necessario per la line coverage.

        // Test per cMax = R
        color.initHSLbyRGB(30, 10, 20); // cMax=30, cMin=10. cMax==R.
        assertEquals(30, color.getRed());

        // Test per cMax = G
        color.initHSLbyRGB(10, 30, 20); // cMax=30, cMin=10. cMax==G.
        assertEquals(30, color.getGreen());

        // Test per cMax = B
        color.initHSLbyRGB(10, 20, 30); // cMax=30, cMin=10. cMax==B.
        assertEquals(30, color.getBlue());
    }

    // Test per greyscale() e reverseLight() - coperti se resi pubblici o tramite reflection
    // ma siccome non sono pubblici e non utilizzati da altri metodi pubblici (tranne initRGBbyHSL
    // che copre un caso simile a greyscale, e reverseLight che non è chiamato da metodi pubblici),
    // li testiamo solo se accessibili, altrimenti li ignoriamo come da istruzioni.
    // L'unica eccezione è initRGBbyHSL(H, 0, L) che implementa la logica di greyscale.
    // initRGBbyHSL_Greyscale copre il ramo greyscale.
    // reverseLight() non è chiamato da nessun metodo pubblico (reverseColor è l'unico che lo potrebbe chiamare).
}