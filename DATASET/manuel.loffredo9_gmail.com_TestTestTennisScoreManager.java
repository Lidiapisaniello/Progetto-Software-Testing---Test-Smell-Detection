/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Manuel"
Cognome: "Loffredo"
Username: manuel.loffredo9@gmail.com
UserID: 1419
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
    
    private TennisScoreManager manager;

    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima dell'inizio dei test nella classe
        System.out.println("Inizio Test Suite TennisScoreManager");
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
        System.out.println("Fine Test Suite TennisScoreManager");
    }

    @Before
    public void setUp() {
        // Inizializza una nuova istanza prima di ogni test per garantire isolamento
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        // Pulizia (non strettamente necessaria qui dato che ricreiamo l'oggetto, ma buona prassi)
        manager = null;
    }

    // --- Helper Methods per facilitare i test ---
    
    // Fa segnare N punti consecutivi a un giocatore
    private void scorePoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    // Fa vincere un game standard a un giocatore (4 punti a 0)
    private void winStandardGame(int player) {
        scorePoints(player, 4);
    }

    // Porta il set sul punteggio specificato (es. 5-5)
    private void playToSetScore(int gamesP1, int gamesP2) {
        // Reset iniziale implicito
        int currentP1 = 0;
        int currentP2 = 0;
        
        while (currentP1 < gamesP1 || currentP2 < gamesP2) {
            if (currentP1 < gamesP1) {
                winStandardGame(1);
                currentP1++;
            }
            if (currentP2 < gamesP2) {
                winStandardGame(2);
                currentP2++;
            }
        }
    }

    // --- TEST CASES ---

    @Test
    public void testInitialState() {
        // Verifica lo stato iniziale: 0-0 (Love-Love)
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testBasicScoreProgression() {
        // P1: 15, P2: 0
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());

        // P1: 30, P2: 0
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());

        // P1: 40, P2: 0
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayerInput() {
        // Test input invalido (non 1 o 2)
        String scoreBefore = manager.getGameScore();
        manager.pointScored(3); 
        manager.pointScored(-1);
        // Il punteggio non deve cambiare
        assertEquals(scoreBefore, manager.getGameScore());
    }

    @Test
    public void testWinGameP1() {
        // P1 vince a zero
        scorePoints(1, 3); // 40-0
        assertEquals("40-Love", manager.getGameScore());
        manager.pointScored(1); // Vince il game
        
        // Il punteggio punti si resetta, game P1 incrementa
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameP2() {
        // P2 vince a zero
        scorePoints(2, 4);
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testDeuce() {
        // Arriviamo a 40-40 (Deuce)
        scorePoints(1, 3);
        scorePoints(2, 3);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1AndWin() {
        // Deuce
        scorePoints(1, 3);
        scorePoints(2, 3);
        
        // Vantaggio P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // P1 vince il game
        manager.pointScored(1);
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testBackToDeuce() {
        // Deuce -> Vantaggio P1 -> Deuce
        scorePoints(1, 3);
        scorePoints(2, 3);
        
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(2); // Torna Deuce (P1=4, P2=4)
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugBehavior() {
        /* * NOTA: Nel codice sorgente fornito c'è un bug nella riga:
         * "if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)"
         * Questa condizione è impossibile. Quindi "Vantaggio P2" non viene mai restituito.
         * Il codice fallisce nell'else finale restituendo "Errore Game".
         * Testiamo questo comportamento per garantire che il test passi con il codice attuale.
         */
        
        // Arriva a Deuce
        scorePoints(1, 3);
        scorePoints(2, 3);
        
        // P2 segna (P1=3, P2=4). Dovrebbe essere Vantaggio P2.
        manager.pointScored(2);
        
        // A causa del bug, ci aspettiamo "Errore Game"
        assertEquals("Errore Game", manager.getGameScore());
        
        // Tuttavia, se P2 segna ancora, deve vincere il game
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testSetWinStandard() {
        // P1 vince 6-0
        for (int i = 0; i < 5; i++) {
            winStandardGame(1);
        }
        // Siamo 5-0
        assertTrue(manager.getMatchScore().contains("Game: 5-0"));
        
        // Vince 6° game -> Vince Set
        winStandardGame(1);
        
        // Verifica cambio set
        assertTrue(manager.getMatchScore().contains("P1: 1-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testSetWinExtended_7_5() {
        // P1 e P2 arrivano a 5-5
        playToSetScore(5, 5);
        assertTrue(manager.getMatchScore().contains("Game: 5-5"));

        // P1 va a 6-5 (non deve vincere il set ancora)
        winStandardGame(1);
        assertTrue(manager.getMatchScore().contains("Game: 6-5"));

        // P1 va a 7-5 -> Vince Set
        winStandardGame(1);
        
        // Il match score dovrebbe mostrare 1 set a 0 per P1 e reset giochi
        assertTrue(manager.getMatchScore().contains("P1: 1-0 (Game: 0-0 Love-Love)"));
    }
    
    @Test
    public void testSetWinExtended_P2_5_7() {
        // P1 e P2 arrivano a 5-5
        playToSetScore(5, 5);
        
        // P2 va a 5-6
        winStandardGame(2);
        
        // P2 va a 5-7 -> Vince Set
        winStandardGame(2);
        
        assertTrue(manager.getMatchScore().contains("P1: 0-1 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testTieBreakTrigger() {
        // Arrivare a 6-6
        playToSetScore(6, 6);
        
        // Verifica che siamo entrati in modalità Tie-Break
        String matchScore = manager.getMatchScore();
        assertTrue("Dovrebbe essere in Tie-Break", matchScore.contains("TIE-BREAK"));
        assertTrue(matchScore.contains("0-0"));
    }

    @Test
    public void testTieBreakWinStandard() {
        // Arrivare a 6-6
        playToSetScore(6, 6);
        
        // P1 vince tiebreak 7-0
        scorePoints(1, 7);
        
        // Verifica vittoria set (P1 vince 1 set)
        assertTrue(manager.getMatchScore().contains("P1: 1-0"));
    }

    @Test
    public void testTieBreakExtension() {
        // Arrivare a 6-6
        playToSetScore(6, 6);
        
        // Punteggio tiebreak 6-6
        scorePoints(1, 6);
        scorePoints(2, 6);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 6-6"));

        // P1 fa 7-6 (non vince ancora, serve scarto di 2)
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 7-6"));

        // P1 fa 8-6 -> Vince Set
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("P1: 1-0"));
    }
    
    @Test
    public void testTieBreakWinP2() {
         playToSetScore(6, 6);
         // P2 vince 7-0
         scorePoints(2, 7);
         assertTrue(manager.getMatchScore().contains("P1: 0-1"));
    }

    @Test
    public void testMatchWinP1() {
        // P1 deve vincere 3 set
        // Set 1
        playToSetScore(6, 0); 
        // Set 2
        playToSetScore(6, 0);
        // Set 3
        playToSetScore(6, 0);

        // Verifica fine partita
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set | P2: 0 Set"));
        
        // Verifica protezione post-partita (i punti non devono cambiare)
        String finalScore = manager.getMatchScore();
        manager.pointScored(1);
        assertEquals(finalScore, manager.getMatchScore());
    }

    @Test
    public void testMatchWinP2() {
        // P2 deve vincere 3 set
        // Set 1 (P2 vince)
        playToSetScore(0, 6); 
        // Set 2 (P2 vince)
        playToSetScore(0, 6);
        // Set 3 (P2 vince)
        playToSetScore(0, 6);

        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 0 Set | P2: 3 Set"));
    }

    @Test
    public void testMixedSetsMatch() {
        // Set 1: P1 vince
        playToSetScore(6, 0);
        
        // Set 2: P2 vince
        playToSetScore(0, 6);
        
        String score = manager.getMatchScore();
        assertTrue(score.contains("1-1"));
    }
    
    @Test
    public void testResetPointsLogic() {
        manager.pointScored(1); // 15-0
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testResetGameAndPointsLogic() {
        manager.pointScored(1);
        manager.resetGameAndPoints();
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
}