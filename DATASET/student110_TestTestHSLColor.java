        
        // 2. Hue range < HSLMAX/2 (~42 to 127)
        hslColor.initRGBbyHSL(80, 255, 128);
        
        // 3. Hue range < HSLMAX * 2/3 (~127 to 170)
        hslColor.initRGBbyHSL(150, 255, 128);
        
        // 4. Hue range > 170 (Else finale)
        hslColor.initRGBbyHSL(200, 255, 128);
    }
    
    // Test per il clamping RGB (> 255) in initRGBbyHSL
    @Test
    public void testInitRGBbyHSL_Clamping() {
        // Tentare di generare valori che arrotondati superano 255
        // È matematicamente difficile con gli interi forniti, ma testiamo i limiti
        hslColor.initRGBbyHSL(0, 255, 255); // Bianco puro
        assertEquals(255, hslColor.getRed());
        
        hslColor.initRGBbyHSL(0, 255, 0); // Nero
        assertEquals(0, hslColor.getRed());
    }

    // --- TEST: Getters e Setters pubblici ---

    @Test
    public void testSetHue_Wrap() {
        // Test wrap negativo
        hslColor.setHue(-10);
        // -10 + 255 = 245
        assertEquals(245, hslColor.getHue());

        // Test wrap positivo
        hslColor.setHue(300);
        // 300 - 255 = 45
        assertEquals(45, hslColor.getHue());
    }

    @Test
    public void testSetSaturation_Clamp() {
        hslColor.setSaturation(-50);
        assertEquals(0, hslColor.getSaturation());

        hslColor.setSaturation(500);
        assertEquals(255, hslColor.getSaturation());
    }

    @Test
    public void testSetLuminence_Clamp() {
        hslColor.setLuminence(-10);
        assertEquals(0, hslColor.getLuminence());

        hslColor.setLuminence(300);
        assertEquals(255, hslColor.getLuminence());
    }
    
    @Test
    public void testPublicGetters() {
        hslColor.initHSLbyRGB(10, 20, 30);
        assertEquals(10, hslColor.getRed());
        assertEquals(20, hslColor.getGreen());
        assertEquals(30, hslColor.getBlue());
    }

    // --- TEST: Metodi operativi (blend, brighten, reverse) ---

    @Test
    public void testReverseColor() {
        hslColor.setHue(0);
        hslColor.reverseColor();
        // 0 + 255/2 = 127
        assertEquals(127, hslColor.getHue());
    }

    @Test
    public void testBrighten() {
        // Caso fPercent == 0 -> return immediato
        hslColor.setLuminence(100);
        hslColor.brighten(0);
        assertEquals(100, hslColor.getLuminence());

        // Caso normale
        hslColor.setLuminence(100);
        hslColor.brighten(1.5f); // +50%
        assertEquals(150, hslColor.getLuminence());
        
        // Caso Overflow
        hslColor.setLuminence(200);
        hslColor.brighten(2.0f); 
        assertEquals(255, hslColor.getLuminence());
        
        // Caso Underflow (L diventa negativo castato a int prima del check)
        // Nota: pLum * fPercent può essere negativo se fPercent è negativo
        hslColor.setLuminence(100);
        hslColor.brighten(-0.5f);
        assertEquals(0, hslColor.getLuminence());
    }

    @Test
    public void testBlend() {
        // Setup
        hslColor.initHSLbyRGB(0, 0, 0);

        // Percent >= 1
        hslColor.blend(255, 255, 255, 1.0f);
        assertEquals(255, hslColor.getRed());

        // Percent <= 0
        hslColor.blend(0, 0, 0, 0.0f);
        // Non deve cambiare nulla (resta bianco dal passo precedente)
        assertEquals(255, hslColor.getRed());

        // Percent 0.5
        hslColor.initHSLbyRGB(0, 0, 0); // Reset a nero
        hslColor.blend(100, 100, 100, 0.5f);
        assertEquals(50, hslColor.getRed());
    }

    // --- REFLECTION: Copertura Metodi Privati "Dead Code" ---
    // Questi test sono necessari SOLO perché è richiesto il 100% coverage
    // su una classe che contiene metodi privati inutilizzati.

    @Test
    public void testPrivate_Greyscale() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("greyscale");
        method.setAccessible(true);
        
        hslColor.setLuminence(100);
        hslColor.setSaturation(255); // Colore pieno
        
        method.invoke(hslColor);
        
        assertEquals(0, hslColor.getSaturation());
        assertEquals(UNDEFINED, hslColor.getHue());
        assertEquals(100, hslColor.getLuminence());
    }

    @Test
    public void testPrivate_ReverseLight() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("reverseLight");
        method.setAccessible(true);
        
        hslColor.setLuminence(55); // HSLMAX(255) - 55 = 200
        method.invoke(hslColor);
        
        assertEquals(200, hslColor.getLuminence());
    }

    @Test
    public void testPrivate_SetRed() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setRed", int.class);
        method.setAccessible(true);
        
        hslColor.initHSLbyRGB(0, 0, 0);
        method.invoke(hslColor, 255);
        
        assertEquals(255, hslColor.getRed());
        assertEquals(0, hslColor.getGreen()); // Invariati
    }

    @Test
    public void testPrivate_SetGreen() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setGreen", int.class);
        method.setAccessible(true);
        
        hslColor.initHSLbyRGB(0, 0, 0);
        method.invoke(hslColor, 255);
        
        assertEquals(0, hslColor.getRed()); // Invariato
        assertEquals(255, hslColor.getGreen());
    }

    @Test
    public void testPrivate_SetBlue() throws Exception {
        Method method = HSLColor.class.getDeclaredMethod("setBlue", int.class);
        method.setAccessible(true);
        
        hslColor.initHSLbyRGB(0, 0, 0);
        method.invoke(hslColor, 255);
        
        assertEquals(0, hslColor.getRed()); // Invariato
        assertEquals(255, hslColor.getBlue());
    }
}
