/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Oriana"
Cognome: "Buccelli"
Username: o.buccelli@studenti.unina.it
UserID: 609
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

public class TestTennisScoreManager {

    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima dell'inizio dei test nella classe
    }
                
    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
    }
                
    @Before
    public void setUp() {
        // Eseguito prima di ogni metodo di test
    }
                
    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
    }

    // --- Helper Method for Atomic State Setup (Reflection) ---
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    // --- Tests ---

    @Test
    public void constructorInitializationTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        
        assertNotNull(setsP1);
        assertNotNull(setsP2);
        assertEquals(0, setsP1[0]);
        assertEquals(0, setsP2[0]);
    }

    @Test
    public void pointScoredPlayer1StandardIncrementTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Initial score 0
        manager.pointScored(1);
        
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        assertEquals("Score should increment to 1", 1, scoreP1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredPlayer2StandardIncrementTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        
        int scoreP2 = (int) getPrivateField(manager, "scoreP2");
        assertEquals("Score should increment to 1", 1, scoreP2);
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(3); // Invalid
        
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        int scoreP2 = (int) getPrivateField(manager, "scoreP2");
        
        assertEquals(0, scoreP1);
        assertEquals(0, scoreP2);
    }

    @Test
    public void pointScoredMatchOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        
        // Simulate Player 1 won 3 sets
        int[] setsP1 = new int[5];
        int[] setsP2 = new int[5];
        setsP1[0]=6; setsP2[0]=0;
        setsP1[1]=6; setsP2[1]=0;
        setsP1[2]=6; setsP2[2]=0;
        
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 4);
        
        // Try to score
        manager.pointScored(1);
        
        // Score shouldn't change because game is over
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        assertEquals(0, scoreP1);
    }

    @Test
    public void getGameScoreDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 3);
        
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 4);
        setPrivateField(manager, "scoreP2", 3);
        
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 4);
        
        assertEquals("Vantaggio P2", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreErrorTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Impossible state for standard game logic, but covered by fallback return
        setPrivateField(manager, "scoreP1", 5);
        setPrivateField(manager, "scoreP2", 0);
        
        assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void checkGamePointP1WinsGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3); // 40
        setPrivateField(manager, "scoreP2", 0); // Love
        
        manager.pointScored(1); // Wins game
        
        int gamesP1 = (int) getPrivateField(manager, "gamesP1");
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        
        assertEquals(1, gamesP1);
        assertEquals(0, scoreP1); // Points reset
    }

    @Test
    public void checkGamePointP2WinsGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 0);
        setPrivateField(manager, "scoreP2", 3); // 40
        
        manager.pointScored(2); // Wins game
        
        int gamesP2 = (int) getPrivateField(manager, "gamesP2");
        assertEquals(1, gamesP2);
    }

    @Test
    public void checkSetPointStandardWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "gamesP2", 3);
        setPrivateField(manager, "scoreP1", 3); // 40
        
        manager.pointScored(1); // Win game -> Win set (6-3)
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int currentSet = (int) getPrivateField(manager, "currentSet");
        
        assertEquals("Set 1 should store 6 games", 6, setsP1[0]);
        assertEquals("Should move to set 2", 2, currentSet);
    }
    
    @Test
    public void checkSetPointStandardWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 4);
        setPrivateField(manager, "gamesP2", 5);
        setPrivateField(manager, "scoreP2", 3); 
        
        manager.pointScored(2); // Win game -> Win set (4-6)
        
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(6, setsP2[0]);
    }

    @Test
    public void checkSetPointTriggerTieBreakTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 3); 
        
        manager.pointScored(1); // P1 wins game -> 6-6
        
        boolean isTieBreak = (boolean) getPrivateField(manager, "isTieBreak");
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        
        assertTrue(isTieBreak);
        assertEquals(0, scoreP1); // Points reset for tie break
    }

    @Test
    public void tieBreakPointScoredTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        
        manager.pointScored(1);
        
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        assertEquals(1, scoreP1);
        assertTrue(manager.getTieBreakScore().contains("1-0"));
    }

    @Test
    public void tieBreakWinP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 6);
        setPrivateField(manager, "scoreP2", 0);
        
        manager.pointScored(1); // 7-0 in TieBreak -> Set Win
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        boolean isTieBreak = (boolean) getPrivateField(manager, "isTieBreak");
        int currentSet = (int) getPrivateField(manager, "currentSet");

        assertEquals(7, setsP1[0]); // Tie break win recorded as 7 games
        assertFalse(isTieBreak); // Tie break ended
        assertEquals(2, currentSet);
    }

    @Test
    public void tieBreakWinP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 5);
        setPrivateField(manager, "scoreP2", 6);
        
        manager.pointScored(2); // P2 wins tie break
        
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals(7, setsP2[0]);
    }

    @Test
    public void checkSetPointExtendedWin7_5Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 5);
        setPrivateField(manager, "scoreP1", 3);
        
        manager.pointScored(1); // 7-5 Win (Not tie break)
        
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        assertEquals(7, setsP1[0]);
    }

    @Test
    public void isGameOverP2WinsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = {0, 0, 0, 0, 0};
        int[] setsP2 = {6, 6, 6, 0, 0};
        
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 3); // Finished 3 sets
        
        assertTrue(manager.isGameOver());
    }
    
    @Test
    public void isGameOverP1WinsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = {6, 6, 6, 0, 0};
        int[] setsP2 = {4, 2, 5, 0, 0};
        
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 3); 
        
        assertTrue(manager.isGameOver());
    }

    @Test
    public void getMatchScoreInProgressTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 2);
        setPrivateField(manager, "gamesP2", 1);
        
        String score = manager.getMatchScore();
        // Expected "0-0 (Game: 2-1 Love-Love)" roughly
        assertTrue(score.contains("0-0"));
        assertTrue(score.contains("Game: 2-1"));
    }

    @Test
    public void getMatchScoreFinalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = {6, 6, 6, 0, 0};
        int[] setsP2 = {0, 0, 0, 0, 0};
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 3); 
        
        String score = manager.getMatchScore();
        assertEquals("P1: 3 Set | P2: 0 Set", score);
    }
    
    @Test
    public void resetPointsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        manager.resetPoints();
        int scoreP1 = (int) getPrivateField(manager, "scoreP1");
        assertEquals(0, scoreP1);
    }

    // Using Mockito Spy to verify void method interaction without return value
    @Test
    public void pointScoredCallsPrintScoreTest() {
        TennisScoreManager manager = new TennisScoreManager();
        TennisScoreManager spyManager = spy(manager);
        
        spyManager.pointScored(1);
        
        // Verify printScore() was called (even though it prints to void)
        verify(spyManager, times(1)).printScore();
    }
}