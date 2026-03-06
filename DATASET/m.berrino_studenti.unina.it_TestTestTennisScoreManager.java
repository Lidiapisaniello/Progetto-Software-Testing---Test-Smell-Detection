/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Tuo Nome"
Cognome: "Tuo Cognome"
Username: m.berrino@studenti.unina.it
UserID: 223
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager mgr;

    @Before
    public void setUp() {
        mgr = new TennisScoreManager();
    }

    // --- Helpers per rendere i test leggibili ---
    
    // Segna n punti per un giocatore
    private void scorePoints(int player, int count) {
        for (int i = 0; i < count; i++) {
            mgr.pointScored(player);
        }
    }

    // Vince un game standard a 0 (4 punti consecutivi)
    private void winGame(int player) {
        scorePoints(player, 4);
    }

    // Porta il set sul 5-5 (P1 vince 5 game, P2 vince 5 game)
    private void playToFiveAll() {
        for (int i = 0; i < 5; i++) {
            winGame(1);
            winGame(2);
        }
    }

    // --- Test Punti e Game Standard ---

    @Test
    public void testBasicScoreProgression() {
        // Test 0 -> 15 -> 30 -> 40
        assertEquals("Love-Love", mgr.getGameScore());
        
        mgr.pointScored(1);
        assertEquals("15-Love", mgr.getGameScore());
        
        mgr.pointScored(1);
        assertEquals("30-Love", mgr.getGameScore());
        
        mgr.pointScored(1);
        assertEquals("40-Love", mgr.getGameScore());
        
        mgr.pointScored(2);
        assertEquals("40-15", mgr.getGameScore());
    }

    @Test
    public void testDeuceAndAdvantageLogic() {
        // Arriviamo a 40-40 (Deuce)
        scorePoints(1, 3); // 40
        scorePoints(2, 3); // 40
        assertEquals("Deuce", mgr.getGameScore());

        // Vantaggio P1
        mgr.pointScored(1);
        assertEquals("Vantaggio P1", mgr.getGameScore());

        // Ritorno a Deuce
        mgr.pointScored(2);
        assertEquals("Deuce", mgr.getGameScore());

        // Vantaggio P2
        mgr.pointScored(2);
        assertEquals("Vantaggio P2", mgr.getGameScore());

        // P2 Vince il game (Vantaggio -> Win)
        mgr.pointScored(2);
        // Punteggio resettato per nuovo game
        assertEquals("Love-Love", mgr.getGameScore());
        // Verifichiamo dai set che P2 ha 1 game
        assertTrue(mgr.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testInvalidPlayerInput() {
        // Assicurarsi che input diversi da 1 o 2 non rompano il codice o cambino lo stato
        String initialScore = mgr.getGameScore();
        mgr.pointScored(3);
        mgr.pointScored(0);
        mgr.pointScored(-1);
        assertEquals(initialScore, mgr.getGameScore());
    }

    // --- Test Set Logic ---

    @Test
    public void testWinSetStandard6_0() {
        // P1 vince 6 game di fila
        for (int i = 0; i < 6; i++) {
            winGame(1);
        }
        // Dovrebbe aver vinto il set 1 e resettato i game per il set 2
        assertTrue(mgr.getMatchScore().contains("P1: 1 Set")); // P1 ha 1 set
        assertTrue(mgr.getMatchScore().contains("Game: 0-0")); // Nuovo set iniziato
    }

    @Test
    public void testWinSetExtended7_5() {
        playToFiveAll(); // 5-5
        
        // P1 va a 6-5
        winGame(1);
        assertTrue(mgr.getMatchScore().contains("Game: 6-5"));
        
        // P1 va a 7-5 (Vince il set)
        winGame(1);
        
        assertTrue(mgr.getMatchScore().contains("P1: 1 Set"));
        assertTrue(mgr.getMatchScore().contains("Game: 0-0"));
    }

    // --- Test Tie-Break ---

    @Test
    public void testTieBreakActivationAndWin() {
        playToFiveAll(); // 5-5
        winGame(1);      // 6-5
        winGame(2);      // 6-6 -> Scatta il Tie-Break
        
        // Verifica che siamo in tie break guardando la stringa del punteggio
        // Nel tie break i punti sono numerici (0, 1, 2...), non "15", "30"
        assertTrue(mgr.getMatchScore().contains("TIE-BREAK: 0-0"));

        // Simuliamo punteggio tie-break
        scorePoints(1, 6); // P1: 6 punti
        scorePoints(2, 5); // P2: 5 punti
        assertTrue(mgr.getMatchScore().contains("TIE-BREAK: 6-5"));

        // P1 fa punto -> 7-5 (Vince tie break e set)
        mgr.pointScored(1);
        
        // Verifica fine set
        assertTrue(mgr.getMatchScore().contains("P1: 1 Set"));
        // Verifica reset per nuovo set
        assertEquals("Love-Love", mgr.getGameScore());
    }
    
    @Test
    public void testTieBreakExtendedWin() {
        // Scenario Tie Break che va oltre il 7 (es. 8-6)
        playToFiveAll();
        winGame(1);
        winGame(2); // 6-6 Tie break

        scorePoints(1, 6);
        scorePoints(2, 6); // 6-6 nel tie break
        
        mgr.pointScored(1); // 7-6 (non vince, serve distacco di 2)
        assertTrue(mgr.getMatchScore().contains("TIE-BREAK: 7-6"));
        
        mgr.pointScored(2); // 7-7
        mgr.pointScored(2); // 7-8
        mgr.pointScored(2); // 7-9 -> P2 vince Set
        
        assertTrue(mgr.getMatchScore().contains("P2: 1 Set"));
    }

    // --- Test Partita Completa (Match) ---

    @Test
    public void testFullMatchVictoryP1() {
        // P1 deve vincere 3 set.
        
        // Set 1: P1 vince 6-0
        for(int i=0; i<6; i++) winGame(1);
        
        // Set 2: P1 vince 6-0
        for(int i=0; i<6; i++) winGame(1);
        
        // Set 3: P1 vince 6-0
        for(int i=0; i<6; i++) winGame(1);

        // Verifica stato GameOver
        assertTrue(mgr.isGameOver());
        assertTrue(mgr.getMatchScore().contains("PARTITA VINTA DAL GIOCATORE 1"));
        assertEquals("PARTITA FINITA", mgr.getGameScore());

        // Test Mutation: Segnare punti dopo Game Over non deve cambiare nulla
        mgr.pointScored(2);
        assertTrue(mgr.isGameOver());
    }
    
    @Test
    public void testFullMatchVictoryP2_SplitSets() {
        // Scenario combattuto: P2 vince 3 set a 1
        // Set 1: P1 vince
        for(int i=0; i<6; i++) winGame(1); // 1-0 set
        
        // Set 2, 3, 4: P2 vince
        for(int s=0; s<3; s++) {
            for(int g=0; g<6; g++) winGame(2);
        }
        
        assertTrue(mgr.isGameOver());
        assertTrue(mgr.getMatchScore().contains("PARTITA VINTA DAL GIOCATORE 2"));
        // Stringa finale specifica
        assertTrue(mgr.getMatchScore().contains("P1: 1 Set | P2: 3 Set"));
    }

    // --- Test Output & Display ---

    @Test
    public void testPrintScore() {
        // Questo metodo stampa a video (void).
        // Lo chiamiamo per assicurarci che non lanci eccezioni e coprire le linee di codice.
        mgr.printScore();
        mgr.pointScored(1);
        mgr.printScore();
    }
    
    @Test
    public void testGameErrorScore() {
        // È difficile raggiungere il ramo "Errore Game" ("return Error Game") con la logica normale,
        // perché i controlli precedenti coprono tutto.
        // Tuttavia, serve per la completezza se possibile, ma in black-box testing rigoroso
        // se la logica interna è corretta quel ramo è "unreachable code".
        // Ci concentriamo su ciò che è testabile.
        assertNotNull(mgr.getGameScore());
    }
    
    @Test
    public void testCheckGamePointBranchCoverage() {
        // Copertura specifica per if (scoreP1 >= 4 && scoreP1 >= scoreP2 + 2)
        // Scenario: 40-30 (3-2 punti). P1 segna -> 4-2 -> Vince.
        scorePoints(1, 3); // 40
        scorePoints(2, 2); // 30
        mgr.pointScored(1); // P1 vince
        
        // Verifica che gamesP1 sia incrementato
        assertTrue(mgr.getMatchScore().contains("Game: 1-0"));
    }
}