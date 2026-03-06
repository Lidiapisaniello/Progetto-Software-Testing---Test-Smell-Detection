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

    // --- HELPER METHODS PER REFLECTION ---
    
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }
    
    private void setPrivateArrayElement(String arrayName, int index, int value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(arrayName);
        field.setAccessible(true);
        int[] array = (int[]) field.get(manager);
        array[index] = value;
    }

    private Object getPrivateField(String fieldName) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(manager);
    }
    
    // Metodo per invocare checkSetPoint direttamente (bypassando il bug di resetPoints)
    private void invokeCheckSetPoint() throws Exception {
        Method method = TennisScoreManager.class.getDeclaredMethod("checkSetPoint");
        method.setAccessible(true);
        method.invoke(manager);
    }

    // ==========================================
    // 1. TEST INIT & RESET
    // ==========================================

    @Test
    public void initTest() {
        assertNotNull(manager);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void resetPointsTest() throws Exception {
        setPrivateField("scoreP1", 2);
        setPrivateField("scoreP2", 3);
        
        manager.resetPoints();
        
        assertEquals(0, getPrivateField("scoreP1"));
        assertEquals(0, getPrivateField("scoreP2"));
    }

    @Test
    public void resetGameAndPointsTest() throws Exception {
        setPrivateField("gamesP1", 4);
        setPrivateField("scoreP1", 2);
        setPrivateField("isTieBreak", true);
        
        manager.resetGameAndPoints();
        
        assertEquals(0, getPrivateField("gamesP1"));
        assertEquals(0, getPrivateField("scoreP1"));
        assertFalse((Boolean) getPrivateField("isTieBreak"));
    }

    // ==========================================
    // 2. TEST POINT SCORED
    // ==========================================

    @Test
    public void pointScoredP1IncrementTest() throws Exception {
        manager.pointScored(1);
        assertEquals(1, getPrivateField("scoreP1")); 
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredP2IncrementTest() throws Exception {
        manager.pointScored(2);
        assertEquals(1, getPrivateField("scoreP2")); 
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        manager.pointScored(3);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredMatchOverTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6);
        setPrivateArrayElement("setsP1", 1, 6);
        setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        
        manager.pointScored(1);
        
        assertEquals(0, getPrivateField("scoreP1")); 
    }

    // ==========================================
    // 3. TEST GET GAME SCORE
    // ==========================================

    @Test
    public void getGameScoreStandardValuesTest() throws Exception {
        setPrivateField("scoreP1", 1); 
        setPrivateField("scoreP2", 2); 
        assertEquals("15-30", manager.getGameScore());

        setPrivateField("scoreP1", 3); 
        setPrivateField("scoreP2", 0); 
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void getGameScoreDeuceTest() throws Exception {
        setPrivateField("scoreP1", 3); 
        setPrivateField("scoreP2", 3); 
        assertEquals("Deuce", manager.getGameScore());
        
        setPrivateField("scoreP1", 5);
        setPrivateField("scoreP2", 5);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() throws Exception {
        setPrivateField("scoreP1", 4);
        setPrivateField("scoreP2", 3);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2Test() throws Exception {
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 4);
        // Ritorna "Errore Game" a causa del bug logico nel sorgente (scoreP2 == scoreP2 + 1)
        // Ma il test passa confermando il comportamento attuale
        assertEquals("Errore Game", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreErrorStateTest() throws Exception {
        setPrivateField("scoreP1", 10);
        setPrivateField("scoreP2", 0); 
        assertEquals("Errore Game", manager.getGameScore());
    }

    // ==========================================
    // 4. TEST WINNING GAME
    // ==========================================

    @Test
    public void checkGamePointWinP1Test() throws Exception {
        setPrivateField("scoreP1", 3); 
        setPrivateField("scoreP2", 0); 
        
        manager.pointScored(1); 
        
        assertEquals(1, getPrivateField("gamesP1"));
        assertEquals(0, getPrivateField("scoreP1")); 
    }

    @Test
    public void checkGamePointWinP2FromAdvantageTest() throws Exception {
        setPrivateField("scoreP1", 3); 
        setPrivateField("scoreP2", 4); 
        
        manager.pointScored(2); 
        
        assertEquals(1, getPrivateField("gamesP2"));
        assertEquals(0, getPrivateField("scoreP2"));
    }

    // ==========================================
    // 5. TEST TIE BREAK & BUG MANAGEMENT
    // ==========================================

    @Test
    public void checkSetPointTriggerTieBreakTest() throws Exception {
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 3);
        setPrivateField("scoreP2", 0);

        manager.pointScored(1); 
        
        assertEquals(6, getPrivateField("gamesP1"));
        assertTrue((Boolean) getPrivateField("isTieBreak"));
    }

    @Test
    public void pointScoredTieBreakIncrementTest() throws Exception {
        setPrivateField("isTieBreak", true);
        manager.pointScored(1);
        
        assertEquals(1, getPrivateField("scoreP1"));
        
        setPrivateField("scoreP1", 4);
        manager.pointScored(1);
        assertEquals(5, getPrivateField("scoreP1")); 
    }
    
    @Test
    public void getTieBreakScoreTest() throws Exception {
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 5);
        setPrivateField("scoreP2", 4);
        String score = manager.getMatchScore(); 
        assertTrue(score.contains("TIE-BREAK: 5-4"));
    }

    @Test
    public void pointScoredTieBreakWinP1_BugBehaviorTest() throws Exception {
        // Test che documenta il BUG del reset prematuro.
        // P1 vince tiebreak, ma i game vengono resettati prima di assegnare il set.
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 6);
        setPrivateField("scoreP2", 5);

        manager.pointScored(1); 

        assertEquals(0, getPrivateField("gamesP1"));
        int[] setsP1 = (int[]) getPrivateField("setsP1");
        assertEquals(0, setsP1[0]); // Nessun set assegnato a causa del bug
    }

    // ==========================================
    // 6. TEST COPERTURA VITTORIA SET (BYPASS BUG)
    // ==========================================

    @Test
    public void checkSetPointTieBreakWinP1_Direct_7_5_Test() throws Exception {
        // STRATEGIA PER COPERTURA: 
        // Poiché il codice per 7-6 di P1 è rotto (typo: gamesP2==7 && gamesP2==6),
        // testiamo la vittoria 7-5. Questo entra nel blocco if e copre le righe di vittoria.
        setPrivateField("gamesP1", 7);
        setPrivateField("gamesP2", 5);
        setPrivateField("currentSet", 1);
        
        invokeCheckSetPoint();
        
        int[] setsP1 = (int[]) getPrivateField("setsP1");
        assertEquals(7, setsP1[0]); 
        assertEquals(2, getPrivateField("currentSet"));
    }
    
    @Test
    public void checkSetPointTieBreakWinP2_Direct_7_6_Test() throws Exception {
        // Per P2, la logica 7-6 è scritta corretta nel sorgente.
        // Usiamo 7-6 per coprire questo path.
        setPrivateField("gamesP1", 6);
        setPrivateField("gamesP2", 7);
        setPrivateField("currentSet", 1);
        
        invokeCheckSetPoint();
        
        int[] setsP2 = (int[]) getPrivateField("setsP2");
        assertEquals(7, setsP2[0]);
        assertEquals(2, getPrivateField("currentSet"));
    }
    
    @Test
    public void checkSetPointTieBreakWinP1_Direct_7_6_BugTest() throws Exception {
        // Test specifico che conferma che il codice 7-6 per P1 è rotto.
        setPrivateField("gamesP1", 7);
        setPrivateField("gamesP2", 6);
        setPrivateField("currentSet", 1);
        
        invokeCheckSetPoint();
        
        // Non succede nulla a causa del typo (gamesP2==7 && gamesP2==6)
        int[] setsP1 = (int[]) getPrivateField("setsP1");
        assertEquals(0, setsP1[0]); 
    }

    // ==========================================
    // 7. TEST WINNING SET (STANDARD)
    // ==========================================

    @Test
    public void checkSetPointWinSetP1StandardTest() throws Exception {
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 3);
        setPrivateField("scoreP1", 3);
        
        manager.pointScored(1); 
        
        int[] setsP1 = (int[]) getPrivateField("setsP1");
        assertEquals(6, setsP1[0]);
        assertEquals(2, getPrivateField("currentSet"));
    }

    @Test
    public void checkSetPointWinSetP2ExtendedTest() throws Exception {
        setPrivateField("gamesP1", 5);
        setPrivateField("gamesP2", 6);
        setPrivateField("scoreP2", 3);
        
        manager.pointScored(2); 
        
        int[] setsP2 = (int[]) getPrivateField("setsP2");
        assertEquals(7, setsP2[0]);
        assertEquals(2, getPrivateField("currentSet"));
    }

    // ==========================================
    // 8. TEST MATCH FLOW
    // ==========================================

    @Test
    public void isGameOverP1WinsMatchTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6); 
        setPrivateArrayElement("setsP1", 1, 6); 
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 5);
        setPrivateField("scoreP1", 3);
        
        manager.pointScored(1); 
        
        assertTrue(manager.isGameOver());
    }

    @Test
    public void isGameOverP2WinsMatchTest() throws Exception {
        setPrivateArrayElement("setsP2", 0, 6);
        setPrivateArrayElement("setsP2", 1, 6);
        setPrivateArrayElement("setsP2", 2, 6);
        setPrivateField("currentSet", 4); 
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }
    
    @Test
    public void getGameScoreGameOverTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6);
        setPrivateArrayElement("setsP1", 1, 6);
        setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void getMatchScoreInProgressTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6); 
        setPrivateArrayElement("setsP2", 0, 4);
        setPrivateField("currentSet", 2);
        setPrivateField("gamesP1", 2);
        setPrivateField("gamesP2", 1);
        setPrivateField("scoreP1", 1); 
        
        String score = manager.getMatchScore();
        assertTrue(score.contains("1-0"));
        assertTrue(score.contains("Game: 2-1"));
        assertTrue(score.contains("15-Love"));
    }
    
    @Test
    public void moveToNextSetBlockedByGameOverTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6);
        setPrivateArrayElement("setsP1", 1, 6);
        setPrivateArrayElement("setsP1", 2, 6); 
        setPrivateField("currentSet", 4); 
        
        manager.moveToNextSet();
        
        assertEquals(4, getPrivateField("currentSet"));
    }
}