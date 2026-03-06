/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: dom.mennillo@studenti.unina.it
UserID: 332
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class TestTennisScoreManager {
	private TennisScoreManager manager;
    
    // To suppress console output during tests to keep execution clean
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Helper method to set private field values using reflection.
     * This allows us to set up specific complex states (like 5-5 in games) 
     * without simulating hundreds of method calls.
     */
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }

    /**
     * Helper method to get private field values using reflection for assertions.
     */
    private Object getPrivateField(String fieldName) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(manager);
    }

    /**
     * Helper to get the sets array for a player.
     */
    private int[] getSetsArray(String fieldName) throws Exception {
        return (int[]) getPrivateField(fieldName);
    }

    // --- Test Cases ---

    @Test
    public void testPointScored_InvalidPlayer() throws Exception {
        // Test input validation branch
        manager.pointScored(3);
        manager.pointScored(0);
        
        int scoreP1 = (int) getPrivateField("scoreP1");
        int scoreP2 = (int) getPrivateField("scoreP2");
        
        assertEquals("Score should not change for invalid player", 0, scoreP1);
        assertEquals("Score should not change for invalid player", 0, scoreP2);
    }

    @Test
    public void testPointScored_P1_StandardPoint() throws Exception {
        // Initial state: 0-0. P1 scores.
        manager.pointScored(1);
        
        int scoreP1 = (int) getPrivateField("scoreP1");
        assertEquals("P1 score should be 1 (15)", 1, scoreP1);
        assertEquals("P2 score should remain 0", 0, (int) getPrivateField("scoreP2"));
    }

    @Test
    public void testPointScored_P2_StandardPoint() throws Exception {
        // Initial state: 0-0. P2 scores.
        manager.pointScored(2);
        
        int scoreP2 = (int) getPrivateField("scoreP2");
        assertEquals("P2 score should be 1 (15)", 1, scoreP2);
        assertEquals("P1 score should remain 0", 0, (int) getPrivateField("scoreP1"));
    }

    @Test
    public void testPointScored_P1_WinGame_Standard() throws Exception {
        // Scenario: P1 has 40 (3 points), P2 has 0. P1 scores -> Wins Game.
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 0);
        
        manager.pointScored(1);
        
        assertEquals("P1 should satisfy game win condition", 0, (int) getPrivateField("scoreP1")); // Points reset
        assertEquals("P1 games should increment", 1, (int) getPrivateField("gamesP1"));
    }

    @Test
    public void testPointScored_P2_WinGame_Standard() throws Exception {
        // Scenario: P1 has 0, P2 has 40 (3 points). P2 scores -> Wins Game.
        setPrivateField("scoreP1", 0);
        setPrivateField("scoreP2", 3);
        
        manager.pointScored(2);
        
        assertEquals("P2 points reset", 0, (int) getPrivateField("scoreP2"));
        assertEquals("P2 games should increment", 1, (int) getPrivateField("gamesP2"));
    }

    @Test
    public void testPointScored_Deuce_Advantage() throws Exception {
        // Scenario: Deuce (3-3). P1 scores -> Advantage P1 (4-3).
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 3);
        
        manager.pointScored(1);
        
        assertEquals("P1 should have 4 points (Advantage)", 4, (int) getPrivateField("scoreP1"));
        assertEquals("Game count should not change yet", 0, (int) getPrivateField("gamesP1"));
        
        // P2 scores -> Back to Deuce logic (technically 4-4 internally in this simplified logic, or just equality preventing win)
        // Note: The provided code doesn't decrement advantage, it keeps adding. 
        // Code Logic: if scoreP1 >= 4 && scoreP1 >= scoreP2 + 2.
        // 4-3 -> P2 scores -> 4-4. Not winning.
        manager.pointScored(2);
        assertEquals("P2 should have 4 points", 4, (int) getPrivateField("scoreP2"));
        assertEquals("Game should still be running", 0, (int) getPrivateField("gamesP1"));
        
        // P2 scores again -> 4-5 (Advantage P2)
        manager.pointScored(2);
        assertEquals("P2 should have 5 points", 5, (int) getPrivateField("scoreP2"));
        
        // P2 scores again -> 4-6 (Diff 2) -> Win Game P2
        manager.pointScored(2);
        assertEquals("P2 should win game", 1, (int) getPrivateField("gamesP2"));
        assertEquals("Points reset", 0, (int) getPrivateField("scoreP2"));
    }

    @Test
    public void testPointScored_SetWin_Standard_6_4() throws Exception {
        // Scenario: Games 5-4. P1 wins game -> 6-4 (Set Win).
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 4);
        setPrivateField("scoreP1", 3); // 40
        setPrivateField("scoreP2", 0); // 0
        
        manager.pointScored(1);
        
        int[] setsP1 = getSetsArray("setsP1");
        assertEquals("Current set games stored in history", 6, setsP1[0]);
        assertEquals("Current set should advance", 2, (int) getPrivateField("currentSet"));
        assertEquals("Games reset for new set", 0, (int) getPrivateField("gamesP1"));
    }
    
    @Test
    public void testPointScored_SetWin_Standard_P2_6_4() throws Exception {
        // Scenario: Games 4-5. P2 wins game -> 4-6 (Set Win).
        setPrivateField("gamesP1", 4);
        setPrivateField("gamesP2", 5);
        setPrivateField("scoreP1", 0); 
        setPrivateField("scoreP2", 3); // 40
        
        manager.pointScored(2);
        
        int[] setsP2 = getSetsArray("setsP2");
        assertEquals("Current set games stored in history for P2", 6, setsP2[0]);
        assertEquals("Current set should advance", 2, (int) getPrivateField("currentSet"));
    }

    @Test
    public void testPointScored_SetExtension_5_5_to_6_5() throws Exception {
        // Scenario: Games 5-5. P1 wins game -> 6-5 (No Set Win yet, needs 7).
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 5);
        setPrivateField("scoreP1", 3);
        
        manager.pointScored(1);
        
        assertEquals("Games P1", 6, (int) getPrivateField("gamesP1"));
        assertEquals("Current set should NOT advance", 1, (int) getPrivateField("currentSet"));
    }

    @Test
    public void testPointScored_SetWin_7_5() throws Exception {
        // Scenario: Games 6-5. P1 wins game -> 7-5 (Set Win).
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 5);
        setPrivateField("scoreP1", 3);
        
        manager.pointScored(1);
        
        int[] setsP1 = getSetsArray("setsP1");
        assertEquals("Set should end 7-5", 7, setsP1[0]);
        assertEquals("Current set should advance", 2, (int) getPrivateField("currentSet"));
    }

    @Test
    public void testPointScored_TriggerTieBreak() throws Exception {
        // Scenario: Games 6-5. P2 wins game -> 6-6 (Trigger Tie Break).
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 5);
        setPrivateField("scoreP2", 3);
        
        manager.pointScored(2);
        
        assertEquals("Games P2", 6, (int) getPrivateField("gamesP2"));
        assertTrue("Should be in Tie Break mode", (boolean) getPrivateField("isTieBreak"));
        assertEquals("Points should be reset", 0, (int) getPrivateField("scoreP1"));
    }

    @Test
    public void testPointScored_TieBreak_Scoring() throws Exception {
        // Scenario: In Tie Break.
        setPrivateField("isTieBreak", true);
        
        manager.pointScored(1);
        assertEquals("Tie break point P1", 1, (int) getPrivateField("scoreP1"));
        
        manager.pointScored(2);
        assertEquals("Tie break point P2", 1, (int) getPrivateField("scoreP2"));
    }

    @Test
    public void testPointScored_TieBreak_Win_7_0() throws Exception {
        // Scenario: Tie break 6-0. P1 scores -> 7-0 (Set Win).
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 6);
        setPrivateField("scoreP2", 0);
        
        manager.pointScored(1);
        
        int[] setsP1 = getSetsArray("setsP1");
        assertEquals("Set should end 7-6 (games logic increments P1)", 7, setsP1[0]);
        assertFalse("Tie break flag should be cleared", (boolean) getPrivateField("isTieBreak"));
        assertEquals("Set advanced", 2, (int) getPrivateField("currentSet"));
    }

    @Test
    public void testPointScored_TieBreak_Extension() throws Exception {
        // Scenario: Tie break 6-6. P1 scores -> 7-6 (No Win, needs diff 2).
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 6);
        setPrivateField("scoreP2", 6);
        
        manager.pointScored(1);
        
        assertEquals("Score P1", 7, (int) getPrivateField("scoreP1"));
        assertEquals("Set not changed", 1, (int) getPrivateField("currentSet"));
        
        // P1 scores again -> 8-6 (Win).
        manager.pointScored(1);
        assertEquals("Set advanced", 2, (int) getPrivateField("currentSet"));
    }
    
    @Test
    public void testPointScored_TieBreak_Win_P2() throws Exception {
         // Scenario: Tie break 6-7 (P2 leads). P2 scores -> 6-8 (Win for P2).
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 6);
        setPrivateField("scoreP2", 7);
        
        manager.pointScored(2);
        
        int[] setsP2 = getSetsArray("setsP2");
        assertEquals("Set should end 6-7 (games logic increments P2)", 7, setsP2[0]);
        assertEquals("Set advanced", 2, (int) getPrivateField("currentSet"));
    }

    @Test
    public void testPointScored_MatchWin_P1() throws Exception {
        // Scenario: P1 Won 2 sets already. Currently Set 3. P1 wins Set 3.
        int[] setsP1 = new int[5];
        setsP1[0] = 6; setsP1[1] = 6;
        int[] setsP2 = new int[5];
        setsP2[0] = 0; setsP2[1] = 0;
        
        setPrivateField("setsP1", setsP1);
        setPrivateField("setsP2", setsP2);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 5);
        setPrivateField("scoreP1", 3);
        
        // Trigger win of 3rd set
        manager.pointScored(1);
        
        // Logic: isGameOver check inside pointScored happens at start, then actions, then printScore calls isGameOver again.
        // After this point, isGameOver should be true.
        assertTrue("Match should be over", manager.isGameOver());
    }

    @Test
    public void testPointScored_MatchWin_P2() throws Exception {
        // Scenario: P2 Won 2 sets. P2 wins Set 3.
        int[] setsP1 = new int[5];
        setsP1[0] = 0; setsP1[1] = 0;
        int[] setsP2 = new int[5];
        setsP2[0] = 6; setsP2[1] = 6;
        
        setPrivateField("setsP1", setsP1);
        setPrivateField("setsP2", setsP2);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP2", 5);
        setPrivateField("scoreP2", 3);
        
        manager.pointScored(2);
        
        assertTrue("Match should be over", manager.isGameOver());
    }

    @Test
    public void testPointScored_AfterGameOver() throws Exception {
        // Scenario: Match is already over. Calling pointScored should do nothing.
        
        // Force state where P1 won 3 sets
        int[] setsP1 = new int[5];
        setsP1[0] = 6; setsP1[1] = 6; setsP1[2] = 6;
        setPrivateField("setsP1", setsP1);
        setPrivateField("currentSet", 4); // Moved to "next" set after 3rd win
        
        // Verify isGameOver is true
        assertTrue(manager.isGameOver());
        
        // Try to score
        setPrivateField("scoreP1", 0);
        manager.pointScored(1);
        
        // Assert score did not increase
        assertEquals("Score should not change after game over", 0, (int) getPrivateField("scoreP1"));
    }
    
    @Test
    public void testGetGameScore_Branches() throws Exception {
        // Specific coverage for getGameScore strings
        
        // 1. Deuce
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 3);
        assertEquals("Deuce", manager.getGameScore());
        
        // 2. Advantage P1
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 3);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // 3. Advantage P2
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 4);
        assertEquals("Vantaggio P2", manager.getGameScore());
        
        // 4. Standard "15-30"
        setPrivateField("scoreP1", 1);
        setPrivateField("scoreP2", 2);
        assertEquals("15-30", manager.getGameScore());
        
        // 5. Error Case (Should cover the final return "Errore Game")
        // Logic: P1 >= 3, but NOT (P1 == P2 + 1). E.g. 5-3 (Wins are usually caught by checkGamePoint, 
        // but getGameScore might be called in intermediate weird states or directly)
        // However, in normal flow checkGamePoint resets points before we print.
        // To hit "Errore Game", we need scoreP1 >= 3, scoreP2 not 3 (or 3 handled by deuce), not adv diff 1.
        // If we force 5-5 (points, not games), it's theoretically impossible via pointScored, 
        // but valid for testing the method's fallback return.
        setPrivateField("scoreP1", 5);
        setPrivateField("scoreP2", 5);
        assertEquals("Errore Game", manager.getGameScore());
    }

}

						