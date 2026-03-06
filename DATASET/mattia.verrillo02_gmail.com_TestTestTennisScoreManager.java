/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Mattia"
Cognome: "Verrillo"
Username: mattia.verrillo02@gmail.com
UserID: 807
Date: 21/11/2025
*/

/*
 * ANALISI COMPLESSITÀ CICLOMATICA (McCabe)
 *
 * 1. pointScored(int player):
 * - Predicati: isGameOver, player==1, player==2, isTieBreak.
 * - Complessità: 5
 *
 * 2. checkGamePoint():
 * - Predicati: (P1>=4 && diff>=2), (P2>=4 && diff>=2).
 * - Complessità: 5
 *
 * 3. getGameScore():
 * - Contiene molteplici condizioni composte (&&, ||).
 * - Complessità stimata: 13
 *
 * 4. checkTieBreakPoint():
 * - Predicati: (P1>=7 && diff>=2), (P2>=7 && diff>=2).
 * - Complessità: 5
 *
 * 5. checkSetPoint():
 * - Condizioni molto complesse per determinare la vittoria del set o il tie-break.
 * - Complessità stimata: ~15
 *
 * 6. isGameOver():
 * - Loop for + due if finali.
 * - Complessità: 4
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
        System.out.println("Inizio esecuzione test suite TestTennisScoreManager");
    }

    @AfterClass
    public static void tearDownClass() {
        // Eseguito una volta alla fine di tutti i test nella classe
        System.out.println("Fine esecuzione test suite TestTennisScoreManager");
    }

    @Before
    public void setUp() {
        // Eseguito prima di ogni metodo di test
        // Inizializzazione di una nuova istanza per garantire indipendenza tra i test
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        // Eseguito dopo ogni metodo di test
        manager = null;
    }

    // --- TEST per pointScored (Driver principale) ---

    @Test
    public void metodo_pointScored_Test_1() {
        // Cammino: Player 1 segna un punto standard (0 -> 15)
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void metodo_pointScored_Test_2() {
        // Cammino: Player 2 segna un punto standard (0 -> 15)
        manager.pointScored(2);
        assertEquals("Love-15", manager.getGameScore());
    }

    @Test
    public void metodo_pointScored_Test_3() {
        // Cammino: Input giocatore non valido (Else finale)
        // Il punteggio non deve cambiare
        manager.pointScored(3);
        assertEquals("Love-Love", manager.getGameScore());
    }

    // --- TEST per getGameScore (Copertura stati punteggio) ---

    @Test
    public void metodo_getGameScore_Test_1() {
        // Cammino: Punteggio standard intermedio (30-15)
        manager.pointScored(1); // 15
        manager.pointScored(1); // 30
        manager.pointScored(2); // 15 P2
        assertEquals("30-15", manager.getGameScore());
    }

    @Test
    public void metodo_getGameScore_Test_2() {
        // Cammino: Deuce (40-40 -> Deuce)
        // P1: 3 punti, P2: 3 punti
        for(int i=0; i<3; i++) manager.pointScored(1);
        for(int i=0; i<3; i++) manager.pointScored(2);
        
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void metodo_getGameScore_Test_3() {
        // Cammino: Vantaggio P1
        // Arriviamo a Deuce
        for(int i=0; i<3; i++) manager.pointScored(1);
        for(int i=0; i<3; i++) manager.pointScored(2);
        // P1 segna ancora
        manager.pointScored(1);
        
        assertEquals("Vantaggio P1", manager.getGameScore());
    }

    @Test
    public void metodo_getGameScore_Test_4() {
        // Cammino: Vantaggio P2
        // NOTA: Nel codice sorgente fornito c'è un bug logico: (scoreP2 == scoreP2 + 1).
        // Questo rende impossibile entrare nell'if "Vantaggio P2".
        // Il codice cade nel return "Errore Game".
        // Testiamo che non crashi e restituisca il valore di fallback.
        
        for(int i=0; i<3; i++) manager.pointScored(1); // 40
        for(int i=0; i<3; i++) manager.pointScored(2); // 40 (Deuce)
        manager.pointScored(2); // P2 segna, dovrebbe essere Adv P2
        
        // Assert adattato al comportamento attuale del codice (Bug compliance)
        assertEquals("Errore Game", manager.getGameScore());
    }

    // --- TEST per checkGamePoint (Vittoria Game) ---

    @Test
    public void metodo_checkGamePoint_Test_1() {
        // Cammino: P1 vince un game a zero (4 punti consecutivi)
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1);
        manager.pointScored(1); // Vince il game
        
        // Verifichiamo resettando i punti e controllando il match score
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("1-0"));
    }

    // --- TEST per checkSetPoint (Vittoria Set) ---

    @Test
    public void metodo_checkSetPoint_Test_1() {
        // Cammino: P1 vince il set 6-0
        // Simuliamo la vittoria di 6 game consecutivi per P1
        for (int g = 0; g < 6; g++) {
            manager.pointScored(1);
            manager.pointScored(1);
            manager.pointScored(1);
            manager.pointScored(1);
        }
        
        // Il set corrente dovrebbe essere avanzato a 2
        // Il punteggio del set precedente (P1 vince 1 set)
        String score = manager.getMatchScore();
        assertTrue(score.contains("P1 [6] - P2 [0]"));
        assertTrue(score.contains("Set Corrente (2)"));
    }

    @Test
    public void metodo_checkSetPoint_Test_2() {
        // Cammino: Tie Break Trigger (6-6)
        // Portiamo il punteggio a 5-5
        for(int i=0; i<5; i++) {
            // P1 vince game
            manager.pointScored(1); manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);
            // P2 vince game
            manager.pointScored(2); manager.pointScored(2); manager.pointScored(2); manager.pointScored(2);
        }
        // Ora 5-5.
        // P1 vince game -> 6-5
        manager.pointScored(1); manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);
        // P2 vince game -> 6-6 -> TieBreak scatta
        manager.pointScored(2); manager.pointScored(2); manager.pointScored(2); manager.pointScored(2);
        
        String output = manager.getMatchScore();
        assertTrue(output.contains("TIE-BREAK"));
    }

    // --- TEST per checkTieBreakPoint ---

    @Test
    public void metodo_checkTieBreakPoint_Test_1() {
        // Setup: Portare a Tie Break (6-6)
        // Simulazione rapida 6-6
        for(int i=0; i<6; i++) {
             // P1 wins game
             manager.pointScored(1); manager.pointScored(1); manager.pointScored(1); manager.pointScored(1);
             // P2 wins game
             manager.pointScored(2); manager.pointScored(2); manager.pointScored(2); manager.pointScored(2);
        }
        
        // Ora siamo nel tie break. P1 deve fare 7 punti per vincere il set
        for(int k=0; k<7; k++) {
            manager.pointScored(1);
        }
        
        // P1 vince il set 7-6. Si passa al set 2.
        String score = manager.getMatchScore();
        assertTrue(score.contains("P1 [7] - P2 [6]"));
        assertTrue(score.contains("Set Corrente (2)"));
    }

    // --- TEST per isGameOver ---

    @Test
    public void metodo_isGameOver_Test_1() {
        // Cammino: P1 vince 3 set consecutivi (Match Over)
        // 3 set * 6 game * 4 punti = 72 iterazioni minime
        
        for (int set = 0; set < 3; set++) {
            for (int game = 0; game < 6; game++) {
                manager.pointScored(1);
                manager.pointScored(1);
                manager.pointScored(1);
                manager.pointScored(1);
            }
        }
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
}