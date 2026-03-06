/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: salva.cinque@studenti.unina.it
UserID: 274
Date: 24/11/2025
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

    // --- TEST PUNTI STANDARD (0-40) ---

    @Test
    public void testInitialScore() {
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("0-0"));
    }

    @Test
    public void testPointsProgressionP1() {
        manager.pointScored(1); // 15
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(1); // 30
        assertEquals("30-Love", manager.getGameScore());
        manager.pointScored(1); // 40
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testPointsProgressionP2() {
        manager.pointScored(2); // 15
        assertEquals("Love-15", manager.getGameScore());
        manager.pointScored(2); // 30
        assertEquals("Love-30", manager.getGameScore());
        manager.pointScored(2); // 40
        assertEquals("Love-40", manager.getGameScore());
    }

    // --- TEST DEUCE E VANTAGGI ---

    @Test
    public void testDeuce() {
        generateDeuce();
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        generateDeuce();
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugCheck() {
        // OBIETTIVO: Coprire il bug nel codice sorgente
        // Il codice ha "if (scoreP2 == scoreP2 + 1)" che è impossibile.
        // Quindi quando P2 ha vantaggio, il codice cade nell'ultimo return "Errore Game".
        generateDeuce();
        manager.pointScored(2); // P2 va in vantaggio
        
        // Asseriamo "Errore Game" perché questo è il comportamento attuale del codice NON modificabile.
        assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void testBackToDeuceFromAdvantageP1() {
        generateDeuce();
        manager.pointScored(1); // Adv P1
        manager.pointScored(2); // Back to Deuce logicamente (punteggi pari)
        // Nota: scoreP1=4, scoreP2=4. 
        // La logica "scoreP1 == scoreP2 && scoreP1 >= 3" cattura il ritorno a Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    // --- TEST VITTORIA GAME ---

    @Test
    public void testWinGameP1_Standard() {
        // 40-0 -> Win
        scorePoints(1, 3); 
        manager.pointScored(1); // Win game
        
        // Verifica reset punti e incremento game
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameP2_Standard() {
        // 0-40 -> Win
        scorePoints(2, 3);
        manager.pointScored(2); // Win game
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testWinGameP1_AfterDeuce() {
        generateDeuce();
        manager.pointScored(1); // Adv P1
        manager.pointScored(1); // Win P1 (diff >= 2)
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameP2_AfterDeuce() {
        generateDeuce();
        manager.pointScored(2); // Adv P2
        manager.pointScored(2); // Win P2
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    // --- TEST SET E TIE-BREAK ---

    @Test
    public void testSetWinP1_6_0() {
        winSet(1, 6); // P1 vince 6 game di fila
        // Verifica passaggio al set 2
        // Stringa attesa: "P1: 1 Set | P2: 0 Set" non appare finché match non finisce o formattazione specifica
        // Verifichiamo dal parziale: Set Corrente 2
        // Il metodo getMatchScore() per set non finiti mostra "1-0 (Game: 0-0...)"
        String score = manager.getMatchScore();
        assertTrue("Deve indicare 1 set a 0", score.startsWith("1-0"));
        assertTrue("Nuovo set deve essere 0-0", score.contains("Game: 0-0"));
    }

    @Test
    public void testSetWinP2_6_4() {
        winGames(1, 4); // P1 4 games
        winGames(2, 6); // P2 6 games (6-4 vince)
        String score = manager.getMatchScore();
        assertTrue("P2 deve vincere il set (0-1 sets)", score.startsWith("0-1"));
    }

    @Test
    public void testSetWinP1_7_5() {
        // Arriviamo a 5-5
        winGames(1, 5);
        winGames(2, 5);
        // P1 vince -> 6-5
        winGames(1, 1);
        // P1 vince -> 7-5 (Vittoria Set)
        winGames(1, 1);
        
        assertTrue("P1 deve vincere il set 7-5", manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void testTieBreakActivation() {
        forceTieBreak(); // Arriva a 6-6
        
        // Verifica stato TieBreak
        String score = manager.getMatchScore();
        assertTrue("Deve mostrare TIE-BREAK", score.contains("TIE-BREAK"));
        assertTrue("Punteggio TieBreak iniziale 0-0", score.contains("0-0"));
    }

    @Test
    public void testTieBreakScoring() {
        forceTieBreak();
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-0"));
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-1"));
    }

    @Test
    public void testTieBreakWinP2_CorrectBehavior() {
        // Testiamo P2 che vince il TieBreak (questo ramo NON ha il bug del typo)
        forceTieBreak();
        // P2 fa 7 punti (7-0)
        for(int i=0; i<7; i++) manager.pointScored(2);
        
        // Deve aver vinto il set (0-1)
        assertTrue("P2 deve vincere il set al tie-break", manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testTieBreakWinP1_BugBehavior() {
        // OBIETTIVO: Coprire il Bug in checkSetPoint() per P1
        // Il codice ha "(gamesP2 == 7 && gamesP2 == 6)" invece di verificare gamesP1
        // Quindi P1 vince i punti del tiebreak, ma il set NON viene chiuso.
        
        forceTieBreak();
        // P1 fa 7 punti (7-0)
        for(int i=0; i<7; i++) manager.pointScored(1);
        
        String score = manager.getMatchScore();
        
        // COMPORTAMENTO ATTESO (BUG): 
        // Punteggio game: 7-6
        // Punteggio Set: 0-0 (Non incrementato)
        // Siamo ancora nel TieBreak tecnicamente o in uno stato inconsistente
        assertTrue("A causa del bug, il set P1 rimane 0", score.startsWith("0-0"));
        assertTrue("I game mostrano 7-6", score.contains("Game: 7-6")); 
    }

    // --- TEST MATCH COMPLETION ---

    @Test
    public void testMatchWinP1() {
        // P1 vince 3 set
        winSet(1, 6); // Set 1
        winSet(1, 6); // Set 2
        winSet(1, 6); // Set 3
        
        assertTrue("Match deve essere finito", manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void testMatchWinP2() {
        // P2 vince 3 set
        winSet(2, 6);
        winSet(2, 6);
        winSet(2, 6);
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    // --- TEST INPUT E BRANCH ALTERNATIVI ---

    @Test
    public void testInvalidPlayerInput() {
        manager.pointScored(99);
        assertEquals("Love-Love", manager.getGameScore()); // Nessun cambiamento
    }

    @Test
    public void testPointsIgnoredAfterGameOver() {
        // Vinciamo il match con P1
        winSet(1, 6);
        winSet(1, 6);
        winSet(1, 6);
        
        String finalScore = manager.getMatchScore();
        
        // Proviamo a segnare ancora
        manager.pointScored(1);
        
        // Il punteggio non deve cambiare
        assertEquals(finalScore, manager.getMatchScore());
    }
    
    @Test
    public void testResetPointsDirectly() {
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testResetGameAndPointsDirectly() {
        manager.pointScored(1);
        manager.resetGameAndPoints();
        assertEquals("Love-Love", manager.getGameScore());
        // Nota: resetGameAndPoints resetta i games ma è void e non accessibile direttamente l'int games
        // Verifichiamo tramite getMatchScore che resetta
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    // --- HELPER METHODS ---

    private void scorePoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    private void generateDeuce() {
        scorePoints(1, 3); // 40
        scorePoints(2, 3); // 40
    }

    private void winGames(int player, int gamesToWin) {
        for (int g = 0; g < gamesToWin; g++) {
            scorePoints(player, 4); // Vince un game a 0
        }
    }

    private void winSet(int player, int gamesNeeded) {
        winGames(player, gamesNeeded);
    }
    
    private void forceTieBreak() {
        winGames(1, 5);
        winGames(2, 5);
        winGames(1, 1); // 6-5
        winGames(2, 1); // 6-6 -> TieBreak Triggered
    }
}

						