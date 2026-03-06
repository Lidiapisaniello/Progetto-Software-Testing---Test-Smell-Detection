/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alessandra"
Cognome: "Zotti"
Username: ales.zotti@studenti.unina.it
UserID: 245
Date: 22/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170; // Valore di pHue in scala di grigi

    /**
     * Test completi per il metodo initHSLbyRGB, coprendo:
     * 1. Colore primario (Rosso, Verde, Blu) per i rami di Hue.
     * 2. Caso di grigio (cMax == cMin) per Saturation e Hue.
     * 3. Saturazione con Lum <= HSLMAX/2.
     * 4. Saturazione con Lum > HSLMAX/2.
     * 5. Caso in cui pHue calcolato sia negativo (necessita di pHue = pHue + HSLMAX).
     * 6. Uso dei metodi iMax e iMin.
     */
    @Test
    public void testInitHSLbyRGB() {
        HSLColor color = new HSLColor();

        // 1. Colore primario Rosso (cMax == R), Saturation (Lum <= 127), pHue > 0
        color.initHSLbyRGB(255, 0, 0); // Rosso puro
        // I valori attesi sono approssimati a causa della logica di arrotondamento originale
        // e sono basati su HSL standard mapping [0-360] convertito a [0-255]
        assertEquals("Red Hue", 0, color.getHue(), 1); // 0/255
        assertEquals("Red Sat", HSLMAX, color.getSaturation(), 1); // 255/255
        assertEquals("Red Lum", 128, color.getLuminence(), 1); // (255+0) * 255 / 510 = 127.5 -> 128

        // 2. Colore primario Verde (cMax == G), Saturation (Lum > 127)
        color.initHSLbyRGB(0, 255, 0); // Verde puro
        assertEquals("Green Hue", 85, color.getHue(), 1); // 85 -> (255/3)
        assertEquals("Green Sat", HSLMAX, color.getSaturation(), 1);
        assertEquals("Green Lum", 128, color.getLuminence(), 1);

        // 3. Colore primario Blu (cMax == B)
        color.initHSLbyRGB(0, 0, 255); // Blu puro
        assertEquals("Blue Hue", 170, color.getHue(), 1); // 170 -> (2*255/3)
        assertEquals("Blue Sat", HSLMAX, color.getSaturation(), 1);
        assertEquals("Blue Lum", 128, color.getLuminence(), 1);

        // 4. Caso Grayscale (cMax == cMin) - Bianco
        color.initHSLbyRGB(255, 255, 255);
        assertEquals("White Hue", UNDEFINED, color.getHue());
        assertEquals("White Sat", 0, color.getSaturation());
        assertEquals("White Lum", HSLMAX, color.getLuminence());

        // 5. Caso Grayscale (cMax == cMin) - Grigio medio
        color.initHSLbyRGB(128, 128, 128);
        assertEquals("Grey Hue", UNDEFINED, color.getHue());
        assertEquals("Grey Sat", 0, color.getSaturation());
        assertEquals("Grey Lum", 128, color.getLuminence()); // 128

        // 6. Test pHue < 0 branch. Esempio: Magenta (R=255, B=255, G=0)
        // cMax=255, cMin=0, cMinus=255, cPlus=255. Lum=128, Sat=255.
        // RDelta=0. GDelta=255/6 = 42.5 -> 43. BDelta=0.
        // cMax==R: pHue = BDelta - GDelta = 0 - 43 = -43.
        // pHue = -43 + 255 = 212
        color.initHSLbyRGB(255, 0, 255);
        assertEquals("Magenta Hue (pHue < 0 branch)", 212, color.getHue(), 1); // 212 -> (5*255/6)
        assertEquals("Magenta Sat", HSLMAX, color.getSaturation(), 1);
        assertEquals("Magenta Lum", 128, color.getLuminence(), 1);
        
        // 7. Saturation con pLum > HSLMAX/2 (es. R=200, G=100, B=100, Lum > 127)
        // cMax=200, cMin=100. cMinus=100, cPlus=300.
        // pLum = (300*255 + 255) / 510 = 150.
        // pSat = (100*255 + 0.5) / (510 - 300) = 25500 / 210 approx 121.
        color.initHSLbyRGB(200, 100, 100);
        assertEquals("Lum > 127 Sat branch", 121, color.getSaturation(), 1);
    }
    
    // ---
    
    /**
     * Test completi per il metodo initRGBbyHSL, coprendo:
     * 1. Caso di grigio (Sat == 0).
     * 2. Caso con Lum <= HSLMAX/2 (calcolo Magic2).
     * 3. Caso con Lum > HSLMAX/2 (calcolo Magic2).
     * 4. Richiama hueToRGB per tutti i suoi rami.
     * 5. Clip pRed, pGreen, pBlue a RGBMAX.
     */
    @Test
    public void testInitRGBbyHSL() {
        HSLColor color = new HSLColor();

        // 1. Caso Greyscale (S=0) - Grigio medio (L=128)
        color.initRGBbyHSL(0, 0, 128);
        assertEquals("Grayscale R", 128, color.getRed());
        assertEquals("Grayscale G", 128, color.getGreen());
        assertEquals("Grayscale B", 128, color.getBlue());

        // 2. Lum <= HSLMAX/2 (es. L=64, S=255) - Rosso scuro
        // Magic2 = (64 * (255 + 255) + 127) / 255 = 128.
        // Magic1 = 2*64 - 128 = 0.
        color.initRGBbyHSL(0, HSLMAX, 64);
        assertEquals("Dark Red R", 128, color.getRed(), 1); // 128
        assertEquals("Dark Red G", 0, color.getGreen(), 1); // 0
        assertEquals("Dark Red B", 0, color.getBlue(), 1); // 0

        // 3. Lum > HSLMAX/2 (es. L=192, S=255) - Rosso chiaro
        // Magic2 = 192 + 255 - ((192*255 + 127) / 255) = 447 - 192 = 255.
        // Magic1 = 2*192 - 255 = 129.
        color.initRGBbyHSL(0, HSLMAX, 192);
        assertEquals("Light Red R", RGBMAX, color.getRed(), 1); // 255
        assertEquals("Light Red G", 128, color.getGreen(), 1); // 128
        assertEquals("Light Red B", 128, color.getBlue(), 1); // 128

        // 4. Test HUE per tutti i rami di hueToRGB (H=0, H=42, H=85, H=127, H=170, H=212, H=255)
        // Usiamo un colore saturo e medio (L=128, S=255). Magic1=1, Magic2=255.
        // Hue 0 (Rosso)
        color.initRGBbyHSL(0, HSLMAX, 128); // H=0, H+85, H-85 -> 0, 85, 170
        assertEquals("Hue 0 R (Hue < HSLMAX/6)", 255, color.getRed(), 1);
        assertEquals("Hue 0 G", 0, color.getGreen(), 1);
        assertEquals("Hue 0 B (Hue < 0 branch)", 0, color.getBlue(), 1);
        
        // Hue 42 (Giallo-Arancio)
        color.initRGBbyHSL(42, HSLMAX, 128); // H=42, H+85=127, H-85=-43 -> 42, 127, 212
        assertEquals("Hue 42 R (Hue < HSLMAX/6)", 255, color.getRed(), 1);
        assertEquals("Hue 42 G (Hue < HSLMAX/2)", 255, color.getGreen(), 1);
        assertEquals("Hue 42 B (Hue < 2*HSLMAX/3)", 0, color.getBlue(), 1);

        // Hue 85 (Verde)
        color.initRGBbyHSL(85, HSLMAX, 128); // H=85, H+85=170, H-85=0
        assertEquals("Hue 85 R (Hue < 2*HSLMAX/3)", 0, color.getRed(), 1);
        assertEquals("Hue 85 G (Hue < HSLMAX/2)", 255, color.getGreen(), 1);
        assertEquals("Hue 85 B (Hue < HSLMAX/6)", 0, color.getBlue(), 1);
        
        // Hue 127 (Ciano)
        color.initRGBbyHSL(127, HSLMAX, 128); // H=127, H+85=212, H-85=42
        assertEquals("Hue 127 R (Hue > 2*HSLMAX/3)", 0, color.getRed(), 1);
        assertEquals("Hue 127 G (Hue < 2*HSLMAX/3)", 255, color.getGreen(), 1);
        assertEquals("Hue 127 B (Hue < HSLMAX/6)", 255, color.getBlue(), 1);
        
        // 5. Test clipping a RGBMAX. Usiamo un Lum molto alto (L=255, S=255)
        color.initRGBbyHSL(0, HSLMAX, HSLMAX);
        assertEquals("R clip to RGBMAX", RGBMAX, color.getRed());
        assertEquals("G clip to RGBMAX", RGBMAX, color.getGreen());
        assertEquals("B clip to RGBMAX", RGBMAX, color.getBlue());
    }

    // ---
    
    /**
     * Test per i getter, assicurandosi che i valori iniziali siano corretti
     * e che i valori impostati siano riflessi.
     */
    @Test
    public void testGetters() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30); // Inizializza pRed=10, pGreen=20, pBlue=30

        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
        
        // I valori HSL sono calcolati. Assicuriamoci che i getter ritornino i valori correnti.
        assertTrue(color.getHue() != 0); // Non dovrebbe essere 0
        assertTrue(color.getSaturation() != 0); // Non dovrebbe essere 0
        assertTrue(color.getLuminence() != 0); // Non dovrebbe essere 0
    }

    // ---
    
    /**
     * Test per i setter HSL, coprendo i vincoli (0 <= HSL <= 255)
     * e i casi di wrap-around per setHue.
     */
    @Test
    public void testSettersHSL() {
        HSLColor color = new HSLColor();
        // Inizializzazione di base per avere pSat e pLum definiti
        color.initHSLbyRGB(128, 128, 128); // H=170 (UNDEFINED), S=0, L=128

        // setHue: wrap-around negativi
        color.setHue(-10); // -10 + 255 = 245
        assertEquals(245, color.getHue());
        // setHue: wrap-around positivi
        color.setHue(260); // 260 - 255 = 5
        assertEquals(5, color.getHue());
        // setHue: valore intermedio valido
        color.setHue(100);
        assertEquals(100, color.getHue());
        
        // setSaturation: sotto limite
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
        // setSaturation: sopra limite
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation());
        // setSaturation: valore intermedio valido
        color.setSaturation(150);
        assertEquals(150, color.getSaturation());

        // setLuminence: sotto limite
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
        // setLuminence: sopra limite
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence());
        // setLuminence: valore intermedio valido
        color.setLuminence(150);
        assertEquals(150, color.getLuminence());
    }

    // ---
    
    /**
     * Test dei metodi setRGB (privati, testati indirettamente).
     * setRed/setGreen/setBlue chiamano initHSLbyRGB, testato in testInitHSLbyRGB.
     * Ci concentriamo sull'aggiornamento corretto del singolo componente RGB.
     */
    @Test
    public void testSettersRGB() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100); // R=100, G=100, B=100

        // setRed (indiretto)
        // La chiamata a setRed(200) aggiorna pRed a 200 e ricalcola HSL.
        try {
            java.lang.reflect.Method setRed = HSLColor.class.getDeclaredMethod("setRed", int.class);
            setRed.setAccessible(true);
            setRed.invoke(color, 200);
            assertEquals("setRed should update pRed", 200, color.getRed());
            assertNotEquals("setRed should re-calculate HSL", 0, color.getSaturation());
        } catch (Exception e) {
            fail("Reflection error on setRed: " + e.getMessage());
        }

        // setGreen (indiretto)
        // setGreen(150) aggiorna pGreen a 150 e ricalcola HSL.
        try {
            java.lang.reflect.Method setGreen = HSLColor.class.getDeclaredMethod("setGreen", int.class);
            setGreen.setAccessible(true);
            setGreen.invoke(color, 150);
            assertEquals("setGreen should update pGreen", 150, color.getGreen());
        } catch (Exception e) {
            fail("Reflection error on setGreen: " + e.getMessage());
        }
        
        // setBlue (indiretto)
        // setBlue(50) aggiorna pBlue a 50 e ricalcola HSL.
        try {
            java.lang.reflect.Method setBlue = HSLColor.class.getDeclaredMethod("setBlue", int.class);
            setBlue.setAccessible(true);
            setBlue.invoke(color, 50);
            assertEquals("setBlue should update pBlue", 50, color.getBlue());
        } catch (Exception e) {
            fail("Reflection error on setBlue: " + e.getMessage());
        }
    }

    // ---
    
    /**
     * Test metodi pubblici di trasformazione del colore.
     */
    @Test
    public void testColorTransformations() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 0, 0); // Rosso medio: H=0, S=255, L=64

        // reverseColor: HUE + 127.5 -> + 128 (pHue è int)
        // H=0 -> 128
        color.reverseColor();
        assertEquals("Reverse Color Hue", 128, color.getHue(), 1); // 128 -> (1/2 HSLMAX)

        // reverseLight (privato, testato indirettamente)
        // L=64 -> 255 - 64 = 191
        try {
            java.lang.reflect.Method reverseLight = HSLColor.class.getDeclaredMethod("reverseLight");
            reverseLight.setAccessible(true);
            reverseLight.invoke(color);
            assertEquals("Reverse Light Lum", 191, color.getLuminence()); // 191
        } catch (Exception e) {
            fail("Reflection error on reverseLight: " + e.getMessage());
        }
        
        // greyscale (privato, testato indirettamente)
        // L rimane 191, S=0, H=UNDEFINED.
        try {
            java.lang.reflect.Method greyscale = HSLColor.class.getDeclaredMethod("greyscale");
            greyscale.setAccessible(true);
            greyscale.invoke(color);
            assertEquals("Greyscale Saturation", 0, color.getSaturation());
            assertEquals("Greyscale Hue", UNDEFINED, color.getHue());
            assertEquals("Greyscale Luminance", 191, color.getLuminence());
            // Verifica che RGB sia aggiornato (191 * 255 / 255 = 191)
            assertEquals("Greyscale R", 191, color.getRed()); 
        } catch (Exception e) {
            fail("Reflection error on greyscale: " + e.getMessage());
        }
    }

    // ---
    
    /**
     * Test per il metodo brighten.
     */
    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100); // L=100

        // fPercent = 0
        color.brighten(0.0f);
        assertEquals("Brighten 0%", 100, color.getLuminence());

        // fPercent < 0 (L < 0) -> L = 0
        color.brighten(-10.0f); // 100 * -10 = -1000 -> 0
        assertEquals("Brighten negative", 0, color.getLuminence());
        
        // Reset L
        color.initHSLbyRGB(100, 100, 100);

        // fPercent > 1 (L > HSLMAX) -> L = HSLMAX
        color.brighten(10.0f); // 100 * 10 = 1000 -> 255
        assertEquals("Brighten positive clip", HSLMAX, color.getLuminence());
        
        // Reset L
        color.initHSLbyRGB(100, 100, 100);
        
        // fPercent intermedio valido
        color.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals("Brighten valid", 150, color.getLuminence());
    }

    // ---
    
    /**
     * Test per il metodo blend.
     */
    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100); // R=100, G=100, B=100

        // fPercent >= 1: initHSLbyRGB(R, G, B)
        color.blend(255, 0, 0, 1.0f);
        assertEquals("Blend 100% R", 255, color.getRed());
        assertEquals("Blend 100% G", 0, color.getGreen());
        assertEquals("Blend 100% B", 0, color.getBlue());
        
        // fPercent <= 0: return
        color.blend(0, 255, 0, 0.0f);
        assertEquals("Blend 0% R (should remain 255)", 255, color.getRed());
        
        // fPercent intermedio (es. 0.5)
        // newR = (10 * 0.5) + (255 * 0.5) = 5 + 127.5 = 132.5 -> 132 (se si parte da 255,0,0)
        // Ma, partiamo da R=100, G=100, B=100
        color.initHSLbyRGB(100, 100, 100);
        // Blend con 200, 0, 0 con 0.5f
        // newR = (200 * 0.5) + (100 * 0.5) = 100 + 50 = 150
        // newG = (0 * 0.5) + (100 * 0.5) = 50
        // newB = (0 * 0.5) + (100 * 0.5) = 50
        color.blend(200, 0, 0, 0.5f);
        assertEquals("Blend 50% R", 150, color.getRed());
        assertEquals("Blend 50% G", 50, color.getGreen());
        assertEquals("Blend 50% B", 50, color.getBlue());
    }
}