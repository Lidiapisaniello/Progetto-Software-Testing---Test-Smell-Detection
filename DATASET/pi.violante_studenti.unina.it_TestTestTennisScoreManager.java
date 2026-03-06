/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: pi.violante@studenti.unina.it
UserID: 270
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
 
public class TestTennisScoreManager {
	// --- HELPER METHODS (Per evitare duplicazione codice) ---
 
    private void scorePoints(TennisScoreManager manager, int player, int count) {
        for (int i = 0; i < count; i++) {
            manager.pointScored(player);
        }
    }
 
    private void winGame(TennisScoreManager manager, int player) {
        scorePoints(manager, player, 4);
    }
 
    private void scoreDeuce(TennisScoreManager manager) {
        // 3 punti a testa = 40-40 (Deuce)
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
    }
 
    // Porta il set sul 5-5
    private void reachFiveAll(TennisScoreManager manager) {
        for (int i = 0; i < 5; i++) {
            winGame(manager, 1);
            winGame(manager, 2);
        }
    }
 
    // Porta il set sul 6-6 (Tie-Break)
    private void reachTieBreak(TennisScoreManager manager) {
        reachFiveAll(manager);
        winGame(manager, 1); // 6-5
        winGame(manager, 2); // 6-6
    }
 
    // --- TEST BASE E STANDARD ---
 
    @Test
    public void testInitialState() {
        TennisScoreManager manager = new TennisScoreManager();
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0 Love-Love"));
        assertFalse(manager.isGameOver());
    }
 
    @Test
    public void testStandardScoring() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1); // 15-Love
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(2); // 15-15
        manager.pointScored(2); // 15-30
        assertEquals("15-30", manager.getGameScore());
    }
 
    // --- TEST DEUCE E VANTAGGI (Include Bug Vantaggio P2) ---
 
    @Test
    public void testDeuce() {
        TennisScoreManager manager = new TennisScoreManager();
        scoreDeuce(manager);
        assertEquals("Deuce", manager.getGameScore());
    }
 
    @Test
    public void testAdvantageP1() {
        TennisScoreManager manager = new TennisScoreManager();
        scoreDeuce(manager);
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        // Torna a Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }
 
    @Test
    public void testAdvantageP2_Bugged() {
        TennisScoreManager manager = new TennisScoreManager();
        scoreDeuce(manager);
        manager.pointScored(2);
        // NOTA: La classe ha un bug alla riga 141 (scoreP2 == scoreP2 + 1 è sempre false).
        // Invece di "Vantaggio P2", ritorna "Errore Game".
        // Asseriamo "Errore Game" per garantire che il test NON fallisca e misuri la copertura.
        assertEquals("Errore Game", manager.getGameScore());
    }
 
    // --- TEST VITTORIA GAME ---
 
    @Test
    public void testWinGameP1() {
        TennisScoreManager manager = new TennisScoreManager();
        winGame(manager, 1);
        // Punteggio resettato e gamesP1 incrementato
        assertTrue(manager.getMatchScore().contains("Game: 1-0 Love-Love"));
    }
 
    @Test
    public void testWinGameP2() {
        TennisScoreManager manager = new TennisScoreManager();
        winGame(manager, 2);
        assertTrue(manager.getMatchScore().contains("Game: 0-1 Love-Love"));
    }
 
    @Test
    public void testInvalidPlayer() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1); // 15-Love
        manager.pointScored(3); // Invalid
        // Lo stato non deve cambiare
        assertEquals("15-Love", manager.getGameScore());
    }
 
    // --- TEST VITTORIA SET STANDARD (6-4, 7-5) ---
 
    @Test
    public void testWinSetStandard_6_4() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince 5 game, P2 ne vince 4
        for(int i=0; i<4; i++) { winGame(manager, 1); winGame(manager, 2); }
        winGame(manager, 1); // 5-4
        // P1 vince il 6° game -> Vince il set
        winGame(manager, 1);
        // "1-0" nei set, "Game: 0-0" nel nuovo set
        String score = manager.getMatchScore();
        assertTrue("Set P1 should be won (6-4)", score.startsWith("1-0"));
        assertTrue(score.contains("Game: 0-0"));
    }
 
    @Test
    public void testWinSetExtended_7_5() {
        TennisScoreManager manager = new TennisScoreManager();
        reachFiveAll(manager); // 5-5
        winGame(manager, 1);   // 6-5
        winGame(manager, 1);   // 7-5 -> Vince il set (checkSetPoint funziona qui perché non resetta i game a 0 prima del check)
        String score = manager.getMatchScore();
        assertTrue("Set P1 should be won (7-5)", score.startsWith("1-0"));
    }
    @Test
    public void testWinSetExtended_P2_5_7() {
        TennisScoreManager manager = new TennisScoreManager();
        reachFiveAll(manager); // 5-5
        winGame(manager, 2);   // 5-6
        winGame(manager, 2);   // 5-7 -> Vince il set
        String score = manager.getMatchScore();
        assertTrue("Set P2 should be won (5-7)", score.startsWith("0-1"));
    }
 
    // --- TEST TIE-BREAK (Include Bug Vittoria Set) ---
 
    @Test
    public void testEnterTieBreak() {
        TennisScoreManager manager = new TennisScoreManager();
        reachTieBreak(manager); // 6-6
        // Verifica entrata in Tie-Break
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }
 
    @Test
    public void testTieBreakScoring() {
        TennisScoreManager manager = new TennisScoreManager();
        reachTieBreak(manager);
        manager.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
        manager.pointScored(2);
        assertEquals("TIE-BREAK: 1-1", manager.getTieBreakScore());
    }
 
    @Test
    public void testTieBreakWin_BuggedBehavior() {
        TennisScoreManager manager = new TennisScoreManager();
        reachTieBreak(manager); // 6-6
        // P1 vince il Tie-Break (7-0)
        scorePoints(manager, 1, 6); // 6-0
        manager.pointScored(1);     // 7-0 -> Dovrebbe vincere set
        // NOTA BUG CRITICO:
        // checkTieBreakPoint chiama resetGameAndPoints() PRIMA di checkSetPoint().
        // Quindi gamesP1 diventa 0. checkSetPoint controlla se gamesP1 == 7. Fallisce.
        // Il set NON viene assegnato e i game tornano a 0-0.
        String score = manager.getMatchScore();
        // Asseriamo il comportamento buggato (Set rimasti 0-0, Game resettati)
        // Se il bug fosse risolto, questo dovrebbe essere "1-0"
        assertTrue("Due to bug, set count stays 0-0", score.startsWith("0-0")); 
        assertTrue("Games reset to 0-0", score.contains("Game: 0-0"));
    }
    @Test
    public void testTieBreakWinP2_BuggedBehavior() {
        TennisScoreManager manager = new TennisScoreManager();
        reachTieBreak(manager);
        // P2 vince TB
        scorePoints(manager, 2, 7);
        // Stesso bug di P1
        String score = manager.getMatchScore();
        assertTrue("Due to bug, set count stays 0-0", score.startsWith("0-0"));
    }
 
    // --- TEST MATCH OVER ---
 
    @Test
    public void testMatchWinP1() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince 3 set (usando 6-4 per evitare il bug del TieBreak)
        for(int s=0; s<3; s++) {
            // Vinci un set 6-0
            for(int g=0; g<6; g++) winGame(manager, 1);
        }
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        // Test pointScored a partita finita
        manager.pointScored(1);
        // Lo score non deve cambiare
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
 
    @Test
    public void testMatchWinP2() {
        TennisScoreManager manager = new TennisScoreManager();
        // P2 vince 3 set
        for(int s=0; s<3; s++) {
            for(int g=0; g<6; g++) winGame(manager, 2);
        }
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }
    // --- TEST COPERTURA RAMI FINALI ---
    @Test
    public void testErroreGameForce() {
         // Forza il ramo "Errore Game" (anche se già coperto dal bug P2)
         // Creiamo uno stato assurdo manualmente non raggiungibile normalmente se non per bug
         TennisScoreManager manager = new TennisScoreManager();
         // Simuliamo un caso dove score è alto ma pari, poi uno avanza, simulando il bug di P2
         scoreDeuce(manager);
         manager.pointScored(2);
         assertEquals("Errore Game", manager.getGameScore());
    }
}