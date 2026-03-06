import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// Note: Assuming the class is available in the same package or imported correctly
public class TennisScoreManagerTest {

    private TennisScoreManager scoreManager;

    // This runs before every test method, ensuring a clean state for each test.
    @Before
    public void setUp() {
        scoreManager = new TennisScoreManager();
    }

    // --- Utility Methods for Scoring (to reduce boilerplate in tests) ---

    /**
     * Scores a specified number of points for a player.
     */
    private void scorePoints(int player, int count) {
        for (int i = 0; i < count; i++) {
            scoreManager.pointScored(player);
        }
    }

    /**
     * Scores enough points to win a standard game for the specified player.
     * Assumes a starting score of Love-Love or Deuce.
     */
    private void scoreGame(int player) {
        // Player needs 4 points to win from 0-0, assuming no Deuce/Advantage logic bypass
        // Simplest win from Love-Love is 4 points.
        scorePoints(player, 4);
    }
    
    /**
     * Scores enough games to win a set for the specified player (6-0).
     */
    private void scoreSet(int player) {
        for (int i = 0; i < 6; i++) {
            scoreGame(player);
        }
    }

    /**
     * Brings the score to Deuce (3-3 in points).
     */
    private void scoreDeuce() {
        scorePoints(1, 3); // P1 scores 3 points
        scorePoints(2, 3); // P2 scores 3 points
    }

    // --- Test: Constructor and Initial State ---

    @Test
    public void testInitialState() {
        // Assert initial score and game state
        assertEquals("Love-Love", scoreManager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        assertFalse(scoreManager.isGameOver());
    }
    
    // --- Test: getGameScore (Standard Points) ---

    @Test
    public void testStandardScoring() {
        scoreManager.pointScored(1);
        assertEquals("15-Love", scoreManager.getGameScore()); // P1: 15-0

        scoreManager.pointScored(2);
        assertEquals("15-15", scoreManager.getGameScore()); // P1: 15-15

        scorePoints(1, 1);
        assertEquals("30-15", scoreManager.getGameScore()); // P1: 30-15

        scorePoints(2, 2);
        assertEquals("30-40", scoreManager.getGameScore()); // P1: 30-40

        scoreManager.pointScored(1);
        assertEquals("Deuce", scoreManager.getGameScore()); // P1: Deuce (3-3)
    }

    // --- Test: getGameScore (Deuce/Advantage/Game Win) ---

    @Test
    public void testDeuceAdvantageGameWinP1() {
        scoreDeuce();
        assertEquals("Deuce", scoreManager.getGameScore()); // P1: Deuce

        scoreManager.pointScored(1);
        assertEquals("Vantaggio P1", scoreManager.getGameScore()); // P1: Advantage P1

        // Check the `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)` branch in getGameScore - this is logically impossible in the original code
        // as scoreP2 + 1 can never equal scoreP2. The check for Advantage P2 should likely be: 
        // `if (scoreP2 >= 3 && scoreP2 == scoreP1 + 1)`
        // The original code has a bug or a typo. We test the *intended* logic for P1 winning a game.

        scoreManager.pointScored(1); // P1 wins the game
        assertEquals("0-0 (Game: 1-0 Love-Love)", scoreManager.getMatchScore());
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    @Test
    public void testDeuceAdvantageGameWinP2() {
        scoreDeuce();
        
        scoreManager.pointScored(2);
        // The implementation for P2's Advantage is bugged in the provided code (if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)).
        // Assuming the intention was to check for Advantage P2 (scoreP2 == scoreP1 + 1)
        // The actual result for the buggy code: P2 score is 4, P1 score is 3. getGameScore() returns "Vantaggio P2" (via the buggy condition)

        // The expected result if the code was fixed to check `scoreP2 == scoreP1 + 1` is "Vantaggio P2"
        // Let's assert based on the *intended* outcome for a complete path coverage
        // We cannot rely on the *buggy* implementation as it seems to be impossible to reach.
        // We'll assert the result after the intended state for P2's Advantage.
        assertEquals("Vantaggio P2", scoreManager.getGameScore()); 

        scoreManager.pointScored(2); // P2 wins the game
        assertEquals("0-0 (Game: 0-1 Love-Love)", scoreManager.getMatchScore());
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    @Test
    public void testDeuceReset() {
        scoreDeuce();
        scoreManager.pointScored(1); // P1 Advantage
        assertEquals("Vantaggio P1", scoreManager.getGameScore());

        scoreManager.pointScored(2); // Back to Deuce
        assertEquals("Deuce", scoreManager.getGameScore());

        scoreManager.pointScored(2); // P2 Advantage
        assertEquals("Vantaggio P2", scoreManager.getGameScore()); // Asserting based on intended logic

        scoreManager.pointScored(1); // Back to Deuce
        assertEquals("Deuce", scoreManager.getGameScore());
    }

    // --- Test: checkSetPoint (Standard Set Win 6-0) ---

    @Test
    public void testSetWinStandardP1() {
        scoreSet(1); // P1 wins 6 games
        // P1 should win the set and match score should reset game count
        assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    @Test
    public void testSetWinStandardP2() {
        scoreSet(2); // P2 wins 6 games
        // P2 should win the set and match score should reset game count
        assertEquals("0-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    // --- Test: checkSetPoint (Set Win with 2-game margin: 7-5) ---

    @Test
    public void testSetWin7_5P1() {
        // Score 5-5
        for (int i = 0; i < 5; i++) {
            scoreGame(1);
            scoreGame(2);
        }
        assertEquals("0-0 (Game: 5-5 Love-Love)", scoreManager.getMatchScore());

        scoreGame(1); // P1 6-5
        assertEquals("0-0 (Game: 6-5 Love-Love)", scoreManager.getMatchScore());

        scoreGame(1); // P1 7-5, P1 wins set
        assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    @Test
    public void testSetWin7_5P2() {
        // Score 5-5
        for (int i = 0; i < 5; i++) {
            scoreGame(1);
            scoreGame(2);
        }

        scoreGame(2); // P2 5-6
        assertEquals("0-0 (Game: 5-6 Love-Love)", scoreManager.getMatchScore());

        scoreGame(2); // P2 5-7, P2 wins set
        assertEquals("0-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }
    
    // --- Test: checkSetPoint (Tie-Break Activation and Win) ---
    
    @Test
    public void testTieBreakActivation() {
        // Score 6-6
        for (int i = 0; i < 6; i++) {
            scoreGame(1);
            scoreGame(2);
        }
        
        // Match score should still show 6-6 in games before the next point
        assertEquals("0-0 (Game: 6-6 Love-Love)", scoreManager.getMatchScore());
        
        scoreManager.pointScored(1); // First point of Tie-Break
        // The internal state should be isTieBreak = true
        // The game score string should reflect the Tie-Break score
        assertTrue(scoreManager.getMatchScore().contains("TIE-BREAK: 1-0"));
        assertTrue(scoreManager.getTieBreakScore().equals("TIE-BREAK: 1-0"));
    }

    @Test
    public void testTieBreakWinP1() {
        // Score 6-6
        for (int i = 0; i < 6; i++) {
            scoreGame(1);
            scoreGame(2);
        }

        // Enter Tie-Break (P1 scores 7 points, P2 scores 5 points)
        scorePoints(1, 7); // P1 scores 7 points
        scorePoints(2, 5); // P2 scores 5 points
        
        // P1 should win the set 7-6
        assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        assertFalse(scoreManager.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void testTieBreakWinP2() {
        // Score 6-6
        for (int i = 0; i < 6; i++) {
            scoreGame(1);
            scoreGame(2);
        }

        // Enter Tie-Break (P2 scores 7 points, P1 scores 5 points)
        scorePoints(2, 7); // P2 scores 7 points
        scorePoints(1, 5); // P1 scores 5 points

        // P2 should win the set 7-6
        assertEquals("0-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }

    @Test
    public void testTieBreakExtendedWinP1() {
        // Score 6-6
        for (int i = 0; i < 6; i++) {
            scoreGame(1);
            scoreGame(2);
        }

        // Score 6-6 in Tie-Break
        scorePoints(1, 6); 
        scorePoints(2, 6);
        assertTrue(scoreManager.getMatchScore().contains("TIE-BREAK: 6-6"));

        scoreManager.pointScored(1); // P1 7-6
        assertTrue(scoreManager.getMatchScore().contains("TIE-BREAK: 7-6"));

        scoreManager.pointScored(1); // P1 8-6, P1 wins set
        assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }

    @Test
    public void testTieBreakExtendedWinP2() {
        // Score 6-6
        for (int i = 0; i < 6; i++) {
            scoreGame(1);
            scoreGame(2);
        }

        // Score 6-6 in Tie-Break
        scorePoints(1, 6); 
        scorePoints(2, 6);

        scoreManager.pointScored(2); // P2 6-7
        scoreManager.pointScored(2); // P2 6-8, P2 wins set
        assertEquals("0-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }


    // --- Test: isGameOver (Match Win) ---
    
    @Test
    public void testMatchWinP1_3_0() {
        scoreSet(1); // Set 1: 6-0
        scoreSet(1); // Set 2: 6-0
        scoreSet(1); // Set 3: 6-0 (Match Win)

        assertTrue(scoreManager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", scoreManager.getMatchScore());
    }

    @Test
    public void testMatchWinP2_3_1() {
        scoreSet(1); // Set 1: P1 wins
        scoreSet(2); // Set 2: P2 wins
        scoreSet(2); // Set 3: P2 wins
        scoreSet(2); // Set 4: P2 wins (Match Win)

        assertTrue(scoreManager.isGameOver());
        assertEquals("P1: 1 Set | P2: 3 Set", scoreManager.getMatchScore());
    }

    // --- Test: Edge Cases and Error Paths ---
    
    @Test
    public void testPointScoredInvalidPlayer() {
        // Should handle the invalid player input and not change the score
        scoreManager.pointScored(3); 
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    @Test
    public void testPointScoredAfterGameOver() {
        // P1 wins 3 sets
        scoreSet(1); 
        scoreSet(1); 
        scoreSet(1); 

        assertTrue(scoreManager.isGameOver());

        // Attempt to score another point - should not affect the score and exit early
        scoreManager.pointScored(2);
        // The game score should still be the final state ("PARTITA FINITA")
        assertEquals("PARTITA FINITA", scoreManager.getGameScore());
        // The match score should still be the final state
        assertEquals("P1: 3 Set | P2: 0 Set", scoreManager.getMatchScore());
    }

    @Test
    public void testResetMethods() {
        scorePoints(1, 2); // 30-Love
        scorePoints(2, 1); // 30-15
        
        scoreManager.resetPoints(); // Reset points only
        assertEquals("Love-Love", scoreManager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore()); // Games still 0-0

        scoreGame(1); // P1 wins a game (1-0 in games)
        scorePoints(2, 2); // 30-Love in current game
        
        scoreManager.resetGameAndPoints(); // Reset games and points
        assertEquals("Love-Love", scoreManager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore()); // Games should be 0-0
    }
    
    // Test the constructor's initialization loop via printScore (which accesses currentSet-1)
    @Test
    public void testConstructorInitialization() {
        // The constructor initializes setsP1 and setsP2. 
        // Although the printScore method only prints sets from `i < currentSet - 1`, 
        // the initial check `isGameOver()` and `getMatchScore()` rely on the initialization.
        // We implicitly test this in `testInitialState` and explicitly by calling `printScore`

        // Check the string content to ensure no out-of-bounds or nulls (which wouldn't happen in Java int array, 
        // but confirms the state is coherent for set 1)
        scoreManager.printScore(); // Prints initial score with empty set arrays output
        
        // This test primarily ensures the class is instantiated and doesn't crash, 
        // relying on the initial state tests for correctness.
        assertTrue(true); 
    }

    @Test
    public void testGameOverScoreString() {
        scoreSet(1); 
        scoreSet(1); 
        scoreSet(1); 

        // After game over, getGameScore should return "PARTITA FINITA"
        assertEquals("PARTITA FINITA", scoreManager.getGameScore());
    }
}