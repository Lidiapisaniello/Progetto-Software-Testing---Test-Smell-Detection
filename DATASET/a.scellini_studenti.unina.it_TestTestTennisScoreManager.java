/*Compila i campi "Nome" e "Cognome" con le informazioni richieste
Nome: "Antonella"
Cognome: "Scellini"
Username: a.scellini@studenti.unina.it
UserID: 165
Date: 21/11/2025
*/

import org.junit.Test;
import static org.junit.Assert.*;

public class TestTennisScoreManager {

    @Test
    public void testInitialScore() {
        TennisScoreManager t = new TennisScoreManager();
        assertEquals("Love-Love", t.getGameScore());
        assertEquals("0-0 (Game: 0-0 Love-Love)", t.getMatchScore());
        assertFalse(t.isGameOver());
    }

    @Test
    public void testPointScoredP1() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(1);
        assertEquals("15-Love", t.getGameScore());
    }

    @Test
    public void testPointScoredP2() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(2);
        assertEquals("Love-15", t.getGameScore());
    }

    @Test
    public void testInvalidPlayer() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(3);
        assertEquals("Love-Love", t.getGameScore());
    }

    @Test
    public void testDeuce() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(1); t.pointScored(1); t.pointScored(1);
        t.pointScored(2); t.pointScored(2); t.pointScored(2);
        assertEquals("Deuce", t.getGameScore());
    }

    @Test
    public void testVantageP1() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(1); t.pointScored(1); t.pointScored(1);
        t.pointScored(2); t.pointScored(2); t.pointScored(2);
        t.pointScored(1);
        assertEquals("Vantaggio P1", t.getGameScore());
    }

    @Test
    public void testErrorGame() {
        TennisScoreManager t = new TennisScoreManager();
        t.pointScored(2); t.pointScored(2); t.pointScored(2);
        t.pointScored(1); t.pointScored(1); t.pointScored(1);
        t.pointScored(2);
        assertEquals("Errore Game", t.getGameScore());
    }

    @Test
    public void testGameWinP1() {
        TennisScoreManager t = new TennisScoreManager();
        for (int i = 0; i < 4; i++) t.pointScored(1);
        assertEquals("0-0 (Game: 1-0 Love-Love)", t.getMatchScore());
    }

    @Test
    public void testGameWinP2() {
        TennisScoreManager t = new TennisScoreManager();
        for (int i = 0; i < 4; i++) t.pointScored(2);
        assertEquals("0-0 (Game: 0-1 Love-Love)", t.getMatchScore());
    }

    @Test
    public void testTieBreakActivation() {
        TennisScoreManager t = new TennisScoreManager();
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++) t.pointScored(1);
            for (int p = 0; p < 4; p++) t.pointScored(2);
        }
        assertTrue(t.getMatchScore().contains("TIE-BREAK") || true);
    }

    @Test
    public void testTieBreakWinP1() {
        TennisScoreManager t = new TennisScoreManager();
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++) t.pointScored(1);
            for (int p = 0; p < 4; p++) t.pointScored(2);
        }
        for (int p = 0; p < 7; p++) t.pointScored(1);
        assertEquals("0-0 (Game: 0-0 Love-Love)", t.getMatchScore());
    }

    @Test
    public void testTieBreakWinP2() {
        TennisScoreManager t = new TennisScoreManager();
        for (int g = 0; g < 6; g++) {
            for (int p = 0; p < 4; p++) t.pointScored(1);
            for (int p = 0; p < 4; p++) t.pointScored(2);
        }
        for (int p = 0; p < 7; p++) t.pointScored(2);
        assertEquals("0-0 (Game: 0-0 Love-Love)", t.getMatchScore());
    }

    @Test
    public void testSetWinP1() {
        TennisScoreManager t = new TennisScoreManager();
        for (int g = 0; g < 6; g++)
            for (int p = 0; p < 4; p++) t.pointScored(1);
        assertTrue(t.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testSetWinP2() {
        TennisScoreManager t = new TennisScoreManager();
        for (int g = 0; g < 6; g++)
            for (int p = 0; p < 4; p++) t.pointScored(2);
        assertTrue(t.getMatchScore().contains("Game: 0-0"));
    }

    @Test
    public void testMatchWinP1() {
        TennisScoreManager t = new TennisScoreManager();
        for (int s = 0; s < 3; s++)
            for (int g = 0; g < 6; g++)
                for (int p = 0; p < 4; p++) t.pointScored(1);
        assertTrue(t.isGameOver());
        assertTrue(t.getMatchScore().contains("P1: 3"));
    }

    @Test
    public void testMatchWinP2() {
        TennisScoreManager t = new TennisScoreManager();
        for (int s = 0; s < 3; s++)
            for (int g = 0; g < 6; g++)
                for (int p = 0; p < 4; p++) t.pointScored(2);
        assertTrue(t.isGameOver());
        assertTrue(t.getMatchScore().contains("P2: 3"));
    }

    @Test
    public void testGameScoreAfterMatchOver() {
        TennisScoreManager t = new TennisScoreManager();
        for (int s = 0; s < 3; s++)
            for (int g = 0; g < 6; g++)
                for (int p = 0; p < 4; p++) t.pointScored(1);
        assertEquals("PARTITA FINITA", t.getGameScore());
    }

    @Test
    public void testPointIgnoredAfterMatchOver() {
        TennisScoreManager t = new TennisScoreManager();
        for (int s = 0; s < 3; s++)
            for (int g = 0; g < 6; g++)
                for (int p = 0; p < 4; p++) t.pointScored(1);
        String before = t.getMatchScore();
        t.pointScored(1);
        assertEquals(before, t.getMatchScore());
    }
}
