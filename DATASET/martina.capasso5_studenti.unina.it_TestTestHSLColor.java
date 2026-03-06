/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Martina"
Cognome: "Capasso"
Username: martina.capasso5@studenti.unina.it
UserID: 135
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class TestHSLColor {
    
    private HSLColor color;
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;
    
    // Delta per i confronti.
    // Usato solo per i test round-trip dove gli errori di arrotondamento si accumulano.
    private final static double ROUND_TRIP_DELTA = 5.0; 
    
    // Delta per i calcoli standard. L'aritmetica è intera, 
    // ma le conversioni HSL->RGB possono avere un errore di +/- 1.
    private final static double DELTA = 1.0; 

    @BeforeClass
    public static void setUpClass() {
    }
            
    @AfterClass
    public static void tearDownClass() {
    }
            
    @Before
    public void setUp() {
        color = new HSLColor();
    }
            
    @After
    public void tearDown() {
    }
            
    @Test
    public void testDefaultConstructor() {
        // I calcoli sono esatti (int)
        assertEquals(0, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }
    
    // --- Test per initHSLbyRGB (Calcoli Esatti) ---

    @Test
    public void testInitHSLbyRGB_Black() {
        color.initHSLbyRGB(0, 0, 0);
        assertEquals(UNDEFINED, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }
    
    @Test
    public void testInitHSLbyRGB_White() {
        color.initHSLbyRGB(255, 255, 255);
        assertEquals(UNDEFINED, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(HSLMAX, color.getLuminence());
        assertEquals(RGBMAX, color.getRed());
        assertEquals(RGBMAX, color.getGreen());
        assertEquals(RGBMAX, color.getBlue());
    }
    
    @Test
    public void testInitHSLbyRGB_Red() {
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(0, color.getHue());
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence()); // (255+0)/2 = 127.5, arrotondato a 128
    }
    
    @Test
    public void testInitHSLbyRGB_Green() {
        color.initHSLbyRGB(0, 255, 0);
        assertEquals(85, color.getHue()); // HSLMAX / 3
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }
    
    @Test
    public void testInitHSLbyRGB_Blue() {
        color.initHSLbyRGB(0, 0, 255);
        assertEquals(170, color.getHue()); // 2 * HSLMAX / 3
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_DarkColor_L_LE_Half() {
        // Test branch saturazione: pLum <= (HSLMAX / 2)
        color.initHSLbyRGB(100, 20, 20);
        assertEquals(0, color.getHue());
        assertEquals(170, color.getSaturation());
        assertEquals(60, color.getLuminence());
    }
    
    @Test
    public void testInitHSLbyRGB_NegativeHuePath() {
        // Test per il branch 'if (pHue < 0)'
        color.initHSLbyRGB(255, 0, 128);
        assertEquals(233, color.getHue());
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    // --- Test per initRGBbyHSL (Calcoli con Arrotondamento) ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        color.initRGBbyHSL(100, 0, 128);
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_DarkColor_L_LE_Half() {
        color.initRGBbyHSL(85, 255, 64);
        assertEquals(0, color.getRed(), DELTA);
        // Calcolo esatto: (128*255+127)/255 = 128
        assertEquals(128, color.getGreen(), DELTA); 
        assertEquals(0, color.getBlue(), DELTA);
    }

    @Test
    public void testInitRGBbyHSL_LightColor_L_GT_Half() {
        color.initRGBbyHSL(85, 255, 192);
        // Calcoli esatti: Magic1=129, Magic2=255
        // R(170)->m1=129, G(85)->m2=255, B(0)->m1=129
        assertEquals(129, color.getRed(), DELTA);
        assertEquals(255, color.getGreen(), DELTA);
        assertEquals(129, color.getBlue(), DELTA);
    }
    
    @Test
    public void testHueToRGB_Normalization() {
        // Test 'Hue < 0' e 'Hue > HSLMAX'
        color.initRGBbyHSL(-10, 255, 128); // 245
        int r1 = color.getRed();
        int g1 = color.getGreen();
        int b1 = color.getBlue();
        
        color.initRGBbyHSL(245, 255, 128);
        assertEquals(r1, color.getRed(), DELTA);
        assertEquals(g1, color.getGreen(), DELTA);
        assertEquals(b1, color.getBlue(), DELTA);

        color.initRGBbyHSL(265, 255, 128); // 10
        r1 = color.getRed();
        g1 = color.getGreen();
        b1 = color.getBlue();

        color.initRGBbyHSL(10, 255, 128);
        assertEquals(r1, color.getRed(), DELTA);
        assertEquals(g1, color.getGreen(), DELTA);
        assertEquals(b1, color.getBlue(), DELTA);
    }

    @Test
    public void testHueToRGB_AllBranches() {
        // Branch 1: Hue < (HSLMAX / 6) -> 42
        color.initRGBbyHSL(30, 255, 128);
        // H=30, L=128, S=255 -> M1=2, M2=254
        // R(115)->m2=254, G(30)->blend=182, B(-55->200)->m1=2
        assertEquals(254, color.getRed(), DELTA);
        assertEquals(182, color.getGreen(), DELTA);
        assertEquals(2, color.getBlue(), DELTA);
        
        // Branch 2: Hue < (HSLMAX / 2) -> 127
        color.initRGBbyHSL(85, 255, 128); // Verde puro
        // H=85, L=128, S=255 -> M1=2, M2=254
        // R(170)->m1=2, G(85)->m2=254, B(0)->blend=128
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(254, color.getGreen(), DELTA);
        assertEquals(128, color.getBlue(), DELTA); // Arrotondato da (128*255+127)/255

        // Branch 3: Hue < (HSLMAX * 2 / 3) -> 170
        color.initRGBbyHSL(150, 255, 128);
        // H=150, L=128, S=255 -> M1=2, M2=254
        // R(235)->m1=2, G(150)->blend=128, B(65)->m2=254
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(128, color.getGreen(), DELTA);
        assertEquals(254, color.getBlue(), DELTA);
        
        // Branch 4: else (Hue >= 170)
        // *** VALORI CORRETTI ***
        color.initRGBbyHSL(200, 255, 128);
        // H=200, L=128, S=255 -> M1=2, M2=254
        // R(285->30)->blend=182, G(200)->m1=2, B(115)->m2=254
        assertEquals(182, color.getRed(), DELTA);
        assertEquals(2, color.getGreen(), DELTA);
        assertEquals(254, color.getBlue(), DELTA);
    }
    
    // --- Test per Setters ---

    @Test
    public void testSetHue() {
        color.initHSLbyRGB(255, 0, 0); // H=0, S=255, L=128
        assertEquals(0, color.getHue());
        
        color.setHue(85); // Verde
        assertEquals(85, color.getHue());
        assertEquals(2, color.getRed(), DELTA); // Valore corretto (non 0)
        assertEquals(254, color.getGreen(), DELTA); // Valore corretto (non 255)
        assertEquals(128, color.getBlue(), DELTA); // Valore corretto (non 0)

        color.setHue(-10); // Loop a 245
        assertEquals(245, color.getHue());
        
        color.setHue(520); // Loop a 520 - 255 - 255 = 10
        assertEquals(10, color.getHue());
    }

    @Test
    public void testSetSaturation() {
        color.initHSLbyRGB(255, 0, 0); // H=0, S=255, L=128
        
        color.setSaturation(128); // Meno saturo
        assertEquals(128, color.getSaturation());
        assertEquals(191, color.getRed(), DELTA); // L=128,S=128 -> M1=64, M2=191
        assertEquals(64, color.getGreen(), DELTA);
        assertEquals(64, color.getBlue(), DELTA);
        
        color.setSaturation(-10); // Clamp a 0 (grigio)
        assertEquals(0, color.getSaturation());
        assertEquals(128, color.getRed(), DELTA);
        assertEquals(128, color.getGreen(), DELTA);
        assertEquals(128, color.getBlue(), DELTA);
        
        color.setSaturation(300); // Clamp a 255
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        color.initHSLbyRGB(255, 0, 0); // H=0, S=255, L=128

        color.setLuminence(64); // Rosso scuro
        assertEquals(64, color.getLuminence());
        assertEquals(128, color.getRed(), DELTA);
        assertEquals(0, color.getGreen(), DELTA);
        assertEquals(0, color.getBlue(), DELTA);

        color.setLuminence(-10); // Clamp a 0 (nero)
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getRed(), DELTA);
        
        color.setLuminence(300); // Clamp a 255 (bianco)
        assertEquals(255, color.getLuminence());
        assertEquals(255, color.getRed(), DELTA);
    }
    
    // --- Test per Manipolatori ---

    @Test
    public void testReverseColor() {
        color.initHSLbyRGB(255, 0, 0); // H=0 (Rosso), L=128
        color.reverseColor(); // H = 0 + 127 = 127
        assertEquals(127, color.getHue());
        
        // H=127, L=128, S=255 -> Ciano
        // M1=2, M2=254
        // R(212)->m1=2, G(127)->blend=260(CLAMPS!), B(42)->m2=254
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(255, color.getGreen(), DELTA); // Clamped
        assertEquals(254, color.getBlue(), DELTA);
    }
    
    @Test
    public void testBrighten() {
        color.initHSLbyRGB(128, 0, 0); // H=0, S=255, L=64
        
        color.brighten(0.0f); // 1. Test fPercent = 0
        assertEquals(64, color.getLuminence());
        
        color.brighten(1.5f); // 2. Test schiarisci -> L = 64 * 1.5 = 96
        assertEquals(96, color.getLuminence());
        
        color.brighten(0.5f); // 3. Test scurisci -> L = 96 * 0.5 = 48
        assertEquals(48, color.getLuminence());
        
        color.brighten(10.0f); // 4. Test clamp alto -> 480 -> 255
        assertEquals(255, color.getLuminence());
        
        color.setLuminence(100); // 5. Test clamp basso
        color.brighten(-0.5f); // L = 100 * -0.5 = -50 -> 0
        assertEquals(0, color.getLuminence());
    }
    
    @Test
    public void testBlend() {
        color.initHSLbyRGB(255, 0, 0); // Rosso
        
        color.blend(0, 255, 0, 0.0f); // 1. Test fPercent = 0
        assertEquals(255, color.getRed());
        
        color.blend(0, 255, 0, -0.5f); // 2. Test fPercent < 0
        assertEquals(255, color.getRed());

        color.blend(0, 255, 0, 1.0f); // 3. Test fPercent = 1.0
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());

        color.initHSLbyRGB(255, 0, 0); // Reset
        color.blend(0, 255, 0, 1.5f); // 4. Test fPercent > 1.0
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());

        color.initHSLbyRGB(255, 0, 0); // Reset
        color.blend(0, 0, 255, 0.5f); // 5. Test 50% blend
        assertEquals(127, color.getRed()); // (0*0.5)+(255*0.5) = 127.5 -> 127
        assertEquals(0, color.getGreen());
        assertEquals(127, color.getBlue());
    }
    
    // --- Test di Round-Trip (con DELTA maggiore) ---

    @Test
    public void testRoundTrip_RGB_HSL_RGB() {
        int R = 123, G = 45, B = 200;
        color.initHSLbyRGB(R, G, B); // (123,45,200) -> HSL(214, 156, 122)
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        // HSL(214, 156, 122) -> RGB(127, 44, 200)
        
        // Qui usiamo il DELTA grande perché gli errori si accumulano
        assertEquals((double)R, (double)color.getRed(), ROUND_TRIP_DELTA);
        assertEquals((double)G, (double)color.getGreen(), ROUND_TRIP_DELTA);
        assertEquals((double)B, (double)color.getBlue(), ROUND_TRIP_DELTA);
    }

    @Test
    public void testRoundTrip_HSL_RGB_HSL() {
        int H = 220, S = 180, L = 90;
        color.initRGBbyHSL(H, S, L); // (220,180,90) -> RGB(52, 28, 151)
        color.initHSLbyRGB(color.getRed(), color.getGreen(), color.getBlue());
        // (52, 28, 151) -> HSL(219, 137, 89)
        
        assertEquals((double)H, (double)color.getHue(), ROUND_TRIP_DELTA);
        assertEquals((double)S, (double)color.getSaturation(), ROUND_TRIP_DELTA);
        assertEquals((double)L, (double)color.getLuminence(), ROUND_TRIP_DELTA);
    }
    
    @Test
    public void testRoundTrip_Greyscale() {
         color.initHSLbyRGB(150, 150, 150);
         assertEquals(UNDEFINED, color.getHue());
         assertEquals(0, color.getSaturation());
         assertEquals(150, color.getLuminence());
         
         color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
         
         assertEquals(150, color.getRed());
         assertEquals(150, color.getGreen());
         assertEquals(150, color.getBlue());
    }
 
    // --- Test per Mutation e 100% Coverage ---
    
    /**
     * Test CHIAVE per il 100% di copertura.
     * Dimostra che i branch di clamping in initRGBbyHSL SONO raggiungibili.
     * Questo accade quando S=255, L=128, che genera Magic1=2, Magic2=254.
     * Il calcolo in hueToRGB può eccedere 254 (es. 260), forzando
     * il valore finale (dopo *255/255) a > 255.
     */
    @Test
    public void testClampingBranches_AreReachable() {
        // 1. Test Clamping pRed
        // H=42. H_Red = 42+85=127. hueToRGB(2, 254, 127) -> 260
        // pRed = (260 * 255 + 127) / 255 = 260.
        // if (pRed > 255) pRed = 255;
        color.initRGBbyHSL(42, 255, 128);
        assertEquals(255, color.getRed()); // Clamped da 260
        assertEquals(254, color.getGreen(), DELTA);
        assertEquals(2, color.getBlue(), DELTA);
        
        // 2. Test Clamping pGreen
        // H=127. H_Green = 127. hueToRGB(2, 254, 127) -> 260
        // pGreen = (260 * 255 + 127) / 255 = 260.
        // if (pGreen > 255) pGreen = 255;
        color.initRGBbyHSL(127, 255, 128);
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(255, color.getGreen()); // Clamped da 260
        assertEquals(254, color.getBlue(), DELTA);

        // 3. Test Clamping pBlue
        // H=212. H_Blue = 212-85=127. hueToRGB(2, 254, 127) -> 260
        // pBlue = (260 * 255 + 127) / 255 = 260.
        // if (pBlue > 255) pBlue = 255;
        color.initRGBbyHSL(212, 255, 128);
        assertEquals(254, color.getRed(), DELTA);
        assertEquals(2, color.getGreen(), DELTA);
        assertEquals(255, color.getBlue()); // Clamped da 260
    }

    /**
     * Test per mutation/boundary su hueToRGB. H = HSLMAX / 6 (42).
     * Deve cadere nel Branch 2.
     */
    @Test
    public void testHueToRGB_Boundary_Branch1_to_2() {
        // Valori corretti per H=42, S=255, L=128
        // M1=2, M2=254
        // R(127)->blend=260(CLAMPS!), G(42)->m2=254, B(-43->212)->m1=2
        color.initRGBbyHSL(42, 255, 128);
        assertEquals(255, color.getRed()); // Clamped
        assertEquals(254, color.getGreen(), DELTA);
        assertEquals(2, color.getBlue(), DELTA);
    }

    /**
     * Test per mutation/boundary su hueToRGB. H = HSLMAX / 2 (127).
     * Deve cadere nel Branch 3.
     */
    @Test
    public void testHueToRGB_Boundary_Branch2_to_3() {
        // Valori corretti per H=127, S=255, L=128
        // M1=2, M2=254
        // R(212)->m1=2, G(127)->blend=260(CLAMPS!), B(42)->m2=254
        color.initRGBbyHSL(127, 255, 128); 
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(255, color.getGreen()); // Clamped
        assertEquals(254, color.getBlue(), DELTA);
    }
    
    /**
     * Test per mutation/boundary su hueToRGB. H = HSLMAX * 2 / 3 (170).
     * Deve cadere nel Branch 4 (else).
     */
    @Test
    public void testHueToRGB_Boundary_Branch3_to_4() {
        // Valori corretti per H=170, S=255, L=128
        // M1=2, M2=254
        // R(255)->m1=2, G(170)->m1=2, B(85)->m2=254
        color.initRGBbyHSL(170, 255, 128); 
        assertEquals(2, color.getRed(), DELTA);
        assertEquals(2, color.getGreen(), DELTA);
        assertEquals(254, color.getBlue(), DELTA);
    }
    
    @Test
    public void testInitHSLbyRGB_Yellow() {
        color.initHSLbyRGB(255, 255, 0); // Giallo
        assertEquals(42, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }
    
    @Test
    public void testInitHSLbyRGB_Cyan() {
        color.initHSLbyRGB(0, 255, 255); // Ciano
        assertEquals(127, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }
    
    // --- Test Metodi Privati (Reflection) ---
    
    @Test
    public void testPrivate_greyscale() throws Exception {
        color.initRGBbyHSL(85, 255, 128);
        Method greyscaleMethod = HSLColor.class.getDeclaredMethod("greyscale");
        greyscaleMethod.setAccessible(true);
        greyscaleMethod.invoke(color);

        assertEquals(0, color.getSaturation());
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }
    
    @Test
    public void testPrivate_setRed() throws Exception {
        color.initHSLbyRGB(0, 0, 0); 
        Method setRedMethod = HSLColor.class.getDeclaredMethod("setRed", int.class);
        setRedMethod.setAccessible(true);
        setRedMethod.invoke(color, 255); 

        assertEquals(255, color.getRed());
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
    }
    
    @Test
    public void testPrivate_setGreen() throws Exception {
        color.initHSLbyRGB(0, 0, 0); 
        Method setGreenMethod = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        setGreenMethod.setAccessible(true);
        setGreenMethod.invoke(color, 255); 

        assertEquals(255, color.getGreen());
        assertEquals(85, color.getHue());
    }
    
    @Test
    public void testPrivate_setBlue() throws Exception {
        color.initHSLbyRGB(0, 0, 0); 
        Method setBlueMethod = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        setBlueMethod.setAccessible(true);
        setBlueMethod.invoke(color, 255); 

        assertEquals(255, color.getBlue());
        assertEquals(170, color.getHue());
    }

    @Test
    public void testPrivate_reverseLight() throws Exception {
        color.initHSLbyRGB(255, 0, 0); // Rosso (L=128)
        int originalLum = color.getLuminence();
        
        Method reverseLightMethod = HSLColor.class.getDeclaredMethod("reverseLight");
        reverseLightMethod.setAccessible(true);
        reverseLightMethod.invoke(color);
        
        int expectedLum = HSLMAX - originalLum; // 255 - 128 = 127
        assertEquals(expectedLum, color.getLuminence());
        
        // Verifica RGB per H=0, S=255, L=127
        // M1=0, M2=254
        // R(85)->m2=254, G(0)->blend=0, B(-85->170)->m1=0
        assertEquals(254, color.getRed(), DELTA);
        assertEquals(0, color.getGreen(), DELTA);
        assertEquals(0, color.getBlue(), DELTA);
    }
}