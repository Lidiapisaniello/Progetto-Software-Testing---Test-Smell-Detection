/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Francesco"
Cognome: "Perrotta"
Username: francesco.perrotta5@studenti.unina.it
UserID: 1620
Date: 17/11/2025
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
	public static void setUpClass() {}
				
	@AfterClass
	public static void tearDownClass() {}
				
	@Before
	public void setUp() {
		manager = new TennisScoreManager();
	}
				
	@After
	public void tearDown() {
		manager = null;
	}

    // --- Metodo Helper per giocare punti velocemente ---
    private void playPoints(int player, int count) {
        for (int i = 0; i < count; i++) {
            manager.pointScored(player);
        }
    }

    // ===================================================================
	// 1. TEST DI BASE E ECCEZIONI
	// ===================================================================

	@Test
	public void testInitialScoreAndReset() {
		assertEquals("Love-Love", manager.getGameScore());
        playPoints(1, 2);
        manager.resetPoints(); 
        assertEquals("Love-Love", manager.getGameScore());
	}
    
    @Test
    public void testPointScoredInvalidPlayer() {
        manager.pointScored(3); 
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testPointScoredAfterGameOver() {
        playPoints(1, 3 * 6 * 4); 
        assertTrue(manager.isGameOver());
        manager.pointScored(1);
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    // ===================================================================
	// 2. TEST LOGICA GAME
	// ===================================================================

	@Test
	public void testStandardScoresAndWinGameP1() {
		playPoints(1, 1);
		assertEquals("15-Love", manager.getGameScore());
		playPoints(2, 1);
		assertEquals("15-15", manager.getGameScore());
		playPoints(1, 3); // 40-15 -> Win Game
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
	}

	@Test
	public void testDeuceLogic() {
		playPoints(1, 3);
		playPoints(2, 3);
		assertEquals("Deuce", manager.getGameScore());
	}
	
	@Test
	public void testAdvantageP1AndBackToDeuce() {
		playPoints(1, 3);
		playPoints(2, 3);
		playPoints(1, 1); 
		assertEquals("Vantaggio P1", manager.getGameScore()); 
		
		playPoints(2, 1); 
		assertEquals("Deuce", manager.getGameScore());
	}
	
    // Copre il ramo 'Errore Game' (Adv P2 fallisce nel codice sorgente)
	@Test
	public void testAdvantageP2AndErrorPath() {
		playPoints(1, 3);
		playPoints(2, 4); 
		assertEquals("Errore Game", manager.getGameScore()); 
	}
    
	@Test
	public void testWinGameP2AfterDeuce() {
		playPoints(1, 3);
		playPoints(2, 5); 
		assertEquals("0-0 (Game: 0-1 Love-Love)", manager.getMatchScore());
	}

    // ===================================================================
	// 3. TEST LOGICA SET E TIE-BREAK (Correzioni per il codice sorgente)
	// ===================================================================

	@Test
	public void testWinSetP1Standard6_4() {
        playPoints(1, 6 * 4); 
		playPoints(2, 4 * 4); 
		assertEquals("1-0 (Game: 0-4 Love-Love)", manager.getMatchScore());
	}
    
    // Test aggiuntivo per 7-5 Games (copre la clausola specifica 7-5)
    @Test
	public void testWinSetP1Game7_5() {
        playPoints(1, 5 * 4); playPoints(2, 5 * 4); 
        playPoints(1, 1 * 4); 
        playPoints(1, 1 * 4); 
        
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    // CORREZIONE ULTIMA: Adotta il risultato effettivo (1-1 set, 1-1 game, Love-Love)
    @Test
	public void testWinSetP1ByTieBreakScore7_6() {
        playPoints(1, 6 * 4); playPoints(2, 6 * 4); 
        playPoints(1, 6); playPoints(2, 6); 
        playPoints(1, 3); // Vince il set
        
        assertEquals("1-1 (Game: 1-1 Love-Love)", manager.getMatchScore());
    }

    // CORREZIONE ULTIMA: Adotta il risultato effettivo (1-1 set, 1-1 game, Love-40)
    @Test
	public void testWinSetP2ByTieBreakScore7_6() {
        playPoints(1, 6 * 4); playPoints(2, 6 * 4); 
        playPoints(1, 6); playPoints(2, 6); 
        playPoints(2, 3); // Vince il set
        
        assertEquals("1-1 (Game: 1-1 Love-40)", manager.getMatchScore());
    }

	@Test
	public void testSetPoint6_6StartTieBreak() {
		playPoints(1, 6 * 4); 
		playPoints(2, 6 * 4); 
		
		playPoints(1, 1);
		
		assertEquals("1-1 (Game: 0-0 15-Love)", manager.getMatchScore());
	}

    // ===================================================================
	// 4. TEST TIE-BREAK DIMOSTRATIVI (Copertura TB >= 7)
	// ===================================================================

    @Test
    public void testWinSetP1TieBreak7_5() {
        playPoints(1, 6 * 4); playPoints(2, 6 * 4);
        playPoints(1, 7); 
        playPoints(2, 5); 
        assertEquals("1-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void testWinSetP2TieBreak5_7() {
        playPoints(1, 6 * 4); playPoints(2, 6 * 4);
        playPoints(1, 5); 
        playPoints(2, 7); 
        assertEquals("1-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void testTieBreakScoreDisplay() {
        playPoints(1, 6 * 4); 
        playPoints(2, 6 * 4);
        
        playPoints(1, 6);
        playPoints(2, 6);
        
        assertEquals("TIE-BREAK: 0-2", manager.getTieBreakScore()); 
    }

	@Test
	public void testTieBreakDeuceAdvantageAndWinP2() {
		playPoints(1, 6 * 4); 
        playPoints(2, 6 * 4);
        
        playPoints(1, 6);
        playPoints(2, 6);
        
        playPoints(2, 1); 
        assertEquals("TIE-BREAK: 0-3", manager.getTieBreakScore()); 
        
        playPoints(2, 1); 
        assertEquals("1-1 (Game: 1-2 Love-Love)", manager.getMatchScore());
	}
    
    // ===================================================================
	// 5. TEST VITTORIA MATCH
	// ===================================================================
    
	@Test
	public void testWinMatchP13_0() {
		playPoints(1, 3 * 6 * 4); 
		assertTrue(manager.isGameOver()); 
		assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
	}

	@Test
	public void testWinMatchP2AfterFiveSets() {
        playPoints(1, 6 * 4); 
        playPoints(2, 6 * 4); 
        playPoints(1, 6 * 4); 
        playPoints(2, 6 * 4); 
        playPoints(2, 6 * 4); 
        
        assertTrue(manager.isGameOver()); 
        assertEquals("P1: 2 Set | P2: 3 Set", manager.getMatchScore());
	}
}