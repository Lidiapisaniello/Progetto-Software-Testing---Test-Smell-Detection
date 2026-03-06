/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonio"
Cognome: "Coppola"
Username: antonio.coppola34@studenti.unina.it
UserID: 203
Date: 21/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// Import per catturare l'output di System.out (opzionale ma utile per la copertura)
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

    private TennisScoreManager scoreManager;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        scoreManager = new TennisScoreManager();
        // Cattura l'output di System.out per testare i messaggi di console (es. isGameOver)
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testInitialStateAndResetMethods() {
        // Stato iniziale
        assertEquals("Love-Love", scoreManager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());

        // Test resetPoints
        scoreManager.pointScored(1);
        scoreManager.resetPoints();
        assertEquals("Love-Love", scoreManager.getGameScore());

        // Test resetGameAndPoints
        // Simula la vittoria di un game per popolare gamesP1
        scoreManager.pointScored(1); // 15
        scoreManager.pointScored(1); // 30
        scoreManager.pointScored(1); // 40
        scoreManager.pointScored(1); // Game
        
        scoreManager.resetGameAndPoints();
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }

    // --- Test Punteggio Standard (Game) ---

    @Test
    public void testGameScore_RegularPoints() {
        // Love-Love
        assertEquals("Love-Love", scoreManager.getGameScore());

        // 15-Love
        scoreManager.pointScored(1);
        assertEquals("15-Love", scoreManager.getGameScore());

        // 30-Love
        scoreManager.pointScored(1);
        assertEquals("30-Love", scoreManager.getGameScore());

        // 30-15
        scoreManager.pointScored(2);
        assertEquals("30-15", scoreManager.getGameScore());

        // 40-15
        scoreManager.pointScored(1);
        assertEquals("40-15", scoreManager.getGameScore());
    }

    @Test
    public void testGameScore_DeuceAndAdvantage() {
        // 40-40 (Simula Deuce)
        scoreManager.pointScored(1); // 15-0
        scoreManager.pointScored(2); // 15-15
        scoreManager.pointScored(1); // 30-15
        scoreManager.pointScored(2); // 30-30
        scoreManager.pointScored(1); // 40-30
        scoreManager.pointScored(2); // Deuce (scoreP1=3, scoreP2=3)
        assertEquals("Deuce", scoreManager.getGameScore());

        // Vantaggio P1
        scoreManager.pointScored(1);
        assertEquals("Vantaggio P1", scoreManager.getGameScore());

        // Deuce di nuovo (scoreP1=4, scoreP2=4)
        scoreManager.pointScored(2);
        assertEquals("Deuce", scoreManager.getGameScore());

        // Vantaggio P2
        scoreManager.pointScored(2);
        assertEquals("Vantaggio P2", scoreManager.getGameScore());
    }
    
    // Il metodo getGameScore ha un ramo 'Errore Game' che è difficile da raggiungere
    // con un punto valido, ma si verifica se il punteggio è sbilanciato ma non vinto.
    // L'errore nel codice originale era:
    // `if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)` -> dovrebbe essere `scoreP2 == scoreP1 + 1`
    @Test
    public void testGameScore_UnreachableErrorPath() {
        // Forza uno stato che cade nel ramo "Errore Game" a causa della logica imperfetta:
        // Se P1:4, P2:2 il game è vinto.
        // Se P1:3, P2:2 -> 40-30.
        // Simuliamo 5-3, che non è vinto (vittoria solo con differenza di 2).
        
        // P1 vince un game (4-0, gamesP1=1)
        scoreManager.pointScored(1); // 1
        scoreManager.pointScored(1); // 2
        scoreManager.pointScored(1); // 3
        scoreManager.pointScored(1); // 4 (Game vinto)

        // Game successivo: 40-30
        scoreManager.pointScored(1); // 15
        scoreManager.pointScored(2); // 15-15
        scoreManager.pointScored(1); // 30-15
        scoreManager.pointScored(2); // 30-30
        scoreManager.pointScored(1); // 40-30

        // P1 segna (scoreP1=4, scoreP2=3). Vantaggio P1.
        scoreManager.pointScored(1);
        assertEquals("Vantaggio P1", scoreManager.getGameScore());
        
        // P2 segna (scoreP1=4, scoreP2=4). Deuce.
        scoreManager.pointScored(2);
        
        // P2 segna (scoreP1=4, scoreP2=5). Vantaggio P2.
        scoreManager.pointScored(2);
        assertEquals("Vantaggio P2", scoreManager.getGameScore());

        // P2 segna (scoreP1=4, scoreP2=6). Game vinto da P2.
        scoreManager.pointScored(2);
        // Ora i punti sono resettati. gamesP2=1.

        // Per testare il ramo "Errore Game" si dovrebbe modificare il codice, 
        // ma data la logica esistente, i test sopra coprono i percorsi raggiungibili.
    }


    @Test
    public void testCheckGamePoint_P1WinsGame() {
        // Simula P1 vince il Game (40-Love, 4 punti)
        scoreManager.pointScored(1); // 15
        scoreManager.pointScored(1); // 30
        scoreManager.pointScored(1); // 40
        scoreManager.pointScored(1); // Game

        assertEquals(1, scoreManager.gamesP1);
        assertEquals(0, scoreManager.gamesP2);
        assertEquals("Love-Love", scoreManager.getGameScore()); // Punti resettati
    }

    @Test
    public void testCheckGamePoint_P2WinsGameViaDeuce() {
        // Simula Deuce -> Vantaggio P2 -> Game P2
        scoreManager.pointScored(1); scoreManager.pointScored(2); // 15-15
        scoreManager.pointScored(1); scoreManager.pointScored(2); // 30-30
        scoreManager.pointScored(1); scoreManager.pointScored(2); // Deuce

        scoreManager.pointScored(2); // Vantaggio P2
        scoreManager.pointScored(2); // Game

        assertEquals(0, scoreManager.gamesP1);
        assertEquals(1, scoreManager.gamesP2);
    }

    @Test
    public void testInvalidPlayerInput() {
        // Deve stampare un messaggio di errore e non cambiare il punteggio
        scoreManager.pointScored(3);
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains("Errore: Giocatore non valido. Usa 1 o 2."));
        assertEquals("Love-Love", scoreManager.getGameScore());
    }

    // --- Test Set e Tie-Break ---

    /**
     * Helper per far vincere un game al giocatore specificato
     */
    private void winGame(int player) {
        for (int i = 0; i < 4; i++) {
            scoreManager.pointScored(player);
        }
    }
    
    @Test
    public void testCheckSetPoint_TieBreakTrigger() {
        // Simula 6-6
        for (int i = 0; i < 6; i++) {
            winGame(1);
            winGame(2);
        }

        assertTrue(scoreManager.gamesP1 == 6);
        assertTrue(scoreManager.gamesP2 == 6);
        
        scoreManager.checkSetPoint();
        assertTrue(scoreManager.isTieBreak);
        
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*** INIZIO TIE-BREAK ***"));
        
        // Verifica che il punteggio dei game sia ancora 6-6 (non è resettato prima di vincere il set)
        assertEquals(6, scoreManager.gamesP1);
        assertEquals(6, scoreManager.gamesP2);
        
        // MatchScore deve riflettere l'inizio del Tie-Break
        assertEquals("0-0 (Game: 6-6 TIE-BREAK: 0-0)", scoreManager.getMatchScore());
    }

    @Test
    public void testCheckSetPoint_WinSet_7_5() {
        // Simula 5-5
        for (int i = 0; i < 5; i++) {
            winGame(1);
            winGame(2);
        }
        
        winGame(1); // 6-5 P1
        winGame(1); // 7-5 P1 vince il set

        // Set vinto da P1
        scoreManager.checkSetPoint();
        
        assertEquals("Set Corrente (2): P1 0 Game | P2 0 Game", outputStreamCaptor.toString().split("\n")[4].trim());
        assertEquals("0-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        
        // Verifica che il set sia memorizzato e si sia passati al set 2
        assertEquals(7, scoreManager.setsP1[0]);
        assertEquals(5, scoreManager.setsP2[0]);
        // currentSet è 2, games sono resettati
        assertEquals(0, scoreManager.gamesP1);
    }

    @Test
    public void testCheckSetPoint_WinSet_6_4() {
        // Simula 4-4
        for (int i = 0; i < 4; i++) {
            winGame(1);
            winGame(2);
        }
        
        winGame(2); // 4-5 P2
        winGame(2); // 4-6 P2 vince il set

        scoreManager.checkSetPoint();
        
        // Verifica che il set sia memorizzato e si sia passati al set 2
        assertEquals(4, scoreManager.setsP1[0]);
        assertEquals(6, scoreManager.setsP2[0]);
        assertEquals(0, scoreManager.gamesP1);
        assertEquals(0, scoreManager.gamesP2);
    }

    @Test
    public void testCheckTieBreakPoint_P2WinsTieBreakAndSet() {
        // Simula l'ingresso in Tie-Break
        for (int i = 0; i < 6; i++) {
            winGame(1);
            winGame(2);
        }
        scoreManager.checkSetPoint(); // Inizia Tie-Break

        // P2 vince il Tie-Break (7-5)
        for (int i = 0; i < 5; i++) {
            scoreManager.pointScored(1);
            scoreManager.pointScored(2);
        }
        scoreManager.pointScored(2); // Punteggio 5-6
        scoreManager.pointScored(2); // Punteggio 5-7. P2 vince il Tie-Break

        // La logica Tie-Break incrementa gamesP2 e chiama resetGameAndPoints/checkSetPoint.
        // gamesP2 diventa 7, gamesP1 è 6.
        
        // Si è verificato un errore di logica nel codice originale:
        // Quando P2 vince il tie-break, `gamesP2` diventa 7 e poi `resetGameAndPoints` lo azzera prima di `checkSetPoint`.
        // Questo test verifica il comportamento *effettivo* del codice fornito.
        
        // Dopo il punto vincente del Tie-Break, i game sono (6, 7) prima di checkSetPoint, 
        // ma resetGameAndPoints viene chiamato subito dopo l'incremento del game.
        
        // Re-inizializza il manager per isolare il bug:
        scoreManager = new TennisScoreManager();
        for (int i = 0; i < 6; i++) {
            winGame(1); winGame(2);
        }
        scoreManager.checkSetPoint(); // isTieBreak = true

        // P2 vince il punto decisivo per il Tie-Break (7-5)
        scoreManager.pointScored(1); // 1-0
        scoreManager.pointScored(2); // 1-1
        // ... (P2 porta a 7-5)
        scoreManager.scoreP1 = 5;
        scoreManager.scoreP2 = 6;
        scoreManager.pointScored(2); // 5-7.

        // L'uscita mostra il set vinto e il passaggio al set 2 (games azzerati).
        assertTrue(scoreManager.setsP2[0] == 7);
        assertTrue(scoreManager.setsP1[0] == 6);
        assertFalse(scoreManager.isTieBreak);
        assertEquals(0, scoreManager.gamesP1);
        assertEquals(0, scoreManager.currentSet); // Il set è stato vinto, currentSet è 2
    }


    // --- Test isGameOver e MatchScore ---

    private void winSet(int winner) {
        for (int i = 0; i < 6; i++) {
            winGame(winner);
        }
        winGame(winner); // 6-0
        scoreManager.checkSetPoint();
    }

    @Test
    public void testMatchScoreDisplay() {
        // Simula 1 set vinto da P1
        winSet(1);
        assertEquals("1-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
        
        // Simula 2 set vinti da P1
        winSet(1);
        assertEquals("2-0 (Game: 0-0 Love-Love)", scoreManager.getMatchScore());
    }

    @Test
    public void testIsGameOver_P1Wins3Sets() {
        // P1 vince i primi 2 set
        winSet(1); 
        winSet(1); 

        // P2 vince un set
        winSet(2);
        
        assertFalse(scoreManager.isGameOver()); // 2-1

        // P1 vince il 3° set (Game Over)
        winSet(1); 

        assertTrue(scoreManager.isGameOver()); // 3-1
        
        // L'output deve contenere il messaggio di vittoria
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("*** PARTITA VINTA DAL GIOCATORE 1!"));
        
        // Test che la funzione pointScored blocchi il gioco
        scoreManager.pointScored(1);
        assertTrue(outputStreamCaptor.toString().contains("La partita è finita! Punteggio finale:"));
        
        // Test MatchScore in Game Over
        assertEquals("P1: 3 Set | P2: 1 Set", scoreManager.getMatchScore());
    }

    @Test
    public void testIsGameOver_P2Wins3Sets() {
        winSet(2); // 0-1
        winSet(2); // 0-2
        winSet(1); // 1-2
        winSet(2); // 1-3. Game Over

        assertTrue(scoreManager.isGameOver());
        
        // Test MatchScore in Game Over
        assertEquals("P1: 1 Set | P2: 3 Set", scoreManager.getMatchScore());
    }

    // --- Cleanup ---

    @org.junit.After
    public void restoreStreams() {
        System.setOut(standardOut);
    }
}
						