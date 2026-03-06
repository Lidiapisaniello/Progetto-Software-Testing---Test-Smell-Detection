/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Luciano"
Cognome: "Balzano"
Username: luci.balzano@studenti.unina.it
UserID: 110
Date: 25/10/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class TestHSLColor {
	HSLColor pippo;
    @Before
    public void setUp() throws Exception {
        pippo=new HSLColor();
        assumeNotNull(pippo);
    }

    @After
    public void tearDown() throws Exception {
        pippo=null;
        assumeTrue(pippo==null);
    }

    @Test
    public void initHSLbyRGB() {
        pippo.initHSLbyRGB(0,255,0);
        assertEquals(0,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(0,pippo.getBlue());
        assertEquals(85,pippo.getHue());
        assertEquals(255,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
    }

    @Test
    public void initHSLbyRGB_red() {
        pippo.initHSLbyRGB(255,0,0);
        assertEquals(255,pippo.getRed());
        assertEquals(0,pippo.getGreen());
        assertEquals(0,pippo.getBlue());
        assertEquals(0,pippo.getHue());
        assertEquals(255,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
    }

    @Test
    public void initHSLbyRGB_blue() {
        pippo.initHSLbyRGB(0,0,255);
        assertEquals(0,pippo.getRed());
        assertEquals(0,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
        assertEquals(170,pippo.getHue());
        assertEquals(255,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
    }

    @Test
    public void initHSLbyRGB_black() {
        pippo.initHSLbyRGB(0,0,0);
        assertEquals(0,pippo.getRed());
        assertEquals(0,pippo.getGreen());
        assertEquals(0,pippo.getBlue());
        assertEquals(170,pippo.getHue());
        assertEquals(0,pippo.getSaturation());
        assertEquals(0,pippo.getLuminence());
    }

    @Test
    public void initHSLbyRGB_white() {
        pippo.initHSLbyRGB(255,255,255);
        assertEquals(255,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
        assertEquals(170,pippo.getHue());
        assertEquals(0,pippo.getSaturation());
        assertEquals(255,pippo.getLuminence());
    }
  
  	@Test
    public void initHSLbyRGB_casual() {
        pippo.initHSLbyRGB(250,-1,1);
        assertEquals(250,pippo.getRed());
        assertEquals(-1,pippo.getGreen());
        assertEquals(1,pippo.getBlue());
        assertEquals(254,pippo.getHue());
        assertEquals(257,pippo.getSaturation());
        assertEquals(125,pippo.getLuminence());
    }

    @Test
    public void initRGBbyHSL_black() {
        pippo.initRGBbyHSL(0,0,255);
        assertEquals(0,pippo.getHue());
        assertEquals(0,pippo.getSaturation());
        assertEquals(255,pippo.getLuminence());
        assertEquals(255,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
    }

    @Test
    public void initRGBbyHSL_1() {
        pippo.initRGBbyHSL(184,77,128);
        assertEquals(184,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
        assertEquals(115,pippo.getRed());
        assertEquals(90,pippo.getGreen());
        assertEquals(166,pippo.getBlue());
    }

    @Test
    public void initRGBbyHSL_2() {
        pippo.initRGBbyHSL(184,77,102);
        assertEquals(184,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        assertEquals(92,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
    }

    @Test
    public void initRGBbyHSL_3() {
        pippo.initRGBbyHSL(-65,77,102);
        assertEquals(-65,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        assertEquals(101,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
    }

    @Test
    public void initRGBbyHSL_4() {
        pippo.initRGBbyHSL(160,77,102);
        assertEquals(160,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
    }
	
  	@Test
    public void initRGBbyHSL_5() {
        pippo.initRGBbyHSL(10,55,3000);
        assertEquals(10,pippo.getHue());
        assertEquals(55,pippo.getSaturation());
        assertEquals(3000,pippo.getLuminence());
        assertEquals(255,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
    }


    @Test
    public void setHue() {
        pippo.initRGBbyHSL(184,77,102);
        assertEquals(184,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        assertEquals(92,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        pippo.setHue(190);
        assertEquals(190,pippo.getHue());
        assertEquals(101,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        pippo.setHue(-65);
        assertEquals(190,pippo.getHue());
        assertEquals(101,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        pippo.setHue(700);
        assertEquals(190,pippo.getHue());
        assertEquals(101,pippo.getRed());
        assertEquals(71,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
    }


    @Test
    public void setSaturation() {
        pippo.initRGBbyHSL(184,77,128);
        assertEquals(184,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
        assertEquals(115,pippo.getRed());
        assertEquals(90,pippo.getGreen());
        assertEquals(166,pippo.getBlue());
        pippo.setSaturation(100);
        assertEquals(100,pippo.getSaturation());
        assertEquals(111,pippo.getRed());
        assertEquals(78,pippo.getGreen());
        assertEquals(178,pippo.getBlue());
        pippo.setSaturation(-60);
        assertEquals(0,pippo.getSaturation());
        assertEquals(128,pippo.getRed());
        assertEquals(128,pippo.getGreen());
        assertEquals(128,pippo.getBlue());
        pippo.setSaturation(280);
        assertEquals(255,pippo.getSaturation());
        assertEquals(86,pippo.getRed());
        assertEquals(1,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
    }

    @Test
    public void setLuminence() {
        pippo.initRGBbyHSL(184,77,128);
        assertEquals(184,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
        assertEquals(115,pippo.getRed());
        assertEquals(90,pippo.getGreen());
        assertEquals(166,pippo.getBlue());
        pippo.setLuminence(60);
        assertEquals(60,pippo.getLuminence());
        assertEquals(54,pippo.getRed());
        assertEquals(42,pippo.getGreen());
        assertEquals(78,pippo.getBlue());
        pippo.setLuminence(-30);
        assertEquals(0,pippo.getLuminence());
        assertEquals(0,pippo.getRed());
        assertEquals(0,pippo.getGreen());
        assertEquals(0,pippo.getBlue());
        pippo.setLuminence(300);
        assertEquals(255,pippo.getLuminence());
        assertEquals(255,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
    }


    @Test
    public void reverseColor() {
        pippo.initHSLbyRGB(71,86,133);
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        assertEquals(159,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        pippo.reverseColor();
        assertEquals(31,pippo.getHue());
        assertEquals(133,pippo.getRed());
        assertEquals(117,pippo.getGreen());
        assertEquals(71,pippo.getBlue());
    }

    @Test
    public void brighten() {
        pippo.initHSLbyRGB(71,86,133);
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        assertEquals(159,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        pippo.brighten(0);
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        assertEquals(159,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        pippo.brighten(0.5F);
        assertEquals(51,pippo.getLuminence());
        assertEquals(36,pippo.getRed());
        assertEquals(44,pippo.getGreen());
        assertEquals(66,pippo.getBlue());
        pippo.brighten(100);
        assertEquals(255,pippo.getLuminence());
        assertEquals(255,pippo.getRed());
        assertEquals(255,pippo.getGreen());
        assertEquals(255,pippo.getBlue());
        pippo.brighten(-23);
        assertEquals(0,pippo.getLuminence());
        assertEquals(0,pippo.getRed());
        assertEquals(0,pippo.getGreen());
        assertEquals(0,pippo.getBlue());
    }

    @Test
    public void blend() {
        pippo.initHSLbyRGB(71,86,133);
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        assertEquals(159,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        pippo.blend(115,90,166,0);
        assertEquals(71,pippo.getRed());
        assertEquals(86,pippo.getGreen());
        assertEquals(133,pippo.getBlue());
        assertEquals(159,pippo.getHue());
        assertEquals(77,pippo.getSaturation());
        assertEquals(102,pippo.getLuminence());
        pippo.blend(115,90,166,1);
        assertEquals(115,pippo.getRed());
        assertEquals(90,pippo.getGreen());
        assertEquals(166,pippo.getBlue());
        assertEquals(184,pippo.getHue());
        assertEquals(76,pippo.getSaturation());
        assertEquals(128,pippo.getLuminence());
        pippo.blend(71,86,133, 0.5F);
        assertEquals(93, pippo.getRed());
        assertEquals(88,pippo.getGreen());
        assertEquals(149,pippo.getBlue());
        assertEquals(174,pippo.getHue());
        assertEquals(65,pippo.getSaturation());
        assertEquals(119,pippo.getLuminence());
    }
}

						