/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Ange"
Cognome: "Ragozzino"
Username: ange.ragozzino@studenti.unina.it
UserID: 249
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {
    
    private HSLColor hsl;
    private static final int HSLMAX = 255;
    private static final int RGBMAX = 255;
    private static final int UNDEFINED = 170;

    @BeforeClass
    public static void setUpClass() {
        // Setup globale se necessario
    }

    @AfterClass
    public static void tearDownClass() {
        // Cleanup globale se necessario
    }

    @Before
    public void setUp() {
        hsl = new HSLColor();
    }

    @After
    public void tearDown() {
        hsl = null;
    }

    /**
     * Testa il caso di scala di grigi (R=G=B).
     * Copre il ramo: if (cMax == cMin)
     */
    @Test
    public void testInitHSLbyRGB_Greyscale() {
        // Grigio medio
        hsl.initHSLbyRGB(128, 128, 128);
        assertEquals("Saturation should be 0 for greyscale", 0, hsl.getSaturation());
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, hsl.getHue());
        // Verifica Lum: ((128*255 + 255) / 510) = 64 (approx) -> Logica interna: cPlus=256.
        // (256*255 + 255) / 510 = 128.
        assertEquals(128, hsl.getLuminence());
        
        // Nero (Boundary basso)
        hsl.initHSLbyRGB(0, 0, 0);
        assertEquals(0, hsl.getLuminence());
        assertEquals(0, hsl.getSaturation());
        
        // Bianco (Boundary alto)
        hsl.initHSLbyRGB(255, 255, 255);
        assertEquals(255, hsl.getLuminence());
        assertEquals(0, hsl.getSaturation());
    }

    /**
     * Testa i colori primari per coprire i rami del calcolo dell'Hue.
     * Copre: if (cMax == R), if (cMax == G), if (cMax == B)
     */
    @Test
    public void testInitHSLbyRGB_PrimaryColors() {
        // RED Case (cMax == R)
        hsl.initHSLbyRGB(255, 0, 0); 
        // Hue calcolato dovrebbe essere 0
        assertEquals("Red Hue", 0, hsl.getHue());
        assertEquals("Red Sat", 255, hsl.getSaturation());
        assertEquals("Red Lum", 127, hsl.getLuminence()); // (255+0)/2 approx

        // GREEN Case (cMax == G)
        hsl.initHSLbyRGB(0, 255, 0);
        // Hue: (DSLMAX/3) + ... approx 85
        assertEquals("Green Hue", 85, hsl.getHue());

        // BLUE Case (cMax == B)
        hsl.initHSLbyRGB(0, 0, 255);
        // Hue: (2*HSLMAX/3) + ... approx 170
        assertEquals("Blue Hue", 170, hsl.getHue());
    }

    /**
     * Testa la soglia di luminanza per il calcolo della saturazione.
     * Copre: if (pLum <= (HSLMAX / 2)) e l'else corrispondente.
     * HSLMAX/2 = 127.
     */
    @Test
    public void testInitHSLbyRGB_SaturationThreshold() {
        // Caso 1: Lum <= 127.
        // R=50, G=50, B=0 -> cMax=50, cMin=0. cPlus=50. Lum = (50*255+255)/510 = 25.
        // 25 <= 127 -> True
        hsl.initHSLbyRGB(50, 50, 0);
        assertTrue("Lum should be <= 127", hsl.getLuminence() <= 127);
        assertEquals(255, hsl.getSaturation()); // Saturazione piena

        // Caso 2: Lum > 127.
        // R=255, G=255, B=200. cMax=255, cMin=200. cPlus=455. 
        // Lum = (455*255 + 255) / 510 = 227.
        // 227 > 127 -> False (Else branch)
        hsl.initHSLbyRGB(255, 255, 200);
        assertTrue("Lum should be > 127", hsl.getLuminence() > 127);
        // Verifica che la saturazione sia calcolata correttamente nel ramo else
        // cMinus = 55. Formula: (55*255 + 0.5) / (510 - 455) = 14025.5 / 55 = 255
        assertEquals(255, hsl.getSaturation());
    }
    
    /**
     * Testa il wrapping dell'Hue negativo durante la conversione RGB->HSL.
     * Questo è difficile da attivare con colori standard, ma testiamo
     * che il codice gestisca i rami matematici.
     * Un caso tipico per pHue < 0 è quando cMax==R e B > G.
     */
    @Test
    public void testInitHSLbyRGB_NegativeHueWrap() {
        // R=255, G=0, B=10. Max=R.
        // Hue formula base: BDelta - GDelta. 
        // GDelta sarà alto, BDelta basso -> risultato negativo -> wrap adding HSLMAX.
        hsl.initHSLbyRGB(255, 0, 20);
        // Verifichiamo solo che Hue sia positivo e valido
        assertTrue(hsl.getHue() >= 0 && hsl.getHue() <= 255);
        // Hue dovrebbe essere vicino a 255 (rosso-violaceo)
        assertTrue(hsl.getHue() > 230); 
    }

    /**
     * Testa la conversione HSL -> RGB (initRGBbyHSL).
     * Copre: S == 0 (Greyscale)
     */
    @Test
    public void testInitRGBbyHSL_Greyscale() {
        hsl.initRGBbyHSL(0, 0, 128);
        assertEquals(128, hsl.getRed());
        assertEquals(128, hsl.getGreen());
        assertEquals(128, hsl.getBlue());
    }

    /**
     * Testa la conversione HSL -> RGB con colori vivi.
     * Copre i rami di hueToRGB e la logica Magic1/Magic2.
     */
    @Test
    public void testInitRGBbyHSL_Colors() {
        // Rosso puro H=0, S=255, L=127
        hsl.initRGBbyHSL(0, 255, 127);
        // A causa di arrotondamenti interi, ci aspettiamo valori vicini a 255, 0, 0
        assertEquals(255, hsl.getRed());
        // Tolleranza di 1 per arrotondamenti
        assertEquals(0, hsl.getGreen(), 1);
        assertEquals(0, hsl.getBlue(), 1);
        
        // Test per coprire il ramo "L > HSLMAX/2" in initRGBbyHSL
        hsl.initRGBbyHSL(0, 255, 200); // Light Red
        assertTrue(hsl.getRed() > 200);
        assertTrue(hsl.getGreen() > 100); // Deve essere schiarito
    }

    /**
     * Testa intensivo per il metodo privato hueToRGB.
     * Dobbiamo passare HUE che cadono nelle diverse regioni:
     * < 1/6, < 1/2, < 2/3, e il resto.
     * HSLMAX = 255.
     * 1/6 = 42.5
     * 1/2 = 127.5
     * 2/3 = 170
     */
    @Test
    public void testHueToRGB_Coverage() {
        int S = 255;
        int L = 128;
        
        // Regione 1: Hue < 42 (Già coperto da Rosso H=0)
        hsl.initRGBbyHSL(20, S, L);
        
        // Regione 2: Hue < 127 (es. 85 = Verde)
        // return mag2
        hsl.initRGBbyHSL(85, S, L);
        assertEquals(255, hsl.getGreen(), 2);

        // Regione 3: Hue < 170 (es. 150)
        hsl.initRGBbyHSL(150, S, L);
        
        // Regione 4: Else (es. 200)
        hsl.initRGBbyHSL(200, S, L);
    }
    
    /**
     * Testa i Getter e Setter con logiche di clamping e wrapping.
     */
    @Test
    public void testSettersAndGetters() {
        // setHue - wrapping
        hsl.setHue(300); // > 255 -> 300-255 = 45
        assertEquals(45, hsl.getHue());
        
        hsl.setHue(-50); // < 0 -> 255-50 = 205
        assertEquals(205, hsl.getHue());
        
        // setSaturation - clamping
        hsl.setSaturation(300);
        assertEquals(255, hsl.getSaturation());
        hsl.setSaturation(-10);
        assertEquals(0, hsl.getSaturation());
        
        // setLuminence - clamping
        hsl.setLuminence(300);
        assertEquals(255, hsl.getLuminence());
        hsl.setLuminence(-10);
        assertEquals(0, hsl.getLuminence());
        
        // Verify RGB update on set
        hsl.initHSLbyRGB(0, 0, 0); // Reset
        hsl.setLuminence(255); // Should become white
        assertEquals(255, hsl.getRed());
    }

    /**
     * Testa reverseColor.
     */
    @Test
    public void testReverseColor() {
        hsl.setHue(0); // Red
        hsl.reverseColor();
        // 0 + 255/2 = 127
        assertEquals(127, hsl.getHue());
    }

    /**
     * Testa brighten.
     */
    @Test
    public void testBrighten() {
        hsl.setLuminence(100);
        
        // Percent 0 -> return immediato
        hsl.brighten(0);
        assertEquals(100, hsl.getLuminence());
        
        // Percent valido
        hsl.brighten(1.5f);
        assertEquals(150, hsl.getLuminence());
        
        // Overflow
        hsl.brighten(10.0f);
        assertEquals(255, hsl.getLuminence());
        
        // Underflow logic check (sebbene float * int sia difficile che dia < 0 se input > 0)
        // Reset a qualcosa
        hsl.setLuminence(100);
        hsl.brighten(-0.5f); // Dovrebbe scurire tecnicamente, o clamp a 0 se logica errata
        // Il codice fa cast a int. 100 * -0.5 = -50 -> clamp a 0.
        assertEquals(0, hsl.getLuminence());
    }

    /**
     * Testa blend.
     */
    @Test
    public void testBlend() {
        // Setup base color: Black
        hsl.initHSLbyRGB(0, 0, 0);
        
        // Blend with White, 50%
        hsl.blend(255, 255, 255, 0.5f);
        // newR = 255*0.5 + 0 = 127
        assertEquals(127, hsl.getRed());
        
        // Blend >= 1 -> Full replace
        hsl.blend(10, 10, 10, 1.1f);
        assertEquals(10, hsl.getRed());
        
        // Blend <= 0 -> No change
        hsl.blend(255, 255, 255, -0.1f);
        assertEquals(10, hsl.getRed());
    }
    
    /**
     * Test specifico per il metodo hueToRGB con argomenti fuori range.
     * Poiché hueToRGB è privato, lo sollecitiamo tramite initRGBbyHSL 
     * passando valori che, sommando l'offset, sballano.
     */
    @Test
    public void testHueToRGB_RangeChecks() {
        // HSLMAX = 255.
        // initRGBbyHSL chiama hueToRGB(..., H + HSLMAX/3).
        // Se H è alto, H+offset sfora.
        
        // Set H to max. Offset will push it over HSLMAX.
        // Internal logic: if (Hue > HSLMAX) Hue = Hue - HSLMAX;
        hsl.initRGBbyHSL(250, 128, 128);
        // Verifica che non crashi e produca valori validi
        assertTrue(hsl.getRed() >= 0 && hsl.getRed() <= 255);
        
        // Set H very low (using setter logic to bypass initial checks if possible, 
        // but setter wraps. We depend on internal offsets in initRGBbyHSL to trigger ranges).
        // hueToRGB viene chiamato anche con H - HSLMAX/3.
        // Se H è piccolo (es 10), 10 - 85 = -75.
        // Internal logic: if (Hue < 0) Hue = Hue + HSLMAX;
        hsl.initRGBbyHSL(10, 128, 128);
        assertTrue(hsl.getBlue() >= 0 && hsl.getBlue() <= 255);
    }
}