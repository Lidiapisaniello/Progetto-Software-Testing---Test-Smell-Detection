import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * Configura l'ambiente di test prima di ogni test.
     * Inizializza un nuovo TennisScoreManager e reindirizza System.out 
     * per catturare l'output dei metodi come printScore() e isGameOver().
     */
    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        // Reindirizza System.out per sopprimere/catturare l'output della console
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Ripristina l'ambiente di test dopo ogni test.
     * Ripristina System.out al suo valore originale.
     */
    public void restoreStreams() {
        System.setOut(System.out);
    }

    // --- Metodi Helper per simulare i punti ---

    /**
     * Simula il punteggio standard del tennis: Love, 15, 30, 40.
     * @param player Giocatore che segna (1 o 2)
     */
    private void scorePoint(int player) {
        manager.pointScored(player);
    }

    /**
     * Simula la vittoria di un intero Game per un giocatore.
     * @param player Giocatore che vince il Game
     */
    private void winGame(int player) {
        // Se il game è a 40-40, il game si vince con 2 punti
        if (manager.getGameScore().equals("Deuce")) {
            scorePoint(player); // Advantage
            scorePoint(player); // Game
        } else {
            // Dal punteggio corrente, vince i punti necessari
            // Questo helper funziona solo se il game è all'inizio o quasi
            int pointsToWin = 4 - (player == 1 ? getScoreP1() : getScoreP2());
            for (int i = 0; i < pointsToWin; i++) {
                scorePoint(player);
            }
        }
    }

    /**
     * Simula la vittoria di un set con punteggio 6-X.
     * @param winner Giocatore che vince il Set
     * @param loserGames Game del perdente (es. 4 per 6-4)
     */
    private void winSet(int winner, int loserGames) {
        // Punteggio iniziale 0-0
        for (int i = 0; i < loserGames; i++) {
            winGame(winner); // Il vincitore vince
            winGame(winner == 1 ? 2 : 1); // Il perdente vince
        }
        // Il vincitore vince i game rimanenti
        for (int i = loserGames; i < 6; i++) {
            winGame(winner);
        }
    }

    // --- Metodi Getter (Tramite Reflection o estrazione, qui simulati per l'accesso ai dati) ---
    // Questi non sono metodi della classe originale, ma aiutano nei test

    private int getScoreP1() {
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("scoreP1");
            field.setAccessible(true);
            return (int) field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getScoreP2() {
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("scoreP2");
            field.setAccessible(true);
            return (int) field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getGamesP1() {
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("gamesP1");
            field.setAccessible(true);
            return (int) field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getGamesP2() {
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("gamesP2");
            field.setAccessible(true);
            return (int) field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getCurrentSet() {
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("currentSet");
            field.setAccessible(true);
            return (int) field.get(manager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- Test per Costruttore e Metodi di Reset ---

    @Test
    public void testCostruttore_InizializzazioneCorretta() {
        assertEquals(0, getScoreP1());
        assertEquals(0, getScoreP2());
        assertEquals(0, getGamesP1());
        assertEquals(0, getGamesP2());
        assertEquals(1, getCurrentSet());
        // Controlla che gli array siano inizializzati a 0
        try {
            java.lang.reflect.Field setsP1Field = TennisScoreManager.class.getDeclaredField("setsP1");
            setsP1Field.setAccessible(true);
            int[] setsP1 = (int[]) setsP1Field.get(manager);
            assertEquals(0, setsP1[0]); // Il costruttore inizializza a 0
        } catch (Exception e) {
            fail("Errore nell'accesso ai campi per il test del costruttore");
        }
    }


    @Test
    public void testResetPoints_GameInProgress() {
        scorePoint(1); // 15-Love
        scorePoint(2); // 15-15
        manager.resetPoints();
        assertEquals(0, getScoreP1());
        assertEquals(0, getScoreP2());
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testResetGameAndPoints_GameFinished() {
        winGame(1); // 1-0 Games
        scorePoint(1); // 15-Love
        manager.resetGameAndPoints();
        assertEquals(0, getScoreP1());
        assertEquals(0, getScoreP2());
        assertEquals(0, getGamesP1());
        assertEquals(0, getGamesP2());
        // Controlla che isTieBreak sia false
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("isTieBreak");
            field.setAccessible(true);
            assertFalse((boolean) field.get(manager));
        } catch (Exception e) {
            fail("Errore nell'accesso al campo isTieBreak");
        }
    }

    // --- Test per pointScored e checkGamePoint (Punteggio Standard) ---

    @Test
    public void testPointScored_P1StandardPoints() {
        scorePoint(1); // 15
        assertEquals("15-Love", manager.getGameScore());
        scorePoint(1); // 30
        assertEquals("30-Love", manager.getGameScore());
        scorePoint(1); // 40
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testPointScored_P2StandardPoints() {
        scorePoint(2);
        scorePoint(2);
        assertEquals("Love-30", manager.getGameScore());
    }

    @Test
    public void testPointScored_InvalidPlayer() {
        manager.pointScored(3);
        String expectedOutput = "Errore: Giocatore non valido. Usa 1 o 2.\n";
        assertTrue(outContent.toString().contains(expectedOutput.trim()));
    }

    @Test
    public void testCheckGamePoint_P1WinsGame_4_0() {
        scorePoint(1); // 15-0
        scorePoint(1); // 30-0
        scorePoint(1); // 40-0
        scorePoint(1); // Game P1
        assertEquals(1, getGamesP1());
        assertEquals(0, getScoreP1()); // Punti resettati
    }

    @Test
    public void testCheckGamePoint_P2WinsGame_0_4() {
        for (int i = 0; i < 4; i++) {
            scorePoint(2);
        }
        assertEquals(1, getGamesP2());
        assertEquals(0, getScoreP2());
    }

    // --- Test per getGameScore (Deuce e Advantage) ---

    @Test
    public void testGetGameScore_Deuce() {
        scorePoint(1); // 15-0
        scorePoint(1); // 30-0
        scorePoint(1); // 40-0
        scorePoint(2); // 40-15
        scorePoint(2); // 40-30
        scorePoint(2); // Deuce (40-40, scoreP1=3, scoreP2=3)
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_AdvantageP1() {
        // Deuce
        scorePoint(1); scorePoint(1); scorePoint(1);
        scorePoint(2); scorePoint(2); scorePoint(2);
        // Advantage P1
        scorePoint(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        // Torna a Deuce
        scorePoint(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_AdvantageP2() {
        // Deuce
        scorePoint(1); scorePoint(1); scorePoint(1);
        scorePoint(2); scorePoint(2); scorePoint(2);
        // Advantage P2
        scorePoint(2);
        assertEquals("Vantaggio P2", manager.getGameScore());
        // Torna a Deuce
        scorePoint(1);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testCheckGamePoint_P1WinsFromDeuce() {
        // Deuce
        scorePoint(1); scorePoint(1); scorePoint(1);
        scorePoint(2); scorePoint(2); scorePoint(2);
        // Advantage P1
        scorePoint(1);
        // Game P1
        scorePoint(1);
        assertEquals(1, getGamesP1());
        assertEquals(0, getScoreP1());
    }

    @Test
    public void testGetGameScore_ErroreGameCase() {
        // Scenario che dovrebbe essere impossibile con la logica attuale, 
        // ma necessario per la copertura del 'return "Errore Game";'
        // Ad esempio, scoreP1=4, scoreP2=1 (non dovrebbe mai essere raggiunto senza Game)
        try {
            java.lang.reflect.Field scoreP1Field = TennisScoreManager.class.getDeclaredField("scoreP1");
            scoreP1Field.setAccessible(true);
            scoreP1Field.set(manager, 4);
            java.lang.reflect.Field scoreP2Field = TennisScoreManager.class.getDeclaredField("scoreP2");
            scoreP2Field.setAccessible(true);
            scoreP2Field.set(manager, 1);
            assertEquals("Errore Game", manager.getGameScore());
        } catch (Exception e) {
            fail("Impossibile testare 'Errore Game'");
        }
    }


    // --- Test per checkSetPoint (Set Standard e Tie-Break) ---

    @Test
    public void testCheckSetPoint_TieBreakActivation_6_6() {
        // Porta il punteggio a 6-6 in games
        winSet(1, 5); // 6-5 P1
        winGame(2); // 6-6
        
        try {
            java.lang.reflect.Field field = TennisScoreManager.class.getDeclaredField("isTieBreak");
            field.setAccessible(true);
            assertTrue((boolean) field.get(manager));
        } catch (Exception e) {
            fail("Errore nell'accesso al campo isTieBreak");
        }
        assertEquals(0, getScoreP1()); // Punti resettati
        assertTrue(outContent.toString().contains("*** INIZIO TIE-BREAK ***"));
    }

    @Test
    public void testCheckSetPoint_P1WinsSet_6_4() {
        winSet(1, 4); // 6-4 P1
        assertEquals(1, getCurrentSet()); // Dovrebbe essere 1 prima dell'aggiornamento
        // I test Game Point chiamano checkSetPoint e moveToNextSet

        assertEquals(2, getCurrentSet());
        assertEquals(0, getGamesP1()); // Games resettati
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (6-4) ***"));
    }

    @Test
    public void testCheckSetPoint_P2WinsSet_7_5() {
        winSet(1, 5); // 6-5 P1
        winGame(2); // 6-6 P1
        // Simula il risultato di P2 che vince due game di seguito
        // Simula lo stato 5-6 prima del punto vincente
        try {
            java.lang.reflect.Field gamesP1Field = TennisScoreManager.class.getDeclaredField("gamesP1");
            gamesP1Field.setAccessible(true);
            gamesP1Field.set(manager, 5);
            java.lang.reflect.Field gamesP2Field = TennisScoreManager.class.getDeclaredField("gamesP2");
            gamesP2Field.setAccessible(true);
            gamesP2Field.set(manager, 6);
        } catch (Exception e) {
            fail("Impossibile impostare i games per il test 7-5");
        }

        // P2 vince il Game finale (risulta 7-5 per P2)
        scorePoint(2); // 15-0
        scorePoint(2); // 30-0
        scorePoint(2); // 40-0
        scorePoint(2); // Game, Set P2 (7-5)

        assertEquals(2, getCurrentSet());
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (7-5) ***"));
    }

    @Test
    public void testCheckSetPoint_P2WinsSet_7_6() {
        // Stato di Tie-Break (non deve vincere il set direttamente)
        winSet(1, 5); // 6-5 P1
        winGame(2); // 6-6
        // Setta P1 6 games, P2 6 games e isTieBreak=true. Punti resettati.

        // Simula vittoria Tie-Break per P2 (es. 7-5 in punti)
        for (int i = 0; i < 5; i++) { // 5-5
            manager.pointScored(1);
            manager.pointScored(2);
        }
        manager.pointScored(2); // 5-6
        manager.pointScored(2); // 5-7, Game (P2 vince Set 7-6)

        assertEquals(2, getCurrentSet());
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (7-6) ***"));
        // I game del set dovrebbero essere 7-6
        try {
            java.lang.reflect.Field setsP1Field = TennisScoreManager.class.getDeclaredField("setsP1");
            setsP1Field.setAccessible(true);
            int[] setsP1 = (int[]) setsP1Field.get(manager);
            assertEquals(6, setsP1[0]); // P1 ha 6 games
            
            java.lang.reflect.Field setsP2Field = TennisScoreManager.class.getDeclaredField("setsP2");
            setsP2Field.setAccessible(true);
            int[] setsP2 = (int[]) setsP2Field.get(manager);
            assertEquals(7, setsP2[0]); // P2 ha 7 games
        } catch (Exception e) {
            fail("Errore nell'accesso ai games del set");
        }
    }

    // --- Test per Tie-Break ---

    @Test
    public void testCheckTieBreakPoint_P1Wins_7_5() {
        // Imposta lo stato di Tie-Break (6-6)
        winSet(1, 5);
        winGame(2);

        // Punti Tie-Break
        for (int i = 0; i < 5; i++) { // 5-5
            manager.pointScored(1);
            manager.pointScored(2);
        }
        manager.pointScored(1); // 6-5
        manager.pointScored(1); // 7-5, Game/Set

        // Controllo della vittoria del Set
        assertEquals(2, getCurrentSet());
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P1 (7-5) ***"));
    }
    
    @Test
    public void testCheckTieBreakPoint_P2Wins_Advantage() {
        // Imposta lo stato di Tie-Break (6-6)
        winSet(1, 5);
        winGame(2);

        // Punti Tie-Break
        for (int i = 0; i < 6; i++) { // 6-6
            manager.pointScored(1);
            manager.pointScored(2);
        }
        manager.pointScored(2); // 6-7 (Advantage P2)
        manager.pointScored(1); // 7-7 (Torna a 7-7)
        manager.pointScored(2); // 7-8 (Advantage P2)
        manager.pointScored(2); // 7-9, Game/Set

        // Controllo della vittoria del Set
        assertEquals(2, getCurrentSet());
        assertTrue(outContent.toString().contains("*** SET 1 Vinto da P2 (9-7) ***"));
    }

    @Test
    public void testGetTieBreakScore() {
        // Imposta lo stato di Tie-Break (6-6)
        winSet(1, 5);
        winGame(2);
        
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(2);
        
        assertEquals("TIE-BREAK: 2-1", manager.getTieBreakScore());
    }


    // --- Test per isGameOver e getMatchScore ---

    @Test
    public void testIsGameOver_P1WinsMatch_3_0() {
        winSet(1, 4); // Set 1 P1 (6-4)
        winSet(1, 4); // Set 2 P1 (6-4)
        winSet(1, 4); // Set 3 P1 (6-4)

        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 1! (3 Set a 0) ***"));
    }

    @Test
    public void testIsGameOver_P2WinsMatch_3_2() {
        winSet(1, 4); // Set 1 P1
        winSet(2, 4); // Set 2 P2
        winSet(1, 4); // Set 3 P1
        winSet(2, 4); // Set 4 P2
        assertFalse(manager.isGameOver()); // Non è finita (2-2)

        winSet(2, 4); // Set 5 P2 (3-2)

        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("*** PARTITA VINTA DAL GIOCATORE 2! (3 Set a 2) ***"));
    }

    @Test
    public void testPointScored_MatchIsOver() {
        // Fa terminare la partita
        winSet(1, 4); // 1-0
        winSet(1, 4); // 2-0
        winSet(1, 4); // 3-0

        // Tenta di segnare un punto dopo la fine
        outContent.reset(); // Pulisce l'output precedente
        manager.pointScored(1);

        // Verifica che abbia stampato il messaggio di partita finita
        assertTrue(outContent.toString().contains("La partita è finita! Punteggio finale: P1: 3 Set | P2: 0 Set"));
    }


    @Test
    public void testGetMatchScore_MatchInProgress_StandardGame() {
        winSet(1, 4); // Set 1 P1 (1-0)
        winSet(2, 4); // Set 2 P2 (1-1)
        winGame(1); // Game 1-0 Set 3

        scorePoint(1); // 15-Love

        String expected = "1-1 (Game: 1-0 15-Love)";
        assertEquals(expected, manager.getMatchScore());
    }

    @Test
    public void testGetMatchScore_MatchInProgress_TieBreak() {
        winSet(1, 5); // 6-5 P1
        winGame(2); // 6-6 -> Tie-Break (Set 1)

        manager.pointScored(1); // P1 1-0

        // Il punteggio è 0-0 (Game: 6-6 TIE-BREAK: 1-0)
        String expected = "0-0 (Game: 6-6 TIE-BREAK: 1-0)";
        assertEquals(expected, manager.getMatchScore());
    }
    
    @Test
    public void testGetMatchScore_MatchFinished() {
        winSet(1, 4); // 1-0
        winSet(1, 4); // 2-0
        winSet(1, 4); // 3-0

        String expected = "P1: 3 Set | P2: 0 Set";
        assertEquals(expected, manager.getMatchScore());
        // Controlla anche il ramo getGameScore quando la partita è finita
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    // --- Test per printScore ---

    @Test
    public void testPrintScore_Standard() {
        winSet(1, 4); // Set 1 P1 (6-4)
        winGame(2); // Game P2 (0-1) Set 2
        scorePoint(1); // 15-Love

        outContent.reset();
        manager.printScore();
        String output = outContent.toString();

        assertTrue(output.contains("Punteggio Set: P1 [6] - P2 [4]"));
        assertTrue(output.contains("Set Corrente (2): P1 0 Game | P2 1 Game"));
        assertTrue(output.contains("Punti Correnti: 15-Love"));
    }

    @Test
    public void testPrintScore_TieBreak() {
        // Imposta lo stato di Tie-Break (6-6)
        winSet(1, 5);
        winGame(2); // 6-6

        manager.pointScored(1); // P1 1-0

        outContent.reset();
        manager.printScore();
        String output = outContent.toString();

        // I games del set 1 non sono ancora finalizzati (sono 6-6)
        assertTrue(output.contains("Punteggio Set: P1 [] - P2 []")); 
        assertTrue(output.contains("Set Corrente (1): P1 6 Game | P2 6 Game"));
        assertTrue(output.contains("Punti Correnti: TIE-BREAK: 1-0"));
    }

    @Test
    public void testPrintScore_MultipleSets() {
        winSet(1, 4); // 1-0
        winSet(2, 4); // 1-1
        winGame(1); // 1-0 Set 3

        outContent.reset();
        manager.printScore();
        String output = outContent.toString();
        
        assertTrue(output.contains("Punteggio Set: P1 [6, 4] - P2 [4, 6]")); 
        assertTrue(output.contains("Set Corrente (3): P1 1 Game | P2 0 Game"));
    }
}