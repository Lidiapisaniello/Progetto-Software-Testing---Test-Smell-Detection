/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: g.lasalvia@studenti.unina.it
UserID: 1155
Date: 24/11/2025
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
        // Eseguito una volta prima dell'inizio dei test
        System.out.println("Inizio suite di test per TennisScoreManager");
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test
        System.out.println("Fine suite di test per TennisScoreManager");
    }

    @Before
    public void setUp() {
        // Inizializzazione fresca prima di ogni test
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        // Pulizia dopo ogni test
        manager = null;
    }

    // --- Helper Methods per facilitare i test ---
    
    private void scorePoints(int player, int times) {
        for (int i = 0; i < times; i++) {
            manager.pointScored(player);
        }
    }

    private void winStandardGame(int player) {
        scorePoints(player, 4);
    }

    private void reachDeuce() {
        scorePoints(1, 3); // 40
        scorePoints(2, 3); // 40
    }

    private void reachSixAllInSet() {
        // P1 vince 5 game
        for(int i=0; i<5; i++) winStandardGame(1);
        // P2 vince 6 game (0-5 -> 6-5)
        for(int i=0; i<6; i++) winStandardGame(2);
        // P1 vince 1 game (6-5 -> 6-6)
        winStandardGame(1);
    }

    // --- Test Cases ---

    @Test
    public void initializationTest() {
        // Verifica lo stato iniziale: 0-0, Set 1
        String score = manager.getGameScore();
        assertEquals("Love-Love", score);
        assertTrue(manager.getMatchScore().contains("0-0"));
    }

    @Test
    public void pointScoredSimpleP1Test() {
        // Copertura: Punti standard
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(1);
        assertEquals("30-Love", manager.getGameScore());
        
        manager.pointScored(1);
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void pointScoredSimpleP2Test() {
        // Copertura: Punti standard avversario
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void pointScoredInvalidPlayerTest() {
        // Copertura: Ramo else finale di pointScored (Input non valido)
        manager.pointScored(3);
        manager.pointScored(0);
        manager.pointScored(-1);
        // Il punteggio non deve cambiare
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void getGameScoreDeuceTest() {
        // Copertura: Logica Deuce (3-3)
        reachDeuce();
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP1Test() {
        // Copertura: Vantaggio P1
        reachDeuce();
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void getGameScoreAdvantageP2Test() {
        // Copertura: Bug nel codice sorgente (scoreP2 == scoreP2 + 1 è impossibile)
        // Il codice cade nel return "Errore Game" finale
        reachDeuce();
        manager.pointScored(2);
        
        // Ci aspettiamo "Errore Game" perché la condizione if per Vantaggio P2 è errata nel codice sorgente
        assertEquals("Errore Game", manager.getGameScore());
    }

    @Test
    public void returnToDeuceFromAdvantageTest() {
        // Scenario: P1 Vantaggio -> P2 segna -> Deuce
        reachDeuce();
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(2); // Torna pari (4-4 interni, ma logica >=3 e pari -> Deuce)
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void winStandardGameP1Test() {
        // Copertura: checkGamePoint (vittoria game)
        manager.pointScored(1); // 15
        manager.pointScored(1); // 30
        manager.pointScored(1); // 40
        manager.pointScored(1); // Win Game
        
        // Verifica che il punteggio punti sia resettato
        assertEquals("Love-Love", manager.getGameScore());
        // Verifica match score aggiornato
        String matchScore = manager.getMatchScore();
        assertTrue("Dovrebbe contenere 1-0 nei game", matchScore.contains("Game: 1-0"));
    }
    
    @Test
    public void winStandardGameP2Test() {
        // Copertura: checkGamePoint ramo else if (P2 vince)
        winStandardGame(2);
        assertTrue("Dovrebbe contenere 0-1 nei game", manager.getMatchScore().contains("Game: 0-1"));
    }

    @Test
    public void winSetStandardP1Test() {
        // Copertura: checkSetPoint (vittoria set 6-0)
        for (int i = 0; i < 6; i++) {
            winStandardGame(1);
        }
        // Set 1 vinto, deve passare al Set 2. Game resettati.
        String matchScore = manager.getMatchScore();
        // Poiché setsWonP1 viene calcolato su setsP1[i] > setsP2[i]
        assertTrue("P1 dovrebbe avere 1 set", matchScore.startsWith("1-0"));
        assertTrue("Game dovrebbero essere 0-0 nel nuovo set", matchScore.contains("Game: 0-0"));
    }

    @Test
    public void winSetStandardP2Test() {
        // Copertura: checkSetPoint (vittoria set 0-6)
        for (int i = 0; i < 6; i++) {
            winStandardGame(2);
        }
        assertTrue("P2 dovrebbe avere 1 set", manager.getMatchScore().startsWith("0-1"));
    }
    
    @Test
    public void winSetExtendedTest() {
        // Copertura: Vittoria 7-5
        // 5-5
        for(int i=0; i<5; i++) { winStandardGame(1); winStandardGame(2); }
        // 6-5
        winStandardGame(1);
        // Verifica che non abbia ancora vinto il set
        assertTrue(manager.getMatchScore().contains("Game: 6-5"));
        // 7-5 (Vittoria)
        winStandardGame(1);
        assertTrue("P1 dovrebbe aver vinto il set", manager.getMatchScore().startsWith("1-0"));
    }

    @Test
    public void triggerTieBreakTest() {
        // Copertura: Entrata in TieBreak (6-6)
        reachSixAllInSet();
        
        // Verifica che siamo in tie break guardando l'output del punteggio
        String score = manager.getMatchScore();
        assertTrue("Dovrebbe mostrare TIE-BREAK", score.contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }

    @Test
    public void scoreTieBreakPointsTest() {
        // Copertura: Punteggio dentro il tie break
        reachSixAllInSet();
        
        manager.pointScored(1);
        assertEquals("TIE-BREAK: 1-0", manager.getTieBreakScore());
        
        manager.pointScored(2);
        manager.pointScored(2);
        assertEquals("TIE-BREAK: 1-2", manager.getTieBreakScore());
    }

    @Test
    public void tieBreakWinLogicBugTest() {
        // Copertura: checkTieBreakPoint e il relativo BUG
        reachSixAllInSet(); // Siamo 6-6
        
        // P1 vince il tie break (7-0)
        scorePoints(1, 7);
        
        // A CAUSA DEL BUG in checkTieBreakPoint (resetGameAndPoints chiamato prima di checkSetPoint),
        // i game vengono azzerati (diventano 0-0) e checkSetPoint vede 0-0, quindi non assegna il set.
        
        // Verifichiamo il comportamento attuale (buggato) per soddisfare il requisito "Nessun test deve fallire"
        String matchScore = manager.getMatchScore();
        
        // Se funzionasse, sarebbe 1-0 nei set. Visto che non funziona:
        assertTrue("A causa del bug, il set count resta 0-0", matchScore.startsWith("0-0"));
        // I game vengono resettati
        assertTrue("I game vengono resettati a 0-0", matchScore.contains("Game: 0-0"));
    }

    @Test
    public void winMatchP1Test() {
        // Copertura: isGameOver e vittoria partita
        // P1 deve vincere 3 set.
        // Usiamo la vittoria standard 6-0 perché il tie-break è buggato
        
        // Set 1
        for(int g=0; g<6; g++) winStandardGame(1);
        // Set 2
        for(int g=0; g<6; g++) winStandardGame(1);
        // Set 3
        for(int g=0; g<6; g++) winStandardGame(1);
        
        assertTrue("Il gioco deve essere finito", manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }

    @Test
    public void winMatchP2Test() {
        // Copertura: isGameOver per P2
        // Set 1, 2, 3 vinti da P2
        for(int s=0; s<3; s++) {
            for(int g=0; g<6; g++) winStandardGame(2);
        }
        
        assertTrue("Il gioco deve essere finito", manager.isGameOver());
        assertEquals("P1: 0 Set | P2: 3 Set", manager.getMatchScore());
    }

    @Test
    public void pointScoredAfterGameOverTest() {
        // Copertura: Primo if di pointScored (isGameOver)
        // Vinciamo la partita
        for(int s=0; s<3; s++) {
            for(int g=0; g<6; g++) winStandardGame(1);
        }
        
        // Proviamo a segnare ancora
        manager.pointScored(1);
        
        // Il punteggio finale non deve cambiare
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void manualResetsTest() {
        // Copertura: resetPoints e resetGameAndPoints chiamati direttamente
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
        
        winStandardGame(1); // 1-0 games
        manager.resetGameAndPoints();
        assertTrue(manager.getMatchScore().contains("Game: 0-0"));
    }
}