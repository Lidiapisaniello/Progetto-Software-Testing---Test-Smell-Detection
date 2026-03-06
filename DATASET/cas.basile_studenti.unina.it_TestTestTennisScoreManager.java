/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Castrese
Cognome: Basile
Username: cas.basile@studenti.unina.it
UserID: 466
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TennisScoreManagerTest {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    // --------- Helper methods ---------

    /**
     * Make a player win a standard game (4 straight points: 0 -> 4).
     */
    private void winStandardGame(TennisScoreManager m, int player) {
        for (int i = 0; i < 4; i++) {
            m.pointScored(player);
        }
    }

    /**
     * Make a player win a standard set 6-0 (no tie-break).
     */
    private void winStandardSet(TennisScoreManager m, int player) {
        for (int g = 0; g < 6; g++) {
            winStandardGame(m, player);
        }
    }

    /**
     * Make a player win an entire match 3 sets to 0 (each 6-0).
     */
    private void winStandardMatch(TennisScoreManager m, int player) {
        for (int s = 0; s < 3; s++) {
            winStandardSet(m, player);
        }
    }

    /**
     * Bring the current set to 6-6 games (activating tie-break).
     * Games are alternated: P1, P2, P1, P2, ...
     */
    private void reachTieBreak(TennisScoreManager m) {
        for (int i = 0; i < 12; i++) {
            int player = (i % 2 == 0) ? 1 : 2;
            winStandardGame(m, player);
        }
    }

    // --------- Basic state & reset tests ---------

    @Test
    public void testInitialState() {
        // Game score at start
        assertEquals("Love-Love", manager.getGameScore());
        // Match score at start
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
        // Match is not over
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testResetPointsAndGames() {
        // Score some points
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(2);
        assertEquals("30-15", manager.getGameScore());

        // Reset only points
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());

        // Win one game for P1
        winStandardGame(manager, 1);
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());

        // Reset games and points
        manager.resetGameAndPoints();
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
        assertEquals("Love-Love", manager.getGameScore());
    }

    // --------- Point scoring & game logic ---------

    @Test
    public void testBasicScoringProgression() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());

        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());

        manager.pointScored(1);
        manager.pointScored(1);
        // P1: 3 points -> "40", P2: 1 point -> "15"
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayerDoesNotChangeScore() {
        manager.pointScored(3); // invalid
        // Still at start
        assertEquals("Love-Love", manager.getGameScore());

        // Now score with a valid player and ensure it works
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void testGameWinByP1() {
        winStandardGame(manager, 1);
        // After game, points reset and gamesP1 incremented
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testGameWinByP2() {
        winStandardGame(manager, 2);
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 0-1 Love-Love)", manager.getMatchScore());
    }

    // --------- Deuce / Advantage / Error Game tests ---------

    @Test
    public void testDeuceAndAdvantageP1AndBackToDeuce() {
        // Bring to 3-3 (Deuce)
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        assertEquals("Deuce", manager.getGameScore());

        // P1 gets advantage: 4-3
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // P2 scores, back to 4-4 -> Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2ResultsInErroreGameBug() {
        // Bring to Deuce (3-3)
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        assertEquals("Deuce", manager.getGameScore());

        // P2 scores once: 3-4, expected logical "Vantaggio P2"
        // but due to implementation bug, we get "Errore Game"
        manager.pointScored(2);
        assertEquals("Errore Game", manager.getGameScore());
    }

    // --------- Tie-break tests ---------

    @Test
    public void testTieBreakActivationAtSixAll() {
        reachTieBreak(manager);

        // Now we should be in tie-break: games 6-6, points 0-0, flag true
        // Match is still 0-0 in sets
        String matchScore = manager.getMatchScore();
        assertEquals("0-0 (Game: 6-6 TIE-BREAK: 0-0)", matchScore);
    }

    @Test
    public void testTieBreakScoringAndWinByP1() {
        reachTieBreak(manager);
        // P1 wins tie-break 7-0
        for (int i = 0; i < 7; i++) {
            manager.pointScored(1);
        }

        // Due to implementation, tie-break increments games, then resetGameAndPoints(),
        // and checkSetPoint at 0-0 (no set recorded). We just assert that:
        // - we are no longer in tie-break (no TIE-BREAK in match score)
        // - game and points look reset.
        String matchScore = manager.getMatchScore();
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }

    @Test
    public void testTieBreakScoringAndWinByP2() {
        reachTieBreak(manager);
        // P2 wins tie-break 7-2 (for example)
        for (int i = 0; i < 7; i++) {
            manager.pointScored(2);
        }

        String matchScore = manager.getMatchScore();
        // Same behavior: back to normal game, no sets recorded.
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }

    @Test
    public void testGetTieBreakScoreSimple() {
        // Not even necessary to enter true tie-break:
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(2);
        assertEquals("TIE-BREAK: 2-1", manager.getTieBreakScore());
    }

    // --------- Set and match logic ---------

    @Test
    public void testSetWinByP1SixZero() {
        winStandardSet(manager, 1);

        // P1 should lead by 1 set, games reset
        String matchScore = manager.getMatchScore();
        assertEquals("1-0 (Game: 0-0 Love-Love)", matchScore);
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testSetWinByP2SixZero() {
        winStandardSet(manager, 2);

        String matchScore = manager.getMatchScore();
        assertEquals("0-1 (Game: 0-0 Love-Love)", matchScore);
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testMatchWinByP1ThreeSetsToZero() {
        winStandardMatch(manager, 1);

        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void testMatchWinByP2ThreeSetsToZero() {
        winStandardMatch(manager, 2);

        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void testGetGameScoreWhenMatchAlreadyOver() {
        winStandardMatch(manager, 1);

        // Game score should report match finished
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void testMoveToNextSetDoesNotChangeFinishedMatch() {
        winStandardMatch(manager, 1);

        String before = manager.getMatchScore();
        // Explicit call; internal code guarded by isGameOver()
        manager.moveToNextSet();
        String after = manager.getMatchScore();

        assertEquals(before, after);
    }

    @Test
    public void testPointScoredAfterMatchOverDoesNothing() {
        winStandardMatch(manager, 1);
        String before = manager.getMatchScore();

        // Any further point should not change state
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);

        String after = manager.getMatchScore();
        assertEquals(before, after);
    }

    // --------- PrintScore coverage (normal + tie-break) ---------

    @Test
    public void testPrintScoreNormalAndTieBreakPaths() {
        // Normal path: just score some points and call printScore
        manager.pointScored(1);
        manager.pointScored(2);
        manager.printScore(); // just for coverage of printing logic

        // Tie-break path: reach tie-break and call printScore again
        manager = new TennisScoreManager();
        reachTieBreak(manager);
        // Now in tie-break mode
        manager.printScore(); // should use getTieBreakScore internally
    }
}
