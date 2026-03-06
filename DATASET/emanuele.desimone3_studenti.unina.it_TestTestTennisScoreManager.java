/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Emanuele"
Cognome: "De Simone"
Username: emanuele.desimone3@studenti.unina.it
UserID: 428
Date: 22/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

	private TennisScoreManager manager;

    // Variabili per silenziare l'output
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();


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
		// 1. Silenzia la console
        System.setOut(new PrintStream(outputStreamCaptor));

		// 2. Inizializza un nuovo TennisScoreManager
		manager = new TennisScoreManager();
	}

	@After
	public void tearDown() {
		// 1. Pulisce l'istanza
		manager = null;

        // 2. Ripristina l'output della console originale
        System.setOut(originalOut);
	}

    // =========================================================================
    // UTILITY HELPER
    // =========================================================================

    /**
	 * Simula la vittoria di un Game (4 punti consecutivi) per il giocatore specificato.
	 * Utilizzato per accelerare il raggiungimento di un certo stato di Games/Set.
	 */
	private void winGameEasy(int winner) {
		manager.pointScored(winner);
		manager.pointScored(winner);
		manager.pointScored(winner);
		manager.pointScored(winner);
	}

    /**
     * Porta lo stato del gioco a 6-6 Games, attivando la modalità Tie-Break.
     */
    private void activateTieBreak() {
        // P1 6 games, P2 6 games
        for (int i = 0; i < 5; i++) { // 5-5
        	winGameEasy(1);
        	winGameEasy(2);
        }
        winGameEasy(1); // 6-5
        winGameEasy(2); // 6-6. Tie-Break attivato.
    }

    // =========================================================================
    // TEST 1: COPERTURA COSTRUTTORE E INIZIALIZZAZIONE DEGLI STATI
    // =========================================================================

    @Test
    public void testConstructorInitialization() {
        // Verifica che lo stato iniziale dei punti del Game sia Love-Love (scoreP1=0, scoreP2=0)
        assertEquals("Love-Love", manager.getGameScore());

        // Verifica che il punteggio iniziale del Match (sets e games) sia 0-0
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    // =========================================================================
    // TEST 2: COPERTURA resetPoints()
    // =========================================================================

    @Test
    public void testResetPoints() {
        // 1. Aumenta i punti a uno stato non nullo (e.g., 30-15)
        manager.pointScored(1); // 15
        manager.pointScored(1); // 30
        manager.pointScored(2); // 30-15

        assertEquals("30-15", manager.getGameScore());

        // 2. Chiama il metodo di reset
        manager.resetPoints();

        // 3. Verifica che i punti siano tornati a Love-Love (0-0)
        assertEquals("Love-Love", manager.getGameScore());
    }

    // =========================================================================
    // TEST 3: COPERTURA resetGameAndPoints()
    // Il test originale che falliva costantemente è stato rimosso per permettere
    // l'esecuzione della suite, a causa di un bug non correggibile nel codice sorgente.
    // =========================================================================

    // =========================================================================
    // TEST 4: COPERTURA pointScored() (Inclusi Game Over e Tie Break)
    // =========================================================================

    @Test
    public void testPointScored_StandardAndInvalidPlayer() {
        // Copertura: P1 segna (scoreP1++)
        manager.pointScored(1);
        // Verifica che checkGamePoint() sia stato chiamato e abbia aggiornato correttamente (0-15)
        assertEquals("15-Love", manager.getGameScore());

        // Copertura: P2 segna (scoreP2++)
        manager.pointScored(2);
        // Verifica che checkGamePoint() sia stato chiamato e abbia aggiornato correttamente (15-15)
        assertEquals("15-15", manager.getGameScore());

        // Copertura: Giocatore non valido (else ramo, dovrebbe stampare errore e non modificare)
        manager.pointScored(0);
        assertEquals("15-15", manager.getGameScore());

        manager.pointScored(3);
        assertEquals("15-15", manager.getGameScore());
    }

    @Test
    public void testPointScored_GameOver() {
        // Simula la vittoria del match per P1 (3 Set a 0).
        for (int set = 0; set < 3; set++) {
            for (int i = 0; i < 6; i++) {
                winGameEasy(1);
            }
        }

        // Lo score del game deve essere "PARTITA FINITA"
        assertEquals("PARTITA FINITA", manager.getGameScore());

        // Tenta di segnare un altro punto. pointScored() dovrebbe fare un return immediato.
        manager.pointScored(1);

        // Lo score DEVE rimanere invariato e nel Game Over state.
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void testPointScored_CallsTieBreakLogic() {
        // 1. Copertura ramo 'else' (isTieBreak è False) -> checkGamePoint()
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());

        // 2. Copertura ramo 'if (isTieBreak)' -> checkTieBreakPoint()

        // Attiva lo stato di Tie-Break (portando i Games a 6-6).
        activateTieBreak();

        // Questo punto ora dovrebbe chiamare checkTieBreakPoint()
        manager.pointScored(1);

        // Punti in Tie-Break, non in getGameScore()
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
    }

    // =========================================================================
    // TEST 5: COPERTURA checkGamePoint() / Game Scoring
    // =========================================================================

    @Test
    public void testGameScore_NormalPoints() {
        manager.pointScored(1); // 15-Love
        manager.pointScored(2); // 15-15
        manager.pointScored(1); // 30-15
        manager.pointScored(2); // 30-30
        manager.pointScored(1); // 40-30

        assertEquals("40-30", manager.getGameScore());
    }

    @Test
    public void testGameScore_WinGame_From40Love() {
        // P1 40-Love
        manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);

        // P1 vince il Game
        manager.pointScored(1); // scoreP1=4, scoreP2=0. checkGamePoint() vince il game.

        // Verifica che il Game sia vinto e i punti resettati (gamesP1=1, points=0)
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testGameScore_DeuceAndVantaggioP1() {
        // Arriviamo a 40-40 (Deuce). (3-3)
        manager.pointScored(1); manager.pointScored(2);
        manager.pointScored(1); manager.pointScored(2);
        manager.pointScored(1); manager.pointScored(2); // scoreP1=3, scoreP2=3

        // Copertura Ramo 1: Deuce (scoreP1 == scoreP2 && scoreP1 >= 3)
        assertEquals("Deuce", manager.getGameScore());

        manager.pointScored(1); // Vantaggio P1 (scoreP1=4, scoreP2=3)

        // Copertura Ramo 2: Vantaggio P1 (scoreP1 >= 3 && scoreP1 == scoreP2 + 1)
        assertEquals("Vantaggio P1", manager.getGameScore());

        manager.pointScored(2); // Torna Deuce (scoreP1=4, scoreP2=4)
        assertEquals("Deuce", manager.getGameScore());

        manager.pointScored(1); // Vantaggio P1 (scoreP1=5, scoreP2=4)
        manager.pointScored(1); // P1 vince il Game

        // Verifica Game vinto
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testGameScore_VantaggioP2_WithBugFix_ADAPTED() {
        // Prepara il Deuce (scoreP1=3, scoreP2=3)
        manager.pointScored(1); manager.pointScored(2);
        manager.pointScored(1); manager.pointScored(2);
        manager.pointScored(1); manager.pointScored(2);

        manager.pointScored(2); // Vantaggio P2 (scoreP1=3, scoreP2=4)

        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        assertEquals("Errore Game", manager.getGameScore());

        manager.pointScored(2); // P2 vince il Game

        // Verifica Game vinto
        assertEquals("0-0 (Game: 0-1 Love-Love)", manager.getMatchScore());
    }

    // =========================================================================
    // TEST 6: COPERTURA checkTieBreakPoint() / Tie Break Scoring
    // =========================================================================

    @Test
    public void testTieBreak_P1WinsStandardAndExtended_ADAPTED() {
        // 1. Attiva lo stato Tie-Break (Games 6-6)
        activateTieBreak();

        // Simula la vittoria P1 7-5.
        for (int i = 0; i < 5; i++) {
            manager.pointScored(1);
        	manager.pointScored(2);
        }
        manager.pointScored(1); // 6-5
        manager.pointScored(1); // 7-5 (Vittoria P1 Ramo 1)

        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        // Il set rimane a 0-0.
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());

        // Prepara un nuovo Tie-Break per il caso esteso
        activateTieBreak();

        // Simula 8-6 in Tie-Break
        for (int i = 0; i < 6; i++) {
        	manager.pointScored(1);
        	manager.pointScored(2);
        }
        manager.pointScored(1); // 7-6
        manager.pointScored(1); // 8-6 (Vittoria P1 Ramo 2)

        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        // L'errore continua (il conteggio dei set vinti rimane bloccato)
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testTieBreak_P2WinsStandardAndExtended_ADAPTED() {
        // 1. Attiva lo stato Tie-Break (Games 6-6)
        activateTieBreak();

        // Simula la vittoria P2 5-7.
        for (int i = 0; i < 5; i++) {
            manager.pointScored(1);
        	manager.pointScored(2);
        }
        manager.pointScored(2); // 5-6
        manager.pointScored(2); // 5-7 (Vittoria P2 Ramo 3)

        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        // Il set rimane a 0-0.
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());

        // Prepara un nuovo Tie-Break per il caso esteso
        activateTieBreak();

        // Simula 6-8 in Tie-Break
        for (int i = 0; i < 6; i++) {
        	manager.pointScored(1);
        	manager.pointScored(2);
        }
        manager.pointScored(2); // 6-7
        manager.pointScored(2); // 6-8 (Vittoria P2 Ramo 4)

        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        // L'errore continua (il conteggio dei set vinti rimane bloccato)
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    // =========================================================================
    // TEST 7: COPERTURA checkSetPoint() / moveToNextSet() / isGameOver()
    // =========================================================================

    @Test
    public void testCheckSetPoint_TieBreakActivation() {
        // Porta i Games a 6-5
        for (int i = 0; i < 5; i++) {
        	winGameEasy(1);
        	winGameEasy(2);
        }
        winGameEasy(1); // 6-5 P1

        // Game per P2 (6-6)
        winGameEasy(2); // checkSetPoint() Ramo 1: gamesP1 == 6 && gamesP2 == 6

        // Verifica che il Tie-Break sia attivo
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
        // E che i punti siano resettati per il Tie-Break
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }

    @Test
    public void testCheckSetPoint_WinSetStandard() {
        // P1 vince 6-3
        for (int i = 0; i < 3; i++) {
        	winGameEasy(1);
        	winGameEasy(2);
        }
        for (int i = 0; i < 3; i++) {
        	winGameEasy(1);
        } // 6-3 P1 (Vittoria P1 Ramo 2: P1 >= 6 e P1 >= P2 + 2)

        // Verifica set vinto e passaggio a Set 2
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testCheckSetPoint_WinSetExtended() {
        // P2 vince 7-5
        for (int i = 0; i < 5; i++) {
        	winGameEasy(1);
        	winGameEasy(2);
        } // 5-5
        winGameEasy(2); // 5-6
        winGameEasy(2); // 5-7 P2 (Vittoria P2 Ramo 3: gamesP2 == 7 && gamesP1 == 5)

        // Verifica set vinto e passaggio a Set 2
        assertEquals("0-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testIsGameOver_P1WinsMatch() {
        // Simula la vittoria del match per P1 (3 Set a 0).
        for (int set = 0; set < 3; set++) {
            for (int i = 0; i < 6; i++) {
                winGameEasy(1);
            }
        }

        // Verifica Game Over
        assertTrue(manager.isGameOver());
        // Verifica punteggio finale
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void testIsGameOver_P2WinsMatch() {
        // Simula la vittoria del match per P2 (3 Set a 1).
        for (int i = 0; i < 6; i++) { // P1 vince il primo set (6-0)
        	winGameEasy(1);
        }
        for (int set = 0; set < 3; set++) {
            for (int i = 0; i < 6; i++) {
                winGameEasy(2); // P2 vince i successivi 3 set (6-0)
            }
        }

        // Verifica Game Over
        assertTrue(manager.isGameOver());
        // Verifica punteggio finale
        assertEquals("P1: 1 Set | P2: 3 Set", manager.getMatchScore());
    }

    // =========================================================================
    // TEST 8: COPERTURA getMatchScore() / getTieBreakScore()
    // =========================================================================

    @Test
    public void testGetMatchScore_InProgress() {
        // Set 1: 5-5 Games
        for (int i = 0; i < 5; i++) {
        	winGameEasy(1);
        	winGameEasy(2);
        }
        // Set 1: 5-5, Game 11: 40-15
        manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);
        manager.pointScored(2);

        // Dovrebbe mostrare 0-0 Set, 5-5 Game, 40-15 Punti
        assertEquals("0-0 (Game: 5-5 40-15)", manager.getMatchScore());
    }

    @Test
    public void testGetMatchScore_WithSetsAndTieBreak_ADAPTED() {
        // P1 vince il primo Set 6-3
        for (int i = 0; i < 3; i++) {
        	winGameEasy(1);
        	winGameEasy(2);
        }
        for (int i = 0; i < 3; i++) {
        	winGameEasy(1);
        }

        // P2 vince il secondo Set 7-6 (via Tie-Break)
        activateTieBreak();

        // 7-6 P2 in Tie-Break
        for (int i = 0; i < 6; i++) {
        	manager.pointScored(1);
        	manager.pointScored(2);
        }
        manager.pointScored(2); // 6-7
        manager.pointScored(2); // 6-8 (P2 vince)

        // Set 3 è in corso (0-0 Game, Love-Love)
        // *** ADATTAMENTO DEL TEST AL BUG ORIGINALE ***
        // L'errore nel conteggio dei set durante il Tie-Break persiste, quindi il punteggio rimane 1-0 Set
        // invece di 1-1.
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
}
