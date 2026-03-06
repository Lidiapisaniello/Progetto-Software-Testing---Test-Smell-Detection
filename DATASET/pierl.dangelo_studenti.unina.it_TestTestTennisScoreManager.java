/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: pierl.dangelo@studenti.unina.it
UserID: 387
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    // --- Utility per simulare lo scorrimento del punteggio ---

    /**
     * Simula la vittoria di un punto (score++)
     */
    private void scorePoint(int player) {
        manager.pointScored(player);
    }

    /**
     * Simula la vittoria di N game per un giocatore.
     */
    private void scoreGames(int player, int gamesToWin) {
        for (int i = 0; i < gamesToWin; i++) {
            // Un game standard viene vinto 4-0 (4 punti di fila)
            for (int j = 0; j < 4; j++) {
                scorePoint(player);
            }
        }
    }

    /**
     * Simula la vittoria di N set per un giocatore.
     */
    private void scoreSets(int player, int setsToWin) {
        for (int i = 0; i < setsToWin; i++) {
            // Un set standard viene vinto 6-0 (6 game di fila)
            scoreGames(player, 6);
        }
    }

    // --- Setup Iniziale ---

    @BeforeEach
    void setUp() {
        manager = new TennisScoreManager();
        // Nota: Si usa System.setOut(new java.io.PrintStream(new java.io.OutputStream() {...}))
        // per sopprimere l'output di System.out.println() in un ambiente di test reale.
        // Qui lo omettiamo per semplicità.
    }

    // ====================================================================
    // 1. TEST DI STATO INIZIALE E METODI DI BASE (Black Box)
    // ====================================================================

    @Test
    @DisplayName("Initial State: Verifica dello stato iniziale del manager")
    void testInitialState() {
        assertEquals("Love-Love", manager.getGameScore(), "Il punteggio iniziale del game dovrebbe essere Love-Love.");
        assertTrue(manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"), "Il punteggio iniziale del match dovrebbe essere 0-0.");
        assertFalse(manager.isGameOver(), "La partita non dovrebbe essere finita all'inizio.");
    }

    @Test
    @DisplayName("Reset Methods: Verifica dei metodi di reset")
    void testResetMethods() {
        // Punteggio a 30-30
        scorePoint(1); scorePoint(2); // 15-Love
        scorePoint(1); scorePoint(2); // 30-30

        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore(), "resetPoints() dovrebbe resettare solo i punti.");

        // Punteggio game 1-1, punti 15-15
        scoreGames(1, 1);
        scoreGames(2, 1);
        scorePoint(1);
        scorePoint(2);

        manager.resetGameAndPoints();
        assertTrue(manager.getMatchScore().contains("Game: 0-0"), "resetGameAndPoints() dovrebbe resettare game e punti.");
        assertFalse(manager.isTieBreak(), "resetGameAndPoints() dovrebbe resettare isTieBreak.");
    }

    @Test
    @DisplayName("Invalid Input: Gestione di input non valido e partita finita")
    void testInvalidInputAndGameOverBlock() {
        // Test input non valido (copre il ramo 'else' in pointScored)
        // Il punteggio non dovrebbe cambiare
        scorePoint(99); 
        assertEquals("Love-Love", manager.getGameScore(), "L'input non valido non dovrebbe cambiare il punteggio.");

        // Simula la fine della partita (P1 vince 3 set a 0)
        scoreSets(1, 3); 
        assertTrue(manager.isGameOver(), "La partita dovrebbe essere finita.");

        // Test blocco partita finita (copre il ramo 'if (isGameOver())' in pointScored)
        // Il punteggio non dovrebbe cambiare
        scorePoint(2);
        assertTrue(manager.isGameOver(), "Dopo la vittoria, il punteggio non dovrebbe cambiare.");
    }

    // ====================================================================
    // 2. TEST LOGICA GAME (Branch/Condition Coverage)
    // ====================================================================

    @Test
    @DisplayName("Game Standard: Copertura di tutti i punteggi standard (Love-40)")
    void testStandardGameScores() {
        // Love-Love (0-0)
        assertEquals("Love-Love", manager.getGameScore());

        // 15-Love (1-0)
        scorePoint(1);
        assertEquals("15-Love", manager.getGameScore());

        // 30-15 (2-1)
        scorePoint(1);
        scorePoint(2);
        assertEquals("30-15", manager.getGameScore());

        // 40-30 (3-2)
        scorePoint(1);
        assertEquals("40-30", manager.getGameScore());

        // 40-40 (3-3) - (Combinazione A/B/C: F/V/V -> Falso per Standard, V/V -> Vero per Deuce)
        scorePoint(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    @DisplayName("Deuce/Advantage: Copertura completa della logica Deuce e Vantaggio")
    void testDeuceAdvantageLogic() {
        // Punteggio a Deuce (3-3)
        scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2);
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P1 (4-3) - (Combinazione A/B/C: F/V/V -> Falso per Deuce, V/V -> Vero per Adv P1)
        scorePoint(1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // Torna a Deuce (4-4) - (Combinazione A/B/C: F/F/V -> Falso per Adv P1, V/V -> Vero per Deuce)
        scorePoint(2);
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P2 (4-5)
        scorePoint(2);
        assertEquals("Vantaggio P2", manager.getGameScore());
    }

    @Test
    @DisplayName("Game Win: Copertura delle condizioni di vittoria Game (4-0 e 4-2)")
    void testGameWinConditions() {
        // Game per P1 (4-0) - (Combinazione V/V in checkGamePoint)
        scorePoint(1); scorePoint(1); scorePoint(1);
        scorePoint(1); // Game Vinto
        assertTrue(manager.getMatchScore().contains("Game: 1-0"), "P1 dovrebbe avere 1 game.");

        // Game per P2 (7-5 dopo Deuce, cioè 5-3 a A-40)
        // 4 game pari, 1 game P1, 1 game P2
        scoreGames(1, 4);
        scoreGames(2, 4);
        // Da Deuce (0-0 nel game)
        scorePoint(1); scorePoint(2); // Deuce
        scorePoint(2); // Vantaggio P2
        scorePoint(2); // Game Vinto
        assertTrue(manager.getMatchScore().contains("Game: 5-5"), "I game dovrebbero essere 5-5.");
        assertEquals("Love-Love", manager.getGameScore(), "Il punteggio dei punti dovrebbe essere resettato.");
    }

    // ====================================================================
    // 3. TEST LOGICA SET E TIE-BREAK (Branch/Condition Coverage)
    // ====================================================================

    @Test
    @DisplayName("Set Win Standard: Copertura condizioni 6-4 e 7-5 (Rami A e B)")
    void testSetWinStandardConditions() {
        // Punteggio 5-4
        scoreGames(1, 5);
        scoreGames(2, 4);
        
        // P1 vince un game -> 6-4 (Ramo A: gameP1 >= 6 AND gameP1 >= gameP2 + 2)
        scoreGames(1, 1); 
        assertTrue(manager.getMatchScore().startsWith("1-0"), "P1 dovrebbe avere 1 Set (6-4).");
        assertTrue(manager.getMatchScore().contains("Game: 0-0"), "Si dovrebbe essere nel Set 2.");

        // P2 vince 4 game (4-0 Set 2), P1 vince 1 game (4-1)
        scoreGames(2, 4);
        scoreGames(1, 1); // 1-1 (Set 2)
        
        // Simula 6-5 (5-4 per P2)
        scoreGames(2, 1); // 1-5
        scoreGames(1, 4); // 5-5
        scoreGames(2, 1); // 5-6
        scoreGames(1, 1); // 6-6 (Attiva Tie-Break)
        
        // P1 vince un game quando è 6-5 -> 7-5 (Ramo B: gameP1 == 7 AND gameP2 == 5)
        manager.resetGameAndPoints(); // Reset per non avere il tie-break a 6-6
        manager.gamesP1 = 6;
        manager.gamesP2 = 5;
        scoreGames(1, 1);
        assertTrue(manager.getMatchScore().startsWith("2-0"), "P1 dovrebbe avere 2 Set (7-5).");
    }

    @Test
    @DisplayName("Tie-Break: Attivazione e vittoria (Copertura rami Tie-Break)")
    void testTieBreakActivationAndWin() {
        // Punteggio 6-6 (Attivazione Tie-Break)
        scoreGames(1, 6);
        scoreGames(2, 6);
        
        assertTrue(manager.isTieBreak, "Il Tie-Break dovrebbe essere attivo.");
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore(), "Il punteggio Tie-Break dovrebbe essere 0-0.");
        
        // Mini Deuce (6-6)
        scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2);
        assertEquals("TIE-BREAK: 3-3", manager.getTieBreakScore());

        scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2);
        assertEquals("TIE-BREAK: 6-6", manager.getTieBreakScore());

        // P1 vince (8-6)
        scorePoint(1); scorePoint(1); 
        
        // Controllo vittoria Tie-Break (Ramo C in checkSetPoint)
        // gamesP1 incrementato (diventa 7), resetGameAndPoints() chiamato.
        assertTrue(manager.getMatchScore().startsWith("1-0"), "P1 dovrebbe avere 1 Set (7-6).");
        assertFalse(manager.isTieBreak, "Il Tie-Break dovrebbe essere disattivato.");
        assertEquals(2, manager.currentSet, "Si dovrebbe essere nel Set 2.");
    }

    @Test
    @DisplayName("!!! BUG ALERT: P2 Set Win 7-6 non funziona !!!")
    void testBugInP2SetWinLogic() {
        // Simula 6-6 e P2 vince il Tie-Break (Punti 6-8)
        scoreGames(1, 6);
        scoreGames(2, 6); // TB attivo (0-0)

        // P1 6 punti, P2 8 punti (P2 vince il Tie-Break)
        for (int i = 0; i < 6; i++) {
            scorePoint(1);
        }
        for (int i = 0; i < 8; i++) {
            scorePoint(2);
        }

        // Il codice di P2 (gamesP2 == 7 && gamesP2 == 6) è sempre FALSO.
        // Di conseguenza, il set non viene chiuso.
        
        // **BUG VERIFICATO**: Nonostante P2 abbia vinto il TB, la partita è bloccata.
        // Il set non è chiuso e currentSet è ancora 1.
        assertEquals(1, manager.currentSet, "Il set non avanza a causa del bug nella condizione P2 7-6.");
        assertTrue(manager.isTieBreak, "Il Tie-Break è ancora attivo a causa della condizione P2 errata.");
    }

    // ====================================================================
    // 4. TEST LOGICA PARTITA (Black Box)
    // ====================================================================

    @Test
    @DisplayName("Match Win P1: Copertura 3-0, 3-1, 3-2 (Tutti i rami isGameOver)")
    void testMatchWinP1Conditions() {
        // P1 vince 3-0
        scoreSets(1, 3);
        assertTrue(manager.isGameOver(), "P1 dovrebbe vincere 3-0.");
        
        // Reset per nuovo test
        manager = new TennisScoreManager(); 

        // P1 vince 3-1
        scoreSets(1, 2); // 2 set P1
        scoreSets(2, 1); // 1 set P2
        scoreGames(1, 6); // P1 vince Set 4
        assertTrue(manager.isGameOver(), "P1 dovrebbe vincere 3-1.");

        // Reset per nuovo test
        manager = new TennisScoreManager(); 

        // P1 vince 3-2 (Test più lungo)
        scoreSets(1, 2); // 2 set P1
        scoreSets(2, 2); // 2 set P2 (4 set giocati)
        
        // P1 vince il 5° set (game 6-4)
        scoreGames(1, 6); 
        assertTrue(manager.isGameOver(), "P1 dovrebbe vincere 3-2.");
    }

    @Test
    @DisplayName("Match Win P2: Verifica vittoria P2 (Simmetrico)")
    void testMatchWinP2Conditions() {
        // P2 vince 3-0
        scoreSets(2, 3);
        assertTrue(manager.isGameOver(), "P2 dovrebbe vincere 3-0.");
    }

    @Test
    @DisplayName("Match Score Display: Verifica visualizzazione punteggio partita")
    void testMatchScoreDisplay() {
        // Punteggio iniziale
        assertTrue(manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"));

        // Punteggio 1-1 set, 3-2 game, Vantaggio P1
        scoreGames(1, 6); // P1 vince Set 1
        scoreGames(2, 6); // P2 vince Set 2
        scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2); scorePoint(1); scorePoint(2);
        scorePoint(1); // Vantaggio P1
        
        assertTrue(manager.getMatchScore().contains("1-1 (Game: 0-0 Vantaggio P1)"), "Il punteggio del match dovrebbe includere i set vinti e il punteggio Adv P1.");

        // Punteggio in Tie-Break
        manager.isTieBreak = true;
        scorePoint(1); scorePoint(2);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-1"), "Il punteggio del match dovrebbe mostrare il punteggio TB.");
    }
}
						