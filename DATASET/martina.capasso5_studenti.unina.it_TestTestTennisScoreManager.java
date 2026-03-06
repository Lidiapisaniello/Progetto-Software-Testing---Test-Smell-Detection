/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Martina"
Cognome: "Capasso"
Username: martina.capasso5@studenti.unina.it
UserID: 135
Date: 23/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

// Import necessari per la Reflection e per testare System.out
import java.lang.reflect.Field;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Suite di test JUnit 4 per la classe TennisScoreManager.
 * Questi test sono progettati per massimizzare la coverage e passare
 * data l'implementazione attuale, inclusi i suoi bug noti.
 * * AGGIORNAMENTO: Include test per i rami 'isGameOver' e test di 
 * 'printScore' più stringenti per la mutation coverage.
 */
public class TestTennisScoreManager {

    private TennisScoreManager manager;

    // Stream per catturare l'output di System.out
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;

    // ========================================================================
    // SETUP E TEARDOWN
    // ========================================================================

    @BeforeClass
    public static void setUpClass() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterClass
    public static void tearDownClass() {
        System.setOut(originalOut);
    }

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        outContent.reset();
    }

    @After
    public void tearDown() {
        manager = null;
    }

    // ========================================================================
    // TEST (MANTENUTI COME PRIMA)
    // ========================================================================

    @Test
    public void testInitialStateAndConstructor() throws Exception {
        assertNotNull("L'istanza non deve essere null", manager);
        assertEquals("scoreP1 iniziale deve essere 0", 0, getPrivateField(manager, "scoreP1"));
        assertEquals("scoreP2 iniziale deve essere 0", 0, getPrivateField(manager, "scoreP2"));
        assertEquals("currentSet iniziale deve essere 1", 1, getPrivateField(manager, "currentSet"));
        assertEquals("isTieBreak iniziale deve essere false", false, getPrivateField(manager, "isTieBreak"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        assertEquals("setsP1[0] iniziale deve essere 0", 0, setsP1[0]);
        assertEquals("Punteggio iniziale deve essere Love-Love", "Love-Love", manager.getGameScore());
        assertFalse("La partita non deve essere finita all'inizio", manager.isGameOver());
    }

    @Test
    public void testResetPoints() throws Exception {
        setPrivateField(manager, "scoreP1", 3);
        setPrivateField(manager, "scoreP2", 2);
        manager.resetPoints();
        assertEquals("scoreP1 dopo reset deve essere 0", 0, getPrivateField(manager, "scoreP1"));
        assertEquals("scoreP2 dopo reset deve essere 0", 0, getPrivateField(manager, "scoreP2"));
    }

    @Test
    public void testResetGameAndPoints() throws Exception {
        setPrivateField(manager, "gamesP1", 5);
        setPrivateField(manager, "scoreP1", 1);
        setPrivateField(manager, "isTieBreak", true);
        manager.resetGameAndPoints();
        assertEquals("gamesP1 dopo reset deve essere 0", 0, getPrivateField(manager, "gamesP1"));
        assertEquals("scoreP1 dopo reset deve essere 0", 0, getPrivateField(manager, "scoreP1"));
        assertEquals("isTieBreak dopo reset deve essere false", false, getPrivateField(manager, "isTieBreak"));
    }

    @Test
    public void testPointScored_StandardGame() {
        manager.pointScored(1); // 15-0
        assertEquals("15-Love", manager.getGameScore());
        manager.pointScored(2); // 15-15
        assertEquals("15-15", manager.getGameScore());
        manager.pointScored(1); // 30-15
        manager.pointScored(1); // 40-15
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_DeuceAndAdvantageP1() {
        winPoints(manager, 1, 3); // 40-0
        winPoints(manager, 2, 3); // 40-40 (Deuce)
        assertEquals("Deuce", manager.getGameScore());
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        manager.pointScored(2); // Ritorno a Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_Bug_AdvantageP2() {
        winPoints(manager, 1, 3); // 40-0
        winPoints(manager, 2, 3); // 40-40 (Deuce)
        manager.pointScored(2); // Punteggio interno 3-4 (dovrebbe essere Adv P2)
        assertEquals("Test del bug: Vantaggio P2 ritorna Errore Game", "Errore Game", manager.getGameScore());
    }
    
    @Test
    public void testWinGame_P1_From_40_15() throws Exception {
        winPoints(manager, 1, 3); // 40-0
        winPoints(manager, 2, 1); // 40-15
        manager.pointScored(1); // Game P1
        assertEquals("gamesP1 deve essere 1", 1, getPrivateField(manager, "gamesP1"));
        assertEquals("scoreP1 deve essere 0", 0, getPrivateField(manager, "scoreP1"));
    }

    @Test
    public void testWinGame_P2_FromDeuce() throws Exception {
        winPoints(manager, 1, 3); // Deuce
        winPoints(manager, 2, 3);
        manager.pointScored(2); // Adv P2 (ma il getter è buggato)
        manager.pointScored(2); // Game P2
        assertEquals("gamesP2 deve essere 1", 1, getPrivateField(manager, "gamesP2"));
    }

    @Test
    public void testWinSet_P1_6_0() throws Exception {
        winGames(manager, 1, 6); // P1 vince 6 game
        assertEquals("currentSet deve essere 2", 2, getPrivateField(manager, "currentSet"));
        assertEquals("gamesP1 deve essere 0", 0, getPrivateField(manager, "gamesP1"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        assertEquals("Set 1 P1 deve essere 6", 6, setsP1[0]);
    }

    @Test
    public void testWinSet_P2_7_5() throws Exception {
        winGames(manager, 1, 5);
        winGames(manager, 2, 5);
        winGames(manager, 2, 1); // 5-6
        winGames(manager, 2, 1); // 5-7, Set P2
        assertEquals("currentSet deve essere 2", 2, getPrivateField(manager, "currentSet"));
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals("Set 1 P2 deve essere 7", 7, setsP2[0]);
    }

    @Test
    public void testTieBreak_Trigger() throws Exception {
        winGames(manager, 1, 5);
        winGames(manager, 2, 5); // 5-5
        winGames(manager, 1, 1); // 6-5
        winGames(manager, 2, 1); // 6-6
        assertEquals("isTieBreak deve essere true", true, getPrivateField(manager, "isTieBreak"));
        assertEquals("scoreP1 deve essere 0", 0, getPrivateField(manager, "scoreP1"));
        assertTrue("Deve stampare l'inizio del tie-break", outContent.toString().contains("*** INIZIO TIE-BREAK ***"));
    }

    @Test
    public void testTieBreak_Win_P1_7_3() throws Exception {
        winGames(manager, 1, 5);
        winGames(manager, 2, 5); // 5-5
        winGames(manager, 1, 1); // 6-5
        winGames(manager, 2, 1); // 6-6
        assertTrue("isTieBreak deve essere true", (Boolean) getPrivateField(manager, "isTieBreak"));
        winPoints(manager, 1, 6); // 6-0
        winPoints(manager, 2, 3); // 6-3
        manager.pointScored(1); // 7-3, P1 vince tiebreak
        
        // ASSERT SUL BUG:
        assertEquals("BUG: currentSet non incrementa", 1, getPrivateField(manager, "currentSet"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        assertEquals("BUG: Set 1 P1 non salvato", 0, setsP1[0]); 
        assertEquals("isTieBreak deve essere false dopo tiebreak", false, getPrivateField(manager, "isTieBreak"));
    }

    @Test
    public void testTieBreak_Win_Extended_P2_9_7() throws Exception {
        winGames(manager, 1, 5);
        winGames(manager, 2, 5); // 5-5
        winGames(manager, 1, 1); // 6-5
        winGames(manager, 2, 1); // 6-6
        winPoints(manager, 1, 7); // 7-0
        winPoints(manager, 2, 7); // 7-7
        manager.pointScored(2); // 7-8
        assertEquals("Il set non deve finire sul 7-8", 1, (int) getPrivateField(manager, "currentSet"));
        manager.pointScored(2); // 7-9, P2 vince tiebreak
        
        // ASSERT SUL BUG:
        assertEquals("BUG: currentSet non incrementa", 1, getPrivateField(manager, "currentSet"));
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        assertEquals("BUG: Set 1 P2 non salvato", 0, setsP2[0]);
    }

    @Test
    public void testIsGameOver_P2_WinsMatch() throws Exception {
        winGames(manager, 1, 6); // Sets: 1-0. currentSet=2
        winGames(manager, 1, 1);
        winGames(manager, 2, 6); // Sets: 1-1. currentSet=3
        winGames(manager, 1, 2);
        winGames(manager, 2, 6); // Sets: 1-2. currentSet=4
        winGames(manager, 1, 5);
        winGames(manager, 2, 5); // 5-5
        winGames(manager, 2, 2); // 5-7. P2 vince. Sets: 1-3. currentSet=5

        assertTrue("La partita deve essere finita", manager.isGameOver());
        assertTrue("Deve stampare la vittoria di P2", outContent.toString().contains("PARTITA VINTA DAL GIOCATORE 2"));
        assertEquals("P1: 1 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void testPointScored_InvalidPlayer() throws Exception {
        manager.pointScored(3);
        assertTrue("Deve stampare 'Giocatore non valido'", outContent.toString().contains("Errore: Giocatore non valido"));
        assertEquals("scoreP1 non deve cambiare", 0, getPrivateField(manager, "scoreP1"));
    }

    @Test
    public void testGetMatchScore_InProgress_TieBreak() {
        winGames(manager, 1, 4);
        winGames(manager, 2, 6); // Sets: 0-1. currentSet=2.
        winGames(manager, 1, 5);
        winGames(manager, 2, 5); // 5-5
        winGames(manager, 1, 1); // 6-5
        winGames(manager, 2, 1); // 6-6 -> Tiebreak.
        winPoints(manager, 1, 4);
        winPoints(manager, 2, 5); // TB score: 4-5
        String expected = "0-1 (Game: 6-6 TIE-BREAK: 4-5)";
        assertEquals(expected, manager.getMatchScore());
    }

    // ========================================================================
    // TEST CORRETTI / MODIFICATI / NUOVI
    // ========================================================================
    
    /**
     * SOSTITUITO: Test reso più stringente (asserzioni esatte)
     * per uccidere i mutanti nei loop di stampa.
     */
    @Test
    public void testPrintScore() throws Exception {
        setPrivateField(manager, "currentSet", 2);
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        int[] setsP2 = (int[]) getPrivateField(manager, "setsP2");
        setsP1[0] = 6;
        setsP2[0] = 3;
        setPrivateField(manager, "gamesP1", 1);
        setPrivateField(manager, "gamesP2", 2);
        setPrivateField(manager, "scoreP1", 2); // 30
        setPrivateField(manager, "scoreP2", 1); // 15
        
        outContent.reset();
        manager.printScore();
        
        String output = outContent.toString().replace("\r\n", "\n").trim();

        // Usa asserzioni esatte per uccidere i mutanti
        String[] lines = output.split("\n");
        
        // Pulisce le linee da spazi extra per un confronto robusto
        String line1 = lines[0].trim();
        String line2 = lines[1].trim();
        String line3 = lines[2].trim();
        String line4 = lines[3].trim();
        String line5 = lines[4].trim();
        
        assertEquals("------------------------------------------", line1);
        // Il loop è 'i < currentSet - 1' (i < 1), quindi stampa solo i=0
        assertEquals("Punteggio Set: P1 [6] - P2 [3]", line2);
        assertEquals("Set Corrente (2): P1 1 Game | P2 2 Game", line3);
        assertEquals("Punti Correnti: 30-15", line4);
        assertEquals("------------------------------------------", line5);
    }

    @Test
    public void testPointScored_AfterGameOver() throws Exception {
        // Simula la vittoria di P2 3-0 in set
        winGames(manager, 2, 6); // Set 1: 0-6. Sets: 0-1
        winGames(manager, 2, 6); // Set 2: 0-6. Sets: 0-2
        winGames(manager, 2, 6); // Set 3: 0-6. Sets: 0-3
        
        assertTrue("La partita deve essere finita (0-3 set)", manager.isGameOver());
        
        setPrivateField(manager, "gamesP1", 1); // Imposta uno stato
        
        outContent.reset(); // Pulisce l'output
        manager.pointScored(1); // Tenta di segnare un punto
        
        assertTrue("Deve stampare 'La partita è finita'", outContent.toString().contains("La partita è finita!"));
        assertEquals("I game non devono cambiare", 1, getPrivateField(manager, "gamesP1"));
    }

    @Test
    public void testGetMatchScore_InProgress() {
        // Set 1: P1 vince 6-2
        winGames(manager, 1, 5);
        winGames(manager, 2, 2); // 5-2
        winGames(manager, 1, 1); // 6-2. Set P1. currentSet=2. games=0-0.

        // Set 2: 2-5 (Game), 40-15 (Punti)
        winGames(manager, 1, 2); // games=2-0
        winGames(manager, 2, 5); // games=2-5
        winPoints(manager, 1, 3); // 40-0
        winPoints(manager, 2, 1); // 40-15
        
        // ASSERT CORRETTA:
        String expected = "1-0 (Game: 2-5 40-15)";
        assertEquals(expected, manager.getMatchScore());
    }

    @Test
    public void testGetGameScore_Deuce_At_4_4() {
        // Va a 3-3 (Deuce)
        winPoints(manager, 1, 3);
        winPoints(manager, 2, 3);
        assertEquals("Deuce", manager.getGameScore());
        
        // Va a 4-3 (Adv P1)
        manager.pointScored(1);
        
        // Va a 4-4 (Deuce)
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testGetGameScore_AdvantageP1_At_5_4() {
        // Raggiungi 3-3 (Deuce)
        winPoints(manager, 1, 3);
        winPoints(manager, 2, 3);
        assertEquals("Deuce", manager.getGameScore());

        // Raggiungi 4-4 (Deuce)
        manager.pointScored(1); // Punteggio 4-3 (Adv P1)
        manager.pointScored(2); // Punteggio 4-4 (Deuce)
        assertEquals("Deuce", manager.getGameScore());

        // Raggiungi 5-4 (Adv P1)
        manager.pointScored(1); // Punteggio 5-4 (Adv P1)
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void testTieBreak_Win_P1_Boundary_7_5() throws Exception {
        // Va al tie-break (6-6)
        winGames(manager, 1, 5);
        winGames(manager, 2, 5);
        winGames(manager, 1, 1);
        winGames(manager, 2, 1);
        
        // Punteggio: 6-5
        winPoints(manager, 1, 6);
        winPoints(manager, 2, 5);
        
        // P1 vince 7-5
        manager.pointScored(1);
        
        // Verifica il BUG (il set non avanza)
        assertEquals("BUG: currentSet non incrementa", 1, getPrivateField(manager, "currentSet"));
        assertEquals("isTieBreak deve essere false dopo tiebreak", false, getPrivateField(manager, "isTieBreak"));
    }

    @Test
    public void testTieBreak_Win_P2_Boundary_7_5() throws Exception {
        // Va al tie-break (6-6)
        winGames(manager, 1, 5);
        winGames(manager, 2, 5);
        winGames(manager, 1, 1);
        winGames(manager, 2, 1);
        
        // Punteggio: 5-6
        winPoints(manager, 1, 5);
        winPoints(manager, 2, 6);
        
        // P2 vince 5-7
        manager.pointScored(2);
        
        // Verifica il BUG (il set non avanza)
        assertEquals("BUG: currentSet non incrementa", 1, getPrivateField(manager, "currentSet"));
        assertEquals("isTieBreak deve essere false dopo tiebreak", false, getPrivateField(manager, "isTieBreak"));
    }


    @Test
    public void testIsGameOver_P1_WinsMatch_3_0() {
        // P1 vince 3 set a 0
        winGames(manager, 1, 6); // Set 1: 6-0. Sets: 1-0
        winGames(manager, 1, 6); // Set 2: 6-0. Sets: 2-0
        winGames(manager, 1, 6); // Set 3: 6-0. Sets: 3-0
        
        assertTrue("La partita deve essere finita (3-0 set)", manager.isGameOver());
        assertTrue("Deve stampare la vittoria di P1", outContent.toString().contains("PARTITA VINTA DAL GIOCATORE 1"));
    }
 
    @Test
    public void testWinSet_P1_7_5() throws Exception {
        // Simula 5-5
        winGames(manager, 1, 5);
        winGames(manager, 2, 5);
        
        // P1 vince 6-5
        winGames(manager, 1, 1);
        assertEquals("Il set non deve finire sul 6-5", 1, getPrivateField(manager, "currentSet"));
        assertEquals("gamesP1 deve essere 6", 6, getPrivateField(manager, "gamesP1"));

        // P1 vince 7-5
        winGames(manager, 1, 1);
        
        // Il set deve avanzare
        assertEquals("currentSet deve essere 2", 2, getPrivateField(manager, "currentSet"));
        int[] setsP1 = (int[]) getPrivateField(manager, "setsP1");
        assertEquals("Set 1 P1 deve essere 7", 7, setsP1[0]);
    }

    // ========================================================================
    // TEST NUOVI PER COPRIRE RAMI MANCANTI
    // ========================================================================

    /**
     * NUOVO: Copre il ramo if (isGameOver()) in getGameScore().
     * Questo ramo è raggiungibile ma spesso non testato.
     */
    @Test
    public void testGetGameScore_WhenGameOver() {
        // Forza la fine della partita
        winGames(manager, 1, 6); // Set 1
        winGames(manager, 1, 6); // Set 2
        winGames(manager, 1, 6); // Set 3
        
        assertTrue("La partita deve essere finita", manager.isGameOver());
        // Testa il ramo specifico
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }

    /**
     * NUOVO: Copre il ramo isGameOver() in printScore() e verifica che
     * l'output sia corretto, usando 'PARTITA FINITA' come punteggio game.
     */
    @Test
    public void testPrintScore_WhenGameOver() throws Exception {
        // Simula una partita completa vinta da P1 (3 set a 1)
        winGames(manager, 1, 6); // Set 1: 6-0. Sets: 1-0. currentSet=2
        winGames(manager, 2, 6); // Set 2: 0-6. Sets: 1-1. currentSet=3
        
        // Simula 7-5 nel set 3
        winGames(manager, 1, 5);
        winGames(manager, 2, 5);
        winGames(manager, 1, 2); // Set 3: 7-5. Sets: 2-1. currentSet=4
        
        winGames(manager, 1, 6); // Set 4: 6-0. Sets: 3-1. currentSet=5
        
        // Ora la partita è finita
        assertTrue(manager.isGameOver());
        
        outContent.reset();
        manager.printScore();
        
        String output = outContent.toString().replace("\r\n", "\n");

        // printScore stampa i set completati (currentSet-1 = 4 set)
        
        // Verifica il comportamento (buggato) della classe:
        // Stampa i set completati (0, 1, 2, 3)
        assertTrue("Output deve contenere i 4 set completati", 
                   output.contains("Punteggio Set: P1 [6, 0, 7, 6] - P2 [0, 6, 5, 0]"));
        
        // E il ramo in printScore() che chiama getGameScore() prenderà "PARTITA FINITA"
        assertTrue("Output deve contenere PARTITA FINITA", 
                   output.contains("Punti Correnti: PARTITA FINITA"));
    }

    // ========================================================================
    // METODI HELPER
    // ========================================================================

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private void winPoints(TennisScoreManager m, int player, int count) {
        for (int i = 0; i < count; i++) {
            m.pointScored(player);
        }
    }

    private void winGames(TennisScoreManager m, int player, int count) {
        for (int i = 0; i < count; i++) {
            // Resetta l'output tra una vittoria e l'altra per non 
            // intasare la console e per isolare gli assert sui game
            outContent.reset(); 
            winPoints(m, player, 4);
        }
    }
}