/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Mio"
Cognome: "Cognome"
Username: ang.paolella@studenti.unina.it
UserID: 439
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // --- Helper Methods to assist in atomic tests ---

    private void scorePoints(TennisScoreManager tm, int player, int points) {
        for (int i = 0; i < points; i++) {
            tm.pointScored(player);
        }
    }

    private void winGame(TennisScoreManager tm, int player) {
        scorePoints(tm, player, 4);
    }

    private void winSet(TennisScoreManager tm, int player) {
        for (int i = 0; i < 6; i++) {
            winGame(tm, player);
        }
    }

    // --- Tests ---

    @Test
    public void ConstructorInitialStateTest() {
        TennisScoreManager tm = new TennisScoreManager();
        assertEquals("Love-Love", tm.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void ResetPointsTest() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1); // 15-Love
        tm.resetPoints();
        assertEquals("Love-Love", tm.getGameScore());
    }

    @Test
    public void PointScoredPlayerOneStandardTest() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1);
        assertEquals("15-Love", tm.getGameScore());
        tm.pointScored(1);
        assertEquals("30-Love", tm.getGameScore());
        tm.pointScored(1);
        assertEquals("40-Love", tm.getGameScore());
    }

    @Test
    public void PointScoredPlayerTwoStandardTest() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(2);
        assertEquals("Love-15", tm.getGameScore());
        tm.pointScored(2);
        assertEquals("Love-30", tm.getGameScore());
    }

    @Test
    public void PointScoredInvalidPlayerTest() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1);
        tm.pointScored(3); // Invalid
        assertEquals("15-Love", tm.getGameScore()); // State should not change
    }

    @Test
    public void GetGameScoreDeuceTest() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40
        assertEquals("Deuce", tm.getGameScore());
    }

    @Test
    public void GetGameScoreAdvantageP1Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40
        tm.pointScored(1); // Adv P1
        assertEquals("Vantaggio P1", tm.getGameScore());
    }

    @Test
    public void GetGameScoreAdvantageP2BugTest() {
        // This test covers the logic flow where P2 has advantage.
        // Due to the bug in source: `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)`
        // The condition is impossible. It falls through to "Errore Game".
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40
        tm.pointScored(2); // Adv P2 in reality
        
        assertEquals("Errore Game", tm.getGameScore());
    }

    @Test
    public void CheckGamePointWinP1Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 4); // Wins game
        // Match score should show 1-0 games
        assertTrue(tm.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", tm.getGameScore()); // Points reset
    }

    @Test
    public void CheckGamePointWinP2Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 2, 4); // Wins game
        assertTrue(tm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void CheckSetPointWinP1Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1); // P1 wins 6 games
        // Should move to next set (Set 2)
        // Output format checks implicit printScore coverage
        String matchScore = tm.getMatchScore(); 
        // "1-0 (Game: 0-0 Love-Love)" because Set 1 is stored, current is Set 2
        assertEquals("1-0 (Game: 0-0 Love-Love)", matchScore);
    }

    @Test
    public void CheckSetPointWinP2Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 2); // P2 wins 6 games
        assertEquals("0-1 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void CheckSetPointTieBreakTriggerTest() {
        TennisScoreManager tm = new TennisScoreManager();
        // Get to 5-5
        for(int i=0; i<5; i++) { winGame(tm, 1); winGame(tm, 2); }
        winGame(tm, 1); // 6-5
        winGame(tm, 2); // 6-6 -> Tie Break
        
        assertTrue(tm.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", tm.getTieBreakScore());
    }

    @Test
    public void CheckTieBreakPointWinP1Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Trigger Tie Break (6-6)
        for(int i=0; i<5; i++) { winGame(tm, 1); winGame(tm, 2); }
        winGame(tm, 1); winGame(tm, 2);

        // Win Tie Break 7-0
        scorePoints(tm, 1, 7); 
        
        // P1 should have won the set (1-0 sets)
        assertEquals("1-0 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void CheckTieBreakPointWinP2Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Trigger Tie Break (6-6)
        for(int i=0; i<5; i++) { winGame(tm, 1); winGame(tm, 2); }
        winGame(tm, 1); winGame(tm, 2);

        // Win Tie Break 0-7
        scorePoints(tm, 2, 7); 
        
        // P2 should have won the set (0-1 sets)
        assertEquals("0-1 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void IsGameOverP1WinsMatchTest() {
        TennisScoreManager tm = new TennisScoreManager();
        // Win 3 sets
        winSet(tm, 1);
        winSet(tm, 1);
        winSet(tm, 1);
        
        assertTrue(tm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", tm.getMatchScore());
        assertEquals("PARTITA FINITA", tm.getGameScore());
    }

    @Test
    public void IsGameOverP2WinsMatchTest() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 2);
        winSet(tm, 2);
        winSet(tm, 2);
        
        assertTrue(tm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", tm.getMatchScore());
    }

    @Test
    public void PointScoredAfterGameOverTest() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1);
        winSet(tm, 1);
        winSet(tm, 1);
        
        // Try to score after game over
        tm.pointScored(1);
        
        // Score shouldn't change, verified by standard message
        assertTrue(tm.isGameOver());
    }
    
    @Test
    public void MoveToNextSetLogicTest() {
        // To cover the loop in printScore `for (int i = 0; i < currentSet - 1; i++)`
        // We need to be at least in Set 3 to verify correct printing of previous sets
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1); // Set 1 done
        winSet(tm, 2); // Set 2 done
        // Now in Set 3
        tm.pointScored(1); // Triggers printScore with historical sets
        
        String score = tm.getMatchScore();
        // 1 Set each. Current Set 3.
        assertEquals("1-1 (Game: 0-0 15-Love)", score);
    }
    
    @Test
    public void CheckSetPointAlternativeWinConditionTest() {
        // Covers the logic: gamesP1 >= 6 && gamesP1 >= gamesP2 + 2
        // Specifically 7-5 (Standard set extension without tiebreak logic if implied, 
        // though code treats 6-6 as tiebreak, 7-5 is valid win)
        TennisScoreManager tm = new TennisScoreManager();
        // 5-5
        for(int i=0; i<5; i++) { winGame(tm, 1); winGame(tm, 2); }
        winGame(tm, 1); // 6-5
        winGame(tm, 1); // 7-5
        
        // Set 1 won by P1
        assertEquals("1-0 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void CheckSetPointAlternativeWinConditionP2Test() {
        // Covers the P2 7-5 win path
        TennisScoreManager tm = new TennisScoreManager();
        for(int i=0; i<5; i++) { winGame(tm, 1); winGame(tm, 2); }
        winGame(tm, 2); // 5-6
        winGame(tm, 2); // 5-7
        
        assertEquals("0-1 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }
}