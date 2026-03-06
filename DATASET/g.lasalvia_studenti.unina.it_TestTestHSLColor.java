/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Tuono"
Cognome: "Pettinato"
Username: g.lasalvia@studenti.unina.it
UserID: 1155
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private HSLColor hslColor;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @BeforeClass
    public static void setUpClass() {
        // Setup condiviso se necessario
    }

    @AfterClass
    public static void tearDownClass() {
        // Teardown condiviso se necessario
    }

    @Before
    public void setUp() {
        hslColor = new HSLColor();
    }

    @After
    public void tearDown() {
        hslColor = null;
    }

    /**
     * Verifica il calcolo HSL quando i valori RGB sono uguali (Scala di grigi).
     * Copre il branch: if (cMax == cMin).
     * Condition Coverage: pSat deve essere 0 e pHue UNDEFINED.
     */
    @Test
    public void initHSLbyRGBGreyscaleTest() {
        // Arrange
        int greyVal = 100;
        
        // Act
        hslColor.initHSLbyRGB(greyVal, greyVal, greyVal);

        // Assert
        assertEquals("Saturation should be 0 for greyscale", 0, hslColor.getSaturation());
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hslColor.getHue());
        assertEquals("Luminance calculation error", greyVal, hslColor.getLuminence());
    }

    /**
     * Verifica il calcolo della Saturazione quando la Luminosità <= HSLMAX/2.
     * Copre il branch: if (pLum <= (HSLMAX / 2)) -> true.
     * Inputs progettati per bassa luminosità.
     */
    @Test
    public void initHSLbyRGBLowLuminanceTest() {
        // Arrange: R=50, G=0, B=0 -> Max=50, Min=0, Sum=50. Lum approx 25 (<= 127).
        int r = 50, g = 0, b = 0;

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        assertTrue("Luminance should be <= 127", hslColor.getLuminence() <= (HSLMAX / 2));
        assertEquals("Red should match input", r, hslColor.getRed());
    }

    /**
     * Verifica il calcolo della Saturazione quando la Luminosità > HSLMAX/2.
     * Copre il branch: if (pLum <= (HSLMAX / 2)) -> false (else).
     * Inputs progettati per alta luminosità.
     */
    @Test
    public void initHSLbyRGBHighLuminanceTest() {
        // Arrange: R=255, G=255, B=200 -> Max=255, Min=200. High Luminance.
        int r = 255, g = 255, b = 200;

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        assertTrue("Luminance should be > 127", hslColor.getLuminence() > (HSLMAX / 2));
        assertNotEquals("Saturation should not be 0", 0, hslColor.getSaturation());
    }

    /**
     * Verifica il calcolo della Tinta (Hue) quando il Rosso è il componente dominante.
     * Copre il branch: if (cMax == R).
     */
    @Test
    public void initHSLbyRGBHueRedMaxTest() {
        // Arrange: Red dominant
        int r = 200, g = 50, b = 50;

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        // Hue dovrebbe essere 0 (o vicino a 0/255) per rosso puro, qui un valore positivo piccolo.
        assertEquals(r, hslColor.getRed());
        assertTrue("Hue check", hslColor.getHue() >= 0);
    }

    /**
     * Verifica il calcolo della Tinta (Hue) quando il Verde è il componente dominante.
     * Copre il branch: else if (cMax == G).
     */
    @Test
    public void initHSLbyRGBHueGreenMaxTest() {
        // Arrange: Green dominant
        int r = 50, g = 200, b = 50;

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        // Hue attorno a 85 (1/3 di 255).
        int expectedBase = HSLMAX / 3;
        assertEquals("Green should be max", g, Math.max(hslColor.getRed(), Math.max(hslColor.getGreen(), hslColor.getBlue())));
        assertTrue("Hue should be around 85", Math.abs(hslColor.getHue() - expectedBase) < 10);
    }

    /**
     * Verifica il calcolo della Tinta (Hue) quando il Blu è il componente dominante.
     * Copre il branch: else if (cMax == B).
     */
    @Test
    public void initHSLbyRGBHueBlueMaxTest() {
        // Arrange: Blue dominant
        int r = 50, g = 50, b = 200;

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        // Hue attorno a 170 (2/3 di 255).
        int expectedBase = (2 * HSLMAX) / 3;
        assertEquals("Blue should be max", b, Math.max(hslColor.getRed(), Math.max(hslColor.getGreen(), hslColor.getBlue())));
        assertTrue("Hue should be around 170", Math.abs(hslColor.getHue() - expectedBase) < 10);
    }

    /**
     * Test critico per coprire il branch 'if (pHue < 0)'.
     * Questo accade quando cMax == R ma BDelta > GDelta (cioè Blue > Green).
     */
    @Test
    public void initHSLbyRGBNegativeHueAdjustmentTest() {
        // Arrange: Max=R, ma B > G significativamente per rendere (BDelta - GDelta) negativo prima della correzione.
        int r = 255;
        int g = 0; 
        int b = 200; // Blue alto, Green basso

        // Act
        hslColor.initHSLbyRGB(r, g, b);

        // Assert
        // Hue formula: BDelta - GDelta. GDelta (basato su Max-G=255) sarà grande. BDelta (Max-B=55) piccolo.
        // Risultato negativo -> deve aggiungere HSLMAX.
        assertTrue("Hue should be positive after correction", hslColor.getHue() >= 0);
        assertTrue("Hue should be valid", hslColor.getHue() <= HSLMAX);
    }

    /**
     * Verifica conversione HSL -> RGB per Saturazione = 0 (Grigio).
     * Copre branch: if (S == 0).
     */
    @Test
    public void initRGBbyHSLGreyscaleTest() {
        // Arrange
        int h = 0; // Irrilevante
        int s = 0;
        int l = 128;

        // Act
        hslColor.initRGBbyHSL(h, s, l);

        // Assert
        assertEquals("Red should equal Lum", l, hslColor.getRed());
        assertEquals("Green should equal Lum", l, hslColor.getGreen());
        assertEquals("Blue should equal Lum", l, hslColor.getBlue());
    }

    /**
     * Verifica conversione HSL -> RGB per colori con bassa luminosità.
     * Copre branch: if (L <= HSLMAX / 2) in initRGBbyHSL.
     * Copre diverse regioni di hueToRGB chiamando con H=0 (Rosso).
     */
    @Test
    public void initRGBbyHSLColorLowLumTest() {
        // Arrange
        int h = 0; // Rosso
        int s = 255;
        int l = 100; // < 127.5

        // Act
        hslColor.initRGBbyHSL(h, s, l);

        // Assert
        // Rosso deve essere alto, G e B bassi
        assertTrue("Red should be high", hslColor.getRed() > 100);
        assertTrue("Green should be low", hslColor.getGreen() < 100);
        assertEquals(0, hslColor.getHue());
    }

    /**
     * Verifica conversione HSL -> RGB per colori con alta luminosità.
     * Copre branch: if (L <= HSLMAX / 2) -> else.
     */
    @Test
    public void initRGBbyHSLColorHighLumTest() {
        // Arrange
        int h = 85; // Verde (approx 255/3)
        int s = 255;
        int l = 200; // > 127.5

        // Act
        hslColor.initRGBbyHSL(h, s, l);

        // Assert
        assertTrue("Green should be dominant", hslColor.getGreen() >= hslColor.getRed());
        assertTrue("Green should be dominant", hslColor.getGreen() >= hslColor.getBlue());
    }

    /**
     * Verifica il metodo privato hueToRGB e il clamping dei valori RGB.
     * Forziamo un valore che potrebbe teoricamente superare RGBMAX se non controllato,
     * e testiamo le regioni "Hue < (HSLMAX / 6)" e "Hue < (HSLMAX / 2)".
     */
    @Test
    public void hueToRGBRegionsAndClampingTest() {
        // Arrange: Un set di valori che attraversa varie regioni in hueToRGB
        // H = 40 (Regione 1 per pGreen o pRed), H = 140 (Regione 3), etc.
        hslColor.initRGBbyHSL(40, 255, 128);
        int r1 = hslColor.getRed();
        
        hslColor.initRGBbyHSL(140, 255, 128);
        int r2 = hslColor.getRed();
        
        // Assert generico per verificare che non crashi e resti nei limiti
        assertTrue(r1 >= 0 && r1 <= RGBMAX);
        assertTrue(r2 >= 0 && r2 <= RGBMAX);
    }

    /**
     * Test del metodo setHue con valori fuori range per verificare il ciclo while.
     * Copre: while (iToValue < 0) e while (iToValue > HSLMAX).
     */
    @Test
    public void setHueBoundaryTest() {
        // Arrange
        hslColor.initRGBbyHSL(0, 100, 100); // Setup iniziale

        // Act 1: Negative loop (es. -10 -> 245)
        hslColor.setHue(-10);
        int h1 = hslColor.getHue();

        // Act 2: Positive loop (es. 265 -> 10)
        hslColor.setHue(265);
        int h2 = hslColor.getHue();

        // Assert
        assertEquals("Should wrap negative hue", 245, h1);
        assertEquals("Should wrap positive hue", 10, h2);
    }

    /**
     * Test setSaturation con clamping.
     */
    @Test
    public void setSaturationBoundaryTest() {
        // Act & Assert
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(300);
        assertEquals(HSLMAX, hslColor.getSaturation());
    }

    /**
     * Test setLuminence con clamping.
     */
    @Test
    public void setLuminenceBoundaryTest() {
        // Act & Assert
        hslColor.setLuminence(-1);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(500);
        assertEquals(HSLMAX, hslColor.getLuminence());
    }

    /**
     * Test blend con percentuali limite.
     * Copre i check if (fPercent >= 1) e if (fPercent <= 0).
     */
    @Test
    public void blendBoundaryTest() {
        // Arrange: Start Red
        hslColor.initHSLbyRGB(255, 0, 0);

        // Act 1: Percent <= 0 -> No change
        hslColor.blend(0, 0, 255, -0.5f); // Blend with Blue
        assertEquals("Should remain Red", 255, hslColor.getRed());

        // Act 2: Percent >= 1 -> Full switch
        hslColor.blend(0, 0, 255, 1.5f); // Blend with Blue
        assertEquals("Should become Blue", 255, hslColor.getBlue());
        assertEquals("Red should be 0", 0, hslColor.getRed());
    }

    /**
     * Test blend mixing standard.
     */
    @Test
    public void blendCalculationTest() {
        // Arrange: Start Black
        hslColor.initHSLbyRGB(0, 0, 0);

        // Act: Blend 50% with White
        hslColor.blend(255, 255, 255, 0.5f);

        // Assert: Should be Grey (approx 127)
        assertTrue(hslColor.getRed() > 120 && hslColor.getRed() < 135);
    }

    /**
     * Test brighten.
     * Copre: fPercent == 0, L < 0, L > HSLMAX.
     */
    @Test
    public void brightenTest() {
        // Arrange
        hslColor.initRGBbyHSL(0, 0, 100);

        // Act 1: 0 percent
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());

        // Act 2: Normal brighten
        hslColor.brighten(1.5f); // 100 * 1.5 = 150
        assertEquals(150, hslColor.getLuminence());
        
        // Act 3: Overflow clamp
        hslColor.brighten(10.0f); // 150 * 10 = 1500 -> clamped to 255
        assertEquals(255, hslColor.getLuminence());
    }

    @Test
    public void reverseColorTest() {
        // Arrange
        hslColor.setHue(0);
        
        // Act
        hslColor.reverseColor(); // + 127
        
        // Assert
        assertEquals(HSLMAX / 2, hslColor.getHue());
    }
    
    /**
     * Il metodo reverseLight è privato, ma possiamo testare metodi che lo usano?
     * Analizzando il codice, reverseLight non è usato da nessun metodo pubblico.
     * Tuttavia, per completezza sulla API pubblica fornita getters/setters.
     * *Nota:* reverseLight non è accessibile, quindi non testabile direttamente
     * senza reflection, e non usato pubblicamente.
     * * Testiamo invece i Getters per Red, Green, Blue assicurandoci che tornino
     * i valori impostati tramite i setter privati (invocati indirettamente).
     */
    @Test
    public void gettersConsistencyTest() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }
}

