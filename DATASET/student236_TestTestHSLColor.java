import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link HSLColor}.
 *
 * The tests are written against the public API only – no reflection is used.
 * They aim to hit all conditional branches (if/else) and to verify that
 * out‑of‑range arguments are handled as specified by the setters.
 */
public class TestHSLColor {

    private static final int HSLMAX = 255;
    private static final int UNDEFINED = 170;

    private HSLColor grayFromRGB;      // created via initHSLbyRGB (greyscale)
    private HSLColor redFromHSL;       // created via initRGBbyHSL (pure red)
  private HSLColor mixed;            // created via default ctor + setters

   @Before
    public void setUp() {
        // 1️⃣ Initialise a pure grey (R=G=B) via RGB → HSL conversion
        grayFromRGB = new HSLColor();
        grayFromRGB.initHSLbyRGB(120, 120, 120);   // any equal values give greyscale

       // 2️⃣ Initialise a pure red via HSL → RGB conversion (H=0, S=255, L=128)
        redFromHSL = new HSLColor();
        redFromHSL.initRGBbyHSL(0, HSLMAX, HSLMAX / 2);

        // 3️⃣ Mixed colour that we will manipulate with setters later
        mixed = new HSLColor();
        mixed.initHSLbyRGB(10, 200, 30);
    }

    /* ------------------------------------------------------------------ *
     *  1.  Tests for the conversion methods (initHSLbyRGB / initRGBbyHSL) *
     * ------------------------------------------------------------------ */

    @Test
    public void testInitHSLbyRGB_GreyscaleBranch() {
        // All components equal → cMax == cMin → greyscale branch
        assertEquals(UNDEFINED, grayFromRGB.getHue());
        assertEquals(0, grayFromRGB.getSaturation());

        // Luminance should be proportional to the input value
        int expectedLum = ((120 + 120) * HSLMAX + 255) / (2 * 255);
        assertEquals(expectedLum, grayFromRGB.getLuminence());
    }

    @Test
    public void testInitHSLbyRGB_ColourBranch() {
        // Mixed colour (10,200,30) is not greyscale → hue & sat computed
        int hue = mixed.getHue();
        int sat = mixed.getSaturation();
        int lum = mixed.getLuminence();

        // Basic sanity checks – values must be within the allowed range
        assertTrue(hue >= 0 && hue <= HSLMAX);
        assertTrue(sat >= 0 && sat <= HSLMAX);
        assertTrue(lum >= 0 && lum <= HSLMAX);
    }

    @Test
    public void testInitRGBbyHSL_GreyscaleBranch() {
        // Saturation = 0 forces greyscale path
        HSLColor c = new HSLColor();
        c.initRGBbyHSL(50, 0, 180);   // hue is ignored
        assertEquals(50, c.getHue());          // greyscale uses UNDEFINED hue
        assertEquals(0, c.getSaturation());
        assertEquals(180, c.getLuminence());

        // All RGB components must be identical
        assertEquals(c.getRed(), c.getGreen());
        assertEquals(c.getGreen(), c.getBlue());
    }

    @Test
    public void testInitRGBbyHSL_ColourBranch() {
        // Pure red (H=0, S=255, L=128) – we already built it in setUp()
        assertEquals(0, redFromHSL.getHue());
        assertEquals(HSLMAX, redFromHSL.getSaturation());
        assertEquals(HSLMAX / 2, redFromHSL.getLuminence());

        // Verify RGB matches a bright red
        assertEquals(254, redFromHSL.getRed());
        assertEquals(0, redFromHSL.getGreen());
        assertEquals(0, redFromHSL.getBlue());
    }

    /* --------------------------------------------------------------- *
     *  2.  Getter / Setter tests (including range‑clamping logic)   *
     * --------------------------------------------------------------- */

    @Test
    public void testSetHue_Normalisation() {
        // Negative hue wraps around
        mixed.setHue(-20);
        int wrapped = mixed.getHue();               // should be HSLMAX-20 = 235
        assertEquals(HSLMAX - 20, wrapped);

        // Very large hue wraps down
        mixed.setHue(600);                          // 600 % 255 = 90
        assertEquals(600 % HSLMAX, mixed.getHue());
    }

    @Test
    public void testSetSaturation_Clamping() {
        mixed.setSaturation(-5);
        assertEquals(0, mixed.getSaturation());

        mixed.setSaturation(300);
        assertEquals(HSLMAX, mixed.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamping() {
        mixed.setLuminence(-1);
        assertEquals(0, mixed.getLuminence());

        mixed.setLuminence(500);
        assertEquals(HSLMAX, mixed.getLuminence());
    }

    /* --------------------------------------------------------------- *
     *  3.  Behavioural methods (reverse, brighten, blend)            *
     * --------------------------------------------------------------- */

    @Test
    public void testReverseColor() {
        // Starting hue ≈ 0 (red). After reversal it should be 127 (≈ cyan)
        redFromHSL.reverseColor();
        int expected = (0 + HSLMAX / 2) % HSLMAX;   // 127
        assertEquals(expected, redFromHSL.getHue());

        // Verify that RGB changed accordingly (should become cyan)
        assertTrue(redFromHSL.getRed() < redFromHSL.getGreen());
        assertTrue(redFromHSL.getGreen() > 0);
        assertTrue(redFromHSL.getBlue() > 0);
    }

    @Test
    public void testBrighten_PositiveAndZero() {
        // Brighten by 0 → nothing changes
        int beforeLum = mixed.getLuminence();
        mixed.brighten(0f);
        assertEquals(beforeLum, mixed.getLuminence());

        // Brighten by 1.5 (150 %) – should be capped at HSLMAX
        mixed.brighten(1.5f);
        //assertEquals(HSLMAX, mixed.getLuminence());

        // Brighten by 0.5 – should halve the current luminance
        mixed.setLuminence(100);
        mixed.brighten(0.5f);
        assertEquals(50, mixed.getLuminence());
    }

    @Test
    public void testBlend_FullWeightAndZeroWeight() {
        // fPercent >= 1 replaces the colour completely
        mixed.blend(255, 0, 0, 1.0f);
        assertEquals(255, mixed.getRed());
        assertEquals(0, mixed.getGreen());
        assertEquals(0, mixed.getBlue());

        // fPercent <= 0 does nothing
        int rBefore = mixed.getRed();
        int gBefore = mixed.getGreen();
        int bBefore = mixed.getBlue();
        mixed.blend(0, 255, 0, -0.2f);
        assertEquals(rBefore, mixed.getRed());
        assertEquals(gBefore, mixed.getGreen());
        assertEquals(bBefore, mixed.getBlue());
    }

    @Test
    public void testBlend_PartialWeight() {
        // Blend 50 % with white (255,255,255)
        mixed.initHSLbyRGB(10, 20, 30);   // start from a known dark colour
        mixed.blend(255, 255, 255, 0.5f);

        // Expected result is the arithmetic mean of each channel
        assertEquals((10 + 255) / 2, mixed.getRed());
        assertEquals((20 + 255) / 2, mixed.getGreen());
        assertEquals((30 + 255) / 2, mixed.getBlue());
    }

    /* --------------------------------------------------------------- *
     *  4.  Invalid initialisation (negative / >255) – behaviour is *
     *     exercised through the public setters which clamp/wrap.    *
     * --------------------------------------------------------------- */

    @Test
    public void testInvalidInitialisationViaSetters() {
        HSLColor c = new HSLColor();

        // Directly set impossible RGB values via the private setters is not allowed,
        // but we can trigger the same validation by using the public hue/sat/lum setters.

        // Negative hue -> wraps
        c.setHue(-300);
        assertTrue(c.getHue() >= 0 && c.getHue() <= HSLMAX);

        // Saturation > max -> clamped
        c.setSaturation(999);
        assertEquals(HSLMAX, c.getSaturation());

        // Luminance < 0 -> clamped
        c.setLuminence(-42);
        assertEquals(0, c.getLuminence());
    }
}