/*
Nome: Marco
Cognome: Canonico
Username: marco.canonico@studenti.unina.it
UserID: 738
Date: 24/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager { 
	
    private TennisScoreManager tsm;

	@BeforeClass
	public static void setUpClass() {
		// Setup statico non necessario per questa classe
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Teardown statico non necessario
	}
				
	@Before
	public void setUp() {
		tsm = new TennisScoreManager();
	}
				
	@After
	public void tearDown() {
		tsm = null;
	}
				
	// --- TEST BASE E INIZIALIZZAZIONE ---

    @Test
    public void testInitialState() {
        // Verifica lo stato iniziale: 0-0 set, 0-0 game, Love-Love
        String score = tsm.getMatchScore();
        assertTrue("Il punteggio iniziale deve contenere 0-0", score.contains("0-0"));
        assertEquals("Love-Love", tsm.getGameScore());
    }

    @Test
    public void testResetPoints() {
        tsm.pointScored(1); // 15-Love
        tsm.resetPoints();
        assertEquals("Love-Love", tsm.getGameScore());
    }

    // --- TEST PUNTEGGIO GAME STANDARD (0, 15, 30, 40) ---

    @Test
    public void testPointSequenceP1() {
        assertEquals("Love-Love", tsm.getGameScore());
        tsm.pointScored(1);
        assertEquals("15-Love", tsm.getGameScore());
        tsm.pointScored(1);
        assertEquals("30-Love", tsm.getGameScore());
        tsm.pointScored(1);
        assertEquals("40-Love", tsm.getGameScore());
    }

    @Test
    public void testPointSequenceP2() {
        tsm.pointScored(2);
        assertEquals("Love-15", tsm.getGameScore());
        tsm.pointScored(2);
        assertEquals("Love-30", tsm.getGameScore());
        tsm.pointScored(2);
        assertEquals("Love-40", tsm.getGameScore());
    }

    @Test
    public void testMixedPoints() {
        tsm.pointScored(1); // 15-0
        tsm.pointScored(2); // 15-15
        assertEquals("15-15", tsm.getGameScore());
        tsm.pointScored(1); // 30-15
        assertEquals("30-15", tsm.getGameScore());
    }

    @Test
    public void testInvalidPlayerInput() {
        // Verifica che l'input diverso da 1 o 2 non cambi il punteggio
        tsm.pointScored(1);
        String scoreBefore = tsm.getGameScore();
        tsm.pointScored(3); // Giocatore non valido
        tsm.pointScored(-1); // Giocatore non valido
        assertEquals(scoreBefore, tsm.getGameScore());
    }

    // --- TEST LOGICA DEUCE E VANTAGGI ---

    @Test
    public void testDeuce() {
        generateDeuce();
        assertEquals("Deuce", tsm.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        generateDeuce();
        tsm.pointScored(1);
        assertEquals("Vantaggio P1", tsm.getGameScore());
    }

    @Test
    public void testAdvantageP2() {
        generateDeuce();
        tsm.pointScored(2);
        assertEquals("Vantaggio P2", tsm.getGameScore());
    }

    @Test
    public void testBackToDeuceFromP1() {
        generateDeuce();
        tsm.pointScored(1); // Vantaggio P1
        tsm.pointScored(2); // Torna a Deuce
        assertEquals("Deuce", tsm.getGameScore());
    }

    @Test
    public void testBackToDeuceFromP2() {
        generateDeuce();
        tsm.pointScored(2); // Vantaggio P2
        tsm.pointScored(1); // Torna a Deuce
        assertEquals("Deuce", tsm.getGameScore());
    }

    @Test
    public void testWinGameFromAdvantageP1() {
        generateDeuce();
        tsm.pointScored(1); // Adv P1
        tsm.pointScored(1); // Win Game
        // Dopo la vittoria del game, i punti tornano a Love-Love e i game incrementano
        assertEquals("Love-Love", tsm.getGameScore());
        assertTrue(tsm.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameFromAdvantageP2() {
        generateDeuce();
        tsm.pointScored(2); // Adv P2
        tsm.pointScored(2); // Win Game
        assertEquals("Love-Love", tsm.getGameScore());
        assertTrue(tsm.getMatchScore().contains("Game: 0-1"));
    }

    // --- TEST VITTORIA GAME E SET (BRANCH COVERAGE SU checkGamePoint e checkSetPoint) ---

    @Test
    public void testWinGameStandardP1() {
        // 40-0 -> Win
        tsm.pointScored(1);
        tsm.pointScored(1);
        tsm.pointScored(1);
        tsm.pointScored(1);
        assertTrue(tsm.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testWinGameStandardP2() {
        // 0-40 -> Win
        tsm.pointScored(2);
        tsm.pointScored(2);
        tsm.pointScored(2);
        tsm.pointScored(2);
        assertTrue(tsm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testWinSetP1_6_0() {
        winSet(1); // P1 vince 6 game consecutivi
        // MatchScore format: "setsWonP1-setsWonP2 (Game: ...)"
        // Se P1 vince il set, i set vinti diventano 1-0 e inizia il secondo set (Game 0-0)
        String score = tsm.getMatchScore();
        assertTrue("P1 dovrebbe aver vinto 1 set", score.startsWith("1-0"));
        assertTrue("Il nuovo set dovrebbe essere a 0 game", score.contains("Game: 0-0"));
    }

    @Test
    public void testWinSetP2_6_4() {
        // Simuliamo un 4-4
        for(int i=0; i<4; i++) { winGame(1); winGame(2); }
        // P2 va a 5-4
        winGame(2);
        // P2 va a 6-4 -> Vince il set (diff >= 2)
        winGame(2);
        
        String score = tsm.getMatchScore();
        assertTrue("P2 dovrebbe aver vinto 1 set", score.startsWith("0-1"));
    }

    @Test
    public void testWinSetP1_7_5() {
        // Copre il ramo (gamesP1 == 7 && gamesP2 == 5)
        // 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1); // 6-5 (Non vince ancora, diff < 2 e non è 7-5)
        assertTrue(tsm.getMatchScore().contains("Game: 6-5"));
        
        winGame(1); // 7-5 -> Vince il set
        String score = tsm.getMatchScore();
        assertTrue("P1 dovrebbe aver vinto il set con 7-5", score.startsWith("1-0"));
    }

    @Test
    public void testWinSetP2_7_5() {
        // Copre il ramo (gamesP2 == 7 && gamesP1 == 5)
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(2); // 5-6
        assertTrue(tsm.getMatchScore().contains("Game: 5-6"));
        
        winGame(2); // 5-7 -> Vince il set
        String score = tsm.getMatchScore();
        assertTrue("P2 dovrebbe aver vinto il set con 7-5", score.startsWith("0-1"));
    }

    // --- TEST TIE-BREAK ---

    @Test
    public void testTieBreakTrigger() {
        reachSixAll(); // Arriva a 6-6
        // Verifica che il flag isTieBreak sia attivo controllando l'output del punteggio
        // Se è tie break, il metodo getMatchScore chiama getTieBreakScore()
        String score = tsm.getMatchScore();
        assertTrue("Dovrebbe essere in TIE-BREAK", score.contains("TIE-BREAK"));
        assertTrue("Punteggio Tie-Break iniziale 0-0", score.contains("0-0"));
    }

    @Test
    public void testTieBreakScoring() {
        reachSixAll();
        // Punti tie break sono numerici semplici
        tsm.pointScored(1); // 1-0
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK: 1-0"));
        tsm.pointScored(2); // 1-1
        tsm.pointScored(2); // 1-2
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK: 1-2"));
    }

    @Test
    public void testTieBreakWinP2() {
        // Testiamo la vittoria corretta del tie break per P2 (7-0)
        // Questo copre il ramo (gamesP2 == 7 && gamesP1 == 6) in checkSetPoint
        reachSixAll();
        
        // P2 fa 7 punti di fila nel tie-break
        for(int i=0; i<7; i++) {
            tsm.pointScored(2);
        }
        
        // P2 vince il tie-break -> Vince il set -> Nuovo set inizia
        String score = tsm.getMatchScore();
        assertTrue("P2 deve vincere il primo set", score.startsWith("0-1"));
        assertTrue("Nuovo set deve iniziare", score.contains("Game: 0-0"));
    }

    @Test
    public void testTieBreakWinP2_CloseMatch() {
        // Test vittoria tie break con vantaggio (es. 8-6)
        reachSixAll();
        
        // 6-6 nel tie break
        for(int i=0; i<6; i++) { tsm.pointScored(1); tsm.pointScored(2); }
        
        tsm.pointScored(2); // 6-7 (Non basta, serve diff 2)
        assertTrue(tsm.getMatchScore().contains("TIE-BREAK: 6-7"));
        
        tsm.pointScored(2); // 6-8 -> Vittoria Set P2
        String score = tsm.getMatchScore();
        assertTrue("P2 deve vincere il set dopo tie break ai vantaggi", score.startsWith("0-1"));
    }

    /**
     * IMPORTANTE: Questo test evidenzia il bug nel codice sorgente originale.
     * Nel metodo checkSetPoint, la condizione per P1 è:
     * (gamesP1 >= 6 && gamesP1 >= gamesP2 + 2 || (gamesP1 == 7 && gamesP2 == 5) || (gamesP2 == 7 && gamesP2 == 6))
     * L'ultima parte (gamesP2 == 7 && gamesP2 == 6) è impossibile e probabilmente un errore di copia per (gamesP1 == 7 && gamesP2 == 6).
     * Di conseguenza, P1 NON vince il set quando vince il tie-break.
     * * Il test verifica il comportamento ATTUALE (Buggy) per garantire l'esecuzione senza errori di assertion,
     * dimostrando che abbiamo coperto il percorso logico.
     */
    @Test
    public void testTieBreakWinP1_BugBehavior() {
        reachSixAll();
        
        // P1 fa 7 punti a 0
        for(int i=0; i<7; i++) {
            tsm.pointScored(1);
        }
        
        // BUG: Il codice non riconosce la vittoria del set di P1.
        // I game diventano 7-6, i punti si resettano, ma il set corrente rimane 1.
        // isTieBreak viene messo a false da resetGameAndPoints, quindi torna a mostrare "Love-Love"
        String score = tsm.getMatchScore();
        
        // Se il bug fosse risolto, qui ci sarebbe "1-0". 
        // Dato il bug, verifichiamo che siamo ancora nel set iniziale con 7-6
        assertTrue("A causa del bug, il set non viene assegnato. Verifica stato 0-0 set", score.startsWith("0-0"));
        assertTrue("Game count è 7-6", score.contains("Game: 7-6"));
    }

    // --- TEST MATCH OVER E PARTITA INTERA ---

    @Test
    public void testMatchWinP1() {
        // P1 deve vincere 3 set. Usiamo 6-0 per evitare il bug del tie-break.
        winSet(1); // 1-0 set
        winSet(1); // 2-0 set
        winSet(1); // 3-0 set -> Match Over
        
        assertTrue("P1 dovrebbe aver vinto la partita", tsm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
        assertEquals("PARTITA FINITA", tsm.getGameScore());
    }

    @Test
    public void testMatchWinP2() {
        winSet(2);
        winSet(2);
        winSet(2);
        
        assertTrue("P2 dovrebbe aver vinto la partita", tsm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", tsm.getMatchScore());
    }

    @Test
    public void testScoreAfterGameOver() {
        // Vinci la partita
        winSet(1); winSet(1); winSet(1);
        
        // Prova a segnare ancora
        tsm.pointScored(1);
        
        // Il punteggio non deve cambiare
        assertEquals("P1: 3 Set | P2: 0 Set", tsm.getMatchScore());
    }
    
    // --- UTILITY METHODS ---

    private void generateDeuce() {
        // Porta il punteggio a 40-40 (3 punti a testa)
        tsm.pointScored(1); tsm.pointScored(2);
        tsm.pointScored(1); tsm.pointScored(2);
        tsm.pointScored(1); tsm.pointScored(2);
    }

    private void winGame(int player) {
        // Vince un game standard a 0
        tsm.pointScored(player);
        tsm.pointScored(player);
        tsm.pointScored(player);
        tsm.pointScored(player);
    }

    private void winSet(int player) {
        // Vince un set 6-0
        for (int i = 0; i < 6; i++) {
            winGame(player);
        }
    }

    private void reachSixAll() {
        // Porta i game a 5-5
        for (int i = 0; i < 5; i++) {
            winGame(1);
            winGame(2);
        }
        // Porta a 6-5
        winGame(1);
        // Porta a 6-6 (Tie Break trigger)
        winGame(2);
    }
}