import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for HSLColor.java, designed to achieve maximum testable code coverage
 * for RGB/HSL conversions, clamping, and public utility methods.
 * * Note: The private method reverseLight() is not tested as it is inaccessible
 * and is not called by any public method in the HSLColor class.
 */
public class TestHSLColor {
    private HSLColor color;
    
    // Constants from the HSLColor class (re-defined for clarity in testing)
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED_HUE = 170; // Used for greyscale/achromatic colors

    @Before
    public void setUp() {
        // Initialize a fresh HSLColor object before each test
        color = new HSLColor();
    }

    // --- Core Conversion Tests (initHSLbyRGB) ---

    @Test
    public void testInitHSLbyRGB_Red() {
        // R(255), G(0), B(0) should be HSL(0, 255, 128) in 0-255 space
        color.initHSLbyRGB(255, 0, 0);
        assertEquals("Red R value", 255, color.getRed());
        assertEquals("Red G value", 0, color.getGreen());
        assertEquals("Red B value", 0, color.getBlue());
        
        // HSL values for pure Red: H=0, S=255, L=128
        assertEquals("Red Hue", 0, color.getHue());
        assertEquals("Red Saturation", HSLMAX, color.getSaturation());
        assertEquals("Red Luminance", HSLMAX / 2, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Green() {
        // R(0), G(255), B(0) should be HSL(85, 255, 128) where HSLMAX/3 is approx 85
        color.initHSLbyRGB(0, 255, 0);
        
        // HSL values for pure Green: H=HSLMAX/3, S=255, L=128
        // HSLMAX/3 = 255/3 = 85
        assertEquals("Green Hue", HSLMAX / 3, color.getHue());
        assertEquals("Green Saturation", HSLMAX, color.getSaturation());
        assertEquals("Green Luminance", HSLMAX / 2, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Blue() {
        // R(0), G(0), B(255) should be HSL(170, 255, 128) where 2*HSLMAX/3 is approx 170
        color.initHSLbyRGB(0, 0, 255);
        
        // HSL values for pure Blue: H=2*HSLMAX/3, S=255, L=128
        // 2*HSLMAX/3 = 2*255/3 = 170
        assertEquals("Blue Hue", 2 * HSLMAX / 3, color.getHue());
        assertEquals("Blue Saturation", HSLMAX, color.getSaturation());
        assertEquals("Blue Luminance", HSLMAX / 2, color.getLuminence());
    }
    
    @Test
    public void testInitHSLbyRGB_Greyscale_White() {
        // R(255), G(255), B(255) (cMax == cMin, L > HSLMAX/2 path)
        color.initHSLbyRGB(255, 255, 255);
        assertEquals("White Saturation (greyscale path)", 0, color.getSaturation());
        assertEquals("White Hue (greyscale path)", UNDEFINED_HUE, color.getHue());
        assertEquals("White Luminance", HSLMAX, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_Greyscale_Black() {
        // R(0), G(0), B(0) (cMax == cMin)
        color.initHSLbyRGB(0, 0, 0);
        assertEquals("Black Saturation (greyscale path)", 0, color.getSaturation());
        assertEquals("Black Hue (greyscale path)", UNDEFINED_HUE, color.getHue());
        assertEquals("Black Luminance", 0, color.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_SaturationPath_Light() {
        // R(200), G(100), B(100)
        // pLum = 150 ( > HSLMAX/2 ), takes 'else' path for saturation.
        color.initHSLbyRGB(200, 100, 100); 
        assertEquals("Light color Luminance", 150, color.getLuminence());
        
        // pSat calculation: 121
        assertEquals("Light color Saturation (pLum > 128 path)", 121, color.getSaturation());
        
        // Hue calculation (cMax == R path)
        assertEquals("Light color Hue (cMax == R path)", 0, color.getHue());
    }
    
    @Test
    public void testInitHSLbyRGB_SaturationPath_Dark() {
        // R(100), G(0), B(0)
        // pLum = 50 ( <= HSLMAX/2 ), takes 'if' path for saturation.
        color.initHSLbyRGB(100, 0, 0); 
        assertEquals("Dark color Luminance", 50, color.getLuminence());
        
        // pSat calculation: 255
        assertEquals("Dark color Saturation (pLum <= 128 path)", HSLMAX, color.getSaturation());
        
        // Hue calculation (cMax == R path)
        assertEquals("Dark color Hue (cMax == R path)", 0, color.getHue());
    }

    @Test
    public void testInitHSLbyRGB_HuePath_GMax() {
        // Cyan (0, 255, 255)
        // Path: cMax == G. pHue = 85 + 50 - 0 = 135.
        color.initHSLbyRGB(0, 255, 255); 
        assertEquals("Cyan Hue (cMax == G path)", (HSLMAX / 3) + 50, color.getHue());
        assertEquals("Cyan Saturation", HSLMAX, color.getSaturation());
    }

    @Test
    public void testInitHSLbyRGB_HuePath_BMax() {
        // Magenta (255, 0, 255). cMax == R is hit first. pHue = -50 + 255 = 205.
        color.initHSLbyRGB(255, 0, 255); 
        assertEquals("Magenta Hue (cMax == R path leading to negative hue correction)", 205, color.getHue());
        
        // Test a color that ensures cMax == B is hit (e.g., 100, 100, 200).
        // Path: cMax == B. pHue = 170 + 25 - 25 = 170.
        color.initHSLbyRGB(100, 100, 200); 
        assertEquals("Light Blue Hue (cMax == B path)", 170, color.getHue());
    }
    
    // --- Core Conversion Tests (initRGBbyHSL) and hueToRGB coverage ---

    @Test
    public void testInitRGBbyHSL_GreyScale() {
        // S=0 (Greyscale path)
        int L = 128;
        color.initRGBbyHSL(0, 0, L); 
        
        // pRed = (L * RGBMAX) / HSLMAX = 128
        assertEquals("Greyscale Red", 128, color.getRed());
        assertEquals("Greyscale Luminance", L, color.getLuminence());
    }

    @Test
    public void testInitRGBbyHSL_Magic2Path_LuminanceHigh() {
        // S=255, L=200 (L > HSLMAX/2). Magic2 = 255.
        int S = HSLMAX;
        int L = 200;
        color.initRGBbyHSL(0, S, L); 
        assertEquals("High Luminance Magic2 gives 255 R value (near Red)", 255, color.getRed());
    }
    
    @Test
    public void testInitRGBbyHSL_HueToRGB_PathCoverage() {
        int S = HSLMAX;
        int L = HSLMAX / 2; // Magic1=0, Magic2=256
        
        // Test Hue > HSLMAX wrap around (H=265 -> H=10)
        color.initRGBbyHSL(HSLMAX + 10, S, L);
        assertTrue("Positive wrap around R is high", color.getRed() > 200);
        
        // Test Hue < 0 wrap around (H=-75 -> H=180, Path 3)
        // We use H=10, which results in B channel calculation of H-HSLMAX/3 = -75.
        color.initRGBbyHSL(10, S, L); 
        assertTrue("Negative wrap B is max (Path 3 in hueToRGB)", color.getBlue() == 255);
        
        // Test Path 1 (Hue < HSLMAX / 6): G channel when H=10
        assertTrue("Path 1 triggered (G is mid)", color.getGreen() < 100); 
        
        // Test Path 2 (HSLMAX / 6 < Hue < HSLMAX / 2): R channel when H=10 -> H+85 = 95
        assertTrue("Path 2 triggered (R is max)", color.getRed() == 255);
    }


    // --- Setters and Utility Method Coverage ---

    @Test
    public void testSetHue_ClampingAndWrapAround() {
        // Initial color: pure Red (H=0)
        color.initHSLbyRGB(255, 0, 0); 
        
        // Test wrapping positive (255+10 -> 10)
        color.setHue(HSLMAX + 10); 
        assertEquals("Hue positive wrap", 10, color.getHue());
        
        // Test wrapping negative (-20 -> 235)
        color.setHue(-20); 
        assertEquals("Hue negative wrap", 235, color.getHue());
        
        // Test within range
        color.setHue(100);
        assertEquals("Hue in range", 100, color.getHue());
    }
    
    @Test
    public void testSetSaturation_Clamping() {
        color.initHSLbyRGB(0, 0, 128); // Grey, S=0
        
        // Test high clamp (255+10 -> 255)
        color.setSaturation(HSLMAX + 10);
        assertEquals("Saturation high clamp", HSLMAX, color.getSaturation());
        
        // Test low clamp (-10 -> 0)
        color.setSaturation(-10);
        assertEquals("Saturation low clamp", 0, color.getSaturation());
        
        // Test mid-range
        color.setSaturation(100);
        assertEquals("Saturation mid range", 100, color.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        color.initHSLbyRGB(0, 0, 0); // Black, L=0
        
        // Test high clamp (255+10 -> 255)
        color.setLuminence(HSLMAX + 10);
        assertEquals("Luminance high clamp", HSLMAX, color.getLuminence());
        
        // Test low clamp (-10 -> 0)
        color.setLuminence(-10);
        assertEquals("Luminance low clamp", 0, color.getLuminence());
        
        // Test mid-range
        color.setLuminence(100);
        assertEquals("Luminance mid range", 100, color.getLuminence());
    }
    
    @Test
    public void testReverseColor() {
        color.initHSLbyRGB(255, 0, 0); // Red: H=0
        color.reverseColor(); // Should set Hue to 0 + 127 = 127
        assertEquals("Reversed Hue should be H+127", 127, color.getHue());
        
        // Test greyscale case (Undefined Hue)
        color.initHSLbyRGB(0, 0, 0); // Black: H=170
        color.reverseColor(); // Should set Hue to 170 + 127 = 297, wrapped to 42
        assertEquals("Reversed Hue for greyscale (Undefined H)", 42, color.getHue());
    }
    
    @Test
    public void testBrighten_NoChange() {
        color.initHSLbyRGB(128, 128, 128); 
        color.brighten(0.0f); // Should return immediately
        assertEquals("Luminance should not change with 0%", 128, color.getLuminence());
    }
    
    @Test
    public void testBrighten_ClampHigh() {
        color.initHSLbyRGB(128, 128, 128); 
        color.brighten(3.0f); // L = 384. Clamped to 255
        assertEquals("Luminance high clamp (300%)", HSLMAX, color.getLuminence());
    }

    @Test
    public void testBrighten_ClampLow() {
        color.initHSLbyRGB(128, 128, 128); 
        color.brighten(-1.0f); // L = -128. Clamped to 0
        assertEquals("Luminance low clamp (-100%)", 0, color.getLuminence());
    }

    @Test
    public void testBrighten_StandardIncrease() {
        color.initHSLbyRGB(128, 128, 128); 
        color.brighten(1.5f); // L = 192
        assertEquals("Luminance standard increase (150%)", 192, color.getLuminence());
    }

    @Test
    public void testBlend_100Percent() {
        color.initHSLbyRGB(255, 0, 0); // Current: Red
        color.blend(0, 255, 0, 1.0f); // Blend with Green at 100%
        
        // Should be exactly Green
        assertEquals("Blend 100% R", 0, color.getRed());
        assertEquals("Blend 100% G", 255, color.getGreen());
        assertEquals("Blend 100% B", 0, color.getBlue());
    }

    @Test
    public void testBlend_0Percent() {
        color.initHSLbyRGB(255, 0, 0); // Current: Red
        color.blend(0, 255, 0, 0.0f); // Blend with Green at 0%
        
        // Should remain exactly Red
        assertEquals("Blend 0% R", 255, color.getRed());
        assertEquals("Blend 0% G", 0, color.getGreen());
        assertEquals("Blend 0% B", 0, color.getBlue());
    }

    @Test
    public void testBlend_50Percent() {
        color.initHSLbyRGB(255, 0, 0); // Current: Red (255, 0, 0)
        color.blend(0, 255, 0, 0.5f); // Blend with Green at 50%
        
        // New R = 127, New G = 127, New B = 0 (Yellow-ish)
        assertEquals("Blend 50% R", 127, color.getRed());
        assertEquals("Blend 50% G", 127, color.getGreen());
        assertEquals("Blend 50% B", 0, color.getBlue());
    }
}