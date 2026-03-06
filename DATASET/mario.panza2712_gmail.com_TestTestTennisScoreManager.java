/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: mario.panza2712@gmail.com
UserID: 434
Date: 22/11/2025
*/
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        System.setOut(new PrintStream(outContent)); // Cattura System.out per testare i messaggi console
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    // --- Helper Methods ---

    /**
     * Simula la vittoria di un game standard (a 0) per il giocatore specificato.
     */
    private void winGame(int player) {
        for (int i = 0; i < 4; i++) {
            manager.pointScored(player);
        }
    }

    /**
     * Porta il game corrente a Deuce (40-40).
     */
    private void reachDeuce() {
        manager.pointScored(1); // 15-0
        manager.pointScored(2); // 15-15
        manager.pointScored(1); // 30-15
        manager.pointScored(2); // 30-30
        manager.pointScored(1); // 40-30
        manager.pointScored(2); // Deuce
    }

    /**
     * Simula un set fino al 6-6 per attivare il Tie Break.
     */
    private void reachTieBreak() {
        // P1 vince 5 game
        for (int i = 0; i < 5; i++) winGame(1);
        // P2 vince 6 game (P2 va in vantaggio 6-5)
        for (int i = 0; i < 6; i++) winGame(2);
        // P1 vince 1 game (6-6) -> Tie Break
        winGame(1);
    }

    // --- Tests: Game Standard ---

    @Test
    public void testInitialScore() {
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("0-0 (Game: 0-0 Love-Love)"));
    }

    @Test
    public void testStandardPoints() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1); // 30-15
        manager.pointScored(1); // 40-15
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testDeuce() {
        reachDeuce();
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        reachDeuce();
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1BackToDeuce() {
        reachDeuce();
        manager.pointScored(1); // Adv P1
        manager.pointScored(2); // Back to Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugCheck() {
        // NOTA: Nel codice sorgente fornito c'è un bug nella condizione:
        // if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)
        // Questa condizione è sempre falsa. Di conseguenza, il codice cade nel return finale "Errore Game".
        // Testiamo questo comportamento per garantire la copertura del ramo "Errore Game".
        reachDeuce();
        manager.pointScored(2);
        assertEquals("Errore Game", manager.getGameScore());
    }

    // --- Tests: Game Win ---

    @Test
    public void testGameWinStandard() {
        // 40-30 P1
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1); // 40-30
        
        manager.pointScored(1); // Win Game
        
        // Verifica reset punti e incremento game
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testGameWinP2Standard() {
        // 30-40 P2
        manager.pointScored(2);
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);
        manager.pointScored(2); // 30-40
        
        manager.pointScored(2); // Win Game P2
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testGameWinFromDeuce() {
        reachDeuce();
        manager.pointScored(1); // Adv P1
        manager.pointScored(1); // Game P1
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    // --- Tests: Set Win Conditions ---

    @Test
    public void testSetWin6_4() {
        // P1 vince 5 game
        for(int i=0; i<5; i++) winGame(1);
        // P2 vince 4 game
        for(int i=0; i<4; i++) winGame(2);
        
        // P1 vince il 6° game (6-4)
        winGame(1);
        
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (6-4) ***"));
        // Verifica che siamo nel Set 2
        assertTrue(manager.getMatchScore().contains("P1: 1 Set | P2: 0 Set")); // getMatchScore formattato per fine partita o parziale? 
        // Il metodo getMatchScore restituisce i set vinti, ma se non è game over ritorna la stringa lunga.
        // Verifichiamo reset dei games per il nuovo set
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testSetWin7_5() {
        // 5-5
        for(int i=0; i<5; i++) winGame(1);
        for(int i=0; i<5; i++) winGame(2);
        
        winGame(1); // 6-5
        winGame(1); // 7-5
        
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (7-5) ***"));
    }

    @Test
    public void testSetWinP2_6_4() {
        for(int i=0; i<4; i++) winGame(1);
        for(int i=0; i<5; i++) winGame(2);
        winGame(2); // P2 vince 6-4
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (6-4) ***"));
    }

    // --- Tests: Tie Break ---

    @Test
    public void testTieBreakTrigger() {
        reachTieBreak();
        assertTrue(outContent.toString().contains("*** INIZIO TIE-BREAK ***"));
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 0-0"));
    }

    @Test
    public void testTieBreakWin7_5() {
        reachTieBreak();
        // Simuliamo punti tie break
        // P1: 6 punti, P2: 5 punti
        for(int i=0; i<5; i++) { manager.pointScored(1); manager.pointScored(2); }
        manager.pointScored(1); // 6-5
        
        // P1 vince 7-5
        manager.pointScored(1);
        
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (7-6) ***"));
        assertFalse(manager.getMatchScore().contains("TIE-BREAK")); // Dovrebbe essere resettato
    }

    @Test
    public void testTieBreakWinP2_Extended() {
        reachTieBreak();
        // 6-6 nel tie break
        for(int i=0; i<6; i++) { manager.pointScored(1); manager.pointScored(2); }
        
        manager.pointScored(2); // 6-7
        manager.pointScored(2); // 6-8 (P2 vince)
        
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (6-7) ***"));
    }

    // --- Tests: Match Win ---

    @Test
    public void testMatchWin3_0() {
        // Set 1 P1
        for(int i=0; i<6; i++) winGame(1);
        // Set 2 P1
        for(int i=0; i<6; i++) winGame(1);
        // Set 3 P1
        for(int i=0; i<6; i++) winGame(1);
        
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 1! (3 Set a 0) ***"));
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void testMatchWin3_1() {
        // Set 1 P2
        for(int i=0; i<6; i++) winGame(2);
        // Set 2, 3, 4 P1
        for(int i=0; i<3; i++) {
            for(int j=0; j<6; j++) winGame(1);
        }
        
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 1! (3 Set a 1) ***"));
    }
    
    @Test
    public void testMatchWinP2_3_2() {
        // Set 1 P1
        for(int i=0; i<6; i++) winGame(1);
        // Set 2 P2
        for(int i=0; i<6; i++) winGame(2);
        // Set 3 P1
        for(int i=0; i<6; i++) winGame(1);
        // Set 4 P2
        for(int i=0; i<6; i++) winGame(2);
        // Set 5 P2
        for(int i=0; i<6; i++) winGame(2);
        
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 2! (3 Set a 2) ***"));
        // Verifica ramo getGameScore con partita finita
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    // --- Tests: Error Handling & Early Returns ---

    @Test
    public void testInvalidPlayer() {
        manager.pointScored(3);
        assertTrue(outContent.toString().contains("Errore: Giocatore non valido. Usa 1 o 2."));
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testScoreAfterGameOver() {
        // Vinci la partita (3-0 P1)
        for(int i=0; i<18; i++) winGame(1);
        
        outContent.reset(); // Pulisci buffer
        
        // Prova a segnare un punto
        manager.pointScored(1);
        
        assertTrue(outContent.toString().contains("La partita è finita!"));
        // Assicurati che i set non siano cambiati (early return)
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void testSetWin7_5_P2() {
        // Copertura del ramo else-if per vittoria P2 7-5
        for(int i=0; i<5; i++) winGame(1); // 5
        for(int i=0; i<7; i++) winGame(2); // 7
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (7-5) ***"));
    }
}
						