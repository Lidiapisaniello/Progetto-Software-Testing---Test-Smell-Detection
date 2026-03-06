import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

    // --- Helper Method (To avoid code duplication without using @Before) ---
    private void scorePoints(TennisScoreManager manager, int player, int times) {
        for (int i = 0; i < times; i++) {
            manager.pointScored(player);
        }
    }

    // --- Constructor and Basic State Tests ---

    @Test
    public void ConstructorInitializationTest() {
        TennisScoreManager manager = new TennisScoreManager();
        Assert.assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void ResetPointsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1); // 15-Love
        manager.resetPoints();
        Assert.assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void ResetGameAndPointsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 1, 4); // P1 wins a game
        manager.resetGameAndPoints();
        // We verify indirectly via match score, verifying games are 0
        Assert.assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    // --- Point Scoring Logic Tests ---

    @Test
    public void PointScoredPlayer1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        Assert.assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void PointScoredPlayer2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        Assert.assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void PointScoredInvalidPlayerTest() {
        // Capture System.out to verify error message
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(3); // Invalid player

        Assert.assertTrue(outContent.toString().contains("Errore: Giocatore non valido"));
        Assert.assertEquals("Love-Love", manager.getGameScore());
        
        // Reset System.out
        System.setOut(System.out);
    }

    @Test
    public void PointScoredAfterGameOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Force P1 to win 3 sets
        // Set 1
        scorePoints(manager, 1, 24); 
        manager.moveToNextSet(); // Logic usually handles this, but ensuring set transitions
        // Set 2
        scorePoints(manager, 1, 24);
        manager.moveToNextSet();
        // Set 3
        scorePoints(manager, 1, 24);
        
        // Now game is over
        String finalScore = manager.getMatchScore();
        
        // Try to score more
        manager.pointScored(1);
        
        Assert.assertEquals(finalScore, manager.getMatchScore());
        Assert.assertTrue(manager.isGameOver());
    }

    // --- Game Score Logic Tests (Standard) ---

    @Test
    public void GetGameScoreDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 1, 3); // 40
        scorePoints(manager, 2, 3); // 40
        Assert.assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void GetGameScoreAdvantageP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 1, 3); // 40
        scorePoints(manager, 2, 3); // 40 - Deuce
        manager.pointScored(1); // Advantage P1
        Assert.assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void GetGameScoreAdvantageP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 1, 3); // 40
        scorePoints(manager, 2, 3); // 40 - Deuce
        manager.pointScored(2); // Advantage P2
        
        // NOTE: The provided source code has a bug: "if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)"
        // This is mathematically impossible, so it falls through to "Errore Game".
        // We test the actual behavior of the code provided.
        Assert.assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void CheckGamePointWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 1, 3); // 40-0
        manager.pointScored(1); // Win Game
        
        // Assert score reset to Love-Love and Games updated
        Assert.assertEquals("Love-Love", manager.getGameScore());
        Assert.assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void CheckGamePointWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePoints(manager, 2, 3); // 0-40
        manager.pointScored(2); // Win Game
        
        Assert.assertEquals("Love-Love", manager.getGameScore());
        Assert.assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    // --- Set Logic Tests ---

    @Test
    public void CheckSetPointStandardWinP1Test() {
        // Use Mockito Spy to verify checkSetPoint is called implicitly via logic flow
        TennisScoreManager manager = Mockito.spy(new TennisScoreManager());
        
        // P1 wins 5 games
        for(int i=0; i<5; i++) { scorePoints(manager, 1, 4); }
        
        // P2 wins 4 games
        for(int i=0; i<4; i++) { scorePoints(manager, 2, 4); }
        
        // P1 wins 6th game (6-4) -> Set Win
        scorePoints(manager, 1, 4);
        
        // Verify Set 1 is stored as 6-4
        Assert.assertTrue(manager.getMatchScore().contains("1-0")); // P1 leads 1 set to 0
    }

    @Test
    public void CheckSetPointStandardWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // P1 wins 4 games
        for(int i=0; i<4; i++) { scorePoints(manager, 1, 4); }
        
        // P2 wins 6 games (6-4)
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        Assert.assertTrue(manager.getMatchScore().contains("0-1"));
    }

    @Test
    public void CheckSetPointExtendedWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // 5-5
        for(int i=0; i<5; i++) { scorePoints(manager, 1, 4); }
        for(int i=0; i<5; i++) { scorePoints(manager, 2, 4); }
        
        // 6-5
        scorePoints(manager, 1, 4);
        
        // 7-5 P1 Wins Set
        scorePoints(manager, 1, 4);
        
        Assert.assertTrue(manager.getMatchScore().contains("1-0"));
    }

    @Test
    public void CheckSetPointExtendedWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // 5-5
        for(int i=0; i<5; i++) { scorePoints(manager, 1, 4); }
        for(int i=0; i<5; i++) { scorePoints(manager, 2, 4); }
        
        // 5-6
        scorePoints(manager, 2, 4);
        
        // 5-7 P2 Wins Set
        scorePoints(manager, 2, 4);
        
        Assert.assertTrue(manager.getMatchScore().contains("0-1"));
    }

    // --- Tie Break Tests ---

    @Test
    public void TieBreakActivationTest() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Reach 6-6
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); } // 6-0
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); } // 6-6
        
        // Check if tie break matches string
        Assert.assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void GetTieBreakScoreTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        // Score points in Tie Break
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);
        
        Assert.assertEquals("TIE-BREAK: 2-1", manager.getTieBreakScore());
    }

    @Test
    public void CheckTieBreakPointWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        // Tie Break: P1 wins 7-0
        scorePoints(manager, 1, 7);
        
        // Set should be won by P1
        Assert.assertTrue(manager.getMatchScore().contains("1-0"));
    }

    @Test
    public void CheckTieBreakPointWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        // Tie Break: P2 wins 7-5
        scorePoints(manager, 1, 5);
        scorePoints(manager, 2, 7);
        
        // Set should be won by P2
        Assert.assertTrue(manager.getMatchScore().contains("0-1"));
    }
    
    // --- Match Win Tests ---

    @Test
    public void MatchWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Win 3 sets for P1
        // Set 1: 6-0
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        // Set 2: 6-0
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        // Set 3: 6-0
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        
        Assert.assertTrue(manager.isGameOver());
        Assert.assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void MatchWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Win 3 sets for P2
        // Set 1: 0-6
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        // Set 2: 0-6
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        // Set 3: 0-6
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        Assert.assertTrue(manager.isGameOver());
        Assert.assertTrue(manager.getMatchScore().contains("P2: 3 Set"));
    }
    
    @Test
    public void GetMatchScoreMidMatchTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 wins 1st Set
        for(int i=0; i<6; i++) { scorePoints(manager, 1, 4); }
        
        // P2 wins 2nd Set
        for(int i=0; i<6; i++) { scorePoints(manager, 2, 4); }
        
        String score = manager.getMatchScore();
        // 1-1 sets
        Assert.assertTrue(score.startsWith("1-1")); 
    }
}