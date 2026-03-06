/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: ange.ragozzino@studenti.unina.it
UserID: 249
Date: 22/11/2025
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
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
        manager = null;
    }

    // --- Helper Methods per facilitare gli scenari complessi ---

    /**
     * Fa segnare n punti a un giocatore.
     */
    private void scorePoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    /**
     * Simula la vittoria di un game standard (a 0) per il giocatore specificato.
     */
    private void winGame(int player) {
        scorePoints(player, 4);
    }

    /**
     * Simula la vittoria di un set (6-0) per il giocatore specificato.
     */
    private void winSetClean(int player) {
        for (int i = 0; i < 6; i++) {
            winGame(player);
        }
    }

    // --- TEST CASES ---

    @Test
    public void testInitialState() {
        // Verifica lo stato iniziale: Love-Love, nessun set vinto
        assertEquals("Love-Love", manager.getGameScore());
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("0-0"));
        assertTrue(matchScore.contains("Game: 0-0"));
        assertFalse(manager.isGameOver());
    }

    @Test
    public void testBasicScoringSequence() {
        // P1: 15
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());

        // P1: 30
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());

        // P1: 40
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore());

        // Reset manuale (test metodo resetPoints)
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testOpponentScoring() {
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("Love-30", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("Love-40", manager.getGameScore());
    }

    @Test
    public void testMixedScore() {
        manager.pointScored(1); // 15-0
        manager.pointScored(2); // 15-15
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1); // 30-15
        manager.pointScored(2); // 30-30
        assertEquals("30-30", manager.getGameScore());
    }

    @Test
    public void testDeuceLogic() {
        // Portiamo il punteggio a 40-40 (Deuce)
        scorePoints(1, 3);
        scorePoints(2, 3);
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // Ritorno a Deuce (P2 segna)
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P2
        manager.pointScored(2);
        assertEquals("Vantaggio P2", manager.getGameScore());

        // Ritorno a Deuce (P1 segna)
        manager.pointScored(1);
        assertEquals("Deuce", manager.getGameScore());

        // P1 vince il game dal Deuce
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(1); // Vince il game
        
        // Verifica che il game sia stato assegnato e i punti resettati
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testGameWinClean() {
        // P1 vince a zero
        scorePoints(1, 4);
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testInvalidPlayerInput() {
        // Memorizza stato attuale
        String scoreBefore = manager.getGameScore();
        // Prova input non valido
        manager.pointScored(3);
        manager.pointScored(-1);
        manager.pointScored(0);
        
        // Lo stato non deve cambiare
        assertEquals(scoreBefore, manager.getGameScore());
    }

    @Test
    public void testSetWinStandard() {
        // P1 vince 6 game di fila
        winSetClean(1);
        
        // Verifica che il set 1 sia stato vinto da P1
        // La stringa match score cambia formato quando i game sono resettati per il nuovo set
        String matchScore = manager.getMatchScore();
        // Punteggio set deve essere 1-0
        assertTrue("Dovrebbe indicare 1 set a 0", matchScore.startsWith("1-0"));
        // Nuovo set corrente deve essere a 0 game
        assertTrue("Nuovo set deve iniziare 0-0", matchScore.contains("Game: 0-0"));
    }

    @Test
    public void testSetWinExtension7_5() {
        // Arriviamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        assertTrue(manager.getMatchScore().contains("Game: 5-5"));

        // P1 va a 6-5 (non vince ancora il set)
        winGame(1);
        assertTrue(manager.getMatchScore().contains("Game: 6-5"));

        // P1 vince 7-5
        winGame(1);
        String matchScore = manager.getMatchScore();
        assertTrue("P1 dovrebbe aver vinto il set 7-5", matchScore.startsWith("1-0"));
        assertTrue(matchScore.contains("Game: 0-0"));
    }
    
    @Test
    public void testSetWinExtension5_7() {
        // Arriviamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // P2 va a 5-6
        winGame(2);
        // P2 vince 5-7
        winGame(2);
        
        String matchScore = manager.getMatchScore();
        assertTrue("P2 dovrebbe aver vinto il set", matchScore.startsWith("0-1"));
    }

    @Test
    public void testTieBreakTrigger() {
        // Arriviamo a 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); } // 5-5
        winGame(1); // 6-5
        winGame(2); // 6-6 -> Trigger TieBreak
        
        // Verifica output TieBreak
        assertTrue(manager.getGameScore().contains("Errore Game")); // In TieBreak getGameScore non è usato logicamente nel print, ma controlliamo lo stato
        // Controlliamo che il match score mostri il tie break
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("TIE-BREAK: 0-0"));
    }

    @Test
    public void testTieBreakScoringAndWin() {
        // Setup 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1);
        winGame(2);

        // Ora siamo nel Tie-Break. I punti sono numerici.
        manager.pointScored(1); // 1-0
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-0"));
        
        manager.pointScored(2); // 1-1
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-1"));

        // Portiamo a 6-0 per P1
        scorePoints(1, 5); 
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 6-1"));
        
        // P1 vince tie break (7-1)
        manager.pointScored(1);
        
        // Verifica fine set
        String matchScore = manager.getMatchScore();
        // P1 ha vinto il set
        assertTrue(matchScore.startsWith("1-0"));
        // Reset per nuovo set
        assertTrue(matchScore.contains("Game: 0-0"));
    }

    @Test
    public void testTieBreakExtension() {
        // Setup 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1);
        winGame(2);

        // Portiamo TieBreak a 6-6
        scorePoints(1, 6);
        scorePoints(2, 6);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 6-6"));

        // P1 va a 7-6 (non basta, serve scarto di 2)
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 7-6"));

        // P1 va a 8-6 (Vince)
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testTieBreakWinPlayer2() {
        // Setup 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1);
        winGame(2);
        
        // P2 vince TieBreak 7-0
        scorePoints(2, 7);
        
        assertTrue(manager.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void testMatchWinP1() {
        // P1 vince 1° set
        winSetClean(1);
        // P1 vince 2° set
        winSetClean(1);
        assertFalse(manager.isGameOver());
        
        // P1 vince 3° set (Match)
        winSetClean(1);
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        
        // Test comportamento dopo Game Over (non deve cambiare punteggio)
        manager.pointScored(1);
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void testMatchWinP2() {
        // P2 vince 3 set
        winSetClean(2);
        winSetClean(2);
        winSetClean(2);
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void testComplexMatchProgression() {
        // Set 1: P1 vince 6-4
        for(int i=0; i<4; i++) { winGame(1); winGame(2); } // 4-4
        winGame(1); winGame(1); // 6-4
        assertTrue(manager.getMatchScore().startsWith("1-0"));

        // Set 2: P2 vince al TieBreak
        for(int i=0; i<5; i++) { winGame(1); winGame(2); } // 5-5
        winGame(1); winGame(2); // 6-6
        scorePoints(2, 7); // TB 0-7
        assertTrue(manager.getMatchScore().startsWith("1-1"));
        
        // Verifica stato pulito all'inizio del 3° set
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testResetGameAndPoints() {
        // Avanza il gioco
        scorePoints(1, 3); // 40-0
        // Reset
        manager.resetGameAndPoints();
        
        assertEquals("Love-Love", manager.getGameScore());
        // Nota: resetGameAndPoints resetta games e points ma NON i set
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
    
    @Test
    public void testManualCheckCalls() {
        // Anche se dovrebbero essere chiamati internamente, per coprire eventuali
        // rami morti o chiamate dirette permesse dalla visibilità pubblica:
        
        // Test checkGamePoint senza punti sufficienti
        manager.checkGamePoint(); 
        assertEquals("Love-Love", manager.getGameScore());
        
        // Test checkTieBreakPoint fuori contesto (non in tie break o senza punti)
        manager.checkTieBreakPoint();
        // Non deve crashare o cambiare stato in modo anomalo se punteggi bassi
        
        // Test checkSetPoint intermedio
        manager.checkSetPoint();
        // Non succede nulla se games < 6
    }
    
    @Test
    public void testMoveToNextSetBlock() {
        // Test che moveToNextSet incrementi currentSet
        // Non possiamo leggere currentSet direttamente, ma possiamo dedurlo
        // Vincendo un set.
        winSetClean(1); 
        // Ora siamo nel set 2.
        // Se chiamiamo moveToNextSet manualmente (è public):
        manager.moveToNextSet();
        // Ora dovremmo essere nel set 3 (virtualmente, anche se i game sono 0-0)
        // L'unico modo per vederlo è vincere questo set e vedere il match score
        winSetClean(1); // Vince set 3
        
        // Se abbiamo saltato il set 2 col metodo manuale, ora dovremmo avere
        // Set 1 vinto, Set 2 vuoto (o perso se logica default), Set 3 vinto?
        // In realtà il codice usa `currentSet` per indicizzare l'array.
        // Se saltiamo, `setsP1[1]` rimane 0.
        
        String score = manager.getMatchScore();
        // Il ciclo in getMatchScore somma i set vinti.
        // Set 1: 6-0 (P1). Set 2: 0-0 (Parità, nessuno vince). Set 3: 6-0 (P1).
        // Totale P1: 2 set.
        assertTrue(score.startsWith("2-0")); 
    }
    
    @Test
    public void testGetGameScoreError() {
         // Caso limite teorico per coprire "Errore Game" o comportamenti imprevisti
         // Difficile da raggiungere con logica standard, ma proviamo a vedere se 
         // resetPoints riporta a stato coerente.
         manager.resetPoints();
         assertEquals("Love-Love", manager.getGameScore());
    }
}