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
 * $Id: FENTests.java,v 1.1 2002/12/08 13:27:28 BerniMan Exp $
 */

package chesspresso.position;

import chesspresso.Chess;
import junit.framework.*;

/**
 * Tests for the FEN class.
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class FENTests extends TestCase
{
    
    public static Test suite()
    {
        return new TestSuite(FENTests.class);
    }
    
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    //======================================================================
    
    public static void testChars()
    {
        for (int stone=Chess.MIN_STONE; stone<Chess.MAX_PIECE - 1; stone++) {
            if (stone != Chess.NO_STONE) {
                assertEquals("stone changed", stone, FEN.fenCharToStone(FEN.stoneToFenChar(stone)));
            }
        }
    }
    
}