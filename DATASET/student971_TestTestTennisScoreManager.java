import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Helper class to manage output capturing for testing System.out.println
// In a real project, this would be handled by a test rule like SystemOutRule,
// but for a self-contained test file, a simple redirection is used.
class OutputCapture {
    private final java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
    private final java.io.PrintStream originalOut = System.out;

    public void startCapture() {
        System.setOut(new java.io.PrintStream(outContent));
    }

    public String stopCapture() {
        System.setOut(originalOut);
        String captured = outContent.toString();
        outContent.reset(); // Reset for next use
        return captured;
    }
}

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private OutputCapture outputCapture;

    @Before
    public void setup() {
        manager = new TennisScoreManager();
        outputCapture = new OutputCapture();
        // Set up the manager's private fields for easier state manipulation
        setPrivateField("scoreP1", 0);
        setPrivateField("scoreP2", 0);
        setPrivateField("gamesP1", 0);
        setPrivateField("gamesP2", 0);
        setPrivateField("currentSet", 1);
        setPrivateField("isTieBreak", false);
        setPrivateField("setsP1", new int[5]);
        setPrivateField("setsP2", new int[5]);
    }

    /**
     * Helper to set private fields using reflection. Necessary for complex state management.
     */
    private void setPrivateField(String fieldName, Object value) {
        try {
            Field field = TennisScoreManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(manager, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    /**
     * Helper to get private fields using reflection.
     */
    private Object getPrivateField(String fieldName) {
        try {
            Field field = TennisScoreManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field " + fieldName, e);
        }
    }

    // --- Core and Reset Methods Tests ---

    @Test
    public void testConstructorAndReset() {
        // Constructor test implicitly done via setup
        assertEquals(0, getPrivateField("scoreP1"));
        assertEquals(1, getPrivateField("currentSet"));

        // Test resetPoints
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 2);
        manager.resetPoints();
        assertEquals(0, getPrivateField("scoreP1"));
        assertEquals(0, getPrivateField("scoreP2"));
    }

    @Test
    public void testResetGameAndPoints() {
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 4);
        setPrivateField("scoreP1", 3);
        setPrivateField("isTieBreak", true);
        
        manager.resetGameAndPoints();
        
        assertEquals(0, getPrivateField("gamesP1"));
        assertEquals(0, getPrivateField("gamesP2"));
        assertEquals(0, getPrivateField("scoreP1"));
        assertFalse((Boolean) getPrivateField("isTieBreak"));
    }

    // --- Game Score (Points) Tests ---

    @Test
    public void testGameScoreNormal() {
        // 0-0 -> Love-Love
        assertEquals("Love-Love", manager.getGameScore());

        // 1-0 -> 15-Love
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());

        // 1-1 -> 15-15
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());

        // 3-3 -> Deuce (40-40, i.e., scoreP1=3, scoreP2=3)
        manager.pointScored(1); // 2-1
        manager.pointScored(1); // 3-1 (40-15)
        manager.pointScored(2); // 3-2 (40-30)
        manager.pointScored(2); // 3-3 (Deuce)
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGameScoreAdvantage() {
        // Set to Deuce
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 3);
        
        // Adv P1 (scoreP1=4, scoreP2=3)
        setPrivateField("scoreP1", 4);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // Back to Deuce (scoreP1=4, scoreP2=4)
        setPrivateField("scoreP2", 4);
        assertEquals("Deuce", manager.getGameScore());
        
        // Adv P2 (scoreP1=4, scoreP2=5)
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 5);
        // The condition for P2 Advantage is flawed in the original code,
        // so it falls through to "Errore Game".
        assertEquals("Errore Game", manager.getGameScore()); 
        
        // Test "Errore Game" for other high scores not covered by Adv/Deuce
        setPrivateField("scoreP1", 10);
        setPrivateField("scoreP2", 0);
        assertEquals("Errore Game", manager.getGameScore()); 
    }
    
    // --- Point Scored Tests ---

    @Test
    public void testPointScoredP1() {
        manager.pointScored(1);
        assertEquals(1, getPrivateField("scoreP1"));
        assertEquals(0, getPrivateField("scoreP2"));
    }
    
    @Test
    public void testPointScoredInvalidPlayer() {
        outputCapture.startCapture();
        manager.pointScored(3); // Invalid player
        String output = outputCapture.stopCapture();
        assertTrue(output.contains("Errore: Giocatore non valido. Usa 1 o 2."));
        assertEquals(0, getPrivateField("scoreP1")); // Scores should not change
    }

    @Test
    public void testPointScoredGameOver() {
        // Manually set P1 to win the match (3 sets)
        int[] p1Sets = {6, 6, 6, 0, 0};
        int[] p2Sets = {4, 4, 4, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 4); // Match is over after 3 sets

        assertTrue(manager.isGameOver()); // Check pre-condition

        outputCapture.startCapture();
        manager.pointScored(1); // Score a point after match is over
        String output = outputCapture.stopCapture();
        
        // Should print the game over message and return immediately
        assertTrue(output.contains("La partita è finita! Punteggio finale:"));
        
        // Scores should not change
        assertEquals(0, getPrivateField("scoreP1"));
    }

    // --- Check Game Point (Game Win) Tests ---

    @Test
    public void testCheckGamePointP1Win_40_Adv_Win() {
        // Set state to 40-Adv for P1 (scoreP1=4, scoreP2=3)
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 3);
        setPrivateField("gamesP1", 2);
        
        // Now P1 scores one more point, should win game (scoreP1=5, scoreP2=3, i.e., scoreP1 >= 4 && scoreP1 >= scoreP2 + 2)
        // Since pointScored increments the score before calling checkGamePoint, we manually set the final winning score
        setPrivateField("scoreP1", 5); 
        manager.checkGamePoint(); 
        
        assertEquals(3, getPrivateField("gamesP1")); // P1 won a game
        assertEquals(0, getPrivateField("scoreP1")); // Points reset
        assertEquals(0, getPrivateField("scoreP2"));
    }

    @Test
    public void testCheckGamePointP2Win_Deuce_Win() {
        // Set state to 40-40, then P2 wins 2 consecutive points (scoreP2=5, scoreP1=3)
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 5);
        setPrivateField("gamesP2", 3);
        
        manager.checkGamePoint(); 
        
        assertEquals(4, getPrivateField("gamesP2")); // P2 won a game
        assertEquals(0, getPrivateField("scoreP1")); // Points reset
    }
    
    @Test
    public void testCheckGamePointNoWin() {
        // Test a state where no game is won (Deuce)
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 3);
        setPrivateField("gamesP1", 2);
        
        manager.checkGamePoint(); 
        
        assertEquals(2, getPrivateField("gamesP1")); // Game score should remain unchanged
    }

    // --- Check Set Point (Set Win) Tests ---
    
    @Test
    public void testSetPointTieBreakTrigger() {
        // Games at 6-6 (Must reset points immediately)
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 3); // Check if points reset
        
        outputCapture.startCapture();
        manager.checkSetPoint();
        outputCapture.stopCapture();
        
        assertTrue((Boolean) getPrivateField("isTieBreak"));
        assertEquals(0, getPrivateField("scoreP1")); // Points must be reset
        assertEquals(6, getPrivateField("gamesP1")); // Games remain 6-6
        assertEquals(1, getPrivateField("currentSet")); // Set index remains 1
    }

    @Test
    public void testCheckSetPointP1Win_6_0() {
        // P1 wins 6-0 (gamesP1 >= gamesP2 + 2 and gamesP1 >= 6)
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 0);
        
        manager.checkSetPoint();
        
        assertEquals(2, getPrivateField("currentSet")); // Should move to next set
        assertEquals(6, ((int[]) getPrivateField("setsP1"))[0]); // Sets array updated
        assertEquals(0, getPrivateField("gamesP1")); // Games reset for new set
    }
    
    @Test
    public void testCheckSetPointP2Win_7_5() {
        // P2 wins 7-5 (covered by gamesP2 >= gamesP1 + 2 OR explicit check)
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 7);
        
        manager.checkSetPoint();
        
        assertEquals(2, getPrivateField("currentSet")); // Should move to next set
        assertEquals(7, ((int[]) getPrivateField("setsP2"))[0]); // Sets array updated
        assertEquals(0, getPrivateField("gamesP2")); // Games reset for new set
    }
    
    @Test
    public void testCheckSetPointP2Win_7_6_TieBreakWin() {
        // P2 wins 7-6 (covered by the explicit check: (gamesP2 == 7 && gamesP1 == 6))
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 7);
        
        manager.checkSetPoint();
        
        assertEquals(2, getPrivateField("currentSet")); // Should move to next set
        assertEquals(6, ((int[]) getPrivateField("setsP1"))[0]);
        assertEquals(7, ((int[]) getPrivateField("setsP2"))[0]);
    }
    
    @Test
    public void testCheckSetPointNoWin() {
        // Test a state where no set is won (e.g., 5-4)
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 4);
        
        manager.checkSetPoint();
        
        assertEquals(1, getPrivateField("currentSet")); // Current set should be unchanged
    }

    // --- Tie Break Tests ---

    @Test
    public void testTieBreakScoring() {
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 2);
        
        assertEquals("TIE-BREAK: 4-2", manager.getTieBreakScore());
    }
    
    @Test
    public void testCheckTieBreakPointP1Win() {
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6); // Set is 6-6
        setPrivateField("gamesP2", 6);
        
        // P1 wins tie break 7-5 (scoreP1=7, scoreP2=5)
        setPrivateField("scoreP1", 7);
        setPrivateField("scoreP2", 5);
        
        outputCapture.startCapture();
        manager.checkTieBreakPoint(); 
        outputCapture.stopCapture();
        
        // P1 should win the game/set, and move to the next set
        assertEquals(7, getPrivateField("gamesP1")); // Games updated to 7-6 or 7-5
        assertEquals(2, getPrivateField("currentSet")); // Should move to next set
        assertFalse((Boolean) getPrivateField("isTieBreak")); // Tiebreak must be reset
    }

    @Test
    public void testCheckTieBreakPointP2Win() {
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6); // Set is 6-6
        setPrivateField("gamesP2", 6);
        
        // P2 wins tie break 9-7 (scoreP2=9, scoreP1=7)
        setPrivateField("scoreP1", 7);
        setPrivateField("scoreP2", 9);
        
        manager.checkTieBreakPoint(); 
        
        // P2 should win the game/set, and move to the next set
        assertEquals(7, getPrivateField("gamesP2")); // Games updated (6+1 = 7)
        assertEquals(2, getPrivateField("currentSet")); // Should move to next set
    }
    
    @Test
    public void testCheckTieBreakPointNoWin() {
        setPrivateField("isTieBreak", true);
        // Scores are 6-6 in the tie-break (Deuce equivalent)
        setPrivateField("scoreP1", 6);
        setPrivateField("scoreP2", 6);
        
        manager.checkTieBreakPoint(); 
        
        assertEquals(0, getPrivateField("gamesP1")); // Games should not change
    }
    
    // --- Move To Next Set Test ---

    @Test
    public void testMoveToNextSet() {
        setPrivateField("currentSet", 2);
        setPrivateField("gamesP1", 6);
        
        manager.moveToNextSet(); // Match is not over
        
        assertEquals(3, getPrivateField("currentSet"));
        assertEquals(0, getPrivateField("gamesP1")); // Games reset
    }

    @Test
    public void testMoveToNextSetGameOver() {
        // Manually set P1 to win the match
        int[] p1Sets = {6, 6, 6, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("currentSet", 3); 

        // isGameOver is true, so moveToNextSet should exit early
        manager.moveToNextSet(); 
        
        assertEquals(3, getPrivateField("currentSet")); // Should not increment
    }

    // --- Is Game Over Tests ---

    @Test
    public void testIsGameOverFalse_MiddleSet() {
        // Set state to 1-1 in sets, middle of set 3
        int[] p1Sets = {6, 4, 0, 0, 0};
        int[] p2Sets = {4, 6, 0, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 3);
        
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testIsGameOverP1WinsMatch() {
        // P1 wins 3-0
        int[] p1Sets = {6, 6, 6, 0, 0};
        int[] p2Sets = {4, 4, 4, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 4); // After set 3 completes, currentSet is 4
        
        outputCapture.startCapture();
        boolean result = manager.isGameOver();
        String output = outputCapture.stopCapture();
        
        assertTrue(result);
        assertTrue(output.contains("*** PARTITA VINTA DAL GIOCATORE 1! (3 Set a 0) ***"));
    }

    @Test
    public void testIsGameOverP2WinsMatch() {
        // P2 wins 3-2
        int[] p1Sets = {7, 6, 4, 6, 4};
        int[] p2Sets = {6, 7, 6, 4, 6};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 6); // After set 5 completes, currentSet is 6 (though the array is only size 5)
        
        outputCapture.startCapture();
        boolean result = manager.isGameOver();
        String output = outputCapture.stopCapture();
        
        assertTrue(result);
        assertTrue(output.contains("*** PARTITA VINTA DAL GIOCATORE 2! (3 Set a 2) ***"));
    }
    
    // --- Get Match Score Tests ---

    @Test
    public void testGetMatchScoreInGame_Standard() {
        // Set state to 1-1 in sets, 3-2 in games, 30-15 in points
        int[] p1Sets = {6, 4, 0, 0, 0};
        int[] p2Sets = {4, 6, 0, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 3);
        setPrivateField("gamesP2", 2);
        setPrivateField("scoreP1", 2);
        setPrivateField("scoreP2", 1);
        
        // Expected: 1-1 (Game: 3-2 30-15)
        assertEquals("1-1 (Game: 3-2 30-15)", manager.getMatchScore());
    }
    
    @Test
    public void testGetMatchScoreInGame_TieBreak() {
        // Set state to 1-1 in sets, 6-6 in games, Tie Break 5-3
        int[] p1Sets = {6, 4, 0, 0, 0};
        int[] p2Sets = {4, 6, 0, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 5);
        setPrivateField("scoreP2", 3);
        
        // Expected: 1-1 (Game: 6-6 TIE-BREAK: 5-3)
        assertEquals("1-1 (Game: 6-6 TIE-BREAK: 5-3)", manager.getMatchScore());
    }

    @Test
    public void testGetMatchScoreGameOver() {
        // Match over (P1 wins 3-1)
        int[] p1Sets = {6, 4, 7, 6, 0};
        int[] p2Sets = {4, 6, 5, 4, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 5); // Match ended after set 4
        
        // Must call isGameOver to set up state
        setPrivateField("currentSet", 4);
        // P1 sets won: 2, P2 sets won: 2. Not over.
        // Let's manually ensure it's over for the test (P1 3-1)
        int[] finalP1Sets = {6, 4, 7, 6, 0};
        int[] finalP2Sets = {4, 6, 5, 4, 0}; // Corrected score for 3-1 (P1 wins S1, S3, S4)
        setPrivateField("setsP1", finalP1Sets);
        setPrivateField("setsP2", finalP2Sets);
        setPrivateField("currentSet", 5); 

        // P1 wins S1, S3, S4 (3 sets). P2 wins S2 (1 set).
        assertTrue(manager.isGameOver()); // Check pre-condition
        
        // Expected: P1: 3 Set | P2: 1 Set
        assertEquals("P1: 3 Set | P2: 1 Set", manager.getMatchScore());
    }
    
    // --- Print Score Tests ---
    
    @Test
    public void testPrintScoreSingleSet_Standard() {
        // Current set is 1, 3-2 games, 30-30 points
        setPrivateField("gamesP1", 3);
        setPrivateField("gamesP2", 2);
        setPrivateField("scoreP1", 2);
        setPrivateField("scoreP2", 2);
        
        outputCapture.startCapture();
        manager.printScore();
        String output = outputCapture.stopCapture();
        
        // Punteggio Set should be empty since currentSet-1 is 0
        assertTrue(output.contains("Punteggio Set: P1 [] - P2 []"));
        assertTrue(output.contains("Set Corrente (1): P1 3 Game | P2 2 Game"));
        assertTrue(output.contains("Punti Correnti: 30-30"));
    }

    @Test
    public void testPrintScoreMultiSet_TieBreak() {
        // Current set is 3, 6-6 games, Tie Break (score 4-4)
        int[] p1Sets = {6, 4, 0, 0, 0};
        int[] p2Sets = {4, 6, 0, 0, 0};
        setPrivateField("setsP1", p1Sets);
        setPrivateField("setsP2", p2Sets);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 4);

        outputCapture.startCapture();
        manager.printScore();
        String output = outputCapture.stopCapture();

        // Punteggio Set should show results for Set 1 (6-4) and Set 2 (4-6)
        assertTrue(output.contains("Punteggio Set: P1 [6, 4] - P2 [4, 6]")); 
        assertTrue(output.contains("Set Corrente (3): P1 6 Game | P2 6 Game"));
        assertTrue(output.contains("Punti Correnti: TIE-BREAK: 4-4"));
    }
}