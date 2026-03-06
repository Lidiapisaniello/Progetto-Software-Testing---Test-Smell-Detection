import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {

    // =================================================================
    // 1. Test dei Costruttori e Inizializzazione
    // =================================================================

    @Test
    public void constructorDefault_creaColoreNero_valoriZero() {
        HSLColor color = new HSLColor();
        assertEquals("Hue should be 0 for default black", 0, color.getHue());
        assertEquals("Saturation should be 0 for default black", 0, color.getSaturation());
        assertEquals("Luminence should be 0 for default black", 0, color.getLuminence());
        assertEquals("Red should be 0 for default black", 0, color.getRed());
        assertEquals("Green should be 0 for default black", 0, color.getGreen());
        assertEquals("Blue should be 0 for default black", 0, color.getBlue());
    }

    @Test
    public void constructorRGB_conValoriValidi_calcolaHSLC correttamente() {
        // Rosso puro
        HSLColor color = new HSLColor(255, 0, 0);
        assertEquals("Hue for pure red", 0, color.getHue());
        assertEquals("Saturation for pure red", 255, color.getSaturation());
        assertEquals("Luminence for pure red", 128, color.getLuminence());
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void constructorRGB_conGrigioMedio_calcolaHSLCcorrettamente() {
        HSLColor color = new HSLColor(128, 128, 128);
        assertEquals("Hue for gray should be 0", 0, color.getHue());
        assertEquals("Saturation for gray should be 0", 0, color.getSaturation());
        assertEquals("Luminence for gray", 128, color.getLuminence());
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void constructorRGB_conValoriNegativi_liTrattaComeZero() {
        HSLColor color = new HSLColor(-1, -10, -100);
        assertEquals("Red should be clamped to 0", 0, color.getRed());
        assertEquals("Green should be clamped to 0", 0, color.getGreen());
        assertEquals("Blue should be clamped to 0", 0, color.getBlue());
        assertEquals("Hue should be 0 for black", 0, color.getHue());
        assertEquals("Saturation should be 0 for black", 0, color.getSaturation());
        assertEquals("Luminence should be 0 for black", 0, color.getLuminence());
    }

    @Test
    public void constructorRGB_conValoriOltre255_liTrattaCome255() {
        HSLColor color = new HSLColor(256, 300, 1000);
        assertEquals("Red should be clamped to 255", 255, color.getRed());
        assertEquals("Green should be clamped to 255", 255, color.getGreen());
        assertEquals("Blue should be clamped to 255", 255, color.getBlue());
        assertEquals("Hue should be 0 for white", 0, color.getHue());
        assertEquals("Saturation should be 0 for white", 0, color.getSaturation());
        assertEquals("Luminence should be 255 for white", 255, color.getLuminence());
    }

    @Test
    public void constructorHSL_conValoriValidi_calcolaRGBCorrettamente() {
        // Ciano
        HSLColor color = new HSLColor(128, 255, 128);
        assertEquals(128, color.getHue());
        assertEquals(255, color.getSaturation());
        assertEquals(128, color.getLuminence());
        assertEquals("Red for cyan", 0, color.getRed());
        assertEquals("Green for cyan", 255, color.getGreen());
        assertEquals("Blue for cyan", 255, color.getBlue());
    }

    @Test
    public void constructorHSL_conValoriNegativi_liTrattaComeZero() {
        HSLColor color = new HSLColor(-1, -10, -100);
        assertEquals("Hue should be clamped to 0", 0, color.getHue());
        assertEquals("Saturation should be clamped to 0", 0, color.getSaturation());
        assertEquals("Luminence should be clamped to 0", 0, color.getLuminence());
        assertEquals("Red should be 0 for black", 0, color.getRed());
        assertEquals("Green should be 0 for black", 0, color.getGreen());
        assertEquals("Blue should be 0 for black", 0, color.getBlue());
    }

    @Test
    public void constructorHSL_conValoriOltre255_liTrattaCome255() {
        HSLColor color = new HSLColor(256, 300, 1000);
        assertEquals("Hue should be clamped to 255", 255, color.getHue());
        assertEquals("Saturation should be clamped to 255", 255, color.getSaturation());
        assertEquals("Luminence should be clamped to 255", 255, color.getLuminence());
        assertEquals("Red should be 255 for white", 255, color.getRed());
        assertEquals("Green should be 255 for white", 255, color.getGreen());
        assertEquals("Blue should be 255 for white", 255, color.getBlue());
    }

    // =================================================================
    // 2. Test di Round-Trip (RGB -> HSL -> RGB)
    // =================================================================

    @Test
    public void roundTrip_rossoPuro_mantieneRGB() {
        HSLColor color = new HSLColor(255, 0, 0);
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        assertEquals(255, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void roundTrip_verdePuro_mantieneRGB() {
        HSLColor color = new HSLColor(0, 255, 0);
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void roundTrip_bluPuro_mantieneRGB() {
        HSLColor color = new HSLColor(0, 0, 255);
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    @Test
    public void roundTrip_grigio_mantieneRGB() {
        HSLColor color = new HSLColor(100, 100, 100);
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    @Test
    public void roundTrip_coloreCasuale_mantieneRGB() {
        HSLColor color = new HSLColor(42, 123, 200);
        color.initRGBbyHSL(color.getHue(), color.getSaturation(), color.getLuminence());
        assertEquals(42, color.getRed());
        assertEquals(123, color.getGreen());
        assertEquals(200, color.getBlue());
    }

    // =================================================================
    // 3. Test dei Setters
    // =================================================================

    @Test
    public void setHue_conValoreValido_aggiornaCorrettamente() {
        HSLColor color = new HSLColor(0, 255, 128); // Rosso
        color.setHue(85); // Verde
        assertEquals(85, color.getHue());
        assertEquals(0, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void setHue_conValoreNegativo_esegueWrap() {
        HSLColor color = new HSLColor();
        color.setHue(-1); // -1 mod 256 = 255
        assertEquals(255, color.getHue());
    }

    @Test
    public void setHue_conValore256_esegueWrap() {
        HSLColor color = new HSLColor();
        color.setHue(256); // 256 mod 256 = 0
        assertEquals(0, color.getHue());
    }

    @Test
    public void setHue_conValoreGrande_esegueWrap() {
        HSLColor color = new HSLColor();
        color.setHue(257); // 257 mod 256 = 1
        assertEquals(1, color.getHue());
    }

    @Test
    public void setSaturation_conValoreNegativo_esegueClampAZero() {
        HSLColor color = new HSLColor(128, 100, 100);
        color.setSaturation(-1);
        assertEquals(0, color.getSaturation());
    }

    @Test
    public void setSaturation_conValoreOltre255_esegueClampA255() {
        HSLColor color = new HSLColor(128, 100, 100);
        color.setSaturation(256);
        assertEquals(255, color.getSaturation());
    }

    @Test
    public void setLuminence_conValoreNegativo_esegueClampAZero() {
        HSLColor color = new HSLColor(128, 100, 100);
        color.setLuminence(-1);
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void setLuminence_conValoreOltre255_esegueClampA255() {
        HSLColor color = new HSLColor(128, 100, 100);
        color.setLuminence(256);
        assertEquals(255, color.getLuminence());
    }

    // =================================================================
    // 4. Test di reverseColor()
    // =================================================================

    @Test
    public void reverseColor_conHueBasso_aggiunge128() {
        HSLColor color = new HSLColor(10, 255, 128);
        color.reverseColor();
        assertEquals(138, color.getHue()); // 10 + 128
    }

    @Test
    public void reverseColor_conHueAlto_esegueWrap() {
        HSLColor color = new HSLColor(200, 255, 128);
        color.reverseColor();
        assertEquals(72, color.getHue()); // (200 + 128) % 256
    }

    @Test
    public void reverseColor_conHue128_diventaZero() {
        HSLColor color = new HSLColor(128, 255, 128);
        color.reverseColor();
        assertEquals(0, color.getHue()); // (128 + 128) % 256
    }

    // =================================================================
    // 5. Test di brighten()
    // =================================================================

    @Test
    public void brighten_conPercentZero_nonCambiaLuminosita() {
        HSLColor color = new HSLColor(100, 100, 100);
        color.brighten(0.0f);
        assertEquals(100, color.getLuminence());
    }

    @Test
    public void brighten_conPercentPositivo_aumentaLuminosita() {
        HSLColor color = new HSLColor(100, 200, 100);
        color.brighten(0.5f); // L = 100 + (255-100)*0.5 = 100 + 77.5 = 177
        assertEquals(177, color.getLuminence());
    }

    @Test
    public void brighten_conPercentNegativo_diminuisceLuminosita() {
        HSLColor color = new HSLColor(100, 200, 100);
        color.brighten(-0.5f); // L = 100 + 100*(-0.5) = 100 - 50 = 50
        assertEquals(50, color.getLuminence());
    }

    @Test
    public void brighten_conAumentoOltreLimite_esegueClampA255() {
        HSLColor color = new HSLColor(100, 200, 200);
        color.brighten(1.0f); // L = 200 + (255-200)*1.0 = 255
        assertEquals(255, color.getLuminence());
        color.brighten(2.0f); // Dovrebbe rimanere 255
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void brighten_conDiminuzioneOltreLimite_esegueClampAZero() {
        HSLColor color = new HSLColor(100, 200, 50);
        color.brighten(-1.0f); // L = 50 + 50*(-1.0) = 0
        assertEquals(0, color.getLuminence());
        color.brighten(-2.0f); // Dovrebbe rimanere 0
        assertEquals(0, color.getLuminence());
    }

    // =================================================================
    // 6. Test di blend()
    // =================================================================

    @Test
    public void blend_conFattoreZero_nonCambiaColore() {
        HSLColor color = new HSLColor(10, 20, 30);
        color.blend(255, 0, 0, 0.0f);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    @Test
    public void blend_conFattoreNegativo_nonCambiaColore() {
        HSLColor color = new HSLColor(10, 20, 30);
        color.blend(255, 0, 0, -1.0f);
        assertEquals(10, color.getRed());
        assertEquals(20, color.getGreen());
        assertEquals(30, color.getBlue());
    }

    @Test
    public void blend_conFattoreUno_diventaColoreTarget() {
        HSLColor color = new HSLColor(10, 20, 30);
        color.blend(255, 100, 50, 1.0f);
        assertEquals(255, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(50, color.getBlue());
    }

    @Test
    public void blend_conFattoreMaggioreDiUno_diventaColoreTarget() {
        HSLColor color = new HSLColor(10, 20, 30);
        color.blend(255, 100, 50, 2.0f);
        assertEquals(255, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(50, color.getBlue());
    }

    @Test
    public void blend_conFattoreMezzo_interpolaLinearmente() {
        HSLColor color = new HSLColor(10, 20, 30);
        color.blend(110, 220, 130, 0.5f);
        // R = 10 * 0.5 + 110 * 0.5 = 5 + 55 = 60
        // G = 20 * 0.5 + 220 * 0.5 = 10 + 110 = 120
        // B = 30 * 0.5 + 130 * 0.5 = 15 + 65 = 80
        assertEquals(60, color.getRed());
        assertEquals(120, color.getGreen());
        assertEquals(80, color.getBlue());
    }

    // =================================================================
    // 7. Test di Casi Limite e Anomalie
    // =================================================================

    @Test
    public void initHSLbyRGB_conNero_calcolaHSLCorrettamente() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(0, 0, 0);
        assertEquals(0, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(0, color.getLuminence());
    }

    @Test
    public void initHSLbyRGB_conBianco_calcolaHSLCorrettamente() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(255, 255, 255);
        assertEquals(0, color.getHue());
        assertEquals(0, color.getSaturation());
        assertEquals(255, color.getLuminence());
    }

    @Test
    public void initRGBbyHSL_conNero_calcolaRGBCorrettamente() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 0);
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    @Test
    public void initRGBbyHSL_conBianco_calcolaRGBCorrettamente() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(0, 0, 255);
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }

    @Test
    public void initRGBbyHSL_conSaturazioneZero_produceGrigio() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(100, 0, 128); // Hue non dovrebbe avere effetto
        assertEquals(128, color.getRed());
        assertEquals(128, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    @Test
    public void anomalia_initRGBbyHSL_conLuminosita255SaturazioneNonZero_produceBianco() {
        HSLColor color = new HSLColor();
        // Anche con saturazione massima, se la luminosità è 255, il colore è bianco.
        color.initRGBbyHSL(128, 255, 255);
        assertEquals("Red should be 255 for max luminence", 255, color.getRed());
        assertEquals("Green should be 255 for max luminence", 255, color.getGreen());
        assertEquals("Blue should be 255 for max luminence", 255, color.getBlue());
    }

    @Test
    public void anomalia_initRGBbyHSL_conLuminositaZeroSaturazioneNonZero_produceNero() {
        HSLColor color = new HSLColor();
        // Anche con saturazione massima, se la luminosità è 0, il colore è nero.
        color.initRGBbyHSL(128, 255, 0);
        assertEquals("Red should be 0 for zero luminence", 0, color.getRed());
        assertEquals("Green should be 0 for zero luminence", 0, color.getGreen());
        assertEquals("Blue should be 0 for zero luminence", 0, color.getBlue());
    }
}