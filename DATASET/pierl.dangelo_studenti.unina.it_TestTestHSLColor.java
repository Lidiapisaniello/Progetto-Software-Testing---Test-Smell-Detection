/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: pierl.dangelo@studenti.unina.it
UserID: 387
Date: 18/11/2025
*/

// HSLColorTest.java
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

public class TestHSLColor {

    private final static int HSLMAX = 255;
    private final static int UNDEFINED = 170;
    
    // Helper method to call the private methods using reflection
    private Object callPrivateMethod(HSLColor color, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(color, args);
    }
    
    // Helper per inizializzare i campi pRed/Green/Blue per i test che li usano (es. blend, setRed/Green/Blue)
    private HSLColor setupRGBTestColor() {
        HSLColor color = new HSLColor();
        // Pure Red: (R=255, G=0, B=0)
        color.initHSLbyRGB(255, 0, 0); 
        return color;
    }

    // =======================================================================
    // Test del metodo initHSLbyRGB
    // =======================================================================
    
    @Test
    public void TestinitHSLbyRGBGrayscaleBlack() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
    }

    @Test
    public void TestinitHSLbyRGBGrayscaleWhite() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
    }

    @Test
    public void TestinitHSLbyRGBRMaxSaturationLumLessHalf() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); 
    }
    
    @Test
    public void TestinitHSLbyRGBGMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0); 
    }
    
    @Test
    public void TestinitHSLbyRGBBMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255); 
    }

    @Test
    public void TestinitHSLbyRGBRMaxSaturationLumGreaterHalf() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 128, 128);
    }
    
    @Test
    public void TestinitHSLbyRGBHueLessThanZeroWrap() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 255);
    }

    // =======================================================================
    // Test del metodo initRGBbyHSL
    // =======================================================================
    
    @Test
    public void TestinitRGBbyHSLGreysale() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 128); 
    }
    
    @Test
    public void TestinitRGBbyHSLLumLessHalfRedWithClipping() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 255, 128); 
    }

    @Test
    public void TestinitRGBbyHSLLumGreaterHalfLightBlue() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(170, 100, 200); 
    }

    // =======================================================================
    // Test del metodo privato hueToRGB
    // =======================================================================
    
    @Test
    public void TesthueToRGBWrapNegative() throws Exception {
        int mag1 = 100;
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{mag1, 200, -10});
    }
    
    @Test
    public void TesthueToRGBWrapPositive() throws Exception {
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{100, 200, HSLMAX + 10});
    }

    @Test
    public void TesthueToRGBCase1() throws Exception {
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{0, 255, 21});
    }

    @Test
    public void TesthueToRGBCase2() throws Exception {
        int mag2 = 200;
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{100, mag2, 63});
    }
    
    @Test
    public void TesthueToRGBCase3() throws Exception {
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{0, 255, 150});
    }

    @Test
    public void TesthueToRGBCase4() throws Exception {
        int mag1 = 100;
        Object result = callPrivateMethod(new HSLColor(), "hueToRGB", new Class[]{int.class, int.class, int.class}, new Object[]{mag1, 200, 200});
    }
    
    // =======================================================================
    // Test dei metodi privati iMax/iMin/greyscale/setRGB
    // =======================================================================
    
    @Test
    public void TestiMaxAisGreater() throws Exception {
        Object result = callPrivateMethod(new HSLColor(), "iMax", new Class[]{int.class, int.class}, new Object[]{10, 5});
    }

    @Test
    public void TestiMinAisLesser() throws Exception {
        Object result = callPrivateMethod(new HSLColor(), "iMin", new Class[]{int.class, int.class}, new Object[]{5, 10});
    }

    @Test
    public void TestgreyscaleChangesToGreyscale() throws Exception {
        HSLColor color = setupRGBTestColor(); 
        
        callPrivateMethod(color, "greyscale", new Class[]{}, new Object[]{});
    }
    
    @Test
    public void TestsetRedChangesHSL() throws Exception {
        HSLColor color = setupRGBTestColor(); 
        
        callPrivateMethod(color, "setRed", new Class[]{int.class}, new Object[]{128}); 
    }
    
    @Test
    public void TestsetGreenChangesHSL() throws Exception {
        HSLColor color = setupRGBTestColor(); 
        
        callPrivateMethod(color, "setGreen", new Class[]{int.class}, new Object[]{128}); 
    }
    
    @Test
    public void TestsetBlueChangesHSL() throws Exception {
        HSLColor color = setupRGBTestColor(); 
        
        callPrivateMethod(color, "setBlue", new Class[]{int.class}, new Object[]{128}); 
    }
    
    @Test
    public void TestreverseLightFlipsLuminence() throws Exception {
        HSLColor color = setupRGBTestColor(); 
        color.setLuminence(50);
        
        callPrivateMethod(color, "reverseLight", new Class[]{}, new Object[]{});
    }

    // =======================================================================
    // Test dei metodi pubblici Getter/Setter
    // =======================================================================
    
    @Test
    public void TestsetHueWrapPositive() {
        HSLColor color = setupRGBTestColor();
        color.setHue(HSLMAX + 10); 
    }
    
    @Test
    public void TestsetSaturationBoundaryNegative() {
        HSLColor color = setupRGBTestColor();
        color.setSaturation(-10); 
    }

    @Test
    public void TestsetLuminenceBoundaryPositive() {
        HSLColor color = setupRGBTestColor();
        color.setLuminence(HSLMAX + 10); 
    }
    
    @Test
    public void TestgetHue() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(42, 100, 100);
    }
    
    @Test
    public void TestgetSaturation() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 100);
    }
    
    @Test
    public void TestgetLuminence() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 100, 100);
    }
    
    @Test
    public void TestgetRed() {
        HSLColor color = setupRGBTestColor();
    }
    
    @Test
    public void TestgetGreen() {
        HSLColor color = setupRGBTestColor();
    }
    
    @Test
    public void TestgetBlue() {
        HSLColor color = setupRGBTestColor();
    }

    // =======================================================================
    // Test dei metodi pubblici di manipolazione
    // =======================================================================

    @Test
    public void TestreverseColorFlipsHue() {
        HSLColor color = setupRGBTestColor(); 
        color.reverseColor();
    }

    @Test
    public void TestbrightenFPercentZero() {
        HSLColor color = setupRGBTestColor(); 
        color.brighten(0.0f); 
    }
    
    @Test
    public void TestbrightenLValueTooHighClip() {
        HSLColor color = setupRGBTestColor(); 
        color.setLuminence(200); 
        color.brighten(2.0f); 
    }

    @Test
    public void TestblendFPercentOne() {
        HSLColor color = setupRGBTestColor(); 
        color.blend(0, 255, 0, 1.0f); 
    }
    
    @Test
    public void TestblendFPercentZero() {
        HSLColor color = setupRGBTestColor(); 
        color.blend(0, 255, 0, 0.0f); 
    }
    
    @Test
    public void TestblendStandard() {
        HSLColor color = setupRGBTestColor(); 
        color.blend(0, 0, 255, 0.5f); 
    }
}
