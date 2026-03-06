import org.junit.Before;
import org.junit.Test;
// Import static Assert methods for readability
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for TennisScoreManager.
 * This suite aims for high line coverage by testing various scenarios,
 * including standard play, deuce, advantage, set wins, tie-breaks, and game over conditions.
 */
public class TennisScoreManagerTest {

    private TennisScoreManager manager;

    /**
     * Set up a new TennisScoreManager instance before each test.
     * This ensures all tests are independent.
     */
    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    // --- Helper Methods ---

    /**
     * Helper method to score points for a player.
     * @param player The player (1 or 2).
     * @param points The number of points to score.
     */
    private void scorePoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    /**
     * Helper method to win a specific number of games for a player.
     * Assumes the opponent scores no points in these games.
     * @param player The player (1 or 2).
     * @param games The number of games to win.
     */
    private void winGames(int player, int games) {
        for (int i = 0; i < games; i++) {
            // Win a game from Love-Love
            scorePoints(player, 4);
        }
    }

    /**
     * Helper method to set the game score directly.
     * @param p1Games Games for player 1.
     * @param p2Games Games for player 2.
     */
    private void setGameScore(int p1Games, int p2Games) {
        winGames(1, p1Games);
        winGames(2, p2Games);
    }

    /**
     * Helper method to win a full set for a player.
     * @param player The player (1 or 2).
     * @param p1Games Games for player 1.
     * @param p2Games Games for player 2.
     */
    private void winSet(int player, int p1Games, int p2Games) {
        // This is a simplified way to win a set.
        // We assume player 1 wins p1Games and player 2 wins p2Games.
        // The final game-winning point is scored by the 'player' specified.
        int opponent = (player == 1) ? 2 : 1;
        int p1Target = p1Games;
        int p2Target = p2Games;

        // Play games up to one-before-the-end
        if (p1Games > p2Games) {
            winGames(1, p1Target - 1);
            winGames(2, p2Target);
            winGames(1, 1); // Win the final game
        } else {
            winGames(1, p1Target);
            winGames(2, p2Target - 1);
            winGames(2, 1); // Win the final game
        }
    }

    // --- Test Cases ---

    @Test
    public void testInitialScore_IsLoveLove_0_0() {
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue("Match score should be 0-0, Love-Love", manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testPointScored_InvalidPlayer_DoesNotChangeScore() {
        manager.pointScored(3); // Invalid player
        manager.pointScored(0); // Invalid player
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_15_Love() {
        scorePoints(1, 1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_Love_30() {
        scorePoints(2, 2);
        assertEquals("Love-30", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_40_15() {
        scorePoints(1, 3);
        scorePoints(2, 1);
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_Deuce() {
        scorePoints(1, 3);
        scorePoints(2, 3);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_AdvantageP1() {
        scorePoints(1, 4);
        scorePoints(2, 3);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_AdvantageP2_ReturnsError_DueToBug() {
        // This tests the bug in getGameScore:
        // `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)` is always false.
        // The code falls through to `return "Errore Game";`
        scorePoints(1, 3);
        scorePoints(2, 4);
        assertEquals("Errore Game", manager.getGameScore());
    }
    
    @Test
    public void testGetGameScore_BackToDeuce() {
        scorePoints(1, 4); // Adv P1
        scorePoints(2, 3);
        scorePoints(2, 1); // P2 scores, back to Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testPointScored_P1WinsGame_From_40_0() {
        scorePoints(1, 3); // 40-0
        scorePoints(1, 1); // P1 wins game
        assertTrue("Match score should show 1-0 games", manager.getMatchScore().contains("(Game: 1-0 Love-Love)"));
        assertEquals("Love-Love", manager.getGameScore()); // Points should reset
    }

    @Test
    public void testPointScored_P2WinsGame_From_Deuce() {
        scorePoints(1, 3);
        scorePoints(2, 3); // Deuce
        scorePoints(2, 1); // Adv P2 (buggy score string, but state is 3-4)
        scorePoints(2, 1); // P2 wins game
        assertTrue("Match score should show 0-1 games", manager.getMatchScore().contains("(Game: 0-1 Love-Love)"));
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testPointScored_P1WinsGame_From_AdvP1() {
        scorePoints(1, 4);
        scorePoints(2, 3); // Adv P1
        scorePoints(1, 1); // P1 wins game
        assertTrue("Match score should show 1-0 games", manager.getMatchScore().contains("(Game: 1-0 Love-Love)"));
    }

    @Test
    public void testSetWin_P1_6_0() {
        winGames(1, 6); // P1 wins 6-0
        // Set 1 should be complete, now in Set 2
        assertTrue("Match score should show 1-0 sets", manager.getMatchScore().contains("1-0 (Game: 0-0 Love-Love)"));
        assertFalse("Game should not be over", manager.isGameOver());
    }

    @Test
    public void testSetWin_P2_6_3() {
        winGames(1, 3);
        winGames(2, 6); // P2 wins 6-3
        assertTrue("Match score should show 0-1 sets", manager.getMatchScore().contains("0-1 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testSetWin_P1_7_5() {
        setGameScore(5, 5); // 5-5
        winGames(1, 1); // 6-5
        winGames(1, 1); // 7-5, P1 wins set
        assertTrue("Match score should show 1-0 sets", manager.getMatchScore().contains("1-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testSetWin_P2_7_5() {
        setGameScore(5, 5); // 5-5
        winGames(2, 1); // 5-6
        winGames(2, 1); // 5-7, P2 wins set
        assertTrue("Match score should show 0-1 sets", manager.getMatchScore().contains("0-1 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testTieBreak_Starts_At_6_6() {
        setGameScore(6, 6); // 6-6, should trigger tie-break
        assertTrue("Match score should indicate Tie-Break", manager.getMatchScore().contains("TIE-BREAK: 0-0"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }
    
    @Test
    public void testGetTieBreakScore_2_1() {
        setGameScore(6, 6); // Start tie-break
        scorePoints(1, 2);
        scorePoints(2, 1);
        assertEquals("TIE-BREAK: 2-1", manager.getTieBreakScore());
    }

    @Test
    public void testTieBreak_P1Wins_7_0_DoesNotWinSet_DueToBug() {
        setGameScore(6, 6); // Start tie-break
        scorePoints(1, 7); // P1 wins tie-break 7-0
        
        // Due to bug in checkTieBreakPoint (calls resetGameAndPoints before checkSetPoint),
        // the set win is NOT registered.
        // We test this *actual* (buggy) behavior.
        // `checkSetPoint` is called with 0-0, so no set is won.
        assertTrue("Match score should still be 0-0 sets due to bug", manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testTieBreak_P2Wins_8_6_DoesNotWinSet_DueToBug() {
        setGameScore(6, 6); // Start tie-break
        scorePoints(1, 6);
        scorePoints(2, 6); // 6-6 in tie-break
        scorePoints(2, 1); // 6-7
        scorePoints(2, 1); // 6-8, P2 wins tie-break
        
        // Test the buggy behavior
        assertTrue("Match score should still be 0-0 sets due to bug", manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testGameOver_P1Wins_3_0() {
        winSet(1, 6, 0); // Set 1
        winSet(1, 6, 0); // Set 2
        winSet(1, 6, 0); // Set 3
        
        assertTrue("Game should be over", manager.isGameOver());
    }

    @Test
    public void testGameOver_P2Wins_3_1() {
        winSet(1, 6, 0); // Set 1: P1
        winSet(2, 6, 0); // Set 2: P2
        winSet(2, 6, 0); // Set 3: P2
        winSet(2, 6, 0); // Set 4: P2
        
        assertTrue("Game should be over", manager.isGameOver());
        assertFalse("P1 should not have 3 sets", manager.getMatchScore().contains("P1: 3 Set"));
    }

    @Test
    public void testPointScored_AfterGameOver_DoesNothing() {
        winSet(1, 6, 0); // Set 1
        winSet(1, 6, 0); // Set 2
        winSet(1, 6, 0); // Set 3
        
        assertTrue(manager.isGameOver());
        String scoreBefore = manager.getMatchScore();
        
        manager.pointScored(1); // Try to score
        
        String scoreAfter = manager.getMatchScore();
        assertEquals("Score should not change after game is over", scoreBefore, scoreAfter);
    }

    @Test
    public void testGetGameScore_ReturnsGameOver_WhenMatchIsOver() {
        winSet(1, 6, 0);
        winSet(1, 6, 0);
        winSet(1, 6, 0);
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void testGetMatchScore_InProgress_WithSets() {
        winSet(1, 6, 2); // Set 1: P1
        winSet(2, 7, 5); // Set 2: P2
        // Now in Set 3
        winGames(1, 2); // 2-0
        winGames(2, 1); // 2-1
        scorePoints(1, 3); // 40-0
        scorePoints(2, 1); // 40-15
        
        // Set score 1-1. Game score 2-1. Point score 40-15
        String expected = "1-1 (Game: 2-1 40-15)";
        assertEquals(expected, manager.getMatchScore());
    }

    @Test
    public void testGetMatchScore_GameOver_P1() {
        winSet(1, 6, 0);
        winSet(1, 6, 0);
        winSet(1, 6, 0);
        // This will test the isGameOver() == true path in getMatchScore
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void testGetMatchScore_GameOver_P2() {
        winSet(1, 6, 0);
        winSet(2, 6, 0);
        winSet(2, 6, 0);
        winSet(2, 6, 0);
        // This will test the isGameOver() == true path in getMatchScore
        assertEquals("P1: 1 Set | P2: 3 Set", manager.getMatchScore());
    }
    
    @Test
    public void testPrintScore_CoversPaths() {
        // This test just calls printScore in various states to ensure
        // the loops and conditionals within it are executed for coverage.
        // We can't easily assert System.out without more setup (like System Rules).
        
        // State 1: Beginning of match
        manager.printScore(); // currentSet = 1, loops won't run
        
        // State 2: After 1 set
        winSet(1, 6, 0);
        manager.printScore(); // currentSet = 2, loops run once, i < currentSet - 2 is false
        
        // State 3: After 3 sets
        winSet(2, 6, 0);
        winSet(1, 6, 0);
        manager.printScore(); // currentSet = 4, loops run, i < currentSet - 2 is true
        
        // State 4: During TieBreak
        setGameScore(6, 6);
        manager.printScore(); // isTieBreak = true
    }

    @Test
    public void testResetPoints_ResetsPoints() {
        scorePoints(1, 3);
        scorePoints(2, 2);
        assertEquals("40-30", manager.getGameScore());
        
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testResetGameAndPoints_ResetsGamesAndPoints() {
        winGames(1, 3);
        winGames(2, 2);
        scorePoints(1, 1);
        
        assertTrue(manager.getMatchScore().contains("(Game: 3-2 15-Love)"));
        
        manager.resetGameAndPoints();
        
        assertTrue(manager.getMatchScore().contains("(Game: 0-0 Love-Love)"));
    }
}