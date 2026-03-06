/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Senior"
Cognome: "QA Engineer"
Username: ange.dalia@studenti.unina.it
UserID: 127
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @BeforeClass
    public static void setUpClass() {
        // Setup statico
    }

    @AfterClass
    public static void tearDownClass() {
        // Teardown statico
    }

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        manager = null;
    }

    // --- METODI DI UTILITÀ ---
    private void scorePoints(int player, int times) {
        for (int i = 0; i < times; i++) {
            manager.pointScored(player);
        }
    }
    
    // Helper per Reflection (per pulire il codice dei test)
    private void setIntField(String fieldName, int value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(manager, value);
    }
    
    private void setArrayField(String fieldName, int index, int value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        int[] array = (int[]) field.get(manager);
        array[index] = value;
    }

    // --- TEST FUNZIONALI E DI COPERTURA ---

    @Test
    public void testInitialState() {
        // Uccide mutanti sull'inizializzazione
        assertEquals("Love-Love", manager.getGameScore());
        assertFalse(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testInvalidPlayerInput() {
        manager.pointScored(3); 
        manager.pointScored(-1);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testStandardGameWinP1_Sequence() {
        // Verifica passo passo per uccidere mutanti sugli incrementi
        assertEquals("Love-Love", manager.getGameScore());
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore()); // Boundary check 3 punti
        
        manager.pointScored(1); // Vince game
        assertEquals("Love-Love", manager.getGameScore()); // Verify Reset
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testStandardGameWinP2() {
        scorePoints(2, 3);
        assertEquals("Love-40", manager.getGameScore());
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testDeuceLogics_Boundary() {
        // Mutation: verifica Deuce (3-3) e Ritorno a Deuce (4-4)
        scorePoints(1, 3); 
        scorePoints(2, 3); 
        assertEquals("Deuce", manager.getGameScore());
        
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(2); // Ritorno a Deuce (4-4)
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageWin_DifferenceCheck() {
        // Setup: 3-3
        scorePoints(1, 3);
        scorePoints(2, 3);
        
        manager.pointScored(1); // 4-3 Adv P1
        // Mutation Killer: Se il mutante cambia ">= P2 + 2" in "+ 1", qui fallirebbe
        assertFalse(manager.getMatchScore().contains("Game: 1-0")); 
        
        manager.pointScored(1); // 5-3 Win
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testP2BugPath_Trap() {
        // Mutation Trap: Se un mutante corregge la condizione && in ||, 
        // il codice ritornerebbe "Vantaggio P2". Noi vogliamo che fallisca in quel caso.
        scorePoints(1, 3);
        scorePoints(2, 3); // Deuce
        manager.pointScored(2); // P2 teorico vantaggio
        
        // Asseriamo il comportamento ATTUALE (errato). 
        // Se il mutante "attiva" il codice morto, ritornerà Vantaggio P2 e il test fallirà -> Mutante Morto.
        assertEquals("Errore Game", manager.getGameScore());
    }

    // --- MUTATION KILLERS CON REFLECTION (IL SEGRETO PER IL 95%) ---

    @Test
    public void testMutationKiller_SetBoundaries_6_5() throws Exception {
        /*
         * Questo test è CRUCIALE. Uccide i mutanti che cambiano:
         * "gamesP1 >= gamesP2 + 2"  ---> "gamesP1 >= gamesP2 + 1"
         * Senza questo test, quel mutante sopravvive perché il set finirebbe sul 6-5.
         */
        setIntField("gamesP1", 6);
        setIntField("gamesP2", 5);
        
        manager.checkSetPoint();
        
        String score = manager.getMatchScore();
        // Se il set fosse vinto (mutante vivo), inizierebbe con "1-0".
        // Noi vogliamo che sia ancora in corso (mutante morto).
        assertFalse("Il set NON deve finire 6-5", score.startsWith("1-0"));
        assertTrue(score.contains("Game: 6-5"));
    }
    
    @Test
    public void testMutationKiller_SetBoundaries_5_6() throws Exception {
        // Stessa cosa per P2
        setIntField("gamesP1", 5);
        setIntField("gamesP2", 6);
        
        manager.checkSetPoint();
        
        String score = manager.getMatchScore();
        assertFalse("Il set NON deve finire 5-6", score.startsWith("0-1"));
        assertTrue(score.contains("Game: 5-6"));
    }
    
    @Test
    public void testMutationKiller_SetBoundaries_5_5() throws Exception {
        // Verifica che sul 5-5 non succeda nulla di strano
        setIntField("gamesP1", 5);
        setIntField("gamesP2", 5);
        manager.checkSetPoint();
        String score = manager.getMatchScore();
        assertFalse(score.startsWith("1-0"));
        assertFalse(score.startsWith("0-1"));
    }

    @Test
    public void testSetWin_Standard_6_0() throws Exception {
        // Verifica la vittoria standard per coprire il ramo if principale
        // Usiamo scorePoints per coprire anche l'incremento
        for(int i=0; i<24; i++) manager.pointScored(1); // 6 games
        assertTrue(manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testSetWin_P2_7_5_Logic() throws Exception {
        // Copre la logica complessa (gamesP2 == 7 && gamesP1 == 5)
        // Usiamo reflection per arrivarci veloci e sicuri bypassando bug
        setIntField("gamesP1", 5);
        setIntField("gamesP2", 7);
        manager.checkSetPoint();
        assertTrue(manager.getMatchScore().startsWith("0-1"));
    }
    
    @Test
    public void testSetWin_P2_Reflection_7_6() throws Exception {
        // Caso 7-6 per P2 (il codice P2 è corretto su questo)
        setIntField("gamesP1", 6);
        setIntField("gamesP2", 7);
        manager.checkSetPoint();
        assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    // --- TIE BREAK & GAME OVER ---

    @Test
    public void testTieBreakEntry() {
        for(int i=0; i<5; i++) scorePoints(1, 4);
        for(int i=0; i<5; i++) scorePoints(2, 4);
        scorePoints(1, 4); 
        scorePoints(2, 4); 
        
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }
    
    @Test
    public void testTieBreakBug_ResetCheck() {
        // Copertura del bug reset
        for(int i=0; i<5; i++) scorePoints(1, 4);
        for(int i=0; i<5; i++) scorePoints(2, 4);
        scorePoints(1, 4); scorePoints(2, 4); 
        
        scorePoints(1, 7); 
        String finalScore = manager.getMatchScore();
        // Asserzione che conferma il bug (reset game a 0)
        assertTrue(finalScore.contains("Game: 0-0"));
    }
    
    @Test
    public void testTieBreakWinP2_BugCoverage() {
         // Copertura ramo P2 nel tie break
        for(int i=0; i<5; i++) scorePoints(1, 4);
        for(int i=0; i<5; i++) scorePoints(2, 4);
        scorePoints(1, 4); scorePoints(2, 4); 
        scorePoints(2, 7); 
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testGameOver() {
        // Partita completa 3 set a 0
        for(int s=0; s<3; s++) {
             for(int g=0; g<6; g++) scorePoints(1, 4);
        }
        assertTrue(manager.isGameOver());
        assertEquals("PARTITA FINITA", manager.getGameScore());
        
        manager.pointScored(1);
        assertTrue(manager.isGameOver()); // Verify still game over
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
    }
    
    @Test
    public void testResetMethodsDirectly() {
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
        
        scorePoints(1, 4); 
        manager.resetGameAndPoints();
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
    
    @Test
    public void testPrintScore_Deep() throws Exception {
        // Copertura formattazione stampa
        setIntField("currentSet", 4);
        setArrayField("setsP1", 0, 6);
        setArrayField("setsP1", 1, 4);
        setArrayField("setsP1", 2, 7);
        manager.printScore();
    }
}