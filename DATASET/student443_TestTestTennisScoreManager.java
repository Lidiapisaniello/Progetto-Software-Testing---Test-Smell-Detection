import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    /**
     * Helper method to set private integer fields using Reflection.
     */
    private void setPrivateIntField(String fieldName, int value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(manager, value);
    }

    /**
     * Helper method to set private boolean fields using Reflection.
     */
    private void setPrivateBooleanField(String fieldName, boolean value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setBoolean(manager, value);
    }

    /**
     * Helper method to set private array fields using Reflection.
     */
    private void setPrivateIntArrayField(String fieldName, int[] value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }

    /**
     * Helper method to get private integer fields using Reflection.
     */
    private int getPrivateIntField(String fieldName) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(manager);
    }

    /**
     * White-box test to target the specific logic of "Vantaggio P2".
     * NOTE: The source code contains a logical impossibility: (scoreP2 == scoreP2 + 1).
     * This test specifically targets that state to ensure the fall-through behavior (path coverage)
     * leads to "Errore Game" instead of "Vantaggio P2", documenting the bug.
     */
    @Test
    public void testAdvantageP2_PathCoverage_BugExposure() throws Exception {
        // P1 = 3 (40), P2 = 4 (Advantage)
        setPrivateIntField("scoreP1", 3);
        setPrivateIntField("scoreP2", 4);

        // This exercises the 'getGameScore' method.
        // Expected behavior per source code logic flaw: checks fail, returns "Errore Game".
        String score = manager.getGameScore();

        assertEquals("Errore Game", score);
    }

    /**
     * White-box test for the internal method 'checkTieBreakPoint'.
     * Covers the branch where P1 wins the Tie-Break.
     * Note: This also exercises the interaction with 'resetGameAndPoints' inside the logic.
     */
    @Test
    public void testTieBreakWinExecution_P1() throws Exception {
        // Setup state: 6-6 in games, TieBreak active, P1 leading 6-0 in points
        setPrivateIntField("gamesP1", 6);
        setPrivateIntField("gamesP2", 6);
        setPrivateBooleanField("isTieBreak", true);
        setPrivateIntField("scoreP1", 6);
        setPrivateIntField("scoreP2", 0);

        // Action: P1 scores the 7th point
        manager.pointScored(1);

        // Verification:
        // We verify that 'resetGameAndPoints' was called inside 'checkTieBreakPoint'.
        // Due to the logic in source, games are reset to 0 immediately.
        assertEquals(0, getPrivateIntField("gamesP1"));
        assertEquals(0, getPrivateIntField("scoreP1"));
        assertFalse((Boolean) getPrivateBooleanField("isTieBreak", false));
    }

    /**
     * White-box test for the internal method 'checkTieBreakPoint'.
     * Covers the branch where P2 wins the Tie-Break.
     */
    @Test
    public void testTieBreakWinExecution_P2() throws Exception {
        // Setup state: 6-6 in games, TieBreak active, P2 leading 6-0 in points
        setPrivateIntField("gamesP1", 6);
        setPrivateIntField("gamesP2", 6);
        setPrivateBooleanField("isTieBreak", true);
        setPrivateIntField("scoreP1", 0);
        setPrivateIntField("scoreP2", 6);

        // Action: P2 scores the 7th point
        manager.pointScored(2);

        // Verification:
        // Verify internal reset occurred
        assertEquals(0, getPrivateIntField("gamesP2"));
        assertEquals(0, getPrivateIntField("scoreP2"));
        assertFalse((Boolean) getPrivateBooleanField("isTieBreak", false));
    }

    /**
     * White-box test to simulate a mid-match state and verify 'getMatchScore' loops.
     * This bypasses playing dozens of points to reach Set 3.
     */
    @Test
    public void testMatchScore_InternalStateReconstruction() throws Exception {
        // Simulate: P1 won Set 1, P2 won Set 2. Currently in Set 3.
        int[] setsP1 = new int[5]; setsP1[0] = 6; setsP1[1] = 4;
        int[] setsP2 = new int[5]; setsP2[0] = 4; setsP2[1] = 6;

        setPrivateIntArrayField("setsP1", setsP1);
        setPrivateIntArrayField("setsP2", setsP2);
        setPrivateIntField("currentSet", 3);
        setPrivateIntField("gamesP1", 2);
        setPrivateIntField("gamesP2", 3);

        String matchScore = manager.getMatchScore();

        // setsWonP1 should be 1, setsWonP2 should be 1
        // Expected string contains "1-1 (Game: 2-3 ...)"
        assertTrue(matchScore.startsWith("1-1"));
        assertTrue(matchScore.contains("Game: 2-3"));
    }


    /**
     * White-box test for 'isGameOver' logic via Reflection.
     * Forces the condition where P2 has won 3 sets to ensure coverage of that specific if-block.
     */
    @Test
    public void testIsGameOver_P2Wins() throws Exception {
        // Inject 3 sets won by P2
        int[] setsP1 = new int[5];
        int[] setsP2 = new int[5];
        for(int i=0; i<3; i++) {
            setsP2[i] = 6;
            setsP1[i] = 0;
        }
        setPrivateIntArrayField("setsP1", setsP1);
        setPrivateIntArrayField("setsP2", setsP2);
        setPrivateIntField("currentSet", 4); // Must be at least set 4 or check loop limit

        boolean result = manager.isGameOver();
        assertTrue(result);
    }

    /**
     * White-box test for 'getTieBreakScore'.
     * Ensures correct string formatting without relying on game flow.
     */
    @Test
    public void testGetTieBreakScore_Direct() throws Exception {
        setPrivateIntField("scoreP1", 5);
        setPrivateIntField("scoreP2", 4);

        assertEquals("TIE-BREAK: 5-4", manager.getTieBreakScore());
    }

    /**
     * White-box test for 'resetGameAndPoints'.
     * Ensures all internal fields (including tie break flag) are reset.
     */
    @Test
    public void testResetGameAndPoints_InternalState() throws Exception {
        // Dirty the state
        setPrivateIntField("gamesP1", 5);
        setPrivateIntField("gamesP2", 5);
        setPrivateIntField("scoreP1", 3);
        setPrivateBooleanField("isTieBreak", true);

        manager.resetGameAndPoints();

        assertEquals(0, getPrivateIntField("gamesP1"));
        assertEquals(0, getPrivateIntField("gamesP2"));
        assertEquals(0, getPrivateIntField("scoreP1"));
        assertFalse((Boolean) getPrivateBooleanField("isTieBreak", false));
    }

    /**
     * Test 'printScore' to ensure statement coverage.
     * Even though it writes to void/System.out, we execute it in a complex state
     * to ensure no runtime exceptions occur during array access in the print loops.
     */
    @Test
    public void testPrintScore_ComplexState() throws Exception {
        setPrivateIntField("currentSet", 2);
        int[] setsP1 = new int[5]; setsP1[0] = 6;
        int[] setsP2 = new int[5]; setsP2[0] = 4;
        setPrivateIntArrayField("setsP1", setsP1);
        setPrivateIntArrayField("setsP2", setsP2);

        // Should not throw exception
        manager.printScore();
    }

    /**
     * White-box test helper helper to access private boolean field method with default.
     */
    private Object getPrivateBooleanField(String fieldName, boolean defaultValue) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(manager);
    }
}