/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonio"
Cognome: "Auricchio"
Username: antonio.auricchio5@studenti.unina.it
UserID: 597
Date: 22/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

// La classe è stata rinominata in TestHSLColor per corrispondere al nome del file TestHSLColor.java
public class TestHSLColor { 

    // --- Metodi Helper per Reflection (per testare iMax, iMin, hueToRGB, greyscale, reverseLight e setter RGB privati) ---

    private int callIMax(int a, int b) throws Exception {
        // Usiamo HSLColor.class.getDeclaredMethod per accedere ai metodi privati
        Method method = HSLColor.class.getDeclaredMethod("iMax", int.class, int.class);
        method.setAccessible(true);
        HSLColor instance = new HSLColor();
        return (int) method.invoke(instance, a, b);
    }

    private int callIMin(int a, int b) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("iMin", int.class, int.class);
        method.setAccessible(true);
        HSLColor instance = new HSLColor();
        return (int) method.invoke(instance, a, b);
    }

    private int callHueToRGB(int mag1, int mag2, int hue) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("hueToRGB", int.class, int.class, int.class);
        method.setAccessible(true);
        HSLColor instance = new HSLColor();
        return (int) method.invoke(instance, mag1, mag2, hue);
    }

    private void callGreyscale(HSLColor color) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        method.invoke(color);
    }

    private void callReverseLight(HSLColor color) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        method.invoke(color);
    }

    private void callSetRed(HSLColor color, int value) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        method.invoke(color, value);
    }

    private void callSetGreen(HSLColor color, int value) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        method.invoke(color, value);
    }

    private void callSetBlue(HSLColor color, int value) throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        method.invoke(color, value);
    }

    // --- Test per iMax (Metodo Privato) ---

    @Test
    public void iMaxAGreaterThanBTest() throws Exception {
        assertEquals(10, callIMax(10, 5));
    }

    @Test
    public void iMaxALessThanBTest() throws Exception {
        assertEquals(10, callIMax(5, 10));
    }

    @Test
    public void iMaxAEqualsBTest() throws Exception {
        assertEquals(7, callIMax(7, 7));
    }

    // --- Test per iMin (Metodo Privato) ---

    @Test
    public void iMinALessThanBTest() throws Exception {
        assertEquals(5, callIMin(5, 10));
    }

    @Test
    public void iMinAGreaterThanBTest() throws Exception {
        assertEquals(5, callIMin(10, 5));
    }

    @Test
    public void iMinAEqualsBTest() throws Exception {
        assertEquals(7, callIMin(7, 7));
    }

    // --- Test per initHSLbyRGB ---

    @Test
    public void initHSLbyRGBRedMaxIsRTest() {
        HSLColor color = new HSLColor();
        // Rosso puro (R=255, G=0, B=0): cMax=R, H=0
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(0, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void initHSLbyRGBGreenMaxIsGTest() {
        HSLColor color = new HSLColor();
        // Verde puro (R=0, G=255, B=0): cMax=G, H=85
        color.initHSLbyRGB(0, 255, 0);
        assertEquals(85, color.getHue());
    }
    
    @Test
    public void initHSLbyRGBBlueMaxIsBTest() {
        HSLColor color = new HSLColor();
        // Blu puro (R=0, G=0, B=255): cMax=B, H=170
        color.initHSLbyRGB(0, 0, 255);
        assertEquals(170, color.getHue());
    }

    @Test
    public void initHSLbyRGBGreyScaleTest() {
        HSLColor color = new HSLColor();
        // Grigio (R=128, G=128, B=128): cMax=cMin -> Sat=0, Hue=UNDEFINED(170)
        color.initHSLbyRGB(128, 128, 128);
        assertEquals(170, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void initHSLbyRGBSaturationLumLowTest() {
        HSLColor color = new HSLColor();
        // Lum <= HSLMAX/2 (50 <= 127). R=100, G=50, B=0. Lum=50.
        color.initHSLbyRGB(100, 50, 0);
        assertEquals(50, color.getLuminence());
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void initHSLbyRGBSaturationLumHighTest() {
        HSLColor color = new HSLColor();
        // Lum > HSLMAX/2 (150 > 127). R=200, G=100, B=150. Lum=150.
        color.initHSLbyRGB(200, 100, 150);
        assertEquals(150, color.getLuminence());
        assertEquals(121, color.getSaturation()); // Calcolato: 121
    }

    @Test
    public void initHSLbyRGBNegativeHueCorrectionTest() {
        HSLColor color = new HSLColor();
        // cMax=R. Risultato H=-17. Corretto: -17 + 255 = 238
        color.initHSLbyRGB(255, 50, 100);
        assertEquals(238, color.getHue());
    }

    // --- Test per hueToRGB (Metodo Privato) ---

    @Test
    public void hueToRGBHueRange1Test() throws Exception {
        // Hue < HSLMAX/6 (0..42). Hue=21
        assertEquals(128, callHueToRGB(0, 255, 21));
    }

    @Test
    public void hueToRGBHueRange2Test() throws Exception {
        // HSLMAX/6 <= Hue < HSLMAX/2 (43..127). Hue=80
        assertEquals(255, callHueToRGB(0, 255, 80));
    }

    @Test
    public void hueToRGBHueRange3Test() throws Exception {
        // HSLMAX/2 <= Hue < HSLMAX*2/3 (128..169). Hue=150
        assertEquals(121, callHueToRGB(0, 255, 150));
    }

    @Test
    public void hueToRGBHueRange4Test() throws Exception {
        // HSLMAX*2/3 <= Hue < HSLMAX (170..255). Hue=200
        assertEquals(0, callHueToRGB(0, 255, 200));
    }

    @Test
    public void hueToRGBHueNegativeCorrectionTest() throws Exception {
        // Hue=-10 -> 245 (Range 4)
        assertEquals(10, callHueToRGB(10, 255, -10));
    }

    @Test
    public void hueToRGBHuePositiveCorrectionTest() throws Exception {
        // Hue=265 -> 10 (Range 1)
        assertEquals(61, callHueToRGB(0, 255, 265));
    }

    // --- Test per initRGBbyHSL ---

    @Test
    public void initRGBbyHSLGreyScaleTest() {
        HSLColor color = new HSLColor();
        // S=0 (Greyscale): R=G=B = L. L=128
        color.initRGBbyHSL(100, 0, 128);
        assertEquals(128, color.getRed());
        assertEquals(0, color.getSaturation());
        assertEquals(128, color.getBlue());
        assertEquals(128, color.getGreen());
    }

    @Test
    public void initRGBbyHSLMagic2Case1Test() {
        HSLColor color = new HSLColor();
        // L <= HSLMAX/2 (L=100). Colore: Rosso (H=0, S=255, L=100)
        color.initRGBbyHSL(0, 255, 100);
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void initRGBbyHSLMagic2Case2Test() {
        HSLColor color = new HSLColor();
        // L > HSLMAX/2 (L=150). Colore: Rosso pastello (H=0, S=255, L=150)
        color.initRGBbyHSL(0, 255, 150);
        assertEquals(255, color.getRed());
        assertEquals(50, color.getGreen());
        assertEquals(50, color.getBlue());
    }

    @Test
    public void initRGBbyHSLClampRedTest() {
        HSLColor color = new HSLColor();
        // Rosso puro: H=0, S=255, L=128. Risultato pRed dovrebbe essere 255.
        // Copre la riga 'if (pRed > RGBMAX)' anche se non clippa
        color.initRGBbyHSL(0, 255, 128);
        assertEquals(255, color.getRed());
    }
    
    @Test
    public void initRGBbyHSLClampGreenTest() {
        HSLColor color = new HSLColor();
        // Verde puro: H=85, S=255, L=128. Risultato pGreen dovrebbe essere 255.
        // Copre la riga 'if (pGreen > RGBMAX)' anche se non clippa
        color.initRGBbyHSL(85, 255, 128);
        assertEquals(255, color.getGreen());
    }
    
    @Test
    public void initRGBbyHSLClampBlueTest() {
        HSLColor color = new HSLColor();
        // Blu puro: H=170, S=255, L=128. Risultato pBlue dovrebbe essere 255.
        // Copre la riga 'if (pBlue > RGBMAX)' anche se non clippa
        color.initRGBbyHSL(170, 255, 128);
        assertEquals(255, color.getBlue());
    }
    
    @Test
    public void initRGBbyHSLClampValuesExceedingTest() {
        HSLColor color = new HSLColor();
        // Valori teorici in hueToRGB potrebbero eccedere 255 (caso non comune, ma per copertura)
        // Usiamo H=40, S=255, L=255 (Bianco) -> Magic1=255, Magic2=255.
        // Questo non clippa, ma è un test di funzionamento normale
        color.initRGBbyHSL(40, 255, 255); 
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    // --- Test per Getter/Setter HSL/RGB ---

    @Test
    public void getHueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // H=0
        assertEquals(0, color.getHue());
    }
    
    @Test
    public void setHueNegativeMultipleCorrectionTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Inizializza L, S
        color.setHue(-500); // -500 mod 255 = 10
        assertEquals(10, color.getHue());
    }

    @Test
    public void setHuePositiveMultipleCorrectionTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setHue(550); // 550 mod 255 = 40
        assertEquals(40, color.getHue());
    }
    
    @Test
    public void setHueZeroTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setHue(0);
        assertEquals(0, color.getHue());
    }


    @Test
    public void getSaturationTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // S=255
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void setSaturationMinValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255); // Inizializza H, L
        color.setSaturation(-10); // Clamped a 0
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void setSaturationMaxValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
        color.setSaturation(300); // Clamped a 255
        assertEquals(255, color.getSaturation());
    }
    
    @Test
    public void setSaturationNormalValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
        color.setSaturation(100);
        assertEquals(100, color.getSaturation());
    }

    @Test
    public void getLuminenceTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // L=128
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void setLuminenceMinValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Inizializza H, S
        color.setLuminence(-10); // Clamped a 0
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void setLuminenceMaxValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setLuminence(300); // Clamped a 255
        assertEquals(255, color.getLuminence());
    }
    
    @Test
    public void setLuminenceNormalValueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.setLuminence(100);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void getRedTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0);
        assertEquals(255, color.getRed());
    }

    @Test
    public void setRedTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 10, 10);
        callSetRed(color, 255); // Da nero (quasi) a rosso
        assertEquals(255, color.getRed());
        assertEquals(0, color.getHue()); // Controlla che abbia chiamato initHSLbyRGB
    }

    @Test
    public void getGreenTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 255, 0);
        assertEquals(255, color.getGreen());
    }

    @Test
    public void setGreenTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 0, 10);
        callSetGreen(color, 255); // Da nero (quasi) a verde
        assertEquals(255, color.getGreen());
        assertEquals(85, color.getHue());
    }

    @Test
    public void getBlueTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 255);
        assertEquals(255, color.getBlue());
    }

    @Test
    public void setBlueTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 0);
        callSetBlue(color, 255); // Da nero (quasi) a blu
        assertEquals(255, color.getBlue());
        assertEquals(170, color.getHue());
    }

    // --- Test per Funzionalità ---

    @Test
    public void greyscaleTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Rosso: L=128
        callGreyscale(color);
        // initRGBbyHSL(UNDEFINED, 0, pLum) -> S=0, L=128
        assertEquals(128, color.getRed()); // R=G=B=128 (Grigio)
        assertEquals(0, color.getSaturation());
        assertEquals(170, color.getHue());
    }

    @Test
    public void reverseColorTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Rosso: H=0
        color.reverseColor();
        // setHue(0 + 127) -> H=127 (Ciano)
        assertEquals(127, color.getHue());
    }
    
    @Test
    public void reverseLightTest() throws Exception {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // L=128
        callReverseLight(color);
        // setLuminence(255 - 128) -> L=127
        assertEquals(127, color.getLuminence());
    }

    @Test
    public void brightenZeroPercentTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // L=128
        color.brighten(0.0f); // Ritorna immediatamente
        assertEquals(128, color.getLuminence());
    }

    @Test
    public void brightenClampedMinTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // L=0
        color.brighten(-1.0f); // L * (-1) = 0. Clamped a 0.
        assertEquals(0, color.getLuminence());
        
        color.initHSLbyRGB(255, 0, 0); // L=128
        color.brighten(-1.0f); // L * (-1) = -128. Clamped a 0.
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void brightenClampedMaxTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255); // L=255
        color.brighten(2.0f); // L * 2 = 510. Clamped a 255.
        assertEquals(255, color.getLuminence());
        
        color.initHSLbyRGB(200, 200, 200); // L=200
        color.brighten(1.5f); // L * 1.5 = 300. Clamped a 255.
        assertEquals(255, color.getLuminence());
    }
    
    @Test
    public void brightenNormalTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // L=128
        color.brighten(0.5f); // L * 0.5 = 64
        assertEquals(64, color.getLuminence());
    }

    @Test
    public void blendPercentOneTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Nero
        color.blend(255, 0, 0, 1.0f); // Blend al 100% (initHSLbyRGB(R, G, B))
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void blendPercentGreaterOneTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        color.blend(255, 0, 0, 1.5f); // Blend al 100% (initHSLbyRGB(R, G, B))
        assertEquals(255, color.getRed());
    }

    @Test
    public void blendPercentZeroTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Rosso
        color.blend(0, 0, 0, 0.0f); // Blend allo 0% (ritorna)
        assertEquals(255, color.getRed());
    }
    
    @Test
    public void blendPercentLessThanZeroTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 0, 0); // Rosso
        color.blend(0, 0, 0, -0.5f); // Blend allo 0% (ritorna)
        assertEquals(255, color.getRed());
    }

    @Test
    public void blendNormalTest() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0); // Nero (R=0, G=0, B=0)
        // Blend 50% con Bianco (255, 255, 255) -> (255*0.5 + 0*0.5) = 127.5 -> 127
        color.blend(255, 255, 255, 0.5f);
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }
}