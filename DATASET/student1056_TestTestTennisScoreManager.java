import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final int P1 = 1;
    private final int P2 = 2;

    @BeforeClass
    public static void setUpClass() {
        // Eseguito una volta prima di tutti i test
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta dopo tutti i test
    }

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        manager = null;
    }

    // ----------------------- METODI DI SUPPORTO -----------------------------

    /** Fa vincere un game standard (4 punti consecutivi) al giocatore indicato. */
    private void winStandardGame(int player) {
        for (int i = 0; i < 4; i++) {
            manager.pointScored(player);
        }
    }

    /** Fa vincere un set 6-0 al giocatore indicato (senza tie-break). */
    private void winSetSixZero(int player) {
        for (int i = 0; i < 6; i++) {
            winStandardGame(player);
        }
    }

    /** Porta il punteggio games del set corrente a 6-6 (attiva il tie-break). */
    private void reachSixSixTieBreak() {
        for (int i = 0; i < 6; i++) {
            winStandardGame(P1);
            winStandardGame(P2);
        }
    }

    // ----------------------- TEST STATO INIZIALE ----------------------------

    @Test
    public void initialState_hasLoveLoveAndNoSetsWon() {
        assertEquals("Love-Love", manager.getGameScore());

        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.startsWith("0-0"));     // 0-0 set
        assertTrue(matchScore.contains("Game: 0-0")); // 0-0 game
        assertFalse(manager.isGameOver());
    }

    // ----------------------- TEST RESET ------------------------------------

    @Test
    public void resetPoints_resetsCurrentGameScore() {
        manager.pointScored(P1);
        manager.pointScored(P2);
        assertNotEquals("Love-Love", manager.getGameScore());

        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void resetGameAndPoints_resetsGamesAndPoints() {
        winStandardGame(P1); // P1 vince un game → 1-0

        String beforeReset = manager.getMatchScore();
        assertTrue(beforeReset.contains("Game: 1-0"));

        manager.resetGameAndPoints();

        String afterReset = manager.getMatchScore();
        assertTrue(afterReset.contains("Game: 0-0"));
        assertEquals("Love-Love", manager.getGameScore());
    }

    // ----------------------- TEST GAME NORMALE ------------------------------

    @Test
    public void player1WinsGameWithoutDeuce() {
        winStandardGame(P1);

        // Dopo la vittoria del game i punti devono essere resettati
        assertEquals("Love-Love", manager.getGameScore());

        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("Game: 1-0"));
        assertTrue(matchScore.startsWith("0-0")); // 0-0 set, 1-0 game nel set corrente
    }
    
    // ----------------------- TEST DEUCE E “VANTAGGI” ------------------------

    @Test
    public void deuceSequenceAndGameWinForPlayer2_withCurrentImplementation() {
        // Portiamo a 40-40 (Deuce) → 3-3
        for (int i = 0; i < 3; i++) {
            manager.pointScored(P1);
            manager.pointScored(P2);
        }
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P1
        manager.pointScored(P1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // Torna Deuce
        manager.pointScored(P2);
        assertEquals("Deuce", manager.getGameScore());

        // Ora P2 segna ancora:
        manager.pointScored(P2);

        // A causa del bug nella classe originale, qui otteniamo "Errore Game":
        assertEquals("Errore Game", manager.getGameScore());

        // P2 segna ancora un punto e vince comunque il game
        manager.pointScored(P2);

        assertEquals("Love-Love", manager.getGameScore());
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("Game: 0-1"));
    }
    
    // NUOVO TEST: Vittoria Game da Vantaggio P1 (copre ramo checkGamePoint per P1)
    @Test
    public void deuceSequenceAndGameWinForPlayer1() {
        // Portiamo a 40-40 (Deuce) → 3-3
        for (int i = 0; i < 3; i++) {
            manager.pointScored(P1);
            manager.pointScored(P2);
        }
        assertEquals("Deuce", manager.getGameScore());

        // Vantaggio P1 (4-3)
        manager.pointScored(P1);
        assertEquals("Vantaggio P1", manager.getGameScore());

        // P1 segna di nuovo e vince il game (copre checkGamePoint per P1 da vantaggio)
        manager.pointScored(P1);

        assertEquals("Love-Love", manager.getGameScore());
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("Game: 1-0"));
    }

    @Test
    public void invalidPlayerDoesNotChangeScore() {
        assertEquals("Love-Love", manager.getGameScore());

        manager.pointScored(3); // Giocatore non valido

        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }

    // ----------------------- TEST SET SENZA TIE-BREAK -----------------------

    @Test
    public void player1WinsSetSixZero() {
        winSetSixZero(P1); // 6-0 per P1 nel primo set

        String matchScore = manager.getMatchScore();

        assertTrue(matchScore.startsWith("1-0"));     // 1-0 set
        assertTrue(matchScore.contains("Game: 0-0")); // nuovo set 0-0 game
        assertFalse(manager.isGameOver());
    }

    @Test
    public void player2WinsSetSixFour() {
        // 4 game P1
        for (int i = 0; i < 4; i++) {
            winStandardGame(P1);
        }
        // 6 game P2
        for (int i = 0; i < 6; i++) {
            winStandardGame(P2);
        }

        String matchScore = manager.getMatchScore();

        assertTrue(matchScore.startsWith("0-1"));     // 0-1 set
        assertTrue(matchScore.contains("Game: 0-0")); // nuovo set 0-0 game
        assertFalse(manager.isGameOver());
    }
    
    // NUOVO TEST: Set 6-5 non vincente (verifica ramo di codice gamesP1 == 6 && gamesP2 == 5)
    @Test
    public void setGame_ReachesSixFive_NotFinished() {
        // P1 5 giochi, P2 5 giochi
        for (int i = 0; i < 5; i++) {
            winStandardGame(P1);
            winStandardGame(P2);
        }
        
        // P1 vince un game → 6-5
        winStandardGame(P1);

        // Il set non è finito perché non c'è uno scarto di 2 giochi (6-5)
        String matchScore = manager.getMatchScore();
        assertFalse(matchScore.startsWith("1-0")); // Set score non deve essere aggiornato
        assertTrue(matchScore.contains("Game: 6-5")); 
        assertFalse(manager.isGameOver());
    }


    // ----------------------- TEST TIE-BREAK: AVVIO E PUNTEGGIO --------------

    @Test
    public void reachingSixSixStartsTieBreak() {
        reachSixSixTieBreak();

        // Dopo il 6-6, la classe imposta isTieBreak = true
        // e getMatchScore deve includere la stringa "TIE-BREAK"
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("TIE-BREAK"));
    }

    @Test
    public void tieBreakScoreIsReportedInMatchScore() {
        reachSixSixTieBreak();

        // In tie-break, facciamo segnare alcuni punti senza chiudere il tie-break
        manager.pointScored(P1); // 1-0
        manager.pointScored(P2); // 1-1
        manager.pointScored(P1); // 2-1

        String matchScore = manager.getMatchScore();
        // Deve mostrare la stringa con il punteggio del tie-break (2-1)
        assertTrue(matchScore.contains("TIE-BREAK: 2-1"));
    }

    // ----------------------- TEST TIE-BREAK: VITTORIA (BUGGY) ---------------

    @Test
    public void player1WinsTieBreakButSetNotRecordedDueToBug() {
        reachSixSixTieBreak();

        // Tie-break: simuliamo un 7-5 per P1
        // Arriviamo prima a 5-5
        for (int i = 0; i < 5; i++) {
            manager.pointScored(P1);
            manager.pointScored(P2);
        }
        // Ora 5-5. P1 fa due punti consecutivi → 7-5
        manager.pointScored(P1); // 6-5
        manager.pointScored(P1); // 7-5 => checkTieBreakPoint scatta

        // A causa del bug: games vengono resettati a 0-0 e il set non viene registrato.
        String matchScore = manager.getMatchScore();

        assertTrue(matchScore.startsWith("0-0"));           // nessun set vinto
        assertTrue(matchScore.contains("Game: 0-0"));       // games azzerati
        assertTrue(matchScore.contains("Love-Love"));       // punti azzerati
        assertFalse(manager.isGameOver());
    }
    
    // NUOVO TEST: Tie-Break prolungato (copre la condizione scoreX >= 7 && scoreX >= scoreY + 2)
    @Test
    public void tieBreakWin_ProlongedScore_7_7to9_7_DueToBug() {
        reachSixSixTieBreak(); // 6-6 games
        
        // Punti 7-7 (oltre il 6-6)
        for (int i = 0; i < 7; i++) {
            manager.pointScored(P1);
            manager.pointScored(P2);
        }
        
        // P1 segna (8-7)
        manager.pointScored(P1);
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 8-7"));
        
        // P1 segna (9-7) e dovrebbe vincere il set, ma fallisce a causa del bug
        manager.pointScored(P1);
        
        // Asserzione che verifica il bug: il set non è registrato e i giochi sono resettati
        assertTrue(manager.getMatchScore().startsWith("0-0")); 
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }


    @Test
    public void player2WinsTieBreakButSetNotRecordedDueToBug() {
        reachSixSixTieBreak();

        // Tie-break: simuliamo un 7-5 per P2
        for (int i = 0; i < 5; i++) {
            manager.pointScored(P2);
            manager.pointScored(P1);
        }
        // 5-5, ora due punti P2
        manager.pointScored(P2); // 6-5
        manager.pointScored(P2); // 7-5

        String matchScore = manager.getMatchScore();

        assertTrue(matchScore.startsWith("0-0"));           // nessun set vinto
        assertTrue(matchScore.contains("Game: 0-0"));       // games azzerati
        assertTrue(matchScore.contains("Love-Love"));       // punti azzerati
        assertFalse(manager.isGameOver());
    }

    // ----------------------- TEST FINE PARTITA ------------------------------

    @Test
    public void player1WinsMatchThreeZero() {
        winSetSixZero(P1);
        winSetSixZero(P1);
        winSetSixZero(P1);

        assertTrue(manager.isGameOver());

        String matchScore = manager.getMatchScore();
        assertEquals("P1: 3 Set | P2: 0 Set", matchScore);

        // Ulteriori punti non devono cambiare l'esito
        manager.pointScored(P1);
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void player2WinsMatchThreeTwoWithMixedSets() {
        // Sequenza di set: P1, P2, P2, P1, P2
        winSetSixZero(P1); // 1-0
        winSetSixZero(P2); // 1-1
        winSetSixZero(P2); // 1-2
        winSetSixZero(P1); // 2-2
        winSetSixZero(P2); // 2-3

        assertTrue(manager.isGameOver());

        String matchScore = manager.getMatchScore();
        assertEquals("P1: 2 Set | P2: 3 Set", matchScore);
    }

    // ----------------------- TEST EXTRA: METODI DOPO GAME OVER --------------

    @Test
    public void getGameScoreAfterMatchOver_returnsPartitaFinita() {
        // Facciamo finire il match
        winSetSixZero(P1);
        winSetSixZero(P1);
        winSetSixZero(P1);
        assertTrue(manager.isGameOver());

        // Ora getGameScore deve tornare "PARTITA FINITA"
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    @Test
    public void moveToNextSetDoesNothingAfterGameOver() {
        // Match finito
        winSetSixZero(P1);
        winSetSixZero(P1);
        winSetSixZero(P1);
        assertTrue(manager.isGameOver());

        String before = manager.getMatchScore();
        manager.moveToNextSet(); // chiamata esplicita sul ramo !isGameOver() == false
        String after = manager.getMatchScore();

        // Lo stato del match deve rimanere invariato
        assertEquals(before, after);
    }
}