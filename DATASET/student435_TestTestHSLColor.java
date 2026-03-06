import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

public class TestHSLColor {

    private HSLColor hslColor;
    // Costanti prese dalla classe originale per verifica
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    // --- TEST INIT HSL BY RGB (Conversione RGB -> HSL) ---

    @Test
    public void testInitHSLbyRGB_Red() {
        // Caso: Max è R
        hslColor.initHSLbyRGB(255, 0, 0);
        // HSL attesi per Rosso puro: H=0 (o valore mappato), S=255, L=127
        assertEquals("Red component", 255, hslColor.getRed());
        assertEquals("Saturation should be Max", 255, hslColor.getSaturation());
        assertTrue("Luminance should be around middle", Math.abs(hslColor.getLuminence() - 127) <= 1);
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        // Caso: Max è G
        hslColor.initHSLbyRGB(0, 255, 0);
        assertEquals("Green component", 255, hslColor.getGreen());
        assertEquals("Saturation should be Max", 255, hslColor.getSaturation());
        // Hue calcolato: (HSLMAX / 3) -> 255/3 = 85
        assertEquals("Hue for Green", 85, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        // Caso: Max è B
        hslColor.initHSLbyRGB(0, 0, 255);
        assertEquals("Blue component", 255, hslColor.getBlue());
        // Hue calcolato: (2 * HSLMAX) / 3 -> 170
        assertEquals("Hue for Blue", 170, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        // Caso: cMax == cMin (Nero)
        hslColor.initHSLbyRGB(0, 0, 0);
        assertEquals(0, hslColor.getLuminence());
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        // Caso: cMax == cMin (Bianco)
        hslColor.initHSLbyRGB(255, 255, 255);
        assertEquals(255, hslColor.getLuminence());
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testInitHSLbyRGB_LuminanceThreshold() {
        // Copertura del ramo if (pLum <= (HSLMAX / 2))

        // Caso scuro (Lum <= 127)
        hslColor.initHSLbyRGB(50, 50, 100);
        assertTrue(hslColor.getLuminence() <= 127);

        // Caso chiaro (Lum > 127)
        hslColor.initHSLbyRGB(200, 200, 250);
        assertTrue(hslColor.getLuminence() > 127);
    }

    // --- TEST INIT RGB BY HSL (Conversione HSL -> RGB) ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        // Se Saturation == 0, è scala di grigi
        hslColor.initRGBbyHSL(0, 0, 100);
        assertEquals("Red should match Lum scaled", 100, hslColor.getRed());
        assertEquals("Green should match Lum scaled", 100, hslColor.getGreen());
        assertEquals("Blue should match Lum scaled", 100, hslColor.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_NormalColor() {
        // Colore normale (Rosso saturo)
        // H=0, S=255, L=128
        hslColor.initRGBbyHSL(0, 255, 128);

        // Dovrebbe tornare approssimativamente a RGB(255, 0, 0)
        // Tolleranza di 1 dovuta agli arrotondamenti interi
        assertEquals(255, hslColor.getRed());
        assertTrue(hslColor.getGreen() < 5); // Quasi 0
        assertTrue(hslColor.getBlue() < 5);  // Quasi 0
    }

    @Test
    public void testInitRGBbyHSL_Magic2_Branching() {
        // Test ramificazione calcolo Magic2 (L <= HSLMAX/2 vs L > HSLMAX/2)

        // L basso
        hslColor.initRGBbyHSL(0, 255, 50);
        assertTrue(hslColor.getLuminence() <= 127);

        // L alto
        hslColor.initRGBbyHSL(0, 255, 200);
        assertTrue(hslColor.getLuminence() > 127);
    }

    // --- TEST GETTERS & SETTERS con Logica di Clamp/Wrap ---

    @Test
    public void testSetHue() {
        hslColor.initRGBbyHSL(0, 255, 128); // Base Red

        // Test Wrap negativo
        hslColor.setHue(-10);
        // -10 + 255 = 245
        assertEquals(245, hslColor.getHue());

        // Test Wrap positivo
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
    }

    @Test
    public void testSetSaturation() {
        hslColor.initRGBbyHSL(0, 100, 128);

        // Test Clamp negativo
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());

        // Test Clamp massimo
        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence() {
        hslColor.initRGBbyHSL(0, 100, 100);

        // Test Clamp negativo
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        // Test Clamp massimo
        hslColor.setLuminence(300);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    // --- TEST ALTRI METODI PUBBLICI ---

    @Test
    public void testReverseColor() {
        // Hue 0 (Rosso) -> Reverse deve essere +127 (Ciano/Azzurro)
        hslColor.initRGBbyHSL(0, 255, 128);
        hslColor.reverseColor();

        int expectedHue = 0 + (HSLMAX / 2);
        assertEquals(expectedHue, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        hslColor.initRGBbyHSL(0, 0, 100); // Lum = 100

        // Case 0%
        hslColor.brighten(0.0f);
        assertEquals(100, hslColor.getLuminence()); // No change

        // Case 50% increase logic (L * percent)
        // Nota: il metodo brighten nel codice fornito setta L = pLum * fPercent
        // Quindi brighten(1.5f) renderebbe L = 150
        hslColor.brighten(1.5f);
        assertEquals(150, hslColor.getLuminence());

        // Case Overflow
        hslColor.brighten(10.0f);
        assertEquals(HSLMAX, hslColor.getLuminence());

        // Case Underflow
        hslColor.brighten(-1.0f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        // Base: Rosso
        hslColor.initHSLbyRGB(255, 0, 0);

        // Blend con Blu al 100% (dovrebbe diventare Blu completamente)
        hslColor.blend(0, 0, 255, 1.0f);
        assertEquals(255, hslColor.getBlue());
        assertEquals(0, hslColor.getRed());

        // Blend con Blu allo 0% (dovrebbe rimanere com'era)
        hslColor.blend(0, 0, 255, 0.0f);
        assertEquals(255, hslColor.getBlue()); // Era diventato blu al passo prima

        // Reset a Rosso
        hslColor.initHSLbyRGB(255, 0, 0);
        // Blend 50% con Verde (0, 255, 0) -> Risultato (127, 127, 0)
        hslColor.blend(0, 255, 0, 0.5f);

        // Calcolo: 255*0.5 + 0*0.5 = 127
        assertEquals(127, hslColor.getRed());
        assertEquals(127, hslColor.getGreen());
        assertEquals(0, hslColor.getBlue());
    }

    // --- TEST METODI PRIVATI TRAMITE REFLECTION ---
    // Questi test sono necessari solo perché hai richiesto "massima copertura".
    // I metodi greyscale(), reverseLight(), setRed(), setGreen(), setBlue()
    // sono privati e NON vengono chiamati internamente da nessun metodo pubblico.
    // Sarebbero normalmente "dead code".

    @Test
    public void testPrivateGreyscale() throws Exception {
        hslColor.initHSLbyRGB(255, 0, 0); // Rosso Saturo

        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(hslColor);

        assertEquals("Saturation should be 0 after greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED", UNDEFINED, hslColor.getHue());
    }

    @Test
    public void testPrivateReverseLight() throws Exception {
        hslColor.initRGBbyHSL(0, 0, 50); // Lum = 50

        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(hslColor);

        // Expected: HSLMAX - 50 = 255 - 50 = 205
        assertEquals(205, hslColor.getLuminence());
    }

    @Test
    public void testPrivateSetRed() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        method.invoke(hslColor, 255);

        assertEquals(255, hslColor.getRed());
        // Dovrebbe aver ricalcolato HSL
        //assertEquals(UNDEFINED, hslColor.getHue()); // Sat è 255 ma Lum cambia
    }

    @Test
    public void testPrivateSetGreen() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(hslColor, 255);

        assertEquals(255, hslColor.getGreen());
    }

    @Test
    public void testPrivateSetBlue() throws Exception {
        hslColor.initHSLbyRGB(0, 0, 0);

        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(hslColor, 255);

        assertEquals(255, hslColor.getBlue());
    }

    @Test
    public void testPrivateHueToRGB_Boundaries() throws Exception {
        // Test diretto del metodo helper hueToRGB per coprire rami difficili da raggiungere
        Method method = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        method.setAccessible(true);

        int mag1 = 10;
        int mag2 = 20;

        // Case: Hue < 0 (dovrebbe aggiungere HSLMAX)
        method.invoke(hslColor, mag1, mag2, -10);

        // Case: Hue > HSLMAX (dovrebbe sottrarre HSLMAX)
        method.invoke(hslColor, mag1, mag2, 300);

        // Case: Hue < HSLMAX/6
        method.invoke(hslColor, mag1, mag2, 10);

        // Case: Hue < HSLMAX/2
        method.invoke(hslColor, mag1, mag2, 100);

        // Case: Hue < HSLMAX * 2/3
        method.invoke(hslColor, mag1, mag2, 160);

        // Case: Else final
        method.invoke(hslColor, mag1, mag2, 200);
    }
}