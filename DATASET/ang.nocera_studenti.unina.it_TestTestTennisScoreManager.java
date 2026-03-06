/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: ang.nocera@studenti.unina.it
UserID: 401
Date: 20/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
	@BeforeClass
	public static void setUpClass() {
		// Eseguito una volta prima dell'inizio dei test nella classe
		// Inizializza risorse condivise 
		// o esegui altre operazioni di setup
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
		// Effettua la pulizia delle risorse condivise 
		// o esegui altre operazioni di teardown
	}
				
	@Before
	public void setUp() {
		// Eseguito prima di ogni metodo di test
		// Preparazione dei dati di input specifici per il test
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
		// Pulizia delle risorse o ripristino dello stato iniziale
	}
				
    /**
     * Helper: Fa segnare N punti al giocatore specificato.
     */
    private void scorePoints(int player, int count) {
        for (int i = 0; i < count; i++) {
            manager.pointScored(player);
        }
    }

    /**
     * Helper: Fa vincere un game standard (a 0) al giocatore specificato.
     */
    private void winGame(int player) {
        scorePoints(player, 4);
    }

    /**
     * Helper: Porta il set corrente sul punteggio specificato (es. 5-5).
     */
    private void reachSetScore(int gamesP1, int gamesP2) {
        // Resetta per sicurezza se non siamo all'inizio
        manager.resetGameAndPoints();
        
        int max = Math.max(gamesP1, gamesP2);
        for (int i = 0; i < max; i++) {
            if (i < gamesP1) winGame(1);
            if (i < gamesP2) winGame(2);
        }
    }

    // --- Test Iniziali e Base ---

    @Test
    public void testInitialScore() {
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testBasicPointsProgression() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1); // 30-15
        manager.pointScored(1); // 40-15
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayer() {
        String scoreBefore = manager.getGameScore();
        manager.pointScored(3); // Giocatore non valido
        manager.pointScored(-1);
        assertEquals("Il punteggio non deve cambiare con input invalido", scoreBefore, manager.getGameScore());
    }

    // --- Test Deuce e Vantaggi ---

    @Test
    public void testDeuceLogic() {
        scorePoints(1, 3); // 40-0
        scorePoints(2, 3); // 40-40
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        scorePoints(1, 3);
        scorePoints(2, 3); // Deuce
        manager.pointScored(1); // Adv P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(2); // Ritorna Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugBehavior() {
        // NOTA: Nel codice originale c'è un bug nella condizione:
        // if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)
        // Questa condizione è sempre falsa, quindi cade nel "Errore Game".
        // Il test verifica che il codice non crashi e ritorni il valore di fallback.
        
        scorePoints(1, 3);
        scorePoints(2, 3); // Deuce
        manager.pointScored(2); // Adv P2 (teorico)
        
        // Qui ci aspettiamo il fallback previsto dal codice attuale
        assertEquals("Errore Game", manager.getGameScore());
        
        // Verifichiamo che comunque P2 possa vincere il game segnando un altro punto
        manager.pointScored(2);
        assertTrue("P2 dovrebbe aver vinto il game nonostante il bug della stringa", 
                   manager.getMatchScore().contains("Game: 0-1"));
    }

    // --- Test Vittoria Game e Set Standard ---

    @Test
    public void testWinGameP1() {
        scorePoints(1, 4); // Vince a 0
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", manager.getGameScore()); // Punti resettati
    }

    @Test
    public void testWinSetP1_6_0() {
        for(int i=0; i<6; i++) {
            winGame(1);
        }
        // Dovrebbe aver vinto il set e passato al set 2
        assertTrue("Il match score dovrebbe indicare 1 set a 0", manager.getMatchScore().startsWith("1-0"));
        assertTrue("Dovrebbe resettare i game per il nuovo set", manager.getMatchScore().contains("Game: 0-0"));
    }
    
    @Test
    public void testWinSetP2_6_4() {
        reachSetScore(4, 5); // P2 in vantaggio 5-4
        winGame(2); // P2 vince 6-4
        
        assertTrue("P2 dovrebbe vincere il set", manager.getMatchScore().startsWith("0-1"));
    }

    // --- Test Set Lunghi e Tie-Break ---

    @Test
    public void testWinSetP1_7_5() {
        reachSetScore(5, 5);
        winGame(1); // 6-5
        assertFalse("Il set non deve finire sul 6-5", manager.getMatchScore().startsWith("1-0"));
        
        winGame(1); // 7-5
        assertTrue("Il set deve finire sul 7-5", manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void testTieBreakTrigger() {
        reachSetScore(6, 6); // Attiva Tie-Break
        assertTrue("Lo score deve mostrare TIE-BREAK", manager.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }

    @Test
    public void testTieBreakScoring() {
        reachSetScore(6, 6);
        manager.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
    }

    @Test
    public void testWinSetP2_TieBreak() {
        // Testiamo la vittoria di P2 al Tie Break (7-6)
        // Nota: Testiamo P2 perché nel codice originale la condizione di vittoria 7-6 per P1 
        // contiene un bug logico (gamesP2 == 7 && gamesP2 == 6) che la rende irraggiungibile.
        // La condizione per P2 invece è corretta (gamesP2 == 7 && gamesP1 == 6).
        
        reachSetScore(6, 6); // Tie Break iniziato
        
        // P2 vince il tie break 7-0
        scorePoints(2, 7); 
        
        assertTrue("P2 deve aver vinto il set dopo il tie break", manager.getMatchScore().startsWith("0-1"));
    }

    // --- Test Partita Completa ---

    @Test
    public void testFullMatchP2Wins() {
        // P2 vince 3 set consecutivi
        // Set 1
        for(int i=0; i<6; i++) winGame(2);
        // Set 2
        for(int i=0; i<6; i++) winGame(2);
        // Set 3
        for(int i=0; i<6; i++) winGame(2);
        
        assertTrue("La partita deve risultare finita", manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
        
        // Testiamo che segnare punti a partita finita non cambi nulla
        manager.pointScored(1);
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    // --- Test Copertura Rami Getter ---

    @Test
    public void testGetGameScoreGameOver() {
        // Forza game over
        for(int i=0; i<18; i++) winGame(1); // 3 set da 6-0
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }
    
    @Test
    public void testManualReset() {
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
        
        winGame(1); // 1-0 games
        manager.resetGameAndPoints(); // Resetta tutto nel set corrente
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
}
				
	// Aggiungi altri metodi di test se necessario
}

						