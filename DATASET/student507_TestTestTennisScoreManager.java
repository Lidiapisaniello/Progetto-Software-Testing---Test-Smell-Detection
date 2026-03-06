import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    // --- Helper Methods per mantenere i test atomici e leggibili ---
    
    /**
     * Simula la vittoria di un game standard (a 0) per un giocatore.
     */
    private void winGame(TennisScoreManager sm, int player) {
        for (int i = 0; i < 4; i++) {
            sm.pointScored(player);
        }
    }

    /**
     * Simula la vittoria di un set (6-0) per un giocatore.
     */
    private void winSet(TennisScoreManager sm, int player) {
        for (int i = 0; i < 6; i++) {
            winGame(sm, player);
        }
    }

    // --- Test Cases ---

    @Test
    public void pointScoredP1IncrementsScoreTest() {
        TennisScoreManager sm = new TennisScoreManager();
        sm.pointScored(1);
        assertEquals("15-Love", sm.getGameScore());
    }

    @Test
    public void pointScoredP2IncrementsScoreTest() {
        TennisScoreManager sm = new TennisScoreManager();
        sm.pointScored(2);
        assertEquals("Love-15", sm.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        TennisScoreManager sm = new TennisScoreManager();
        // Non deve lanciare eccezioni e il punteggio non deve cambiare
        sm.pointScored(99); 
        assertEquals("Love-Love", sm.getGameScore());
    }

    @Test
    public void getGameScoreDeuceTest() {
        TennisScoreManager sm = new TennisScoreManager();
        // 3 punti a testa (40-40)
        sm.pointScored(1); sm.pointScored(1); sm.pointScored(1);
        sm.pointScored(2); sm.pointScored(2); sm.pointScored(2);
        
        assertEquals("Deuce", sm.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Arriviamo a Deuce
        sm.pointScored(1); sm.pointScored(1); sm.pointScored(1);
        sm.pointScored(2); sm.pointScored(2); sm.pointScored(2);
        // Vantaggio P1
        sm.pointScored(1);
        
        assertEquals("Vantaggio P1", sm.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Arriviamo a Deuce
        sm.pointScored(1); sm.pointScored(1); sm.pointScored(1);
        sm.pointScored(2); sm.pointScored(2); sm.pointScored(2);
        // Vantaggio P2
        sm.pointScored(2);
        
        // NOTA: Nel codice sorgente c'è un BUG: "scoreP2 == scoreP2 + 1" è impossibile.
        // Pertanto il codice cade nel return finale "Errore Game" invece di "Vantaggio P2".
        // Il test verifica il comportamento ATTUALE del codice fornito.
        assertEquals("Errore Game", sm.getGameScore());
    }

    @Test
    public void pointScoredWinGameP1Test() {
        TennisScoreManager sm = new TennisScoreManager();
        winGame(sm, 1); // P1 vince il game
        // Verifica tramite getMatchScore che P1 ha 1 game e 0 per P2
        assertTrue(sm.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", sm.getGameScore()); // Punti resettati
    }

    @Test
    public void pointScoredWinGameP2Test() {
        TennisScoreManager sm = new TennisScoreManager();
        winGame(sm, 2); // P2 vince il game
        assertTrue(sm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void checkSetPointWinSetP1Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // P1 vince 6 game di fila
        winSet(sm, 1);
        
        // Dovrebbe aver vinto il set 1 e ora essere nel set 2
        // Match score dovrebbe mostrare 1 set a 0
        assertTrue(sm.getMatchScore().contains("1-0"));
        assertTrue(sm.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void checkSetPointWinSetP2Test() {
        TennisScoreManager sm = new TennisScoreManager();
        winSet(sm, 2);
        assertTrue(sm.getMatchScore().contains("0-1"));
    }

    @Test
    public void checkSetPointTieBreakTriggerTest() {
        TennisScoreManager sm = new TennisScoreManager();
        // P1 vince 5 games
        for(int i=0; i<5; i++) winGame(sm, 1);
        // P2 vince 6 games (0-5 -> 6-5)
        for(int i=0; i<6; i++) winGame(sm, 2);
        // P1 vince 1 game (6-6) -> Trigger TieBreak
        winGame(sm, 1);
        
        assertTrue(sm.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void pointScoredTieBreakWinP1Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Setup 6-6
        for(int i=0; i<5; i++) winGame(sm, 1);
        for(int i=0; i<6; i++) winGame(sm, 2);
        winGame(sm, 1); 
        
        // Ora siamo nel TieBreak. P1 fa 7 punti (7-0)
        for(int i=0; i<7; i++) {
            sm.pointScored(1);
        }
        
        // P1 dovrebbe aver vinto il set
        assertTrue(sm.getMatchScore().contains("1-0"));
    }

    @Test
    public void pointScoredTieBreakWinP2Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Setup 6-6
        for(int i=0; i<5; i++) winGame(sm, 1);
        for(int i=0; i<6; i++) winGame(sm, 2);
        winGame(sm, 1); 
        
        // TieBreak: P2 vince 7-0
        for(int i=0; i<7; i++) {
            sm.pointScored(2);
        }
        
        assertTrue(sm.getMatchScore().contains("0-1"));
    }
    
    @Test
    public void checkSetPointWinExtendedSetP1Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Simuliamo 5-5
        for(int i=0; i<5; i++) winGame(sm, 1);
        for(int i=0; i<5; i++) winGame(sm, 2);
        
        // P1 va a 6-5
        winGame(sm, 1);
        // P1 va a 7-5 (Vince il set senza tiebreak)
        winGame(sm, 1);
        
        assertTrue(sm.getMatchScore().contains("1-0"));
    }

    @Test
    public void checkSetPointWinExtendedSetP2Test() {
        TennisScoreManager sm = new TennisScoreManager();
        // Simuliamo 5-5
        for(int i=0; i<5; i++) winGame(sm, 1);
        for(int i=0; i<5; i++) winGame(sm, 2);
        
        // P2 va a 5-6
        winGame(sm, 2);
        // P2 va a 5-7 (Vince il set)
        winGame(sm, 2);
        
        assertTrue(sm.getMatchScore().contains("0-1"));
    }

    @Test
    public void isGameOverP1WinsTest() {
        TennisScoreManager sm = new TennisScoreManager();
        
        // P1 vince 3 Set
        winSet(sm, 1); // Set 1
        winSet(sm, 1); // Set 2
        winSet(sm, 1); // Set 3
        
        assertTrue(sm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", sm.getMatchScore());
        assertEquals("PARTITA FINITA", sm.getGameScore());
    }

    @Test
    public void isGameOverP2WinsTest() {
        TennisScoreManager sm = new TennisScoreManager();
        
        winSet(sm, 2);
        winSet(sm, 2);
        winSet(sm, 2);
        
        assertTrue(sm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", sm.getMatchScore());
    }

    @Test
    public void pointScoredAfterGameOverTest() {
        TennisScoreManager sm = new TennisScoreManager();
        // Vinci la partita
        winSet(sm, 1);
        winSet(sm, 1);
        winSet(sm, 1);
        
        // Prova a segnare ancora
        sm.pointScored(1);
        
        // Il match score deve rimanere quello finale
        assertEquals("P1: 3 Set | P2: 0 Set", sm.getMatchScore());
    }
    
    @Test
    public void getMatchScoreMixedResultTest() {
        TennisScoreManager sm = new TennisScoreManager();
        // Set 1: P1 vince
        winSet(sm, 1);
        // Set 2: P2 vince
        winSet(sm, 2);
        
        String score = sm.getMatchScore();
        // Ci aspettiamo 1-1 nei set
        assertTrue(score.startsWith("1-1"));
    }
}