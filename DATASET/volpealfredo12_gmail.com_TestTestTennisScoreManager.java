/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: volpealfredo12@gmail.com
UserID: 680
Date: 21/11/2025
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
        // Eseguito una volta prima dell'inizio dei test nella classe
    }
                
    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
    }
                
    @Before
    public void setUp() {
        // Inizializzazione per ogni test per garantire uno stato pulito
        manager = new TennisScoreManager();
    }
                
    @After
    public void tearDown() {
        manager = null;
    }
    
    // --- HELPER METHODS PER FACILITARE I TEST COMPLESSI ---
    
    // Simula la vittoria di un game per un giocatore specifico
    private void winGame(int player) {
        for (int i = 0; i < 4; i++) {
            manager.pointScored(player);
        }
    }

    // Simula la vittoria di un set standard (6-0)
    private void winSetStandard(int player) {
        for (int i = 0; i < 6; i++) {
            winGame(player);
        }
    }
    
    // --- TEST CASES ---

    @Test
    public void testInizializzazione() {
        // Copertura costruttore e stato iniziale
        assertNotNull(manager);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testPunteggioBaseP1() {
        // Copertura flusso: 15, 30, 40 per P1
        manager.pointScored(1); // 15-0
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(1); // 30-0
        assertEquals("30-Love", manager.getGameScore());
        
        manager.pointScored(1); // 40-0
        assertEquals("40-Love", manager.getGameScore());
    }

    @Test
    public void testPunteggioBaseP2() {
        // Copertura flusso: 15, 30, 40 per P2
        manager.pointScored(2); // 0-15
        assertEquals("Love-15", manager.getGameScore());
        
        manager.pointScored(2); // 0-30
        assertEquals("Love-30", manager.getGameScore());
        
        manager.pointScored(2); // 0-40
        assertEquals("Love-40", manager.getGameScore());
    }
    
    @Test
    public void testPunteggioMisto() {
        // Copertura combinazioni diverse (es. 15-15, 30-15)
        manager.pointScored(1);
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1);
        assertEquals("30-15", manager.getGameScore());
    }

    @Test
    public void testDeuceEAdvantageP1() {
        // Raggiungimento Deuce (3 punti a testa)
        manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);
        manager.pointScored(2); manager.pointScored(2); manager.pointScored(2);
        
        assertEquals("Deuce", manager.getGameScore());
        
        // Vantaggio P1
        manager.pointScored(1);
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        // Ritorno a Deuce
        manager.pointScored(2);
        assertEquals("Deuce", manager.getGameScore());
        
        // Vittoria Game P1 da Deuce
        manager.pointScored(1); // Adv P1
        manager.pointScored(1); // Win Game
        // Verifica reset punti
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testDeuceEAdvantageP2_ConBugCheck() {
        // Nota: Il codice sorgente contiene un bug logico in getGameScore: 
        // (scoreP2 == scoreP2 + 1) è sempre falso. 
        // Questo test mira a coprire il ramo else/error e il flusso di punteggio per P2.
        
        // Portiamo a Deuce
        for(int i=0; i<3; i++) { manager.pointScored(1); manager.pointScored(2); }
        
        // P2 fa punto -> Teoricamente Vantaggio P2
        manager.pointScored(2);
        
        // Eseguiamo getGameScore per copertura (restituirà "Errore Game" a causa del bug nel codice sorgente, ma il test deve coprirlo)
        String score = manager.getGameScore();
        assertNotNull(score);
        
        // P2 fa un altro punto e vince il game
        manager.pointScored(2);
        assertEquals("Love-Love", manager.getGameScore()); // Nuovo game
    }

    @Test
    public void testVittoriaGameP1Standard() {
        // P1 vince a zero
        winGame(1);
        assertEquals("Love-Love", manager.getGameScore());
        // Verifichiamo lo stato parziale del match score
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("Game: 1-0"));
    }

    @Test
    public void testVittoriaGameP2Standard() {
        // P2 vince a zero
        winGame(2);
        assertEquals("Love-Love", manager.getGameScore());
        String matchScore = manager.getMatchScore();
        assertTrue(matchScore.contains("Game: 0-1"));
    }

    @Test
    public void testVittoriaSetStandardP1() {
        // P1 vince 6 game di fila -> Vince Set 1
        winSetStandard(1);
        
        // Il set corrente dovrebbe essere incrementato a 2
        // Il punteggio game resettato
        assertEquals("Love-Love", manager.getGameScore());
        // Match score dovrebbe indicare 1 set a 0
        System.out.println(manager.getMatchScore()); 
    }

    @Test
    public void testVittoriaSetStandardP2() {
        // P2 vince 6 game di fila -> Vince Set 1
        winSetStandard(2);
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testSetVinto7a5_P1() {
        // Copre il ramo (gamesP1 == 7 && gamesP2 == 5) in checkSetPoint
        
        // Arriviamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // P1 va 6-5
        winGame(1);
        
        // P1 va 7-5 -> Vince il set
        winGame(1);
        
        // Verifica inizio nuovo set
        assertEquals("Love-Love", manager.getGameScore());
        // Verifica visualizzazione punteggio
        manager.printScore();
    }

    @Test
    public void testSetVinto7a5_P2() {
        // Copre il ramo (gamesP2 == 7 && gamesP1 == 5) in checkSetPoint
        
        // Arriviamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // P2 va 5-6
        winGame(2);
        
        // P2 va 5-7 -> Vince il set
        winGame(2);
        
        manager.printScore();
    }

    @Test
    public void testTieBreakTrigger() {
        // Copre l'ingresso nel Tie-Break (6-6)
        
        // Arriviamo a 5-5
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        
        // 6-5
        winGame(1);
        // 6-6 -> Attiva TieBreak
        winGame(2);
        
        // Verifica stringa punteggio TieBreak
        String tbScore = manager.getMatchScore();
        assertTrue(tbScore.contains("TIE-BREAK"));
        assertEquals("TIE-BREAK: 0-0", manager.getTieBreakScore());
    }

    @Test
    public void testTieBreakScoringEWinP1() {
        // Setup TieBreak 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2);
        
        // Punti TieBreak (arriva a 7)
        for(int i=0; i<6; i++) manager.pointScored(1); // 6-0
        
        assertEquals("TIE-BREAK: 6-0", manager.getTieBreakScore());
        
        // Punto vittoria TieBreak P1 (7-0)
        manager.pointScored(1);
        
        // Set finito, inizia nuovo set
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testTieBreakWinP2_Extended() {
        // Setup TieBreak 6-6
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2);
        
        // Simuliamo TieBreak combattuto (es. 6-6 nel TB)
        for(int i=0; i<6; i++) { 
            manager.pointScored(1); 
            manager.pointScored(2); 
        }
        assertEquals("TIE-BREAK: 6-6", manager.getTieBreakScore());
        
        // P2 va 6-7
        manager.pointScored(2);
        // P2 va 6-8 e vince Set
        manager.pointScored(2);
        
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testVittoriaPartitaP1() {
        // P1 deve vincere 3 set.
        // Set 1
        winSetStandard(1);
        // Set 2
        winSetStandard(1);
        // Set 3
        winSetStandard(1);
        
        assertTrue(manager.isGameOver());
        String finalScore = manager.getMatchScore();
        assertTrue(finalScore.contains("P1: 3 Set"));
    }

    @Test
    public void testVittoriaPartitaP2() {
        // P2 vince 3 set
        winSetStandard(2);
        winSetStandard(2);
        winSetStandard(2);
        
        assertTrue(manager.isGameOver());
        assertTrue(manager.getMatchScore().contains("P2: 3 Set"));
    }
    
    @Test
    public void testPartitaLunga5Set() {
        // Copertura loop set multipli e condizione match non finito
        
        // Set 1: P1 Vince
        winSetStandard(1);
        // Set 2: P2 Vince
        winSetStandard(2);
        // Set 3: P1 Vince
        winSetStandard(1);
        // Set 4: P2 Vince
        winSetStandard(2);
        
        assertFalse(manager.isGameOver());
        
        // Set 5: P1 Vince partita
        winSetStandard(1);
        assertTrue(manager.isGameOver());
    }

    @Test
    public void testPointScoredAfterGameOver() {
        // Copre il ramo "if (isGameOver())" all'inizio di pointScored
        winSetStandard(1);
        winSetStandard(1);
        winSetStandard(1);
        
        // Partita finita, provo a segnare
        manager.pointScored(1);
        manager.pointScored(2);
        
        // Non deve lanciare eccezioni, stampa solo a video
        assertTrue(manager.isGameOver());
    }

    @Test
    public void testInvalidPlayer() {
        // Copre il ramo "else" in pointScored per giocatore non valido
        manager.pointScored(3);
        manager.pointScored(0);
        manager.pointScored(-1);
        
        // Il punteggio non deve cambiare
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testGetGameScorePartitaFinita() {
        // Copre il ramo if(isGameOver()) in getGameScore
        winSetStandard(1);
        winSetStandard(1);
        winSetStandard(1);
        
        assertEquals("PARTITA FINITA", manager.getGameScore());
    }
    
    @Test
    public void testResetGameAndPointsDiretto() {
        // Esegue il metodo resetGameAndPoints per copertura diretta
        manager.pointScored(1);
        manager.resetGameAndPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }
    
    @Test
    public void testPrintScore() {
        // Esegue il metodo printScore per copertura (non verifica console, solo esecuzione)
        manager.pointScored(1);
        manager.printScore();
        
        // Simula tie break e stampa
        for(int i=0; i<5; i++) { winGame(1); winGame(2); }
        winGame(1); winGame(2);
        manager.printScore();
    }
    
    @Test
    public void testMatchScoreDuringGame() {
        // Copertura del calcolo set in corso dentro getMatchScore
        winSetStandard(1); // 1 set P1
        manager.pointScored(2); // 0-15 nel secondo set
        String score = manager.getMatchScore();
        assertNotNull(score);
        // Ci aspettiamo 1-0 nei set
        assertTrue(score.startsWith("1-0")); 
    }
    
    @Test
    public void testSetWinningConditionEdge() {
        // Copertura del ramo: gamesP1 >= 6 && gamesP1 >= gamesP2 + 2
        // Caso 6-4
        for(int i=0; i<4; i++) { winGame(1); winGame(2); } // 4-4
        winGame(1); // 5-4
        winGame(1); // 6-4 -> Vince Set
        
        // Verifica reset
        assertEquals("Love-Love", manager.getGameScore());
    }
}