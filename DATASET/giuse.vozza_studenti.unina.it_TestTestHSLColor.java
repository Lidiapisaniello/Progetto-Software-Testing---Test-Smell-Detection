/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giuse.vozza@studenti.unina.it
UserID: 511
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class HSLColorTest {

    // --- Helper per invocare metodi privati (necessario per il 100% coverage su codice morto) ---
    private void invokePrivateMethod(Object instance, String name, Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        method.invoke(instance, args);
    }

    // --- Test Logica Principale: RGB to HSL ---

    @Test
    public void initHSLbyRGBGreyScaleTest() {
        HSLColor color = new HSLColor();
        // R=G=B innesca il ramo (cMax == cMin)
        color.initHSLbyRGB(100, 100, 100);
        
        assertEquals("Saturazione dovrebbe essere 0 per grigio", 0, color.getSaturation());
        assertEquals("Luminosità calcolata errata", 100, color.getLuminence());
        // Hue è UNDEFINED (170) nel codice
        assertEquals("Hue dovrebbe essere UNDEFINED", 170, color.getHue());
    }

    @Test
    public void initHSLbyRGBLowLuminenceTest() {
        HSLColor color = new HSLColor();
        // Lum <= HSLMAX/2 innesca il primo ramo di calcolo Sat
        // R=50, G=0, B=0 -> Max=50, Min=0 -> Lum = 25. 25 <= 127.
        color.initHSLbyRGB(50, 0, 0);
        
        assertEquals(50, color.getRed()); // Verifica setup
        assertTrue(color.getLuminence() <= 127);
        assertTrue(color.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBHighLuminenceTest() {
        HSLColor color = new HSLColor();
        // Lum > HSLMAX/2 innesca il ramo 'else' di calcolo Sat
        // R=255, G=255, B=200 -> Max=255, Min=200.
        color.initHSLbyRGB(255, 255, 200);
        
        assertTrue(color.getLuminence() > 127);
    }

    @Test
    public void initHSLbyRGBRedMaxWithNegativeHueLogicTest() {
        HSLColor color = new HSLColor();
        // Caso: cMax == R. 
        // Per avere Hue < 0 (che attiva il fix pHue += HSLMAX), serve G < B.
        // R=200, G=10, B=100.
        color.initHSLbyRGB(200, 10, 100);
        
        assertTrue("Hue non dovrebbe essere negativo dopo la correzione", color.getHue() >= 0);
        assertEquals(200, color.getRed());
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        HSLColor color = new HSLColor();
        // Caso: cMax == G
        color.initHSLbyRGB(50, 200, 50);
        
        // Hue formula per Green max
        assertTrue(color.getHue() > 0);
        assertEquals(200, color.getGreen());
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        HSLColor color = new HSLColor();
        // Caso: cMax == B
        color.initHSLbyRGB(50, 50, 200);
        
        assertTrue(color.getHue() > 0);
        assertEquals(200, color.getBlue());
    }

    // --- Test Logica Principale: HSL to RGB ---

    @Test
    public void initRGBbyHSLGreyScaleTest() {
        HSLColor color = new HSLColor();
        // S = 0 innesca il ramo grigio
        color.initRGBbyHSL(100, 0, 128);
        
        assertEquals(color.getRed(), color.getGreen());
        assertEquals(color.getRed(), color.getBlue());
        assertEquals(128, color.getRed()); // Approx mapping
    }

    @Test
    public void initRGBbyHSLLowLuminenceTest() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2
        color.initRGBbyHSL(0, 255, 100);
        
        assertEquals(0, color.getHue());
        assertEquals(100, color.getLuminence());
        // Verifica che i valori RGB siano stati popolati
        assertTrue(color.getRed() > 0); 
    }

    @Test
    public void initRGBbyHSLHighLuminenceTest() {
        HSLColor color = new HSLColor();
        // L > HSLMAX/2
        color.initRGBbyHSL(0, 255, 200);
        
        assertEquals(200, color.getLuminence());
    }

    @Test
    public void initRGBbyHSLClampingTest() {
        HSLColor color = new HSLColor();
        // Questo test cerca di forzare i rami 'if (pRed > RGBMAX)'
        // È matematicamente difficile con gli interi standard 0-255, 
        // ma passiamo valori limite per sicurezza.
        color.initRGBbyHSL(0, 255, 255); // Bianco
        
        assertTrue(color.getRed() <= 255);
        assertTrue(color.getGreen() <= 255);
        assertTrue(color.getBlue() <= 255);
    }

    // --- Test Copertura Helper Privato hueToRGB (tramite initRGBbyHSL) ---
    // hueToRGB viene chiamato 3 volte per ogni initRGBbyHSL con offset diversi.
    // Dobbiamo assicurarci di colpire tutti i return interni.

    @Test
    public void hueToRGBBranchCoverageTest() {
        HSLColor color = new HSLColor();
        
        // Usiamo vari Hue per colpire i diversi 'if' dentro hueToRGB
        // H=0   -> colpisce < 1/6 e wrappa negativo
        // H=85  -> colpisce < 1/2
        // H=170 -> colpisce < 2/3
        
        color.initRGBbyHSL(0, 255, 128);   // Hue rosso
        color.initRGBbyHSL(85, 255, 128);  // Hue verde (circa 1/3 max)
        color.initRGBbyHSL(170, 255, 128); // Hue blu (circa 2/3 max)
        color.initRGBbyHSL(250, 255, 128); // Hue alto (wrappa positivo)
    }

    // --- Test Getters e Setters Pubblici ---

    @Test
    public void setHueLoopTest() {
        HSLColor color = new HSLColor();
        
        // Test while(iToValue < 0)
        color.setHue(-300); 
        assertTrue(color.getHue() >= 0);
        
        // Test while(iToValue > HSLMAX)
        color.setHue(600);
        assertTrue(color.getHue() <= 255);
    }

    @Test
    public void setSaturationClampTest() {
        HSLColor color = new HSLColor();
        
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
        
        color.setSaturation(300);
        assertEquals(255, color.getSaturation()); // HSLMAX
    }

    @Test
    public void setLuminenceClampTest() {
        HSLColor color = new HSLColor();
        
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
        
        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
    }

    // --- Test Metodi di Utilità Pubblici ---

    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Red hue = 0
        int oldHue = color.getHue(); // 0
        
        color.reverseColor();
        // 0 + 127 (HSLMAX/2) = 127
        assertEquals(oldHue + 127, color.getHue());
    }

    @Test
    public void brightenNormalTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100); // Lum = 100
        
        color.brighten(1.5f); // +50%
        assertEquals(150, color.getLuminence());
    }

    @Test
    public void brightenZeroPercentTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        
        color.brighten(0f); // return immediato
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void brightenClampTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 100, 100);
        
        color.brighten(10.0f); // Overflow
        assertEquals(255, color.getLuminence());
        
        color.brighten(-5.0f); // Underflow (diventa negativo nel calcolo int)
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void blendLimitsTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        // fPercent >= 1 -> prende il nuovo colore interamente
        color.blend(255, 255, 255, 1.5f);
        assertEquals(255, color.getRed());
        
        // fPercent <= 0 -> non fa nulla (rimane bianco da sopra)
        color.blend(0, 0, 0, -0.5f);
        assertEquals(255, color.getRed());
    }

    @Test
    public void blendMixTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Nero
        
        // Blend 50% con bianco -> Grigio (127)
        color.blend(255, 255, 255, 0.5f);
        
        // Calcolo: (255 * 0.5) + (0 * 0.5) = 127
        assertEquals(127, color.getRed());
    }

    // --- Test Metodi Privati (Reflection) ---
    // Questi metodi sono "dead code" (mai chiamati internamente dalla classe),
    // ma per il 100% coverage delle istruzioni dobbiamo testarli.

    @Test
    public void privateSetRedTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setRed", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, color.getRed());
    }

    @Test
    public void privateSetGreenTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setGreen", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, color.getGreen());
    }

    @Test
    public void privateSetBlueTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        
        invokePrivateMethod(color, "setBlue", new Class[]{int.class}, new Object[]{255});
        assertEquals(255, color.getBlue());
    }

    @Test
    public void privateReverseLightTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(50, 50, 50); // Lum 50
        
        // reverseLight: setLuminence(HSLMAX - pLum) -> 255 - 50 = 205
        invokePrivateMethod(color, "reverseLight", null, null);
        assertEquals(205, color.getLuminence());
    }

    @Test
    public void privateGreyscaleTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Rosso saturo
        
        invokePrivateMethod(color, "greyscale", null, null);
        // greyscale chiama initRGBbyHSL(UNDEFINED, 0, pLum)
        assertEquals(0, color.getSaturation());
    }
    
    @Test
    public void privateIMaxIMinCoverageTest() throws Exception {
        // Anche se iMax/iMin sono usati da initHSL, testiamo i rami esplicitamente
        // per garantire che a>b e b>a siano coperti al 100% in isolamento
        HSLColor color = new HSLColor();
        Method iMax = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        iMax.setAccessible(true);
        
        assertEquals(10, iMax.invoke(color, 10, 5)); // a > b
        assertEquals(10, iMax.invoke(color, 5, 10)); // b > a (else)

        Method iMin = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        iMin.setAccessible(true);
        
        assertEquals(5, iMin.invoke(color, 5, 10)); // a < b
        assertEquals(5, iMin.invoke(color, 10, 5)); // else
    }
}
						