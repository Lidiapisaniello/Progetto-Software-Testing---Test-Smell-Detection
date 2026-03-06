import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    // Helper to win points
    private void scorePoints(TennisScoreManager tsm, int player, int points) {
        for (int i = 0; i < points; i++) {
            tsm.pointScored(player);
        }
    }

    // Helper to win a standard game (assuming start from 0-0)
    private void winStandardGame(TennisScoreManager tsm, int player) {
        scorePoints(tsm, player, 4);
    }

    @Test
    public void testInitialState() {
        TennisScoreManager tsm = new TennisScoreManager();
        assertEquals("Love-Love", tsm.getGameScore());
        // Verify exact initial match score string format based on getMatchScore implementation
        // Format: setsWonP1 + "-" + setsWonP2 + " (Game: " + gamesP1 + "-" + gamesP2 + " " + score + ")"
        assertEquals("0-0 (Game: 0-0 Love-Love)", tsm.getMatchScore());
        assertFalse(tsm.isGameOver());
    }

    @Test
    public void testBasicScoringPlayer1() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(1); // 15-Love
        assertEquals("15-Love", tsm.getGameScore());
        tsm.pointScored(1); // 30-Love
        assertEquals("30-Love", tsm.getGameScore());
        tsm.pointScored(1); // 40-Love
        assertEquals("40-Love", tsm.getGameScore());
    }

    @Test
    public void testBasicScoringPlayer2() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(2); // Love-15
        assertEquals("Love-15", tsm.getGameScore());
        tsm.pointScored(2); // Love-30
        assertEquals("Love-30", tsm.getGameScore());
        tsm.pointScored(2); // Love-40
        assertEquals("Love-40", tsm.getGameScore());
    }

    @Test
    public void testMixedScoring() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(1); // 15-0
        tsm.pointScored(2); // 15-15
        assertEquals("15-15", tsm.getGameScore());
        tsm.pointScored(1); // 30-15
        tsm.pointScored(2); // 30-30
        assertEquals("30-30", tsm.getGameScore());
    }

    @Test
    public void testGameWinPlayer1() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3); // 40-0
        tsm.pointScored(1); // Game P1
        // Should reset to Love-Love
        assertEquals("Love-Love", tsm.getGameScore());
        // Match score should show 1 game for P1
        assertTrue(tsm.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testGameWinPlayer2() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 2, 3); // 0-40
        tsm.pointScored(2); // Game P2
        assertEquals("Love-Love", tsm.getGameScore());
        assertTrue(tsm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testDeuceLogic() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3); // 40-0
        scorePoints(tsm, 2, 3); // 40-40
        assertEquals("Deuce", tsm.getGameScore());
    }

    @Test
    public void testAdvantagePlayer1() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(1); // Adv P1
        assertEquals("Vantaggio P1", tsm.getGameScore());
        
        // Win from Advantage
        tsm.pointScored(1);
        assertTrue(tsm.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testAdvantagePlayer1BackToDeuce() {
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(1); // Adv P1
        assertEquals("Vantaggio P1", tsm.getGameScore());
        
        // P2 scores, back to Deuce
        tsm.pointScored(2);
        assertEquals("Deuce", tsm.getGameScore());
    }

    @Test
    public void testAdvantagePlayer2_BugReproduction() {
        // The provided class has a bug: if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)
        // This condition is always false (x == x + 1 is impossible).
        // Therefore "Vantaggio P2" is unreachable.
        // It falls through to "Errore Game".
        
        TennisScoreManager tsm = new TennisScoreManager();
        scorePoints(tsm, 1, 3);
        scorePoints(tsm, 2, 3); // Deuce
        tsm.pointScored(2); // Technically Advantage P2 logic attempt
        
        // Assert the actual behavior existing in the code
        assertEquals("Errore Game", tsm.getGameScore());
        
        // Verify that scoring again still awards the game correctly despite the display bug
        tsm.pointScored(2); 
        assertTrue(tsm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testInvalidPlayerInput() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Ensure these do not change the state
        tsm.pointScored(3);
        tsm.pointScored(0);
        tsm.pointScored(-1);
        
        // Score should remain unchanged
        assertEquals("Love-Love", tsm.getGameScore());
    }

    @Test
    public void testSetWinStandard() {
        TennisScoreManager tsm = new TennisScoreManager();
        // P1 wins 6 games in a row
        for (int i = 0; i < 6; i++) {
            winStandardGame(tsm, 1);
        }
        
        // Current set should now be 2, P1 has 1 set won
        // MatchScore format: "1-0 (Game: 0-0 Love-Love)"
        String matchScore = tsm.getMatchScore();
        assertTrue("Expected match score to start with 1-0", matchScore.startsWith("1-0")); 
        assertTrue("Expected games to be reset to 0-0", matchScore.contains("Game: 0-0"));
    }
    
    @Test
    public void testSetWinStandardPlayer2() {
        TennisScoreManager tsm = new TennisScoreManager();
        for (int i = 0; i < 6; i++) {
            winStandardGame(tsm, 2);
        }
        assertTrue(tsm.getMatchScore().startsWith("0-1")); 
    }

    @Test
    public void testSetWin7_5() {
        TennisScoreManager tsm = new TennisScoreManager();
        // 5-5 Games
        for (int i = 0; i < 5; i++) {
            winStandardGame(tsm, 1);
            winStandardGame(tsm, 2);
        }
        
        // P1 wins 6th game -> 6-5 (No Tie Break yet, no Set Win yet)
        winStandardGame(tsm, 1);
        assertTrue(tsm.getMatchScore().contains("Game: 6-5"));
        
        // P1 wins 7th game -> 7-5 (Set Win)
        winStandardGame(tsm, 1);
        String matchScore = tsm.getMatchScore();
        assertTrue("Expected match score to start with 1-0", matchScore.startsWith("1-0"));
        assertTrue("Expected games to be reset", matchScore.contains("Game: 0-0"));
    }

    @Test
    public void testSetWin5_7() {
        TennisScoreManager tsm = new TennisScoreManager();
        // 5-5 Games
        for (int i = 0; i < 5; i++) {
            winStandardGame(tsm, 1);
            winStandardGame(tsm, 2);
        }
        
        // P2 wins 6th game -> 5-6
        winStandardGame(tsm, 2);
        assertTrue(tsm.getMatchScore().contains("Game: 5-6"));
        
        // P2 wins 7th game -> 5-7 (Set Win)
        winStandardGame(tsm, 2);
        assertTrue(tsm.getMatchScore().startsWith("0-1")); 
    }

    @Test
    public void testTieBreakTrigger() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Reach 5-5
        for (int i = 0; i < 5; i++) {
            winStandardGame(tsm, 1);
            winStandardGame(tsm, 2);
        }
        // Reach 6-6
        winStandardGame(tsm, 1); // 6-5
        winStandardGame(tsm, 2); // 6-6 -> Trigger Tie Break
        
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void testTieBreakScoring() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Fast forward to Tie Break (6-6)
        for (int i = 0; i < 5; i++) { winStandardGame(tsm, 1); winStandardGame(tsm, 2); }
        winStandardGame(tsm, 1);
        winStandardGame(tsm, 2);
        
        // Check Tie Break scoring display
        tsm.pointScored(1);
        assertTrue(tsm.getTieBreakScore().contains("1-0"));
        tsm.pointScored(2);
        tsm.pointScored(2);
        assertTrue(tsm.getTieBreakScore().contains("1-2"));
    }

    @Test
    public void testTieBreakWin_BugReproduction() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Fast forward to Tie Break (6-6)
        for (int i = 0; i < 5; i++) { winStandardGame(tsm, 1); winStandardGame(tsm, 2); }
        winStandardGame(tsm, 1);
        winStandardGame(tsm, 2);
        
        // P1 Wins Tie Break (7-0)
        // BUG IN SOURCE: checkTieBreakPoint calls resetGameAndPoints() BEFORE checkSetPoint().
        // This wipes the game count (gamesP1 becomes 0), so checkSetPoint sees 0 games and does NOT award the set.
        for (int i = 0; i < 7; i++) {
            tsm.pointScored(1);
        }
        
        String matchScore = tsm.getMatchScore();
        // Because of the bug, the set is not awarded, and games reset to 0.
        // Format: "0-0 (Game: 0-0 Love-Love)"
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }
    
    @Test
    public void testTieBreakWinPlayer2_BugReproduction() {
        TennisScoreManager tsm = new TennisScoreManager();
        for (int i = 0; i < 5; i++) { winStandardGame(tsm, 1); winStandardGame(tsm, 2); }
        winStandardGame(tsm, 1);
        winStandardGame(tsm, 2);
        
        // P2 Wins Tie Break
        for (int i = 0; i < 7; i++) {
            tsm.pointScored(2);
        }
        
        String matchScore = tsm.getMatchScore();
        // Bug ensures set is not awarded
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }

    @Test
    public void testMatchOver() {
        TennisScoreManager tsm = new TennisScoreManager();
        
        // P1 needs to win 3 sets.
        // Since Tie-Breaks are broken in the source code (don't award sets), we must win by 6-0 or 7-5.
        
        // Set 1
        for(int i=0; i<6; i++) winStandardGame(tsm, 1);
        // Set 2
        for(int i=0; i<6; i++) winStandardGame(tsm, 1);
        // Set 3
        for(int i=0; i<6; i++) winStandardGame(tsm, 1);
        
        assertTrue(tsm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
        
        // Try scoring after game over
        tsm.pointScored(1);
        // Score should not change/Result remains final
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
        assertEquals("PARTITA FINITA", tsm.getGameScore());
    }
    
    @Test
    public void testMatchOverPlayer2() {
        TennisScoreManager tsm = new TennisScoreManager();
        
        // Set 1, 2, 3 for P2
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) winStandardGame(tsm, 2);
        }
        
        assertTrue(tsm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", tsm.getMatchScore());
    }

    @Test
    public void testResetMethods() {
        TennisScoreManager tsm = new TennisScoreManager();
        tsm.pointScored(1);
        tsm.resetPoints();
        assertEquals("Love-Love", tsm.getGameScore());
        
        winStandardGame(tsm, 1); // 1-0 games
        tsm.pointScored(1); // 15-Love
        tsm.resetGameAndPoints();
        assertTrue(tsm.getMatchScore().contains("Game: 0-0"));
        assertEquals("Love-Love", tsm.getGameScore());
    }
    
    @Test
    public void testPrintScore() {
        // Just ensuring no exceptions occur during print operations in various states
        TennisScoreManager tsm = new TennisScoreManager();
        
        tsm.printScore(); // 0-0
        tsm.pointScored(1);
        tsm.printScore(); // 15-0
        
        // Verify state didn't crash
        assertEquals("15-Love", tsm.getGameScore());
    }
    
    @Test
    public void testGetMatchScoreIntermediate() {
        TennisScoreManager tsm = new TennisScoreManager();
        // Win Set 1
        for(int i=0; i<6; i++) winStandardGame(tsm, 1);
        // Win Set 2 for P2
        for(int i=0; i<6; i++) winStandardGame(tsm, 2);
        
        // Check specific formatting of getMatchScore
        String score = tsm.getMatchScore();
        // 1-1 sets
        assertTrue(score.startsWith("1-1"));
        assertTrue(score.contains("Game: 0-0"));
    }
}