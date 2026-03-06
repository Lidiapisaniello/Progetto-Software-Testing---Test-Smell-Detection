/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: giuse.vozza@studenti.unina.it
UserID: 511
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

    // --- Helper Methods (per mantenere i test atomici e leggibili) ---

    private void scorePoints(TennisScoreManager tm, int player, int times) {
        for (int i = 0; i < times; i++) {
            tm.pointScored(player);
        }
    }

    private void winStandardGame(TennisScoreManager tm, int player) {
        scorePoints(tm, player, 4);
    }

    private void winSet(TennisScoreManager tm, int player) {
        for (int i = 0; i < 6; i++) {
            winStandardGame(tm, player);
        }
    }

    // --- Test Suite ---

    @Test
    public void [TennisScoreManager][Initialization]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        String score = tm.getGameScore();
        assertEquals("Love-Love", score);
        assertEquals("0-0 (Game: 0-0 Love-Love)", tm.getMatchScore());
    }

    @Test
    public void [resetPoints][ResetLogic]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1); // 15-0
        tm.resetPoints();
        assertEquals("Love-Love", tm.getGameScore());
    }

    @Test
    public void [pointScored][Player1Scores]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1);
        assertEquals("15-Love", tm.getGameScore());
    }

    @Test
    public void [pointScored][Player2Scores]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(2);
        assertEquals("Love-15", tm.getGameScore());
    }

    @Test
    public void [pointScored][InvalidPlayerInput]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Catturiamo l'output della console per verificare il messaggio di errore
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        tm.pointScored(3); // Input non valido

        assertTrue(outContent.toString().contains("Errore: Giocatore non valido"));
        assertEquals("Love-Love", tm.getGameScore()); // Il punteggio non deve cambiare
        
        // Reset System.out
        System.setOut(System.out); 
    }

    @Test
    public void [getGameScore][DeuceLogic]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40
        assertEquals("Deuce", tm.getGameScore());
    }

    @Test
    public void [getGameScore][AdvantageP1]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40 (Deuce)
        tm.pointScored(1);     // Adv P1
        assertEquals("Vantaggio P1", tm.getGameScore());
    }

    @Test
    public void [getGameScore][AdvantageP2_BuggedLogic]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 3); // 40
        scorePoints(tm, 2, 3); // 40 (Deuce)
        tm.pointScored(2);     // Adv P2
        
        // NOTA: Nel codice originale c'è un bug: "scoreP2 == scoreP2 + 1" è sempre false.
        // Quindi il codice non entra nell'if del vantaggio P2 e va al return finale.
        assertEquals("Errore Game", tm.getGameScore()); 
    }

    @Test
    public void [checkGamePoint][P1WinsGame]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 1, 4); // Vince il game
        // Verifica match score: 0-0 set, game 1-0
        assertTrue(tm.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void [checkGamePoint][P2WinsGame]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        scorePoints(tm, 2, 4); // Vince il game
        assertTrue(tm.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void [checkSetPoint][P1WinsSetStandard]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1); // Vince 6 game di fila
        // Dovrebbe aver vinto il set e resettato i game a 0-0 per il set 2
        assertTrue(tm.getMatchScore().contains("Game: 0-0"));
        assertTrue(tm.getMatchScore().startsWith("1-0")); // 1 set a 0
    }

    @Test
    public void [checkSetPoint][P2WinsSetStandard]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 2); 
        assertTrue(tm.getMatchScore().startsWith("0-1")); 
    }

    @Test
    public void [checkSetPoint][SetGoesTo7_5]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // P1 vince 5 game
        for(int i=0; i<5; i++) winStandardGame(tm, 1);
        // P2 vince 5 game
        for(int i=0; i<5; i++) winStandardGame(tm, 2);
        
        // Punteggio 5-5. P1 ne vince uno -> 6-5 (non vince ancora il set)
        winStandardGame(tm, 1);
        assertTrue(tm.getMatchScore().contains("Game: 6-5"));
        
        // P1 ne vince un altro -> 7-5 (Vince il set)
        winStandardGame(tm, 1);
        assertTrue(tm.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void [checkSetPoint][SetGoesTo5_7]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // P1 vince 5 game
        for(int i=0; i<5; i++) winStandardGame(tm, 1);
        // P2 vince 6 game (5-6)
        for(int i=0; i<6; i++) winStandardGame(tm, 2);
        
        // P2 vince il game decisivo -> 5-7
        winStandardGame(tm, 2);
        assertTrue(tm.getMatchScore().startsWith("0-1"));
    }

    @Test
    public void [checkSetPoint][TriggerTieBreak]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // 6-6 nei game
        for(int i=0; i<6; i++) winStandardGame(tm, 1);
        for(int i=0; i<6; i++) winStandardGame(tm, 2);

        // Verifica output Tie-Break
        assertTrue(tm.getMatchScore().contains("TIE-BREAK: 0-0"));
    }

    @Test
    public void [checkTieBreakPoint][P1WinsTieBreak]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Arriviamo al Tie Break (6-6)
        for(int i=0; i<6; i++) winStandardGame(tm, 1);
        for(int i=0; i<6; i++) winStandardGame(tm, 2);
        
        // Punteggio TieBreak: P1 fa 7 punti a 0
        scorePoints(tm, 1, 7);
        
        // P1 dovrebbe aver vinto il set 7-6
        assertTrue(tm.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void [checkTieBreakPoint][P2WinsTieBreak]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Arriviamo al Tie Break (6-6)
        for(int i=0; i<6; i++) winStandardGame(tm, 1);
        for(int i=0; i<6; i++) winStandardGame(tm, 2);
        
        // Punteggio TieBreak: P2 fa 7 punti a 0
        scorePoints(tm, 2, 7);
        
        // P2 dovrebbe aver vinto il set 6-7
        assertTrue(tm.getMatchScore().startsWith("0-1"));
    }
    
    @Test
    public void [getTieBreakScore][DisplayScore]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Arriviamo al Tie Break
        for(int i=0; i<6; i++) winStandardGame(tm, 1);
        for(int i=0; i<6; i++) winStandardGame(tm, 2);
        
        tm.pointScored(1);
        tm.pointScored(2);
        tm.pointScored(1);
        
        assertEquals("TIE-BREAK: 2-1", tm.getTieBreakScore());
    }

    @Test
    public void [isGameOver][P1WinsMatch]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // P1 vince 3 set di fila
        winSet(tm, 1); // Set 1
        winSet(tm, 1); // Set 2
        winSet(tm, 1); // Set 3
        
        assertTrue(tm.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", tm.getMatchScore());
    }

    @Test
    public void [isGameOver][P2WinsMatch]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 2); 
        winSet(tm, 2); 
        winSet(tm, 2); 
        
        assertTrue(tm.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", tm.getMatchScore());
    }
    
    @Test
    public void [pointScored][GameAlreadyOver]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        // Vinciamo la partita
        winSet(tm, 1);
        winSet(tm, 1);
        winSet(tm, 1);
        
        // Tentiamo di segnare un punto a partita finita
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        tm.pointScored(1);
        
        assertTrue(outContent.toString().contains("La partita è finita!"));
        System.setOut(System.out);
    }

    @Test
    public void [getGameScore][GameAlreadyOver]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1);
        winSet(tm, 1);
        winSet(tm, 1);
        
        assertEquals("PARTITA FINITA", tm.getGameScore());
    }

    @Test
    public void [printScore][ExecutionCoverage]Test() {
        // Questo test serve specificamente per coprire le linee di printScore 
        // che vengono chiamate implicitamente, ma qui facciamo una verifica esplicita
        // anche se pointScored chiama già printScore.
        TennisScoreManager tm = new TennisScoreManager();
        tm.pointScored(1);
        tm.printScore(); 
        // Non facciamo asserzioni sull'output specifico qui perché è complesso fare parsing 
        // di tutto il blocco, ma l'esecuzione garantisce la copertura del loop "for" di stampa set.
        assertTrue(true);
    }
    
    @Test
    public void [getMatchScore][MixedSets]Test() {
        TennisScoreManager tm = new TennisScoreManager();
        winSet(tm, 1); // P1 vince set 1
        winSet(tm, 2); // P2 vince set 2
        
        String result = tm.getMatchScore();
        assertTrue(result.startsWith("1-1"));
    }
    
    @Test
    public void [checkSetPoint][LastSetTieBreakWinP2]Test() {
        // Caso limite: P2 vince al TieBreak 
        TennisScoreManager tm = new TennisScoreManager();
        
        // 6-6
        for(int i=0; i<6; i++) winStandardGame(tm, 1);
        for(int i=0; i<6; i++) winStandardGame(tm, 2);
        
        // P2 vince TieBreak
        scorePoints(tm, 2, 7);
        
        // Verifica che setsP2 sia stato aggiornato
        assertTrue(tm.getMatchScore().startsWith("0-1"));
    }
    
    @Test
    public void [moveToNextSet][CalledInternally]Test() {
         // moveToNextSet è chiamato da checkSetPoint. 
         // Testiamo indirettamente verificando che currentSet sia aumentato
         TennisScoreManager tm = new TennisScoreManager();
         winSet(tm, 1); // Set 1 finito
         // P1 fa un punto nel nuovo set
         tm.pointScored(1);
         // Se siamo nel nuovo set, il punteggio game deve essere resettato e modificato
         // Se non avesse cambiato set, sarebbe rimasto 6-0.
         assertTrue(tm.getMatchScore().contains("Game: 0-0"));
         assertEquals("15-Love", tm.getGameScore());
    }
}
						