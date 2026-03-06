import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestTennisScoreManager {

    // Helper method to set private field via reflection
    private void setPrivateField(TennisScoreManager manager, String fieldName, Object value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }

    // Helper method to get private field via reflection
    private Object getPrivateField(TennisScoreManager manager, String fieldName) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(manager);
    }
    
    // Helper method to invoke private method via reflection (not needed for this class based on analysis)
    // private void invokePrivateMethod(TennisScoreManager manager, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
    //     Method method = TennisScoreManager.class.getDeclaredMethod(methodName, parameterTypes);
    //     method.setAccessible(true);
    //     method.invoke(manager, args);
    // }

    // Helper to simulate points for a game score
    private void simulateGamePoints(TennisScoreManager manager, int p1Score, int p2Score) throws Exception {
        setPrivateField(manager, "scoreP1", p1Score);
        setPrivateField(manager, "scoreP2", p2Score);
    }

    // Helper to simulate games won
    private void simulateGames(TennisScoreManager manager, int p1Games, int p2Games) throws Exception {
        setPrivateField(manager, "gamesP1", p1Games);
        setPrivateField(manager, "gamesP2", p2Games);
    }

    // Helper to simulate sets won
    private void simulateSetScores(TennisScoreManager manager, int setIndex, int p1SetScore, int p2SetScore) throws Exception {
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        setsP1[setIndex] = p1SetScore;
        setsP2[setIndex] = p2SetScore;
    }
    
    // Helper to move to a specific set
    private void setSetNumber(TennisScoreManager manager, int set) throws Exception {
        setPrivateField(manager, "currentSet", set);
    }
    
    // Helper to set tiebreak state
    private void setTieBreak(TennisScoreManager manager, boolean isTieBreak) throws Exception {
        setPrivateField(manager, "isTieBreak", isTieBreak);
    }


    // --- Tests for Constructor ---

    @Test
    public void TennisScoreManagerInitializationTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(0, getPrivateField(manager, "gamesP2"));
        assertEquals(1, getPrivateField(manager, "currentSet"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        for (int i = 0; i < 5; i++) {
            assertEquals(0, setsP1[i]);
            assertEquals(0, setsP2[i]);
        }
    }

    // --- Tests for resetPoints() ---

    @Test
    public void resetPointsNonZeroTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 2);
        
        manager.resetPoints();
        
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
    }

    // --- Tests for resetGameAndPoints() ---

    @Test
    public void resetGameAndPointsNonZeroTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 2, 1);
        simulateGames(manager, 4, 3);
        setTieBreak(manager, true);
        
        manager.resetGameAndPoints();
        
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(0, getPrivateField(manager, "gamesP2"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }

    // --- Tests for pointScored(int player) ---

    @Test
    public void pointScoredP1NormalTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        assertEquals(1, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }

    @Test
    public void pointScoredP2NormalTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(1, getPrivateField(manager, "scoreP2"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }
    
    @Test
    public void pointScoredInvalidPlayerTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(3); // Invalid player
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
    }
    
    @Test
    public void pointScoredGameOverTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 6, 4);
        simulateSetScores(manager, 2, 6, 4);
        setSetNumber(manager, 4); // Match is over (P1 won 3 sets)
        
        manager.pointScored(1); // Should print game over and return
        
        // Scores should not change
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
    }
    
    @Test
    public void pointScoredP1TieBreakTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        manager.pointScored(1);
        assertEquals(1, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertTrue((boolean) getPrivateField(manager, "isTieBreak"));
    }
    
    @Test
    public void pointScoredP2GameEndTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 2, 3); // 30-40
        manager.pointScored(2); // P2 wins game
        assertEquals(0, getPrivateField(manager, "scoreP1")); // Reset points
        assertEquals(0, getPrivateField(manager, "scoreP2")); // Reset points
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(1, getPrivateField(manager, "gamesP2"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }


    // --- Tests for checkGamePoint() ---

    @Test
    public void checkGamePointP1WinGameTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 1); // 40-15
        manager.pointScored(1); // P1 scores, 4-1, P1 wins game
        
        assertEquals(0, getPrivateField(manager, "scoreP1")); // Points reset
        assertEquals(0, getPrivateField(manager, "scoreP2")); // Points reset
        assertEquals(1, getPrivateField(manager, "gamesP1")); // Game won
        assertEquals(0, getPrivateField(manager, "gamesP2"));
        assertEquals(1, getPrivateField(manager, "currentSet"));
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }
    
    @Test
    public void checkGamePointP2WinGameTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 4, 6); // Advantage P2, but scoreP2 >= 4 and scoreP2 >= scoreP1 + 2 is true
        
        // Call checkGamePoint directly to test game end logic
        Method checkGamePoint = TennisScoreManager.class.getDeclaredMethod("checkGamePoint");
        checkGamePoint.setAccessible(true);
        checkGamePoint.invoke(manager);
        
        assertEquals(0, getPrivateField(manager, "scoreP1")); // Points reset
        assertEquals(0, getPrivateField(manager, "scoreP2")); // Points reset
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(7, getPrivateField(manager, "gamesP2")); // Game won (6+1 = 7) - this is testing the logic if scores were set this way
    }
    
    @Test
    public void checkGamePointNoWinTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 3); // Deuce
        
        // Call checkGamePoint directly
        Method checkGamePoint = TennisScoreManager.class.getDeclaredMethod("checkGamePoint");
        checkGamePoint.setAccessible(true);
        checkGamePoint.invoke(manager);
        
        assertEquals(3, getPrivateField(manager, "scoreP1")); // No reset
        assertEquals(3, getPrivateField(manager, "scoreP2")); // No reset
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(0, getPrivateField(manager, "gamesP2"));
    }
    
    @Test
    public void checkGamePointP1AdvantageTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 4, 3); // Adv P1 (scoreP1 = 4, scoreP2 = 3)
        
        // Call checkGamePoint directly
        Method checkGamePoint = TennisScoreManager.class.getDeclaredMethod("checkGamePoint");
        checkGamePoint.setAccessible(true);
        checkGamePoint.invoke(manager);
        
        assertEquals(4, getPrivateField(manager, "scoreP1")); // No reset (scoreP1 < scoreP2 + 2 is 4 < 5, false)
        assertEquals(3, getPrivateField(manager, "scoreP2"));
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(0, getPrivateField(manager, "gamesP2"));
    }
    
    @Test
    public void checkGamePointP1WinSetTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 1);
        simulateGames(manager, 5, 4); // Set score 5-4
        
        manager.pointScored(1); // P1 wins game, 4-1 -> P1 wins game. Games 6-4, P1 wins set
        
        assertEquals(0, getPrivateField(manager, "scoreP1")); 
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertEquals(0, getPrivateField(manager, "gamesP1")); // Games reset
        assertEquals(0, getPrivateField(manager, "gamesP2")); // Games reset
        assertEquals(2, getPrivateField(manager, "currentSet")); // Next set
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(6, setsP1[0]);
        assertEquals(4, setsP2[0]);
    }


    // --- Tests for getGameScore() ---

    @Test
    public void getGameScoreLoveLoveTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 0, 0);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void getGameScore15LoveTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 1, 0);
        assertEquals("15-Love", manager.getGameScore());
    }
    
    @Test
    public void getGameScore3040Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 2, 3);
        assertEquals("30-40", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreDeuce33Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 3);
        assertEquals("Deuce", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreDeuce44Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 4, 4);
        assertEquals("Deuce", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreAdvantageP1Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 4, 3);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreAdvantageP2Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        // The condition in the source code has a bug: `scoreP2 == scoreP2 + 1` is always false.
        // It should probably be `scoreP2 >= 3 && scoreP2 == scoreP1 + 1`.
        // However, we test the code *as is*, where this block will never be hit, and the score will default to "Errore Game".
        // To cover the branch `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)` we would need a scenario where scoreP2 is 3, 4, or 5 and equals scoreP2 + 1, which is impossible.
        // The *only* way to cover the branch `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)` is by using an impossible condition, so we can only aim to cover the 'else' (Errore Game).
        simulateGamePoints(manager, 3, 4); // P2 Advantage - The correct logic should result in "Vantaggio P2"
        // Testing the existing incorrect logic:
        // P1 check: scoreP1=3, scoreP1=4, 3==4+1 false
        // P2 check: scoreP2=4, scoreP2=4, 4==4+1 false -> falls through to Errore Game
        assertEquals("Errore Game", manager.getGameScore()); 
    }
    
    @Test
    public void getGameScoreErroreGameTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 5, 3); // P1 wins game, but *if* we check score before reset
        assertEquals("Errore Game", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreGameOverTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 6, 4);
        simulateSetScores(manager, 2, 6, 4);
        setSetNumber(manager, 4); 
        assertTrue(manager.isGameOver()); // To set the internal "Game Over" state
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }


    // --- Tests for checkTieBreakPoint() ---

    @Test
    public void checkTieBreakPointP1WinTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 6, 4); // 6-4
        simulateGames(manager, 6, 6);
        
        manager.pointScored(1); // P1 scores, 7-4, P1 wins tie-break (and set)
        
        assertFalse((boolean) getPrivateField(manager, "isTieBreak")); // Tie-break ended, reset by resetGameAndPoints
        assertEquals(0, getPrivateField(manager, "scoreP1")); // Points reset
        assertEquals(0, getPrivateField(manager, "scoreP2")); // Points reset
        assertEquals(0, getPrivateField(manager, "gamesP1")); // Games reset
        assertEquals(0, getPrivateField(manager, "gamesP2")); // Games reset
        assertEquals(2, getPrivateField(manager, "currentSet")); // Next set
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(7, setsP1[0]); // Tie-break game score is 7
        assertEquals(6, setsP2[0]);
    }
    
    @Test
    public void checkTieBreakPointP2WinTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 5, 6); // 5-6
        simulateGames(manager, 6, 6);
        
        manager.pointScored(2); // P2 scores, 5-7, P2 wins tie-break (and set)
        
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
        assertEquals(0, getPrivateField(manager, "scoreP1"));
        assertEquals(0, getPrivateField(manager, "scoreP2"));
        assertEquals(0, getPrivateField(manager, "gamesP1"));
        assertEquals(0, getPrivateField(manager, "gamesP2"));
        assertEquals(2, getPrivateField(manager, "currentSet"));
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(6, setsP1[0]);
        assertEquals(7, setsP2[0]);
    }

    @Test
    public void checkTieBreakPointNoWinTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 6, 6); // 6-6
        
        Method checkTieBreakPoint = TennisScoreManager.class.getDeclaredMethod("checkTieBreakPoint");
        checkTieBreakPoint.setAccessible(true);
        checkTieBreakPoint.invoke(manager);
        
        assertTrue((boolean) getPrivateField(manager, "isTieBreak"));
        assertEquals(6, getPrivateField(manager, "scoreP1"));
        assertEquals(6, getPrivateField(manager, "scoreP2"));
    }
    
    @Test
    public void checkTieBreakPointP1AdvantageTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 7, 6); // 7-6
        
        Method checkTieBreakPoint = TennisScoreManager.class.getDeclaredMethod("checkTieBreakPoint");
        checkTieBreakPoint.setAccessible(true);
        checkTieBreakPoint.invoke(manager);
        
        assertTrue((boolean) getPrivateField(manager, "isTieBreak"));
        assertEquals(7, getPrivateField(manager, "scoreP1"));
        assertEquals(6, getPrivateField(manager, "scoreP2"));
    }


    // --- Tests for getTieBreakScore() ---

    @Test
    public void getTieBreakScoreTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 5, 5);
        assertEquals("TIE-BREAK: 5-5", manager.getTieBreakScore());
    }

    // --- Tests for checkSetPoint() ---

    @Test
    public void checkSetPointP1Wins64Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGames(manager, 6, 4); // Already 6-4
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        assertEquals(2, getPrivateField(manager, "currentSet"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(6, setsP1[0]);
        assertEquals(4, setsP2[0]);
        assertEquals(0, getPrivateField(manager, "gamesP1"));
    }
    
    @Test
    public void checkSetPointP2Wins75Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGames(manager, 5, 7); // Already 5-7
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        assertEquals(2, getPrivateField(manager, "currentSet"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(5, setsP1[0]);
        assertEquals(7, setsP2[0]);
        assertEquals(0, getPrivateField(manager, "gamesP2"));
    }

    @Test
    public void checkSetPointP1Wins76Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGames(manager, 7, 6); // Already 7-6
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        assertEquals(2, getPrivateField(manager, "currentSet"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(7, setsP1[0]);
        assertEquals(6, setsP2[0]);
    }
    
    @Test
    public void checkSetPointP2Wins76TestLogicErrorCase() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        // The condition has a logic error: gamesP2 == 7 && gamesP2 == 6 is false.
        // It should probably be gamesP2 == 7 && gamesP1 == 6. We test the actual code.
        simulateGames(manager, 6, 7);
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        // P1 conditions: gamesP1 >= 6 && gamesP1 >= gamesP2 + 2 (7 >= 9 false) || (gamesP1 == 7 && gamesP2 == 5) (7==7 && 6==5 false) || (gamesP2 == 7 && gamesP2 == 6) (6==7 && 6==6 false) -> false
        // P2 conditions: gamesP2 >= 6 && gamesP2 >= gamesP1 + 2 (7 >= 8 false) || (gamesP2 == 7 && gamesP1 == 5) (7==7 && 6==5 false) || (gamesP2 == 7 && gamesP1 == 6) (7==7 && 6==6 false) -> false
        // The *actual* condition in code for P2: `(gamesP2 == 7 && gamesP1 == 5) || (gamesP2 == 7 && gamesP1 == 6)` is WRONG in the source code provided:
        // P2 condition in code: `(gamesP2 == 7 && gamesP1 == 5) || (gamesP2 == 7 && gamesP2 == 6)`
        // With 6-7: gamesP2 >= gamesP1 + 2 (7 >= 8) -> false.
        // Second part: gamesP2 == 7 && gamesP1 == 5 (7==7 && 6==5) -> false.
        // Third part: gamesP2 == 7 && gamesP2 == 6 (7==7 && 7==6) -> false.
        // Therefore, NO SET END occurs with 6-7 with the provided source code's logic.
        
        assertEquals(1, getPrivateField(manager, "currentSet")); // No change
        assertEquals(7, getPrivateField(manager, "gamesP2"));
        // This confirms the bug in the provided code's P2 set win logic for 7-6.
    }

    @Test
    public void checkSetPointNoWinTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGames(manager, 4, 3);
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        assertEquals(1, getPrivateField(manager, "currentSet"));
        assertEquals(4, getPrivateField(manager, "gamesP1"));
    }
    
    @Test
    public void checkSetPointTieBreakStartTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGames(manager, 6, 6);
        
        Method checkSetPoint = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        checkSetPoint.setAccessible(true);
        checkSetPoint.invoke(manager);
        
        assertTrue((boolean) getPrivateField(manager, "isTieBreak"));
        assertEquals(0, getPrivateField(manager, "scoreP1")); // Points reset
        assertEquals(1, getPrivateField(manager, "currentSet")); // Same set
    }


    // --- Tests for moveToNextSet() ---

    @Test
    public void moveToNextSetNormalTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setSetNumber(manager, 1);
        simulateGames(manager, 6, 4); // Simulate set win without actually calling checkSetPoint
        simulateSetScores(manager, 0, 6, 4);
        
        manager.moveToNextSet();
        
        assertEquals(2, getPrivateField(manager, "currentSet"));
        assertEquals(0, getPrivateField(manager, "gamesP1")); // Game/points reset
        assertFalse((boolean) getPrivateField(manager, "isTieBreak"));
    }
    
    @Test
    public void moveToNextSetGameOverTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 6, 4);
        simulateSetScores(manager, 2, 6, 4);
        setSetNumber(manager, 3);
        
        assertTrue(manager.isGameOver()); // Sets game over state
        
        manager.moveToNextSet();
        
        assertEquals(3, getPrivateField(manager, "currentSet")); // Should not increment
    }


    // --- Tests for getMatchScore() ---

    @Test
    public void getMatchScoreInitialTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 0, 0);
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void getMatchScoreMidGameTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 2, 1); // 30-15
        simulateGames(manager, 3, 2);
        
        assertEquals("0-0 (Game: 3-2 30-15)", manager.getMatchScore());
    }
    
    @Test
    public void getMatchScoreMidSetScoreTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setSetNumber(manager, 2);
        simulateSetScores(manager, 0, 6, 4);
        simulateGames(manager, 1, 2);
        simulateGamePoints(manager, 3, 2); // 40-30
        
        assertEquals("1-0 (Game: 1-2 40-30)", manager.getMatchScore());
    }
    
    @Test
    public void getMatchScoreMidTieBreakTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 4, 3); // Tie-break 4-3
        simulateGames(manager, 6, 6); // Current games 6-6
        
        assertEquals("0-0 (Game: 6-6 TIE-BREAK: 4-3)", manager.getMatchScore());
    }
    
    @Test
    public void getMatchScoreGameOverP1Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 6, 7);
        simulateSetScores(manager, 2, 6, 4);
        simulateSetScores(manager, 3, 7, 5);
        setSetNumber(manager, 4);
        
        manager.isGameOver(); // Triggers the calculation
        
        assertEquals("P1: 3 Set | P2: 1 Set", manager.getMatchScore());
    }

    @Test
    public void getMatchScoreGameOverP2Test() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 4, 6);
        simulateSetScores(manager, 1, 6, 7);
        simulateSetScores(manager, 2, 4, 6);
        simulateSetScores(manager, 3, 5, 7);
        setSetNumber(manager, 4);
        
        manager.isGameOver(); // Triggers the calculation
        
        assertEquals("P1: 1 Set | P2: 3 Set", manager.getMatchScore());
    }


    // --- Tests for isGameOver() ---

    @Test
    public void isGameOverFalseTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 4, 6);
        setSetNumber(manager, 3);
        
        assertFalse(manager.isGameOver());
    }

    @Test
    public void isGameOverP1WinsTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4); // P1 win
        simulateSetScores(manager, 1, 4, 6); // P2 win
        simulateSetScores(manager, 2, 7, 5); // P1 win
        simulateSetScores(manager, 3, 6, 3); // P1 win
        setSetNumber(manager, 4);
        
        assertTrue(manager.isGameOver()); // P1 wins 3-1
    }
    
    @Test
    public void isGameOverP2WinsTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4); // P1 win
        simulateSetScores(manager, 1, 4, 6); // P2 win
        simulateSetScores(manager, 2, 5, 7); // P2 win
        simulateSetScores(manager, 3, 3, 6); // P2 win
        setSetNumber(manager, 4);
        
        assertTrue(manager.isGameOver()); // P2 wins 1-3
    }
    
    @Test
    public void isGameOverP1WinsOn3SetTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 4); 
        simulateSetScores(manager, 1, 6, 4); 
        simulateSetScores(manager, 2, 6, 4); 
        setSetNumber(manager, 4); // Set 3 has been completed
        
        assertTrue(manager.isGameOver()); // P1 wins 3-0
    }
    
    @Test
    public void isGameOverP2WinsOn5SetTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateSetScores(manager, 0, 6, 7); 
        simulateSetScores(manager, 1, 7, 6); 
        simulateSetScores(manager, 2, 6, 7); 
        simulateSetScores(manager, 3, 7, 6); 
        simulateSetScores(manager, 4, 4, 6); // P2 wins
        setSetNumber(manager, 5);
        
        assertTrue(manager.isGameOver()); // P2 wins 3-2
    }


    // --- Tests for printScore() ---

    @Test
    public void printScoreInitialTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        // Just call to ensure no exceptions and coverage of standard game score path
        manager.printScore(); 
    }
    
    @Test
    public void printScoreMidSetTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setSetNumber(manager, 2);
        simulateSetScores(manager, 0, 6, 4);
        simulateGames(manager, 5, 3);
        simulateGamePoints(manager, 1, 2); // 15-30
        
        // Just call to ensure no exceptions and coverage of existing set scores
        manager.printScore();
    }
    
    @Test
    public void printScoreDeuceTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 3, 3); // Deuce
        
        manager.printScore();
    }
    
    @Test
    public void printScoreAdvantageTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        simulateGamePoints(manager, 4, 3); // Advantage P1
        
        manager.printScore();
    }
    
    @Test
    public void printScoreTieBreakTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setTieBreak(manager, true);
        simulateGamePoints(manager, 5, 5);
        
        manager.printScore(); // Coverage of isTieBreak path
    }

    @Test
    public void printScoreMultipleSetsTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setSetNumber(manager, 4);
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 4, 6);
        simulateSetScores(manager, 2, 7, 5);
        
        manager.printScore();
    }
    
    @Test
    public void printScoreMaxSetsTest() throws Exception {
        TennisScoreManager manager = new TennisScoreManager();
        setSetNumber(manager, 5);
        simulateSetScores(manager, 0, 6, 4);
        simulateSetScores(manager, 1, 4, 6);
        simulateSetScores(manager, 2, 7, 5);
        simulateSetScores(manager, 3, 6, 3);

        manager.printScore();
    }
}