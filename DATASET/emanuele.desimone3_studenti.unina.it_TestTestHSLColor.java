/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Emanuele"
Cognome: "De Simone"
Username: emanuele.desimone3@studenti.unina.it
UserID: 428
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class TestHSLColor {
	
	// Valori costanti per gli assert, mirroring della classe HSLColor
	private final static int HSLMAX = 255;
	private final static int RGBMAX = 255;
	private final static int UNDEFINED = 170; // 255 * 2 / 3
	
	private HSLColor color;
	
	@BeforeClass
	public static void setUpClass() {}
				
	@AfterClass
	public static void tearDownClass() {}
				
	@Before
	public void setUp() {
		color = new HSLColor();
	}
				
	@After
	public void tearDown() {
		color = null;
	}
	
	// --- Metodo di utilità per la Reflection ---
	
	private Object invokePrivateMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) throws Exception {
		Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(obj, args);
	}

	// =================================================================================
	// TEST PER initHSLbyRGB (Calcoli e Limiti)
	// =================================================================================

	@Test
	public void testInitHSLbyRGB_Red() {
		color.initHSLbyRGB(255, 0, 0);
		assertEquals("R deve essere 255", 255, color.getRed());
		assertEquals("Hue per Rosso deve essere 0", 0, color.getHue());
		assertEquals("Luminence per Rosso pieno deve essere 128", 128, color.getLuminence());
		assertEquals("Saturation per Rosso pieno deve essere 255", 255, color.getSaturation());
	}
	
	@Test
	public void testInitHSLbyRGB_Green() {
		color.initHSLbyRGB(0, 255, 0);
		assertEquals("Hue per Verde pieno deve essere circa HSLMAX/3", HSLMAX / 3, color.getHue());
		assertEquals("Luminence per Verde pieno deve essere 128", 128, color.getLuminence());
		assertEquals("Saturation per Verde pieno deve essere 255", 255, color.getSaturation());
	}
	
	@Test
	public void testInitHSLbyRGB_Blue() {
		color.initHSLbyRGB(0, 0, 255);
		assertEquals("Hue per Blu pieno deve essere circa 2*HSLMAX/3", (2 * HSLMAX) / 3, color.getHue());
		assertEquals("Luminence per Blu pieno deve essere 128", 128, color.getLuminence());
		assertEquals("Saturation per Blu pieno deve essere 255", 255, color.getSaturation());
	}

	@Test
	public void testInitHSLbyRGB_Greyscale() {
		color.initHSLbyRGB(128, 128, 128);
		assertEquals("Saturation per scala di grigi deve essere 0", 0, color.getSaturation());
		assertEquals("Hue per scala di grigi deve essere UNDEFINED", UNDEFINED, color.getHue());
		assertEquals("Luminence deve essere 128", 128, color.getLuminence());
	}
    
	@Test
	public void testInitHSLbyRGB_AbsoluteLimits() {
		color.initHSLbyRGB(0, 0, 0);
		assertEquals("Luminence per Nero deve essere 0", 0, color.getLuminence());

		color.initHSLbyRGB(255, 255, 255);
		assertEquals("Luminence per Bianco deve essere 255", 255, color.getLuminence());
	}
	
	@Test
	public void testInitHSLbyRGB_RedMax_HueNonZero() {
		// Input (255, 50, 0) forzano Luminance=128 e Hue=9 (valori empiricamente corretti)
		color.initHSLbyRGB(255, 50, 0); 
		assertEquals("Luminence", 128, color.getLuminence());
		assertEquals("Hue", 9, color.getHue());
	}
    
	@Test
	public void testInitHSLbyRGB_NegativeHueWrap() {
		color.initHSLbyRGB(255, 20, 255);
		assertTrue("Hue deve essere positivo dopo il wrap", color.getHue() >= 0);
		assertTrue("Hue (Magenta) dovrebbe essere alto", color.getHue() > 200);
	}


	// =================================================================================
	// TEST PER initRGBbyHSL (Conversione HSL -> RGB)
	// =================================================================================

	@Test
	public void testInitRGBbyHSL_ColorLowLuminance() {
		color.initRGBbyHSL(42, 255, 64);
		assertEquals("Red deve essere 131", 131, color.getRed());
		assertEquals("Green deve essere 128", 128, color.getGreen());
		assertEquals("Blue deve essere 0", 0, color.getBlue());
	}

	@Test
	public void testInitRGBbyHSL_ColorHighLuminance() {
		color.initRGBbyHSL(42, 255, 200);
		assertTrue("Green deve essere 255", color.getGreen() == 255);
	}
	
	@Test
	public void testInitRGBbyHSL_Greyscale() {
		color.initRGBbyHSL(100, 0, 150);
		assertEquals("Red", 150, color.getRed());
		assertEquals("Green", 150, color.getGreen());
		assertEquals("Blue", 150, color.getBlue());
	}
	
	@Test
	public void testInitRGBbyHSL_HueToRGB_AllBranches() {
		color.initRGBbyHSL(0, 255, 128); 
		assertTrue("Red deve essere 255 (H=0)", color.getRed() == 255);

		color.initRGBbyHSL(85, 255, 128); 
		assertTrue("Green deve essere 255 (H=85)", color.getGreen() == 255);
		
		color.initRGBbyHSL(170, 255, 128); 
		assertTrue("Blue deve essere 255 (H=170)", color.getBlue() == 255);
	}
    
    // NUOVO TEST (modificato): Forziamo un Hue nel range intermedio (H=100) per coprire la riga pBlue
    @Test
	public void testInitRGBbyHSL_BlueCalculation_MidHue() {
		// Hue = 100 (tra Verde 85 e Blu 170), S=255, L=128
		color.initRGBbyHSL(100, 255, 128); 
		
		// Green dovrebbe essere 255 (Max)
		assertEquals("Green deve essere 255", 255, color.getGreen());
		
		// Blue e Red devono essere calcolati da hueToRGB
		assertTrue("Blue deve essere maggiore di 0", color.getBlue() > 0);
		assertTrue("Red deve essere maggiore di 0", color.getRed() > 0);
	}

    // TENTATIVO FINALE: Attivazione ramo High Hue
	@Test
	public void testInitRGBbyHSL_HueToRGB_HighHueRamo() {
		color.initRGBbyHSL(200, 255, 128); 
		
		assertEquals("Blue deve essere 255 per H=200", 255, color.getBlue());
		assertTrue("Red deve essere >= 0", color.getRed() >= 0);
		assertTrue("Green deve essere <= 255", color.getGreen() <= 255); 
	}


	// =================================================================================
	// TEST PER Getters e Setters (Copertura wrap/clamp/else if di Hue)
	// =================================================================================
	
	@Test
	public void testSetHue_WrapPositive() {
        // Copre il ramo: else if (Hue > HSLMAX)
		color.setHue(300);
		assertEquals("Hue > HSLMAX deve wrappare a 45", 45, color.getHue()); 
	}
	
	@Test
	public void testSetHue_WrapNegative() {
		color.setHue(-10);
		assertEquals("Hue negativo deve wrappare a 245", 245, color.getHue());
	}

	@Test
	public void testSetSaturation_ClampNegative() {
		color.setSaturation(-10);
		assertEquals("Saturation negativa deve essere clmapata a 0", 0, color.getSaturation());
	}

	@Test
	public void testSetSaturation_ClampPositive() {
		color.setSaturation(300);
		assertEquals("Saturation troppo alta deve essere clmapata a 255", HSLMAX, color.getSaturation());
	}

	@Test
	public void testSetLuminence_ClampNegative() {
		color.setLuminence(-10);
		assertEquals("Luminence negativa deve essere clmapata a 0", 0, color.getLuminence());
	}

	@Test
	public void testSetLuminence_ClampPositive() {
		color.setLuminence(300);
		assertEquals("Luminence troppo alta deve essere clmapata a 255", HSLMAX, color.getLuminence());
	}
	
	@Test
	public void testGettersAndSetters_Normal() {
		color.initHSLbyRGB(10, 20, 30);
		assertEquals(10, color.getRed());
		color.setHue(100);
		assertEquals(100, color.getHue());
	}


	// =================================================================================
	// TEST PER Metodi Pubblici Aggiuntivi
	// =================================================================================

	@Test
	public void testReverseColor() {
		color.initHSLbyRGB(255, 0, 0); 
		color.reverseColor();
		assertEquals("Hue invertito deve essere circa 127", HSLMAX / 2, color.getHue());
	}

	@Test
	public void testBrighten_Normal() {
		color.initHSLbyRGB(100, 100, 100); 
		color.brighten(1.5f); 
		assertEquals("Luminence deve aumentare a 150", 150, color.getLuminence());
	}
	
	@Test
	public void testBrighten_NegativeClamp() {
		color.initHSLbyRGB(100, 100, 100); 
		color.brighten(-1.0f);
		assertEquals("Luminence deve essere clmapata a 0", 0, color.getLuminence());
	}

	@Test
	public void testBlend_Normal() {
		color.initHSLbyRGB(10, 10, 10);
		color.blend(200, 200, 200, 0.5f);
		assertEquals("Red dopo blend 50%", 105, color.getRed());
	}
	
	@Test
	public void testBlend_GreaterThanOne() {
		color.initHSLbyRGB(10, 10, 10);
		color.blend(200, 200, 200, 1.5f);
		assertEquals("Il colore deve diventare 200, 200, 200", 200, color.getRed());
	}

	@Test
	public void testBlend_LessThanZero() {
		color.initHSLbyRGB(10, 10, 10);
		color.blend(200, 200, 200, -0.5f);
		assertEquals("Il colore non deve cambiare", 10, color.getRed());
	}

	// =================================================================================
	// TEST PER Metodi Privati Inutilizzati (Reflection)
	// =================================================================================
	
	@Test
	public void testPrivate_greyscale() throws Exception {
		color.initHSLbyRGB(255, 0, 0); 
		invokePrivateMethod(color, "greyscale", new Class<?>[]{}, new Object[]{});
		assertEquals("Saturation deve essere 0", 0, color.getSaturation());
	}

	@Test
	public void testPrivate_setRed() throws Exception {
		color.initHSLbyRGB(0, 10, 20); 
		invokePrivateMethod(color, "setRed", new Class<?>[]{int.class}, new Object[]{100});
		assertEquals("Red deve essere 100", 100, color.getRed());
	}
	
	@Test
	public void testPrivate_setGreen() throws Exception {
		color.initHSLbyRGB(10, 0, 20); 
		invokePrivateMethod(color, "setGreen", new Class<?>[]{int.class}, new Object[]{100});
		assertEquals("Green deve essere 100", 100, color.getGreen());
	}
	
	@Test
	public void testPrivate_setBlue() throws Exception {
		color.initHSLbyRGB(10, 20, 0); 
		invokePrivateMethod(color, "setBlue", new Class<?>[]{int.class}, new Object[]{100});
		assertEquals("Blue deve essere 100", 100, color.getBlue());
	}
    
	@Test
	public void testPrivate_reverseLight() throws Exception {
		color.initHSLbyRGB(255, 0, 0);
		invokePrivateMethod(color, "reverseLight", new Class<?>[]{}, new Object[]{});
		assertEquals("Luminence invertita deve essere 127", 127, color.getLuminence());
	}
}

/*
ho utilizzato Gemini come IA, il prompt iniziale inviato è questo:
mi puoi generare una classe di test per questa classe che mi copre il 100% del codice:
(ho copiato e incollato la classe)
parti da qui, inserendo i test e gli elementi i setup e teardown:
(ho copiato e incollato la base della classe di test)
il codice copre il 98,95%, oltre non riesce ad andare
le maggiori difficoltà sono state nell'effettuare i calcoli per generare i test
*/

