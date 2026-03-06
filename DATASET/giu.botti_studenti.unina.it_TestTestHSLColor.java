/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giu.botti@studenti.unina.it
UserID: 604
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class TestHSLColor {
    
    private HSLColor hslColor;

    @BeforeClass
    public static void setUpClass() {
        // Setup statico
    }

    @AfterClass
    public static void tearDownClass() {
        // Teardown statico
    }

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    @After
    public void tearDown() {
        hslColor = null;
    }

    // --- Helper per Reflection (Per testare metodi privati) ---
    private void invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(hslColor, args);
    }

    private void invokePrivateMethodNoArgs(String methodName) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(hslColor);
    }

    // --- TEST CONVERSIONE RGB -> HSL ---

    @Test
    public void initHSLbyRGBRedMaxTest() {
        // Caso: Max è Red, Min è Blue (o Green). R=255, G=0, B=0
        // Calcolo Hue: BDelta - GDelta.
        hslColor.initHSLbyRGB(255, 0, 0);
        assertEquals(0, hslColor.getHue()); // Rosso puro è Hue 0
        assertEquals(255, hslColor.getSaturation());
        assertEquals(128, hslColor.getLuminence()); // (255+0 + 255)/(2*255) approx 128
        
        // Verifica valori RGB memorizzati
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen());
        assertEquals(0, hslColor.getBlue());
    }

    @Test
    public void initHSLbyRGBGreenMaxTest() {
        // Caso: Max è Green. R=0, G=255, B=0
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue dovrebbe essere circa 85 (HSLMAX / 3)
        assertEquals(85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void initHSLbyRGBBlueMaxTest() {
        // Caso: Max è Blue. R=0, G=0, B=255
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue dovrebbe essere circa 170 (2 * HSLMAX / 3)
        assertEquals(170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void initHSLbyRGBGreyscaleTest() {
        // Caso cMax == cMin (Grigio). Hue diventa UNDEFINED (170), Sat 0
        hslColor.initHSLbyRGB(100, 100, 100);
        assertEquals(170, hslColor.getHue()); // UNDEFINED
        assertEquals(0, hslColor.getSaturation());
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void initHSLbyRGBNegativeHueFixTest() {
        // Caso in cui Hue calcolato è negativo e deve essere corretto (+HSLMAX)
        // Succede quando R è Max e G < B
        hslColor.initHSLbyRGB(255, 0, 50); 
        // R=255, G=0, B=50. 
        // Max=R. Hue = BDelta - GDelta.
        // Poiché G è 0, GDelta è alto. BDelta è più basso. Risultato negativo.
        assertTrue(hslColor.getHue() > 0); 
    }

    @Test
    public void initHSLbyRGBLumLesserThanHalfTest() {
        // Copre if (pLum <= (HSLMAX / 2))
        // Usiamo un colore scuro
        hslColor.initHSLbyRGB(50, 0, 0);
        // Lum = (50+255)/(510) -> bassa
        assertTrue(hslColor.getLuminence() <= 127);
        assertEquals(255, hslColor.getSaturation());
    }
    
    @Test
    public void initHSLbyRGBLumGreaterThanHalfTest() {
        // Copre else di (pLum <= (HSLMAX / 2))
        // Usiamo un colore chiaro ma saturo
        hslColor.initHSLbyRGB(255, 200, 200);
        assertTrue(hslColor.getLuminence() > 127);
    }

    // --- TEST CONVERSIONE HSL -> RGB ---

    @Test
    public void initRGBbyHSLGreyscaleTest() {
        // Se Saturation == 0
        hslColor.initRGBbyHSL(0, 0, 100);
        assertEquals(100, hslColor.getRed());
        assertEquals(100, hslColor.getGreen());
        assertEquals(100, hslColor.getBlue());
    }

    @Test
    public void initRGBbyHSLLumLesserThanHalfTest() {
        // L <= HSLMAX/2. Hue=0 (Rosso), Sat=255, Lum=100
        hslColor.initRGBbyHSL(0, 255, 100);
        // Verifica che RGB siano calcolati (non grigi)
        assertNotEquals(hslColor.getRed(), hslColor.getGreen());
    }

    @Test
    public void initRGBbyHSLLumGreaterThanHalfTest() {
        // L > HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 200);
        assertNotEquals(hslColor.getRed(), hslColor.getGreen());
    }

    @Test
    public void initRGBbyHSLClampTest() {
        // Forza valori che potrebbero superare RGBMAX se non controllati
        // Magic numbers alti
        hslColor.initRGBbyHSL(0, 255, 255); // Bianco
        assertEquals(255, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        assertEquals(255, hslColor.getBlue());
    }

    // --- TEST hueToRGB (Copertura Rami Interni) ---
    // Questo metodo è privato e chiamato da initRGBbyHSL. 
    // Dobbiamo stimolare i vari rami modificando Hue di input.
    // initRGBbyHSL chiama hueToRGB con: (H + 85), (H), (H - 85).

    @Test
    public void hueToRGBRangeCheckTest() {
        // 1. Hue < 0 (viene corretto in hueToRGB quando passiamo H=0 -> H-85)
        hslColor.initRGBbyHSL(0, 255, 128);
        
        // 2. Hue > HSLMAX (viene corretto quando passiamo H=250 -> H+85 > 255)
        hslColor.initRGBbyHSL(250, 255, 128);
    }

    @Test
    public void hueToRGBBranchesTest() {
        // Copre:
        // Hue < (HSLMAX / 6) -> (42)
        // Hue < (HSLMAX / 2) -> (127)
        // Hue < (HSLMAX * 2 / 3) -> (170)
        
        // Testando con vari Hue copriamo indirettamente i return
        hslColor.initRGBbyHSL(20, 255, 128);  // Ramo < 1/6
        hslColor.initRGBbyHSL(85, 255, 128);  // Ramo < 1/2
        hslColor.initRGBbyHSL(150, 255, 128); // Ramo < 2/3
        hslColor.initRGBbyHSL(200, 255, 128); // Ramo else (return mag1)
    }

    // --- TEST GETTERS & SETTERS (Boundary & Wrapping) ---

    @Test
    public void setHueWrapTest() {
        // While < 0
        hslColor.setHue(-50); 
        // -50 + 255 = 205
        assertEquals(205, hslColor.getHue());

        // While > 255
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
        
        // Valid
        hslColor.setHue(100);
        assertEquals(100, hslColor.getHue());
    }

    @Test
    public void setSaturationClampTest() {
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
        
        hslColor.setSaturation(100);
        assertEquals(100, hslColor.getSaturation());
    }

    @Test
    public void setLuminenceClampTest() {
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.setLuminence(100);
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void reverseColorTest() {
        hslColor.setHue(0);
        hslColor.reverseColor();
        // 0 + 255/2 = 127
        assertEquals(127, hslColor.getHue());
    }

    // --- TEST METODI LOGICI (Blend, Brighten) ---

    @Test
    public void brightenZeroTest() {
        hslColor.setLuminence(100);
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void brightenNormalTest() {
        hslColor.setLuminence(100);
        hslColor.brighten(1.5f); // +50%
        assertEquals(150, hslColor.getLuminence());
    }
    
    @Test
    public void brightenClampTest() {
        hslColor.setLuminence(200);
        hslColor.brighten(2.0f); // -> 400 -> Clamped 255
        assertEquals(255, hslColor.getLuminence());
        
        hslColor.brighten(-1.0f); // -> negativo -> Clamped 0
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void blendBoundariesTest() {
        // fPercent >= 1 -> initRGB puro
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 1.0f);
        assertEquals(255, hslColor.getRed());

        // fPercent <= 0 -> Nessun cambio
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 0.0f);
        assertEquals(0, hslColor.getRed());
    }

    @Test
    public void blendMixTest() {
        // Mix 50% tra nero (0) e bianco (255) -> 127
        hslColor.initHSLbyRGB(0, 0, 0);
        hslColor.blend(255, 255, 255, 0.5f);
        // (255*0.5) + (0*0.5) = 127
        assertEquals(127, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(127, hslColor.getBlue());
    }

    // --- TEST METODI PRIVATI (Tramite Reflection) ---
    // Questi metodi non sono usati da nessuna parte pubblicamente (Dead Code),
    // ma per coverage totale vanno testati.

    @Test
    public void privateSetRedTest() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        invokePrivateMethod("setRed", new Class<?>[]{int.class}, new Object[]{255});
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen()); // Invariati
    }

    @Test
    public void privateSetGreenTest() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        invokePrivateMethod("setGreen", new Class<?>[]{int.class}, new Object[]{255});
        assertEquals(255, hslColor.getGreen());
    }

    @Test
    public void privateSetBlueTest() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        invokePrivateMethod("setBlue", new Class<?>[]{int.class}, new Object[]{255});
        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void privateGreyscaleTest() throws Exception {
        // Imposta un colore saturo
        hslColor.initHSLbyRGB(255, 0, 0); 
        // Chiama greyscale()
        invokePrivateMethodNoArgs("greyscale");
        
        // Verifica che R=G=B (grigio basato sulla luminanza precedente)
        assertEquals(hslColor.getRed(), hslColor.getGreen());
        assertEquals(hslColor.getRed(), hslColor.getBlue());
        // Hue dovrebbe essere UNDEFINED (170) o quello che initRGBbyHSL imposta
        // Nota: greyscale() chiama initRGBbyHSL(UNDEFINED, 0, pLum). 
        // initRGBbyHSL imposta pHue = H. Quindi hue = 170.
        assertEquals(170, hslColor.getHue());
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void privateReverseLightTest() throws Exception {
        hslColor.setLuminence(100);
        invokePrivateMethodNoArgs("reverseLight");
        // HSLMAX(255) - 100 = 155
        assertEquals(155, hslColor.getLuminence());
    }

    @Test
    public void privateIMaxIMinTest() throws Exception {
        // Anche se usati da initHSLbyRGB, testarli direttamente garantisce 
        // che i rami (a>b) e (a<b) siano coperti isolatamente.
        
        Method imax = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        imax.setAccessible(true);
        assertEquals(10, imax.invoke(hslColor, 5, 10));
        assertEquals(10, imax.invoke(hslColor, 10, 5)); // Branch coverage

        Method imin = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        imin.setAccessible(true);
        assertEquals(5, imin.invoke(hslColor, 5, 10));
        assertEquals(5, imin.invoke(hslColor, 10, 5)); // Branch coverage
    }
}