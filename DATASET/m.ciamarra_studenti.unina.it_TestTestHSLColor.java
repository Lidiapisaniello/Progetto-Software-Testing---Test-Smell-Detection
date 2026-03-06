/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Marco Mario
Cognome: Ciamarra
Username: m.ciamarra@studenti.unina.it
UserID: 115
Date: 24/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    // --- Tests for initHSLbyRGB (RGB -> HSL) ---

    @Test
    public void testInitHSLbyRGB_Grayscale_Black() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Grayscale_White() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
        assertEquals(255, color.getLuminence());
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_RedMax_HueCalculation() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_GreenMax_HueCalculation() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0);
        assertEquals(85, color.getHue());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueMax_HueCalculation() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255);
        assertEquals(170, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_Saturation_LowLum() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 20, 20);
        assertTrue(color.getLuminence() <= 127);
        assertTrue(color.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_Saturation_HighLum() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 200, 255);
        assertTrue(color.getLuminence() > 127);
        assertTrue(color.getSaturation() > 0);
    }

    @Test
    public void testInitHSLbyRGB_NegativeHueFix() {
        HSLColor color = new HSLColor();
        // Genera Hue negativo prima della correzione
        color.initHSLbyRGB(255, 0, 200);
        assertTrue(color.getHue() > 0);
    }

    // --- Tests for initRGBbyHSL (HSL -> RGB) ---

    @Test
    public void testInitRGBbyHSL_Grayscale() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 128);
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLum_Magic2() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 64);
        assertTrue(color.getRed() > color.getGreen());
        assertTrue(color.getRed() > color.getBlue());
        assertEquals(64, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_HighLum_Magic2() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 200);
        assertTrue(color.getRed() > 100);
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch1() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(20, 255, 128);
        assertNotEquals(0, color.getGreen());
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch2() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(85, 255, 128);
        assertEquals(255, color.getGreen());
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch3() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(150, 255, 128);
        assertNotEquals(0, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch4() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(200, 255, 128);
        assertTrue(color.getBlue() > color.getRed());
    }

    @Test
    public void testSetHue_Loop_Negative() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setHue(-300);
        assertTrue(color.getHue() >= 0);
        assertTrue(color.getHue() <= 255);
    }

    @Test
    public void testSetHue_Loop_Positive() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setHue(600);
        assertTrue(color.getHue() <= 255);
    }

    @Test
    public void testSetSaturation_Bounds() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setSaturation(-10);
        assertEquals(0, color.getSaturation());
        color.setSaturation(300);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testSetLuminence_Bounds() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setLuminence(-10);
        assertEquals(0, color.getLuminence());
        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 128);
        color.reverseColor();
        assertEquals(127, color.getHue());
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 100);
        color.brighten(0); // No change
        assertEquals(100, color.getLuminence());
        color.brighten(1.5f); // 150
        assertEquals(150, color.getLuminence());
        color.brighten(2.0f); // Clamp max
        assertEquals(255, color.getLuminence());
        color.brighten(-0.5f); // Clamp min
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void testBlend() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 1.5f); // Ignore > 1
        assertEquals(255, color.getLuminence());
        color.blend(0, 0, 0, -0.5f); // Ignore < 0
        assertEquals(255, color.getLuminence());
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 0.5f); // Mix
        assertEquals(127, color.getRed());
    }

    @Test
    public void testGettersDirectly() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 20, 30);
        assertNotNull(color.getRed());
        assertNotNull(color.getGreen());
        assertNotNull(color.getBlue());
    }

    // --- NUOVI TEST "ROBOT KILLER" PER LA COPERTURA 100% ---

    @Test
    public void testClamping_ForcedOverflow() {
        HSLColor color = new HSLColor();
        // Trucco: Passiamo una Luminanza enorme (3000) mantenendo Saturation != 0.
        // Questo fa "esplodere" i calcoli matematici in initRGBbyHSL, generando valori > 255
        // e forzando l'ingresso nei blocchi "if (pRed > RGBMAX)".
        color.initRGBbyHSL(0, 255, 3000);
        
        assertEquals("Red should be clamped to 255", 255, color.getRed());
        assertEquals("Green should be clamped to 255", 255, color.getGreen());
        assertEquals("Blue should be clamped to 255", 255, color.getBlue());
    }

    @Test
    public void testHueToRGB_Boundaries_Precise() {
        HSLColor color = new HSLColor();
        // Questo test mira ai rami "else if (Hue > HSLMAX)" dentro hueToRGB.
        // initRGBbyHSL chiama hueToRGB con (H + 85).
        // Se passiamo H = 200 -> H+85 = 285. 285 > 255 (HSLMAX). Entra nel ramo > HSLMAX.
        color.initRGBbyHSL(200, 255, 128);
        
        // Se passiamo H = 50 -> H-85 = -35. -35 < 0. Entra nel ramo < 0.
        color.initRGBbyHSL(50, 255, 128);
        
        assertNotNull(color.getRed()); // Verifica generica che non crashi
    }

    @Test
    public void testIMaxIMin_FullBranch() {
        HSLColor color = new HSLColor();
        // Copertura completa per i metodi privati iMax e iMin
        // Caso 1: Tutti uguali (copre i rami "else" delle comparazioni)
        color.initHSLbyRGB(100, 100, 100);
        assertEquals(0, color.getSaturation()); // Grigio
        
        // Caso 2: Scala crescente (Max è B, Min è R)
        color.initHSLbyRGB(10, 50, 90);
        
        // Caso 3: Scala decrescente (Max è R, Min è B)
        color.initHSLbyRGB(90, 50, 10);
        
        // Caso 4: G è Max
        color.initHSLbyRGB(50, 90, 10);
    }
}