/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: cri.dibenedetto@studenti.unina.it
UserID: 1093
Date: 25/11/2025
*/

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        System.setOut(new PrintStream(outContent)); // Cattura System.out
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut); // Ripristina console
    }

    // ==========================================
    // HELPER METHODS (REFLECTION)
    // ==========================================
    
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }
    
    private Object getPrivateField(String fieldName) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(manager);
    }

    private void setPrivateArrayElement(String arrayName, int index, int value) throws Exception {
        Field field = TennisScoreManager.class.getDeclaredField(arrayName);
        field.setAccessible(true);
        int[] array = (int[]) field.get(manager);
        array[index] = value;
    }

    private void invokeMethod(String methodName) throws Exception {
        Method method = TennisScoreManager.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(manager);
    }

    // ==========================================
    // 1. BRANCH COVERAGE BOOSTER (CRITICO PER IL 100%)
    // ==========================================

    @Test
    public void coverageBoosterPrintLoopsTest() throws Exception {
        // Copre i rami dei cicli for e l'operatore ternario delle virgole nella stampa
        
        // CASO 1: Loop virgola attivato (i < currentSet - 2)
        // Impostiamo Set 3, così il loop corre per i=0. 0 < (3-2) è Vero -> Stampa virgola
        setPrivateField("currentSet", 3);
        setPrivateArrayElement("setsP1", 0, 6);
        setPrivateArrayElement("setsP1", 1, 6);
        setPrivateArrayElement("setsP2", 0, 4);
        setPrivateArrayElement("setsP2", 1, 2);
        
        manager.printScore();
        
        String log = outContent.toString();
        // Verifica che la virgola sia stata stampata
        assertTrue("Dovrebbe stampare la virgola per la formattazione dei set", log.contains(","));
    }

    @Test
    public void checkSetPointComplexBranchesTest() throws Exception {
        // Copre le condizioni OR (A || B || C) in checkSetPoint
        
        // Ramo: (gamesP1 == 7 && gamesP2 == 5)
        setPrivateField("gamesP1", 7); 
        setPrivateField("gamesP2", 5);
        invokeMethod("checkSetPoint");
        
        // CORREZIONE ERRORE PRECEDENTE: SetsP1 diventa gamesP1 (cioè 7)
        int[] sets = (int[]) getPrivateField("setsP1");
        assertEquals(7, sets[0]); 
    }
    
    @Test
    public void checkSetPointP2ComplexBranchesTest() throws Exception {
        // Ramo: (gamesP2 == 7 && gamesP1 == 5)
        setPrivateField("gamesP1", 5); 
        setPrivateField("gamesP2", 7);
        invokeMethod("checkSetPoint");
        
        assertTrue(outContent.toString().contains("Vinto da P2"));
        int[] sets = (int[]) getPrivateField("setsP2");
        assertEquals(7, sets[0]);
    }

    @Test
    public void checkSetPointNoWinTest() throws Exception {
        // Ramo: Nessuno vince (Else implicito)
        setPrivateField("gamesP1", 5); 
        setPrivateField("gamesP2", 5);
        invokeMethod("checkSetPoint");
        
        // Non deve succedere nulla, currentSet rimane 1
        assertEquals(1, (int) getPrivateField("currentSet")); 
    }

    @Test
    public void getMatchScoreLoopTest() throws Exception {
        // Copre i cicli in getMatchScore
        setPrivateField("currentSet", 3);
        setPrivateArrayElement("setsP1", 0, 6); 
        setPrivateArrayElement("setsP2", 0, 4); // P1 vince set 1
        
        setPrivateArrayElement("setsP1", 1, 4); 
        setPrivateArrayElement("setsP2", 1, 6); // P2 vince set 2
        
        String score = manager.getMatchScore();
        assertTrue(score.contains("1-1"));
    }

    // ==========================================
    // 2. TEST BASE & RESET
    // ==========================================

    @Test
    public void initTest() {
        assertNotNull(manager);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void resetPointsTest() throws Exception {
        setPrivateField("scoreP1", 3);
        manager.resetPoints();
        assertEquals(0, (int) getPrivateField("scoreP1"));
    }

    @Test
    public void resetGameAndPointsTest() throws Exception {
        setPrivateField("gamesP1", 4);
        setPrivateField("isTieBreak", true);
        manager.resetGameAndPoints();
        assertEquals(0, (int) getPrivateField("gamesP1"));
        assertFalse((boolean) getPrivateField("isTieBreak"));
    }

    // ==========================================
    // 3. TEST INPUT & OUTPUT
    // ==========================================

    @Test
    public void pointScoredTests() throws Exception {
        manager.pointScored(1);
        assertEquals(1, (int) getPrivateField("scoreP1"));
        
        manager.pointScored(2);
        assertEquals(1, (int) getPrivateField("scoreP2"));
        
        manager.pointScored(3); // Invalid
        assertTrue(outContent.toString().contains("Errore"));
    }
    
    @Test
    public void pointScoredGameOverTest() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6);
        setPrivateArrayElement("setsP1", 1, 6);
        setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        
        manager.pointScored(1);
        assertTrue(outContent.toString().contains("La partita è finita"));
    }

    // ==========================================
    // 4. TEST GAME LOGIC (WIN & ADVANTAGE)
    // ==========================================

    @Test
    public void getGameScoreTests() throws Exception {
        setPrivateField("scoreP1", 1); setPrivateField("scoreP2", 2);
        assertEquals("15-30", manager.getGameScore());

        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 3);
        assertEquals("Deuce", manager.getGameScore());
        
        setPrivateField("scoreP1", 4); setPrivateField("scoreP2", 3);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // BUG SOURCE CODE COVERAGE
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 4);
        assertEquals("Errore Game", manager.getGameScore());
        
        // Game Over
        setPrivateArrayElement("setsP1", 0, 6); setPrivateArrayElement("setsP1", 1, 6); setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }
    
    @Test
    public void checkGamePointP1WinsTest() throws Exception {
        setPrivateField("scoreP1", 3); setPrivateField("scoreP2", 0);
        manager.pointScored(1);
        assertEquals(1, (int) getPrivateField("gamesP1"));
    }

    @Test
    public void checkGamePointP2WinsTest() throws Exception {
        setPrivateField("scoreP1", 0); setPrivateField("scoreP2", 3);
        manager.pointScored(2);
        assertEquals(1, (int) getPrivateField("gamesP2"));
    }

    @Test
    public void checkGamePointNoWinTest() throws Exception {
        // Nessuno vince (Else branch coverage)
        // CORREZIONE: Resettiamo esplicitamente i game a 0 per evitare sporcizia
        setPrivateField("gamesP1", 0); 
        setPrivateField("scoreP1", 0); 
        setPrivateField("scoreP2", 0);
        
        manager.pointScored(1); // 15-0
        assertEquals(0, (int) getPrivateField("gamesP1")); // I game devono restare 0
    }

    // ==========================================
    // 5. TEST SET & TIE BREAK
    // ==========================================

    @Test
    public void tieBreakLogicTest() throws Exception {
        // Activation
        setPrivateField("gamesP1", 6); setPrivateField("gamesP2", 6);
        invokeMethod("checkSetPoint");
        assertTrue((boolean) getPrivateField("isTieBreak"));
        
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
        
        // No Win Check (Else branch)
        setPrivateField("scoreP1", 1); setPrivateField("scoreP2", 0);
        invokeMethod("checkTieBreakPoint");
        assertTrue((boolean) getPrivateField("isTieBreak")); // Ancora attivo
        
        // P1 Wins Tie Break
        setPrivateField("scoreP1", 6); setPrivateField("scoreP2", 0);
        manager.pointScored(1);
        assertEquals(0, (int) getPrivateField("gamesP1")); // Bug reset
        assertFalse((boolean) getPrivateField("isTieBreak"));
    }
    
    @Test
    public void tieBreakP2WinsTest() throws Exception {
        setPrivateField("isTieBreak", true);
        setPrivateField("scoreP1", 0); setPrivateField("scoreP2", 6);
        manager.pointScored(2);
        assertFalse((boolean) getPrivateField("isTieBreak"));
    }

    @Test
    public void setWinsP1StandardTest() throws Exception {
        setPrivateField("gamesP1", 6); 
        setPrivateField("gamesP2", 4);
        invokeMethod("checkSetPoint");
        assertEquals(6, ((int[])getPrivateField("setsP1"))[0]);
    }
    
    @Test
    public void setWinsP2StandardTest() throws Exception {
        setPrivateField("gamesP1", 4); 
        setPrivateField("gamesP2", 6);
        invokeMethod("checkSetPoint");
        assertEquals(6, ((int[])getPrivateField("setsP2"))[0]);
    }

    // ==========================================
    // 6. MATCH OVER & UTILS
    // ==========================================

    @Test
    public void isGameOverP1Test() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6); 
        setPrivateArrayElement("setsP1", 1, 6); 
        setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("VINTA DAL GIOCATORE 1"));
    }

    @Test
    public void isGameOverP2Test() throws Exception {
        setPrivateArrayElement("setsP2", 0, 6); 
        setPrivateArrayElement("setsP2", 1, 6); 
        setPrivateArrayElement("setsP2", 2, 6);
        setPrivateField("currentSet", 4);
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("VINTA DAL GIOCATORE 2"));
    }
    
    @Test
    public void moveToNextSetBlocked() throws Exception {
        setPrivateArrayElement("setsP1", 0, 6); setPrivateArrayElement("setsP1", 1, 6); setPrivateArrayElement("setsP1", 2, 6);
        setPrivateField("currentSet", 4);
        invokeMethod("moveToNextSet");
        assertEquals(4, (int) getPrivateField("currentSet"));
    }
}