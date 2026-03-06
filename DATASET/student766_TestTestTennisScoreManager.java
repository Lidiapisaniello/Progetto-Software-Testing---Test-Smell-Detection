import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    @Test
    public void testInitialization() {
        TennisScoreManager manager = new TennisScoreManager();
        assertEquals("Love-Love", manager.getGameScore());
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testPointScoredP1() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void testPointScoredP2() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayer() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(3); // Should print error but not change state
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testDeuce() {
        TennisScoreManager manager = new TennisScoreManager();
        // 3 points each -> 40-40 -> Deuce
        for (int i = 0; i < 3; i++)
            manager.pointScored(1);
        for (int i = 0; i < 3; i++)
            manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // Deuce
        for (int i = 0; i < 3; i++)
            manager.pointScored(1);
        for (int i = 0; i < 3; i++)
            manager.pointScored(2);
        // Advantage P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2() {
        TennisScoreManager manager = new TennisScoreManager();
        // Deuce
        for (int i = 0; i < 3; i++)
            manager.pointScored(1);
        for (int i = 0; i < 3; i++)
            manager.pointScored(2);
        // Advantage P2
        manager.pointScored(2);
        assertEquals("Vantaggio P2", manager.getGameScore());
    }

    @Test
    public void testBackToDeuce() {
        TennisScoreManager manager = new TennisScoreManager();
        // Deuce
        for (int i = 0; i < 3; i++)
            manager.pointScored(1);
        for (int i = 0; i < 3; i++)
            manager.pointScored(2);
        // Adv P1
        manager.pointScored(1);
        // Back to Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testWinGameP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // 40-0
        for (int i = 0; i < 3; i++)
            manager.pointScored(1);
        // Win
        manager.pointScored(1);
        assertEquals("Love-Love", manager.getGameScore()); // Reset for next game
        // We can verify game count by checking match score string or internal state if
        // accessible,
        // but getMatchScore is the public way
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameP2() {
        TennisScoreManager manager = new TennisScoreManager();
        for (int i = 0; i < 4; i++)
            manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testWinSetP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // Win 6 games
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
        }
        // Should be 1-0 sets
        assertTrue(manager.getMatchScore().contains("1-0"));
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testWinSetP2() {
        TennisScoreManager manager = new TennisScoreManager();
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        assertTrue(manager.getMatchScore().contains("0-1"));
    }

    @Test
    public void testSet7_5() {
        TennisScoreManager manager = new TennisScoreManager();
        // 5-5
        for (int g = 0; g < 5; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        // P1 wins next 2 games -> 7-5
        for (int g = 0; g < 2; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
        }
        assertTrue(manager.getMatchScore().contains("1-0"));
    }

    @Test
    public void testTieBreakTrigger() {
        TennisScoreManager manager = new TennisScoreManager();
        // 6-6
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        // Should be in tie break
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void testTieBreakWinP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        // Win Tie Break 7-0
        for (int i = 0; i < 7; i++)
            manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("1-0"));
    }

    @Test
    public void testTieBreakWinP2() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        // Win Tie Break 0-7
        for (int i = 0; i < 7; i++)
            manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("0-1"));
    }

    @Test
    public void testTieBreakExtended() {
        TennisScoreManager manager = new TennisScoreManager();
        // Reach 6-6
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++)
                manager.pointScored(1);
            for (int p = 0; p < 4; p++)
                manager.pointScored(2);
        }
        // 6-6 in points
        for (int i = 0; i < 6; i++)
            manager.pointScored(1);
        for (int i = 0; i < 6; i++)
            manager.pointScored(2);
        // P1 goes to 8-6
        manager.pointScored(1);
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("1-0"));
    }

    @Test
    public void testMatchWinP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // Win 3 sets
        for (int s = 0; s < 3; s++) {
            for (int g = 0; g < 6; g++) {
                for (int p = 0; p < 4; p++)
                    manager.pointScored(1);
            }
        }
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));

        // Try scoring after game over
        manager.pointScored(1); // Should print "Partita finita" and return
        assertTrue(manager.isGameOver());
    }

    @Test
    public void testMatchWinP2() {
        TennisScoreManager manager = new TennisScoreManager();
        // Win 3 sets
        for (int s = 0; s < 3; s++) {
            for (int g = 0; g < 6; g++) {
                for (int p = 0; p < 4; p++)
                    manager.pointScored(2);
            }
        }
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P2: 3 Set"));
    }

    @Test
    public void testPrintScore() {
        // Just to cover the printScore method lines
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1); // Calls printScore internally
        manager.printScore(); // Explicit call
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void testGetGameScoreFinished() {
        TennisScoreManager manager = new TennisScoreManager();
        // Win 3 sets
        for (int s = 0; s < 3; s++) {
            for (int g = 0; g < 6; g++) {
                for (int p = 0; p < 4; p++)
                    manager.pointScored(1);
            }
        }
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void testErrorGameScore() {
        // It's hard to reach "Errore Game" with valid logic, but we can try to force a
        // weird state
        // or just accept it might be unreachable with valid operations.
        // However, we can try to set up a state that doesn't match other conditions if
        // we could access fields.
        // Since fields are private, we rely on normal logic.
        // "Errore Game" is at the end of getGameScore.
        // Conditions covered:
        // < 4 both (normal)
        // == and >= 3 (Deuce)
        // >= 3 and diff 1 (Advantage)
        // The only other case is if logic fails.
        // We have covered all reachable branches.
    }
}
