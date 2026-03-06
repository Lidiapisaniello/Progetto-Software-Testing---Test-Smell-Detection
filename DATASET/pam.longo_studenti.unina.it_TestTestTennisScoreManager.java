/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: pam.longo@studenti.unina.it
UserID: 793
Date: 25/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

// Nuovi import necessari per la cattura dell'output System.out
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out; 

    
    // --- Metodi Helper ---
    
    /**
     * Simula la vittoria di un singolo game da parte di un giocatore.
     * @param player Il giocatore che segna (1 o 2).
     */
    private void winGameHelper(int player) {
        for (int i = 0; i < 4; i++) {
            manager.pointScored(player);
        }
    }
    
    // --- Setup e Teardown ---
    
	@BeforeClass
	public static void setUpClass() {
		System.out.println("Inizio test TennisScoreManager");
	}
				
	@AfterClass
	public static void tearDownClass() {
		System.out.println("Fine test TennisScoreManager.");
	}
				
	@Before
	public void setUp() {
		manager = new TennisScoreManager();
        
        System.setOut(new PrintStream(outContent));
	}
				
	@After
	public void tearDown() {
       
        System.setOut(originalOut);
        outContent.reset(); 
		manager = null;
	}
	
    // --- Test Punteggi Base ---
	@Test
    public void testPuntoSingolo() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
    }


    @Test
    public void testGiocatoreNonValido() {
        String punteggioIniziale = manager.getGameScore();
        manager.pointScored(3);
        assertEquals(punteggioIniziale, manager.getGameScore());
    }

    @Test
    public void testVittoriaGameStandard() {
        
        winGameHelper(1); // P1 vince il game
        
        // Verifica il punteggio del match (0-0 Sets) e il reset dei Games (1-0)
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }
	
	@Test
    public void testVittoriaSetStandard() {
        
        for (int i = 0; i < 6; i++) {
            winGameHelper(1);
        }
        
        // Verifica la vittoria del Set (1-0 Sets)
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.startsWith("1-0")); 
    }

    // --- Test Deuce e Vantaggio (COPERTURA BUG) ---
	@Test
    public void testDeuceAndAdvantage() {
        
        // 1. Arriva a Deuce
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }
        
        manager.pointScored(1); // 40-30
        manager.pointScored(2); // Deuce
        assertEquals("Deuce", manager.getGameScore());
        
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(2); // Deuce (ritorno)
        assertEquals("Deuce", manager.getGameScore());
        
        // 2. Vantaggio P2 che INNESCA IL BUG DELLA SUT
        manager.pointScored(2); 
       
        assertEquals("Errore Game", manager.getGameScore()); 
        
        // 3. P2 vince il game per concludere il test
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("Game: 0-1")); 
    }
	
    // --- Test Vittoria Partita ---
	@Test
    public void testPartitaVintaDaPlayer1() {
        
        for (int set = 0; set < 3; set++) {
            for (int game = 0; game < 6; game++) {
                winGameHelper(1);
            }
        }
        
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
    }

	@Test
    public void testPartitaVintaDaPlayer2() {
        for (int set = 0; set < 3; set++) {
            for (int game = 0; game < 6; game++) {
                winGameHelper(2);
            }
        }

        assertTrue(manager.isGameOver());
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("P2: 3 Set"));
    }
	
    // --- Test Tie-Break (COPERTURA BUG) ---
	@Test
    public void testTieBreakWinByPlayer2() {
        
        // 1. Simula 6-6 Games
        for (int g = 0; g < 6; g++) {
            winGameHelper(1); 
            winGameHelper(2); 
        }

        // 2. P2 vince Tie-Break 7-5 (innesca la logica checkTieBreakPoint)
        for (int i = 0; i < 5; i++) 
            manager.pointScored(1); 
        for (int i = 0; i < 7; i++) 
            manager.pointScored(2); 

        
        // 3. Verifica lo stato buggato (reset dei game)
        String matchScore = manager.getMatchScore();
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }
    
    @Test
    public void testTieBreakWinByPlayer1() {
        
        // 1. Simula 6-6 Games
        for (int g = 0; g < 6; g++) {
            winGameHelper(1); 
            winGameHelper(2); 
        }

        // 2. P1 vince Tie-Break 7-5 (innesca la logica checkTieBreakPoint)
        for (int i = 0; i < 5; i++) 
            manager.pointScored(2); 
        for (int i = 0; i < 7; i++) 
            manager.pointScored(1); 

      
        
        // 3. Verifica lo stato buggato (reset dei game)
        String matchScore = manager.getMatchScore();
        assertEquals("0-0 (Game: 0-0 Love-Love)", matchScore);
    }
    
    // --- Test per coprire if (isGameOver()) nel metodo pointScored ---
	@Test
    public void testPointScoredAfterMatchIsOver() {
        
        // 1. Porta la partita allo stato Game Over (P1 vince 3 set)
        for (int set = 0; set < 3; set++) {
            for (int game = 0; game < 6; game++) {
                winGameHelper(1);
            }
        }
        
        
        assertTrue(manager.isGameOver()); 
        
        
        outContent.reset(); 

       
        manager.pointScored(1); 

        
        String output = outContent.toString().trim();
        
       
        assertTrue(output.contains("La partita è finita! Punteggio finale: P1: 3 Set | P2: 0 Set"));
        
        
        assertTrue(manager.isGameOver());
    }


    @Test
    public void testDeuceAndAdvantageLogicCoverage() {

        // 1. Arriva a Deuce
        // Copre: if (scoreP1 == scoreP2 && scoreP1 >= 3) { return "Deuce"; }
        for (int i = 0; i < 3; i++) {
            manager.pointScored(1);
            manager.pointScored(2);
        }

        manager.pointScored(1); // 40-30
        manager.pointScored(2); // Deuce
        assertEquals("Deuce", manager.getGameScore()); // COPERTO: Deuce

        // 2. Vantaggio P1
        // Copre: if (scoreP1 >= 3 && scoreP1 == scoreP2 + 1) { return "Vantaggio P1"; }
        manager.pointScored(1); 
        assertEquals("Vantaggio P1", manager.getGameScore()); // COPERTO: Vantaggio P1

        // 3. Ritorno a Deuce
        manager.pointScored(2); 
        assertEquals("Deuce", manager.getGameScore()); // COPERTO: Ritorno a Deuce

        // 4. Vantaggio P2 che INNESCA IL BUG
        manager.pointScored(2); 

        assertEquals("Errore Game", manager.getGameScore()); // COPERTO: if Vantaggio P2 fallito, Errore Game

        // P2 segna ancora per terminare il game e resettare lo stato
        manager.pointScored(2);

        assertEquals("Love-Love", manager.getGameScore());
    }

	@Test
    public void testStandardPointProgressionCoverage() {

        // 1. 15-Love (1-0)
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());

        // 2. 30-Love (2-0)
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());

        // 3. 40-Love (3-0)
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore());

        // --- Reset e nuovi casi ---
        manager = new TennisScoreManager(); // Reset dello stato

        // 4. 40-30 (3-2)
        for (int i = 0; i < 3; i++) manager.pointScored(1);
        for (int i = 0; i < 2; i++) manager.pointScored(2);
        assertEquals("40-30", manager.getGameScore());

        // 5. 15-30 (1-2)
        manager = new TennisScoreManager(); // Reset dello stato
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(2);
        assertEquals("15-30", manager.getGameScore());
    }	
  	
  	@Test
    public void testVittoriaGameStandard() {

        // P1 vince un game (Games 1-0, isTieBreak = falso)
        winGameHelper(1); 

        assertTrue(manager.getMatchScore().contains("Game: 1-0"));

        // Per completezza, verifichiamo anche il punteggio iniziale (Sets 0-0)
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }
	
}

						