import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        manager = new TennisScoreManager();
        // Catturiamo lo System.out per verificare i messaggi stampati (es. vittoria partita)
        System.setOut(new PrintStream(outContent));
    }

    // --- Helper Methods con Reflection per ispezionare lo stato privato ---
    // Questo è cruciale per la Weak Mutation: verifichiamo che le variabili interne cambino correttamente
    // anche se l'output pubblico (getGameScore) potrebbe essere buggato.
    
    private int getField(String fieldName) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (int) field.get(manager);
    }

    private int[] getArrayField(String fieldName) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (int[]) field.get(manager);
    }

    private boolean getBooleanField(String fieldName) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (boolean) field.get(manager);
    }

    // Helper per simulare punti veloci
    private void playPoints(int player, int points) {
        for (int i = 0; i < points; i++) {
            manager.pointScored(player);
        }
    }

    // --- TEST DI UNITÀ ---

    @Test
    public void testInitialState() throws Exception {
        assertEquals("Love-Love", manager.getGameScore());
        assertEquals(0, getField("scoreP1"));
        assertEquals(0, getField("scoreP2"));
        assertEquals(1, getField("currentSet"));
    }

    @Test
    public void testPointIncrementP1() throws Exception {
        manager.pointScored(1); // 15-0
        assertEquals("15-Love", manager.getGameScore());
        assertEquals(1, getField("scoreP1"));
        
        manager.pointScored(1); // 30-0
        assertEquals("30-Love", manager.getGameScore());
        
        manager.pointScored(1); // 40-0
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testPointIncrementP2() throws Exception {
        manager.pointScored(2); // 0-15
        assertEquals("Love-15", manager.getGameScore());
        assertEquals(1, getField("scoreP2"));
    }

    @Test
    public void testInvalidPlayer() {
        // Test input non valido (ne 1 ne 2)
        manager.pointScored(3);
        // Verifichiamo che l'errore sia stampato
        assertTrue(outContent.toString().contains("Errore: Giocatore non valido"));
        // Il punteggio non deve cambiare
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testDeuceLogic() {
        playPoints(1, 3); // P1: 40
        playPoints(2, 3); // P2: 40
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP1() {
        playPoints(1, 3); // 40
        playPoints(2, 3); // 40 -> Deuce
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // Torna a Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testAdvantageP2_BugRevelation() {
        // Questo test copre il percorso dell'Advantage P2.
        // A causa del BUG nel codice (scoreP2 == scoreP2 + 1), non otterremo "Vantaggio P2",
        // ma "Errore Game". Testiamo che il codice non esploda e ritorni il fallback.
        playPoints(1, 3); // 40
        playPoints(2, 3); // 40
        manager.pointScored(2); // Dovrebbe essere Vantaggio P2
        
        // Asserzione basata sul comportamento ATTUALE del codice (buggato)
        assertEquals("Errore Game", manager.getGameScore()); 
    }

    @Test
    public void testGameWinP1() throws Exception {
        playPoints(1, 3); // 40-0
        manager.pointScored(1); // Game P1
        
        assertEquals(1, getField("gamesP1"));
        assertEquals(0, getField("scoreP1")); // Punti resettati
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testGameWinP2() throws Exception {
        playPoints(2, 3); // 0-40
        manager.pointScored(2); // Game P2
        
        assertEquals(1, getField("gamesP2"));
        assertEquals(0, getField("scoreP2"));
    }

    @Test
    public void testSetWinStandard_6_4() throws Exception {
        // Portiamo P1 a 5-4
        for(int i=0; i<5; i++) { playPoints(1, 4); } // P1 5 games
        for(int i=0; i<4; i++) { playPoints(2, 4); } // P2 4 games
        
        assertEquals(5, getField("gamesP1"));
        assertEquals(4, getField("gamesP2"));
        
        // P1 vince il 6° game
        playPoints(1, 4);
        
        // Verifica che il set sia stato assegnato
        int[] setsP1 = getArrayField("setsP1");
        assertEquals(6, setsP1[0]);
        
        // Verifica passaggio al set successivo
        assertEquals(2, getField("currentSet"));
        assertTrue(outContent.toString().contains("Vinto da P1"));
    }

    @Test
    public void testSetWinExtended_7_5() throws Exception {
        // Scenario 5-5
        for(int i=0; i<5; i++) { playPoints(1, 4); }
        for(int i=0; i<5; i++) { playPoints(2, 4); }
        
        // Scenario 6-5 P2
        playPoints(2, 4); 
        
        // Scenario 7-5 P2 (Vittoria Set)
        playPoints(2, 4);
        
        int[] setsP2 = getArrayField("setsP2");
        assertEquals(7, setsP2[0]);
        assertEquals(2, getField("currentSet"));
    }

    @Test
    public void testTieBreakTrigger() throws Exception {
        // Arriviamo a 6-6
        for(int i=0; i<5; i++) { playPoints(1, 4); }
        for(int i=0; i<5; i++) { playPoints(2, 4); }
        playPoints(1, 4); // 6-5
        playPoints(2, 4); // 6-6 -> Tie Break triggers here
        
        assertTrue(getBooleanField("isTieBreak"));
        assertTrue(outContent.toString().contains("INIZIO TIE-BREAK"));
        assertTrue(manager.getMatchScore().contains("TIE-BREAK"));
    }

    @Test
    public void testTieBreakScoring() throws Exception {
        // Forza stato TieBreak
        testTieBreakTrigger(); 
        outContent.reset(); // Pulisci buffer
        
        manager.pointScored(1);
        assertTrue(manager.getTieBreakScore().contains("1-0"));
        
        manager.pointScored(2);
        assertTrue(manager.getTieBreakScore().contains("1-1"));
    }

    @Test
    public void testTieBreakWinLogic_BuggedPath() throws Exception {
        // Questo test esercita la logica di vittoria del TieBreak.
        // NOTA: A causa del bug in checkTieBreakPoint() che chiama resetGameAndPoints()
        // PRIMA di checkSetPoint(), il set non verrà mai chiuso correttamente (i game tornano a 0).
        // Testiamo il flusso di esecuzione per garantire la copertura, non la correttezza logica.
        
        testTieBreakTrigger(); // Siamo 6-6 in TB
        
        // P1 vince il TB 7-0
        playPoints(1, 7); 
        
        // A causa del bug, currentSet non avanza e gamesP1 è resettato a 0 invece di segnare il set
        // Se il codice fosse corretto, qui ci aspetteremmo currentSet = 2.
        // Ma per passare il test col codice fornito:
        assertEquals(0, getField("gamesP1")); 
        assertEquals(1, getField("currentSet")); // Il set non è avanzato
    }

    @Test
    public void testMatchWinP1() throws Exception {
        // P1 deve vincere 3 set.
        // Usiamo set standard 6-0 per velocità ed evitare i bug del TieBreak
        
        // Set 1
        for(int i=0; i<6; i++) playPoints(1, 4);
        assertEquals(2, getField("currentSet"));
        
        // Set 2
        for(int i=0; i<6; i++) playPoints(1, 4);
        assertEquals(3, getField("currentSet"));
        
        // Set 3
        for(int i=0; i<6; i++) playPoints(1, 4);
        
        // Verifica fine partita
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("PARTITA VINTA DAL GIOCATORE 1"));
        
        // Verifica output match score finale
        String finalScore = manager.getMatchScore();
        assertTrue(finalScore.contains("P1: 3 Set"));
    }

    @Test
    public void testMatchWinP2() throws Exception {
        // Simile a sopra ma per P2
        for(int s=0; s<3; s++) {
            for(int i=0; i<6; i++) playPoints(2, 4);
        }
        assertTrue(manager.isGameOver());
        assertTrue(outContent.toString().contains("PARTITA VINTA DAL GIOCATORE 2"));
    }
    
    @Test
    public void testPlayAfterGameOver() throws Exception {
        testMatchWinP1(); // Partita finita
        outContent.reset();
        
        // Tentativo di segnare un punto a partita finita
        int scoreBefore = getField("scoreP1");
        manager.pointScored(1);
        int scoreAfter = getField("scoreP1");
        
        assertEquals(scoreBefore, scoreAfter); // Punteggio immutato
        assertTrue(outContent.toString().contains("La partita è finita"));
    }
    
    @Test
    public void testGetGameScore_GameErrorFallback() throws Exception {
        // Questo test mira a coprire l'ultimo "return Error Game" in getGameScore
        // È difficile da raggiungere logicamente se non con lo stato inconsistente del vantaggio P2.
        // Manipoliamo lo stato interno via Reflection per creare una situazione assurda
        // che non sia Deuce, non sia <4, ma non sia vantaggio standard.
        
        Field p1 = manager.getClass().getDeclaredField("scoreP1");
        p1.setAccessible(true);
        p1.setInt(manager, 5);
        
        Field p2 = manager.getClass().getDeclaredField("scoreP2");
        p2.setAccessible(true);
        p2.setInt(manager, 8); // Differenza > 1 ma logica game point non scattata (simulazione)
        
        // In una esecuzione normale checkGamePoint avrebbe resettato, ma chiamando direttamente getGameScore:
        assertEquals("Errore Game", manager.getGameScore());
    }
    
    @Test
    public void testCheckSetPoint_ImpossibleConditionBranches() throws Exception {
         // Copertura dei rami else if in checkSetPoint per P2
         // Settiamo manualmente i game per testare il ramo 7-5 per P2
         
         Field g1 = manager.getClass().getDeclaredField("gamesP1");
         g1.setAccessible(true);
         g1.setInt(manager, 5);
         
         Field g2 = manager.getClass().getDeclaredField("gamesP2");
         g2.setAccessible(true);
         g2.setInt(manager, 7);
         
         manager.checkSetPoint();
         
         // Verifica che P2 abbia vinto il set
         int[] setsP2 = getArrayField("setsP2");
         assertEquals(7, setsP2[0]);
         assertTrue(outContent.toString().contains("Vinto da P2"));
    }
    
    @Test
    public void testMoveToNextSetBlockedIfGameOver() throws Exception {
        // Simula match finito
        testMatchWinP1();
        int setBefore = getField("currentSet");
        
        manager.moveToNextSet();
        
        int setAfter = getField("currentSet");
        assertEquals(setBefore, setAfter);
    }
    
    @Test
    public void testCheckTieBreakPoint_P2Win_Bugged() throws Exception {
        // Copertura ramo else if in checkTieBreakPoint (P2 vince TB)
        testTieBreakTrigger(); // 6-6
        
        playPoints(2, 7); // P2 vince TB
        
        // Come per P1, il bug resetta tutto a 0. Verifichiamo solo che non esploda.
        assertEquals(0, getField("gamesP2"));
    }
    
    @Test
    public void testGetMatchScore_Calculations() throws Exception {
        // Manipola setsP1 e setsP2 per verificare il conteggio dei set vinti in getMatchScore
        int[] s1 = getArrayField("setsP1");
        int[] s2 = getArrayField("setsP2");
        
        s1[0] = 6; s2[0] = 4; // P1 vince set 1
        s1[1] = 2; s2[1] = 6; // P2 vince set 2
        
        // Avanza current set a 3
        Field cs = manager.getClass().getDeclaredField("currentSet");
        cs.setAccessible(true);
        cs.setInt(manager, 3);
        
        String score = manager.getMatchScore();
        // Deve mostrare 1-1 nei set
        assertTrue(score.startsWith("1-1"));
    }
}
