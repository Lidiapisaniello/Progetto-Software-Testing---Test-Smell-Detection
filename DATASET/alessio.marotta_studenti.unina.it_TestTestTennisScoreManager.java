/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Alessio"
Cognome: "Marotta"
Username: alessio.marotta@studenti.unina.it
UserID: 1159
Date: 24/11/2025
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
        System.setOut(new PrintStream(outContent)); // Cattura System.out per testare i messaggi
    }

    @After
    public void tearDown() {
        System.setOut(originalOut); // Ripristina System.out
    }

    /**
     * Helper per resettare lo stream di output tra le fasi di un singolo test se necessario.
     */
    private void resetOutput() {
        outContent.reset();
    }

    /**
     * Helper per far vincere un game a un giocatore specifico.
     * Porta il punteggio a 40-0 (o simile) e segna il punto finale.
     */
    private void winGame(int player) {
        manager.resetPoints(); // Assicura pulizia interna se usiamo metodi pubblici
        // 4 punti consecutivi per vincere un game da 0
        manager.pointScored(player);
        manager.pointScored(player);
        manager.pointScored(player);
        manager.pointScored(player);
    }

    // --- TEST INIZIALI E BASE ---

    @Test
    public void testInitialState() {
        // Verifica stato iniziale tramite getMatchScore
        String score = manager.getMatchScore();
        assertTrue("Il punteggio iniziale deve contenere 0-0", score.contains("0-0"));
        assertTrue("Il punteggio deve essere Love-Love", score.contains("Love-Love"));
        
        // Copertura printScore
        manager.printScore();
        assertTrue(outContent.toString().contains("Set Corrente (1)"));
    }

    @Test
    public void testSimplePointScoring() {
        // P1 segna -> 15-Love
        manager.pointScored(1);
        assertTrue(manager.getGameScore().contains("15-Love"));
        
        // P2 segna -> 15-15
        manager.pointScored(2);
        assertTrue(manager.getGameScore().contains("15-15"));
        
        // P1 segna -> 30-15
        manager.pointScored(1);
        assertTrue(manager.getGameScore().contains("30-15"));
        
        // P1 segna -> 40-15
        manager.pointScored(1);
        assertTrue(manager.getGameScore().contains("40-15"));
    }

    @Test
    public void testInvalidPlayer() {
        manager.pointScored(3); // Giocatore non valido
        assertTrue("Dovrebbe stampare messaggio di errore", outContent.toString().contains("Errore: Giocatore non valido"));
        
        resetOutput();
        manager.pointScored(0);
        assertTrue(outContent.toString().contains("Errore: Giocatore non valido"));
    }

    // --- TEST LOGICA GAME (DEUCE / VANTAGGI) ---

    @Test
    public void testDeuceAndAdvantageP1() {
        // Portiamo a 40-40 (Deuce)
        manager.pointScored(1); // 15
        manager.pointScored(2); // 15
        manager.pointScored(1); // 30
        manager.pointScored(2); // 30
        manager.pointScored(1); // 40
        manager.pointScored(2); // 40 (Deuce)

        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // Ritorno a Deuce (P2 segna)
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
        
        // P1 vince dal vantaggio
        manager.pointScored(1); // Adv P1
        manager.pointScored(1); // Win
        
        // Verifica che il game sia stato assegnato a P1
        assertTrue("P1 dovrebbe aver vinto un game", manager.getMatchScore().contains("Game: 1-0"));
        assertTrue("Punti resettati a Love-Love", manager.getGameScore().contains("Love-Love"));
    }

    @Test
    public void testAdvantageP2_BuggyBehavior() {
        // NOTA: C'è un bug nel codice: "if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)" è sempre false.
        // Questo test copre il ramo "else" finale ("Errore Game") invece del "Vantaggio P2".
        
        // Portiamo a 40-40
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(2);
        manager.pointScored(2); // Deuce

        // P2 segna il punto del vantaggio
        manager.pointScored(2);

        // A causa del bug, ci aspettiamo "Errore Game" invece di "Vantaggio P2"
        // Se il bug fosse risolto, questo test fallirebbe, segnalando la regressione/fix.
        assertEquals("Errore Game", manager.getGameScore());
        
        // Se P2 segna ancora, dovrebbe comunque vincere il game perché checkGamePoint è corretto
        manager.pointScored(2);
        assertTrue("P2 dovrebbe aver vinto il game nonostante il bug della stringa", manager.getMatchScore().contains("Game: 0-1"));
    }

    // --- TEST VITTORIA SET ---

    @Test
    public void testWinSetP1_Standard() {
        // P1 vince 6 game di fila
        for (int i = 0; i < 6; i++) {
            winGame(1);
        }
        
        // Verifica output console per vittoria set
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (6-0) ***"));
        
        // Verifica reset per set 2
        assertTrue("Dovremmo essere nel secondo set (0-0 set score, 1-0 match score)", manager.getMatchScore().contains("1-0"));
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testWinSetP2_Standard() {
        // P2 vince 6 game di fila
        for (int i = 0; i < 6; i++) {
            winGame(2);
        }
        
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (6-0) ***"));
        assertTrue(manager.getMatchScore().contains("0-1"));
    }
    
    @Test
    public void testWinSet7_5() {
        // Scenario 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // P1 va 6-5
        winGame(1);
        assertFalse("Set non deve finire 6-5", outContent.toString().contains("Vinto da P1"));
        
        // P1 vince 7-5
        winGame(1);
        assertTrue("Set deve finire 7-5", outContent.toString().contains("*** SET 1 Vinto da P1 (7-5) ***"));
    }

    // --- TEST TIE-BREAK ---

    @Test
    public void testTieBreakTrigger() {
        // Portiamo a 6-6
        for (int i = 0; i < 5; i++) { winGame(1); winGame(2); } // 5-5
        winGame(1); // 6-5
        winGame(2); // 6-6 -> TieBreak trigger
        
        assertTrue(outContent.toString().contains("*** INIZIO TIE-BREAK ***"));
        assertTrue("Il punteggio deve mostrare TIE-BREAK", manager.getMatchScore().contains("TIE-BREAK: 0-0"));
    }

    @Test
    public void testTieBreakScoring() {
        // Setup 6-6
        for (int i = 0; i < 5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2); 
        
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-0"));
        
        manager.pointScored(2);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-1"));
    }

    @Test
    public void testTieBreakWinP2() {
        // Setup 6-6
        for (int i = 0; i < 5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2); 
        
        // P2 vince tie break 7-0
        for (int i = 0; i < 6; i++) {
            manager.pointScored(2);
        }
        // 0-6 nel TB. P2 segna il 7imo punto.
        manager.pointScored(2);
        
        // BUG NEL CODICE: In checkTieBreakPoint, viene chiamato resetGameAndPoints()
        // PRIMA di checkSetPoint(). Questo resetta gamesP1 e gamesP2 a 0.
        // Di conseguenza, checkSetPoint controlla 0 vs 0 e nessuno vince il set.
        // Il set 1 rimane "in corso" ma con game 0-0.
        
        // Assertiamo che NON stampi la vittoria (comportamento attuale buggato)
        assertFalse("A causa del bug, il set non viene vinto", outContent.toString().contains("Vinto da P2"));
        
        // Assertiamo che i game siano stati resettati a 0-0
        assertTrue("A causa del bug, i game vengono resettati a 0-0", manager.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testTieBreakWinP1_BuggyBehavior() {
        // Setup 6-6
        for (int i = 0; i < 5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2); 
        
        // P1 vince tie break 7-0
        for (int i = 0; i < 6; i++) {
            manager.pointScored(1);
        }
        
        resetOutput();
        // P1 segna il 7imo punto (7-0). 
        manager.pointScored(1);
        
        // BUG NEL CODICE:
        // 1. checkTieBreakPoint chiama resetGameAndPoints() -> gamesP1=0, gamesP2=0.
        // 2. checkSetPoint() viene chiamato su 0-0 -> Nessuna vittoria.
        // (Inoltre c'è un bug logico nella condizione di P1 in checkSetPoint, ma viene mascherato dal reset precedente).
        
        String score = manager.getMatchScore();
        
        // Asseriamo che il set NON è stato vinto
        assertFalse(outContent.toString().contains("Vinto da P1"));
        
        // Asseriamo che i game sono tornati a 0-0 invece di 7-6 o vittoria set
        assertTrue("A causa del bug, i game vengono resettati a 0-0", score.contains("Game: 0-0")); 
    }

    // --- TEST MATCH FINALE ---

    @Test
    public void testMatchWin() {
        // Simula vittoria P1 per 3 set a 0 (utilizzando game normali, non tie-break, per evitare il bug)
        // Set 1
        for(int i=0; i<6; i++) winGame(1);
        // Set 2
        for(int i=0; i<6; i++) winGame(1);
        // Set 3
        for(int i=0; i<6; i++) winGame(1);
        
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 1!"));
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void testMatchWinP2() {
         // Simula vittoria P2 per 3 set a 0
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) winGame(2);
        }
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 2!"));
    }

    @Test
    public void testPlayAfterGameOver() {
        // Vinciamo la partita
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) winGame(1);
        }
        
        resetOutput();
        // Proviamo a segnare ancora
        manager.pointScored(1);
        
        assertTrue("Dovrebbe stampare che la partita è finita", outContent.toString().contains("La partita è finita!"));
        
        // Copertura getGameScore con partita finita
        assertEquals("PARTITA FINITA", manager.getGameScore());
        
        // Copertura moveToNextSet con partita finita (non deve fare nulla, ma copriamo il ramo else/check)
        manager.moveToNextSet();
        assertTrue(manager.isGameOver());
    }
    
    // --- TEST COPERTURA EXTREME CORNER CASES & BRANCHES ---
    
    @Test
    public void testConstructorExplicitLoop() {
        // Il costruttore ha un ciclo for esplicito. 
        // È già coperto dal setUp, ma verifichiamo che l'oggetto sia pulito.
        TennisScoreManager localManager = new TennisScoreManager();
        assertEquals("Love-Love", localManager.getGameScore());
    }
    
    @Test
    public void testGetMatchScoreSetsEvaluation() {
        // Test specifico per il loop dentro getMatchScore che conta i set vinti
        // P1 vince set 1
        for(int i=0; i<6; i++) winGame(1);
        // P2 vince set 2
        for(int i=0; i<6; i++) winGame(2);
        
        String score = manager.getMatchScore();
        // Deve mostrare 1-1 nei set
        assertTrue(score.startsWith("1-1"));
    }
}