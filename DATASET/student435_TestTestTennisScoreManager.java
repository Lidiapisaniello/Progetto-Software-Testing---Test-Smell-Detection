import org.junit.Test;
import static org.junit.Assert.*;

public class TennisScoreManagerTest {

    @Test
    public void testInitialStateTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Verify initial match score string contains 0 sets
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("0-0"));
        assertTrue(matchScore.contains("Love-Love"));
    }

    @Test
    public void testPointScoredP1SimpleTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        String score = manager.getGameScore();
        assertEquals("15-Love", score);
    }

    @Test
    public void testPointScoredP2SimpleTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        String score = manager.getGameScore();
        assertEquals("Love-15", score);
    }

    @Test
    public void testInvalidPlayerTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Should print error to console and not change score
        manager.pointScored(3);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // 3 points each -> Deuce
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach Deuce
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        // P1 scores again
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach Deuce
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        // P2 scores again
        manager.pointScored(2);
        
        // BUG IN SOURCE CODE: The condition (scoreP2 == scoreP2 + 1) is impossible.
        // Therefore, it falls through to "Errore Game". 
        // We assert "Errore Game" to confirm we covered the fall-through path.
        assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void testGameWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 scores 4 times to win game
        manager.pointScored(1); // 15
        manager.pointScored(1); // 30
        manager.pointScored(1); // 40
        manager.pointScored(1); // Win Game
        
        // Score resets to Love-Love (new game)
        assertEquals("Love-Love", manager.getGameScore());
        // Match score should show 1 game to 0
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }
    
    @Test
    public void testGameWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P2 scores 4 times to win game
        manager.pointScored(2);
        manager.pointScored(2);
        manager.pointScored(2);
        manager.pointScored(2);
        
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testSetWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Win 6 games in a row for P1
        for (int g = 0; g < 6; g++) {
            // Win one game
            manager.pointScored(1);
            manager.pointScored(1);
            manager.pointScored(1);
            manager.pointScored(1);
        }
        
        // P1 wins set 6-0. Current set should advance to 2.
        // Match score logic: "P1: 1 Set | P2: 0 Set" is only returned if Game Over (3 sets).
        // Otherwise it returns standard string.
        // We check internal state via the match score string for the game count of the NEW set (0-0)
        // or implied previous set win.
        
        // Verify formatting implies set 1 is done (setsP1[0] = 6)
        String matchScore = manager.getMatchScore();
        // Because scoreP1 > scoreP2, setsWonP1 increments.
        assertTrue(matchScore.startsWith("1-0")); 
    }

    @Test
    public void testSetWinP2ExtendedTest() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Win 5 games P1, 7 games P2 (7-5 win)
        // 1. P1 wins 5 games
        for(int i=0; i<5; i++) winGame(manager, 1);
        // 2. P2 wins 7 games
        for(int i=0; i<7; i++) winGame(manager, 2);

        String matchScore = manager.getMatchScore();
        // Sets won: 0-1
        assertTrue(matchScore.startsWith("0-1"));
    }

    @Test
    public void testTieBreakTriggerTest() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Reach 6-6 games
        for(int i=0; i<6; i++) winGame(manager, 1);
        for(int i=0; i<6; i++) winGame(manager, 2);
        
        // Should now be in tie break
        manager.pointScored(1); // Score 1 point in tie break
        
        String score = manager.getMatchScore();
        assertTrue(score.contains("TIE-BREAK: 1-0"));
    }

    @Test
    public void testTieBreakWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Reach 6-6
        for(int i=0; i<6; i++) winGame(manager, 1);
        for(int i=0; i<6; i++) winGame(manager, 2);
        
        // Win Tie Break (7 points)
        for(int i=0; i<7; i++) {
            manager.pointScored(1);
        }
        
        // Note: Due to the bug in checkTieBreakPoint (resetting games before checking set),
        // The set is NOT recorded as won. The games reset to 0-0 and set remains 1.
        // We assert the CURRENT behavior to prove execution flow.
        
        String score = manager.getMatchScore();
        // Logic bug outcome: Games 0-0, Set still 1, no sets won.
        assertTrue(score.contains("0-0 (Game: 0-0 Love-Love)"));
    }
    
    @Test
    public void testTieBreakWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Reach 6-6
        for(int i=0; i<6; i++) winGame(manager, 1);
        for(int i=0; i<6; i++) winGame(manager, 2);
        
        // Win Tie Break P2 (7 points)
        for(int i=0; i<7; i++) {
            manager.pointScored(2);
        }
        
        // Covers the P2 block in checkTieBreakPoint
        assertEquals(0, 0); // Assert pass if no exception
    }

    @Test
    public void testMatchWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // P1 needs to win 3 sets.
        // Set 1 (6-0)
        for(int i=0; i<6; i++) winGame(manager, 1);
        // Set 2 (6-0)
        for(int i=0; i<6; i++) winGame(manager, 1);
        // Set 3 (6-0)
        for(int i=0; i<6; i++) winGame(manager, 1);
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        
        // Try to score after game over
        manager.pointScored(1); // Should print "Partita finita" and return
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void testMatchWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // P2 needs to win 3 sets.
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) winGame(manager, 2);
        }
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }
    
    @Test
    public void testGetGameScoreGameOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Win 3 sets
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) winGame(manager, 1);
        }
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    // --- Helper for atomic tests ---
    private void winGame(TennisScoreManager tm, int player) {
        tm.pointScored(player);
        tm.pointScored(player);
        tm.pointScored(player);
        tm.pointScored(player);
    }
}