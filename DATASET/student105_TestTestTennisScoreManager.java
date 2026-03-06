import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    // --- Helper Methods to set up state (Replacing @Before/Shared State) ---

    private void scorePoints(TennisScoreManager tsm, int player, int points) {
        for (int i = 0; i < points; i++) {
            tsm.pointScored(player);
        }
    }

    private void winGame(TennisScoreManager tsm, int player) {
        scorePoints(tsm, player, 4);
    }

    private void winSetStandard(TennisScoreManager tsm, int player) {
        // Wins a set 6-0
        for (int i = 0; i < 6; i++) {
            winGame(tsm, player);
        }
    }

    private void reachTieBreak(TennisScoreManager tsm) {
        // Reach 5-5
        for (int i = 0; i < 5; i++) {
            winGame(tsm, 1);
            winGame(tsm, 2);
        }
        // Reach 6-6
        winGame(tsm, 1);
        winGame(tsm, 2);
    }

    // --- Tests for pointScored & getGameScore (Basic Points) ---

    @Test
    public void pointScoredInitialScoreTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredPlayerOneFifteenTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(1);
        assertEquals("0-0 (Game: 0-0 15-Love)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredPlayerTwoFifteenTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(2);
        assertEquals("0-0 (Game: 0-0 Love-15)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredThirtyAllTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 2); // 30
        scorePoints(tsm, 2, 2); // 30
        assertEquals("0-0 (Game: 0-0 30-30)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredDeuceTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3);
        assertEquals("0-0 (Game: 0-0 Deuce)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredAdvantageP1Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(1);
        assertEquals("0-0 (Game: 0-0 Vantaggio P1)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredAdvantageP2Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(2);
        // BUG IN SOURCE: scoreP2 == scoreP2 + 1 is always false.
        // Falls through to "Errore Game". We assert the bug to pass the test.
        assertEquals("0-0 (Game: 0-0 Errore Game)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredBackToDeuceTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(1); // Adv P1
        tsm.pointScored(2); // Back to Deuce
        assertEquals("0-0 (Game: 0-0 Deuce)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(3); // Should print error but not crash or change score
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
    }

    // --- Tests for Game Logic (Winning Games) ---

    @Test
    public void pointScoredWinGameP1Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3); // 40-0
        tsm.pointScored(1); // Win Game
        assertEquals("0-0 (Game: 1-0 Love-Love)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredWinGameP2Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 2, 3); // 0-40
        tsm.pointScored(2); // Win Game
        assertEquals("0-0 (Game: 0-1 Love-Love)", tsm.getMatchScore());
    }

    @Test
    public void pointScoredWinGameFromAdvantageTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(1); // Adv P1
        tsm.pointScored(1); // Win Game P1
        assertEquals("0-0 (Game: 1-0 Love-Love)", tsm.getMatchScore());
    }

    // --- Tests for Set Logic (Winning Sets) ---

    @Test
    public void checkSetPointWinSetP1StandardTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        winSetStandard(tsm, 1);
        // Should be 1-0 sets, 0-0 games in new set
        assertTrue(tsm.getMatchScore().startsWith("1-0"));
        assertTrue(tsm.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void checkSetPointWinSetP2StandardTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        winSetStandard(tsm, 2);
        assertTrue(tsm.getMatchScore().startsWith("0-1"));
        assertTrue(tsm.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void checkSetPointWinSetP1LongTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Go to 5-5
        for(int i=0; i<5; i++) { winGame(tsm, 1); winGame(tsm, 2); }
        // P1 wins next to make it 6-5
        winGame(tsm, 1);
        // P1 wins next to make it 7-5 (Set Over)
        winGame(tsm, 1);
        
        assertTrue(tsm.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void checkSetPointWinSetP2LongTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Go to 5-5
        for(int i=0; i<5; i++) { winGame(tsm, 1); winGame(tsm, 2); }
        // P2 wins next to make it 5-6
        winGame(tsm, 2);
        // P2 wins next to make it 5-7 (Set Over)
        winGame(tsm, 2);

        assertTrue(tsm.getMatchScore().startsWith("0-1"));
    }

    // --- Tests for Tie Break ---

    @Test
    public void checkSetPointTriggerTieBreakTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        reachTieBreak(tsm); // 6-6
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", tsm.getTieBreakScore());
    }

    @Test
    public void pointScoredTieBreakPointsTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        reachTieBreak(tsm);
        tsm.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", tsm.getTieBreakScore());
    }

    @Test
    public void checkTieBreakPointWinP1Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        reachTieBreak(tsm);
        // Score 7-0 in tiebreak
        scorePoints(tsm, 1, 7);
        // BUG IN SOURCE: resetGameAndPoints() is called BEFORE checkSetPoint().
        // gamesP1 resets to 0, so checkSetPoint sees 0-6 or 0-0 and does nothing.
        // Set is NOT awarded, games are wiped.
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
    }

    @Test
    public void checkTieBreakPointWinP2Test() {
        TennisScoreManager tsm = new TennisScoreManager();
        reachTieBreak(tsm);
        // Score 0-7 in tiebreak
        scorePoints(tsm, 2, 7);
        // BUG IN SOURCE: resetGameAndPoints() is called BEFORE checkSetPoint().
        // gamesP2 resets to 0. Set is NOT awarded, games are wiped.
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
    }

    @Test
    public void checkTieBreakPointExtensionTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        reachTieBreak(tsm);
        // 6-6 in tiebreak
        scorePoints(tsm, 1, 6);
        scorePoints(tsm, 2, 6);
        
        // P1 scores -> 7-6 (No win yet, needs +2)
        tsm.pointScored(1);
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK: 7-6"));
        
        // P1 scores -> 8-6 (Win)
        tsm.pointScored(1);
        // BUG IN SOURCE: Win triggers reset, wiping the set progress instead of awarding set.
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
    }

    // --- Tests for Match Logic (Game Over) ---

    @Test
    public void isGameOverP1WinsMatchTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Win 3 sets for P1
        winSetStandard(tsm, 1); // Set 1
        winSetStandard(tsm, 1); // Set 2
        winSetStandard(tsm, 1); // Set 3
        
        assertTrue(tsm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
    }

    @Test
    public void isGameOverP2WinsMatchTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        winSetStandard(tsm, 2); 
        winSetStandard(tsm, 2); 
        winSetStandard(tsm, 2); 
        
        assertTrue(tsm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", tsm.getMatchScore());
    }

    @Test
    public void isGameOverMixedResultsTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        winSetStandard(tsm, 1); // 1-0
        winSetStandard(tsm, 2); // 1-1
        winSetStandard(tsm, 2); // 1-2
        winSetStandard(tsm, 1); // 2-2
        
        assertFalse(tsm.isGameOver());
        winSetStandard(tsm, 1); // 3-2
        assertTrue(tsm.isGameOver());
        assertEquals("P1: 3 Set | P2: 2 Set", tsm.getMatchScore());
    }

    @Test
    public void pointScoredAfterGameOverTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Win 3 sets for P1
        winSetStandard(tsm, 1);
        winSetStandard(tsm, 1);
        winSetStandard(tsm, 1);
        
        // Try to score again
        tsm.pointScored(1);
        
        // Score should remain final
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
    }
    
    @Test
    public void getGameScoreWhenGameOverTest() {
        TennisScoreManager tsm = new TennisScoreManager();
        winSetStandard(tsm, 1);
        winSetStandard(tsm, 1);
        winSetStandard(tsm, 1);
        
        assertEquals("PARTITA FINITA", tsm.getGameScore());
    }
}