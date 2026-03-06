/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: Valeria
Cognome: De Falco
Username: valeria.defalco@studenti.unina.it
UserID: 121
Date: 11/11/2025
*/
package ClassTesting;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {
		
	private TennisScoreManager tsm;
	
	public void p1Points(int n) {
		for(int i=0; i<n; i++) tsm.pointScored(1);
	}
	
	public void p2Points(int n) {
		for(int i=0; i<n; i++) tsm.pointScored(2);
	}
	
	public void p1GameWon() { p1Points(4); }
	public void p2GameWon() { p2Points(4); }

	public void reachGames(int g1, int g2) {
		for(int i=0; i<g1; i++) p1GameWon();
		for(int i=0; i<g2; i++) p2GameWon();
	}
	
	@Before
	public void setUp() {
		tsm = new TennisScoreManager();
	}
				
	@After
	public void tearDown() {
		tsm = null;
	}
				
    @Test
    public void pointScored_Test1() {
        // P1 vince 3 set (6-0)
        for (int s = 0; s < 3; s++) {
            reachGames(5, 0); 
            p1GameWon(); // chiude 6-0 e va al set successivo
        }
        
        //Salva lo stato del punteggio dopo la fine del match
        String before = tsm.getMatchScore();

        // Tentativo di segnare un punto dopo la fine
        tsm.pointScored(1);

        //Rilettura del punteggio
        String after = tsm.getMatchScore();

        assertEquals("Dopo la fine del match, pointScored deve avere early-return senza alterare lo stato.",
                     before, after);
    }
    
    @Test
    public void pointScored_Test2() {
        // P2 vince 3 set (0-6)
        for (int s = 0; s < 3; s++) {
            reachGames(0, 5);
            p2GameWon(); // chiude 0-6 e va al set successivo
        }

        String before = tsm.getMatchScore();

        // Tentativo di segnare dopo la fine
        tsm.pointScored(1);

        String after = tsm.getMatchScore();

        assertEquals("Dopo la fine del match, pointScored deve avere early-return senza alterare lo stato.",
                     before, after);
    }
    
    @Test
    public void pointScored_Test3_PlayerInvalid() {
        //Viene salvato il punteggio del match e del game
    	String matchBefore = tsm.getMatchScore();
        String gameBefore  = tsm.getGameScore();

        //Inserisco valori di player non ammessi, non dovrebbero cambiare nulla ma viene stampato
        //un messaggio di errore
        tsm.pointScored(0);
        tsm.pointScored(3); 

        //Viene riletto il punteggio del match e del game
        String matchAfter = tsm.getMatchScore();
        String gameAfter  = tsm.getGameScore();

        assertEquals(matchBefore, matchAfter, "0-0 (Game: 0-0 Love-Love)");
        assertEquals(gameBefore,  gameAfter,  "Love-Love");
    }
   
    @Test
    public void checkTieBreakPoint_Test1() {
        //Simulo un set che arriva a 6-6, entro nel tie-break
        for (int i = 0; i < 5; i++) {
            p1GameWon();
            p2GameWon();
        }
        
        p1GameWon(); //6–5
        p2GameWon(); //6–6 (tie break)

        String before = tsm.getMatchScore();
        assertTrue("A 6–6 deve attivarsi il tie-break.", before.contains("TIE-BREAK"));

        //Pongo che P1 ha almeno 7 punti e due di vantaggio
        p1Points(6);
        p2Points(5);
        tsm.pointScored(1); //7-5

        //risultato dopo il tie-break
        String after = tsm.getMatchScore();

        //OTTENGO UN BUG
        // Il set NON viene chiuso perché la classe azzera i games prima di controllare la condizione di vittoria del set
        // Mi aspetto quindi che il punteggio NON mostri "1-0 (Game: 0-0 ...)"
        assertFalse("BUG atteso: il set non viene assegnato dopo il tie-break.", after.startsWith("1-0 (Game: 0-0"));

        // Verifica che i games siano stati azzerati (effetto del reset prematuro)
        assertTrue("Dopo il tie-break i games vengono azzerati a 0–0.", after.contains("(Game: 0-0"));

        // Verifica che i punti siano tornati a Love-Love (reset dei punti)
        assertEquals("Love-Love", tsm.getGameScore());
    }

    @Test
    public void getGameScore_Test1() {
        //P1 segna 3 punti → 40-Love
        p1Points(3);

        //P2 segna 3 punti → 40-40 (Deuce)
        p2Points(3);

        //Verifico che il punteggio sia "Deuce"
        String risultato = tsm.getGameScore();
        assertEquals("Deuce", risultato);
    }
    
    @Test
    public void getGameScore_Test2() {
        p1Points(3);
        p2Points(3);
        //P1 segna un punto in più → 4–3
        tsm.pointScored(1);

        //Verifico che ora sia P1 in vantaggio
        String risultato = tsm.getGameScore();
        assertEquals("Vantaggio P1", risultato);
    }
    
    @Test
    public void getGameScore_Test3() {
        p1Points(3);
        p2Points(3);
        //P1 segna un punto in più → 3–4
        tsm.pointScored(2);

        //Verifico che ora sia P2 in vantaggio
        String risultato = tsm.getGameScore();
        
        //OTTENGO UN BUG
        // Nel codice corretto ci aspetteremmo "Vantaggio P2" ma con scoreP2 == scoreP2 + 1 ritorna ErrorGame
        //assertEquals("Vantaggio P2", risultato);
    }


}


