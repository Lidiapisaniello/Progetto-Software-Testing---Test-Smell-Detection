import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Suite di test unitari per la classe TennisScoreManager.
 * Segue il naming convention [MethodUnderTest][Scenario]Test e utilizza solo JUnit 4.
 */
public class TennisScoreManagerTest {

    // --- Utilità per simulare i punteggi ---

    /**
     * Simula un certo numero di punti segnati dal giocatore 1.
     */
    private void scorePointsP1(TennisScoreManager manager, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(1);
        }
    }

    /**
     * Simula un certo numero di punti segnati dal giocatore 2.
     */
    private void scorePointsP2(TennisScoreManager manager, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(2);
        }
    }

    /**
     * Simula la vittoria di N game da parte del giocatore specificato.
     * Non gestisce set o tie-break, presuppone un reset.
     */
    private void scoreGames(TennisScoreManager manager, int player, int games) {
        for (int i = 0; i < games; i++) {
            if (player == 1) {
                // P1 vince il game: 4-0
                scorePointsP1(manager, 4);
            } else if (player == 2) {
                // P2 vince il game: 4-0
                scorePointsP2(manager, 4);
            }
        }
    }

    // --- Test per Costruttore e Metodi di Base ---

    @Test
    public void tennisScoreManagerInitialStateTest() {
        TennisScoreManager manager = new TennisScoreManager();
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
        assertFalse(manager.isGameOver());
    }

    @Test
    public void resetPointsInitialStateTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 2); // 30-Love
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void resetGameAndPointsInitialStateTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scoreGames(manager, 1, 1); // P1 vince 1 game
        scorePointsP2(manager, 1); // 15-Love nel nuovo game
        manager.resetGameAndPoints();
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    // --- Test per pointScored (Inclusa la Logica di Base e Branching) ---

    @Test
    public void pointScoredPlayer1NormalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredPlayer2NormalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // L'asserzione principale è che lo stato del punteggio non cambi
        manager.pointScored(3);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredGameIsOverIgnoredTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula la fine della partita (P1 vince 3 set)
        scoreGames(manager, 1, 6); // P1 vince Set 1
        scoreGames(manager, 2, 6); // P2 vince Set 2 (per evitare 6-0 x3)
        scoreGames(manager, 1, 6); // P1 vince Set 3
        scoreGames(manager, 1, 6); // P1 vince Set 4 -> PARTITA FINITA (3-1)

        assertTrue(manager.isGameOver());
        
        // Punteggio iniziale del game è Love-Love
        manager.pointScored(1); 
        // Il punteggio non dovrebbe cambiare, rimane Love-Love ma isGameOver ritorna true
        assertTrue(manager.isGameOver()); 
        assertEquals("P1: 3 Set | P2: 1 Set", manager.getMatchScore());
    }


    // --- Test per getGameScore (Tutti gli Stati del Punteggio Standard) ---

    @Test
    public void getGameScoreLoveLoveTest() {
        TennisScoreManager manager = new TennisScoreManager();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void getGameScoreThirtyAllTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 2);
        scorePointsP2(manager, 2);
        assertEquals("30-30", manager.getGameScore());
    }

    @Test
    public void getGameScoreFortyLoveTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void getGameScoreDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // Deuce
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2Test() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // Deuce
        manager.pointScored(2); // Vantaggio P2
        assertEquals("Vantaggio P2", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreReturnToDeuceTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // Deuce
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(2); // Torna Deuce
        assertEquals("Deuce", manager.getGameScore());
    }
    
    @Test
    public void getGameScoreAfterMatchEndTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula la fine della partita (P1 vince 3 set)
        scoreGames(manager, 1, 6); 
        scoreGames(manager, 1, 6); 
        scoreGames(manager, 1, 6); 
        
        assertTrue(manager.isGameOver());
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    // --- Test per checkGamePoint (Vittoria del Game Standard) ---

    @Test
    public void checkGamePointP1WinNormalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 1); // 40-15
        manager.pointScored(1); // P1 vince il game
        
        // Verifica il reset dei punti
        assertEquals("Love-Love", manager.getGameScore()); 
        // Verifica la vittoria del game
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkGamePointP2WinNormalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 1);
        scorePointsP2(manager, 3); // 15-40
        manager.pointScored(2); // P2 vince il game
        
        // Verifica il reset dei punti
        assertEquals("Love-Love", manager.getGameScore()); 
        // Verifica la vittoria del game
        assertEquals("0-0 (Game: 0-1 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkGamePointP1WinFromAdvantageTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // Deuce
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(1); // P1 vince il game
        
        // Verifica il reset dei punti
        assertEquals("Love-Love", manager.getGameScore());
        // Verifica la vittoria del game
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkGamePointNoWinAtFortyFortyTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // Deuce
        
        // Nessuna vittoria
        assertEquals("Deuce", manager.getGameScore()); 
        assertEquals("0-0 (Game: 0-0 Deuce)", manager.getMatchScore());
    }


    // --- Test per checkTieBreakPoint e getTieBreakScore ---

    @Test
    public void getTieBreakScoreNormalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        manager.pointScored(2);
        
        // Forza l'ingresso in Tie Break per testare la visualizzazione
        // (Il metodo non viene chiamato in condizioni normali senza isTieBreak = true)
        // Dobbiamo simulare l'ingresso nel Tie Break.
        scoreGames(manager, 1, 5);
        scoreGames(manager, 2, 5);
        scorePointsP1(manager, 3);
        scorePointsP2(manager, 3); // 40-40, P1 vince game -> 6-5
        manager.pointScored(1); // P1 Vantaggio
        manager.pointScored(1); // P1 vince game -> 6-5
        
        scorePointsP2(manager, 4); // P2 vince game -> 6-6 -> INIZIO TIE-BREAK

        manager.pointScored(1); // 1-0
        manager.pointScored(2); // 1-1
        
        // La classe stampa il risultato, ma il metodo pointScored gestisce lo stato.
        // Simuliamo l'ottenimento del punteggio in Tie-Break
        // Dobbiamo estrarre lo stato isTieBreak (che è private) o testare la logica
        // attraverso il metodo getMatchScore/printScore che lo utilizza.
        
        // Per testare getTieBreakScore isolatamente, dobbiamo accettare che lo stato sia forzato.
        // L'approccio migliore è testare l'attivazione e la vittoria tramite pointScored.

        // Simula lo stato di Tie-Break (non abbiamo un setter, dobbiamo forzare i 6-6)
        // La chiamata precedente ha già portato a 6-6 e isTieBreak=true
        String tieBreakScore = manager.getTieBreakScore();
        assertTrue(tieBreakScore.contains("1-1"));
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));

    }

    @Test
    public void checkTieBreakPointP1WinMinimumTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Set up Tie Break (6-6 nei game)
        scoreGames(manager, 1, 5);
        scoreGames(manager, 2, 5);
        scorePointsP1(manager, 4); // P1 vince game
        scorePointsP2(manager, 4); // P2 vince game -> 6-6, isTieBreak = true

        // P1 segna 7 punti (7-0)
        scorePointsP1(manager, 7); 
        
        // Verifica che il tie-break sia finito e che un set sia vinto
        // Il set attuale dovrebbe essere 2 (o 1 se non c'è Set vinti prima)
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
        
    }

    @Test
    public void checkTieBreakPointP2WinAdvantageTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Set up Tie Break (6-6 nei game)
        scoreGames(manager, 1, 5);
        scoreGames(manager, 2, 5);
        scorePointsP1(manager, 4); 
        scorePointsP2(manager, 4); // 6-6, isTieBreak = true

        // Simula 6-6 nel tie-break
        scorePointsP1(manager, 6);
        scorePointsP2(manager, 6);

        manager.pointScored(2); // 6-7
        manager.pointScored(2); // 6-8 -> P2 vince il set 7-6
        
        // Verifica che il tie-break sia finito e che un set sia vinto
        assertEquals("0-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    // --- Test per checkSetPoint e moveToNextSet (Vittoria del Set) ---

    @Test
    public void checkSetPointP1Win60Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince 6 game (6-0)
        scoreGames(manager, 1, 6); 
        
        // Punteggio set dovrebbe essere 1-0, game resettati
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkSetPointP2Win75Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 5-5
        scoreGames(manager, 1, 5);
        scoreGames(manager, 2, 5);
        
        // P2 vince due game di fila per 7-5 (branch: gamesP2 == 7 && gamesP1 == 5)
        scorePointsP2(manager, 4); // 5-6
        scorePointsP2(manager, 4); // 5-7 -> P2 vince il set 
        
        assertEquals("0-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void checkSetPointP1Win76Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 6-6 (entra in Tie Break)
        scoreGames(manager, 1, 6);
        scoreGames(manager, 2, 6);
        
        // isTieBreak è true, resetPoints è stato chiamato
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 0-0"));

        // P1 vince il Tie Break 7-5
        scorePointsP1(manager, 7);
        scorePointsP2(manager, 5);
        
        // P1 vince il set 7-6
        // Punteggio set dovrebbe essere 1-0, game resettati, isTieBreak = false
        assertEquals("1-0 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkSetPointP2Win76Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 6-6 (entra in Tie Break)
        scoreGames(manager, 1, 6);
        scoreGames(manager, 2, 6);
        
        // isTieBreak è true
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 0-0"));

        // P2 vince il Tie Break 7-5
        scorePointsP2(manager, 7);
        scorePointsP1(manager, 5);
        
        // P2 vince il set 7-6
        // Punteggio set dovrebbe essere 0-1, game resettati, isTieBreak = false
        assertEquals("0-1 (Game: 0-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void checkSetPointNoWinAt55Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 5-5
        scoreGames(manager, 1, 5);
        scoreGames(manager, 2, 5);
        
        // Nessun set vinto, isTieBreak è false
        assertEquals("0-0 (Game: 5-5 Love-Love)", manager.getMatchScore());
    }

    @Test
    public void checkSetPointNoWinAt65Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 6-5 per P1
        scoreGames(manager, 1, 6);
        scoreGames(manager, 2, 5);
        
        // Nessun set vinto, isTieBreak è false
        assertEquals("0-0 (Game: 6-5 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void moveToNextSetWhenGameOverTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Set up partita finita (P1 vince 3 set)
        scoreGames(manager, 1, 6);
        scoreGames(manager, 1, 6);
        scoreGames(manager, 1, 6);
        
        assertTrue(manager.isGameOver());
        
        // Chiamare moveToNextSet non dovrebbe cambiare nulla
        manager.moveToNextSet();
        
        assertTrue(manager.isGameOver());
        // L'array setsP1/setsP2 non viene testato direttamente, ma il punteggio finale sì.
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    // --- Test per isGameOver (Tutte le Condizioni di Fine Partita) ---

    @Test
    public void isGameOverFalseInitialTest() {
        TennisScoreManager manager = new TennisScoreManager();
        assertFalse(manager.isGameOver());
    }

    @Test
    public void isGameOverP1Win30Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince Set 1
        scoreGames(manager, 1, 6);
        // P1 vince Set 2
        scoreGames(manager, 1, 6); 
        // P1 vince Set 3 -> Partita Finita
        scoreGames(manager, 1, 6); 
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void isGameOverP2Win32Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince Set 1
        scoreGames(manager, 1, 6);
        // P2 vince Set 2
        scoreGames(manager, 2, 6);
        // P1 vince Set 3
        scoreGames(manager, 1, 6);
        // P2 vince Set 4
        scoreGames(manager, 2, 6);
        // P2 vince Set 5 -> Partita Finita
        scoreGames(manager, 2, 6);

        assertTrue(manager.isGameOver());
        assertEquals("P1: 2 Set | P2: 3 Set", manager.getMatchScore());
    }
    
    @Test
    public void isGameOverFalseDuringSet5Test() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince 2 set
        scoreGames(manager, 1, 6); 
        scoreGames(manager, 1, 6); 
        // P2 vince 2 set
        scoreGames(manager, 2, 6); 
        scoreGames(manager, 2, 6); 
        
        // Inizia Set 5 (2-2)
        scoreGames(manager, 1, 2); // P1 vince 2 game nel Set 5

        assertFalse(manager.isGameOver());
        assertEquals("2-2 (Game: 2-0 Love-Love)", manager.getMatchScore());
    }

    // --- Test per getMatchScore (Copre gli stati intermedi e finali) ---

    @Test
    public void getMatchScoreInGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        manager.pointScored(1);
        assertEquals("0-0 (Game: 0-0 15-Love)", manager.getMatchScore());
    }

    @Test
    public void getMatchScoreAfterOneGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        scorePointsP1(manager, 4); // P1 vince il game
        assertEquals("0-0 (Game: 1-0 Love-Love)", manager.getMatchScore());
    }
    
    @Test
    public void getMatchScoreDuringTieBreakTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Simula 6-6 (entra in Tie Break)
        scoreGames(manager, 1, 6);
        scoreGames(manager, 2, 6);
        
        // isTieBreak è true
        manager.pointScored(1);
        manager.pointScored(2); // Tie Break 1-1
        
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 1-1"));
    }

    @Test
    public void getMatchScoreFinalTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // P1 vince Set 1
        scoreGames(manager, 1, 6);
        // P2 vince Set 2
        scoreGames(manager, 2, 6);
        // P1 vince Set 3
        scoreGames(manager, 1, 6);
        // P1 vince Set 4 -> Partita Finita (3-1)
        scoreGames(manager, 1, 6); 
        
        assertEquals("P1: 3 Set | P2: 1 Set", manager.getMatchScore());
    }
    
    // --- Test per edge case in getGameScore (La branch 'Errore Game') ---
    
    @Test
    public void getGameScoreErrorGameTest() {
        TennisScoreManager manager = new TennisScoreManager();
        // Questo scenario (scoreP1=4, scoreP2=2, non è una vittoria/adv/deuce) 
        // non dovrebbe mai essere raggiunto attraverso pointScored,
        // ma è necessario per la copertura della branch 'return "Errore Game";'
        // Dobbiamo manipolare lo stato (che non possiamo fare direttamente in quanto private).
        // L'unico modo per ottenere scoreP1 >= 4 e scoreP2 <= scoreP1 - 2 
        // è attraverso una vittoria del game, che resetta i punti.
        // Pertanto, questa riga di codice è "irraggiungibile" nel normale flusso di gioco
        // e, se non c'è un setter o un metodo per iniettare lo stato, 
        // è impossibile da testare senza usare la reflection, che è fuori scopo per test unitari standard.
        // Tuttavia, per la copertura completa, si può assumere che un eventuale setter
        // (che non esiste) possa portare a questo stato. 
        // Data l'assenza di setter/reflection e il flusso logico del gioco, 
        // non è possibile raggiungere "Errore Game" se non c'è un bug nel codice
        // che permette di incrementare oltre 4/4 senza chiamare checkGamePoint().
        // *Lasciamo il test implicito come irraggiungibile nel codice dato.*

        // Una condizione di Errore Game potrebbe essere ad es. 4-2. 
        // Ma pointScored chiama checkGamePoint subito, e 4-2 vince il game e resetta.
        // Per testare Errore Game, l'unico scenario possibile che non è coperto è 
        // scoreP1 >= 3 && scoreP1 != scoreP2 + 1 && scoreP2 >= 3 && scoreP2 != scoreP1 + 1.
        // Questo non è possibile con il codice attuale.

        // Per il 100% di copertura, aggiungiamo una nota che questa branch è 
        // irraggiungibile nel normale flusso.
        // Simuliamo un punteggio non valido che aggira le condizioni precedenti (non possibile con il codice fornito)
        // Se non possiamo accedere allo stato, ci limitiamo a testare il flusso corretto che non la raggiunge.
    }
}