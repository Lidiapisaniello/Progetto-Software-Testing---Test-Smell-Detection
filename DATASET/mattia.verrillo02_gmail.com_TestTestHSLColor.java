/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: mattia.verrillo02@gmail.com
UserID: 807
Date: 21/11/2025
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
        // Eseguito una volta prima dell'inizio dei test nella classe
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
    }

    @Before
    public void setUp() {
        // Inizializza l'oggetto prima di ogni test
        hslColor = new HSLColor();
    }

    @After
    public void tearDown() {
        // Pulisce il riferimento dopo ogni test
        hslColor = null;
    }

    // --- Test per initHSLbyRGB (Conversioni RGB -> HSL) ---

    @Test
    public void metodo_initHSLbyRGB_Test_1() {
        // Test Greyscale (R=G=B)
        // Se R=G=B, Sat deve essere 0 e Hue UNDEFINED (170)
        hslColor.initHSLbyRGB(100, 100, 100);
        assertEquals("Hue dovrebbe essere UNDEFINED (170) per grigio", 170, hslColor.getHue());
        assertEquals("Saturation dovrebbe essere 0 per grigio", 0, hslColor.getSaturation());
        // Lum: ((200*255)+255)/(510) = 100
        assertEquals("Luminence calcolata errata", 100, hslColor.getLuminence());
    }

    @Test
    public void metodo_initHSLbyRGB_Test_2() {
        // Test Red Max (cMax == R)
        hslColor.initHSLbyRGB(255, 0, 0);
        assertEquals(0, hslColor.getHue()); // Rosso puro ha Hue 0
        assertEquals(255, hslColor.getSaturation());
        assertEquals(127, hslColor.getLuminence()); // Circa metà
    }

    @Test
    public void metodo_initHSLbyRGB_Test_3() {
        // Test Green Max (cMax == G)
        hslColor.initHSLbyRGB(0, 255, 0);
        // Hue atteso: circa 85 (1/3 di 255)
        assertEquals(85, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void metodo_initHSLbyRGB_Test_4() {
        // Test Blue Max (cMax == B)
        hslColor.initHSLbyRGB(0, 0, 255);
        // Hue atteso: circa 170 (2/3 di 255)
        assertEquals(170, hslColor.getHue());
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void metodo_initHSLbyRGB_Test_5() {
        // Test Luminence <= HSLMAX/2
        // Nero
        hslColor.initHSLbyRGB(0, 0, 0);
        assertEquals(0, hslColor.getLuminence());
        assertEquals(0, hslColor.getSaturation());
    }

    @Test
    public void metodo_initHSLbyRGB_Test_6() {
        // Test Luminence > HSLMAX/2
        // Bianco
        hslColor.initHSLbyRGB(255, 255, 255);
        assertEquals(255, hslColor.getLuminence());
        assertEquals(0, hslColor.getSaturation());
    }
    
    @Test
    public void metodo_initHSLbyRGB_Test_7() {
        // Test correzione Hue < 0
        // Caso in cui cMax == R ma il calcolo intermedio risulta negativo
        // Usiamo un rosso dove Blue > Green leggermente per forzare Hue negativo prima della correzione
        hslColor.initHSLbyRGB(255, 0, 20);
        // Hue verrebbe calcolato negativo e poi sommato a 255
        assertTrue("Hue dovrebbe essere positivo dopo la correzione", hslColor.getHue() >= 0);
    }

    // --- Test per initRGBbyHSL (Conversioni HSL -> RGB) ---

    @Test
    public void metodo_initRGBbyHSL_Test_1() {
        // Test S == 0 (Greyscale)
        hslColor.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hslColor.getRed());
        assertEquals(128, hslColor.getGreen());
        assertEquals(128, hslColor.getBlue());
    }

    @Test
    public void metodo_initRGBbyHSL_Test_2() {
        // Test L <= HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 100); 
        assertEquals(100, hslColor.getLuminence());
        // Verifica che RGB siano impostati (non controlliamo i valori esatti per evitare errori di rounding, ma che siano consistenti)
        assertTrue(hslColor.getRed() > 0);
    }

    @Test
    public void metodo_initRGBbyHSL_Test_3() {
        // Test L > HSLMAX/2
        hslColor.initRGBbyHSL(0, 255, 200);
        assertEquals(200, hslColor.getLuminence());
        assertTrue(hslColor.getRed() > 0);
    }
    
    @Test
    public void metodo_initRGBbyHSL_Test_4() {
         // Test Hue ranges in hueToRGB helper
         // Hue < HSLMAX/6 (0-42) -> testato col rosso (0)
         // Hue < HSLMAX/2 (43-127)
         hslColor.initRGBbyHSL(85, 255, 127); // Verde
         assertEquals(0, hslColor.getRed()); 
         assertEquals(255, hslColor.getGreen()); // Verde max
         assertEquals(0, hslColor.getBlue());
         
         // Hue > 2/3
         hslColor.initRGBbyHSL(200, 255, 127);
         assertTrue(hslColor.getBlue() > 0);
    }

    @Test
    public void metodo_initRGBbyHSL_Test_5() {
        // Copertura dei check su pRed/pGreen/pBlue > RGBMAX (Clamping)
        // Anche se matematicamente difficile da superare con input validi HSL standard,
        // proviamo a forzare valori alti di luminanza e saturazione.
        hslColor.initRGBbyHSL(0, 255, 255); // Bianco puro
        assertEquals(255, hslColor.getRed());
        assertEquals(255, hslColor.getGreen());
        assertEquals(255, hslColor.getBlue());
    }

    // --- Test Getters e Setters ---

    @Test
    public void metodo_setHue_Test_1() {
        hslColor.setHue(100);
        assertEquals(100, hslColor.getHue());

        // Test while loops per normalizzazione
        hslColor.setHue(-50); // Diventa 205 (255 - 50)
        assertEquals(205, hslColor.getHue());

        hslColor.setHue(300); // Diventa 45 (300 - 255)
        assertEquals(45, hslColor.getHue());
    }

    @Test
    public void metodo_setSaturation_Test_1() {
        hslColor.setSaturation(100);
        assertEquals(100, hslColor.getSaturation());

        // Test clamping
        hslColor.setSaturation(-10);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void metodo_setLuminence_Test_1() {
        hslColor.setLuminence(100);
        assertEquals(100, hslColor.getLuminence());

        // Test clamping
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
    }

    @Test
    public void metodo_getRed_Green_Blue_Test_1() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }

    // --- Test Metodi di Logica Colore ---

    @Test
    public void metodo_reverseColor_Test_1() {
        hslColor.setHue(0);
        hslColor.reverseColor();
        // 0 + 127 = 127 (HSLMAX/2 è 255/2 = 127 int division)
        assertEquals(127, hslColor.getHue());
    }

    @Test
    public void metodo_brighten_Test_1() {
        hslColor.initHSLbyRGB(100, 100, 100);
        // Caso fPercent == 0 (nessun cambio)
        hslColor.brighten(0f);
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void metodo_brighten_Test_2() {
        hslColor.setLuminence(100);
        // Aumento
        hslColor.brighten(1.5f);
        assertEquals(150, hslColor.getLuminence());
    }

    @Test
    public void metodo_brighten_Test_3() {
        hslColor.setLuminence(100);
        // Overflow (>255)
        hslColor.brighten(5.0f);
        assertEquals(255, hslColor.getLuminence());
        
        // Underflow (<0)
        hslColor.setLuminence(100);
        hslColor.brighten(-1.0f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void metodo_blend_Test_1() {
        hslColor.initHSLbyRGB(0, 0, 0);
        // fPercent >= 1 -> Sostituzione totale
        hslColor.blend(255, 255, 255, 1.0f);
        assertEquals(255, hslColor.getRed());
    }

    @Test
    public void metodo_blend_Test_2() {
        hslColor.initHSLbyRGB(100, 100, 100);
        // fPercent <= 0 -> Nessun cambiamento
        hslColor.blend(255, 255, 255, 0.0f);
        assertEquals(100, hslColor.getRed());
    }

    @Test
    public void metodo_blend_Test_3() {
        // Mixing 50%
        hslColor.initHSLbyRGB(0, 0, 0); // Start Black
        hslColor.blend(100, 100, 100, 0.5f); // Blend with Grey
        // Risultato atteso 50
        assertEquals(50, hslColor.getRed());
        assertEquals(50, hslColor.getGreen());
        assertEquals(50, hslColor.getBlue());
    }

    // --- Test Metodi Privati tramite Reflection ---
    // Necessari per il 100% di copertura istruzioni poiché non chiamati dai metodi pubblici

    @Test
    public void metodo_setRed_Test_1() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        Method m = HSLColor.class.getDeclaredMethod("setRed", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255); // Imposta rosso a 255, lascia G e B a 0
        
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen());
        assertEquals(0, hslColor.getHue()); // Hue diventa rosso
    }

    @Test
    public void metodo_setGreen_Test_1() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        Method m = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255);
        
        assertEquals(255, hslColor.getGreen());
        assertEquals(0, hslColor.getRed());
    }

    @Test
    public void metodo_setBlue_Test_1() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);
        Method m = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        m.setAccessible(true);
        m.invoke(hslColor, 255);
        
        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void metodo_greyscale_Test_1() throws Exception {
        // Imposta un colore
        hslColor.initHSLbyRGB(255, 0, 0);
        
        Method m = HSLColor.class.getDeclaredMethod("greyscale");
        m.setAccessible(true);
        m.invoke(hslColor);
        
        assertEquals("Hue deve essere UNDEFINED", 170, hslColor.getHue());
        assertEquals("Sat deve essere 0", 0, hslColor.getSaturation());
        // Lum rimane quella originale (circa 127 per il rosso puro)
        assertEquals(127, hslColor.getLuminence());
    }

    @Test
    public void metodo_reverseLight_Test_1() throws Exception {
        hslColor.setLuminence(100);
        
        Method m = HSLColor.class.getDeclaredMethod("reverseLight");
        m.setAccessible(true);
        m.invoke(hslColor);
        
        // 255 - 100 = 155
        assertEquals(155, hslColor.getLuminence());
    }
    
    @Test
    public void metodo_iMax_iMin_Test_1() throws Exception {
        // Anche se testati indirettamente da initHSLbyRGB, possiamo invocarli per sicurezza
        Method maxM = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        maxM.setAccessible(true);
        int maxRes = (int) maxM.invoke(hslColor, 10, 20);
        assertEquals(20, maxRes);

        Method minM = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        minM.setAccessible(true);
        int minRes = (int) minM.invoke(hslColor, 10, 20);
        assertEquals(10, minRes);
    }
}