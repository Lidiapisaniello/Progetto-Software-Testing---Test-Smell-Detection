/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: la.trani@studenti.unina.it
UserID: 1014
Date: 25/11/2025
*/
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    /**
     * Verifica lo stato iniziale della partita.
     */
    @Test
    public void testInitialState() {
        assertEquals("Love-Love", manager.getGameScore());
        // Verifica che il match score inizi con 0 set
        assertTrue(manager.getMatchScore().contains("0-0"));
        assertFalse(manager.isGameOver());
    }

    /**
     * Verifica il punteggio standard (15, 30, 40).
     */
    @Test
    public void testStandardScoringP1() {
        manager.pointScored(1); // 15
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(1); // 30
        assertEquals("30-Love", manager.getGameScore());
        
        manager.pointScored(1); // 40
        assertEquals("40-Love", manager.getGameScore());
    }

    /**
     * Verifica la situazione di Deuce (Parità).
     * Raggiungiamo 3-3 (40-40).
     */
    @Test
    public void testDeuce() {
        generateDeuce();
        assertEquals("Deuce", manager.getGameScore());
    }

    /**
     * Verifica il Vantaggio per il Giocatore 1.
     */
    @Test
    public void testAdvantageP1() {
        generateDeuce();
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // Se P2 segna, si torna a Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    /**
     * TEST DEL BUG NEL CODICE SORGENTE:
     * Il metodo getGameScore ha una condizione errata per il vantaggio P2:
     * (scoreP2 >= 3 && scoreP2 == scoreP2 + 1) che è sempre falsa.
     * Di conseguenza, quando P2 ha il vantaggio, il metodo ritorna "Errore Game"
     * (o cade nell'ultimo return se non matcha nulla, che è "Errore Game").
     */
    @Test
    public void testAdvantageP2_BugBehavior() {
        generateDeuce();
        manager.pointScored(2); // In teoria Vantaggio P2
        
        // A causa del bug nella logica if (scoreP2 == scoreP2 + 1), 
        // il codice restituisce "Errore Game" invece di "Vantaggio P2".
        assertEquals("Errore Game", manager.getGameScore());
        
        // Verifichiamo che comunque se P1 segna, si torna a Deuce (la logica interna dei punti funziona, è la stringa che è rotta)
        manager.pointScored(1);
        assertEquals("Deuce", manager.getGameScore());
    }

    /**
     * Verifica la vittoria di un Game standard da parte di P1.
     */
    @Test
    public void testWinGameP1() {
        // P1 fa 4 punti consecutivi
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1);
        
        // Il punteggio dei punti si resetta
        assertEquals("Love-Love", manager.getGameScore());
        // Verifichiamo che P1 abbia 1 game nella stringa del match
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    /**
     * Verifica la vittoria di un Set standard (6-0).
     */
    @Test
    public void testWinSetStandard_6_0() {
        winSetP1_6_0();
        
        // P1 ha vinto il primo set. Dovremmo essere nel secondo set.
        // La stringa match score dovrebbe mostrare "1-0" nei set
        String matchScore = manager.getMatchScore();
        assertTrue("Il match score dovrebbe indicare 1 set a 0 per P1", matchScore.startsWith("1-0"));
        assertTrue("Dovrebbe resettare i game per il nuovo set", matchScore.contains("Game: 0-0"));
    }

    /**
     * Verifica la vittoria di un Set per 7-5.
     * Questo copre la condizione (gamesP1 == 7 && gamesP2 == 5).
     */
    @Test
    public void testWinSet_7_5() {
        // Portiamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // P1 va a 6-5
        winGame(1);
        // P1 va a 7-5 -> Vince il set
        winGame(1);
        
        String matchScore = manager.getMatchScore();
        assertTrue("P1 dovrebbe aver vinto il set 7-5", matchScore.startsWith("1-0"));
    }

    /**
     * Verifica l'attivazione del Tie-Break sul 6-6.
     */
    @Test
    public void testTieBreakTrigger() {
        // Portiamo a 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); } // 5-5
        winGame(1); // 6-5
        winGame(2); // 6-6 -> Tie Break trigger
        
        assertTrue("La stringa dovrebbe indicare TIE-BREAK", manager.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }

    /**
     * Verifica il punteggio durante il Tie-Break.
     */
    @Test
    public void testTieBreakScoring() {
        forceTieBreak();
        
        manager.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
        
        manager.pointScored(2);
        assertEquals("TIE-BREAK: 1-1", manager.getTieBreakScore());
    }

    /**
     * TEST DEL BUG NEL CODICE SORGENTE (Reset nel Tie-Break):
     * Nel metodo checkTieBreakPoint, quando un giocatore vince il tie-break (es. arriva a 7 punti),
     * viene chiamato resetGameAndPoints(). Questo metodo azzera gamesP1 e gamesP2.
     * Successivamente viene chiamato checkSetPoint().
     * Poiché i game sono stati azzerati a 0, checkSetPoint fallisce nel decretare la vittoria del set
     * (perché 0-0 non è una condizione di vittoria).
     * Risultato: Il tie-break finisce ma il set ricomincia da 0-0 senza essere assegnato.
     */
    @Test
    public void testTieBreakWin_BugBehavior() {
        forceTieBreak(); // Siamo 6-6
        
        // P1 vince il tie break (7 punti a 0)
        for(int i=0; i<6; i++) manager.pointScored(1); 
        // Ultimo punto
        manager.pointScored(1); 
        
        // A questo punto, a causa del bug, il set NON viene assegnato a P1.
        // I game vengono resettati a 0-0 e il set corrente rimane 1.
        String score = manager.getMatchScore();
        
        // Asseriamo questo comportamento anomalo per confermare "weak mutation" e aderenza al codice
        assertTrue("A causa del bug, i set vinti rimangono 0", score.startsWith("0-0"));
        assertTrue("A causa del bug, i game vengono resettati a 0-0", score.contains("Game: 0-0"));
    }

    /**
     * Verifica la fine della partita (Game Over) con 3 set vinti.
     * Usiamo vittorie 6-0 per evitare il bug del tie-break.
     */
    @Test
    public void testGameOver() {
        // Set 1
        winSetP1_6_0();
        // Set 2
        winSetP1_6_0();
        // Set 3
        winSetP1_6_0();
        
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
        
        // Verifica che segnare punti dopo il Game Over non cambi nulla
        manager.pointScored(1);
        assertTrue(manager.getMatchScore().contains("P1: 3 Set"));
    }

    /**
     * Verifica la gestione di un giocatore non valido.
     */
    @Test
    public void testInvalidPlayer() {
        manager.pointScored(3); // Giocatore invalido
        assertEquals("Love-Love", manager.getGameScore());
        
        manager.pointScored(0); // Giocatore invalido
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    /**
     * Test per coprire la condizione di vittoria Tie-Break per P2.
     * Anche qui ci aspettiamo il reset anomalo a causa del bug.
     */
    @Test
    public void testTieBreakWinP2_BugBehavior() {
        forceTieBreak();
        
        // P2 vince il tie break (7 punti a 0)
        for(int i=0; i<7; i++) manager.pointScored(2);
        
        // Verifica comportamento buggato: reset dei game, nessun set assegnato
        String score = manager.getMatchScore();
        assertTrue(score.startsWith("0-0")); 
        assertTrue(score.contains("Game: 0-0"));
    }

    // --- Helper Methods ---

    private void winGame(int player) {
        for (int i = 0; i < 4; i++) {
            manager.pointScored(player);
        }
    }

    private void winSetP1_6_0() {
        for (int i = 0; i < 6; i++) {
            winGame(1);
        }
    }
    
    private void generateDeuce() {
        manager.pointScored(1); // 15
        manager.pointScored(2); // 15
        manager.pointScored(1); // 30
        manager.pointScored(2); // 30
        manager.pointScored(1); // 40
        manager.pointScored(2); // 40 - Deuce
    }
    
    private void forceTieBreak() {
        // P1 vince 5 game
        for(int i=0; i<5; i++) winGame(1);
        // P2 vince 6 game (5-6)
        for(int i=0; i<6; i++) winGame(2);
        // P1 vince 1 game (6-6)
        winGame(1);
    }
}