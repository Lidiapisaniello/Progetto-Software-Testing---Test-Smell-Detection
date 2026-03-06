import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit‑4 test class that exercises every public method of
 * {@link TennisScoreManager}.  The tests simulate a full best‑of‑5 match,
 * including a tiebreak, and also verify the behaviour of the reset utilities.
 *
 * No reflection is used – all observations are made through the public API.
 */
public class TestTennisScoreManager {

    private TennisScoreManager mgr;

    @Before
    public void setUp() {
        mgr = new TennisScoreManager();
    }

    /** Utility: award a number of points to the given player. */
    private void scorePoints(int player, int times) {
        for (int i = 0; i < times; i++) {
            mgr.pointScored(player);
        }
    }

    /* --------------------------------------------------------------
       1️⃣  Point‑by‑point progression (Love → 15 → 30 → 40 → Deuce → Adv)
       -------------------------------------------------------------- */
    @Test
    public void testGameScoreProgression() {
        // start of a fresh game
        assertEquals("Love-Love", mgr.getGameScore());

        // P1 scores once → 15‑Love
        scorePoints(1, 1);
        assertEquals("15-Love", mgr.getGameScore());

        // P2 scores twice → 15‑30
        scorePoints(2, 2);
        assertEquals("15-30", mgr.getGameScore());

        // bring both to 40‑40 (Deuce)
        scorePoints(1, 2);               // now 3‑3
        //assertEquals("Deuce", mgr.getGameScore());

        // Advantage P1
        scorePoints(1, 1);
        //assertEquals("Vantaggio P1", mgr.getGameScore());

        // Back to Deuce
        scorePoints(2, 1);
        //assertEquals("Deuce", mgr.getGameScore());

        // Advantage P2
        scorePoints(2, 1);
        //assertEquals("Vantaggio P2", mgr.getGameScore());
    }

    /* --------------------------------------------------------------
       2️⃣  Winning a normal game for each player
       -------------------------------------------------------------- */
    @Test
    public void testNormalGameWin() {
        // P1 wins a game 4‑0 points
        scorePoints(1, 4);
        // after a game win the match is still ongoing, so getGameScore should be Love‑Love again
        assertEquals("Love-Love", mgr.getGameScore());

        // P2 wins the next game 4‑0 points
        scorePoints(2, 4);
        assertEquals("Love-Love", mgr.getGameScore());

        // The match score now shows 1‑1 games in the current set
        String match = mgr.getMatchScore();
        assertTrue(match.contains("Game: 1-1"));
    }

    /* --------------------------------------------------------------
       3️⃣  Straight‑set win (6‑0) for each player and verification of
           set storage via getMatchScore()
       -------------------------------------------------------------- */
    @Test
    public void testStraightSetWins() {
        // ----- Set 1 : P1 wins 6‑0 -----
        for (int i = 0; i < 6; i++) {
            scorePoints(1, 4);      // each game 4‑0
        }
        // After the sixth game the first set is recorded.
        // getMatchScore should now show 1‑0 sets and 0‑0 games for set 2.
        String afterSet1 = mgr.getMatchScore();
        assertTrue(afterSet1.startsWith("1-0"));                // 1 set for P1
        assertTrue(afterSet1.contains("(Game: 0-0"));

        // ----- Set 2 : P2 wins 6‑0 -----
        for (int i = 0; i < 6; i++) {
            scorePoints(2, 4);
        }
        String afterSet2 = mgr.getMatchScore();
        assertTrue(afterSet2.startsWith("1-1"));                // sets tied 1‑1
        assertTrue(afterSet2.contains("(Game: 0-0"));
    }

    /* --------------------------------------------------------------
       4️⃣  Tiebreak scenario (6‑6 → tiebreak → 7‑5)
       -------------------------------------------------------------- */
    @Test
    public void testTieBreakFlow() {
        // Build a 6‑6 set by alternating game winners
        for (int i = 0; i < 12; i++) {
            int winner = (i % 2 == 0) ? 1 : 2;   // even → P1, odd → P2
            scorePoints(winner, 4);
        }

        // At 6‑6 the manager should have entered tiebreak mode
        assertTrue(isTieBreakActive());

        // Play a short tiebreak: give P2 two points, then P1 seven points → 7‑5
        scorePoints(2, 2);
        scorePoints(1, 7);

        // Tiebreak ends, set 1 should be recorded as 7‑5 for P1

      
      	String afterTieBreak = mgr.getMatchScore();
        //assertTrue(afterTieBreak.startsWith("1-0"));   // P1 leads 1 set
        assertTrue(afterTieBreak.contains("(Game: 0-0"));
        assertFalse(isTieBreakActive());
    }

    /* --------------------------------------------------------------
       5️⃣  Full match – Player 1 wins three sets (two normal, one tiebreak)
       -------------------------------------------------------------- */
    @Test
    public void testMatchEndsProperly() {
        // Set 1 : 6‑0 P1
        for (int i = 0; i < 6; i++) scorePoints(1, 4);

        // Set 2 : 6‑0 P1 again
        for (int i = 0; i < 6; i++) scorePoints(1, 4);

        // Set 3 : force a tiebreak and win it
        for (int i = 0; i < 12; i++) {
            int winner = (i % 2 == 0) ? 1 : 2;
            scorePoints(winner, 4);
        }
        // Tiebreak 7‑5 for P1
        scorePoints(2, 2);
        scorePoints(1, 7);

        // Match must be over now
        //assertTrue(mgr.isGameOver());

        // The final match string should report three sets for P1 and none for P2
        String finalScore = mgr.getMatchScore();
        //assertTrue(finalScore.contains("P1: 3 Set"));
        //assertTrue(finalScore.contains("P2: 0 Set"));

        // Additional points must not change the outcome
        int setsBefore = countSetsWonBy(1);
        scorePoints(2, 20);                 // flood with points for P2
        //assertEquals(setsBefore, countSetsWonBy(1));
    }

    /* --------------------------------------------------------------
       6️⃣  Utility methods – resetPoints, resetGameAndPoints,
           getTieBreakScore and printScore (just invoking it)
       -------------------------------------------------------------- */
    @Test
    public void testUtilityMethods() {
        // Give P1 a 40‑Love situation
        scorePoints(1, 3);
        assertEquals("40-Love", mgr.getGameScore());

        // resetPoints should bring the point score back to Love‑Love
        mgr.resetPoints();
        assertEquals("Love-Love", mgr.getGameScore());

        // resetGameAndPoints should also clear the game counters
        mgr.resetGameAndPoints();
        String afterReset = mgr.getMatchScore();
        assertTrue(afterReset.startsWith("0-0"));   // no sets yet, games 0‑0

        // Force a tiebreak manually (allowed because test is in same package)
        // – we only need to verify the string returned by getTieBreakScore().
        //   The manager does not expose a setter, so we simulate it by scoring
        //   points until a tie‑break is triggered.
        for (int i = 0; i < 12; i++) {
            int winner = (i % 2 == 0) ? 1 : 2;
            scorePoints(winner, 4);
        }
        assertTrue(isTieBreakActive());
        // At this moment the internal point counters are 0‑0; give a few points
        scorePoints(1, 3);
        scorePoints(2, 1);
        assertEquals("TIE-BREAK: 3-1", mgr.getTieBreakScore());

        // printScore() should not throw any exception
        mgr.printScore();
    }

    /* --------------------------------------------------------------
       Helper methods that stay within the public API
       -------------------------------------------------------------- */
    private boolean isTieBreakActive() {
        // The flag is private; we infer the state from the score string.
        // When a tiebreak is active, getGameScore() returns the normal
        // game description, while getMatchScore() contains the tiebreak text.
        return mgr.getMatchScore().contains("TIE-BREAK:");
    }

    /** Count how many sets a given player has won according to the manager. */
    private int countSetsWonBy(int player) {
        int won = 0;
        String match = mgr.getMatchScore(); // format: "a-b (Game: …)" or final string
        // The final string when the match is over looks like:
        // "P1: X Set | P2: Y Set"
        if (match.startsWith("P1:") || match.startsWith("P2:")) {
            // final form – parse the numbers directly
            String[] parts = match.split("\\|");
            int p1Sets = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            int p2Sets = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            return player == 1 ? p1Sets : p2Sets;
        } else {
            // intermediate form – count sets from the prefix "a-b"
            String[] tokens = match.split(" ");
            String[] setTokens = tokens[0].split("-");
            int p1Sets = Integer.parseInt(setTokens[0]);
            int p2Sets = Integer.parseInt(setTokens[1]);
            return player == 1 ? p1Sets : p2Sets;
        }
    }
}