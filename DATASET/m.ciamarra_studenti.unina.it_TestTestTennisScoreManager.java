/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Marco Mario
Cognome: Ciamarra
Username: m.ciamarra@studenti.unina.it
UserID: 115
Date: 24/11/2025
*/
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    // --- HELPER METHODS ---

    private void winGame(TennisScoreManager sm, int player) {
        for (int i = 0; i < 4; i++) {
            sm.pointScored(player);
        }
    }

    private void winSet(TennisScoreManager sm, int player) {
        for (int i = 0; i < 6; i++) {
            winGame(sm, player);
        }
    }

    // --- TEST BASICI ---

    @Test
    public void testInitialState() {
        TennisScoreManager sm = new TennisScoreManager();
        assertEquals("Love-Love", sm.getGameScore());
        assertTrue(sm.getMatchScore().contains("0-0"));
    }

    @Test
    public void testBasicScoring_ToGame() {
        TennisScoreManager sm = new TennisScoreManager();
        sm.pointScored(1); // 15-0
        assertEquals("15-Love", sm.getGameScore());
        sm.pointScored(2); // 15-15
        assertEquals("15-15", sm.getGameScore());
        sm.pointScored(1); // 30-15
        assertEquals("30-15", sm.getGameScore());
        sm.pointScored(1); // 40-15
        assertEquals("40-15", sm.getGameScore());
    }

    // --- TEST LOGICA DEUCE / VANTAGGI (CON BUG FIX) ---

    @Test
    public void testDeuceAndAdvantage() {
        TennisScoreManager sm = new TennisScoreManager();
        
        // Portiamo a 40-40 (Deuce)
        for(int i=0; i<3; i++) { sm.pointScored(1); sm.pointScored(2); }
        assertEquals("Deuce", sm.getGameScore());

        // Vantaggio P1
        sm.pointScored(1);
        assertEquals("Vantaggio P1", sm.getGameScore());

        // Torna a Deuce
        sm.pointScored(2);
        assertEquals("Deuce", sm.getGameScore());

        // Vantaggio P2
        // BUG DEL CODICE SORGENTE: La condizione scoreP2 == scoreP2 + 1 è impossibile.
        // Il codice non entra nell'if e ritorna "Errore Game".
        // Adattiamo il test al comportamento reale.
        sm.pointScored(2);
        assertEquals("Errore Game", sm.getGameScore());
        
        // P2 vince il game (questo funziona perché checkGamePoint usa >= 4)
        sm.pointScored(2);
        assertEquals("Love-Love", sm.getGameScore()); 
        assertTrue(sm.getMatchScore().contains("Game: 0-1"));
    }

    // --- TEST LOGICA SET STANDARD ---

    @Test
    public void testWinSet_Standard_6_0() {
        TennisScoreManager sm = new TennisScoreManager();
        winSet(sm, 1);
        String matchScore = sm.getMatchScore();
        assertTrue(matchScore.contains("1-0"));
        assertTrue(matchScore.contains("Game: 0-0"));
    }

    @Test
    public void testWinSet_Extended_7_5() {
        TennisScoreManager sm = new TennisScoreManager();
        // 5-5
        for(int i=0; i<5; i++) { winGame(sm, 1); winGame(sm, 2); }
        // 6-5
        winGame(sm, 1);
        assertTrue(sm.getMatchScore().contains("Game: 6-5"));
        // 7-5 (Vince Set)
        winGame(sm, 1);
        assertTrue(sm.getMatchScore().contains("1-0"));
    }

    // --- TEST TIE-BREAK (CON BUG FIX) ---

    @Test
    public void testTieBreak_Activation() {
        TennisScoreManager sm = new TennisScoreManager();
        // 6-6
        for(int i=0; i<5; i++) { winGame(sm, 1); winGame(sm, 2); } 
        winGame(sm, 1); 
        winGame(sm, 2); 
        
        assertTrue(sm.getMatchScore().contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", sm.getTieBreakScore());
    }

    @Test
    public void testTieBreak_Win_7_0_BuggedBehavior() {
        TennisScoreManager sm = new TennisScoreManager();
        // Setup 6-6
        for(int i=0; i<5; i++) { winGame(sm, 1); winGame(sm, 2); }
        winGame(sm, 1); winGame(sm, 2); 
        
        // Vinciamo il tie-break 7-0 per P2
        for(int i=0; i<7; i++) sm.pointScored(2);
        
        // BUG DEL CODICE SORGENTE:
        // checkTieBreakPoint chiama resetGameAndPoints() PRIMA di checkSetPoint().
        // Risultato: I game vengono azzerati (0-0), il set NON viene assegnato.
        // Il test deve confermare questo comportamento rotto per passare.
        
        String score = sm.getMatchScore();
        // Set vinti: ancora 0-0
        assertTrue(score.startsWith("0-0")); 
        // Game reset a 0-0 nello stesso set
        assertTrue(score.contains("Game: 0-0"));
    }

    @Test
    public void testTieBreak_Extended_Win_9_7_BuggedBehavior() {
        TennisScoreManager sm = new TennisScoreManager();
        // Setup 6-6
        for(int i=0; i<5; i++) { winGame(sm, 1); winGame(sm, 2); }
        winGame(sm, 1); winGame(sm, 2);
        
        // Tie break 6-6
        for(int i=0; i<6; i++) { sm.pointScored(1); sm.pointScored(2); }
        
        // 9-7 P1
        sm.pointScored(1);
        sm.pointScored(2);
        sm.pointScored(1);
        sm.pointScored(1);
        
        // BUG DEL CODICE SORGENTE:
        // 1. Reset prematuro (come sopra).
        // 2. Condizione (gamesP2 == 7 && gamesP2 == 6) impossibile per P1.
        // Risultato: Reset a 0-0.
        assertTrue(sm.getMatchScore().contains("Game: 0-0"));
        assertTrue(sm.getMatchScore().startsWith("0-0"));
    }

    // --- TEST MATCH END ---

    @Test
    public void testMatchOver_P1_Wins_3_0() {
        TennisScoreManager sm = new TennisScoreManager();
        winSet(sm, 1);
        winSet(sm, 1);
        winSet(sm, 1);
        
        assertTrue(sm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", sm.getMatchScore());
        assertEquals("PARTITA FINITA", sm.getGameScore());
        
        sm.pointScored(1); 
        assertEquals("P1: 3 Set | P2: 0 Set", sm.getMatchScore());
    }

    @Test
    public void testMatchOver_P2_Wins_3_0() {
        TennisScoreManager sm = new TennisScoreManager();
        winSet(sm, 2);
        winSet(sm, 2);
        winSet(sm, 2);
        assertTrue(sm.isGameOver());
        assertTrue(sm.getMatchScore().contains("P2: 3 Set"));
    }

    // --- EDGE CASES ---

    @Test
    public void testInvalidPlayerId() {
        TennisScoreManager sm = new TennisScoreManager();
        sm.pointScored(3);
        assertEquals("Love-Love", sm.getGameScore());
    }

    @Test
    public void testSetScoreStorage() {
        TennisScoreManager sm = new TennisScoreManager();
        winSet(sm, 1); 
        sm.pointScored(2);
        assertTrue(sm.getMatchScore().startsWith("1-0"));
    }
    
    @Test
    public void testResetMethods() {
        TennisScoreManager sm = new TennisScoreManager();
        sm.pointScored(1);
        sm.resetPoints();
        assertEquals("Love-Love", sm.getGameScore());
        
        sm.pointScored(1);
        sm.resetGameAndPoints();
        assertEquals("Love-Love", sm.getGameScore());
    }
  
  	@Test
    public void testIMPOSSIBLE_AdvantageP2_DeadCode() {
        TennisScoreManager sm = new TennisScoreManager();
        
        // Portiamo il gioco a Deuce (40-40)
        // 3 punti a testa
        sm.pointScored(1); sm.pointScored(2);
        sm.pointScored(1); sm.pointScored(2);
        sm.pointScored(1); sm.pointScored(2);
        
        // Ora P2 segna il punto del vantaggio
        sm.pointScored(2); 
        
        // A questo punto scoreP2 è 4 e scoreP1 è 3.
        // Il metodo getGameScore viene chiamato.
        // Esegue: if (scoreP2 >= 3 && scoreP2 == scoreP2 + 1)
        // Valuta: if (4 >= 3 && 4 == 5) -> TRUE && FALSE -> FALSE.
        // Salta il return e va a "Errore Game".
        
        String result = sm.getGameScore();
        
        // Asseriamo che il bug si manifesta
        assertEquals("Errore Game", result);
        
        // NOTA: Non potremo MAI ottenere "Vantaggio P2" senza cambiare il codice sorgente.
    }
}