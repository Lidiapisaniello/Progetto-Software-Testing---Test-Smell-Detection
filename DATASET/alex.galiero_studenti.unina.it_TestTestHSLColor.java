/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alex"
Cognome: "Galiero"
Username: alex.galiero@studenti.unina.it
UserID: 212
Date: 24/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;


    private HSLColor color;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Inizio test per HSLColor...");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("...Fine test per HSLColor.");
    }

    @Before
    public void setUp() {
        color = new HSLColor();
    }

    @After
    public void tearDown() {
        color = null;
    }

    //initHSLbyRGB

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        color.initHSLbyRGB(0, 0, 0);
        
        assertEquals("La saturazione deve essere 0 per il nero", 0, color.getSaturation());
        assertEquals("La tonalità (Hue) deve essere UNDEFINED per il nero", UNDEFINED, color.getHue());
        

        int cPlus = 0 + 0;
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX);
        
        assertEquals("La luminanza deve essere 0 per il nero", expectedLum, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_Grey() {
        int val = 128;
        color.initHSLbyRGB(val, val, val);

        assertEquals("La saturazione deve essere 0 per il grigio", 0, color.getSaturation());
        assertEquals("La tonalità (Hue) deve essere UNDEFINED per il grigio", UNDEFINED, color.getHue());
        
        int cMax = val;
        int cMin = val;
        int cPlus = cMax + cMin; 
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); 
        
        assertEquals("Luminanza errata per il grigio medio", expectedLum, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        color.initHSLbyRGB(255, 255, 255);
        
        assertEquals("La saturazione deve essere 0 per il bianco", 0, color.getSaturation());
        assertEquals("La tonalità (Hue) deve essere UNDEFINED per il bianco", UNDEFINED, color.getHue());

        int cPlus = 255 + 255; 
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX);
        
        assertEquals("La luminanza deve essere 255 per il bianco", expectedLum, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Saturation_DarkBranch() {

        int R = 127, G = 0, B = 0;
        color.initHSLbyRGB(R, G, B);

        int cMax = R, cMin = G;
        int cPlus = cMax + cMin; 
        int cMinus = cMax - cMin; 
        

        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); // 64
        assertEquals("Luminanza errata per ramo 'scuro'", expectedLum, color.getLuminence());

        int expectedSat = (int) (((cMinus * HSLMAX) + 0.5) / cPlus); // (127 * 255) / 127 = 255
        
        assertEquals("Saturazione errata per il ramo 'scuro'", expectedSat, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_Saturation_LightBranch() {

        int R = 255, G = 0, B = 0;
        color.initHSLbyRGB(R, G, B);
        
        int cMax = R, cMin = G;
        int cPlus = cMax + cMin; 
        int cMinus = cMax - cMin; 

        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); 
        assertEquals("Luminanza errata per ramo 'chiaro'", expectedLum, color.getLuminence());
        
        int expectedSat = (int) (((cMinus * HSLMAX) + 0.5) / (2 * RGBMAX - cPlus));
        
        assertEquals("Saturazione errata per il ramo 'chiaro'", expectedSat, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_Hue_RedMax() {
        color.initHSLbyRGB(255, 0, 0); //rosso
        
        assertEquals("Tonalità errata per Rosso (atteso 0)", 0, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Hue_GreenMax() {
        color.initHSLbyRGB(0, 255, 0); //verde

        int expectedHue = (HSLMAX / 3);
        
        assertEquals("Tonalità errata per Verde", expectedHue, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Hue_BlueMax() {
        color.initHSLbyRGB(0, 0, 255); //blu

        int expectedHue = ((2 * HSLMAX) / 3);
        
        assertEquals("Tonalità errata per Blu", expectedHue, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Hue_NegativeCorrection() {
        int R = 200, G = 50, B = 100;
        color.initHSLbyRGB(R, G, B);

        int cMax = 200;
        int cMin = 50;
        int cMinus = cMax - cMin; 

        int RDelta = (int) ((((cMax - R) * (HSLMAX / 6)) + 0.5) / cMinus);
        int GDelta = (int) ((((cMax - G) * (HSLMAX / 6)) + 0.5) / cMinus); 
        int BDelta = (int) ((((cMax - B) * (HSLMAX / 6)) + 0.5) / cMinus); 

        int expectedHue = BDelta - GDelta; 

        if (expectedHue < 0) {
            expectedHue = expectedHue + HSLMAX; 
        }

        assertEquals("Correzione tonalità negativa errata", expectedHue, color.getHue());
    }

    //initRGBbyHSL

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        int H = 100, S = 0, L = 50;
        color.initRGBbyHSL(H, S, L);
        

        int expectedRGB = (L * RGBMAX) / HSLMAX; 
        
        assertEquals("Rosso errato per greyscale", expectedRGB, color.getRed());
        assertEquals("Verde errato per greyscale", expectedRGB, color.getGreen());
        assertEquals("Blu errato per greyscale", expectedRGB, color.getBlue());
        
        assertEquals(H, color.getHue());
        assertEquals(S, color.getSaturation());
        assertEquals(L, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_Color_DarkBranch() {
  
        int L = 50; 
        color.initRGBbyHSL(100, 200, L);
      
        assertEquals(100, color.getHue());
        assertEquals(200, color.getSaturation());
        assertEquals(L, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_Color_LightBranch() {

        int L = 200; 
        color.initRGBbyHSL(100, 200, L);

        assertEquals(100, color.getHue());
        assertEquals(200, color.getSaturation());
        assertEquals(L, color.getLuminence());
    }
    
    @Test
    public void testInitRGBbyHSL_ClampingBranch() {

        color.initRGBbyHSL(42, 255, 200); 
        
        assertEquals("Clamping Rosso non riuscito", RGBMAX, color.getRed());
    }

    //hueToRGB

    @Test
    public void testHueToRGB_NegativeHue() {

        color.initRGBbyHSL(20, 200, 100);
        assertEquals(20, color.getHue());
    }

    @Test
    public void testHueToRGB_PositiveHue() {

        color.initRGBbyHSL(200, 200, 100);
        assertEquals(200, color.getHue());
    }

    @Test
    public void testHueToRGB_Branch1() {

        color.initRGBbyHSL(20, 200, 100);
        assertEquals(20, color.getHue());
    }

    @Test
    public void testHueToRGB_Branch2() {

        color.initRGBbyHSL(100, 200, 100);
        assertEquals(100, color.getHue());
    }


    //manipolazione 

    @Test
    public void testBrighten_NoChange() {
        color.initRGBbyHSL(100, 100, 128); 
        
        color.brighten(0.0f);
        assertEquals("La luminanza non doveva cambiare", 128, color.getLuminence());
    }

    @Test
    public void testBrighten_OverMax() {
        color.initRGBbyHSL(100, 100, 128); 
        
        color.brighten(3.0f);
        assertEquals("La luminanza doveva essere limitata a HSLMAX", HSLMAX, color.getLuminence());
    }

    @Test
    public void testBrighten_BelowMin() {
        color.initRGBbyHSL(100, 100, 128); 
        
        color.brighten(-1.0f); 
        assertEquals("La luminanza doveva essere limitata a 0", 0, color.getLuminence());
    }

    @Test
    public void testBrighten_Normal() {
        int startLum = 128;
        float percent = 1.5f;
        color.initRGBbyHSL(100, 100, startLum); 
        
        color.brighten(percent); 
        
        int expectedLum = (int) (startLum * percent); 
        
        assertEquals("Luminanza errata dopo brighten", expectedLum, color.getLuminence());
    }

    @Test
    public void testBlend_Full() {
        color.initHSLbyRGB(0, 255, 0);
        
        int R_IN = 255, G_IN = 0, B_IN = 0;
        color.blend(R_IN, G_IN, B_IN, 1.0f);
        
        assertEquals(R_IN, color.getRed());
        assertEquals(G_IN, color.getGreen());
        assertEquals(B_IN, color.getBlue());
    }

    @Test
    public void testBlend_None() {
        int R_START = 0, G_START = 255, B_START = 0;
        color.initHSLbyRGB(R_START, G_START, B_START);

        color.blend(255, 0, 0, 0.0f); 
        
        assertEquals("Il colore non doveva cambiare", R_START, color.getRed());
        assertEquals("Il colore non doveva cambiare", G_START, color.getGreen());
        assertEquals("Il colore non doveva cambiare", B_START, color.getBlue());
    }

    @Test
    public void testBlend_Partial() {

        color.initHSLbyRGB(0, 255, 0);
        int pR = color.getRed(), pG = color.getGreen(), pB = color.getBlue();


        int R = 254, G = 0, B = 0;
        float fPercent = 0.5f;
        

        color.blend(R, G, B, fPercent);


        int newR = (int) ((R * fPercent) + (pR * (1.0 - fPercent)));
        int newG = (int) ((G * fPercent) + (pG * (1.0 - fPercent))); 
        int newB = (int) ((B * fPercent) + (pB * (1.0 - fPercent))); 
        
        assertEquals("Blend parziale Red errato", newR, color.getRed());
        assertEquals("Blend parziale Green errato", newG, color.getGreen());
        assertEquals("Blend parziale Blue errato", newB, color.getBlue());
    }

    @Test
    public void testReverseColor() {
        int startHue = 20;
        color.initRGBbyHSL(startHue, 200, 100);
        
        color.reverseColor(); 
        
        int expectedHue = startHue + (HSLMAX / 2); 
        
        assertEquals("Inversione colore fallita", expectedHue, color.getHue());
    }

    
    //setters

    @Test
    public void testSetHue_NegativeWrap() {
        int startHue = -50;
        color.setHue(startHue);
        
        int expectedHue = startHue + HSLMAX; 
        
        assertEquals("Hue negativo deve fare il wrap", expectedHue, color.getHue());
    }

    @Test
    public void testSetHue_PositiveWrap() {
        int startHue = 300;
        color.setHue(startHue);
        
        int expectedHue = startHue - HSLMAX;
        
        assertEquals("Hue positivo deve fare il wrap", expectedHue, color.getHue());
    }

    @Test
    public void testSetSaturation_NegativeClamp() {
        color.setSaturation(-50);
        assertEquals("Saturazione negativa deve essere limitata a 0", 0, color.getSaturation());
    }

    @Test
    public void testSetSaturation_PositiveClamp() {
        color.setSaturation(300);
        assertEquals("Saturazione positiva deve essere limitata a HSLMAX", HSLMAX, color.getSaturation());
    }

    @Test
    public void testSetLuminence_NegativeClamp() {
        color.setLuminence(-50);
        assertEquals("Luminanza negativa deve essere limitata a 0", 0, color.getLuminence());
    }

    @Test
    public void testSetLuminence_PositiveClamp() {
        color.setLuminence(300);
        assertEquals("Luminanza positiva deve essere limitata a HSLMAX", HSLMAX, color.getLuminence());
    }


    //Round-Trip

    @Test
    public void testRoundTripConversion() {
        int R_IN = 123, G_IN = 45, B_IN = 67;

        color.initHSLbyRGB(R_IN, G_IN, B_IN);

        int cMax = 123, cMin = 45, cPlus = cMax + cMin, cMinus = cMax - cMin;
        int L_calc = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); 
        int S_calc = (int) (((cMinus * HSLMAX) + 0.5) / cPlus);  
        int RDelta_calc = (int) ((((cMax - R_IN) * (HSLMAX / 6)) + 0.5) / cMinus); 
        int GDelta_calc = (int) ((((cMax - G_IN) * (HSLMAX / 6)) + 0.5) / cMinus); 
        int BDelta_calc = (int) ((((cMax - B_IN) * (HSLMAX / 6)) + 0.5) / cMinus); 
        int H_calc = BDelta_calc - GDelta_calc; 
        if (H_calc < 0) H_calc += HSLMAX;

        
        int H = color.getHue();
        int S = color.getSaturation();
        int L = color.getLuminence();

        color.initRGBbyHSL(H, S, L);

        assertEquals("Round-trip Red fallito", R_IN, color.getRed());
        assertEquals("Round-trip Green fallito", G_IN, color.getGreen());
        assertEquals("Round-trip Blue fallito", B_IN, color.getBlue());
    }
  @Test
    public void testSetSaturation1_NormalValue() {

        color.initHSLbyRGB(255, 0, 0);

        assertEquals("Setup fallito: Saturazione iniziale errata", HSLMAX, color.getSaturation());

        int iToValue = 120;

        color.setSaturation(iToValue);

        assertEquals("Saturazione non impostata al valore normale", iToValue, color.getSaturation());
    }
}