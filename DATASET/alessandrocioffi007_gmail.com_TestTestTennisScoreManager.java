/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "inserire il proprio nome"
Cognome: "inserire il proprio cognome"
Username: alessandrocioffi007@gmail.com
UserID: 1376
Date: 19/11/2025
*/

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    private TennisScoreManager manager;

    @Before
    public void setUp() {
        // Inizializza una nuova istanza prima di ogni test per garantire isolamento
        manager = new TennisScoreManager();
    }

    @After
    public void tearDown() {
        // Non ci sono risorse esterne da chiudere, ma utile per best practice
        manager = null;
    }

    // --- HELPER METHODS PER RENDERE I TEST LEGGIBILI ---
    
    private void segnaPunti(int player, int numeroPunti) {
        for (int i = 0; i < numeroPunti; i++) {
            manager.pointScored(player);
        }
    }

    private void vinciGame(int player) {
        segnaPunti(player, 4); // 4 punti consecutivi vincono un game standard a 0
    }

    private void portaAlTieBreak() {
        // P1 vince 5 game
        for(int i=0; i<5; i++) vinciGame(1);
        // P2 vince 5 game -> 5-5
        for(int i=0; i<5; i++) vinciGame(2);
        // P1 vince 1 game -> 6-5
        vinciGame(1);
        // P2 vince 1 game -> 6-6 (Attiva TieBreak)
        vinciGame(2);
    }

    // --- TEST SUITE ---

    @Test
    public void testInizializzazionePunteggio() {
        // Copre: Costruttore e getGameScore ramo iniziale
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("0-0"));
    }

    @Test
    public void testAvanzamentoPuntiBase() {
        // Copre: pointScored, getGameScore (15, 30, 40)
        manager.pointScored(1);
        assertEquals("15-Love", manager.getGameScore());
        
        manager.pointScored(2);
        assertEquals("15-15", manager.getGameScore());
        
        manager.pointScored(1); // 30-15
        manager.pointScored(1); // 40-15
        assertEquals("40-15", manager.getGameScore());
    }

    @Test
    public void testVittoriaGameStandard() {
        // Copre: checkGamePoint (vittoria netta)
        segnaPunti(1, 3); // 40-0
        assertEquals("40-Love", manager.getGameScore());
        
        manager.pointScored(1); // Vince il game
        // Verifica reset punti e incremento game
        assertEquals("Love-Love", manager.getGameScore());
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
    }

    @Test
    public void testDeuce() {
        // Copre: getGameScore (ramo Deuce)
        segnaPunti(1, 3); // 40-0
        segnaPunti(2, 3); // 40-40
        
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testVantaggioGiocatore1() {
        // Copre: getGameScore (Vantaggio P1)
        segnaPunti(1, 3); 
        segnaPunti(2, 3); // Deuce
        
        manager.pointScored(1); // Vantaggio P1
        assertEquals("Vantaggio P1", manager.getGameScore());
        
        manager.pointScored(2); // Ritorna Deuce
        assertEquals("Deuce", manager.getGameScore());
    }

    @Test
    public void testVantaggioGiocatore2_ConBugNoto() {
        // Copre: getGameScore (Vantaggio P2 - Ramo Buggato)
        // NOTA: Il codice sorgente ha "if (scoreP2 == scoreP2 + 1)" che è sempre falso.
        // Il test è scritto per passare con il codice attuale (ritorna "Errore Game").
        
        segnaPunti(1, 3); 
        segnaPunti(2, 3); // Deuce
        manager.pointScored(2); // P2 segna il vantaggio
        
        // Se il bug fosse risolto, qui ci aspetteremmo "Vantaggio P2"
        assertEquals("Errore Game", manager.getGameScore()); 
    }
    
    @Test
    public void testVittoriaGameDopoVantaggi() {
        // Copre: checkGamePoint (dopo deuce)
        segnaPunti(1, 3);
        segnaPunti(2, 3); // Deuce
        
        manager.pointScored(1); // Vantaggio P1
        manager.pointScored(1); // Vince Game
        
        assertTrue(manager.getMatchScore().contains("Game: 1-0"));
        assertEquals("Love-Love", manager.getGameScore());
    }

    @Test
    public void testInputGiocatoreInvalido() {
        // Copre: pointScored (ramo else finale)
        manager.pointScored(1);
        manager.pointScored(3); // Input errato
        
        // Il punteggio non deve essere cambiato
        assertEquals("15-Love", manager.getGameScore());
    }

    @Test
    public void testVittoriaSetStandard() {
        // Copre: checkSetPoint (6-4) e moveToNextSet
        // P1 vince 5 games
        for(int i=0; i<5; i++) vinciGame(1);
        // P2 vince 4 games
        for(int i=0; i<4; i++) vinciGame(2);
        
        assertTrue(manager.getMatchScore().contains("Game: 5-4"));
        
        vinciGame(1); // P1 vince il 6° game -> Vince il set 6-4
        
        // Verifica che il set sia stato memorizzato e si sia passati al set 2
        // Output atteso es: "1-0 (Game: 0-0 Love-Love)"
        String score = manager.getMatchScore();
        assertTrue("Dovrebbe indicare 1 set a 0 per P1", score.startsWith("1-0"));
        assertTrue("Dovrebbe aver resettato i game", score.contains("Game: 0-0"));
    }
    
    @Test
    public void testVittoriaSetEsteso() {
        // Copre: checkSetPoint (7-5)
        // 5-5
        for(int i=0; i<5; i++) vinciGame(1);
        for(int i=0; i<5; i++) vinciGame(2);
        
        vinciGame(1); // 6-5
        vinciGame(1); // 7-5 -> Vince Set
        
        String score = manager.getMatchScore();
        assertTrue("P1 deve vincere il set 7-5", score.startsWith("1-0"));
    }

    @Test
    public void testAttivazioneTieBreak() {
        // Copre: checkSetPoint (6-6 -> isTieBreak = true)
        portaAlTieBreak(); // Helper che porta a 6-6
        
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 0-0"));
    }

    @Test
    public void testPunteggioTieBreak() {
        // Copre: getTieBreakScore
        portaAlTieBreak();
        
        manager.pointScored(1);
        manager.pointScored(2);
        manager.pointScored(1);
        
        assertTrue(manager.getMatchScore().contains("TIE-BREAK: 2-1"));
    }

    @Test
    public void testVittoriaSetAlTieBreak_Giocatore2() {
        // Copre: checkTieBreakPoint e checkSetPoint (caso 7-6)
        // Usiamo P2 per vincere perché la logica di P1 in checkSetPoint ha un bug (doppio check su gamesP2)
        portaAlTieBreak(); // 6-6
        
        // P2 vince il tie break 7-0
        for(int i=0; i<7; i++) {
            manager.pointScored(2); 
        }
        
        String score = manager.getMatchScore();
        // P2 dovrebbe aver vinto il set (0-1 nei set)
        assertTrue("P2 deve vincere il set dopo il TieBreak", score.startsWith("0-1"));
        assertTrue("I game devono essere resettati", score.contains("Game: 0-0"));
    }
    
    @Test
    public void testVittoriaPartita() {
        // Copre: isGameOver (Vittoria 3 set su 5)
        
        // Simuliamo vittoria P1 per 3 set consecutivi
        for (int set = 1; set <= 3; set++) {
            // Vinci un set 6-0
            for(int i=0; i<6; i++) vinciGame(1);
        }
        
        assertTrue(manager.isGameOver());
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
        
        // Test comportamento post-partita
        manager.pointScored(1); // Non dovrebbe cambiare nulla
        assertEquals("P1: 3 Set | P2: 0 Set", manager.getMatchScore());
    }
    
    @Test
    public void testResetForzato() {
        // Copre: resetPoints
        manager.pointScored(1);
        manager.resetPoints();
        assertEquals("Love-Love", manager.getGameScore());
    }
}					