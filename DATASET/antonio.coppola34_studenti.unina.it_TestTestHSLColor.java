/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonio"
Cognome: "Coppola"
Username: antonio.coppola34@studenti.unina.it
UserID: 203
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    // Costanti interne della classe HSLColor (per riferimento nei test)
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    // Tolleranza per il confronto degli interi nei test di conversione.
    private final static int DELTA = 1;


    // --- Test di Conversione RGB -> HSL (initHSLbyRGB) ---

    @Test
    public void testInitHSLbyRGB_Red() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, 0, 0); // Rosso puro (255, 0, 0)

        assertEquals(RGBMAX, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());

        assertEquals(0, color.getHue(), DELTA);
        assertEquals(HSLMAX, color.getSaturation(), DELTA);
        assertEquals(HSLMAX / 2, color.getLuminence(), DELTA);
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, RGBMAX, 0); // Verde puro (0, 255, 0)

        // HSL atteso: H=85 (255/3), S=255, L=128
        assertEquals(85, color.getHue(), DELTA);
        assertEquals(HSLMAX, color.getSaturation(), DELTA);
        assertEquals(HSLMAX / 2, color.getLuminence(), DELTA);
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, RGBMAX); // Blu puro (0, 0, 255)

        // HSL atteso: H=170 (2*255/3), S=255, L=128
        assertEquals(170, color.getHue(), DELTA);
        assertEquals(HSLMAX, color.getSaturation(), DELTA);
        assertEquals(HSLMAX / 2, color.getLuminence(), DELTA);
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Nero (0, 0, 0)

        // Greyscale (cMax == cMin)
        assertEquals(UNDEFINED, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, RGBMAX, RGBMAX); // Bianco (255, 255, 255)

        // Correzione sintassi JUnit (da assertEquals(HSLMAX) a assertEquals(expected, actual))
        assertEquals(UNDEFINED, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(HSLMAX, color.getLuminence()); // L=255
    }

    @Test
    public void testInitHSLbyRGB_MidColor_LowLum_SatIfLumLessHalf() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 50, 25);
        assertEquals(63, color.getLuminence());
        assertEquals(153, color.getSaturation(), DELTA);
    }

    @Test
    public void testInitHSLbyRGB_MidColor_HighLum_SatIfLumGreaterHalf() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(220, 120, 100);
        assertEquals(160, color.getLuminence());
        assertEquals(161, color.getSaturation(), DELTA);
    }

    // ------------------------------------------------------------------------

    // --- Test di Conversione HSL -> RGB (initRGBbyHSL) ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(50, 0, 128); // S=0, L=128 (Grigio medio)

        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_MidLum_Coverage_Branch1_3() {
        HSLColor color = new HSLColor();
        // H=10, S=128, L=64
        color.initRGBbyHSL(10, 128, 64);
        
        // Correzione aspettative (riga 124): 96 è il valore corretto calcolato.
        assertEquals(96, color.getRed(), DELTA); 
        assertEquals(47, color.getGreen(), DELTA); // Aggiunto per maggiore copertura
        assertEquals(32, color.getBlue(), DELTA);  // Aggiunto per maggiore copertura
    }

    @Test
    public void testInitRGBbyHSL_HighLum_Coverage_Branch2_4() {
        HSLColor color = new HSLColor();
        // H=140, S=128, L=191
        color.initRGBbyHSL(140, 128, 191);
        
        // Correzione aspettative (riga 137): 159 è il valore corretto calcolato.
        assertEquals(159, color.getRed(), DELTA); 
        assertEquals(205, color.getGreen(), DELTA); // Aggiunto per maggiore copertura
        assertEquals(223, color.getBlue(), DELTA);  // Aggiunto per maggiore copertura
    }
    
    // ------------------------------------------------------------------------

    // --- Test Getter/Setter ---

    @Test
    public void testSetters_RangeChecks_and_Conversion() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);

        // Test setHue con wrapping
        color.setHue(HSLMAX + 10);
        assertEquals(10, color.getHue());
        color.setHue(-10);
        assertEquals(245, color.getHue());

        // Test setSaturation con clamping
        color.setSaturation(HSLMAX + 10);
        assertEquals(HSLMAX, color.getSaturation());
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());

        // Test setLuminence con clamping
        color.setLuminence(HSLMAX + 10);
        assertEquals(HSLMAX, color.getLuminence());
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testGreyscale_IndirectCoverage() {
        HSLColor color = new HSLColor();
        // Luminance calcolata per (100, 50, 0) è 50.
        color.initHSLbyRGB(100, 50, 0); 

        // setSaturation(0) chiama initRGBbyHSL(pHue, 0, pLum) con L=50.
        // La conversione HSL(S=0, L=50) -> RGB è (50*255)/255 = 50.
        color.setSaturation(0); 

        // Correzione aspettativa (riga 180): 50 è il valore corretto.
        assertEquals(0, color.getSaturation());
        assertEquals(50, color.getRed(), DELTA);
        assertEquals(50, color.getGreen(), DELTA);
        assertEquals(50, color.getBlue(), DELTA);
    }
    
    // ------------------------------------------------------------------------

    // --- Test Metodi di Manipolazione ---

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, 0, 0); // Rosso (H=0)

        color.reverseColor();
        assertEquals(HSLMAX / 2, color.getHue(), DELTA);
        
        color.reverseColor();
        assertEquals(254, color.getHue(), DELTA); 
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 0, 0); // Colore iniziale (L=64)

        // Aumenta Luminosità
        color.brighten(1.5f);
        assertEquals(96, color.getLuminence());

        // Clamping HSLMAX
        color.brighten(10.0f);
        assertEquals(HSLMAX, color.getLuminence());

        // Clamping 0
        color.initHSLbyRGB(128, 0, 0);
        color.brighten(-1.0f);
        assertEquals(0, color.getLuminence());

        // No-op (fPercent == 0)
        color.initHSLbyRGB(10, 10, 10);
        color.brighten(0.0f);
        assertEquals(10, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10); // Colore iniziale
        
        int R = 200, G = 200, B = 200; // Colore target

        // Blend al 50%
        float fPercent = 0.5f;
        int newR = (int) ((R * fPercent) + (10 * (1.0 - fPercent))); // 105
        
        color.blend(R, G, B, fPercent);

        assertEquals(newR, color.getRed(), DELTA);

        // Clamping (Blend al 100%)
        color.blend(R, G, B, 1.0f);
        assertEquals(R, color.getRed());
        
        // No-op (Blend allo 0%)
        color.initHSLbyRGB(10, 10, 10);
        color.blend(R, G, B, 0.0f);
        assertEquals(10, color.getRed());
        
        // Blend > 1.0 (clamping implicito a 1.0f nel metodo blend)
        color.blend(R, G, B, 1.5f);
        assertEquals(R, color.getRed());
    }
}