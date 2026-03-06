import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor { // Il nome della classe corrisponde al nome del file

    // Costanti della classe sorgente (Assumendo HSLMAX = 255, RGBMAX = 255, UNDEFINED = 170)
    private final static int UNDEFINED = 170;
    private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;

    // --- Metodi Helper ---

    // Inizializza l'oggetto con Rosso puro (R=255, G=0, B=0)
    private HSLColor initNonGrey() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(RGBMAX, 0, 0);
        return color;
    }

    // Inizializza l'oggetto con Grigio 50% (R=128, G=128, B=128)
    private HSLColor initGrey() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        return color;
    }

    // --- Test per initHSLbyRGB (RGB -> HSL) ---

    @Test
    public void TestInitHSLbyRGB_GreyScale() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        assertEquals("Hue should be UNDEFINED for greyscale", UNDEFINED, color.getHue());
        assertEquals("Saturation should be 0 for greyscale", 0, color.getSaturation());
        assertEquals("Luminence should be 128 for 50% grey", 128, color.getLuminence());
        assertEquals("Red should be 128", 128, color.getRed());
    }

    @Test
    public void TestInitHSLbyRGB_SaturationCase1() {
        HSLColor color = new HSLColor();
        // Blu scuro (0, 0, 128)
        color.initHSLbyRGB(0, 0, 128);
        assertEquals("Luminence should be 64", 64, color.getLuminence());
        assertEquals("Saturation should be 255 (Max/Min)", 255, color.getSaturation());
    }

    @Test
    public void TestInitHSLbyRGB_SaturationCase2() {
        HSLColor color = new HSLColor();
        // Blu chiaro (128, 128, 255) -> L=((383 * 255) + 255) / 510 = 192.07 -> 192.
        // **CORRETTO**
        color.initHSLbyRGB(128, 128, 255);
        assertEquals("Luminence should be 192", 192, color.getLuminence());
        assertEquals("Saturation should be 255 (Max/Min)", 255, color.getSaturation());
    }

    @Test
    public void TestInitHSLbyRGB_HueCase1RMax() {
        HSLColor color = new HSL