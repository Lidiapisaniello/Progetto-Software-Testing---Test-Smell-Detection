/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giorgi.tarallo@studenti.unina.it
UserID: 277
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestHSLColor {
	private final static int HSLMAX = 255;
    private final static int RGBMAX = 255;
    private final static int UNDEFINED = 170;

    // --- Metodi di utilità (iMax, iMin) e Getters ---
    // Questi sono impliciti nella copertura dei metodi principali, ma li testiamo per completezza.

    @Test
    public void testIMax() {
        HSLColor color = new HSLColor();
        // Usiamo la reflection o chiamiamo un metodo che usa iMax, ma dato che iMax/iMin sono private,
        // ci affidiamo al fatto che siano coperti da initHSLbyRGB.
        // Eseguiamo un init standard.
        color.initHSLbyRGB(200, 100, 50);
        // Verifichiamo un Getter per assicurare che lo stato sia modificato.
        assertTrue(color.getRed() == 200);
        assertTrue(color.getGreen() == 100);
        assertTrue(color.getBlue() == 50);
    }

    // --- Test initHSLbyRGB (RGB -> HSL) ---

    // 1. Caso Greyscale: cMax == cMin. Attiva il ramo 'if' principale.
    @Test
    public void testInitHSLbyRGB_Greyscale() {
        HSLColor color = new HSLColor();
        // Grigio medio (R=G=B)
        color.initHSLbyRGB(128, 128, 128);
        
        // Assertions per HSL greyscale: Saturation = 0, Hue = UNDEFINED (170)
        assertEquals(0, color.getSaturation());
        assertEquals(UNDEFINED, color.getHue());
        
        // La luminosità deve essere circa (128+128)/2 * (255/255) = 128
        assertEquals(128, color.getLuminence());
    }

    // 2. Caso Colore (Sat): pLum <= HSLMAX/2 (Luminanza Bassa/Media). Attiva il ramo 'else', poi 'if'.
    @Test
    public void testInitHSLbyRGB_Color_LowLum_SatIf() {
        HSLColor color = new HSLColor();
        // Rosso puro (Luminanza bassa)
        color.initHSLbyRGB(255, 0, 0); 
        
        // L = 127 (calcolato: 255/2), L <= 127
        assertTrue(color.getLuminence() <= HSLMAX / 2);
        
        // Hue: cMax=R, dovrebbe essere BDelta - GDelta = 0.
        assertTrue(color.getHue() < 5); // Rosso dovrebbe essere vicino a 0/255
    }

    // 3. Caso Colore (Sat): pLum > HSLMAX/2 (Luminanza Alta). Attiva il ramo 'else', poi 'else'.
    @Test
    public void testInitHSLbyRGB_Color_HighLum_SatElse() {
        HSLColor color = new HSLColor();
        // Ciano pallido (R basso, G/B alti)
        color.initHSLbyRGB(50, 255, 255); 
        
        // L = (255+50)/2 * (255/255) = 152.5. L > 127
        assertTrue(color.getLuminence() > HSLMAX / 2);
        
        // Hue: cMax=G o B. In questo caso cMax=G=B=255.
        // HSLMAX/3 (85) < Hue < 2*HSLMAX/3 (170) -> Ciano/Verde
        assertTrue(color.getHue() > HSLMAX/3 && color.getHue() < 2 * HSLMAX / 3);
    }
    
    // 4. Caso Hue: cMax == R. Attiva il primo 'if' del calcolo Hue.
    @Test
    public void testInitHSLbyRGB_Hue_RMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(200, 100, 150); // R max
        // La condizione cMax == R è vera
        assertEquals(200, color.getRed()); 
    }

    // 5. Caso Hue: cMax == G. Attiva il secondo 'else if' del calcolo Hue.
    @Test
    public void testInitHSLbyRGB_Hue_GMax() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(100, 200, 150); // G max
        // La condizione cMax == G è vera
        assertEquals(200, color.getGreen()); 
        
        // Assicurarsi che pHue < 0 venga testato (si attiva il pHue = pHue + HSLMAX)
        // Questo è difficile da forzare, ma il test successivo lo verifica indirettamente.
    }

    // 6. Caso Hue: cMax == B. Attiva il terzo 'else if' del calcolo Hue e testa pHue < 0
    @Test
    public void testInitHSLbyRGB_Hue_BMax_AndNegativeHueCorrection() {
        HSLColor color = new HSLColor();
        // Blu (G=0). BDelta-RDelta sarà negativo.
        color.initHSLbyRGB(0, 0, 255); 
        
        // La correzione di Hue deve avvenire: if (pHue < 0) { pHue = pHue + HSLMAX; }
        // Blu puro dovrebbe essere attorno a 2*HSLMAX/3 + HSLMAX/6 = 5*HSLMAX/6 (212 circa)
        assertTrue(color.getHue() > 200); 
    }
    
    // --- Test hueToRGB (Metodo privato) ---
    // Testiamo i 4 rami di hueToRGB attraverso initRGBbyHSL.

    // 7. hueToRGB Ramo 1: Hue < HSLMAX/6 (0-42) - Punti medi del R, G, B
    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch1() {
        HSLColor color = new HSLColor();
        // H=10 (vicino a 0), S=255, L=128 (Rosso)
        color.initRGBbyHSL(10, HSLMAX, HSLMAX/2); 
        
        // Il rosso (H+(HSLMAX/3)) sarà ~95 (> 42), quindi non attiva il ramo 1.
        // Il verde (H) sarà 10 (< 42), attiva il ramo 1.
        // Il blu (H-(HSLMAX/3)) sarà negativo, attivando la correzione, poi > 42.
        
        // Basta che il GREEN (H) attivi il primo ramo: Hue < (HSLMAX / 6)
        assertTrue(color.getGreen() > 100 && color.getGreen() < 200); // Valore intermedio
    }

    // 8. hueToRGB Ramo 2: HSLMAX/6 <= Hue < HSLMAX/2 (43-127)
    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch2() {
        HSLColor color = new HSLColor();
        // H=100 (Verde puro)
        color.initRGBbyHSL(100, HSLMAX, HSLMAX/2); 
        
        // H=100 (Verde) attiva il secondo ramo: Hue < (HSLMAX / 2) -> ritorna mag2
        assertEquals(RGBMAX, color.getGreen()); // Verde Max (255)
    }

    // 9. hueToRGB Ramo 3: HSLMAX/2 <= Hue < HSLMAX*2/3 (128-170)
    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch3() {
        HSLColor color = new HSLColor();
        // H=150 (Ciano scuro)
        color.initRGBbyHSL(150, HSLMAX, HSLMAX/2); 
        
        // Blu: H - (HSLMAX/3) = 150 - 85 = 65. Ramo 2.
        // Verde: H = 150. Ramo 3.
        // Rosso: H + (HSLMAX/3) = 235. Ramo 4.
        
        // La condizione del VERDE attiva il ramo 3: Hue < (HSLMAX * 2 / 3)
        assertTrue(color.getGreen() < 255 && color.getGreen() > 100); // Valore intermedio
    }
    
    // 10. hueToRGB Ramo 4: Hue >= HSLMAX*2/3 (171-255)
    @Test
    public void testInitRGBbyHSL_HueToRGB_Branch4() {
        HSLColor color = new HSLColor();
        // H=200 (Blu scuro/Viola)
        color.initRGBbyHSL(200, HSLMAX, HSLMAX/2); 
        
        // Verde: H=200. Attiva il ramo 4: ritorna mag1
        assertEquals(0, color.getGreen()); // Il verde dovrebbe essere 0 per H=200 S=255 L=128
    }

    // 11. Test Range Correction in hueToRGB: Hue < 0 e Hue > HSLMAX
    @Test
    public void testInitRGBbyHSL_HueToRGB_RangeCorrection() {
        HSLColor color = new HSLColor();
        // H=-5 (attiva Hue = Hue + HSLMAX) e H=260 (attiva Hue = Hue - HSLMAX)
        // Testiamo il Rosso per H + HSLMAX/3: 260 + 85 = 345 -> corretto a 345 - 255 = 90
        // Testiamo il Blu per H - HSLMAX/3: -5 - 85 = -90 -> corretto a -90 + 255 = 165
        color.initRGBbyHSL(260, HSLMAX, HSLMAX/2); 
        
        // L'importante è che il codice non vada in errore e che le correzioni vengano eseguite.
        // Ci aspettiamo un colore simile al rosso per H=5 (260-255).
        assertTrue(color.getRed() > 200); 
    }

    // --- Test initRGBbyHSL (HSL -> RGB) ---

    // 12. Caso Greyscale: S == 0. Attiva il ramo 'if' principale.
    @Test
    public void testInitRGBbyHSL_Greyscale() {
        HSLColor color = new HSLColor();
        // H=100 (irrilevante), S=0, L=100
        color.initRGBbyHSL(100, 0, 100); 
        
        // R=G=B = (100 * 255) / 255 = 100
        assertEquals(100, color.getRed());
        assertEquals(100, color.getGreen());
        assertEquals(100, color.getBlue());
    }

    // 13. Caso Colore (Magic2): L <= HSLMAX/2 (Luminanza Bassa/Media). Attiva il ramo 'else', poi 'if'.
    @Test
    public void testInitRGBbyHSL_Color_LowLum_MagicIf() {
        HSLColor color = new HSLColor();
        // H=0 (Rosso), S=100, L=50
        color.initRGBbyHSL(0, 100, 50); 
        
        // L <= 127
        assertTrue(color.getLuminence() <= HSLMAX / 2);
    }

    // 14. Caso Colore (Magic2): L > HSLMAX/2 (Luminanza Alta). Attiva il ramo 'else', poi 'else'.
    @Test
    public void testInitRGBbyHSL_Color_HighLum_MagicElse() {
        HSLColor color = new HSLColor();
        // H=0 (Rosso), S=100, L=200
        color.initRGBbyHSL(0, 100, 200); 
        
        // L > 127
        assertTrue(color.getLuminence() > HSLMAX / 2);
    }
    
    // 15. Test clamping di pRed, pGreen, pBlue (> RGBMAX).
    @Test
    public void testInitRGBbyHSL_Clamping() {
        HSLColor color = new HSLColor();
        // H=0, S=255, L=255 (Bianco) -> Le operazioni di Magic e hueToRGB dovrebbero tendere a valori molto alti,
        // garantendo che R, G, B superino 255 prima del clamp.
        color.initRGBbyHSL(0, HSLMAX, HSLMAX); 
        
        // R, G, B devono essere clampati a RGBMAX (255)
        assertEquals(RGBMAX, color.getRed());
        assertEquals(RGBMAX, color.getGreen());
        assertEquals(RGBMAX, color.getBlue());
    }

    // --- Test Setters e Metodi di Trasformazione ---
    
    // 16. setHue: Test del ciclo while (iToValue < 0)
    @Test
    public void testSetHue_Negative() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128); // Inizializza L/S/H
        // HSLMAX = 255. Setto H=-10. Deve diventare 245.
        color.setHue(-10);
        
        // setHue chiama initRGBbyHSL
        assertEquals(245, color.getHue());
    }

    // 17. setHue: Test del ciclo while (iToValue > HSLMAX)
    @Test
    public void testSetHue_Overflow() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(128, 128, 128);
        // Setto H=300. Deve diventare 300 - 255 = 45.
        color.setHue(300);
        
        assertEquals(45, color.getHue());
    }
    
    // 18. setSaturation: Test iToValue < 0
    @Test
    public void testSetSaturation_Negative() {
        HSLColor color = new HSLColor();
        color.setSaturation(-50);
        assertEquals(0, color.getSaturation()); // Clamped a 0
    }

    // 19. setSaturation: Test iToValue > HSLMAX
    @Test
    public void testSetSaturation_Overflow() {
        HSLColor color = new HSLColor();
        color.setSaturation(300);
        assertEquals(HSLMAX, color.getSaturation()); // Clamped a 255
    }

    // 20. setLuminence: Test iToValue < 0
    @Test
    public void testSetLuminence_Negative() {
        HSLColor color = new HSLColor();
        color.setLuminence(-50);
        assertEquals(0, color.getLuminence()); // Clamped a 0
    }

    // 21. setLuminence: Test iToValue > HSLMAX
    @Test
    public void testSetLuminence_Overflow() {
        HSLColor color = new HSLColor();
        color.setLuminence(300);
        assertEquals(HSLMAX, color.getLuminence()); // Clamped a 255
    }
    
    // 22. reverseColor: Copre setHue
    @Test
    public void testReverseColor() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(10, HSLMAX, HSLMAX/2); // H=10
        color.reverseColor(); // H = 10 + 127 = 137
        assertEquals(137, color.getHue());
    }
    
    // 23. reverseLight (privato): Assicuriamo che venga chiamato da qualche parte se fosse pubblico,
    // altrimenti testiamo solo i metodi pubblici che lo usano (nessuno lo usa pubblicamente qui).
    // Non potendo testare metodi privati, si è coperto setLuminence.

    // 24. brighten: fPercent == 0
    @Test
    public void testBrighten_Zero() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(10, HSLMAX, 100);
        color.brighten(0.0f);
        assertEquals(100, color.getLuminence()); // L non cambia
    }
    
    // 25. brighten: L < 0 (clamping a 0)
    @Test
    public void testBrighten_NegativeLuminence_ClampToZero() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(10, HSLMAX, 100);
        // 100 * -1.5 = -150 -> L < 0 vero -> clamp a 0
        color.brighten(-1.5f);
        assertEquals(0, color.getLuminence());
    }
    
    // 26. brighten: L > HSLMAX (clamping a HSLMAX)
    @Test
    public void testBrighten_OverflowLuminence_ClampToMax() {
        HSLColor color = new HSLColor();
        color.initRGBbyHSL(10, HSLMAX, 100);
        // 100 * 3.0 = 300 -> L > HSLMAX (255) vero -> clamp a 255
        color.brighten(3.0f);
        assertEquals(HSLMAX, color.getLuminence());
    }

    // 27. blend: fPercent >= 1 (primo if, chiama initHSLbyRGB con colore target)
    @Test
    public void testBlend_PercentOneOrMore() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10);
        color.blend(200, 100, 50, 1.0f);
        // R dovrebbe essere 200 (initHSLbyRGB(R, G, B))
        assertEquals(200, color.getRed());
    }

    // 28. blend: fPercent <= 0 (secondo if, ritorna senza fare nulla)
    @Test
    public void testBlend_PercentZeroOrLess() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10);
        color.blend(200, 100, 50, 0.0f);
        // R dovrebbe rimanere 10
        assertEquals(10, color.getRed());
    }
    
    // 29. blend: caso intermedio (ramo principale)
    @Test
    public void testBlend_Intermediate() {
        HSLColor color = new HSLColor();
        color.initHSLbyRGB(10, 10, 10); // Stato iniziale: R=10
        // Blenda con (200, 100, 50) al 50%
        color.blend(200, 100, 50, 0.5f);
        // newR = (200 * 0.5) + (10 * 0.5) = 100 + 5 = 105
        assertEquals(105, color.getRed());
    }

    // 30. greyscale (privato): Assicuriamo la copertura attraverso l'uso (non c'è un uso pubblico,
    // quindi si testa solo l'inizializzazione HSLbyRGB e RGBbyHSL separatamente).
}