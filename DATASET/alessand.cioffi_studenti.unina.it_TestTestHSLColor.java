/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Alessandro
Cognome: Cioffi
Username: alessand.cioffi@studenti.unina.it
UserID: 132
Date: 22/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;




public class TestHSLColor {
    static HSLColor hslColor;
    private final static int UNDEFINED = 170;
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;

    int cMax;
    int cMin;
    int RDelta;
    int GDelta;
    int BDelta;
    int cMinus;
    int cPlus;

    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima dell'inizio dei test nella classe
        // Inizializza risorse condivise
        // o esegui altre operazioni di setup
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
        // Effettua la pulizia delle risorse condivise
        // o esegui altre operazioni di teardown
    }

    @Before
    public void setUp() {
        // Eseguito prima di ogni metodo di test
        // Preparazione dei dati di input specifici per il test
        hslColor = new HSLColor();
        assumeNotNull(hslColor);
    }

    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
        // Pulizia delle risorse o ripristino dello stato iniziale
        hslColor = null;
        assumeTrue(hslColor == null);
    }

    @Test
    public void testMetodo() {
        // Preparazione dei dati di input
        // Esegui il metodo da testare
        // Verifica l'output o il comportamento atteso
        // Utilizza assert per confrontare il risultato atteso
        // con il risultato effettivo
    }

    // test del metodo initHSLbyRGB per vedere se setta correttamente i valori
    @Test
    public void test_initHSLbyRGB_1() {
        int R = 255;
        int G = 0;
        int B = 0;
        hslColor.initHSLbyRGB(R, G, B);
        assertEquals(R, hslColor.getRed());
        assertEquals(G, hslColor.getGreen());
        assertEquals(B, hslColor.getBlue());
    }

    //test del metodo initHSLbyRGB con valori di iMAX E iMin uguali(test greyscale)
    @Test
    public void test_initHSLbyRGB_Greyscale() {
        int R = 10;
        int G = 10;
        int B = 10;
        hslColor.initHSLbyRGB(R, G, B);
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
    }

    //test del metodo initHSLbyRGB per vedere se setta correttamente i valori di saturazione nel caso di pLum >= (HSLMAX / 2)
    @Test
    public void test_initHSLbyRGB_CalculateColorSaturation_1() {
        int R = 1000;
        int G = 50;
        int B = 50;
        cMinus = R - G;
        cPlus = R + G;
        hslColor.initHSLbyRGB(R, G, B);
        int risultato = (int) (((cMinus * HSLMAX) + 0.5) / (2 * RGBMAX - cPlus));
        assertEquals(risultato, hslColor.getSaturation());
    }

    //test del metodo initHSLbyRGB per vedere se setta correttamente i valori di saturazione nel caso di pLum <= (HSLMAX / 2)
    @Test
    public void test_initHSLbyRGB_CalculateColorSaturation_2() {
        int R = 80;
        int G = 20;
        int B = 20;
        cMinus = R - G;
        cPlus = R + G;
        hslColor.initHSLbyRGB(R, G, B);
        int risultato = (int) (((cMinus * HSLMAX) + 0.5) / cPlus);
        assertEquals(risultato, hslColor.getSaturation());
    }

    //test del metodo initHSLbyRGB per vedere se normalizza il valore di HUE
    @Test
    public void test_initHSLbyRGB_CalculateColorSaturation_3() {
        int R = 200;
        int G = 50;
        int B = 180;

        hslColor.initHSLbyRGB(R, G, B);
        assertTrue(hslColor.getHue() > 0 && hslColor.getHue()< HSLMAX);
    }

    //test del metodo initHSLbyRGB per vedere se setta correttamente i valori di hue, nel caso di cMax == G
    @Test
    public void test_initHSLbyRGB_CalculateColorHue_1() {
        int R = 20;
        int G = 200;
        int B = 100;

        hslColor.initHSLbyRGB(R, G, B);

        cMax = Math.max(Math.max(R, G), B);
        cMin = Math.min(Math.min(R, G), B);
        cMinus = cMax - cMin;

        RDelta = (int) ((((cMax - R) * (HSLMAX / 6)) + 0.5) / cMinus);
        BDelta = (int) ((((cMax - B) * (HSLMAX / 6)) + 0.5) / cMinus);

        int pHue = (HSLMAX / 3) + RDelta - BDelta;

        assertEquals(pHue, hslColor.getHue());
    }


    //test del metodo initHSLbyRGB per vedere se setta correttamente i valori di hue, nel caso di cMax == B
    @Test
    public void test_initHSLbyRGB_CalculateColorHue_2() {
        int R = 50;
        int G = 100;
        int B = 200;

        hslColor.initHSLbyRGB(R, G, B);

        cMax = Math.max(Math.max(R, G), B);
        cMin = Math.min(Math.min(R, G), B);
        cMinus = cMax - cMin;

        RDelta = (int) ((((cMax - R) * (HSLMAX / 6)) + 0.5) / cMinus);
        GDelta = (int) ((((cMax - G) * (HSLMAX / 6)) + 0.5) / cMinus);

        int pHue = ((2 * HSLMAX) / 3) + GDelta - RDelta;

        assertEquals(pHue, hslColor.getHue());
    }

    //test del metodo initHSLbyRGB per vedere se setta correttamente i valori di hue, nel caso di cMax == R
    @Test
    public void test_initHSLbyRGB_CalculateColorHue_3() {
        int R = 150;
        int G = 100;
        int B = 20;

        hslColor.initHSLbyRGB(R, G, B);

        cMax = Math.max(Math.max(R, G), B);
        cMin = Math.min(Math.min(R, G), B);
        cMinus = cMax - cMin;

        BDelta = (int) ((((cMax - B) * (HSLMAX / 6)) + 0.5) / cMinus);
        GDelta = (int) ((((cMax - G) * (HSLMAX / 6)) + 0.5) / cMinus);

        int pHue = BDelta - GDelta;

        assertEquals(pHue, hslColor.getHue());
    }

    //test di setHUE  nel caso di iToValue negativo
    @Test
    public void test_setHUE_1() {
        int iToValue = -10;
        hslColor.setHue(iToValue);
        assertEquals(hslColor.getHue(), HSLMAX+iToValue);

    }

    //test di setHUE  nel caso di iToValue > HSLMAX
    @Test
    public void test_setHUE_2() {
        int iToValue = 300;
        hslColor.setHue(iToValue);
        assertEquals(hslColor.getHue(), iToValue-HSLMAX);

    }

    //test di setSaturation  nel caso di iToValue negativo
    @Test
    public void test_setSaturation_1() {
        int iToValue = -30;
        hslColor.setSaturation(iToValue);
        int expected = 0;
        assertEquals(hslColor.getSaturation(), expected);

    }

    //test di setSaturation  nel caso di iToValue > HSLMAX
    @Test
    public void test_setSaturation_2() {
        int iToValue = 400;
        hslColor.setSaturation(iToValue);
        int expected = HSLMAX;
        assertEquals(hslColor.getSaturation(), expected);
    }

    //test di setSaturation  nel caso di iToValue < HSLMAX ma iToValue> 0
    @Test
    public void test_setSaturation_3() {
        int iToValue = 50;
        hslColor.setSaturation(iToValue);
        int expected = iToValue;
        assertEquals(hslColor.getSaturation(), expected);
    }

    //test di setLuminence  nel caso di iToValue negativo
    @Test
    public void test_setLuminence_1() {
        int iToValue = -30;
        hslColor.setLuminence(iToValue);
        int expected = 0;
        assertEquals(hslColor.getLuminence(), expected);

    }

    //test di setLuminence  nel caso di iToValue > HSLMAX
    @Test
    public void test_setLuminence_2() {
        int iToValue = 500;
        hslColor.setLuminence(iToValue);
        int expected = HSLMAX;
        assertEquals(hslColor.getLuminence(), expected);

    }

    //test di setLuminence  nel caso di iToValue < HSLMAX ma iToValue > 0
    @Test
    public void test_setLuminence_3() {
        int iToValue = 50;
        hslColor.setLuminence(iToValue);
        int expected = iToValue;
        assertEquals(hslColor.getLuminence(), expected);
    }

    //test del metodo initRGBbyHSL per vedere se setta correttamente i valori nel caso di S = 0 --> greyscale
@Test
    public void test_initRGBbyHSL_1(){
        int iHue = 100;
        int iSat = 0;
        int iLum = 50;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        int expected = (iLum * RGBMAX) / HSLMAX;
        assertEquals(expected, hslColor.getRed());
        assertEquals(expected, hslColor.getGreen());
        assertEquals(expected, hslColor.getBlue());
}

    //test del metodo initRGBbyHSL per vedere se setta correttamente i valori nel caso di S != 0 e iLum <= HSLMAX / 2
    // e (pRed > RGBMAX)
    @Test
    public void test_initRGBbyHSL_2(){
        int iHue = 240;
        int iSat = 256;
        int iLum = 256;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);

        int expected = HSLMAX;
        assertEquals(expected, hslColor.getRed());

    }

    //test del metodo initRGBbyHSL per vedere se setta correttamente i valori nel caso di S != 0 e iLum <= HSLMAX / 2
    // e (pGreen > RGBMAX)
    @Test
    public void test_initRGBbyHSL_3(){
        int iHue = 120;
        int iSat = 256;
        int iLum = 256;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);

        int expected = HSLMAX;
        assertEquals(expected, hslColor.getGreen());

    }

    //test del metodo initRGBbyHSL per vedere se setta correttamente i valori nel caso di S != 0 e iLum <= HSLMAX / 2
    // e (pBlue > RGBMAX)
    @Test
    public void test_initRGBbyHSL_4(){
        int iHue = 0;
        int iSat = 255;
        int iLum = 256;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);

        int expected = HSLMAX;
        assertEquals(expected, hslColor.getBlue());

    }

    // test reverseColor
    @Test
    public void test_reverseColor_1(){
        int iHue = 100;
        int iSat = 0;
        int iLum = 50;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        hslColor.reverseColor();
        int expected = iHue + (HSLMAX / 2);
        assertEquals(expected, hslColor.getHue());
    }

//test brighten con fPercent = 0
    @Test
    public void test_brighten_1(){
        int iHue = 100;
        int iSat = 0;
        int iLum = 50;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        hslColor.brighten(0);
        int expected = iLum;
        assertEquals(expected, hslColor.getLuminence());
    }

    //test brighten con fPercent != 0 e iLum compreso tra 0 e HSLMAX
    @Test
    public void test_brighten_2(){
        int iHue = 100;
        int iSat = 0;
        int iLum = 50;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        hslColor.brighten(0.5f);
        int expected = (int) (iLum * 0.5f);
        assertEquals(expected, hslColor.getLuminence());
    }

    //test brighten con fPercent != 0 e iLum <0
    @Test
    public void test_brighten_3(){
        int iHue = 100;
        int iSat = 0;
        int iLum = -50;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        hslColor.brighten(0.5f);
        int expected = 0;
        assertEquals(expected, hslColor.getLuminence());
    }

    //test brighten con fPercent != 0 e iLum <0
    @Test
    public void test_brighten_4(){
        int iHue = 100;
        int iSat = 0;
        int iLum = 500;
        hslColor.initRGBbyHSL(iHue, iSat, iLum);
        hslColor.brighten(100f);
        int expected = HSLMAX;
        assertEquals(expected, hslColor.getLuminence());
    }

    //test blendColor con fPercent > 1
    @Test
    public void test_blendColor_1(){
        int R = 150;
        int G = 100;
        int B = 20;
        float fPercent = 1.5f;
        hslColor.blend(R, G, B, fPercent);
        assertEquals(hslColor.getRed(), R);
        assertEquals(hslColor.getGreen(), G);
        assertEquals(hslColor.getBlue(), B);

    }

    //test blendColor con fPercent < 0
    @Test
    public void test_blendColor_2(){
        int R = 150;
        int G = 100;
        int B = 20;
        float fPercent = -1.5f;
        hslColor.blend(R, G, B, fPercent);
        assertNotSame(hslColor.getRed(), R);
        assertNotSame(hslColor.getGreen(), G);
        assertNotSame(hslColor.getBlue(), B);
    }

    //test blendColor con fPercent compreso tra 0 e 1
    @Test
    public void test_blendColor_3(){
        int R = 150;
        int G = 100;
        int B = 20;
        float fPercent = 0.5f;
        hslColor.initHSLbyRGB(R,G,B);
        hslColor.blend(R, G, B, fPercent);

        int newR = (int) ((R * fPercent) + (R * (1.0 - fPercent)));
        int newG = (int) ((G * fPercent) + (G * (1.0 - fPercent)));
        int newB = (int) ((B * fPercent) + (B * (1.0 - fPercent)));

        assertEquals(hslColor.getRed(), newR);
        assertEquals(hslColor.getGreen(), newG);
        assertEquals(hslColor.getBlue(), newB);
    }

}

						