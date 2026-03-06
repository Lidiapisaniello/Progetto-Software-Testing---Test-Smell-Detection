/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Federica
Cognome: Musella
Username: federica.musella4@studenti.unina.it
UserID: 131
Date: 23/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        // Catturiamo lo stdout per verificare che i metodi void stampino (copertura linee System.out)
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    // --- Reflection Helpers ---

    private void setPrivateField(String fieldName, Object value) {
        try {
            Field field = manager.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(manager, value);
        } catch (Exception e) {
            fail("Reflection set error: " + e.getMessage());
        }
    }

    private Object getPrivateField(String fieldName) {
        try {
            Field field = manager.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(manager);
        } catch (Exception e) {
            fail("Reflection get error: " + e.getMessage());
            return null;
        }
    }

    private void invokePrivateMethod(String methodName) {
        try {
            Method method = manager.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(manager);
        } catch (Exception e) {
            // Ignora eccezioni per testare i branch puri
        }
    }

    // =========================================================================
    // 1. Basic Inputs & Guards
    // =========================================================================

    @Test
    public void testPointScored_P1() {
        manager.pointScored(1);
        assertEquals(1, getPrivateField("scoreP1"));
    }

    @Test
    public void testPointScored_P2() {
        manager.pointScored(2);
        assertEquals(1, getPrivateField("scoreP2"));
    }

    @Test
    public void testPointScored_Invalid() {
        manager.pointScored(99);
        assertEquals(0, getPrivateField("scoreP1"));
        assertTrue(outContent.toString().contains("Errore"));
    }

    @Test
    public void testPointScored_GameOverGuard() {
        int[] sets = {6, 6, 6, 0, 0};
        setPrivateField("setsP1", sets);
        setPrivateField("currentSet", 4);
        
        manager.pointScored(1);
        assertTrue(outContent.toString().contains("finale")); 
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }
    
    @Test
    public void testResetMethods() {
        setPrivateField("scoreP1", 3);
        manager.resetPoints();
        assertEquals(0, getPrivateField("scoreP1"));
        
        setPrivateField("gamesP1", 2);
        manager.resetGameAndPoints();
        assertEquals(0, getPrivateField("gamesP1"));
        assertFalse((boolean) getPrivateField("isTieBreak"));
    }

    // =========================================================================
    // 2. Logic: checkGamePoint & getGameScore
    // =========================================================================

    @Test
    public void testGameScore_Conditions() {
        // Love-Love
        assertEquals("Love-Love", manager.getGameScore());
        
        // 30-30
        setPrivateField("scoreP1", 2); setPrivateField("scoreP2", 2);
        assertEquals("30-30", manager.getGameScore());
        
        // Deuce
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 3);
        assertEquals("Deuce", manager.getGameScore());
        
        // Adv P1
        setPrivateField("scoreP1", 4); setPrivateField("scoreP2", 3);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // Adv P2 (Dead Code Coverage attempt -> fallback logic)
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 4);
        assertEquals("Errore Game", manager.getGameScore()); 
        
        // Mixed (Love-40) -> covers (scoreP1 != 3 || scoreP2 != 3) logic
        setPrivateField("scoreP1", 0); setPrivateField("scoreP2", 3);
        assertEquals("Love-40", manager.getGameScore());
        
        // Mixed (40-Love)
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 0);
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testCheckGamePoint_Logic() {
        // --- P1 Wins ---
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 0);
        manager.pointScored(1);
        assertEquals(1, getPrivateField("gamesP1"));
        
        // FIX: Reset gamesP1 per le asserzioni successive
        setPrivateField("gamesP1", 0);
        
        // --- P2 Wins ---
        setPrivateField("scoreP1", 0); setPrivateField("scoreP2", 3);
        manager.pointScored(2);
        assertEquals(1, getPrivateField("gamesP2"));
        
        // FIX: Reset gamesP2 per le asserzioni successive
        setPrivateField("gamesP2", 0);
        
        // --- Adv P1 No Win (diff < 2) ---
        setPrivateField("scoreP1", 4); setPrivateField("scoreP2", 3);
        invokePrivateMethod("checkGamePoint");
        assertEquals(0, getPrivateField("gamesP1"));
        
        // --- Adv P2 No Win (diff < 2) ---
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 4);
        invokePrivateMethod("checkGamePoint");
        assertEquals(0, getPrivateField("gamesP2"));
    }

    // =========================================================================
    // 3. Logic: checkSetPoint (Complex Branches & Bugs)
    // =========================================================================

    @Test
    public void testSetWin_6_4() {
        setPrivateField("gamesP1", 5); setPrivateField("gamesP2", 4);
        setPrivateField("scoreP1", 3);
        manager.pointScored(1);
        assertEquals(2, getPrivateField("currentSet"));
        assertTrue(outContent.toString().contains("Vinto da P1"));
    }
    
    @Test
    public void testSetWin_5_7() {
        setPrivateField("gamesP1", 5); setPrivateField("gamesP2", 6);
        setPrivateField("scoreP2", 3);
        manager.pointScored(2);
        assertEquals(2, getPrivateField("currentSet"));
        assertTrue(outContent.toString().contains("Vinto da P2"));
    }

    @Test
    public void testSetWin_7_5_Direct() {
        setPrivateField("gamesP1", 7); setPrivateField("gamesP2", 5);
        invokePrivateMethod("checkSetPoint");
        int[] sets = (int[]) getPrivateField("setsP1");
        assertEquals(7, sets[0]);
    }
    
    @Test
    public void testSetWin_5_7_Direct() {
        setPrivateField("gamesP1", 5); setPrivateField("gamesP2", 7);
        invokePrivateMethod("checkSetPoint");
        int[] sets = (int[]) getPrivateField("setsP2");
        assertEquals(7, sets[0]);
    }

    @Test
    public void testSetWin_6_7_Direct() {
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 7);
        invokePrivateMethod("checkSetPoint");
        int[] sets = (int[]) getPrivateField("setsP2");
        assertEquals(7, sets[0]);
    }
    
    @Test
    public void testSetNoWin_6_5() {
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 5);
        invokePrivateMethod("checkSetPoint");
        assertEquals(1, getPrivateField("currentSet"));
    }

    // =========================================================================
    // 4. TieBreak (Logic & Bug Accommodation)
    // =========================================================================

    @Test
    public void testTieBreak_Activation() {
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 5);
        setPrivateField("scoreP2", 3);
        manager.pointScored(2); // 6-6
        assertTrue((boolean) getPrivateField("isTieBreak"));
        assertTrue(outContent.toString().contains("INIZIO TIE-BREAK"));
    }
    
    @Test
    public void testTieBreak_PointScored_Routing() {
        setPrivateField("isTieBreak", true);
        manager.pointScored(1);
        assertEquals(1, getPrivateField("scoreP1"));
    }

    @Test
    public void testTieBreak_Win_P1_Bugged() {
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 6); setPrivateField("scoreP2", 0);
        manager.pointScored(1); // 7-0
        
        // EXPECT 0 invece di 7 a causa del bug (resetGame chiamato prima di checkSet)
        int[] sets = (int[]) getPrivateField("setsP1");
        assertEquals(0, sets[0]);
    }
    
    @Test
    public void testTieBreak_Win_P2_Extended_Bugged() {
        setPrivateField("isTieBreak", true);
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 6);
        setPrivateField("scoreP1", 8); setPrivateField("scoreP2", 9);
        manager.pointScored(2); // 8-10
        
        int[] sets = (int[]) getPrivateField("setsP2");
        assertEquals(0, sets[0]);
    }
    
    @Test
    public void testTieBreak_NoWin() {
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 6); setPrivateField("scoreP2", 5);
        manager.pointScored(2); // 6-6
        assertTrue((boolean) getPrivateField("isTieBreak"));
    }

    // =========================================================================
    // 5. Loops, Formatting & GameOver (High Coverage Area)
    // =========================================================================

    @Test
    public void testIsGameOver_P1_Msg() {
        int[] s1 = {6, 6, 6, 0, 0};
        setPrivateField("setsP1", s1);
        setPrivateField("currentSet", 4);
        
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("VINTA DAL GIOCATORE 1"));
    }
    
    @Test
    public void testIsGameOver_P2_Msg() {
        int[] s2 = {6, 6, 6, 0, 0};
        setPrivateField("setsP2", s2);
        setPrivateField("currentSet", 4);
        
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("VINTA DAL GIOCATORE 2"));
    }
    
    @Test
    public void testIsGameOver_False_Loop() {
        int[] s1 = {6, 6, 0, 0, 0};
        setPrivateField("setsP1", s1);
        setPrivateField("currentSet", 3);
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testGetMatchScore_Detailed() {
        int[] s1 = {6, 4, 0, 0, 0};
        int[] s2 = {4, 6, 0, 0, 0};
        setPrivateField("setsP1", s1);
        setPrivateField("setsP2", s2);
        setPrivateField("currentSet", 3);
        setPrivateField("gamesP1", 2);
        
        String score = manager.getMatchScore();
        assertTrue(score.contains("1-1"));
        assertTrue(score.contains("Game: 2-0"));
    }
    
    @Test
    public void testGetMatchScore_TieBreakString() {
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 5); setPrivateField("scoreP2", 4);
        String score = manager.getMatchScore();
        assertTrue(score.contains("TIE-BREAK: 5-4"));
    }

    @Test
    public void testPrintScore_FullCoverage_Loops() {
        // Setup per coprire virgole e rami ternari nella stampa
        int[] s1 = {6, 6, 6, 0, 0};
        int[] s2 = {4, 4, 4, 0, 0};
        setPrivateField("setsP1", s1);
        setPrivateField("setsP2", s2);
        setPrivateField("currentSet", 4); 
        
        outContent.reset();
        manager.printScore();
        String output = outContent.toString();
        
        assertTrue(output.contains("6, 6, 6"));
        assertTrue(output.contains("4, 4, 4"));
        assertTrue(output.contains("Set Corrente (4)"));
    }
}