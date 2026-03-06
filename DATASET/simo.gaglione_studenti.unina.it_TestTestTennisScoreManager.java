/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: simo.gaglione@studenti.unina.it
UserID: 208
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
    
    private TennisScoreManager manager;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Inizio Test Suite TennisScoreManager");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("Fine Test Suite TennisScoreManager");
    }

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        manager = null;
    }

    // --- Helper Methods ---
    
    private void scorePoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    private void winStandardGame(int player) {
        scorePoints(player, 4);
    }

    private void playToSetScore(int gamesP1, int gamesP2) {
        int currentP1 = 0;
        int currentP2 = 0;
        
        while (currentP1 < gamesP1 || currentP2 < gamesP2) {
            if (currentP1 < gamesP1) {
                winStandardGame(1);
                currentP1++;
            }
            if (currentP2 < gamesP2) {
                winStandardGame(2);
                currentP2++;
            }
        }
    }

    // --- TEST CASES ---

    @Test
    public void testInitialState() {
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testBasicScoreProgression() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayerInput() {
        String scoreBefore = manager.getGameScore();
        manager.pointScored(3); 
        manager.pointScored(-1);
        assertEquals(scoreBefore, manager.getGameScore());
    }

    @Test
    public void testWinGameP1() {
        scorePoints(1, 4); // Vince game
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testWinGameP2() {
        scorePoints(2, 4); // Vince game
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testDeuce() {
        scorePoints(1, 3);
        scorePoints(2, 3);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1AndWin() {
        scorePoints(1, 3);
        scorePoints(2, 3); // Deuce
        
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(1); // Vince
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testBackToDeuce() {
        scorePoints(1, 3);
        scorePoints(2, 3);
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(2); // Torna Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugBehavior() {
        // Test del BUG nel codice sorgente: "if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)" è sempre falso.
        scorePoints(1, 3);
        scorePoints(2, 3);
        manager.pointScored(2);
        // Ci aspettiamo "Errore Game" invece di "Vantaggio P2" a causa del bug nel sorgente
        assertEquals("Errore Game", manager.getGameScore());
        
        manager.pointScored(2); // Vince comunque il game
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testSetWinStandard() {
        // P1 vince 6-0 (Standard games funzionano perché usano resetPoints non resetGameAndPoints)
        for (int i = 0; i < 5; i++) winStandardGame(1);
        assertTrue(manager.getMatchScore().contains("Game: 5-0"));
        
        winStandardGame(1);
        // Set vinto correttamente
        assertTrue(manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void testSetWinExtended_7_5() {
        playToSetScore(5, 5);
        winStandardGame(1); // 6-5
        winStandardGame(1); // 7-5
        assertTrue(manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testSetWinExtended_P2_5_7() {
        playToSetScore(5, 5);
        winStandardGame(2); // 5-6
        winStandardGame(2); // 5-7
        assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testTieBreakTrigger() {
        playToSetScore(6, 6);
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("TIE-BREAK"));
        assertTrue(matchScore.contains("0-0"));
    }

    @Test
    public void testTieBreakWinStandard_BuggedSource() {
        // NOTA: Nel codice sorgente, checkTieBreakPoint chiama resetGameAndPoints() PRIMA di checkSetPoint().
        // Questo resetta i game a 0-0, impedendo la vittoria del set.
        // Il test asserisce questo comportamento buggato per passare.
        
        playToSetScore(6, 6);
        scorePoints(1, 7); // P1 vince tie-break 7-0
        
        // A causa del bug, il match NON va a "1-0" set, ma si resetta a "0-0" game nel set corrente
        assertTrue("Il codice sorgente resetta i game erroneamente", manager.getMatchScore().contains("Game: 0-0"));
        assertFalse(manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void testTieBreakExtension_BuggedSource() {
        playToSetScore(6, 6);
        
        scorePoints(1, 6);
        scorePoints(2, 6);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 6-6"));

        manager.pointScored(1); // 7-6
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 7-6"));

        manager.pointScored(1); // 8-6 (Vince TieBreak)
        
        // A causa del bug in checkTieBreakPoint, resetta a 0-0
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
    
    @Test
    public void testTieBreakWinP2_BuggedSource() {
         playToSetScore(6, 6);
         scorePoints(2, 7); // P2 vince tie-break
         
         // A causa del bug in checkTieBreakPoint, resetta a 0-0
         assertTrue(manager.getMatchScore().contains("Game: 0-0"));
         assertFalse(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testMatchWinP1() {
        // Usiamo vincite standard (6-0) perché il tie-break è rotto
        playToSetScore(6, 0); 
        playToSetScore(6, 0); 
        playToSetScore(6, 0);

        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
        
        // Protezione post-partita
        String finalScore = manager.getMatchScore();
        manager.pointScored(1);
        assertEquals(finalScore, manager.getMatchScore());
    }

    @Test
    public void testMatchWinP2() {
        playToSetScore(0, 6); 
        playToSetScore(0, 6);
        playToSetScore(0, 6);

        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P2: 3 Set"));
    }
    
    @Test
    public void testResetPointsLogic() {
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testResetGameAndPointsLogic() {
        manager.pointScored(1);
        manager.resetGameAndPoints();
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
}