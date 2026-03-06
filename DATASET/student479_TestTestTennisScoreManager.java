import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestTennisScoreManager {

    private TennisScoreManager scoreManager;

    // --- Utility per simulare un numero di punti/game rapidi ---

    /**
     * Simula un punto segnato dal giocatore specificato.
     */
    private void scorePoint(int player) {
        scoreManager.pointScored(player);
    }

    /**
     * Simula la vittoria di un Game da parte del giocatore specificato.
     */
    private void winGame(int player) {
        for (int i = 0; i < 4; i++) {
            scorePoint(player);
        }
    }

    /**
     * Simula la vittoria di un Tie-Break da parte del giocatore specificato (es. 7-5)
     * Richiede che sia già in Tie-Break.
     */
    private void winTieBreak(int player) {
        // Portare a 6-6
        for (int i = 0; i < 6; i++) {
            scorePoint(1);
            scorePoint(2);
        }
        // Portare a 8-6 (vittoria con margine di 2)
        scorePoint(player);
        scorePoint(player);
    }
    
    // --- Setup ---
    
    @BeforeEach
    void setUp() {
        scoreManager = new TennisScoreManager();
    }

    // =================================================================
    // SCENARIO 1: Test del Game Standard (Copertura P3.2, P3.3, P3.4)
    // =================================================================

    @Test
    void testInitialScore() {
        assertEquals("Love-Love", scoreManager.getGameScore(), "Il punteggio iniziale deve essere Love-Love.");
    }

    @Test
    void testStandardGameScore() {
        scorePoint(1); // 15-Love
        assertEquals("15-Love", scoreManager.getGameScore(), "P1 15");
        scorePoint(2); // 15-15
        assertEquals("15-15", scoreManager.getGameScore(), "15 pari");
        scorePoint(1); // 30-15
        assertEquals("30-15", scoreManager.getGameScore(), "P1 30");
        scorePoint(2); // 30-30
        assertEquals("30-30", scoreManager.getGameScore(), "30 pari");
    }

    @Test
    void testDeuceAndAdvantage() {
        // 40-40 (Simula i punti fino a 3-3)
        winGame(1); // 1-0 Game, resetta.
        scoreManager.resetGameAndPoints(); // Torniamo a 0-0 game
        for (int i = 0; i < 3; i++) {
            scorePoint(1);
            scorePoint(2);
        }
        assertEquals("Deuce", scoreManager.getGameScore(), "Punteggio 3-3 deve essere Deuce");

        // Vantaggio P1
        scorePoint(1); // 4-3
        assertEquals("Vantaggio P1", scoreManager.getGameScore(), "Punteggio 4-3 deve essere Vantaggio P1");
        
        // Ritorno a Deuce
        scorePoint(2); // 4-4
        assertEquals("Deuce", scoreManager.getGameScore(), "Punteggio 4-4 deve essere Deuce");
    }
    
    @Test
    void testWinGameAfterDeuce() {
        // Simula 4-4 (Deuce)
        for (int i = 0; i < 3; i++) {
            scorePoint(1);
            scorePoint(2);
        }
        
        // Vantaggio P2
        scorePoint(2); // 3-4
        // P2 vince il game
        scorePoint(2); // 3-5 -> Game P2
        
        // Verifica vittoria Game (P2)
        assertEquals(0, scoreManager.gamesP1, "Game P1 deve essere 0");
        assertEquals(1, scoreManager.gamesP2, "Game P2 deve essere 1");
        assertEquals("Love-Love", scoreManager.getGameScore(), "Punti devono essere resettati");
    }

    // =================================================================
    // SCENARIO 2: Test del Bug di Vantaggio P2 (Copertura P3.5 BUG)
    // =================================================================

    @Test
    void testBugAdvantageP2Score() {
        // Simula 4-4 (Deuce)
        for (int i = 0; i < 3; i++) {
            scorePoint(1);
            scorePoint(2);
        }
        
        // Vantaggio P2
        scorePoint(2); // 3-4
        
        // A causa del bug nella condizione 'scoreP2 == scoreP2 + 1', l'output atteso è l'errore.
        // La condizione 'scoreP2 >= 3 && scoreP2 == scoreP1 + 1' è quella corretta.
        // Testiamo il comportamento attuale del codice (che fallisce il ramo "Vantaggio P2")
        assertNotEquals("Vantaggio P2", scoreManager.getGameScore(), 
            "A causa del bug 'scoreP2 == scoreP2 + 1', non viene mostrato 'Vantaggio P2'");
        assertEquals("Errore Game", scoreManager.getGameScore(), 
            "Il bug fa sì che la logica cada nel ramo 'Errore Game'");
        
        // Nota: se il bug fosse corretto a 'scoreP2 == scoreP1 + 1', l'assert atteso sarebbe "Vantaggio P2"
    }

    // =================================================================
    // SCENARIO 3: Test del Set Standard (Copertura L1, L2, L5, L6)
    // =================================================================

    @Test
    void testWinSetStandard6_4() {
        // Simula 6-4 per P1
        for (int i = 0; i < 6; i++) {
            winGame(1); // P1 vince 6 game
        }
        for (int i = 0; i < 4; i++) {
            winGame(2); // P2 vince 4 game
        }
        
        // Set Point
        winGame(1); // P1 vince il 7° game (7-4, in realtà è 6-4, il 7° game non viene giocato)
        
        // Simula 5-4. P1 vince il game (6-4)
        scoreManager.resetGameAndPoints();
        for(int i=0; i<5; i++) winGame(1);
        for(int i=0; i<4; i++) winGame(2);
        winGame(1); // P1 vince il set 6-4
        
        // Verifica
        assertEquals(2, scoreManager.currentSet, "Dopo 6-4, si passa al Set 2");
        assertEquals(6, scoreManager.setsP1[0], "P1 ha vinto il Set 1 con 6 game");
        assertEquals(4, scoreManager.setsP2[0], "P2 ha perso il Set 1 con 4 game");
    }

    @Test
    void testWinSetTight7_5() {
        // Simula 5-5
        for (int i = 0; i < 5; i++) {
            winGame(1);
            winGame(2);
        }
        
        // P1 vince 6-5 (il set NON è finito)
        winGame(1);
        assertEquals(1, scoreManager.currentSet, "A 6-5, il set deve continuare");
        
        // P1 vince 7-5 (il set è finito L2)
        winGame(1);
        assertEquals(2, scoreManager.currentSet, "A 7-5, si passa al Set 2");
        assertEquals(7, scoreManager.setsP1[0], "P1 ha vinto 7-5");
        assertEquals(5, scoreManager.setsP2[0], "P2 ha perso 7-5");
    }

    // =================================================================
    // SCENARIO 4: Test del Tie-Break (Copertura P5.1, T1, T2, L7)
    // =================================================================

    @Test
    void testEnterTieBreak() {
        // Simula 6-6
        for (int i = 0; i < 6; i++) {
            winGame(1);
            winGame(2);
        }
        
        // P1 vince l'ultimo game (il sistema chiama checkSetPoint e trova 6-6)
        winGame(1); // Ultimo punto per P1 che porta il game a 40-Love, vince il game 6-6
        
        assertTrue(scoreManager.isTieBreak, "A 6-6 deve attivarsi il Tie-Break");
        assertEquals(0, scoreManager.scoreP1, "Punti devono essere resettati all'inizio del TB");
        assertEquals(0, scoreManager.scoreP2, "Punti devono essere resettati all'inizio del TB");
    }

    @Test
    void testWinTieBreak7_6() {
        // Simula 6-6 + Entrata in TB
        for (int i = 0; i < 6; i++) {
            winGame(1);
            winGame(2);
        }
        winGame(1); 
        
        // Punteggio Tie-Break 6-6 (TB continua T4)
        for (int i = 0; i < 6; i++) {
            scorePoint(1);
            scorePoint(2);
        }
        assertEquals("TIE-BREAK: 6-6", scoreManager.getTieBreakScore());
        
        // P1 7-6 (TB continua T2)
        scorePoint(1);
        assertEquals("TIE-BREAK: 7-6", scoreManager.getTieBreakScore());
        
        // P1 8-6 (TB vinto T1)
        scorePoint(1);
        
        // Verifica vittoria Set 7-6
        assertFalse(scoreManager.isTieBreak, "Il Tie-Break deve essere disattivato");
        assertEquals(2, scoreManager.currentSet, "Dopo 7-6, si passa al Set 2");
        assertEquals(7, scoreManager.setsP1[0], "P1 ha vinto il Set 7-6");
        assertEquals(6, scoreManager.setsP2[0], "P2 ha perso il Set 7-6");
    }

    // =================================================================
    // SCENARIO 5: Test del Match End e Error Handling (Copertura P1.1, P1.4, M1)
    // =================================================================
    
    @Test
    void testWinMatch3_0() {
        // Simula vittoria Set 1 (6-4)
        for(int i=0; i<5; i++) winGame(1);
        for(int i=0; i<4; i++) winGame(2);
        winGame(1); // Set 1 (6-4)

        // Simula vittoria Set 2 (6-0)
        for(int i=0; i<6; i++) winGame(1); // Set 2 (6-0)

        // Simula vittoria Set 3 (6-2)
        for(int i=0; i<6; i++) winGame(1);
        for(int i=0; i<2; i++) winGame(2);
        winGame(1); // Set 3 (6-2)

        assertTrue(scoreManager.isGameOver(), "P1 deve aver vinto la partita (3-0 set)");
        assertTrue(scoreManager.getMatchScore().contains("3 Set"), "Punteggio finale deve mostrare la vittoria");
    }

    @Test
    void testPointScoredAfterGameOver() {
        // Simula la fine del match (3-0)
        winGame(1); scoreManager.checkSetPoint(); // Simula un set vinto (per resettare)
        scoreManager.setsP1[0] = 6; scoreManager.setsP2[0] = 4;
        scoreManager.setsP1[1] = 6; scoreManager.setsP2[1] = 0;
        scoreManager.setsP1[2] = 6; scoreManager.setsP2[2] = 2;
        scoreManager.currentSet = 4; // Imposta lo stato a 3 set vinti da P1
        assertTrue(scoreManager.isGameOver());

        // Tenta di segnare un punto (Test P1.1)
        scoreManager.pointScored(1); 

        // Verifica che lo stato non sia cambiato (i game/punti devono essere 0 perché resettati all'inizio del set 4)
        assertEquals(0, scoreManager.gamesP1, "Game P1 non deve cambiare dopo Game Over");
        assertEquals(0, scoreManager.scoreP1, "Score P1 non deve cambiare dopo Game Over");
    }

    @Test
    void testInvalidPlayerInput() {
        // Tenta di segnare con giocatore non valido (Test P1.4)
        scoreManager.pointScored(0);
        scoreManager.pointScored(3);

        // Verifica che non ci siano cambiamenti
        assertEquals(0, scoreManager.scoreP1, "Score P1 non deve cambiare con input non valido");
        assertEquals(0, scoreManager.scoreP2, "Score P2 non deve cambiare con input non valido");
    }
}