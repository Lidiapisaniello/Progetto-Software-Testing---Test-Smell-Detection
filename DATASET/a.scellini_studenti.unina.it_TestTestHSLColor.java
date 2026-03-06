/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonella"
Cognome: "Scellini"
Username: a.scellini@studenti.unina.it
UserID: 165
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int UNDEFINED = 170;

    
    private void assertChannelClose(String msg, int expected, int actual, int tolerance) {
        assertTrue(msg + " Expected " + expected + " but was " + actual,
                Math.abs(expected - actual) <= tolerance);
    }

    @Test
    public void testInitHSLbyRGB_Greyscale() {
        HSLColor color = new HSLColor();
        // cMax == cMin
        color.initHSLbyRGB(100, 100, 100);
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
        // pLum = (200*255 + 255) / 510 ~ 100
        assertChannelClose("Lum", 100, color.getLuminence(), 1);
    }

    @Test
    public void testInitHSLbyRGB_RedDominant() {
        HSLColor color = new HSLColor();
        // cMax == R
        color.initHSLbyRGB(255, 0, 0); 
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_GreenDominant() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0); 
        assertEquals(85, color.getHue()); 
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_BlueDominant() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255);
        assertEquals(170, color.getHue()); 
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_HighLuminanceSaturation() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(220, 255, 220);
        
        assertTrue("Luminance should be > 127", color.getLuminence() > 127);
        assertTrue("Saturation should be calculated via high-lum branch", color.getSaturation() > 0);
      
        assertChannelClose("Hue", 85, color.getHue(), 1);
    }
    
    @Test
    public void testInitHSLbyRGB_NegativeHueWrapLogic() {

        HSLColor color = new HSLColor();
        color.initHSLbyRGB(240, 0, 250); 
  
        assertTrue(color.getHue() >= 0);
        assertTrue(color.getHue() <= 255);
    }


    @Test
    public void testInitRGBbyHSL_Greyscale() {
        HSLColor color = new HSLColor();
        // S = 0
        color.initRGBbyHSL(UNDEFINED, 0, 100);
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_LowLuminance() {
        HSLColor color = new HSLColor();
       
        color.initRGBbyHSL(0, 255, 64);
        assertEquals(128, color.getRed()); 
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_HighLuminance() {
        HSLColor color = new HSLColor();
       
        color.initRGBbyHSL(0, 255, 192);
       
        assertTrue(color.getRed() > 128);
        assertTrue(color.getGreen() > 64);
        assertTrue(color.getBlue() > 64);
    }

    @Test
    public void testInitRGBbyHSL_HueToRGB_InternalBranches() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(20, 255, 128); 
        assertChannelClose("Ramo 1 (R)", 255, color.getRed(), 2);

        color.initRGBbyHSL(85, 255, 128);
        assertChannelClose("Ramo 2 (G)", 255, color.getGreen(), 2);

        color.initRGBbyHSL(150, 255, 128); 
        assertTrue("Ramo 3 check", color.getBlue() > 0);

        color.initRGBbyHSL(200, 255, 128);
        assertTrue("Ramo 4 check", color.getRed() > 100);
    }
    
    @Test
    public void testInitRGBbyHSL_HueNormalizationInsideHelper() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(250, 255, 128);
        assertTrue(color.getRed() >= 0 && color.getRed() <= 255);
   
        color.initRGBbyHSL(10, 255, 128);
        assertTrue(color.getBlue() >= 0 && color.getBlue() <= 255);
    }

 

    @Test
    public void testSetHue_MultiLoop() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0,0,0);
  
        color.setHue(300); 
        assertEquals(45, color.getHue());
  
        color.setHue(555); // 555 - 255 - 255 = 45
        assertEquals(45, color.getHue());

        color.setHue(-10);
        assertEquals(245, color.getHue());

        color.setHue(-265); // -265 + 255 = -10 + 255 = 245
        assertEquals(245, color.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255,0,0); // S=255
        
        color.setSaturation(300);
        assertEquals(255, color.getSaturation());
        
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation());
        
        assertEquals(128, color.getRed()); 
    }

    @Test
    public void testSetLuminence_Clamping() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255,0,0); 
        
        color.setLuminence(300);
        assertEquals(255, color.getLuminence());
        assertEquals(255, color.getRed()); 
        
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence());
        assertEquals(0, color.getRed()); 
    }


    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // H=0
        color.reverseColor();
        assertEquals(127, color.getHue()); 
        
        color.initHSLbyRGB(0, 255, 255); 
        color.reverseColor();
        assertEquals(254, color.getHue()); 
    }

    @Test
    public void testBrighten() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 0, 0); // luminanza bassa
        int initialL = color.getLuminence();

        color.brighten(0.0f);
        assertEquals(initialL, color.getLuminence());

        color.brighten(1.0f);
        assertEquals(initialL, color.getLuminence());

        color.brighten(-1.0f);
        assertEquals(0, color.getLuminence());

		color.initHSLbyRGB(250, 250, 250);
		color.brighten(2.0f);

		assertTrue(color.getLuminence() >= 200);
    }

    @Test
    public void testBlend_Limits() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
     
        color.blend(255, 255, 255, 0.0f);
        assertEquals(0, color.getLuminence());
        color.blend(255, 255, 255, -0.5f);
        assertEquals(0, color.getLuminence());
       
        color.blend(255, 255, 255, 1.0f);
        assertEquals(255, color.getLuminence());
        
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 255, 255, 1.5f);
        assertEquals(255, color.getLuminence());
    }
    
    @Test
    public void testBlend_Mixing() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Nero
     
        color.blend(255, 255, 255, 0.5f);
   
        assertChannelClose("Blended Red", 127, color.getRed(), 2);
        assertChannelClose("Blended Green", 127, color.getGreen(), 2);
        assertChannelClose("Blended Blue", 127, color.getBlue(), 2);
    }
}