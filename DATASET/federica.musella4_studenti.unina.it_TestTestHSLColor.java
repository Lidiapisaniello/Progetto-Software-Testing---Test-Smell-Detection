/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Federica"
Cognome: "Musella"
Username: federica.musella4@studenti.unina.it
UserID: 131
Date: 21/11/2025
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
    private final static int UNDEFINED = 170; 

	@BeforeClass
	public static void setUpClass() {
	}
				
	@AfterClass
	public static void tearDownClass() {
	}
				
	@Before
	public void setUp() {
        color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); 
	}
				
	@After
	public void tearDown() {
	}
				
	// --- TEST initHSLbyRGB ---
    
    @Test
    public void testInitHSLbyRGB_Red() {
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(0, color.getHue());
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        color.initHSLbyRGB(0, 255, 0);
        assertEquals(HSLMAX / 3, color.getHue()); 
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        color.initHSLbyRGB(0, 0, 255);
        assertEquals((2 * HSLMAX) / 3, color.getHue()); 
        assertEquals(HSLMAX, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_LuminenceLow() {
        color.initHSLbyRGB(100, 0, 0);
        assertEquals(50, color.getLuminence());
        assertEquals(HSLMAX, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_LuminenceHigh() {
        color.initHSLbyRGB(255, 255, 155);
        assertEquals(205, color.getLuminence());
        assertEquals(HSLMAX, color.getSaturation());
    }
    
    @Test
    public void testInitHSLbyRGB_GreyscaleBlack() {
        color.initHSLbyRGB(0, 0, 0);
        assertEquals(0, color.getSaturation()); 
        assertEquals(UNDEFINED, color.getHue()); 
        assertEquals(0, color.getLuminence()); 
    }

    @Test
    public void testInitHSLbyRGB_GreyscaleWhite() {
        color.initHSLbyRGB(255, 255, 255);
        assertEquals(0, color.getSaturation()); 
        assertEquals(UNDEFINED, color.getHue()); 
        assertEquals(HSLMAX, color.getLuminence()); 
    }

    @Test
    public void testInitHSLbyRGB_HueNegativeCorrection() {
        color.initHSLbyRGB(255, 150, 200);
        assertEquals(235, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_SmallDifference() {
        color.initHSLbyRGB(10, 12, 10);
        assertEquals(12, color.getGreen());
        assertNotEquals(0, color.getSaturation());
    }
    
    @Test
    public void testInitHSLbyRGB_ForceIMaxEquality() {
        // Forza il ramo "else" (o uguaglianza) dentro iMax/iMin passando valori identici
        color.initHSLbyRGB(10, 10, 10); // Grigio
        assertEquals(0, color.getSaturation());
        
        color.initHSLbyRGB(10, 10, 5); // R=G > B
        assertEquals(10, color.getRed());
    }

	// --- TEST initRGBbyHSL ---

    @Test
    public void testInitRGBbyHSL_Greyscale() {
        color.initRGBbyHSL(50, 0, 127);
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LuminenceLow() {
        color.initRGBbyHSL(0, HSLMAX, 100);
        assertEquals(200, color.getRed());
        assertEquals(0, color.getGreen()); 
        assertEquals(0, color.getBlue()); 
    }

    @Test
    public void testInitRGBbyHSL_LuminenceHigh() {
        color.initRGBbyHSL(85, HSLMAX, 200);
        assertEquals(145, color.getRed()); 
        assertEquals(255, color.getGreen()); 
        assertEquals(145, color.getBlue()); 
    }

    @Test
    public void testInitRGBbyHSL_LuminenceBoundary() {
        // Mutation Test: L=127 (<= HSLMAX/2) vs L=128 (> HSLMAX/2)
        color.initRGBbyHSL(0, 255, 127);
        int r127 = color.getRed();
        
        color.initRGBbyHSL(0, 255, 128);
        int r128 = color.getRed();
        
        assertNotEquals(r127, r128);
    }
    
    @Test
    public void testInitRGBbyHSL_Magic2Rounding() {
        // TEST SPECIFICO PER MUTATION KILLING:
        // L=2, S=64. 
        // Magic2 con arrotondamento (+127) = 3.
        // Magic2 senza arrotondamento (mutante) = 2.
        // Questo cambierà il valore RGB finale di 1 unità.
        color.initRGBbyHSL(0, 64, 2);
        
        // Magic2=3 -> Magic1 = 2*2 - 3 = 1.
        // Red (H=0+85=85 -> Branch2) -> ritorna Magic2 = 3.
        // pRed = (3 * 255 + 127) / 255 = 3.
        
        // Se il mutante rimuove l'arrotondamento:
        // Magic2=2 -> Magic1 = 2*2 - 2 = 2.
        // Red -> ritorna Magic2 = 2.
        // pRed = 2.
        
        assertEquals(3, color.getRed());
    }

    @Test
    public void testInitRGBbyHSL_RedOverflow() {
        color.initRGBbyHSL(0, HSLMAX, HSLMAX);
        assertEquals(HSLMAX, color.getRed());
    }

    @Test
    public void testInitRGBbyHSL_ForceOverflow_AllChannels() {
        int[] testHues = {0, 85, 170}; 
        for (int h : testHues) {
            color.initRGBbyHSL(h, 255, 500);
            assertEquals(255, color.getRed());
            assertEquals(255, color.getGreen());
            assertEquals(255, color.getBlue());
        }
    }

	// --- TEST hueToRGB ---
	
    @Test
    public void testHueToRGB_ExactBoundaries() {
        int mag1 = 25;
        int mag2 = 200;

        // Boundary 1: 42
        assertEquals(196, callHueToRGB(mag1, mag2, 41));
        assertEquals(200, callHueToRGB(mag1, mag2, 42));

        // Boundary 2: 127
        assertEquals(200, callHueToRGB(mag1, mag2, 126));
        assertEquals(204, callHueToRGB(mag1, mag2, 127));

        // Boundary 3: 170
        assertEquals(29, callHueToRGB(mag1, mag2, 169));
        assertEquals(25, callHueToRGB(mag1, mag2, 170));
    }

    @Test
    public void testHueToRGB_RoundingSensitivity() {
        // Uccide i mutanti che rimuovono "+ (HSLMAX / 12)"
        int mag1 = 10;
        int mag2 = 50; // Delta = 40
        int hue = 1;
        
        int result = callHueToRGB(mag1, mag2, hue);
        assertEquals(11, result);
    }

    @Test
    public void testHueToRGB_HueWrapLow() {
        int result = callHueToRGB(100, 200, -1);
        assertEquals(100, result);
    }

    @Test
    public void testHueToRGB_HueWrapHigh() {
        int result = callHueToRGB(25, 200, 256);
        assertEquals(29, result);
    }

	// --- TEST Getters & Setters HSL ---
	
	@Test
	public void testGettersHSL() {
        color.initHSLbyRGB(100, 50, 200);
		assertEquals(184, color.getHue());
		assertEquals(153, color.getSaturation());
		assertEquals(125, color.getLuminence()); 
	}

    @Test
    public void testSetHue_Valid() {
        color.setHue(200);
        assertEquals(200, color.getHue());
        assertNotEquals(255, color.getRed()); 
    }

    @Test
    public void testSetHue_WrapLow() {
        color.setHue(-1); 
        assertEquals(254, color.getHue());
    }
    
    @Test
    public void testSetHue_WrapHigh() {
        color.setHue(HSLMAX + 10); 
        assertEquals(10, color.getHue());
    }
    
    @Test
    public void testSetHue_MultiWrap() {
        color.setHue(HSLMAX * 2 + 10); 
        assertEquals(10, color.getHue());
        color.setHue(-HSLMAX - 1); 
        assertEquals(254, color.getHue());
    }

    @Test
    public void testSetSaturation_Valid() {
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
    }

    @Test
    public void testSetSaturation_ClampLow() {
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void testSetSaturation_ClampHigh() {
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation());
    }

    @Test
    public void testSetLuminence_Valid() {
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void testSetLuminence_ClampLow() {
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testSetLuminence_ClampHigh() {
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence());
    }

	// --- TEST Getters & Setters RGB (Privati) ---

	@Test
	public void testGettersRGB() {
		assertEquals(255, color.getRed());
		assertEquals(0, color.getGreen());
		assertEquals(0, color.getBlue());
	}

    @Test
    public void testSetRed() {
        invokePrivateSetter("setRed", 10);
        assertEquals(10, color.getRed());
        assertEquals(0, color.getHue()); 
        assertEquals(HSLMAX, color.getSaturation()); 
    }
    
    @Test
    public void testSetGreen() {
        invokePrivateSetter("setGreen", 10);
        assertEquals(10, color.getGreen());
        assertEquals(2, color.getHue());
    }
    
    @Test
    public void testSetBlue() {
        invokePrivateSetter("setBlue", 10);
        assertEquals(10, color.getBlue());
        assertEquals(253, color.getHue()); 
    }

	// --- TEST Metodi Speciali ---

    @Test
    public void testReverseColor() {
        int initialHue = color.getHue(); 
        color.reverseColor();
        assertEquals(initialHue + (HSLMAX / 2), color.getHue()); 
    }

    @Test
    public void testReverseLight() {
        int initialLum = color.getLuminence();
        invokePrivateMethod("reverseLight");
        assertEquals(HSLMAX - initialLum, color.getLuminence()); 
    }

    @Test
    public void testGreyscale() {
        int initialLum = color.getLuminence();
        invokePrivateMethod("greyscale");
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
        assertEquals(initialLum, color.getLuminence());
        assertEquals(128, color.getRed());
    }

    @Test
    public void testBrighten_ZeroPercent() {
        int initialLum = color.getLuminence(); 
        color.brighten(0.0f);
        assertEquals(initialLum, color.getLuminence());
    }

    @Test
    public void testBrighten_PositivePercent() {
        color.brighten(1.5f); 
        assertEquals(192, color.getLuminence());
    }

    @Test
    public void testBrighten_NegativeL_Corrected() {
        color.initHSLbyRGB(255, 255, 255);
        color.brighten(-0.5f);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampHigh() {
        color.brighten(3.0f);
        assertEquals(HSLMAX, color.getLuminence());
    }

    @Test
    public void testBlend_MaxPercent() {
        color.blend(0, 0, 255, 1.0f);
        assertEquals(170, color.getHue()); 
    }

    @Test
    public void testBlend_MinPercent() {
        color.blend(0, 0, 255, 0.0f);
        assertEquals(0, color.getHue());
    }

    @Test
    public void testBlend_Asymmetric() {
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.25f);
        assertEquals(63, color.getRed());
        assertEquals(63, color.getGreen());
        assertEquals(63, color.getBlue());
    }
	
	// --- Metodi di utilità (iMax, iMin) ---

    @Test
    public void testIMax_AisMax() {
        assertEquals(10, callIMax(10, 5));
    }

    @Test
    public void testIMax_BisMax() {
        assertEquals(10, callIMax(5, 10));
    }
    
    @Test
    public void testIMax_Equal() {
        assertEquals(10, callIMax(10, 10));
    }

    @Test
    public void testIMin_AisMin() {
        assertEquals(5, callIMin(5, 10));
    }

    @Test
    public void testIMin_BisMin() {
        assertEquals(5, callIMin(10, 5));
    }
    
    @Test
    public void testIMin_Equal() {
        assertEquals(10, callIMin(10, 10));
    }
	
	// --- Metodi di supporto per la Reflection ---

    private void invokePrivateMethod(String methodName) {
        try {
            Method method = HSLColor.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(color);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection Error: " + methodName);
        }
    }

    private void invokePrivateSetter(String methodName, int value) {
        try {
            Method method = HSLColor.class.getDeclaredMethod(methodName, int.class);
            method.setAccessible(true);
            method.invoke(color, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection Error: " + methodName);
        }
    }

    private int callHueToRGB(int mag1, int mag2, int hue) {
        try {
            Method method = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(color, mag1, mag2, hue);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection Error: hueToRGB");
            return -1;
        }
    }
    
    private int callIMax(int a, int b) {
        try {
            Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(color, a, b);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection Error: iMax");
            return -1;
        }
    }

    private int callIMin(int a, int b) {
        try {
            Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(color, a, b);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("Reflection Error: iMin");
            return -1;
        }
    }
}
						