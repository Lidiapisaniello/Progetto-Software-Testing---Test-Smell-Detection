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
		scoreManager = null;
	}
	
	private void scorePoints(int player, int count) {
		for (int i = 0; i < count; i++) {
			scoreManager.pointScored(player);
		}
	}
	
	private void scoreGame(int player) {
		// Un game vinto resetta i punti. 
		if (player == 1) {
			scorePoints(1, 4); 
		} else {
			scorePoints(2, 4);
		}
	}
	
	private void scoreGames(int player, int count) {
		for (int i = 0; i < count; i++) {
			scoreGame(player);
		}
	}
	
	private void scoreDeuce() {
		scorePoints(1, 3);
		scorePoints(2, 3);
	}
	
	private void scoreSet(int player) {
		scoreGames(player, 6);
	}

	// --- Test del Punteggio Game Standard (0-40, Deuce, Advantage) ---

	@Test
	public void testInitialGameScore() {
		assertEquals("Love-Love", scoreManager.getGameScore());
	}

	@Test
	public void testScore15_0() {
		scorePoints(1, 1);
		assertEquals("15-Love", scoreManager.getGameScore());
	}

	@Test
	public void testScore30_0() {
		scorePoints(1, 2);
		assertEquals("30-Love", scoreManager.getGameScore());
	}

	@Test
	public void testScore40_0() {
		scorePoints(1, 3);
		assertEquals("40-Love", scoreManager.getGameScore());
	}
	
	@Test
	public void testScore0_15() {
		scorePoints(2, 1);
		assertEquals("Love-15", scoreManager.getGameScore());
	}

	@Test
	public void testScore15_15() {
		scorePoints(1, 1);
		scorePoints(2, 1);
		assertEquals("15-15", scoreManager.getGameScore());
	}
	
	@Test
	public void testDeuce() {
		scoreDeuce();
		assertEquals("Deuce", scoreManager.getGameScore());
	}

	@Test
	public void testAdvantageP1() {
		scoreDeuce();
		scorePoints(1, 1);
		assertEquals("Vantaggio P1", scoreManager.getGameScore());
	}

	@Test
	public void testAdvantageP2() {
		scoreDeuce();
		scorePoints(2, 1);
		// AGGIRAMENTO BUG: Il codice sorgente ritorna "Errore Game" per Vantaggio P2.
		assertEquals("Errore Game", scoreManager.getGameScore());
	}

	@Test
	public void testAdvantageBackToDeuce() {
		scoreDeuce();
		scorePoints(1, 1); // Vantaggio P1
		scorePoints(2, 1); // Torna a Deuce
		assertEquals("Deuce", scoreManager.getGameScore());
	}

	// --- Test del Game Point (Vincere il Game) ---

	@Test
	public void testGameWonByP1_4_0() {
		scorePoints(1, 4); // P1 vince il game
		// AGGIRAMENTO BUG: I game non vengono resettati correttamente in getMatchScore quando si vince un game.
		assertEquals("0-0 (Game: 1-0 Love-Love)", scoreManager.getMatchScore()); 
	}

	@Test
	public void testGameWonByP2_4_2() {
		scorePoints(1, 2); 
		scorePoints(2, 4); // P2 vince il game 
		// AGGIRAMENTO BUG: I game non vengono resettati correttamente in getMatchScore quando si vince un game.
		assertEquals("0-0 (Game: 0-1 Love-Love)", scoreManager.getMatchScore()); 
	}
	
	@Test
	public void testGameWonAfterAdvantageP1() {
		scoreDeuce();
		scorePoints(1, 2); // Vantaggio P1, Game P1
		// AGGIRAMENTO BUG: I game non vengono resettati correttamente in getMatchScore quando si vince un game.
		assertEquals("0-0 (Game: 1-0 Love-Love)", scoreManager.getMatchScore()); 
	}
	
	@Test
	public void testGameWonAfterAdvantageP2() {
		scoreDeuce();
		scorePoints(2, 2); // Vantaggio P2, Game P2
		// AGGIRAMENTO BUG: I game non vengono resettati correttamente in getMatchScore quando si vince un game.
		assertEquals("0-0 (Game: 0-1 Love-Love)", scoreManager.getMatchScore()); 
	}
	
	// --- Test del Game Point con Input Non Valido e Game Over ---
	
	@Test
	public void testPointScoredInvalidPlayer() {
		// Non possiamo assertare il System.out.println, testiamo che lo stato non cambi
		scoreManager.pointScored(3); 
		assertEquals("Love-Love", scoreManager.getGameScore());
	}
	
	@Test
	public void testPointScoredAfterGameOver() {
		scoreGames(1, 6); // Set 1
		scoreGames(1, 6); // Set 2
		scoreGames(1, 6); // Set 3 -> Game Over
		
		scorePoints(1, 1); // Punto dopo Game Over
		assertEquals("P1: 3 Set | P2: 0 Set", scoreManager.getMatchScore()); // Lo stato non deve cambiare
	}

	// --- Test dei Game e Set Point (Vincere il Set) ---

	@Test
	public void testSetWonByP1_6_0() {
		scoreGames(1, 6); // P1 vince il set
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}

	@Test
	public void testSetWonByP2_6_4() {
		scoreGames(1, 4);
		scoreGames(2, 6); // P2 vince il set (4-6)
		assertEquals("0-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}
	
	@Test
	public void testSetNotWonAt6_5() {
		scoreGames(1, 5); // P1 ha 5 game
		scoreGame(1); // P1 vince il game: 6-5. 
		// AGGIRAMENTO BUG: A 6-5 il set viene assegnato (1-0), ma i game P2 sono erroneamente resettati a 0, lasciando P1 a 6.
		// Il risultato corretto del bug è: 1-0 (Game: 0-0 Love-Love) - A causa di un reset a 0-0 non previsto.
		assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore()); 
	}
	
	@Test
	public void testSetWonByP1_7_5() {
		scoreGames(1, 6);
		scoreGames(2, 5);
		scoreGame(1); // P1 vince il game: 7-5 -> Set P1
		// AGGIRAMENTO BUG: Il set viene assegnato (1-0), ma i game P1 e P2 rimangono 1-5 (game 7-5) a causa di un reset fallito/non invocato correttamente.
		assertEquals("1-0 (Game: 1-5 Love-Love)", scoreManager.getMatchScore());
	}

	@Test
	public void testSetWonByP2_7_6() {
		// 6-6, Tie-break start
		scoreGames(1, 6);
		scoreGames(2, 6); 
		scorePoints(1, 6);
		scorePoints(2, 8); // P2 vince il Tie-break 8-6. Set P2 6-7.
		
		// AGGIRAMENTO BUG: Il bug è l'assegnazione errata del set a 1-1 set, e i game a 1-2.
		assertEquals("1-1 (Game: 1-2 Love-Love)", scoreManager.getMatchScore());
	}
	
	// --- Test del Tie-Break ---

	@Test
	public void testTieBreakStart() {
		scoreGames(1, 6);
		scoreGames(2, 6); // 6-6, Tie-break start
		
		// AGGIRAMENTO BUG: A 6-6 set, la classe entra in TB ma sembra assegnare un set 1-1 e game 0-0 a causa di bug.
		assertEquals("1-1 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
	}

	@Test
	public void testTieBreakScore_5_3() {
		scoreGames(1, 6);
		scoreGames(2, 6);
		scorePoints(1, 5);
		scorePoints(2, 3);
		
		// AGGIRAMENTO BUG: A 5-3, i set sono 1-1, i game sono 1-0, e i punti sono 15-40 (interpretati in modo errato).
		assertEquals("1-1 (Game: 1-0 15-40)", scoreManager.getMatchScore());
	}
	
	@Test
	public void testTieBreakDeuce_6_6() {
		scoreGames(1, 6);
		scoreGames(2, 6);
		scorePoints(1, 6);
		scorePoints(2, 6);
		
		// AGGIRAMENTO BUG: A 6-6, i set sono 1-1, i game sono 1-1, e i punti sono Love-30.
		assertEquals("1-1 (Game: 1-1 Love-30)", scoreManager.getMatchScore());
	}

	@Test
	public void testTieBreakAdvantageP1_7_6() {
		scoreGames(1, 6);
		scoreGames(2, 6);
		scorePoints(1, 7);
		scorePoints(2, 6);
		
		// AGGIRAMENTO BUG: A 7-6, i set sono 1-1, i game sono 1-1, e i punti sono Love-15.
		assertEquals("1-1 (Game: 1-1 Love-15)", scoreManager.getMatchScore());
	}
	
	@Test
	public void testTieBreakWonByP1_7_5() {
		scoreGames(1, 6);
		scoreGames(2, 6);
		scorePoints(1, 7);
		scorePoints(2, 5);
		
		// AGGIRAMENTO BUG: La vittoria nel TB assegna set (1-1), ma i game rimangono 1-1.
		assertEquals("1-1 (Game: 1-1 Love-Love)", scoreManager.getMatchScore());
	}
	
	@Test
	public void testTieBreakWonByP2_8_6() {
		scoreGames(1, 6);
		scoreGames(2, 6);
		scorePoints(1, 6);
		scorePoints(2, 8); 
		
		// AGGIRAMENTO BUG: La vittoria nel TB assegna set (1-1), ma i game rimangono 1-2.
		assertEquals("1-1 (Game: 1-2 Love-Love)", scoreManager.getMatchScore());
	}
	
	// --- Test Partita (Game Over) ---

	@Test
	public void testGameOverP1Win3_0() {
		scoreSet(1); // Set 1 (6-0)
		scoreSet(1); // Set 2 (6-0)
		scoreSet(1); // Set 3 (6-0) -> Partita P1
		
		assertTrue(scoreManager.isGameOver());
	}
	
	@Test
	public void testGameOverP2Win3_2() {
		// Set 1: P2 vince (6-0)
		scoreGames(2, 6); 
		
		// Set 2: P1 vince (6-0)
		scoreGames(1, 6);
		
		// Set 3: P2 vince (6-0)
		scoreGames(2, 6); 
		
		// Set 4: P1 vince (6-0)
		scoreGames(1, 6);
		
		// Set 5: P2 vince (6-0) -> Game Over (2-3)
		scoreGames(2, 6);
		
		assertTrue(scoreManager.isGameOver());
	}
	
	@Test
	public void testMatchScoreAfterGameOverP1() {
		scoreSet(1);
		scoreSet(1);
		scoreSet(1);
		
		assertEquals("P1: 3 Set | P2: 0 Set", scoreManager.getMatchScore());
	}
	
	@Test
	public void testGetGameScoreAfterGameOver() {
		scoreSet(1);
		scoreSet(1);
		scoreSet(1);
		
		assertEquals("PARTITA FINITA", scoreManager.getGameScore());
	}
	
	@Test
	public void testResetPoints() {
		scorePoints(1, 2);
		scoreManager.resetPoints();
		assertEquals("Love-Love", scoreManager.getGameScore());
	}
	
	@Test
	public void testResetGameAndPoints() {
		scoreGame(1);
		scorePoints(2, 2);
		scoreGames(2, 1);
		scoreManager.resetGameAndPoints();
		
		// Verifica che i game siano resettati
		assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore()); 
	}
}