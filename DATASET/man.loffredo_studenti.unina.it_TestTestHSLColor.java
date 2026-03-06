/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Manuel"
Cognome: "Loffredo"
Username: man.loffredo@studenti.unina.it
UserID: 216
Date: 28/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {
  private HSLColor color;
  private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;
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
		// Preparazione dei dati di input specifici per il testù
      color =new HSLColor();
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia delle risorse o ripristino dello stato iniziale
      color=null;
	}
				
	@Test
	public void testInitHSLbyRGB_Greyscale_Grey() {
	     color.initHSLbyRGB(0, 0, 0);
        
        assertEquals("La saturazione deve essere 0 per il nero", 0, color.getSaturation());
        assertEquals("La tonalità (Hue) deve essere UNDEFINED per il nero", UNDEFINED, color.getHue());
        
      
        int cPlus = 0 + 0;
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); 
        
        assertEquals("La luminanza deve essere 0 per il nero", expectedLum, color.getLuminence());
   
	}
	    @Test
    public void testInitHSLbyRGB_Saturation_DarkBranch() {
        
        int R = 127, G = 0, B = 0;
        color.initHSLbyRGB(R, G, B);

        int cMax = R, cMin = G;
        int cPlus = cMax + cMin; 
        int cMinus = cMax - cMin; 
        
        
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); 
        assertEquals("Luminanza errata per ramo 'scuro'", expectedLum, color.getLuminence());
        
        
        int expectedSat = (int) (((cMinus * HSLMAX) + 0.5) / cPlus); 
        
        assertEquals("Saturazione errata per il ramo 'scuro'", expectedSat, color.getSaturation());
    }	
   @Test
    public void testInitHSLbyRGB_Saturation_LightBranch() {
        
        int R = 255, G = 0, B = 0;
        color.initHSLbyRGB(R, G, B);
        
        int cMax = R, cMin = G;
        int cPlus = cMax + cMin; 
        int cMinus = cMax - cMin; 
        
        int expectedLum = ((cPlus * HSLMAX) + RGBMAX) / (2 * RGBMAX); // 128
        assertEquals("Luminanza errata per ramo 'chiaro'", expectedLum, color.getLuminence());
        
        int expectedSat = (int) (((cMinus * HSLMAX) + 0.5) / (2 * RGBMAX - cPlus)); 
        
        assertEquals("Saturazione errata per il ramo 'chiaro'", expectedSat, color.getSaturation());
    }

 
   @Test
    public void testInitHSLbyRGB_Hue_GreenMax() {
        color.initHSLbyRGB(0, 255, 0); 
        
    
        int expectedHue = (HSLMAX / 3);
        
        assertEquals("Tonalità errata per Verde", expectedHue, color.getHue());
    }
  @Test
    public void testInitHSLbyRGB_Hue_BlueMax() {
        color.initHSLbyRGB(0, 0, 255); 
        
        
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////
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
  
     @Test
    public void testHueToRGB_PositiveHue() {
        
        color.initRGBbyHSL(200, 200, 100);
        assertEquals(200, color.getHue());
    }
	
  /////////////////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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

       
        int newR = (int) ((R * fPercent) + (pR * (1.0 - fPercent))); // (254*0.5) + (0*0.5) = 127
        int newG = (int) ((G * fPercent) + (pG * (1.0 - fPercent))); // (0*0.5) + (255*0.5) = 127.5 -> 127
        int newB = (int) ((B * fPercent) + (pB * (1.0 - fPercent))); // (0*0.5) + (0*0.5) = 0
        
        assertEquals("Blend parziale Red errato", newR, color.getRed());
        assertEquals("Blend parziale Green errato", newG, color.getGreen());
        assertEquals("Blend parziale Blue errato", newB, color.getBlue());
    }
   @Test
    public void testReverseColor() {
        int startHue = 20;
        color.initRGBbyHSL(startHue, 200, 100);
        
        color.reverseColor(); 
        
        // Calcolo atteso
        int expectedHue = startHue + (HSLMAX / 2); // 20 + 127 = 147
        
        assertEquals("Inversione colore fallita", expectedHue, color.getHue());
    }
  /////////////////////////////////////////////////////////////////////////////////////////////////////////
  
 

}

						