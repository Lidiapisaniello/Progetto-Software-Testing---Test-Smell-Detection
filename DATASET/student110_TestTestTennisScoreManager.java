import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    // --- HELPER METHODS ---
    // Metodi di utilità per rendere i test leggibili e ridurre la duplicazione

    private void scorePoints(int player, int times) {
        for (int i = 0; i < times; i++) {
            manager.pointScored(player);
        }
    }

    private void reachDeuce() {
        scorePoints(1, 3); // 40
        scorePoints(2, 3); // 40
    }

    // Simula la vittoria di un game standard (partendo da 0-0)
    private void winGame(int player) {
        scorePoints(player, 4);
    }
    
    // Simula la vittoria di 'n' game per un giocatore (assumendo l'altro a 0 o ininfluente)
    private void winGames(int player, int count) {
        for (int i = 0; i < count; i++) {
            winGame(player);
        }
    }

    // --- TEST CASES ---

    @Test
    public void testInitialScore() {
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void testBasicPointsEvolution() {
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1);
        assertEquals("30-15", manager.getGameScore());
        
        manager.pointScored(1);
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testGameWinPlayer1() {
        scorePoints(1, 4); // 4 punti consecutivi
        // P1 dovrebbe aver vinto il game. Il punteggio resetta a Love-Love, Games: 1-0
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testGameWinPlayer2() {
        scorePoints(2, 4); 
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testDeuceLogic() {
        reachDeuce();
        assertEquals("Deuce", manager.getGameScore());
        
        // P1 fa punto -> Vantaggio P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // P2 fa punto -> Si torna a Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantagePlayer2_BugDiscovery() {
        reachDeuce();
        manager.pointScored(2);
        
        // BUG NOTO NEL CODICE SORGENTE:
        // La riga 'if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)' è logicamente falsa.
        // Il codice attuale ritorna "Errore Game".
        // Un test corretto deve aspettarsi "Vantaggio P2".
        // Se il test fallisce con "expected <Vantaggio P2> but was <Errore Game>", abbiamo trovato il bug.
        assertEquals("Il codice sorgente ha un bug logico nel calcolo del vantaggio P2", 
                     "Vantaggio P2", manager.getGameScore());
        
        // Se P2 segna ancora, dovrebbe vincere il game
        manager.pointScored(2);
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void testSetWinStandard_6_0() {
        // P1 vince 6 game di fila
        winGames(1, 6);
        
        // Dovrebbe aver vinto il primo set e resettato i game per il secondo set
        // Match Score format: "1-0 (Game: 0-0 Love-Love)"
        String score = manager.getMatchScore();
        assertTrue("P1 dovrebbe aver vinto il set", score.startsWith("1-0"));
        assertTrue("I game dovrebbero essere resettati", score.contains("Game: 0-0"));
    }

    @Test
    public void testSetWinExtended_7_5() {
        // Arriviamo a 5-5
        winGames(1, 5);
        winGames(2, 5);
        assertTrue(manager.getMatchScore().contains("Game: 5-5"));

        // P1 va a 6-5 (non vince ancora il set perché deve vincere di 2 o arrivare a 7-6)
        winGame(1); 
        assertTrue(manager.getMatchScore().contains("Game: 6-5"));

        // P1 va a 7-5 -> Vince il Set
        winGame(1);
        assertTrue("P1 dovrebbe vincere il set 7-5", manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void testTieBreakTrigger() {
        // Arriviamo a 6-6
        winGames(1, 5);
        winGames(2, 6); // 5-6
        winGame(1);     // 6-6
        
        // Ora dovrebbe essere attivato il Tie-Break
        // Il punteggio "punti" non è più Love/15/30, ma numerico
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
        
        manager.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
    }

    @Test
    public void testTieBreakWin_Player2() {
        // Setup 6-6
        winGames(1, 5);
        winGames(2, 6);
        winGame(1); 

        // Vinciamo il Tie-Break per P2 (7 punti a 0)
        for(int i=0; i<7; i++) {
            manager.pointScored(2);
        }
        
        // P2 dovrebbe aver vinto il set (0-1 nei set)
        assertTrue("P2 dovrebbe vincere il set dopo il tie-break", manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testTieBreakWin_Player1_BugDiscovery() {
        // Setup 6-6
        winGames(2, 5);
        winGames(1, 6);
        winGame(2); // 6-6

        // Vinciamo il Tie-Break per P1 (7 punti a 0)
        for(int i=0; i<7; i++) {
            manager.pointScored(1);
        }
        
        // BUG NOTO NEL CODICE SORGENTE:
        // Nella funzione checkSetPoint c'è: (gamesP2 == 7 && gamesP2 == 6) invece di P1.
        // P1 non riesce a chiudere il set col tie-break nel codice attuale.
        assertTrue("P1 dovrebbe vincere il set dopo il tie-break", manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testTieBreakExtended() {
        // Setup 6-6
        winGames(1, 5);
        winGames(2, 6);
        winGame(1);
        
        // Arriviamo a 6-6 nel tie break
        scorePoints(1, 6);
        scorePoints(2, 6);
        assertEquals("TIE-BREAK: 6-6", manager.getTieBreakScore());
        
        // P1 va a 7-6 (non vince ancora, serve distacco di 2)
        manager.pointScored(1); 
        // Nota: a causa del bug sopra citato, questo controllo su P1 è parziale, 
        // ma verifichiamo che il gioco continui
        
        // P2 pareggia 7-7
        manager.pointScored(2);
        
        // P2 va a 8-7
        manager.pointScored(2);
        
        // P2 vince 9-7 -> Vince il set
        manager.pointScored(2);
        
        assertTrue("P2 dovrebbe vincere il set ai vantaggi del tie-break", manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testMatchWin() {
        // P1 vince 3 set di fila (6-0, 6-0, 6-0)
        for (int set = 0; set < 3; set++) {
            winGames(1, 6);
        }
        
        assertTrue("La partita dovrebbe essere finita", manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
        assertEquals("PARTITA FINITA", manager.getGameScore());
        
        // Punti segnati dopo la fine non dovrebbero cambiare nulla
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
    }

    @Test
    public void testInvalidPlayerInput() {
        manager.pointScored(1);
        manager.pointScored(99); // Input invalido
        assertEquals("15-Love", manager.getGameScore()); // Stato non deve cambiare
    }
}