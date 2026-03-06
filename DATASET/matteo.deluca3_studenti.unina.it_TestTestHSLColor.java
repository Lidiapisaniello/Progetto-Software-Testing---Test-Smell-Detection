/*
Nome: Matteo
Cognome: De Luca
Username: matteo.deluca3@studenti.unina.it
UserID: 621
Date: 28/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHSLColor {

    private static HSLColor sharedColor;
    private HSLColor color;

    @BeforeClass
    public static void setUpClass() {
        sharedColor = new HSLColor();
        sharedColor.initHSLbyRGB(0, 0, 0);
    }

    @AfterClass
    public static void tearDownClass() {
        sharedColor = null;
    }

    @Before
    public void setUp() {
        color = new HSLColor();
    }

    @After
    public void tearDown() {
        color = null;
    }

    @Test
    public void testInitHSLbyRGB_red() {
        int R = 255;
        int G = 0;
        int B = 0;

        color.initHSLbyRGB(R, G, B);

        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());

        assertTrue(color.getSaturation() > 0);

        int hue = color.getHue();
        assertTrue(hue >= 0 && hue <= 255);

        int lum = color.getLuminence();
        assertTrue(lum >= 120 && lum <= 135);
    }

    @Test
    public void testInitRGBbyHSL_greyscale() {
        int H = 170;
        int S = 0;
        int L = 128;

        color.initRGBbyHSL(H, S, L);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        assertEquals(r, g);
        assertEquals(g, b);
        assertTrue(r >= 120 && r <= 135);

        assertEquals(H, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(L, color.getLuminence());
    }

    @Test
    public void testSetHue_normalizzaValoriFuoriRange() {
        color.initRGBbyHSL(100, 200, 150);

        color.setHue(-10);
        int hueAfterNegative = color.getHue();
        assertTrue(hueAfterNegative >= 0 && hueAfterNegative <= 255);

        color.setHue(300);
        int hueAfterTooHigh = color.getHue();
        assertTrue(hueAfterTooHigh >= 0 && hueAfterTooHigh <= 255);
    }

    @Test
    public void testSetSaturation_clampValori() {
        color.initRGBbyHSL(50, 100, 120);

        color.setSaturation(-5);
        assertEquals(0, color.getSaturation());
        assertTrue(color.getRed() >= 0 && color.getRed() <= 255);
        assertTrue(color.getGreen() >= 0 && color.getGreen() <= 255);
        assertTrue(color.getBlue() >= 0 && color.getBlue() <= 255);

        color.setSaturation(999);
        assertEquals(255, color.getSaturation());
        assertTrue(color.getRed() >= 0 && color.getRed() <= 255);
        assertTrue(color.getGreen() >= 0 && color.getGreen() <= 255);
        assertTrue(color.getBlue() >= 0 && color.getBlue() <= 255);
    }

    @Test
    public void testSetLuminence_clampValori() {
        color.initRGBbyHSL(80, 220, 150);

        color.setLuminence(-100);
        assertEquals(0, color.getLuminence());
        assertTrue(color.getRed() >= 0 && color.getRed() <= 255);
        assertTrue(color.getGreen() >= 0 && color.getGreen() <= 255);
        assertTrue(color.getBlue() >= 0 && color.getBlue() <= 255);

        color.setLuminence(999);
        assertEquals(255, color.getLuminence());
        assertTrue(color.getRed() >= 0 && color.getRed() <= 255);
        assertTrue(color.getGreen() >= 0 && color.getGreen() <= 255);
        assertTrue(color.getBlue() >= 0 && color.getBlue() <= 255);
    }

    @Test
    public void testBrighten_zeroNonModifica() {
        color.initRGBbyHSL(100, 200, 80);
        int beforeLum = color.getLuminence();

        color.brighten(0.0f);

        assertEquals(beforeLum, color.getLuminence());
    }

    @Test
    public void testBrighten_negativoClampZero() {
        color.initRGBbyHSL(100, 200, 80);

        color.brighten(-2.0f);

        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBrighten_valoreAltoClamp255() {
        color.initRGBbyHSL(100, 200, 80);

        color.brighten(5.0f);

        assertEquals(255, color.getLuminence());
    }

    @Test
    public void testBrighten_scalareInRange() {
        color.initRGBbyHSL(100, 200, 80);

        color.brighten(1.5f);

        int lum = color.getLuminence();
        assertTrue(lum >= 118 && lum <= 122);
    }

    @Test
    public void testBlend_percentMaggioreUgualeUno() {
        color.initHSLbyRGB(255, 0, 0);

        color.blend(0, 255, 0, 1.0f);

        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void testBlend_percentMinoreUgualeZero() {
        color.initHSLbyRGB(10, 20, 30);

        color.blend(200, 150, 100, 0.0f);

        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    @Test
    public void testBlend_percentIntermedio() {
        color.initHSLbyRGB(100, 200, 50);

        color.blend(200, 100, 0, 0.5f);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        assertTrue(r >= 145 && r <= 155);
        assertTrue(g >= 145 && g <= 155);
        assertTrue(b >= 20 && b <= 30);
    }

    @Test
    public void testReverseColor() {
        color.initRGBbyHSL(200, 100, 100);
        int originalHue = color.getHue();

        color.reverseColor();

        int newHue = color.getHue();
        assertNotEquals(originalHue, newHue);
        assertTrue(newHue >= 0 && newHue <= 255);
    }
}

						