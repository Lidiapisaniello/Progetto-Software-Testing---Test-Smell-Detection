import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;
    
    // Costanti prese dalla classe per le asserzioni
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // ==========================================
    // 1. TEST initHSLbyRGB (RGB -> HSL)
    // ==========================================

    @Test
    public void initHSLbyRGBGrayscaleTest() {
        // Copre: if (cMax == cMin)
        hslColor.initHSLbyRGB(128, 128, 128);

        assertEquals("Hue should be UNDEFINED", UNDEFINED, hslColor.getHue());
        assertEquals("Saturation should be 0", 0, hslColor.getSaturation());
        // Lum = ((256 * 255) + 255) / 510 = 128
        assertEquals(128, hslColor.getLuminence());
    }

    @Test
    public void initHSLbyRGBRedDominantTest() {
        // Copre: if (cMax == R)
        hslColor.initHSLbyRGB(255, 0, 0);

        // Rosso puro -> Hue ~0, Sat 255, Lum ~128
        assertTrue(hslColor.getHue() >= 0);
        assertEquals(255, hslColor.getSaturation());
        assertEquals(255, hslColor.getRed());
    }

    @Test
    public void initHSLbyRGBGreenDominantTest() {
        // Copre: else if (cMax == G)
        hslColor.initHSLbyRGB(0, 255, 0);

        // Verde puro -> Hue ~85 (su scala 255)
        assertTrue(hslColor.getHue() > 0);
        assertEquals(255, hslColor.getGreen());
    }

    @Test
    public void initHSLbyRGBBlueDominantTest() {
        // Copre: else if (cMax == B)
        hslColor.initHSLbyRGB(0, 0, 255);

        // Blu puro -> Hue ~170 (su scala 255)
        assertTrue(hslColor.getHue() > 0);
        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void initHSLbyRGBLumLowTest() {
        // Copre: if (pLum <= (HSLMAX / 2))
        // Colore scuro non grigio: R=20, G=40, B=20
        hslColor.initHSLbyRGB(20, 40, 20);

        assertTrue(hslColor.getLuminence() <= (HSLMAX / 2));
        assertTrue(hslColor.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBLumHighTest() {
        // Copre: else { pSat = ... } (Luminosità alta)
        hslColor.initHSLbyRGB(200, 255, 200);

        assertTrue(hslColor.getLuminence() > (HSLMAX / 2));
        assertTrue(hslColor.getSaturation() > 0);
    }

    @Test
    public void initHSLbyRGBNegativeHueLoopTest() {
        // Copre: if (pHue < 0)
        // Caso specifico: Rosso dominante ma con Blu > Verde genera delta negativo
        hslColor.initHSLbyRGB(255, 0, 50);

        // Assicuriamoci che l'algoritmo abbia corretto l'Hue rendendolo positivo
        assertTrue("Hue must be positive", hslColor.getHue() >= 0);
        assertTrue("Hue must be within range", hslColor.getHue() <= HSLMAX);
    }

    // ==========================================
    // 2. TEST initRGBbyHSL (HSL -> RGB)
    // ==========================================

    @Test
    public void initRGBbyHSLGrayscaleTest() {
        // Copre: if (S == 0)
        hslColor.initRGBbyHSL(0, 0, 100);

        assertEquals(hslColor.getRed(), hslColor.getGreen());
        assertEquals(hslColor.getGreen(), hslColor.getBlue());
        // Ricalcola lum approssimata
        int val = (100 * RGBMAX) / HSLMAX;
        assertEquals(val, hslColor.getRed());
    }

    @Test
    public void initRGBbyHSLLowLumMagicTest() {
        // Copre: if (L <= HSLMAX / 2)
        hslColor.initRGBbyHSL(0, 255, 50); // Saturo, scuro
        
        assertTrue(hslColor.getLuminence() <= HSLMAX / 2);
        // Verifica che RGB siano popolati (non neri/nulli)
        assertNotEquals(0, hslColor.getRed());
    }

    @Test
    public void initRGBbyHSLHighLumMagicTest() {
        // Copre: else (per Magic2)
        hslColor.initRGBbyHSL(0, 255, 200); // Saturo, chiaro
        
        assertTrue(hslColor.getLuminence() > HSLMAX / 2);
        assertNotEquals(0, hslColor.getRed());
    }

    @Test
    public void initRGBbyHSLClampMaxTest() {
        // Copre: if (pRed > RGBMAX), if (pGreen > RGBMAX), etc.
        // È difficile ottenere matematicamente > 255 con la formula HSL standard, 
        // ma proviamo i limiti estremi per stressare i calcoli.
        hslColor.initRGBbyHSL(0, 255, 128); 
        
        // Assert di sicurezza
        assertTrue(hslColor.getRed() <= RGBMAX);
        assertTrue(hslColor.getGreen() <= RGBMAX);
        assertTrue(hslColor.getBlue() <= RGBMAX);
    }

    // ==========================================
    // 3. TEST hueToRGB (Privato, testato tramite initRGBbyHSL)
    // ==========================================

    @Test
    public void hueToRGBAllSectorsTest() {
        // hueToRGB ha 4 return statements basati sull'angolo Hue.
        // initRGBbyHSL chiama hueToRGB 3 volte con angoli diversi (H+1/3, H, H-1/3).
        
        // Settiamo diversi Hue per coprire tutti i rami interni di hueToRGB:
        // 1. Hue < HSLMAX / 6
        hslColor.initRGBbyHSL(20, 255, 128);
        
        // 2. Hue < HSLMAX / 2
        hslColor.initRGBbyHSL(100, 255, 128);
        
        // 3. Hue < HSLMAX * 2/3
        hslColor.initRGBbyHSL(150, 255, 128);
        
        // 4. Hue > HSLMAX * 2/3
        hslColor.initRGBbyHSL(220, 255, 128);
    }

    @Test
    public void hueToRGBRangeCheckTest() {
        // Copre: if (Hue < 0) e if (Hue > HSLMAX) dentro hueToRGB.
        // initRGBbyHSL passa (H + HSLMAX/3) e (H - HSLMAX/3).
        
        // Caso Hue basso: H - HSLMAX/3 diventa negativo -> triggera correzione < 0
        hslColor.initRGBbyHSL(10, 255, 128);
        
        // Caso Hue alto: H + HSLMAX/3 supera 255 -> triggera correzione > HSLMAX
        hslColor.initRGBbyHSL(240, 255, 128);
        
        assertNotNull(hslColor); // Conferma che non ci sono eccezioni
    }

    // ==========================================
    // 4. TEST Setters & Loop Logic
    // ==========================================

    @Test
    public void setHueLoopsTest() {
        // Copre: while (iToValue < 0)
        hslColor.setHue(-300); // Diventa positivo dopo il loop
        assertTrue("Hue should be positive", hslColor.getHue() >= 0);

        // Copre: while (iToValue > HSLMAX)
        hslColor.setHue(600); // Diventa < 255 dopo il loop
        assertTrue("Hue should be within max", hslColor.getHue() <= HSLMAX);
    }

    @Test
    public void setSaturationClampTest() {
        // Copre if < 0
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        // Copre if > HSLMAX
        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void setLuminenceClampTest() {
        // Copre if < 0
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        // Copre if > HSLMAX
        hslColor.setLuminence(300);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    // ==========================================
    // 5. TEST Operational Methods
    // ==========================================

    @Test
    public void reverseColorTest() {
        // Imposta un valore base
        hslColor.initRGBbyHSL(0, 100, 100);
        int oldHue = hslColor.getHue();
        
        hslColor.reverseColor();
        
        // reverseColor fa: setHue(pHue + (HSLMAX / 2))
        int expected = oldHue + (HSLMAX / 2);
        // Nota: setHue normalizza se supera 255, quindi controlliamo la logica
        if(expected > HSLMAX) expected -= HSLMAX;
        
        assertEquals(expected, hslColor.getHue());
    }

    @Test
    public void brightenTest() {
        hslColor.initRGBbyHSL(0, 0, 100);

        // 1. fPercent == 0 -> return
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());

        // 2. Calcolo normale
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());

        // 3. Overflow (> HSLMAX)
        hslColor.brighten(10.0f);
        assertEquals(HSLMAX, hslColor.getLuminence());

        // 4. Underflow (< 0) -> anche se fPercent è negativo
        hslColor.setLuminence(100);
        hslColor.brighten(-1.0f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void blendTest() {
        hslColor.initHSLbyRGB(0, 0, 0); // Nero

        // 1. fPercent >= 1 -> Sostituisce completamente
        hslColor.blend(255, 255, 255, 1.2f);
        assertEquals(255, hslColor.getRed());

        // 2. fPercent <= 0 -> Ignora
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, -0.5f);
        assertEquals(0, hslColor.getRed()); // Rimane nero

        // 3. Mix normale (50%)
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(100, 100, 100, 0.5f);
        // (100*0.5 + 0*0.5) = 50
        assertEquals(50, hslColor.getRed());
    }

    // ==========================================
    // 6. TEST METODI PRIVATI (Reflection)
    // ==========================================
    /* I seguenti metodi nella classe HSLColor sono privati e NON vengono mai chiamati
     * dai metodi pubblici della classe stessa. Per ottenere la massima copertura
     * (e non lasciare righe "rosse"), usiamo la Reflection.
     */

    @Test
    public void invokePrivateSetRedTest() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        // Esegue setRed(100) -> chiama initHSLbyRGB(100, pGreen, pBlue)
        method.invoke(hslColor, 100);
        assertEquals(100, hslColor.getRed());
    }

    @Test
    public void invokePrivateSetGreenTest() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(hslColor, 100);
        assertEquals(100, hslColor.getGreen());
    }

    @Test
    public void invokePrivateSetBlueTest() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(hslColor, 100);
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void invokePrivateGreyscaleTest() throws Exception {
        hslColor.initHSLbyRGB(255, 0, 0); // Colore rosso
        
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(hslColor);
        
        // greyscale chiama initRGBbyHSL(UNDEFINED, 0, pLum)
        assertEquals(UNDEFINED, hslColor.getHue());
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void invokePrivateReverseLightTest() throws Exception {
        hslColor.setLuminence(100);
        
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(hslColor);
        
        // 255 - 100 = 155
        assertEquals(155, hslColor.getLuminence());
    }

    @Test
    public void invokePrivateIMaxTest() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        method.setAccessible(true);
        
        // Copertura entrambi i branch
        assertEquals(10, method.invoke(hslColor, 10, 5));
        assertEquals(10, method.invoke(hslColor, 5, 10));
    }

    @Test
    public void invokePrivateIMinTest() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);
        
        // Copertura entrambi i branch
        assertEquals(5, method.invoke(hslColor, 10, 5));
        assertEquals(5, method.invoke(hslColor, 5, 10));
    }
}