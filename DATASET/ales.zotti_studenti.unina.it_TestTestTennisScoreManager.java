/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Gemini"
Cognome: "Assistant"
Username: ales.zotti@studenti.unina.it
UserID: 245
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
	
	private TennisScoreManager scoreManager;
	
	@BeforeClass
	public static void setUpClass() {
		// Eseguito una volta prima dell'inizio dei test nella classe
	}
				
	@AfterClass
	public static void tearDownClass() {
		// Eseguito una volta alla fine di tutti i test nella classe
	}
				
	@Before
	public void setUp() {
		// Eseguito prima di ogni metodo di test
		scoreManager = new TennisScoreManager();
	}
				
	@After
	public void tearDown() {
		// Eseguito dopo ogni metodo di test
	}
				
	/**
	 * Testa il punteggio standard dei punti (Love, 15, 30, 40)
	 */
	@Test
	public void testStandardGameScore() {
		assertEquals("Love-Love", scoreManager.getGameScore());
		
		scoreManager.pointScored(1); // P1 15-Love
		assertEquals("15-Love", scoreManager.getGameScore());
		
		scoreManager.pointScored(2); // 15-15
		assertEquals("15-15", scoreManager.getGameScore());
		
		scoreManager.pointScored(1); // 30-15
		assertEquals("30-15", scoreManager.getGameScore());
		
		scoreManager.pointScored(2); // 30-30
		assertEquals("30-30", scoreManager.getGameScore());
		
		scoreManager.pointScored(1); // 40-30
		assertEquals("40-30", scoreManager.getGameScore());
		
		scoreManager.pointScored(2); // 40-40 (Deuce, testato in un altro metodo)
	}

	/**
	 * Testa il Deuce, il Vantaggio e la vincita del game.
	 * Copre i rami in checkGamePoint che portano alla vittoria del game.
	 */
	@Test
	public void testDeuceAdvantageAndGameWin() {
		// Porta il punteggio a 40-40 (Deuce)
		scoreManager.pointScored(1); // 15-Love
		scoreManager.pointScored(2); // 15-15
		scoreManager.pointScored(1); // 30-15
		scoreManager.pointScored(2); // 30-30
		scoreManager.pointScored(1); // 40-30
		scoreManager.pointScored(2); // Deuce
		assertEquals("Deuce", scoreManager.getGameScore());
		
		// Vantaggio P1
		scoreManager.pointScored(1); 
		assertEquals("Vantaggio P1", scoreManager.getGameScore());
		
		// Ritorno a Deuce
		scoreManager.pointScored(2); 
		assertEquals("Deuce", scoreManager.getGameScore());
		
		// Vantaggio P2
		scoreManager.pointScored(2); 
		assertEquals("Vantaggio P2", scoreManager.getGameScore());
		
		// Ritorno a Deuce (di nuovo)
		scoreManager.pointScored(1); 
		assertEquals("Deuce", scoreManager.getGameScore());

		// Vantaggio P1
		scoreManager.pointScored(1);
		assertEquals("Vantaggio P1", scoreManager.getGameScore());

		// P1 vince il game (P1 ha 1 game, punteggio game resettato)
		scoreManager.pointScored(1); 
		assertEquals("Love-Love", scoreManager.getGameScore());
		// Verifica che il contatore dei game sia aumentato
		assertEquals("0-0 (Game: 1-0 Love-Love)", scoreManager.getMatchScore());
	}

	/**
	 * Testa la vincita di un game senza Deuce (es. 40-Love).
	 * Copre il ramo della vincita diretta in checkGamePoint.
	 */
	@Test
	public void testDirectGameWin() {
		scoreManager.pointScored(2); // Love-15
		scoreManager.pointScored(2); // Love-30
		scoreManager.pointScored(2); // Love-40
		
		// P2 vince il game
		scoreManager.pointScored(2); 
		assertEquals("Love-Love", scoreManager.getGameScore());
		assertEquals("0-0 (Game: 0-1 Love-Love)", scoreManager.getMatchScore());
	}
	
	/**
	 * Testa l'arrivo al Tie-Break (6-6 nei game) e il punteggio.
	 * Copre il ramo del Tie-Break in checkSetPoint e isTieBreak a true.
	 */
	@Test
	public void testTieBreakTransitionAndScore() {
		// Porta il punteggio a 6-5 per P1 nei game
		for (int i = 0; i < 5; i++) { // P1 vince 5 games
			winGame(1);
		}
		for (int i = 0; i < 5; i++) { // P2 vince 5 games
			winGame(2);
		}
		winGame(1); // 6-5 P1
		
		// P2 vince un game per arrivare a 6-6
		winGame(2); // 6-6, dovrebbe iniziare il Tie-Break
		
		assertTrue("Dovrebbe essere in modalità Tie-Break", scoreManager.isTieBreak);
		assertEquals("TIE-BREAK: 0-0", scoreManager.getTieBreakScore());
		
		// Punti del Tie-Break
		scoreManager.pointScored(1); // 1-0
		scoreManager.pointScored(1); // 2-0
		scoreManager.pointScored(2); // 2-1
		assertEquals("TIE-BREAK: 2-1", scoreManager.getTieBreakScore());
	}

	/**
	 * Testa la vincita del Set tramite Tie-Break (es. 7-5 nel Tie-Break).
	 * Copre il ramo della vittoria del Tie-Break in checkTieBreakPoint.
	 */
	@Test
	public void testTieBreakSetWin_7_5() {
		// Prepara 6-6 nei game
		prepareTieBreak();
		
		// P1 porta il Tie-Break a 6-5
		for (int i = 0; i < 6; i++) {
			scoreManager.pointScored(1);
		}
		for (int i = 0; i < 5; i++) {
			scoreManager.pointScored(2);
		}
		
		// P1 vince il Tie-Break (7-5), P1 vince il set 7-6
		scoreManager.pointScored(1); 
		assertFalse("Non dovrebbe più essere in Tie-Break", scoreManager.isTieBreak);
		// Verifica che il set sia stato registrato e che la partita sia a 1-0 set
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}
	
	/**
	 * Testa la vincita del Set tramite Tie-Break (es. 7-6 nel Tie-Break, con Deuce).
	 * Copre il ramo della vittoria del Tie-Break dopo 6-6.
	 */
	@Test
	public void testTieBreakSetWin_Over7Points() {
		// Prepara 6-6 nei game
		prepareTieBreak();
		
		// Punteggio a 6-6 nel Tie-Break
		for (int i = 0; i < 6; i++) {
			scoreManager.pointScored(1);
			scoreManager.pointScored(2);
		}
		assertEquals("TIE-BREAK: 6-6", scoreManager.getTieBreakScore());
		
		// 7-6 P1
		scoreManager.pointScored(1);
		assertEquals("TIE-BREAK: 7-6", scoreManager.getTieBreakScore());
		
		// 7-7 (ripristino del Deuce nel Tie-Break)
		scoreManager.pointScored(2);
		assertEquals("TIE-BREAK: 7-7", scoreManager.getTieBreakScore());

		// 8-7 P1 (Vantaggio)
		scoreManager.pointScored(1);
		assertEquals("TIE-BREAK: 8-7", scoreManager.getTieBreakScore());
		
		// 9-7 P1 vince il Tie-Break (9-7), P1 vince il set 7-6
		scoreManager.pointScored(1);
		assertFalse("Non dovrebbe più essere in Tie-Break", scoreManager.isTieBreak);
		// Verifica che il set sia stato registrato e che la partita sia a 1-0 set
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}
	
	/**
	 * Testa la vincita di un Set standard (es. 6-4).
	 * Copre il ramo della vincita del set in checkSetPoint.
	 */
	@Test
	public void testStandardSetWin_6_4() {
		// P1 vince 5 games, P2 vince 4 games
		for (int i = 0; i < 5; i++) { winGame(1); }
		for (int i = 0; i < 4; i++) { winGame(2); }
		
		// Punteggio attuale: Game 5-4. Set 0-0.
		assertEquals("0-0 (Game: 5-4 Love-Love)", scoreManager.getMatchScore());
		
		// P1 vince il game per il 6-4 e vince il Set
		winGame(1);
		
		// Punteggio partita dovrebbe essere 1-0 Set. Game e punti resettati.
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
		// Simula la vittoria di un altro punto per forzare la stampa del set (copertura printScore)
		scoreManager.pointScored(1);
	}
	
	/**
	 * Testa la vincita di un Set con un game di margine (es. 7-5).
	 * Copre il ramo (gamesP1 == 7 && gamesP2 == 5) in checkSetPoint.
	 */
	@Test
	public void testStandardSetWin_7_5() {
		// Punteggio 6-5 P1
		for (int i = 0; i < 6; i++) { winGame(1); }
		for (int i = 0; i < 5; i++) { winGame(2); }
		
		// P1 vince il game per il 7-5 e vince il Set
		winGame(1);
		
		// Punteggio partita dovrebbe essere 1-0 Set.
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}
	
	/**
	 * Testa la vincita della Partita (es. 3 Set a 0).
	 * Copre il ramo di isGameOver() e l'output in getMatchScore quando la partita è finita.
	 */
	@Test
	public void testMatchWin_3_0() {
		// P1 vince 3 set di fila (es. 6-4)
		for (int s = 0; s < 3; s++) {
			for (int i = 0; i < 5; i++) { winGame(1); }
			for (int i = 0; i < 4; i++) { winGame(2); }
			winGame(1); // 6-4 e Set Win
		}
		
		assertTrue("La partita dovrebbe essere finita", scoreManager.isGameOver());
		assertEquals("P1: 3 Set | P2: 0 Set", scoreManager.getMatchScore());
		
		// Tentativo di segnare dopo la fine della partita (copertura di pointScored)
		scoreManager.pointScored(1);
	}
	
	/**
	 * Testa la vincita della Partita con un punteggio più equilibrato (es. 3 Set a 2).
	 * Copre la vittoria in isGameOver dopo 5 set.
	 */
	@Test
	public void testMatchWin_3_2() {
		// P1 vince il 1° Set
		winSet(1);
		// P2 vince il 2° Set
		winSet(2);
		// P1 vince il 3° Set
		winSet(1);
		// P2 vince il 4° Set
		winSet(2);
		// P1 vince il 5° Set
		winSet(1); 
		
		assertTrue("La partita dovrebbe essere finita", scoreManager.isGameOver());
		assertEquals("P1: 3 Set | P2: 2 Set", scoreManager.getMatchScore());
	}
	
	/**
	 * Testa l'errore per giocatore non valido (copertura di pointScored).
	 */
	@Test
	public void testInvalidPlayer() {
		// Il punteggio dovrebbe rimanere Love-Love
		scoreManager.pointScored(3); 
		assertEquals("Love-Love", scoreManager.getGameScore());
		assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
		
		scoreManager.pointScored(0); 
		assertEquals("Love-Love", scoreManager.getGameScore());
	}
	
	/**
	 * Testa il metodo getGameScore in caso di errore logico (copertura del ramo "Errore Game").
	 * Questo ramo è difficile da raggiungere con un flusso normale, ma l'implementazione
	 * attuale di pointScored lo rende teoricamente inaccessibile per valori >= 4.
	 * Per testare l'errore, si possono manipolare i punteggi interni, ma ci si affida
	 * qui al corretto funzionamento di pointScored e checkGamePoint per non raggiungerlo.
	 * Se non viene raggiunto, l'implementazione è robusta.
	 * * Testiamo invece il ramo 'PARTITA FINITA' di getGameScore.
	 */
	@Test
	public void testGameOverGameScoreMessage() {
		winSet(1); winSet(1); winSet(1);
		assertTrue(scoreManager.isGameOver());
		assertEquals("PARTITA FINITA", scoreManager.getGameScore());
	}
	
	// --- Metodi Helper ---
	
	/**
	 * Simula la vincita di un game da parte del giocatore specificato (4 punti di fila).
	 * @param player Il giocatore che vince (1 o 2).
	 */
	private void winGame(int player) {
		for (int i = 0; i < 4; i++) {
			scoreManager.pointScored(player);
		}
	}
	
	/**
	 * Simula la vincita di un set standard (6-4) da parte del giocatore specificato.
	 * @param player Il giocatore che vince (1 o 2).
	 */
	private void winSet(int player) {
		int otherPlayer = (player == 1) ? 2 : 1;
		
		// 5-4 per il vincitore
		for (int i = 0; i < 5; i++) { winGame(player); }
		for (int i = 0; i < 4; i++) { winGame(otherPlayer); }
		
		// Vincita del 6° game per il set 6-4
		winGame(player);
	}
	
	/**
	 * Porta il punteggio dei game a 6-6 per forzare l'ingresso nel Tie-Break.
	 */
	private void prepareTieBreak() {
		// P1 vince 6 games
		for (int i = 0; i < 6; i++) { winGame(1); }
		// P2 vince 6 games -> Tie-Break
		for (int i = 0; i < 6; i++) { winGame(2); }
		
		assertTrue("Errore nella preparazione: non è in Tie-Break", scoreManager.isTieBreak);
	}
}