package chesspresso.pgn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;


public class JohnTest {
	
	@Test
	public void test() throws PGNSyntaxError, IOException {
		// testcase submitted by John Nahlen
		
		String pgn =
			"[Event \"FICS rated lightning game\"]" +
			"[Site \"FICS\"]" +
			"[FICSGamesDBGameNo \"240071715\"]" +
			"[White \"metalshredde\"]" +
			"[Black \"Anna\"]" +
			"[WhiteElo \"1745\"]" +
			"[BlackElo \"1627\"]" +
			"[TimeControl \"60+0\"]" +
			"[Date \"2010.01.01\"]" +
			"[Time \"23:02:00\"]" +
			"[WhiteClock \"0:01:00.000\"]" +
			"[BlackClock \"0:01:00.000\"]" +
			"[ECO \"C16\"]" +
			"[PlyCount \"47\"]" +
			"[Result \"1-0\"]" +
			  
			"1. e4 e6 2. d4 d5 3. Nc3 Bb4 4. e5 Bxc3+ 5. bxc3 c5 6. Qg4 c4 7. Qxg7 Ne7 8." +
			"Qxh8+ Kd7 9. Qxh7 Qb6 10. Qxf7 Qa5 11. Bd2 Nc6 12. Nf3 a6 13. Ng5 Kc7 14. Nxe6+ " +
			"Kb8 15. Nc5 b5 16. Be2 Ra7 17. O-O Nf5 18. Qf6 Qd8 19. Qxc6 Bb7 20. Nxb7 Rxb7 " +
			"21. Qxa6 Kc7 22. Rab1 Qb8 23. Bf4 Kd7 24. e6+ {Black resigns} 1-0";
		
		InputStream stream = new ByteArrayInputStream(pgn.getBytes());
		PGNReader rdr = new PGNReader(stream, "test");
		rdr.parseGame();
	}

}