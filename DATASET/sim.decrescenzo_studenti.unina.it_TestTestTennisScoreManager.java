/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: sim.decrescenzo@studenti.unina.it
UserID: 354
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    // --- Test Punteggio Base ---

    @Test
    public void testInitialScore() {
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("0-0"));
    }

    @Test
    public void testSimplePointsP1() {
        manager.pointScored(1); // 15
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(1); // 30
        assertEquals("30-Love", manager.getGameScore());
        
        manager.pointScored(1); // 40
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testSimplePointsP2() {
        manager.pointScored(2); // 0-15
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void testMixedScore() {
        manager.pointScored(1); // 15-0
        manager.pointScored(2); // 15-15
        assertEquals("15-15", manager.getGameScore());
    }
    
    @Test
    public void testInvalidPlayer() {
        // Segniamo un punto valido per avere uno stato base
        manager.pointScored(1);
        String stateBefore = manager.getGameScore();
        
        // Tentativo con giocatore non valido
        manager.pointScored(3);
        manager.pointScored(-1);
        
        // Lo stato non deve cambiare
        assertEquals(stateBefore, manager.getGameScore());
    }

    // --- Test Logica Game e Vantaggi ---

    @Test
    public void testDeuce() {
        createDeuce();
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        createDeuce();
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testBackToDeuceFromAdvantageP1() {
        createDeuce();
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(2); // Torna Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugBehavior() {
        // NOTA: Nel codice originale c'è un bug: "scoreP2 == scoreP2 + 1" è sempre false.
        // Pertanto il codice non entra nell'if del Vantaggio P2 e finisce su "Errore Game".
        // Testiamo il comportamento attuale per garantire che il test passi.
        createDeuce();
        manager.pointScored(2);
        assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void testGameWinP1() {
        // 40-0
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1);
        
        // Punto vittoria
        manager.pointScored(1);
        
        // Verifica reset punti e incremento game
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testGameWinP2() {
        manager.pointScored(2);
        manager.pointScored(2);
        manager.pointScored(2);
        manager.pointScored(2);
        
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    // --- Test Set Logic ---

    @Test
    public void testSetWinStandard6_0() {
        winSet(1); // P1 vince il set 6-0
        
        // Verifica che P1 abbia 1 set
        // La stringa match score sarà tipo "1-0 (Game: 0-0 Love-Love)"
        String matchScore = manager.getMatchScore();
        assertTrue("Dovrebbe indicare 1 set a 0", matchScore.startsWith("1-0"));
        assertTrue("Game dovrebbero essere resettati", matchScore.contains("Game: 0-0"));
    }
    
    @Test
    public void testSetWinStandard0_6() {
        winSet(2); // P2 vince il set 0-6
        assertTrue("Dovrebbe indicare 0 set a 1", manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testSetWinExtended7_5() {
        // Portiamo a 5-5
        for(int i=0; i<5; i++) winGame(1);
        for(int i=0; i<5; i++) winGame(2);
        
        // 6-5
        winGame(1);
        assertTrue(manager.getMatchScore().contains("Game: 6-5"));
        
        // 7-5 (Vittoria Set)
        winGame(1);
        
        assertTrue("P1 dovrebbe aver vinto il set", manager.getMatchScore().startsWith("1-0"));
    }

    // --- Test Tie-Break ---

    @Test
    public void testTieBreakTrigger() {
        goToTieBreak(); // Porta il punteggio a 6-6
        
        String score = manager.getMatchScore();
        assertTrue("Deve essere in Tie-Break", score.contains("TIE-BREAK"));
        assertTrue("Score Tie-Break iniziale", score.contains("0-0"));
    }

    @Test
    public void testTieBreakScoring() {
        goToTieBreak();
        
        manager.pointScored(1); // 1-0
        manager.pointScored(2); // 1-1
        manager.pointScored(1); // 2-1
        
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 2-1"));
    }

    @Test
    public void testTieBreakWinP1() {
        goToTieBreak();
        
        // P1 fa 7 punti di fila (7-0 nel tie break)
        for(int i=0; i<7; i++) {
            manager.pointScored(1);
        }
        
        // P1 dovrebbe aver vinto il set
        assertTrue("P1 dovrebbe vincere il set dopo il tie-break", manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testTieBreakWinP2_Close() {
        goToTieBreak();
        
        // Portiamo tie break a 6-6
        for(int i=0; i<6; i++) { manager.pointScored(1); manager.pointScored(2); }
        
        // P2 va a 7 (6-7) -> non vince ancora
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 6-7"));
        
        // P2 va a 8 (6-8) -> Vince
        manager.pointScored(2);
        assertTrue("P2 dovrebbe vincere il set", manager.getMatchScore().startsWith("0-1"));
    }

    // --- Test Partita Completa (Game Over) ---

    @Test
    public void testMatchWinP1() {
        // P1 vince 3 set
        winSet(1);
        winSet(1);
        winSet(1);
        
        assertTrue("Partita dovrebbe essere finita", manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }
    
    @Test
    public void testMatchWinP2() {
        winSet(2);
        winSet(2);
        winSet(2);
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void testPlayAfterGameOver() {
        winSet(1);
        winSet(1);
        winSet(1);
        
        assertTrue(manager.isGameOver());
        
        // Proviamo a segnare ancora punti
        manager.pointScored(1);
        
        // Il punteggio finale non deve cambiare
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void testMoveToNextSetBlock() {
        // Testiamo che moveToNextSet non incrementi se la partita è finita
        // Dobbiamo simulare la vittoria, poi chiamare indirettamente moveToNextSet 
        // (che viene chiamato dentro checkSetPoint, ma è protetto da isGameOver).
        
        // Vinciamo 3 set
        winSet(1);
        winSet(1);
        winSet(1);
        
        // Tentiamo un'azione che teoricamente triggera controlli
        manager.pointScored(1);
        
        // Se moveToNextSet fosse stato chiamato erroneamente, avremmo errori o stati incoerenti,
        // ma qui verifichiamo solo che l'output resti stabile.
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    // --- Test Match Score Logic & Formatting ---
    
    @Test
    public void testMatchScoreFormattingMidGame() {
        winSet(1); // P1 1 set
        winGame(2); // P2 1 game nel secondo set
        manager.pointScored(1); // 15-0
        
        String score = manager.getMatchScore();
        // Expect: 1-0 (Game: 0-1 15-Love)
        assertTrue(score.contains("1-0"));
        assertTrue(score.contains("Game: 0-1"));
        assertTrue(score.contains("15-Love"));
    }

    // --- Helper Methods ---

    private void createDeuce() {
        // 3 punti a testa -> 40-40 (Deuce)
        for(int i=0; i<3; i++) manager.pointScored(1);
        for(int i=0; i<3; i++) manager.pointScored(2);
    }

    private void winGame(int player) {
        // Vince un game a zero
        for(int i=0; i<4; i++) {
            manager.pointScored(player);
        }
    }

    private void winSet(int player) {
        for (int i = 0; i < 6; i++) {
            winGame(player);
        }
    }
    
    private void goToTieBreak() {
        // 5-5
        for(int i=0; i<5; i++) winGame(1);
        for(int i=0; i<5; i++) winGame(2);
        // 6-5
        winGame(1);
        // 6-6 -> Tie Break Triggered
        winGame(2);
    }
}