/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id$
 */
package chesspresso.game;

import chesspresso.pgn.*;
import ch.seybold.util.*;

import java.io.*;
import java.util.zip.*;
import junit.framework.*;

/**
 * 
 * 
 * @author  Bernhard Seybold
 * @version $Revision$
 *
 */
public class GameTests extends FootprintTestCase
{

	public static Test suite()
	{
		return new TestSuite(GameTests.class);
	}
    
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}

	//======================================================================
    
	public void testInsertGame() throws Exception
	{
		String pgnFilename = "chesspresso/pgn/chusa99.pgn.gz";
		InputStream is = new GZIPInputStream(ClassLoader.getSystemResourceAsStream(pgnFilename));
		PGNReader pgnReader = new PGNReader(is, "InserTest");
		PGNWriter pgnWriter = new PGNWriter(getFootprint());		
		startFootprint("chesspresso/pgn/InsertGameTest.pgn.gz", true);
        
		GameModel gameModel = pgnReader.parseGame();
		for (;;) {
			try {
				Game game = new Game(gameModel);
				gameModel = pgnReader.parseGame();
				if (gameModel == null) break;
				game.insertGameModel(gameModel, true, true);				
				pgnWriter.write(game.getModel());
				writeln();
			} catch (Exception ex) {
				writeln(ex.getMessage());
			}
		}
		
		stopFootprint();
	}
}