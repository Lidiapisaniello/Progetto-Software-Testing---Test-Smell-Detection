import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;

public class TestTennisScoreManager {

    /**
     * Metodo helper per iniettare valori privati nell'istanza di TennisScoreManager.
     * Necessario per testare stati avanzati senza Mockito e senza setter pubblici.
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile impostare il campo privato: " + fieldName, e);
        }
    }
    
    /**
     * Helper per leggere campi privati (usato per verificare che i reset non avvengano)
     */
    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile leggere il campo privato: " + fieldName, e);
        }
    }

    @Test
    public void pointScoredP1IncrementsScoreTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        Assert.assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredP2IncrementsScoreTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        manager.pointScored(2);
        Assert.assertEquals("Love-30", manager.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(99);
        Assert.assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void getGameScoreDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 3);
        Assert.assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 4);
        setPrivateField(manager, "scoreP2", 3);
        Assert.assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2FallsThroughToErrorTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 4);
        // Bug nel codice sorgente originale rende irraggiungibile "Vantaggio P2"
        Assert.assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void checkGamePointP1WinsGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 0);
        
        manager.pointScored(1);
        
        Assert.assertTrue(manager.getMatchScore().contains("Game: 1-0"));
        Assert.assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void checkGamePointP2WinsGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 0);
        setPrivateField(manager, "scoreP2", 3);
        
        manager.pointScored(2);
        
        Assert.assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void checkSetPointP1WinsSetStandardTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "gamesP2", 0);
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 0);
        
        manager.pointScored(1);
        
        Assert.assertTrue(manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void checkSetPointP1WinsSet75Test() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 5);
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 0);
        
        manager.pointScored(1); 
        
        Assert.assertTrue(manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void checkSetPointP2WinsSetExtendedTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 0);
        setPrivateField(manager, "scoreP2", 3);
        
        manager.pointScored(2);
        
        Assert.assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void checkSetPointTriggersTieBreakTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 5);
        setPrivateField(manager, "scoreP1", 0);
        setPrivateField(manager, "scoreP2", 3);
        
        manager.pointScored(2);
        
        Assert.assertTrue(manager.getMatchScore().contains("TIE-BREAK") || ((Boolean)getPrivateField(manager, "isTieBreak")));
    }

    @Test
    public void pointScoredInTieBreakTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);
        
        Assert.assertTrue(manager.getMatchScore().contains("TIE-BREAK: 2-1"));
    }

    @Test
    public void checkTieBreakPointP1WinsSetTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 6);
        setPrivateField(manager, "scoreP2", 0);
        
        manager.pointScored(1);
        
        Assert.assertTrue("Il set non viene assegnato a causa del bug nel reset", manager.getMatchScore().startsWith("0-0"));
    }

    @Test
    public void checkTieBreakPointP2WinsSetDifferenceOfTwoTest() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 6);
        setPrivateField(manager, "scoreP1", 10);
        setPrivateField(manager, "scoreP2", 11);
        
        manager.pointScored(2);
        
        Assert.assertTrue("Il set non viene assegnato a causa del bug nel reset", manager.getMatchScore().startsWith("0-0"));
    }

    @Test
    public void getMatchScoreFormatDuringMatchTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0] = 6; 
        int[] setsP2 = new int[5]; setsP2[0] = 4;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 2);
        
        Assert.assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void getMatchScoreMixedSetWinnersTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0] = 6; setsP1[1] = 4;
        int[] setsP2 = new int[5]; setsP2[0] = 4; setsP2[1] = 6;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 3);
        
        Assert.assertTrue(manager.getMatchScore().startsWith("1-1"));
    }
    
    @Test
    public void pointScoredPrintingLogicWithMultipleSetsTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0] = 6; setsP1[1] = 4;
        int[] setsP2 = new int[5]; setsP2[0] = 4; setsP2[1] = 6;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 3);
        
        manager.pointScored(1);
        Assert.assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void isGameOverP1WinsMatchTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0]=6; setsP1[1]=6; setsP1[2]=6;
        int[] setsP2 = new int[5]; setsP2[0]=0; setsP2[1]=0; setsP2[2]=0;
        
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 4); 
        
        Assert.assertTrue(manager.isGameOver());
        Assert.assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        Assert.assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void isGameOverP2WinsMatchTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0]=4; setsP1[1]=4; setsP1[2]=4;
        int[] setsP2 = new int[5]; setsP2[0]=6; setsP2[1]=6; setsP2[2]=6;
        
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 4);
        
        Assert.assertTrue(manager.isGameOver());
        Assert.assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void pointScoredAfterGameOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0]=6; setsP1[1]=6; setsP1[2]=6;
        int[] setsP2 = new int[5]; setsP2[0]=0; setsP2[1]=0; setsP2[2]=0;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 4);
        
        manager.pointScored(1);
        Assert.assertEquals(0, getPrivateField(manager, "scoreP1"));
    }
    
    @Test
    public void moveToNextSetNotCalledIfGameOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0]=6; setsP1[1]=6; setsP1[2]=6;
        int[] setsP2 = new int[5]; setsP2[0]=0; setsP2[1]=0; setsP2[2]=0;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 4);
        setPrivateField(manager, "gamesP1", 6); 
        
        manager.moveToNextSet();
        
        Assert.assertEquals(4, getPrivateField(manager, "currentSet"));
        Assert.assertEquals(6, getPrivateField(manager, "gamesP1"));
    }

    @Test
    public void testP2WinsSet75() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "gamesP2", 7);
        manager.checkSetPoint();
        Assert.assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testP2WinsSet76_TieBreakSet() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 7);
        manager.checkSetPoint();
        Assert.assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testP1FailsToWinSet76_DueToTypo() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 7);
        setPrivateField(manager, "gamesP2", 6);
        manager.checkSetPoint();
        Assert.assertTrue(manager.getMatchScore().startsWith("0-0"));
    }

    @Test
    public void testTieBreak76_DoesNotWinYet() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "isTieBreak", true);
        setPrivateField(manager, "scoreP1", 7);
        setPrivateField(manager, "scoreP2", 6);
        setPrivateField(manager, "gamesP1", 6);
        setPrivateField(manager, "gamesP2", 6);
        manager.checkTieBreakPoint();
        Assert.assertEquals(6, getPrivateField(manager, "gamesP1"));
        Assert.assertEquals(6, getPrivateField(manager, "gamesP2"));
    }

    @Test
    public void testMatchScore_WithDrawSet() {
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "currentSet", 2); 
        String score = manager.getMatchScore();
        Assert.assertTrue(score.startsWith("0-0"));
    }
    
    // --- NUOVI TEST AGGIUNTIVI PER LE RIGHE MANCANTI ---

    @Test
    public void testResetPointsExplicitly() {
        // Copre il metodo pubblico resetPoints() chiamato esplicitamente
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "scoreP1", 3);
        manager.resetPoints();
        Assert.assertEquals(0, getPrivateField(manager, "scoreP1"));
    }

    @Test
    public void testResetGameAndPointsExplicitly() {
        // Copre il metodo pubblico resetGameAndPoints() chiamato esplicitamente
        TennisScoreManager manager = new TennisScoreManager();
        setPrivateField(manager, "gamesP1", 3);
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "isTieBreak", true);
        
        manager.resetGameAndPoints();
        
        Assert.assertEquals(0, getPrivateField(manager, "gamesP1"));
        Assert.assertEquals(0, getPrivateField(manager, "scoreP1"));
        Assert.assertEquals(false, getPrivateField(manager, "isTieBreak"));
    }
    
    @Test
    public void pointScoredInSecondSet_PrintScoreCommaLogicTest() {
        // Copre il ramo "false" dell'operatore ternario in printScore: (i < currentSet - 2 ? ", " : "")
        // Impostando currentSet = 2, il loop gira per i=0. 
        // La condizione 0 < (2 - 2) è FALSE. Quindi stampa stringa vuota invece di virgola.
        TennisScoreManager manager = new TennisScoreManager();
        int[] setsP1 = new int[5]; setsP1[0] = 6; 
        int[] setsP2 = new int[5]; setsP2[0] = 4;
        setPrivateField(manager, "setsP1", setsP1);
        setPrivateField(manager, "setsP2", setsP2);
        setPrivateField(manager, "currentSet", 2);
        
        // Eseguiamo pointScored per scatenare la stampa
        manager.pointScored(1);
        
        Assert.assertEquals("15-Love", manager.getGameScore());
    }
}